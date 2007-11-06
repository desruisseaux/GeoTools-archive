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


import org.geotools.filter.Filters;
import org.geotools.gui.swing.contexttree.renderer.DefaultCellEditor;
import org.geotools.gui.swing.contexttree.renderer.DefaultCellRenderer;
import org.geotools.gui.swing.contexttree.renderer.DefaultHeaderRenderer;
import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.map.MapLayer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;
import org.opengis.filter.expression.Expression;


/**
 * @author johann sorel
 */
public class OpacityTreeTableColumn extends TreeTableColumn {


    /**
     * Creates a new instance of JXVisibleColumn
     */
    public OpacityTreeTableColumn() {
        super();
        
        setHeaderRenderer(new DefaultHeaderRenderer(IconBundle.getResource().getIcon("16_opacity"),null,TextBundle.getResource().getString("col_opacity")));
        setCellRenderer(new DefaultCellRenderer( new OpacityComponent()));
        setCellEditor(new DefaultCellEditor( new OpacityComponent()));

        setEditable(true);
        setResizable(false);
        setMaxWidth(60);
        setMinWidth(60);
        setPreferredWidth(60);
        setWidth(25);
    }

    public void setValue(Object target, Object value) {

        if (target instanceof MapLayer && value instanceof Double) {
            applyOpacity((MapLayer) target, (Double) value);
        }
    }

    public Object getValue(Object target) {

        if (target instanceof MapLayer) {

            MapLayer layer = (MapLayer) target;
            FeatureTypeStyle[] sty = layer.getStyle().getFeatureTypeStyles();
            double valeur = 1d;

            Rule[] rules = sty[0].getRules();
            for (int i = 0; i < rules.length; i++) {
                Rule r = rules[i];

                //on regarde si la regle s'applique au maplayer (s'il n'y a aucun filtre)
                if (r.getFilter() == null) {
                    Symbolizer[] symbolizers = r.getSymbolizers();
                    for (int j = 0; j < symbolizers.length; j++) {

                        if (symbolizers[j] instanceof PolygonSymbolizer) {
                            PolygonSymbolizer sym = (PolygonSymbolizer) symbolizers[j];
                            valeur = Filters.asDouble(sym.getFill().getOpacity());
                        }

                        if (symbolizers[j] instanceof PointSymbolizer) {
                            PointSymbolizer sym = (PointSymbolizer) symbolizers[j];
                            //valeur = SLD.pointOpacity(sym);
                            valeur = Filters.asDouble(sym.getGraphic().getOpacity());
                        }

                        if (symbolizers[j] instanceof LineSymbolizer) {
                            LineSymbolizer sym = (LineSymbolizer) symbolizers[j];
                            valeur = Filters.asDouble(sym.getStroke().getOpacity());
                        }

                        if (symbolizers[j] instanceof RasterSymbolizer) {
                            RasterSymbolizer sym = (RasterSymbolizer) symbolizers[j];
                            valeur = Filters.asDouble(sym.getOpacity());
                        }
                    }
                }

            }
            
            return valeur;
        } else {
            return "n/a";
        }
    }

    public String getName() {
        return TextBundle.getResource().getString("col_symbol");
    }

    public boolean isCellEditable(Object target) {

        if (target instanceof MapLayer) {
            return isEditable();
        } else {
            return false;
        }
    }

    public Class getColumnClass() {
        return Boolean.class;
    }

    @Override
    public boolean isEditableOnMouseOver() {
        return true;
    }

    private void applyOpacity(MapLayer layer, Double d) {
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
                        } else if (symbolizers[j] instanceof PointSymbolizer) {
                            PointSymbolizer sym = (PointSymbolizer) symbolizers[j];
                            sym.getGraphic().setOpacity(opa);

                            Mark[] marks = sym.getGraphic().getMarks();

                            for (int k = 0; k < marks.length; k++) {
                                marks[k].getFill().setOpacity(opa);
                                marks[k].getStroke().setOpacity(opa);
                            }

                        } else if (symbolizers[j] instanceof LineSymbolizer) {
                            LineSymbolizer sym = (LineSymbolizer) symbolizers[j];
                            sym.getStroke().setOpacity(opa);
                        } else if (symbolizers[j] instanceof RasterSymbolizer) {
                            RasterSymbolizer sym = (RasterSymbolizer) symbolizers[j];
                            sym.setOpacity(opa);
                        }
                    }
                }
            }

            layer.setStyle(layer.getStyle());
        }
    }
}

