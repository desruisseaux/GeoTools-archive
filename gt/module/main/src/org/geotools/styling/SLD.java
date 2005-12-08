/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2005, Geotools Project Managment Committee (PMC)
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
package org.geotools.styling;

import org.geotools.feature.FeatureType;
import org.geotools.filter.Expression;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.LiteralExpression;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Font;
import org.geotools.styling.Graphic;
import org.geotools.styling.Halo;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;
import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Utility class for working with Geotools SLD objects.
 * 
 * <p>
 * This class assumes a subset of the SLD specification:
 * 
 * <ul>
 * <li>
 * Single Rule - matching Filter.NONE
 * </li>
 * <li>
 * Symbolizer lookup by name
 * </li>
 * </ul>
 * </p>
 * 
 * <p>
 * When you start to branch out to SLD information that contains multiple rules
 * you will need to modify this class.
 * </p>
 *
 * @since 0.7.0
 */
public class SLD {
    /** <code>NOTFOUND</code> indicates int value was unavailable */
    public static final int NOTFOUND = -1;
    public static final StyleBuilder builder = new StyleBuilder();

    /**
     * Retrieve linestring color from linesymbolizer if available.
     *
     * @param symbolizer Line symbolizer information.
     *
     * @return Color of linestring, or null if unavailable.
     */
    public static Color lineColor(LineSymbolizer symbolizer) {
        if (symbolizer == null) {
            return null;
        }

        Stroke stroke = symbolizer.getStroke();

        return strokeColor(stroke);
    }

    public static Color strokeColor(Stroke stroke) {
        if (stroke == null) {
            return null;
        }

        return color(stroke.getColor());
    }

    public static Color color(Fill fill) {
        if (fill == null) {
            return null;
        }

        return color(fill.getColor());
    }

    /**
     * Sets the colour for a line symbolizer
     *
     * @param style
     * @param colour
     */
    public static void setLineColour(Style style, Color colour) {
        if (style == null) {
            return;
        }

        setLineColour(lineSymbolizer(style), colour);
    }

    /**
     * Sets the Colour for the given Line symbolizer
     *
     * @param symbolizer
     * @param colour
     */
    public static void setLineColour(LineSymbolizer symbolizer, Color colour) {
        if (symbolizer == null) {
            return;
        }

        Stroke stroke = symbolizer.getStroke();

        if (stroke == null) {
            stroke = builder.createStroke(colour);
            symbolizer.setStroke(stroke);
        }

        if (colour != null) {
            stroke.setColor(builder.colorExpression(colour));
        }
    }

    /**
     * Retrieve color from linesymbolizer if available.
     *
     * @param symbolizer Line symbolizer information.
     *
     * @return Color of linestring, or null if unavailable.
     */
    public static Color color(LineSymbolizer symbolizer) {
        return lineColor(symbolizer);
    }

    /**
     * Retrieve linestring width from symbolizer if available.
     *
     * @param symbolizer Line symbolizer information.
     *
     * @return width of linestring, or NOTFOUND
     */
    public static int lineWidth(LineSymbolizer symbolizer) {
        if (symbolizer == null) {
            return NOTFOUND;
        }

        Stroke stroke = symbolizer.getStroke();

        return width(stroke);
    }

    public static int width(Stroke stroke) {
        if (stroke == null) {
            return NOTFOUND;
        }

        return intValue(stroke.getWidth());
    }

    public static int size(Mark mark) {
        if (mark == null) {
            return NOTFOUND;
        }

        return intValue(mark.getSize());
    }

    /**
     * Retrieve linestring width from symbolizer if available.
     *
     * @param symbolizer Line symbolizer information.
     *
     * @return width of linestring, or NOTFOUND
     */
    public static int width(LineSymbolizer symbolizer) {
        return lineWidth(symbolizer);
    }

    /**
     * Grabs the opacity from the first LineSymbolizer.
     *
     * @param symbolizer Line symbolizer information.
     *
     * @return double of the line stroke's opacity, or NaN if unavailable.
     */
    public static double lineOpacity(LineSymbolizer symbolizer) {
        if (symbolizer == null) {
            return Double.NaN;
        }

        Stroke stroke = symbolizer.getStroke();

        return opacity(stroke);
    }

