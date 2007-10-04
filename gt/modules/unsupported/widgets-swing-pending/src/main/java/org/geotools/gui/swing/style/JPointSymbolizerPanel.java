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

import org.geotools.gui.swing.propertyedit.styleproperty.*;
import org.geotools.gui.swing.contexttree.renderer.StyleCellProvider;
import org.geotools.gui.swing.contexttree.renderer.StyleCellRenderer;
import org.geotools.gui.swing.i18n.TextBundle;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JComponent;
import javax.swing.ListSelectionModel;
import org.geotools.gui.swing.style.sld.DemoTableModel;
import org.geotools.map.MapLayer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.SLD;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;
import org.jdesktop.swingx.renderer.ComponentProvider;

/**
 *
 * @author johann sorel
 */
public class JPointSymbolizerPanel extends javax.swing.JPanel implements SymbolizerPanel {

    private MapLayer layer = null;

    /** Creates new form LineStylePanel
     * @param layer
     */
    public JPointSymbolizerPanel(MapLayer layer) {
        initComponents();

        this.layer = layer;

        lbl_gen_alpha.setText(TextBundle.getResource().getString("opacity"));
        lbl_gen_size.setText(TextBundle.getResource().getString("size"));
        lbl_gen_color.setText(TextBundle.getResource().getString("color"));
        lbl_gen_forme.setText(TextBundle.getResource().getString("shape"));

        jcb_forme.addItem("square");
        jcb_forme.addItem("circle");
        jcb_forme.addItem("triangle");
        jcb_forme.addItem("star");
        jcb_forme.addItem("cross");
        jcb_forme.addItem("x");
        jcb_forme.setSelectedItem("cross");

        parse(layer.getStyle());

        jsp_gen_alpha.setMargins(0, 100);
        jsp_gen_alpha.setFloatable(false);
        jsp_gen_size.setMargins(0, 65000);
        jsp_gen_size.setFloatable(false);


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
            PointSymbolizer sym = (PointSymbolizer) symbol;
            
            
            Graphic gra = sym.getGraphic();
            
            
            
            jStrokePanel1.parseStroke( SLD.stroke(sym) );
            
            
            jsp_gen_size.setValue(SLD.pointSize(sym));
            clock.setDegree(    Double.parseDouble( sym.getGraphic().getRotation().toString() ));
            but_gen_color.setBackground(SLD.pointFill(sym));
            jsp_gen_alpha.setValue(SLD.pointOpacity(sym)*100);

            
            Mark mark = SLD.pointMark(layer.getStyle());
            if(mark != null){
                 jcb_forme.setSelectedItem( mark.getWellKnownName().toString() );
            }
            else{
                jcb_forme.setSelectedIndex(0);
            }
        }
    }

    public Symbolizer getSymbolizer(){
        StyleBuilder sb = new StyleBuilder();
        
        Fill fill = sb.createFill(but_gen_color.getBackground(), jsp_gen_alpha.getIntValue()/100f);
        Mark mark = sb.createMark(jcb_forme.getSelectedItem().toString(), fill, jStrokePanel1.getStroke()  );
        Graphic gra = sb.createGraphic();
        gra.setOpacity( sb.literalExpression( jsp_gen_alpha.getIntValue()) );
        gra.setMarks(new Mark[]{mark});
        gra.setRotation(sb.literalExpression(clock.getDegree()));
        gra.setSize(sb.literalExpression(jsp_gen_size.getIntValue()));
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
        jPanel7 = new javax.swing.JPanel();
        jStrokePanel1 = new org.geotools.gui.swing.style.sld.JStrokePanel();
        jPanel1 = new javax.swing.JPanel();
        lbl_gen_forme = new javax.swing.JLabel();
        jcb_forme = new javax.swing.JComboBox();
        jsp_gen_alpha = new org.geotools.gui.swing.extended.JNumberPanel();
        lbl_gen_alpha = new javax.swing.JLabel();
        but_gen_color = new org.geotools.gui.swing.style.sld.JColorPanel();
        lbl_gen_color = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        clock = new org.geotools.gui.swing.extended.JDegreePanel();
        jsp_gen_size = new org.geotools.gui.swing.extended.JNumberPanel();
        lbl_gen_size = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tab_demo = new org.geotools.gui.swing.style.sld.JDemoTable();

        org.jdesktop.layout.GroupLayout jPanel7Layout = new org.jdesktop.layout.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jStrokePanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel7Layout.createSequentialGroup()
                .add(jStrokePanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(53, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(TextBundle.getResource().getString("border"), jPanel7);

        lbl_gen_forme.setText("_");

        lbl_gen_alpha.setText("_");

        org.jdesktop.layout.GroupLayout but_gen_colorLayout = new org.jdesktop.layout.GroupLayout(but_gen_color);
        but_gen_color.setLayout(but_gen_colorLayout);
        but_gen_colorLayout.setHorizontalGroup(
            but_gen_colorLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 56, Short.MAX_VALUE)
        );
        but_gen_colorLayout.setVerticalGroup(
            but_gen_colorLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 18, Short.MAX_VALUE)
        );

        lbl_gen_color.setText("_");

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(lbl_gen_forme)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jcb_forme, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(lbl_gen_color)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(but_gen_color, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 58, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(lbl_gen_alpha)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jsp_gen_alpha, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(173, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lbl_gen_forme)
                    .add(jcb_forme, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(2, 2, 2)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(lbl_gen_color)
                            .add(but_gen_color, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                        .add(lbl_gen_alpha)
                        .add(jsp_gen_alpha, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(163, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(TextBundle.getResource().getString("simple"), jPanel1);

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, TextBundle.getResource().getString("fill"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));

        lbl_gen_size.setText("_");

        org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .add(clock, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lbl_gen_size)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jsp_gen_size, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(130, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(clock, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel6Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(lbl_gen_size)
                            .add(jsp_gen_size, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                    .add(jPanel6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 238, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jPanel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPane1.getAccessibleContext().setAccessibleName("");
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.geotools.gui.swing.style.sld.JColorPanel but_gen_color;
    private org.geotools.gui.swing.extended.JDegreePanel clock;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private org.geotools.gui.swing.style.sld.JStrokePanel jStrokePanel1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JComboBox jcb_forme;
    private org.geotools.gui.swing.extended.JNumberPanel jsp_gen_alpha;
    private org.geotools.gui.swing.extended.JNumberPanel jsp_gen_size;
    private javax.swing.JLabel lbl_gen_alpha;
    private javax.swing.JLabel lbl_gen_color;
    private javax.swing.JLabel lbl_gen_forme;
    private javax.swing.JLabel lbl_gen_size;
    private org.geotools.gui.swing.style.sld.JDemoTable tab_demo;
    // End of variables declaration//GEN-END:variables
}