/*
 * ClassificationFunction.java
 *
 * Created on October 27, 2004, 11:27 AM
 */

package org.geotools.filter.function;

import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.filter.LiteralExpression;
import org.opengis.feature.Attribute;

/**
 *
 * @author  jfc173
 */
public abstract class ClassificationFunction extends FunctionExpressionImpl implements FunctionExpression{
    
    int classNum;
    Expression expr; 
    
    /** Creates a new instance of ClassificationFunction */
    public ClassificationFunction() {
    }
    
    public int getArgCount() {
        return 2;
    }
    
    public int getNumberOfClasses(){
        return classNum;
    }
    
    public void setNumberOfClasses(int i){
        classNum = i;
    }
    
    public Expression getExpression(){
        return expr;
    }
    
    public void setExpression(Expression e){
        expr = e;
    }
    
    public Expression[] getArgs(){
        Expression[] ret = new Expression[2];
        ret[0] = expr;
        FilterFactory ff = FilterFactory.createFilterFactory();
        ret[1] = ff.createLiteralExpression(classNum);
        return ret;
    }
    
    public abstract String getName();
    
    public void setArgs(Expression[] args){
        expr = args[0];
        classNum = ((Number) ((LiteralExpression) args[1]).getLiteral()).intValue();
    }
    
    public abstract Object getValue(Attribute att);
    
}
