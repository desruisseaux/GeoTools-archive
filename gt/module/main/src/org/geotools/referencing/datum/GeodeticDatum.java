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

// J2SE dependencies
import java.util.Map;
import java.util.Arrays;
import java.util.Collections;

// OpenGIS dependencies
import org.opengis.referencing.datum.Datum;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.PrimeMeridian;
import org.opengis.referencing.operation.Matrix;

// Geotools dependencies
import org.geotools.referencing.IdentifiedObject;
import org.geotools.referencing.wkt.Formatter;


/**
 * Defines the location and precise orientation in 3-dimensional space of a defined ellipsoid
 * (or sphere) that approximates the shape of the earth. Used also for Cartesian coordinate
 * system centered in this ellipsoid (or sphere).
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see Ellipsoid
 * @see PrimeMeridian
 */
public class GeodeticDatum extends org.geotools.referencing.datum.Datum
                        implements org.opengis.referencing.datum.GeodeticDatum
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 8832100095648302944L;
    
    /**
     * The default WGS 1984 datum.
     */
    public static final GeodeticDatum WGS84 = new GeodeticDatum("WGS84",
                                              org.geotools.referencing.datum.Ellipsoid.WGS84,
                                              org.geotools.referencing.datum.PrimeMeridian.GREENWICH);

    /**
     * The ellipsoid.
     */
    private final Ellipsoid ellipsoid;

    /**
     * The prime meridian.
     */
    private final PrimeMeridian primeMeridian;
    
    /**
     * Parameters for Bursa Wolf transformations, or <code>null</code> if none.
     */
    private final BursaWolfParameters[] transformations;

    /**
     * Construct a geodetic datum from a name.
     *
     * @param name          The datum name.
     * @param ellipsoid     The ellipsoid.
     * @param primeMeridian The prime meridian.
     */
    public GeodeticDatum(final String        name,
                         final Ellipsoid     ellipsoid,
                         final PrimeMeridian primeMeridian)
    {
        this(Collections.singletonMap(NAME_PROPERTY, name), ellipsoid, primeMeridian, null);
    }

    /**
     * Construct a geodetic datum from a set of properties. The properties map is
     * given unchanged to the {@linkplain org.geotools.referencing.datum.Datum#Datum(Map)
     * super-class constructor}.
     *
     * @param properties      Set of properties. Should contains at least <code>"name"</code>.
     * @param ellipsoid       The ellipsoid.
     * @param primeMeridian   The prime meridian.
     * @param transformations An optional set of transformation parameters from this datum
     *                        to an other datum (usually {@link #WGS84 WGS 1984}.
     */
    public GeodeticDatum(final Map                   properties,
                         final Ellipsoid             ellipsoid,
                         final PrimeMeridian         primeMeridian,
                               BursaWolfParameters[] transformations)
    {
        super(properties);
        this.ellipsoid     = ellipsoid;
        this.primeMeridian = primeMeridian;
        ensureNonNull("ellipsoid",     ellipsoid);
        ensureNonNull("primeMeridian", primeMeridian);
        if (transformations != null) {
            if (transformations.length == 0) {
                transformations = null;
            } else {
                transformations = (BursaWolfParameters[]) transformations.clone();
                for (int i=0; i<transformations.length; i++) {
                    transformations[i] = (BursaWolfParameters) transformations[i].clone();
                }
            }
        }
        this.transformations = transformations;
    }

    /**
     * Returns the ellipsoid.
     */
    public Ellipsoid getEllipsoid() {
        return ellipsoid;
    }

    /**
     * Returns the prime meridian.
     */
    public PrimeMeridian getPrimeMeridian() {
        return primeMeridian;
    }

    /**
     * Returns a matrix that can be used to define a transformation to the specified datum.
     * If no transformation path is found, then this method returns <code>null</code>.
     *
     * @see BursaWolfParameters#getAffineTransform
     */
    public Matrix getTransformTo(final Datum datum) {
        if (transformations != null) {
            for (int i=0; i<transformations.length; i++) {
                final BursaWolfParameters transformation = transformations[i];
                if (equals(datum, transformation.targetDatum, false)) {
                    return transformation.getAffineTransform();
                }
            }
        }
        return null;
    }
    
    /**
     * Returns <code>true</code> if the specified object is equals (at least on
     * computation purpose) to the {@link #WGS84} datum. This method may conservatively
     * returns <code>false</code> if the specified datum is uncertain (for example
     * because it come from an other implementation).
     */
    public static boolean isWGS84(final Datum datum) {
        if (datum instanceof IdentifiedObject) {
            return WGS84.equals((IdentifiedObject) datum, false);
        }
        // Maybe the specified object has its own test...
        return datum.equals(WGS84);
    }
    
    /**
     * Compare this datum with the specified object for equality.
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
            final GeodeticDatum that = (GeodeticDatum) object;
            return   equals(this.ellipsoid,       that.ellipsoid,      compareMetadata) &&
                     equals(this.primeMeridian,   that.primeMeridian,  compareMetadata) &&
              Arrays.equals(this.transformations, that.transformations                );
        }
        return false;
    }

    /**
     * Returns a hash value for this geodetic datum. {@linkplain #getName Name},
     * {@linkplain #getRemarks remarks} and the like are not taken in account. In
     * other words, two geodetic datums will return the same hash value if they
     * are equal in the sense of
     * <code>{@link #equals equals}(IdentifiedObject, <strong>false</strong>)</code>.
     *
     * @return The hash code value. This value doesn't need to be the same
     *         in past or future versions of this class.
     */
    public int hashCode() {
        int code = (int)serialVersionUID ^
            37*(super        .hashCode() ^
            37*(ellipsoid    .hashCode() ^
            37*(primeMeridian.hashCode())));
        return code;
    }
    
    /**
     * Format the inner part of a
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite> (WKT)</A> element.
     *
     * @param  formatter The formatter to use.
     * @return The WKT element name, which is "DATUM"
     */
    protected String formatWKT(final Formatter formatter) {
        // Do NOT invokes the super-class method, because
        // horizontal datum do not write the datum type.
        formatter.append(ellipsoid);
        if (transformations != null) {
            for (int i=0; i<transformations.length; i++) {
                final BursaWolfParameters transformation = transformations[i];
                if (isWGS84(transformation.targetDatum)) {
                    formatter.append(transformation);
                    break;
                }
            }
        }
        return "DATUM";
    }
}
