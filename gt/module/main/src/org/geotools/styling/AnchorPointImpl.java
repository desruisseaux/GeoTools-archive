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
 */
package org.geotools.styling;

// OpenGIS dependencies
import org.geotools.filter.Expression;
import org.geotools.resources.Utilities;
import org.opengis.util.Cloneable;


/**
 * @version $Id: AnchorPointImpl.java,v 1.7 2003/09/06 04:52:31 seangeo Exp $
 * @author Ian Turton, CCG
 */
public class AnchorPointImpl implements AnchorPoint, Cloneable {
    /**
     * The logger for the default core module.
     */
    private static final java.util.logging.Logger LOGGER = 
            java.util.logging.Logger.getLogger("org.geotools.core");
    private static final org.geotools.filter.FilterFactory filterFactory = 
            org.geotools.filter.FilterFactory.createFilterFactory();
    private Expression anchorPointX = null;
    private Expression anchorPointY = null;

    /** Creates a new instance of DefaultAnchorPoint */
    public AnchorPointImpl() {
        try {
            anchorPointX = filterFactory.createLiteralExpression(
                                   new Double(0.0));
            anchorPointY = filterFactory.createLiteralExpression(
                                   new Double(0.5));
        } catch (org.geotools.filter.IllegalFilterException ife) {
            LOGGER.severe("Failed to build defaultAnchorPoint: " + ife);
        }
    }

    /** Getter for property anchorPointX.
     * @return Value of property anchorPointX.
     */
    public Expression getAnchorPointX() {
        return anchorPointX;
    }

    /** Setter for property anchorPointX.
     * @param anchorPointX New value of property anchorPointX.
     */
    public void setAnchorPointX(Expression anchorPointX) {
        this.anchorPointX = anchorPointX;
    }

    /** Getter for property anchorPointY.
     * @return Value of property anchorPointY.
     */
    public Expression getAnchorPointY() {
        return anchorPointY;
    }

    /** Setter for property anchorPointY.
     * @param anchorPointY New value of property anchorPointY.
     */
    public void setAnchorPointY(Expression anchorPointY) {
        this.anchorPointY = anchorPointY;
    }
    
    public void accept(StyleVisitor visitor){
        visitor.visit(this);
    }
    
    /* (non-Javadoc)
     * @see Cloneable#clone()
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Never happen");
        }
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (obj instanceof AnchorPointImpl) {
            AnchorPointImpl other = (AnchorPointImpl) obj;
            return Utilities.equals(this.anchorPointX, other.anchorPointX) &&
                   Utilities.equals(this.anchorPointY, other.anchorPointY);
        }
        
        return false;     
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        final int PRIME = 37;
        int result = 17;
        
        if (anchorPointX != null) {
            result = result * PRIME + anchorPointX.hashCode(); 
        }
        
        if (anchorPointY != null) {
            result = result * PRIME + anchorPointY.hashCode();
        }
        
        return result;
    }

}