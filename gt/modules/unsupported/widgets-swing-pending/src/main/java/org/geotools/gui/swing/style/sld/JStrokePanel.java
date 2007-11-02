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
import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.map.MapLayer;
import org.geotools.styling.SLD;
import org.geotools.styling.Stroke;
import org.geotools.styling.StyleBuilder;

/**
 *
 * @author johann sorel
 */
public class JStrokePanel extends javax.swing.JPanel {

    //private StyleBuilder sb = new StyleBuilder();
    //private JExpressionDialog dialog = new JExpressionDialog();
    private MapLayer layer = null;

    /** 
     * Creates new form JStrokePanel 
     */
    public JStrokePanel() {
        super();
        initComponents();

    //GuiStrokeAlpha.setMargins(0, 1);
        //GuiStrokeAlpha.setFloatable(true);
        //GuiStrokeWidth.setMargins(0, 65000);
        //GuiStrokeWidth.setFloatable(false);
    }

    /**
     * 
     * @param layer the layer style to edit
     */
    public void setLayer(MapLayer layer) {
        this.layer = layer;
        GuiStrokeWidth.setLayer(layer);
        GuiStrokeColor.setLayer(layer);
        GuiStrokeAlpha.setLayer(layer);
        GuiStrokeLineCap.setLayer(layer);
        GuiStrokeLineJoin.setLayer(layer);
    }

    /**
     * 
     * @param stroke the stroke to edit
     */
    public void parseStroke(Stroke stroke) {
        if (stroke != null) {
            // TODO : not yet implemented
            //Graphic graf = stroke.getGraphicFill();
            //Graphic gras = stroke.getGraphicStroke();
            GuiStrokeLineCap.setLineCap(stroke.getLineCap());
            GuiStrokeLineJoin.setLineJoin(stroke.getLineJoin());
            GuiStrokeDashes.setDashes(stroke.getDashArray());
            GuiStrokeDashes.setOffset(stroke.getDashOffset());
            GuiStrokeWidth.setExpression(stroke.getWidth());
            GuiStrokeColor.setExpression(stroke.getColor());
            GuiStrokeAlpha.setExpression(stroke.getOpacity());
        }
    }

    /**
     * 
     * @return Stroke : a new Stroke result from the edit panel
     */
    public Stroke getStroke() {
        StyleBuilder sb = new StyleBuilder();

        Stroke stroke = sb.createStroke(GuiStrokeColor.getExpression(), GuiStrokeWidth.getExpression());
        stroke.setLineCap(GuiStrokeLineCap.getLinecap());
        stroke.setLineJoin(GuiStrokeLineJoin.getLineJoin());
        stroke.setDashArray(GuiStrokeDashes.getDashes());
        stroke.setDashOffset(GuiStrokeDashes.getOffset());
        stroke.setOpacity(GuiStrokeAlpha.getExpression());

        return stroke;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        lbl_b_alpha = new javax.swing.JLabel();
        lbl_b_color = new javax.swing.JLabel();
        lbl_b_width = new javax.swing.JLabel();
        GuiStrokeWidth = new org.geotools.gui.swing.style.sld.JExpressionPanel();
        GuiStrokeColor = new org.geotools.gui.swing.style.sld.JExpressionPanel();
        GuiStrokeAlpha = new org.geotools.gui.swing.style.sld.JExpressionPanel();
        jButton3 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        GuiStrokeDashes = new org.geotools.gui.swing.style.sld.JDashPanel();
        jPanel2 = new javax.swing.JPanel();
        lbl_b_alpha1 = new javax.swing.JLabel();
        lbl_b_alpha2 = new javax.swing.JLabel();
        GuiStrokeLineCap = new org.geotools.gui.swing.style.sld.JLinecapPanel();
        GuiStrokeLineJoin = new org.geotools.gui.swing.style.sld.JLinejoinPanel();
        jSeparator2 = new javax.swing.JSeparator();

        lbl_b_alpha.setText(TextBundle.getResource().getString("opacity"));

        lbl_b_color.setText(TextBundle.getResource().getString("color"));

        lbl_b_width.setText(TextBundle.getResource().getString("width"));

        jButton3.setIcon(IconBundle.getResource().getIcon("JS16_color"));
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lbl_b_color)
                            .add(lbl_b_alpha))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(GuiStrokeColor, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jButton3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(GuiStrokeAlpha, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(lbl_b_width)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(GuiStrokeWidth, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lbl_b_width, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(GuiStrokeWidth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(GuiStrokeColor, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jButton3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 19, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, lbl_b_color, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 19, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lbl_b_alpha)
                    .add(GuiStrokeAlpha, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(new java.awt.Component[] {GuiStrokeWidth, lbl_b_alpha, lbl_b_color, lbl_b_width}, org.jdesktop.layout.GroupLayout.VERTICAL);

        lbl_b_alpha1.setText(TextBundle.getResource().getString("linecap"));

        lbl_b_alpha2.setText(TextBundle.getResource().getString("linejoin"));

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(lbl_b_alpha1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(GuiStrokeLineCap, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(83, 83, 83))
            .add(jPanel2Layout.createSequentialGroup()
                .add(lbl_b_alpha2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(GuiStrokeLineJoin, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(83, 83, 83))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, lbl_b_alpha1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, GuiStrokeLineCap, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, lbl_b_alpha2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, GuiStrokeLineJoin, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(47, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(GuiStrokeDashes, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 69, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(GuiStrokeDashes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        StyleBuilder sb = new StyleBuilder();
        JColorChooser choose = new JColorChooser(GuiStrokeColor.getBackground());

        Color col = Color.WHITE;
        if (GuiStrokeColor.getExpression() != null) {
            try {
                Color origin = SLD.color(GuiStrokeColor.getExpression());
                col = JColorChooser.showDialog(null, "", (origin != null) ? origin : Color.WHITE);
            } catch (Exception e) {
            }
        }

        GuiStrokeColor.setExpression(sb.colorExpression(col));
    }//GEN-LAST:event_jButton3ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.geotools.gui.swing.style.sld.JExpressionPanel GuiStrokeAlpha;
    private org.geotools.gui.swing.style.sld.JExpressionPanel GuiStrokeColor;
    private org.geotools.gui.swing.style.sld.JDashPanel GuiStrokeDashes;
    private org.geotools.gui.swing.style.sld.JLinecapPanel GuiStrokeLineCap;
    private org.geotools.gui.swing.style.sld.JLinejoinPanel GuiStrokeLineJoin;
    private org.geotools.gui.swing.style.sld.JExpressionPanel GuiStrokeWidth;
    private javax.swing.JButton jButton3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lbl_b_alpha;
    private javax.swing.JLabel lbl_b_alpha1;
    private javax.swing.JLabel lbl_b_alpha2;
    private javax.swing.JLabel lbl_b_color;
    private javax.swing.JLabel lbl_b_width;
    // End of variables declaration//GEN-END:variables
}