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
import java.util.Map;
import java.util.Locale;
import java.util.HashMap;

// OpenGIS dependencies
import org.opengis.parameter.GeneralOperationParameter;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

// Geotools dependencies
import org.geotools.referencing.IdentifiedObject;
import org.geotools.referencing.wkt.Formatter;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Definition of an algorithm used to perform a coordinate operation. Most operation
 * methods use a number of operation parameters, although some coordinate conversions
 * use none. Each coordinate operation using the method assigns values to these parameters.
 *  
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see Operation
 */
public class OperationMethod extends IdentifiedObject
                          implements org.opengis.referencing.operation.OperationMethod
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -98032729598205972L;

    /**
     * An empty array of parameters.
     */
    private static final GeneralOperationParameter[] EMPTY_PARAMETER = new GeneralOperationParameter[0];

    /**
     * List of localizable properties. To be given to {@link IdentifiedObject} constructor.
     */
    private static final String[] LOCALIZABLES = {"formula"};

    /**
     * Formula(s) or procedure used by this operation method. This may be a reference to a
     * publication. Note that the operation method may not be analytic, in which case this
     * attribute references or contains the procedure, not an analytic formula.
     */
    private final Map formula;

    /**
     * Number of dimensions in the source CRS of this operation method.
     */
    protected final int sourceDimensions;

    /**
     * Number of dimensions in the target CRS of this operation method.
     */
    protected final int targetDimensions;

    /**
     * The set of parameters, or <code>null</code> if none.
     */
    private final GeneralOperationParameter[] parameters;

    /**
     * Construct an operation method from a set of properties. The properties given in argument
     * follow the same rules than for the {@linkplain IdentifiedObject#IdentifiedObject(Map)
     * super-class constructor}. Additionally, the following properties are understood by this
     * construtor:
     * <br><br>
     * <table border='1'>
     *   <tr bgcolor="#CCCCFF" class="TableHeadingColor">
     *     <th nowrap>Property name</th>
     *     <th nowrap>Value type</th>
     *     <th nowrap>Value given to</th>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;<code>"formula"</code>&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getFormula}</td>
     *   </tr>
     * </table>
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param sourceDimensions Number of dimensions in the source CRS of this operation method.
     * @param targetDimensions Number of dimensions in the target CRS of this operation method.
     * @param parameters The set of parameters, or <code>null</code> or an empty array if none.
     */
    public OperationMethod(final Map properties,
                           final int sourceDimensions,
                           final int targetDimensions,
                           final GeneralOperationParameter[] parameters)
    {
        this(properties, new HashMap(), sourceDimensions, targetDimensions, parameters);
    }

    /**
     * Work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private OperationMethod(final Map properties, final Map subProperties,
                            final int sourceDimensions,
                            final int targetDimensions,
                            GeneralOperationParameter[] parameters)
    {
        super(properties, subProperties, LOCALIZABLES);
        formula = (Map)    subProperties.get("formula");
        if (parameters==null || parameters.length==0) {
            parameters = null;
        } else {
            parameters = (GeneralOperationParameter[]) parameters.clone();
            for (int i=0; i<parameters.length; i++) {
                ensureNonNull("parameters", parameters, i);
            }
        }
        this.parameters       = parameters;
        this.sourceDimensions = sourceDimensions;
        this.targetDimensions = targetDimensions;
    }

    /**
     * Formula(s) or procedure used by this operation method. This may be a reference to a
     * publication. Note that the operation method may not be analytic, in which case this
     * attribute references or contains the procedure, not an analytic formula.
     *
     * @param  locale The desired locale for the formula to be returned, or <code>null</code>
     *         for a formula in some default locale (may or may not be the
     *         {@linkplain Locale#getDefault() system default}).
     * @return The coordinate operation method formula in the given locale. If no formula
     *         is available in the given locale, then some default locale is used.
     */
    public String getFormula(final Locale locale) {
        return getLocalized(formula, locale);
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
     *
     * @return The dimension of target CRS.
     */
    public int getTargetDimensions() {
        return targetDimensions;
    }

    /**
     * The set of parameters.
     *
     * @return The parameters, or an empty array if none.
     */
    public GeneralOperationParameter[] getParameters() {
        return (parameters!=null) ? (GeneralOperationParameter[]) parameters.clone()
                                  : EMPTY_PARAMETER;
    }

    /**
     * Compare this operation method with the specified object for equality.
     * If <code>compareMetadata</code> is <code>true</code>, then all available
     * properties are compared including {@linkplain #getFormula formula}.
     *
     * @param  object The object to compare to <code>this</code>.
     * @param  compareMetadata <code>true</code> for performing a strict comparaison, or
     *         <code>false</code> for comparing only properties relevant to transformations.
     * @return <code>true</code> if both objects are equal.
     */
    public boolean equals(final IdentifiedObject object, final boolean compareMetadata) {
        if (object == this) {
            return true; // Slight optimization.
        }
        if (super.equals(object, compareMetadata)) {
            final OperationMethod that = (OperationMethod) object;
            if (this.sourceDimensions == that.sourceDimensions &&
                this.targetDimensions == that.targetDimensions &&
                equals(this.parameters,  that.parameters))
            {
                return !compareMetadata || equals(this.formula, that.formula);
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
            for (int i=parameters.length; --i>=0;) {
                code = code*37 + parameters[i].hashCode();
            }
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
        return "PROJECTION";
    }

    /**
     * Check if an operation method and a math transform have a compatible number of source
     * and target dimensions. This convenience method is provided for argument checking.
     *
     * @param  method    The operation method to compare to the math transform, or <code>null</code>.
     * @param  transform The math transform to compare to the operation method, or <code>null</code>.
     * @throws MismatchedDimensionException if the number of dimensions are incompatibles.
     */
    public static void checkDimensions(final org.opengis.referencing.operation.OperationMethod method,
                                       final MathTransform transform)
            throws MismatchedDimensionException
    {
        if (method!=null && transform!=null) {
            final String name;
            int actual, expected;
            if ((actual=transform.getDimSource()) != (expected=method.getSourceDimensions())) {
                name = "sourceDimensions";
            } else if ((actual=transform.getDimTarget()) != (expected=method.getTargetDimensions())) {
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
