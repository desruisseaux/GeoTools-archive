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
import java.util.Arrays;
import java.util.Locale;
import java.util.HashMap;

// OpenGIS dependencies
import org.opengis.util.InternationalString;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.quality.PositionalAccuracy;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

// Geotools dependencies
import org.geotools.referencing.crs.GeneralDerivedCRS;
import org.geotools.referencing.IdentifiedObject;
import org.geotools.referencing.wkt.Formatter;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.Utilities;


/**
 * Establishes an association between a source and a target coordinate reference system,
 * and provides a {@linkplain MathTransform transform} for transforming coordinates in
 * the source CRS to coordinates in the target CRS. Many but not all coordinate operations (from
 * {@linkplain org.geotools.referencing.crs.CoordinateReferenceSystem coordinate reference system} <VAR>A</VAR> to
 * {@linkplain org.geotools.referencing.crs.CoordinateReferenceSystem coordinate reference system} <VAR>B</VAR>)
 * also uniquely define the inverse operation (from
 * {@linkplain org.geotools.referencing.crs.CoordinateReferenceSystem coordinate reference system} <VAR>B</VAR> to
 * {@linkplain org.geotools.referencing.crs.CoordinateReferenceSystem coordinate reference system} <VAR>A</VAR>).
 * In some cases, the operation method algorithm for the inverse operation is the same
 * as for the forward algorithm, but the signs of some operation parameter values must
 * be reversed. In other cases, different algorithms are required for the forward and
 * inverse operations, but the same operation parameter values are used. If (some)
 * entirely different parameter values are needed, a different coordinate operation
 * shall be defined.
 *  
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class CoordinateOperation extends IdentifiedObject
                              implements org.opengis.referencing.operation.CoordinateOperation
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 1237358357729193885L;

    /**
     * An empty array of positional accuracy.
     */
    private static final PositionalAccuracy[] EMPTY_ACCURACY = new PositionalAccuracy[0];

    /**
     * List of localizable properties. To be given to {@link IdentifiedObject} constructor.
     *
     * @todo Declare constants
     */
    private static final String[] LOCALIZABLES = {"scope"};

    /**
     * The source CRS, or <code>null</code> if not available.
     */
    protected final CoordinateReferenceSystem sourceCRS;

    /**
     * The target CRS, or <code>null</code> if not available.
     */
    protected final CoordinateReferenceSystem targetCRS;

    /**
     * Version of the coordinate transformation
     * (i.e., instantiation due to the stochastic nature of the parameters).
     */
    final String operationVersion;

    /**
     * Estimate(s) of the impact of this operation on point accuracy, or <code>null</code>
     * if none.
     */
    private final PositionalAccuracy[] positionalAccuracy;

    /**
     * Area in which this operation is valid, or <code>null</code> if not available.
     */
    protected final Extent validArea;

    /**
     * Description of domain of usage, or limitations of usage, for which this operation is valid.
     */
    private final InternationalString scope;

    /**
     * Transform from positions in the {@linkplain #getSourceCRS source coordinate reference system}
     * to positions in the {@linkplain #getTargetCRS target coordinate reference system}.
     */
    protected final MathTransform transform;

    /**
     * Constructs a coordinate operation from a set of properties. The properties given in argument
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
     *     <td nowrap>&nbsp;<code>"operationVersion"</code>&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getOperationVersion}</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;<code>"positionalAccuracy"</code>&nbsp;</td>
     *     <td nowrap>&nbsp;<code>{@linkplain PositionalAccuracy}[]</code>&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getPositionalAccuracy}</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;<code>"validArea"</code>&nbsp;</td>
     *     <td nowrap>&nbsp;{@link Extent}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getValidArea}</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;<code>"scope"</code>&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String} or {@link InternationalString}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getScope}</td>
     *   </tr>
     * </table>
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param sourceCRS The source CRS, or <code>null</code> if not available.
     * @param targetCRS The target CRS, or <code>null</code> if not available.
     * @param transform Transform from positions in the {@linkplain #getSourceCRS source coordinate
     *                  reference system} to positions in the {@linkplain #getTargetCRS target
     *                  coordinate reference system}.
     */
    public CoordinateOperation(final Map                      properties,
                               final CoordinateReferenceSystem sourceCRS,
                               final CoordinateReferenceSystem targetCRS,
                               final MathTransform             transform)
    {
        this(properties, new HashMap(), sourceCRS, targetCRS, transform);
    }

    /**
     * Work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private CoordinateOperation(final Map properties, final Map subProperties,
                                final CoordinateReferenceSystem sourceCRS,
                                final CoordinateReferenceSystem targetCRS,
                                final MathTransform             transform)
    {
        super(properties, subProperties, LOCALIZABLES);
        PositionalAccuracy[] positionalAccuracy;
        validArea          = (Extent)               subProperties.get("validArea"         );
        scope              = (InternationalString)  subProperties.get("scope"             );
        operationVersion   = (String)               subProperties.get("operationVersion"  );
        positionalAccuracy = (PositionalAccuracy[]) subProperties.get("positionalAccuracy");
        if (positionalAccuracy==null || positionalAccuracy.length==0) {
            positionalAccuracy = null;
        } else {
            positionalAccuracy = (PositionalAccuracy[]) positionalAccuracy.clone();
            for (int i=0; i<positionalAccuracy.length; i++) {
                ensureNonNull("positionalAccuracy", positionalAccuracy, i);
            }
        }
        this.positionalAccuracy = positionalAccuracy;
        this.sourceCRS = sourceCRS;
        this.targetCRS = targetCRS;
        this.transform = transform;
        ensureNonNull("transform", transform);
        checkDimension("sourceCRS", sourceCRS, transform.getDimSource());
        checkDimension("targetCRS", targetCRS, transform.getDimTarget());
    }

    /**
     * Check if a reference coordinate system has the expected number of dimensions.
     *
     * @param name     The argument name.
     * @param crs      The coordinate reference system to check.
     * @param expected The expected number of dimensions.
     */
    private static void checkDimension(final String name,
                                       final CoordinateReferenceSystem crs,
                                       final int expected)
    {
        if (crs != null) {
            final int actual = crs.getCoordinateSystem().getDimension();
            if (actual != expected) {
                throw new IllegalArgumentException(Resources.format(
                          ResourceKeys.ERROR_MISMATCHED_DIMENSION_$3,
                          name, new Integer(actual), new Integer(expected)));
            }
        }
    }

    /**
     * Returns the source CRS.
     */
    public CoordinateReferenceSystem getSourceCRS() {
        return sourceCRS;
    }

    /**
     * Returns the target CRS.
     */
    public CoordinateReferenceSystem getTargetCRS() {
        return targetCRS;
    }

    /**
     * Version of the coordinate transformation (i.e., instantiation due to the stochastic
     * nature of the parameters). Mandatory when describing a transformation, and should not
     * be supplied for a conversion.
     *
     * @return The coordinate operation version, or <code>null</code> in none.
     */
    public String getOperationVersion() {
        return operationVersion;
    }

    /**
     * Estimate(s) of the impact of this operation on point accuracy. Gives
     * position error estimates for target coordinates of this coordinate
     * operation, assuming no errors in source coordinates.
     *
     * @return The position error estimates, or an empty array if not available.
     */
    public PositionalAccuracy[] getPositionalAccuracy() {
        return (positionalAccuracy!=null) ? (PositionalAccuracy[]) positionalAccuracy.clone()
                                          : EMPTY_ACCURACY;
    }

    /**
     * Area in which this operation is valid.
     *
     * @return Coordinate operation valid area, or <code>null</code> if not available.
     */
    public Extent getValidArea() {
        return validArea;
    }

    /**
     * Description of domain of usage, or limitations of usage, for which this operation is valid.
     */
    public InternationalString getScope() {
        return scope;
    }
    
    /**
     * Gets the math transform. The math transform will transform positions in the
     * {@linkplain #getSourceCRS source coordinate reference system} into positions
     * in the {@linkplain #getTargetCRS target coordinate reference system}.
     */
    public MathTransform getMathTransform() {
        return transform;
    }

    /**
     * Compare this coordinate operation with the specified object for equality.
     * If <code>compareMetadata</code> is <code>true</code>, then all available properties are
     * compared including {@linkplain #getValidArea valid area} and {@linkplain #getScope scope}.
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
            final CoordinateOperation that = (CoordinateOperation) object;
            if (equals(this.sourceCRS, that.sourceCRS, compareMetadata) &&
                Utilities.equals(this.transform, that.transform))
            {
                if (compareMetadata) {
                    if (!Utilities.equals(this.validArea,          that.validArea) ||
                        !Utilities.equals(this.scope,              that.scope    ) ||
                        !Arrays   .equals(this.positionalAccuracy, that.positionalAccuracy))
                        // TODO: Uses Arrays.deepEquals(...) when J2SE 1.5 will be available.
                    {
                        return false;
                    }
                }
                /*
                 * Avoid never-ending recursivity: GeneralDerivedCRS has a
                 * 'conversionFromBase' field that is set to this CoordinateOperation.
                 */
                synchronized (GeneralDerivedCRS.class) {
                    if (GeneralDerivedCRS.\u00A4COMPARING != null) {
                        assert GeneralDerivedCRS.\u00A4COMPARING == targetCRS;
                        return true;
                    }
                    try {
                        GeneralDerivedCRS.\u00A4COMPARING = this;
                        return equals(this.targetCRS, that.targetCRS, compareMetadata);
                    } finally {
                        GeneralDerivedCRS.\u00A4COMPARING = null;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns a hash code value for this coordinate operation.
     */
    public int hashCode() {
        int code = (int)serialVersionUID;
        if (sourceCRS != null) code ^= sourceCRS.hashCode();
        if (targetCRS != null) code ^= targetCRS.hashCode();
        if (transform != null) code ^= transform.hashCode();
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
        formatter.append(sourceCRS);
        formatter.append(targetCRS);
        formatter.append(transform);
        return super.formatWKT(formatter);
    }
}
