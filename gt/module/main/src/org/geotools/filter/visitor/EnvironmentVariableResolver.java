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
package org.geotools.filter.visitor;
import java.util.logging.Logger;
import org.geotools.filter.Expression;
import org.geotools.filter.ExpressionBuilder;
import org.geotools.filter.Filter;
import org.geotools.filter.MapScaleDenominator;

import org.geotools.filter.parser.ParseException;


/**
 * Finds instances of specific environment variable expressions within filters and
 * composite expressins and replaces them with simple literals.
 *
 * @author James Macgill, Penn State
 */
public class EnvironmentVariableResolver extends AbstractFilterVisitor implements org.geotools.filter.FilterVisitor {
    
    
    
    /** Standard java logger */
    private static Logger LOGGER = Logger.getLogger("org.geotools.filter");
    
    
    /**
     * Empty constructor
     */
    public EnvironmentVariableResolver() {
    }
    
    /**
     * Find all instances of MapScaleDenominator and replace them with 
     * the a literal expression for the provided map scale.
     * The passed in filter is NOT modifed by calls to this method.
     * @param filter The Filter to check for MapScaleDenominator Environment Variables
     * @return A Filter with the map scale env variable replaced by literals
     */
    public Filter resolve(Filter filter, double mapScale) throws ParseException {
        String input = filter.toString();
        input = input.replaceAll("sld:MapScaleDenominator", ""+mapScale);
        Filter output = (Filter)ExpressionBuilder.parse(input);
        return output;
    }
    
    
    /**
     * Find all instances of MapScaleDenominator and replace them with 
     * the a literal expression for the provided map scale.
     * The passed in filter is NOT modifed by calls to this method.
     * @param exp The Expression to check for MapScaleDenominator Environment Variables
     * @return An Epression with the map scale env variable replaced by literals
     */
    public Expression resolve(Expression exp, double mapScale) throws ParseException {
        
        String input = exp.toString();
        input = input.replaceAll("sld:MapScaleDenominator", ""+mapScale);
        Expression output = (Expression)ExpressionBuilder.parse(input);
        return output;
    }
    
    public boolean needsResolving(Filter f){
        return true;
    }
    
    
    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.Expression)
     */
    public void visit(Expression expression) {
        if(expression instanceof MapScaleDenominator){
            System.err.println("FOUND ONE");
        }
    }
    
}
