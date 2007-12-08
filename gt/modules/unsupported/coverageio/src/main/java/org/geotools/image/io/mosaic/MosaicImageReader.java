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
import javax.imageio.IIOException;
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
     * The tile manager.
     */
    private TileManager tiles;

    /**
     * Constructs an image reader with the specified provider.
     */
    public MosaicImageReader(final ImageReaderSpi spi) {
        super(spi != null ? spi : Spi.DEFAULT);
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
        tiles = new TileManager(inputTiles);
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
     * Returns the tiles manager, making sure that it is set.
     */
    private TileManager getTileManager() throws IOException {
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
        return getTileManager().getNumImages();
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
        return getTileManager().getRegion(imageIndex).width;
    }

    /**
     * Returns the height in pixels of the given image within the input source.
     *
     * @param  imageIndex The index of the image to be queried.
     * @return The height of the image.
     * @throws IOException If an error occurs reading the information from the input source.
     */
    public int getHeight(final int imageIndex) throws IOException {
        return getTileManager().getRegion(imageIndex).height;
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
     * Returns {@code true} if every image reader uses the default implementation for the given
     * method. Some methods may avoid costly file seeking when this method returns {@code true}.
     * <p>
     * This method always returns {@code true} if there is no tiles.
     */
    private boolean useDefaultImplementation(final String methodName) {
        if (tiles == null) {
            return true;
        }
        // TODO: implement that.
        return false;
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
        if (useDefaultImplementation("isRandomAccessEasy")) {
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
     * Returns the aspect ratio. If all tiles have the same aspect ratio, then that ratio is
     * returned. Otherwise the {@linkplain ImageReader#getAspectRatio default value} is returned.
     *
     * @param  imageIndex The index of the image to be queried.
     * @throws IOException If an error occurs reading the information from the input source.
     */
    @Override
    public float getAspectRatio(final int imageIndex) throws IOException {
        if (!useDefaultImplementation("getAspectRatio")) {
            float ratio = Float.NaN;
            for (final Tile tile : tiles.getTiles(imageIndex, null)) {
                final float candidate = tile.getPreparedReader(true, true).getAspectRatio(imageIndex);
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
        if (tiles != null) {
            final ImageTypeSpecifier type =
                    getRawImageType(imageIndex, tiles.getTiles(imageIndex, null));
            if (type != null) {
                return type;
            }
        }
        return super.getRawImageType(imageIndex);
    }

    /**
     * Returns an image type which most closely represents the "raw" internal format of the
     * given set of tiles. If none is found, returns {@code null}.
     *
     * @param  imageIndex The image index, from 0 inclusive to {@link #getNumImages} exclusive.
     * @param  tiles       The tiles to iterate over.
     * @return A raw image type specifier acceptable for all tiles, or {@code null} if none.
     * @throws IOException If an error occurs reading the information from the input source.
     */
    private ImageTypeSpecifier getRawImageType(final int imageIndex, final Collection<Tile> tiles)
            throws IOException
    {
        // Gets the list of every raw image types, with the most frequent type first.
        final Set<ImageTypeSpecifier> rawTypes = new FrequencySortedSet<ImageTypeSpecifier>(true);
        final Set<ImageTypeSpecifier> allowed = getImageTypes(imageIndex, tiles, rawTypes);
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
     * @param  imageIndex  The image index, from 0 inclusive to {@link #getNumImages} exclusive.
     * @param  tiles       The tiles to iterate over.
     * @param  rawTypes    If non-null, a collection where to store the raw image types.
     *                     No filtering is applied on this collection.
     * @return The image type specifiers that are common to all tiles.
     * @throws IOException If an error occurs reading the information from the input source.
     */
    private Set<ImageTypeSpecifier> getImageTypes(final int imageIndex,
                                                  final Collection<Tile> tiles,
                                                  final Collection<ImageTypeSpecifier> rawTypes)
            throws IOException
    {
        int pass = 0;
        final Map<ImageTypeSpecifier,Integer> types = new LinkedHashMap<ImageTypeSpecifier,Integer>();
        for (final Tile tile : tiles) {
            final ImageReader reader = tile.getPreparedReader(true, true);
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
        final Collection<Tile> tiles = getTileManager().getTiles(imageIndex, null);
        return getImageTypes(imageIndex, tiles, null).iterator();
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
        if (!useDefaultImplementation("getDefaultReadParam")) {
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
        for (final Tile tile : getTileManager().getTiles()) {
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
        for (final Tile tile : getTileManager().getTiles()) {
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
        for (final Tile tile : getTileManager().getTiles(imageIndex, null)) {
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
        for (final Tile tile : getTileManager().getTiles(imageIndex, null)) {
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
    public BufferedImage read(final int imageIndex, ImageReadParam param) throws IOException {
        clearAbortRequest();
        final int          srcWidth  = getWidth (imageIndex);
        final int          srcHeight = getHeight(imageIndex);
        final Rectangle sourceRegion = getSourceRegion(param, srcWidth, srcHeight);
        final Collection<Tile> tiles = getTileManager().getTiles(imageIndex, sourceRegion);
        if (tiles.size() == 1) {
            /*
             * If there is exactly one tile, delegates to the image reader
             * directly without any other processing from ImageMosaicReader.
             */
            final Tile tile = tiles.iterator().next();
            if (param != null) {
                final Rectangle region = param.getSourceRegion();
                if (region != null) {
                    final Point origin = tile.getOrigin();
                    region.translate(-origin.x, -origin.y);
                    param.setSourceRegion(region);
                }
            }
            final ImageReader reader = tile.getPreparedReader(seekForwardOnly, ignoreMetadata);
            return reader.read(imageIndex, param);
        }
        /*
         * We need to read at least two tiles (or zero).
         * Creates the destination image here.
         */
        BufferedImage image = (param != null) ? param.getDestination() : null;
        final Rectangle   destRegion = new Rectangle(); // Will be computed later.
        computeRegions(param, srcWidth, srcHeight, image, sourceRegion, destRegion);
        if (image == null) {
            /*
             * If no image was explicitly specified, creates one using a raw image type acceptable
             * for all tiles. An exception will be thrown if no such raw type was found. Note that
             * this fallback may be a little bit costly since it may imply to open, close and
             * reopen later some streams.
             */
            ImageTypeSpecifier imageType = null;
            if (param != null) {
                imageType = param.getDestinationType();
            }
            if (imageType == null) {
                imageType = getRawImageType(imageIndex, tiles);
                if (imageType == null) {
                    throw new IIOException(Errors.format(ErrorKeys.DESTINATION_NOT_SET));
                }
            }
            image = imageType.createBufferedImage(destRegion.x + destRegion.width,
                                                  destRegion.y + destRegion.height);
            computeRegions(param, srcWidth, srcHeight, image, sourceRegion, destRegion);
        }
        /*
         * Now read every tiles...
         */
        final int  destinationXOffset = destRegion.x - sourceRegion.x;
        final int  destinationYOffset = destRegion.y - sourceRegion.y;
        final Point destinationOffset = new Point();
        if (param == null) {
            param = getDefaultReadParam();
        }
        for (final Tile tile : tiles) {
            if (abortRequested()) {
                break;
            }
            final Rectangle tileRegion = tile.getRegion();
            final Rectangle sourceTileRegion = tileRegion.intersection(sourceRegion);
            if (sourceTileRegion.isEmpty()) {
                continue;
            }
            destinationOffset.move(sourceTileRegion.x, sourceTileRegion.y);
            destinationOffset.translate(destinationXOffset, destinationYOffset);
            sourceTileRegion .translate(-tileRegion.x, -tileRegion.y);
            param.setController(null);
            param.setDestinationType(null);
            param.setDestination(image); // Must be after setDestinationType.
            param.setDestinationOffset(destinationOffset);
            param.setSourceRegion(sourceTileRegion);
            if (param.canSetSourceRenderSize()) {
                param.setSourceRenderSize(null); // TODO.
            }
            final ImageReader reader = tile.getPreparedReader(true, true);
            final BufferedImage output = reader.read(imageIndex, param);
            if (output != image) {
                /*
                 * The read operation ignored our destination image. Copy the pixels (slower)
                 * (todo: not yet implemented).
                 */
                throw new IIOException("Not yet implemented.");
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
        static final Spi DEFAULT = new Spi();

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
