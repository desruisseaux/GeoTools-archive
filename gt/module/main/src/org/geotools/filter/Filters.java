package org.geotools.filter;

import org.geotools.filter.visitor.DuplicatorFilterVisitor;

/**
 * Utility class for working with Filters & Expression.
 * <p>
 * Note additional utility methods are available if you create
 * an instanceof this Object w/ a FilterFactory.
 * </p>
 * <p>
 * Example use:
 * <pre><code>
 * Filters filters = new Filters( factory );
 * filters.duplicate( origional );
 * </code></pre>
 * The above example creates a copy of the provided Filter,
 * the factory provided will be used when creating the duplicated
 * content.
 * </p>
 * @author Jody Garnett, Refractions Research
 *
 */
public class Filters {
	/** <code>NOTFOUND</code> indicates int value was unavailable */
	public static final int NOTFOUND = -1;
	
	FilterFactory ff;
	
	public Filters(){
		this( FilterFactoryFinder.createFilterFactory() );
	}	
	public Filters( FilterFactory factory ){
		ff = factory;
	}
	public void setFilterFactory( FilterFactory factory ){
		ff = factory;
	}
    /**
     * Deep copy the filter.
     * <p>
     * Filter objects are mutable, when copying a rich
     * data structure (like SLD) you will need to duplicate
     * the Filters referenced therein.
     * </p>
     */
    public Filter duplicate( Filter filter ){
    	DuplicatorFilterVisitor xerox = new DuplicatorFilterVisitor( ff );
    	filter.accept( xerox );
    	return (Filter) xerox.getCopy();
    	
    }
    
    /**
     * Uses number( expr ), will turn result into an interger, or NOTFOUND
     *
     * @param expr
     *
     * @return int value of first Number, or NOTFOUND
     */
    public static int intValue(Expression expr) {
        Number number = (Number) value(expr, Number.class);
        
        if (number != null) {
            return number.intValue();
        }

        //look for a string
        String string = (String) value(expr,String.class);
        if (string != null) {
        	//try parsing into a integer
        	try {
        		return Integer.parseInt(string);
        	}
        	catch(NumberFormatException e) {}
        }
        
        //no dice
        return NOTFOUND;
    }

    /**
     * Uses string( expr ), will turn result into a String
     *
     * @param expr
     *
     * @return value of first String
     */
    public static String stringValue(Expression expr) {
        String string = (String) value(expr, String.class);

        return string;
    }

    /**
     * Uses number( expr ), will turn result into an interger, or NaN.
     *
     * @param expr
     *
     * @return int value of first Number, or Double.NaN
     */
    public static double doubleValue(Expression expr) {
        Number number = (Number) value(expr, Number.class);

        if (number != null) {
            return number.doubleValue();
        }
        
        //try for a string
        String string = (String) value(expr,String.class);
        if (string != null) {
        	//try parsing into a double
        	try {
        		return Double.parseDouble(string);
        	}
        	catch(NumberFormatException e) {}
        }

        //too bad
        return Double.NaN;
    }

    /**
     * Navigate through the expression finding the first mentioned Integer.
     * 
     * <p>
     * Does not evaulate math expression (yet).
     * </p>
     *
     * @param expr
     *
     * @return Number or null
     */
    public static Number number(Expression expr) {
        return (Number) value(expr, Number.class);
    }
    
    /**
     * Navigate through the expression seaching for TYPE.
     * 
     * <p>
     * This will work even with dynamic expression that would normall require a
     * feature. It works especially well when the Expression is a Literal
     * literal (which is usually the case).
     * </p>
     * 
     * <p>
     * If you have a specific Feature, please do this:
     * <pre><code>
     * Object value = expr.getValue( feature );
     * return value instanceof Color ? (Color) value : null;
     * </code></pre>
     * </p>
     *
     * @param expr
     * @param TYPE DOCUMENT ME!
     *
     * @return First available color, or null.
     */
    public static Object value(Expression expr, Class TYPE) {
        if (expr == null) {
            return null;
        } else if (expr instanceof LiteralExpression) {
            LiteralExpression literal = (LiteralExpression) expr;
            Object value = literal.getLiteral();

            if (TYPE.isInstance(value)) {
                return value;
            }
        } else if (expr instanceof FunctionExpression) {
            FunctionExpression function = (FunctionExpression) expr;

            if (function.getArgCount() != 0) {
                for (int i = 0; i < function.getArgCount(); i++) {
                    Expression e = function.getArgs()[i];
                    Object value = value(e, TYPE);

                    if (value != null) {
                        return value;
                    }
                }
            }
        } else {
            try { // this is a bad idea, not expected to work much

                Object value = expr.getValue(null);

                if (TYPE.isInstance(value)) {
                    return value;
                }
            } catch (NullPointerException expected) {
                return null; // well that was not unexpected
            } catch (Throwable ignore) { // I did say that was a bad idea                
            }
        }

        return null; // really need a Feature to acomplish this one
    }
}

