/*
 * PaletteFunction.java
 *
 * Created on October 27, 2004, 11:27 AM
 */

package org.geotools.filter.function;

//import edu.psu.geovista.colorbrewer.OriginalColor;
import java.awt.Color;
import org.geotools.brewer.color.BrewerPalette;
import org.geotools.brewer.color.ColorBrewer;
import org.geotools.feature.Feature;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.filter.LiteralExpression;

/**
 *
 * @author  jfc173
 */
public class PaletteFunction extends FunctionExpressionImpl implements FunctionExpression{
    
    ClassificationFunction classifier;
    String paletteName;
    
    /** Creates a new instance of PaletteFunction */
    public PaletteFunction() {
    }
    
    public int getArgCount() {
        return 2;
    }
    
    public Expression[] getArgs() {
        Expression[] ret = new Expression[2];
        ret[0] = classifier;
        FilterFactory ff = FilterFactory.createFilterFactory();
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
    
    public Expression getEvaluationExpression(){
        return classifier.getExpression();
    }
    
    public void setEvaluationExpression(Expression e){
        classifier.setExpression(e);
    }
    
    public ClassificationFunction getClassifier(){
        return classifier;
    }
    
    public void setClassifier(ClassificationFunction cf){
        classifier = cf;
    }
    
    public int getNumberOfClasses(){
        return classifier.getNumberOfClasses();
    }
    
    public void setNumberOfClasses(int i){
        classifier.setNumberOfClasses(i);
    }
    
    public String getPaletteName(){
        return paletteName;
    }
    
    public void setPaletteName(String s){
        paletteName = s;
    }
    
    private String intToHex(int i){
        String prelim = Integer.toHexString(i);
        while (prelim.length() < 2){
            prelim = "0" + prelim;
        }
        if (prelim.length() > 2){
            prelim = prelim.substring(0, 1);
        }
        return prelim;
    }
    
    public Object getValue(Feature feature){
        int classNum = classifier.getNumberOfClasses();
        ColorBrewer brewer = new ColorBrewer();
        int klass = ((Integer) classifier.getValue(feature)).intValue();

        BrewerPalette pal = brewer.getPalette(paletteName);
        Color colors[] = pal.getColors(classNum);
        String color =  "#" + intToHex(colors[klass].getRed()) + 
                              intToHex(colors[klass].getGreen()) +
                              intToHex(colors[klass].getBlue());
        return color;
        
    }
    
    public String toString(){
        return "Color Brewer palette";
    }
    
}