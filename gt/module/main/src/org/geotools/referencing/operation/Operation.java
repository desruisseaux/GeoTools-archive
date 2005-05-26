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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing.operation;

// J2SE dependencies
import java.util.Map;

// OpenGIS dependencies
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.Transformation;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.Projection;
import org.opengis.referencing.operation.PlanarProjection;
import org.opengis.referencing.operation.CylindricalProjection;
import org.opengis.referencing.operation.ConicProjection;

// Geotools dependencies
import org.geotools.referencing.DefaultIdentifiedObject;
import org.geotools.referencing.operation.transform.AbstractMathTransform;
import org.geotools.referencing.operation.transform.ConcatenatedTransform;
import org.geotools.referencing.wkt.Formatter;
import org.geotools.util.UnsupportedImplementationException;


/**
 * A parameterized mathematical operation on coordinates that transforms or converts
 * coordinates to another coordinate reference system. This coordinate operation thus
 * uses an operation method, usually with associated parameter values.
 *
 * <P>In the Geotools implementation, the {@linkplain #getParameterValues parameter values}
 * are inferred from the {@linkplain #transform transform}. Other implementations may have
 * to overrides the {@link #getParameterValues} method.</P>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see OperationMethod
 *
 * @todo OpenGIS is considering to remove this class from UML diagrams. In this case, all
 *       methods in this interface will move to the {@link SingleOperation} interface. The
 *       removal of {@code Operation} interface would simplify the API and brings more
 *       flexibility: {@link ConcatenatedOperation} would be a legal component in both
 *       other {@link ConcatenatedOperation} and in {@link PassThroughOperation}.
 */
