/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.filter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Collections;

import org.opengis.filter.expression.ExpressionVisitor;

/**
 * Abstract class for a function expression implementation
 *
 * @author James Macgill, PSU
 * @source $URL$
 */
public abstract class FunctionExpressionImpl
    extends org.geotools.filter.DefaultExpression implements FunctionExpression {
	
	/** function name **/
	String name;
	/** function params **/
	List params;
	
    /**
     * Creates a new instance of FunctionExpression
     */
    protected FunctionExpressionImpl() {
    }


     /**
     * Gets the type of this expression.
     *
     * @return the short representation of a function expression.
     */
    public short getType() {
        return FUNCTION;
    }

    /**
     * Gets the name of this function.
     *
     * @return the name of the function.
     * 
     */
    public String getName() {
    	return name;
    }

    /**
     * Sets the name of hte function.
     */
    public void setName(String name) {
    	this.name = name;
    }
    
    /**
     * Returns the function parameters.
     */
    public List getParameters() {
    	return params;
    }
    
    /**
     * Sets the function paramters.
     */
    public void setParameters(List params) {
    	this.params = params;
    }
    
    /**
     * Since this class is heavily subclasses within the geotools toolkit 
     * itself we relax the 'final' restriction of this deprecated method.
     * 
     * @deprecated use {@link #getParameters()}.
     * 
     */
    public Expression[] getArgs() {
    	List params = getParameters();
    	return (Expression[])params.toArray(new Expression[params.size()]);
    }
    
    /**
     * Since this class is heavily subclassed within the geotools toolkit 
     * itself we relax the 'final' restriction of this deprecated method.
     * 
     * @deprecated use {@link #setParameters(List)}
     */
    public void setArgs(Expression[] args) {
    	setParameters(Arrays.asList(args));
    }

    /**
     * Gets the number of arguments that are set.
     *
     * @return the number of args.
     */
    public abstract int getArgCount();

    /**
     * @see org.opengis.filter.expression.Expression#accept(ExpressionVisitor, Object)
     */
    public Object accept(ExpressionVisitor visitor, Object extraData) {
    	return visitor.visit(this,extraData);
    }
    
    /**
     * Returns the implementation hints. The default implementation returns en empty map.
     */
    public Map getImplementationHints() {
        return Collections.EMPTY_MAP;
    }
}
