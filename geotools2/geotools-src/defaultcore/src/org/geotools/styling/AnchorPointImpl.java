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

// J2SE dependencies
import java.util.logging.Logger;

// Geotools dependencies
import org.geotools.filter.*;


/**
 * @version $Id: AnchorPointImpl.java,v 1.3 2002/10/24 16:54:40 ianturton Exp $
 * @author Ian Turton, CCG
 */
public class AnchorPointImpl implements AnchorPoint {

    /**
     * The logger for the default core module.
     */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.core");
    private static final FilterFactory filterFactory = FilterFactory.createFilterFactory();
    private Expression anchorPointX = null;
    private Expression anchorPointY = null;
    /** Creates a new instance of DefaultAnchorPoint */
    public AnchorPointImpl() {
        try {
            anchorPointX = filterFactory.createLiteralExpression(new Double(0.0));
            anchorPointY = filterFactory.createLiteralExpression(new Double(0.5));
        } catch (org.geotools.filter.IllegalFilterException ife){
            LOGGER.severe("Failed to build defaultAnchorPoint: " + ife);
        }
    }
    
    /** Getter for property anchorPointX.
     * @return Value of property anchorPointX.
     */
    public org.geotools.filter.Expression getAnchorPointX() {
        return anchorPointX;
    }
    
    /** Setter for property anchorPointX.
     * @param anchorPointX New value of property anchorPointX.
     */
    public void setAnchorPointX(org.geotools.filter.Expression anchorPointX) {
        this.anchorPointX = anchorPointX;
    }
    
    /** Getter for property anchorPointY.
     * @return Value of property anchorPointY.
     */
    public org.geotools.filter.Expression getAnchorPointY() {
        return anchorPointY;
    }
    
    /** Setter for property anchorPointY.
     * @param anchorPointY New value of property anchorPointY.
     */
    public void setAnchorPointY(org.geotools.filter.Expression anchorPointY) {
        this.anchorPointY = anchorPointY;
    }
    
}
