/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, GeoTools Project Managment Committee (PMC)
 *    (C) 2007, Geomatys
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
 */
package org.geotools.image.io.netcdf;

// J2SE dependencies
import java.util.Map;
import java.util.HashMap;
import java.util.List;

// OpenGIS dependencies
import org.opengis.referencing.cs.AxisDirection;

// NetCDF dependencies
import ucar.nc2.Variable;
import ucar.nc2.dataset.AxisType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateSystem;
import ucar.nc2.dataset.EnhanceScaleMissing;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;

// Geotools dependencies
import org.geotools.image.io.metadata.GeographicMetadata;
import org.geotools.image.io.metadata.GeographicMetadataFormat;


/**
 * Metadata from NetCDF file. This implementation assumes that the NetCDF file follows the
 * <A HREF="http://www.cfconventions.org/">CF Metadata conventions</A>.
 * <p>
 * <b>Limitation:</b>
 * Current implementation retains only the first {@linkplain CoordinateSystem coordinate system}
 * found in the NetCDF file or for a given variable. The {@link org.geotools.coverage.io} package
 * would not know what to do with the extra coordinate systems anyway.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class NetcdfMetadata extends GeographicMetadata {
    /**
     * Forces usage of UCAR libraries in some places where we use our own code instead.
     * This may result in rounding errors and absence of information regarding fill values,
     * but is useful for checking if we are doing the right thing compared to the UCAR way.
     */
    private static final boolean USE_UCAR_LIB = false;

    /**
     * The mapping between UCAR axis type and ISO axis directions.
     */
    private static final Map/*<AxisType,AxisDirection>*/ DIRECTIONS = new HashMap();
    static {
        add(AxisType.Time,     AxisDirection.FUTURE);
        add(AxisType.GeoX,     AxisDirection.EAST);
        add(AxisType.GeoY,     AxisDirection.NORTH);
        add(AxisType.GeoZ,     AxisDirection.UP);
        add(AxisType.Lat,      AxisDirection.NORTH);
        add(AxisType.Lon,      AxisDirection.EAST);
        add(AxisType.Height,   AxisDirection.UP);
        add(AxisType.Pressure, AxisDirection.UP);
    }

    /**
     * Adds a mapping between UCAR type and ISO direction.
     */
    private static void add(final AxisType type, final AxisDirection direction) {
        if (DIRECTIONS.put(type, direction) != null) {
            throw new IllegalArgumentException(String.valueOf(type));
        }
    }

    /**
     * Creates metadata from the specified file. This constructor is typically invoked
     * for creating {@linkplain NetcdfReader#getStreamMetadata stream metadata}. Note that
     * {@link ucar.nc2.dataset.CoordSysBuilder#addCoordinateSystems} should have been invoked
     * (if needed) before this constructor.
     */
    public NetcdfMetadata(final NetcdfDataset file) {
        final List/*<CoordinateSystem>*/ systems = file.getCoordinateSystems();
        if (!systems.isEmpty()) {
            addCoordinateSystem((CoordinateSystem) systems.get(0));
        }
    }

    /**
     * Creates metadata from the specified file. This constructor is typically invoked
     * for creating {@linkplain NetcdfReader#getImageMetadata image metadata}. Note that
     * {@link ucar.nc2.dataset.CoordSysBuilder#addCoordinateSystems} should have been invoked
     * (if needed) before this constructor.
     */
    public NetcdfMetadata(final VariableDS variable) {
        final List/*<CoordinateSystem>*/ systems = variable.getCoordinateSystems();
        if (!systems.isEmpty()) {
            addCoordinateSystem((CoordinateSystem) systems.get(0));
        }
        setSampleDimensions(GeographicMetadataFormat.PACKED);
        addSampleDimension(variable);
    }

    /**
     * Adds the specified coordinate system. Current implementation can adds at most one
     * coordinate system, but this limitation may be revisited in a future Geotools version.
     */
    protected void addCoordinateSystem(final CoordinateSystem cs) {
        String crsType, csType;
        if (cs.isLatLon()) {
            crsType = cs.hasVerticalAxis() ? GeographicMetadataFormat.GEOGRAPHIC_3D
                                           : GeographicMetadataFormat.GEOGRAPHIC;
            csType  = GeographicMetadataFormat.ELLIPSOIDAL;
        } else if (cs.isGeoXY()) {
            crsType = cs.hasVerticalAxis() ? GeographicMetadataFormat.PROJECTED_3D
                                           : GeographicMetadataFormat.PROJECTED;
            csType  = GeographicMetadataFormat.CARTESIAN;
        } else {
            crsType = null;
            csType  = null;
        }
        setCoordinateReferenceSystem(null, crsType);
        setCoordinateSystem(cs.getName(), csType);
        setGridGeometry("center");
        /*
         * Adds the axis in reverse order, because the NetCDF image reader put the last
         * dimensions in the rendered image. Typical NetCDF convention is to put axis in
         * the (time, depth, latitude, longitude) order, which typically maps to
         * (longitude, latitude, depth, time) order in Geotools referencing framework.
         */
        final List/*<CoordinateAxis>*/ axis = cs.getCoordinateAxes();
        for (int i=axis.size(); --i>=0;) {
            addCoordinateAxis((CoordinateAxis) axis.get(i));
        }
    }

    /**
     * Gets the name, as the "description", "title" or "standard name"
     * attribute if possible, or as the variable name otherwise.
     */
    private static String getName(final Variable variable) {
        String name = variable.getDescription();
        if (name == null || (name=name.trim()).length() == 0) {
            name = variable.getName();
        }
        return name;
    }

    /**
     * Adds the specified coordinate axis. This method is invoked recursively
     * by {@link #addCoordinateSystem}.
     */
    protected void addCoordinateAxis(final CoordinateAxis axis) {
        /*
         * Gets the axis direction, taking in account the possible reversal or vertical axis.
         * Note that geographic and projected CRS have the same directions. We can distinguish
         * them either using the ISO CRS type ("geographic" or "projected"), the ISO CS type
         * ("ellipsoidal" or "cartesian") or the units ("degrees" or "m").
         */
        String direction = null;
        AxisDirection dir = (AxisDirection) DIRECTIONS.get(axis.getAxisType());
        if (dir != null) {
            if (CoordinateAxis.POSITIVE_DOWN.equalsIgnoreCase(axis.getPositive())) {
                dir = dir.opposite();
            }
            direction = dir.name();
        }
        /*
         * Gets the axis units and add to the coordinate system.
         */
        final String units = axis.getUnitsString();
        addAxis(getName(axis), direction, units);
        /*
         * If the axis is not numeric, we can't process any further.
         * If it is, then adds the coordinate and index ranges.
         */
        if (!axis.isNumeric()) {
            return;
        }
        if (axis instanceof CoordinateAxis1D) {
            final CoordinateAxis1D axis1D = (CoordinateAxis1D) axis;
            if (axis1D.isRegular()) {
                // Reminder: pixel orientation is "center", maximum value is inclusive.
                final double increment = axis1D.getIncrement();
                final double start     = axis1D.getStart();
                final int    length    = axis1D.getDimension(0).getLength() - 1;
                final double end       = start + increment * length;
                addCoordinateRange(0, length, start, end);
            } else {
                final double[] values = axis1D.getCoordValues();
                addCoordinateValues(0, values);
            }
        }
    }

    /**
     * Adds sample dimension information for the specified variable.
     */
    protected void addSampleDimension(final VariableDS variable) {
        final VariableMetadata m;
        if (USE_UCAR_LIB) {
            m = new VariableMetadata(variable);
        } else {
            m = new VariableMetadata(variable, forcePacking("valid_range"));
        }
        addSampleDimension(getName(variable), m.scale, m.offset, m.minimum, m.maximum, m.missingValues);
    }

    /**
     * Returns {@code true} if an attribute (usually the <cite>valid range</cite>) should be
     * converted from unpacked to packed units. The <A HREF="http://www.cfconventions.org/">CF
     * Metadata conventions</A> states that valid ranges should be in packed units, but not
     * every NetCDF files follow this advice in practice. The UCAR NetCDF library applies the
     * following heuristic rules (quoting from {@link ucar.nc2.dataset.EnhanceScaleMissing}):
     *
     * <blockquote>
     * If {@code valid_range} is the same type as {@code scale_factor} (actually the wider of
     * {@code scale_factor} and {@code add_offset}) and this is wider than the external data,
     * then it will be interpreted as being in the units of the internal (unpacked) data.
     * Otherwise it is in the units of the external (packed) data.
     * <blockquote>
     *
     * However some NetCDF files stores unpacked ranges using the same type than packed data.
     * The above cited heuristic rule can not resolve those cases.
     * <p>
     * If this method returns {@code true}, then the attribute is assumed in unpacked units no
     * matter what the CF convention and the heuristic rules said. If this method returns
     * {@code false}, then UCAR's heuristic rules applies.
     * <p>
     * The default implementation returns {@code false} in all cases.
     *
     * @param  attribute The attribute (usually {@code "valid_range"}).
     * @return {@code true} if the attribute should be converted from unpacked to packed units
     *         regardless CF convention and UCAR's heuristic rules.
     *
     * @see ucar.nc2.dataset.EnhanceScaleMissing
     */
    protected boolean forcePacking(final String attribute) {
        return false;
    }
}
