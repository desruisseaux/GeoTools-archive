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
package org.geotools.referencing.crs;

// J2SE dependencies
import java.util.Map;
import javax.units.Unit;

// OpenGIS direct dependencies
import org.opengis.referencing.datum.Datum;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;

// Geotools dependencies
import org.geotools.referencing.IdentifiedObject;
import org.geotools.referencing.wkt.Formatter;
import org.geotools.referencing.ReferenceSystem;


/**
 * Abstract coordinate reference system, consisting of a single
 * {@linkplain org.geotools.referencing.cs.CoordinateSystem Coordinate System} and a single
 * {@linkplain org.geotools.referencing.datum.Datum Datum} (as opposed to
 * {@linkplain org.geotools.referencing.crs.CompoundCRS Compound CRS}).
 *
 * A coordinate reference system consists of an ordered sequence of coordinate system
 * axes that are related to the earth through a datum. A coordinate reference system
 * is defined by one datum and by one coordinate system. Most coordinate reference system
 * do not move relative to the earth, except for engineering coordinate reference systems
 * defined on moving platforms such as cars, ships, aircraft, and spacecraft.
 *
 * Coordinate reference systems are commonly divided into sub-types. The common classification
 * criterion for sub-typing of coordinate reference systems is the way in which they deal with
 * earth curvature. This has a direct effect on the portion of the earth's surface that can be
 * covered by that type of CRS with an acceptable degree of error. The exception to the rule is
 * the subtype "Temporal" which has been added by analogy.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see org.geotools.referencing.cs.CoordinateSystem
 * @see org.geotools.referencing.datum.Datum
 */
public class SingleCRS extends CoordinateReferenceSystem
                    implements org.opengis.referencing.crs.SingleCRS
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 1815712797774273L;

    /**
     * The datum.
     */
    protected final Datum datum;

    /**
     * Constructs a coordinate reference system from a set of properties. The properties are given
     * unchanged to the {@linkplain ReferenceSystem#ReferenceSystem(Map) super-class constructor}.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param datum The datum.
     * @param cs The coordinate system.
     */
    public SingleCRS(final Map      properties,
                     final Datum         datum,
                     final CoordinateSystem cs)
    {
        super(properties, cs);
        this.datum = datum;
        ensureNonNull("datum", datum);
    }

    /**
     * Returns the datum.
     *
     * @return The datum.
     */
    public Datum getDatum() {
        return datum;
    }

    /**
     * Returns the dimension of the underlying {@linkplain CoordinateSystem coordinate system}.
     * This is equivalent to <code>{@linkplain #coordinateSystem}.{@linkplain
     * org.geotools.referencing.cs.CoordinateSystem#getDimension getDimension}()</code>.
     *
     * @return The dimension of this coordinate reference system.
     */
    public int getDimension() {
        return coordinateSystem.getDimension();
    }

    /**
     * Returns the axis for the underlying {@linkplain CoordinateSystem coordinate system} at
     * the specified dimension. This is equivalent to
     * <code>{@linkplain #coordinateSystem}.{@linkplain
     * org.geotools.referencing.cs.CoordinateSystem#getAxis getAxis}(dimension)</code>.
     *
     * @param  dimension The zero based index of axis.
     * @return The axis at the specified dimension.
     * @throws IndexOutOfBoundsException if <code>dimension</code> is out of bounds.
     */
    public CoordinateSystemAxis getAxis(int dimension) throws IndexOutOfBoundsException {
        return coordinateSystem.getAxis(dimension);
    }

    /**
     * Compare this coordinate reference system with the specified object for equality.
     * If <code>compareMetadata</code> is <code>true</code>, then all available properties are
     * compared including {@linkplain #getValidArea valid area} and {@linkplain #getScope scope}.
     *
     * @param  object The object to compare to <code>this</code>.
     * @param  compareMetadata <code>true</code> for performing a strict comparaison, or
     *         <code>false</code> for comparing only properties relevant to transformations.
     * @return <code>true</code> if both objects are equal.
     */
    public boolean equals(final IdentifiedObject object, final boolean compareMetadata) {
        if (super.equals(object, compareMetadata)) {
            final SingleCRS that = (SingleCRS) object;
            return equals(this.datum, that.datum);
        }
        return false;
    }
    
    /**
     * Returns a hash value for this CRS. {@linkplain #getName Name},
     * {@linkplain #getIdentifiers identifiers} and {@linkplain #getRemarks remarks}
     * are not taken in account. In other words, two CRS objects will return the same
     * hash value if they are equal in the sense of
     * <code>{@link #equals(IdentifiedObject,boolean) equals}(IdentifiedObject, <strong>false</strong>)</code>.
     *
     * @return The hash code value. This value doesn't need to be the same
     *         in past or future versions of this class.
     */
    public int hashCode() {
        return super.hashCode() ^ datum.hashCode();
    }
    
    /**
     * Format the inner part of a
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite> (WKT)</A> element. The default implementation write the following elements:
     * <ul>
     *   <li>The {@linkplain #datum datum}.</li>
     *   <li>The unit if all axis use the same unit. Otherwise the unit is omitted and
     *       the WKT format is {@linkplain Formatter#isInvalidWKT flagged as invalid}.</li>
     *   <li>All {@linkplain #coordinateSystem coordinate system}'s axis.</li>
     * </ul>
     *
     * @param  formatter The formatter to use.
     * @return The WKT element name.
     */
    protected String formatWKT(final Formatter formatter) {
        formatter.append(datum);
        return super.formatWKT(formatter);
    }
}
