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
import java.util.Map;
import java.util.HashMap;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.awt.Color;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.IndexColorModel;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.MultiPixelPackedSampleModel;
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
 * A palette can create {@linkplain IndexColorModel Index color models} or
 * {@linkplain ImageTypeSpecifier image type specifiers}
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public abstract class Palette {
    /**
     * The originating factory.
     */
    private final PaletteFactory factory;

    /**
     * The name of this palette.
     */
    protected final String name;

    /**
     * Index of the first valid element (inclusive) in the {@linkplain IndexColorModel
     * index color model} to be created. This is usually 0.
     */
    protected final int lower;

    /**
     * Index of the last valid element (exclusive) in the {@linkplain IndexColorModel
     * index color model} to be created. This is usually equals to
     * {@link IndexColorModel#getMapSize}.
     */
    protected final int upper;

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
     * and we don't want to prevent it to be garbage collected. We prefer weak instead of soft
     * reference for the same reason than {@link #colors}.
     *
     * @see #colors
     */
    private transient Reference/*<ImageTypeSpecifier>*/ specifier;

    /**
     * Creates a palette with the specified name and size. The size is the value that will be
     * returned by {@link IndexColorModel#getMapSize}.
     *
     * @param factory The originating factory.
     * @param name    The palette name.
     * @param lower   Index of the first valid element (inclusive) in the
     *                {@linkplain IndexColorModel index color model} to be created.
     *                This is usually 0.
     * @param upper   Index of the last valid element (exclusive) in the
     *                {@linkplain IndexColorModel index color model} to be created.
     *                This is usually equals to {@link IndexColorModel#getMapSize}.
     */
    protected Palette(final PaletteFactory factory, final String name, final int lower, final int upper) {
        this.factory = factory;
        this.name    = name;
        this.lower   = lower;
        this.upper   = upper;
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
    protected abstract int[] createARGB() throws IOException;

    /**
     * Creates an image type specifier for this palette. This method tries to reuse the color
     * model if possible, since it may consume a significant amount of memory.
     *
     * @throws  FileNotFoundException If the RGB values need to be read from a file and this file
     *          (typically inferred from {@link #name}) is not found.
     * @throws  IOException  If an other find of I/O error occured.
     * @throws  IIOException If an other kind of error prevent this method to complete.
     */
    private ImageTypeSpecifier createImageTypeSpecifier() throws IOException {
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
        colors    = new WeakReference/*<ColorModel>*/(its.getColorModel());
        specifier = new WeakReference/*<ImageTypeSpecifier>*/(its);
        return its;
    }

    /**
     * Returns an image type specifier for this palette.
     *
     * @throws  FileNotFoundException If the RGB values need to be read from a file and this file
     *          (typically inferred from {@link #name}) is not found.
     * @throws  IOException  If an other find of I/O error occured.
     * @throws  IIOException If an other kind of error prevent this method to complete.
     */
    protected final ImageTypeSpecifier getImageTypeSpecifier() throws IOException {
        synchronized (factory.palettes) {
            Palette candidate = (Palette) factory.palettes.get(this);
            if (candidate == null) {
                candidate = this;
                if (factory.palettes.put(this, this) != null) {
                    // Should never happen.
                    throw new AssertionError(this);
                }
            }
            return candidate.createImageTypeSpecifier();
        }
    }

    /**
     * Reads ARGB values from file or resources.
     *
     * @throws  FileNotFoundException If the RGB values need to be read from a file and this file
     *                       (typically inferred from {@link #name}) is not found.
     * @throws  IOException  If an other find of I/O error occured.
     * @throws  IIOException If an other kind of error prevent this method to complete.
     */
    final Color[] readColors() throws IOException {
        final Color[] colors = factory.getColors(name);
        if (colors == null) {
            throw new FileNotFoundException(Errors.format(ErrorKeys.FILE_DOES_NOT_EXIST_$1, name));
        }
        return colors;
    }

    /**
     * Retourne un type où les données manquantes sont représentées par la première valeur d'index.
     * Cette première valeur d'index sera toujours 0.
     * 
     * @param   palette Le nom de la palette qui permet de construire le modèle ce couleurs.
     * @param   size    La borne supérieure de la plage des valeurs autorisées (on considère 
     *                  celles-ci de 0 inclusivement à {@code size} exclusivement).
     * @return          Le type d'image correspondant à la palette.
     *
     * @throws  IOException             En cas d'erreur de lecture de la palette.
     * @throws  IIOException            En cas d'erreur logique de la palette.
     * @throws  FileNotFoundException   Si le fichier {@code palette} n'existe pas.
     */
//    public static ImageTypeSpecifier forNodataFirst(final String palette, final int size) throws IOException {
//        return new NodataFirst(palette, 0, size).getImageTypeSpecifier();
//    }

    /**
     * Fabrique de modèles de couleurs où les données manquantes sont représentées par
     * la première valeur d'index.
     */
    private static final class NodataFirst extends Palette {
        /**
         * Construit une palette du nom spécifié.
         */
        public NodataFirst(final PaletteFactory factory, final String name,
                           final int lower, final int upper)
        {
            super(factory, name, lower, upper);
        }

        /**
         * {@inheritDoc}
         */
        protected int[] createARGB() throws IOException {
            final Color[] colors = readColors();
            final int[] ARGB = new int[upper];
            /*
             * On VEUT que la première valeur du tableau ARGB soit 0,
             * car c'est la valeur signalant une absence de données.
             */
            ColorUtilities.expand(colors, ARGB, lower+1, upper);
            return ARGB;
        }
    }

    /**
     * Retourne un type où les données manquantes sont représentées par la dernière valeur d'index.
     * Cette première valeur d'index sera toujours {@code size-1}. Ce type d'image est utilisé pour
     * les concentrations en chlorophylle-<var>a</var> par exemple.
     *
     * @param   palette Le nom de la palette qui permet de construire le modèle ce couleurs.
     * @param   size    La borne supérieure de la plage des valeurs autorisées (on considère 
     *                  celles-ci de 0 inclusivement à {@code size} exclusivement).
     * @return          Le type d'image correspondant à la palette.
     *
     * @throws  IOException             En cas d'erreur de lecture de la palette.
     * @throws  IIOException            En cas d'erreur logique de la palette.
     * @throws  FileNotFoundException   Si le fichier {@code palette} n'existe pas.
     */
//    public static ImageTypeSpecifier forNodataLast(final String palette, final int size) throws IOException {
//        return new NodataLast(palette, 0, size).getImageTypeSpecifier();
//    }

    /**
     * Fabrique de modèles de couleurs où les données manquantes sont représentées par
     * la dernière valeur d'index.
     */
    private static final class NodataLast extends Palette {
        /**
         * Construit une palette du nom spécifié.
         */
        public NodataLast(final PaletteFactory factory, final String name,
                          final int lower, final int upper) {
            super(factory, name, lower, upper);
        }

        /**
         * {@inheritDoc}
         */
        protected int[] createARGB() throws IOException {
            final Color[] colors = readColors();
            final int[] ARGB = new int[upper];
            /*
             * On VEUT que la dernière valeur du tableau ARGB soit 0,
             * car c'est la valeur signalant une absence de données.
             */
            ColorUtilities.expand(colors, ARGB, lower, (upper-1));
            return ARGB;
        }
    }

    /**
     * Retourne un type où la palette de couleurs est répétée un certain nombre de fois. Ce type
     * est utilisée par exemple pour certaines images de SST de la Nasa, où une valeur de qualité
     * est compactée avec la valeur de température.
     * 
     * @param   palette   Le nom de la palette qui permet de construire le modèle ce couleurs.
     * @param   size      La borne supérieure de la plage des valeurs autorisées (on considère 
     *                    celles-ci de 0 inclusivement à {@code size} exclusivement).
     * @param   validSize Le nombre de couleurs valides. Ce nombre doit être inférieur ou égal
     *                    à {@code size}.
     * @param   pageCount Le nombre de fois que les couleurs doivent être répétées.
     * @return            Le type d'image correspondant à la palette.
     *
     * @throws  IOException             En cas d'erreur de lecture de la palette.
     * @throws  IIOException            En cas d'erreur de 'parsing' de la palette.
     * @throws  FileNotFoundException   Si le fichier {@code palette} n'existe pas.
     */
//    public static ImageTypeSpecifier forRepeated(final String palette, final int size,
//                                                 final int validSize, final int pageCount)
//            throws IOException
//    {
//        return new Repeated(palette, size, validSize, pageCount).getImageTypeSpecifier();
//    }

    /**
     * Fabrique de modèles de couleurs où une palette est répétée un certain nombre de fois.
     */
    private static final class Repeated extends Palette {
        /**
         * Le nombre de couleurs valides. Ce nombre doit être inférieur ou égal à {@link #size}.
         * Pour les fichiers de SST de la Nasa, cette valeur est de 512.
         */
        private final int validSize;

        /**
         * Le nombre de fois que les couleurs doivent être répétées.
         * Pour les fichiers de SST de la Nasa, cette valeur est de 8.
         */
        private final int pageCount;

        /**
         * Construit une palette du nom spécifié.
         */
        public Repeated(final PaletteFactory factory, final String name,
                        final int lower, final int upper, final int validSize, final int pageCount)
        {
            super(factory, name, lower, upper);
            this.validSize = validSize;
            this.pageCount = pageCount;
        }

        /**
         * {@inheritDoc}
         */
        protected int[] createARGB() throws IOException {
            final int size = upper - lower;
            final Color[] colors = readColors();
            final int[] ARGB = new int[size];
            final int pageSize = size / pageCount;
            /* 
             * La plage des valeurs dans les images SST varie de 0 à 512...
             * on construit donc un modèle de 512 couleurs.
             */
            ColorUtilities.expand(colors, ARGB, lower, lower + validSize);
            /*
             * Les valeurs comprises entre 512 et 1024 correpondent à un absence de données.
             */
            final Color[] c = new Color[] {Color.BLACK, Color.WHITE};
            ColorUtilities.expand(c, ARGB, lower + validSize, lower + pageSize);
            /* 
             * L'information de qualité est codé sur 3 bits... il y a donc 8 niveau de qualité
             * On répète donc le modèle de couleurs huit fois.
             */
            for (int i=1; i<pageCount; i++) {
                System.arraycopy(ARGB, lower, ARGB, lower + pageSize*i, pageSize);
            }
            return ARGB;
        }

        /**
         * {@inheritDoc}
         */
        //@Override
        public int hashCode() {
            return super.hashCode() ^ (((pageCount*37) ^ validSize) * 37);
        }

        /**
         * {@inheritDoc}
         */
        //@Override
        public boolean equals(final Object object) {
            if (object == this) {
                return true;
            }
            if (super.equals(object)) {
                final Repeated that = (Repeated) object;
                return this.validSize == that.validSize &&
                       this.pageCount == that.pageCount;
            }
            return false;
        }
    }

    /**
     * Retourne une valeur représentative de cette palette de couleurs.
     */
    //@Override
    public int hashCode() {
        return getClass().hashCode() ^ name.hashCode() ^ upper;
    }

    /**
     * Compare cette palette avec la palette spécifiée.
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
                   Utilities.equals(this.name, that.name);
        }
        return false;
    }

    /**
     * Retourne une représentation textuelle de cette palette. Cette méthode
     * est utilisée principalement à des fins de déboguages.
     */
    //@Override
    public String toString() {
        return Utilities.getShortClassName(this) + '[' + name + ' ' + (upper-lower) + " colors]";
    }
}
