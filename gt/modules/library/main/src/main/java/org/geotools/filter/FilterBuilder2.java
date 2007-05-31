package org.geotools.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.Hints;
import org.opengis.filter.And;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Or;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;

/**
 * Facilitate the building of Filters and Expressions by maintaining state.
 * <p>
 * This builder let's you chain together expressions using Java methods:
 * 
 * <pre><code>
 * FilterBuilder build = new FilterBuilder();
 * 
 * Expression expression = build.name( &quot;cos&quot; ).attribute( &quot;theta&quot; ).function().add().attribute( &quot;x&quot; ) ).expr();
 * Filter filter = build.attribute( &quot;y&quot; ).greater().literal( 1.0 ) ).filter();
 * </code></pre>
 * 
 * It also supports nested use:
 * 
 * <pre><code>
 * Expression expression = build.name(&quot;cos&quot;).property(&quot;theta&quot;).add(
 * 		build.attribute(&quot;x&quot;));
 * 
 * Filter filter = build.attribute(&quot;y&quot;).greater(build.literal(1.0));
 * </code></pre>
 * 
 * This works in the following ways:
 * <ul>
 * <li>state is maintained, expression "pile up" in a stack until used
 * <li>infix operations, such as add() transfer the existing expression to the
 * right hand side, all The following state is of interest: <table>
 * <tr>
 * <th>method</th>
 * <th>left</th>
 * <th>right</th>
 * </tr>
 * <tr>
 * <td>and</td>
 * </tr>
 * 
 * @author Jody Garnett, Refractions Research Inc.
 */
public class FilterBuilder2 {
	FilterFactory ff;

	/**
	 * Stack<BuilderState> of builder state.
	 */
	Stack stack = new Stack();
	
	/**
	 * Used to "set" the current BuilderState, actually pushes State onto the
	 * top of an internal stack.
	 * 
	 * @param state
	 */
	void state( BuilderState state ){
		if( !stack.isEmpty() ){
			BuilderState current = (BuilderState) stack.peek();
			
			if( current.isComplete() ){				
				Object value = build();
				
				// value is ready to go; but user wants to refine it more ... must be an undefined infix
				//
				UndefinedState undefined = new UndefinedState();
				if( value instanceof Filter ){
					undefined.add( (Filter) value );
				}
				else if ( value instanceof Expression ){
					undefined.add( (Expression) value );
				}
				else {
					throw new IllegalStateException( "Unexpected value");
				}
				stack.push( undefined );
				current = undefined;
			}
			if( current instanceof UndefinedState ){
				UndefinedState undefined = (UndefinedState) current;
				if( !undefined.isEmpty() ){
				    boolean replace = state.init( (UndefinedState) current );
				    if( replace ){
				    	// used current undefined state up .. replace with defined state
					    stack.pop();
				    }
				    else {
				    	// still not defined ... but we need to work out this nested state first
				    }
				}
			}
		}
		stack.push( state );
	}
	
	/**
	 * Retrive the current builder state - top of the stack.
	 * 
	 * @return BuilderState
	 */
	BuilderState state(){
		if( stack.isEmpty() ){
			stack.push( new UndefinedState() ); // let's have something to catch
		}
		BuilderState state = (BuilderState) stack.peek();
		return state;
	}
	/**
	 * Operators such as substract() are eager about grabbing as many
	 * states as are completed.
	 * @return BuilderState 
	 */
	BuilderState state2(){
		if( stack.size() > 2 ){
			return null; // nothing defined yet
		}
		return (BuilderState) stack.get( stack.size()-2);
	}
	
	public FilterBuilder2 literal(byte number) {
		return expression(ff.literal(number));
	}

	public FilterBuilder2 literal(short number) {
		return expression(ff.literal(number));
	}

	public FilterBuilder2 literal(int number) {
		return expression(ff.literal(number));
	}

	public FilterBuilder2 literal(long number) {
		return expression(ff.literal(number));
	}

	public FilterBuilder2 literal(double number) {
		return expression(ff.literal(number));
	}

	public FilterBuilder2 literal(Object object) {
		return expression( ff.literal(object) );
	}
	
