/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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

import java.util.Map;
import org.opengis.filter.expression.Expression;


/**
 * A symbolizer describes how a feature should appear on a map.
 *
 * <p>
 * A symbolizer is obtained by specifying one of a small number of different
 * types of symbolizer and then supplying parameters to override its default
 * behaviour.
 * </p>
 *
 * <p>
 * The text symbolizer describes how to display text labels and the like.
 * </p>
 *
 * <p>
 * The details of this object are taken from the <a
 * href="https://portal.opengeospatial.org/files/?artifact_id=1188"> OGC
 * Styled-Layer Descriptor Report (OGC 02-070) version 1.0.0.</a>:
 * <pre><code>
 * &lt;xsd:element name="TextSymbolizer" substitutionGroup="sld:Symbolizer">
 *   &lt;xsd:annotation>
 *     &lt;xsd:documentation>
 *       A "TextSymbolizer" is used to render text labels according to
 *       various graphical parameters.
 *     &lt;/xsd:documentation>
 *   &lt;/xsd:annotation>
 *   &lt;xsd:complexType>
 *     &lt;xsd:complexContent>
 *       &lt;xsd:extension base="sld:SymbolizerType">
 *         &lt;xsd:sequence>
 *           &lt;xsd:element ref="sld:Geometry" minOccurs="0"/>
 *           &lt;xsd:element ref="sld:Label" minOccurs="0"/>
 *           &lt;xsd:element ref="sld:Font" minOccurs="0"/>
 *           &lt;xsd:element ref="sld:LabelPlacement" minOccurs="0"/>
 *           &lt;xsd:element ref="sld:Halo" minOccurs="0"/>
 *           &lt;xsd:element ref="sld:Fill" minOccurs="0"/>
 *         &lt;/xsd:sequence>
 *       &lt;/xsd:extension>
 *     &lt;/xsd:complexContent>
 *   &lt;/xsd:complexType>
 * &lt;/xsd:element>
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
public interface TextSymbolizer extends Symbolizer {
    /**
     * Returns the expression that will be evaluated to determine what text is
     * displayed.
     *
     * @return DOCUMENT ME!
     */
    Expression getLabel();

    /**
     * Sets the expression that will be evaluated to determine what text is
     * displayed. See {@link #getLabel} for details.
     *
     * @param label DOCUMENT ME!
     */
    void setLabel(Expression label);

    /**
     * Returns a device independent Font object that is to be used to render
     * the label.
     *
     * @return DOCUMENT ME!
     */
    Font[] getFonts();

    /**
     * sets a list of device independent Font objects to be used to render the
     * label.
     *
     * @param fonts DOCUMENT ME!
     */
    void setFonts(Font[] fonts);

    /**
     * A LabelPlacement specifies how a text element should be rendered
     * relative to its geometric point or line.
     *
     * @return DOCUMENT ME!
     *
     * @since Geotools 2.2 (GeoAPI 2.0)
     */
    LabelPlacement getPlacement();

    /**
     * A LabelPlacement specifies how a text element should be rendered
     * relative to its geometric point or line.
     *
     * @return DOCUMENT ME!
     *
     * @deprecated use getPlacement()
     */
    LabelPlacement getLabelPlacement();

    /**
     * A LabelPlacement specifies how a text element should be rendered
     * relative to its geometric point or line.
     *
     * @param labelPlacement DOCUMENT ME!
     *
     * @deprecated use setPlacement(LabelPlacement)
     */
    void setLabelPlacement(LabelPlacement labelPlacement);

    /**
     * A LabelPlacement specifies how a text element should be rendered
     * relative to its geometric point or line.
     *
     * @param labelPlacement DOCUMENT ME!
     */
    void setPlacement(LabelPlacement labelPlacement);

    /**
     * A halo fills an extended area outside the glyphs of a rendered text
     * label to make the label easier to read over a background.
     *
     * @return DOCUMENT ME!
     */
    Halo getHalo();

    /**
     * A halo fills an extended area outside the glyphs of a rendered text
     * label to make the label easier to read over a background.
     *
     * @param halo DOCUMENT ME!
     */
    void setHalo(Halo halo);

    /**
     * Returns the object that indicates how the text will be filled.
     *
     * @return DOCUMENT ME!
     */
    Fill getFill();

    /**
     * Sets the object that indicates how the text will be filled. See {@link
     * #getFill} for details.
     *
     * @param fill DOCUMENT ME!
     */
    void setFill(Fill fill);

    /**
     * This property defines the geometry to be used for styling.<br>
     * The property is optional and if it is absent (null) then the "default"
     * geometry property of the feature should be used.  Geometry types other
     * than inherently point types can be used.  The geometryPropertyName is
     * the name of a geometry property in the Feature being styled.
     * Typically, features only have one geometry so, in general, the need to
     * select one is not required. Note: this moves a little away from the SLD
     * spec which provides an XPath reference to a Geometry object, but does
     * follow it in spirit.
     *
     * @return The name of the attribute in the feature being styled  that
     *         should be used.  If null then the default geometry should be
     *         used.
     */
    String getGeometryPropertyName();

    /**
     * This property defines the geometry to be used for styling.<br>
     * The property is optional and if it is absent (null) then the "default"
     * geometry property of the feature should be used.  Geometry types other
     * than inherently point types can be used.  The geometryPropertyName is
     * the name of a geometry property in the Feature being styled.
     * Typically, features only have one geometry so, in general, the need to
     * select one is not required. Note: this moves a little away from the SLD
     * spec which provides an XPath reference to a Geometry object, but does
     * follow it in spirit.
     *
     * @param name The name of the attribute in the feature being styled  that
     *        should be used.  If null then the default geometry should be
     *        used.
     */
    void setGeometryPropertyName(String name);

    /**
     * Priority -- null       = use the default labeling priority Expression =
     * an expression that evaluates to a number (ie. Integer, Long, Double...)
     * Larger = more likely to be rendered
     *
     * @param e
     */
    void setPriority(Expression e);

    /**
     * Priority -- null       = use the default labeling priority Expression =
     * an expression that evaluates to a number (ie. Integer, Long, Double...)
     * Larger = more likely to be rendered
     *
     * @return DOCUMENT ME!
     */
    Expression getPriority();

    /**
     * adds a parameter value to the options map
     *
     * @param key
     * @param value
     */
    void addToOptions(String key, String value);

    /**
     * Find the value of a key in the map (may return null)
     *
     * @param key
     *
     */
    String getOption(String key);

    /**
     * return the map of option
     *
     * @return null - no options set
     */
    Map getOptions();
}
