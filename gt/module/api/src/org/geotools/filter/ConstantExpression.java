package org.geotools.filter;

import java.awt.Color;
import java.math.BigDecimal;
import java.util.Date;

import org.geotools.feature.Feature;

import com.vividsolutions.jts.geom.Geometry;

/**
 * The Expression class is not immutable!
 * <p>
 * However we do have a need for immutable literal expressions when
 * defining our API for SLD, and any other standards based on 
 * Expression.
 * </p>
 * 
 * @author Jody Garnett, Refractions Research
 */
public class ConstantExpression implements LiteralExpression, Cloneable {
	
	public static final ConstantExpression NULL = constant( null );		
	public static final ConstantExpression BLACK = color( Color.BLACK );	
	public static final ConstantExpression ZERO = constant( 0 );	
	public static final ConstantExpression ONE = constant( 1 );
	public static final ConstantExpression TWO = constant( 2 );
	public static final ConstantExpression UNNAMED = constant( "" );
	
	final short type;
	final Object value;
	private ConstantExpression( Object value ){
		this( type( value ), value );
	}
	private ConstantExpression( short type, Object value ){
		this.type = type;
		this.value = value;
	}
	public void setLiteral(Object literal) throws IllegalFilterException {
		throw new UnsupportedOperationException("Default value is immutable");
	}
	public Object getValue(Feature feature) {
		return value;
	}
	public short getType() {
		return LITERAL_INTEGER;
	}
	public Object getLiteral() {
		return value;
	}
	public void accept(FilterVisitor visitor) {
		visitor.visit( this );				
	}	
	protected Object clone() throws CloneNotSupportedException {
		return this; // we are immutable!
	}
	public boolean equals(Object obj) {
		if(!(obj instanceof LiteralExpression)) return false;		
		LiteralExpression other = (LiteralExpression) obj;
		Object otherLiteral = other.getLiteral();
		if( value == null ) return otherLiteral == null;
		
		if( value.getClass().isAssignableFrom( otherLiteral.getClass() ) ){
			return value.equals( other.getLiteral() );	
		}
		if( value instanceof Number ){
			if( otherLiteral instanceof Number ){
				Number myNumber = (Number) value;
				Number otherNumber = (Number) otherLiteral;
				
				return myNumber.doubleValue() == otherNumber.doubleValue();
			}
		}
		// Okay we are into String Compare land!
		String myString = value.toString();
		String otherString = otherLiteral.toString();
		return myString.equals( otherString );		
	}
	
	public int hashCode() {
		if( value instanceof Geometry ||
			value instanceof Date ){
			// forms of complex content ...
			return value.hashCode();
		}
		return value == null ? 0 : value.toString().hashCode();
	}
	
	public String toString() {
		if( value instanceof Color ){
			Color color = (Color) value;
			String redCode = Integer.toHexString(color.getRed());
	        String greenCode = Integer.toHexString(color.getGreen());
	        String blueCode = Integer.toHexString(color.getBlue());

	        if (redCode.length() == 1) redCode = "0" + redCode;
	        if (greenCode.length() == 1) greenCode = "0" + greenCode;
	        if (blueCode.length() == 1) blueCode = "0" + blueCode;
		}
		return value == null ? "NULL" : value.toString();
	}
	
	/** Encode provided color as a String */
	public static ConstantExpression color( Color color ){
		if( color == null ) return NULL;		

        String redCode = Integer.toHexString(color.getRed());
        String greenCode = Integer.toHexString(color.getGreen());
        String blueCode = Integer.toHexString(color.getBlue());

        if (redCode.length() == 1) redCode = "0" + redCode;
        if (greenCode.length() == 1) greenCode = "0" + greenCode;
        if (blueCode.length() == 1) blueCode = "0" + blueCode;
        
        String colorCode = "#" + redCode + greenCode + blueCode;
        return new ConstantExpression( colorCode );
	}
	public static ConstantExpression constant( double number){				
		return new ConstantExpression( new Double( number ) );
	}	
	public static ConstantExpression constant( int number){				
		return new ConstantExpression( new Integer( number ) );
	}	
	public static ConstantExpression constant( Object value ){
		return new ConstantExpression( value );
	}
	
	static short type( Object value ){
		if( value instanceof Number){
			if( value instanceof Double ){
				return Expression.LITERAL_DOUBLE;
			}
			else if( value instanceof BigDecimal ){
				return Expression.LITERAL_DOUBLE;
			}
			else {
				return Expression.LITERAL_INTEGER;
			}
		}
		else if( value instanceof Geometry  ){
			return Expression.LITERAL_GEOMETRY;
		}
		
		return Expression.LITERAL_STRING;
		
	}
}
