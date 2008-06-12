/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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

import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.propertyedit.PropertyPane;
import org.geotools.gui.swing.style.JLineSymbolizerPane;
import org.geotools.gui.swing.style.JPointSymbolizerPane;
import org.geotools.gui.swing.style.JPolygonSymbolizerPane;
import org.geotools.gui.swing.style.JRasterSymbolizerPane;
import org.geotools.gui.swing.style.SymbolizerPane;
import org.geotools.map.MapLayer;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import javax.swing.JScrollPane;
import org.geotools.gui.swing.style.JTextSymbolizerPane;
import org.geotools.styling.TextSymbolizer;

/**
 *
 * @author  johann sorel
 */
public class JSimpleStylePanel extends javax.swing.JPanel implements PropertyPane {

    private MapLayer layer;
    private SymbolizerPane detail = null;

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
            .add(0, 0, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    public JComponent getComponent() {
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
        return BUNDLE.getString("simple");
    }

    public void setTarget(Object layer) {
        
        if(layer instanceof MapLayer){
            this.layer = (MapLayer) layer;
            parse();
            }
    }

    private void parse() {

        if (layer != null) {
            if (layer.getFeatureSource() != null) {
                removeAll();
                
                Class val = layer.getFeatureSource().getSchema().getDefaultGeometry().getType().getBinding();

                if (layer.getFeatureSource().getSchema().getName().getLocalPart().equals("GridCoverage")) {
                    detail = new JRasterSymbolizerPane();
                    detail.setLayer(layer);
                    detail.setStyle(layer.getStyle());   
                    JScrollPane jsp = new JScrollPane(detail.getComponent());
                    jsp.setBorder(null);
                    jsp.setViewportBorder(null);
                    add(BorderLayout.CENTER, jsp );
                } else if (val.equals(Polygon.class) || val.equals(MultiPolygon.class)) {
                    detail = new JPolygonSymbolizerPane();
                    detail.setLayer(layer);
                    detail.setStyle(layer.getStyle());   
                    JScrollPane jsp = new JScrollPane(detail.getComponent());
                    jsp.setBorder(null);
                    jsp.setViewportBorder(null);
                    add(BorderLayout.CENTER, jsp );
                } else if (val.equals(MultiLineString.class) || val.equals(LineString.class)) {
                    detail = new JLineSymbolizerPane();
                    detail.setLayer(layer);
                    detail.setStyle(layer.getStyle());   
                    JScrollPane jsp = new JScrollPane(detail.getComponent());
                    jsp.setBorder(null);
                    jsp.setViewportBorder(null);
                    add(BorderLayout.CENTER, jsp );
                } else if (val.equals(Point.class) || val.equals(MultiPoint.class)) {
                    detail = new JPointSymbolizerPane();
                    detail.setLayer(layer);
                    detail.setStyle(layer.getStyle());   
                    JScrollPane jsp = new JScrollPane(detail.getComponent());
                    jsp.setBorder(null);
                    jsp.setViewportBorder(null);
                    add(BorderLayout.CENTER, jsp );
                } else if (val.equals(TextSymbolizer.class) ) {
                    detail = new JTextSymbolizerPane();
                    detail.setLayer(layer);
                    detail.setStyle(layer.getStyle());   
                    JScrollPane jsp = new JScrollPane(detail.getComponent());
                    jsp.setBorder(null);
                    jsp.setViewportBorder(null);
                    add(BorderLayout.CENTER, jsp );
                }else {        
                    detail = null;
                    add(BorderLayout.CENTER,new JLabel("<b>" + BUNDLE.getString("unknown_simplestyle") + "</b>"));
                }
            }
        }
    }

    public void reset() {
        parse();
    }

    public String getToolTip() {
        return "";
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
