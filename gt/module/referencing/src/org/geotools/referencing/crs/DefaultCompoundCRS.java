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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// OpenGIS dependencies
import org.opengis.referencing.crs.CompoundCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.SingleCRS;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.datum.Datum;

// Geotools dependencies
import org.geotools.referencing.AbstractIdentifiedObject;
import org.geotools.referencing.AbstractReferenceSystem;
import org.geotools.referencing.cs.DefaultCompoundCS;
import org.geotools.referencing.wkt.Formatter;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;


/**
 * A coordinate reference system describing the position of points through two or more
 * independent coordinate reference systems. Thus it is associated with two or more
 * {@linkplain CoordinateSystem coordinate systems} and {@linkplain Datum datums} by
 * defining the compound CRS as an ordered set of two or more instances of
 * {@link CoordinateReferenceSystem}.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class DefaultCompoundCRS extends AbstractCRS implements CompoundCRS {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -2656710314586929286L;

    /**
     * The coordinate reference systems in this compound CRS.
     */
    private final List/*<CoordinateReferenceSystem>*/ crs;

    /**
     * Constructs a new compound CRS with the same values than the specified one.
     * This copy constructor provides a way to wrap an arbitrary implementation into a
     * Geotools one or a user-defined one (as a subclass), usually in order to leverage
     * some implementation-specific API. This constructor performs a shallow copy,
     * i.e. the properties are not cloned.
     *
     * @since 2.2
     */
    public DefaultCompoundCRS(final CompoundCRS crs) {
        super(crs);
        this.crs = crs.getCoordinateReferenceSystems();
    }

    /**
     * Constructs a coordinate reference system from a name and two CRS.
     *
     * @param name The name.
     * @param head The head CRS.
     * @param tail The tail CRS.
     */
    public DefaultCompoundCRS(final String name,
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
    public DefaultCompoundCRS(final String name,
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
    public DefaultCompoundCRS(final String name, final CoordinateReferenceSystem[] crs) {
        this(Collections.singletonMap(NAME_KEY, name), crs);
    }

    /**
     * Constructs a coordinate reference system from a set of properties.
     * The properties are given unchanged to the
     * {@linkplain AbstractReferenceSystem#AbstractReferenceSystem(Map) super-class constructor}.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param crs The array of coordinate reference system making this compound CRS.
     */
    public DefaultCompoundCRS(final Map properties, CoordinateReferenceSystem[] crs) {
        super(properties, createCoordinateSystem(crs));
        ensureNonNull("crs", crs);
        crs = (CoordinateReferenceSystem[]) crs.clone();
        for (int i=0; i<crs.length; i++) {
            ensureNonNull("crs", crs, i);
        }
        if (crs.length < 2) {
            throw new IllegalArgumentException(Errors.format(
                        ErrorKeys.MISSING_PARAMETER_$1, "crs["+crs.length+']'));
        }
        this.crs = Collections.unmodifiableList(Arrays.asList(crs));
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
        return new DefaultCompoundCS(cs);
    }

    /**
     * The ordered list of coordinate reference systems.
     *
     * @return The coordinate reference systems.
     */
    public List/*<CoordinateReferenceSystem>*/ getCoordinateReferenceSystems() {
        return crs;
    }

    /**
     * Returns the ordered list of single coordinate reference systems.
     * If this compound CRS contains other compound CRS, all of
     * them are expanded in an array of {@code SingleCRS} objects.
     *
     * @return The single coordinate reference systems.
     * @throws ClassCastException if a CRS is neither a {@link SingleCRS} or a {@link CompoundCRS}.
     */
    public SingleCRS[] getSingleCRS() {
        final List singles = new ArrayList(crs.size());
        getSingleCRS(crs, singles);
        return (SingleCRS[]) singles.toArray(new SingleCRS[singles.size()]);
    }

    /**
     * Returns the ordered list of single coordinate reference systems
     * for the specified CRS. The specified CRS doesn't need to be a
     * Geotools implementation.
     *
     * @param  crs The coordinate reference system.
     * @return The single coordinate reference systems.
     * @throws ClassCastException if a CRS is neither a {@link SingleCRS} or a {@link CompoundCRS}.
     */
    public static SingleCRS[] getSingleCRS(final CoordinateReferenceSystem crs) {
        if (crs instanceof DefaultCompoundCRS) {
            return ((DefaultCompoundCRS) crs).getSingleCRS();
        }
        if (crs instanceof CompoundCRS) {
            final List/*<CoordinateReferenceSystem>*/ elements =
                ((CompoundCRS) crs).getCoordinateReferenceSystems();
            final List singles = new ArrayList(elements.size());
            getSingleCRS(elements, singles);
            return (SingleCRS[]) singles.toArray(new SingleCRS[singles.size()]);
        }
        return new SingleCRS[] {(SingleCRS) crs};
    }

    /**
     * Recursively add all {@link SingleCRS} in the specified list.
     *
     * @throws ClassCastException if a CRS is neither a {@link SingleCRS} or a
     *         {@link CompoundCRS}.
     */
    private static void getSingleCRS(final List/*<CoordinateReferenceSystem>*/ crs,
                                     final List/*<SingleCRS>*/ singles)
    {
        for (final Iterator it=crs.iterator(); it.hasNext();) {
            final CoordinateReferenceSystem candidate = (CoordinateReferenceSystem) it.next();
            if (candidate instanceof CompoundCRS) {
                getSingleCRS(((CompoundCRS) candidate).getCoordinateReferenceSystems(), singles);
            } else {
                singles.add((SingleCRS) candidate);
            }
        }
    }

    /**
     * Compares this coordinate reference system with the specified object for equality.
     *
     * @param  object The object to compare to {@code this}.
     * @param  compareMetadata {@code true} for performing a strict comparaison, or
     *         {@code false} for comparing only properties relevant to transformations.
     * @return {@code true} if both objects are equal.
     */
    public boolean equals(final AbstractIdentifiedObject object, final boolean compareMetadata) {
        if (object == this) {
            return true; // Slight optimization.
        }
        if (super.equals(object, compareMetadata)) {
            final DefaultCompoundCRS that = (DefaultCompoundCRS) object;
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
        return crs.hashCode() ^ (int)serialVersionUID;
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
        for (final Iterator it=crs.iterator(); it.hasNext();) {
            formatter.append((CoordinateReferenceSystem) it.next());
        }
        return "COMPD_CS";
    }
}
