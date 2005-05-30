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

// J2SE dependencies and extensions
import java.util.Map;
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

// Geotools dependencies
import org.geotools.measure.Measure;
import org.geotools.referencing.AbstractIdentifiedObject;
import org.geotools.referencing.AbstractReferenceSystem;
import org.geotools.referencing.cs.AbstractCS;
import org.geotools.referencing.wkt.Formatter;
import org.geotools.util.UnsupportedImplementationException;


/**
 * Abstract coordinate reference system, usually defined by a coordinate system and a datum.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @since 2.1
 *
 * @see AbstractCS
 * @see org.geotools.referencing.datum.AbstractDatum
 */
public abstract class AbstractCRS extends AbstractReferenceSystem implements CoordinateReferenceSystem {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -7433284548909530047L;

    /**
     * The coordinate system.
     */
    protected final CoordinateSystem coordinateSystem;

    /**
     * Constructs a coordinate reference system from a set of properties. The properties are given
     * unchanged to the {@linkplain AbstractReferenceSystem#AbstractReferenceSystem(Map) super-class
     * constructor}.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param cs The coordinate system.
     */
    public AbstractCRS(final Map properties, final CoordinateSystem cs) {
        super(properties);
        ensureNonNull("cs", cs);
        this.coordinateSystem = cs;
    }

    /**
     * Returns the coordinate system.
     */
    public CoordinateSystem getCoordinateSystem() {
        return coordinateSystem;
    }

    /**
     * Returns the unit used for all axis. If not all axis uses the same unit,
     * then this method returns {@code null}. This method is often used
     * for Well Know Text (WKT) formatting.
     */
    final Unit getUnit() {
        Unit unit = null;
        for (int i=coordinateSystem.getDimension(); --i>=0;) {
            final Unit candidate = coordinateSystem.getAxis(i).getUnit();
            if (candidate != null) {
                if (unit == null) {
                    unit = candidate;
                } else if (!unit.equals(candidate)) {
                    return null;
                }
            }
        }
        return unit;
    }

    /**
     * Computes the distance between two points. This convenience method delegates the work to
     * the underlyling {@linkplain AbstractCS coordinate system}, if possible.
     *
     * @param  coord1 Coordinates of the first point.
     * @param  coord2 Coordinates of the second point.
     * @return The distance between {@code coord1} and {@code coord2}.
     * @throws UnsupportedOperationException if this coordinate reference system can't compute
     *         distances.
     * @throws MismatchedDimensionException if a coordinate doesn't have the expected dimension.
     */
    public Measure distance(final double[] coord1, final double[] coord2)
            throws UnsupportedOperationException, MismatchedDimensionException
    {
        if (coordinateSystem instanceof AbstractCS) {
            return ((AbstractCS) coordinateSystem).distance(coord1, coord2);
        }
        throw new UnsupportedImplementationException(coordinateSystem.getClass());
    }

    /**
     * Compare this coordinate reference system with the specified object for equality.
     * If {@code compareMetadata} is {@code true}, then all available properties are
     * compared including {@linkplain #getValidArea valid area} and {@linkplain #getScope scope}.
     *
     * @param  object The object to compare to {@code this}.
     * @param  compareMetadata {@code true} for performing a strict comparaison, or
     *         {@code false} for comparing only properties relevant to transformations.
     * @return {@code true} if both objects are equal.
     */
    public boolean equals(final AbstractIdentifiedObject object, final boolean compareMetadata) {
        if (super.equals(object, compareMetadata)) {
            final AbstractCRS that = (AbstractCRS) object;
            return equals(this.coordinateSystem, that.coordinateSystem, compareMetadata);
        }
        return false;
    }
    
    /**
     * Returns a hash value for this CRS. {@linkplain #getName Name},
     * {@linkplain #getIdentifiers identifiers} and {@linkplain #getRemarks remarks}
     * are not taken in account. In other words, two CRS objects will return the same
     * hash value if they are equal in the sense of
     * <code>{@link #equals(AbstractIdentifiedObject,boolean) equals}(AbstractIdentifiedObject,
     * <strong>false</strong>)</code>.
     *
     * @return The hash code value. This value doesn't need to be the same
     *         in past or future versions of this class.
     */
    public int hashCode() {
        return (int)serialVersionUID ^ coordinateSystem.hashCode();
    }
    
    /**
     * Format the inner part of a
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite> (WKT)</A> element. The default implementation write the following elements:
     * <ul>
     *   <li>The unit if all axis use the same unit. Otherwise the unit is omitted and
     *       the WKT format is {@linkplain Formatter#isInvalidWKT flagged as invalid}.</li>
     *   <li>All {@linkplain #coordinateSystem coordinate system}'s axis.</li>
     * </ul>
     *
     * @param  formatter The formatter to use.
     * @return The WKT element name.
     */
    protected String formatWKT(final Formatter formatter) {
        final Unit unit = getUnit();
        formatter.append(unit);
        final int dimension = coordinateSystem.getDimension();
        for (int i=0; i<dimension; i++) {
            formatter.append(coordinateSystem.getAxis(i));
        }
        if (unit == null) {
            formatter.setInvalidWKT();
        }
        return super.formatWKT(formatter);
    }
}
