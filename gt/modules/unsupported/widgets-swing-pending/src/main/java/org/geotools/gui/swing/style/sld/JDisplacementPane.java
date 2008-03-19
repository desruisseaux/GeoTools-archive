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

import org.geotools.gui.swing.style.StyleFeatureEditor;
import org.geotools.map.MapLayer;
import org.geotools.styling.Displacement;
import org.geotools.styling.StyleBuilder;

/**
 *
 * @author johann sorel
 */
public class JDisplacementPane extends javax.swing.JPanel implements StyleFeatureEditor{
    
    private MapLayer layer = null;
    private Displacement displacement = null;
    
    /** Creates new form JDisplacementPanel */
    public JDisplacementPane() {
        initComponents();
        init();
    }
    
    private void init(){
        guiX.setType(JExpressionPane.EXP_TYPE.NUMBER);
        guiY.setType(JExpressionPane.EXP_TYPE.NUMBER);
    }
    
    public void setLayer(MapLayer layer){
        guiX.setLayer(layer);
        guiY.setLayer(layer);
        this.layer = layer;
    }
    
    public MapLayer getLayer(){
        return layer;
    }
    
    public void setDisplacement(Displacement disp){
        this.displacement = disp;
        
        if(displacement != null){
            guiX.setExpression(disp.getDisplacementX());
            guiY.setExpression(disp.getDisplacementY());            
        }
    }
    
    public Displacement getDisplacement(){
        
        if(displacement == null){
            StyleBuilder sb = new StyleBuilder();
            displacement = sb.createDisplacement(0,0);
        }
        
        apply();
        return displacement;
    }
            
    public void apply(){
        if(displacement != null){
            displacement.setDisplacementX(guiX.getExpression());
            displacement.setDisplacementY(guiY.getExpression());
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        guiX = new org.geotools.gui.swing.style.sld.JExpressionPane();
        guiY = new org.geotools.gui.swing.style.sld.JExpressionPane();

        setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/geotools/gui/swing/style/sld/Bundle"); // NOI18N
        jLabel1.setText(bundle.getString("x")); // NOI18N

        jLabel2.setText(bundle.getString("y")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(guiX, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(guiY, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(guiX, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(guiY, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.geotools.gui.swing.style.sld.JExpressionPane guiX;
    private org.geotools.gui.swing.style.sld.JExpressionPane guiY;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    // End of variables declaration//GEN-END:variables
    
}
