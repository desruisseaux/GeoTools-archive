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
 *
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
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.LikeFilter;
import org.geotools.filter.LiteralExpression;
import org.geotools.filter.LogicFilter;
import org.geotools.filter.MathExpression;
import org.geotools.filter.NullFilter;


/**
 * Used to duplicate a Filter & or Expression
 *
 * @author Jody Garnett, Refractions Research Inc.
 */
public class DuplicatorFilterVisitor extends AbstractFilterVisitor {
    Stack pages = new Stack(); // need a Stack as Filter structure is recursive
    FilterFactory ff;

    public Stack getPages() {
    	return pages;
    }
    
    public DuplicatorFilterVisitor(FilterFactory factory) {
        ff = factory;
    }

    public void setFilterFactory(FilterFactory factory) {
        ff = factory;
    }

    public void visit(Filter filter) {
        // Should not happen?
    	throw new RuntimeException("visit(Filter) unsupported");
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
    	
    	Iterator iterator = copy.getFilterIterator();
    	
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
    	
    	String[] fids = copy.getFids();
    	for (int i = 0; i < fids.length; i++) {
    		copy.addFid(fids[i]);
    	}
    	
		pages.push(copy);
    }

    public void visit(AttributeExpression expression) {
    	AttributeExpression copy = null;
    	try {
			copy = ff.createAttributeExpression(null, expression.getAttributePath()); //not a true copy, but what the heck...
		} catch (IllegalFilterException erp) {
            throw new RuntimeException(erp);
		} 
    	
        pages.push(copy);
    }

    public void visit(Expression expression) {
        // Should not happen?
    	throw new RuntimeException("visit(Expression) unsupported");
    }

    public void visit(LiteralExpression expression) {
        // TODO Auto-generated method stub
    }

    public void visit(MathExpression expression) {
        // TODO Auto-generated method stub
    }

    public void visit(FunctionExpression expression) {
        // TODO Auto-generated method stub
    }

    public Object getCopy() {
        return pages.firstElement();
    }
}
