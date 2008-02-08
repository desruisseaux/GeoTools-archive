/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2008, GeoTools Project Managment Committee (PMC)
 *    (C) 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.image.io.mosaic;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.geotools.resources.XArray;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;


/**
 * An image writer that delegates to a mosaic of other image writers. The mosaic is specified as a
 * collection of {@link Tile} objects given to {@link #setOutput(Object)} method. While this class
 * can write a {@link RenderedImage}, the preferred approach is to invoke {@link #writeFromInput}
 * with a {@link File} input in argument. This approach is non-standard but often required since
 * images to mosaic are typically bigger than {@link RenderedImage} capacity.
 *
 * @since 2.5
 * @source $URL$
 * @version $Id$
 * @author Cédric Briançon
 * @author Martin Desruisseaux
 */
public class MosaicImageWriter extends ImageWriter {
    /**
     * Constructs an image writer with the default provider.
     */
    public MosaicImageWriter() {
        this(null);
    }

    /**
     * Constructs an image writer with the specified provider.
     */
    public MosaicImageWriter(final ImageWriterSpi spi) {
        super(spi != null ? spi : Spi.DEFAULT);
    }

    /**
     * Returns the output, which is a an array of {@linkplain TileManager tile managers}.
     * The array length is the maximum number of images that can be inserted. The element
     * at index <var>i</var> is the tile manager to use when writing at image index <var>i</var>.
     */
    @Override
    public TileManager[] getOutput() {
        final TileManager[] managers = (TileManager[]) super.getOutput();
        return (managers != null) ? managers.clone() : null;
    }

    /**
     * Sets the output, which is expected to be an array of {@linkplain TileManager tile managers}.
     * If the given input is a singleton, an array or a {@linkplain Collection collection} of
     * {@link Tile} objects, then it will be wrapped in an array of {@link TileManager}s.
     *
     * @param output The output.
     */
    @Override
    public void setOutput(final Object output) {
        super.setOutput(TileManagerFactory.DEFAULT.createFromObject(output));
    }

    /**
     * Writes the specified image as a set of tiles. The default implementation copies the image in
     * a temporary file, then invokes {@link #writeFromInput}. This somewhat inefficient approach
     * may be changed in a future version.
     *
     * @param  metadata The stream metadata.
     * @param  image    The image to write.
     * @param  param    The parameter for the image to write.
     * @throws IOException if an error occured while writing the image.
     */
    public void write(final IIOMetadata metadata, final IIOImage image, final ImageWriteParam param)
            throws IOException
    {
        /*
         * We could check for 'output' before to create the temporary file in order to avoid
         * creating the file if we are going to fail anyway,  but we don't because users are
         * allowed to override the 'filter' methods and set the output there (undocumented
         * but possible, and TileBuilder do something like that).
         *
         * Uses the PNG format, which is lossless and bundled in standard Java distributions.
         */
        final Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
        while (writers.hasNext()) {
            final ImageWriter writer = writers.next();
            if (!filter(writer)) {
                continue;
            }
            final File file = File.createTempFile("MIW", ".png");
            try {
                final ImageOutputStream output = ImageIO.createImageOutputStream(file);
                writer.setOutput(output);
                writer.write(metadata, image, param);
                output.close();
                writeFromInput(file, 0, 0);
            } finally {
                file.delete();
            }
            return;
        }
        throw new IIOException(Errors.format(ErrorKeys.NO_IMAGE_WRITER));
    }

