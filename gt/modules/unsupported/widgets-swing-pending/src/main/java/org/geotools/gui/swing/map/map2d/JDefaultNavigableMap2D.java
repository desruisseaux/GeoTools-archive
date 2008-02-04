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
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.event.MouseInputListener;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.map.MapConstants;
import org.geotools.gui.swing.map.MapConstants.ACTION_STATE;
import org.geotools.gui.swing.map.map2d.decoration.ZoomPanDecoration;
import org.geotools.gui.swing.map.map2d.event.Map2DActionStateEvent;
import org.geotools.gui.swing.map.map2d.event.Map2DContextEvent;
import org.geotools.gui.swing.map.map2d.event.Map2DMapAreaEvent;
import org.geotools.gui.swing.map.map2d.listener.NavigableMap2DListener;

/**
 * Default implementation of NavigableMap2D
 * @author Johann Sorel
 */
public class JDefaultNavigableMap2D extends JDefaultMap2D implements NavigableMap2D {

    /**
     * Action state of the map widget
     */
    protected MapConstants.ACTION_STATE actionState = MapConstants.ACTION_STATE.PAN;
    protected Cursor CUR_ZOOM_IN;
    protected Cursor CUR_ZOOM_OUT;
    protected Cursor CUR_ZOOM_PAN;
    private final MouseListen mouseListen;
    private final ZoomPanDecoration zoompanPanel = new ZoomPanDecoration();
    private double zoomFactor = 2;
    private final List<Envelope> mapAreas = new ArrayList<Envelope>();
    private Envelope lastMapArea = null;

    /**
     * create a default JDefaultNavigableMap2D
     */
    public JDefaultNavigableMap2D() {
        super();
        mouseListen = new MouseListen();
        addMouseListener(mouseListen);
        addMouseMotionListener(mouseListen);
        addMouseWheelListener(mouseListen);
        addMapDecoration(zoompanPanel);

        buildCursors();
    }

    private void buildCursors() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        ImageIcon ico_zoomIn = IconBundle.getResource().getIcon("16_zoom_in");
        ImageIcon ico_zoomOut = IconBundle.getResource().getIcon("16_zoom_out");
        ImageIcon ico_zoomPan = IconBundle.getResource().getIcon("16_zoom_pan");


        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        img.getGraphics().drawImage(ico_zoomIn.getImage(), 0, 0, null);
        CUR_ZOOM_IN = tk.createCustomCursor(img, new Point(1, 1), "in");

        BufferedImage img2 = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        img2.getGraphics().drawImage(ico_zoomOut.getImage(), 0, 0, null);
        CUR_ZOOM_OUT = tk.createCustomCursor(img2, new Point(1, 1), "in");

