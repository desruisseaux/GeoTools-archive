/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
 * (C) 2005, Institut de Recherche pour le Développement
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
package org.geotools.parameter;

// J2SE dependencies and extensions
import java.net.URI;
import javax.units.Unit;

// JAI dependencies
import javax.media.jai.ParameterList;

// OpenGIS dependencies
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.InvalidParameterTypeException;
import org.opengis.parameter.InvalidParameterValueException;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * A particular parameter in a JAI's {@link ParameterList}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @since 2.2
 */
final class ImagingParameter extends AbstractParameter implements ParameterValue {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -170895429717041733L;

    /**
     * The JAI's parameter list used as the backing store for parameter values.
     */
    private final ParameterList parameters;

    /**
     * Creates a new parameter from the specified list.
     */
    public ImagingParameter(final ParameterDescriptor descriptor, final ParameterList parameters) {
        super(descriptor);
        this.parameters = parameters;
    }

    /**
     * Returns the exception to be throws for an operation on a wrong parameter type.
     */
    private InvalidParameterTypeException invalidType(final ClassCastException cause) {
        final InvalidParameterTypeException exception = new InvalidParameterTypeException(
                Resources.format(ResourceKeys.ERROR_ILLEGAL_OPERATION_FOR_VALUE_CLASS_$1,
                Utilities.getShortName(getType())), getName(descriptor));
        exception.initCause(cause);
        return exception;
    }

    /**
     * Returns the unlocalized operation name. This is different from
     * {@link AbstractParameter#getName}, which may returns a localized name.
     */
    private String getName() {
        return descriptor.getName().getCode();
    }

    /**
     * Returns the parameter type.
     */
    private Class getType() {
        return ((ParameterDescriptor)descriptor).getValueClass();
    }

    /**
     * Returns {@code null} since JAI's parameters have no units.
     */
    public Unit getUnit() {
        return null;
    }

    /**
     * Always throws an exception, since this parameter has no unit.
     */
    public double doubleValue(final Unit unit) throws InvalidParameterTypeException {
        throw unitlessParameter(descriptor);
    }

    /**
     * Returns the numeric value of the coordinate operation parameter.
     */
    public double doubleValue() throws InvalidParameterTypeException {
        final String name = getName();
        final Class  type = getType();
        try {
            if (type.equals(Float  .class)) parameters.getFloatParameter(name);
            if (type.equals(Long   .class)) parameters.getLongParameter (name);
            if (type.equals(Integer.class)) parameters.getIntParameter  (name);
            if (type.equals(Short  .class)) parameters.getShortParameter(name);
            if (type.equals(Byte   .class)) parameters.getByteParameter (name);
            return parameters.getDoubleParameter(name);
        } catch (ClassCastException exception) {
            throw invalidType(exception);
        }
    }

    /**
     * Returns the positive integer value of an operation parameter.
     */
    public int intValue() throws InvalidParameterTypeException {
        final String name = getName();
        final Class  type = getType();
        try {
            if (type.equals(Short.class)) parameters.getShortParameter(name);
            if (type.equals(Byte .class)) parameters.getByteParameter (name);
            return parameters.getIntParameter(name);
        } catch (ClassCastException exception) {
            throw invalidType(exception);
        }
    }

    /**
     * Returns the boolean value of an operation parameter.
     */
    public boolean booleanValue() throws InvalidParameterTypeException {
        final String name = getName();
        try {
            return parameters.getBooleanParameter(name);
        } catch (ClassCastException exception) {
            throw invalidType(exception);
        }
    }

    /**
     * Returns the string value of an operation parameter.
     */
    public String stringValue() throws InvalidParameterTypeException {
        final String name = getName();
        try {
            // Really cast to CharSequence (even if not needed for toString())
            // because we want the ClassCastException if the type mismatch.
            return ((CharSequence) parameters.getObjectParameter(name)).toString();
        } catch (ClassCastException exception) {
            throw invalidType(exception);
        }
    }

    /**
     * Always throws an exception, since this parameter has no unit.
     */
    public double[] doubleValueList(Unit unit) throws InvalidParameterTypeException {
        throw unitlessParameter(descriptor);
    }

