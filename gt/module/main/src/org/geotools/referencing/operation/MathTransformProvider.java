/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le Développement
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
package org.geotools.referencing.operation;

// J2SE dependencies
import java.util.Map;
import java.util.Collections;

// OpenGIS dependencies
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.GeneralOperationParameter;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;


/**
 * An {@linkplain OperationMethod operation method} capable to creates a {@linkplain MathTransform
 * math transform} from set of {@linkplain GeneralParameterValue parameter values}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class MathTransformProvider extends OperationMethod {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 440922384162006481L;

    /**
     * Construct a math transform provider from a name.
     *
     * @param name The operation name.
     * @param sourceDimensions Number of dimensions in the source CRS of this operation method.
     * @param targetDimensions Number of dimensions in the target CRS of this operation method.
     * @param parameters The set of parameters, or <code>null</code> or an empty array if none.
     */
    public MathTransformProvider(final String name,
                                 final int sourceDimensions,
                                 final int targetDimensions,
                                 final GeneralOperationParameter[] parameters)
    {
        this(Collections.singletonMap("name", name), sourceDimensions, targetDimensions, parameters);
    }

    /**
     * Construct a math transform provider from a set of properties.
     * The properties map is given unchanged to the
     * {@linkplain OperationMethod#OperationMethod(Map,int,int,GeneralOperationParameter[])
     * super-class constructor}.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param sourceDimensions Number of dimensions in the source CRS of this operation method.
     * @param targetDimensions Number of dimensions in the target CRS of this operation method.
     * @param parameters The set of parameters, or <code>null</code> or an empty array if none.
     */
    public MathTransformProvider(final Map properties,
                                 final int sourceDimensions,
                                 final int targetDimensions,
                                 final GeneralOperationParameter[] parameters)
    {
        super(properties, sourceDimensions, targetDimensions, parameters);
    }

    /**
     * Creates a math transform from the specified set of parameter values.
     * The default implementation wraps the parameters in a group and invokes
     * {@link #createMathTransform(ParameterValueGroup)}.
     *
     * @param  parameters The parameter values.
     * @return The created math transform.
     */
    public MathTransform createMathTransform(GeneralParameterValue[] parameters) {
        if (parameters.length == 1) {
            final GeneralParameterValue param = parameters[0];
            if (param instanceof ParameterValueGroup) {
                return createMathTransform((ParameterValueGroup) param);
            }
        }
        return createMathTransform(new org.geotools.parameter.ParameterValueGroup(
                Collections.singletonMap("name", getName(null)), parameters));
    }

    /**
     * Creates a math transform from the specified group of parameter values.
     *
     * @param  parameters The group of parameter values.
     * @return The created math transform.
     */
    public abstract MathTransform createMathTransform(ParameterValueGroup parameters);
}
