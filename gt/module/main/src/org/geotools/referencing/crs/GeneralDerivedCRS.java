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

// OpenGIS dependencies
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.crs.SingleCRS;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.datum.Datum;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.referencing.operation.Projection;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

// Geotools dependencies
import org.geotools.referencing.IdentifiedObject;
import org.geotools.referencing.operation.SingleOperation;
import org.geotools.referencing.wkt.Formatter;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.cts.Resources;


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
     * A lock for avoiding never-ending recursivity in the <CODE>equals</CODE>
     * method. This lock is necessary because <CODE>GeneralDerivedCRS</CODE>
     * objects contain a {@link #conversionFromBase} field, which contains a
     * {@link org.geotools.referencing.operation.Conversion#targetCRS} field
     * set to this <CODE>GeneralDerivedCRS</CODE> object.
     *
     * <P>This field can be though as a <CODE>boolean</CODE> flag set to <CODE>true</CODE>
     * when a comparaison is in progress. A null value means <CODE>false</CODE>, and a non-null
     * value means <CODE>true</CODE>. The non-null value is used for assertion.</P>
     *
     * <P>When an <CODE>equals</CODE> method is invoked, this <CODE>\u00A4COMPARING</CODE>
     * field is set to the originator (either the <CODE>GeneralDerivedCRS</CODE> or the
     * {@link org.geotools.referencing.operation.CoordinateOperation} object where the
     * comparaison begin).</P>
     *
     * <P><STRONG>DO NOT USE THIS FIELD. It is strictly for internal use by {@link #equals} and
     * {@link org.geotools.referencing.operation.CoordinateOperation#equals} methods.</STRONG></P>
     *
     * @todo Hide this field from the javadoc. It is not possible to make it package-privated
     *       because {@link org.geotools.referencing.operation.CoordinateOperation} lives in
     *       a different package.
     */
    public static IdentifiedObject COMPARING;

    /**
     * The base coordinate reference system.
     */
    protected final CoordinateReferenceSystem baseCRS;

    /**
     * The conversion from the {@linkplain #getBaseCRS base CRS} to this CRS.
     */
    protected final Conversion conversionFromBase;

    /**
     * Constructs a derived CRS from a set of properties. The properties are given unchanged to
     * the {@linkplain org.geotools.referencing.ReferenceSystem#ReferenceSystem(Map) super-class
     * constructor}. The following optional properties are also understood:
     * <br><br>
     * <table border='1'>
     *   <tr bgcolor="#CCCCFF" class="TableHeadingColor">
     *     <th nowrap>Property name</th>
     *     <th nowrap>Value type</th>
     *     <th nowrap>Value given to</th>
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
     * @param  method A description of the {@linkplain Conversion#getMethod method for the
     *         conversion}.
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
                             final OperationMethod         method,
                             final CoordinateReferenceSystem base,
                             final MathTransform    baseToDerived,
                             final CoordinateSystem     derivedCS)
            throws MismatchedDimensionException
    {
        super(properties, getDatum(base), derivedCS);
        ensureNonNull("method",        method);
        ensureNonNull("baseToDerived", baseToDerived);
        this.baseCRS = base;
        final int dimSource = baseToDerived.getSourceDimensions();
        final int dimTarget = baseToDerived.getTargetDimensions();
        int dim1, dim2;
        if ((dim1=dimSource) != (dim2=base.getCoordinateSystem().getDimension()) ||
            (dim1=dimTarget) != (dim2=derivedCS.getDimension()))
        {
            throw new MismatchedDimensionException(Resources.format(
                        ResourceKeys.ERROR_MISMATCHED_DIMENSION_$2,
                        new Integer(dim1), new Integer(dim2)));
        }
        /*
         * A method was explicitly specified. Make sure that the source and target
         * dimensions match. We do not check parameters in current version of this
         * implementation (we may add this check in a future version), since the
         * descriptors provided in this user-supplied OperationMethod may be more
         * accurate than the one inferred from the MathTransform.
         */
        org.geotools.referencing.operation.OperationMethod.checkDimensions(method, baseToDerived);
        this.conversionFromBase = (Conversion) SingleOperation.create(
            /* properties */ new UnprefixedMap(properties, "conversion."),
            /* sourceCRS  */ base,
            /* targetCRS  */ this,
            /* transform  */ baseToDerived,
            /* method     */ method,
            /* type       */ (this instanceof ProjectedCRS) ? Projection.class : Conversion.class);
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
            if (equals(this.baseCRS, that.baseCRS, compareMetadata)) {
                /*
                 * Avoid never-ending recursivity: Conversion has a 'targetCRS' field (inherited
                 * from the CoordinateOperation super-class) that is set to this GeneralDerivedCRS.
                 */
                synchronized (GeneralDerivedCRS.class) {
                    if (COMPARING != null) {
                        // NOTE: the following assertion fails for deserialized objects.
                        // assert \u00A4COMPARING == conversionFromBase;
                        return true;
                    }
                    try {
                        COMPARING = this;
                        return equals(this.conversionFromBase,
                                      that.conversionFromBase,
                                      compareMetadata);
                    } finally {
                        COMPARING = null;
                    }
                }
            }
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
        /*
         * Do not invoke 'conversionFromBase.hashCode()' in order to avoid a never-ending loop.
         * This is because Conversion has a 'sourceCRS' field (in the CoordinateOperation super-
         * class), which is set to this GeneralDerivedCRS. Checking the identifier should be enough.
         */
        return (int)serialVersionUID ^ baseCRS.hashCode() ^ conversionFromBase.getName().hashCode();
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
