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
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.event.EventListenerList;

import org.geotools.gui.swing.map.map2d.decoration.InformationDecoration;
import org.geotools.gui.swing.map.map2d.decoration.MapDecoration;
import org.geotools.gui.swing.map.map2d.event.Map2DContextEvent;
import org.geotools.gui.swing.map.map2d.event.Map2DMapAreaEvent;
import org.geotools.gui.swing.map.map2d.listener.Map2DListener;
import org.geotools.gui.swing.map.map2d.listener.StrategyListener;
import org.geotools.gui.swing.map.map2d.strategy.RenderingStrategy;
import org.geotools.gui.swing.map.map2d.strategy.SingleBufferedImageStrategy;
import org.geotools.map.MapContext;
import org.geotools.renderer.GTRenderer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

/**
 * Default implementation of Map2D
 * @author Johann Sorel
 */
public class JDefaultMap2D extends JPanel implements Map2D {

    /**
     * EventListenerList to manage all possible Listeners
     */
    protected final EventListenerList MAP2DLISTENERS = new EventListenerList();
    /**
     * Map2D reference , same as "this" but needed to explicitly point to the 
     * map2d object when coding a private class
     */
    protected final JDefaultMap2D THIS_MAP;
    /**
     * Rendering Strategy of the map2d widget, should never be null
     */
    protected RenderingStrategy renderingStrategy = new SingleBufferedImageStrategy();
    private static final MapDecoration[] EMPTY_OVERLAYER_ARRAY = {};
    private final InformationDecoration informationDecoration = new InformationDecoration();
    private final List<MapDecoration> userDecorations = new ArrayList<MapDecoration>();
    private final StrategyListener strategylisten = new StrategyListen();
    private final PropertyChangeListener crslisten = new CRSLsiten();
    private final JLayeredPane mapDecorationPane = new JLayeredPane();
    private final JLayeredPane userDecorationPane = new JLayeredPane();
    private final JLayeredPane mainDecorationPane = new JLayeredPane();
    private int nextMapDecorationIndex = 1;
    private MapDecoration backDecoration;

    /**
     * create a default JDefaultMap2D
     */
    public JDefaultMap2D() {
        this.THIS_MAP = this;

        setLayout(new BorderLayout());
        mapDecorationPane.setLayout(new BufferLayout());
        userDecorationPane.setLayout(new BufferLayout());
        mainDecorationPane.setLayout(new BufferLayout());

        mainDecorationPane.add(informationDecoration, new Integer(3));
        mainDecorationPane.add(userDecorationPane, new Integer(2));
        mainDecorationPane.add(mapDecorationPane, new Integer(1));

        add(BorderLayout.CENTER, mainDecorationPane);
        
        renderingStrategy.addStrategyListener(strategylisten);        
        
        mapDecorationPane.add(renderingStrategy.getComponent(), new Integer(0));
        mapDecorationPane.revalidate();


        setOpaque(false);
    }

    private void fireStrategyChanged(RenderingStrategy oldOne, RenderingStrategy newOne) {

        Map2DListener[] lst = getMap2DListeners();

        for (Map2DListener l : lst) {
            l.mapStrategyChanged(oldOne, newOne);
        }

    }

    //----------------------Use as extend for subclasses------------------------
    protected void mapAreaChanged(Map2DMapAreaEvent event) {

    }

    protected void mapContextChanged(Map2DContextEvent event) {

        if (event.getPreviousContext() != null) {
            event.getPreviousContext().removePropertyChangeListener(crslisten);
        }

        if (event.getNewContext() != null) {
            event.getNewContext().addPropertyChangeListener(crslisten);
        }

    }

    protected void crsChanged(PropertyChangeEvent arg0) {


    }

    protected void setRendering(boolean render) {
        informationDecoration.setDrawing(render);
    }

    //----------------------Over/Sub/information layers-------------------------
    public InformationDecoration getInformationLayer() {
        return informationDecoration;
    }

    public void setBackDecoration(MapDecoration back) {

        if (backDecoration != null) {
            mainDecorationPane.remove(backDecoration.geComponent());
        }
        backDecoration = back;

        if (back != null) {
            mainDecorationPane.add(backDecoration.geComponent(), new Integer(0));
        }

        mainDecorationPane.revalidate();
        mainDecorationPane.repaint();
    }

    public MapDecoration getBackDecoration() {
        return backDecoration;
    }

    public void addDecoration(MapDecoration deco) {

        if (deco != null && !userDecorations.contains(deco)) {
            deco.setMap2D(THIS_MAP);
            userDecorations.add(deco);
            userDecorationPane.add(deco.geComponent(), new Integer(userDecorations.indexOf(deco)));
            userDecorationPane.revalidate();
            userDecorationPane.repaint();
        }
    }

