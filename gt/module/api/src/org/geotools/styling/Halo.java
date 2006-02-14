/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2005, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
/*
 * Geotools - OpenSource mapping toolkit
 *            (C) 2002, Center for Computational Geography
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
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
 * Contacts:
 *     UNITED KINGDOM: James Macgill.  j.macgill@geog.leeds.ac.uk
 */
package org.geotools.styling;

import org.geotools.event.GTComponent;
import org.geotools.filter.expression.Expression;


/**
 * A Halo fills an extended area outside the glyphs of a rendered textlabel to
 * make it easier to read over a background.
 * 
 * <p>
 * The details of this object are taken from the <a
 * href="https://portal.opengeospatial.org/files/?artifact_id=1188"> OGC
 * Styled-Layer Descriptor Report (OGC 02-070) version 1.0.0.</a>:
 * <pre><code>
 * &lt;xsd:element name="Halo"&gt;
 *   &lt;xsd:annotation&gt;
 *     &lt;xsd:documentation&gt;
 *      A "Halo" fills an extended area outside the glyphs of a rendered
 *       text label to make the label easier to read over a background.
 *     &lt;/xsd:documentation&gt;
 *   &lt;/xsd:annotation&gt;
 *   &lt;xsd:complexType&gt;
 *     &lt;xsd:sequence&gt;
 *       &lt;xsd:element ref="sld:Radius" minOccurs="0"/&gt;
 *       &lt;xsd:element ref="sld:Fill" minOccurs="0"/&gt;
 *     &lt;/xsd:sequence&gt;
 *   &lt;/xsd:complexType&gt;
 * &lt;/xsd:element&gt;
 * </code></pre>
 * </p>
 * 
 * <p>
 * Renderers can use this information when displaying styled features, though
 * it must be remembered that not all renderers will be able to fully
 * represent strokes as set out by this interface.  For example, opacity may
 * not be supported.
 * </p>
 * 
 * <p>
 * Notes:
 * 
 * <ul>
 * <li>
 * The graphical parameters and their values are derived from SVG/CSS2
 * standards with names and semantics which are as close as possible.
 * </li>
 * </ul>
 * </p>
 * $Id$
 *
 * @author Ian Turton, CCG
 * @source $URL$
 */
public interface Halo extends GTComponent {
    /**
     * Expression that represents the the distance the halo extends from the
     * text
     *
     * @return DOCUMENT ME!
     */
    Expression getRadius();

    /**
     * Expression that represents the the distance the halo extends from the
     * text
     *
     * @param radius DOCUMENT ME!
     */
    void setRadius(Expression radius);

    /**
     * The fill (color) of the halo
     *
     * @return DOCUMENT ME!
     */
    Fill getFill();

    /**
     * The fill (color) of the halo
     *
     * @param fill DOCUMENT ME!
     */
    void setFill(Fill fill);

    void accept(StyleVisitor visitor);
}