    public static double opacity(Stroke stroke) {
        if (stroke == null) {
            return Double.NaN;
        }

        Expression opacityExp = stroke.getOpacity();
        double opacity = Double.parseDouble(opacityExp.toString());

        return opacity;
    }

    /**
     * Grabs the linejoin from the first LineSymbolizer.
     *
     * @param symbolizer Line symbolizer information.
     *
     * @return String of the line stroke's linejoin, or null if unavailable.
     */
    public static String lineLinejoin(LineSymbolizer symbolizer) {
        if (symbolizer == null) {
            return null;
        }

        Stroke stroke = symbolizer.getStroke();

        if (stroke == null) {
            return null;
        }

        Expression linejoinExp = stroke.getLineJoin();

        return linejoinExp.toString();
    }

    /**
     * Grabs the linecap from the first LineSymbolizer.
     *
     * @param symbolizer Line symbolizer information.
     *
     * @return String of the line stroke's linecap, or null if unavailable.
     */
    public static String lineLinecap(LineSymbolizer symbolizer) {
        if (symbolizer == null) {
            return null;
        }

        Stroke stroke = symbolizer.getStroke();

        if (stroke == null) {
            return null;
        }

        Expression linecapExp = stroke.getLineCap();

        return linecapExp.toString();
    }

    /**
     * Grabs the dashes array from the first LineSymbolizer.
     *
     * @param symbolizer Line symbolizer information.
     *
     * @return float[] of the line dashes array, or null if unavailable.
     */
    public static float[] lineDash(LineSymbolizer symbolizer) {
        if (symbolizer == null) {
            return null;
        }

        Stroke stroke = symbolizer.getStroke();

        if (stroke == null) {
            return null;
        }

        float[] linedash = stroke.getDashArray();

        return linedash;
    }

    /**
     * Grabs the location of the first external graphic.
     *
     * @param style SLD style information.
     *
     * @return Location of the first external graphic, or null
     */
    public static URL pointGraphic(Style style) {
        PointSymbolizer point = pointSymbolizer(style);

        if (point == null) {
            return null;
        }

        Graphic graphic = point.getGraphic();

        if (graphic == null) {
            return null;
        }

        ExternalGraphic[] graphicList = graphic.getExternalGraphics();

        for (int i = 0; i < graphicList.length; i++) {
            ExternalGraphic externalGraphic = graphicList[i];

            if (externalGraphic == null) {
                continue;
            }

            URL location;

            try {
                location = externalGraphic.getLocation(); // Should check format is supported by SWT

                if (location != null) {
                    return location;
                }
            } catch (MalformedURLException e) {
                // ignore, try the next one
            }
        }

        return null;
    }

    public static Mark pointMark(Style style) {
        if (style == null) {
            return null;
        }

        return mark(pointSymbolizer(style));
    }

    public static Mark mark(PointSymbolizer sym) {
        return mark(graphic(sym));
    }

    public static Mark mark(Graphic graphic) {
        if (graphic == null) {
            return null;
        }

        return ((graphic.getMarks() != null) && (graphic.getMarks().length > 0))
        ? graphic.getMarks()[0] : null;
    }

    public static Graphic graphic(PointSymbolizer sym) {
        if (sym == null) {
            return null;
        }

        return sym.getGraphic();
    }

    /**
     * Grabs the size of the points graphic, if found.
     * 
     * <p>
     * If you are using something fun like symbols you  will need to do your
     * own thing.
     * </p>
     *
     * @param symbolizer Point symbolizer information.
     *
     * @return size of the graphic
     */
    public static int pointSize(PointSymbolizer symbolizer) {
        if (symbolizer == null) {
            return NOTFOUND;
        }

        Graphic g = symbolizer.getGraphic();

        if (g == null) {
            return NOTFOUND;
        }

        Expression exp = g.getSize();
        int size = intValue(exp);

        return size;
    }

