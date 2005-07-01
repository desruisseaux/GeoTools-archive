/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2003, Institut de Recherche pour le Développement
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
package org.geotools.image.jai;

// J2SE dependencies
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

// JAI dependencies
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.registry.RenderedRegistryMode;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.gcs.ResourceKeys;
import org.geotools.resources.gcs.Resources;


/**
 * The operation descriptor for the {@link Combine} operation. While this descriptor declare
 * to support 0 {@link RenderedImage} sources, an arbitrary amount of sources can really be
 * specified. The "0" should be understood as the <em>minimal</em> number of sources required.
 *
 * @version $Id$
 * @author Remi Eve
 * @author Martin Desruisseaux
 *
 * @since 2.1
 */
public class CombineDescriptor extends OperationDescriptorImpl {
    /**
     * The operation name, which is {@value}.
     */
    public static final String OPERATION_NAME = "org.geotools.Combine";

    /**
     * Constructs the descriptor.
     */
    public CombineDescriptor() {
        super(new String[][]{{"GlobalName",  OPERATION_NAME},
                             {"LocalName",   OPERATION_NAME},
                             {"Vendor",      "org.geotools"},
                             {"Description", "Combine rendered images using a linear relation."},
                             {"DocURL",      "http://www.geotools.org/"}, // TODO: provides more accurate URL
                             {"Version",     "1.0"},
                             {"arg0Desc",    "The coefficients for linear combinaison as a matrix."},
                             {"arg1Desc",    "An optional transform to apply on sample values "+
                                             "before the linear combinaison."}},
              new String[]   {RenderedRegistryMode.MODE_NAME}, 0,        // Supported modes
              new String[]   {"matrix", "transform"},                    // Parameter names
              new Class []   {double[][].class, CombineTransform.class}, // Parameter classes
              new Object[]   {NO_PARAMETER_DEFAULT, null},               // Default value
              null                                                       // Valid parameter values
        );
    }

    /**
     * Returns {@code true} if this operation supports the specified mode, and
     * is capable of handling the given input source(s) for the specified mode. 
     *
     * @param modeName The mode name (usually "Rendered").
     * @param param The parameter block for the operation to performs.
     * @param message A buffer for formatting an error message if any.
     */
    protected boolean validateSources(final String      modeName,
                                      final ParameterBlock param,
                                      final StringBuffer message)
    {
        if (super.validateSources(modeName, param, message)) {
            for (int i=param.getNumSources(); --i>=0;) {
                final Object source = param.getSource(i);
                if (!(source instanceof RenderedImage)) {
                    message.append(Resources.format(ResourceKeys.ERROR_BAD_PARAMETER_TYPE_$2,
                                   "source"+i, Utilities.getShortClassName(source)));
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Returns {@code true} if the parameters are valids. This implementation check
     * that the number of bands in the source src1 is equals to the number of bands of 
     * source src2.
     *
     * @param modeName The mode name (usually "Rendered").
     * @param param The parameter block for the operation to performs.
     * @param message A buffer for formatting an error message if any.
     */
    protected boolean validateParameters(final String      modeName,
                                         final ParameterBlock param,
                                         final StringBuffer message)
    {
        if (!super.validateParameters(modeName, param, message))  {
            return false;
        }
        final double[][] matrix = (double[][]) param.getObjectParameter(0);
        int numSamples = 1; // Begin at '1' for the offset value.
        for (int i=param.getNumSources(); --i>=0;) {
            numSamples += ((RenderedImage) param.getSource(i)).getSampleModel().getNumBands();
        }
        for (int i=0; i<matrix.length; i++) {
            if (matrix[i].length != numSamples) {
                message.append(Resources.format(ResourceKeys.ERROR_UNEXPECTED_ROW_LENGTH_$3,
                        new Integer(i), new Integer(matrix[i].length), new Integer(numSamples)));
                return false;
            }
        }
        return true;
    }
}
