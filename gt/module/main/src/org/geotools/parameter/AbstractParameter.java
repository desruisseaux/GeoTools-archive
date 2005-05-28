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
import java.io.IOException;
import java.io.Serializable;

import org.geotools.io.TableWriter;
import org.geotools.referencing.wkt.Formattable;
import org.geotools.referencing.wkt.Formatter;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.cts.Resources;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.GeneralParameterValue;


/**
 * Abstract parameter value or group of parameter values.
 * <p>
 * This maps directly to opengis GeneralParameterValue, the name is changed to protect
 * developers from confusing the two.
 * </p>
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see org.geotools.parameter.AbstractParameterDescriptor
 */
public abstract class AbstractParameter extends Formattable
           implements org.opengis.parameter.GeneralParameterValue, Serializable
{
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
    protected AbstractParameter(final GeneralParameterDescriptor descriptor) {
        this.descriptor = descriptor;
        ensureNonNull("descriptor", descriptor);
    }
    
    /**
     * Returns the abstract definition of this parameter or group of parameters.
     */
    public GeneralParameterDescriptor getDescriptor() {
        return descriptor;
    }
    
    /**
     * Makes sure that an argument is non-null. This method was already defined in
     * {@link org.geotools.referencing.AbstractIdentifiedObject}, but is defined here again
     * in order to get a more appropriate stack trace, and for access by class which do not
     * inherit from {@link org.geotools.referencing.AbstractIdentifiedObject}.
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
     * @param  array The array to look at.
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
     * Convenience method returning the name of the specified descriptor. This method is used
     * mostly for output to be read by human, not for processing. Concequently, we may consider
     * to returns a localized name in a future version.
     */
    static String getName(final GeneralParameterDescriptor descriptor) {
        return descriptor.getName().getCode();
    }

    /**
     * Returns a copy of this parameter value or group.
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
     * Compares the specified object with this parameter for equality.
     *
     * @param  object The object to compare to <code>this</code>.
     * @return <code>true</code> if both objects are equal.
     */
    public boolean equals(final Object object) {
        if (object!=null && object.getClass().equals(getClass())) {
            final AbstractParameter that = (AbstractParameter) object;
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

    /**
     * Returns a string representation of this parameter. The default implementation
     * delegates the work to {@link #write}, which should be overriden by subclasses.
     */
    public final String toString() {
        final TableWriter table = new TableWriter(null, 1);
        table.setMultiLinesCells(true);
        try {
            write(table);
        } catch (IOException exception) {
            // Should never happen, since we write to a StringWriter.
            throw new AssertionError(exception);
        }
        return table.toString();
    }

    /**
     * Write the content of this parameter to the specified table. This method make it easier
     * to align values properly than overriding the {@link #toString} method. The table's columns
     * are defined as below:
     * <ol>
     *   <li>The parameter name</li>
     *   <li>The separator</li>
     *   <li>The parameter value</li>
     * </ol>
     *
     * <P>Subclasses should override this method with the following idiom:</P>
     *
     * <blockquote><pre>
     * table.{@linkplain TableWriter#write(String) write}("<var>parameter name</var>");
     * table.{@linkplain TableWriter#nextColumn() nextColumn}()
     * table.{@linkplain TableWriter#write(String) write}('=');
     * table.{@linkplain TableWriter#nextColumn() nextColumn}()
     * table.{@linkplain TableWriter#write(String) write}("<var>parameter value</var>");
     * table.{@linkplain TableWriter#nextLine() nextLine}()
     * </pre></blockquote>
     *
     * @param  table The table where to format the parameter value.
     * @throws IOException if an error occurs during output operation.
     */
    protected void write(final TableWriter table) throws IOException {
        table.write(descriptor.getName().getCode());
        // No know parameter value for this default implementation.
        table.nextLine();
    }

    /**
     * Format the inner part of this parameter as
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite> (WKT)</A>. This method doesn't need to be overriden, since the formatter
     * already know how to {@linkplain Formatter#append(GeneralParameterValue) format parameters}.
     */
    protected final String formatWKT(final Formatter formatter) {
        return "PARAMETER";
    }
}
