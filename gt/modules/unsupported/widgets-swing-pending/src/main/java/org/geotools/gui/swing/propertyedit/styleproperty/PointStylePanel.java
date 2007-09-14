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
import org.geotools.gui.swing.i18n.TextBundle;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JComponent;
import javax.swing.ListSelectionModel;
import org.geotools.gui.swing.propertyedit.styleproperty.defaultset.DemoTableModel;
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
public class PointStylePanel extends javax.swing.JPanel implements DetailPanel {

    private MapLayer layer = null;

    /** Creates new form LineStylePanel
     * @param layer
     */
    public PointStylePanel(MapLayer layer) {
        initComponents();

        this.layer = layer;

        lbl_gen_alpha.setText(TextBundle.getResource().getString("opacity"));
        lbl_gen_size.setText(TextBundle.getResource().getString("size"));
        lbl_gen_color.setText(TextBundle.getResource().getString("color"));
        lbl_bord_alpha.setText(TextBundle.getResource().getString("opacity"));
        lbl_bord_color.setText(TextBundle.getResource().getString("color"));
        lbl_bord_width.setText(TextBundle.getResource().getString("width"));
        lbl_gen_forme.setText(TextBundle.getResource().getString("shape"));

        jcb_forme.addItem("square");
        jcb_forme.addItem("circle");
        jcb_forme.addItem("triangle");
        jcb_forme.addItem("star");
        jcb_forme.addItem("cross");
        jcb_forme.addItem("x");
        jcb_forme.setSelectedItem("cross");

        parse(layer.getStyle());

        jsp_bord_alpha.setMargins(0, 100);
        jsp_bord_alpha.setFloatable(false);
        jsp_bord_width.setMargins(0, 65000);
        jsp_bord_width.setFloatable(false);
        jsp_gen_alpha.setMargins(0, 100);
        jsp_gen_alpha.setFloatable(false);
        jsp_gen_size.setMargins(0, 65000);
        jsp_gen_size.setFloatable(false);


        tab_demo.setModel(new DemoTableModel("/org/geotools/gui/swing/propertyedit/styleproperty/defaultset/pointstyles.sld"));
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

            Stroke stroke = SLD.stroke(sym);
            if (stroke != null) {
                jsp_bord_width.setValue( SLD.pointWidth(sym) );
                jsp_bord_alpha.setValue( SLD.pointBorderOpacity(sym) );
                but_bord_color.setBackground( SLD.pointColor(sym));
                pan_linecap.setlineCap(stroke.getLineCap().toString());
                pan_linejoin.setlineJoin(stroke.getLineJoin().toString());
                pan_dashes.setOffset(stroke.getDashOffset());
                pan_dashes.setDashes(stroke.getDashArray());                
            }

            jsp_gen_size.setValue(SLD.pointSize(sym));
            clock.setDegree(Double.parseDouble(sym.getGraphic().getRotation().toString()));
            but_gen_color.setBackground(SLD.pointFill(sym));
            jsp_gen_alpha.setValue(SLD.pointOpacity(sym));

            
            jcb_forme.setSelectedItem( SLD.pointMark(layer.getStyle()) );
        }
    }

    public Style getStyle() {

        StyleBuilder sb = new StyleBuilder();

        Stroke stroke = sb.createStroke(but_bord_color.getBackground(), jsp_bord_width.getIntValue());
        stroke.setLineCap(pan_linecap.getLinecap());
        stroke.setLineJoin(pan_linejoin.getLineJoin());
        stroke.setDashArray(pan_dashes.getDashes());
        stroke.setDashOffset(pan_dashes.getOffset());
        stroke.setOpacity(sb.literalExpression( jsp_bord_alpha.getIntValue()/100f ));

        Fill fill = sb.createFill(but_gen_color.getBackground(), jsp_gen_alpha.getIntValue()/100f);
        Mark mark = sb.createMark(jcb_forme.getSelectedItem().toString(), fill, stroke);
        Graphic gra = sb.createGraphic();
        gra.setOpacity( sb.literalExpression( jsp_gen_alpha.getIntValue()) );
        gra.setMarks(new Mark[]{mark});
        gra.setRotation(sb.literalExpression(clock.getDegree()));
        gra.setSize(sb.literalExpression(jsp_gen_size.getIntValue()));
        PointSymbolizer ps = sb.createPointSymbolizer(gra);

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

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel7 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jsp_bord_width = new org.geotools.gui.swing.extended.JNumberPanel();
        lbl_bord_width = new javax.swing.JLabel();
        lbl_bord_color = new javax.swing.JLabel();
        lbl_bord_alpha = new javax.swing.JLabel();
        jsp_bord_alpha = new org.geotools.gui.swing.extended.JNumberPanel();
        but_bord_color = new org.geotools.gui.swing.extended.JColorPanel();
        jPanel4 = new javax.swing.JPanel();
        pan_linecap = new org.geotools.gui.swing.extended.JLinecapPanel();
        pan_linejoin = new org.geotools.gui.swing.extended.JLinejoinPanel();
        jSeparator1 = new javax.swing.JSeparator();
        pan_dashes = new org.geotools.gui.swing.extended.JDashPanel();
        jPanel1 = new javax.swing.JPanel();
        lbl_gen_forme = new javax.swing.JLabel();
        jcb_forme = new javax.swing.JComboBox();
        jsp_gen_alpha = new org.geotools.gui.swing.extended.JNumberPanel();
        lbl_gen_alpha = new javax.swing.JLabel();
        but_gen_color = new org.geotools.gui.swing.extended.JColorPanel();
        lbl_gen_color = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        clock = new org.geotools.gui.swing.extended.JDegreePanel();
        jsp_gen_size = new org.geotools.gui.swing.extended.JNumberPanel();
        lbl_gen_size = new javax.swing.JLabel();
        jsp_table = new javax.swing.JScrollPane();
        tab_demo = new org.jdesktop.swingx.JXTable();

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lbl_bord_width.setText("_");

        lbl_bord_color.setText("_");

        lbl_bord_alpha.setText("_");

        org.jdesktop.layout.GroupLayout but_bord_colorLayout = new org.jdesktop.layout.GroupLayout(but_bord_color);
        but_bord_color.setLayout(but_bord_colorLayout);
        but_bord_colorLayout.setHorizontalGroup(
            but_bord_colorLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 61, Short.MAX_VALUE)
        );
        but_bord_colorLayout.setVerticalGroup(
            but_bord_colorLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 18, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jPanel5Layout.createSequentialGroup()
                        .add(lbl_bord_width)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jsp_bord_width, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel5Layout.createSequentialGroup()
                        .add(lbl_bord_color)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(but_bord_color, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE))
                    .add(jPanel5Layout.createSequentialGroup()
                        .add(lbl_bord_alpha)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jsp_bord_alpha, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jsp_bord_width, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lbl_bord_width))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(lbl_bord_color)
                    .add(but_bord_color, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jsp_bord_alpha, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lbl_bord_alpha))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(pan_linecap, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jSeparator1)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, pan_linejoin, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(pan_linecap, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(pan_linejoin, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        pan_dashes.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        org.jdesktop.layout.GroupLayout jPanel7Layout = new org.jdesktop.layout.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel7Layout.createSequentialGroup()
                        .add(jPanel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(pan_dashes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pan_dashes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(57, Short.MAX_VALUE))
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
                .addContainerGap(122, Short.MAX_VALUE))
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
                .addContainerGap(179, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(TextBundle.getResource().getString("simple"), jPanel1);

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, TextBundle.getResource().getString("fill"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(0, 0, 0)));

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
                .addContainerGap(79, Short.MAX_VALUE))
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

        jsp_table.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

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

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jsp_table, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 187, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {jPanel6, jTabbedPane1}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jsp_table, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 358, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(jPanel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 271, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        jTabbedPane1.getAccessibleContext().setAccessibleName("");
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.geotools.gui.swing.extended.JColorPanel but_bord_color;
    private org.geotools.gui.swing.extended.JColorPanel but_gen_color;
    private org.geotools.gui.swing.extended.JDegreePanel clock;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JComboBox jcb_forme;
    private org.geotools.gui.swing.extended.JNumberPanel jsp_bord_alpha;
    private org.geotools.gui.swing.extended.JNumberPanel jsp_bord_width;
    private org.geotools.gui.swing.extended.JNumberPanel jsp_gen_alpha;
    private org.geotools.gui.swing.extended.JNumberPanel jsp_gen_size;
    private javax.swing.JScrollPane jsp_table;
    private javax.swing.JLabel lbl_bord_alpha;
    private javax.swing.JLabel lbl_bord_color;
    private javax.swing.JLabel lbl_bord_width;
    private javax.swing.JLabel lbl_gen_alpha;
    private javax.swing.JLabel lbl_gen_color;
    private javax.swing.JLabel lbl_gen_forme;
    private javax.swing.JLabel lbl_gen_size;
    private org.geotools.gui.swing.extended.JDashPanel pan_dashes;
    private org.geotools.gui.swing.extended.JLinecapPanel pan_linecap;
    private org.geotools.gui.swing.extended.JLinejoinPanel pan_linejoin;
    private org.jdesktop.swingx.JXTable tab_demo;
    // End of variables declaration//GEN-END:variables
}