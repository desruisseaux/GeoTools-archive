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

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;

import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;


/**
 * An image reader built from a mosaic of other image readers.
 *
 * @since 2.5
 * @input $URL$
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
                    Utilities.getShortClassName(input), Utilities.getShortClassName(Tile[].class)));
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

    public Iterator<ImageTypeSpecifier> getImageTypes(final int imageIndex) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public IIOMetadata getStreamMetadata() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public IIOMetadata getImageMetadata(final int imageIndex) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public BufferedImage read(final int imageIndex, final ImageReadParam param) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
