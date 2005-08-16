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

// J2SE dependencies and extensions
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.units.ConversionException;
import javax.units.Converter;
import javax.units.NonSI;
import javax.units.SI;
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.cs.EllipsoidalCS;
import org.opengis.referencing.cs.SphericalCS;
import org.opengis.referencing.cs.VerticalCS;
import org.opengis.referencing.operation.Matrix;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.measure.Measure;
import org.geotools.referencing.AbstractIdentifiedObject;
import org.geotools.referencing.operation.matrix.GeneralMatrix;
import org.geotools.referencing.wkt.Formatter;
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;


/**
 * The set of coordinate system axes that spans a given coordinate space. A coordinate system (CS)
 * is derived from a set of (mathematical) rules for specifying how coordinates in a given space
 * are to be assigned to points. The coordinate values in a coordinate tuple shall be recorded in
 * the order in which the coordinate system axes are recorded, whenever those
 * coordinates use a coordinate reference system that uses this coordinate system.
 * <p>
 * This class is conceptually <cite>abstract</cite>, even if it is technically possible to
 * instantiate it. Typical applications should create instances of the most specific subclass with
 * {@code Default} prefix instead. An exception to this rule may occurs when it is not possible to
 * identify the exact type. For example it is not possible to infer the exact coordinate system from
 * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
 * Known Text</cite></A> is some cases (e.g. in a {@code LOCAL_CS} element). In such exceptional
 * situation, a plain {@code AbstractCS} object may be instantiated.
 *
 * @since 2.1
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see DefaultCoordinateSystemAxis
 * @see javax.units.Unit
 * @see org.geotools.referencing.datum.AbstractDatum
 * @see org.geotools.referencing.crs.AbstractCRS
 */
public class AbstractCS extends AbstractIdentifiedObject implements CoordinateSystem {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 6757665252533744744L;
    
    /**
     * The axis for this coordinate system at the specified dimension.
     */
    private final CoordinateSystemAxis[] axis;

    /**
     * The unit for measuring distance in this coordinate system, or {@code null} if none.
     * Will be computed only when first needed.
     */
    private transient Unit distanceUnit;

    /**
     * Constructs a new coordinate system with the same values than the specified one.
     * This copy constructor provides a way to wrap an arbitrary implementation into a
     * Geotools one or a user-defined one (as a subclass), usually in order to leverage
     * some implementation-specific API. This constructor performs a shallow copy,
     * i.e. the properties are not cloned.
     *
     * @since 2.2
     */
    public AbstractCS(final CoordinateSystem cs) {
        super(cs);
        if (cs instanceof AbstractCS) {
            axis = ((AbstractCS) cs).axis;
        } else {
            axis = new CoordinateSystemAxis[cs.getDimension()];
            for (int i=0; i<axis.length; i++) {
                axis[i] = cs.getAxis(i);
            }
        }
    }

    /**
     * Constructs a coordinate system from a name.
     *
     * @param name  The coordinate system name.
     * @param axis  The set of axis.
     */
    public AbstractCS(final String name, final CoordinateSystemAxis[] axis) {
        this(Collections.singletonMap(NAME_KEY, name), axis);
    }

    /**
     * Constructs a coordinate system from a set of properties. The properties map is given
     * unchanged to the {@linkplain AbstractIdentifiedObject#AbstractIdentifiedObject(Map)
     * super-class constructor}.
     *
     * @param properties   Set of properties. Should contains at least <code>"name"</code>.
     * @param axis         The set of axis.
     */
    public AbstractCS(final Map properties, final CoordinateSystemAxis[] axis) {
        super(properties);
        ensureNonNull("axis", axis);
        this.axis = (CoordinateSystemAxis[]) axis.clone();
        /*
         * Makes sure there is no axis along the same direction
         * (e.g. two North axis, or an East and a West axis).
         */
        for (int i=0; i<axis.length; i++) {
            ensureNonNull("axis", axis, i);
            AxisDirection check = axis[i].getDirection();
            ensureNonNull("direction", check);
            if (!isCompatibleDirection(check)) {
                // TOOD: localize name()
                throw new IllegalArgumentException(Errors.format(
                            ErrorKeys.ILLEGAL_AXIS_ORIENTATION_$2,
                            check.name(), Utilities.getShortClassName(this)));
            }
            final Unit unit = axis[i].getUnit();
            if (!isCompatibleUnit(check, unit)) {
                throw new IllegalArgumentException(Errors.format(
                            ErrorKeys.INCOMPATIBLE_UNIT_$1, unit));
            }
            check = check.absolute();
            if (!check.equals(AxisDirection.OTHER)) {
                for (int j=i; --j>=0;) {
                    if (check.equals(axis[j].getDirection().absolute())) {
                        // TODO: localize name()
                        final String nameI = axis[i].getDirection().name();
                        final String nameJ = axis[j].getDirection().name();
                        throw new IllegalArgumentException(Errors.format(
                                    ErrorKeys.COLINEAR_AXIS_$2, nameI, nameJ));
                    }
                }
            }
        }
    }

