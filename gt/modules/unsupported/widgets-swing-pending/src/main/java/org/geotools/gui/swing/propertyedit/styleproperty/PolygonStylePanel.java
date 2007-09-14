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

package org.geotools.gui.swing.propertyedit.styleproperty;

import org.geotools.gui.swing.contexttree.renderer.StyleCellProvider;
import org.geotools.gui.swing.contexttree.renderer.StyleCellRenderer;
import org.geotools.gui.swing.propertyedit.styleproperty.defaultset.DemoTableModel;
import org.geotools.gui.swing.i18n.TextBundle;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JComponent;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import org.geotools.map.MapLayer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.SLD;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;
import org.jdesktop.swingx.renderer.ComponentProvider;

/**
 *
 * @author  johann sorel
 */
public class PolygonStylePanel extends javax.swing.JPanel implements DetailPanel {

    private int b_width = 2;
    private double b_opa = 100d;
    private double i_opa = 100d;
    private String linecap = "round";
    private String linejoin = "round";
    private float[] dashes = new float[]{15f, 5f};
    private MapLayer layer = null;

    /** Creates new form LineStylePanel
     * @param layer
     */
    public PolygonStylePanel(MapLayer layer) {
        initComponents();
        this.layer = layer;

        lbl_b_alpha.setText(TextBundle.getResource().getString("opacity"));
        lbl_b_color.setText(TextBundle.getResource().getString("color"));
        lbl_b_width.setText(TextBundle.getResource().getString("width"));
        ((TitledBorder) pan_contour.getBorder()).setTitle(TextBundle.getResource().getString("border"));

        lbl_alpha1.setText(TextBundle.getResource().getString("opacity"));
        lbl_color1.setText(TextBundle.getResource().getString("color"));
        ((TitledBorder) pan_interieur.getBorder()).setTitle(TextBundle.getResource().getString("fill"));
        
        parse(layer.getStyle());

        jsp_b_alpha.setMargins(0, 100);
        jsp_b_alpha.setFloatable(false);
        jsp_b_width.setMargins(0, 65000);
        jsp_b_width.setFloatable(false);
        jsp_i_alpha.setMargins(0, 100);
        jsp_i_alpha.setFloatable(false);



       


        tab_demo.setModel(new DemoTableModel("/org/geotools/gui/swing/propertyedit/styleproperty/defaultset/polygonstyles.sld"));
        tab_demo.setHorizontalScrollEnabled(false);
        tab_demo.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ComponentProvider myProvider = new StyleCellProvider();
        tab_demo.getColumnExt(0).setCellRenderer(new StyleCellRenderer(myProvider));
        tab_demo.getColumnExt(0).setMaxWidth(25);
        tab_demo.getColumnExt(0).setMinWidth(25);
        tab_demo.getColumnExt(0).setPreferredWidth(25);
        tab_demo.getColumnExt(0).setWidth(25);
        tab_demo.setTableHeader(null);
        tab_demo.setGridColor(Color.LIGHT_GRAY);
        tab_demo.setShowVerticalLines(false);
        tab_demo.setColumnMargin(0);
        tab_demo.setRowMargin(0);

        tab_demo.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                int ligne;
                Point p = e.getPoint();
                ligne = tab_demo.rowAtPoint(p);
                if(ligne<tab_demo.getModel().getRowCount() && ligne>=0)
                parse((Symbolizer) tab_demo.getModel().getValueAt(ligne, 0));
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

        if (symbol instanceof PolygonSymbolizer) {
            PolygonSymbolizer sym = (PolygonSymbolizer) symbol;

            Stroke stroke = SLD.stroke(sym);
            b_width = SLD.polyWidth(sym);
            b_opa = SLD.polyBorderOpacity(sym);        
            
            
            linecap = stroke.getLineCap().toString();
            linejoin = stroke.getLineJoin().toString();
            dashes = stroke.getDashArray();
           
            pan_linecap.setlineCap(linecap);
            pan_linejoin.setlineJoin(linejoin);
            pan_dashes.setDashes(dashes);
            pan_dashes.setOffset(stroke.getDashOffset());
            
            but_b_color.setBackground( SLD.polyColor(sym) );            
            jsp_b_alpha.setValue( new Double(b_opa*100).floatValue());
            jsp_b_width.setValue(b_width);
            
            i_opa = SLD.polyFillOpacity(sym);
            but_i_color.setBackground( SLD.polyFill(sym) );
            jsp_i_alpha.setValue( new Double(i_opa*100).floatValue() );

        }
    }

