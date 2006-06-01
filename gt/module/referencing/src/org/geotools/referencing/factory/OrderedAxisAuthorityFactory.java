/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
 * (C) 2005, Institut de Recherche pour le Développement
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
 */
package org.geotools.referencing.factory;

// J2SE dependencies and extensions
import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Comparator;
import javax.units.Unit;
import javax.units.SI;
import javax.units.NonSI;

// OpenGIS dependencies
import org.opengis.referencing.cs.*;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.FactoryException;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.cs.DefaultCoordinateSystemAxis;
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * An authority factory which delegates all the work to an other factory, and reorder the axis in
 * some pre-determined order. This factory is mostly used by application expecting geographic
 * coordinates in (<var>longitude</var>, <var>latitude</var>) order, while most geographic CRS
 * specified in the <A HREF="http://www.epsg.org">EPSG database</A> use the opposite axis order.
 * <p>
 * It is better to avoid this class if you can. This class exists primarily for compatibility with
 * external data or applications that assume (<var>longitude</var>, <var>latitude</var>) axis order
 * no matter what the EPSG database said, for example Shapefiles.
 * <p>
 * The axis order can be specified at construction time as an array of {@linkplain AxisDirection
 * axis directions}. If no such array is explicitly specified, then the default order is
 * {@linkplain AxisDirection#EAST               East},
 * {@linkplain AxisDirection#EAST_NORTH_EAST    East-North-East},
 * {@linkplain AxisDirection#NORTH_EAST         North-East},
 * {@linkplain AxisDirection#NORTH_NORTH_EAST   North-North-East},
 * {@linkplain AxisDirection#NORTH              North},
 * {@linkplain AxisDirection#UP                 Up},
 * {@linkplain AxisDirection#GEOCENTRIC_X       Geocentric X},
 * {@linkplain AxisDirection#GEOCENTRIC_Y       Geocentric Y},
 * {@linkplain AxisDirection#GEOCENTRIC_Z       Geocentric Z},
 * {@linkplain AxisDirection#COLUMN_POSITIVE    Column},
 * {@linkplain AxisDirection#ROW_POSITIVE       Row},
 * {@linkplain AxisDirection#DISPLAY_RIGHT      Display right},
 * {@linkplain AxisDirection#DISPLAY_UP         Display up} and
 * {@linkplain AxisDirection#FUTURE             Future}.
 * This means that, for example, axis with East or West direction will be placed before any
 * axis with North or South direction. Axis directions not specified in the table (for example
 * {@link AxisDirection#OTHER OTHER}) will be ordered last. This is somewhat equivalent to the
 * ordering of {@link Double#NaN NaN} values in an array of {@code double}.
 * <p>
 * <strong>Notes:</strong>
 * <ul>
 *   <li>This class compares only the "{@linkplain AxisDirection#absolute absolute}" axis
 *       directions, so North and South are considered equivalent.</li>
 *   <li>The default direction order may changes in future Geotools version in order
 *       to fit what appears to be the most common usage on the market.</li>
 *   <li>The actual axis ordering is determined by the {@link #compare compare} method
 *       implementation. Subclasses may override this method if the want to provide a more
 *       sophesticated axis ordering.</li>
 * </ul>
 * <p>
 * For some authority factories, an instance of this class can be obtained by passing a
 * {@link Hints#FORCE_LONGITUDE_FIRST_AXIS_ORDER FORCE_LONGITUDE_FIRST_AXIS_ORDER} hint
 * to the <code>{@linkplain FactoryFinder#getCRSAuthorityFactory
 * FactoryFinder.getCRSAuthorityFactory}(...)</code> method. Whatever this hint is supported
 * or not is authority dependent. Example:
 *
 * <blockquote><pre>
 * Hints                   hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
 * CRSAuthorityFactory   factory = FactoryFinder.getCRSAuthorityFactory("EPSG", hints);
 * CoordinateReferenceSystem crs = factory.createCoordinateReferenceSystem("EPSG:4326");
 * </pre></blockquote>
 *
 * This class is named <cite>ordered axis authority factory</cite> instead of something like
 * <cite>longitude first axis order</code> because the axis order can be user-supplied. The
 * (<var>longitude</var>, <var>latitude</var>) order just appears to be the default one.
 *
 * @since 2.2
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see Hints#FORCE_LONGITUDE_FIRST_AXIS_ORDER
 * @see Hints#FORCE_STANDARD_AXIS_UNITS
 * @tutorial http://docs.codehaus.org/display/GEOTOOLS/The+axis+order+issue
 */
public class OrderedAxisAuthorityFactory extends AuthorityFactoryAdapter
        implements CSAuthorityFactory, CRSAuthorityFactory, Comparator/*<CoordinateSystemAxis>*/
{
    /**
     * The default order for axis directions. Note that this array needs to contain only the
     * "{@linkplain AxisDirection#absolute absolute}" directions.
     *
     * REMINDER: If this array is modified, don't forget to update the class javadoc above.
     */
    private static final AxisDirection[] DEFAULT_ORDER = {
        AxisDirection.EAST,
        AxisDirection.EAST_NORTH_EAST,
        AxisDirection.NORTH_EAST,
        AxisDirection.NORTH_NORTH_EAST,
        AxisDirection.NORTH,
        AxisDirection.UP,
        AxisDirection.GEOCENTRIC_X,
        AxisDirection.GEOCENTRIC_Y,
        AxisDirection.GEOCENTRIC_Z,
        AxisDirection.COLUMN_POSITIVE,
        AxisDirection.ROW_POSITIVE,
        AxisDirection.DISPLAY_RIGHT,
        AxisDirection.DISPLAY_UP,
        AxisDirection.FUTURE
    };

    /**
     * Instances of {@link OrderedAxisAuthorityFactory} registered in {@link FactoryFinder} for
     * the specified authority names. This is used by {@link #register} and {@link #unregister}
     * methods.
     *
     * @deprecated Remove after we deleted {@link #register} and {@link #unregister}.
     */
    private static final Map/*<String,OrderedAxisAuthorityFactory>*/ REGISTERED = new HashMap();

    /**
     * The rank to be given to each axis direction. The rank is stored at the indice
     * corresponding to the direction {@linkplain AxisDirection#ordinal ordinal} value.
     */
    private final int[] directionRanks;

    /**
     * {@code true} if this authority factory should also force the axis to their standard
     * direction. For example if {@code true}, then axis with increasing values toward South
     * will be converted to axis with increasing values toward North. The default value is
     * {@code false}.
     *
     * @see Hints#FORCE_STANDARD_AXIS_DIRECTIONS
     * @since 2.3
     */
    protected final boolean forceStandardDirections;

    /**
     * {@code true} if this authority factory should also force all angular units to degrees
     * and linear units to meters. The default value is {@code false}.
     *
     * @see Hints#FORCE_STANDARD_AXIS_UNITS
     * @since 2.3
     */
    protected final boolean forceStandardUnits;

    /**
     * Creates a factory which will reorder the axis of all objects created by the default
     * authority factories. The factories are fetched using {@link FactoryFinder}. This
     * constructor accepts the following hints:
     * <p>
     * <ul>
     *   <li>{@link Hints#FORCE_STANDARD_AXIS_UNITS}</li>
     *   <li>{@link Hints#FORCE_STANDARD_AXIS_DIRECTIONS}</li>
     *   <li>All hints understood by {@link FactoryFinder}</li>
     * </ul>
     *
     * @param  authority The authority to wraps (example: {@code "EPSG"}). If {@code null},
     *         then all authority factories must be explicitly specified in the set of hints.
     * @param  hints An optional set of hints, or {@code null} if none.
     * @param  axisOrder An array of axis directions that determine the axis order wanted,
     *         or {@code null} for the default axis order.
     * @throws FactoryRegistryException if at least one factory can not be obtained.
     * @throws IllegalArgumentException If at least two axis directions are colinear.
     *
     * @since 2.3
     */
    public OrderedAxisAuthorityFactory(final String          authority,
                                       final Hints           hints,
                                       final AxisDirection[] axisOrder)
            throws FactoryRegistryException, IllegalArgumentException
    {
        super(authority, hints);
        forceStandardUnits      = booleanValue(hints, Hints.FORCE_STANDARD_AXIS_UNITS);
        forceStandardDirections = booleanValue(hints, Hints.FORCE_STANDARD_AXIS_DIRECTIONS);
        directionRanks          = computeDirectionRanks(axisOrder);
        completeHints();
    }

    /**
     * Creates a factory which will reorder the axis of all objects created by the supplied
     * factory. This constructor accepts the following optional hints:
     * <p>
     * <ul>
     *   <li>{@link Hints#FORCE_STANDARD_AXIS_UNITS}</li>
     *   <li>{@link Hints#FORCE_STANDARD_AXIS_DIRECTIONS}</li>
     * </ul>
     *
     * @param  factory   The factory that produces objects using arbitrary axis order.
     * @param  hints     An optional set of hints, or {@code null} if none.
     * @param  axisOrder An array of axis directions that determine the axis order wanted,
     *                   or {@code null} for the default axis order.
     * @throws IllegalArgumentException If at least two axis directions are colinear.
     *
     * @since 2.3
     */
    public OrderedAxisAuthorityFactory(final AbstractAuthorityFactory factory,
                                       final Hints                    hints,
                                       final AxisDirection[]          axisOrder)
            throws IllegalArgumentException
    {
        super(factory);
        forceStandardUnits      = booleanValue(hints, Hints.FORCE_STANDARD_AXIS_UNITS);
        forceStandardDirections = booleanValue(hints, Hints.FORCE_STANDARD_AXIS_DIRECTIONS);
        directionRanks          = computeDirectionRanks(axisOrder);
        completeHints();
    }

    /**
     * Creates a factory which will reorder the axis of all objects created by the default
     * authority factories. The factories are fetched using {@link FactoryFinder}.
     * <p>
     * <strong>WARNING:</strong> Do not invoke this constructor from a subclass to be registered in
     * a {@code META-INF/services/} file for use by {@link FactoryFinder}. It may lead to recursive
     * calls until a {@link StackOverflowError} is thrown.
     *
     * @param  authority The authority to wraps (example: {@code "EPSG"}).
     * @param  hints An optional set of hints, or {@code null} if none.
     * @param  forceStandardUnits {@code true} if this authority factory should also force all
     *         angular units to degrees and linear units to meters, or {@code false} if the
     *         units should be left unchanged.
     * @throws FactoryRegistryException if at least one factory can not be obtained.
     *
     * @deprecated Replaced by {@link #OrderedAxisAuthorityFactory(String,Hints,AxisDirection[])}.
     */
    public OrderedAxisAuthorityFactory(String authority, Hints hints, boolean forceStandardUnits) {
        this(authority, mergeHints(hints, forceStandardUnits), null);
    }

    /**
     * Creates a factory which will reorder the axis of all objects created by
     * the supplied factory.
     *
     * @param factory  The factory that produces objects using arbitrary axis order.
     * @param forceStandardUnits {@code true} if this authority factory should also force all
     *        angular units to degrees and linear units to meters, or {@code false} if the units
     *        should be left unchanged.
     *
     * @deprecated Replaced by {@link #OrderedAxisAuthorityFactory(AbstractAuthorityFactory,Hints,
     *             AxisDirection[])}.
     */
    public OrderedAxisAuthorityFactory(final AbstractAuthorityFactory factory,
                                       final boolean forceStandardUnits)
    {
        this(factory, mergeHints(null, forceStandardUnits), null);
    }

    /**
     * @deprecated To be removed after we removed the deprecated constructors.
     */
    private static Hints mergeHints(Hints hints, final boolean forceStandardUnits) {
        hints = new Hints(hints);
        hints.put(Hints.FORCE_STANDARD_AXIS_UNITS, Boolean.valueOf(forceStandardUnits));
        return hints;
    }

    /**
     * Returns the boolean value for the specified hint.
     */
    private static boolean booleanValue(final Hints hints, final Hints.Key key) {
        if (hints != null) {
            final Boolean value = (Boolean) hints.get(key);
            if (value != null) {
                return value.booleanValue();
            }
        }
        return false;
    }

    /**
     * Completes the set of hints according the value currently set in this object.
     * This method is invoked by constructors only.
     */
    private void completeHints() {
        hints.put(Hints.FORCE_STANDARD_AXIS_UNITS,      Boolean.valueOf(forceStandardUnits));
        hints.put(Hints.FORCE_STANDARD_AXIS_DIRECTIONS, Boolean.valueOf(forceStandardDirections));
        // The following hint has no effect on this class behaviour,
        // but tells to the user what this factory do about axis order.
        if (compare(DefaultCoordinateSystemAxis.EASTING, DefaultCoordinateSystemAxis.NORTHING) < 0) {
            hints.put(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
        }
    }

    /**
     * Registers an <cite>ordered axis authority factory</cite> as a replacement of the specified
     * authority. If this method has already been invoked previously for the same authority, then
     * this method invocation does nothing. Otherwise, it performs the following steps:
     * <p>
     * <ul>
     *   <li>A new {@code OrderedAxisAuthorityFactory} instance is created as a wrapper around
     *       the default authority factories provided by
     * <code>{@linkplain FactoryFinder}.get<var>Foo</var>AuthorityFactory(authority)</code></li>
     *
     *   <li>This new instance is registered in {@link FactoryFinder} with a priority slightly
     *       higher than the priority of wrapped factories. Consequently, the <cite>reordered
     *       axis authority factory</cite> should become the default one for the specified
     *       authority.</li>
     * </ul>
     * <p>
     * <strong>WARNING:</strong> this method has a system-wide effect. Any user asking for the
     * specified {@code authority} will get an <cite>ordered axis authority factory</cite>
     * instance. It may be misleading for client code expecting the official factory. Avoid
     * this method unless you really need reordered axis for all code in the current Java
     * Virtual Machine.
     *
     * @param  authority The name of the authority factories to override with an ordered axis
     *         instance.
     * @throws FactoryRegistryException if the registration failed.
     *
     * @deprecated This method has a system-wide effect. Use
     *             {@link Hints#FORCE_LONGITUDE_FIRST_AXIS_ORDER} instead, which provides
     *             a case-by-case control. If a system-wide effect is really wanted, use
     *             {@code System.setProperty("org.geotools.referencing.forceXY", "true")}.
     */
    public static void register(String authority) throws FactoryRegistryException {
        authority = authority.toUpperCase().trim();
        synchronized (REGISTERED) {
            if (REGISTERED.containsKey(authority)) {
                return;
            }
            final OrderedAxisAuthorityFactory candidate =
                    new OrderedAxisAuthorityFactory(authority, null, false);
            FactoryFinder.addAuthorityFactory(candidate);
            if (REGISTERED.put(authority, candidate) != null) {
                // Paranoïac check: should never happen because of the 'containsKey' check above.
                throw new AssertionError();
            }
        }
    }

    /**
     * Unregisters an <cite>ordered axis authority factory</cite> previously registered with
     * the {@link #register register} method.
     *
     * @param  authority The authority name given to the {@link #register register} method.
     * @throws FactoryRegistryException if the unregistration failed.
     *
     * @deprecated This method has a system-wide effect. Use
     *             {@link Hints#FORCE_LONGITUDE_FIRST_AXIS_ORDER} instead, which provides
     *             a case-by-case control. If a system-wide effect is really wanted, use
     *             {@code System.setProperty("org.geotools.referencing.forceXY", "false")}.
     */
    public static void unregister(String authority) throws FactoryRegistryException {
        authority = authority.toUpperCase().trim();
        synchronized (REGISTERED) {
            final AbstractAuthorityFactory candidate;
            candidate = (AbstractAuthorityFactory) REGISTERED.remove(authority);
            if (candidate != null) {
                FactoryFinder.removeAuthorityFactory(candidate);
            }
        }
    }

    /**
     * Computes the rank for every direction in the specified. The rank is stored in an array
     * at the indice corresponding to the direction {@linkplain AxisDirection#ordinal ordinal}
     * value. This method is used by constructors for computing the {@link #directionRanks} field.
     *
     * @throws IllegalArgumentException If at least two axis directions are colinear.
     */
    private static int[] computeDirectionRanks(AxisDirection[] axisOrder)
            throws IllegalArgumentException
    {
        if (axisOrder == null) {
            axisOrder = DEFAULT_ORDER;
        }
        int length = 0;
        for (int i=0; i<axisOrder.length; i++) {
            final int ordinal = axisOrder[i].absolute().ordinal() + 1;
            if (ordinal > length) {
                length = ordinal;
            }
        }
        final int[] directionRanks = new int[length];
        Arrays.fill(directionRanks, length);
        for (int i=0; i<axisOrder.length; i++) {
            final int ordinal  = axisOrder[i].absolute().ordinal();
            final int previous = directionRanks[ordinal];
            if (previous != length) {
                // TODO: Use the localized version of 'getName' in GeoAPI 2.1
                throw new IllegalArgumentException(Errors.format(ErrorKeys.COLINEAR_AXIS_$2,
                                          axisOrder[previous].name(), axisOrder[i].name()));
            }
            directionRanks[ordinal] = i;
        }
        return directionRanks;
    }

    /**
     * Returns the rank for the specified axis. Any axis that were not specified
     * at construction time will ordered after all known axis.
     */
    private final int rank(final CoordinateSystemAxis axis) {
        int c = axis.getDirection().absolute().ordinal();
        c = (c>=0 && c<directionRanks.length) ? directionRanks[c] : directionRanks.length;
        return c;
    }

    /**
     * Compares two axis for order. This method is invoked automatically by the
     * {@link #replace(CoordinateSystem) replace} method for ordering the axis in a
     * coordinate system. The default implementation orders the axis according their
     * {@linkplain CoordinateSystemAxis#getDirection direction}, using the direction
     * table given at {@linkplain #OrderedAxisAuthorityFactory(AbstractAuthorityFactory,
     * Hints, AxisDirection[]) construction time} (see also the class description).
     * Subclasses may override this method if they want to define a more sophesticated
     * axis ordering.
     *
     * @param  axis1 The first axis to compare.
     * @param  axis2 The second axis to compare.
     * @return A negative integer if {@code axis1} should appears before {@code axis2}, or a
     *         positive number if {@code axis2} should appears before {@code axis1}, or 0 if
     *         the two axis are unordered one relative to the other.
     *
     * @todo The argument type will be changed to {@link CoordinateSystemAxis} when we will
     *       be allowed to compile for J2SE 1.5.
     *
     * @since 2.3
     */
    public int compare(final Object axis1, final Object axis2) {
        return rank((CoordinateSystemAxis) axis1) - rank((CoordinateSystemAxis) axis2);
    }

    /**
     * Returns a coordinate system with the same properties than the specified one, except for
     * axis order, units and direction. The axis may be reordered according the criterion defined
     * by the {@link #compare compare} method. In addition, axis directions and units may also be
     * modified according the hints provided at construction time.
     *
     * @throws FactoryException if an error occured while creating the new coordinate system.
     */
    protected CoordinateSystem replace(final CoordinateSystem cs) throws FactoryException {
        final int dimension = cs.getDimension();
        final CoordinateSystemAxis[] orderedAxis = new CoordinateSystemAxis[dimension];
        FactoryGroup factories = null;
        for (int i=0; i<dimension; i++) {
            CoordinateSystemAxis axis = cs.getAxis(i);
            final AxisDirection oldDirection = axis.getDirection();
            final AxisDirection newDirection = replace(oldDirection);
            final Unit          oldUnits     = axis.getUnit();
            final Unit          newUnits     = replace(oldUnits);
            if (!oldDirection.equals(newDirection) || !oldUnits.equals(newUnits)) {
                if (factories == null) {
                    factories = getFactoryGroup(csFactory);
                }
                final CSFactory csFactory = factories.getCSFactory();
                final Map properties = getProperties(axis);
                axis = csFactory.createCoordinateSystemAxis(properties,
                        axis.getAbbreviation(), newDirection, newUnits);
            }
            orderedAxis[i] = axis;
        }
        Arrays.sort(orderedAxis, this);
        for (int i=0; i<dimension; i++) {
            if (!orderedAxis[i].equals(cs.getAxis(i))) {
                CoordinateSystem modified = createCS(cs.getClass(), getProperties(cs), orderedAxis);
                assert Utilities.sameInterfaces(cs.getClass(), modified.getClass(), CoordinateSystem.class);
                modified = (CoordinateSystem) pool.canonicalize(modified);
                return modified;
            }
        }
        // All axis are identical - the CS was actually not changed.
        return cs;
    }

    /**
     * Replaces the specified direction, if applicable. This method is invoked automatically by the
     * {@link #replace(CoordinateSystem)} method. The default implementation replaces the direction
     * only if the {@link Hints#FORCE_STANDARD_AXIS_DIRECTIONS FORCE_STANDARD_AXIS_DIRECTIONS} hint
     * was specified as {@link Boolean#TRUE TRUE} at construction time. In such case, the default
     * substitution table is as specified in the {@link AxisDirection#absolute} method.
     * Subclasses may override this method if they want to use a different substitution table.
     *
     * @since 2.3
     */
    protected AxisDirection replace(final AxisDirection direction) {
        return (forceStandardDirections) ? direction.absolute() : direction;
    }

    /**
     * Replaces the specified unit, if applicable. This method is invoked automatically by the
     * {@link #replace(CoordinateSystem)} method. The default implementation replaces the unit
     * only if the {@link Hints#FORCE_STANDARD_AXIS_UNITS FORCE_STANDARD_AXIS_UNITS} hint was
     * specified as {@link Boolean#TRUE TRUE} at construction time. In such case, the default
     * substitution table is:
     * <p>
     * <ul>
     *   <li>Any linear units converted to {@linkplain SI#METER meters}</li>
     *   <li>{@linkplain SI#RADIAN Radians} and {@linkplain NonSI#GRADE grades} converted to
     *       {@linkplain NonSI#DEGREE_ANGLE degrees}</li>
     * </ul>
     * <p>
     * This default substitution table may be expanded in future Geotools versions.
     *
     * @since 2.3
     */
    protected Unit replace(final Unit units) {
        if (forceStandardUnits) {
            if (units.isCompatible(SI.METER)) {
                return SI.METER;
            }
            if (units.equals(SI.RADIAN) || units.equals(NonSI.GRADE)) {
                return NonSI.DEGREE_ANGLE;
            }
        }
        return units;
    }

    /**
     * Creates a new coordinate system of the specified kind. This method is invoked automatically
     * by {@link #replace(CoordinateSystem)} after it determined that the axis order need to be
     * changed. Subclasses can override this method if they want to performs some extra processing
     * on the axis order.
     * 
     * Note: this method is private for now because I'm not sure it is worth protected access.
     *
     * @param  type The coordinate system type to create.
     * @param  properties The properties to gives to the new coordinate system.
     * @param  axis The axis to give to the new coordinate system. Subclasses are allowed to write
     *              directly in this array (no need to copy it).
     * @return A new coordinate system of the specified kind with the specified axis.
     * @throws FactoryException if the coordinate system can't be created.
     */
    private CoordinateSystem createCS(final Class/*<CoordinateSystem>*/ type,
                                      final Map properties,
                                      final CoordinateSystemAxis[] axis)
            throws FactoryException
    {
        final int          dimension = axis.length;
        final FactoryGroup factories = getFactoryGroup(csFactory);
        final CSFactory    csFactory = factories.getCSFactory();
        if (CartesianCS.class.isAssignableFrom(type)) {
            switch (dimension) {
                case 2: return csFactory.createCartesianCS(properties, axis[0], axis[1]);
                case 3: return csFactory.createCartesianCS(properties, axis[0], axis[1], axis[2]);
            }
        } else
        if (EllipsoidalCS.class.isAssignableFrom(type)) {
            switch (dimension) {
                case 2: return csFactory.createEllipsoidalCS(properties, axis[0], axis[1]);
                case 3: return csFactory.createEllipsoidalCS(properties, axis[0], axis[1], axis[2]);
            }
        } else
        if (SphericalCS.class.isAssignableFrom(type)) {
            switch (dimension) {
                case 3: return csFactory.createSphericalCS(properties, axis[0], axis[1], axis[2]);
            }
        } else
        if (CylindricalCS.class.isAssignableFrom(type)) {
            switch (dimension) {
                case 3: return csFactory.createCylindricalCS(properties, axis[0], axis[1], axis[2]);
            }
        } else
        if (PolarCS.class.isAssignableFrom(type)) {
            switch (dimension) {
                case 2: return csFactory.createPolarCS(properties, axis[0], axis[1]);
            }
        } else
        if (VerticalCS.class.isAssignableFrom(type)) {
            switch (dimension) {
                case 1: return csFactory.createVerticalCS(properties, axis[0]);
            }
        } else
        if (TimeCS.class.isAssignableFrom(type)) {
            switch (dimension) {
                case 1: return csFactory.createTimeCS(properties, axis[0]);
            }
        } else
        if (LinearCS.class.isAssignableFrom(type)) {
            switch (dimension) {
                case 1: return csFactory.createLinearCS(properties, axis[0]);
            }
        } else
        if (UserDefinedCS.class.isAssignableFrom(type)) {
            switch (dimension) {
                case 2: return csFactory.createUserDefinedCS(properties, axis[0], axis[1]);
                case 3: return csFactory.createUserDefinedCS(properties, axis[0], axis[1], axis[2]);
            }
        }
        throw new FactoryException(Errors.format(ErrorKeys.UNSUPPORTED_COORDINATE_SYSTEM_$1,
                                   Utilities.getShortName(type)));
    }

    /**
     * Returns the error message for the specified coordinate system.
     * Used when throwing {@link FactoryException}.
     */
    private static final String getErrorMessage(final CoordinateSystem cs) {
        return Errors.format(ErrorKeys.UNSUPPORTED_COORDINATE_SYSTEM_$1, cs.getName().getCode());
    }
}
