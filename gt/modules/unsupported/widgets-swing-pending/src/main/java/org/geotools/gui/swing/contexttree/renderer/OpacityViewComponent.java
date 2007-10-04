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

package org.geotools.gui.swing.contexttree.renderer;

import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.JSlider;
import org.geotools.filter.Filters;
import org.geotools.map.MapLayer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Symbolizer;

/**
 *
 * @author johann sorel
 */
public class OpacityViewComponent extends JPanel {

    private MapLayer layer = null;
    private JSlider slide = new JSlider(0, 100);

    /** Creates a new instance of StyleViewComponent */
    public OpacityViewComponent() {
        super();

        slide.setOpaque(false);
        slide.setPaintTicks(true);
        setLayout(new GridLayout(1, 1));
    }

    public void format(Object obj) {
        layer = null;

        removeAll();

        if (obj instanceof MapLayer) {
            layer = (MapLayer) obj;

            double oldopa = 100;

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
                            oldopa = Filters.asDouble(sym.getFill().getOpacity());
                        }

                        if (symbolizers[j] instanceof PointSymbolizer) {
                            PointSymbolizer sym = (PointSymbolizer) symbolizers[j];
                            //oldopa = SLD.pointOpacity(sym);
                            oldopa = Filters.asDouble(sym.getGraphic().getOpacity());
                        }

                        if (symbolizers[j] instanceof LineSymbolizer) {
                            LineSymbolizer sym = (LineSymbolizer) symbolizers[j];
                            oldopa = Filters.asDouble(sym.getStroke().getOpacity());
                        }

                        if (symbolizers[j] instanceof RasterSymbolizer) {
                            RasterSymbolizer sym = (RasterSymbolizer) symbolizers[j];
                            oldopa = Filters.asDouble(sym.getOpacity());
                        }
                    }
                }
            }


            slide.setValue(Double.valueOf(oldopa * 100).intValue());
            add(slide);
        }
    }
}