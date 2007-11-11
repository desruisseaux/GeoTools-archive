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

package org.geotools.gui.swing.map.map2d;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import javax.swing.event.EventListenerList;
import org.geotools.filter.IllegalFilterException;
import org.geotools.gui.swing.map.map2d.event.HighlightChangeListener;
import org.geotools.gui.swing.map.map2d.event.HighlightChangedEvent;
import org.geotools.map.MapLayer;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

/**
 * default highlight manager
 * @author Ian Turton,Johann Sorel
 */
public class DefaultHighlightManager implements HighlightManager{
    
    private EventListenerList listeners = new EventListenerList();
    private MapLayer highlightLayer;
    private String geomName;

    private FilterFactory2 ff = (FilterFactory2) org.geotools.factory.CommonFactoryFinder.getFilterFactory(null);
    private GeometryFactory gf = new GeometryFactory();
    
    private Filter lastFilter = null;

    public DefaultHighlightManager(MapLayer layer){
        setHighlightLayer(layer);
    }

    public void mouseDragged(MouseEvent e) {}
    
    public void mouseMoved(MouseEvent e) {
        if (highlightLayer == null) {
            return;
        }

        Rectangle bounds = e.getComponent().getBounds();
        Map2D pane = (Map2D) e.getSource();
        Envelope mapArea = pane.getMapArea();
        double x = (double) (e.getX());
        double y = (double) (e.getY());
        double width = mapArea.getWidth();
        double height = mapArea.getHeight();

        double mapX = ((x * width) / (double) bounds.width) + mapArea.getMinX();
        double mapY = (((bounds.getHeight() - y) * height) / (double) bounds.height) + mapArea.getMinY();
        Filter f = null;

        Geometry geometry = gf.createPoint(new Coordinate(mapX, mapY));

        try {
        	Filter bb = ff.bbox(ff.property(geomName),mapArea.getMinX(),mapArea.getMinY(),
        			mapArea.getMaxX(),mapArea.getMaxY(),pane.getContext().getCoordinateReferenceSystem().toString());
            f = ff.contains(ff.property(geomName), ff.literal(geometry));
            f = ff.and(bb,f);
            if (f == lastFilter) {
                return;
            }

            lastFilter = f;
            this.fireHighlightChanged(e.getSource(), f);
        } catch (IllegalFilterException ex) {
            ex.printStackTrace();
        }
    }

    public void addHighlightChangeListener(HighlightChangeListener l) {
        listeners.add(HighlightChangeListener.class, l);
    }

    public void removeHightlightChangeListener(HighlightChangeListener l) {
        listeners.remove(HighlightChangeListener.class, l);
    }

    public HighlightChangeListener[] getHighlightChangeListeners() {
        return listeners.getListeners(  HighlightChangeListener.class );
    }
    
    public void fireHighlightChanged(Object source, Filter filter) {
        HighlightChangeListener[] l = (HighlightChangeListener[]) listeners.getListeners(HighlightChangeListener.class);
        HighlightChangedEvent ev = new HighlightChangedEvent(source, filter);

        for (int i = 0; i < l.length; i++) {
            l[i].highlightChanged(ev);
        }
    }

    public MapLayer getHighlightLayer() {
        return highlightLayer;
    }

    public void setHighlightLayer(MapLayer highlightLayer) {
        this.highlightLayer = highlightLayer;

        if (this.highlightLayer != null) {
            geomName = this.highlightLayer.getFeatureSource().getSchema().getDefaultGeometry()
                                          .getLocalName();

            if ((geomName == null) || (geomName == "")) {
                geomName = "the_geom";
            }
        }        
    }

    
    
    
}
