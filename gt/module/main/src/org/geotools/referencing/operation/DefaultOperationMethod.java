/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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

// J2SE dependencies
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// OpenGIS dependencies
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.referencing.operation.Projection;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.parameter.Parameters;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.referencing.AbstractIdentifiedObject;
import org.geotools.referencing.operation.transform.AbstractMathTransform;
import org.geotools.referencing.wkt.Formatter;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.cts.Resources;


/**
 * Definition of an algorithm used to perform a coordinate operation. Most operation
 * methods use a number of operation parameters, although some coordinate conversions
 * use none. Each coordinate operation using the method assigns values to these parameters.
 *  
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @since 2.1
 *
 * @see DefaultOperation
 */
public class DefaultOperationMethod extends AbstractIdentifiedObject implements OperationMethod {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -98032729598205972L;

    /**
     * List of localizable properties. To be given to {@link AbstractIdentifiedObject} constructor.
     */
    private static final String[] LOCALIZABLES = {FORMULA_KEY};

    /**
     * Formula(s) or procedure used by this operation method. This may be a reference to a
     * publication. Note that the operation method may not be analytic, in which case this
     * attribute references or contains the procedure, not an analytic formula.
     */
    private final InternationalString formula;

    /**
     * Number of dimensions in the source CRS of this operation method.
     */
    protected final int sourceDimensions;

    /**
     * Number of dimensions in the target CRS of this operation method.
     */
    protected final int targetDimensions;

    /**
     * The set of parameters, or {@code null} if none.
     */
    private final ParameterDescriptorGroup parameters;

    /**
     * Convenience constructor that creates an operation method from a math transform.
     * The information provided in the newly created object are approximative, and
     * usually acceptable only as a fallback when no other information are available.
     *
     * @param transform The math transform to describe.
     */
    public DefaultOperationMethod(final MathTransform transform) {
        this(getProperties(transform),
             transform.getSourceDimensions(),
             transform.getTargetDimensions(),
             getDescriptor(transform));
    }

    /**
     * Work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private static Map getProperties(final MathTransform transform) {
        ensureNonNull("transform", transform);
        final Map properties;
        if (transform instanceof AbstractMathTransform) {
            final AbstractMathTransform mt = (AbstractMathTransform) transform;
            properties = getProperties(mt.getParameterDescriptors(), null);
        } else {
            properties = Collections.singletonMap(NAME_KEY, Resources.format(ResourceKeys.UNKNOW));
        }
        return properties;
    }

    /**
     * Work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     * This code should have been merged with {@code getProperties} above.
     */
    private static ParameterDescriptorGroup getDescriptor(final MathTransform transform) {
        ParameterDescriptorGroup descriptor = null;
        if (transform instanceof AbstractMathTransform) {
            descriptor = ((AbstractMathTransform) transform).getParameterDescriptors();
        }
        return descriptor;
    }

    /**
     * Constructs an operation method from a set of properties and an array of parameter
     * descriptors. The properties given in argument follow the same rules than for the
     * {@linkplain #DefaultOperationMethod(Map, int, int, ParameterDescriptorGroup) constructor
     * expecting a parameter group}. This convenience constructor build automatically a
     * parameter group using the same properties than the one specified for this operation
     * method.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param sourceDimensions Number of dimensions in the source CRS of this operation method.
     * @param targetDimensions Number of dimensions in the target CRS of this operation method.
     * @param parameters The set of parameters, or {@code null} or an empty array if none.
     *
     * @deprecated Use the method expecting a parameter group instead.
     */
    public DefaultOperationMethod(final Map properties,
                                  final int sourceDimensions,
                                  final int targetDimensions,
                                  final GeneralParameterDescriptor[] parameters)
    {
        this(properties, sourceDimensions, targetDimensions, toGroup(properties, parameters));
    }

    /**
     * Utility method used to kludge {@code GeneralParameterDescriptor[]}
     * into a {@code ParameterDescriptorGroup}.
     * This is a work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private static ParameterDescriptorGroup toGroup(final Map properties,
                                                    final GeneralParameterDescriptor[] parameters)
    {
        return (parameters==null || parameters.length==0) ? null :
               new DefaultParameterDescriptorGroup(properties, parameters);
    }

    /**
     * Constructs an operation method from a set of properties and a descriptor group.
     * The properties given in argument follow the same rules than for the
     * {@linkplain AbstractIdentifiedObject#AbstractIdentifiedObject(Map) super-class constructor}.
     * Additionally, the following properties are understood by this construtor:
     * <br><br>
     * <table border='1'>
     *   <tr bgcolor="#CCCCFF" class="TableHeadingColor">
     *     <th nowrap>Property name</th>
     *     <th nowrap>Value type</th>
     *     <th nowrap>Value given to</th>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;{@link #FORMULA_KEY "formula"}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String} or {@link InternationalString}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getFormula}</td>
     *   </tr>
     * </table>
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param sourceDimensions Number of dimensions in the source CRS of this operation method.
     * @param targetDimensions Number of dimensions in the target CRS of this operation method.
     * @param parameters The set of parameters, or {@code null} if none.
     */
    public DefaultOperationMethod(final Map properties,
                                  final int sourceDimensions,
                                  final int targetDimensions,
                                  final ParameterDescriptorGroup parameters)
    {
        this(properties, new HashMap(), sourceDimensions, targetDimensions, parameters);
    }

