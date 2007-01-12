package org.geotools.filter;

import java.util.ArrayList;
import java.util.List;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.Hints;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Filter;
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
    Filter filter;
        
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
    public void init( Filter filter ){
        
    }
    /** Create a Filter based on builder state. */
    public Filter filter(){
        return filter;
    }
    public FilterBuilder and( Filter filter ){
        this.filter = ff.and( this.filter, filter );
        return this;
    }

    public FilterBuilder left( Expression left ){
        setLeft(left);
        return this;
    }
    public void setLeft(Expression left) {
        setExpression( 0, left );
    }
    public FilterBuilder right( Expression right ){
        setRight(right);
        return this;
    }
    public void setRight(Expression right) {
        setExpression( 2, right );
    }
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
     * expr.name("sin").expression( 45 ).function();
     */
    FilterBuilder name( String name ){
        this.name = name;
        return this;
    }
    /**
     * Create a Function based on name and expression list.
     * 
     * @return Function
     */
    Function function(){
        Function function = ff.function( name, (Expression[]) expression.toArray( new Expression[ expression.size()]));
        expression.clear();
        expression.add( function );
        return function;
    }
    
    public Expression add( FilterBuilder build ) {
        return add( build.expr() );
    }
    public Expression add( Expression left ) {
        Expression right = expr();
        return ff.add( right, left );
    }
    public Expression add() {
        Expression right = expr();
        Expression left = expr();
        return ff.add( left, right );
    }
    
}
