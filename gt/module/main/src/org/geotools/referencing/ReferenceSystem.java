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
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing;

// J2SE dependencies
import java.util.Map;
import java.util.HashMap;
import java.util.Locale;

// OpenGIS dependencies
import org.opengis.metadata.extent.Extent;


/**
 * Description of a spatial and temporal reference system used by a dataset.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ReferenceSystem extends Info implements org.opengis.referencing.ReferenceSystem {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -2017349900886658590L;

    /**
     * List of localizable properties. To be given to {@link Info} constructor.
     */
    private static final String[] LOCALIZABLES = {"scope"};

    /**
     * Area for which the (coordinate) reference system is valid.
     */
    private final Extent validArea;

    /**
     * Description of domain of usage, or limitations of usage, for which this
     * (coordinate) reference system object is valid.
     */
    private final Map scope;

    /**
     * Construct a reference system from a set of properties. The properties given in argument
     * follow the same rules than for the {@link Info} constructor. Additionally, the following
     * properties are understood by this construtor:
     *
     * <table border='1'>
     *   <tr bgcolor="#CCCCFF" class="TableHeadingColor">
     *     <th nowrap>Property name</th>
     *     <th nowrap>Value type</th>
     *     <th nowrap>Value given to</th>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;<code>"validArea"</code>&nbsp;</td>
     *     <td nowrap>&nbsp;{@link Extent}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getValidArea}</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;<code>"scope"</code>&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getScope}</td>
     *   </tr>
     * </table>
     */
    public ReferenceSystem(final Map properties) {
        this(properties, new HashMap());
    }

    /**
     * Work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private ReferenceSystem(final Map properties, final Map subProperties) {
        super(properties, subProperties, LOCALIZABLES);
        validArea = (Extent) subProperties.get("validArea");
        scope     = (Map)    subProperties.get("scope"    );
    }

    /**
     * Area for which the (coordinate) reference system is valid.
     *
     * @return Coordinate reference system valid area, or <code>null</code> if not available.
     */
    public Extent getValidArea() {
        return validArea;
    }

    /**
     * Description of domain of usage, or limitations of usage, for which this
     * (coordinate) reference system object is valid.
     *
     * @param  locale The desired locale for the scope to be returned,
     *         or <code>null</code> for a non-localized string.
     * @return The remarks, or <code>null</code> if not available.
     */
    public String getScope(final Locale locale) {
        return Identifier.getLocalized(scope, locale);
    }

    /**
     * Compare this reference system with the specified object for equality.
     * If <code>compareMetadata</code> is <code>true</code>, then all available properties are
     * compared including {@linkplain #getValidArea valid area} and {@linkplain #getScope scope}.
     *
     * @param  object The object to compare to <code>this</code>.
     * @param  compareMetadata <code>true</code> for performing a strict comparaison, or
     *         <code>false</code> for comparing only properties relevant to transformations.
     * @return <code>true</code> if both objects are equal.
     */
    public boolean equals(final Info object, final boolean compareMetadata) {
        if (super.equals(object, compareMetadata)) {
            if (!compareMetadata) {
                return true;
            }
            final ReferenceSystem that = (ReferenceSystem) object;
            return equals(validArea, that.validArea) &&
                   equals(scope,     that.scope    );
        }
        return false;
    }
}
