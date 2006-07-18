/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2001, Institut de Recherche pour le D�veloppement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.coverage;

// J2SE dependencies
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.util.Arrays;
import java.util.Map;

// JAI dependencies
import javax.media.jai.FloatDoubleColorModel;
import javax.media.jai.RasterFactory;

// Geotools dependencies
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.image.ColorUtilities;
import org.geotools.resources.image.ComponentColorModelJAI;
import org.geotools.util.WeakValueHashMap;


/**
 * A factory for {@link ColorModel} objects built from a list of {@link Category} objects.
 * This factory provides only one public static method: {@link #getColorModel}.  Instances
 * of {@link ColorModel} are shared among all callers in the running virtual machine.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class ColorModelFactory {
    /**
     * Mod�les de couleurs sugg�r�s pour l'affichage des cat�gories. Ces mod�les de couleurs
     * peuvent �tre construits � partir des couleurs qui ont �t� d�finies dans les diff�rentes
     * cat�gories du tableau {@link #categories}.
     */
    private static final Map colors = new WeakValueHashMap();

    /**
     * The list of categories for the construction of a single instance of a {@link ColorModel}.
     */
    private final Category[] categories;
    
    /**
     * The visible band (usually 0) used for the construction
     * of a single instance of a {@link ColorModel}.
     */
    private final int visibleBand;

    /**
     * The number of bands (usually 1) used for the construction
     * of a single instance of a {@link ColorModel}.
     */
    private final int numBands;

    /**
     * The color model type. One of {@link DataBuffer#TYPE_BYTE}, {@link DataBuffer#TYPE_USHORT},
     * {@link DataBuffer#TYPE_FLOAT} or {@link DataBuffer#TYPE_DOUBLE}.
     *
     * @task TODO: The user may want to set explicitly the number of bits each pixel occupied.
     *             We need to think about an API to allows that.
     */
    private final int type;

    /**
     * Construct a new {@code ColorModelFactory}. This object will actually be used
     * as a key in a {@link Map}, so this is not really a {@code ColorModelFactory}
     * but a kind of "{@code ColorModelKey}" instead. However, since this constructor
     * is private, user doesn't need to know that.
     */
    private ColorModelFactory(final Category[] categories, final int type,
                              final int visibleBand, final int numBands)
    {
        this.categories  = categories;
        this.visibleBand = visibleBand;
        this.numBands    = numBands;
        this.type        = type;
        if (visibleBand<0 || visibleBand>=numBands) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.BAD_BAND_NUMBER_$1,
                                               new Integer(visibleBand)));
        }
    }
    
    /**
     * Returns a color model for a category set. This method builds up the color model
     * from each category's colors (as returned by {@link Category#getColors}).
     *
     * @param  categories The set of categories.
     * @param  type The color model type. One of {@link DataBuffer#TYPE_BYTE},
     *         {@link DataBuffer#TYPE_USHORT}, {@link DataBuffer#TYPE_FLOAT} or
     *         {@link DataBuffer#TYPE_DOUBLE}.
     * @param  visibleBand The band to be made visible (usually 0). All other bands, if any
     *         will be ignored.
     * @param  numBands The number of bands for the color model (usually 1). The returned color
     *         model will renderer only the {@code visibleBand} and ignore the others, but
     *         the existence of all {@code numBands} will be at least tolerated. Supplemental
     *         bands, even invisible, are useful for processing with Java Advanced Imaging.
     * @return The requested color model, suitable for {@link RenderedImage} objects with values
     *         in the <code>{@link CategoryList#getRange}</code> range.
     */
    public static synchronized ColorModel getColorModel(final Category[] categories,
                                                        final int type,
                                                        final int visibleBand,
                                                        final int numBands)
    {
        ColorModelFactory key = new ColorModelFactory(categories, type, visibleBand, numBands);
        ColorModel model = (ColorModel) colors.get(key);
        if (model == null) {
            model = key.getColorModel();
            colors.put(key, model);
        }
        return model;
    }
    
    /**
     * Construct the color model.
     */
    private ColorModel getColorModel() {
		final int length = categories.length;
		if (type != DataBuffer.TYPE_BYTE && type != DataBuffer.TYPE_USHORT) {
			// If the requested type is any type not supported by
			// IndexColorModel,
			// fallback on a generic (but very slow!) color model.
			double min = 0;
			double max = 1;

			if (length != 0) {
				min = categories[0].minimum;
				for (int i = length; --i >= 0;) {
					final double val = categories[i].maximum;
					if (!Double.isNaN(val)) {
						max = val;
						break;
					}
				}
			}
            final int  transparency = Transparency.OPAQUE;
            final ColorSpace colors = new ScaledColorSpace(visibleBand, numBands, min, max);
            if (false) {
                // This is the J2SE implementation of color model. It should be our preferred one.
                // Unfortunatly, as of JAI 1.1 we have to use JAI implementation instead of J2SE's
                // one because javax.media.jai.iterator.RectIter do not work with J2SE's DataBuffer
                // when the data type is float or double.
                return new ComponentColorModel(colors, false, false, transparency, type);
            }
            if (false) {
                // This is the JAI implementation of color model. This implementation work with
                // JAI's RectIter and should in theory support float and double data buffer.
                // Unfortunatly, it seems to completly ignore our custom ColorSpace. We end
                // up basically with all-black or all-white images.
                return new FloatDoubleColorModel(colors, false, false, transparency, type);
            }
            if (true) {
                // Our patched color model extends J2SE's ComponentColorModel (which work correctly
                // with our custom ColorSpace), but create JAI's SampleModel instead of J2SE's one.
                // It make RectIter happy and display colors correctly.
                return new ComponentColorModelJAI(colors, false, false, transparency, type);
            }
            // This factory is not really different from a direct construction of
            // FloatDoubleColorModel. We provide it here just because we must end
            // with something.
            return RasterFactory.createComponentColorModel(type, colors, false, false, transparency);
        }
		if (numBands == 1 && length == 0) {
            // Construct a gray scale palette.
            final ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
            final int[] nBits = {DataBuffer.getDataTypeSize(type)};
            return new ComponentColorModel(cs, nBits, false, true, Transparency.OPAQUE, type);
        }
        /*
         * Computes the number of entries required for the color palette.
         * We take the upper range value of the last category.
         */
		final int mapSize = (int) Math.round(categories[length - 1].maximum) + 1;
        final int[]  ARGB = new int[mapSize];
        /*
         * Interpolate the colors in the color palette. Colors that do not fall
         * in the range of a category will be set to a transparent color.
         */
		for (int i = 0; i < length; i++) {
            final Category category = categories[i];
            ColorUtilities.expand(category.getColors(), ARGB,
                                  (int)Math.round(category.minimum),
                                  (int)Math.round(category.maximum)+1);
        }
        return ColorUtilities.getIndexColorModel(ARGB, numBands, visibleBand);
    }

    /**
     * Returns a hash code.
     */
    public int hashCode() {
        int code = 962745549 + (numBands*37 + visibleBand)*37 + categories.length;
		final int length = categories.length;
		for (int i = 0; i < length; i++) {
            code += categories[i].hashCode();
            // Better be independant of categories order.
        }
        return code;
    }

    /**
     * Check this object with an other one for equality.
     */
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (other instanceof ColorModelFactory) {
            final ColorModelFactory that = (ColorModelFactory) other;
            return this.numBands    == that.numBands    &&
                   this.visibleBand == that.visibleBand &&
                   Arrays.equals(this.categories, that.categories);
        }
        return false;
    }
}
