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
import javax.units.Unit;
import org.opengis.util.CodeList;



/**
 * The definition of a parameter used by an operation method.
 *
 * @deprecated Renamed as {@link DefaultParameterDescriptor}
 */
public class ParameterDescriptor extends DefaultParameterDescriptor {
    /**
     * Constructs a mandatory parameter for a range of integer values.
     */
    public ParameterDescriptor(final String name,
                               final int defaultValue,
                               final int minimum,
                               final int maximum)
    {
        super(name, defaultValue, minimum, maximum);
    }

    /**
     * Constructs a parameter for a range of integer values.
     */
    public ParameterDescriptor(final Map properties,
                               final int defaultValue,
                               final int minimum,
                               final int maximum,
                               final boolean required)
    {
        super(properties, defaultValue, minimum, maximum, required);
    }

    /**
     * Constructs a mandatory parameter for a range of floating point values.
     */
    public ParameterDescriptor(final String name,
                               final double defaultValue,
                               final double minimum,
                               final double maximum,
                               final Unit   unit)
    {
        super(name, defaultValue, minimum, maximum, unit);
    }

    /**
     * Constructs a parameter for a range of floating point values.
     */
    public ParameterDescriptor(final Map     properties,
                               final double  defaultValue,
                               final double  minimum,
                               final double  maximum,
                               final Unit    unit,
                               final boolean required)
    {
        super(properties, defaultValue, minimum, maximum, unit, required);
    }

    /**
     * Constructs a mandatory parameter from a range of comparable objects.
     */
    public ParameterDescriptor(final String     name,
                               final Class      valueClass,
                               final Comparable defaultValue,
                               final Comparable minimum,
                               final Comparable maximum,
                               final Unit       unit)
    {
        super(name, valueClass, defaultValue, minimum, maximum, unit);
    }

    /**
     * Constructs a parameter for a name and a default value.
     */
    public ParameterDescriptor(final String       name,
                               final CharSequence remarks,
                               final Object       defaultValue,
                               final boolean      required)
    {
        super(name, remarks, defaultValue, required);
    }

    /**
     * Constructs a parameter for a code list.
     */
    public ParameterDescriptor(final String   name,
                               final CodeList defaultValue)
    {
        super(name, defaultValue);
    }

    /**
     * Constructs a mandatory parameter for a set of predefined values.
     */
    public ParameterDescriptor(final String   name,
                               final Class    valueClass,
                               final Object[] validValues,
                               final Object   defaultValue)
    {
        super(name, valueClass, validValues, defaultValue);
    }

    /**
     * Constructs a parameter from a set of properties.
     */
    public ParameterDescriptor(final Map        properties,
                               final Class      valueClass,
                               final Object[]   validValues,
                               final Object     defaultValue,
                               final Comparable minimum,
                               final Comparable maximum,
                               final Unit       unit,
                               final boolean    required)
    {
        super(properties, valueClass, validValues, defaultValue, minimum, maximum, unit, required);
    }

    /**
     * Constructs a parameter from a set of properties.
     */
    public ParameterDescriptor(final Map        properties,
                               final boolean    required,
                                     Class      valueClass,
                               final Object[]   validValues,
                               final Object     defaultValue,
                               final Comparable minimum,
                               final Comparable maximum,
                               final Unit       unit)
    {
        super(properties, required, valueClass, validValues, defaultValue, minimum, maximum, unit);
    }
}