    /**
     * Reads the image from the given input and writes it as a set of tiles. The input is typically
     * a {@link File} object, but other kind of inputs may be accepted depending on available image
     * readers. The output files and tiling layout can be specified as a collection of {@link Tile}
     * objects given to {@link #setOutput(Object)} method.
     *
     * @param  input The image input, typically as a {@link File}.
     * @param  inputIndex The image index to read from the given input file.
     * @param  outputIndex The output image index, which is the index of the
     *         {@linkplain TileManager tile manager} to use in the array returned by
     *         {@link #getOutput}.
     * @throws IOException If an error occured while reading or writing.
     */
    public void writeFromInput(final Object input, final int inputIndex, final int outputIndex)
            throws IOException
    {
        /*
         * Gets the reader first - especially before getOutput() - because the user may have
         * overriden filter(ImageReader) and set the output accordingly. TileBuilder do that.
         */
        final ImageReader     reader = getImageReader(input);
        final ImageReadParam  params = reader.getDefaultReadParam();
        final TileManager[] managers = getOutput();
        if (managers == null) {
            throw new IllegalStateException(Errors.format(ErrorKeys.NO_IMAGE_OUTPUT));
        }
        final Collection<Tile> tiles = managers[outputIndex].getTiles();
        for (final Tile tile : tiles) {
            final Object       tileInput = tile.getInput();
            final Rectangle sourceRegion = tile.getAbsoluteRegion();
            final Dimension  subSampling = tile.getSubsampling();
            params.setSourceRegion(sourceRegion);
            params.setSourceSubsampling(subSampling.width, subSampling.height, 0, 0);
            final RenderedImage image = reader.readAsRenderedImage(inputIndex, params);
            final ImageWriter writer = getImageWriter(tile, image);
            writer.write(image);
            close(writer.getOutput(), tileInput);
            writer.dispose();
        }
        close(reader.getInput(), input);
        reader.dispose();
    }

    /**
     * Closes the given stream if it is different than the user object. When different, the
     * output is not the {@link File} (or whatever object) given by {@link Tile}. It is probably
     * an {@link ImageOutputStream} created by {@link #getImageWriter}, so we need to close it.
     */
    private static void close(final Object stream, final Object user) throws IOException {
        if (stream != user) {
            if (stream instanceof Closeable) {
                ((Closeable) stream).close();
            } else if (stream instanceof ImageInputStream) {
                ((ImageInputStream) stream).close();
            }
            // Note: ImageOutputStream extends ImageInputStream, so the above check is suffisient.
        }
    }

    /**
     * Invoked after {@code MosaicImageWriter} has created a reader and
     * {@linkplain ImageReader#setInput(Object) set the input}. Users can override this method
     * for performing additional configuration and may returns {@code false} if the given reader
     * is not suitable. The default implementation returns {@code true} in all case.
     *
     * @param  reader The image reader created and configured by {@code MosaicImageWriter}.
     * @return {@code true} If the given reader is ready for use, or {@code false} if an other
     *         reader should be fetched.
     * @throws IOException if an error occured while inspecting or configuring the reader.
     */
    protected boolean filter(final ImageReader reader) throws IOException {
        return true;
    }

    /**
     * Invoked after {@code MosaicImageWriter} has created a writer and
     * {@linkplain ImageWriter#setOutput(Object) set the output}. Users can override this method
     * for performing additional configuration and may returns {@code false} if the given writer
     * is not suitable. The default implementation returns {@code true} in all case.
     *
     * @param  writer The image writer created and configured by {@code MosaicImageWriter}.
     * @return {@code true} If the given writer is ready for use, or {@code false} if an other
     *         writer should be fetched.
     * @throws IOException if an error occured while inspecting or configuring the writer.
     */
    protected boolean filter(final ImageWriter writer) throws IOException {
        return true;
    }

    /**
     * Gets and initializes an {@linkplain ImageReader image reader} that can decode the specified
     * input. The returned reader has its {@linkplain ImageReader#setInput input} already set. If
     * the reader input is different than the specified one, then it is probably an {@linkplain
     * ImageInputStream image input stream} and closing it is caller's responsability.
     *
     * @param  input The input to read.
     * @return The image reader that seems to be the most appropriated (never {@code null}).
     * @throws IOException If no suitable image reader has been found or if an error occured
     *         while creating an image reader or initiazing it.
     */
    private ImageReader getImageReader(final Object input) throws IOException {
        ImageInputStream stream = null;
        boolean createStream = false;
        /*
         * The following loop will be executed at most twice. The first iteration tries the given
         * input directly. The second iteration tries the input wrapped in an ImageInputStream.
         */
        do {
            final Object candidate;
            if (createStream) {
                stream = ImageIO.createImageInputStream(input);
                if (stream == null) {
                    continue;
                }
                candidate = stream;
            } else {
                candidate = input;
            }
            final Iterator<ImageReader> readers = ImageIO.getImageReaders(candidate);
            while (readers.hasNext()) {
                final ImageReader reader = readers.next();
                reader.setInput(candidate);
                // If there is any more advanced check to perform, we should do it here.
                // For now we accept the reader inconditionnaly.
                if (filter(reader)) {
                    return reader;
                }
                reader.dispose();
            }
        } while ((createStream = !createStream) == true);
        if (stream != null) {
            stream.close();
        }
        throw new IIOException(Errors.format(ErrorKeys.NO_IMAGE_READER));
    }