    /**
     * Work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private DefaultOperationMethod(final Map properties,
                                   final Map subProperties,
                                   final int sourceDimensions,
                                   final int targetDimensions,
                                   ParameterDescriptorGroup parameters)
    {
        super(properties, subProperties, LOCALIZABLES);
        formula = (InternationalString) subProperties.get(FORMULA_KEY);
        // 'parameters' may be null, which is okay. A null value will
        // make serialization smaller and faster than an empty object.
        this.parameters       = parameters;
        this.sourceDimensions = sourceDimensions;
        this.targetDimensions = targetDimensions;
        ensurePositive("sourceDimensions", sourceDimensions);
        ensurePositive("targetDimensions", targetDimensions);
    }

    /**
     * Ensure that the specified value is positive.
     * An {@link IllegalArgumentException} is throws if it is not.
     *
     * @param name  The parameter name.
     * @param value The parameter value.
     * @throws IllegalArgumentException if the specified value is not positive.
     */
    private static void ensurePositive(final String name, final int value)
            throws IllegalArgumentException
    {
        if (value < 0) {
            throw new IllegalArgumentException(Resources.format(
                      ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2, name, new Integer(value)));
        }
    }

    /**
     * Formula(s) or procedure used by this operation method. This may be a reference to a
     * publication. Note that the operation method may not be analytic, in which case this
     * attribute references or contains the procedure, not an analytic formula.
     */
    public InternationalString getFormula() {
        return formula;
    }

    /**
     * Number of dimensions in the source CRS of this operation method.
     *
     * @return The dimension of source CRS.
     */
    public int getSourceDimensions() {
        return sourceDimensions;
    }

    /**
     * Number of dimensions in the target CRS of this operation method.
     */
    public int getTargetDimensions() {
        return targetDimensions;
    }

    /**
     * Returns the set of parameters.
     */
    public ParameterDescriptorGroup getParameters() {
        return (parameters!=null) ? parameters : Parameters.EMPTY_GROUP;
    }

    /**
     * Returns the operation type. Current implementation returns {@code Projection.class} for
     * proper WKT formatting using an unknow implementation. But the {@link MathTransformProvider}
     * subclass (with protected access) will overrides this method with a more conservative default
     * value.
     */
    Class getOperationType() {
        return Projection.class;
    }

    /**
     * Compare this operation method with the specified object for equality.
     * If {@code compareMetadata} is {@code true}, then all available
     * properties are compared including {@linkplain #getFormula formula}.
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
            final DefaultOperationMethod that = (DefaultOperationMethod) object;
            if (this.sourceDimensions == that.sourceDimensions &&
                this.targetDimensions == that.targetDimensions &&
                equals(this.parameters,  that.parameters, compareMetadata))
            {
                return !compareMetadata || Utilities.equals(this.formula, that.formula);
            }
        }
        return false;
    }

    /**
     * Returns a hash code value for this operation method.
     */
    public int hashCode() {
        int code = (int)serialVersionUID + sourceDimensions + 37*targetDimensions;
        if (parameters != null) {
            code = code * 37 + parameters.hashCode();
        }
        return code;
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
        if (Projection.class.isAssignableFrom(getOperationType())) {
            return "PROJECTION";
        }
        return super.formatWKT(formatter);
    }

    /**
     * Check if an operation method and a math transform have a compatible number of source
     * and target dimensions. This convenience method is provided for argument checking.
     *
     * @param  method    The operation method to compare to the math transform, or {@code null}.
     * @param  transform The math transform to compare to the operation method, or {@code null}.
     * @throws MismatchedDimensionException if the number of dimensions are incompatibles.
     */
    public static void checkDimensions(final OperationMethod method,
                                       final MathTransform transform)
            throws MismatchedDimensionException
    {
        if (method!=null && transform!=null) {
            final String name;
            int actual, expected;
            if ((actual=transform.getSourceDimensions()) !=
                 (expected=method.getSourceDimensions()))
            {
                name = "sourceDimensions";
            } else if ((actual=transform.getTargetDimensions()) !=
                        (expected=method.getTargetDimensions()))
            {
                name = "targetDimensions";
            } else {
                return;
            }
            throw new IllegalArgumentException(Resources.format(
                                               ResourceKeys.ERROR_MISMATCHED_DIMENSION_$3,
                                               name, new Integer(actual), new Integer(expected)));
        }
    }
}
