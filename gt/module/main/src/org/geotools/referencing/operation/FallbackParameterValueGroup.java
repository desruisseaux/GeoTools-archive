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
 */
package org.geotools.referencing.operation;

// J2SE dependencies
import java.util.Collections;

// OpenGIS dependencies
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.OperationParameterGroup;
import org.opengis.parameter.ParameterNotFoundException;

// Geotools dependencies
import org.geotools.parameter.ParameterValueGroup;


/**
 * A parameter value group which return default values when an unknow parameter
 * was requested.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class FallbackParameterValueGroup extends ParameterValueGroup {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -8246501404619542820L;

    /**
     * The expected parameter descriptor. Will be used in order to create default values
     * when the requested value was not found.
     */
    private final OperationParameterGroup fallback;
    
    /**
     * Construct a parameter group from the specified list of parameters.
     * If a requested parameters was not found, the supplied descriptor
     * will be used in order to create a default value.
     *
     * @param fallback The expected descriptor.
     * @param values The list of parameters.
     */
    public FallbackParameterValueGroup(final OperationParameterGroup fallback,
                                       final GeneralParameterValue[] values)
    {
        super(Collections.singletonMap("name", fallback.getName(null)), values);
        this.fallback = fallback;
    }

    /**
     * Returns the first value in this group for the specified name.
     *
     * @param  name The case insensitive name of the parameter to search for.
     * @return The parameter value for the given name.
     * @throws ParameterNotFoundException if there is no parameter for the given name.
     */
    public ParameterValue getValue(final String name) throws ParameterNotFoundException {
        try {
            return super.getValue(name);
        } catch (ParameterNotFoundException exception) {
            try {
                // Remove cast if covariance is allowed.
                return (ParameterValue) fallback.getParameter(name).createValue();
            } catch (ParameterNotFoundException ignore) {
                throw exception;
            }
        }
    }
}
