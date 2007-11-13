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
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Date;
import javax.swing.event.MouseInputListener;
import org.geotools.gui.swing.map.MapConstants;
import org.geotools.gui.swing.map.MapConstants.NAVIGATION;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.shape.ShapefileRenderer;

/**
 * @author Johann Sorel
 */
public class DefaultNavigableMap2D extends DefaultMap2D implements NavigableMap2D{

    protected MapConstants.NAVIGATION navigation = MapConstants.NAVIGATION.PAN;
    protected MouseInputListener mouseInputListener; 
    protected double zoomFactor = 2;
    
    
    public DefaultNavigableMap2D(){
        this(new ShapefileRenderer());
    }    
    
    public DefaultNavigableMap2D(GTRenderer renderer){
        super(renderer);
        mouseInputListener = new MouseListen(this);
        addMouseListener(mouseInputListener);
        addMouseMotionListener(mouseInputListener);
    }

    
    
    
    
    //-----------------------NAVIGABLEMAP2D-------------------------------------
    public void setNavigationState(NAVIGATION state) {
        navigation = state;
    }
    public NAVIGATION getNavigationState() {
        return navigation;
    }

    public void setZoomFactor(double zoomFactor) {
        this.zoomFactor = zoomFactor;
    }

    public double getZoomFactor() {
        return zoomFactor;
    }
    
}


class MouseListen implements MouseInputListener{
    
    private int startX;
    private int startY;
    private int lastX;
    private int lastY; 
    
    private final DefaultNavigableMap2D map;    
    MouseListen(DefaultNavigableMap2D map){
        this.map = map;
    }
    
    
    private void drawRectangle(Graphics graphics) {
        int left = Math.min(startX, lastX);
        int right = Math.max(startX, lastX);
        int top = Math.max(startY, lastY);
        int bottom = Math.min(startY, lastY);
        int width = right - left;
        int height = top - bottom;
        graphics.drawRect(left, bottom, width, height);
    }
    
    
    
    void processDrag(int x1, int y1, int x2, int y2) {
        
        if ((x1 == x2) && (y1 == y2)) {            
            this.mouseClicked(new MouseEvent(map, 0, new Date().getTime(), 0,x1, y1, y2, false));            
            return;
        }

        Rectangle bounds = map.getBounds();

        double mapWidth = map.mapArea.getWidth();
        double mapHeight = map.mapArea.getHeight();

        double startX = ((x1 * mapWidth) / (double) bounds.width) + map.mapArea.getMinX();
        double startY = (((bounds.getHeight() - y1) * mapHeight) / (double) bounds.height) + map.mapArea.getMinY();
        double endX = ((x2 * mapWidth) / (double) bounds.width) + map.mapArea.getMinX();
        double endY = (((bounds.getHeight() - y2) * mapHeight) / (double) bounds.height) + map.mapArea.getMinY();

        double left;
        double right;
        double bottom;
        double top;
        Coordinate ll;
        Coordinate ur;
        
        switch(map.navigation){
        case PAN :
            // move the image with the mouse
            // calculate X offsets from start point to the end Point
            double deltaX1 = endX - startX;

            // System.out.println("deltaX " + deltaX1);
            // new edges
            left = map.mapArea.getMinX() - deltaX1;
            right = map.mapArea.getMaxX() - deltaX1;

            // now for Y
            double deltaY1 = endY - startY;

            // System.out.println("deltaY " + deltaY1);
            bottom = map.mapArea.getMinY() - deltaY1;
            top = map.mapArea.getMaxY() - deltaY1;
            ll = new Coordinate(left, bottom);
            ur = new Coordinate(right, top);

            map.mapArea = map.fixAspectRatio(map.getBounds(), new Envelope(ll, ur));
            break;
            
        case ZOOM_IN :
            // make the dragged rectangle (in map coords) the new BBOX
            left = Math.min(startX, endX);
            right = Math.max(startX, endX);
            bottom = Math.min(startY, endY);
            top = Math.max(startY, endY);
            ll = new Coordinate(left, bottom);
            ur = new Coordinate(right, top);

            map.mapArea = map.fixAspectRatio(map.getBounds(), new Envelope(ll, ur));
            break;
            
        case ZOOM_OUT :
            // make the dragged rectangle in screen coords the new map size?
            left = Math.min(startX, endX);
            right = Math.max(startX, endX);
            bottom = Math.min(startY, endY);
            top = Math.max(startY, endY);
            double nWidth = (mapWidth * mapWidth) / (right - left);
            double nHeight = (mapHeight * mapHeight) / (top - bottom);
            deltaX1 = left - map.mapArea.getMinX();
            double nDeltaX1 = (deltaX1 * nWidth) / mapWidth;
            deltaY1 = bottom - map.mapArea.getMinY();
            double nDeltaY1 = (deltaY1 * nHeight) / mapHeight;
            ll = new Coordinate(map.mapArea.getMinX() - nDeltaX1,map.mapArea.getMinY() - nDeltaY1);
            double deltaX2 = map.mapArea.getMaxX() - right;
            double nDeltaX2 = (deltaX2 * nWidth) / mapWidth;
            double deltaY2 = map.mapArea.getMaxY() - top;
            double nDeltaY2 = (deltaY2 * nHeight) / mapHeight;
            ur = new Coordinate(map.mapArea.getMaxX() + nDeltaX2,map.mapArea.getMaxY() + nDeltaY2);
            map.mapArea = map.fixAspectRatio(map.getBounds(), new Envelope(ll, ur));
        }

        map.repaint();
    }
    
    
    
    
    
    
    
    
    
    
    
