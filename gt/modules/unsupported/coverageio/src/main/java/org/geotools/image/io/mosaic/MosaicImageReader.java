/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, GeoTools Project Managment Committee (PMC)
 *    (C) 2007, Geomatys
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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;

import org.geotools.image.io.metadata.MetadataMerge;
import org.geotools.resources.Classes;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.util.FrequencySortedSet;


/**
 * An image reader built from a mosaic of other image readers.
 *
 * @since 2.5
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class MosaicImageReader extends ImageReader {
    /**
     * The tile collections.
     */
    private TileCollection tiles;

    /**
     * Constructs an image reader with the specified provider.
     */
    public MosaicImageReader(final ImageReaderSpi spi) {
        super(spi);
    }

    /**
     * Sets the input source. The input should be an array or a {@linkplain Collection collection}
     * of {@link Tile} objects.
     */
    @Override
    public void setInput(final Object  input,
                         final boolean seekForwardOnly,
                         final boolean ignoreMetadata)
    {
        /*
         * Closes previous streams, if any. This is not a big deal if this operation fails,
         * since we will not use anymore the old streams anyway. However it is worth to log.
         */
        try {
            close();
        } catch (IOException exception) {
            Tile.recoverableException(MosaicImageReader.class, "setInput", exception);
        }
        /*
         * Gets the new collection of tiles.
         */
        tiles = null;
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        if (input == null) {
            return;
        }
        final Tile[] inputTiles;
        if (input instanceof Tile[]) {
            inputTiles = ((Tile[]) input).clone();
        } else if (input instanceof Collection) {
            final Collection<?> c = (Collection<?>) input;
            inputTiles = c.toArray(new Tile[c.size()]);
        } else {
            throw new IllegalStateException(Errors.format(ErrorKeys.ILLEGAL_CLASS_$2,
                    Classes.getClass(input), Tile[].class));
        }
        tiles = new TileCollection(inputTiles);
        /*
         * Copies the configuration to every readers in the tile collection.
         */
        if (locale != null) {
            for (final ImageReader reader : tiles.getReaders()) {
                try {
                    reader.setLocale(locale);
                } catch (IllegalArgumentException e) {
                    // Invalid locale. Ignore this exception since it will not prevent the image
                    // reader to work mostly as expected (warning messages may be in a different
                    // locale, which is not a big deal).
                    Tile.recoverableException(MosaicImageReader.class, "setInput", e);
                }
            }
        }
    }

    /**
     * Returns the tiles collection, making sure that it is set.
     */
    private TileCollection getTileCollection() throws IOException {
        if (tiles == null) {
            throw new IllegalStateException(Errors.format(ErrorKeys.NO_IMAGE_INPUT));
        }
        return tiles;
    }

    /**
     * Returns an array of locales that may be used to localize warning listeners. The default
     * implementations returns the union of the locales supported by this reader and every
     * {@linkplain Tile#getReader tile readers}.
     *
     * @return An array of supported locales, or {@code null}.
     */
    @Override
    public Locale[] getAvailableLocales() {
        if (tiles == null) {
            return super.getAvailableLocales();
        }
        final Set<Locale> locales = new LinkedHashSet<Locale>();
        if (availableLocales != null) {
            for (final Locale locale : availableLocales) {
                locales.add(locale);
            }
        }
        for (final ImageReader reader : tiles.getReaders()) {
            final Locale[] additional = reader.getAvailableLocales();
            if (additional != null) {
                for (final Locale locale : additional) {
                    locales.add(locale);
                }
            }
        }
        return locales.toArray(new Locale[locales.size()]);
    }

    /**
     * Sets the current locale of this image reader and every
     * {@linkplain Tile#getReader tile readers}.
     *
     * @param locale the desired locale, or {@code null}.
     * @throws IllegalArgumentException if {@code locale} is non-null but is not
     *         one of the {@linkplain #getAvailableLocales available locales}.
     */
    @Override
    public void setLocale(final Locale locale) throws IllegalArgumentException {
        super.setLocale(locale); // May thrown an exception.
        if (tiles != null) {
            for (final ImageReader reader : tiles.getReaders()) {
                try {
                    reader.setLocale(locale);
                } catch (IllegalArgumentException e) {
                    // Locale not supported by the reader. It may occurs
                    // if not all readers support the same set of locales.
                    Tile.recoverableException(MosaicImageReader.class, "setLocale", e);
                }
            }
        }
    }

    /**
     * Returns the number of images, not including thumbnails. The default implementation
     * returns the maximal {@linkplain Tile#getImageIndex image index} in tiles plus 1.
     *
     * @throws IOException If an error occurs reading the information from the input source.
     */
    public int getNumImages(final boolean allowSearch) throws IOException {
        return getTileCollection().getNumImages();
    }

    /**
     * Returns {@code true} if there is more than one tile for the given image index.
     *
     * @throws IOException If an error occurs reading the information from the input source.
     */
    @Override
    public boolean isImageTiled(final int imageIndex) throws IOException {
        return (tiles != null) ? tiles.isImageTiled(imageIndex) : super.isImageTiled(imageIndex);
    }

    /**
     * Returns the width in pixels of the given image within the input source.
     *
     * @param  imageIndex The index of the image to be queried.
     * @return The width of the image.
     * @throws IOException If an error occurs reading the information from the input source.
     */
    public int getWidth(final int imageIndex) throws IOException {
        return getTileCollection().getArea(imageIndex).width;
    }

    /**
     * Returns the height in pixels of the given image within the input source.
     *
     * @param  imageIndex The index of the image to be queried.
     * @return The height of the image.
     * @throws IOException If an error occurs reading the information from the input source.
     */
    public int getHeight(final int imageIndex) throws IOException {
        return getTileCollection().getArea(imageIndex).height;
    }

    /**
     * Returns the width of a tile in the given image.
     *
     * @param  imageIndex The index of the image to be queried.
     * @return The width of a tile.
     * @throws IOException If an error occurs reading the information from the input source.
     */
    @Override
    public int getTileWidth(final int imageIndex) throws IOException {
        return (tiles != null) ? tiles.getTileSize(imageIndex).width : super.getTileWidth(imageIndex);
    }

    /**
     * Returns the height of a tile in the given image.
     *
     * @param  imageIndex The index of the image to be queried.
     * @return The height of a tile.
     * @throws IOException If an error occurs reading the information from the input source.
     */
    @Override
    public int getTileHeight(final int imageIndex) throws IOException {
        return (tiles != null) ? tiles.getTileSize(imageIndex).height : super.getTileHeight(imageIndex);
    }

    /**
     * Returns {@code true} if the storage format of the given image places no inherent impediment
     * on random access to pixels. The default implementation returns {@code true} if the input of
     * every tiles is a {@link File} and {@code isRandomAccessEasy} returned {@code true} for all
     * tile readers.
     *
     * @throws IOException If an error occurs reading the information from the input source.
     */
    @Override
    public boolean isRandomAccessEasy(final int imageIndex) throws IOException {
        if (tiles == null) {
            return super.isRandomAccessEasy(imageIndex);
        }
        for (final Tile tile : tiles.getTiles(imageIndex, null)) {
            final Object input = tile.getInput();
            if (!(input instanceof File)) {
                return false;
            }
            if (!tile.getPreparedReader(true, true).isRandomAccessEasy(imageIndex)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns an image type which most closely represents the "raw" internal format of the
     * image. The default implementation invokes {@code getRawImageType} for every tile readers,
     * ommits the types that are not declared in <code>{@linkplain ImageReader#getImageTypes
     * getImageTypes}(imageIndex)</code> for every tile readers, and returns the most common
     * remainding value.
     *
     * @throws IOException If an error occurs reading the information from the input source.
     */
    @Override
    public ImageTypeSpecifier getRawImageType(final int imageIndex) throws IOException {
        if (tiles != null) {
            final Set<ImageTypeSpecifier> allowed = getImageTypeSet(imageIndex);
            // Gets the list of every raw image types, with the most frequent type first.
            final Set<ImageTypeSpecifier> types = new FrequencySortedSet<ImageTypeSpecifier>(true);
            for (final Tile tile : tiles.getTiles(imageIndex, null)) {
                final ImageReader reader = tile.getPreparedReader(true, true);
                final Iterator<ImageTypeSpecifier> it = reader.getImageTypes(imageIndex);
                while (it.hasNext()) {
                    final ImageTypeSpecifier type = it.next();
                    if (allowed.contains(type)) {
                        types.add(type);
                    }
                }
            }
            final Iterator<ImageTypeSpecifier> it = types.iterator();
            if (it.hasNext()) {
                final ImageTypeSpecifier type = it.next();
                if (it.hasNext()) {
                    // TODO: log some low-level warnings here.
                }
                return type;
            }
        }
        return super.getRawImageType(imageIndex);
    }

    /**
     * Returns the possible image types to which the given image may be decoded. This method
     * invokes <code>{@linkplain ImageReader#getImageTypes getImageTypes}(imageIndex)</code>
     * on every tile readers and returns the intersection of all sets (i.e. only the types
     * that are supported by every readers).
     *
     * @throws IOException If an error occurs reading the information from the input source.
     */
    private Set<ImageTypeSpecifier> getImageTypeSet(final int imageIndex) throws IOException {
        int pass = 0;
        final Map<ImageTypeSpecifier,Integer> types = new LinkedHashMap<ImageTypeSpecifier,Integer>();
        for (final Tile tile : getTileCollection().getTiles(imageIndex, null)) {
            final ImageReader reader = tile.getPreparedReader(true, true);
            final Iterator<ImageTypeSpecifier> toAdd = reader.getImageTypes(imageIndex);
            while (toAdd.hasNext()) {
                final ImageTypeSpecifier type = toAdd.next();
                final Integer old = types.put(type, pass);
                if (old == null && pass != 0) {
                    // Just added a type that did not exists in previous tiles, so remove it.
                    types.remove(type);
                }
            }
            // Remove all previous types not found in this pass.
            for (final Iterator<Integer> it=types.values().iterator(); it.hasNext();) {
                if (it.next().intValue() != pass) {
                    it.remove();
                }
            }
            pass++;
        }
        return types.keySet();
    }

    /**
     * Returns possible image types to which the given image may be decoded. Default implementation
     * invokes <code>{@linkplain ImageReader#getImageTypes getImageTypes}(imageIndex)</code> on
     * every tile readers and returns the intersection of all sets (i.e. only the types that are
     * supported by every readers).
     *
     * @throws IOException If an error occurs reading the information from the input source.
     */
    public Iterator<ImageTypeSpecifier> getImageTypes(final int imageIndex) throws IOException {
        return getImageTypeSet(imageIndex).iterator();
    }

    /**
     * Returns default parameters appropriate for this format. The default implementation tries
     * to delegate to an {@linkplain Tile#getReader tile image reader} which is an instance of
     * the most specialized class. We look for the most specialized subclass because it may
     * declares additional parameters that are ignored by super-classes. If we fail to find
     * a suitable instance, then the default parameters are returned.
     */
    @Override
    public ImageReadParam getDefaultReadParam() {
        if (tiles != null) {
            final ImageReader reader = tiles.getReader();
            if (reader != null) {
                return reader.getDefaultReadParam();
            }
        }
        return super.getDefaultReadParam();
    }

    /**
     * Returns the metadata associated with the input source as a whole, or {@code null}.
     * The default implementation tries to {@linkplain IIOMetadata#mergeTree merge} the
     * metadata from every tiles.
     *
     * @throws IOException if an error occurs during reading.
     */
    public IIOMetadata getStreamMetadata() throws IOException {
        IIOMetadata metadata = null;
        for (final Tile tile : getTileCollection().getTiles()) {
            final ImageReader reader = tile.getPreparedReader(true, ignoreMetadata);
            final IIOMetadata candidate = reader.getStreamMetadata();
            metadata = MetadataMerge.merge(candidate, metadata);
        }
        return metadata;
    }

    /**
     * Returns the stream metadata for the given format and nodes, or {@code null}.
     * The default implementation tries to {@linkplain IIOMetadata#mergeTree merge}
     * the metadata from every tiles.
     *
     * @throws IOException if an error occurs during reading.
     */
    @Override
    public IIOMetadata getStreamMetadata(final String formatName, final Set<String> nodeNames)
            throws IOException
    {
        IIOMetadata metadata = null;
        for (final Tile tile : getTileCollection().getTiles()) {
            final ImageReader reader = tile.getPreparedReader(true, ignoreMetadata);
            final IIOMetadata candidate = reader.getStreamMetadata(formatName, nodeNames);
            metadata = MetadataMerge.merge(candidate, metadata);
        }
        return metadata;
    }

    /**
     * Returns the metadata associated with the given image, or {@code null}. The
     * default implementation tries to {@linkplain IIOMetadata#mergeTree merge}
     * the metadata from every tiles.
     *
     * @param  imageIndex the index of the image whose metadata is to be retrieved.
     * @return The metadata, or {@code null}.
     * @throws IllegalStateException if the input source has not been set.
     * @throws IndexOutOfBoundsException if the supplied index is out of bounds.
     * @throws IOException if an error occurs during reading.
     */
    public IIOMetadata getImageMetadata(final int imageIndex) throws IOException {
        IIOMetadata metadata = null;
        for (final Tile tile : getTileCollection().getTiles(imageIndex, null)) {
            final ImageReader reader = tile.getPreparedReader(true, ignoreMetadata);
            final IIOMetadata candidate = reader.getImageMetadata(imageIndex);
            metadata = MetadataMerge.merge(candidate, metadata);
        }
        return metadata;
    }

    /**
     * Returns the image metadata for the given format and nodes, or {@code null}.
     * The default implementation tries to {@linkplain IIOMetadata#mergeTree merge}
     * the metadata from every tiles.
     *
     * @throws IOException if an error occurs during reading.
     */
    @Override
    public IIOMetadata getImageMetadata(final int imageIndex,
            final String formatName, final Set<String> nodeNames) throws IOException
    {
        IIOMetadata metadata = null;
        for (final Tile tile : getTileCollection().getTiles(imageIndex, null)) {
            final ImageReader reader = tile.getPreparedReader(true, ignoreMetadata);
            final IIOMetadata candidate = reader.getImageMetadata(imageIndex, formatName, nodeNames);
            metadata = MetadataMerge.merge(candidate, metadata);
        }
        return metadata;
    }

    /**
     * Reads the image indexed by {@code imageIndex} using a supplied parameters.
     *
     * @param  imageIndex The index of the image to be retrieved.
     * @param  param The parameters used to control the reading process, or {@code null}.
     * @return The desired portion of the image.
     * @throws IOException if an error occurs during reading.
     */
    public BufferedImage read(final int imageIndex, final ImageReadParam param) throws IOException {
        clearAbortRequest();
        final int[]      sourceBands;
        final int[] destinationBands;
        final int sourceXSubsampling;
        final int sourceYSubsampling;
        final int subsamplingXOffset;
        final int subsamplingYOffset;
        final int destinationXOffset;
        final int destinationYOffset;
        if (param != null) {
            sourceBands        = param.getSourceBands();
            destinationBands   = param.getDestinationBands();
            final Point offset = param.getDestinationOffset();
            sourceXSubsampling = param.getSourceXSubsampling();
            sourceYSubsampling = param.getSourceYSubsampling();
            subsamplingXOffset = param.getSubsamplingXOffset();
            subsamplingYOffset = param.getSubsamplingYOffset();
            destinationXOffset = offset.x;
            destinationYOffset = offset.y;
        } else {
            sourceBands        = null;
            destinationBands   = null;
            sourceXSubsampling = 1;
            sourceYSubsampling = 1;
            subsamplingXOffset = 0;
            subsamplingYOffset = 0;
            destinationXOffset = 0;
            destinationYOffset = 0;
        }
        final int width  = getWidth (imageIndex);
        final int height = getHeight(imageIndex);
        final Rectangle sourceRegion = getSourceRegion(param, width, height);
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Closes any image input streams thay may be held by tiles.
     * The streams will be opened again when they will be first needed.
     *
     * @throws IOException if error occured while closing a stream.
     */
    public void close() throws IOException {
        if (tiles != null) {
            tiles.close(false);
        }
    }

    /**
     * Allows any resources held by this reader to be released. The default implementation
     * closes any image input streams thay may be held by tiles, then disposes every
     * {@linkplain Tile#getReader tile image readers}.
     */
    @Override
    public void dispose() {
        if (tiles != null) try {
            tiles.close(true);
        } catch (IOException e) {
            Tile.recoverableException(MosaicImageReader.class, "dispose", e);
        }
        tiles = null;
        super.dispose();
    }
}