    /**
     * Creates a name for the predefined constants in subclasses. The name is an unlocalized String
     * object. However, since this method is used for creation of convenience objects only (not for
     * objects created from an "official" database), the "unlocalized" name is actually choosen
     * according the user's locale at class initialization time. The same name is also added in
     * a localizable form as an alias. Since the {@link #nameMatches} convenience method checks
     * the alias, it still possible to consider two objects are equivalent even if their names
     * were formatted in different locales.
     */
    static Map name(final int key) {
        final Map properties = new HashMap(4);
        final InternationalString name = Vocabulary.formatInternational(key);
        properties.put(NAME_KEY,  name.toString());
        properties.put(ALIAS_KEY, name);
        return properties;
    }

    /**
     * Returns {@code true} if the specified axis direction is allowed for this coordinate
     * system. This method is invoked at construction time for checking argument validity.
     * The default implementation returns {@code true} for all axis directions. Subclasses
     * will overrides this method in order to put more restrictions on allowed axis directions.
     */
    protected boolean isCompatibleDirection(final AxisDirection direction) {
        return true;
    }

    /**
     * Returns {@code true} is the specified unit is legal for the specified axis direction.
     * This method is invoked at construction time for checking units compatibility. The default
     * implementation returns {@code true} in all cases. Subclasses can override this method and
     * check for compatibility with {@linkplain SI#METER meter} or
     * {@linkplain NonSI#DEGREE_ANGLE degree} units.
     *
     * @since 2.2
     */
    protected boolean isCompatibleUnit(final AxisDirection direction, final Unit unit) {
        return true;
    }

    /**
     * Returns the dimension of the coordinate system.
     * This is the number of axis.
     */
    public int getDimension() {
        return axis.length;
    }

    /**
     * Returns the axis for this coordinate system at the specified dimension.
     *
     * @param  dimension The zero based index of axis.
     * @return The axis at the specified dimension.
     * @throws IndexOutOfBoundsException if {@code dimension} is out of bounds.
     */
    public CoordinateSystemAxis getAxis(final int dimension) throws IndexOutOfBoundsException {
        return axis[dimension];
    }
    
    /**
     * Returns the axis direction for the specified coordinate system.
     *
     * @param  cs The coordinate system.
     * @return The axis directions for the specified coordinate system.
     */
    private static AxisDirection[] getAxisDirections(final CoordinateSystem cs) {
        final AxisDirection[] axis = new AxisDirection[cs.getDimension()];
        for (int i=0; i<axis.length; i++) {
            axis[i] = cs.getAxis(i).getDirection();
        }
        return axis;
    }