	/**
	 * Used to insert provided expressions into the list (for later use).
	 * <p>
	 * Examples:
	 * <ul>
	 * <li>build.expression( expr1 ).add().expression( expr2 );
	 * <li>build.expression( expr1 ).expression( expr2 ).expression( expr3 ).function("sum");
	 * </ul>
	 * @param expr Expression to add to builder
	 * @return builder with provided expr added
	 */
	private FilterBuilder2 expression(Expression expression) {
		state().add( expression );
		simplify();
		return this;
	}

	/**
	 * Create a FilterBuilder with the "default" FilterFactory.
	 * <p>
	 * The default filter factory is produced using:
	 * <code>CommonFactoryFinder.getFilterFactory(null)</code>
	 * 
	 */
	public FilterBuilder2() {
		this(CommonFactoryFinder.getFilterFactory(null));
	}

	/**
	 * Create a FilterBuilder using the provided Hints.
	 * <p>
	 * The filter factory is produced using:
	 * <code>CommonFactoryFinder.getFilterFactory(hints)</code>
	 * 
	 * @param hints
	 *            Used to control the selection of a FilterFactory
	 *            implementation
	 */
	public FilterBuilder2(Hints hints) {
		this(CommonFactoryFinder.getFilterFactory(hints));
	}

	/**
	 * Create a FilterBuilder using the provided Factory.
	 * 
	 * @param filterFactory
	 */
	public FilterBuilder2(FilterFactory filterFactory) {
		ff = filterFactory;
	}

	public void setFilterFactory(FilterFactory factory) {
		ff = factory;
	}

	public void init() {
		this.stack.clear();
	}

	public Expression expr() {
		// we did not check isCompleted first in the hopes of getting a more
		// specific error message
		return (Expression) build();
    }
	
	/** Create a Filter based on builder state. */
	public Filter filter() {
		return (Filter) build();
	}
	
	/**
	 * Churns through builder state building as much as is completed.
	 * 
	 * @return Value represented by builder
	 */
	public Object build(){
		BuilderState state = state();		
		return state.build();
	}
	/**
	 * Collapse any completed states (eager matching).
	 * <p>
	 * Please note that some states are *never* completed automatically:
	 * <ul>
	 * <li>AND
	 * <li>OR
	 * <li>FUNCTION
	 * </ul>
	 */
	private void simplify(){
		while( state().isComplete() ){
			BuilderState state = state();
			Object value = state.build(); // this will "pop" state off the stack as completed
			if( value instanceof Expression){
				Expression expression = (Expression) value;				
				state().add( expression );
			}
			else if( value instanceof Filter ){
				Filter filter = (Filter) value;				
				state().add( filter );
			}
			else {
				throw new IllegalStateException("Completed but did not produce a result");
			}
		}
	}
	
	public FilterBuilder2 left(Expression left) {
		state().setLeftExpression(left);
		simplify();				
		return this;
	}

	public void setLeft(Expression left) {
		state().setLeftExpression( left );
		simplify();		
	}
	
	/**
	 * Set the right expression (usually in refernece to an infix operator such
	 * as ADD.
	 * <p>
	 * Example:<code>build.left( expr1 ).right( expr2 ).add();
	 * </p>
	 * @param right
	 * @return Builder for use in chaining
	 */
	public FilterBuilder2 right(Expression right) {
		state().setRightExpression( right );
		simplify();		
		return this;
	}
	/**
	 * Set the right expression (in reference to an infix operator such as ADD).
	 * 
	 * @param right
	 */
	public void setRight(Expression right) {
		state().setRightExpression( right );
		simplify();		
	}
	
	public FilterBuilder2 expression(int position, Expression expr) {
		state().setExpression(position, expr);
		simplify();		
		return this;
	}
	public void setExpression(int position, Expression expr) {
		state().setExpression( position, expr );
		simplify();		
	}
	
	public FilterBuilder2 filter(Filter filter) {
		state().add( filter );
		simplify();		
		return this;
	}

