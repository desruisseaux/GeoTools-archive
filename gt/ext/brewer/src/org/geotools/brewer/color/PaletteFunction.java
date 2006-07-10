/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.brewer.color;

import org.geotools.feature.Feature;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.filter.expression.Expression;
import org.geotools.filter.expression.FunctionExpression;
import org.geotools.filter.expression.LiteralExpression;
import org.geotools.filter.function.ClassificationFunction;

//import edu.psu.geovista.colorbrewer.OriginalColor;
import java.awt.Color;


/**
 *
 * @author James Macgill
 * @source $URL$
 */
public class PaletteFunction extends FunctionExpressionImpl
    implements FunctionExpression {
    ClassificationFunction classifier;
    String paletteName;
    FilterFactory ff;

    /** Creates a new instance of PaletteFunction */
    public PaletteFunction() {
        this(FilterFactoryFinder.createFilterFactory());
    }

    public PaletteFunction(FilterFactory factory) {
        ff = factory;
    }

    public void setFilterFactory(FilterFactory factory) {
        ff = factory;
    }

    public int getArgCount() {
        return 2;
    }

    public Expression[] getArgs() {
        Expression[] ret = new Expression[2];
        ret[0] = classifier;
        ret[1] = ff.createLiteralExpression(paletteName);

        return ret;
    }

    public String getName() {
        return "Palette";
    }

    public void setArgs(Expression[] args) {
        classifier = (ClassificationFunction) args[0];
        paletteName = ((LiteralExpression) args[1]).getLiteral().toString();
    }

    public Expression getEvaluationExpression() {
        return classifier.getExpression();
    }

    public void setEvaluationExpression(Expression e) {
        classifier.setExpression(e);
    }

    public ClassificationFunction getClassifier() {
        return classifier;
    }

    public void setClassifier(ClassificationFunction cf) {
        classifier = cf;
    }

    public int getNumberOfClasses() {
        return classifier.getNumberOfClasses();
    }

    public void setNumberOfClasses(int i) {
        classifier.setNumberOfClasses(i);
    }

    public String getPaletteName() {
        return paletteName;
    }

    public void setPaletteName(String s) {
        paletteName = s;
    }

    private String intToHex(int i) {
        String prelim = Integer.toHexString(i);

        while (prelim.length() < 2) {
            prelim = "0" + prelim;
        }

        if (prelim.length() > 2) {
            prelim = prelim.substring(0, 1);
        }

        return prelim;
    }

    public Object evaluate(Feature feature) {
        int classNum = classifier.getNumberOfClasses();
        ColorBrewer brewer = new ColorBrewer();
        int klass = ((Integer) classifier.evaluate(feature)).intValue();

        BrewerPalette pal = brewer.getPalette(paletteName);
        Color[] colors = pal.getColors(classNum);
        String color = "#" + intToHex(colors[klass].getRed())
            + intToHex(colors[klass].getGreen())
            + intToHex(colors[klass].getBlue());

        return color;
    }

    public String toString() {
        return "Color Brewer palette";
    }
}
