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

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JColorChooser;
import javax.swing.JComponent;

import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.style.SymbolizerPanel;
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
public class JPointSymbolizerPanel extends javax.swing.JPanel implements SymbolizerPanel {

    private MapLayer layer = null;

    /** Creates new form LineStylePanel
     * @param layer the layer style to edit
     */
    public JPointSymbolizerPanel(MapLayer layer) {
        initComponents();

        this.layer = layer;


        gui_jcb_forme.addItem("square");
        gui_jcb_forme.addItem("circle");
        gui_jcb_forme.addItem("triangle");
        gui_jcb_forme.addItem("star");
        gui_jcb_forme.addItem("cross");
        gui_jcb_forme.addItem("x");
        gui_jcb_forme.setSelectedItem("cross");

        parse(layer.getStyle());

        gui_opacity.setLayer(layer);
        gui_size.setLayer(layer);
        gui_orientation.setLayer(layer);
        gui_stroke.setLayer(layer);
        gui_color.setLayer(layer);
        
        //jsp_gen_alpha.setMargins(0, 100);
        //jsp_gen_alpha.setFloatable(false);
        //jsp_gen_size.setMargins(0, 65000);
        //jsp_gen_size.setFloatable(false);


        tab_demo.setSLDSource("/org/geotools/gui/swing/propertyedit/styleproperty/defaultset/pointstyles.sld");
        
        tab_demo.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                int ligne;
                Point p = e.getPoint();
                ligne = tab_demo.rowAtPoint(p);
                if (ligne < tab_demo.getModel().getRowCount() && ligne >= 0) {
                    parse((Symbolizer) tab_demo.getModel().getValueAt(ligne, 0));
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

    private void parse(Style style) {

        FeatureTypeStyle[] sty = style.getFeatureTypeStyles();

        Rule[] rules = sty[0].getRules();
        for (int i = 0; i < rules.length; i++) {
            Rule r = rules[i];

            //on regarde si la regle s'applique au maplayer (s'il n'y a aucun filtre)
            if (r.getFilter() == null) {
                Symbolizer[] symbolizers = r.getSymbolizers();
                for (int j = 0; j < symbolizers.length; j++) {
                    parse(symbolizers[j]);
                }
            }
        }
    }

    private void parse(Symbolizer symbol) {
        
        
        if (symbol instanceof PointSymbolizer) {
            StyleBuilder sb = new StyleBuilder();
            PointSymbolizer sym = (PointSymbolizer) symbol;
                        
            Graphic gra = sym.getGraphic();
            
            gui_stroke.parseStroke( SLD.stroke(sym) );            
            
            gui_size.setExpression( gra.getSize() );
            gui_orientation.setExpression( gra.getRotation());
            gui_color.setExpression( sb.colorExpression(SLD.pointFill(sym)) );
            gui_opacity.setExpression( gra.getOpacity() );

            
            Mark mark = SLD.pointMark(layer.getStyle());
            if(mark != null){
                 gui_jcb_forme.setSelectedItem( mark.getWellKnownName().toString() );
            }
            else{
                gui_jcb_forme.setSelectedIndex(0);
            }
        }
    }

    public Symbolizer getSymbolizer(){
        StyleBuilder sb = new StyleBuilder();
        
        Fill fill = sb.createFill(gui_color.getExpression(), gui_opacity.getExpression());
        Mark mark = sb.createMark(gui_jcb_forme.getSelectedItem().toString(), fill, gui_stroke.getStroke()  );
        Graphic gra = sb.createGraphic();
        gra.setOpacity( gui_opacity.getExpression() );
        gra.setMarks(new Mark[]{mark});
        gra.setRotation(gui_orientation.getExpression());
        gra.setSize( gui_size.getExpression() );
        PointSymbolizer ps = sb.createPointSymbolizer(gra);
        
        return ps;
    }
    
    
    public Style getStyle() {
        StyleBuilder sb = new StyleBuilder();
        Style style = sb.createStyle();
        style.addFeatureTypeStyle(sb.createFeatureTypeStyle( getSymbolizer() ));
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

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        gui_opacity = new org.geotools.gui.swing.style.sld.JExpressionPanel();
        lbl_gen_alpha = new javax.swing.JLabel();
        lbl_gen_color = new javax.swing.JLabel();
        gui_color = new org.geotools.gui.swing.style.sld.JExpressionPanel();
        jButton1 = new javax.swing.JButton();
        gui_jcb_forme = new javax.swing.JComboBox();
        lbl_gen_forme = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        gui_stroke = new org.geotools.gui.swing.style.sld.JStrokePanel();
        jPanel6 = new javax.swing.JPanel();
        gui_orientation = new org.geotools.gui.swing.style.sld.JDegreePanel();
        lbl_gen_size = new javax.swing.JLabel();
        gui_size = new org.geotools.gui.swing.style.sld.JExpressionPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tab_demo = new org.geotools.gui.swing.style.sld.JDemoTable();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/geotools/gui/swing/style/Bundle"); // NOI18N
        lbl_gen_alpha.setText(bundle.getString("opacity")); // NOI18N

        lbl_gen_color.setText(bundle.getString("color")); // NOI18N

        jButton1.setIcon(IconBundle.getResource().getIcon("JS16_color"));
        jButton1.setMargin(new java.awt.Insets(2, 3, 2, 3));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        lbl_gen_forme.setText(bundle.getString("shape")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(lbl_gen_color)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(gui_color, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE)
                        .add(10, 10, 10)
                        .add(jButton1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(lbl_gen_forme)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(gui_jcb_forme, 0, 220, Short.MAX_VALUE))
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(lbl_gen_alpha)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(gui_opacity, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lbl_gen_forme)
                    .add(gui_jcb_forme, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jButton1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 19, Short.MAX_VALUE)
                    .add(gui_color, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(lbl_gen_color, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(lbl_gen_alpha, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(gui_opacity, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(144, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(bundle.getString("simple"), jPanel1); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel7Layout = new org.jdesktop.layout.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(gui_stroke, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(gui_stroke, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(bundle.getString("border"), jPanel7); // NOI18N

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("fill"))); // NOI18N

        lbl_gen_size.setText(bundle.getString("size")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .add(gui_orientation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 127, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lbl_gen_size)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(gui_size, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 89, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(gui_orientation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel6Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, lbl_gen_size, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, gui_size, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );

        jScrollPane1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        tab_demo.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tab_demo);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jTabbedPane1, 0, 286, Short.MAX_VALUE)
                    .add(jPanel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 353, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(jPanel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jTabbedPane1.getAccessibleContext().setAccessibleName("");
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        StyleBuilder sb = new StyleBuilder();
        JColorChooser choose = new JColorChooser(gui_color.getBackground());

        Color col = Color.WHITE;
        if (gui_color.getExpression() != null) {
            try {
                Color origin = SLD.color(gui_color.getExpression());
                col = JColorChooser.showDialog(null, "", (origin != null) ? origin : Color.WHITE);
            } catch (Exception e) {
            }
        }

        gui_color.setExpression(sb.colorExpression(col));
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.geotools.gui.swing.style.sld.JExpressionPanel gui_color;
    private javax.swing.JComboBox gui_jcb_forme;
    private org.geotools.gui.swing.style.sld.JExpressionPanel gui_opacity;
    private org.geotools.gui.swing.style.sld.JDegreePanel gui_orientation;
    private org.geotools.gui.swing.style.sld.JExpressionPanel gui_size;
    private org.geotools.gui.swing.style.sld.JStrokePanel gui_stroke;
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lbl_gen_alpha;
    private javax.swing.JLabel lbl_gen_color;
    private javax.swing.JLabel lbl_gen_forme;
    private javax.swing.JLabel lbl_gen_size;
    private org.geotools.gui.swing.style.sld.JDemoTable tab_demo;
    // End of variables declaration//GEN-END:variables
}