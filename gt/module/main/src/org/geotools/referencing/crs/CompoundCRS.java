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
import java.util.Collections;

// OpenGIS dependencies
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

// Geotools dependencies
import org.geotools.referencing.IdentifiedObject;
import org.geotools.referencing.ReferenceSystem;  // For javadoc
import org.geotools.referencing.cs.CompoundCS;
import org.geotools.referencing.wkt.Formatter;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * A coordinate reference system describing the position of points through two or more
 * independent coordinate reference systems. Thus it is associated with two or more
 * {@linkplain org.geotools.referencing.cs.CoordinateSystem coordinate systems} and
 * {@linkplain org.geotools.referencing.datum.Datum datums} by defining the compound CRS
 * as an ordered set of two or more instances of
 * {@link org.geotools.referencing.crs.CoordinateReferenceSystem}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class CompoundCRS extends org.geotools.referencing.crs.CoordinateReferenceSystem
                         implements org.opengis.referencing.crs.CompoundCRS
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -2656710314586929286L;

    /**
     * The coordinate reference systems in this compound CRS.
     */
    private final CoordinateReferenceSystem[] crs;

    /**
     * Constructs a coordinate reference system from a name and two CRS.
     *
     * @param name The name.
     * @param head The head CRS.
     * @param tail The tail CRS.
     */
    public CompoundCRS(final String name,
                       final CoordinateReferenceSystem head,
                       final CoordinateReferenceSystem tail)
    {
        this(name, new CoordinateReferenceSystem[] {head, tail});
    }

    /**
     * Constructs a coordinate reference system from a name and three CRS.
     *
     * @param name The name.
     * @param head The head CRS.
     * @param middle The middle CRS.
     * @param tail The tail CRS.
     */
    public CompoundCRS(final String name,
                       final CoordinateReferenceSystem head,
                       final CoordinateReferenceSystem middle,
                       final CoordinateReferenceSystem tail)
    {
        this(name, new CoordinateReferenceSystem[] {head, middle, tail});
    }

    /**
     * Constructs a coordinate reference system from a name.
     *
     * @param name The name.
     * @param crs The array of coordinate reference system making this compound CRS.
     */
    public CompoundCRS(final String name, final CoordinateReferenceSystem[] crs) {
        this(Collections.singletonMap("name", name), crs);
    }

    /**
     * Constructs a coordinate reference system from a set of properties. The properties are given
     * unchanged to the {@linkplain ReferenceSystem#ReferenceSystem(Map) super-class constructor}.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param crs The array of coordinate reference system making this compound CRS.
     */
    public CompoundCRS(final Map properties, CoordinateReferenceSystem[] crs) {
        super(properties, createCoordinateSystem(crs));
        ensureNonNull("crs", crs);
        this.crs = crs = (CoordinateReferenceSystem[]) crs.clone();
        for (int i=0; i<crs.length; i++) {
            ensureNonNull("crs", crs, i);
        }
        if (crs.length < 2) {
            throw new IllegalArgumentException(Resources.format(
                        ResourceKeys.ERROR_MISSING_PARAMETER_$1, "crs["+crs.length+']'));
        }
    }

    /**
     * Returns a compound coordinate system for the specified array of CRS objects.
     * This method is a work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private static CoordinateSystem createCoordinateSystem(final CoordinateReferenceSystem[] crs) {
        if (crs == null) {
            return null;
        }
        final CoordinateSystem[] cs = new CoordinateSystem[crs.length];
        for (int i=0; i<crs.length; i++) {
            cs[i] = crs[i].getCoordinateSystem();
        }
        return new CompoundCS(cs);
    }

    /**
     * The ordered list of coordinate reference systems.
     *
     * @return The coordinate reference systems.
     */
    public CoordinateReferenceSystem[] getCoordinateReferenceSystems() {
        return (CoordinateReferenceSystem[]) crs.clone();
    }

    /**
     * Compare this coordinate reference system with the specified object for equality.
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
            final CompoundCRS that = (CompoundCRS) object;
            return equals(this.crs, that.crs, compareMetadata);
        }
        return false;
    }
    
    /**
     * Returns a hash value for this compound CRS.
     *
     * @return The hash code value. This value doesn't need to be the same
     *         in past or future versions of this class.
     */
    public int hashCode() {
        // Don't call superclass method since 'coordinateSystem' and 'datum' may be null.
        int code = (int)serialVersionUID;
        for (int i=0; i<crs.length; i++) {
            code = code*37 + crs[i].hashCode();
        }
        return code;
    }
    
    /**
     * Format the inner part of a
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite> (WKT)</A> element.
     *
     * @param  formatter The formatter to use.
     * @return The WKT element name, which is "COMPD_CS"
     */
    protected String formatWKT(final Formatter formatter) {
        for (int i=0; i<crs.length; i++) {
            formatter.append(crs[i]);
        }
        return "COMPD_CS";
    }
}
