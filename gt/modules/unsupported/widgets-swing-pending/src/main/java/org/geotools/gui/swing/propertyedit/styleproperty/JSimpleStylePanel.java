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

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.style.JLineSymbolizerPanel;
import org.geotools.gui.swing.style.JPointSymbolizerPanel;
import org.geotools.gui.swing.style.JPolygonSymbolizerPanel;
import org.geotools.gui.swing.style.JRasterSymbolizerPanel;
import org.geotools.map.MapLayer;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 *
 * @author  johann sorel
 */
public class JSimpleStylePanel extends javax.swing.JPanel implements StylePanel {

    private MapLayer layer;
    private SymbolizerPanel detail = null;

    /** Creates new form XMLStylePanel */
    public JSimpleStylePanel() {
        initComponents();
        setLayout(new BorderLayout());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 511, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 368, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    public JComponent getPanel() {
        return this;
    }

    public void apply() {
        if(detail != null){
            layer.setStyle(detail.getStyle());
        }
    }

    public ImageIcon getIcon() {
        return IconBundle.getResource().getIcon("16_simplestylepanel");
    }

    public String getTitle() {
        return TextBundle.getResource().getString("simple");
    }

    public void setTarget(MapLayer layer) {
        this.layer = layer;
        parse();
    }

    private void parse() {

        if (layer != null) {
            if (layer.getFeatureSource() != null) {


                Class val = layer.getFeatureSource().getSchema().getDefaultGeometry().getType().getBinding();

                if (layer.getFeatureSource().getSchema().getTypeName().equals("GridCoverage")) {
                    detail = new JRasterSymbolizerPanel(layer);
                    add(BorderLayout.CENTER, detail.getComponent() );
                } else if (val.equals(Polygon.class) || val.equals(MultiPolygon.class)) {
                    detail = new JPolygonSymbolizerPanel(layer);
                   add(BorderLayout.CENTER, detail.getComponent() );
                } else if (val.equals(MultiLineString.class) || val.equals(LineString.class)) {
                    detail = new JLineSymbolizerPanel(layer);
                   add(BorderLayout.CENTER, detail.getComponent() );
                } else if (val.equals(Point.class) || val.equals(MultiPoint.class)) {
                    detail = new JPointSymbolizerPanel(layer);
                    add(BorderLayout.CENTER, detail.getComponent() );
                } else {        
                    detail = null;
                    add(BorderLayout.CENTER,new JLabel("<b>" + TextBundle.getResource().getString("unknown_simplestyle") + "</b>"));
                }
            }
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}