    /**
     * Grabs the well known name of the first Mark that has one.
     * 
     * <p>
     * If you are using something fun like symbols you  will need to do your
     * own thing.
     * </p>
     *
     * @param symbolizer Point symbolizer information.
     *
     * @return well known name of the first Mark
     */
    public static String pointWellKnownName(PointSymbolizer symbolizer) {
        if (symbolizer == null) {
            return null;
        }

        Graphic g = symbolizer.getGraphic();

        if (g == null) {
            return null;
        }

        Mark[] markList = g.getMarks();

        for (int i = 0; i < markList.length; i++) {
            Mark mark = markList[i];

            if (mark == null) {
                continue;
            }

            String string = wellKnownName(mark);

            if (string == null) {
                continue;
            }

            return string;
        }

        return null;
    }

    public static String wellKnownName(Mark mark) {
        if (mark == null) {
            return null;
        }

        Expression exp = mark.getWellKnownName();

        if (exp == null) {
            return null;
        }

        String string = stringValue(exp);

        return string;
    }

    /**
     * Grabs the color from the first Mark.
     * 
     * <p>
     * If you are using something fun like symbols you  will need to do your
     * own thing.
     * </p>
     *
     * @param symbolizer Point symbolizer information.
     *
     * @return Color of the point's mark, or null if unavailable.
     */
    public static Color pointColor(PointSymbolizer symbolizer) {
        return color(symbolizer);
    }

    /**
     * Sets the Colour for the point symbolizer
     *
     * @param style
     * @param colour
     */
    public static void setPointColour(Style style, Color colour) {
        if (style == null) {
            return;
        }

        setPointColour(pointSymbolizer(style), colour);
    }

    /**
     * Sets the Colour for the given point symbolizer
     *
     * @param symbolizer
     * @param colour
     */
    public static void setPointColour(PointSymbolizer symbolizer, Color colour) {
        if (symbolizer == null) {
            return;
        }

        Graphic graphic = symbolizer.getGraphic();

        if (graphic == null) {
            graphic = builder.createGraphic();
        }

        Mark[] markList = graphic.getMarks();

        for (int i = 0; i < markList.length; i++) {
            Mark mark = markList[i];

            if (mark == null) {
                continue;
            }

            Stroke stroke = mark.getStroke();

            if (stroke == null) {
                stroke = builder.createStroke(colour);
                mark.setStroke(stroke);
            }

            if (colour != null) {
                Fill fill = mark.getFill();

                if (fill == null) {
                    continue;
                }

                stroke.setColor(builder.colorExpression(colour));
                fill.setColor(builder.colorExpression(colour));
            }
        }
    }

    /**
     * Grabs the color from the first Mark.
     * 
     * <p>
     * If you are using something fun like symbols you  will need to do your
     * own thing.
     * </p>
     *
     * @param symbolizer Point symbolizer information.
     *
     * @return Color of the point's mark, or null if unavailable.
     */
    public static Color color(PointSymbolizer symbolizer) {
        if (symbolizer == null) {
            return null;
        }

        Graphic graphic = symbolizer.getGraphic();

        if (graphic == null) {
            return null;
        }

        Mark[] markList = graphic.getMarks();

        for (int i = 0; i < markList.length; i++) {
            Mark mark = markList[i];

            if (mark == null) {
                continue;
            }

            Stroke stroke = mark.getStroke();

            if (stroke == null) {
                continue;
            }

            Color colour = color(stroke.getColor());

            if (colour != null) {
                return colour;
            }
        }

        return null;
    }

    /**
     * Grabs the width of the first Mark with a Stroke that has a non-null
     * width.
     * 
     * <p>
     * If you are using something fun like symbols you  will need to do your
     * own thing.
     * </p>
     *
     * @param symbolizer Point symbolizer information.
     *
     * @return width of the points border
     */
    public static int pointWidth(PointSymbolizer symbolizer) {
        if (symbolizer == null) {
            return NOTFOUND;
        }

        Graphic g = symbolizer.getGraphic();

        if (g == null) {
            return NOTFOUND;
        }

        Mark[] markList = g.getMarks();

        for (int i = 0; i < markList.length; i++) {
            Mark mark = markList[i];

            if (mark == null) {
                continue;
            }

            Stroke stroke = mark.getStroke();

            if (stroke == null) {
                continue;
            }

            Expression exp = stroke.getWidth();

            if (exp == null) {
                continue;
            }

            int width = intValue(exp);

            if (width == NOTFOUND) {
                continue;
            }

            return width;
        }

        return NOTFOUND;
    }

