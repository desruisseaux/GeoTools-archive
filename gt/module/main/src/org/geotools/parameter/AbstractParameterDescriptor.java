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
import java.util.Map;
import java.io.Serializable;

// Geotools dependencies
import org.geotools.referencing.IdentifiedObject;
import org.geotools.referencing.wkt.Formatter;
import org.geotools.resources.rsc.Resources;
import org.geotools.resources.rsc.ResourceKeys;


/**
 * Abstract definition of a parameter or group of parameters used by an operation method.
 * <p>
 * This class maps directly to opengis GeneralParameterDescriptor - the name
 * has changed to prevent confusion.
 * </p>
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see org.geotools.parameter.AbstractParameter
 */
public abstract class AbstractParameterDescriptor extends IdentifiedObject
        implements org.opengis.parameter.GeneralParameterDescriptor, Serializable
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 6058480546466947696L;
    
    /**
     * The minimum number of times that values for this parameter group or
     * parameter are required.
     */
    private final int minimumOccurs;

    /**
     * The maximum number of times that values for this parameter group or
     * parameter can be included.
     */
    private final int maximumOccurs;

    /**
     * Construct a parameter from a set of properties. The properties map is given unchanged
     * to the {@linkplain IdentifiedObject#IdentifiedObject(Map) super-class constructor}.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param minimumOccurs The {@linkplain #getMinimumOccurs minimum number of times}
     *        that values for this parameter group or parameter are required.
     * @param maximumOccurs The {@linkplain #getMaximumOccurs maximum number of times}
     *        that values for this parameter group or parameter are required.
     */
    protected AbstractParameterDescriptor(final Map properties,
                                          final int minimumOccurs,
                                          final int maximumOccurs)
    {
        super(properties);
        this.minimumOccurs = minimumOccurs;
        this.maximumOccurs = maximumOccurs;
        if (minimumOccurs < 0  ||  maximumOccurs < minimumOccurs) {
            throw new IllegalArgumentException(Resources.format(ResourceKeys.ERROR_BAD_RANGE_$2,
                        new Integer(minimumOccurs), new Integer(maximumOccurs)));
        }
    }

    /**
     * Creates a new instance of
     * {@linkplain org.geotools.parameter.AbstractParameter parameter value or group}
     * initialized with the
     * {@linkplain org.geotools.parameter.ParameterDescriptor#getDefaultValue default value(s)}. The
     * {@linkplain org.geotools.parameter.AbstractParameter#getDescriptor parameter value
     * descriptor} for the created parameter value(s) will be <code>this</code> object.
     * <p>
     * Example implementation:
     * <pre>
     * <b>return</b> new {@link Parameter}(this);
     * </pre>
     * </p>
     */
    public abstract org.opengis.parameter.GeneralParameterValue createValue();
    
    /**
     * The minimum number of times that values for this parameter group or
     * parameter are required. The default value is one. A value of 0 means
     * an optional parameter.
     *
     * @return The minimum occurrences.
     *
     * @see #getMaximumOccurs
     */
    public int getMinimumOccurs() {
        return minimumOccurs;
    }

    /**
     * The maximum number of times that values for this parameter group or
     * parameter can be included. For a {@linkplain ParameterDescriptor single parameter},
     * the value is always 1. For a {@linkplain ParameterDescriptorGroup parameter group},
     * it may vary. The default value is one.
     *
     * @return The maximum occurrences.
     *
     * @see #getMinimumOccurs
     */
    public int getMaximumOccurs() {
        return maximumOccurs;
    }
    
    /**
     * Compares the specified object with this parameter for equality.
     *
     * @param  object The object to compare to <code>this</code>.
     * @param  compareMetadata <code>true</code> for performing a strict comparaison, or
     *         <code>false</code> for comparing only properties relevant to transformations.
     * @return <code>true</code> if both objects are equal.
     */
    public boolean equals(final IdentifiedObject object, final boolean compareMetadata) {
        if (super.equals(object, compareMetadata)) {
            final AbstractParameterDescriptor that = (AbstractParameterDescriptor) object;
            return this.minimumOccurs == that.minimumOccurs &&
                   this.maximumOccurs == that.maximumOccurs;
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
        return (int)serialVersionUID ^ (int)minimumOccurs + 37*(int)maximumOccurs;
    }
    
    /**
     * Format the inner part of a
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite> (WKT)</A> element. Note that WKT is not yet defined for parameter descriptor.
     * Current implementation print only the name.
     *
     * @param  formatter The formatter to use.
     * @return The WKT element name, which is "PARAMETER"
     */
    protected String formatWKT(final Formatter formatter) {
        formatter.setInvalidWKT();
        return "PARAMETER";
    }
}
