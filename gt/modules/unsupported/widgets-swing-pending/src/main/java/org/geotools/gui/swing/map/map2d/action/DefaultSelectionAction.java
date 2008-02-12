/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.gui.swing.map.map2d.action;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.map.map2d.SelectableMap2D;
import org.geotools.gui.swing.map.map2d.decoration.SelectionDecoration;

/**
 *
 * @author johann sorel
 */
public class DefaultSelectionAction implements SelectionAction {

    protected final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private final MouseInputListener mouseInputListener = new MouseListen();
    private final SelectionDecoration selectionPane = new SelectionDecoration();
    private SelectableMap2D map2D = null;
    private JComponent mapComponent = null;
    private boolean installed = false;
    protected Cursor CUR_SELECT;

    public DefaultSelectionAction() {
        buildCursors();
    }

    private void buildCursors() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        ImageIcon ico_select = IconBundle.getResource().getIcon("16_select");

        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        img.getGraphics().drawImage(ico_select.getImage(), 0, 0, null);
        CUR_SELECT = tk.createCustomCursor(img, new java.awt.Point(1, 1), "select");

    }

    private void doMouseSelection(int mx, int my) {

        Geometry geometry = mousePositionToGeometry(mx, my);
        if (geometry != null) {
            map2D.doSelection(geometry);
        }
    }

    /**
     *  transform a mouse coordinate in JTS Geometry using the CRS of the mapcontext
     * @param mx : x coordinate of the mouse on the map (in pixel)
     * @param my : y coordinate of the mouse on the map (in pixel)
     * @return JTS geometry (corresponding to a square of 6x6 pixel around mouse coordinate)
     */
    private Geometry mousePositionToGeometry(int mx, int my) {
        Coordinate[] coord = new Coordinate[5];
        int taille = 4;

        coord[0] = map2D.toMapCoord(mx - taille, my - taille);
        coord[1] = map2D.toMapCoord(mx - taille, my + taille);
        coord[2] = map2D.toMapCoord(mx + taille, my + taille);
        coord[3] = map2D.toMapCoord(mx + taille, my - taille);
        coord[4] = coord[0];

        LinearRing lr1 = GEOMETRY_FACTORY.createLinearRing(coord);
        return GEOMETRY_FACTORY.createPolygon(lr1, null);
    }

    private void doMouseSelection(int mx, int my, int ex, int ey) {
        Coordinate[] coord = new Coordinate[5];

        coord[0] = map2D.toMapCoord(mx, my);
        coord[1] = map2D.toMapCoord(mx, ey);
        coord[2] = map2D.toMapCoord(ex, ey);
        coord[3] = map2D.toMapCoord(ex, my);
        coord[4] = coord[0];

        LinearRing lr1 = GEOMETRY_FACTORY.createLinearRing(coord);
        Geometry geometry = GEOMETRY_FACTORY.createPolygon(lr1, null);

        map2D.doSelection(geometry);
    }

    public void install(SelectableMap2D map2D, JComponent mapComponent) {
        installed = true;
        this.map2D = map2D;
        this.mapComponent = mapComponent;
        map2D.addDecoration(selectionPane);
        mapComponent.addMouseListener(mouseInputListener);
        mapComponent.addMouseMotionListener(mouseInputListener);
    }

    public void uninstall() {
        map2D.removeDecoration(selectionPane);
        mapComponent.addMouseListener(mouseInputListener);
        mapComponent.addMouseMotionListener(mouseInputListener);
        map2D = null;
        mapComponent = null;
        installed = false;
    }
    
    public boolean isInstalled() {
        return installed;
    }
    

    private class MouseListen implements MouseInputListener {

        int startX = 0;
        int startY = 0;
        int lastX = 0;
        int lastY = 0;

        private void drawRectangle(boolean view, boolean fill) {
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
            doMouseSelection(e.getX(), e.getY());
        }

        public void mousePressed(MouseEvent e) {
            startX = e.getX();
            startY = e.getY();
            lastX = 0;
            lastY = 0;

        }

        public void mouseReleased(MouseEvent e) {
            lastX = e.getX();
            lastY = e.getY();
            drawRectangle(false, true);
            doMouseSelection(startX, startY, lastX, lastY);
        }

        public void mouseEntered(MouseEvent e) {
            mapComponent.setCursor(CUR_SELECT);

        }

        public void mouseExited(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {
            lastX = e.getX();
            lastY = e.getY();

            drawRectangle(true, true);


        }

        public void mouseMoved(MouseEvent e) {
        }
    }

    
}
