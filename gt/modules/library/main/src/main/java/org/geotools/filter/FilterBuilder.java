package org.geotools.filter;

import java.util.ArrayList;
import java.util.List;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.Hints;
import org.opengis.filter.And;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Filter;
import org.opengis.filter.Or;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;

/**
 * Facilitate the building of Filters and Expressions by maintaining state.
 * <p>
 * This builder let's you chain together expressions using Java methods:<pre><code>
 * FilterBuilder build = new FilterBuilder();
 * 
 * Expression expression = build.name( "cos" ).attribute( "theta" ).function().add().attribute( "x" ) ).expr();
 * Filter filter = build.attribute( "y" ).greater().literal( 1.0 ) ).filter();
 * </code></pre>
 * It also supports nested use:<pre><code>
 * Expression expression = build.name( "cos" ).property( "theta" ).add( build.attribute( "x" ) );
 * Filter filter = build.attribute( "y" ).greater( build.literal( 1.0 ) );
 * </code></pre>
 * This works in the following ways:
 * <ul>
 * <li>state is maintained, expression "pile up" in a stack until used
 * <li>infix operations, such as add() transfer the existing expression to the right hand side, all 
 * The following state is of interest:
 * <table>
 * <tr>
 * <th>method</th>
 * <th>left</th>
 * <th>right</th>
 * </tr>
 * <tr>
 *    <td>and</td>
 * </tr>
 * 
 * @author Jody Garnett, Refractions Research Inc.
 */
public class FilterBuilder {
    FilterFactory ff;
    
    /**
     * Currently represented filter.
     * <p>
     * May be combined with other filters using:
     * <ul>
     * <li>and
     * <li>or
     * <li>not
     * </ul>
     */
    List filter = new ArrayList(2);
        
    /**
     * List of Expression(s) used in creating Filters, or Functions.
     * <p>
     * Used in the creation of Filters (or function calls)
     * <p>
     * <ol>
     * <li>Left or Parameter 0
     * <li>Middle or Parameter 1
     * <li>Right, or Parameter 2
     * <li>Parameter 3, etc...
     * </ul>
     */
    List expression = new ArrayList(3);

    private String name;
    
    public FilterBuilder literal( byte number ){
        expression.add( ff.literal( number ));
        return this;
    }
    public FilterBuilder literal( short number ){
        expression.add( ff.literal( number ));
        return this;
    }
    public FilterBuilder literal( int number ){
        expression.add( ff.literal( number ));
        return this;
    }
    public FilterBuilder literal( long number ){
        expression.add( ff.literal( number ));
        return this;
    }
    public FilterBuilder literal( double number ){
        expression.add( ff.literal( number ));
        return this;
    }
    public FilterBuilder literal( Object object ){
        expression.add( ff.literal( object ));
        return this;
    }
    public Expression expr(){
        if( expression.isEmpty() ){
            return Expression.NIL;
        }
        return (Expression) expression.remove( expression.size()-1);
    }
    private FilterBuilder build( Expression expression ){
         this.expression.add( expression  );
         return this;
    }
    private FilterBuilder build( Filter filter ){
    	this.filter.add( filter );
        return this;
    }
    /**
     * Create a FilterBuilder with the "default" FilterFactory.
     * <p>
     * The default filter factory is produced using:
     * <code>CommonFactoryFinder.getFilterFactory(null)</code>
     *
     */
    public FilterBuilder(){
        this( CommonFactoryFinder.getFilterFactory(null));        
    }
    /**
     * Create a FilterBuilder using the provided Hints.
     * <p>
     * The filter factory is produced using:
     * <code>CommonFactoryFinder.getFilterFactory(hints)</code>
     * 
     * @param hints Used to control the selection of a FilterFactory implementation
     */
    public FilterBuilder( Hints hints ){
        this( CommonFactoryFinder.getFilterFactory( hints ));
    }
    /**
     * Create a FilterBuilder using the provided Factory.
     * @param filterFactory
     */
    public FilterBuilder(FilterFactory filterFactory) {
        ff = filterFactory;
    }
    public void setFilterFactory( FilterFactory factory ){
        ff = factory;
    }
    public void init(){
    	this.expression.clear();
    	this.filter.clear();
    }
    public void init( Filter filter ){
    	 init();
         this.filter.add( filter );
    }
    public void init( Expression expr ){
    	init();
    	this.expression.add( expr );   
   }
    /** Create a Filter based on builder state. */
    public Filter filter(){
        if( filter.isEmpty() ){
            return Filter.EXCLUDE;
        }
        return (Filter) filter.remove( filter.size()-1);
    }

    public FilterBuilder left( Expression left ){
        setLeft(left);
        return this;
    }
    public void setLeft(Expression left) {
        setExpression( 0, left );
    }
    /**
     * Set the right expression (usually in refernece to an infix operator such as ADD.
     * <p>
     * Example:<code>build.left( expr1 ).right( expr2 ).add();
     * </p>
     * @param right
     * @return Builder for use in chaining
     */
    public FilterBuilder right( Expression right ){
        setRight(right);
        return this;
    }
    /**
     * Set the right expression (in reference to an infix operator such as ADD).
     * @param right
     */
    public void setRight(Expression right) {
        setExpression( 2, right );
    }
    
