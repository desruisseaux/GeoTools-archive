/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2004, Institut de Recherche pour le Développement
 *   
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing.cs;

// J2SE dependencies and extensions
import java.util.Map;
import javax.units.SI;
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.referencing.cs.TimeCS;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

// Geotools dependencies
import org.geotools.measure.Measure;
import org.geotools.resources.i18n.VocabularyKeys;


/**
 * A one-dimensional coordinate system containing a single time axis, used to describe the
 * temporal position of a point in the specified time units from a specified time origin.
 * A {@code TimeCS} shall have one {@linkplain #getAxis axis}.
 *
 * <TABLE CELLPADDING='6' BORDER='1'>
 * <TR BGCOLOR="#EEEEFF"><TH NOWRAP>Used with CRS type(s)</TH></TR>
 * <TR><TD>
 *   {@link org.geotools.referencing.crs.DefaultTemporalCRS Temporal}
 * </TD></TR></TABLE>
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class DefaultTimeCS extends AbstractCS implements TimeCS {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 5222911412381303989L;

    /**
     * A one-dimensional temporal CS with
     * <var>{@linkplain DefaultCoordinateSystemAxis#TIME time}</var>,
     * axis in days.
     */
    public static DefaultTimeCS DAYS = new DefaultTimeCS(
                  name(VocabularyKeys.TEMPORAL),
                  DefaultCoordinateSystemAxis.TIME);

    /**
     * Constructs a new coordinate system with the same values than the specified one.
     * This copy constructor provides a way to wrap an arbitrary implementation into a
     * Geotools one or a user-defined one (as a subclass), usually in order to leverage
     * some implementation-specific API. This constructor performs a shallow copy,
     * i.e. the properties are not cloned.
     *
     * @since 2.2
     */
    public DefaultTimeCS(final TimeCS cs) {
        super(cs);
    }

    /**
     * Constructs a coordinate system from a name.
     *
     * @param name  The coordinate system name.
     * @param axis  The axis.
     */
    public DefaultTimeCS(final String name, final CoordinateSystemAxis axis) {
        super(name, new CoordinateSystemAxis[] {axis});
        ensureTimeUnit(getAxis(0).getUnit());
    }

    /**
     * Constructs a coordinate system from a set of properties.
     * The properties map is given unchanged to the
     * {@linkplain AbstractCS#AbstractCS(Map,CoordinateSystemAxis[]) super-class constructor}.
     *
     * @param properties   Set of properties. Should contains at least <code>"name"</code>.
     * @param axis         The axis.
     */
    public DefaultTimeCS(final Map properties, final CoordinateSystemAxis axis) {
        super(properties, new CoordinateSystemAxis[] {axis});
        ensureTimeUnit(getAxis(0).getUnit());
    }

    /**
     * Returns {@code true} if the specified axis direction is allowed for this coordinate
     * system. The default implementation accepts only temporal directions (i.e.
     * {@link AxisDirection#FUTURE FUTURE} and {@link AxisDirection#PAST PAST}).
     */
    protected boolean isCompatibleDirection(final AxisDirection direction) {
        return AxisDirection.FUTURE.equals(direction.absolute());
    }

    /**
     * Returns {@code true} if the specified unit is compatible with {@linkplain SI#SECOND seconds}.
     * This method is invoked at construction time for checking units compatibility.
     *
     * @since 2.2
     */
    protected boolean isCompatibleUnit(final AxisDirection direction, final Unit unit) {
        return SI.SECOND.isCompatible(unit);
    }

    /**
     * Computes the time difference between two points.
     *
     * @param  coord1 Coordinates of the first point.
     * @param  coord2 Coordinates of the second point.
     * @return The time difference between {@code coord1} and {@code coord2}.
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
