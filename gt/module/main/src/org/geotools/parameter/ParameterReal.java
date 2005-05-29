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

import org.opengis.parameter.ParameterDescriptor;


/**
 * A parameter value as a real number.
 *
 * @deprecated Renamed as {@link FloatParameter}.
 */
public class ParameterReal extends FloatParameter {
    /**
     * Constructs a parameter from the specified descriptor.
     */
    public ParameterReal(final ParameterDescriptor descriptor) {
        super(descriptor);
    }

    /**
     * Constructs a parameter from the specified descriptor and value.
     */
    public ParameterReal(final ParameterDescriptor descriptor, final double value) {
        super(descriptor, value);
    }
}
