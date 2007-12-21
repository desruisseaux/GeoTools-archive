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

import java.util.*;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

import org.geotools.referencing.operation.matrix.XAffineTransform;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.util.logging.Logging;


/**
 * Convenience methods for building a collection of {@link Tile tiles}.
 *
 * @since 2.5
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class TileCollection {
    /**
     * Small number for floating point comparaisons.
     */
    private static final double EPS = 1E-6;

    /**
     * The collection of tiles.
     */
    private final List<Tile> tiles;

    /**
     * Tiles for which we should compute the bounding box only when we have them all.
     * Their bounding box (region) will need to be adjusted for the affine transform.
     */
    private Map<AffineTransform,Tile> pending;

    /**
     * The image reader for the next tiles to be added.
     */
    private ImageReader reader;

    /**
     * Creates an initially empty tile collection.
     */
    public TileCollection() {
        tiles = new ArrayList<Tile>();
    }

    /**
     * Removes any entry from this tile collection.
     */
    public void clear() {
        tiles.clear();
        if (pending != null) {
            pending.clear();
        }
    }

    /**
     * Returns {@code true} if there is no tile in this collection.
     */
    public boolean isEmpty() {
        return tiles.isEmpty() && (pending == null || pending.isEmpty());
    }

    /**
     * Sets the image reader for next tiles to be {@linkplain #add added}.
     */
    public void setImageReader(final ImageReader reader) {
        this.reader = reader;
    }

    /**
     * Returns the image reader for next tiles to be {@linkplain #add added}.
     */
    public ImageReader getImageReader() {
        return reader;
    }

    /**
     * Returns an image reader for the given input. If a reader was explicitly specified,
     * it will be used. Otherwise we will try to detect automatically a suitable reader.
     * <p>
     * Note that this method <strong>does not</strong> attempt to convert the given string
     * to image input stream, because {@link MosaicImageReader} is not well suited for them.
     *
     * @throws IllegalStateException if no suitable image reader has been found.
     */
    private ImageReader getImageReader(final Object input) throws IllegalStateException {
        final ImageReader reader = getImageReader();
        if (reader != null) {
            return reader;
        }
        Iterator<ImageReader> it = ImageIO.getImageReaders(input);
        if (it.hasNext()) {
            return it.next();
        }
        throw new IllegalStateException(Errors.format(ErrorKeys.NO_IMAGE_READER));
    }

    /**
     * Adds a tile for the given input and origin point. The tile width and
     * height will be computed when first needed.
     *
     * @param input      The input to be given to the image reader.
     * @param imageIndex The image index of the tile to be read. This is often 0.
     * @param origin     The upper-left corner in the destination image.
     * @param pixelSize  Pixel size relative to the finest resolution in an image pyramid,
     *                   or {@code null} if none.
     */
    public void add(Object input, int imageIndex, Point origin, Dimension pixelSize) {
        tiles.add(new Tile(getImageReader(input), input, imageIndex, origin, pixelSize));
    }

    /**
     * Adds a tile for the given input and bounds.
     *
     * @param input      The input to be given to the image reader.
     * @param imageIndex The image index of the tile to be read. This is often 0.
     * @param region     The region in the destination image. The {@linkplain Rectangle#width width}
     *                   and {@linkplain Rectangle#height height} should match the image size.
     * @param pixelSize  Pixel size relative to the finest resolution in an image pyramid,
     *                   or {@code null} if none.
     */
    public void add(Object input, int imageIndex, Rectangle region, Dimension pixelSize) {
        tiles.add(new Tile(getImageReader(input), input, imageIndex, region, pixelSize));
    }

    /**
     * Adds a tile for the given input, size and affine transform.
     *
     * @param input      The input to be given to the image reader.
     * @param imageIndex The image index of the tile to be read. This is often 0.
     * @param region     The region in the destination image. The {@linkplain Rectangle#width width}
     *                   and {@linkplain Rectangle#height height} should match the image size.
     * @param gridToCRS  A <cite>grid to coordinate reference system</cite> transform.
     */
    public void add(Object input, int imageIndex, Rectangle region, AffineTransform gridToCRS) {
        Tile.ensureNonNull("gridToCRS", gridToCRS);
        if (pending == null) {
            pending = new IdentityHashMap<AffineTransform,Tile>();
        }
        gridToCRS = new AffineTransform(gridToCRS);
        pending.put(gridToCRS, new Tile(getImageReader(input), input, imageIndex, region, null));
    }

    /**
     * Returns the tiles.
     */
    public Tile[] getTiles() {
        flush();
        return tiles.toArray(new Tile[tiles.size()]);
    }

    /**
     * If there is any tiles waiting in the {@linkplain #pending} list, computes
     * their bounds now and flush them to the final {@linkplain #tiles} list.
     *
     * @todo Current implementation consider only the mosaic/pyramid with the greatest
     *       amount of images, and discards the other ones. We should keep them all,
     *       maybe by allowing this class to handle more than one mosaic/pyramid.
     */
    private void flush() {
        if (pending == null || pending.isEmpty()) {
            return;
        }
        /*
         * Searchs for the map with the greatest amount of images. If we had to
         * ignore some images, a warning will be logged at the end of this method.
         */
        int count = 0;
        Map<AffineTransform,Dimension> levels = null;
        for (final Map<AffineTransform,Dimension> candidate : computePyramidLevels(pending.keySet())) {
            final int n = candidate.size();
            if (n > count) {
                count = n;
                levels = candidate;
            }
        }
        if (levels != null) {
            /*
             * Picks an affine transform to be used as the reference one. We need the finest one.
             * If more than one have the finest resolution, the exact choice does not matter much.
             */
            AffineTransform reference = null;
            double scale = Double.POSITIVE_INFINITY;
            for (final AffineTransform tr : levels.keySet()) {
                double s = 0;
                for (int i=0; i<4; i++) {
                    final double c = coefficient(tr, i);
                    s += c*c;
                }
                if (s < scale) {
                    scale = s;
                    reference = tr;
                }
            }
            if (!Double.isInfinite(scale)) {
                /*
                 * Transforms the image bounding box from its own space to the reference space. If
                 * 'computePyramidLevels' did its job correctly, the transform should contains only
                 * a scale and translation - no shear (we don't put assertions because of rounding
                 * errors). In such particular case, transforming a Rectangle2D is accurate. We
                 * round (we do not clip as in the default Rectangle implementation) because we
                 * really expect integer results.
                 */
                try {
                    reference = reference.createInverse(); // Really need a copy, not in-place.
                } catch (NoninvertibleTransformException e) {
                    throw new IllegalStateException(e);
                }
                final Rectangle2D.Double envelope = new Rectangle2D.Double();
                for (final Map.Entry<AffineTransform,Dimension> entry : levels.entrySet()) {
                    final AffineTransform tr = entry.getKey();
                    Tile tile = pending.remove(tr); // Should never be null.
                    final Rectangle bounds;
                    try {
                        bounds = tile.getRegion();
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                    tr.preConcatenate(reference);
                    XAffineTransform.transform(tr, bounds, envelope);
                    bounds.x      = (int) Math.round(envelope.x);
                    bounds.y      = (int) Math.round(envelope.y);
                    bounds.width  = (int) Math.round(envelope.width);
                    bounds.height = (int) Math.round(envelope.height);
                    tile = new Tile(tile.getReader(), tile.getInput(), tile.getImageIndex(),
                                    bounds, entry.getValue());
                    tiles.add(tile);
                }
            }
        }
        count = pending.size();
        pending.clear();
        if (count != 0) {
            // TODO: localize
            Logging.getLogger("org.geotools.image.io.mosaic").warning("Ignored " + count + " images.");
        }
    }

    /**
     * Sorts affine transform by increasing X scales in absolute value.
     * For {@link #computePyramidLevels} internal working only.
     */
    private static final Comparator<AffineTransform> X_COMPARATOR = new ScaleComparator(0);

    /**
     * Sorts affine transform by increasing Y scales in absolute value.
     * For {@link #computePyramidLevels} internal working only.
     */
    private static final Comparator<AffineTransform> Y_COMPARATOR = new ScaleComparator(2);

    /**
     * Implementation of {@link #X_COMPARATOR} and {@link #Y_COMPARATOR}.
     */
    private static final class ScaleComparator implements Comparator<AffineTransform> {
        /**
         * 0 for comparing the X scale, or 2 for the Y scale.
         */
        private final int term;

        /**
         * Creates a comparator for the given term (0 for X scale, 2 for Y scale).
         */
        ScaleComparator(final int term) {
            this.term = term;
        }

        /**
         * Compares the X <strong>or</strong> Y scale of the given transforms.
         */
        public int compare(final AffineTransform tr1, final AffineTransform tr2) {
            return Double.compare(vector(tr1), vector(tr2));
        }

        /**
         * Computes the square of the norm of scale and shear coefficients.
         */
        private double vector(final AffineTransform tr) {
            double value;
            return (value = coefficient(tr, term  )) * value +
                   (value = coefficient(tr, term+1)) * value;
        }
    }

    /**
     * Returns an affine transform coefficient from a numerical identifier.
     */
    private static double coefficient(final AffineTransform tr, final int term) {
        switch (term) {
            case 0: return tr.getScaleX();
            case 1: return tr.getShearX();
            case 2: return tr.getScaleY();
            case 3: return tr.getShearY();
            default: throw new AssertionError(term);
        }
    }

    /**
     * From a set of arbitrary affine transforms, computes pyramid levels that can be given to
     * {@link Tile} constructors. This method tries to locate the affine transform with finest
     * resolution. This is typically (but not always, depending on rotation or axis flip) the
     * transform with smallest {@linkplain AffineTransform#getScaleX scale X} and {@linkplain
     * AffineTransform#getScaleY scale Y} coefficients in absolute value. This transform is
     * given a dimension of (1,1) and stored in an {@linkplain IdentityHashMap identity hash
     * map}. Other transforms are stored in the same map with their dimension relative to the
     * first one, or discarted if the scale ratio is not an integer.
     *
     * @param  gridToCRS The <cite>grid to CRS</cite> affine transforms computed from the
     *         image to use in a pyramid. Those transforms will not be modified.
     * @return A subset of the given transforms with their relative resolution. This method
     *         typically returns one map, but more could be returned if the scale ratio is
     *         not an integer for every transforms.
     */
    private static List<Map<AffineTransform,Dimension>> computePyramidLevels(
            final Collection<AffineTransform> gridToCRS)
    {
        Map<AffineTransform,Dimension> result = null;
        List<Map<AffineTransform,Dimension>> results = null;
        /*
         * First, computes the pyramid levels along the X axis. Hash map will be created
         * when needed. Transforms that we were unable to classify will be discarted.
         */
        AffineTransform[] transforms = gridToCRS.toArray(new AffineTransform[gridToCRS.size()]);
        Arrays.sort(transforms, X_COMPARATOR);
        int length = transforms.length;
        while (length != 0) {
            if (result == null) {
                result = new IdentityHashMap<AffineTransform,Dimension>();
            }
            if (length >= (length = computePyramidLevels(transforms, length, result, 0))) {
                throw new AssertionError(length); // Should always be decreasing.
            }
            if (!result.isEmpty()) {
                if (results == null) {
                    results = new ArrayList<Map<AffineTransform,Dimension>>(2);
                }
                results.add(result);
                result = null;
            }
        }
        /*
         * Next, computes the pyramid levels along the Y axis. If we fail to compute the
         * pyramid level for some AffineTransform, they will be removed from the map. If
         * a map became empty because of that, the whole map will be removed.
         */
        if (results != null) {
            final Iterator<Map<AffineTransform,Dimension>> iterator = results.iterator();
            while (iterator.hasNext()) {
                result = iterator.next();
                length = result.size();
                transforms = result.keySet().toArray(transforms);
                Arrays.sort(transforms, 0, length, Y_COMPARATOR);
                length = computePyramidLevels(transforms, length, result, 2);
                while (--length >= 0) {
                    if (result.remove(transforms[length]) == null) {
                        throw new AssertionError(length);
                    }
                }
                if (result.size() <= 1) {
                    iterator.remove();
                }
            }
            if (!results.isEmpty()) {
                return results;
            }
        }
        return Collections.emptyList();
    }

    /**
     * Computes the pyramid level for the given affine transforms along the X or Y axis, and
     * stores the result in the given map.
     *
     * @param  gridToCRS The AffineTransform to analyse. This array <strong>must</strong> be
     *                   sorted along the dimension specififed by {@code term}.
     * @param  length    The number of valid entries in the {@code gridToCRS} array.
     * @param  result    An initially empty map in which to store the results.
     * @param  term      0 for analyzing the X axis, or 2 for the Y axis.
     * @return The number of entries remaining in {@code gridToCRS}.
     */
    private static int computePyramidLevels(final AffineTransform[] gridToCRS, final int length,
            final Map<AffineTransform,Dimension> result, final int term)
    {
        int processing = 0;  // Index of the AffineTransform under process.
        int remaining  = 0;  // Count of AffineTransforms that this method did not processed.
        AffineTransform base;
        double scale, shear;
        boolean scaleIsNull, shearIsNull;
        do {
            if (processing >= length) {
                return remaining;
            }
            base  = gridToCRS[processing++];
            scale = coefficient(base, term);
            shear = coefficient(base, term+1);
            scaleIsNull = Math.abs(scale) < EPS;
            shearIsNull = Math.abs(shear) < EPS;
        } while (scaleIsNull && shearIsNull && redo(result.remove(base)));
        if (term != 0) {
            // If we get a NullPointerException here, it would be a bug in the algorithm.
            result.get(base).height = 1;
        }
        /*
         * From this point, consider 'base', 'scale', 'shear', 'scaleIsNull', 'shearIsNull'
         * as final. They describe the AffineTransform with finest resolution along one axis
         * (X or Y), not necessarly both.
         */
        while (processing < length) {
            final AffineTransform candidate = gridToCRS[processing++];
            final double scale2 = coefficient(candidate, term);
            final double shear2 = coefficient(candidate, term+1);
            final int level;
            if (scaleIsNull) {
                if (!(Math.abs(scale2) < EPS)) {
                    // Expected a null scale but was not.
                    gridToCRS[remaining++] = candidate;
                    continue;
                }
                level = level(shear2 / shear);
            } else {
                level = level(scale2 / scale);
                if (shearIsNull ? !(Math.abs(shear2) < EPS) : (level(shear2 / shear) != level)) {
                    // Expected (a null shear) : (the same pyramid level), but was not.
                    gridToCRS[remaining++] = candidate;
                    continue;
                }
            }
            if (level == 0) {
                // Not a pyramid level (the ratio is not an integer).
                gridToCRS[remaining++] = candidate;
                continue;
            }
            /*
             * Stores the pyramid level either as the width or as the height, depending on the
             * 'term' value. The map is assumed initially empty for the X values, and containing
             * every required entries for the Y values.
             */
            switch (term) {
                default: {
                    throw new AssertionError(term);
                }
                case 0: {
                    if (result.isEmpty()) {
                        result.put(base, new Dimension(1,0));
                    }
                    if (result.put(candidate, new Dimension(level,0)) != null) {
                        throw new AssertionError(candidate); // Should never happen.
                    }
                    break;
                }
                case 2: {
                    // If we get a NullPointerException here, it would be a bug in the algorithm.
                    result.get(candidate).height = level;
                    break;
                }
            }
        }
        Arrays.fill(gridToCRS, remaining, length, null);
        return remaining;
    }

    /**
     * Computes the pyramid level from the ratio between two affine transform coefficients.
     * If the ratio has been computed from {@code entry2.scaleX / entry1.scaleX}, then a
     * return value of:
     * <p>
     * <ul>
     *   <li>1 means that both entries are at the same level.</li>
     *   <li>2 means that the second entry has pixels twice as large as first entry.</li>
     *   <li>3 means that the second entry has pixels three time larger than first entry.</li>
     *   <li><cite>etc...</cite></li>
     *   <li>A negative number means that the second entry has pixels smaller than first entry.</li>
     *   <li>0 means that the ratio between entries is not an integer number.</li>
     * </ul>
     *
     * @param  ratio The ratio between affine transform coefficients.
     * @return The pixel size relative to the smallest pixel, or 0 if it can't be computed.
     *         If the ratio is between 0 and 1, then this method returns a negative number.
     */
    private static int level(double ratio) {
        if (ratio > 0 && ratio < Double.POSITIVE_INFINITY) {
            // The 0.75 threshold could be anything between 0.5 and 1. We
            // take a middle value for being safe regarding rounding errors.
            final boolean inverse = (ratio < 0.75);
            if (inverse) {
                ratio = 1 / ratio;
            }
            final double integer = Math.rint(ratio);
            if (integer < Integer.MAX_VALUE && Math.abs(ratio - integer) < EPS) {
                // Found an integer ratio. Inverse the sign (just
                // as a matter of convention) if smaller than 1.
                int level = (int) integer;
                if (inverse) {
                    level = -level;
                }
                return level;
            }
        }
        return 0;
    }

    /**
     * A hack for a {@code while} loop.
     */
    private static boolean redo(final Dimension size) {
        return true;
    }
}
