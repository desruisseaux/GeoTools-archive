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
package org.geotools.gui.swing.style;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.Map;
import javax.swing.JComponent;

import org.geotools.gui.swing.style.sld.JExpressionPanel;
import org.geotools.map.MapLayer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;

/**
 *
 * @author johann sorel
 */
public class JPointSymbolizerPanel extends javax.swing.JPanel implements org.geotools.gui.swing.style.SymbolizerPanel {

    private MapLayer layer = null;

    /** Creates new form LineStylePanel
     * @param layer the layer style to edit
     */
    public JPointSymbolizerPanel() {
        initComponents();
        init();
    }

    private void init() {
        gui_color.setType(JExpressionPanel.EXP_TYPE.COLOR);
        gui_opacity.setType(JExpressionPanel.EXP_TYPE.NUMBER);
        gui_size.setType(JExpressionPanel.EXP_TYPE.NUMBER);

        gui_jcb_forme.addItem("square");
        gui_jcb_forme.addItem("circle");
        gui_jcb_forme.addItem("triangle");
        gui_jcb_forme.addItem("star");
        gui_jcb_forme.addItem("cross");
        gui_jcb_forme.addItem("x");
        gui_jcb_forme.setSelectedItem("cross");


        tab_demo.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                int ligne;
                Point p = e.getPoint();
                ligne = tab_demo.rowAtPoint(p);
                if (ligne < tab_demo.getModel().getRowCount() && ligne >= 0) {
                    setSymbolizer((Symbolizer) tab_demo.getModel().getValueAt(ligne, 0));
                }
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }
        });
    }

    public void setDemoSymbolizers(Map<Symbolizer,String> symbols){
        tab_demo.setMap(symbols);        
    }
    
    public Map<Symbolizer,String> getDemoSymbolizers(){
        return tab_demo.getMap();
    }
    
    
    public void setLayer(MapLayer layer) {
        this.layer = layer;
        gui_color.setLayer(layer);
        gui_opacity.setLayer(layer);
        gui_orientation.setLayer(layer);
        gui_size.setLayer(layer);
        gui_stroke.setLayer(layer);
    }

    public MapLayer getLayer() {
        return layer;
    }

    public void setSymbolizer(Symbolizer symbol) {
        if (symbol instanceof PointSymbolizer) {
            StyleBuilder sb = new StyleBuilder();
            PointSymbolizer sym = (PointSymbolizer) symbol;

            Graphic gra = sym.getGraphic();

            gui_stroke.parseStroke(SLD.stroke(sym));

            gui_size.setExpression(gra.getSize());
            gui_orientation.setExpression(gra.getRotation());
            gui_color.setExpression(sb.colorExpression(SLD.pointFill(sym)));
            gui_opacity.setExpression(gra.getOpacity());


            Mark mark = SLD.pointMark(layer.getStyle());
            if (mark != null) {
                gui_jcb_forme.setSelectedItem(mark.getWellKnownName().toString());
            } else {
                gui_jcb_forme.setSelectedIndex(0);
            }
        }
    }

    public Symbolizer getSymbolizer() {
        StyleBuilder sb = new StyleBuilder();

        Fill fill = sb.createFill(gui_color.getExpression(), gui_opacity.getExpression());
        Mark mark = sb.createMark(gui_jcb_forme.getSelectedItem().toString(), fill, gui_stroke.getStroke());
        Graphic gra = sb.createGraphic();
        gra.setOpacity(gui_opacity.getExpression());
        gra.setMarks(new Mark[]{mark});
        gra.setRotation(gui_orientation.getExpression());
        gra.setSize(gui_size.getExpression());
        PointSymbolizer ps = sb.createPointSymbolizer(gra);

        return ps;
    }

    public void setStyle(Style style) {
        FeatureTypeStyle[] sty = style.getFeatureTypeStyles();

        Rule[] rules = sty[0].getRules();
        for (int i = 0; i < rules.length; i++) {
            Rule r = rules[i];

            //on regarde si la regle s'applique au maplayer (s'il n'y a aucun filtre)
            if (r.getFilter() == null) {
                Symbolizer[] symbolizers = r.getSymbolizers();
                for (int j = 0; j < symbolizers.length; j++) {
                    setSymbolizer(symbolizers[j]);
                }
            }
        }
    }

    public Style getStyle() {
        StyleBuilder sb = new StyleBuilder();
        Style style = sb.createStyle();
        style.addFeatureTypeStyle(sb.createFeatureTypeStyle(getSymbolizer()));
        return style;
    }

    public JComponent getComponent() {
        return this;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel6 = new javax.swing.JPanel();
        gui_orientation = new org.geotools.gui.swing.style.sld.JDegreePanel();
        gui_jcb_forme = new javax.swing.JComboBox();
        lbl_gen_forme = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tab_demo = new org.geotools.gui.swing.style.sld.JDemoTable();
        gui_stroke = new org.geotools.gui.swing.style.sld.JStrokePanel();
        jXTitledSeparator1 = new org.jdesktop.swingx.JXTitledSeparator();
        jXTitledSeparator2 = new org.jdesktop.swingx.JXTitledSeparator();
        jPanel1 = new javax.swing.JPanel();
        lbl_gen_size = new javax.swing.JLabel();
        gui_size = new org.geotools.gui.swing.style.sld.JExpressionPanel();
        lbl_gen_color = new javax.swing.JLabel();
        gui_color = new org.geotools.gui.swing.style.sld.JExpressionPanel();
        lbl_gen_alpha = new javax.swing.JLabel();
        gui_opacity = new org.geotools.gui.swing.style.sld.JExpressionPanel();

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/geotools/gui/swing/style/Bundle"); // NOI18N
        lbl_gen_forme.setText(bundle.getString("shape")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel6Layout.createSequentialGroup()
                        .add(lbl_gen_forme)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(gui_jcb_forme, 0, 227, Short.MAX_VALUE))
                    .add(gui_orientation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 126, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lbl_gen_forme)
                    .add(gui_jcb_forme, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(gui_orientation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jScrollPane1.setViewportView(tab_demo);

        jXTitledSeparator1.setTitle(bundle.getString("fill")); // NOI18N

        jXTitledSeparator2.setTitle(bundle.getString("border")); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lbl_gen_size.setText(bundle.getString("size")); // NOI18N

        lbl_gen_color.setText(bundle.getString("color")); // NOI18N

        lbl_gen_alpha.setText(bundle.getString("opacity")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(lbl_gen_color)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(gui_color, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(lbl_gen_alpha)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(gui_opacity, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(lbl_gen_size)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(gui_size, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(139, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, lbl_gen_size, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, gui_size, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(lbl_gen_color, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(gui_color, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(lbl_gen_alpha, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(gui_opacity, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(gui_stroke, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jXTitledSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jXTitledSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 464, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(jXTitledSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(jXTitledSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(gui_stroke, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.geotools.gui.swing.style.sld.JExpressionPanel gui_color;
    private javax.swing.JComboBox gui_jcb_forme;
    private org.geotools.gui.swing.style.sld.JExpressionPanel gui_opacity;
    private org.geotools.gui.swing.style.sld.JDegreePanel gui_orientation;
    private org.geotools.gui.swing.style.sld.JExpressionPanel gui_size;
    private org.geotools.gui.swing.style.sld.JStrokePanel gui_stroke;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private org.jdesktop.swingx.JXTitledSeparator jXTitledSeparator1;
    private org.jdesktop.swingx.JXTitledSeparator jXTitledSeparator2;
    private javax.swing.JLabel lbl_gen_alpha;
    private javax.swing.JLabel lbl_gen_color;
    private javax.swing.JLabel lbl_gen_forme;
    private javax.swing.JLabel lbl_gen_size;
    private org.geotools.gui.swing.style.sld.JDemoTable tab_demo;
    // End of variables declaration//GEN-END:variables
}