    public void mouseClicked(MouseEvent e) {
         // TODO Auto-generated method stub
        // System.out.println("before area "+mapArea+"\nw:"+mapArea.getWidth()+"
        // h:"+mapArea.getHeight());
        Rectangle bounds = map.getBounds();
        double x = (double) (e.getX());
        double y = (double) (e.getY());
        double width = map.mapArea.getWidth();
        double height = map.mapArea.getHeight();
        double width2 = map.mapArea.getWidth() / 2.0;
        double height2 = map.mapArea.getHeight() / 2.0;

        double mapX = ((x * width) / (double) bounds.width) + map.mapArea.getMinX();
        double mapY = (((bounds.getHeight() - y) * height) / (double) bounds.height) + map.mapArea.getMinY();

        /*
         * System.out.println(""+x+"->"+mapX);
         * System.out.println(""+y+"->"+mapY);
         */

        /*
         * Coordinate ll = new Coordinate(mapArea.getMinX(), mapArea.getMinY());
         * Coordinate ur = new Coordinate(mapArea.getMaxX(), mapArea.getMaxY());
         */
        double zlevel = 1.0;

        switch (map.navigation) {
        case PAN:
            zlevel = 1.0;
            break;
        case ZOOM_IN:
            zlevel = map.zoomFactor;
            break;
        case ZOOM_OUT:
            zlevel = 1.0 / map.zoomFactor;
            break;
        default:
            return;
        }

        Coordinate ll = new Coordinate(mapX - (width2 / zlevel), mapY - (height2 / zlevel));
        Coordinate ur = new Coordinate(mapX + (width2 / zlevel), mapY + (height2 / zlevel));

        map.setMapArea( new Envelope(ll, ur) );
        // System.out.println("after area "+mapArea+"\nw:"+mapArea.getWidth()+"
        // h:"+mapArea.getHeight());
        map.repaint();
    
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

        if ((map.navigation == MapConstants.NAVIGATION.ZOOM_IN) || (map.navigation == MapConstants.NAVIGATION.ZOOM_OUT)) {
            drawRectangle(map.getGraphics());
        }

        processDrag( startX,startY, endX, endY);
        lastX = 0;
        lastY = 0;
    
    }
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseDragged(MouseEvent e) {   
        Graphics graphics = map.getGraphics();
        int x = e.getX();
        int y = e.getY();

        if (map.navigation == MapConstants.NAVIGATION.PAN) {
            // move the image with the mouse
            if ((lastX > 0) && (lastY > 0)) {
//                int dx = lastX - startX;
//                int dy = lastY - startY;
//                // System.out.println("translate "+dx+","+dy);
//                graphics.clearRect(0, 0, map.getWidth(), map.getHeight());
//                
//                
//                for (BufferedImage buf : map.bufferLayer) {
//                    ((Graphics2D) graphics).drawImage(buf, dx, dy, map);
//                }
//                
                //((Graphics2D) graphics).drawImage(map.bufferImage, dx, dy, map);
            }

            lastX = x;
            lastY = y;
        } else if ((map.navigation == MapConstants.NAVIGATION.ZOOM_IN) || (map.navigation == MapConstants.NAVIGATION.ZOOM_OUT)) {
            graphics.setXORMode(Color.RED);

            if ((lastX > 0) && (lastY > 0)) {
                drawRectangle(graphics);
            }

            // draw new box
            lastX = x;
            lastY = y;
            drawRectangle(graphics);
        }
    
        
    
}

    public void mouseMoved(MouseEvent e) {}
    
}

