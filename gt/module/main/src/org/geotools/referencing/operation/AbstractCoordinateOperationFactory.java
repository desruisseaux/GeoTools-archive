/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing.operation;

// J2SE dependencies and extensions
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.units.ConversionException;

// OpenGIS dependencies
import org.opengis.metadata.Identifier;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.Operation;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.referencing.operation.Transformation;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.metadata.citation.Citation;
import org.geotools.referencing.Factory;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.cts.Resources;
import org.geotools.util.Singleton;
import org.geotools.util.WeakHashSet;


/**
 * Base class for coordinate operation factories. This class provides helper methods for the
 * construction of building blocks. It doesn't figure out any operation path by itself. This
 * more "intelligent" job is left to subclasses.
 *
 * @version $Id$
 * @author <A HREF="http://www.opengis.org">OpenGIS</A>
 * @author Martin Desruisseaux
 */
public abstract class AbstractCoordinateOperationFactory extends Factory
        implements org.opengis.referencing.operation.CoordinateOperationFactory
{
    /**
     * The identifier for temporary objects created.
     *
     * @todo localize
     * @todo Provides a more elaborated name, e.g. "NAD27 (temporary-1)".
     */
    private static final Identifier TEMPORARY_NAME =
            new org.geotools.referencing.Identifier(Citation.GEOTOOLS, "Temporary");

    /**
     * The identifier for an identity operation.
     *
     * @todo localize
     */
    protected static final Identifier IDENTITY =
            new org.geotools.referencing.Identifier(Citation.GEOTOOLS, "Identity");

    /**
     * The identifier for conversion using an affine transform for axis swapping and/or
     * unit conversions.
     *
     * @todo localize
     */
    protected static final Identifier AXIS_CHANGES =
            new org.geotools.referencing.Identifier(Citation.GEOTOOLS, "Axis changes");

    /**
     * The identifier for a transformation which is a datum shift.
     *
     * @todo localize
     */
    protected static final Identifier DATUM_SHIFT =
            new org.geotools.referencing.Identifier(Citation.GEOTOOLS, "Datum shift");

    /**
     * The identifier for a geocentric conversion.
     */
    protected static final Identifier GEOCENTRIC_CONVERSION =
            new org.geotools.referencing.Identifier(Citation.GEOTOOLS,
                Resources.formatInternational(ResourceKeys.GEOCENTRIC_TRANSFORM));

    /**
     * The identifier for an inverse operation.
     *
     * @todo localize
     */
    protected static final Identifier INVERSE_OPERATION =
            new org.geotools.referencing.Identifier(Citation.GEOTOOLS,
                "Inverse operation");

    /**
     * Shortcut to identified object constants.
     *
     * @todo Replace by a static import when we will be allowed to compile with J2SE 1.5.
     */
    private static final String NAME_PROPERTY =
            org.geotools.referencing.IdentifiedObject.NAME_PROPERTY;

    /**
     * The underlying math transform factory. This factory
     * is used for constructing {@link MathTransform} objects for
     * all {@linkplain CoordinateOperation coordinate operations}.
     */
    protected final MathTransformFactory mtFactory;

    /**
     * A pool of coordinate operation. This pool is used in order
     * to returns instance of existing operations when possible.
     */
    private final WeakHashSet pool = new WeakHashSet();

    /**
     * Constructs a coordinate operation factory.
     *
     * @param mtFactory The math transform factory to use.
     */
    public AbstractCoordinateOperationFactory(final MathTransformFactory mtFactory) {
        this.mtFactory = mtFactory;
        ensureNonNull("mtFactory", mtFactory);
    }

    /**
     * Returns the underlying math transform factory. This factory
     * is used for constructing {@link MathTransform} objects for
     * all {@linkplain CoordinateOperation coordinate operations}.
     */
    public final MathTransformFactory getMathTransformFactory() {
        return mtFactory;
    }

    /**
     * Returns an affine transform between two coordinate systems. Only units and
     * axis order (e.g. transforming from (NORTH,WEST) to (EAST,NORTH)) are taken
     * in account.
     * <br><br>
     * Example: If coordinates in <code>sourceCS</code> are (x,y) pairs in metres and
     * coordinates in <code>targetCS</code> are (-y,x) pairs in centimetres, then the
     * transformation can be performed as below:
     *
     * <pre><blockquote>
     *          [-y(cm)]   [ 0  -100    0 ] [x(m)]
     *          [ x(cm)] = [ 100   0    0 ] [y(m)]
     *          [ 1    ]   [ 0     0    1 ] [1   ]
     * </blockquote></pre>
     *
     * @param  sourceCS The source coordinate system.
     * @param  targetCS The target coordinate system.
     * @return The transformation from <code>sourceCS</code> to <code>targetCS</code> as
     *         an affine transform. Only axis orientation and units are taken in account.
     * @throws OperationNotFoundException If the affine transform can't be constructed.
     */
    protected Matrix swapAndScaleAxis(final CoordinateSystem sourceCS,
                                      final CoordinateSystem targetCS)
            throws OperationNotFoundException
    {
        try {
            return org.geotools.referencing.cs.CoordinateSystem.swapAndScaleAxis(sourceCS,targetCS);
        } catch (IllegalArgumentException exception) {
            throw new OperationNotFoundException(getErrorMessage(sourceCS, targetCS), exception);
        } catch (ConversionException exception) {
            throw new OperationNotFoundException(getErrorMessage(sourceCS, targetCS), exception);
        }
        // No attempt to catch ClassCastException: it would be a programming error.
    }

    /**
     * Creates a coordinate operation from a matrix, which usually describes an affine tranform.
     * A default {@link OperationMethod} object is given to this transform. In the special case
     * where the <code>name</code> identifier is {@link #DATUM_SHIFT}, the operation will be an
     * instance of {@link Transformation} instead of the usual {@link Conversion}.
     *
     * @param  name      The identifier for the operation to be created.
     * @param  sourceCRS The source coordinate reference system.
     * @param  targetCRS The target coordinate reference system.
     * @param  matrix    The matrix which describe an affine transform operation.
     * @return The conversion or transformation.
     * @throws FactoryException if the operation can't be created.
     *
     * @todo In the datum shift case, an operation version is mandatory but unknow at this time.
     */
    protected CoordinateOperation createFromAffineTransform(
                                  final Identifier                name,
                                  final CoordinateReferenceSystem sourceCRS,
                                  final CoordinateReferenceSystem targetCRS,
                                  final Matrix                    matrix)
            throws FactoryException
    {
        final MathTransform transform = mtFactory.createAffineTransform(matrix);
        final Map properties;
        final Class type;
        if (name == DATUM_SHIFT) {
            properties = new HashMap(4);
            properties.put(NAME_PROPERTY, name);
            properties.put(
                  org.geotools.referencing.operation.CoordinateOperation.OPERATION_VERSION_PROPERTY,
                  "(unknow)");
            type = Transformation.class;
        } else {
            properties = Collections.singletonMap(NAME_PROPERTY, name);
            type = Conversion.class;
        }
        return createFromMathTransform(properties, sourceCRS, targetCRS, transform,
                ProjectiveTransform.Provider.getMethod(transform.getSourceDimensions(),
                                                       transform.getTargetDimensions()), type);
    }

    /**
     * Creates a coordinate operation from a set of parameters.
     * The {@linkplain OperationMethod operation method} is inferred automatically,
     * if possible.
     *
     * @param  name       The identifier for the operation to be created.
     * @param  sourceCRS  The source coordinate reference system.
     * @param  targetCRS  The target coordinate reference system.
     * @param  parameters The parameters.
     * @return The conversion or transformation.
     * @throws FactoryException if the operation can't be created.
     */
    protected CoordinateOperation createFromParameters(
                                  final Identifier                name,
                                  final CoordinateReferenceSystem sourceCRS,
                                  final CoordinateReferenceSystem targetCRS,
                                  final ParameterValueGroup       parameters)
            throws FactoryException
    {
        final OperationMethod method;
        final MathTransform transform;
        if (mtFactory instanceof org.geotools.referencing.operation.MathTransformFactory) {
            // Special processing for Geotools implementation.
            final Singleton methods = new Singleton();
            transform = ((org.geotools.referencing.operation.MathTransformFactory) mtFactory)
                        .createParameterizedTransform(parameters, methods);
            method = (OperationMethod) methods.get();
        } else {
            // Not a geotools implementation. Try to guess the method.
            // TODO: remove the cast when we will be allowed to compile against J2SE 1.5.
            transform = mtFactory.createParameterizedTransform(parameters);
            method    = org.geotools.referencing.operation.MathTransformFactory.getMethod(
                        mtFactory.getAvailableMethods(CoordinateOperation.class),
                        (ParameterDescriptorGroup) parameters.getDescriptor());
        }
        return createFromMathTransform(Collections.singletonMap(NAME_PROPERTY, name),
                                       sourceCRS, targetCRS, transform, method,
                                       Operation.class);
    }

    /**
     * Creates a coordinate operation from a math transform.
     *
     * @param  name       The identifier for the operation to be created.
     * @param  sourceCRS  The source coordinate reference system.
     * @param  targetCRS  The destination coordinate reference system.
     * @param  transform  The math transform.
     * @return A coordinate operation using the specified math transform.
     * @throws FactoryException if the operation can't be constructed.
     */
    protected CoordinateOperation createFromMathTransform(
                                  final Identifier                name,
                                  final CoordinateReferenceSystem sourceCRS,
                                  final CoordinateReferenceSystem targetCRS,
                                  final MathTransform             transform)
            throws FactoryException
    {
        return createFromMathTransform(Collections.singletonMap(NAME_PROPERTY, name),
                                       sourceCRS, targetCRS, transform, null,
                                       CoordinateOperation.class);
    }

    /**
     * Creates a coordinate operation from a math transform.
     * If the specified math transform is already a coordinate operation, and if source
     * and target CRS match, then <code>transform</code> is returned with no change.
     * Otherwise, a new coordinate operation is created.
     *
     * @param  properties The properties to give to the operation.
     * @param  sourceCRS  The source coordinate reference system.
     * @param  targetCRS  The destination coordinate reference system.
     * @param  transform  The math transform.
     * @param  method     The operation method, or <code>null</code>.
     * @param  type       The required super-class (e.g. <code>{@link Transformation}.class</code>).
     * @return A coordinate operation using the specified math transform.
     * @throws FactoryException if the operation can't be constructed.
     */
    protected CoordinateOperation createFromMathTransform(
                                  final Map                       properties,
                                  final CoordinateReferenceSystem sourceCRS,
                                  final CoordinateReferenceSystem targetCRS,
                                  final MathTransform             transform,
                                  final OperationMethod           method,
                                  final Class                     type)
            throws FactoryException
    {
        CoordinateOperation operation;
        if (transform instanceof CoordinateOperation) {
            operation = (CoordinateOperation) transform;
            if (Utilities.equals(operation.getSourceCRS(),     sourceCRS) &&
                Utilities.equals(operation.getTargetCRS(),     targetCRS) &&
                Utilities.equals(operation.getMathTransform(), transform))
            {
                if (operation instanceof Operation) {
                    if (Utilities.equals(((Operation) operation).getMethod(), method)) {
                        return operation;
                    }
                } else {
                    return operation;
                }
            }
        }
        operation = org.geotools.referencing.operation.SingleOperation.create(properties,
                    sourceCRS, targetCRS, transform, method, type);
        operation = (CoordinateOperation) pool.canonicalize(operation);
        return operation;
    }

    /**
     * Creates a concatenated operation from a sequence of operations.
     *
     * @param  properties Set of properties. Should contains at least <code>"name"</code>.
     * @param  operations The sequence of operations.
     * @return The concatenated operation.
     * @throws FactoryException if the object creation failed.
     */
    public CoordinateOperation createConcatenatedOperation(Map properties,
                                                           CoordinateOperation[] operations)
            throws FactoryException
    {
        CoordinateOperation operation;
        operation = new org.geotools.referencing.operation.ConcatenatedOperation(
                        properties, operations, mtFactory);
        operation = (CoordinateOperation) pool.canonicalize(operation);
        return operation;
    }

    /**
     * Concatenate two operation steps.
     *
     * @param  step1 The first  step, or <code>null</code> for the identity operation.
     * @param  step2 The second step, or <code>null</code> for the identity operation.
     * @return A concatenated operation, or <code>null</code> if all arguments was nul.
     * @throws FactoryException if the operation can't be constructed.
     */
    final CoordinateOperation concatenate(final CoordinateOperation step1,
                                          final CoordinateOperation step2)
            throws FactoryException
    {
        if (step1==null) return step2;
        if (step2==null) return step1;
        if (false) {
            // Note: we sometime get this assertion failure if the user provided CRS with two
            //       different ellipsoids but an identical TOWGS84 conversion infos (which is
            //       wrong).
            assert equalsIgnoreMetadata(step1.getTargetCRS(), step2.getSourceCRS()) :
                   "CRS 1 =" + step1.getTargetCRS() + '\n' +
                   "CRS 2 =" + step2.getSourceCRS();
        }
        final MathTransform mt1 = step1.getMathTransform(); if (mt1.isIdentity()) return step2;
        final MathTransform mt2 = step2.getMathTransform(); if (mt2.isIdentity()) return step1;
        final CoordinateReferenceSystem sourceCRS = step1.getSourceCRS();
        final CoordinateReferenceSystem targetCRS = step2.getTargetCRS();
        return createConcatenatedOperation(getTemporaryName(sourceCRS, targetCRS),
                                           new CoordinateOperation[] {step1, step2});
    }

    /**
     * Concatenate three transformation steps.
     *
     * @param  step1 The first  step, or <code>null</code> for the identity operation.
     * @param  step2 The second step, or <code>null</code> for the identity operation.
     * @param  step3 The third  step, or <code>null</code> for the identity operation.
     * @return A concatenated operation, or <code>null</code> if all arguments were null.
     * @throws FactoryException if the operation can't be constructed.
     */
    final CoordinateOperation concatenate(final CoordinateOperation step1,
                                          final CoordinateOperation step2,
                                          final CoordinateOperation step3)
            throws FactoryException
    {
        if (step1==null) return concatenate(step2, step3);
        if (step2==null) return concatenate(step1, step3);
        if (step3==null) return concatenate(step1, step2);
        assert equalsIgnoreMetadata(step1.getTargetCRS(), step2.getSourceCRS()) : step1;
        assert equalsIgnoreMetadata(step2.getTargetCRS(), step3.getSourceCRS()) : step3;

        final MathTransform mt1 = step1.getMathTransform();
        if (mt1.isIdentity()) return concatenate(step2, step3);
        final MathTransform mt2 = step2.getMathTransform();
        if (mt2.isIdentity()) return concatenate(step1, step3);
        final MathTransform mt3 = step3.getMathTransform();
        if (mt3.isIdentity()) return concatenate(step1, step2);
        final CoordinateReferenceSystem sourceCRS = step1.getSourceCRS();
        final CoordinateReferenceSystem targetCRS = step3.getTargetCRS();
        return createConcatenatedOperation(getTemporaryName(sourceCRS, targetCRS),
                                           new CoordinateOperation[] {step1, step2, step3});
    }
    



    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////
    ////////////                                                         ////////////
    ////////////                M I S C E L L A N E O U S                ////////////
    ////////////                                                         ////////////
    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the dimension of the specified coordinate system,
     * or <code>0</code> if the coordinate system is null.
     */
    static int getDimension(final CoordinateReferenceSystem crs) {
        return (crs!=null) ? crs.getCoordinateSystem().getDimension() : 0;
    }

    /**
     * Returns the name of the specified object.
     */
    static String getName(final IdentifiedObject object) {
        if (object != null) {
            final Identifier id = object.getName();
            if (id != null) {
                return Utilities.getShortClassName(object) + '(' + id.getCode() + ')';
            }
            return Utilities.getShortClassName(object);
        }
        return null;
    }
    
    /**
     * Returns a temporary name for object derived from the specified one.
     *
     * @param source The CRS to base name on, or <code>null</code> if none.
     *
     * @todo Find better names, and localize.
     */
    static Map getTemporaryName(final IdentifiedObject source) {
        final Map properties = new HashMap(4);
        properties.put(NAME_PROPERTY, TEMPORARY_NAME);
        final StringBuffer remarks = new StringBuffer("Derived from \"");
        remarks.append(getName(source));
        remarks.append('"');
        final InternationalString previous = source.getRemarks();
        if (previous != null) {
            remarks.append(System.getProperty("line.separator", "\n"));
            remarks.append(previous);
        }
        properties.put(org.geotools.referencing.IdentifiedObject.REMARKS_PROPERTY,
                       remarks.toString());
        return properties;
    }

    /**
     * Returns a temporary name for object derived from a concatenation.
     *
     * @param source The CRS to base name on, or <code>null</code> if none.
     */
    static Map getTemporaryName(final CoordinateReferenceSystem source,
                                final CoordinateReferenceSystem target)
    {
        final String name = getName(source) + " \u21E8 " + getName(target);
        return Collections.singletonMap(NAME_PROPERTY, name);
    }

    /**
     * Compare the specified objects for equality. If both objects are Geotools
     * implementations of {@linkplain org.geotools.referencing.IdentifiedObject},
     * then this method will ignore the metadata during the comparaison.
     *
     * @param  object1 The first object to compare (may be null).
     * @param  object2 The second object to compare (may be null).
     * @return <code>true</code> if both objects are equals.
     *
     * @todo This method may be insuffisient, since it will returns <code>false</code> for
     *       two different implementations, even if they encapsulate the same data values.
     */
    static boolean equalsIgnoreMetadata(final IdentifiedObject object1,
                                        final IdentifiedObject object2)
    {
        return CRSUtilities.equalsIgnoreMetadata(object1, object2);
    }

    /**
     * Makes sure an argument is non-null.
     *
     * @param  name   Argument name.
     * @param  object User argument.
     * @throws IllegalArgumentException if <code>object</code> is null.
     */
    protected static void ensureNonNull(final String name, final Object object)
            throws IllegalArgumentException
    {
        if (object == null) {
            throw new IllegalArgumentException(Resources.format(
                        ResourceKeys.ERROR_NULL_ARGUMENT_$1, name));
        }
    }

    /**
     * Returns an error message for "No path found from sourceCRS to targetCRS".
     * This is used for the construction of {@link OperationNotFoundException}.
     *
     * @param  source The source CRS.
     * @param  target The target CRS.
     * @return A default error message.
     */
    protected static String getErrorMessage(final IdentifiedObject source,
                                            final IdentifiedObject target)
    {
        return Resources.format(ResourceKeys.ERROR_NO_TRANSFORMATION_PATH_$2,
                                getName(source), getName(target));
    }
}
