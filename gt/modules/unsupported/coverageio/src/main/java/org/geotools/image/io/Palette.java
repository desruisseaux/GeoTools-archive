/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2006, Geomatys
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
package org.geotools.image.io;

// J2SE dependencies
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.*;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.IIOException;
import java.io.FileNotFoundException;
import java.io.IOException;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.image.ColorUtilities;


/**
 * A set of RGB colors created by a {@linkplain PaletteFactory palette factory} from a name.
 * A palette can creates an {@linkplain IndexColorModel index color model} or an {@linkplain
 * ImageTypeSpecifier image type specifier} from the RGB colors. The color model is retained
 * by the palette as a {@linkplain WeakReference weak reference} (<strong>not</strong> as a
 * {@linkplain java.lang.ref.SoftReference soft reference}) because it may consume up to 256
 * kb. The purpose of the weak reference is to share existing instances in order to reduce
 * memory usage; the purpose is not to provide caching.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public class Palette {
    /**
     * The originating factory.
     */
    final PaletteFactory factory;

    /**
     * The name of this palette.
     */
    private final String name;

    /**
     * Index of the first valid element (inclusive) in the {@linkplain IndexColorModel
     * index color model} to be created. Pixels in the range 0 inclusive to {@code lower}
     * exclusive will be reserved for "no data" values.
     */
    protected final int lower;

    /**
     * Index of the last valid element (exclusive) in the {@linkplain IndexColorModel
     * index color model} to be created. Pixels in the range {@code upper} inclusive
     * to {@link #size} exclusive will be reserved for "no data" values.
     */
    protected final int upper;

    /**
     * The size of the {@linkplain IndexColorModel index color model} to be created.
     * This is the value to be returned by {@link IndexColorModel#getMapSize}.
     */
    private final int size;

    /**
     * The sample model to be given to {@link ImageTypeSpecifier}.
     */
    private transient SampleModel samples;

    /**
     * A weak reference to the color model. This color model may consume a significant
     * amount of memory (up to 256 kb). Consequently, we will prefer {@link WeakReference}
     * over {@link java.lang.ref.SoftReference}. The purpose of this weak reference is to
     * share existing instances, not to cache it since it is cheap to rebuild.
     */
    private transient Reference/*<ColorModel>*/ colors;

    /**
     * A weak reference to the image specifier to be returned by {@link #getImageTypeSpecifier}.
     * We use weak reference because the image specifier contains a reference to the color model
     * and we don't want to prevent it to be garbage collected. See {@link #colors} for an
     * explanation about why we use weak instead of soft references.
     */
    private transient Reference/*<ImageTypeSpecifier>*/ specifier;

    /**
     * Creates a palette with the specified name and size. The RGB colors will be distributed
     * in the range {@code lower} inclusive to {@code upper} exclusive. Remaining pixel values
     * (if any) will be left to a black or transparent color by default.
     *
     * @param factory The originating factory.
     * @param name    The palette name.
     * @param lower   Index of the first valid element (inclusive) in the
     *                {@linkplain IndexColorModel index color model} to be created.
     * @param upper   Index of the last valid element (exclusive) in the
     *                {@linkplain IndexColorModel index color model} to be created.
     * @param size    The size of the {@linkplain IndexColorModel index color model} to be created.
     *                This is the value to be returned by {@link IndexColorModel#getMapSize}.
     */
    protected Palette(final PaletteFactory factory, final String name,
                      final int lower, final int upper, final int size)
    {
        this.factory = factory;
        this.name    = name;
        this.lower   = lower;
        this.upper   = upper;
        this.size    = size;
        if (lower < 0 || lower >= upper) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.BAD_RANGE_$2,
                    new Integer(lower), new Integer(upper)));
        }
        if (upper > size) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.ILLEGAL_ARGUMENT_$2,
                    "size", new Integer(size)));
        }
    }

    /**
     * Creates and returns ARGB values for the {@linkplain IndexColorModel index color model} to be
     * created. This method is invoked automatically the first time the color model is required, or
     * when it need to be rebuilt.
     *
     * @throws  FileNotFoundException If the RGB values need to be read from a file and this file
     *                                (typically inferred from {@link #name}) is not found.
     * @throws  IOException           If an other find of I/O error occured.
     * @throws  IIOException          If an other kind of error prevent this method to complete.
     */
    protected int[] createARGB() throws IOException {
        final Color[] colors = factory.getColors(name);
        if (colors == null) {
            throw new FileNotFoundException(Errors.format(ErrorKeys.FILE_DOES_NOT_EXIST_$1, name));
        }
        final int[] ARGB = new int[size];
        ColorUtilities.expand(colors, ARGB, lower, upper);
        return ARGB;
    }

    /**
     * Returns the color model for this palette. This method tries to reuse existing
     * color model if possible, since it may consume a significant amount of memory.
     *
     * @throws  FileNotFoundException If the RGB values need to be read from a file and this file
     *          (typically inferred from {@link #name}) is not found.
     * @throws  IOException  If an other find of I/O error occured.
     * @throws  IIOException If an other kind of error prevent this method to complete.
     */
    public synchronized ColorModel getColorModel() throws IOException {
        if (colors != null) {
            final ColorModel candidate = (ColorModel) colors.get();
            if (candidate != null) {
                return candidate;
            }
        }
        return getImageTypeSpecifier().getColorModel();
    }

    /**
     * Returns the image type specifier for this palette. This method tries to reuse existing
     * color model if possible, since it may consume a significant amount of memory.
     *
     * @throws  FileNotFoundException If the RGB values need to be read from a file and this file
     *          (typically inferred from {@link #name}) is not found.
     * @throws  IOException  If an other find of I/O error occured.
     * @throws  IIOException If an other kind of error prevent this method to complete.
     */
    public synchronized ImageTypeSpecifier getImageTypeSpecifier() throws IOException {
        /*
         * First checks the weak references.
         */
        if (specifier != null) {
            final ImageTypeSpecifier candidate = (ImageTypeSpecifier) specifier.get();
            if (candidate != null) {
                return candidate;
            }
        }
        if (samples!=null && colors!=null) {
            final ColorModel candidate = (ColorModel) colors.get();
            if (candidate != null) {
                final ImageTypeSpecifier its = new ImageTypeSpecifier(candidate, samples);
                specifier = new WeakReference(its);
                return its;
            }
        }
        /*
         * Nothing reacheable. Rebuild the specifier.
         */
        final int[] ARGB = createARGB();
        final byte[] A = new byte[ARGB.length];
        final byte[] R = new byte[ARGB.length];
        final byte[] G = new byte[ARGB.length];
        final byte[] B = new byte[ARGB.length];
        for (int i=0; i<ARGB.length; i++) {
            int code = ARGB[i];
            B[i] = (byte) ((code       ) & 0xFF);
            G[i] = (byte) ((code >>>= 8) & 0xFF);
            R[i] = (byte) ((code >>>= 8) & 0xFF);
            A[i] = (byte) ((code >>>= 8) & 0xFF);
        }
        final int bits = ColorUtilities.getBitCount(ARGB.length);
        final int type = (bits <= 8) ? DataBuffer.TYPE_BYTE : DataBuffer.TYPE_USHORT;
        final boolean packed = (bits==1 || bits==2 || bits==4);
        final boolean dense  = (packed || bits==8 || bits==16);
        final ImageTypeSpecifier its;
        if (dense && (1 << bits) == ARGB.length) {
            its = ImageTypeSpecifier.createIndexed(R,G,B,A, bits, type);
        } else {
            /*
             * The "ImageTypeSpecifier.createIndexed(...)" method is too strict. The IndexColorModel
             * constructor is more flexible. This block mimic the "ImageTypeSpecifier.createIndexed"
             * work without the constraints imposed by "createIndexed". Being more flexible consume
             * less memory for the color palette, since we don't force it to be 64 kb in the USHORT
             * data type case.
             */
            final IndexColorModel colors = new IndexColorModel(bits, ARGB.length, R,G,B,A);
            final SampleModel samples;
            if (packed) {
                samples = new MultiPixelPackedSampleModel(type, 1, 1, bits);
            } else {
                samples = new PixelInterleavedSampleModel(type, 1, 1, 1, 1, new int[1]);
            }
            its = new ImageTypeSpecifier(colors, samples);
        }
        samples   = its.getSampleModel();
        colors    = new PaletteDisposer.Reference(this, its.getColorModel());
        specifier = new WeakReference/*<ImageTypeSpecifier>*/(its);
        return its;
    }

    /**
     * Returns the color palette as an image of the specified size.
     * This is useful for looking visually at a color palette.
     *
     * @param size The image size. The palette will be vertical if
     *        <code>size.{@linkplain Dimension#height height}</code> &gt;
     *        <code>size.{@linkplain Dimension#width  width }</code>
     */
    public RenderedImage getImage(final Dimension size) throws IOException {
        final IndexColorModel colors;
        final BufferedImage   image;
        final WritableRaster  raster;
        colors = (IndexColorModel) getColorModel();
        image  = new BufferedImage(size.width, size.height, BufferedImage.TYPE_BYTE_INDEXED, colors);
        raster = image.getRaster();
        int xmin   = raster.getMinX();
        int ymin   = raster.getMinY();
        int width  = raster.getWidth();
        int height = raster.getHeight();
        final boolean horizontal = size.width >= size.height;
        // Computation will be performed as if the image were horizontal.
        // If it is not, interchanges x and y values.
        if (!horizontal) {
            int tmp;
            tmp = xmin;  xmin  = ymin;   ymin   = tmp;
            tmp = width; width = height; height = tmp;
        }
        final int xmax = xmin + width;
        final int ymax = ymin + height;
        final double scale = (double)colors.getMapSize() / (double)width;
        for (int x=xmin; x<xmax; x++) {
            final int value = (int) Math.round(scale * (x-xmin));
            for (int y=ymin; y<ymax; y++) {
                if (horizontal) {
                    raster.setSample(x, y, 0, value);
                } else {
                    raster.setSample(y, x, 0, value);
                }
            }
        }
        return image;
    }

    /**
     * Returns a hash value for this palette.
     */
    //@Override
    public int hashCode() {
        return name.hashCode() + 37*(lower + 37*(upper + 37*size));
    }

    /**
     * Compares this palette with the specified object for equality.
     */
    //@Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object != null && getClass().equals(object.getClass())) {
            final Palette that = (Palette) object;
            return this.lower == that.lower &&
                   this.upper == that.upper &&
                   this.size  == that.size  &&
                   Utilities.equals(this.name, that.name);
        }
        return false;
    }

    /**
     * Returns a string representation of this palette. Used for debugging purpose only.
     */
    //@Override
    public String toString() {
        return Utilities.getShortClassName(this) + '[' + name + ' ' + size + " colors]";
    }
}