    /**
     * Returns an affine transform between two coordinate systems. Only units and
     * axis order (e.g. transforming from
     * ({@linkplain AxisDirection#NORTH NORTH},{@linkplain AxisDirection#WEST WEST}) to
     * ({@linkplain AxisDirection#EAST EAST},{@linkplain AxisDirection#NORTH NORTH}
     * are taken in account.
     *
     * <P><STRONG>Example:</STRONG> If coordinates in {@code sourceCS} are
     * (<var>x</var>,<var>y</var>) pairs in metres and coordinates in {@code targetCS}
     * are (-<var>y</var>,<var>x</var>) pairs in centimetres, then the transformation
     * can be performed as below:</P>
     *
     * <pre><blockquote>
     *          [-y(cm)]   [ 0  -100    0 ] [x(m)]
     *          [ x(cm)] = [ 100   0    0 ] [y(m)]
     *          [ 1    ]   [ 0     0    1 ] [1   ]
     * </blockquote></pre>
     *
     * @param  sourceCS The source coordinate system.
     * @param  targetCS The target coordinate system.
     * @return The conversion from {@code sourceCS} to {@code targetCS} as
     *         an affine transform. Only axis orientation and units are taken in account.
     * @throws IllegalArgumentException if axis doesn't matches, or the CS doesn't have the
     *         same geometry.
     * @throws ConversionException if the unit conversion is non-linear.
     */
    public static Matrix swapAndScaleAxis(final CoordinateSystem sourceCS,
                                          final CoordinateSystem targetCS)
            throws IllegalArgumentException, ConversionException
    {
        if (!Utilities.sameInterfaces(sourceCS.getClass(), targetCS.getClass(),
                                      CoordinateSystem.class))
        {
            throw new IllegalArgumentException(Errors.format(
                      ErrorKeys.INCOMPATIBLE_COORDINATE_SYSTEM_TYPE));
        }
        final AxisDirection[] sourceAxis = getAxisDirections(sourceCS);
        final AxisDirection[] targetAxis = getAxisDirections(targetCS);
        final GeneralMatrix matrix = new GeneralMatrix(sourceAxis, targetAxis);
        assert Arrays.equals(sourceAxis, targetAxis) == matrix.isIdentity() : matrix;
        /*
         * The previous code computed a matrix for swapping axis. Usually, this
         * matrix contains only 0 and 1 values with only one "1" value by row.
         * For example, the matrix operation for swapping x and y axis is:
         *
         *          [y]   [ 0  1  0 ] [x]
         *          [x] = [ 1  0  0 ] [y]
         *          [1]   [ 0  0  1 ] [1]
         *
         * Now, take in account units conversions. Each matrix's element (j,i)
         * is multiplied by the conversion factor from sourceCS.getUnit(i) to
         * targetCS.getUnit(j). This is an element-by-element multiplication,
         * not a matrix multiplication. The last column is processed in a special
         * way, since it contains the offset values.
         */
        final int sourceDim = matrix.getNumCol()-1;
        final int targetDim = matrix.getNumRow()-1;
        assert sourceDim == sourceCS.getDimension() : sourceCS;
        assert targetDim == targetCS.getDimension() : targetCS;
        for (int j=0; j<targetDim; j++) {
            final Unit targetUnit = targetCS.getAxis(j).getUnit();
            for (int i=0; i<sourceDim; i++) {
                final double element = matrix.getElement(j,i);
                if (element == 0) {
                    // There is no dependency between source[i] and target[j]
                    // (i.e. axis are orthogonal).
                    continue;
                }
                final Unit sourceUnit = sourceCS.getAxis(i).getUnit();
                if (Utilities.equals(sourceUnit, targetUnit)) {
                    // There is no units conversion to apply
                    // between source[i] and target[j].
                    continue;
                }
                final Converter converter = sourceUnit.getConverterTo(targetUnit);
                if (!converter.isLinear()) {
                    throw new ConversionException(Errors.format(
                              ErrorKeys.NON_LINEAR_UNIT_CONVERSION_$2, sourceUnit, targetUnit));
                }
                final double offset = converter.convert(0);
                final double scale  = converter.derivative(0);
                matrix.setElement(j,i, element*scale);
                matrix.setElement(j,sourceDim, matrix.getElement(j,sourceDim) + element*offset);
            }
        }
        return matrix;
    }

