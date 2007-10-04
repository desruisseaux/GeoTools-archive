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

package org.geotools.gui.swing.contexttree.column;

import java.awt.event.FocusEvent;
import javax.swing.event.ChangeEvent;
import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.gui.swing.icon.IconBundle;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.geotools.filter.Filters;
import org.geotools.gui.swing.contexttree.renderer.ColumnHeader;
import org.geotools.gui.swing.contexttree.renderer.HeaderRenderer;
import org.geotools.gui.swing.contexttree.renderer.OpacityCellProvider;
import org.geotools.gui.swing.contexttree.renderer.OpacityCellRenderer;
import org.geotools.gui.swing.contexttree.renderer.StyleCellProvider;
import org.geotools.gui.swing.contexttree.renderer.StyleCellRenderer;
import org.geotools.gui.swing.propertyedit.JPropertyDialog;
import org.geotools.gui.swing.propertyedit.LayerStylePropertyPanel;
import org.geotools.gui.swing.propertyedit.PropertyPanel;
import org.geotools.map.MapLayer;
import org.geotools.styling.AnchorPoint;
import org.geotools.styling.ColorMap;
import org.geotools.styling.ColorMapEntry;
import org.geotools.styling.Displacement;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.FeatureTypeConstraint;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.Halo;
import org.geotools.styling.LinePlacement;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.NamedLayer;
import org.geotools.styling.PointPlacement;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.SLD;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleVisitor;
import org.geotools.styling.StyledLayerDescriptor;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;
import org.geotools.styling.UserLayer;
import org.jdesktop.swingx.renderer.ComponentProvider;
import org.jdesktop.swingx.table.TableColumnExt;
import org.opengis.filter.expression.Expression;

/**
 * @author johann sorel
 */
public class OpacityColumnModel implements ColumnModel {

    private boolean edit = true;
    private TableColumnExt col = new TableColumnExt();

    /**
     * Creates a new instance of JXVisibleColumn
     */
    public OpacityColumnModel() {
        ColumnHeader head1 = new ColumnHeader(TextBundle.getResource().getString("col_opacity"), new JLabel(IconBundle.getResource().getIcon("16_opacity")));

        TableCellRenderer headerRenderer = new HeaderRenderer(head1);

        col.setHeaderValue(head1);
        col.setHeaderRenderer(headerRenderer);

        ComponentProvider myProvider = new OpacityCellProvider();
        col.setCellRenderer(new OpacityCellRenderer(myProvider));
        col.setCellEditor(new Edito());

        col.setResizable(false);
        col.setMaxWidth(60);
        col.setMinWidth(60);
        col.setPreferredWidth(60);
        col.setWidth(25);
    }

    public void setValue(Object target, Object value) {
    }

    public Object getValue(Object target) {

        if (target instanceof MapLayer) {
            return (MapLayer) target;
        } else {
            return "n/a";
        }
    }

    public String getName() {
        return TextBundle.getResource().getString("col_symbol");
    }

    public boolean isEditable() {
        return edit;
    }

    public boolean isCellEditable(Object target) {

        if (target instanceof MapLayer) {
            return edit;
        } else {
            return false;
        }
    }

    public void setEditable(boolean edit) {
        this.edit = edit;
    }

    public Class getColumnClass() {
        return Boolean.class;
    }

    public TableColumnExt getTableColumnExt() {
        return col;
    }
}

class Edito extends AbstractCellEditor implements TableCellEditor {

    private JSlider slide = new JSlider(0, 100);
    private MapLayer layer = null;
    private boolean changed = false;
    private double oldopa = 100;

