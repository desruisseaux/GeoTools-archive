/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.renderer.style;

// J2SE dependencies
import java.awt.BasicStroke;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.font.GlyphVector;

import org.geotools.resources.Utilities;


/**
 * Style used to represent labels over lines, polygons and points
 * 
 *
 * @author Andrea Aime
 * @author dblasby
 * @version $Id$
 */

/** DJB:
	 * 
	 *  This class was fundamentally wrong - it tried to convert <LinePlacement> into <PointPlacement>. 
	 *   Not only was it doing a really crappy job, but its fundamentally the wrong place to do it.
	 * 
	 *   The SLD spec defines a <PointPlacement> as:
	 * <xsd:sequence>
	 *    <xsd:element ref="sld:AnchorPoint" minOccurs="0"/>
	 *    <xsd:element ref="sld:Displacement" minOccurs="0"/>
	 *    <xsd:element ref="sld:Rotation" minOccurs="0"/>
	 * </xsd:sequence>
	 * 
	 *  and <LinePlacement> as:
	 * <xsd:sequence>
	 *  <xsd:element ref="sld:PerpendicularOffset "minOccurs="0"/>
	 * </xsd:sequence>
	 * 
	 *   its annotated as:
	 * A "PerpendicularOffset" gives the perpendicular distance away from a line to draw a label.
     * which is a bit vague, but there's a little more details here:
     * 
     * The PerpendicularOffset element of a LinePlacement gives the perpendicular distance away from a line to draw a label.   ...
     * The distance is in pixels and is positive to the left-hand.
     * 
     *  Left hand/right hand for perpendicularOffset is just crap - I'm assuming them mean +ive --> "up" and -ive --> "down".
     *  See the actual label code for how it deals with this.
     * 
     *  I've removed all the absoluteLineDisplacement stuff and replaced it with
     *     isPointPlacement() (true) --> render normally (PointPlacement Attributes)
     *     isPointPlacement() (false) --> render LinePlacement 
     * 
     *   This replaces the old behavior which converted a LinePlacement -> pointplacement and set the absoluteLineDisplacement flag!
	 * 
	 * */
	 
public class TextStyle2D extends Style2D {
    GlyphVector textGlyphVector;
    Shape haloShape;
    String label;
    Font font;
    double rotation;
      /** yes = <PointPlacement> no = <LinePlacement>  default = yes**/
    boolean pointPlacement = true;
    int     perpendicularOffset =0; // only valid when using a LinePlacement
    double anchorX;
    double anchorY;
    double displacementX;
    double displacementY;
    Paint haloFill;
    Composite haloComposite;
    float haloRadius;

    /** Holds value of property fill. */
    private Paint fill;

    /** Holds value of property composite. */
    private Composite composite;

    /**
     * @return
     */
    public double getAnchorX() {
        return anchorX;
    }

    /**
     * @return
     */
    public double getAnchorY() {
        return anchorY;
    }

    /**
     * @return
     */
    public Font getFont() {
        return font;
    }

    /**
     * @return
     */
    public Composite getHaloComposite() {
        return haloComposite;
    }

    /**
     * @return
     */
    public Paint getHaloFill() {
        return haloFill;
    }

    /**
     * @return
     */
    public float getHaloRadius() {
        return haloRadius;
    }

    /**
     * @return
     */
    public double getRotation() {
        return rotation;
    }

    /**
     * @return
     */
    public GlyphVector getTextGlyphVector(Graphics2D graphics) {
        if (textGlyphVector == null) {
            textGlyphVector = font.createGlyphVector(graphics.getFontRenderContext(), label);
        }

        return textGlyphVector;
    }

    /**
     * @return
     */
    public Shape getHaloShape(Graphics2D graphics) {
        if (haloShape == null) {
            GlyphVector gv = getTextGlyphVector(graphics);
            haloShape = new BasicStroke(2f * haloRadius, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND).createStrokedShape(gv.getOutline());
        }
        return haloShape;
    }

    /**
     * @param i
     */
    public void setAnchorX(double f) {
        anchorX = f;
    }

    /**
     * @param i
     */
    public void setAnchorY(double f) {
        anchorY = f;
    }

    /**
     * @param font
     */
    public void setFont(Font font) {
        this.font = font;
    }

    /**
     * @param composite
     */
    public void setHaloComposite(Composite composite) {
        haloComposite = composite;
    }

    /**
     * @param paint
     */
    public void setHaloFill(Paint paint) {
        haloFill = paint;
    }

    /**
     * @param f
     */
    public void setHaloRadius(float f) {
        haloRadius = f;
    }

    /**
     * @param f
     */
    public void setRotation(double f) {
        rotation = f;
    }

    /**
     * @return Returns the label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label The label to set.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return Returns the pointPlacement (true => point placement, false => line placement)
     */
    public boolean isPointPlacement() {
        return pointPlacement;
    }

    /**
     * @param pointPlacement (true => point placement, false => line placement.)
     */
    public void setPointPlacement(boolean pointPlacement) {
        this.pointPlacement = pointPlacement;
    }

    /**
     * @return Returns the displacementX.
     */
    public double getDisplacementX() {
        return displacementX;
    }

    /**
     * @param displacementX The displacementX to set.
     */
    public void setDisplacementX(double displacementX) {
        this.displacementX = displacementX;
    }

    /**
     * @return Returns the displacementY.
     */
    public double getDisplacementY() {
        return displacementY;
    }

    /**
     * @param displacementY The displacementY to set.
     */
    public void setDisplacementY(double displacementY) {
        this.displacementY = displacementY;
    }

    /**
     * Getter for property fill.
     *
     * @return Value of property fill.
     */
    public Paint getFill() {
        return this.fill;
    }

    /**
     * Setter for property fill.
     *
     * @param fill New value of property fill.
     */
    public void setFill(Paint fill) {
        this.fill = fill;
    }
    
    /**
     *  only valid for a isPointPlacement=false (ie. a lineplacement)
     * @param displace in pixels
     */
    public void setPerpendicularOffset(int displace)
    {
    	perpendicularOffset = displace;
    }
    
    /**
     * only valid for a isPointPlacement=false (ie. a lineplacement)
     * @return displacement in pixels
     */
    public int getPerpendicularOffset()
    {
    	return perpendicularOffset;
    }
	
	

    /**
     * Getter for property composite.
     *
     * @return Value of property composite.
     */
    public Composite getComposite() {
        return this.composite;
    }

    /**
     * Setter for property composite.
     *
     * @param composite New value of property composite.
     */
    public void setComposite(Composite composite) {
        this.composite = composite;
    }

    /**
     * Returns a string representation of this style.
     */
    public String toString() {
        return Utilities.getShortClassName(this) + "[\"" + label + "\"]";
    }
}
