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
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.parameter;

import java.util.Map;
import org.opengis.parameter.GeneralParameterDescriptor;


/**
 * The definition of a group of related parameters used by an operation method.
 *
 * @deprecated Renamed as {@link DefaultParameterDescriptorGroup}.
 */
public class ParameterDescriptorGroup extends DefaultParameterDescriptorGroup {
    /**
     * Constructs a parameter group from a name.
     */
    public ParameterDescriptorGroup(final String name,
                                    final GeneralParameterDescriptor[] parameters)
    {
        super(name, parameters);
    }

    /**
     * Constructs a parameter group from a set of properties.
     */
    public ParameterDescriptorGroup(final Map properties,
                                    final GeneralParameterDescriptor[] parameters)
    {
        super(properties, parameters);
    }

    /**
     * Constructs a parameter group from a set of properties.
     */
    public ParameterDescriptorGroup(final Map properties,
                                    final int minimumOccurs,
                                    final int maximumOccurs,
                                    GeneralParameterDescriptor[] parameters)
    {
        super(properties, minimumOccurs, maximumOccurs, parameters);
    }
}