    /**
     * Grabs the point border opacity from the first PointSymbolizer.
     * 
     * <p>
     * If you are using something fun like rules you  will need to do your own
     * thing.
     * </p>
     *
     * @param symbolizer Point symbolizer information.
     *
     * @return double of the point's border opacity, or NaN if unavailable.
     */
    public static double pointBorderOpacity(PointSymbolizer symbolizer) {
        if (symbolizer == null) {
            return Double.NaN;
        }

        Graphic graphic = symbolizer.getGraphic();

        if (graphic == null) {
            return Double.NaN;
        }

        Mark[] markList = graphic.getMarks();

        for (int i = 0; i < markList.length; i++) {
            Mark mark = markList[i];

            if (mark == null) {
                continue;
            }

            Stroke stroke = mark.getStroke();

            if (stroke == null) {
                continue;
            }

            Expression opacityExp = stroke.getOpacity();

            return Double.parseDouble(opacityExp.toString());
        }

        return Double.NaN;
    }

    /**
     * Grabs the point opacity from the first PointSymbolizer.
     * 
     * <p>
     * If you are using something fun like rules you  will need to do your own
     * thing.
     * </p>
     *
     * @param symbolizer Point symbolizer information.
     *
     * @return double of the point's opacity, or NaN if unavailable.
     */
    public static double pointOpacity(PointSymbolizer symbolizer) {
        if (symbolizer == null) {
            return Double.NaN;
        }

        Graphic graphic = symbolizer.getGraphic();

        if (graphic == null) {
            return Double.NaN;
        }

        Mark[] markList = graphic.getMarks();

        for (int i = 0; i < markList.length; i++) {
            Mark mark = markList[i];

            if (mark == null) {
                continue;
            }

            Fill fill = mark.getFill();

            if (fill == null) {
                continue;
            }

            Expression opacityExp = fill.getOpacity();

            return Double.parseDouble(opacityExp.toString());
        }

        return Double.NaN;
    }

    /**
     * Grabs the fill from the first Mark.
     * 
     * <p>
     * If you are using something fun like symbols you  will need to do your
     * own thing.
     * </p>
     *
     * @param symbolizer Point symbolizer information.
     *
     * @return Color of the point's fill, or null if unavailable.
     */
    public static Color pointFill(PointSymbolizer symbolizer) {
        if (symbolizer == null) {
            return null;
        }

        Graphic graphic = symbolizer.getGraphic();

        if (graphic == null) {
            return null;
        }

        Mark[] markList = graphic.getMarks();

        for (int i = 0; i < markList.length; i++) {
            Mark mark = markList[i];

            if (mark == null) {
                continue;
            }

            Fill fill = mark.getFill();

            if (fill == null) {
                continue;
            }

            Color colour = color(fill.getColor());

            if (colour != null) {
                return colour;
            }
        }

        return null;
    }

    /**
     * Grabs the color from the first PolygonSymbolizer.
     * 
     * <p>
     * If you are using something fun like rules you  will need to do your own
     * thing.
     * </p>
     *
     * @param symbolizer Polygon symbolizer information.
     *
     * @return Color of the polygon's stroke, or null if unavailable.
     */
    public static int polyWidth(PolygonSymbolizer symbolizer) {
        if (symbolizer == null) {
            return NOTFOUND;
        }

        Stroke stroke = symbolizer.getStroke();

        if (stroke == null) {
            return NOTFOUND;
        }

        int width = intValue(stroke.getWidth());

        return width;
    }

    /**
     * Grabs the color from the first PolygonSymbolizer.
     * 
     * <p>
     * If you are using something fun like rules you  will need to do your own
     * thing.
     * </p>
     *
     * @param symbolizer Polygon symbolizer information.
     *
     * @return Color of the polygon's stroke, or null if unavailable.
     */
    public static Color polyColor(PolygonSymbolizer symbolizer) {
        if (symbolizer == null) {
            return null;
        }

        Stroke stroke = symbolizer.getStroke();

        if (stroke == null) {
            return null;
        }

        Color colour = color(stroke.getColor());

        if (colour != null) {
            return colour;
        }

        return null;
    }

