/**
 *    Geotools - OpenSource mapping toolkit
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill j.macgill@geog.leeds.ac.uk
 *
 */
package org.geotools.styling;

// J2SE dependencies
import java.util.logging.Logger;

import org.geotools.event.AbstractGTComponent;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.resources.Utilities;
import org.opengis.util.Cloneable;


/**
 * @version $Id: FillImpl.java,v 1.10 2003/09/06 04:52:31 seangeo Exp $
 * @author James Macgill, CCG
 */
public class FillImpl extends AbstractGTComponent implements Fill, Cloneable {
    /**
     * The logger for the default core module.
     */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.core");
    private FilterFactory filterFactory;
    private Expression color = null;
    private Expression backgroundColor = null;
    private Expression opacity = null;
    private Graphic graphicFill = null;

    /** Creates a new instance of DefaultFill */
    protected FillImpl() {
    	this( FilterFactoryFinder.createFilterFactory() );
    }

    public FillImpl(FilterFactory factory) {
		filterFactory = factory;
	}
    public void setFilterFactory( FilterFactory factory ){
    	filterFactory = factory;
    }

	/**
     * This parameter gives the solid color that will be used for a Fill.<br>
     * The color value is RGB-encoded using two hexidecimal digits per
     * primary-color component, in the order Red, Green, Blue, prefixed with
     * the hash (#) sign.
     * The hexidecimal digits between A and F may be in either upper
     * or lower case.  For example, full red is encoded as "#ff0000" (with no
     * quotation marks).
     * The default color is defined to be 50% gray ("#808080").
     *
     * Note: in CSS this parameter is just called Fill and not Color.
     *
     * @return The color of the Fill encoded as a hexidecimal RGB value.
     */
    public Expression getColor() {
        return color;
    }

    /**
     * This parameter gives the solid color that will be used for a Fill.<br>
     * The color value is RGB-encoded using two hexidecimal digits per
     * primary-color component, in the order Red, Green, Blue, prefixed with
     * the hash (#) sign.
     * The hexidecimal digits between A and F may be in either upper
     * or lower case.  For example, full red is encoded as "#ff0000" (with no
     * quotation marks).
     *
     * Note: in CSS this parameter is just called Fill and not Color.
     *
     * @param rgb The color of the Fill encoded as a hexidecimal RGB value.
     */
    public void setColor(Expression rgb) {
    	if( color == rgb ) return;
    	Expression old = color;
    	color = rgb;
    	fireChildChanged( "color", rgb, old );
    }

    public void setColor(String rgb) {
    	if( color.toString() == rgb ) return;
    	
    	setColor( filterFactory.createLiteralExpression(rgb) );    	    
    }

    /**
     * This parameter gives the solid color that will be used as a background for a Fill.<br>
     * The color value is RGB-encoded using two hexidecimal digits per
     * primary-color component, in the order Red, Green, Blue, prefixed with
     * the hash (#) sign.
     * The hexidecimal digits between A and F may be in either upper
     * or lower case.  For example, full red is encoded as "#ff0000" (with no
     * quotation marks).
     * The default color is defined to be transparent.
     *
     *
     * @return The color of the Fill encoded as a hexidecimal RGB value.
     */
    public Expression getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * This parameter gives the solid color that will be used as a background for a Fill.<br>
     * The color value is RGB-encoded using two hexidecimal digits per
     * primary-color component, in the order Red, Green, Blue, prefixed with
     * the hash (#) sign.
     * The hexidecimal digits between A and F may be in either upper
     * or lower case.  For example, full red is encoded as "#ff0000" (with no
     * quotation marks).
     *
     *
     *
     * @param rgb The color of the Fill encoded as a hexidecimal RGB value.
     */
    public void setBackgroundColor(Expression rgb) {
    	if( this.backgroundColor == rgb ) return;
    	Expression old = backgroundColor;
    	backgroundColor = rgb;
    	fireChildChanged( "backgroundColor", rgb, old );    
    }

    public void setBackgroundColor(String rgb) {
        LOGGER.fine("setting bg color with " + rgb + " as a string");
    	if( backgroundColor.toString() == rgb ) return;
    	
    	setBackgroundColor( filterFactory.createLiteralExpression(rgb) );
    }

    /**
     * This specifies the level of translucency to use when rendering the fill.
     * <br>
     * The value is encoded as a floating-point value between 0.0 and 1.0
     * with 0.0 representing totally transparent and 1.0 representing totally
     * opaque, with a linear scale of translucency for intermediate values.<br>
     * For example, "0.65" would represent 65% opacity.
     * The default value is 1.0 (opaque).
     *
     * @return The opacity of the fill, where 0.0 is completely transparent
     *         and 1.0 is completely opaque.
     */
    public Expression getOpacity() {
        return opacity;
    }

    /**
     * Setter for property opacity.
     * @param opacity New value of property opacity.
     */
    public void setOpacity(Expression opacity) {
    	if( this.opacity == opacity ) return;
    	
    	Expression old = this.opacity;
    	this.opacity = opacity;
    	
    	fireChildChanged( "opacity", opacity, old );    
    }

    public void setOpacity(String opacity) {
    	if( this.opacity.toString() == opacity ) return;
    	
    	setOpacity( filterFactory.createLiteralExpression(opacity) );
    }

    /**
     * This parameter indicates that a stipple-fill repeated graphic will be
     * used and specifies the fill graphic to use.
     *
     * @return graphic The graphic to use as a stipple fill.
     *         If null then no Stipple fill should be used.
     */
    public org.geotools.styling.Graphic getGraphicFill() {
        return graphicFill;
    }

    /**
     * Setter for property graphic.
     * @param graphicFill New value of property graphic.
     */
    public void setGraphicFill(org.geotools.styling.Graphic graphicFill) {
    	if( this.graphicFill == graphicFill ) return;
    	Graphic old = this.graphicFill;
    	this.graphicFill = graphicFill;    	
    	fireChildChanged( "graphicFill", graphicFill, old );    
    }
    
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }
    
    /**
     * Returns a clone of the FillImpl.
     *  
     * @see org.geotools.styling.Fill#clone()
     */
    public Object clone() {
       try {
            FillImpl clone = (FillImpl) super.clone();
            if ( graphicFill != null ) {
                clone.graphicFill = (Graphic) ((Cloneable)graphicFill).clone();
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            // This will never happen
            throw new RuntimeException("Failed to clone FillImpl");
        }
    }

    /**
     * Generates a hashcode for the FillImpl.
     * 
     * @return The hashcode.
     */
    public int hashCode() {
        final int PRIME = 1000003;
        int result = 0;
        if (color != null) {
            result = PRIME * result + color.hashCode();
        }
        if (backgroundColor != null) {
            result = PRIME * result + backgroundColor.hashCode();
        }
        if (opacity != null) {
            result = PRIME * result + opacity.hashCode();
        }
        if (graphicFill != null) {
            result = PRIME * result + graphicFill.hashCode();
        }

        return result;
    }

    /** Compares a FillImpl with another for equality.
     * 
     *  <p>Two FillImpls are equal if they contain the same,
     *  color, backgroundcolor, opacity and graphicFill.
     *  
     *  @param oth The other FillImpl
     *  @return True if this FillImpl is equal to oth.
     */
    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }

        if (oth instanceof FillImpl) {
            FillImpl other = (FillImpl) oth;
            return Utilities.equals(this.color, other.color) &&
                   Utilities.equals(this.backgroundColor, other.backgroundColor) &&
                   Utilities.equals(this.opacity, other.opacity) &&
                   Utilities.equals(this.graphicFill, other.graphicFill);
        }

        return false;
    }
}