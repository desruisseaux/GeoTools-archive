/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le D�veloppement
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
import java.util.Map;

import org.geotools.measure.Measure;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;


/**
 * A one-dimensional coordinate system used to record the heights (or depths) of points. Such a
 * coordinate system is usually dependent on the Earth's gravity field, perhaps loosely as when
 * atmospheric pressure is the basis for the vertical coordinate system axis. An exact definition
 * is deliberately not provided as the complexities of the subject fall outside the scope of this
 * specification. A <code>VerticalCS</code> shall have one {@linkplain #getAxis axis}.
 *
 * <TABLE CELLPADDING='6' BORDER='1'>
 * <TR BGCOLOR="#EEEEFF"><TH NOWRAP>Used with CRS type(s)</TH></TR>
 * <TR><TD>
 *   {@link org.geotools.referencing.crs.VerticalCRS    Vertical},
 *   {@link org.geotools.referencing.crs.EngineeringCRS Engineering}
 * </TD></TR></TABLE>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class VerticalCS extends CoordinateSystem implements org.opengis.referencing.cs.VerticalCS {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 1201155778896630499L;;

    /**
     * A one-dimensional vertical CS with
     * <var>{@linkplain org.geotools.referencing.cs.CoordinateSystemAxis#ELLIPSOIDAL_HEIGHT
     * ellipsoidal height}</var> axis in metres.
     *
     * @todo Localize name.
     */
    public static VerticalCS ELLIPSOIDAL_HEIGHT = new VerticalCS("Ellipsoidal height",
                    org.geotools.referencing.cs.CoordinateSystemAxis.ELLIPSOIDAL_HEIGHT);

    /**
     * A one-dimensional vertical CS with
     * <var>{@linkplain org.geotools.referencing.cs.CoordinateSystemAxis#GRAVITY_RELATED_HEIGHT
     * gravity-related height}</var> axis in metres.
     *
     * @todo Localize name.
     */
    public static VerticalCS GRAVITY_RELATED = new VerticalCS("Gravity-related",
                    org.geotools.referencing.cs.CoordinateSystemAxis.GRAVITY_RELATED_HEIGHT);

    /**
     * A one-dimensional vertical CS with
     * <var>{@linkplain org.geotools.referencing.cs.CoordinateSystemAxis#DEPTH depth}</var>
     * axis in metres.
     *
     * @todo Localize name.
     */
    public static VerticalCS DEPTH = new VerticalCS("Depth",
                    org.geotools.referencing.cs.CoordinateSystemAxis.DEPTH);

    /**
     * Construct a coordinate system from a name.
     *
     * @param name  The coordinate system name.
     * @param axis  The axis.
     */
    public VerticalCS(final String name, final CoordinateSystemAxis axis) {
        super(name, new CoordinateSystemAxis[] {axis});
    }

    /**
     * Construct a coordinate system from a set of properties. The properties map is given
     * unchanged to the {@linkplain CoordinateSystem#CoordinateSystem(Map,CoordinateSystemAxis[])
     * super-class constructor}.
     *
     * @param properties   Set of properties. Should contains at least <code>"name"</code>.
     * @param axis         The axis.
     */
    public VerticalCS(final Map properties, final CoordinateSystemAxis axis) {
        super(properties, new CoordinateSystemAxis[] {axis});
    }

    /**
     * Returns <code>true</code> if the specified axis direction is allowed for this coordinate
     * system. The default implementation accepts only vertical directions (i.e.
     * {@link AxisDirection#UP UP} and {@link AxisDirection#DOWN DOWN}).
     */
    protected boolean isCompatibleDirection(final AxisDirection direction) {
        return AxisDirection.UP.equals(direction.absolute());
    }

    /**
     * Computes the distance between two points.
     *
     * @param  coord1 Coordinates of the first point.
     * @param  coord2 Coordinates of the second point.
     * @return The distance between <code>coord1</code> and <code>coord2</code>.
     * @throws MismatchedDimensionException if a coordinate doesn't have the expected dimension.
     */
    public Measure distance(final double[] coord1, final double[] coord2)
            throws MismatchedDimensionException
    {
        ensureDimensionMatch("coord1", coord1);
        ensureDimensionMatch("coord2", coord2);
        return new Measure(Math.abs(coord1[0] - coord2[0]), getDistanceUnit());
    }
}
