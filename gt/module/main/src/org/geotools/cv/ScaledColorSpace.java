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
package org.geotools.cv;

// J2SE dependencies
import java.awt.color.ColorSpace;

import org.geotools.resources.Utilities;


/**
 * Espace de couleurs pour les images dont les valeurs
 * de pixels se situent entre deux nombre r�els.
 *
 * NOTE: Actual implementation is a copy of org.geotools.io.image.ScaledColorSpace.
 *       Future implementation will be differents (interpolate in a color table
 *       instead of computing grayscales).
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Replaced by {@link org.geotools.coverage.ScaledColorSpace}
 *             in the <code>org.geotools.coverage</code> package.
 */
public final class ScaledColorSpace extends ColorSpace {
    /**
     * Minimal normalized RGB value.
     */
    private static final float MIN_VALUE = 0f;
    
    /**
     * Maximal normalized RGB value.
     */
    private static final float MAX_VALUE = 1f;

    /**
     * The band to make visible (usually 0).
     */
    private final int band;
    
    /**
     * Facteur par lequel multiplier les pixels.
     */
    private final float scale;
    
    /**
     * Nombre � aditionner aux pixels apr�s
     * les avoir multiplier par {@link #scale}.
     */
    private final float offset;
    
    /**
     * Construit un mod�le de couleurs.
     *
     * @param band La bande � rendre visible (habituellement 0).
     * @param numComponents Nombre de composante (seule la premi�re sera prise en compte).
     * @param minimum La valeur g�ophysique minimale.
     * @param maximum La valeur g�ophysique maximale.
     */
    public ScaledColorSpace(final int band, final int numComponents,
                            final double minimum, final double maximum)
    {
        super(TYPE_GRAY, numComponents);
        this.band = band;
        final double scale  = (maximum-minimum)/(MAX_VALUE-MIN_VALUE);
        final double offset = minimum - MIN_VALUE*scale;
        this.scale  = (float)scale;
        this.offset = (float)offset;
    }
    
    /**
     * Retourne une couleur RGB en tons de
     * gris pour le nombre r�el sp�cifi�.
     */
    public float[] toRGB(final float[] values) {
        float value = (values[band]-offset)/scale;
        if (Float.isNaN(value)) value=MIN_VALUE;
        return new float[] {value, value, value};
    }
    
    /**
     * Retourne une valeur r�elle pour
     * le ton de gris sp�cifi�.
     */
    public float[] fromRGB(final float[] RGB) {
        final float[] values = new float[getNumComponents()];
        values[band] = (RGB[0]+RGB[1]+RGB[2])/3*scale + offset;
        return values;
    }
    
    /**
     * Convertit les valeurs en couleurs dans l'espace CIEXYZ.
     */
    public float[] toCIEXYZ(final float[] values) {
        float value = (values[band]-offset)/scale;
        if (Float.isNaN(value)) value=MIN_VALUE;
        return new float[] {
            value*0.9642f,
            value*1.0000f,
            value*0.8249f
        };
    }
    
    /**
     * Convertit les couleurs de l'espace CIEXYZ en valeurs.
     */
    public float[] fromCIEXYZ(final float[] RGB) {
        final float[] values = new float[getNumComponents()];
        values[band] = (RGB[0]/0.9642f + RGB[1] + RGB[2]/0.8249f)/3*scale + offset;
        return values;
    }
    
    /**
     * Retourne la valeur minimale autoris�e.
     */
    public float getMinValue(final int component) {
        return MIN_VALUE*scale + offset;
    }
    
    /**
     * Retourne la valeur maximale autoris�e.
     */
    public float getMaxValue(final int component) {
        return MAX_VALUE*scale + offset;
    }
    
    /**
     * Returns a string representation of this color model.
     */
    public String toString() {
        return Utilities.getShortClassName(this)+'['+getMinValue(band)+", "+getMaxValue(band)+']';
    }
}