    /**
     * Sets the colour for a polygon symbolizer
     *
     * @param style
     * @param colour
     */
    public static void setPolyColour(Style style, Color colour) {
        if (style == null) {
            return;
        }

        setPolyColour(polySymbolizer(style), colour);
    }

    /**
     * Sets the Colour for the given polygon symbolizer
     *
     * @param symbolizer
     * @param colour
     */
    public static void setPolyColour(PolygonSymbolizer symbolizer, Color colour) {
        if (symbolizer == null) {
            return;
        }

        Stroke stroke = symbolizer.getStroke();

        if (stroke == null) {
            stroke = builder.createStroke(colour);
            symbolizer.setStroke(stroke);
        }

        if (colour != null) {
            stroke.setColor(builder.colorExpression(colour));

            Fill fill = symbolizer.getFill();

            if (fill != null) {
                fill.setColor(builder.colorExpression(colour));
            }
        }
    }

    /**
     * Grabs the fill from the first PolygonSymbolizer.
     * 
     * <p>
     * If you are using something fun like rules you  will need to do your own
     * thing.
     * </p>
     *
     * @param symbolizer Polygon symbolizer information.
     *
     * @return Color of the polygon's fill, or null if unavailable.
     */
    public static Color polyFill(PolygonSymbolizer symbolizer) {
        if (symbolizer == null) {
            return null;
        }

        Fill fill = symbolizer.getFill();

        if (fill == null) {
            return null;
        }

        Color colour = color(fill.getColor());

        if (colour != null) {
            return colour;
        }

        return null;
    }

    /**
     * Grabs the border opacity from the first PolygonSymbolizer.
     * 
     * <p>
     * If you are using something fun like rules you  will need to do your own
     * thing.
     * </p>
     *
     * @param symbolizer Polygon symbolizer information.
     *
     * @return double of the polygon's border opacity, or NaN if unavailable.
     */
    public static double polyBorderOpacity(PolygonSymbolizer symbolizer) {
        if (symbolizer == null) {
            return Double.NaN;
        }

        Stroke stroke = symbolizer.getStroke();

        if (stroke == null) {
            return Double.NaN;
        }

        Expression opacityExp = stroke.getOpacity();
        double opacity = Double.parseDouble(opacityExp.toString());

        return opacity;
    }

    /**
     * Grabs the fill opacity from the first PolygonSymbolizer.
     * 
     * <p>
     * If you are using something fun like rules you  will need to do your own
     * thing.
     * </p>
     *
     * @param symbolizer Polygon symbolizer information.
     *
     * @return double of the polygon's fill opacity, or NaN if unavailable.
     */
    public static double polyFillOpacity(PolygonSymbolizer symbolizer) {
        if (symbolizer == null) {
            return Double.NaN;
        }

        Fill fill = symbolizer.getFill();

        return opacity(fill);
    }

    public static double opacity(Fill fill) {
        if (fill == null) {
            return Double.NaN;
        }

        Expression opacityExp = fill.getOpacity();
        double opacity = Double.parseDouble(opacityExp.toString());

        return opacity;
    }

    /**
     * Grabs the opacity from the first RasterSymbolizer.
     * 
     * <p>
     * If you are using something fun like rules you  will need to do your own
     * thing.
     * </p>
     *
     * @param symbolizer Raster symbolizer information.
     *
     * @return opacity of the first RasterSymbolizer
     */
    public static double rasterOpacity(RasterSymbolizer symbolizer) {
        if (symbolizer == null) {
            return Double.NaN;
        }

        return doubleValue(symbolizer.getOpacity());
    }

    public static double rasterOpacity(Style style) {
        return rasterOpacity(rasterSymbolizer(style));
    }

    /**
     * Retrieve the first PolygonSymbolizer from the provided Style.
     *
     * @param style SLD style information.
     *
     * @return PolygonSymbolizer, or null if not found.
     */
    public static TextSymbolizer textSymbolizer(Style style) {
        return (TextSymbolizer) symbolizer(style, TextSymbolizer.class);
    }

