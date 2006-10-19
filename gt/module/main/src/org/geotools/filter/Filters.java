/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.filter;

import java.awt.Color;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;


import org.geotools.filter.visitor.DuplicatorFilterVisitor;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;

/**
 * Utility class for working with Filters & Expression.
 * <p>
 * To get the full benifit you will need to create an instanceof
 * this Object (supports your own custom FilterFactory!). Additional
 * methods to help create expressions are available.
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
 * <h3>Expression</h3>
 * <p>
 * Expressions form an interesting little semi scripting languge,
 * intended for queries.  A interesting Feature of Filter as a language
 * is that it is not strongly typed. This utility class many helper
 * methods that ease the transition from Strongly typed Java to the more
 * relaxed setting of Expression where most everything can be a string.
 * </p>
 * <pre><code>
 * double sum = Filters.number( Object ) + Filters.number( Object );
 * </code></pre>
 * The above example will support the conversion of many things into a format
 * suitable for addition - the complete list is something like:
 * <ul>
 * <li>Any instance of Number
 * <li>"1234" - aka Integer
 * <li>"#FFF" - aka Integer 
 * <li>"123.0" - aka Double
 * </ul>
 * A few things (like Geometry and "ABC") will not be considered addative.
 * </p>
 * In general the scope of these functions should be similar to that
 * allowed by the XML Atomic Types, aka those that can be seperated by
 * whitespace to form a list.
 * </p>
 * <p>
 * We do our best to be forgiving, any Java class which takes a String as
 * a constructor can be tried, and toString() assumed to be the inverse. This
 * lets many things (like URL and Date) function without modification.
 * </p>
 * 
 * @author Jody Garnett, Refractions Research
 * @since GeoTools 2.2.M3
 * @source $URL$
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
    public static int asInt( Expression expr ) {
        Number number = (Number) asType(expr, Number.class);
 	
        if (number != null) {
            return number.intValue();
        }

        //look for a string
        String string = (String) asType(expr,String.class);
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
    public static String asString(Expression expr) {
        String string = (String) asType(expr, String.class);

        return string;
    }

    /**
     * Uses number( expr ), will turn result into an interger, or NaN.
     *
     * @param expr
     *
     * @return int value of first Number, or Double.NaN
     */
    public static double asDouble(Expression expr) {
        Number number = (Number) asType(expr, Number.class);

        if (number != null) {
            return number.doubleValue();
        }
        
        //try for a string
        String string = (String) asType(expr,String.class);
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
    public static Object asType(Expression expr, Class TYPE) {
        if (expr == null) {
            return null;
        }
        else if (expr instanceof Literal) {
        		Literal literal = (Literal) expr;
            Object value = literal.getValue();

            if (TYPE.isInstance(value)) {
                return value;
            }
        }
        else if (expr instanceof Function) {
        		Function function = (Function) expr;
        		List params = function.getParameters();
            if ( params != null && params.size() != 0 ) {
                for (int i = 0; i < params.size(); i++) {
                    Expression e = (Expression) params.get(i);
                    Object value = asType(e, TYPE);

                    if (value != null) {
                        return value;
                    }
                }
            }
        }
        else {
            try { // this is a bad idea, not expected to work much
                Object value = expr.evaluate(null);

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
    
    /**
     * Treat provided value as a Number, used for math opperations.
     * <p>
     * This function allows for the non stongly typed Math Opperations
     * favoured by the Expression standard.
     * </p>
     * <p>
     * Able to hanle:
     * <ul>
     * <li>null - to NaN
     * <li>Number
     * <li>String - valid Integer and Double encodings
     * </ul>
     * 
     * </p>
     * @param value
     * @return double or Double.NaN;
     * @throws IllegalArgumentException For non numerical among us -- like Geometry 
     */
    public static double number(Object value) {
    	if( value == null ) return Double.NaN;
    	if( value instanceof Number ){
    		Number number = (Number) value;
    		return number.doubleValue();
    	}
    	if( value instanceof String ){
    		String text = (String) value;
    		try {
				Number number = (Number) gets( text, Number.class );
				return number.doubleValue();
			} catch (Throwable e) {
				throw new IllegalArgumentException("Unable to decode '"+text+"' as a number" );				
			}    		
    	}
    	if( value instanceof Expression ){
    		throw new IllegalArgumentException("Cannot deal with un evaulated Expression");
    	}
    	throw new IllegalArgumentException("Unable to evaulate "+value.getClass()+" in a numeric context");
    }
    
    /**
     * Used to upcovnert a "Text Value" into the provided TYPE.
     * <p>
     * Used to tread softly on the Java typing system, because
     * Filter/Expression is not strongly typed. Values in in
     * Expression land are often not the the real Java Objects
     * we wish they were - it is reall a small, lax, query
     * language and Java objects need a but of help getting
     * through.
     * <p>
     * </p>
     * A couple notes:
     * <ul>
     * <li>Usual trick of reflection for a Constructors that
     *     supports a String parameter is used as a last ditch effort.
     *     </li>
     * <li>will do its best to turn Object into the indicated Class
     * <li>will be used for ordering literals against attribute values
     *     are calculated at runtime (like Date.)
     * </ul>
     * Remember Strong typing is for whimps who know what they are
     * doing ahead of time. Real programmers let their program
     * learn at runtime... :-)
     * </p>
     * 
     * @param text
     * @param TYPE
     * @throws open set of Throwable reflection for TYPE( String ) 
     */
    public static Object gets( String text, Class TYPE ) throws Throwable {
    	if( text == null ) return null;
    	if( TYPE == String.class ) return text;
    	if( TYPE == Integer.class ) {
    		return Integer.decode( text );    		
    	}
    	if( TYPE == Double.class ){
    		return Double.valueOf( text );
    	}
    	if( TYPE == Number.class ){
    		try {
    			return Double.valueOf( text );
    		}
    		catch( NumberFormatException ignore ){
    		}
    		return Integer.decode( text );    		
    	}
    	if( TYPE == Color.class ){
    		return new Color( Integer.decode( text ).intValue() );
    	}    	
    	try {
			Constructor create = TYPE.getConstructor( new Class[]{String.class});
			return create.newInstance( new Object[]{ text } );
		} catch (SecurityException e) {
			// hates you
		} catch (NoSuchMethodException e) {
			// nope
		} catch (IllegalArgumentException e) {
			// should not occur
		} catch (InstantiationException e) {
			// should not occur, perhaps the class was abstract?
			// eg. Number.class is a bad idea
		} catch (IllegalAccessException e) {
			// hates you
		} catch (InvocationTargetException e) {
			// should of worked but we got a real problem,
			// an actual problem
			throw e.getCause();
		}    	
    	return null;
    }
    
    public static String puts( double number ){
    	if( Math.rint(number) == number ){
    		return Integer.toString( (int) number );
    	}
    	return Double.toString( number );    	
    }
    /**
     * Inverse of eval, used to softly type supported
     * types into Text for use as literals.
     */
    public static String puts( Object obj ){
    	if( obj == null ) return null;
    	if( obj instanceof String) return (String) obj;    	
    	if( obj instanceof Color ){
    		Color color = (Color) obj;
    		return puts( color );
    	}
    	if( obj instanceof Number ){
    		Number number = (Number) obj;
    		return puts( number.doubleValue() );    		
    	}
    	return obj.toString();
    }
    
    public static String puts( Color color ){
    	String redCode = Integer.toHexString(color.getRed());
        String greenCode = Integer.toHexString(color.getGreen());
        String blueCode = Integer.toHexString(color.getBlue());

        if (redCode.length() == 1) redCode = "0" + redCode;
        if (greenCode.length() == 1) greenCode = "0" + greenCode;
        if (blueCode.length() == 1) blueCode = "0" + blueCode;
        
        return "#" + redCode + greenCode + blueCode;  
    }
}

