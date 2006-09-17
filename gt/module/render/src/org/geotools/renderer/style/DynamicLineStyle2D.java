/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.renderer.style;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Paint;

import org.geotools.feature.Feature;
import org.geotools.filter.Expression;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Stroke;


/**
 * A dynamic line style, that will compute its parameters each time they are requested instead of
 * caching them
 *
 * @author jamesm
 * @source $URL$
 */
public class DynamicLineStyle2D extends org.geotools.renderer.style.LineStyle2D {
    /** The feature that will be styled as a polygon */
    protected Feature feature;

    /** The line symbolizer used to get stroke/composite/... */
    protected LineSymbolizer ls;

    /**
     * Creates a new instance of DynamicLineStyle2D
     */
    public DynamicLineStyle2D(Feature feature, LineSymbolizer sym) {
        this.feature = feature;
        ls = sym;
    }

    /**
     * Computes and returns the stroke
     */
    public java.awt.Stroke getStroke() {
        Stroke stroke = ls.getStroke();

        if (stroke == null) {
            return null;
        }

        // resolve join type into a join code
        String joinType;
        int joinCode;

        joinType = evaluateExpression(stroke.getLineJoin(), feature, "miter");

        joinCode = SLDStyleFactory.lookUpJoin(joinType);

        // resolve cap type into a cap code
        String capType;
        int capCode;

        capType = evaluateExpression(stroke.getLineCap(), feature, "square");
        capCode = SLDStyleFactory.lookUpCap(capType);

        // get the other properties needed for the stroke
        float[] dashes = stroke.getDashArray();
        float width = ((Number) stroke.getWidth().getValue(feature)).floatValue();
        float dashOffset = ((Number) stroke.getDashOffset().getValue(feature)).floatValue();

        // Simple optimization: let java2d use the fast drawing path if the line width
        // is small enough...
        if (width <= 1) {
            width = 0;
        }

        // now set up the stroke
        BasicStroke stroke2d;

        if ((dashes != null) && (dashes.length > 0)) {
            stroke2d = new BasicStroke(width, capCode, joinCode, 1, dashes, dashOffset);
        } else {
            stroke2d = new BasicStroke(width, capCode, joinCode, 1);
        }

        return stroke2d;
    }

    /**
     * Computes and returns the contour style
     */
    public java.awt.Composite getContourComposite() {
        Stroke stroke = ls.getStroke();

        if (stroke == null) {
            return null;
        }

        float opacity = ((Number) stroke.getOpacity().getValue(feature)).floatValue();
        Composite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);

        return composite;
    }

    /**
     * Returns the contour paint
     *
     * @return the contour paint
     */
    public java.awt.Paint getContour() {
        Stroke stroke = ls.getStroke();

        if (stroke == null) {
            return null;
        }

        // the foreground color
        Paint contourPaint = Color.decode((String) stroke.getColor().getValue(feature));

        // if a graphic fill is to be used, prepare the paint accordingly....
        org.geotools.styling.Graphic gr = stroke.getGraphicFill();
        SLDStyleFactory fac = new SLDStyleFactory();

        if (gr != null) {
            contourPaint = fac.getTexturePaint(gr, feature);
        }

        return contourPaint;
    }

    /**
     * Evaluates an expression over the passed feature, if the expression or the result is null,
     * the default value will be returned
     */
    private String evaluateExpression(Expression e, Feature feature, String defaultValue) {
        String result = defaultValue;

        if (e != null) {
            result = (String) e.getValue(feature);

            if (result == null) {
                result = defaultValue;
            }
        }

        return result;
    }
}
