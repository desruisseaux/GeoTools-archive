/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2003, Institut de Recherche pour le Développement
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
package org.geotools.referencing.crs;

// J2SE dependencies
import java.util.Map;
import java.util.Collections;

// OpenGIS direct dependencies
import org.opengis.referencing.datum.Datum;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.SingleCRS;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.Projection;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.GeneralParameterValue;

// Geotools dependencies
import org.geotools.referencing.IdentifiedObject;
import org.geotools.referencing.ReferenceSystem;  // For javadoc
import org.geotools.referencing.wkt.Formatter;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * A coordinate reference system that is defined by its coordinate
 * {@linkplain Conversion conversion} from another coordinate reference system
 * (not by a {@linkplain org.opengis.referencing.datum.Datum datum}).
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class GeneralDerivedCRS extends org.geotools.referencing.crs.SingleCRS
                            implements org.opengis.referencing.crs.GeneralDerivedCRS
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -175151161496419854L;

    /**
     * The base coordinate reference system.
     */
    protected final CoordinateReferenceSystem baseCRS;

    /**
     * The conversion from the {@linkplain #getBaseCRS base CRS} to this CRS.
     */
    protected final Conversion conversionFromBase;

    /**
     * Constructs a derived CRS from a name.
     *
     * @param name The name.
     * @param  base Coordinate reference system to base the derived CRS on.
     * @param  baseToDerived The transform from the base CRS to returned CRS.
     * @param  derivedCS The coordinate system for the derived CRS. The number
     *         of axes must match the target dimension of the transform
     *         <code>baseToDerived</code>.
     * @throws MismatchedDimensionException if the source and target dimension of
     *         <code>baseToDerived</code> don't match the dimension of <code>base</code>
     *         and <code>derivedCS</code> respectively.
     */
    public GeneralDerivedCRS(final String                    name,
                             final CoordinateReferenceSystem base,
                             final MathTransform    baseToDerived,
                             final CoordinateSystem     derivedCS)
            throws MismatchedDimensionException
    {
        this(Collections.singletonMap("name", name), base, baseToDerived, derivedCS);
    }

    /**
     * Constructs a derived CRS from a set of properties. The properties are given unchanged to
     * the {@linkplain ReferenceSystem#ReferenceSystem(Map) super-class constructor}. The following
     * optional properties are also understood:
     * <br><br>
     * <table border='1'>
     *   <tr bgcolor="#CCCCFF" class="TableHeadingColor">
     *     <th nowrap>Property name</th>
     *     <th nowrap>Value type</th>
     *     <th nowrap>Value given to</th>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;<code>"parameters"</code>&nbsp;</td>
     *     <td nowrap>&nbsp;<code>{@linkplain GeneralParameterValue}[]</code>&nbsp;</td>
     *     <td nowrap>&nbsp;{@link Conversion#getParameterValues}</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;<code>"method.name"</code>&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String}&nbsp;</td>
     *     <td nowrap>&nbsp;<code>{@linkplain Conversion#getMethod}.getName()</code></td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;<code>"conversion.name"</code>&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String}&nbsp;</td>
     *     <td nowrap>&nbsp;<code>{@linkplain #getConversionFromBase}.getName()</code></td>
     *   </tr>
     * </table>
     *
     * <P>Additional properties for the {@link org.geotools.referencing.operation.Conversion} object
     * to be created can be specified with the <code>"conversion."</code> prefix added in front of
     * property names (example: <code>"conversion.remarks"</code>). The same applies for operation
     * method, using the <code>"method."</code> prefix.</P>
     *
     * @param  properties Name and other properties to give to the new derived CRS object and to
     *         the underlying {@link org.geotools.referencing.operation.Conversion conversion}.
     * @param  base Coordinate reference system to base the derived CRS on.
     * @param  baseToDerived The transform from the base CRS to returned CRS.
     * @param  derivedCS The coordinate system for the derived CRS. The number
     *         of axes must match the target dimension of the transform
     *         <code>baseToDerived</code>.
     * @throws MismatchedDimensionException if the source and target dimension of
     *         <code>baseToDerived</code> don't match the dimension of <code>base</code>
     *         and <code>derivedCS</code> respectively.
     */
    public GeneralDerivedCRS(final Map                 properties,
                             final CoordinateReferenceSystem base,
                             final MathTransform    baseToDerived,
                             final CoordinateSystem     derivedCS)
            throws MismatchedDimensionException
    {
        super(properties, getDatum(base), derivedCS);
        ensureNonNull("baseToDerived", baseToDerived);
        this.baseCRS = base;
        final int dimSource = baseToDerived.getDimSource();
        final int dimTarget = baseToDerived.getDimTarget();
        int dim1, dim2;
        if ((dim1=dimSource) != (dim2=base.getCoordinateSystem().getDimension()) ||
            (dim1=dimTarget) != (dim2=derivedCS.getDimension()))
        {
            throw new MismatchedDimensionException(Resources.format(
                        ResourceKeys.ERROR_MISMATCHED_DIMENSION_$2,
                        new Integer(dim1), new Integer(dim2)));
        }
        /*
         * Gets the parameters, which may be null. If no OperationMethod was explicitly specified,
         * a new one will be inferred from the parameters. The operation method will inherit the
         * name from this GeneralDerivedCRS, unless a "method.name" property were explicitly
         * specified.
         */
        final GeneralParameterValue[] parameters;
        parameters = (GeneralParameterValue[]) properties.get("parameters");
        OperationMethod method = (OperationMethod) properties.get("method");
        if (method == null) {
            final GeneralParameterDescriptor[] descriptors;
            if (parameters != null) {
                descriptors = new GeneralParameterDescriptor[parameters.length];
                for (int i=0; i<descriptors.length; i++) {
                    descriptors[i] = parameters[i].getDescriptor();
                }
            } else {
                descriptors = null;
            }
            method = new org.geotools.referencing.operation.OperationMethod(
                         /* properties       */ new UnprefixedMap(properties, "method."),
                         /* sourceDimensions */ dimSource,
                         /* targetDimensions */ dimTarget,
                         /* parameters       */ descriptors);
        } else {
            /*
             * A method was explicitly specified. Make sure that the source and target dimensions
             * match. We do not check parameters in current version of this implementation (we may
             * add this check in a future version), since the provided parameter descriptors may be
             * more accurate than the one inferred from the parameter values.
             */
            org.geotools.referencing.operation.OperationMethod.checkDimensions(method, baseToDerived);
        }
        /*
         * Constructs the conversion from all the information above. The ProjectedCRS subclass
         * will overrides the createConversion method in order to create a projection instead.
         */
        this.conversionFromBase = createConversion(
                                  /* properties */ new UnprefixedMap(properties, "conversion."),
                                  /* sourceCRS  */ base,
                                  /* targetCRS  */ this,
                                  /* transform  */ baseToDerived,
                                  /* method     */ method,
                                  /* parameters */ parameters);
    }

    /**
     * Wraps the specified arguments in a {@link Conversion} object. Class {@link ProjectedCRS}
     * will overrides this method in order to wraps the arguments in a  in a {@link Projection}
     * object instead.
     */
    Conversion createConversion(final Map                       properties,
                                final CoordinateReferenceSystem sourceCRS,
                                final CoordinateReferenceSystem targetCRS,
                                final MathTransform             transform,
                                final OperationMethod           method,
                                final GeneralParameterValue[]   values)
    {
        return new org.geotools.referencing.operation.Conversion(properties,
                    sourceCRS, targetCRS, transform, method, values);
    }

    /**
     * Work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     *
     * @todo What to do if <code>base</code> is not an instance of {@link SingleCRS}?
     */
    private static Datum getDatum(final CoordinateReferenceSystem base) {
        ensureNonNull("base",  base);
        return (base instanceof SingleCRS) ? ((SingleCRS) base).getDatum() : null;
    }

    /**
     * Returns the base coordinate reference system.
     *
     * @return The base coordinate reference system.
     */
    public CoordinateReferenceSystem getBaseCRS() {
        return baseCRS;
    }

    /**
     * Returns the conversion from the {@linkplain #getBaseCRS base CRS} to this CRS.
     *
     * @return The conversion to this CRS.
     */
    public Conversion getConversionFromBase() {
        return conversionFromBase;
    }

    /**
     * Compare this coordinate reference system with the specified object for equality.
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
            final GeneralDerivedCRS that = (GeneralDerivedCRS) object;
            return equals(this.baseCRS,            that.baseCRS,            compareMetadata) &&
                   equals(this.conversionFromBase, that.conversionFromBase, compareMetadata);
        }
        return false;
    }
    
    /**
     * Returns a hash value for this derived CRS.
     *
     * @return The hash code value. This value doesn't need to be the same
     *         in past or future versions of this class.
     */
    public int hashCode() {
        return (int)serialVersionUID ^ baseCRS.hashCode() ^ conversionFromBase.hashCode();
    }
    
    /**
     * Format the inner part of a
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite> (WKT)</A> element.
     *
     * @param  formatter The formatter to use.
     * @return The WKT element name, which is "FITTED_CS"
     */
    protected String formatWKT(final Formatter formatter) {
        try {
            formatter.append(conversionFromBase.getMathTransform().inverse());
            formatter.append(baseCRS);
            return "FITTED_CS";
        } catch (NoninvertibleTransformException exception) {
            // TODO: provide a more accurate error message.
            IllegalStateException e = new IllegalStateException(exception.getLocalizedMessage());
            e.initCause(exception);
            throw e;
        }
    }
}
