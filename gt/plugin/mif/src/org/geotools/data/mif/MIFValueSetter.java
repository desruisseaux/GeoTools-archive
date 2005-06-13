/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.data.mif;

/**
 * Utility class for setting object values from strings
 *
 * @author Luca S. Percich, AMA-MI
 */
public abstract class MIFValueSetter {
    protected String strValue = null; // The object value as string
    protected Object objValue = null; // the object value
    private String defaultValue = ""; // String representation of the default value (must be correctly converted into object by the stringToValue method!!!)
    private String errorMessage = "";

    /**
     * The constructor accepts a default value for the ValueSetter
     *
     * @param defa String representation of the default value
     */
    public MIFValueSetter(String defa) {
        defaultValue = defa;
    }

    /**
     * Sets the value as a String. After a setString call, getValue() can be
     * used to access the converted value.
     *
     * @param value String representation of the object value
     *
     * @return true if conversion was successful
     */
    public final boolean setString(String value) {
        strValue = value;

        try {
            stringToValue();

            return true;
        } catch (Exception e) {
            errorMessage = e.getMessage();
            strValue = defaultValue;

            try {
                stringToValue();
            } catch (Exception ex) {
                // Should never reach this place!!!!
                objValue = null;
                errorMessage += ". Bad default string value";
            }

            return false;
        }
    }

    /**
     * Returns the string value
     *
     * @return
     */
    public final String getString() {
        return strValue;
    }

    /**
     * Sets the object value, and calculates the String value.
     *
     * @param value The Object value
     */
    public final void setValue(Object value) {
        objValue = value;
        valueToString();
    }

    /**
     * Gets the object value
     *
     * @return
     */
    public final Object getValue() {
        return objValue;
    }

    /**
     * Gets and resets the current error message.
     *
     * @return The current error message, "" if none
     */
    public final String getError() {
        String tmp = errorMessage;
        errorMessage = "";

        return tmp;
    }

    /**
     * Converts an object value to string - the default implementation uses
     * toString for non-null values
     */
    protected void valueToString() {
        // String.valueOf() would yeld "null"
        if (objValue == null) {
            strValue = "";
        } else {
            strValue = objValue.toString();
        }
    }

    /**
     * This method must be overridden by descendants in order to implement the
     * correct conversion between strings and object values. <br>
     * Must raise an exception if conversion failed
     *
     * @throws Exception if Value conversion failed
     */
    protected abstract void stringToValue() throws Exception;
}