	/**
	 * Name (currently just used for function calls).
	 * <p>
	 * Example: <code>expr.name("sin").literal( 45 ).function();</code>
	 * 
	 * @return builder (for use in chaning)
	 */
	FilterBuilder2 name(String name) {
		state().setName( name );
		return this;
	}

	/**
	 * Create a Function based on name and expression list.
	 * <p>
	 * Example:
	 * 
	 * <pre><code>
	 * build.name(&quot;sine&quot;).property(&quot;angle&quot;).function();
	 * </code></pre>
	 * 
	 * @return Function based on name and previous expressions
	 */
	FilterBuilder2 function() {
		state( new FunctionState() ); // may slurp up undefined.expression during init
		return this;
	}

	FilterBuilder2 function(String functionName) {
		state( new FunctionState() );
		state().setName( functionName );
		return this;
	}

	public FilterBuilder2 add() {
		state( new AddState() );
		return this;
	}
	public FilterBuilder2 add(Object number) {
		return add(ff.literal(number));
	}
	public FilterBuilder2 add(int number) {
		return add(ff.literal(number));
	}
	public FilterBuilder2 add(long number) {
		return add(ff.literal(number));
	}

	public FilterBuilder2 add(double number) {
		return add(ff.literal(number));
	}

	public FilterBuilder2 add(FilterBuilder2 build) {
		return add( build.expr() );		
	}

	public FilterBuilder2 add(Expression right) {
		state( new AddState() );
		expression( right );		
		return this;
	}

	/**
	 * Sets the builder into "subtract" mode.
	 * <p>
	 * Example use (from nothing):
	 * <ul>
	 * <li>prefix: build.subtract().literal(1).literal(1);
	 * <li>infix: build.literal(1).subtract().literal(1);
	 * <li>postfix: build.literal(1).literal(2).subtract();
	 * </ul>
	 * 
	 * @return builder with modified state
	 */
	public FilterBuilder2 subtract(){
		state( new SubtractState() );
		return this;
	}

	public FilterBuilder2 subtract(Object number) {
		return subtract(ff.literal(number));
	}

	public FilterBuilder2 subtract(long number) {
		return subtract(ff.literal(number));
	}

	public FilterBuilder2 subtract(double number) {
		return subtract(ff.literal(number));
	}

	public FilterBuilder2 subtract(FilterBuilder build) {
		return subtract(build.expr());
	}

	public FilterBuilder2 subtract(Expression right) {
		state( new SubtractState() );
		expression( right );		
		return this;
	}

	public FilterBuilder2 multiply() {
		state( new MultiplyState() );
		return this;
	}

	public FilterBuilder2 multiply(Object number) {
		return multiply(ff.literal(number));
	}

	public FilterBuilder2 multiply(long number) {
		return multiply(ff.literal(number));
	}

	public FilterBuilder2 multiply(double number) {
		return multiply(ff.literal(number));
	}

	public FilterBuilder2 multiply(FilterBuilder build) {
		return multiply(build.expr());
	}

	public FilterBuilder2 multiply(Expression right) {
		state( new MultiplyState() );
		expression( right );
		return this;
	}

	/**
	 * Example: build.literal(1.0).literal("2").divide();
	 * 
	 * @return division of the last two expressions
	 */
	public FilterBuilder2 divide() {
		state( new DivideState() );
		return this;
	}

	public FilterBuilder2 divide(long number) {
		return divide(ff.literal(number));
	}

	public FilterBuilder2 divide(double number) {
		return divide(ff.literal(number));
	}

	public FilterBuilder2 divide(FilterBuilder build) {
		return divide(build.expr());
	}

	public FilterBuilder2 divide(Expression right) {
		state( new DivideState() );
		expression( right );
		return this;
	}

	/**
	 * Collapse all built filters into a single AND filter.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * build.property(&quot;age&quot;).less( build.literal( 23 );
	 * build.property(&quot;sex&quot;).equal( build.literal(&quot;male&quot;) );
	 * build.and();
	 * </pre>
	 * 
	 * Returns all filters as an *And* filter.
	 * 
	 */
	public FilterBuilder2 and() {
		state( new AndState() );
		return this;
	}