    /**
     * Grabs the label from the first TextSymbolizer.
     * 
     * <p>
     * If you are using something fun like symbols you  will need to do your
     * own thing.
     * </p>
     *
     * @param symbolizer Text symbolizer information.
     *
     * @return Expression of the label's text, or null if unavailable.
     */
    public static Expression textLabel(TextSymbolizer symbolizer) {
        if (symbolizer == null) {
            return null;
        }

        Expression exp = symbolizer.getLabel();

        if (exp == null) {
            return null;
        }

        return exp;
    }

    public static String textLabelString(TextSymbolizer sym) {
        Expression exp = textLabel(sym);

        return (exp == null) ? null : exp.toString();
    }

    /**
     * Grabs the fontFill from the first TextSymbolizer.
     * 
     * <p>
     * If you are using something fun like symbols you  will need to do your
     * own thing.
     * </p>
     *
     * @param symbolizer Text symbolizer information.
     *
     * @return Color of the font's fill, or null if unavailable.
     */
    public static Color textFontFill(TextSymbolizer symbolizer) {
        if (symbolizer == null) {
            return null;
        }

        Fill fill = symbolizer.getFill();

        if (fill == null) {
            return null;
        }

        Color colour = color(fill.getColor());

        if (colour != null) {
            return colour;
        }

        return null;
    }

    public static Font font(TextSymbolizer symbolizer) {
        if (symbolizer == null) {
            return null;
        }

        Font[] font = symbolizer.getFonts();

        if ((font == null) || (font[0] == null)) {
            return null;
        }

        return font[0];
    }

    /**
     * Grabs the haloFill from the first TextSymbolizer.
     * 
     * <p>
     * If you are using something fun like symbols you  will need to do your
     * own thing.
     * </p>
     *
     * @param symbolizer Text symbolizer information.
     *
     * @return Color of the halo's fill, or null if unavailable.
     */
    public static Color textHaloFill(TextSymbolizer symbolizer) {
        Halo halo = symbolizer.getHalo();

        if (halo == null) {
            return null;
        }

        Fill fill = halo.getFill();

        if (fill == null) {
            return null;
        }

        Color colour = color(fill.getColor());

        if (colour != null) {
            return colour;
        }

        return null;
    }

    /**
     * Grabs the halo width from the first TextSymbolizer.
     * 
     * <p>
     * If you are using something fun like symbols you  will need to do your
     * own thing.
     * </p>
     *
     * @param symbolizer Text symbolizer information.
     *
     * @return float of the halo's width, or null if unavailable.
     */
    public static int textHaloWidth(TextSymbolizer symbolizer) {
        Halo halo = symbolizer.getHalo();

        if (halo == null) {
            return 0;
        }

        Expression exp = halo.getRadius();

        if (exp == null) {
            return 0;
        }

        int width = (int) Float.parseFloat(exp.toString());

        if (width != 0) {
            return width;
        }

        return 0;
    }

    /**
     * Grabs the halo opacity from the first TextSymbolizer.
     * 
     * <p>
     * If you are using something fun like symbols you  will need to do your
     * own thing.
     * </p>
     *
     * @param symbolizer Text symbolizer information.
     *
     * @return double of the halo's opacity, or NaN if unavailable.
     */
    public static double textHaloOpacity(TextSymbolizer symbolizer) {
        if (symbolizer == null) {
            return Double.NaN;
        }

        Halo halo = symbolizer.getHalo();

        if (halo == null) {
            return Double.NaN;
        }

        Fill fill = halo.getFill();

        if (fill == null) {
            return Double.NaN;
        }

        Expression opacityExp = fill.getOpacity();
        double opacity = Double.parseDouble(opacityExp.toString());

        return opacity;
    }

