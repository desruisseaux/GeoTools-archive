/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geotools.data.DataTestCase;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.MathExpression;
import org.geotools.filter.function.ClassificationFunction;
import org.geotools.filter.function.EqualIntervalFunction;
import org.geotools.filter.function.RangedClassifier;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.opengis.filter.Filter;


/**
 *
 * @source $URL$
 */
public class StyleGeneratorTest extends DataTestCase {
    public StyleGeneratorTest(String arg0) {
        super(arg0);
    }

    public void checkFilteredResultNotEmpty(Rule[] rule, FeatureSource fs,
        String attribName) throws IOException {
        for (int i = 0; i < rule.length; i++) {
            Filter filter = rule[i].getFilter();
            FeatureCollection filteredCollection = fs.getFeatures(filter);
            assertTrue(filteredCollection.size() > 0);

            String filterInfo = "Filter \"" + filter.toString()
                + "\" contains " + filteredCollection.size() + " element(s) (";
            FeatureIterator it = filteredCollection.features();

            while (it.hasNext()) {
                Feature feature = it.next();
                filterInfo += ("'" + feature.getAttribute(attribName) + "'");

                if (it.hasNext()) {
                    filterInfo += ", ";
                }
            }

            it.close();
            System.out.println(filterInfo + ")");
        }
    }

    public void testComplexExpression() throws Exception {
        ColorBrewer brewer = new ColorBrewer();
        brewer.loadPalettes();

        FilterFactory ff = FilterFactoryFinder.createFilterFactory();
        MathExpression expr = null;
        MathExpression expr2 = null;
        FeatureType type = roadType;
        String attribName = type.getAttributeType(0).getName();
        FeatureCollection fc = DataUtilities.collection(roadFeatures);
        FeatureSource fs = DataUtilities.source(fc);

        try {
            expr = ff.createMathExpression(MathExpression.MATH_MULTIPLY);
            expr.addLeftValue(ff.createAttributeExpression(attribName));
            expr.addRightValue(ff.createAttributeExpression(attribName));
            expr2 = ff.createMathExpression(MathExpression.MATH_ADD);
            expr2.addLeftValue(expr);
            expr2.addRightValue(ff.createLiteralExpression(3));
        } catch (IllegalFilterException e) {
            fail(e.getMessage());
        }

        String paletteName = "YlGn"; //type = Sequential

        //create the classification function
        ClassificationFunction function = new EqualIntervalFunction();
        List params = new ArrayList();
        params.add(0, expr2); //expression
        params.add(1, ff.literal(2)); //classes
        function.setParameters(params);

        Object object = function.evaluate(fc);
        assertTrue(object instanceof RangedClassifier);
        RangedClassifier classifier = (RangedClassifier) object;

        Color[] colors = brewer.getPalette(paletteName).getColors(2);
        // get the fts
        FeatureTypeStyle fts = StyleGenerator.createFeatureTypeStyle(
                classifier, expr2, colors, "myfts", roadFeatures[0].getFeatureType()
                        .getDefaultGeometry(), StyleGenerator.ELSEMODE_IGNORE,
                0.5, null);
        assertNotNull(fts);

        // test each filter
        Rule[] rule = fts.getRules();
        assertEquals(2, rule.length);
        //do a preliminary test to make sure each rule's filter returns some results
        checkFilteredResultNotEmpty(rule, fs, attribName);

        assertNotNull(StyleGenerator.toStyleExpression(rule[0].getFilter()));
        assertNotNull(StyleGenerator.toStyleExpression(rule[1].getFilter()));
    }
}