    /**
     * Gets and initializes an {@linkplain ImageWriter image writer} that can encode the specified
     * image. The returned writer has its {@linkplain ImageWriter#setOutput output} already set.
     * If the output is different than the {@linkplain Tile#getInput tile input}, then it is
     * probably an {@linkplain ImageOutputStream image output stream} and closing it is caller's
     * responsability.
     *
     * @param  tile The tile to encode.
     * @param  image The image associated to the specified tile.
     * @return The image writer that seems to be the most appropriated (never {@code null}).
     * @throws IOException If no suitable image writer has been found or if an error occured
     *         while creating an image writer or initiazing it.
     */
    private ImageWriter getImageWriter(final Tile tile, final RenderedImage image)
            throws IOException
    {
        // Note: we rename "Tile.input" as "output" because we want to write in it.
        final Object         output      = tile.getInput();
        final Class<?>       outputType  = output.getClass();
        final ImageReaderSpi readerSpi   = tile.getImageReaderSpi();
        final String[]       formatNames = readerSpi.getFormatNames();
        final String[]       spiNames    = readerSpi.getImageWriterSpiNames();
        ImageOutputStream    stream      = null; // Created only if needed.
        /*
         * The search will be performed at most twice. In the first try (code below) we check the
         * plugins specified in 'spiNames' since we assume that they will encode the image in the
         * best suited format for the reader. In the second try (to be run if the first one found
         * no suitable writer), we look for providers by their name.
         */
        if (spiNames != null) {
            final IIORegistry registry = IIORegistry.getDefaultInstance();
            final ImageWriterSpi[] providers = new ImageWriterSpi[spiNames.length];
            int count = 0;
            for (final String name : spiNames) {
                final Class<?> spiType;
                try {
                    spiType = Class.forName(name);
                } catch (ClassNotFoundException e) {
                    // May be normal.
                    continue;
                }
                /*
                 * For each image writers, checks if at least one format name is an expected one
                 * and check if the writer can encode the image. If a suitable writter is found
                 * and is capable to encode the output, returns it immediately. Otherwise the
                 * writers are stored in an array as we found it, in order to try them again with
                 * an ImageOutputStream after this loop.
                 */
                final Object candidate = registry.getServiceProviderByClass(spiType);
                if (candidate instanceof ImageWriterSpi) {
                    final ImageWriterSpi spi = (ImageWriterSpi) candidate;
                    final String[] names = spi.getFormatNames();
                    if (XArray.intersects(formatNames, names) && spi.canEncodeImage(image)) {
                        providers[count++] = spi;
                        for (final Class<?> legalType : spi.getOutputTypes()) {
                            if (legalType.isAssignableFrom(outputType)) {
                                final ImageWriter writer = spi.createWriterInstance();
                                writer.setOutput(output);
                                if (filter(writer)) {
                                    return writer;
                                }
                                writer.dispose();
                                break;
                            }
                        }
                    }
                }
            }
            /*
             * No provider accepts the output directly. This output is typically a File or URL.
             * Creates an image output stream from it and try again.
             */
            if (count != 0) {
                stream = ImageIO.createImageOutputStream(output);
                if (stream != null) {
                    final Class<? extends ImageOutputStream> streamType = stream.getClass();
                    for (int i=0; i<count; i++) {
                        final ImageWriterSpi spi = providers[i];
                        for (final Class<?> legalType : spi.getOutputTypes()) {
                            if (legalType.isAssignableFrom(streamType)) {
                                final ImageWriter writer = spi.createWriterInstance();
                                writer.setOutput(stream);
                                if (filter(writer)) {
                                    return writer;
                                }
                                writer.dispose();
                                break;
                            }
                        }
                    }
                }
            }
        }
        /*
         * No suitable writer found from 'spiNames'. Try again using format name.
         * At the difference of the previous try, this one works on ImageWriters
         * instead of ImageWriterSpi instances.
         */
        for (final String name : formatNames) {
            final List<ImageWriter> writers = new ArrayList<ImageWriter>();
            final Iterator<ImageWriter> it = ImageIO.getImageWritersByFormatName(name);
            while (it.hasNext()) {
                final ImageWriter writer = it.next();
                final ImageWriterSpi spi = writer.getOriginatingProvider();
                if (spi == null || !spi.canEncodeImage(image)) {
                    writer.dispose();
                    continue;
                }
                writers.add(writer);
                for (final Class<?> legalType : spi.getOutputTypes()) {
                    if (legalType.isAssignableFrom(outputType)) {
                        writer.setOutput(output);
                        if (filter(writer)) {
                            return writer;
                        }
                        // Do not dispose the writer since we will try it again later.
                        break;
                    }
                }
            }
            if (!writers.isEmpty()) {
                if (stream == null) {
                    stream = ImageIO.createImageOutputStream(output);
                    if (stream == null) {
                        break;
                    }
                }
                final Class<? extends ImageOutputStream> streamType = stream.getClass();
                for (final ImageWriter writer : writers) {
                    final ImageWriterSpi spi = writer.getOriginatingProvider();
                    for (final Class<?> legalType : spi.getOutputTypes()) {
                        if (legalType.isAssignableFrom(streamType)) {
                            writer.setOutput(stream);
                            if (filter(writer)) {
                                return writer;
                            }
                            break;
                        }
                    }
                    writer.dispose();
                }
            }
        }
        if (stream != null) {
            stream.close();
        }
        throw new IIOException(Errors.format(ErrorKeys.NO_IMAGE_WRITER));
    }

