package org.geotools.gui.swing;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.event.EventListenerList;

import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.IllegalFilterException;
import org.geotools.map.MapLayer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class HighlightManager extends MouseMotionAdapter {
    EventListenerList listeners = new EventListenerList();

    MapLayer highlightLayer;

    FilterFactory ff = FilterFactoryFinder.createFilterFactory();

    GeometryFactory gf = new GeometryFactory();

    String geomName;

    public HighlightManager(MapLayer layer) {
        setHighlightLayer(layer);
        
    }

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
           
            this.highlightChanged(e.getSource(), f);
        } catch (IllegalFilterException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }

    }

    public void addHighlightChangeListener(HighlightChangeListener l) {
        listeners.add(HighlightChangeListener.class, l);
    }

    public void removeHightlightChangeListener(HighlightChangeListener l) {
        listeners.remove(HighlightChangeListener.class, l);
    }

    public void highlightChanged(Object source, Filter filter) {
        HighlightChangeListener[] l = (HighlightChangeListener[]) listeners
                .getListeners(HighlightChangeListener.class);
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
            geomName = this.highlightLayer.getFeatureSource().getSchema()
                    .getDefaultGeometry().getName();
            if (geomName== null || geomName == "") {
                geomName = "the_geom";
            }
        }
    }
}
