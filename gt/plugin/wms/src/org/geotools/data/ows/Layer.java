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

import java.util.HashMap;
import java.util.List;
import java.util.Set;


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
    private Set srs;

    /**
     * A HashMap representings the bounding boxes on each layer. The Key is the
     * CRS (or SRS) of the bounding box. The Value is the BoundingBox object
     * itself.
     */
    private HashMap boundingBoxes;

    /**
     * A boundingbox containing the minimum rectangle of the map data in
     * EPSG:4326
     */
    private LatLonBoundingBox latLonBoundingBox;

    /** A list of type String */
    private List styles;
    private boolean queryable = false;
    private Layer parent;
    
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
     * Returns every BoundingBox contained within this layer. The
     * <code>HashMap</code> returned has each bounding box's CRS/SRS value as
     * the key, and the value is the <code>BoundingBox</code> object itself.
     *
     * @return a HashMap of all of this layer's bounding boxes.
     */
    public HashMap getBoundingBoxes() {
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

    public Set getSrs() {
        return srs;
    }

    public void setSrs(Set srs) {
        this.srs = srs;
    }

    public List getStyles() {
        return styles;
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

    public boolean isQueryable() {
        return queryable;
    }

    public void setQueryable(boolean queryable) {
        this.queryable = queryable;
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

    public LatLonBoundingBox getLatLonBoundingBox() {
        return latLonBoundingBox;
    }

    public void setLatLonBoundingBox(LatLonBoundingBox latLonBoundingBox) {
        this.latLonBoundingBox = latLonBoundingBox;
    }
}