    /**
     * Returns an ordered sequence of two or more numeric values of an operation parameter list.
     */
    public double[] doubleValueList() throws InvalidParameterTypeException {
        final String name = getName();
        try {
            return (double[]) parameters.getObjectParameter(name);
        } catch (ClassCastException exception) {
            throw invalidType(exception);
        }
    }

    /**
     * Returns an ordered sequence of two or more integer values of an operation parameter list.
     */
    public int[] intValueList() throws InvalidParameterTypeException {
        final String name = getName();
        try {
            return (int[]) parameters.getObjectParameter(name);
        } catch (ClassCastException exception) {
            throw invalidType(exception);
        }
    }

    /**
     * Returns a reference to a file or a part of a file containing one or more parameter value.
     *
     * @todo Add automatic conversions, if it appears usefull for JAI parameters.
     */
    public URI valueFile() throws InvalidParameterTypeException {
        final String name = getName();
        try {
            return (URI) parameters.getObjectParameter(name);
        } catch (ClassCastException exception) {
            throw invalidType(exception);
        }
    }

    /**
     * Returns the parameter value as an object. The object type is typically a {@link Double},
     * {@link Integer}, {@link Boolean}, {@link String}, {@link URI}, {@code double[]} or
     * {@code int[]}.
     */
    public Object getValue() {
        final String name = getName();
        try {
            return parameters.getObjectParameter(name);
        } catch (IllegalStateException ignore) {
            return null;
        }
    }

    /**
     * Always throws an exception, since this parameter has no unit.
     */
    public void setValue(final double value, Unit unit) throws InvalidParameterValueException {
        throw unitlessParameter(descriptor);
    }

    /**
     * Set the parameter value as a floating point.
     */
    public void setValue(final double value) throws InvalidParameterValueException {
        final String name = getName();
        final Class  type = getType();
        try {
            if (type.equals(Float  .class)) {parameters.setParameter(name, (float) value); return;}
            if (type.equals(Long   .class)) {parameters.setParameter(name, (long)  value); return;}
            if (type.equals(Integer.class)) {parameters.setParameter(name, (int)   value); return;}
            if (type.equals(Short  .class)) {parameters.setParameter(name, (short) value); return;}
            if (type.equals(Byte   .class)) {parameters.setParameter(name, (byte)  value); return;}
            parameters.setParameter(name, value);
        } catch (ClassCastException exception) {
            throw invalidType(exception);
        }
    }

    /**
     * Set the parameter value as an integer.
     */
    public void setValue(final int value) throws InvalidParameterValueException {
        final String name = getName();
        final Class  type = getType();
        try {
            if (type.equals(Short.class)) {parameters.setParameter(name, (short) value); return;}
            if (type.equals(Byte .class)) {parameters.setParameter(name, (byte)  value); return;}
            parameters.setParameter(name, value);
        } catch (ClassCastException exception) {
            throw invalidType(exception);
        }
    }

    /**
     * Set the parameter value as a boolean.
     */
    public void setValue(final boolean value) throws InvalidParameterValueException {
        final String name = getName();
        try {
            parameters.setParameter(name, value);
        } catch (ClassCastException exception) {
            throw invalidType(exception);
        }
    }

    /**
     * Set the parameter value as an object. The object type is typically a {@link Double},
     * {@link Integer}, {@link Boolean}, {@link String}, {@link URI}, {@code double[]}
     * or {@code int[]}.
     */
    public void setValue(final Object value) throws InvalidParameterValueException {
        final String name = getName();
        try {
            parameters.setParameter(name, value);
        } catch (ClassCastException exception) {
            throw invalidType(exception);
        }
    }

    /**
     * Always throws an exception, since this parameter has no unit.
     */
    public void setValue(double[] values, Unit unit) throws InvalidParameterValueException {
        throw unitlessParameter(descriptor);
    }
    
    /**
     * Compares the specified object with this parameter for equality.
     */
    public boolean equals(final Object object) {
        if (object == this) {
            // Slight optimization
            return true;
        }
        if (super.equals(object)) {
            final ImagingParameter that = (ImagingParameter) object;
            return Utilities.equals(this.getValue(), that.getValue());
        }
        return false;
    }

    /**
     * Returns a hash value for this parameter.
     */
    public int hashCode() {
        int code = super.hashCode()*37;
        final Object value = getValue();
        if (value != null) {
            code += value.hashCode();
        }
        return code ^ (int)serialVersionUID;
    }
}
