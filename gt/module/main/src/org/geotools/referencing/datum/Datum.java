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
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing.datum;

// J2SE dependencies
import java.util.Map;
import java.util.Date;
import java.util.HashMap;

// OpenGIS dependencies
import org.opengis.metadata.extent.Extent;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.referencing.IdentifiedObject;
import org.geotools.referencing.wkt.Formatter;
import org.geotools.resources.Utilities;


/**
 * Specifies the relationship of a coordinate system to the earth, thus creating a {@linkplain
 * org.geotools.referencing.crs.CoordinateReferenceSystem coordinate reference system}. A datum
 * uses a parameter or set of parameters that determine the location of the origin of the coordinate
 * reference system. Each datum subtype can be associated with only specific types of
 * {@linkplain org.geotools.referencing.cs.CoordinateSystem coordinate systems}.
 *
 * A datum can be defined as a set of real points on the earth that have coordinates.
 * The definition of the datum may also include the temporal behavior (such as the
 * rate of change of the orientation of the coordinate axes).
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see org.geotools.referencing.cs.CoordinateSystem
 * @see org.geotools.referencing.crs.CoordinateReferenceSystem
 */
public class Datum extends IdentifiedObject implements org.opengis.referencing.datum.Datum {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -4894180465652474930L;

    /**
     * Key for the <code>"anchorPoint"</code> property to be given to the
     * {@linkplain #Datum(Map) constructor}. This is used
     * for setting the value to be returned by {@link #getAnchorPoint()}.
     */
    public static final String ANCHOR_POINT_PROPERTY = "anchorPoint";

    /**
     * Key for the <code>"realizationEpoch"</code> property to be given to the
     * {@linkplain #Datum(Map) constructor}. This is used
     * for setting the value to be returned by {@link #getRealizationEpoch()}.
     */
    public static final String REALIZATION_EPOCH_PROPERTY = "realizationEpoch";

    /**
     * Key for the <code>"validArea"</code> property to be given to the
     * {@linkplain #Datum(Map) constructor}. This is used
     * for setting the value to be returned by {@link #getValidArea()}.
     */
    public static final String VALID_AREA_PROPERTY = "validArea";

    /**
     * Key for the <code>"scope"</code> property to be given to the
     * {@linkplain #Datum(Map) constructor}. This is used
     * for setting the value to be returned by {@link #getScope()}.
     */
    public static final String SCOPE_PROPERTY = "scope";
    
    /**
     * List of localizable properties. To be given to
     * {@link org.geotools.referencing.IdentifiedObject} constructor.
     */
    private static final String[] LOCALIZABLES = {ANCHOR_POINT_PROPERTY, SCOPE_PROPERTY};

    /**
     * Description, possibly including coordinates, of the point or points used to anchor the datum
     * to the Earth. Also known as the "origin", especially for Engineering and Image Datums.
     */
    private final InternationalString anchorPoint;

    /**
     * The time after which this datum definition is valid. This time may be precise
     * (e.g. 1997 for IRTF97) or merely a year (e.g. 1983 for NAD83). If the time is
     * not defined, then the value is {@link Long#MIN_VALUE}.
     */
    private final long realizationEpoch;

    /**
     * Area or region in which this datum object is valid.
     */
    private final Extent validArea;

    /**
     * Description of domain of usage, or limitations of usage, for which this
     * datum object is valid.
     */
    private final InternationalString scope;

    /**
     * Construct a datum from a set of properties. The properties given in argument follow
     * the same rules than for the {@linkplain IdentifiedObject#IdentifiedObject(Map) super-class
     * constructor}. Additionally, the following properties are understood by this construtor:
     * <br><br>
     * <table border='1'>
     *   <tr bgcolor="#CCCCFF" class="TableHeadingColor">
     *     <th nowrap>Property name</th>
     *     <th nowrap>Value type</th>
     *     <th nowrap>Value given to</th>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;{@link #ANCHOR_POINT_PROPERTY "anchorPoint"}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link InternationalString} or {@link String}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getAnchorPoint}</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;{@link #REALIZATION_EPOCH_PROPERTY "realizationEpoch"}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link Date}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getRealizationEpoch}</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;{@link #VALID_AREA_PROPERTY "validArea"}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link Extent}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getValidArea}</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;{@link #SCOPE_PROPERTY "scope"}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link InternationalString} or {@link String}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getScope}</td>
     *   </tr>
     * </table>
     */
    public Datum(final Map properties) {
        this(properties, new HashMap());
    }

