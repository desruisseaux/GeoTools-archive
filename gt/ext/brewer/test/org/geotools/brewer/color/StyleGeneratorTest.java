package org.geotools.brewer.color;

import org.geotools.brewer.color.ColorBrewer;
import org.geotools.brewer.color.StyleGenerator;
import org.geotools.data.DataTestCase;
import org.geotools.data.DataUtilities;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.IllegalFilterException;
import org.geotools.styling.Style;

public class StyleGeneratorTest extends DataTestCase {

	public StyleGeneratorTest(String arg0) {
        super(arg0);
	}
	
	public void testSequential() {
		ColorBrewer brewer = new ColorBrewer();
        brewer.loadPalettes(ColorBrewer.SEQUENTIAL);
        FilterFactory ff = FilterFactory.createFilterFactory();
        Expression expr = null; 
        FeatureType type = roadType;
        String attribName = type.getAttributeType(0).getName();
        FeatureCollection fc = DataUtilities.collection(roadFeatures);
        try {
			expr = ff.createAttributeExpression(type, attribName);
		} catch (IllegalFilterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        String paletteName = "YlGn"; //type = Sequential
        //get the style
        StyleGenerator sg = new StyleGenerator(brewer, paletteName, 2, expr, fc);
        Style style = sg.createStyle();
        assertNotNull(style);
	}

	public void testDiverging() {
		ColorBrewer brewer = new ColorBrewer();
        brewer.loadPalettes(ColorBrewer.DIVERGING);
        FilterFactory ff = FilterFactory.createFilterFactory();
        Expression expr = null; 
        FeatureType type = roadType;
        String attribName = type.getAttributeType(0).getName();
        FeatureCollection fc = DataUtilities.collection(roadFeatures);
        try {
			expr = ff.createAttributeExpression(type, attribName);
		} catch (IllegalFilterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        String paletteName = "BrBG"; //type = Diverging
        //get the style
        StyleGenerator sg = new StyleGenerator(brewer, paletteName, 2, expr, fc);
        Style style = sg.createStyle();
        assertNotNull(style);
	}
	
	public void testQualitative() {
		ColorBrewer brewer = new ColorBrewer();
        brewer.loadPalettes(ColorBrewer.QUALITATIVE);
        FilterFactory ff = FilterFactory.createFilterFactory();
        Expression expr = null; 
        FeatureType type = roadType;
        String attribName = type.getAttributeType(2).getName();
        FeatureCollection fc = DataUtilities.collection(roadFeatures);
        try {
			expr = ff.createAttributeExpression(type, attribName);
		} catch (IllegalFilterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        String paletteName = "Set3"; //type = Qualitative
        //get the style
        StyleGenerator sg = new StyleGenerator(brewer, paletteName, 2, expr, fc);
        Style style = sg.createStyle();
        assertNotNull(style);
	}
	
}
