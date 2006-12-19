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
package org.geotools.filter.visitor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.geotools.filter.AttributeExpression;
import org.geotools.filter.BetweenFilter;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Expression;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterVisitor2;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.LikeFilter;
import org.geotools.filter.LiteralExpression;
import org.geotools.filter.LogicFilter;
import org.geotools.filter.MathExpression;
import org.geotools.filter.NullFilter;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.IncludeFilter;


/**
 * Used to duplicate a Filter & or Expression
 *
 * @author Jody Garnett, Refractions Research Inc.
 * @source $URL$
 */
public class DuplicatorFilterVisitor extends AbstractFilterVisitor implements FilterVisitor2 {
    protected Stack pages = new Stack(); // need a Stack as Filter structure is recursive
    protected FilterFactory ff;
	private boolean strict;

    public Stack getPages() {
    	return pages;
    }
    
    /**
     * New instance
     * @param factory factory to use for creating the filters.
     */
    public DuplicatorFilterVisitor(FilterFactory factory) {
       	this(factory,true);
    }
    /**
     * New instance
     * @param factory factory to use for creating the filters.
     * @param strict if false then unkown filters will not be copied.  The same instance will be returned as the copy.
     * Otherwise an exception will be thrown when encountering an unkown filter.
     */
    public DuplicatorFilterVisitor(FilterFactory factory, boolean strict) {
        ff = factory;
        this.strict=strict;
    }

    public void setFilterFactory(FilterFactory factory) {
        ff = factory;
    }

    public void visit( IncludeFilter filter ){
        pages.push( Filter.INCLUDE );
    }
    public void visit( ExcludeFilter filter ){
        pages.push( Filter.EXCLUDE );
    }
    
    public void visit(Filter filter) {
    	if( filter==org.geotools.filter.Filter.NONE){
    		pages.push(org.geotools.filter.Filter.NONE);
    		return;
    	}else if( filter==org.geotools.filter.Filter.ALL){
    		pages.push(org.geotools.filter.Filter.ALL);
    		return;
    	}
    	if( strict )
        // Should not happen?
    	throw new RuntimeException("visit(Filter) unsupported");
    	else
    		pages.push(filter);
    }

    public void visit(BetweenFilter filter) {
        BetweenFilter copy = null;

        try {
            Expression leftCopy = null;

            if (filter.getLeftValue() != null) {
                filter.getLeftValue().accept(this);
                leftCopy = (Expression) pages.pop();
            }

            Expression middleCopy = null;

            if (filter.getMiddleValue() != null) {
                filter.getMiddleValue().accept(this);
                middleCopy = (Expression) pages.pop();
            }

            Expression rightCopy = null;

            if (filter.getRightValue() != null) {
                filter.getRightValue().accept(this);
                rightCopy = (Expression) pages.pop();
            }

            copy = ff.createBetweenFilter();
            copy.addLeftValue(leftCopy);
            copy.addMiddleValue(middleCopy);
            copy.addRightValue(rightCopy);
        } catch (IllegalFilterException erp) {
            throw new RuntimeException(erp);
        }

        pages.push(copy);
    }

    public void visit(CompareFilter filter) {
    	CompareFilter copy = null;
    	
    	try {
    		Expression leftCopy = null;
    		
    		if (filter.getLeftValue() != null) {
    			filter.getLeftValue().accept(this);
    			leftCopy = (Expression) pages.pop();
    		}

    		Expression rightCopy = null;
    		
    		if (filter.getRightValue() != null) {
    			filter.getRightValue().accept(this);
    			rightCopy = (Expression) pages.pop();
    		}
			
        	copy = ff.createCompareFilter(filter.getFilterType());
        	copy.addLeftValue(leftCopy);
        	copy.addRightValue(rightCopy);
		} catch (IllegalFilterException erp) {
            throw new RuntimeException(erp);
		}
		
		pages.push(copy);
    }

    public void visit(GeometryFilter filter) {
    	GeometryFilter copy = null;
    
    	try {
    		Expression leftCopy = null;
    		
    		if (filter.getLeftGeometry() != null) {
    			filter.getLeftGeometry().accept(this);
    			leftCopy = (Expression) pages.pop();
    		}

    		Expression rightCopy = null;
    		
    		if (filter.getRightGeometry() != null) {
    			filter.getRightGeometry().accept(this);
    			rightCopy = (Expression) pages.pop();
    		}
			
        	copy = ff.createGeometryFilter(filter.getFilterType());
        	copy.addLeftGeometry(leftCopy);
        	copy.addRightGeometry(rightCopy);
		} catch (IllegalFilterException erp) {
            throw new RuntimeException(erp);
		}
		
		pages.push(copy);
    }

