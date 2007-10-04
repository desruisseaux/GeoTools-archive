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

import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.styling.Fill;
import org.geotools.styling.StyleBuilder;

/**
 * @author  johann sorel
 */
public class JFillPanel extends javax.swing.JPanel {

    /** Creates new form JFillPanel */
    public JFillPanel() {
        initComponents();
                
        GuiFillAlpha.setMargins(0, 1);
        GuiFillAlpha.setFloatable(true);
    }

    
    public void parseFill(Fill fill) {
        if (fill != null) {
            // TODO : not yet implemented
            //fill.getBackgroundColor();
            //fill.getGraphicFill();
            GuiFillColor.setColor(fill.getColor());
            GuiFillAlpha.setValue(fill.getOpacity());
        }
    }

    public Fill getFill() {
        StyleBuilder sb = new StyleBuilder();
        return sb.createFill(GuiFillColor.getExpressionColor(), GuiFillAlpha.getExpressionValue());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lbl_color1 = new javax.swing.JLabel();
        GuiFillColor = new org.geotools.gui.swing.style.sld.JColorPanel();
        GuiFillAlpha = new org.geotools.gui.swing.extended.JNumberPanel();
        lbl_alpha1 = new javax.swing.JLabel();

        lbl_color1.setText(TextBundle.getResource().getString("color"));

        javax.swing.GroupLayout GuiFillColorLayout = new javax.swing.GroupLayout(GuiFillColor);
        GuiFillColor.setLayout(GuiFillColorLayout);
        GuiFillColorLayout.setHorizontalGroup(
            GuiFillColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 61, Short.MAX_VALUE)
        );
        GuiFillColorLayout.setVerticalGroup(
            GuiFillColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 18, Short.MAX_VALUE)
        );

        lbl_alpha1.setText(TextBundle.getResource().getString("opacity"));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lbl_color1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(GuiFillColor, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lbl_alpha1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(GuiFillAlpha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lbl_color1)
                    .addComponent(GuiFillColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(GuiFillAlpha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbl_alpha1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.geotools.gui.swing.extended.JNumberPanel GuiFillAlpha;
    private org.geotools.gui.swing.style.sld.JColorPanel GuiFillColor;
    private javax.swing.JLabel lbl_alpha1;
    private javax.swing.JLabel lbl_color1;
    // End of variables declaration//GEN-END:variables
}