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
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;

// Geotools dependencies
import org.geotools.parameter.ParameterGroup;
import org.geotools.referencing.IdentifiedObject;


/**
 * A parameter value group which return default values when an unknow parameter
 * was requested.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class FallbackParameterValueGroup extends ParameterGroup {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -8246501404619542820L;

    /**
     * The expected parameter descriptor. Will be used in order to create default values
     * when the requested value was not found.
     */
    protected final ParameterDescriptorGroup fallback;
    
    /**
     * Construct a parameter group from the specified list of parameters.
     * If a requested parameters was not found, the supplied descriptor
     * will be used in order to create a default value.
     *
     * @param fallback The expected descriptor.
     * @param values The list of parameters.
     */
    public FallbackParameterValueGroup(final ParameterDescriptorGroup fallback,
                                       final GeneralParameterValue[] values)
    {
        super(Collections.singletonMap(IdentifiedObject.NAME_PROPERTY,
                                       fallback.getName()), values);
        this.fallback = fallback;
    }

    /**
     * Returns the first value in this group for the specified name.
     *
     * @param  name The case insensitive name of the parameter to search for.
     * @return The parameter value for the given name.
     * @throws ParameterNotFoundException if there is no parameter for the given name.
     */
    public ParameterValue parameter(final String name) throws ParameterNotFoundException {
        try {
            return super.parameter(name);
        } catch (ParameterNotFoundException exception) {
            try {
                // Note: Remove cast if covariance is allowed (with J2SE 1.5).
                return (ParameterValue) fallback.descriptor(name).createValue();
            } catch (ParameterNotFoundException ignore) {
                // Rethrows the original exception (not the one from the fallback).
                throw exception;
            }
        }
    }
}
