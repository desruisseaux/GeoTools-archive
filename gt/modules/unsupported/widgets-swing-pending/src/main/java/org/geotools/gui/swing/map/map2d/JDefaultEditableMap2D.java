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

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JComponent;
import org.geotools.gui.swing.map.MapConstants.ACTION_STATE;
import org.geotools.gui.swing.map.map2d.decoration.MapDecoration;
import org.geotools.gui.swing.map.map2d.event.Map2DContextEvent;
import org.geotools.gui.swing.map.map2d.event.Map2DEditLayerEvent;
import org.geotools.gui.swing.map.map2d.event.Map2DMapAreaEvent;
import org.geotools.gui.swing.map.map2d.handler.DefaultEditionHandler;
import org.geotools.gui.swing.map.map2d.handler.EditionHandler;
import org.geotools.gui.swing.map.map2d.listener.EditableMap2DListener;
import org.geotools.gui.swing.map.map2d.strategy.RenderingStrategy;
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
    protected Color editionStyleColor = Color.RED;
    private EditionHandler editionHandler = new DefaultEditionHandler();

    /**
     * create a default JDefaultEditableMap2D
     */
    public JDefaultEditableMap2D() {
        super();

        mapLayerListlistener = new MapLayerListListen();
        addMapDecoration(memoryPane);

        // memory strategy------------------------------------------------------
        memoryStrategy.setAutoRefreshEnabled(false);
        memoryStrategy.setContext(memoryMapContext);

        editionHandler.install(this);
    }

    public Style createPointStyle() {
        Fill fill = STYLE_BUILDER.createFill(editionStyleColor, 1f);
        Stroke stroke = STYLE_BUILDER.createStroke(editionStyleColor, 1);
        stroke.setOpacity(STYLE_BUILDER.literalExpression(1f));

        Mark mark = STYLE_BUILDER.createMark("cross", fill, stroke);
        Graphic gra = STYLE_BUILDER.createGraphic();
        gra.setOpacity(STYLE_BUILDER.literalExpression(1f));
        gra.setMarks(new Mark[]{mark});
        gra.setSize(STYLE_BUILDER.literalExpression(14));

        PointSymbolizer ps = STYLE_BUILDER.createPointSymbolizer(gra);

        Style pointSelectionStyle = STYLE_BUILDER.createStyle();
        pointSelectionStyle.addFeatureTypeStyle(STYLE_BUILDER.createFeatureTypeStyle(ps));

        return pointSelectionStyle;
    }

    public Style createStyle() {
        Fill fill = STYLE_BUILDER.createFill(editionStyleColor, 0.4f);
        Stroke stroke = STYLE_BUILDER.createStroke(editionStyleColor, 2);
        stroke.setOpacity(STYLE_BUILDER.literalExpression(0.6f));

        PolygonSymbolizer pls = STYLE_BUILDER.createPolygonSymbolizer(stroke, fill);
        LineSymbolizer ls = STYLE_BUILDER.createLineSymbolizer(stroke);

        Rule r2 = STYLE_BUILDER.createRule(new Symbolizer[]{ls});
        r2.setFilter(new GeometryClassFilter(LineString.class, MultiLineString.class));
        Rule r3 = STYLE_BUILDER.createRule(new Symbolizer[]{pls});
        r3.setFilter(new GeometryClassFilter(Polygon.class, MultiPolygon.class));

        Style editionStyle = STYLE_BUILDER.createStyle();
        editionStyle.addFeatureTypeStyle(STYLE_BUILDER.createFeatureTypeStyle(null, new Rule[]{r2, r3}));

        return editionStyle;
    }

    
    private void adjusteContexts() {
        MapContext context = getRenderingStrategy().getContext();

        if (context != null && context.getCoordinateReferenceSystem() != null) {

            if (!context.getCoordinateReferenceSystem().equals(memoryMapContext.getCoordinateReferenceSystem())) {
                try {
                    memoryMapContext.setCoordinateReferenceSystem(context.getCoordinateReferenceSystem());
                } catch (TransformException ex) {
                    ex.printStackTrace();
                } catch (FactoryException ex) {
                    ex.printStackTrace();
                }
            }
        }

        if (context != null && context.getAreaOfInterest() != null) {
            memoryMapContext.setAreaOfInterest(context.getAreaOfInterest());
        }
    }
    
    private void fireEditLayerChanged(MapLayer oldone, MapLayer newone) {
        Map2DEditLayerEvent mce = new Map2DEditLayerEvent(this, oldone, newone);

        EditableMap2DListener[] lst = getEditableMap2DListeners();

        for (EditableMap2DListener l : lst) {
            l.mapEditLayerChanged(mce);
        }

    }

    private void fireHandlerChanged(EditionHandler handler) {

        EditableMap2DListener[] lst = getEditableMap2DListeners();

        for (EditableMap2DListener l : lst) {
            l.editionHandlerChanged(handler);
        }
    }

    //---------------------MAP2D OVERLOAD---------------------------------------
    @Override
    public void setActionState(ACTION_STATE state) {

        if (state == ACTION_STATE.EDIT) {
            editionHandler.installListeners(this);
        } else {
            editionHandler.uninstallListeners();
        }

        super.setActionState(state);
    }

    @Override
    protected void setRendering(boolean render) {
        super.setRendering(render);
        memoryStrategy.refresh();
    }

    @Override
    protected void mapAreaChanged(Map2DMapAreaEvent event) {
        super.mapAreaChanged(event);
    }

    @Override
    protected void mapContextChanged(Map2DContextEvent event) {

        if (event.getPreviousContext() != null) {
            event.getPreviousContext().removeMapLayerListListener(mapLayerListlistener);
        }

        if (event.getNewContext() != null) {
            event.getNewContext().addMapLayerListListener(mapLayerListlistener);
        }

        super.mapContextChanged(event);
    }

    @Override
    public void setRenderingStrategy(RenderingStrategy stratege) {
        super.setRenderingStrategy(stratege);
    }


    //--------------------EDITABLE MAP2D----------------------------------------
    public void setEditionHandler(EditionHandler handler) {
        if (handler == null) {
            throw new NullPointerException();
        } else if (handler != editionHandler) {

            if (editionHandler.isInstalled()) {
                editionHandler.uninstallListeners();                
                editionHandler.uninstall();
            }

            editionHandler = handler;

            editionHandler.install(this);

            if (actionState == ACTION_STATE.EDIT) {
                editionHandler.installListeners(this);
            }



            fireHandlerChanged(editionHandler);
        }
    }

    public EditionHandler getEditionHandler() {
        return editionHandler;
    }

    public void setEditedMapLayer(MapLayer layer) {

        if (editionLayer != layer) {
            fireEditLayerChanged(editionLayer, layer);
            editionLayer = layer;
        }

    }

    public MapLayer getEditedMapLayer() {
        return editionLayer;
    }


    public void addEditableMap2DListener(EditableMap2DListener listener) {
        MAP2DLISTENERS.add(EditableMap2DListener.class, listener);
    }

    public void removeEditableMap2DListener(EditableMap2DListener listener) {
        MAP2DLISTENERS.remove(EditableMap2DListener.class, listener);
    }

    public EditableMap2DListener[] getEditableMap2DListeners() {
        return MAP2DLISTENERS.getListeners(EditableMap2DListener.class);
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

    public void setMemoryLayers(MapLayer[] layers) {
        memoryMapContext.clearLayerList();
        memoryMapContext.addLayers(layers);        
        adjusteContexts();
    }

    public void repaintMemoryDecoration() {
        adjusteContexts();
        memoryStrategy.refresh();
    }

}

