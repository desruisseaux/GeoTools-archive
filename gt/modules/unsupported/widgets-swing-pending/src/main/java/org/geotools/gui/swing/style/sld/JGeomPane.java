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

import org.geotools.map.MapLayer;

/**
 * @author johann sorel
 */
public class JGeomPane extends javax.swing.JPanel {
     
    /** Creates new form JGeomPane */
    public JGeomPane() {
        initComponents();
    }
    
    public void setLayer(MapLayer layer){
        
        if(layer == null){
            throw new NullPointerException();
        }
        
        guiBox.setLayer(layer);
        lbl.setEnabled(true);              
    }
    
    public MapLayer getLayer(){
        return guiBox.getLayer();
    }
    
    public String getGeometryPropertyName(){
        return guiBox.getGeometryPropertyName();
    }
    
    public void setGeometryPropertyName(String name){
        guiBox.setGeometryPropertyName(name);
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        guiBox = new org.geotools.gui.swing.style.sld.JGeomBox();
        lbl = new javax.swing.JLabel();

        guiBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/geotools/gui/swing/style/sld/Bundle"); // NOI18N
        lbl.setText(bundle.getString("geometry")); // NOI18N
        lbl.setEnabled(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(lbl)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(guiBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 51, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(lbl)
                .add(guiBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.geotools.gui.swing.style.sld.JGeomBox guiBox;
    private javax.swing.JLabel lbl;
    // End of variables declaration//GEN-END:variables
    
}
