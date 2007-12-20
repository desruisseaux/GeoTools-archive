package org.geotools.filter.visitor;

import java.util.Iterator;

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

/**
 * Abstract implementation of FilterVisitor that simply walks the data structure.
 * <p>
 * This class implements the full FilterVisitor interface and will visit every Filter member of a
 * Filter object. This class performs no actions and is not intended to be used directly, instead
 * extend it and overide the methods for the Filter type you are interested in. Remember to call the
 * super method if you want to ensure that the entier filter tree is still visited. This class can
 * take an ExpressionVisitor as a construction parameter, if provided it will be called for each
 * expression in the Filter object.
 * 
 * <pre><code>
 * FilterVisitor allFids = new DefaultFilterVisitor(){
 *     public Object visit( Id filter, Object data ) {
 *         Set set = (Set) data;
 *         set.addAll(filter.getIDs());
 *         return set;
 *     }
 * };
 * Set set = (Set) myFilter.accept(allFids, new HashSet());
 * </code></pre>
 * 
 * @author Jody
 */
public abstract class DefaultFilterVisitor implements FilterVisitor, ExpressionVisitor {

    public DefaultFilterVisitor() {        
    }

    public Object visit( ExcludeFilter filter, Object data ) {
        return data;
    }

    public Object visit( IncludeFilter filter, Object data ) {
        return data;
    }

    public Object visit( And filter, Object data ) {
        if (filter.getChildren() != null) {
            for( Iterator i = filter.getChildren().iterator(); i.hasNext(); ) {
                Filter child = (Filter) i.next();
                child.accept(this, data);
            }
        }
        return data;
    }

    public Object visit( Id filter, Object data ) {
        return data;
    }

    public Object visit( Not filter, Object data ) {
        if (filter.getFilter() != null) {
            filter.getFilter().accept(this, data);
        }
        return data;
    }

    public Object visit( Or filter, Object data ) {
        if (filter.getChildren() != null) {
            for( Iterator i = filter.getChildren().iterator(); i.hasNext(); ) {
                Filter child = (Filter) i.next();
                child.accept(this, data);
            }
        }
        return data;
    }

    public Object visit( PropertyIsBetween filter, Object data ) {
        filter.getLowerBoundary().accept(this, data);
        filter.getExpression().accept(this, data);
        filter.getUpperBoundary().accept(this, data);
        return data;
    }

    public Object visit( PropertyIsEqualTo filter, Object data ) {
        filter.getExpression1().accept(this, data);
        filter.getExpression2().accept(this, data);

        return data;
    }

    public Object visit( PropertyIsNotEqualTo filter, Object data ) {
        filter.getExpression1().accept(this, data);
        filter.getExpression2().accept(this, data);

        return data;
    }

    public Object visit( PropertyIsGreaterThan filter, Object data ) {
        filter.getExpression1().accept(this, data);
        filter.getExpression2().accept(this, data);

        return data;
    }

    public Object visit( PropertyIsGreaterThanOrEqualTo filter, Object data ) {
        filter.getExpression1().accept(this, data);
        filter.getExpression2().accept(this, data);

        return data;
    }

    public Object visit( PropertyIsLessThan filter, Object data ) {
        filter.getExpression1().accept(this, data);
        filter.getExpression2().accept(this, data);

        return data;
    }

    public Object visit( PropertyIsLessThanOrEqualTo filter, Object data ) {
        filter.getExpression1().accept(this, data);
        filter.getExpression2().accept(this, data);

        return data;
    }

    public Object visit( PropertyIsLike filter, Object data ) {
        filter.getExpression().accept(this, data);

        return data;
    }

    public Object visit( PropertyIsNull filter, Object data ) {
        filter.getExpression().accept(this, data);
        return data;
    }

    public Object visit( final BBOX filter, Object data ) {
        // We will just use a simple wrapper until we add a getExpression method
        PropertyName property = new PropertyName(){
            public String getPropertyName() {
                return filter.getPropertyName();
            }
            public Object accept( ExpressionVisitor visitor, Object data ) {
                return visitor.visit(this, data);
            }
            public Object evaluate( Object object ) {
                return null;
            }
            public Object evaluate( Object object, Class context ) {
                return null;
            }
        };
        property.accept(this, data);
        return data;
    }

    public Object visit( Beyond filter, Object data ) {
        filter.getExpression1().accept(this, data);
        filter.getExpression2().accept(this, data);
        return data;
    }

    public Object visit( Contains filter, Object data ) {
        filter.getExpression1().accept(this, data);
        filter.getExpression2().accept(this, data);
        return data;
    }

    public Object visit( Crosses filter, Object data ) {
        filter.getExpression1().accept(this, data);
        filter.getExpression2().accept(this, data);
        return data;
    }

    public Object visit( Disjoint filter, Object data ) {
        filter.getExpression1().accept(this, data);
        filter.getExpression2().accept(this, data);
        return data;
    }

    public Object visit( DWithin filter, Object data ) {
        filter.getExpression1().accept(this, data);
        filter.getExpression2().accept(this, data);
        return data;
    }

    public Object visit( Equals filter, Object data ) {
        filter.getExpression1().accept(this, data);
        filter.getExpression2().accept(this, data);
        return data;
    }

    public Object visit( Intersects filter, Object data ) {
        filter.getExpression1().accept(this, data);
        filter.getExpression2().accept(this, data);

        return data;
    }

    public Object visit( Overlaps filter, Object data ) {
        filter.getExpression1().accept(this, data);
        filter.getExpression2().accept(this, data);

        return data;
    }

    public Object visit( Touches filter, Object data ) {
        filter.getExpression1().accept(this, data);
        filter.getExpression2().accept(this, data);

        return data;
    }

    public Object visit( Within filter, Object data ) {
        filter.getExpression1().accept(this, data);
        filter.getExpression2().accept(this, data);
        
        return data;
    }

    public Object visitNullFilter( Object data ) {
        return data;
    }

    public Object visit( NilExpression expression, Object data ) {        
        return null;
    }

    public Object visit( Add expression, Object data ) {
        expression.getExpression1().accept( this, data);
        expression.getExpression2().accept( this, data);
        return data;
    }

    public Object visit( Divide expression, Object data ) {
        expression.getExpression1().accept( this, data);
        expression.getExpression2().accept( this, data);        
        return data;
    }

    public Object visit( Function expression, Object data ) {
        for( Iterator i=expression.getParameters().iterator();i.hasNext();){
            Expression parameter = (Expression) i.next();
            parameter.accept( this, data);
        }
        return data;
    }

    public Object visit( Literal expression, Object data ) {        
        return data;
    }

    public Object visit( Multiply expression, Object data ) {
        expression.getExpression1().accept( this, data);
        expression.getExpression2().accept( this, data);                
        return data;
    }

    public Object visit( PropertyName expression, Object data ) {
        return data;
    }

    public Object visit( Subtract expression, Object data ) {
        expression.getExpression1().accept( this, data);
        expression.getExpression2().accept( this, data);                
        return data;
    }

}