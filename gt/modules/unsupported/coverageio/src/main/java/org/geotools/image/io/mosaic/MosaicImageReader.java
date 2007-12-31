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
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
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
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;

import org.geotools.image.io.metadata.MetadataMerge;
import org.geotools.io.TableWriter;
import org.geotools.resources.Classes;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.util.FrequencySortedSet;
import org.geotools.util.logging.Logging;


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
     * {@code true} for disabling operations that may corrupt data values,
     * or {@code false} if only the visual effect matter.
     */
    private static final boolean PRESERVE_DATA = true;

    /**
     * Type arguments made of a single {@code int} value.
     */
    private static final Class<?>[] INTEGER_ARGUMENTS = {
        int.class
    };

    /**
     * The logging level for tiling information during reads.
     */
    private Level level = Level.FINE;

    /**
     * The tile manager.
     */
    private TileManager tiles;

    /**
     * The reader currently under process of reading, or {@code null} if none.
     * Used by {@link #abort} only.
     */
    private transient ImageReader reading;

    /**
     * Constructs an image reader with the specified provider.
     */
    public MosaicImageReader(final ImageReaderSpi spi) {
        super(spi != null ? spi : Spi.DEFAULT);
    }

    /**
     * Sets the input source. This method expects an array or a {@linkplain Collection collection}
     * of {@link Tile} objects.
     */
    @Override
    public void setInput(Object input, final boolean seekForwardOnly, final boolean ignoreMetadata) {
        /*
         * Closes previous streams, if any. This is not a big deal if this operation fails,
         * since we will not use anymore the old streams anyway. However it is worth to log.
         */
        try {
            close();
        } catch (IOException exception) {
            Logging.recoverableException(MosaicImageReader.class, "setInput", exception);
        }
        /*
         * Gets the new collection of tiles. We set the superclass input to the collection returned
         * by the TileManager, not the TileManager itself, in order to be consistent with the input
         * given by the user to this method. We do not store directly the given input neither in
         * order to protect it from changes.
         */
        this.tiles = null;
        this.input = null; // Clears first in case of failure.
        if (input != null) {
            if (input instanceof Tile[]) {
                tiles = new TileManager((Tile[]) input);
            } else if (input instanceof Collection) {
                @SuppressWarnings("unchecked") // TileManager constructor will checks indirectly.
                final Collection<Tile> c = (Collection<Tile>) input;
                tiles = new TileManager(c);
            } else {
                throw new IllegalStateException(Errors.format(ErrorKeys.ILLEGAL_CLASS_$2,
                        Classes.getClass(input), Tile[].class));
            }
            input = tiles.getTiles();
        }
        super.setInput(input, seekForwardOnly, ignoreMetadata);
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
                    Logging.recoverableException(MosaicImageReader.class, "setInput", e);
                }
            }
        }
    }

    /**
     * Returns the tiles manager, making sure that it is set.
     *
     * @param imageIndex The image index, from 0 inclusive to {@link #getNumImages} exclusive.
     * @return The tile manager for image at the given index.
     *
     * @todo Current implementation has only one tile manager because we allow only one image,
     *       but we could allow for more in a future version.
     */
    private TileManager getTileManager(final int imageIndex) throws IOException {
        if (tiles == null) {
            throw new IllegalStateException(Errors.format(ErrorKeys.NO_IMAGE_INPUT));
        }
        final int numImages = getNumImages(false);
        if (imageIndex < 0 || (numImages >= 0 && imageIndex >= numImages)) {
            throw new IndexOutOfBoundsException(Errors.format(
                    ErrorKeys.INDEX_OUT_OF_BOUNDS_$1, imageIndex));
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
                    Logging.recoverableException(MosaicImageReader.class, "setLocale", e);
                }
            }
        }
    }

    /**
     * Returns the number of images, not including thumbnails.
     *
     * @throws IOException If an error occurs reading the information from the input source.
     */
    public int getNumImages(final boolean allowSearch) throws IOException {
        return 1;
    }

    /**
     * Returns {@code true} if there is more than one tile for the given image index.
     *
     * @param  imageIndex The index of the image to be queried.
     * @return {@code true} If there is at least two tiles.
     * @throws IOException If an error occurs reading the information from the input source.
     */
    @Override
    public boolean isImageTiled(final int imageIndex) throws IOException {
        return getTileManager(imageIndex).isImageTiled();
    }

    /**
     * Returns the width in pixels of the given image within the input source.
     *
     * @param  imageIndex The index of the image to be queried.
     * @return The width of the image.
     * @throws IOException If an error occurs reading the information from the input source.
     */
    public int getWidth(final int imageIndex) throws IOException {
        return getTileManager(imageIndex).getRegion().width;
    }

    /**
     * Returns the height in pixels of the given image within the input source.
     *
     * @param  imageIndex The index of the image to be queried.
     * @return The height of the image.
     * @throws IOException If an error occurs reading the information from the input source.
     */
    public int getHeight(final int imageIndex) throws IOException {
        return getTileManager(imageIndex).getRegion().height;
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
        return getTileManager(imageIndex).getTileSize().width;
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
        return getTileManager(imageIndex).getTileSize().height;
    }

    /**
     * Returns {@code true} if every image reader uses the default implementation for the given
     * method. Some methods may avoid costly file seeking when this method returns {@code true}.
     * <p>
     * This method always returns {@code true} if there is no tiles.
     */
    private boolean useDefaultImplementation(final String methodName, final Class<?>[] parameterTypes) {
        if (tiles != null) {
            for (final ImageReader reader : tiles.getReaders()) {
                Class<?> type = reader.getClass();
                try {
                    type = type.getMethod(methodName, parameterTypes).getDeclaringClass();
                } catch (NoSuchMethodException e) {
                    Logging.unexpectedException(MosaicImageReader.class, "useDefaultImplementation", e);
                    return false; // Conservative value.
                }
                if (!type.equals(ImageReader.class)) {
                    return false;
                }
            }
        }
        return true;
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
        if (useDefaultImplementation("isRandomAccessEasy", INTEGER_ARGUMENTS)) {
            return super.isRandomAccessEasy(imageIndex);
        }
        for (final Tile tile : getTileManager(imageIndex).getTiles()) {
            final Object input = tile.getInput();
            if (!(input instanceof File)) {
                return false;
            }
            final ImageReader reader = tile.getPreparedReader(true, true);
            if (!reader.isRandomAccessEasy(tile.getImageIndex())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the aspect ratio. If all tiles have the same aspect ratio, then that ratio is
     * returned. Otherwise the {@linkplain ImageReader#getAspectRatio default value} is returned.
     *
     * @param  imageIndex The index of the image to be queried.
     * @throws IOException If an error occurs reading the information from the input source.
     */
    @Override
    public float getAspectRatio(final int imageIndex) throws IOException {
        if (!useDefaultImplementation("getAspectRatio", INTEGER_ARGUMENTS)) {
            float ratio = Float.NaN;
            for (final Tile tile : getTileManager(imageIndex).getTiles()) {
                final ImageReader reader = tile.getPreparedReader(true, true);
                final float candidate = reader.getAspectRatio(tile.getImageIndex());
                if (candidate == ratio || Float.isNaN(candidate)) {
                    // Same ratio or unspecified ratio.
                    continue;
                }
                if (!Float.isNaN(ratio)) {
                    // The ratio is different for different tile. Fall back on default.
                    return super.getAspectRatio(imageIndex);
                }
                ratio = candidate;
            }
            if (!Float.isNaN(ratio)) {
                return ratio;
            }
        }
        return super.getAspectRatio(imageIndex);
    }

    /**
     * Returns an image type which most closely represents the "raw" internal format of the
     * image. The default implementation invokes {@code getRawImageType} for every tile readers,
     * ommits the types that are not declared in <code>{@linkplain ImageReader#getImageTypes
     * getImageTypes}(imageIndex)</code> for every tile readers, and returns the most common
     * remainding value. If none is found, then some {@linkplain ImageReader#getRawImageType
     * default specifier} is returned.
     *
     * @param  imageIndex The image index, from 0 inclusive to {@link #getNumImages} exclusive.
     * @return A raw image type specifier.
     * @throws IOException If an error occurs reading the information from the input source.
     */
    @Override
    public ImageTypeSpecifier getRawImageType(final int imageIndex) throws IOException {
        final ImageTypeSpecifier type = getRawImageType(getTileManager(imageIndex).getTiles());
        return (type != null) ? type : super.getRawImageType(imageIndex);
    }

    /**
     * Returns an image type which most closely represents the "raw" internal format of the
     * given set of tiles. If none is found, returns {@code null}.
     *
     * @param  tiles The tiles to iterate over.
     * @return A raw image type specifier acceptable for all tiles, or {@code null} if none.
     * @throws IOException If an error occurs reading the information from the input source.
     */
    private ImageTypeSpecifier getRawImageType(final Collection<Tile> tiles) throws IOException {
        // Gets the list of every raw image types, with the most frequent type first.
        final Set<ImageTypeSpecifier> rawTypes = new FrequencySortedSet<ImageTypeSpecifier>(true);
        final Set<ImageTypeSpecifier> allowed = getImageTypes(tiles, rawTypes);
        rawTypes.retainAll(allowed);
        Iterator<ImageTypeSpecifier> it = rawTypes.iterator();
        if (it.hasNext()) {
            final ImageTypeSpecifier type = it.next();
            if (it.hasNext()) {
                // TODO: log some low-level warnings here.
            }
            return type;
        }
        /*
         * No raw image reader type. Returns the first allowed type even if it is not "raw".
         */
        it = allowed.iterator();
        if (it.hasNext()) {
            // TODO: log some low-level warnings here.
            return it.next();
        }
        return null;
    }

    /**
     * Returns the possible image types to which the given image may be decoded. This method
     * invokes <code>{@linkplain ImageReader#getImageTypes getImageTypes}(imageIndex)</code>
     * on every tile readers and returns the intersection of all sets (i.e. only the types
     * that are supported by every readers).
     *
     * @param  tiles       The tiles to iterate over.
     * @param  rawTypes    If non-null, a collection where to store the raw image types.
     *                     No filtering is applied on this collection.
     * @return The image type specifiers that are common to all tiles.
     * @throws IOException If an error occurs reading the information from the input source.
     */
    private Set<ImageTypeSpecifier> getImageTypes(final Collection<Tile> tiles,
                                                  final Collection<ImageTypeSpecifier> rawTypes)
            throws IOException
    {
        int pass = 0;
        final Map<ImageTypeSpecifier,Integer> types = new LinkedHashMap<ImageTypeSpecifier,Integer>();
        for (final Tile tile : tiles) {
            final ImageReader reader = tile.getPreparedReader(true, true);
            final int imageIndex = tile.getImageIndex();
            if (rawTypes != null) {
                rawTypes.add(reader.getRawImageType(imageIndex));
            }
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
     * @param  imageIndex  The image index, from 0 inclusive to {@link #getNumImages} exclusive.
     * @return The image type specifiers that are common to all tiles.
     * @throws IOException If an error occurs reading the information from the input source.
     */
    public Iterator<ImageTypeSpecifier> getImageTypes(final int imageIndex) throws IOException {
        final Collection<Tile> tiles = getTileManager(imageIndex).getTiles();
        return getImageTypes(tiles, null).iterator();
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
        if (!useDefaultImplementation("getDefaultReadParam", null)) {
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
        if (tiles != null) {
            ImageReader previousReader = null;
            Object      previousInput  = null;
            /*
             * The tiles are supposed to be ordered with tiles using same reader and input first.
             * So checking if the (reader,input) pair is different from the previous iteration is
             * suffisient for avoiding querying the same stream twice.
             */
            for (final Tile tile : tiles.getTiles()) {
                final ImageReader reader = tile.getPreparedReader(true, ignoreMetadata);
                final Object input = reader.getInput();
                if (reader != previousReader || input != previousInput) {
                    final IIOMetadata candidate = reader.getStreamMetadata();
                    metadata = MetadataMerge.merge(candidate, metadata);
                    previousReader = reader;
                    previousInput  = input;
                }
            }
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
        if (tiles != null) {
            ImageReader previousReader = null;
            Object      previousInput  = null;
            // Same assumption and optimization than getStreamMetadata().
            for (final Tile tile : tiles.getTiles()) {
                final ImageReader reader = tile.getPreparedReader(true, ignoreMetadata);
                final Object input = reader.getInput();
                if (reader != previousReader || input != previousInput) {
                    final IIOMetadata candidate = reader.getStreamMetadata(formatName, nodeNames);
                    metadata = MetadataMerge.merge(candidate, metadata);
                    previousReader = reader;
                    previousInput  = input;
                }
            }
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
        for (final Tile tile : getTileManager(imageIndex).getTiles()) {
            final ImageReader reader = tile.getPreparedReader(true, ignoreMetadata);
            final IIOMetadata candidate = reader.getImageMetadata(tile.getImageIndex());
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
        for (final Tile tile : getTileManager(imageIndex).getTiles()) {
            final ImageReader reader = tile.getPreparedReader(true, ignoreMetadata);
            final IIOMetadata candidate = reader.getImageMetadata(tile.getImageIndex(), formatName, nodeNames);
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
    public BufferedImage read(final int imageIndex, ImageReadParam param) throws IOException {
        clearAbortRequest();
        final int xSubsampling, ySubsampling;
        if (param != null) {
            xSubsampling = param.getSourceXSubsampling();
            ySubsampling = param.getSourceYSubsampling();
            // Note: we don't extract subsampling offsets because they will be taken in account
            //       in the 'sourceRegion' to be calculated by ImageReader.computeRegions(...).
        } else {
            xSubsampling = 1;
            ySubsampling = 1;
        }
        final int srcWidth  = getWidth (imageIndex);
        final int srcHeight = getHeight(imageIndex);
        final Rectangle sourceRegion = getSourceRegion(param, srcWidth, srcHeight);
        final Collection<Tile> tiles = getTileManager(imageIndex).getTiles(sourceRegion,
                xSubsampling, ySubsampling);
        /*
         * If there is exactly one image to read, we will left the image reference to null. It will
         * be understood later as an indication to delegate directly to the sole image reader as an
         * optimization (no search for raw data type). Otherwise, we need to create the destination
         * image here. Note that this is the only image ever to be created during a mosaic read,
         * unless some underlying ImageReader do not honor our ImageReadParam.setDestination(image)
         * setting. In such case, the default behavior is to thrown an exception.
         */
        BufferedImage image = null;
        final Rectangle destRegion;
        if (tiles.size() == 1) {
            destRegion = null;
        } else {
            if (param != null) {
                image = param.getDestination();
            }
            destRegion = new Rectangle(); // Computed by the following method call.
            computeRegions(param, srcWidth, srcHeight, image, sourceRegion, destRegion);
            if (image == null) {
                /*
                 * If no image was explicitly specified, creates one using a raw image type
                 * acceptable for all tiles. An exception will be thrown if no such raw type
                 * was found. Note that this fallback may be a little bit costly since it may
                 * imply to open, close and reopen later some streams.
                 */
                ImageTypeSpecifier imageType = null;
                if (param != null) {
                    imageType = param.getDestinationType();
                }
                if (imageType == null) {
                    imageType = getRawImageType(tiles);
                    if (imageType == null) {
                        throw new IIOException(Errors.format(ErrorKeys.DESTINATION_NOT_SET));
                    }
                }
                image = imageType.createBufferedImage(destRegion.x + destRegion.width,
                                                      destRegion.y + destRegion.height);
                computeRegions(param, srcWidth, srcHeight, image, sourceRegion, destRegion);
            }
        }
        /*
         * Now read every tiles... If logging are enabled, we will format the tiles that we read in
         * a table and logs the table as one log record once the reading is completed or failed.
         */
        final Logger logger = Logging.getLogger(MosaicImageReader.class);
        final TableWriter table;
        if (logger.isLoggable(level)) {
            table = new TableWriter(null, TableWriter.SINGLE_VERTICAL_LINE);
            table.writeHorizontalSeparator();
            table.write("Reader\tTile\tSize\tSource\tDestination\tSubsampling");
            table.writeHorizontalSeparator();
        } else {
            table = null;
        }
        if (param == null) {
            param = getDefaultReadParam();
        }
        final Point destinationOffset = (destRegion != null) ? destRegion.getLocation() : null;
        Exception failure = null;
        for (final Tile tile : tiles) {
            if (abortRequested()) {
                break;
            }
            final Rectangle tileRegion = tile.getRegion();
            final Rectangle regionToRead = tileRegion.intersection(sourceRegion);
            /*
             * Computes the location of the region to read relative to the source region requested
             * by the user, and make sure that this location is a multiple of subsampling (if any).
             * The region to read may become smaller as a result of this calculation, i.e. the
             * value of its (x,y) location may increase, never reduce.
             */
            int xOffset = (regionToRead.x - sourceRegion.x) % xSubsampling;
            int yOffset = (regionToRead.y - sourceRegion.y) % ySubsampling;
            if (xOffset != 0) {
                xOffset -= xSubsampling;
                regionToRead.x     -= xOffset;
                regionToRead.width += xOffset;
            }
            if (yOffset != 0) {
                yOffset -= ySubsampling;
                regionToRead.y      -= yOffset;
                regionToRead.height += yOffset;
            }
            if (regionToRead.isEmpty()) {
                continue;
            }
            /*
             * Now that the offset is a multiple of subsampling, computes the destination offset.
             * Then translate the region to read from "this image reader" space to "tile" space.
             */
            if (destinationOffset != null) {
                xOffset = (regionToRead.x - sourceRegion.x) / xSubsampling;
                yOffset = (regionToRead.y - sourceRegion.y) / ySubsampling;
                destinationOffset.x = destRegion.x + xOffset;
                destinationOffset.y = destRegion.y + yOffset;
            }
            assert sourceRegion.contains(regionToRead) : regionToRead;
            assert tileRegion  .contains(regionToRead) : regionToRead;
            regionToRead.translate(-tileRegion.x, -tileRegion.y);
            /*
             * Sets the parameters to be given to the tile reader. We don't use any subsampling
             * offset because it has already been calculated in the region to read. Note that
             * the pixel size should be a dividor of subsampling; this condition must have been
             * checked by the tile manager when it selected the tiles to be returned.
             */
            final Dimension pixelSize = tile.getPixelSize();
            assert xSubsampling % pixelSize.width  == 0 : pixelSize;
            assert ySubsampling % pixelSize.height == 0 : pixelSize;
            param.setController(null);
            if (image != null) {
                param.setDestinationType(null);
                param.setDestination(image); // Must be after setDestinationType.
                param.setDestinationOffset(destinationOffset);
                if (param.canSetSourceRenderSize()) {
                    param.setSourceRenderSize(null); // TODO.
                }
            }
            param.setSourceRegion(regionToRead);
            param.setSourceSubsampling(xSubsampling / pixelSize.width,
                                       ySubsampling / pixelSize.height, 0, 0);
            final ImageReader reader = tile.getPreparedReader(true, true);
            /*
             * Adds a row in the table to be logged (if logable) and process to the reading.
             */
            if (table != null) {
                table.write(Tile.toString(reader));
                table.nextColumn();
                table.write(tile.getInputString());
                format(table, regionToRead.width,  regionToRead.height);
                format(table, regionToRead.x,      regionToRead.y);
                format(table, destinationOffset.x, destinationOffset.y);
                format(table, param.getSourceXSubsampling(), param.getSourceYSubsampling());
                table.nextLine();
            }
            final BufferedImage output;
            synchronized (this) {  // Same lock than ImageReader.abort()
                reading = reader;
            }
            try {
                output = reader.read(tile.getImageIndex(), param);
            } catch (IOException exception) {
                failure = exception;
                break;
            } catch (RuntimeException exception) {
                failure = exception;
                break;
            } finally {
                synchronized (this) {  // Same lock than ImageReader.abort()
                    reading = null;
                }
            }
            if (image == null) {
                image = output;
            } else if (output != image) {
                /*
                 * The read operation ignored our destination image.  By default we treat that
                 * as an error since the SampleModel may be incompatible and changing it would
                 * break the geophysics meaning of pixel values. However if we are interrested
                 * only in the visual aspect, we can copy the data (slow, consumes memory) and
                 * let Java2D performs the required color conversions. Note that it should not
                 * occur anyway if we choose correctly the raw image type in the code above.
                 */
                if (PRESERVE_DATA) {
                    failure = new IIOException("Incompatible data format."); // TODO: localize
                    break;
                } else {
                    final AffineTransform at = AffineTransform.getTranslateInstance(
                            destinationOffset.x, destinationOffset.y);
                    final Graphics2D graphics = image.createGraphics();
                    graphics.drawRenderedImage(output, at);
                    graphics.dispose();
                }
            }
        }
        /*
         * Finished the read operation, either on success of failure. Sends the log before
         * to throw the exception, so developpers can inspect the log in order to see what
         * has been read so far.
         */
        if (table != null) {
            table.writeHorizontalSeparator();
            final StringBuilder message = new StringBuilder("Loading ").append(tiles.size())
                    .append(" tiles").append(System.getProperty("line.separator", "\n"))
                    .append(table);
            if (failure != null) {
                message.append("Failed before completion with ").append(failure);
            }
            final LogRecord record = new LogRecord(level, message.toString());
            record.setSourceClassName(MosaicImageReader.class.getName());
            record.setSourceMethodName("read");
            logger.log(record);
        }
        if (failure != null) {
            if (failure instanceof IOException) {
                throw (IOException) failure;
            } else {
                throw (RuntimeException) failure;
            }
        }
        return image;
    }

    /**
     * Reads the tile indicated by the {@code tileX} and {@code tileY} arguments.
     *
     * @param  imageIndex The index of the image to be retrieved.
     * @param  tileX The column index (starting with 0) of the tile to be retrieved.
     * @param  tileY The row index (starting with 0) of the tile to be retrieved.
     * @return The desired tile.
     * @throws IOException if an error occurs during reading.
     */
    @Override
    public BufferedImage readTile(final int imageIndex, final int tileX, final int tileY)
            throws IOException
    {
        final int width  = getTileWidth (imageIndex);
        final int height = getTileHeight(imageIndex);
        final Rectangle sourceRegion = new Rectangle(tileX*width, tileY*height, width, height);
        final ImageReadParam param = getDefaultReadParam();
        param.setSourceRegion(sourceRegion);
        return read(imageIndex, param);
    }

    /**
     * Returns the logging level for tile information during reads.
     */
    public Level getLogLevel() {
        return level;
    }

    /**
     * Sets the logging level for tile information during reads. The default
     * value is {@link Level#FINE}. A {@code null} value restore the default.
     */
    public void setLogLevel(Level level) {
        if (level == null) {
            level = Level.FINE;
        }
        this.level = level;
    }

    /**
     * Formats a (x,y) value pair. A call to {@link TableWriter#nextColumn} is performed first.
     */
    private static void format(final TableWriter table, final int x, final int y) {
        table.nextColumn();
        table.write('(');
        table.write(String.valueOf(x));
        table.write(',');
        table.write(String.valueOf(y));
        table.write(')');
    }

    /**
     * Requests that any current read operation be aborted.
     */
    @Override
    public synchronized void abort() {
        super.abort();
        if (reading != null) {
            reading.abort();
        }
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
            Logging.recoverableException(MosaicImageReader.class, "dispose", e);
        }
        tiles = null;
        super.dispose();
    }

    /**
     * Service provider for {@link MosaicImageReader}.
     *
     * @since 2.5
     * @source $URL$
     * @version $Id$
     * @author Martin Desruisseaux
     */
    public static class Spi extends ImageReaderSpi {
        /**
         * The format names.
         */
        private static final String[] NAMES = new String[] {
            "mosaic"
        };

        /**
         * The input types.
         */
        private static final Class<?>[] INPUT_TYPES = new Class[] {
            Tile[].class,
            Collection.class
        };

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
            names           = NAMES;
            inputTypes      = INPUT_TYPES;
            pluginClassName = "org.geotools.image.io.mosaic.MosaicImageReader";
        }

        /**
         * Returns {@code true} if the image reader can decode the given input. The default
         * implementation returns {@code true} if the given object is non-null and an instance
         * of an {@linkplain #inputTypes input types}, or {@code false} otherwise.
         */
        @Override
        public boolean canDecodeInput(final Object source) throws IOException {
            if (source != null) {
                final Class<?> type = source.getClass();
                for (final Class<?> inputType : inputTypes) {
                    if (inputType.isAssignableFrom(type)) {
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * Returns a new {@link MosaicImageReader}.
         */
        @Override
        public ImageReader createReaderInstance(final Object extension) throws IOException {
            return new MosaicImageReader(this);
        }

        /**
         * Returns a brief, human-readable description of this service provider.
         *
         * @todo Localize.
         */
        @Override
        public String getDescription(final Locale locale) {
            return "Mosaic Image Reader";
        }
    }
}
