/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.gui.swing.map.map2d.stream.control;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ResourceBundle;

import org.geotools.geometry.jts.JTS;
import org.geotools.gui.swing.crschooser.JCRSChooser;
import org.geotools.gui.swing.map.map2d.Map2D;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.geotools.gui.swing.map.map2d.stream.StreamingMap2D;

/**
 *
 * @author johann sorel
 */
public class JStreamCoordPane extends javax.swing.JPanel {
    
    private final myListener listener = new myListener();
    private StreamingMap2D map = null;
    private CoordinateReferenceSystem defaultCRS = null;
    private String error = ResourceBundle.getBundle("org/geotools/gui/swing/map/map2d/control/Bundle").getString("coord_error");
    
    /** Creates new form JMap2DMouseCoordPanel */
    public JStreamCoordPane() {
        initComponents();
                
    }
    
    public StreamingMap2D getMap() {
        return map;
    }

    public void setMap(StreamingMap2D map) {
        
        if(this.map != null){
            this.map.getComponent().removeMouseMotionListener(listener);
        }
        
        this.map = map;
        
        if(this.map != null){
            this.map.getComponent().addMouseMotionListener(listener);
        }
    }
    
    public void setDefaultCRS(CoordinateReferenceSystem crs){
        defaultCRS = crs;
    }
    
    public CoordinateReferenceSystem getDefaultCRS(){
        return defaultCRS;
    }
    
        
    private Coordinate toMapCoord(double mx, double my, double width, double height, Rectangle bounds, Envelope mapArea) {
        double mapX = ((mx * width) / (double) bounds.width) + mapArea.getMinX();
        double mapY = (((bounds.getHeight() - my) * height) / (double) bounds.height) + mapArea.getMinY();
        return new Coordinate(mapX, mapY);
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        menu = new javax.swing.JPopupMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jLabel1 = new javax.swing.JLabel();
        jtf_coord = new javax.swing.JTextField();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/geotools/gui/swing/map/map2d/control/Bundle"); // NOI18N
        jMenuItem1.setText(bundle.getString("map_crs")); // NOI18N
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        menu.add(jMenuItem1);

        jMenuItem2.setText(bundle.getString("crs_choose")); // NOI18N
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        menu.add(jMenuItem2);

        setComponentPopupMenu(menu);
        setOpaque(false);

        jLabel1.setComponentPopupMenu(menu);
        jLabel1.setText(bundle.getString("mouse_coord")); // NOI18N

        jtf_coord.setComponentPopupMenu(menu);
        jtf_coord.setEditable(false);
        jtf_coord.setOpaque(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jtf_coord, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(jLabel1)
                .add(jtf_coord, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        
        if(map != null && map.getRenderingStrategy().getContext() != null){
            defaultCRS = map.getRenderingStrategy().getContext().getCoordinateReferenceSystem();            
        }
        
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        JCRSChooser cc = new JCRSChooser(null, true);
        
        cc.setCRS(defaultCRS);
        
        JCRSChooser.ACTION action = cc.showDialog();
        
        if(action == JCRSChooser.ACTION.APPROVE){
            defaultCRS = cc.getCRS();
        }
        
    }//GEN-LAST:event_jMenuItem2ActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JTextField jtf_coord;
    private javax.swing.JPopupMenu menu;
    // End of variables declaration//GEN-END:variables

    
    private class myListener extends MouseMotionAdapter{

        @Override
        public void mouseMoved(MouseEvent e) {
            update(e);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            update(e);
        }

        private void update(MouseEvent event){
            event.getX();
            
            Rectangle rec = map.getComponent().getBounds();            
            Envelope env = map.getRenderingStrategy().getMapArea();
            
            if(env!=null && rec!= null){
                GeometryFactory geofact = new GeometryFactory();
                Coordinate coord = toMapCoord(event.getX(), event.getY(), env.getWidth(), env.getHeight(), rec,env);   
                Point point = geofact.createPoint(coord);
                
                
                if(defaultCRS != null){
                    CoordinateReferenceSystem sourceCRS = map.getRenderingStrategy().getContext().getCoordinateReferenceSystem();
                    CoordinateReferenceSystem targetCRS = defaultCRS;
                    
                    try{
                        MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
                        Geometry targetGeometry = JTS.transform( point, transform);
                        coord = targetGeometry.getCoordinate();
                        jtf_coord.setText("X= "+ coord.x +"  /  Y= "+coord.y);
                    }catch(Exception e){
                        jtf_coord.setText(error);
                    }                    
                    
                }else{
                    jtf_coord.setText("X= "+ coord.x +"  /  Y= "+coord.y);
                }
                
                
                
                
            }else{
                jtf_coord.setText(error);
            }
            
        }
                
        
        
    }
    
}
