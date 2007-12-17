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
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Date;
import javax.swing.event.MouseInputListener;
import org.geotools.gui.swing.map.MapConstants;
import org.geotools.gui.swing.map.MapConstants.ACTION_STATE;
import org.geotools.gui.swing.map.map2d.event.Map2DActionStateEvent;
import org.geotools.gui.swing.map.map2d.listener.NavigableMap2DListener;
import org.geotools.gui.swing.map.map2d.overLayer.ZoomPanDecoration;

/**
 * @author Johann Sorel
 */
public class JDefaultNavigableMap2D extends JDefaultMap2D implements NavigableMap2D {

    private final MouseInputListener mouseInputListener;
    private final ZoomPanDecoration zoompanPanel = new ZoomPanDecoration();
    private double zoomFactor = 2;
    protected MapConstants.ACTION_STATE actionState = MapConstants.ACTION_STATE.PAN;

    public JDefaultNavigableMap2D() {
        super();
        mouseInputListener = new MouseListen();
        addMouseListener(mouseInputListener);
        addMouseMotionListener(mouseInputListener);        
        addMapDecoration(zoompanPanel);
    }

    private void fireActionStateChanged(MapConstants.ACTION_STATE oldone, MapConstants.ACTION_STATE newone) {
        Map2DActionStateEvent mce = new Map2DActionStateEvent(this, oldone, newone);

        NavigableMap2DListener[] lst = getNavigableMap2DListeners();

        for (NavigableMap2DListener l : lst) {
            l.mapActionStateChanged(mce);
        }

    }

    
    //-----------------------NAVIGABLEMAP2D-------------------------------------
    public void setActionState(ACTION_STATE state) {
        
        if(actionState != state){
            fireActionStateChanged(actionState, state);
            actionState = state;
        }
        
    }

    public ACTION_STATE getActionState() {
        return actionState;
    }

    public void setZoomFactor(double zoomFactor) {
        this.zoomFactor = zoomFactor;
    }

    public double getZoomFactor() {
        return zoomFactor;
    }
    
    public void addNavigableMap2DListener(NavigableMap2DListener listener) {
        MAP2DLISTENERS.add(NavigableMap2DListener.class, listener);
    }

    public void removeNavigableMap2DListener(NavigableMap2DListener listener) {
        MAP2DLISTENERS.remove(NavigableMap2DListener.class, listener);
    }

    public NavigableMap2DListener[] getNavigableMap2DListeners() {
        return MAP2DLISTENERS.getListeners(NavigableMap2DListener.class);
    }
    

    //---------------------PRIVATE CLASSES--------------------------------------
    private class MouseListen implements MouseInputListener {

        private int startX;
        private int startY;
        private int lastX;
        private int lastY;

        private void drawRectangle(boolean view, boolean fill) {
            int left = Math.min(startX, lastX);
            int right = Math.max(startX, lastX);
            int top = Math.max(startY, lastY);
            int bottom = Math.min(startY, lastY);
            int width = right - left;
            int height = top - bottom;
            zoompanPanel.setFill(fill);
            zoompanPanel.setCoord(left, bottom, width, height, view);
        //graphics.drawRect(left, bottom, width, height);
        }

