/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.coverage.processing;

// J2SE dependencies
import java.awt.Color;
import java.io.Writer;

// JAI dependencies
import javax.media.jai.EnumeratedParameter;
import javax.media.jai.Interpolation;
import javax.media.jai.KernelJAI;

// Geotools dependencies
import org.geotools.coverage.AbstractCoverage;
import org.geotools.parameter.ParameterWriter;
import org.geotools.resources.image.ImageUtilities;


/**
 * Format grid coverage operation parameters in a tabular format.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class CoverageParameterWriter extends ParameterWriter {
    /**
     * Creates a new formatter writting parameters to the specified output stream.
     */
    public CoverageParameterWriter(final Writer out) {
        super(out);
    }

    /**
     * Format the specified value as a string.
     */
    protected String formatValue(final Object value) {
        if (KernelJAI.GRADIENT_MASK_SOBEL_HORIZONTAL.equals(value)) {
            return "GRADIENT_MASK_SOBEL_HORIZONTAL";
        }
        if (KernelJAI.GRADIENT_MASK_SOBEL_VERTICAL.equals(value)) {
            return "GRADIENT_MASK_SOBEL_VERTICAL";
        }
        if (value instanceof AbstractCoverage) {
            return ((AbstractCoverage) value).getName().toString(getLocale());
        }
        if (value instanceof Interpolation) {
            return ImageUtilities.getInterpolationName((Interpolation) value);
        }
        if (value instanceof EnumeratedParameter) {
            return ((EnumeratedParameter) value).getName();
        }
        if (value instanceof Color) {
            final Color c = (Color) value;
            return "RGB["+c.getRed()+','+c.getGreen()+','+c.getBlue()+']';
        }
        return super.formatValue(value);
    }
}
