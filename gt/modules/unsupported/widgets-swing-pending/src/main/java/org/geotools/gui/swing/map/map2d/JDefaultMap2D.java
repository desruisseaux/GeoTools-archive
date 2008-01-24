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

import org.geotools.gui.swing.map.map2d.strategy.SingleVolatileImageStrategy;
import org.geotools.gui.swing.map.map2d.strategy.RenderingStrategy;
import java.awt.BorderLayout;
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
import org.geotools.map.MapContext;
import org.geotools.renderer.GTRenderer;

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
    protected final Map2D THIS_MAP;
    /**
     * Rendering Strategy of the map2d widget, should never be null
     */
    protected RenderingStrategy renderingStrategy;
    
    
    private static final MapDecoration[] EMPTY_OVERLAYER_ARRAY = {};
    private final InformationDecoration informationDecoration = new InformationDecoration();
    private final List<MapDecoration> userDecorations = new ArrayList<MapDecoration>();
    private final StrategyListener strategylisten = new StrategyListen();
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
        setRenderingStrategy(new SingleVolatileImageStrategy());


        setOpaque(false);
    }
    
    

        

    private void fireStrategyChanged(RenderingStrategy oldOne, RenderingStrategy newOne) {
        
        Map2DListener[] lst = getMap2DListeners();

        for (Map2DListener l : lst) {
            l.mapStrategyChanged(oldOne, newOne);
        }

    }

   

    //----------------------Over/Sub/information layers-------------------------
    /**
     * get the top InformationDecoration of the map2d widget
     * @return InformationDecoration
     */
    public InformationDecoration getInformationLayer() {
        return informationDecoration;
    }

    /**
     * set the decoration behind the map
     * @param back : MapDecoration, use null to remove the decoration
     */
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

    /**
     * get the decoration behind the map
     * @return MapDecoration : or null if no back decoration
     */
    public MapDecoration getBackDecoration() {
        return backDecoration;
    }

    /**
     * add a Decoration between the map and the information top decoration
     * @param deco : MapDecoration to add
     */
    public void addDecoration(MapDecoration deco) {

        if (deco != null && !userDecorations.contains(deco)) {
            deco.setMap2D(THIS_MAP);
            userDecorations.add(deco);
            userDecorationPane.add(deco.geComponent(), new Integer(userDecorations.indexOf(deco)));
            userDecorationPane.revalidate();
            userDecorationPane.repaint();
        }
    }

    /**
     * insert a MapDecoration at a specific index
     * @param index : index where to isert the decoration
     * @param deco : MapDecoration to add
     */
    public void addDecoration(int index, MapDecoration deco) {

        if (deco != null && !userDecorations.contains(deco)) {
            deco.setMap2D(THIS_MAP);
            userDecorations.add(index, deco);
            userDecorationPane.add(deco.geComponent(), new Integer(userDecorations.indexOf(deco)));
            userDecorationPane.revalidate();
            userDecorationPane.repaint();
        }
    }

    /**
     * get the index of a MapDecoration
     * @param deco : MapDecoration to find
     * @return index of the MapDecoration
     * @throw ClassCastException or NullPointerException
     */
    public int getDecorationIndex(MapDecoration deco) {
        return userDecorations.indexOf(deco);
    }

    /**
     * remove a MapDecoration
     * @param deco : MapDecoration to remove
     */
    public void removeDecoration(MapDecoration deco) {
        if (deco != null && userDecorations.contains(deco)) {
            deco.setMap2D(null);
            userDecorations.remove(deco);
            userDecorationPane.remove(deco.geComponent());
            userDecorationPane.revalidate();
            userDecorationPane.repaint();
        }
    }

    /**
     * get an array of all MapDecoration
     * @return array of MapDecoration
     */
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
    
    public void setRenderingStrategy(RenderingStrategy stratege) {

        if (stratege == null) {
            throw new NullPointerException();
        }

        if (stratege != null) {

            fireStrategyChanged(renderingStrategy, stratege);
            
            GTRenderer ren = null;
            MapContext context = null;

            if (renderingStrategy != null) {
                ren = renderingStrategy.getRenderer();
                context = renderingStrategy.getContext();
                renderingStrategy.setContext(null);
                mapDecorationPane.remove(renderingStrategy.getComponent());
                renderingStrategy.removeStrategyListener(strategylisten);
            }
            renderingStrategy = stratege;
            renderingStrategy.addStrategyListener(strategylisten);

            if (ren != null) {
                renderingStrategy.setRenderer(ren);
            }
            renderingStrategy.setContext(context);
            mapDecorationPane.add(renderingStrategy.getComponent(), new Integer(0));
            mapDecorationPane.revalidate();

        }
    }

    public RenderingStrategy getRenderingStrategy() {
        return renderingStrategy;
    }

    public void refresh() {
    //getRenderingStrategy().reset();
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
    

    private class StrategyListen implements StrategyListener {

        public void setRendering(boolean rendering) {
            informationDecoration.setDrawing(rendering);
        }

        public void mapAreaChanged(Map2DMapAreaEvent event) {
        }

        public void mapContextChanged(Map2DContextEvent event) {
        }
    }
}