        private void processDrag(int x1, int y1, int x2, int y2) {
                        
            
            if (mapArea != null) {

                if ((x1 == x2) && (y1 == y2)) {
                    this.mouseClicked(new MouseEvent((JDefaultNavigableMap2D) THIS_MAP, 0, new Date().getTime(), 0, x1, y1, y2, false));
                    return;
                }

                Rectangle bounds = getBounds();

                double mapWidth = mapArea.getWidth();
                double mapHeight = mapArea.getHeight();

                double startX = ((x1 * mapWidth) / (double) bounds.width) + mapArea.getMinX();
                double startY = (((bounds.getHeight() - y1) * mapHeight) / (double) bounds.height) + mapArea.getMinY();
                double endX = ((x2 * mapWidth) / (double) bounds.width) + mapArea.getMinX();
                double endY = (((bounds.getHeight() - y2) * mapHeight) / (double) bounds.height) + mapArea.getMinY();

                double left;
                double right;
                double bottom;
                double top;
                Coordinate ll;
                Coordinate ur;

                switch (actionState) {
                    case PAN:
                        // move the image with the mouse
                        // calculate X offsets from start point to the end Point
                        double deltaX1 = endX - startX;

                        // System.out.println("deltaX " + deltaX1);
                        // new edges
                        left = mapArea.getMinX() - deltaX1;
                        right = mapArea.getMaxX() - deltaX1;

                        // now for Y
                        double deltaY1 = endY - startY;

                        // System.out.println("deltaY " + deltaY1);
                        bottom = mapArea.getMinY() - deltaY1;
                        top = mapArea.getMaxY() - deltaY1;
                        ll = new Coordinate(left, bottom);
                        ur = new Coordinate(right, top);

                        setMapArea(fixAspectRatio(getBounds(), new Envelope(ll, ur)));
                        //mapArea = fixAspectRatio(getBounds(), new Envelope(ll, ur));
                        break;

                    case ZOOM_IN:
                        // make the dragged rectangle (in map coords) the new BBOX
                        left = Math.min(startX, endX);
                        right = Math.max(startX, endX);
                        bottom = Math.min(startY, endY);
                        top = Math.max(startY, endY);
                        ll = new Coordinate(left, bottom);
                        ur = new Coordinate(right, top);

                        setMapArea(fixAspectRatio(getBounds(), new Envelope(ll, ur)));
//                        mapArea = fixAspectRatio(getBounds(), new Envelope(ll, ur));
                        break;

                    case ZOOM_OUT:
                        // make the dragged rectangle in screen coords the new map size?
                        left = Math.min(startX, endX);
                        right = Math.max(startX, endX);
                        bottom = Math.min(startY, endY);
                        top = Math.max(startY, endY);
                        double nWidth = (mapWidth * mapWidth) / (right - left);
                        double nHeight = (mapHeight * mapHeight) / (top - bottom);
                        deltaX1 = left - mapArea.getMinX();
                        double nDeltaX1 = (deltaX1 * nWidth) / mapWidth;
                        deltaY1 = bottom - mapArea.getMinY();
                        double nDeltaY1 = (deltaY1 * nHeight) / mapHeight;
                        ll = new Coordinate(mapArea.getMinX() - nDeltaX1, mapArea.getMinY() - nDeltaY1);
                        double deltaX2 = mapArea.getMaxX() - right;
                        double nDeltaX2 = (deltaX2 * nWidth) / mapWidth;
                        double deltaY2 = mapArea.getMaxY() - top;
                        double nDeltaY2 = (deltaY2 * nHeight) / mapHeight;
                        ur = new Coordinate(mapArea.getMaxX() + nDeltaX2, mapArea.getMaxY() + nDeltaY2);
                        
                        setMapArea(fixAspectRatio(getBounds(), new Envelope(ll, ur)));
                        break;
                }

            }
        }

        public void mouseClicked(MouseEvent e) {
            if (mapArea != null) {
                // TODO Auto-generated method stub
                // System.out.println("before area "+mapArea+"\nw:"+mapArea.getWidth()+"
                // h:"+mapArea.getHeight());
                Rectangle bounds = getBounds();
                double x = (double) (e.getX());
                double y = (double) (e.getY());
                double width = mapArea.getWidth();
                double height = mapArea.getHeight();
                double width2 = mapArea.getWidth() / 2.0;
                double height2 = mapArea.getHeight() / 2.0;

                double mapX = ((x * width) / (double) bounds.width) + mapArea.getMinX();
                double mapY = (((bounds.getHeight() - y) * height) / (double) bounds.height) + mapArea.getMinY();

                /*
                 * System.out.println(""+x+"->"+mapX);
                 * System.out.println(""+y+"->"+mapY);
                 */

                /*
                 * Coordinate ll = new Coordinate(mapArea.getMinX(), mapArea.getMinY());
                 * Coordinate ur = new Coordinate(mapArea.getMaxX(), mapArea.getMaxY());
                 */
                double zlevel = 1.0;

                switch (actionState) {
                    case PAN:
                        zlevel = 1.0;
                        break;
                    case ZOOM_IN:
                        zlevel = zoomFactor;
                        break;
                    case ZOOM_OUT:
                        zlevel = 1.0 / zoomFactor;
                        break;
                    default:
                        return;
                }

                Coordinate ll = new Coordinate(mapX - (width2 / zlevel), mapY - (height2 / zlevel));
                Coordinate ur = new Coordinate(mapX + (width2 / zlevel), mapY + (height2 / zlevel));

                setMapArea(new Envelope(ll, ur));
                repaint();
            }

        }

        public void mousePressed(MouseEvent e) {
            startX = e.getX();
            startY = e.getY();
            lastX = 0;
            lastY = 0;

        }

        public void mouseReleased(MouseEvent e) {
            int endX = e.getX();
            int endY = e.getY();


            switch (actionState) {
                case PAN:
                    zoompanPanel.setFill(false);
                    zoompanPanel.setCoord(0, 0, 0, 0, false);
                    break;
                case ZOOM_IN:
                case ZOOM_OUT:
                    drawRectangle(false, true);
                    break;
            }


            processDrag(startX, startY, endX, endY);
            lastX = 0;
            lastY = 0;

        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();

            switch (actionState) {
                case PAN:
                    if ((lastX > 0) && (lastY > 0)) {
                        int dx = lastX - startX;
                        int dy = lastY - startY;
                        zoompanPanel.setFill(false);
                        zoompanPanel.setCoord(dx, dy, getWidth(), getHeight(), true);
                    }
                    lastX = x;
                    lastY = y;
                    break;
                case ZOOM_IN:
                case ZOOM_OUT:
                    if ((lastX > 0) && (lastY > 0)) {
                        drawRectangle(true, true);
                    }

                    // draw new box
                    lastX = x;
                    lastY = y;
                    drawRectangle(true, true);
                    break;
            }



        }

        public void mouseMoved(MouseEvent e) {
        }
    }

    
}