    /**
     * Navigate through the expression seaching for TYPE.
     * 
     * <p>
     * This will work even with dynamic expression that would normall require a
     * feature. It works especially well when the Expression is a Literal
     * literal (which is usually the case).
     * </p>
     * 
     * <p>
     * If you have a specific Feature, please do this:
     * <pre><code>
     * Object value = expr.getValue( feature );
     * return value instanceof Color ? (Color) value : null;
     * </code></pre>
     * </p>
     *
     * @param expr
     * @param TYPE DOCUMENT ME!
     *
     * @return First available color, or null.
     */
    protected static Object value(Expression expr, Class TYPE) {
        if (expr == null) {
            return null;
        } else if (expr instanceof LiteralExpression) {
            LiteralExpression literal = (LiteralExpression) expr;
            Object value = literal.getLiteral();

            if (TYPE.isInstance(value)) {
                return value;
            }
        } else if (expr instanceof FunctionExpression) {
            FunctionExpression function = (FunctionExpression) expr;

            if (function.getArgCount() != 0) {
                for (int i = 0; i < function.getArgCount(); i++) {
                    Expression e = function.getArgs()[i];
                    Object value = value(e, TYPE);

                    if (value != null) {
                        return value;
                    }
                }
            }
        } else {
            try { // this is a bad idea, not expected to work much

                Object value = expr.getValue(null);

                if (TYPE.isInstance(value)) {
                    return value;
                }
            } catch (NullPointerException expected) {
                return null; // well that was not unexpected
            } catch (Throwable ignore) { // I did say that was a bad idea                
            }
        }

        return null; // really need a Feature to acomplish this one
    }

    /**
     * Navigate through the expression finding the first mentioned Color.
     * 
     * <p>
     * If you have a specific Feature in mind please use:
     * <pre><code>
     * Object value = expr.getValue( feature );
     * return value instanceof Color ? (Color) value : null;
     * </code></pre>
     * </p>
     *
     * @param expr
     *
     * @return First available color, or null.
     */
    public static Color color(Expression expr) {
        if (expr == null) {
            return null;
        }

        Color color = (Color) value(expr, Color.class);

        if (color != null) {
            return color;
        }

        String rgba = (String) value(expr, String.class);

        try {
            color = Color.decode(rgba);

            if (color != null) {
                return color;
            }
        } catch (NumberFormatException badRGB) {
            // unavailable
        }

        return null;
    }

    /**
     * Uses number( expr ), will turn result into an interger, or NOTFOUND
     *
     * @param expr
     *
     * @return int value of first Number, or NOTFOUND
     */
    public static int intValue(Expression expr) {
        Number number = (Number) value(expr, Number.class);
        
        if (number != null) {
            return number.intValue();
        }

        //look for a string
        String string = (String) value(expr,String.class);
        if (string != null) {
        	//try parsing into a integer
        	try {
        		return Integer.parseInt(string);
        	}
        	catch(NumberFormatException e) {}
        }
        
        //no dice
        return NOTFOUND;
    }

    /**
     * Uses string( expr ), will turn result into a String
     *
     * @param expr
     *
     * @return value of first String
     */
    public static String stringValue(Expression expr) {
        String string = (String) value(expr, String.class);

        return string;
    }

    /**
     * Uses number( expr ), will turn result into an interger, or NaN.
     *
     * @param expr
     *
     * @return int value of first Number, or Double.NaN
     */
    public static double doubleValue(Expression expr) {
        Number number = (Number) value(expr, Number.class);

        if (number != null) {
            return number.doubleValue();
        }
        
        //try for a string
        String string = (String) value(expr,String.class);
        if (string != null) {
        	//try parsing into a double
        	try {
        		return Double.parseDouble(string);
        	}
        	catch(NumberFormatException e) {}
        }

        //too bad
        return Double.NaN;
    }

    /**
     * Navigate through the expression finding the first mentioned Integer.
     * 
     * <p>
     * Does not evaulate math expression (yet).
     * </p>
     *
     * @param expr
     *
     * @return Number or null
     */
    public Number number(Expression expr) {
        return (Number) value(expr, Number.class);
    }

    /**
     * Retrieve the first RasterSymbolizer from the provided Style.
     *
     * @param style SLD style information.
     *
     * @return LineSymbolizer, or null if not found.
     */
    public static RasterSymbolizer rasterSymbolizer(Style style) {
        return (RasterSymbolizer) symbolizer(style, RasterSymbolizer.class);
    }

    /**
     * Retrieve the first LineSymbolizer from the provided Style.
     *
     * @param style SLD style information.
     *
     * @return LineSymbolizer, or null if not found.
     */
    public static LineSymbolizer lineSymbolizer(Style style) {
        return (LineSymbolizer) symbolizer(style, LineSymbolizer.class);
    }

    public static Stroke stroke(LineSymbolizer sym) {
        if (sym == null) {
            return null;
        }

        return sym.getStroke();
    }

