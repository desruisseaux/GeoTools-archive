/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2002, Center for Computational Geography
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
 * Contacts:
 *     UNITED KINGDOM: James Macgill.  j.macgill@geog.leeds.ac.uk
 */
package org.geotools.styling;

import org.geotools.event.GTComponent;
import org.geotools.event.GTConstant;
import org.geotools.filter.ConstantExpression;
import org.geotools.filter.Expression;
import java.awt.Color;


/**
 * The Fill object encapsulates the graphical-symbolization parameters for
 * areas of geometries.
 *
 * <p>
 * There are two types of fill: solid-color and repeated graphic fill.
 * </p>
 *
 * <p>
 * The details of this object are taken from the <a
 * href="https://portal.opengeospatial.org/files/?artifact_id=1188"> OGC
 * Styled-Layer Descriptor Report (OGC 02-070) version 1.0.0.</a>:
 * <pre><code>
 * &lt;xsd:element name="Fill"&gt;
 *   &lt;xsd:annotation&gt;
 *     &lt;xsd:documentation&gt;
 *       A "Fill" specifies the pattern for filling an area geometry.
 *       The allowed CssParameters are: "fill" (color) and "fill-opacity".
 *     &lt;/xsd:documentation&gt;
 *   &lt;/xsd:annotation&gt;
 *   &lt;xsd:complexType&gt;
 *     &lt;xsd:sequence&gt;
 *       &lt;xsd:element ref="sld:GraphicFill" minOccurs="0"/&gt;
 *       &lt;xsd:element ref="sld:CssParameter" minOccurs="0"
 *                    maxOccurs="unbounded"/&gt;
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
 *
 * @author James Macgill, CCG
 * @source $URL$
 * @version $Id$
 */
public interface Fill extends GTComponent {
    static final Fill DEFAULT = new ConstantFill() {
            final Expression COLOR = ConstantExpression.constant(new Color(
                        128, 128, 128));
            final Expression BGCOLOR = ConstantExpression.constant(new Color(
                        255, 255, 255, 0));
            final Expression OPACITY = ConstantExpression.ONE;

            public Expression getColor() {
                return COLOR;
            }

            public Expression getBackgroundColor() {
                return BGCOLOR;
            }

            public Expression getOpacity() {
                return OPACITY;
            }

            public Graphic getGraphicFill() {
                return Graphic.NULL;
            }
        };

    static final Fill NULL = new ConstantFill() {
            public Expression getColor() {
                return ConstantExpression.NULL;
            }

            public Expression getBackgroundColor() {
                return ConstantExpression.NULL;
            }

            public Expression getOpacity() {
                return ConstantExpression.NULL;
            }

            public Graphic getGraphicFill() {
                return Graphic.NULL;
            }
        };

    /**
     * This parameter gives the solid color that will be used for a Fill.<br>
     * The color value is RGB-encoded using two hexidecimal digits per
     * primary-color component, in the order Red, Green, Blue, prefixed with
     * the hash (#) sign.  The hexidecimal digits beetween A and F may be in
     * either upper or lower case.  For example, full red is encoded as
     * "#ff0000" (with no quotation marks).  The default color is defined to
     * be 50% gray ("#808080"). Note: in CSS this parameter is just called
     * Fill and not Color.
     *
     * @return The color of the Fill encoded as a hexidecimal RGB value.
     */
    Expression getColor();

    /**
     * This parameter gives the solid color that will be used for a Fill.<br>
     * The color value is RGB-encoded using two hexidecimal digits per
     * primary-color component, in the order Red, Green, Blue, prefixed with
     * the hash (#) sign.  The hexidecimal digits beetween A and F may be in
     * either upper or lower case.  For example, full red is encoded as
     * "#ff0000" (with no quotation marks).
     *
     * @param color solid color that will be used for a Fill
     */
    void setColor(Expression color);

    /**
     * This parameter gives the solid color that will be used as a background
     * for a Fill.<br>
     * The color value is RGB-encoded using two hexidecimal digits per
     * primary-color component, in the order Red, Green, Blue, prefixed with
     * the hash (#) sign.  The hexidecimal digits beetween A and F may be in
     * either upper or lower case.  For example, full red is encoded as
     * "#ff0000" (with no quotation marks).  The default color is defined to
     * be transparent.
     *
     * @return The background color of the Fill encoded as a hexidecimal RGB
     *         value.
     */
    Expression getBackgroundColor();

    /**
     * This parameter gives the solid color that will be used as a background
     * for a Fill.<br>
     * The color value is RGB-encoded using two hexidecimal digits per
     * primary-color component, in the order Red, Green, Blue, prefixed with
     * the hash (#) sign.  The hexidecimal digits beetween A and F may be in
     * either upper or lower case.  For example, full red is encoded as
     * "#ff0000" (with no quotation marks).
     *
     * @param backgroundColor solid color that will be used as a background
     */
    void setBackgroundColor(Expression backgroundColor);

    /**
     * This specifies the level of translucency to use when rendering the fill. <br>
     * The value is encoded as a floating-point value between 0.0 and 1.0 with
     * 0.0 representing totally transparent and 1.0 representing totally
     * opaque, with a linear scale of translucency for intermediate values.<br>
     * For example, "0.65" would represent 65% opacity.  The default value is
     * 1.0 (opaque).
     *
     * @return The opacity of the fill, where 0.0 is completely transparent and
     *         1.0 is completely opaque.
     */
    Expression getOpacity();

    /**
     * This specifies the level of translucency to use when rendering the fill. <br>
     * The value is encoded as a floating-point value between 0.0 and 1.0 with
     * 0.0 representing totally transparent and 1.0 representing totally
     * opaque, with a linear scale of translucency for intermediate values.<br>
     * For example, "0.65" would represent 65% opacity.
     *
     * @param opacity DOCUMENT ME!
     */
    void setOpacity(Expression opacity);

    /**
     * This parameter indicates that a stipple-fill repeated graphic will be
     * used and specifies the fill graphic to use.
     *
     * @return The graphic to use as a stipple fill.  If null then no stipple
     *         fill should be used.
     */
    Graphic getGraphicFill();

    /**
     * This parameter indicates that a stipple-fill repeated graphic will be
     * used and specifies the fill graphic to use.
     *
     * @param graphicFill DOCUMENT ME!
     */
    void setGraphicFill(Graphic graphicFill);

    void accept(StyleVisitor visitor);
}


abstract class ConstantFill extends GTConstant implements Fill {
    private void cannotModifyConstant() {
        throw new UnsupportedOperationException(
            "Constant Fill may not be modified");
    }

    public void setColor(Expression color) {
        cannotModifyConstant();
    }

    public void setBackgroundColor(Expression backgroundColor) {
        cannotModifyConstant();
    }

    public void setOpacity(Expression opacity) {
        cannotModifyConstant();
    }

    public void setGraphicFill(Graphic graphicFill) {
        cannotModifyConstant();
    }

    public void accept(StyleVisitor visitor) {
        cannotModifyConstant();
    }
}
;
