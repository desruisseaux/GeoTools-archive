/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.gui.swing;

/**
 * a simple highlight manager 
 * @author Ian Turton
 */
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.event.EventListenerList;

import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.IllegalFilterException;
import org.geotools.gui.swing.event.HighlightChangeListener;
import org.geotools.gui.swing.event.HighlightChangedEvent;
import org.geotools.map.MapLayer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class HighlightManager extends MouseMotionAdapter {
    EventListenerList listeners = new EventListenerList();
    /** 
     * the layer to highlight
     */
    MapLayer highlightLayer;
    /**
     * The filterfactory needed to create the highlight filter
     */
    FilterFactory ff = FilterFactoryFinder.createFilterFactory();
    /**
     * the geometry factory to convert the mouse point to a jts.Point
     */
    GeometryFactory gf = new GeometryFactory();
    /** 
     * stores our best guess as to what the name of the geometry 
     * in the highlighted layer is.
     */
    String geomName;
    Filter lastFilter = null;
    public HighlightManager(MapLayer layer) {
        setHighlightLayer(layer);
        
    }
    /**
     * listens for mouse moved events 
     * @param e - MouseEvent
     */
    public void mouseMoved(MouseEvent e) {
        
        if (highlightLayer == null)
            return;
        Rectangle bounds = e.getComponent().getBounds();
        Envelope mapArea = ((JMapPane) e.getSource()).mapArea;
        double x = (double) (e.getX());
        double y = (double) (e.getY());
        double width = mapArea.getWidth();
        double height = mapArea.getHeight();

        double mapX = (x * width / (double) bounds.width) + mapArea.getMinX();
        double mapY = ((bounds.getHeight() - y) * height / (double) bounds.height)
                + mapArea.getMinY();
        GeometryFilter f = null;

        Geometry geometry = gf.createPoint(new Coordinate(mapX, mapY));
        try {
            f = ff.createGeometryFilter(GeometryFilter.GEOMETRY_CONTAINS);
            f.addRightGeometry(ff.createLiteralExpression(geometry));
            f.addLeftGeometry(ff.createAttributeExpression(geomName));
            if(f==lastFilter) return;
            lastFilter=f;
            this.highlightChanged(e.getSource(), f);
        } catch (IllegalFilterException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }

    }
    /**
     * add a HighlightChangedListener to this manager, not this need not
     * be a map which is why we pass a filter back not a map point.
     * @param l - the listener
     */
    public void addHighlightChangeListener(HighlightChangeListener l) {
        listeners.add(HighlightChangeListener.class, l);
    }
    /**
     * Remove a highlightlistener
     * @param l - the listener
     */
    public void removeHightlightChangeListener(HighlightChangeListener l) {
        listeners.remove(HighlightChangeListener.class, l);
    }
    /**
     * notify listeners that the highlight has changed
     * @param source - where the mousemovement came from
     * @param filter - the filter which selects the 
     * highlighted feature(s)
     */
    public void highlightChanged(Object source, Filter filter) {
        HighlightChangeListener[] l = (HighlightChangeListener[]) listeners
                .getListeners(HighlightChangeListener.class);
        HighlightChangedEvent ev = new HighlightChangedEvent(source, filter);
        for (int i = 0; i < l.length; i++) {
            l[i].highlightChanged(ev);
        }
    }
    /**
     * get the highlighted layer
     * @return - the layer
     */
    public MapLayer getHighlightLayer() {
        return highlightLayer;
    }
    /**
     * sets the highlighted layer
     * @param highlightLayer - the layer
     */
    public void setHighlightLayer(MapLayer highlightLayer) {
        this.highlightLayer = highlightLayer;
        if (this.highlightLayer != null) {
            geomName = this.highlightLayer.getFeatureSource().getSchema()
                    .getDefaultGeometry().getName();
            if (geomName== null || geomName == "") {
                geomName = "the_geom";
            }
        }
    }
}
