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

import java.util.ArrayList;
import java.util.List;

import org.geotools.gui.swing.map.map2d.event.Map2DContextEvent;
import org.geotools.gui.swing.map.map2d.event.Map2DMapAreaEvent;
import org.geotools.gui.swing.map.map2d.handler.NavigationHandler;
import org.geotools.gui.swing.map.map2d.listener.Map2DNavigationListener;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.gui.swing.map.map2d.event.Map2DNavigationEvent;
import org.geotools.gui.swing.map.map2d.handler.DefaultPanHandler;
import org.geotools.gui.swing.map.map2d.strategy.RenderingStrategy;

/**
 * Default implementation of NavigableMap2D
 * @author Johann Sorel
 */
public class JDefaultNavigableMap2D extends JDefaultMap2D implements NavigableMap2D {

    private final List<Envelope> mapAreas = new ArrayList<Envelope>();
    private Envelope lastMapArea = null;
    private NavigationHandler navigationHandler = new DefaultPanHandler();

    /**
     * create a default JDefaultNavigableMap2D
     */
    public JDefaultNavigableMap2D() {
        super();
    }
    
    private void fireHandlerChanged(NavigationHandler handler) {
        Map2DNavigationEvent mce = new Map2DNavigationEvent(this, handler);

        Map2DNavigationListener[] lst = getNavigableMap2DListeners();

        for (Map2DNavigationListener l : lst) {
            l.navigationHandlerChanged(mce);
        }

    }
    
    //----------------------Map2d override--------------------------------------
    @Override
    protected void mapContextChanged(Map2DContextEvent event) {
        super.mapContextChanged(event);
        
        mapAreas.clear();

        lastMapArea = getRenderingStrategy().getMapArea();
    }

    @Override
    protected void mapAreaChanged(Map2DMapAreaEvent event) {
        super.mapAreaChanged(event);

        while (mapAreas.size() > 10) {
            mapAreas.remove(0);
        }

        Envelope newMapArea = event.getNewMapArea();
        lastMapArea = newMapArea;

        if (mapAreas.contains(newMapArea)) {

//            if (mapAreas.size() > 1) {
//
//                int position = mapAreas.indexOf(newMapArea);
//
//                if (position == 0) {
//                    gui_previousArea.setEnabled(false);
//                    gui_nextArea.setEnabled(true);
//                } else if (position == mapAreas.size() - 1) {
//                    gui_previousArea.setEnabled(true);
//                    gui_nextArea.setEnabled(false);
//                } else {
//                    gui_previousArea.setEnabled(true);
//                    gui_nextArea.setEnabled(true);
//                }
//
//            } else {
//                gui_previousArea.setEnabled(false);
//                gui_nextArea.setEnabled(false);
//            }


        } else {
            mapAreas.add(newMapArea);

//            if (mapAreas.size() > 1) {
//                gui_previousArea.setEnabled(true);
//                gui_nextArea.setEnabled(false);
//            } else {
//                gui_previousArea.setEnabled(false);
//                gui_nextArea.setEnabled(false);
//            }
        }

    }

    @Override
    public void setRenderingStrategy(RenderingStrategy stratege) {
        ACTION_STATE oldAction = actionState;
        super.setRenderingStrategy(stratege);
        
        if (oldAction == ACTION_STATE.NAVIGATE && navigationHandler.isInstalled()) {
            navigationHandler.uninstall();
        }

        if (actionState == ACTION_STATE.NAVIGATE) {
            navigationHandler.install(this);
        }

    }
    
    @Override
    public void setActionState(ACTION_STATE state) {
        super.setActionState(state);
                        
        if (state == ACTION_STATE.NAVIGATE && !navigationHandler.isInstalled()) {
            navigationHandler.install(this);
        } else if (navigationHandler.isInstalled()) {
            navigationHandler.uninstall();
        }

    }
    
    //-----------------------NAVIGABLEMAP2D-------------------------------------
        
    public void setNavigationHandler(NavigationHandler handler) {
        if (handler == null) {
            throw new NullPointerException();
        } else if (handler != navigationHandler) {

            if (navigationHandler.isInstalled()) {
                navigationHandler.uninstall();
            }

            navigationHandler = handler;

            if (actionState == ACTION_STATE.SELECT) {
                navigationHandler.install(this);
            }

            fireHandlerChanged(navigationHandler);
        }
    }

    public NavigationHandler getNavigationHandler() {
        return navigationHandler;
    }

    public void previousMapArea() {
        if (lastMapArea != null) {
            int index = mapAreas.indexOf(lastMapArea);

            index--;
            if (index >= 0) {
                getRenderingStrategy().setMapArea(mapAreas.get(index));
            }
        }
    }

    public void nextMapArea() {
        if (lastMapArea != null) {
            int index = mapAreas.indexOf(lastMapArea);

            index++;
            if (index < mapAreas.size()) {
                getRenderingStrategy().setMapArea(mapAreas.get(index));
            }
        }
    }

    public void addNavigableMap2DListener(Map2DNavigationListener listener) {
        MAP2DLISTENERS.add(Map2DNavigationListener.class, listener);
    }

    public void removeNavigableMap2DListener(Map2DNavigationListener listener) {
        MAP2DLISTENERS.remove(Map2DNavigationListener.class, listener);
    }

    public Map2DNavigationListener[] getNavigableMap2DListeners() {
        return MAP2DLISTENERS.getListeners(Map2DNavigationListener.class);
    }
}
