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

// J2SE dependencies
import java.io.Serializable;

// OpenGIS dependencies
import org.opengis.parameter.GeneralParameterDescriptor;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Abstract parameter value or group of parameter values.
 *  
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see org.geotools.parameter.GeneralParameterDescriptor
 */
public class GeneralParameterValue implements org.opengis.parameter.GeneralParameterValue, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 8458179223988766398L;

    /**
     * The abstract definition of this parameter or group of parameters.
     */
    final GeneralParameterDescriptor descriptor;

    /**
     * Construct a parameter value from the specified descriptor.
     *
     * @param descriptor The abstract definition of this parameter or group of parameters.
     */
    protected GeneralParameterValue(GeneralParameterDescriptor descriptor) {
        this.descriptor = descriptor;
        ensureNonNull("descriptor", descriptor);
    }
    
    /**
     * Returns the abstract definition of this parameter or group of parameters.
     *
     * @return The abstract definition of this parameter or group of parameters.
     */
    public GeneralParameterDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Returns a copy of this parameter value or group.
     *
     * @return A copy of this parameter value or group.
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException exception) {
            // Should not happen, since we are cloneable
            throw new AssertionError(exception);
        }
    }
    
    /**
     * Makes sure that an argument is non-null. This method was already defined in
     * {@link org.geotools.referencing.IdentifiedObject}, but is defined here again in order
     * to get a more appropriate stack trace, and for access by class which do not inherit
     * from {@link org.geotools.referencing.IdentifiedObject}.
     *
     * @param  name   Argument name.
     * @param  object User argument.
     * @throws IllegalArgumentException if <code>object</code> is null.
     */
    static void ensureNonNull(final String name, final Object object)
            throws IllegalArgumentException
    {
        if (object == null) {
            throw new IllegalArgumentException(Resources.format(
                      ResourceKeys.ERROR_NULL_ARGUMENT_$1, name));
        }
    }
    
    /**
     * Makes sure an array element is non-null. This is
     * a convenience method for subclass constructors.
     *
     * @param  name  Argument name.
     * @param  array User argument.
     * @param  index Index of the element to check.
     * @throws IllegalArgumentException if <code>array[i]</code> is null.
     */
    static void ensureNonNull(final String name, final Object[] array, final int index)
        throws IllegalArgumentException
    {
        if (array[index] == null) {
            throw new IllegalArgumentException(Resources.format(
                      ResourceKeys.ERROR_NULL_ARGUMENT_$1, name+'['+index+']'));
        }
    }

    /**
     * Verify that the specified value is of the specified class.
     *
     * @param  valueClass the expected class.
     * @param  value The expected value, or <code>null</code>.
     * @throws IllegalArgumentException if <code>value</code> is non-null and has a non-assignable
     *         class.
     */
    static void ensureValidClass(final Class valueClass, final Object value)
            throws IllegalArgumentException
    {
        if (value != null) {
            if (!valueClass.isAssignableFrom(value.getClass())) {
                throw new IllegalArgumentException(Resources.format(
                          ResourceKeys.ERROR_ILLEGAL_CLASS_$2,
                          Utilities.getShortClassName(value), Utilities.getShortName(valueClass)));
            }
        }
    }
    
    /**
     * Compares the specified object with this parameter for equality.
     *
     * @param  object The object to compare to <code>this</code>.
     * @return <code>true</code> if both objects are equal.
     */
    public boolean equals(final Object object) {
        if (object!=null && object.getClass().equals(getClass())) {
            final GeneralParameterValue that = (GeneralParameterValue) object;
            return Utilities.equals(this.descriptor, that.descriptor);
        }
        return false;
    }
    
    /**
     * Returns a hash value for this parameter.
     *
     * @return The hash code value. This value doesn't need to be the same
     *         in past or future versions of this class.
     */
    public int hashCode() {
        return descriptor.hashCode() ^ (int)serialVersionUID;
    }
}