public class Operation extends SingleOperation
                    implements org.opengis.referencing.operation.Operation
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -8923365753849532179L;

    /**
     * The operation method.
     */
    protected final OperationMethod method;

    /**
     * Constructs a new operation with the same values than the specified defining
     * conversion, together with the specified source and target CRS. This constructor
     * is used by {@link ConversionImpl} only.
     */
    Operation(final Conversion                definition,
              final CoordinateReferenceSystem sourceCRS,
              final CoordinateReferenceSystem targetCRS,
              final MathTransform             transform)
    {
        super(definition, sourceCRS, targetCRS, transform);
        method = definition.getMethod();
    }

    /**
     * Constructs an operation from a set of properties. The properties given in argument
     * follow the same rules than for the {@link org.geotools.referencing.operation.CoordinateOperation} constructor.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param sourceCRS The source CRS.
     * @param targetCRS The target CRS.
     * @param transform Transform from positions in the {@linkplain #getSourceCRS source CRS}
     *                  to positions in the {@linkplain #getTargetCRS target CRS}.
     * @param method    The operation method.
     */
    public Operation(final Map                      properties,
                     final CoordinateReferenceSystem sourceCRS,
                     final CoordinateReferenceSystem targetCRS,
                     final MathTransform             transform,
                     final OperationMethod           method)
    {
        super(properties, sourceCRS, targetCRS, transform);
        ensureNonNull("method", method);
        org.geotools.referencing.operation.OperationMethod.checkDimensions(method, transform);
        this.method = method;
    }

    /**
     * Returns a coordinate operation of the specified class. This method may constructs instance of
     * {@link Conversion} or {@link Transformation} among others.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param sourceCRS The source CRS.
     * @param targetCRS The target CRS.
     * @param transform Transform from positions in the {@linkplain #getSourceCRS source CRS}
     *                  to positions in the {@linkplain #getTargetCRS target CRS}.
     * @param method    The operation method, or {@code null}.
     * @param type      The minimal type as <code>{@linkplain Conversion}.class</code>,
     *                  <code>{@linkplain Projection}.class</code>, etc. This method may
     *                  create an instance of a subclass of <code>type</code>.
     *
     * @see org.geotools.referencing.operation.Conversion#create
     */
    public static CoordinateOperation create(final Map                      properties,
                                             final CoordinateReferenceSystem sourceCRS,
                                             final CoordinateReferenceSystem targetCRS,
                                             final MathTransform             transform,
                                             final OperationMethod           method,
                                                   Class                     type)
    {
        if (method != null) {
            if (method instanceof MathTransformProvider) {
                final Class candidate = ((MathTransformProvider) method).getOperationType();
                if (candidate != null) {
                    if (type==null || type.isAssignableFrom(candidate)) {
                        type = candidate;
                    }
                }
            }
            if (type != null) {
                if (Transformation.class.isAssignableFrom(type)) {
                    return new org.geotools.referencing.operation.Transformation(
                               properties, sourceCRS, targetCRS, transform, method);
                }
                if (ConicProjection.class.isAssignableFrom(type)) {
                    return new org.geotools.referencing.operation.ConicProjection(
                               properties, sourceCRS, targetCRS, transform, method);
                }
                if (CylindricalProjection.class.isAssignableFrom(type)) {
                    return new org.geotools.referencing.operation.CylindricalProjection(
                               properties, sourceCRS, targetCRS, transform, method);
                }
                if (PlanarProjection.class.isAssignableFrom(type)) {
                    return new org.geotools.referencing.operation.PlanarProjection(
                               properties, sourceCRS, targetCRS, transform, method);
                }
                if (Projection.class.isAssignableFrom(type)) {
                    return new org.geotools.referencing.operation.Projection(
                               properties, sourceCRS, targetCRS, transform, method);
                }
                if (Conversion.class.isAssignableFrom(type)) {
                    return new org.geotools.referencing.operation.Conversion(
                               properties, sourceCRS, targetCRS, transform, method);
                }
            }
            return new org.geotools.referencing.operation.Operation(
                       properties, sourceCRS, targetCRS, transform, method);
        }
        return new org.geotools.referencing.operation.SingleOperation(
                   properties, sourceCRS, targetCRS, transform);
    }

    /**
     * Returns the operation method.
     */
    public OperationMethod getMethod() {
        return method;
    }

    /**
     * Returns the parameter values. The default implementation infer the parameter
     * values from the {@link #transform transform}, if possible.
     *
     * @throws UnsupportedOperationException if the parameters values can't be determined
     *         for current math transform implementation.
     *
     * @see MathTransformFactory#createParameterizedTransform
     * @see org.geotools.referencing.operation.transform.AbstractMathTransform#getParameterValues
     */
    public ParameterValueGroup getParameterValues() throws UnsupportedOperationException {
        return getParameterValues(transform, method.getParameters(), true);
    }

    /**
     * Returns the parameter values for the math transform that use the specified descriptor.
     *
     * @param  mt The math transform for which parameters are desired.
     * @param  descriptor The descriptor to search for.
     * @param  required <code>true</code> if an exception must be thrown if parameters are unknow.
     * @return The parameter values, or null.
     * @throws UnsupportedImplementationException if the math transform implementation do not
     *         provide information about parameters.
     */
    private static ParameterValueGroup getParameterValues(final MathTransform mt,
                                                          final ParameterDescriptorGroup descriptor,
                                                          boolean required)
    {
        if (mt instanceof ConcatenatedTransform) {
            final ConcatenatedTransform ct = (ConcatenatedTransform) mt;
            final ParameterValueGroup param1 = getParameterValues(ct.transform1, descriptor, false);
            final ParameterValueGroup param2 = getParameterValues(ct.transform2, descriptor, false);
            if (param1==null && param2!=null) return param2;
            if (param2==null && param1!=null) return param1;
            required = true;
        }
        if (mt instanceof AbstractMathTransform) {
            final ParameterValueGroup param = ((AbstractMathTransform) mt).getParameterValues();
            if (param != null) {
                return param;
            }
        }
        if (required) {
            throw new UnsupportedImplementationException(mt.getClass());
        }
        return null;
    }

    /**
     * Compare this operation method with the specified object for equality.
     * If <code>compareMetadata</code> is <code>true</code>, then all available
     * properties are compared including
     * {@linkplain org.geotools.referencing.operation.OperationMethod#getFormula formula}.
     *
     * @param  object The object to compare to <code>this</code>.
     * @param  compareMetadata <code>true</code> for performing a strict comparaison, or
     *         <code>false</code> for comparing only properties relevant to transformations.
     * @return <code>true</code> if both objects are equal.
     */
    public boolean equals(final DefaultIdentifiedObject object, final boolean compareMetadata) {
        if (super.equals(object, compareMetadata)) {
            final Operation that = (Operation) object;
            return equals(this.method, that.method, compareMetadata);
        }
        return false;
    }

    /**
     * Returns a hash code value for this operation method.
     */
    public int hashCode() {
        return super.hashCode() ^ method.hashCode();
    }
    
    /**
     * Format the inner part of a
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite> (WKT)</A> element.
     *
     * @param  formatter The formatter to use.
     * @return The WKT element name.
     */
    protected String formatWKT(final Formatter formatter) {
        final String name = super.formatWKT(formatter);
        formatter.append(method);
        return name;
    }
}