    /**
     * Work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private Datum(final Map properties, final Map subProperties) {
        super(properties, subProperties, LOCALIZABLES);
        final Date realizationEpoch;
        anchorPoint      = (InternationalString) subProperties.get(ANCHOR_POINT_PROPERTY     );
        realizationEpoch = (Date)                subProperties.get(REALIZATION_EPOCH_PROPERTY);
        validArea        = (Extent)              subProperties.get(VALID_AREA_PROPERTY       );
        scope            = (InternationalString) subProperties.get(SCOPE_PROPERTY            );
        this.realizationEpoch = (realizationEpoch != null) ?
                                 realizationEpoch.getTime() : Long.MIN_VALUE;
    }

    /**
     * Description, possibly including coordinates, of the point or points used to anchor the datum
     * to the Earth. Also known as the "origin", especially for Engineering and Image Datums.
     *
     * <ul>
     *   <li>For a geodetic datum, this point is also known as the fundamental point, which is
     *       traditionally the point where the relationship between geoid and ellipsoid is defined.
     *       In some cases, the "fundamental point" may consist of a number of points. In those
     *       cases, the parameters defining the geoid/ellipsoid relationship have then been averaged
     *       for these points, and the averages adopted as the datum definition.</li>
     *
     *   <li>For an engineering datum, the anchor point may be a physical point, or it may be a
     *       point with defined coordinates in another CRS.</li>
     *
     *   <li>For an image datum, the anchor point is usually either the centre of the image or the
     *       corner of the image.</li>
     *
     *   <li>For a temporal datum, this attribute is not defined. Instead of the anchor point,
     *       a temporal datum carries a separate time origin of type {@link Date}.</li>
     * </ul>
     */
    public InternationalString getAnchorPoint() {
        return anchorPoint;
    }

    /**
     * The time after which this datum definition is valid. This time may be precise (e.g. 1997
     * for IRTF97) or merely a year (e.g. 1983 for NAD83). In the latter case, the epoch usually
     * refers to the year in which a major recalculation of the geodetic control network, underlying
     * the datum, was executed or initiated. An old datum can remain valid after a new datum is
     * defined. Alternatively, a datum may be superseded by a later datum, in which case the
     * realization epoch for the new datum defines the upper limit for the validity of the
     * superseded datum.
     */
    public Date getRealizationEpoch() {
        return (realizationEpoch!=Long.MIN_VALUE) ? new Date(realizationEpoch) : null;
    }

    /**
     * Area or region in which this datum object is valid.
     */
    public Extent getValidArea() {
        return validArea;
    }

    /**
     * Description of domain of usage, or limitations of usage, for which this
     * datum object is valid.
     */
    public InternationalString getScope() {
        return scope;
    }
    
    /**
     * Gets the type of the datum as an enumerated code. Datum type was provided
     * for all kind of datum in the legacy OGC 01-009 specification. In the new
     * OGC 03-73 (ISO 19111) specification, datum type is provided only for
     * vertical datum. Nevertheless, we keep this method around since it is
     * needed for WKT formatting. Note that we returns the datum type ordinal
     * value, not the code list object.
     */
    int getLegacyDatumType() {
        return 0;
    }
    
    /**
     * Compares the specified object with this datum for equality.
     *
     * @param  object The object to compare to <code>this</code>.
     * @param  compareMetadata <code>true</code> for performing a strict comparaison, or
     *         <code>false</code> for comparing only properties relevant to transformations.
     * @return <code>true</code> if both objects are equal.
     */
    public boolean equals(final IdentifiedObject object, final boolean compareMetadata) {
        if (super.equals(object, compareMetadata)) {
            if (!compareMetadata) {
                return true;
            }
            final Datum that = (Datum) object;
            return this.realizationEpoch == that.realizationEpoch &&
                   Utilities.equals(this.validArea,   that.validArea      ) &&
                   Utilities.equals(this.anchorPoint, that.anchorPoint    ) &&
                   Utilities.equals(this.scope,       that.scope);
        }
        return false;
    }
    
    /**
     * Format the inner part of a
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite> (WKT)</A> element.
     *
     * Note: All subclasses will override this method, but only
     *       {@link org.geotools.referencing.datum.GeodeticDatum} will <strong>not</strong>
     *       invokes this parent method, because horizontal datum do not write the datum type.
     *
     * @param  formatter The formatter to use.
     * @return The WKT element name.
     */
    protected String formatWKT(final Formatter formatter) {
        formatter.append(getLegacyDatumType());
        return super.formatWKT(formatter);
    }
}