    /**
     * Returns the default stream metadata, or {@code null} if none.
     * The default implementation returns {@code null} in all cases.
     */
    public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {
        return null;
    }

    /**
     * Returns the default image metadata, or {@code null} if none.
     * The default implementation returns {@code null} in all cases.
     */
    public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType, ImageWriteParam param) {
        return null;
    }

    /**
     * Returns stream metadata initialized to the specified state, or {@code null}.
     * The default implementation returns {@code null} in all cases since this plugin
     * doesn't provide metadata encoding capabilities.
     */
    public IIOMetadata convertStreamMetadata(IIOMetadata inData, ImageWriteParam param) {
        return null;
    }

    /**
     * Returns image metadata initialized to the specified state, or {@code null}.
     * The default implementation returns {@code null} in all cases since this plugin
     * doesn't provide metadata encoding capabilities.
     */
    public IIOMetadata convertImageMetadata(IIOMetadata inData, ImageTypeSpecifier imageType, ImageWriteParam param) {
        return null;
    }

    /**
     * Service provider for {@link MosaicImageWriter}.
     *
     * @since 2.5
     * @source $URL$
     * @version $Id$
     * @author Cédric Briançon
     * @author Martin Desruisseaux
     */
    public static class Spi extends ImageWriterSpi {
        /**
         * The default instance.
         */
        public static final Spi DEFAULT = new Spi();

        /**
         * Creates a default provider.
         */
        public Spi() {
            vendorName      = "GeoTools";
            version         = "1.0";
            names           = MosaicImageReader.Spi.NAMES;
            outputTypes     = MosaicImageReader.Spi.INPUT_TYPES;
            pluginClassName = "org.geotools.image.io.mosaic.MosaicImageWriter";
        }

        /**
         * Returns {@code true} if this writer is likely to be able to encode images with the given
         * layout. The default implementation returns {@code true} in all cases. The capability to
         * encode images depends on the tile format specified in {@link Tile} objects, which are
         * not known to this provider.
         */
        public boolean canEncodeImage(ImageTypeSpecifier type) {
            return true;
        }

        /**
         * Returns a new {@link MosaicImageWriter}.
         */
        public ImageWriter createWriterInstance(Object extension) throws IOException {
            return new MosaicImageWriter(this);
        }

        /**
         * Returns a brief, human-readable description of this service provider.
         *
         * @todo Localize.
         */
        public String getDescription(final Locale locale) {
            return "Mosaic Image Writer";
        }
    }
}
