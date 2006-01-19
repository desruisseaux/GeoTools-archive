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
package org.geotools.referencing.cs;

// J2SE dependencies
import java.util.List;
import java.util.Arrays;
import java.util.Collections;

// OpenGIS dependencies
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;

// Geotools dependencies
import org.geotools.referencing.AbstractIdentifiedObject;


/**
 * A coordinate system made of two or more independent coordinate systems.
 *
 * <TABLE CELLPADDING='6' BORDER='1'>
 * <TR BGCOLOR="#EEEEFF"><TH NOWRAP>Used with CRS type(s)</TH></TR>
 * <TR><TD>
 *   {@link org.geotools.referencing.crs.DefaultCompoundCRS Compound}
 * </TD></TR></TABLE>
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class DefaultCompoundCS extends AbstractCS {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -5726410275278843372L;

    /**
     * The coordinate systems.
     */
    private final CoordinateSystem[] cs;

    /**
     * An immutable view of {@link #cs} as a list. Will be created only when first needed.
     */
    private transient List/*<CoordinateSystem>*/ asList;

    /**
     * Constructs a compound coordinate system. A name for this CS will
     * be automatically constructed from the name of all specified CS.
     *
     * @param cs The set of coordinate syztem.
     */
    public DefaultCompoundCS(CoordinateSystem[] cs) {
        super(getName(cs=clone(cs)), getAxis(cs));
        this.cs = cs;
    }

    /**
     * Returns a clone of the specified array. This method would be bundle right
     * into the constructor if RFE #4093999 was fixed.
     */
    private static CoordinateSystem[] clone(CoordinateSystem[] cs) {
        ensureNonNull("cs", cs);
        cs = (CoordinateSystem[]) cs.clone();
        for (int i=0; i<cs.length; i++) {
            ensureNonNull("cs", cs, i);
        }
        return cs;
    }

    /**
     * Returns the axis of all coordinate systems.
     */
    private static CoordinateSystemAxis[] getAxis(final CoordinateSystem[] cs) {
        int count = 0;
        for (int i=0; i<cs.length; i++) {
            count += cs[i].getDimension();
        }
        final CoordinateSystemAxis[] axis = new CoordinateSystemAxis[count];
        count = 0;
        for (int i=0; i<cs.length; i++) {
            final CoordinateSystem c = cs[i];
            final int dim = c.getDimension();
            for (int j=0; j<dim; j++) {
                axis[count++] = c.getAxis(j);
            }
        }
        assert count == axis.length;
        return axis;
    }

    /**
     * Constructs a name from a merge of the name of all coordinate systems.
     *
     * @param cs The coordinate systems.
     * @param locale The locale for the name.
     */
    private static String getName(final CoordinateSystem[] cs) {
        final StringBuffer buffer = new StringBuffer();
        for (int i=0; i<cs.length; i++) {
            if (buffer.length() != 0) {
                buffer.append(" / ");
            }
            buffer.append(cs[i].getName().getCode());
        }
        return buffer.toString();
    }

    /**
     * Returns all coordinate systems in this compound CS.
     */
    public List/*<CoordinateSystem>*/ getCoordinateSystems() {
        if (asList == null) {
            // No need to synchronize; this is not a big deal if two lists are created.
            asList = Collections.unmodifiableList(Arrays.asList(cs));
        }
        return asList;
    }

    /**
     * Compares this coordinate system with the specified object for equality.
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
            final DefaultCompoundCS that = (DefaultCompoundCS) object;
            return equals(this.cs, that.cs, compareMetadata);
        }
        return false;
    }
}