    public void addDecoration(int index, MapDecoration deco) {

        if (deco != null && !userDecorations.contains(deco)) {
            deco.setMap2D(THIS_MAP);
            userDecorations.add(index, deco);
            userDecorationPane.add(deco.geComponent(), new Integer(userDecorations.indexOf(deco)));
            userDecorationPane.revalidate();
            userDecorationPane.repaint();
        }
    }

    public int getDecorationIndex(MapDecoration deco) {
        return userDecorations.indexOf(deco);
    }

    public void removeDecoration(MapDecoration deco) {
        if (deco != null && userDecorations.contains(deco)) {
            deco.setMap2D(null);
            userDecorations.remove(deco);
            userDecorationPane.remove(deco.geComponent());
            userDecorationPane.revalidate();
            userDecorationPane.repaint();
        }
    }

    public MapDecoration[] getDecorations() {
        return userDecorations.toArray(EMPTY_OVERLAYER_ARRAY);
    }

    /**
     * add a MapDecoration between the map and the user MapDecoration
     * those MapDecoration can not be removed because they are important
     * for edition/selection/navigation.
     * @param deco : MapDecoration to add
     */
    protected void addMapDecoration(MapDecoration deco) {
        mapDecorationPane.add(deco.geComponent(), new Integer(nextMapDecorationIndex));
        nextMapDecorationIndex++;
    }

    //-----------------------------MAP2D----------------------------------------
    public Coordinate toMapCoord(int mx, int my) {
        Envelope mapArea = renderingStrategy.getMapArea();

        Rectangle bounds = getBounds();
        double width = mapArea.getWidth();
        double height = mapArea.getHeight();
        return toMapCoord(mx, my, width, height, bounds);
    }
    
    public Point toComponentCoord(Coordinate coord){
        
        Envelope mapArea = renderingStrategy.getMapArea();
        Rectangle bounds = getBounds();
        
        double width = mapArea.getWidth();
        double height = mapArea.getHeight();
        
        double xval = bounds.width/width;
        double yval = bounds.height/height;
        
        double minX = coord.x - mapArea.getMinX();
        double minY = coord.y - mapArea.getMinY();
        
        int x = (int)(minX*xval);
        int y = (int)(minY*yval);
        
        return new Point(x,y);
        
    }

    private Coordinate toMapCoord(double mx, double my, double width, double height, Rectangle bounds) {
        Envelope mapArea = renderingStrategy.getMapArea();

        double mapX = ((mx * width) / (double) bounds.width) + mapArea.getMinX();
        double mapY = (((bounds.getHeight() - my) * height) / (double) bounds.height) + mapArea.getMinY();
        return new Coordinate(mapX, mapY);
    }

    public void setRenderingStrategy(RenderingStrategy strategy) {

        if (strategy == null) {
            throw new NullPointerException();
        }

        RenderingStrategy oldStrategy = renderingStrategy;

        //removing old strategy
        GTRenderer ren = renderingStrategy.getRenderer();
        MapContext context = renderingStrategy.getContext();
        mapDecorationPane.remove(renderingStrategy.getComponent());        
        renderingStrategy.removeStrategyListener(strategylisten);
        
        //adding new strategy
        renderingStrategy = strategy;
        renderingStrategy.addStrategyListener(strategylisten);
        renderingStrategy.setRenderer(ren);
        renderingStrategy.setContext(context);
        
        
        mapDecorationPane.add(renderingStrategy.getComponent(), new Integer(0));
        mapDecorationPane.revalidate();

        fireStrategyChanged(oldStrategy, renderingStrategy);

    }

    public RenderingStrategy getRenderingStrategy() {
        return renderingStrategy;
    }

    public JPanel getComponent() {
        return this;
    }

    public void addMap2DListener(Map2DListener listener) {
        MAP2DLISTENERS.add(Map2DListener.class, listener);
    }

    public void removeMap2DListener(Map2DListener listener) {
        MAP2DLISTENERS.remove(Map2DListener.class, listener);
    }

    public Map2DListener[] getMap2DListeners() {
        return MAP2DLISTENERS.getListeners(Map2DListener.class);
    }

    //---------------------- PRIVATE CLASSES------------------------------------    
    private class CRSLsiten implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent arg0) {
            THIS_MAP.crsChanged(arg0);
        }
    }

    private class StrategyListen implements StrategyListener {

        public void setRendering(boolean rendering) {
            THIS_MAP.setRendering(rendering);
        }

        public void mapAreaChanged(Map2DMapAreaEvent event) {
            THIS_MAP.mapAreaChanged(event);
        }

        public void mapContextChanged(Map2DContextEvent event) {
            THIS_MAP.mapContextChanged(event);
        }
    }
}

