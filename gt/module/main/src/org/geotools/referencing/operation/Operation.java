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
import java.util.Arrays;

// OpenGIS dependencies
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

// Geotools dependencies
import org.geotools.referencing.Info;


/**
 * A parameterized mathematical operation on coordinates that transforms or converts
 * coordinates to another coordinate reference system. This coordinate operation thus
 * uses an operation method, usually with associated parameter values.
 *  
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see OperationMethod
 */
public class Operation extends SingleOperation
                    implements org.opengis.referencing.operation.Operation
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -8923365753849532179L;

    /**
     * An empty array of parameters.
     */
    private static final GeneralParameterValue[] EMPTY_PARAMETER = new GeneralParameterValue[0];

    /**
     * The operation method.
     */
    protected final OperationMethod method;

    /**
     * The parameter values, or <code>null</code> if none.
     */
    private final GeneralParameterValue[] values;

    /**
     * Construct an operation from a set of properties. The properties given in argument
     * follow the same rules than for the {@link CoordinateOperation} constructor.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param sourceCRS The source CRS, or <code>null</code> if not available.
     * @param targetCRS The target CRS, or <code>null</code> if not available.
     * @param transform Transform from positions in the {@linkplain #getSourceCRS source coordinate
     *                  reference system} to positions in the {@linkplain #getTargetCRS target
     *                  coordinate reference system}.
     * @param method    The operation method.
     * @param values    The parameter values, or <code>null</code> or an empty array if none.
     */
    public Operation(final Map                      properties,
                     final CoordinateReferenceSystem sourceCRS,
                     final CoordinateReferenceSystem targetCRS,
                     final MathTransform             transform,
                     final OperationMethod           method,
                           GeneralParameterValue[]   values)
    {
        super(properties, sourceCRS, targetCRS, transform);
        ensureNonNull("method", method);
        org.geotools.referencing.operation.OperationMethod.checkDimensions(method, transform);
        if (values==null || values.length==0) {
            values = null;
        } else {
            values = (GeneralParameterValue[]) values.clone();
            for (int i=0; i<values.length; i++) {
                ensureNonNull("values", values, i);
                values[i] = (GeneralParameterValue) values[i].clone();
            }
        }
        this.values = values;
        this.method = method;
    }

    /**
     * Returns the operation method.
     *
     * @return The operation method.
     */
    public OperationMethod getMethod() {
        return method;
    }

    /**
     * Returns the parameter values.
     *
     * @return The parameter values, or an empty array if none.
     */
    public GeneralParameterValue[] getParameterValues() {
        return (values!=null) ? (GeneralParameterValue[]) values.clone() : EMPTY_PARAMETER;
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
    public boolean equals(final Info object, final boolean compareMetadata) {
        if (super.equals(object, compareMetadata)) {
            final Operation that = (Operation) object;
            return equals(this.method, that.method, compareMetadata) &&
                   Arrays.equals(this.values, that.values);
        }
        return false;
    }

    /**
     * Returns a hash code value for this operation method.
     */
    public int hashCode() {
        int code = super.hashCode() + method.hashCode();
        if (values != null) {
            for (int i=values.length; --i>=0;) {
                code = code*37 + values[i].hashCode();
            }
        }
        return code;
    }
}
