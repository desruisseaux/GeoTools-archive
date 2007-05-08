package org.geotools.filter.visitor;

import org.opengis.filter.expression.Add;
import org.opengis.filter.expression.Divide;
import org.opengis.filter.expression.ExpressionVisitor;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.Multiply;
import org.opengis.filter.expression.NilExpression;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.expression.Subtract;

/**
 * This class does *nothing* - useful to prevent null checks in AbstractFilterVisitor.
 * 
 * @author Jody
 */
public class NullExpressionVisitor implements ExpressionVisitor{

    public Object visit( NilExpression expression, Object extraData ) {
        return null;
    }

    public Object visit( Add expression, Object extraData ) {
        return null;
    }

    public Object visit( Divide expression, Object extraData ) {
        return null;
    }

    public Object visit( Function expression, Object extraData ) {
        return null;
    }

    public Object visit( Literal expression, Object extraData ) {
        return null;
    }

    public Object visit( Multiply expression, Object extraData ) {
        return null;
    }

    public Object visit( PropertyName expression, Object extraData ) {
        return null;
    }

    public Object visit( Subtract expression, Object extraData ) {
        return null;
    }

}