    public static Stroke stroke(PolygonSymbolizer sym) {
        if (sym == null) {
            return null;
        }

        return sym.getStroke();
    }

    public static Stroke stroke(PointSymbolizer sym) {
        Mark mark = mark(sym);

        return (mark == null) ? null : mark.getStroke();
    }

    public static Fill fill(PolygonSymbolizer sym) {
        if (sym == null) {
            return null;
        }

        return sym.getFill();
    }

    public static Fill fill(PointSymbolizer sym) {
        Mark mark = mark(sym);

        return (mark == null) ? null : mark.getFill();
    }

    /**
     * Retrieve the first PointSymbolizer from the provided Style.
     *
     * @param style SLD style information.
     *
     * @return PointSymbolizer, or null if not found.
     */
    public static PointSymbolizer pointSymbolizer(Style style) {
        return (PointSymbolizer) symbolizer(style, PointSymbolizer.class);
    }

    /**
     * Retrieve the first PolygonSymbolizer from the provided Style.
     *
     * @param style SLD style information.
     *
     * @return PolygonSymbolizer, or null if not found.
     */
    public static PolygonSymbolizer polySymbolizer(Style style) {
        return (PolygonSymbolizer) symbolizer(style, PolygonSymbolizer.class);
    }

    /**
     * Returns the feature type style in the style which matched a particular
     * name.
     *
     * @param style The style in question.
     * @param type The feature type must be non-null.
     *
     * @return Teh FeatureTypeStyle object if it exists, otherwise false.
     */
    public static FeatureTypeStyle featureTypeStyle(Style style,
        FeatureType type) {
        if (style == null) {
            return null;
        }

        if ((type == null) || (type.getTypeName() == null)) {
            return null;
        }

        FeatureTypeStyle[] styles = style.getFeatureTypeStyles();

        if (styles == null) {
            return null;
        }

        for (int i = 0; i < styles.length; i++) {
            FeatureTypeStyle ftStyle = styles[i];

            if (type.getTypeName().equals(ftStyle.getName())) {
                return ftStyle;
            }
        }

        return null;
    }

    /**
     * Returns the first style object which matches a given schema.
     *
     * @param styles Array of style objects.
     * @param schema Feature schema.
     *
     * @return The first object to match the feature type, otherwise null if
     *         no match.
     */
    public static Style matchingStyle(Style[] styles, FeatureType schema) {
        if ((styles == null) || (styles.length == 0)) {
            return null;
        }

        for (int i = 0; i < styles.length; i++) {
            Style style = styles[i];

            if (featureTypeStyle(style, schema) != null) {
                return style;
            }
        }

        return null;
    }

    /**
     * Retrieve the first SYMBOLIZER from the provided Style.
     *
     * @param style SLD style information.
     * @param SYMBOLIZER DOCUMENT ME!
     *
     * @return symbolizer instance from style, or null if not found.
     */
    protected static Symbolizer symbolizer(Style style, final Class SYMBOLIZER) {
        if (style == null) {
            return null;
        }

        FeatureTypeStyle[] styles = style.getFeatureTypeStyles();

        if (styles == null) {
            return null;
        }

        FeatureTypeStyle[] ftStyleList = style.getFeatureTypeStyles();

        if (ftStyleList == null) {
            return null;
        }

FEATURETYPE: 
        for (int i = 0; i < ftStyleList.length; i++) {
            FeatureTypeStyle ftStyle = ftStyleList[i];
            Rule[] ruleList = ftStyle.getRules();

            if (ruleList == null) {
                continue FEATURETYPE;
            }

RULE: 
            for (int j = 0; j < ruleList.length; j++) {
                Rule rule = ruleList[j];
                Symbolizer[] symbolizerList = rule.getSymbolizers();

                if (symbolizerList == null) {
                    continue RULE;
                }

SYMBOLIZER: 
                for (int k = 0; k < symbolizerList.length; k++) {
                    Symbolizer symbolizer = symbolizerList[k];

                    if (symbolizer == null) {
                        continue SYMBOLIZER;
                    }

                    if (SYMBOLIZER.isInstance(symbolizer)) {
                        return symbolizer;
                    }
                }
            }
        }

        return null;
    }
}
