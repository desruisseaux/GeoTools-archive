/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2005, Institut de Recherche pour le D�veloppement
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
package org.geotools.image;

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
import org.geotools.resources.image.ColorUtilities;


/**
 * Fabrique de mod�les de couleurs index�s.
 * 
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public abstract class Palette {
    /**
     * Ensemble des palettes d�j� construites.
     */
    private static final Map<Palette,Palette> POOL = new HashMap<Palette,Palette>();

    /**
     * Le nom de palette qui permet de construire l'objet {@link IndexColorModel}.
     */
    protected final String palette;

    /**
     * La borne sup�rieure de la plage des valeurs autoris�es (on consid�re 
     * celles-ci de 0 inclusivement � {@code size} exclusivement).
     */
    protected final int size;

    /**
     * Le mod�le des pixels.
     */
    private transient SampleModel samples;

    /**
     * Une r�f�rence faible vers le mod�le des couleurs. Ce mod�le peut �tre volumineux
     * (jusqu'� 256 ko). Ce volume explique que l'on utilise une r�f�rence faible.
     */
    private transient Reference<ColorModel> colors;

    /**
     * Une r�f�rence faible vers le type s'image retourn� par {@link #getImageTypeSpecifier}.
     * On ne conserve pas de r�f�rence dure vers {@link ImageTypeSpecifier} car ce dernier
     * contient lui-m�me une r�f�rence vers le mod�le de couleurs, et on ne veut pas emp�cher
     * le ramasse-miettes de le collecter.
     */
    private transient Reference<ImageTypeSpecifier> specifier;

    /**
     * Construit une palette du nom et de la taille sp�cifi�e.
     */
    protected Palette(final String palette, final int size) {
        this.palette = palette;
        this.size    = size;
    }

    /**
     * Construit et retourne les codes de couleurs ARGB. Cette m�thode est appel�e automatiquement
     * la premi�re fois o� la palette est demand�e, et le r�sultat sera conserv� dans une cache pour
     * r�utilisation par d'autres images.
     *
     * @throws  IOException             En cas d'erreur de lecture de la palette.
     * @throws  IIOException            En cas d'erreur logique de la palette.
     * @throws  FileNotFoundException   Si le fichier {@code palette} n'existe pas.
     */
    protected abstract int[] createARGB() throws IOException;

    /**
     * Construit un descripteur de type d'image pour cette palette. Cette m�thode tente de
     * r�utiliser le mod�le de couleurs si possible, car ce dernier peut �tre volumineux
     * (jusqu'� 256 ko).
     *
     * @throws  IOException             En cas d'erreur de lecture de la palette.
     * @throws  IIOException            En cas d'erreur logique de la palette.
     * @throws  FileNotFoundException   Si le fichier {@code palette} n'existe pas.
     */
    private ImageTypeSpecifier createImageTypeSpecifier() throws IOException {
        if (specifier != null) {
            final ImageTypeSpecifier candidate = specifier.get();
            if (candidate != null) {
                return candidate;
            }
        }
        if (samples!=null && colors!=null) {
            final ColorModel candidate = colors.get();
            if (candidate != null) {
                return new ImageTypeSpecifier(candidate, samples);
            }
        }
        final int[] ARGB = createARGB();
        final byte[] A = new byte[ARGB.length];
        final byte[] R = new byte[ARGB.length];
        final byte[] G = new byte[ARGB.length];
        final byte[] B = new byte[ARGB.length];
        for (int i=0; i<ARGB.length; i++) {
            int code = ARGB[i];
            B[i] = (byte) ((code       ) & 0xFF);
            G[i] = (byte) ((code >>>=8 ) & 0xFF);
            R[i] = (byte) ((code >>>=8 ) & 0xFF);
            A[i] = (byte) ((code >>>=8 ) & 0xFF);
        }
        final int bits = ColorUtilities.getBitCount(ARGB.length);
        final int type = (ARGB.length <= 256) ? DataBuffer.TYPE_BYTE : DataBuffer.TYPE_USHORT;
        final boolean packed = (bits==1 || bits==2 || bits==4);
        final ImageTypeSpecifier its;
        if ((packed || bits==8 || bits==16) && (1 << bits) == ARGB.length) {
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
        colors    = new WeakReference<ColorModel>(its.getColorModel());
        specifier = new WeakReference<ImageTypeSpecifier>(its);
        return its;
    }

    /**
     * Retourne le type d'image associ� � cette palette.
     *
     * @throws  IOException             En cas d'erreur de lecture de la palette.
     * @throws  IIOException            En cas d'erreur logique de la palette.
     * @throws  FileNotFoundException   Si le fichier {@code palette} n'existe pas.
     */
    protected final ImageTypeSpecifier getImageTypeSpecifier() throws IOException {
        synchronized (POOL) {
            Palette candidate = POOL.get(this);
            if (candidate == null) {
                candidate = this;
                if (POOL.put(this, this) != null) {
                    // Ce cas ne devrait jamais se produire.
                    throw new AssertionError(this);
                }
            }
            return candidate.createImageTypeSpecifier();
        }
    }

    /**
     * Proc�de � la lecture des couleurs � partir des ressources de l'application.
     *
     * @throws  IOException             En cas d'erreur de lecture de la palette.
     * @throws  IIOException            En cas d'erreur logique de la palette.
     * @throws  FileNotFoundException   Si le fichier {@code palette} n'existe pas.
     */
    final Color[] readColors() throws IOException {
        final Color[] colors = Utilities.getPaletteFactory().getColors(palette);
        if (colors == null) {
            throw new FileNotFoundException("Palette non trouv�e : " + palette);
        }
        return colors;
    }

    /**
     * Retourne un type o� les donn�es manquantes sont repr�sent�es par la premi�re valeur d'index.
     * Cette premi�re valeur d'index sera toujours 0.
     * 
     * @param   palette Le nom de la palette qui permet de construire le mod�le ce couleurs.
     * @param   size    La borne sup�rieure de la plage des valeurs autoris�es (on consid�re 
     *                  celles-ci de 0 inclusivement � {@code size} exclusivement).
     * @return          Le type d'image correspondant � la palette.
     *
     * @throws  IOException             En cas d'erreur de lecture de la palette.
     * @throws  IIOException            En cas d'erreur logique de la palette.
     * @throws  FileNotFoundException   Si le fichier {@code palette} n'existe pas.
     */
    public static ImageTypeSpecifier forNodataFirst(final String palette, final int size) throws IOException {
        return new NodataFirst(palette, size).getImageTypeSpecifier();
    }

    /**
     * Fabrique de mod�les de couleurs o� les donn�es manquantes sont repr�sent�es par
     * la premi�re valeur d'index.
     */
    private static final class NodataFirst extends Palette {
        /**
         * Construit une palette du nom sp�cifi�.
         */
        public NodataFirst(final String name, final int size) {
            super(name, size);
        }

        /**
         * {@inheritDoc}
         */
        protected int[] createARGB() throws IOException {
            final Color[] colors = readColors();
            final int[] ARGB = new int[size];
            /*
             * On VEUT que la premi�re valeur du tableau ARGB soit 0,
             * car c'est la valeur signalant une absence de donn�es.
             */
            ColorUtilities.expand(colors, ARGB, 1, size);
            return ARGB;
        }
    }

    /**
     * Retourne un type o� les donn�es manquantes sont repr�sent�es par la derni�re valeur d'index.
     * Cette premi�re valeur d'index sera toujours {@code size-1}. Ce type d'image est utilis� pour
     * les concentrations en chlorophylle-<var>a</var> par exemple.
     *
     * @param   palette Le nom de la palette qui permet de construire le mod�le ce couleurs.
     * @param   size    La borne sup�rieure de la plage des valeurs autoris�es (on consid�re 
     *                  celles-ci de 0 inclusivement � {@code size} exclusivement).
     * @return          Le type d'image correspondant � la palette.
     *
     * @throws  IOException             En cas d'erreur de lecture de la palette.
     * @throws  IIOException            En cas d'erreur logique de la palette.
     * @throws  FileNotFoundException   Si le fichier {@code palette} n'existe pas.
     */
    public static ImageTypeSpecifier forNodataLast(final String palette, final int size) throws IOException {
        return new NodataLast(palette, size).getImageTypeSpecifier();
    }

    /**
     * Fabrique de mod�les de couleurs o� les donn�es manquantes sont repr�sent�es par
     * la derni�re valeur d'index.
     */
    private static final class NodataLast extends Palette {
        /**
         * Construit une palette du nom sp�cifi�.
         */
        public NodataLast(final String name, final int size) {
            super(name, size);
        }

        /**
         * {@inheritDoc}
         */
        protected int[] createARGB() throws IOException {
            final Color[] colors = readColors();
            final int[] ARGB = new int[size];
            /*
             * On VEUT que la derni�re valeur du tableau ARGB soit 0,
             * car c'est la valeur signalant une absence de donn�es.
             */
            ColorUtilities.expand(colors, ARGB, 0, (size-1));
            return ARGB;
        }
    }

    /**
     * Retourne un type o� la palette de couleurs est r�p�t�e un certain nombre de fois. Ce type
     * est utilis�e par exemple pour certaines images de SST de la Nasa, o� une valeur de qualit�
     * est compact�e avec la valeur de temp�rature.
     * 
     * @param   palette   Le nom de la palette qui permet de construire le mod�le ce couleurs.
     * @param   size      La borne sup�rieure de la plage des valeurs autoris�es (on consid�re 
     *                    celles-ci de 0 inclusivement � {@code size} exclusivement).
     * @param   validSize Le nombre de couleurs valides. Ce nombre doit �tre inf�rieur ou �gal
     *                    � {@code size}.
     * @param   pageCount Le nombre de fois que les couleurs doivent �tre r�p�t�es.
     * @return            Le type d'image correspondant � la palette.
     *
     * @throws  IOException             En cas d'erreur de lecture de la palette.
     * @throws  IIOException            En cas d'erreur de 'parsing' de la palette.
     * @throws  FileNotFoundException   Si le fichier {@code palette} n'existe pas.
     */
    public static ImageTypeSpecifier forRepeated(final String palette, final int size,
                                                 final int validSize, final int pageCount)
            throws IOException {
        return new Repeated(palette, size, validSize, pageCount).getImageTypeSpecifier();
    }

    /**
     * Fabrique de mod�les de couleurs o� une palette est r�p�t�e un certain nombre de fois.
     */
    private static final class Repeated extends Palette {
        /**
         * Le nombre de couleurs valides. Ce nombre doit �tre inf�rieur ou �gal � {@link #size}.
         * Pour les fichiers de SST de la Nasa, cette valeur est de 512.
         */
        private final int validSize;

        /**
         * Le nombre de fois que les couleurs doivent �tre r�p�t�es.
         * Pour les fichiers de SST de la Nasa, cette valeur est de 8.
         */
        private final int pageCount;

        /**
         * Construit une palette du nom sp�cifi�.
         */
        public Repeated(final String name, final int size, final int validSize, final int pageCount) {
            super(name, size);
            this.validSize = validSize;
            this.pageCount = pageCount;
        }

        /**
         * {@inheritDoc}
         */
        protected int[] createARGB() throws IOException {
            final Color[] colors = readColors();
            final int[] ARGB = new int[size];
            final int pageSize = size / pageCount;
            /* 
             * La plage des valeurs dans les images SST varie de 0 � 512...
             * on construit donc un mod�le de 512 couleurs.
             */
            ColorUtilities.expand(colors, ARGB, 0, validSize);
            /*
             * Les valeurs comprises entre 512 et 1024 correpondent � un absence de donn�es.
             */
            final Color[] c = new Color[] {Color.BLACK, Color.WHITE};
            ColorUtilities.expand(c, ARGB, validSize, pageSize);
            /* 
             * L'information de qualit� est cod� sur 3 bits... il y a donc 8 niveau de qualit�
             * On r�p�te donc le mod�le de couleurs huit fois.
             */
            for (int i=1; i<pageCount; i++) {
                System.arraycopy(ARGB, 0, ARGB, pageSize*i, pageSize);
            }
            return ARGB;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return super.hashCode() ^ (((pageCount*37) ^ validSize) * 37);
        }

        /**
         * {@inheritDoc}
         */
        @Override
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
     * Retourne une valeur repr�sentative de cette palette de couleurs.
     */
    @Override
    public int hashCode() {
        return getClass().hashCode() ^ palette.hashCode() ^ size;
    }

    /**
     * Compare cette palette avec la palette sp�cifi�e.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object != null && getClass().equals(object.getClass())) {
            final Palette that = (Palette) object;
            return this.size == that.size &&
                    org.geotools.resources.Utilities.equals(this.palette, that.palette);
        }
        return false;
    }

    /**
     * Retourne une repr�sentation textuelle de cette palette. Cette m�thode
     * est utilis�e principalement � des fins de d�boguages.
     */
    @Override
    public String toString() {
        return org.geotools.resources.Utilities.getShortClassName(this) + '[' + palette + ' ' + size + " couleurs]";
    }
}