	/** A.and( B ) */
	public FilterBuilder2 and(FilterBuilder build) {
		return and(build.filter());
	}

	/** A.and( B ) */
	public FilterBuilder2 and(Filter right) {
		state( new AndState() );
		filter( right );
		return this;
	}

	/**
	 * Take all built filters and return a single OR statement.
	 * <p>
	 * Examples:
	 * <ul>
	 * <li>build.or().filter( filter1 ).filter( filter2 ).filter( filter3);
	 * <li>build.filter( filter1 ).or().filter( filter2 );
	 * </ul>
	 * @return OR filter based on all previous content.
	 */
	public FilterBuilder2 or() {
		state( new OrState() );
		return this;
	}

	/** A.and( B ) */
	public FilterBuilder2 or(FilterBuilder build) {
		return or(build.filter());
	}

	/** A.and( B ) */
	public FilterBuilder2 or(Filter right) {
		state( new OrState() );
		filter( right );
		return this;	
	}
	
	/**
	 * This stratagy object represents the current builder state.
	 * <p>
	 * After each call to a state changing method the builder can check the following:<pre><code>
	 * if( state.isComplete() ){
	 *     Object build();
	 * }
	 * </code></pre>
	 * The type of object returned by the build method may be used to fill in the previous state, forming
	 * a chain of construction.
	 * 
	 * @author Jody Garnett
	 */
	abstract class BuilderState {
		/**
		 * 
		 * @param undefined
		 * @return true if provided undefined could was used (and can now be replaced), false otherwise.
		 */
		public boolean init( UndefinedState undefined ){
			return false;
		}
		public void setName( String name ){
			throw new IllegalStateException("Name not expected");
		}
		public void setLeftExpression( Expression expression ){
	        add( expression );
		}
		public Expression getLeftExpression(){
			return null;
		}
		public void addFilter( Filter filter){
	        add( filter );
		}
		public void setRightExpression( Expression expression ){
	    	add( expression );
		}
		public Expression getRightExpression(){
			return null;
		}
		public void setExpression( int position, Expression expression){
   		    throw new IllegalStateException("Expression "+position+" not expected");
		}
		/**
		 * Called when a new expression is made available via build.expression( expr )
		 * <p>
		 * Subclass is expected to fill the next available expression slot, and possible
		 * complete.
		 * @param expression
		 * @return Filter or Expression (if complete), or null if still incomplete.
		 */
	    public void add( Expression expression ){
	    	throw new IllegalStateException("Expression not expected");
	    }
	    /**
	     * Only used by logical filters (AND, OR, NOT).
	     * 
	     * @param filter New filter made available by user
	     * @return Filter or Expression if compelte, or null if incomplete
	     */
	    public void add( Filter filter ){
	    	throw new IllegalStateException("Filter not expected");
	    }
	    
	    /**
	     * True if state is completed, added any additional expressions or filters
	     * would be meaningless.
	     * 
	     * @return true of expression is complete
	     */
	    public boolean isComplete(){
	    	return false;
	    }
	    
