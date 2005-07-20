package org.geotools.expr;

import org.geotools.api.filter.Filter;
import org.geotools.api.filter.expression.Expression;
import org.geotools.api.filter.expression.ExpressionVisitor;
import org.geotools.feature.Feature;

/**
 * Abstract superclass of these Expression implementations.
 * <p>
 * Contains additional support for "Expression chaining". This allows
 * Expressions to be constructed as a chain of Java commands similar to the use
 * of the java collections api.
 * </p>
 * <p>
 * Note: Expression chaining is a simple developer convience, it has no effect
 * on the data model exposed by the GeoAPI interfaces.
 * </p>
 * <p>
 * Idea: We may also be able to teach this implementation to make use of
 * JXPath to extract "attribute values" from Java Beans, DOM, JDOM in addition
 * to the geotools & geoapi FeatureType models. It is a cunning plan - any
 * implementation will make use of this abstract base class.
 * </p>
 * 
 * @author Jody Garnett
 *
 */
public class ExpressionAbstract implements Expression {
	/** Used in the construciton of chained Expressions */
	Expr expr;
	protected ExpressionAbstract( Expr factory ){
		expr = factory;
	}
	/** Subclass should overide, default implementation returns null */
	public Object evaluate(Feature feature){
		return null;
	}

	/** Subclass should override, default implementation just returns extraData */
	public Object accept(ExpressionVisitor visitor, Object extraData) {
		return extraData;
	}
	/**
	 * Helper method for subclasses to reduce null checks
	 * @param expression
	 * @param feature
	 * @return value or null
	 */
	protected Object eval( Expression expression, Feature feature ){
		if( expression == null || feature == null ) return null;
		return expression.evaluate( feature );
	}
	/**
	 * Pretend this Expression is a filter, as close to Perl conventions as possible.
	 * <p>
	 * Here is what the Perl concept of truth means conceptually:
	 * <ul>
	 * <li>true
	 * <li>non 0 numeric
	 * <li>non empty String
	 * <li>non null Object
	 * </ul>
	 * </p>
	 * <p>
	 * Here is that what that means exactly:
	 * <ul>
	 * <li>instances of Filter are returned directly
	 * <li>instanceof MathExpression are changed to this.ne( 0 )
	 * <li>default: this.isNull().not()
	 * </ul>
	 * It is very tempting to try and detect empty Geometry for SpatialExpression.
	 * </p>
	 * @return A semblence of boolean truth based on this Expression.
	 */
	public Filter filter(){
		return this.isNull().not();
	}
	private IsNullImpl isNull() {
		return expr.isNull( this );
	}
	public NotImpl not(){
		return expr.not( filter() );
	}
	/** implementation: and( this, filter ) */
	public AndImpl and( Filter filter ){
		return expr.and( filter(), filter );
	}
}
