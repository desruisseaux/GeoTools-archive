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
package org.geotools.data.ows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


/**
 * Nested list of zero or more map Layers offered by this server. It contains
 * only fields for information that we currently find interesting. Feel free
 * to add your own.
 *
 * @author rgould
 */
public class Layer implements Comparable {
    /** A machine-readable (typically one word) identifier */
    private String name;

    /** The title is for informative display to a human. */
    private String title;

    /** A set of Strings representing SRSs */
    private Set srs = null;

    /**
     * A HashMap representings the bounding boxes on each layer. The Key is the
     * CRS (or SRS) of the bounding box. The Value is the BoundingBox object
     * itself.
     */
    private HashMap boundingBoxes = null;

    /**
     * A boundingbox containing the minimum rectangle of the map data in
     * EPSG:4326
     */
    private LatLonBoundingBox latLonBoundingBox = null;

    /** A list of type String */
    private List styles;
    private Boolean queryable = null;
    private Layer parent;
    private Layer[] children;
    
    public Layer() {
        
    }

    /**
     * DOCUMENT ME!
     *
     * @param title
     */
    public Layer(String title) {
        this.title = title;
    }

    /**
     * Returns every BoundingBox associated with this layer. The
     * <code>HashMap</code> returned has each bounding box's CRS/SRS value as
     * the key, and the value is the <code>BoundingBox</code> object itself.
     *
     * Implements inheritance: if this layer's bounding box is null, query ancestors until
     * the first bounding box is found or no more ancestors
     * 
     * @return a HashMap of all of this layer's bounding boxes or null if no
     * bounding boxes found
     */
    public HashMap getBoundingBoxes() {
       if (boundingBoxes == null) {
          Layer parent = this.getParent();
          while (parent != null) {
             HashMap bb = parent.getBoundingBoxes();
             if (bb != null)
                return bb;
             else 
                parent = parent.getParent();
          }
       } 
       // May return null. But that is OK since spec says 0 or more may be specified 
        return boundingBoxes;
    }

    /**
     * Sets this layer's bounding boxes. The HashMap must have each
     * BoundingBox's  CRS/SRS value as its key, and the
     * <code>BoundingBox</code> object as its value.
     *
     * @param boundingBoxes a HashMap containing bounding boxes
     */
    public void setBoundingBoxes(HashMap boundingBoxes) {
        this.boundingBoxes = boundingBoxes;
    }

    /**
     * Gets the name of the <code>Layer</code>. It is designed to be machine
     * readable, and if it is present, this layer is determined to be drawable
     * and is a valid candidate for use in a GetMap or GetFeatureInfo request.
     *
     * @return the machine-readable name of the layer
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this layer. Giving the layer name indicates that it
     * can be drawn during a GetMap or GetFeatureInfo request.
     *
     * @param name the layer's new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Accumulates all of the srs/crs specified for this layer and all srs/crs inherited from
     * its ancestors. No duplicates are returned.
     * 
     * @return Set of all srs/crs for this layer and its ancestors
     */
    public Set getSrs() {
       TreeSet allSrs = new TreeSet();
       // Get my ancestor's srs/crs
       Layer parent = this.getParent();
       if (parent != null) {
          Set parentSrs = parent.getSrs();
          if (parentSrs != null)  //got something, add to accumulation
             allSrs.addAll(parentSrs);
       }
       // Now add my srs/crs, if any. Set collection intrinsically prevents duplicates
       allSrs.addAll(srs);
       
       // May return an empty list, but spec says at least one must be specified. Perhaps, need
       // to check and throw exception if set is empty. I'm leaving that out for now since 
       // it changes the method signature and would potentially break existing users of this class
       return allSrs;
    }

    public void setSrs(Set srs) {
        this.srs = srs;
    }

    /**
     * Accumulates all of the styles specified for this layer and all styles inherited from
     * its ancestors. No duplicates are returned.
     * 
     * @return List of all styles for this layer and its ancestors
     */
    public List getStyles() {
       ArrayList allStyles = new ArrayList();
       // Get my ancestor's styles
       Layer parent = this.getParent();
       if (parent != null) {
          List parentStyles = parent.getStyles();
          if (parentStyles != null)  //got something, add to accumulation
             allStyles.addAll(parentStyles);
       }
       // Now add my styles, if any
       // Brute force check for duplicates. The spec says duplicates are not allowed:
       // (para 7.1.4.5.4) "A child shall not redefine a Style with the same Name as one 
       // inherited from a parent. A child may define a new Style with a new Name that is 
       // not available for the parent Layer."
       if ((styles != null) && !styles.isEmpty()) {
          for (Iterator iter = styles.iterator(); iter.hasNext();) {
             Object style = iter.next();
            if (!allStyles.contains(style))
                allStyles.add(style);
          }
       }
       
       // May return an empty list, but that is OK since spec says 0 or more styles may be specified 
       return allStyles;
    }

    public void setStyles(List styles) {
        this.styles = styles;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Determines if this layer is queryable. Implements inheritance: if this layer's 
     * Queryable attribute is null, check ancestors until the first Queryable attribute is found 
     * or no more ancestors. If a Queryable attribute is not found for this layer, it will return
     * the default value of false.
     * 
     * @return true is this layer is Queryable
     */
    public boolean isQueryable() {
       if (queryable == null) {
          Layer parent = this.getParent();
          while (parent != null) {
             Boolean q = parent.getQueryable();
             if (q != null)
                return q.booleanValue();
             else 
                parent = parent.getParent();
          }
          // At this point a attribute was not found so return default 
          return false;
       } 
        return queryable.booleanValue();
    }
    
    private Boolean getQueryable() {
       return queryable;
    }

    public void setQueryable(boolean queryable) {
        this.queryable = new Boolean(queryable);
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object arg0) {
        Layer layer = (Layer) arg0;

        if ((this.getName() != null) && (layer.getName() != null)) {
            return this.getName().compareTo(layer.getName());
        }

        return this.getTitle().compareTo(layer.getTitle());
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the parent.
     */
    public Layer getParent() {
        return parent;
    }

    /**
     * DOCUMENT ME!
     *
     * @param parent The parent to set.
     */
    public void setParent(Layer parent) {
        this.parent = parent;
    }

    /**
     * Returns the LatLonBoundingBox for this layer. Implements inheritance: if this layer's 
     * bounding box is null, query ancestors until the first bounding box is found 
     * or no more ancestors.
     * 
     * @return the LatLonBoundingBox for this layer or null if no lat/lon bounding box is found
     */
    public LatLonBoundingBox getLatLonBoundingBox() {
       if (latLonBoundingBox == null) {
          Layer parent = this.getParent();
          while (parent != null) {
             LatLonBoundingBox llbb = parent.getLatLonBoundingBox();
             if (llbb != null)
                return llbb;
             else 
                parent = parent.getParent();
          }
          // We should never get to falling out of the while loop w/o a LatLonBoundingBox
          // being found. The WMS spec says one is required. So perhaps if we don't find one,
          // then throw an exception. I'm leaving that out for now since it changes the method signature
          // and would potentially break existing users of this class
       } 
       // May return null! 
       return latLonBoundingBox;
    }

    public void setLatLonBoundingBox(LatLonBoundingBox latLonBoundingBox) {
        this.latLonBoundingBox = latLonBoundingBox;
    }
    public Layer[] getChildren() {
        return children;
    }
    public void setChildren( Layer[] children ) {
        this.children = children;
    }
}
