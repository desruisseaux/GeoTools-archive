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
package org.geotools.gui.swing.map.map2d.decoration;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.map.MapContext;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;

/**
 *
 * @author Johann Sorel
 */
public class MiniMapDecoration extends AbstractMapDecoration {

    private JDesktopPane desktop = new JDesktopPane();
    private JInternalFrame internal = new JInternalFrame("MiniMap", true, false, false, true);
    private MiniMap mini = new MiniMap();

    public MiniMapDecoration() {
        desktop.setOpaque(false);

        internal.setFrameIcon(IconBundle.EMPTY_ICON);
        internal.setBounds(50, 50, 200, 200);
        internal.getContentPane().setLayout(new BorderLayout());
        internal.getContentPane().add(BorderLayout.CENTER, mini);
        internal.getContentPane().setBackground(Color.WHITE);

        desktop.add(internal);

        internal.setVisible(true);
    }

    public void refresh() {
        desktop.revalidate();
        desktop.repaint();

    }

    public JComponent geComponent() {
        return desktop;
    }
    
    
    private Envelope fixAspectRatio(Rectangle r, Envelope mapArea, MapContext context) {

        if (mapArea == null && context != null) {
            try {
                mapArea = context.getLayerBounds();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        if (mapArea == null) {
            return null;
        }

        double mapWidth = mapArea.getWidth(); /* get the extent of the map */
        double mapHeight = mapArea.getHeight();
        double scaleX = r.getWidth() / mapArea.getWidth(); /*
         * calculate the new
         * scale
         */

        double scaleY = r.getHeight() / mapArea.getHeight();
        double scale = 1.0; // stupid compiler!

        if (scaleX < scaleY) { /* pick the smaller scale */
            scale = scaleX;
        } else {
            scale = scaleY;
        }

        /* calculate the difference in width and height of the new extent */
        double deltaX = /* Math.abs */ ((r.getWidth() / scale) - mapWidth);
        double deltaY = /* Math.abs */ ((r.getHeight() / scale) - mapHeight);

        /*
         * System.out.println("delta x " + deltaX); System.out.println("delta y " +
         * deltaY);
         */

        /* create the new extent */
        Coordinate ll = new Coordinate(mapArea.getMinX() - (deltaX / 2.0), mapArea.getMinY() - (deltaY / 2.0));
        Coordinate ur = new Coordinate(mapArea.getMaxX() + (deltaX / 2.0), mapArea.getMaxY() + (deltaY / 2.0));

        return new Envelope(ll, ur);
    }

    //-------------private classes----------------------------------
    private class MiniMap extends JComponent {

        private BufferedImage minimap = null;
        private Rectangle oldRect = null;

        void setImage(BufferedImage img) {
            minimap = img;
            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Rectangle newRect = getBounds();

            if (map != null && !newRect.equals(oldRect)) {
                System.out.println("la");
                try {
                    BufferedImage img = new BufferedImage(newRect.width, newRect.height, BufferedImage.TYPE_INT_ARGB);                    
                    Envelope env = fixAspectRatio(newRect, map.getContext().getLayerBounds(), map.getContext());
                    map.getRenderingStrategy().getRenderer().paint((Graphics2D) img.getGraphics(), newRect, env);
                    minimap = img;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            oldRect = newRect;

            if(minimap != null){
                g.drawImage(minimap, 0, 0, this);
            }
        }
    }

    private class listener implements MapLayerListListener {

        public void layerAdded(MapLayerListEvent event) {
        }

        public void layerRemoved(MapLayerListEvent event) {
        }

        public void layerChanged(MapLayerListEvent event) {
        }

        public void layerMoved(MapLayerListEvent event) {
        }
    }
}
