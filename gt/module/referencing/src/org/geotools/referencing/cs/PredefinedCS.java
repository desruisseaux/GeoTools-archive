/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.geotools.referencing.cs;

// J2SE dependencies
import java.util.List;
import java.util.Arrays;
import java.util.Comparator;

// OpenGIS dependencies
import org.opengis.referencing.cs.*;

// Geotools dependencies
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.referencing.cs.DefaultCoordinateSystemAxis;


/**
 * Converts an arbitrary CS into one of the predefined constants provided in the
 * {@link org.geotools.referencing.cs} package. The main usage for this class is
 * to reorder the axis in some "standard" order like (<var>x</var>, <var>y</var>,
 * <var>z</var>) or (<var>longitude</var>, <var>latitude</var>). What "standard"
 * order means is sometime an arbitrary choice, which explain why this class is
 * not public at this time.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class PredefinedCS implements Comparator {
    /**
     * An instance of {@link PredefinedCS}. Will be created only when first needed.
     */
    private static Comparator csComparator;

    /**
     * Our ordering for coordinate system objects.
     */
    private final Class[] types = {
        CartesianCS  .class,
        AffineCS     .class,
        EllipsoidalCS.class,
        SphericalCS  .class,
        CylindricalCS.class,
        PolarCS      .class,
        VerticalCS   .class,
        TimeCS       .class,
        LinearCS     .class,
        UserDefinedCS.class
    };

    /**
     * Creates a comparator.
     */
    private PredefinedCS() {
    }

    /**
     * Compares the ordering between two coordinate systems. This comparator is used for sorting
     * the axis in an user-supplied compound CS in an order closes to some "standard" order.
     */
    public int compare(final Object object1, final Object object2) {
        final Class type1 = object1.getClass();
        final Class type2 = object2.getClass();
        for (int i=0; i<types.length; i++) {
            final Class type = types[i];
            final boolean a1 = type.isAssignableFrom(type1);
            final boolean a2 = type.isAssignableFrom(type2);
            if (a1) return a2 ? 0 : -1;
            if (a2) return a1 ? 0 : +1;
        }
        return 0;
    }

    /**
     * Implementation of the {@link AbstractCS#standard} method.
     */
    static CoordinateSystem standard(final CoordinateSystem cs) throws IllegalArgumentException {
        final int dimension = cs.getDimension();
        if (cs instanceof CartesianCS) {
            switch (dimension) {
                case 2: {
                    if (sameAxisNames(DefaultCartesianCS.PROJECTED, cs)) {
                        return DefaultCartesianCS.PROJECTED;
                    }
                    if (sameAxisNames(DefaultCartesianCS.GRID, cs)) {
                        return DefaultCartesianCS.GRID;
                    }
                    return DefaultCartesianCS.GENERIC_2D;
                }
                case 3: {
                    if (sameAxisNames(DefaultCartesianCS.GEOCENTRIC, cs)) {
                        return DefaultCartesianCS.GEOCENTRIC;
                    }
                    return DefaultCartesianCS.GENERIC_3D;
                }
            }
        }
        if (cs instanceof EllipsoidalCS) {
            switch (dimension) {
                case 2: return DefaultEllipsoidalCS.GEODETIC_2D;
                case 3: return DefaultEllipsoidalCS.GEODETIC_3D;
            }
        }
        if (cs instanceof SphericalCS) {
            switch (dimension) {
                case 3: return DefaultSphericalCS.GEOCENTRIC;
            }
        }
        if (cs instanceof VerticalCS) {
            switch (dimension) {
                case 1: return DefaultVerticalCS.ELLIPSOIDAL_HEIGHT;
            }
        }
        if (cs instanceof TimeCS) {
            switch (dimension) {
                case 1: return DefaultTimeCS.DAYS;
            }
        }
        if (cs instanceof DefaultCompoundCS) {
            final List components = ((DefaultCompoundCS) cs).getCoordinateSystems();
            final CoordinateSystem[] user = new CoordinateSystem[components.size()];
            final CoordinateSystem[] std  = new CoordinateSystem[user.length];
            for (int i=0; i<std.length; i++) {
                std[i] = standard(user[i] = (CoordinateSystem) components.get(i));
            }
            if (csComparator == null) {
                csComparator = new PredefinedCS();
            }
            Arrays.sort(std, csComparator);
            return Arrays.equals(user, std) ? cs : new DefaultCompoundCS(std);
        }
        throw new IllegalArgumentException(
                Errors.format(ErrorKeys.UNSUPPORTED_COORDINATE_SYSTEM_$1, cs.getName().getCode()));
    }

    /**
     * Checks if all axis in this coordinate system has the same names than axis in the specified
     * CS. The order, direction and units may be different however. Note that the specified CS may
     * have more dimensions than this CS.
     * <p>
     * We uses the axis names as the criterion for comparaisons because those names are fixed
     * by the ISO 19111 specification.
     */
    private static boolean sameAxisNames(final CoordinateSystem stdCS, final CoordinateSystem userCS) {
        final String[] userNames = new String[userCS.getDimension()];
        for (int i=0; i<userNames.length; i++) {
            userNames[i] = userCS.getAxis(i).getName().getCode().trim();
        }
check:  for (int i=stdCS.getDimension(); --i>=0;) {
            final CoordinateSystemAxis axis = stdCS.getAxis(i);
            final String name = axis.getName().getCode().trim();
            String opposite = null;
            if (axis instanceof DefaultCoordinateSystemAxis) {
                final CoordinateSystemAxis op = ((DefaultCoordinateSystemAxis) axis).getOpposite();
                if (op != null) {
                    opposite = op.getName().getCode().trim();
                }
            }
            for (int j=userNames.length; --j>=0;) {
                final String userName = userNames[j];
                if (userName!=null && (userName.equalsIgnoreCase(name) ||
                   (opposite!=null && userName.equalsIgnoreCase(opposite))))
                {
                    userNames[j] = null; // Do not compare the same axis twice.
                    continue check;
                }
            }
            return false; // At least one axis doesn't match.
        }
        /*
         * All axis in this CS match one axis in the specified CS.  Note that some extra axis may
         * remains in the specified CS; we choose to ignore them. Anyway, it should not happen if
         * both CS have the same number of dimensions.
         */
        return true;
    }
}
