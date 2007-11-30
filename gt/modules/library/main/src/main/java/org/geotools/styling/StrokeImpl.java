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
package org.geotools.styling;

import org.geotools.event.AbstractGTComponent;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.filter.ConstantExpression;
import org.geotools.resources.Utilities;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.util.Cloneable;

// J2SE depedencies
import java.util.Arrays;


/**
 * Provides a Java representation of the Stroke object in an SLD document. A
 * stroke defines how a line is rendered.
 *
 * @author James Macgill, CCG
 * @source $URL$
 * @version $Id$
 */
public class StrokeImpl extends AbstractGTComponent implements Stroke,
    Cloneable {
    private FilterFactory filterFactory;
    private Expression color;
    private float[] dashArray;
    private Expression dashOffset;
    private Graphic fillGraphic;
    private Graphic strokeGraphic;
    private Expression lineCap;
    private Expression lineJoin;
    private Expression opacity;
    private Expression width;

    /**
     * Creates a new instance of Stroke
     */
    protected StrokeImpl() {
        this( CommonFactoryFinder.getFilterFactory(GeoTools.getDefaultHints()));
    }

    protected StrokeImpl(FilterFactory factory) {
        filterFactory = factory;
    }

    public void setFilterFactory(FilterFactory factory) {
        filterFactory = factory;
    }

    /**
     * This parameter gives the solid color that will be used for a stroke.<br>
     * The color value is RGB-encoded using two hexidecimal digits per
     * primary-color component in the order Red, Green, Blue, prefixed with
     * the hash (#) sign.  The hexidecimal digits between A and F may be in
     * either upper or lower case.  For example, full red is encoded as
     * "#ff0000"  (with no quotation marks).  The default color is defined to
     * be black ("#000000"). Note: in CSS this parameter is just called Stroke
     * and not Color.
     *
     * @return The color of the stroke encoded as a hexidecimal RGB value.
     */
    public Expression getColor() {
        return color;
    }

    /**
     * This parameter sets the solid color that will be used for a stroke.<br>
     * The color value is RGB-encoded using two hexidecimal digits per
     * primary-color component in the order Red, Green, Blue, prefixed with
     * the hash (#) sign.  The hexidecimal digits between A and F may be in
     * either upper or lower case.  For example, full red is encoded as
     * "#ff0000"  (with no quotation marks).  The default color is defined to
     * be black ("#000000"). Note: in CSS this parameter is just called Stroke
     * and not Color.
     *
     * @param color The color of the stroke encoded as a hexidecimal RGB value.
     *        This must not be null.
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public void setColor(Expression color) {
        if (this.color == color) {
            return;
        }

        Expression old = this.color;
        this.color = color;
        fireChildChanged("color", color, old);
    }

    /**
     * This parameter sets the solid color that will be used for a stroke.<br>
     * The color value is RGB-encoded using two hexidecimal digits per
     * primary-color component in the order Red, Green, Blue, prefixed with
     * the hash (#) sign.  The hexidecimal digits between A and F may be in
     * either upper or lower case.  For example, full red is encoded as
     * "#ff0000"  (with no quotation marks).  The default color is defined to
     * be black ("#000000"). Note: in CSS this parameter is just called Stroke
     * and not Color.
     *
     * @param color The color of the stroke encoded as a hexidecimal RGB value.
     */
    public void setColor(String color) {
        setColor(filterFactory.literal(color));
    }

    /**
     * This parameter encodes the dash pattern as a series of floats.<br>
     * The first number gives the length in pixels of the dash to draw, the
     * second gives the amount of space to leave, and this pattern repeats.<br>
     * If an odd number of values is given, then the pattern is expanded by
     * repeating it twice to give an even number of values. The default is to
     * draw an unbroken line.<br>
     * For example, "2 1 3 2" would produce:<br>
     * <code>--&nbsp;---&nbsp;&nbsp;--&nbsp;---&nbsp;&nbsp;--&nbsp;
     * ---&nbsp;&nbsp;--&nbsp;---&nbsp;&nbsp;--&nbsp;---&nbsp;&nbsp;--</code>
     *
     * @return The dash pattern as an array of float values in the form
     *         "dashlength gaplength ..."
     */
    public float[] getDashArray() {
        float[] ret = new float[0];

        if (dashArray != null) {
            ret = new float[dashArray.length];
            System.arraycopy(dashArray, 0, ret, 0, dashArray.length);
        }

        return ret;
    }

    /**
     * This parameter encodes the dash pattern as a series of floats.<br>
     * The first number gives the length in pixels of the dash to draw, the
     * second gives the amount of space to leave, and this pattern repeats.<br>
     * If an odd number of values is given, then the pattern is expanded by
     * repeating it twice to give an even number of values. The default is to
     * draw an unbroken line.<br>
     * 
     * For example, "2 1 3 2" would produce:<br>
     * <code>--&nbsp;---&nbsp;&nbsp;--&nbsp;---&nbsp;&nbsp;
     * --&nbsp;---&nbsp;&nbsp;--&nbsp;---&nbsp;&nbsp;--&nbsp;
     * ---&nbsp;&nbsp;--</code>
     *
     * @param dashPattern The dash pattern as an array of float values in the
     *        form "dashlength gaplength ..."
     */
    public void setDashArray(float[] dashPattern) {
        dashArray = dashPattern;
        fireChanged();
    }

    /**
     * This param determines where the dash pattern should start from.
     *
     * @return where the dash should start from.
     */
    public Expression getDashOffset() {
        return dashOffset;
    }

    /**
     * This param determines where the dash pattern should start from.
     *
     * @param dashOffset The distance into the dash pattern that should act as
     *        the start.
     */
    public void setDashOffset(Expression dashOffset) {
        if (dashOffset == null) {
            return;
        }

        Expression old = this.dashOffset;
        this.dashOffset = dashOffset;
        fireChildChanged("dashOffset", dashOffset, old);
    }

    /**
     * This parameter indicates that a stipple-fill repeated graphic will be
     * used and specifies the fill graphic to use.
     *
     * @return The graphic to use as a stipple fill. If null, then no Stipple
     *         fill should be used.
     */
    public Graphic getGraphicFill() {
        return fillGraphic;
    }

    /**
     * This parameter indicates that a stipple-fill repeated graphic will be
     * used and specifies the fill graphic to use.
     *
     * @param fillGraphic The graphic to use as a stipple fill. If null, then
     *        no Stipple fill should be used.
     */
    public void setGraphicFill(Graphic fillGraphic) {
        if (this.fillGraphic == fillGraphic) {
            return;
        }

        Graphic old = this.fillGraphic;

        this.fillGraphic = fillGraphic;
        fireChildChanged("fillGraphic", fillGraphic, old);
    }

    /**
     * This parameter indicates that a repeated-linear-graphic graphic stroke
     * type will be used and specifies the graphic to use. Proper stroking
     * with a linear graphic requires two "hot-spot" points within the space
     * of the graphic to indicate where the rendering line starts and stops.
     * In the case of raster images with no special mark-up, this line will be
     * assumed to be the middle pixel row of the image, starting from the
     * first pixel column and ending at the last pixel column.
     *
     * @return The graphic to use as a linear graphic. If null, then no graphic
     *         stroke should be used.
     */
    public Graphic getGraphicStroke() {
        return strokeGraphic;
    }

    /**
     * This parameter indicates that a repeated-linear-graphic graphic stroke
     * type will be used and specifies the graphic to use. Proper stroking
     * with a linear graphic requires two "hot-spot" points within the space
     * of the graphic to indicate where the rendering line starts and stops.
     * In the case of raster images with no special mark-up, this line will be
     * assumed to be the middle pixel row of the image, starting from the
     * first pixel column and ending at the last pixel column.
     *
     * @param strokeGraphic The graphic to use as a linear graphic. If null,
     *        then no graphic stroke should be used.
     */
    public void setGraphicStroke(Graphic strokeGraphic) {
        if (this.strokeGraphic == strokeGraphic) {
            return;
        }

        Graphic old = this.strokeGraphic;
        this.strokeGraphic = strokeGraphic;
        fireChildChanged("strokeGraphic", strokeGraphic, old);
    }

    /**
     * This parameter controls how line strings should be capped.
     *
     * @return The cap style.  This will be one of "butt", "round" and "square"
     *         There is no defined default.
     */
    public Expression getLineCap() {
        if( lineCap == null ){
            // ConstantExpression.constant("miter")
            return Stroke.DEFAULT.getLineCap();
        }
        return lineCap;
    }

    /**
     * This parameter controls how line strings should be capped.
     *
     * @param lineCap The cap style. This can be one of "butt", "round" and
     *        "square" There is no defined default.
     */
    public void setLineCap(Expression lineCap) {
        if (lineCap == null) {
            return;
        }

        Expression old = this.lineCap;
        this.lineCap = lineCap;
        fireChildChanged("lineCap", lineCap, old);
    }

    /**
     * This parameter controls how line strings should be joined together.
     *
     * @return The join style.  This will be one of "mitre", "round" and
     *         "bevel".  There is no defined default.
     */
    public Expression getLineJoin() {
        if( lineCap == null ){
            // ConstantExpression.constant("miter")
            return Stroke.DEFAULT.getLineJoin();
        }
        return lineJoin;
    }

    /**
     * This parameter controls how line strings should be joined together.
     *
     * @param lineJoin The join style.  This will be one of "mitre", "round"
     *        and "bevel". There is no defined default.
     */
    public void setLineJoin(Expression lineJoin) {
        if (lineJoin == null) {
            return;
        }

        Expression old = this.lineJoin;
        this.lineJoin = lineJoin;
        fireChildChanged("lineJoin", lineJoin, old);
    }

    /**
     * This specifies the level of translucency to use when rendering the stroke.<br>
     * The value is encoded as a floating-point value between 0.0 and 1.0 with
     * 0.0 representing totally transparent and 1.0 representing totally
     * opaque.  A linear scale of translucency is used for intermediate values.<br>
     * For example, "0.65" would represent 65% opacity. The default value is
     * 1.0 (opaque).
     *
     * @return The opacity of the stroke, where 0.0 is completely transparent
     *         and 1.0 is completely opaque.
     */
    public Expression getOpacity() {
        if( lineCap == null ){
            return Stroke.DEFAULT.getOpacity();
        }
        return opacity;
    }

    /**
     * This specifies the level of translucency to use when rendering the stroke.<br>
     * The value is encoded as a floating-point value between 0.0 and 1.0 with
     * 0.0 representing totally transparent and 1.0 representing totally
     * opaque.  A linear scale of translucency is used for intermediate values.<br>
     * For example, "0.65" would represent 65% opacity. The default value is
     * 1.0 (opaque).
     *
     * @param opacity The opacity of the stroke, where 0.0 is completely
     *        transparent and 1.0 is completely opaque.
     */
    public void setOpacity(Expression opacity) {
        if (opacity == null) {
            return;
        }

        Expression old = this.opacity;
        this.opacity = opacity;
        fireChildChanged("opacity", opacity, old);
    }

    /**
     * This parameter gives the absolute width (thickness) of a stroke in
     * pixels encoded as a float. The default is 1.0.  Fractional numbers are
     * allowed but negative numbers are not.
     *
     * @return The width of the stroke in pixels.  This may be fractional but
     *         not negative.
     */
    public Expression getWidth() {
        if( width == null ){
            return filterFactory.literal(1.0);
        }
        return width;
    }

    /**
     * This parameter sets the absolute width (thickness) of a stroke in pixels
     * encoded as a float. The default is 1.0.  Fractional numbers are allowed
     * but negative numbers are not.
     *
     * @param width The width of the stroke in pixels.  This may be fractional
     *        but not negative.
     */
    public void setWidth(Expression width) {
        Expression old = getWidth();
        this.width = width;
        fireChildChanged("width", getWidth(), old);
    }

    public String toString() {
        StringBuffer out = new StringBuffer(
                "org.geotools.styling.StrokeImpl:\n");
        out.append("\tColor " + this.color + "\n");
        out.append("\tWidth " + this.width + "\n");
        out.append("\tOpacity " + this.opacity + "\n");
        out.append("\tLineCap " + this.lineCap + "\n");
        out.append("\tLineJoin " + this.lineJoin + "\n");
        out.append("\tDash Array " + this.dashArray + "\n");
        out.append("\tDash Offset " + this.dashOffset + "\n");
        out.append("\tFill Graphic " + this.fillGraphic + "\n");
        out.append("\tStroke Graphic " + this.strokeGraphic);

        return out.toString();
    }

    public java.awt.Color getColor(SimpleFeature feature) {
        return java.awt.Color.decode((String) this.getColor().evaluate(feature));
    }

    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Clone the StrokeImpl object.
     * 
     * <p>
     * The clone is a deep copy of the original, except for the expression
     * values which are immutable.
     * </p>
     *
     * @see org.geotools.styling.Stroke#clone()
     */
    public Object clone() {
        try {
            StrokeImpl clone = (StrokeImpl) super.clone();

            if (dashArray != null) {
                clone.dashArray = new float[dashArray.length];
                System.arraycopy(dashArray, 0, clone.dashArray, 0,
                    dashArray.length);
            }

            if (fillGraphic != null) {
                clone.fillGraphic = (Graphic) ((Cloneable) fillGraphic).clone();
            }

            if (strokeGraphic != null) {
                clone.strokeGraphic = (Graphic) ((Cloneable) strokeGraphic)
                    .clone();
            }

            return clone;
        } catch (CloneNotSupportedException e) {
            // This will never happen
            throw new RuntimeException("Failed to clone StrokeImpl");
        }
    }

    public int hashCode() {
        final int PRIME = 1000003;
        int result = 0;

        if (color != null) {
            result = (PRIME * result) + color.hashCode();
        }

        if (dashOffset != null) {
            result = (PRIME * result) + dashOffset.hashCode();
        }

        if (fillGraphic != null) {
            result = (PRIME * result) + fillGraphic.hashCode();
        }

        if (strokeGraphic != null) {
            result = (PRIME * result) + strokeGraphic.hashCode();
        }

        if (lineCap != null) {
            result = (PRIME * result) + lineCap.hashCode();
        }

        if (lineJoin != null) {
            result = (PRIME * result) + lineJoin.hashCode();
        }

        if (opacity != null) {
            result = (PRIME * result) + opacity.hashCode();
        }

        if (width != null) {
            result = (PRIME * result) + width.hashCode();
        }

        if (dashArray != null) {
            result = (PRIME * result) + hashCodeDashArray(dashArray);
        }

        return result;
    }

    /*
     * Helper method to compute the hashCode of float arrays.
     */
    private int hashCodeDashArray(float[] a) {
        final int PRIME = 1000003;

        if (a == null) {
            return 0;
        }

        int result = 0;

        for (int i = 0; i < a.length; i++) {
            result = (PRIME * result) + Float.floatToIntBits(a[i]);
        }

        return result;
    }

    /**
     * Compares this stroke with another stroke for equality.
     *
     * @param oth The other StrokeImpl to compare
     *
     * @return True if this and oth are equal.
     */
    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }

        if (oth == null) {
            return false;
        }

        if (oth.getClass() != getClass()) {
            return false;
        }

        StrokeImpl other = (StrokeImpl) oth;

        // check the color first - most likely to change
        if( !Utilities.equals( getColor(), other.getColor() )){
            return false;
        }

        // check the width 
        if( !Utilities.equals( getWidth(), other.getWidth() )){
            return false;
        }

        if( !Utilities.equals( getLineCap(), other.getLineCap() )){
            return false;
        }

        if( !Utilities.equals( getLineJoin(), other.getLineJoin() )){
            return false;
        }

        if( !Utilities.equals( getOpacity(), other.getOpacity() )){
            return false;
        }

        if( !Utilities.equals( getGraphicFill(), other.getGraphicFill() )){
            return false;
        }

        if( !Utilities.equals( getGraphicStroke(), other.getGraphicStroke() )){
            return false;
        }

        if (!Arrays.equals(getDashArray(), other.getDashArray())) {
            return false;
        }

        return true;
    }
}
