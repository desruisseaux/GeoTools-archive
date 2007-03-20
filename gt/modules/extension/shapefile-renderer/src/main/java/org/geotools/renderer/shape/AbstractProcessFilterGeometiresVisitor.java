package org.geotools.renderer.shape;

import org.geotools.filter.AttributeExpression;
import org.geotools.filter.BetweenFilter;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Expression;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterType;
import org.geotools.filter.FilterVisitor;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.LikeFilter;
import org.geotools.filter.LiteralExpression;
import org.geotools.filter.LogicFilter;
import org.geotools.filter.MathExpression;
import org.geotools.filter.NullFilter;

public abstract class AbstractProcessFilterGeometiresVisitor implements FilterVisitor  {

    public void visit(Filter filter) {
        if (Filter.NONE == filter) {
            return;
        }
        switch (filter.getFilterType()) {
        case FilterType.BETWEEN:
            visit((BetweenFilter) filter);
    
            break;
    
        case FilterType.COMPARE_EQUALS:
        case FilterType.COMPARE_GREATER_THAN:
        case FilterType.COMPARE_GREATER_THAN_EQUAL:
        case FilterType.COMPARE_LESS_THAN:
        case FilterType.COMPARE_LESS_THAN_EQUAL:
        case FilterType.COMPARE_NOT_EQUALS:
            visit((BetweenFilter) filter);
    
            break;
    
        case FilterType.FID:
            visit((FidFilter) filter);
    
            break;
    
        case FilterType.GEOMETRY_BBOX:
        case FilterType.GEOMETRY_BEYOND:
        case FilterType.GEOMETRY_CONTAINS:
        case FilterType.GEOMETRY_CROSSES:
        case FilterType.GEOMETRY_DISJOINT:
        case FilterType.GEOMETRY_DWITHIN:
        case FilterType.GEOMETRY_EQUALS:
        case FilterType.GEOMETRY_INTERSECTS:
        case FilterType.GEOMETRY_OVERLAPS:
        case FilterType.GEOMETRY_TOUCHES:
        case FilterType.GEOMETRY_WITHIN:
            visit((GeometryFilter) filter);
    
            break;
    
        case FilterType.LIKE:
            visit((LikeFilter) filter);
    
            break;
    
        case FilterType.LOGIC_AND:
        case FilterType.LOGIC_NOT:
        case FilterType.LOGIC_OR:
            visit((LogicFilter) filter);
    
            break;
    
        case FilterType.NULL:
            visit((NullFilter) filter);
    
            break;
    
        default:
        }
    }

    public void visit(BetweenFilter filter) {
        Expression left = filter.getLeftValue();
        if( left!=null )
            left.accept(this);
        Expression right = filter.getRightValue();
        if( right!=null )
            right.accept(this);
        Expression middle = filter.getMiddleValue();
        if( middle!=null )
            middle.accept(this);
    }

    public void visit(CompareFilter filter) {
        Expression left = filter.getLeftValue();
        if( left!=null )
            left.accept(this);
        Expression right = filter.getRightValue();
        if( right!=null )
            right.accept(this);
    }

    public void visit(GeometryFilter filter) {
        if( filter.getLeftGeometry() instanceof LiteralExpression 
                && filter.getRightGeometry() instanceof LiteralExpression ){
            // affect anything because it is two literals compared
            return;
        }

        Expression left = filter.getLeftGeometry();
        if( left!=null )
            left.accept(this);
        Expression right = filter.getRightGeometry();
        if( right!=null )
            right.accept(this);
    }

    public void visit(LikeFilter filter) {
        Expression value = filter.getValue();
        if( value!=null )
            value.accept(this);
    }

    public void visit(NullFilter filter) {
    }

    public void visit(FidFilter filter) {
    }

    public void visit(AttributeExpression expression) {
    }

    public void visit(Expression expression) {
        expression.accept(this);
    }

    public void visit(MathExpression expression) {
    }

    public void visit(FunctionExpression expression) {
        Expression[] args = expression.getArgs();
        for (int i = 0; i < args.length; i++) {
            Expression expression2 = args[i];
            if( expression2 != null)
                expression2.accept(this);
        }
    }

}