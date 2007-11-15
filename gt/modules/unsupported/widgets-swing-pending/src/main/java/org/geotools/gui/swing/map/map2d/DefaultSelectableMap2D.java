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
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.MouseInputListener;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.IllegalFilterException;
import org.geotools.gui.swing.map.map2d.overLayer.SelectionOverLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.shape.ShapefileRenderer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.spatial.BinarySpatialOperator;

/**
 *
 * @author Johann Sorel
 */
public class DefaultSelectableMap2D extends DefaultNavigableMap2D implements SelectableMap2D {

    private final MouseInputListener mouseInputListener;
    private final MapLayerListListener mapLayerListlistener;
    private final SelectionOverLayer selectionPane = new SelectionOverLayer();
    private final FilterFactory2 ff = (FilterFactory2) CommonFactoryFinder.getFilterFactory(null);     
    private final GeometryFactory gf = new GeometryFactory();
    private final List<MapLayer> selectableLayers = new ArrayList<MapLayer>();
    
    public DefaultSelectableMap2D() {
        this(new ShapefileRenderer());
    }

    public DefaultSelectableMap2D(GTRenderer renderer) {
        super(renderer);
        mouseInputListener = new MouseListen();
        mapLayerListlistener = new MapLayerListListen();
        addMouseListener(mouseInputListener);
        addMouseMotionListener(mouseInputListener);
        layerPane.add(selectionPane,new Integer(NEXT_OVER_LAYER_INDEX));
        NEXT_OVER_LAYER_INDEX++;
    }

    private Coordinate toMapCoord(double mx, double my, double width, double height, Rectangle bounds){
        double mapX = ((mx * width) / (double) bounds.width) + mapArea.getMinX();
        double mapY = (((bounds.getHeight() - my) * height) / (double) bounds.height) + mapArea.getMinY();
        return new Coordinate(mapX,mapY);
    }
    
    private void doMouseSelection(double mx, double my) {
        
        Rectangle bounds = getBounds();
        double width = mapArea.getWidth();
        double height = mapArea.getHeight();
        Coordinate[] coord = new Coordinate[5];
        
        int taille = 3;   
        
        coord[0] = toMapCoord( mx-taille, my-taille, width, height, bounds);
        coord[1] = toMapCoord( mx-taille, my+taille, width, height, bounds);
        coord[2] = toMapCoord( mx+taille, my+taille, width, height, bounds);
        coord[3] = toMapCoord( mx+taille, my-taille, width, height, bounds);
        coord[4] = coord[0];
        
        LinearRing lr1 = gf.createLinearRing(coord);
        LinearRing[] lr2 = {lr1};
        Geometry geometry = gf.createPolygon(lr1, lr2);
        
        findFeature(geometry);
    }
    
    
    private void doMouseSelection(double mx, double my, double ex, double ey) {
        
        Rectangle bounds = getBounds();
        double width = mapArea.getWidth();
        double height = mapArea.getHeight();
        Coordinate[] coord = new Coordinate[5];
        
        coord[0] = toMapCoord( mx, my, width, height, bounds);
        coord[1] = toMapCoord( mx, ey, width, height, bounds);
        coord[2] = toMapCoord( ex, ey, width, height, bounds);
        coord[3] = toMapCoord( ex, my, width, height, bounds);
        coord[4] = coord[0];
        
        LinearRing lr1 = gf.createLinearRing(coord);
        LinearRing[] lr2 = {lr1};
        Geometry geometry = gf.createPolygon(lr1, lr2);
        
        findFeature(geometry);
    }
    
    
    //-------------------OVERLOAD MAP2D-----------------------------------------
    
    @Override
    public void setContext(MapContext context) {
        if(this.context != null){
            this.context.removeMapLayerListListener(mapLayerListlistener);
        }
        
        if(context != null){
            context.addMapLayerListListener(mapLayerListlistener);
        }
        
        super.setContext(context);        
    }
    
    //----------------------SELECTABLE MAP2D------------------------------------

    public void addSelectableLayer(MapLayer layer) {
        if (layer != null) {
            selectableLayers.add(layer);
        }
    }

    public void addSelectableLayer(MapLayer[] layers) {
        if (layers != null) {
            for(MapLayer layer : layers){
                selectableLayers.add(layer);
            }
        }
    }

    public void removeSelectableLayer(MapLayer layer){
        selectableLayers.remove(layer);
    }
    
    public MapLayer[] getSelectableLayer() {
        return selectableLayers.toArray(new MapLayer[selectableLayers.size()]);
    }
    
    public boolean isLayerSelectable(MapLayer layer){
        return selectableLayers.contains(layer);
    }

    public void doSelection(double x, double y) {
        
        Geometry geometry = gf.createPoint(new Coordinate(x, y));
        // org.opengis.geometry.Geometry geometry = new Point();
        findFeature(geometry);
    }

    private void findFeature(Geometry geometry){
        BinarySpatialOperator f = null;

        if ((context == null) || (selectableLayers.size() == 0)) {
            return;
        }

        try {

            for (MapLayer layer : selectableLayers) {
                String name = layer.getFeatureSource().getSchema().getDefaultGeometry().getLocalName();

                if (name == "") {
                    name = "the_geom";
                }

                try {
                    f = ff.intersects(ff.property(name), ff.literal(geometry));
                    FeatureCollection col = layer.getFeatureSource().getFeatures(f);
                    FeatureIterator fi = col.features();
                    
                    while(fi.hasNext()){
                        SimpleFeature sf = fi.next();
                        
                        for(Object obj : sf.getAttributes()){
                            System.out.print(obj);
                        }
                        System.out.println("");
                                
                    }
                    fi.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }



        } catch (IllegalFilterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return;
    }
        
    
    
    //---------------------PRIVATE CLASSES--------------------------------------
    
    private class MouseListen implements MouseInputListener {

        int startX = 0;
        int startY = 0;
        int lastX = 0;
        int lastY = 0;
        
        private void drawRectangle(boolean view,boolean fill) {
        int left = Math.min(startX, lastX);
        int right = Math.max(startX, lastX);
        int top = Math.max(startY, lastY);
        int bottom = Math.min(startY, lastY);
        int width = right - left;
        int height = top - bottom;
        selectionPane.setFill(fill);
        selectionPane.setCoord(left, bottom, width, height, view);
        //graphics.drawRect(left, bottom, width, height);
    }
        
        
        
    public void mouseClicked(MouseEvent e) {

        switch (actionState) {
            case SELECT:
                doMouseSelection(e.getX(), e.getY());
                return;
        }


    }

    public void mousePressed(MouseEvent e) {
        startX = e.getX();
        startY = e.getY();
        lastX = 0;
        lastY = 0;        
        
    }

    public void mouseReleased(MouseEvent e) {
        lastX = e.getX();
        lastY =  e.getY();
        
        switch (actionState) {
            case SELECT:
                drawRectangle(false, true);
                doMouseSelection(startX, startY, lastX, lastY);
                return;
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
        lastX = e.getX();
        lastY =  e.getY();
        
        switch (actionState) {
            case SELECT:
                drawRectangle(true, true);
                return;
        }
        
    }

    public void mouseMoved(MouseEvent e) {
    }
}
    
    private class MapLayerListListen implements MapLayerListListener{

        public void layerAdded(MapLayerListEvent event) {
        }

        public void layerRemoved(MapLayerListEvent event) {
            removeSelectableLayer(event.getLayer());
        }

        public void layerChanged(MapLayerListEvent event) {
        }

        public void layerMoved(MapLayerListEvent event) {
        }
        
    }
    
    
}