    public Edito() {
        super();

        slide.setOpaque(false);
        slide.setPaintTicks(true);
        slide.addFocusListener( new FocusListener() {

            public void focusGained(FocusEvent e) {
                changed = false;
            }

            public void focusLost(FocusEvent e) {
                
                if(changed){
                    int i = slide.getValue();
                    double d = i / 100d;
                    applyOpacity(d);
                }
            }
        });
                
        slide.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                changed = true;
            }
        });
    }

    private void applyOpacity(double d) {
        StyleBuilder sb = new StyleBuilder();
        Expression opa = sb.literalExpression(d);

        if (layer != null) {

            FeatureTypeStyle[] sty = layer.getStyle().getFeatureTypeStyles();

            Rule[] rules = sty[0].getRules();
            for (int i = 0; i < rules.length; i++) {
                Rule r = rules[i];

                //on regarde si la regle s'applique au maplayer (s'il n'y a aucun filtre)
                if (r.getFilter() == null) {
                    Symbolizer[] symbolizers = r.getSymbolizers();
                    for (int j = 0; j < symbolizers.length; j++) {

                        if (symbolizers[j] instanceof PolygonSymbolizer) {
                            PolygonSymbolizer sym = (PolygonSymbolizer) symbolizers[j];
                            sym.getFill().setOpacity(opa);
                            sym.getStroke().setOpacity(opa);
                        }
                        
                        else if (symbolizers[j] instanceof PointSymbolizer) {
                            PointSymbolizer sym = (PointSymbolizer) symbolizers[j];
                            sym.getGraphic().setOpacity(opa);
                                                        
                            Mark[] marks = sym.getGraphic().getMarks();
                                                        
                            for(int k=0; k<marks.length; k++){                                                                
                                marks[k].getFill().setOpacity(opa);
                                marks[k].getStroke().setOpacity(opa);
                            }
                            
                        }
                        
                        else if (symbolizers[j] instanceof LineSymbolizer) {
                            LineSymbolizer sym = (LineSymbolizer) symbolizers[j];
                            sym.getStroke().setOpacity(opa);
                        }
                        
                        else if (symbolizers[j] instanceof RasterSymbolizer) {
                            RasterSymbolizer sym = ( RasterSymbolizer) symbolizers[j];
                            sym.setOpacity(opa);
                        }
                    }
                }
            }
            
            layer.setStyle( layer.getStyle());
        }
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

        layer = (value instanceof MapLayer) ? (MapLayer) value : null;

        if (layer != null) {

            FeatureTypeStyle[] sty = layer.getStyle().getFeatureTypeStyles();

            Rule[] rules = sty[0].getRules();
            for (int i = 0; i < rules.length; i++) {
                Rule r = rules[i];

                //on regarde si la regle s'applique au maplayer (s'il n'y a aucun filtre)
                if (r.getFilter() == null) {
                    Symbolizer[] symbolizers = r.getSymbolizers();
                    for (int j = 0; j < symbolizers.length; j++) {

                        if (symbolizers[j] instanceof PolygonSymbolizer) {
                            PolygonSymbolizer sym = (PolygonSymbolizer) symbolizers[j];
                            oldopa = Filters.asDouble(sym.getFill().getOpacity() );
                        }
                        
                        if (symbolizers[j] instanceof PointSymbolizer) {
                            PointSymbolizer sym = (PointSymbolizer) symbolizers[j];
                            //oldopa = SLD.pointOpacity(sym);
                            oldopa = Filters.asDouble(sym.getGraphic().getOpacity() );
                        }
                        
                        if (symbolizers[j] instanceof LineSymbolizer) {
                            LineSymbolizer sym = (LineSymbolizer) symbolizers[j];
                            oldopa = Filters.asDouble(sym.getStroke().getOpacity() );
                        }
                        
                        if (symbolizers[j] instanceof RasterSymbolizer) {
                            RasterSymbolizer sym = ( RasterSymbolizer) symbolizers[j];
                            oldopa = Filters.asDouble(sym.getOpacity() );
                        }
                    }
                }
            }
            
        }

        slide.setValue( Double.valueOf(oldopa*100).intValue() );
        
        return slide;
    }

    public Object getCellEditorValue() {
        return null;
    }
}