    /**
     * Used to insert provided expressions into the list (for later use).
     * <p>
     * Example 1:
     * <code>build.expression( expr1 ).expression( expr2 ).expression( expr3 ).function("sum");
     * @param expr Expression to add to builder
     * @return builder with provided expr added
     */
    public FilterBuilder expression( Expression expr){
        this.expression.add( expr );
        return this;
    }
    public FilterBuilder expression( int position, Expression expr ){
        setExpression( position, expr );
        return this;
    }
    public void setExpression(int position, Expression expr) {
        while( expression.size() < position ){
            expression.add( null );
        }
        this.expression.set( position, expr);
    }

    /** Name (currently just used for function calls).
     * <p>
     * Example:
     * <code>expr.name("sin").literal( 45 ).function();</code>
     * @return builder (for use in chaning)
     */
    FilterBuilder name( String name ){
        this.name = name;
        return this;
    }
    /**
     * Create a Function based on name and expression list.
     * <p>
     * Example:<pre><code>
     * build.name("sine").property("angle").function();
     * </code></pre>
     * @return Function based on name and previous expressions
     */
    Function function(){
        Function function = ff.function( name, (Expression[]) expression.toArray( new Expression[ expression.size()]));
        expression.clear();
        expression.add( function );
        return function;
    }
    Function function( String functionName ){
    	this.name( functionName );
    	return function();
    }
    public Expression add() {
		return add(this).expr();
	}

	public FilterBuilder add(Object number) {
		return add(ff.literal(number));
	}

	public FilterBuilder add(long number) {
		return add(ff.literal(number));
	}

	public FilterBuilder add(double number) {
		return add(ff.literal(number));
	}

	public FilterBuilder add(FilterBuilder build) {
		return add(build.expr());
	}

	public FilterBuilder add(Expression right) {
		Expression left = expr();
		return build(ff.add(left, right));
	}
    
	public Expression subtract() {
		return subtract(this).expr();
	}

	public FilterBuilder subtract(Object number) {
		return subtract(ff.literal(number));
	}

	public FilterBuilder subtract(long number) {
		return subtract(ff.literal(number));
	}

	public FilterBuilder subtract(double number) {
		return subtract(ff.literal(number));
	}

	public FilterBuilder subtract(FilterBuilder build) {
		return subtract(build.expr());
	}

	public FilterBuilder subtract(Expression right) {
		Expression left = expr();
		return build(ff.subtract(left, right));
	}
	
	public Expression multiply() {
		return multiply(this).expr();
	}
	public FilterBuilder multiply(Object number) {
		return multiply(ff.literal(number));
	}

	public FilterBuilder multiply(long number) {
		return multiply(ff.literal(number));
	}

	public FilterBuilder multiply(double number) {
		return multiply(ff.literal(number));
	}

	public FilterBuilder multiply(FilterBuilder build) {
		return multiply(build.expr());
	}

	public FilterBuilder multiply(Expression right) {
		Expression left = expr();
		return build(ff.multiply(left, right));
	}
	/**
	 * Example: build.literal(1.0).literal("2").divide();
	 * 
	 * @return division of the last two expressions
	 */
	public Expression divide(){
	    return divide( this ).expr();
	}
	public FilterBuilder divide( long number ){
		return divide( ff.literal( number ));
	}
	public FilterBuilder divide( double number){
		return divide( ff.literal( number ));
	}
	public FilterBuilder divide( FilterBuilder build ){
    	return divide( build.expr() );    	
    }
	public FilterBuilder divide(Expression right) {
		Expression left = expr();		
		return build( ff.divide( left, right ) );
	}
	/**
	 * Collapse all built filters into a single AND filter.
	 * <p>
	 * Example:<pre>
	 * build.property("age").less( build.literal( 23 );
	 * build.property("sex").equal( build.literal("male") );
	 * build.and();
	 * </pre>
	 * Returns all filters as an *And* filter.
	 * 
	 */
	public Filter and(){
		if( filter.isEmpty()){
			return Filter.EXCLUDE;
		}
		else if (filter.size() == 1 ){
			return filter();
		}
		Filter and = ff.and( this.filter );
		
		this.filter = new ArrayList(2);		
		return and;
	}
	
	/** A.and( B ) */
    public FilterBuilder and( FilterBuilder build ){
    	return and( build.filter() );    	
    }
    /** A.and( B ) */
	public FilterBuilder and(Filter right) {
		Filter left = filter();
		if( left instanceof And){
			// logic to collapse multiple AND filters
			// occurs in a builder (rather then factory)
			//
			And and = (And) left;
			List children = new ArrayList( and.getChildren() );
			children.add( right );
			return build( ff.and( children) );
		}
		return build(ff.and(left, right));
	}

	/** Take all built filters and return a single OR statement.
	 * 
	 * @return OR filter based on all previous content.
	 */
	public Filter or(){
		if( filter.isEmpty()){
			return Filter.INCLUDE;
		}
		else if (filter.size() == 1 ){
			return filter();
		}
		Filter or = ff.or( this.filter );
		
		this.filter = new ArrayList(2);		
		return or;
	}
	
	/** A.or( B ) */
    public FilterBuilder or( FilterBuilder build ){
    	return or( build.filter() );    	
    }
    /** A.or( B ) */
	public FilterBuilder or(Filter right) {
		Filter left = filter();
		if( left instanceof Or){
			// logic to collapse multiple OR filters
			// occurs in a builder (rather then factory)
			//
			Or or = (Or) left;
			List children = new ArrayList( or.getChildren() );
			children.add( right );
			return build( ff.or( children) );
		}
		return build(ff.or(left, right));
	}
	
}

