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
import org.opengis.metadata.quality.PositionalAccuracy;
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

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.metadata.iso.citation.CitationImpl;
import org.geotools.metadata.iso.quality.PositionalAccuracyImpl;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.factory.AbstractFactory;
import org.geotools.referencing.factory.FactoryGroup;
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
public abstract class AbstractCoordinateOperationFactory extends AbstractFactory
        implements org.opengis.referencing.operation.CoordinateOperationFactory
{
    /**
     * The identifier for an identity operation.
     *
     * @todo localize
     */
    protected static final Identifier IDENTITY =
            new org.geotools.referencing.Identifier(CitationImpl.GEOTOOLS, "Identity");

    /**
     * The identifier for conversion using an affine transform for axis swapping and/or
     * unit conversions.
     *
     * @todo localize
     */
    protected static final Identifier AXIS_CHANGES =
            new org.geotools.referencing.Identifier(CitationImpl.GEOTOOLS, "Axis changes");

    /**
     * The identifier for a transformation which is a datum shift.
     *
     * @see PositionalAccuracyImpl#DATUM_SHIFT_APPLIED
     *
     * @todo localize
     */
    protected static final Identifier DATUM_SHIFT =
            new org.geotools.referencing.Identifier(CitationImpl.GEOTOOLS, "Datum shift");

    /**
     * The identifier for a transformation which is a datum shift without
     * {@linkplain org.geotools.referencing.datum.BursaWolfParameters Bursa Wolf parameters}.
     * Only the changes in ellipsoid axis-length are taken in account. Such ellipsoid shifts
     * are approximative and may have 1 kilometer error. This transformation is allowed
     * only if the factory was created with {@link Hints#LENIENT_DATUM_SHIFT} set to
     * {@link Boolean#TRUE}.
     *
     * @see PositionalAccuracyImpl#DATUM_SHIFT_OMITTED
     *
     * @todo localize
     */
    protected static final Identifier ELLIPSOID_SHIFT =
            new org.geotools.referencing.Identifier(CitationImpl.GEOTOOLS, "Ellipsoid shift");

    /**
     * The identifier for a geocentric conversion.
     */
    protected static final Identifier GEOCENTRIC_CONVERSION =
            new org.geotools.referencing.Identifier(CitationImpl.GEOTOOLS,
                Resources.formatInternational(ResourceKeys.GEOCENTRIC_TRANSFORM));

    /**
     * The identifier for an inverse operation.
     *
     * @todo localize
     */
    protected static final Identifier INVERSE_OPERATION =
            new org.geotools.referencing.Identifier(CitationImpl.GEOTOOLS,
                "Inverse operation");

    /**
     * Shortcut to identified object constants.
     *
     * @todo Replace by a static import when we will be allowed to compile with J2SE 1.5.
     */
    private static final String NAME_PROPERTY =
            org.geotools.referencing.IdentifiedObject.NAME_PROPERTY;

    /**
     * The set of helper methods on factories.
     */
    final FactoryGroup factories;

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
     * Constructs a coordinate operation factory using the specified hints.
     *
     * @param hints The hints, or {@code null} if none.
     *
     * @todo Need a FactoryGroup hint.
     */
    public AbstractCoordinateOperationFactory(final Hints hints) {
        /*
         * Examines the hints.
         */
        MathTransformFactory mtFactory = null;
        if (hints != null) {
            mtFactory = (MathTransformFactory) hints.get(Hints.MATH_TRANSFORM_FACTORY);
        }
        if (mtFactory == null) {
            mtFactory = FactoryFinder.getMathTransformFactory(hints);
        }
        /*
         * Stores the hints in inner fields.
         */
        this.mtFactory = mtFactory;
        /*
         * Declares the hints that we use.
         */
        super.hints.put(Hints.MATH_TRANSFORM_FACTORY, mtFactory);
        factories = new FactoryGroup(null, null, null, mtFactory);
    }

    /**
     * Constructs a coordinate operation factory from a group of factories.
     *
     * @param factories The factories to use.
     *
     * @deprecated Use {@link #AbstractCoordinateOperationFactory(Hints)} instead.
     */
    public AbstractCoordinateOperationFactory(final FactoryGroup factories) {
        ensureNonNull("factories", factories);
        this.factories = factories;
        mtFactory = factories.getMathTransformFactory();
    }

    /**
     * Constructs a coordinate operation factory from a math transform.
     *
     * @param mtFactory The math transform factory to use.
     *
     * @deprecated Use {@link #AbstractCoordinateOperationFactory(Hints)} instead.
     */
    public AbstractCoordinateOperationFactory(final MathTransformFactory mtFactory) {
        this.mtFactory = mtFactory;
        ensureNonNull("mtFactory", mtFactory);
        factories = new FactoryGroup(null, null, null, mtFactory);
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
     *
     * @see org.geotools.referencing.cs.CoordinateSystem#swapAndScaleAxis
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
        // No attempt to catch ClassCastException since such
        // exception would indicates a programming error.
    }

    /**
     * Returns the specified identifier in a map to be given to coordinate operation constructors.
     * In the special case where the <code>name</code> identifier is {@link #DATUM_SHIFT} or
     * {@link #ELLIPSOID_SHIFT}, the map will contains extra informations like positional
     * accuracy.
     *
     * @todo In the datum shift case, an operation version is mandatory but unknow at this time.
     */
    private static Map getProperties(final Identifier name) {
        final Map properties;
        if (name==DATUM_SHIFT || name==ELLIPSOID_SHIFT) {
            properties = new HashMap(4);
            properties.put(NAME_PROPERTY, name);
            properties.put(
                  org.geotools.referencing.operation.CoordinateOperation.OPERATION_VERSION_PROPERTY,
                  "(unknow)");
            properties.put(
                  org.geotools.referencing.operation.CoordinateOperation.POSITIONAL_ACCURACY_PROPERTY,
                  new org.opengis.metadata.quality.PositionalAccuracy[] {
                      name==DATUM_SHIFT ? PositionalAccuracyImpl.DATUM_SHIFT_APPLIED
                                        : PositionalAccuracyImpl.DATUM_SHIFT_OMITTED});
        } else {
            properties = Collections.singletonMap(NAME_PROPERTY, name);
        }
        return properties;
    }

    /**
     * Creates a coordinate operation from a matrix, which usually describes an affine tranform.
     * A default {@link OperationMethod} object is given to this transform. In the special case
     * where the <code>name</code> identifier is {@link #DATUM_SHIFT} or {@link #ELLIPSOID_SHIFT},
     * the operation will be an instance of {@link Transformation} instead of the usual
     * {@link Conversion}.
     *
     * @param  name      The identifier for the operation to be created.
     * @param  sourceCRS The source coordinate reference system.
     * @param  targetCRS The target coordinate reference system.
     * @param  matrix    The matrix which describe an affine transform operation.
     * @return The conversion or transformation.
     * @throws FactoryException if the operation can't be created.
     */
    protected CoordinateOperation createFromAffineTransform(
                                  final Identifier                name,
                                  final CoordinateReferenceSystem sourceCRS,
                                  final CoordinateReferenceSystem targetCRS,
                                  final Matrix                    matrix)
            throws FactoryException
    {
        final MathTransform transform = mtFactory.createAffineTransform(matrix);
        final Map properties = getProperties(name);
        final Class type = properties.containsKey(org.geotools.referencing.operation.CoordinateOperation.POSITIONAL_ACCURACY_PROPERTY)
                           ? Transformation.class : Conversion.class;
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
        final MathTransform transform;
        final Singleton method = new Singleton();
        final Map   properties = getProperties(name);
        transform = factories.createParameterizedTransform(parameters, method);
        return createFromMathTransform(properties, sourceCRS, targetCRS, transform,
                                       (OperationMethod) method.get(), Operation.class);
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
     * Concatenate two operation steps. If an operation is an {@link #AXIS_CHANGES},
     * it will be included as part of the second operation instead of creating an
     * {@link ConcatenatedOperation}. If a concatenated operation is created, it
     * will get an automatically generated name.
     *
     * @param  step1 The first  step, or <code>null</code> for the identity operation.
     * @param  step2 The second step, or <code>null</code> for the identity operation.
     * @return A concatenated operation, or <code>null</code> if all arguments was nul.
     * @throws FactoryException if the operation can't be constructed.
     */
    protected CoordinateOperation concatenate(final CoordinateOperation step1,
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
        CoordinateOperation step = null;
        if (step1.getName()==AXIS_CHANGES && mt1.getSourceDimensions()==mt1.getTargetDimensions()) step = step2;
        if (step2.getName()==AXIS_CHANGES && mt2.getSourceDimensions()==mt2.getTargetDimensions()) step = step1;
        if (step instanceof Operation) {
            /*
             * Applies only on operation in order to avoid merging with PassThroughOperation.
             * Also applies only if the transform to hide has identical source and target
             * dimensions in order to avoid mismatch with the method's dimensions.
             */
            return createFromMathTransform(getProperties(step), sourceCRS, targetCRS,
                   mtFactory.createConcatenatedTransform(mt1, mt2),
                   ((Operation) step).getMethod(), CoordinateOperation.class);
        }
        return createConcatenatedOperation(getTemporaryName(sourceCRS, targetCRS),
                                           new CoordinateOperation[] {step1, step2});
    }

    /**
     * Concatenate three transformation steps. If the first and/or the last operation is an
     * {@link #AXIS_CHANGES}, it will be included as part of the second operation instead of
     * creating an {@link ConcatenatedOperation}. If a concatenated operation is created, it
     * will get an automatically generated name.
     *
     * @param  step1 The first  step, or <code>null</code> for the identity operation.
     * @param  step2 The second step, or <code>null</code> for the identity operation.
     * @param  step3 The third  step, or <code>null</code> for the identity operation.
     * @return A concatenated operation, or <code>null</code> if all arguments were null.
     * @throws FactoryException if the operation can't be constructed.
     */
    protected CoordinateOperation concatenate(final CoordinateOperation step1,
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
        if (step1.getName() == AXIS_CHANGES) return concatenate(concatenate(step1, step2), step3);
        if (step3.getName() == AXIS_CHANGES) return concatenate(step1, concatenate(step2, step3));
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
     * Returns the properties of the given object.
     *
     * @todo Delete and replace by a static import when we will be allowed to compile against
     *       J2SE 1.5. Note: there is a bunch of constants in this class that we could
     *       simplified as well.
     */
    static Map getProperties(final IdentifiedObject object) {
        return org.geotools.referencing.IdentifiedObject.getProperties(object);
    }

    /**
     * An identifier for temporary objects. This identifier manage a count of temporary
     * identifier. The count is appended to the identifier name (e.g. "WGS84 (step 1)").
     */
    private static final class TemporaryIdentifier extends org.geotools.referencing.Identifier {
        /** The parent identifier. */
        private final Identifier parent;

        /** The temporary object count. */
        private final int count;

        /** Constructs an identifier derived from the specified one. */
        public TemporaryIdentifier(final Identifier parent) {
            this(parent, ((parent instanceof TemporaryIdentifier) ?
                         ((TemporaryIdentifier) parent).count : 0) + 1);
        }

        /** Work around for RFE #4093999 in Sun's bug database */
        private TemporaryIdentifier(final Identifier parent, final int count) {
            super(CitationImpl.GEOTOOLS, unwrap(parent).getCode() + " (step " + count + ')');
            this.parent = parent;
            this.count  = count;
        }

        /** Returns the parent identifier for the specified identifier, if any. */
        public static Identifier unwrap(Identifier identifier) {
            while (identifier instanceof TemporaryIdentifier) {
                identifier = ((TemporaryIdentifier) identifier).parent;
            }
            return identifier;
        }
    }

    /**
     * Returns the name of the specified object.
     */
    private static String getClassName(final IdentifiedObject object) {
        if (object != null) {
            String name = Utilities.getShortClassName(object);
            final Identifier id = object.getName();
            if (id != null) {
                name = name + '[' + id.getCode() + ']';
            }
            return name;
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
        properties.put(NAME_PROPERTY, new TemporaryIdentifier(source.getName()));
        properties.put(org.geotools.referencing.IdentifiedObject.REMARKS_PROPERTY,
                       "Derived from " + getClassName(source));
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
        final String name = getClassName(source) + " \u21E8 " + getClassName(target);
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
                                getClassName(source), getClassName(target));
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
}