	    /**
	     * Return the content for this state of the builder.
	     * @return
	     */
	    public abstract Object build();
	}
	/**
	 * Used to create an And filter from a list of gathered filters.
	 */
	class AndState extends BuilderState {
		List filters;
		AndState(){
			filters = new ArrayList(2);
		}
		public boolean init( UndefinedState undefined ){
			filters = undefined.filters;
			return true;
		}
		/** We cannot handle expressions - create an UndefinedState to handle it for us */
		public void add(Expression expression) {
			if( expression == null ){
				throw new NullPointerException("Expression required");
			}
			BuilderState undefined = new UndefinedState();
			undefined.add( expression );
			stack.push( undefined);
		}
		/** Accept new Filters */
		public void add(Filter filter) {
			if( filter == null ){
				throw new NullPointerException("Filter required");
			}
			if( filter instanceof And ){
				And and = (And) filter;
				filters.addAll( and.getChildren() );
			}
			else {
				filters.add( filter );
			}
		}
		public Object build() {
			if( filters.isEmpty() ){
				throw new IllegalStateException("No filters provided for AND");
			}
			if( filters.size() == 1){
				return filters.get(0);
			}
			Filter filter = ff.and( filters );
			stack.pop();
			return filter;
		}
	}
	/**
	 * Used to create an Or filter from a list of gathered filters.
	 */
	class OrState extends BuilderState {
		List filters;
		OrState(){
			filters = new ArrayList(2);
		}
		public boolean init( UndefinedState undefined ){
			filters = undefined.filters;
			return true;
		}
		/** We cannot handle expressions - create an UndefinedState to handle it for us */
		public void add(Expression expression) {
			if( expression == null ){
				throw new NullPointerException("Expression required");
			}
			BuilderState undefined = new UndefinedState();
			undefined.add( expression );
			stack.push( undefined);
		}
		/** Accept new Filters */
		public void add(Filter filter) {
			if( filter == null ){
				throw new NullPointerException("Filter required");
			}
			if( filter instanceof Or ){
				Or or = (Or) filter;
				filters.addAll( or.getChildren() );
			}
			else {
				filters.add( filter );
			}
		}
		public Object build() {
			if( filters.isEmpty() ){
				throw new IllegalStateException("No filters provided for AND");
			}
			if( filters.size() == 1){
				return filters.get(0);
			}
			Filter filter = ff.or( filters );
			stack.pop();
			return filter;
		}
	}
	
	/**
	 * Used to create an Not filter from either a single existing filter, or the next filter.
	 */
	class NotState extends BuilderState {
		Filter filter;
		NotState(){
			filter = null;
		}
		/**
		 * Handles: builder.filter( filter ).not();
		 * @param undefined
		 * @return
		 */
		public boolean init ( UndefinedState undefined ){
			if( undefined.filters.size() == 1 ){
                 filter = (Filter) undefined.filters.get(0);				
                 return true;
			}
			else {
				return false;
			}
		}
		/** We cannot handle expressions - create an UndefinedState to handle it for us */
		public void add(Expression expression) {
			BuilderState undefined = new UndefinedState();
			undefined.add( expression );
			stack.push( undefined);
		}
		/** Accept new Filters */
		public void add(Filter filter) {
			if( filter == null ){
				throw new NullPointerException("Filter required");
			}
			if( this.filter == null ){
				this.filter = filter;
			}
			else {
				// we are already completed - lets nest
				BuilderState undefined = new UndefinedState();
				undefined.add( filter );
				stack.push( undefined );
			}
		}
		public Object build() {
			Filter value = ff.not( filter );
			stack.pop();
			return value;
		}
	}
	/**
	 * Used to collect filters or expressions while waiting for definition from user.
	 * <p>
	 * In this state the builder is unable to produce a result, it is simply colleciton values
	 * that will be used to initialize the next known state.
	 * @author Jody
	 */
	class UndefinedState extends BuilderState {
        List expressions;
        List filters;
		public String name;
		public UndefinedState() {
			expressions = new ArrayList(2);
			filters = new ArrayList(2);
		}
		public String toString() {
			StringBuffer buf = new StringBuffer("Undefined[");
			if( name != null){
				buf.append( name );
				buf.append( "|" );				
			}
			if( !expressions.isEmpty() ){
				buf.append( expressions );
			}
			if( !filters.isEmpty() ){
				buf.append( filters );
			}
			buf.append("]");
			return buf.toString();
		}
		public void setName(String name) {
			this.name = name;
		}
		public void add( Expression expression ){
			expressions.add( expression );
		}
		public void add( Filter filter){
			filters.add( filter );
		}
        public void setLeftExpression(Expression expression) {
        	setExpression( 0, expression);
        }
        public Expression getLeftExpression() {
        	if( expressions.size() >= 1){
        		return (Expression) expressions.get(0);
        	}
        	return null;
        }
        public void setExpression(int position, Expression expression) {
        	while( expressions.size() <= position ){
        		expressions.add( null );
        	}
        	expressions.set( position, expression );
        }
        public boolean isEmpty(){
        	return expressions.isEmpty() && filters.isEmpty() && name == null;
        }
        public Filter getLeftFilter() {
        	if( filters.size() >= 1){
        		return (Filter) filters.get(0);
        	}
        	return null;
        }
        public Expression getRightExpression() {
        	if( expressions.size() >= 2){
        		return (Expression) expressions.get(1);
        	}
        	return null;
        }
        public void setRightExpression(Expression expression) {
        	setExpression( 1, expression);
        }
        public Filter getRightFilter() {
        	if( filters.size() >= 2){
        		return (Filter) filters.get(1);
        	}
        	return null;
        }
        public void setRightFilter(Filter filter) {
        	if( filters.isEmpty()){
        		filters.add( null );
        	}
        	filters.set(1, filter);
        }
		public Object build() {
			if( expressions.size() == 1 ){
				Expression value = (Expression) expressions.get(0);
				stack.pop();
				return value;
			}
			else if( filters.size() == 1 ){
				Filter value = (Filter) filters.get(0);
				stack.pop();
				return value;
			}
			else {
  			    throw new IllegalStateException("Undefined state");
			}
		}		
	}
	abstract class InfixState extends BuilderState {
	     Expression left;
	     Expression right;
	     
