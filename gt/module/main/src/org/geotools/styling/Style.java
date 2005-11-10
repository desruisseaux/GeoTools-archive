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

/**
 * Indicates how geographical content should be displayed!
 * 
 * @version $Id: Style.java,v 1.12 2003/10/10 18:31:28 ianschneider Exp $
 * @author James Macgill
 */
public interface Style {
    
	/** Style name (machine readable, don't show to users) */
    String getName();
    void setName(String name);
    
    /* Style Title (human readable name for user interfaces) */ 
    String getTitle();
    void setTitle(String title);
    
    /** Description of this style */
    String getAbstract();
    void setAbstract(String abstractStr);
    
    /**
     * Indicates that tis is the default style.
     */
    boolean isDefault();
    /**
     * Indicates that this is the default style.
     * <p>
     * Assume this is kept for GeoServer enabling a WMS to track
     * which style is considered the default. May consider providing a
     * clientProperties mechanism similar to Swing JComponent allowing
     * applications to mark up the Style content for custom uses.
     * </p>
     * @param isDefault
     */
    void setDefault(boolean isDefault);
    
    /**
     * Array of FeatureTypeStyles in portrayal order.
     * <p>
     * FeatureTypeStyle entries are rendered in order of appearance in this
     * list.
     * </p>
     * <p>
     * <i>Note: We are using a Array here to continue with Java 1.4 deployment.</i>
     * </p>
     */
    FeatureTypeStyle[] getFeatureTypeStyles();    
    void setFeatureTypeStyles(FeatureTypeStyle[] types);    
    void addFeatureTypeStyle(FeatureTypeStyle type);
    
    /**
     * Used to navigate Style information during portrayal.
     * 
     * @StyleVisitor for an example 
     * @param visitor
     */
    void accept(StyleVisitor visitor);    
    
}