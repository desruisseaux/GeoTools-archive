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

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JComponent;

import org.geotools.gui.swing.map.map2d.decoration.MapDecoration;
import org.geotools.gui.swing.map.map2d.event.Map2DContextEvent;
import org.geotools.gui.swing.map.map2d.event.Map2DMapAreaEvent;
import org.geotools.gui.swing.map.map2d.handler.DefaultEditionHandler;
import org.geotools.gui.swing.map.map2d.handler.EditionHandler;
import org.geotools.gui.swing.map.map2d.listener.Map2DEditionListener;
import org.geotools.gui.swing.map.map2d.strategy.SingleBufferedImageStrategy;
import org.geotools.gui.swing.misc.GeometryClassFilter;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.Symbolizer;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import org.geotools.gui.swing.map.map2d.event.Map2DEditionEvent;

/**
 * Default implementation of EditableMap2D
 * @author Johann Sorel
 */
public class JDefaultEditableMap2D extends JDefaultSelectableMap2D implements EditableMap2D {

    private final MapLayerListListener mapLayerListlistener;
    private final SingleBufferedImageStrategy memoryStrategy = new SingleBufferedImageStrategy();
    protected final MapContext memoryMapContext = new DefaultMapContext(DefaultGeographicCRS.WGS84);
    private final BufferComponent memoryPane = new BufferComponent();
    private MapLayer editionLayer = null;
    private EditionHandler editionHandler = new DefaultEditionHandler();
    
    private PointSymbolizer pointSymbol = null;
    private LineSymbolizer lineSymbol = null;
    private PolygonSymbolizer polygonSymbol = null;

    /**
     * create a default JDefaultEditableMap2D
     */
    public JDefaultEditableMap2D() {
        super();

        initSymbols();
        
        mapLayerListlistener = new MapLayerListListen();
        addMapDecoration(memoryPane);

        // memory strategy------------------------------------------------------
        memoryStrategy.setAutoRefreshEnabled(false);
        memoryStrategy.setContext(memoryMapContext);

        editionHandler.install(this);
    }
    
    private void initSymbols(){
        Color editionStyleColor = Color.RED;
        
        Fill fill = STYLE_BUILDER.createFill(editionStyleColor, 1f);
        Stroke stroke = STYLE_BUILDER.createStroke(editionStyleColor, 1);
        stroke.setOpacity(STYLE_BUILDER.literalExpression(1f));

        Mark mark = STYLE_BUILDER.createMark("cross", fill, stroke);
        Graphic gra = STYLE_BUILDER.createGraphic();
        gra.setOpacity(STYLE_BUILDER.literalExpression(1f));
        gra.setMarks(new Mark[]{mark});
        gra.setSize(STYLE_BUILDER.literalExpression(14));
       
        pointSymbol = STYLE_BUILDER.createPointSymbolizer(gra);
                
        fill = STYLE_BUILDER.createFill(editionStyleColor, 0.4f);
        stroke = STYLE_BUILDER.createStroke(editionStyleColor, 2);
        stroke.setOpacity(STYLE_BUILDER.literalExpression(0.6f));

        polygonSymbol = STYLE_BUILDER.createPolygonSymbolizer(stroke, fill);
        lineSymbol = STYLE_BUILDER.createLineSymbolizer(stroke);
        
    }


    private void adjusteContexts() {
        MapContext context = renderingStrategy.getContext();

        if (!context.getCoordinateReferenceSystem().equals(memoryMapContext.getCoordinateReferenceSystem())) {
            try {
                memoryMapContext.setCoordinateReferenceSystem(context.getCoordinateReferenceSystem());
            } catch (TransformException ex) {
                ex.printStackTrace();
            } catch (FactoryException ex) {
                ex.printStackTrace();
            }
        }

        memoryStrategy.setMapArea(renderingStrategy.getMapArea());
        memoryStrategy.refresh();
    }

    private void fireEditLayerChanged(MapLayer oldone, MapLayer newone) {
        Map2DEditionEvent mce = new Map2DEditionEvent(this, oldone, newone,editionHandler);

        Map2DEditionListener[] lst = getEditableMap2DListeners();

        for (Map2DEditionListener l : lst) {
            l.editedLayerChanged(mce);
        }

    }

    private void fireHandlerChanged(EditionHandler oldhandler, EditionHandler newhandler) {
        Map2DEditionEvent mce = new Map2DEditionEvent(this, editionLayer,oldhandler,newhandler);
        
        Map2DEditionListener[] lst = getEditableMap2DListeners();

        for (Map2DEditionListener l : lst) {
            l.editionHandlerChanged(mce);
        }
    }

