/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2007, GeoTools Project Managment Committee (PMC)
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

package org.geotools.gui.swing.style.sld;

import java.awt.Component;
import org.geotools.gui.swing.style.StyleElementEditor;
import org.geotools.map.MapLayer;
import org.geotools.styling.PointPlacement;
import org.geotools.styling.StyleBuilder;

/**
 *
 * @author johann sorel
 */
public class JPointPlacementPane extends javax.swing.JPanel implements StyleElementEditor<PointPlacement>{
    
    private MapLayer layer = null;
    private PointPlacement placement = null;
    
    /** Creates new form JPointPlacementPanel */
    public JPointPlacementPane() {
        initComponents();
        init();
    }
    
    private void init(){
        guiRotation.setType(JExpressionPane.EXP_TYPE.NUMBER);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        guiAnchor = new org.geotools.gui.swing.style.sld.JAnchorPointPane();
        guiDisplacement = new org.geotools.gui.swing.style.sld.JDisplacementPane();
        guiRotation = new org.geotools.gui.swing.style.sld.JExpressionPane();
        jLabel1 = new javax.swing.JLabel();

        setOpaque(false);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/geotools/gui/swing/style/sld/Bundle"); // NOI18N
        guiAnchor.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("anchor"))); // NOI18N
        guiAnchor.setOpaque(false);

        guiDisplacement.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("displacement"))); // NOI18N
        guiDisplacement.setOpaque(false);

        jLabel1.setText(bundle.getString("rotation")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(guiRotation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(guiAnchor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(guiDisplacement, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(guiRotation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(guiAnchor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(guiDisplacement, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    public void setLayer(MapLayer layer) {
        this.layer = layer;
        guiAnchor.setLayer(layer);
        guiDisplacement.setLayer(layer);
        guiRotation.setLayer(layer);
    }

    public MapLayer getLayer() {
        return layer;
    }

    public void setEdited(PointPlacement target) {
        placement = target;
        
        if(placement != null){
            guiAnchor.setEdited(placement.getAnchorPoint());
            guiDisplacement.setEdited(placement.getDisplacement());
            guiRotation.setExpression(placement.getRotation());
        }
        
    }

    public PointPlacement getEdited() {
        
        if(placement == null){
            placement = new StyleBuilder().createPointPlacement();
        }
        
        apply();
        return placement;
    }

    public void apply() {
        if(placement != null){
            placement.setAnchorPoint(guiAnchor.getEdited());
            placement.setDisplacement(guiDisplacement.getEdited());
            placement.setRotation(guiRotation.getExpression());
        }
    }

    public Component getComponent() {
        return this;
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.geotools.gui.swing.style.sld.JAnchorPointPane guiAnchor;
    private org.geotools.gui.swing.style.sld.JDisplacementPane guiDisplacement;
    private org.geotools.gui.swing.style.sld.JExpressionPane guiRotation;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
    
}
