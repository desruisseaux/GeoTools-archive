/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
/**
 *
 */
package org.geotools.renderer.shape;

import org.geotools.filter.Expression;
import org.geotools.filter.LiteralExpression;
import org.geotools.styling.AnchorPoint;
import org.geotools.styling.Displacement;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.Halo;
import org.geotools.styling.LinePlacement;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointPlacement;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleVisitor;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;


/**
 * Searches for symbolizers with opacity.
 *
 * @author jones
 */
public class OpacityFinder implements StyleVisitor {
    private Class[] acceptableTypes;
    public boolean hasOpacity;

    public OpacityFinder(Class[] acceptableTypes) {
        this.acceptableTypes = acceptableTypes;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.Style)
     */
    public void visit(Style style) {
        FeatureTypeStyle[] styles = style.getFeatureTypeStyles();

        for (int i = 0; i < styles.length; i++) {
            if (hasOpacity) {
                break;
            }

            styles[i].accept(this);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.Rule)
     */
    public void visit(Rule rule) {
        Symbolizer[] symbs = rule.getSymbolizers();

        for (int i = 0; i < symbs.length; i++) {
            if (hasOpacity) {
                break;
            }

            symbs[i].accept(this);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.FeatureTypeStyle)
     */
    public void visit(FeatureTypeStyle fts) {
        Rule[] rules = fts.getRules();

        for (int i = 0; i < rules.length; i++) {
            if (hasOpacity) {
                break;
            }

            rules[i].accept(this);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.Fill)
     */
    public void visit(Fill fill) {
        checkOpacity(fill.getOpacity());
    }

    private void checkOpacity(Expression exp) {
        if (exp != null) {
            if (exp instanceof LiteralExpression) {
                LiteralExpression literal = (LiteralExpression) exp;
                Object obj = literal.getLiteral();
                float opacity;

                if (obj instanceof Integer) {
                    Integer i = (Integer) obj;
                    opacity = i.floatValue();
                } else if (obj instanceof Float) {
                    Float i = (Float) obj;
                    opacity = i.floatValue();
                } else if (obj instanceof Double) {
                    Double i = (Double) obj;
                    opacity = i.floatValue();
                } else if (obj instanceof Short) {
                    Short i = (Short) obj;
                    opacity = i.floatValue();
                } else if (obj instanceof Byte) {
                    Byte i = (Byte) obj;
                    opacity = i.floatValue();
                } else {
                    return;
                }

                if ((opacity > 0.01) && (opacity < 0.99)) {
                    this.hasOpacity = true;
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.Stroke)
     */
    public void visit(Stroke stroke) {
        checkOpacity(stroke.getOpacity());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.Symbolizer)
     */
    public void visit(Symbolizer sym) {
        if (sym instanceof PointSymbolizer) {
            PointSymbolizer ps = (PointSymbolizer) sym;
            ps.accept(this);
        }

        if (sym instanceof LineSymbolizer) {
            LineSymbolizer ps = (LineSymbolizer) sym;
            ps.accept(this);
        }

        if (sym instanceof PolygonSymbolizer) {
            PolygonSymbolizer ps = (PolygonSymbolizer) sym;
            ps.accept(this);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.PointSymbolizer)
     */
    public void visit(PointSymbolizer ps) {
        if (isAcceptable(ps)) {
            ps.getGraphic().accept(this);
        }
    }

    private boolean isAcceptable(Symbolizer s) {
        for (int i = 0; i < acceptableTypes.length; i++) {
            Class type = acceptableTypes[i];

            if (type.isAssignableFrom(s.getClass())) {
                return true;
            }
        }

        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.LineSymbolizer)
     */
    public void visit(LineSymbolizer line) {
        if (isAcceptable(line)) {
            line.getStroke().accept(this);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.PolygonSymbolizer)
     */
    public void visit(PolygonSymbolizer poly) {
        if (isAcceptable(poly)) {
            poly.getStroke().accept(this);
            poly.getFill().accept(this);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.TextSymbolizer)
     */
    public void visit(TextSymbolizer text) {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.RasterSymbolizer)
     */
    public void visit(RasterSymbolizer raster) {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     *
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.Graphic)
     */
    public void visit(Graphic gr) {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     *
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.Mark)
     */
    public void visit(Mark mark) {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     *
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.ExternalGraphic)
     */
    public void visit(ExternalGraphic exgr) {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     *
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.PointPlacement)
     */
    public void visit(PointPlacement pp) {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     *
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.AnchorPoint)
     */
    public void visit(AnchorPoint ap) {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     *
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.Displacement)
     */
    public void visit(Displacement dis) {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     *
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.LinePlacement)
     */
    public void visit(LinePlacement lp) {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     *
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.Halo)
     */
    public void visit(Halo halo) {
        // TODO Auto-generated method stub
    }

    /**
     * DOCUMENT ME!
     *
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
    }
}
