/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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
package org.geotools.referencing.datum;

// J2SE extensions
import java.util.Map;
import java.util.Collections;
import javax.units.Unit;
import javax.units.NonSI;

// Geotools dependencies
import org.geotools.referencing.IdentifiedObject;
import org.geotools.referencing.wkt.Formatter;
import org.geotools.resources.Utilities;


/**
 * A prime meridian defines the origin from which longitude values are determined.
 * The {@link #getName name} initial value is "Greenwich", and that value shall be
 * used when the {@linkplain #getGreenwichLongitude greenwich longitude} value is
 * zero.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class PrimeMeridian extends IdentifiedObject
                        implements org.opengis.referencing.datum.PrimeMeridian
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 541978454643213305L;;
    
    /**
     * The Greenwich meridian, with angular measurements in degrees.
     */
    public static final PrimeMeridian GREENWICH = 
                    new PrimeMeridian("Greenwich", 0, NonSI.DEGREE_ANGLE);
    
    /**
     * Longitude of the prime meridian measured from the Greenwich meridian, positive eastward.
     */
    private final double greenwichLongitude;

    /**
     * The angular unit of the {@linkplain #getGreenwichLongitude Greenwich longitude}.
     */
    private final Unit angularUnit;

    /**
     * Construct a prime meridian from a name. The <code>greenwichLongitude</code> value
     * is assumed in {@linkplain NonSI#DEGREE_ANGLE degrees}.
     *
     * @param name                The datum name.
     * @param greenwichLongitude  The longitude value relative to the Greenwich Meridian.
     */
    public PrimeMeridian(final String name, final double greenwichLongitude) {
        this(name, greenwichLongitude, NonSI.DEGREE_ANGLE);
    }

    /**
     * Construct a prime meridian from a name.
     *
     * @param name                The datum name.
     * @param greenwichLongitude  The longitude value relative to the Greenwich Meridian.
     * @param angularUnit         The angular unit of the longitude.
     */
    public PrimeMeridian(final String name, final double greenwichLongitude, final Unit angularUnit) {
        this(Collections.singletonMap(NAME_PROPERTY, name), greenwichLongitude, angularUnit);
    }

    /**
     * Construct a prime meridian from a set of properties. The properties map is
     * given unchanged to the {@linkplain IdentifiedObject#IdentifiedObject(Map)
     * super-class constructor}.
     *
     * @param properties          Set of properties. Should contains at least <code>"name"</code>.
     * @param greenwichLongitude  The longitude value relative to the Greenwich Meridian.
     * @param angularUnit         The angular unit of the longitude.
     */
    public PrimeMeridian(final Map properties, final double greenwichLongitude, final Unit angularUnit) {
        super(properties);
        this.greenwichLongitude = greenwichLongitude;
        this.angularUnit        = angularUnit;
        ensureAngularUnit(angularUnit);
    }

    /**
     * Longitude of the prime meridian measured from the Greenwich meridian, positive eastward.
     * The <code>greenwichLongitude</code> initial value is zero, and that value shall be used
     * when the {@linkplain #getName meridian name} value is "Greenwich".
     *
     * @return The prime meridian Greenwich longitude, in {@linkplain #getAngularUnit angular unit}.
     */
    public double getGreenwichLongitude() {
        return greenwichLongitude;
    }
    
    /**
     * Returns the longitude value relative to the Greenwich Meridian, expressed in the specified
     * units. This convenience method makes it easier to obtain longitude in degrees
     * (<code>getGreenwichLongitude(NonSI.DEGREE_ANGLE)</code>), regardless of the underlying
     * angular units of this prime meridian.
     *
     * @param targetUnit The unit in which to express longitude.
     */
    public double getGreenwichLongitude(final Unit targetUnit) {
        return getAngularUnit().getConverterTo(targetUnit).convert(getGreenwichLongitude());
    }

    /**
     * Returns the angular unit of the {@linkplain #getGreenwichLongitude Greenwich longitude}.
     */
    public Unit getAngularUnit() {
        return angularUnit;
    }
    
    /**
     * Compare this prime meridian with the specified object for equality.
     *
     * @param  object The object to compare to <code>this</code>.
     * @param  compareMetadata <code>true</code> for performing a strict comparaison, or
     *         <code>false</code> for comparing only properties relevant to transformations.
     * @return <code>true</code> if both objects are equal.
     */
    public boolean equals(final IdentifiedObject object, final boolean compareMetadata) {
        if (object == this) {
            return true; // Slight optimization.
        }
        if (super.equals(object, compareMetadata)) {
            final PrimeMeridian that = (PrimeMeridian) object;
            return Double.doubleToLongBits(this.greenwichLongitude) ==
                   Double.doubleToLongBits(that.greenwichLongitude) &&
                   Utilities.equals(this.angularUnit, that.angularUnit);
        }
        return false;
    }
    
    /**
     * Returns a hash value for this prime meridian. {@linkplain #getName Name},
     * {@linkplain #getRemarks remarks} and the like are not taken in account.
     * In other words, two prime meridians will return the same hash value if
     * they are equal in the sense of
     * <code>{@link #equals equals}(IdentifiedObject, <strong>false</strong>)</code>.
     *
     * @return The hash code value. This value doesn't need to be the same
     *         in past or future versions of this class.
     */
    public int hashCode() {
        final long code = Double.doubleToLongBits(greenwichLongitude);
        return ((int)(code >>> 32) ^ (int)code) ^ (int)serialVersionUID;
    }
    
    /**
     * Format the inner part of a
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite> (WKT)</A> element.
     *
     * @param  formatter The formatter to use.
     * @return The WKT element name, which is "PRIMEM"
     */
    protected String formatWKT(final Formatter formatter) {
        Unit context = formatter.getAngularUnit();
        if (context == null) {
            // If the PrimeMeridian is written inside a "GEOGCS",
            // then OpenGIS say that it must be written into the
            // unit of the enclosing geographic coordinate system.
            // Otherwise, default to degrees.
            context = NonSI.DEGREE_ANGLE;
        }
        formatter.append(getGreenwichLongitude(context));
        return "PRIMEM";
    }
}
