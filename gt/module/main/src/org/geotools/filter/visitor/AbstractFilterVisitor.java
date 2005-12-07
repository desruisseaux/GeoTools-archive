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

import java.util.Iterator;
import java.util.logging.Logger;
import org.geotools.filter.AttributeExpression;
import org.geotools.filter.BetweenFilter;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Expression;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.LikeFilter;
import org.geotools.filter.LiteralExpression;
import org.geotools.filter.LogicFilter;
import org.geotools.filter.MathExpression;
import org.geotools.filter.NullFilter;


/**
 * A basic implemenation of the FilterVisitor interface.
 * <p>
 * This class implements the full FilterVisitor interface and will visit every
 * member of a Filter object.  This class performs no actions and is not intended
 * to be used directly, instead extend it and overide the methods for the
 * expression types you are interested in.  Remember to call the super method
 * if you want to ensure that the entier filter tree is still visited.
 * </p>
 * <p>
 * You may still need to implement FilterVisitor directly if the visit order
 * set out in this class does not meet your needs.  This class visits in sequence
 * i.e. Left - Middle - Right for all expressions which have sub-expressions.
 * </p>
 * @author James Macgill, Penn State
 */
public class AbstractFilterVisitor implements org.geotools.filter.FilterVisitor {
    
    /** Standard java logger */
    private static Logger LOGGER = Logger.getLogger("org.geotools.filter.visitor");

    /**
     * Empty constructor
     */
    public AbstractFilterVisitor() {
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.Filter)
     */
    public void visit(Filter filter) {
       // James - unknown filter type (not good, should not happen)
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.BetweenFilter)
     */
    public void visit(BetweenFilter filter) {
        if (filter.getLeftValue() != null) {
            filter.getLeftValue().accept(this);
        }

        if (filter.getMiddleValue() != null) {
            filter.getMiddleValue().accept(this);
        }
        
        if (filter.getRightValue() != null) {
            filter.getRightValue().accept(this);
        } 
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.CompareFilter)
     */
    public void visit(CompareFilter filter) {
        if (filter.getLeftValue() != null) {
            filter.getLeftValue().accept(this);
        }

        if (filter.getRightValue() != null) {
            filter.getRightValue().accept(this);
        }
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.GeometryFilter)
     */
    public void visit(GeometryFilter filter) {
        if (filter.getLeftGeometry() != null) {
            filter.getLeftGeometry().accept(this);
        }

        if (filter.getRightGeometry() != null) {
            filter.getRightGeometry().accept(this);
        }
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.LikeFilter)
     */
    public void visit(LikeFilter filter) {
        if (filter.getValue() != null) {
            filter.getValue().accept(this);
        }
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.LogicFilter)
     */
    public void visit(LogicFilter filter) {
        for (Iterator it = filter.getFilterIterator(); it.hasNext();) {
            Filter f = (Filter) it.next();
            f.accept(this);
        }
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.NullFilter)
     */
    public void visit(NullFilter filter) {
        if (filter.getNullCheckValue() != null) {
            filter.getNullCheckValue().accept(this);
        }
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.FidFilter)
     */
    public void visit(FidFilter filter) {
        // nothing to do, the feature id is implicit and should always be
        // included, but cannot be derived from the filter itself 
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.AttributeExpression)
     */
    public void visit(AttributeExpression expression) {
       //nothing to do
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.Expression)
     */
    public void visit(Expression expression) {
      // nothing to do
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.LiteralExpression)
     */
    public void visit(LiteralExpression expression) {
        // nothing to do
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.MathExpression)
     */
    public void visit(MathExpression expression) {
        if (expression.getLeftValue() != null) {
            expression.getLeftValue().accept(this);
        }

        if (expression.getRightValue() != null) {
            expression.getRightValue().accept(this);
        }
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.FunctionExpression)
     */
    public void visit(FunctionExpression expression) {
        Expression[] args = expression.getArgs();

        for (int i = 0; i < args.length; i++) {
            if (args[i] != null) {
                args[i].accept(this);
            }
        }
    }
}
