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
package org.geotools.filter;

import java.util.Iterator;
import org.geotools.data.jdbc.fidmapper.FIDMapper;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.geotools.filter.parser.ParseException;


/**
 *
 */
public class EnvironmentVariableResolver implements org.geotools.filter.FilterVisitor {
   

  
    /** Standard java logger */
    private static Logger LOGGER = Logger.getLogger("org.geotools.filter");
   
  
    /**
     * Empty constructor
     */
    public EnvironmentVariableResolver() {
    }

    

   

   
    /**
     *
     */
    public void resolve(Filter filter, double mapScale) throws ParseException {  
        
         String input = filter.toString();
        Filter output = (Filter)ExpressionBuilder.parse(input);
        
    
    }
    
    
    /**
     * 
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
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.Filter)
     */
    public void visit(Filter filter) {
        if (filter instanceof BetweenFilter) {
            visit((BetweenFilter) filter);
        } else if (filter instanceof CompareFilter) {
            visit((CompareFilter) filter);
        } else if (filter instanceof GeometryFilter) {
            visit((GeometryFilter) filter);
        } else if (filter instanceof LikeFilter) {
            visit((LikeFilter) filter);
        } else if (filter instanceof LogicFilter) {
            visit((LogicFilter) filter);
        } else if (filter instanceof NullFilter) {
            visit((NullFilter) filter);
        } else if (filter instanceof FidFilter) {
            visit((FidFilter) filter);
        }
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.BetweenFilter)
     */
    public void visit(BetweenFilter filter) {
        if (filter.getLeftValue() != null) {
            filter.getLeftValue().accept(this);
        }

        if (filter.getRightValue() != null) {
            filter.getRightValue().accept(this);
        }

        if (filter.getMiddleValue() != null) {
            filter.getMiddleValue().accept(this);
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
       
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.Expression)
     */
    public void visit(Expression expression) {
      if(expression instanceof MapScaleDenominator){
        System.err.println("FOUND ONE");
      }
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
