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

import java.awt.Color;
import javax.swing.JColorChooser;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.map.MapLayer;
import org.geotools.styling.Fill;
import org.geotools.styling.SLD;
import org.geotools.styling.StyleBuilder;

/**
 * @author  johann sorel
 */
public class JFillPanel extends javax.swing.JPanel {

    
    /** 
     * Creates new form JFillPanel 
     */
    public JFillPanel() {
        initComponents();
                
    }

    /**
     * 
     * @param layer the layer style to edit
     */
    public void setLayer(MapLayer layer){
        GuiFillColor.setLayer(layer);
        GuiFillAlpha.setLayer(layer);
    }
    
    /**
     * 
     * @param fill The fill to edit
     */
    public void parseFill(Fill fill) {
        if (fill != null) {
            // TODO : not yet implemented
            //fill.getBackgroundColor();
            //fill.getGraphicFill();
            GuiFillColor.setExpression(fill.getColor());
            GuiFillAlpha.setExpression(fill.getOpacity());
            //Graphic graph = fill.getGraphicFill();
            //graph.
        }
    }

    /**
     * 
     * @return Fill : new Fill
     */
    public Fill getFill() {
        StyleBuilder sb = new StyleBuilder();
        return sb.createFill(GuiFillColor.getExpression(), GuiFillAlpha.getExpression());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lbl_color1 = new javax.swing.JLabel();
        lbl_alpha1 = new javax.swing.JLabel();
        GuiFillColor = new org.geotools.gui.swing.style.sld.JExpressionPanel();
        GuiFillAlpha = new org.geotools.gui.swing.style.sld.JExpressionPanel();
        jButton3 = new javax.swing.JButton();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/geotools/gui/swing/style/sld/Bundle"); // NOI18N
        lbl_color1.setText(bundle.getString("color")); // NOI18N

        lbl_alpha1.setText(bundle.getString("opacity")); // NOI18N

        jButton3.setIcon(IconBundle.getResource().getIcon("JS16_color"));
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(lbl_alpha1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(GuiFillAlpha, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(lbl_color1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(GuiFillColor, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)))
                .add(10, 10, 10)
                .add(jButton3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(layout.createSequentialGroup()
                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(jButton3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(GuiFillColor, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(lbl_color1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 19, Short.MAX_VALUE))))
                .add(8, 8, 8)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(GuiFillAlpha, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(lbl_alpha1))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        StyleBuilder sb = new StyleBuilder();
        JColorChooser choose = new JColorChooser(GuiFillColor.getBackground());
        
        Color col = Color.WHITE;
        if (GuiFillColor.getExpression() != null) {
            try {
                Color origin = SLD.color(GuiFillColor.getExpression());
                col = JColorChooser.showDialog(null, "", (origin != null) ? origin : Color.WHITE);
            } catch (Exception e) {
            }
        }
        
        GuiFillColor.setExpression(sb.colorExpression(col));
    }//GEN-LAST:event_jButton3ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.geotools.gui.swing.style.sld.JExpressionPanel GuiFillAlpha;
    private org.geotools.gui.swing.style.sld.JExpressionPanel GuiFillColor;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel lbl_alpha1;
    private javax.swing.JLabel lbl_color1;
    // End of variables declaration//GEN-END:variables
}