	     public InfixState(){
	    	 left = null;
	    	 right = null;
	     }
	     public String toString() {
	    	 String name = getClass().getName();
	    	return "Infix["+left+" "+name+" "+right+"]";
	    }
	     public boolean init(UndefinedState undefined) {
	    	if( undefined.expressions.size() == 0 ){
	    		return true;
	    	}
	    	else if( undefined.expressions.size() == 1 ){
	    		left = undefined.getLeftExpression();
	    		return true;
	    	}
	    	else if( undefined.expressions.size() == 2 ){
			   left = undefined.getLeftExpression();
			   right = undefined.getRightExpression();
			   return true;
	    	}
	    	else {
				left = (Expression) undefined.expressions.remove( 0 );
				right = (Expression) undefined.expressions.remove( 0 );
				return false;
	    	}
	     }
	     public void setLeftExpression( Expression expr ){
	    	 left = expr;
	     }
	     public void setRightExpression( Expression expr ){
	    	 right = expr;
	     }
	     public void add(Expression expression) {
	    	if( left == null ){
	    		left = expression;
	    	}
	    	else if (right == null ){
	    		right = expression;	    		
	    	}
	    	else {
	    		throw new IllegalStateException("Both left and right have been defined");
	    	}
	    }
	    public void add(Filter filter) {
	    	throw new IllegalStateException("Filter not expected");
	    }
        public boolean isComplete(){
        	return left != null && right != null;
        }
        protected void assertCompleted() throws IllegalStateException {
        	if( left == null ){
				throw new IllegalStateException( "Left expression required");
			}
			if( right == null ){
				throw new IllegalStateException( "Right expression required");
			}
        }
	}
	class SubtractState extends InfixState {
		public SubtractState() {
		}
		public Object build() {
			assertCompleted();
			stack.pop();
			return ff.subtract( left, right );
		}
	}
	class AddState extends InfixState {
		public Object build() {
			assertCompleted();
			stack.pop();
			return ff.add( left, right );
		}
	}
	class MultiplyState extends InfixState {
		public Object build() {
			assertCompleted();
			stack.pop();
			return ff.multiply(left, right );
		}
	}
	class DivideState extends InfixState {
		public Object build() {
			assertCompleted();
			stack.pop();
			return ff.divide( left, right );
		}
	}

	class FunctionState extends BuilderState {
        List expressions = new ArrayList(1);
        String name;
        
        public boolean init(UndefinedState undefined) {
        	name = undefined.name;
        	expressions = undefined.expressions;
        	
        	return !undefined.expressions.isEmpty() || undefined.name != null; 
        }
        public void setName( String name ){
        	this.name = name;
        }
        public void add(Expression expression) {
        	expressions.add( expression );
        }
		public Object build() {
			if( name == null ) throw new IllegalStateException("Name required for function");
			Function function = ff.function(name, (Expression[]) expressions
					.toArray(new Expression[expressions.size()]));
			
			stack.pop();			
			return function;
		}		
	}
}
