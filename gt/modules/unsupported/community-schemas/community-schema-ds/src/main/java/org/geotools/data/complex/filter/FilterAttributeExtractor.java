package org.geotools.data.complex.filter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.geotools.filter.GeometryFilter;
import org.opengis.filter.And;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.Id;
import org.opengis.filter.IncludeFilter;
import org.opengis.filter.Not;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNotEqualTo;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.Add;
import org.opengis.filter.expression.Divide;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.ExpressionVisitor;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.Multiply;
import org.opengis.filter.expression.NilExpression;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.expression.Subtract;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Beyond;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.DWithin;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.Equals;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Touches;
import org.opengis.filter.spatial.Within;

public class FilterAttributeExtractor implements FilterVisitor, ExpressionVisitor {

    private List attributeNames = new LinkedList();

    public Set getAttributeNameSet() {
        return new HashSet(attributeNames);
    }

    public Object visit(ExcludeFilter filter, Object extraData) {
        return null;
    }

    public Object visit(IncludeFilter filter, Object extraData) {
        return null;
    }

    private void visitFilters(Collection filters) {
        for (Iterator it = filters.iterator(); it.hasNext();) {
            Filter f = (Filter) it.next();
            f.accept(this, null);
        }
    }

    public Object visit(And filter, Object extraData) {
        visitFilters(filter.getChildren());
        return null;
    }

    public Object visit(Id filter, Object extraData) {
        return null;
    }

    public Object visit(Not filter, Object extraData) {
        filter.getFilter().accept(this, null);
        return null;
    }

    public Object visit(Or filter, Object extraData) {
        visitFilters(filter.getChildren());
        return null;
    }

    public Object visit(PropertyIsBetween filter, Object extraData) {
        filter.getExpression().accept(this, null);
        filter.getLowerBoundary().accept(this, null);
        filter.getUpperBoundary().accept(this, null);
        return null;
    }

    public Object visit(PropertyIsEqualTo filter, Object extraData) {
        filter.getExpression1().accept(this, null);
        filter.getExpression2().accept(this, null);
        return null;
    }

    public Object visit(PropertyIsNotEqualTo filter, Object extraData) {
        filter.getExpression1().accept(this, null);
        filter.getExpression2().accept(this, null);
        return null;
    }

    public Object visit(PropertyIsGreaterThan filter, Object extraData) {
        filter.getExpression1().accept(this, null);
        filter.getExpression2().accept(this, null);
        return null;
    }

    public Object visit(PropertyIsGreaterThanOrEqualTo filter, Object extraData) {
        filter.getExpression1().accept(this, null);
        filter.getExpression2().accept(this, null);
        return null;
    }

    public Object visit(PropertyIsLessThan filter, Object extraData) {
        filter.getExpression1().accept(this, null);
        filter.getExpression2().accept(this, null);
        return null;
    }

    public Object visit(PropertyIsLessThanOrEqualTo filter, Object extraData) {
        filter.getExpression1().accept(this, null);
        filter.getExpression2().accept(this, null);
        return null;
    }

    public Object visit(PropertyIsLike filter, Object extraData) {
        filter.getExpression().accept(this, null);
        return null;
    }

    public Object visit(PropertyIsNull filter, Object extraData) {
        filter.getExpression().accept(this, null);
        return null;
    }

    public Object visit(BBOX filter, Object extraData) {
        String propertyName = filter.getPropertyName();
        if (propertyName != null) {
            this.attributeNames.add(propertyName);
        } else if (filter instanceof GeometryFilter) {
            // bad behavior due to weak BBoxImpl implementation in geotools
            GeometryFilter geomFilter = (GeometryFilter) filter;
            Expression expression1 = geomFilter.getExpression1();
            Expression expression2 = geomFilter.getExpression2();
            expression1.accept(this, null);
            expression2.accept(this, null);
        }
        return null;
    }

    public Object visit(Beyond filter, Object extraData) {
        filter.getExpression1().accept(this, null);
        filter.getExpression2().accept(this, null);
        return null;
    }

    public Object visit(Contains filter, Object extraData) {
        filter.getExpression1().accept(this, null);
        filter.getExpression2().accept(this, null);
        return null;
    }

    public Object visit(Crosses filter, Object extraData) {
        filter.getExpression1().accept(this, null);
        filter.getExpression2().accept(this, null);
        return null;
    }

    public Object visit(Disjoint filter, Object extraData) {
        filter.getExpression1().accept(this, null);
        filter.getExpression2().accept(this, null);
        return null;
    }

    public Object visit(DWithin filter, Object extraData) {
        filter.getExpression1().accept(this, null);
        filter.getExpression2().accept(this, null);
        return null;
    }

    public Object visit(Equals filter, Object extraData) {
        filter.getExpression1().accept(this, null);
        filter.getExpression2().accept(this, null);
        return null;
    }

    public Object visit(Intersects filter, Object extraData) {
        filter.getExpression1().accept(this, null);
        filter.getExpression2().accept(this, null);
        return null;
    }

    public Object visit(Overlaps filter, Object extraData) {
        filter.getExpression1().accept(this, null);
        filter.getExpression2().accept(this, null);
        return null;
    }

    public Object visit(Touches filter, Object extraData) {
        filter.getExpression1().accept(this, null);
        filter.getExpression2().accept(this, null);
        return null;
    }

    public Object visit(Within filter, Object extraData) {
        filter.getExpression1().accept(this, null);
        filter.getExpression2().accept(this, null);
        return null;
    }

    public Object visitNullFilter(Object filter) {
        return null;
    }

    public Object visit(NilExpression expression, Object extraData) {
        return null;
    }

    public Object visit(Add expression, Object extraData) {
        expression.getExpression1().accept(this, null);
        expression.getExpression2().accept(this, null);
        return null;
    }

    public Object visit(Divide expression, Object extraData) {
        expression.getExpression1().accept(this, null);
        expression.getExpression2().accept(this, null);
        return null;
    }

    public Object visit(Function expression, Object extraData) {
        for (Iterator it = expression.getParameters().iterator(); it.hasNext();) {
            Expression exp = (Expression) it.next();
            exp.accept(this, null);
        }
        return null;
    }

    public Object visit(Literal expression, Object extraData) {
        return null;
    }

    public Object visit(Multiply expression, Object extraData) {
        expression.getExpression1().accept(this, null);
        expression.getExpression2().accept(this, null);
        return null;
    }

    public Object visit(PropertyName expression, Object extraData) {
        String propertyName = expression.getPropertyName();
        this.attributeNames.add(propertyName);
        return null;
    }

    public Object visit(Subtract expression, Object extraData) {
        expression.getExpression1().accept(this, null);
        expression.getExpression2().accept(this, null);
        return null;
    }
}