        BufferedImage img3 = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        img3.getGraphics().drawImage(ico_zoomPan.getImage(), 0, 0, null);
        CUR_ZOOM_PAN = tk.createCustomCursor(img3, new Point(1, 1), "in");
    }

    private void fireActionStateChanged(MapConstants.ACTION_STATE oldone, MapConstants.ACTION_STATE newone) {
        Map2DActionStateEvent mce = new Map2DActionStateEvent(this, oldone, newone);

        NavigableMap2DListener[] lst = getNavigableMap2DListeners();

        for (NavigableMap2DListener l : lst) {
            l.mapActionStateChanged(mce);
        }

    }

    //----------------------defaultmap2d override-------------------------------
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

    //-----------------------NAVIGABLEMAP2D-------------------------------------
    public void setActionState(ACTION_STATE state) {

        if (actionState != state) {
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
    private class MouseListen implements MouseInputListener, MouseWheelListener {

        private int startX;
        private int startY;
        private int lastX;
        private int lastY;
        private int mousebutton = 0;

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

        private void processDrag(int x1, int y1, int x2, int y2, boolean pan) {

            Envelope mapArea = renderingStrategy.getMapArea();

            if (mapArea != null) {

                if ((x1 == x2) && (y1 == y2)) {
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

                if (!pan) {
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

                            renderingStrategy.setMapArea(new Envelope(ll, ur));
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

                            renderingStrategy.setMapArea(new Envelope(ll, ur));
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

                            renderingStrategy.setMapArea(new Envelope(ll, ur));
                            break;
                    }
                } else {
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

                    renderingStrategy.setMapArea(new Envelope(ll, ur));

                }
            }
        }

        public void mouseClicked(MouseEvent e) {

            mousebutton = e.getButton();

            Envelope mapArea = renderingStrategy.getMapArea();

            if (mapArea != null) {
                Rectangle bounds = getBounds();
                double x = (double) (e.getX());
                double y = (double) (e.getY());
                double width = mapArea.getWidth();
                double height = mapArea.getHeight();
                double width2 = mapArea.getWidth() / 2.0;
                double height2 = mapArea.getHeight() / 2.0;

                double mapX = ((x * width) / (double) bounds.width) + mapArea.getMinX();
                double mapY = (((bounds.getHeight() - y) * height) / (double) bounds.height) + mapArea.getMinY();

                double zlevel = 1.0;

                // left mouse button
                if (e.getButton() == MouseEvent.BUTTON1) {
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


                    switch (actionState) {
                        case PAN:
                            renderingStrategy.setMapArea(new Envelope(ll, ur));
                            repaint();
                            break;
                        case ZOOM_IN:
                            int width3 = getWidth() / 2;
                            int height3 = getHeight() / 2;

                            int x1 = e.getX() - (width3 / 2);
                            int y1 = e.getY() - (height3 / 2);
                            int x2 = x1 + width3;
                            int y2 = y1 + height3;

                            processDrag(x1, y1, x2, y2, false);
                            break;
                        case ZOOM_OUT:
                            renderingStrategy.setMapArea(new Envelope(ll, ur));
                            repaint();
                            break;
                        default:
                            return;
                    }
                } //right mouse button : pan action
                else if (e.getButton() == MouseEvent.BUTTON3) {
                    zlevel = 1.0;
                    Coordinate ll = new Coordinate(mapX - (width2 / zlevel), mapY - (height2 / zlevel));
                    Coordinate ur = new Coordinate(mapX + (width2 / zlevel), mapY + (height2 / zlevel));
                    renderingStrategy.setMapArea(new Envelope(ll, ur));
                    repaint();
                }



            }

        }

        public void mousePressed(MouseEvent e) {
            startX = e.getX();
            startY = e.getY();
            lastX = 0;
            lastY = 0;

            mousebutton = e.getButton();
            if (mousebutton == MouseEvent.BUTTON1) {
                switch (actionState) {
                    case PAN:
                        setCursor(CUR_ZOOM_PAN);
                        break;
                    case ZOOM_IN:
                        setCursor(CUR_ZOOM_IN);
                        break;
                    case ZOOM_OUT:
                        setCursor(CUR_ZOOM_OUT);
                        break;
                }
            } else if (mousebutton == MouseEvent.BUTTON3) {
                zoompanPanel.setCoord(0, 0, getWidth(), getHeight(), true);
                setCursor(CUR_ZOOM_PAN);
            }


        }

        public void mouseReleased(MouseEvent e) {
            int endX = e.getX();
            int endY = e.getY();


            if (mousebutton == MouseEvent.BUTTON1) {
                switch (actionState) {
                    case PAN:
                        zoompanPanel.setFill(false);
                        zoompanPanel.setCoord(0, 0, 0, 0, false);
                        processDrag(startX, startY, endX, endY, false);
                        break;
                    case ZOOM_IN:
                        int width = getWidth() / 2;
                        int height = getHeight() / 2;
                        int left = e.getX() - (width / 2);
                        int bottom = e.getY() - (height / 2);
                        zoompanPanel.setFill(false);
                        zoompanPanel.setCoord(left, bottom, width, height, true);
                    case ZOOM_OUT:
                        drawRectangle(false, true);
                        processDrag(startX, startY, endX, endY, false);
                        break;
                }
            } //right mouse button : pan action
            else if (mousebutton == MouseEvent.BUTTON3) {
                zoompanPanel.setFill(false);
                zoompanPanel.setCoord(0, 0, 0, 0, false);
                processDrag(startX, startY, endX, endY, true);
            }

            lastX = 0;
            lastY = 0;


            switch (actionState) {
                case PAN:
                    setCursor(CUR_ZOOM_PAN);
                    break;
                case ZOOM_IN:
                    setCursor(CUR_ZOOM_IN);
                    break;
                case ZOOM_OUT:
                    setCursor(CUR_ZOOM_OUT);
                    break;
            }

        }

        public void mouseEntered(MouseEvent e) {

            switch (actionState) {
                case PAN:
                    setCursor(CUR_ZOOM_PAN);
                    break;
                case ZOOM_IN:
                    setCursor(CUR_ZOOM_IN);
                    break;
                case ZOOM_OUT:
                    setCursor(CUR_ZOOM_OUT);
                    break;
            }
        }

        public void mouseExited(MouseEvent e) {
            zoompanPanel.setFill(false);
            zoompanPanel.setCoord(0, 0, 0, 0, true);
        }

        public void mouseDragged(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();


            // left mouse button
            if (mousebutton == MouseEvent.BUTTON1) {
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
            } //right mouse button : pan action
            else if (mousebutton == MouseEvent.BUTTON3) {
                if ((lastX > 0) && (lastY > 0)) {
                    int dx = lastX - startX;
                    int dy = lastY - startY;
                    zoompanPanel.setFill(false);
                    zoompanPanel.setCoord(dx, dy, getWidth(), getHeight(), true);
                }
                lastX = x;
                lastY = y;

                setCursor(CUR_ZOOM_PAN);


            }



        }

        public void mouseMoved(MouseEvent e) {

            if (actionState == MapConstants.ACTION_STATE.ZOOM_IN) {

                int width = getWidth() / 2;
                int height = getHeight() / 2;

                int left = e.getX() - (width / 2);
                int bottom = e.getY() - (height / 2);

                zoompanPanel.setFill(false);
                zoompanPanel.setCoord(left, bottom, width, height, true);
            }

        }

        public void mouseWheelMoved(MouseWheelEvent e) {
            int val = e.getWheelRotation();

            if (val > 0) {
                Envelope env = getRenderingStrategy().getMapArea();
                double width = env.getWidth();
                double height = env.getHeight();

                Coordinate nw = env.centre();
                nw.x -= width;
                nw.y -= height;
                Coordinate se = env.centre();
                se.x += width;
                se.y += height;

                Envelope env2 = new Envelope(nw, se);
                getRenderingStrategy().setMapArea(env2);

            } else if (val < 0) {
                Envelope env = getRenderingStrategy().getMapArea();
                double width = env.getWidth();
                double height = env.getHeight();

                Coordinate nw = env.centre();
                nw.x -= width / 4;
                nw.y -= height / 4;
                Coordinate se = env.centre();
                se.x += width / 4;
                se.y += height / 4;

                Envelope env2 = new Envelope(nw, se);
                getRenderingStrategy().setMapArea(env2);
            }

        }
    }
}




