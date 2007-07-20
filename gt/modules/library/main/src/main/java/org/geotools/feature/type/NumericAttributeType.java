/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *    
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.feature.type;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.geotools.feature.DefaultAttributeType;
import org.geotools.feature.PrimativeAttributeType;
import org.opengis.filter.Filter;

/**
 * Class that represents a Numeric.
 *
 * @author Ian Schneider
 * @author Chris Holmes, TOPP
 * @source $URL$
 */
public class NumericAttributeType extends DefaultAttributeType implements PrimativeAttributeType {
    /**
     * Constructor with name, type and nillable.  Type should always be a
     * Number class.
     *
     * @param name Name of this attribute.
     * @param type Class type of this attribute.
     * @param nillable If nulls are allowed for the attribute of this type.
     * @param min 
     * @param max 
     * @param defaultValue default value when none is suppled
     * @param filter
     *
     * @throws IllegalArgumentException is type is not a Number.
     *
     * @task REVISIT: protected?
     */
    public NumericAttributeType(String name, 
                                Class type, 
                                boolean nillable,
                                int min, 
                                int max, 
                                Object defaultValue, 
                                Filter filter)
        throws IllegalArgumentException {
        super(name, type, nillable, min, max,  defaultValue);
        this.filter = filter;
        if (!Number.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException(
                "Numeric requires Number class, " + "not " + type);
        }
    }
    private Filter filter;
    public NumericAttributeType(String name, Class type, boolean nillable,
         Object defaultValue,Filter filter)
        throws IllegalArgumentException {
        super(name, type, nillable, defaultValue);
        this.filter = filter;
        if (!Number.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException(
                "Numeric requires Number class, " + "not " + type);
        }
    }

    /**
     * Allows this AttributeType to convert an argument to its prefered storage
     * type. If no parsing is possible, returns the original value. If a parse
     * is attempted, yet fails (i.e. a poor decimal format) throw the
     * Exception. This is mostly for use internally in Features, but
     * implementors should simply follow the rules to be safe.
     *
     * @param value the object to attempt parsing of.
     *
     * @return <code>value</code> converted to the preferred storage of this
     *         <code>AttributeType</code>.  If no parsing was possible then
     *         the same object is returned.
     *
     * @throws IllegalArgumentException if parsing is attempted and is
     *         unsuccessful.
     *
     * @task REVISIT: When type is Number, should we always be using Double?
     *       (What else would we do? - IanS)
     */
    public Object parse(Object value) throws IllegalArgumentException {
        // handle null values first
        if (value == null) {
            return value;
        }

        // no parse needed here if types are compatable
        if ((value.getClass() == getBinding())
                || getBinding().isAssignableFrom(value.getClass())) {
            return value;
        }

        // convert one Number to our preferred type
        if (value instanceof Number) {
            return convertNumber((Number) value);
        }

        // parse a String to our preferred type
        // note, this is the final parsing attempt !
        String str = value.toString();
        
        //JD: trim any spaces off
        str = str.trim();

        try {
            Object parsed = parseFromString(str);

            if (parsed != null) {
                return parsed;
            }
        } catch (IllegalArgumentException iae) {
            // do nothing
        }

        // check empty string or black space
        if ((str.length() == 0) || (str.trim().length() == 0)) {
            Object parsed = parseFromString("0");

            if (parsed != null) {
                return parsed;
            }
        }

        // nothing else to do
        throw new IllegalArgumentException("Cannot parse " + value.getClass());
    }

    /**
     * Duplicate the given Object. In this case, since Number classes are
     * immutable, lets return the Object.
     *
     * @param o DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Object duplicate(Object o) {
        return o;
    }

    protected Object parseFromString(String value)
        throws IllegalArgumentException {
        if (getBinding() == Byte.class) {
            return Byte.decode(value);
        }

        if (getBinding() == Short.class) {
            return Short.decode(value);
        }

        if (getBinding() == Integer.class) {
            return Integer.decode(value);
        }

        if (getBinding() == Float.class) {
            return Float.valueOf(value);
        }

        if (getBinding() == Double.class) {
            return Double.valueOf(value);
        }

        if (getBinding() == Long.class) {
            return Long.decode(value);
        }

        if (getBinding() == BigInteger.class) {
            return new BigInteger(value);
        }

        if (getBinding() == BigDecimal.class) {
            return new BigDecimal(value);
        }

        if (Number.class.isAssignableFrom(getBinding())) {
            return new Double(value);
        }

        return null;
    }

    protected Object convertNumber(Number number) {
        if (getBinding() == Byte.class) {
            return new Byte(number.byteValue());
        }

        if (getBinding() == Short.class) {
            return new Short(number.shortValue());
        }

        if (getBinding() == Integer.class) {
            return new Integer(number.intValue());
        }

        if (getBinding() == Float.class) {
            return new Float(number.floatValue());
        }

        if (getBinding() == Double.class) {
            return new Double(number.doubleValue());
        }

        if (getBinding() == Long.class) {
            return new Long(number.longValue());
        }

        if (getBinding() == BigInteger.class) {
            return BigInteger.valueOf(number.longValue());
        }

        if (getBinding() == BigDecimal.class) {
            return BigDecimal.valueOf(number.longValue());
        }

        throw new RuntimeException("AttributeGT.Numeric cannot parse " + number);
    }

	/* (non-Javadoc)
	 * @see org.geotools.feature.PrimativeAttributeType#getRestriction()
	 */
	public Filter getRestriction() {
		return filter;
	}


}