    public Style getStyle() {

        b_width = jsp_b_width.getIntValue();
        b_opa = jsp_b_alpha.getIntValue() / 100d;
        i_opa = jsp_i_alpha.getIntValue() / 100d;

        StyleBuilder sb = new StyleBuilder();

        Stroke stroke = sb.createStroke( but_b_color.getBackground(), b_width);  
        stroke.setLineCap(pan_linecap.getLinecap());
        stroke.setLineJoin(pan_linejoin.getLineJoin());
        stroke.setDashArray(pan_dashes.getDashes());
        stroke.setDashOffset(pan_dashes.getOffset());
        stroke.setOpacity(sb.literalExpression(b_opa));
        

        Symbolizer ps = sb.createPolygonSymbolizer(stroke, sb.createFill(but_i_color.getBackground(), i_opa));
        
        Style style = sb.createStyle();
        style.addFeatureTypeStyle(sb.createFeatureTypeStyle(ps));

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

        jPanel2 = new javax.swing.JPanel();
        jsp_table = new javax.swing.JScrollPane();
        tab_demo = new org.jdesktop.swingx.JXTable();
        pan_interieur = new javax.swing.JPanel();
        lbl_alpha1 = new javax.swing.JLabel();
        jsp_i_alpha = new org.geotools.gui.swing.extended.JNumberPanel();
        but_i_color = new org.geotools.gui.swing.extended.JColorPanel();
        lbl_color1 = new javax.swing.JLabel();
        pan_contour = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        pan_linecap = new org.geotools.gui.swing.extended.JLinecapPanel();
        pan_linejoin = new org.geotools.gui.swing.extended.JLinejoinPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel4 = new javax.swing.JPanel();
        jsp_b_width = new org.geotools.gui.swing.extended.JNumberPanel();
        lbl_b_width = new javax.swing.JLabel();
        lbl_b_color = new javax.swing.JLabel();
        lbl_b_alpha = new javax.swing.JLabel();
        jsp_b_alpha = new org.geotools.gui.swing.extended.JNumberPanel();
        but_b_color = new org.geotools.gui.swing.extended.JColorPanel();
        pan_dashes = new org.geotools.gui.swing.extended.JDashPanel();

        jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        tab_demo.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Style", "Name"
            }
        ));
        jsp_table.setViewportView(tab_demo);

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jsp_table, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jsp_table, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
        );

        pan_interieur.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Interieur", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(0, 0, 0)));

        lbl_alpha1.setText("_");

        org.jdesktop.layout.GroupLayout but_i_colorLayout = new org.jdesktop.layout.GroupLayout(but_i_color);
        but_i_color.setLayout(but_i_colorLayout);
        but_i_colorLayout.setHorizontalGroup(
            but_i_colorLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 61, Short.MAX_VALUE)
        );
        but_i_colorLayout.setVerticalGroup(
            but_i_colorLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 18, Short.MAX_VALUE)
        );

        lbl_color1.setText("_");

        org.jdesktop.layout.GroupLayout pan_interieurLayout = new org.jdesktop.layout.GroupLayout(pan_interieur);
        pan_interieur.setLayout(pan_interieurLayout);
        pan_interieurLayout.setHorizontalGroup(
            pan_interieurLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pan_interieurLayout.createSequentialGroup()
                .addContainerGap()
                .add(pan_interieurLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(pan_interieurLayout.createSequentialGroup()
                        .add(lbl_color1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(but_i_color, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE))
                    .add(pan_interieurLayout.createSequentialGroup()
                        .add(lbl_alpha1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jsp_i_alpha, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(174, Short.MAX_VALUE))
        );
        pan_interieurLayout.setVerticalGroup(
            pan_interieurLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pan_interieurLayout.createSequentialGroup()
                .add(pan_interieurLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(lbl_color1)
                    .add(but_i_color, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pan_interieurLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jsp_i_alpha, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lbl_alpha1))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pan_contour.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Contour", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(0, 0, 0)));

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(pan_linecap, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jSeparator1)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, pan_linejoin, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(pan_linecap, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 8, Short.MAX_VALUE)
                .add(pan_linejoin, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lbl_b_width.setText("_");

        lbl_b_color.setText("_");

        lbl_b_alpha.setText("_");

        org.jdesktop.layout.GroupLayout but_b_colorLayout = new org.jdesktop.layout.GroupLayout(but_b_color);
        but_b_color.setLayout(but_b_colorLayout);
        but_b_colorLayout.setHorizontalGroup(
            but_b_colorLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 61, Short.MAX_VALUE)
        );
        but_b_colorLayout.setVerticalGroup(
            but_b_colorLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 18, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(lbl_b_width)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jsp_b_width, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(lbl_b_color)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(but_b_color, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE))
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(lbl_b_alpha)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jsp_b_alpha, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(35, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jsp_b_width, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lbl_b_width))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(lbl_b_color)
                    .add(but_b_color, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jsp_b_alpha, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lbl_b_alpha))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pan_dashes.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        org.jdesktop.layout.GroupLayout pan_contourLayout = new org.jdesktop.layout.GroupLayout(pan_contour);
        pan_contour.setLayout(pan_contourLayout);
        pan_contourLayout.setHorizontalGroup(
            pan_contourLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, pan_contourLayout.createSequentialGroup()
                .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 127, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(pan_dashes, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
        );
        pan_contourLayout.setVerticalGroup(
            pan_contourLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pan_contourLayout.createSequentialGroup()
                .add(pan_contourLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pan_dashes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(pan_contour, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(pan_interieur, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(pan_contour, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(pan_interieur, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.geotools.gui.swing.extended.JColorPanel but_b_color;
    private org.geotools.gui.swing.extended.JColorPanel but_i_color;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JSeparator jSeparator1;
    private org.geotools.gui.swing.extended.JNumberPanel jsp_b_alpha;
    private org.geotools.gui.swing.extended.JNumberPanel jsp_b_width;
    private org.geotools.gui.swing.extended.JNumberPanel jsp_i_alpha;
    private javax.swing.JScrollPane jsp_table;
    private javax.swing.JLabel lbl_alpha1;
    private javax.swing.JLabel lbl_b_alpha;
    private javax.swing.JLabel lbl_b_color;
    private javax.swing.JLabel lbl_b_width;
    private javax.swing.JLabel lbl_color1;
    private javax.swing.JPanel pan_contour;
    private org.geotools.gui.swing.extended.JDashPanel pan_dashes;
    private javax.swing.JPanel pan_interieur;
    private org.geotools.gui.swing.extended.JLinecapPanel pan_linecap;
    private org.geotools.gui.swing.extended.JLinejoinPanel pan_linejoin;
    private org.jdesktop.swingx.JXTable tab_demo;
    // End of variables declaration//GEN-END:variables
}