    //---------------------MAP2D OVERLOAD---------------------------------------
    @Override
    public void setActionState(ACTION_STATE state) {

        if (state == ACTION_STATE.EDIT) {
            editionHandler.installListeners(this);
        } else {
            editionHandler.cancelEdition();
            editionHandler.uninstallListeners();
        }

        super.setActionState(state);
    }

    @Override
    protected void setRendering(boolean render) {
        super.setRendering(render);
        adjusteContexts();
    }

    @Override
    protected void mapAreaChanged(Map2DMapAreaEvent event) {
        super.mapAreaChanged(event);
        adjusteContexts();
    }

    @Override
    protected void mapContextChanged(Map2DContextEvent event) {
        super.mapContextChanged(event);
        
        event.getPreviousContext().removeMapLayerListListener(mapLayerListlistener);
        event.getNewContext().addMapLayerListListener(mapLayerListlistener);
    }


    //--------------------EDITABLE MAP2D----------------------------------------
    
    public void setPointSymbolizer(PointSymbolizer symbol) {
        if(symbol == null){
            throw new NullPointerException("symbol can't be null");
        }        
        pointSymbol = symbol;
    }

    public void setLineSymbolizer(LineSymbolizer symbol) {
        if(symbol == null){
            throw new NullPointerException("symbol can't be null");
        }        
        lineSymbol = symbol;
    }

    public void setPolygonSymbolizer(PolygonSymbolizer symbol) {
        if(symbol == null){
            throw new NullPointerException("symbol can't be null");
        }        
        polygonSymbol = symbol;
    }

    public LineSymbolizer getLineSymbolizer() {
        return lineSymbol;
    }

    public PointSymbolizer getPointSymbolizer() {
        return pointSymbol;
    }
    
    public PolygonSymbolizer getPolygonSymbolizer() {
        return polygonSymbol;
    }
        
    public void setEditionHandler(EditionHandler newHandler) {
        if (newHandler == null) {
            throw new NullPointerException();
        } else if (newHandler != editionHandler) {

            editionHandler.cancelEdition();
            editionHandler.uninstallListeners();
            editionHandler.uninstall();

            EditionHandler oldHandler = editionHandler;
            editionHandler = newHandler;

            editionHandler.install(this);

            if (actionState == ACTION_STATE.EDIT) {
                editionHandler.installListeners(this);
            }

            fireHandlerChanged(oldHandler,newHandler);
        }
    }

    public EditionHandler getEditionHandler() {
        return editionHandler;
    }

    public void setEditedMapLayer(MapLayer layer) {

        if (editionLayer != layer) {
            editionHandler.cancelEdition();

            fireEditLayerChanged(editionLayer, layer);
            editionLayer = layer;
        }

    }

    public MapLayer getEditedMapLayer() {
        return editionLayer;
    }

    public void setMemoryLayers(MapLayer[] layers) {
        memoryMapContext.clearLayerList();
        memoryMapContext.addLayers(layers);
        adjusteContexts();
    }

    public void repaintMemoryDecoration() {
        adjusteContexts();
    }

    public void addEditableMap2DListener(Map2DEditionListener listener) {
        MAP2DLISTENERS.add(Map2DEditionListener.class, listener);
    }

    public void removeEditableMap2DListener(Map2DEditionListener listener) {
        MAP2DLISTENERS.remove(Map2DEditionListener.class, listener);
    }

    public Map2DEditionListener[] getEditableMap2DListeners() {
        return MAP2DLISTENERS.getListeners(Map2DEditionListener.class);
    }

    //---------------------PRIVATE CLASSES--------------------------------------
    private class MapLayerListListen implements MapLayerListListener {

        public void layerRemoved(MapLayerListEvent event) {

            if (editionLayer == event.getLayer()) {
                setEditedMapLayer(null);
            }
        }

        public void layerChanged(MapLayerListEvent event) {
        }

        public void layerMoved(MapLayerListEvent event) {
        }

        public void layerAdded(MapLayerListEvent event) {
        }
    }

    private class BufferComponent extends JComponent implements MapDecoration {

        public BufferComponent() {
            setLayout(new BorderLayout());
            add(memoryStrategy.getComponent());
        }

        public void refresh() {
        }

        public JComponent geComponent() {
            return this;
        }

        public void setMap2D(Map2D map) {

        }

        public Map2D getMap2D() {
            return null;
        }
    }

    
}