    public void visit(LikeFilter filter) {
    	LikeFilter copy = null;
    	
    	try {
    		Expression valueCopy = null;
    		
    		if (filter.getValue() != null) {
    			filter.getValue().accept(this);
    			valueCopy = (Expression) pages.pop();
    		}

        	copy = ff.createLikeFilter();
        	copy.setValue(valueCopy);
        	copy.setPattern(filter.getPattern(), filter.getWildcardMulti(), filter.getWildcardSingle(), filter.getEscape());
		} catch (IllegalFilterException erp) {
            throw new RuntimeException(erp);
		}
		
		pages.push(copy);
    }

    public void visit(LogicFilter filter) {
    	LogicFilter copy = null;
    	
    	Iterator iterator = filter.getFilterIterator();
    	
    	List subFilters = new ArrayList();
    	while (iterator.hasNext()) {
    		Filter subFilter = (Filter) iterator.next();
    		subFilter.accept(this);
    		subFilters.add((Filter) pages.pop());
    	}

    	try {
    		copy = ff.createLogicFilter(filter.getFilterType());
    		Iterator copyIterator = subFilters.iterator();
    		while (copyIterator.hasNext()) {
    			copy.addFilter((Filter) copyIterator.next());
    		}
		} catch (IllegalFilterException erp) {
            throw new RuntimeException(erp);
		}
    	
		pages.push(copy);
    }

    public void visit(NullFilter filter) {
    	NullFilter copy = null;
    	
    	try {
    		Expression valueCopy = null;
    		
    		if (filter.getNullCheckValue() != null) {
    			filter.getNullCheckValue().accept(this);
    			valueCopy = (Expression) pages.pop();
    		}

        	copy = ff.createNullFilter( );
        	copy.nullCheckValue(valueCopy);
    	} catch (IllegalFilterException erp) {
            throw new RuntimeException(erp);
		}
		
		pages.push(copy);
    }

    public void visit(FidFilter filter) {
    	FidFilter copy = ff.createFidFilter();
    	
    	String[] fids = filter.getFids();
    	for (int i = 0; i < fids.length; i++) {
    		copy.addFid(fids[i]);
    	}
    	
		pages.push(copy);
    }

    public void visit(AttributeExpression expression) {
    	AttributeExpression copy = null;
		copy = ff.createAttributeExpression(expression.getAttributePath()); //not a true copy, but what the heck...
    	
        pages.push(copy);
    }

    public void visit(Expression expression) {
        // Should not happen?
    	throw new RuntimeException("visit(Expression) unsupported");
    }

    public void visit(LiteralExpression expression) {
    	LiteralExpression copy = null;
    	
    	try {
			copy = ff.createLiteralExpression(expression.getLiteral());
		} catch (IllegalFilterException erp) {
            throw new RuntimeException(erp);
		} 
		
		pages.push(copy);
    }

    public void visit(MathExpression expression) {
    	MathExpression copy = null;
    	
    	try {
			copy = ff.createMathExpression(expression.getType());
		
			if (expression.getLeftValue() != null) {
				expression.getLeftValue().accept(this);
				copy.addLeftValue((Expression) pages.pop());
			}
			if (expression.getRightValue() != null) {
				expression.getRightValue().accept(this);
				copy.addRightValue((Expression) pages.pop());
			}
		} catch (IllegalFilterException erp) {
            throw new RuntimeException(erp);
		} 

		pages.push(copy);
    }

    public void visit(FunctionExpression expression) {
    	FunctionExpression copy = null;
    	
		Expression[] args = expression.getArgs();
		Expression[] copyArgs = new Expression[args.length];
		for (int i = 0; i < args.length; i++) {
			args[i].accept(this);
			copyArgs[i] = (Expression) pages.pop();
		}

		copy = ff.createFunctionExpression(expression.getName());
		copy.setArgs(copyArgs);
		
		pages.push(copy);
    }

    public Object getCopy() {
        if( pages.isEmpty() ) return null;
        return pages.firstElement();
    }
}