    /**
     * Returns a coordinate system with standard axis order and units. This method returns one
     * of the predefined constants with axis in (<var>longitude</var>,<var>latitude</var>) or
     * (<var>X</var>,<var>Y</var>) order, and units in degree or metres. This method is typically
     * used together with {@link #swapAndScaleAxis swapAndScaleAxis} for the creation of a
     * transformation step before some {@linkplain org.opengis.referencing.operation.MathTransform
     * math transform}. Example:
     *
     * <blockquote><pre>
     * Matrix step1 = swapAndScaleAxis(sourceCS, standard(sourceCS));
     * Matrix step2 = ... some transform operating on standard axis ...
     * Matrix step3 = swapAndScaleAxis(standard(targetCS), targetCS);
     * </pre></blockquote>
     * <p>
     * A rational for standard axis order and units is explained in the <cite>Axis units and
     * orientation</cite> section in the {@linkplain org.geotools.referencing.operation.projection
     * description of map projection package}.
     *
     * @param  cs The coordinate system.
     * @return A constant similar to the specified {@code cs} with standard axis.
     * @throws IllegalArgumentException if the specified {@code cs} is unknow to this method.
     *
     * @since 2.2
     */
    public static CoordinateSystem standard(final CoordinateSystem cs)
            throws IllegalArgumentException
    {
        final int dimension = cs.getDimension();
        if (cs instanceof CartesianCS) {
            switch (dimension) {
                case 2: return DefaultCartesianCS.GENERIC_2D;
                case 3: return DefaultCartesianCS.GENERIC_3D;
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
        throw new IllegalArgumentException(
                Errors.format(ErrorKeys.UNSUPPORTED_COORDINATE_SYSTEM_$1, cs.getName().getCode()));
    }

    /**
     * Suggests an unit for measuring distances in this coordinate system. The default
     * implementation scans all {@linkplain CoordinateSystemAxis#getUnit axis units},
     * ignoring angular ones (this also implies ignoring {@linkplain Unit#ONE dimensionless} ones).
     * If more than one non-angular unit is found, the default implementation returns the "largest"
     * one (e.g. kilometers instead of meters).
     *
     * @return Suggested distance unit.
     * @throws ConversionException if some non-angular units are incompatibles.
     */
    final Unit getDistanceUnit() throws ConversionException {
        Unit unit = distanceUnit;  // Avoid the need for synchronization.
        if (unit == null) {
            for (int i=0; i<axis.length; i++) {
                final Unit candidate = axis[i].getUnit();
                if (candidate!=null && !candidate.isCompatible(SI.RADIAN)) {
                    // TODO: checks the unit scale type (keeps RATIO only).
                    if (unit != null) {
                        final Converter converter = candidate.getConverterTo(unit);
                        if (!converter.isLinear()) {
                            // TODO: use the localization provided in 'swapAxis'. We could also
                            //       do a more intelligent work by checking the unit scale type.
                            throw new ConversionException("Unit conversion is non-linear");
                        }
                        if (Math.abs(converter.derivative(0)) <= 1) {
                            // The candidate is a "smaller" unit than the current one
                            // (e.g. "m" instead of "km"). Keeps the "largest" unit.
                            continue;
                        }
                    }
                    unit = candidate;
                }
            }
            distanceUnit = unit;
        }
        return unit;
    }
    
    /**
     * Convenience method for checking object dimension validity.
     *
     * @param  name The name of the argument to check.
     * @param  coordinates The coordinate array to check.
     * @throws MismatchedDimensionException if the coordinate doesn't have the expected dimension.
     */
    final void ensureDimensionMatch(final String name, final double[] coordinates)
            throws MismatchedDimensionException
    {
        if (coordinates.length != axis.length) {
            throw new MismatchedDimensionException(Errors.format(
                        ErrorKeys.MISMATCHED_DIMENSION_$3, name,
                        new Integer(coordinates.length), new Integer(axis.length)));
        }
    }

    /**
     * Computes the distance between two points. This method is not available for all coordinate
     * systems. For example, {@linkplain DefaultEllipsoidalCS ellipsoidal CS} doesn't have
     * suffisient information.
     *
     * @param  coord1 Coordinates of the first point.
     * @param  coord2 Coordinates of the second point.
     * @return The distance between {@code coord1} and {@code coord2}.
     * @throws UnsupportedOperationException if this coordinate system can't compute distances.
     * @throws MismatchedDimensionException if a coordinate doesn't have the expected dimension.
     *
     * @todo Provides a localized message in the exception.
     */
    public Measure distance(final double[] coord1, final double[] coord2)
            throws UnsupportedOperationException, MismatchedDimensionException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns all axis in the specified unit. This method is used for implementation of
     * {@code usingUnit} methods in subclasses.
     *
     * @param  unit The unit for the new axis.
     * @return New axis using the specified unit, or {@code null} if current axis fits.
     * @throws IllegalArgumentException If the specified unit is incompatible with the expected one.
     *
     * @since 2.2
     */
    final CoordinateSystemAxis[] axisUsingUnit(final Unit unit) throws IllegalArgumentException {
        final CoordinateSystemAxis[] newAxis = new CoordinateSystemAxis[axis.length];
        boolean modified = false;
        for (int i=0; i<newAxis.length; i++) {
            CoordinateSystemAxis a = axis[i];
            DefaultCoordinateSystemAxis da;
            if (a instanceof DefaultCoordinateSystemAxis) {
                da = (DefaultCoordinateSystemAxis) a;
            } else {
                a = da = new DefaultCoordinateSystemAxis(a);
            }
            da = da.usingUnit(unit);
            if (a != da) {
                modified = true;
            }
            newAxis[i] = da;
        }
        return modified ? newAxis : null;
    }

    /**
     * Compares the specified object with this coordinate system for equality.
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
            final AbstractCS that = (AbstractCS) object;
            return equals(this.axis, that.axis, compareMetadata);
        }
        return false;
    }
    
    /**
     * Returns a hash value for this coordinate system.
     *
     * @return The hash code value. This value doesn't need to be the same
     *         in past or future versions of this class.
     */
    public int hashCode() {
        int code = (int)serialVersionUID;
        for (int i=0; i<axis.length; i++) {
            code = code*37 + axis[i].hashCode();
        }
        return code;
    }
    
    /**
     * Format the inner part of a
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite> (WKT)</A> element. Note that WKT is not yet defined for coordinate system.
     * Current implementation list the axis contained in this CS.
     *
     * @param  formatter The formatter to use.
     * @return The WKT element name. Current implementation default to the class name.
     */
    protected String formatWKT(final Formatter formatter) {
        for (int i=0; i<axis.length; i++) {
            formatter.append(axis[i]);
        }
        formatter.setInvalidWKT();
        return super.formatWKT(formatter);
    }
}
