package org.geotools.feature;

import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geotools.filter.Filter;
import org.opengis.feature.type.AttributeType;

/**
 * Helper methods for dealing with Type restrictions represented by Filters
 * 
 * @author Gabriel Roldan, Axios Engineering
 */
public class Restrictions {

	/**
	 * Creates a Filter that enforces the length of value to be equal to the
	 * declared length.
	 * <p>
	 * <code>length</code> is the number of units of length, where units of
	 * length varies depending on the type that is being ?derived? from (#of
	 * chars for a string type, #of octets for a binary type, etc)
	 * </p>
	 * @param attributeName
	 * @param binding
	 * @param length
	 * @return
	 */
	public static Filter createLength(QName attributeName, Class binding,
			int length) {
		throw new UnsupportedOperationException("Not yet implemented");
	}


	/**
	 * Creates a Filter that 
	 */
	public static Filter createMinLength(QName attributeName, Class binding,
			int length) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/**
	 * Creates a Filter that 
	 */
	public static Filter createMaxLength(QName attributeName, Class binding,
			int length) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	/**
	 * Creates a Filter that 
	 */
	public static Filter createPattern(QName attributeName, Class binding,
			String regExp) {
		throw new UnsupportedOperationException("Not yet implemented");
	}


	/**
	 * Creates a Filter that 
	 */
	public static Filter createEnumeration(QName attributeName, Class binding,
			Set values) {
		throw new UnsupportedOperationException("Not yet implemented");
	}


	/**
	 * Creates a Filter that
	 * @parameter constraintName one of <code>"preserve"</code>, <code>"replace"</code>, 
	 * <code>"collapse"</code> 
	 */
	public static Filter createWhiteSpace(QName attributeName, Class binding,
			String constraintName) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	/**
	 * Creates a Filter that 
	 */
	public static Filter createMaxInclusive(QName attributeName, Class binding,
			Object maxValue) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	/**
	 * Creates a Filter that 
	 */
	public static Filter createMaxExclusive(QName attributeName, Class binding,
			Object maxValue) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	/**
	 * Creates a Filter that 
	 */
	public static Filter createMinInclusive(QName attributeName, Class binding,
			Object minValue) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	/**
	 * Creates a Filter that 
	 */
	public static Filter createMinExclusive(QName attributeName, Class binding,
			Object minValue) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	/**
	 * Creates a Filter that 
	 */
	public static Filter createTotalDigits(QName attributeName, Class binding,
			int totalDigits) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	/**
	 * Creates a Filter that 
	 */
	public static Filter createFractionDigits(QName attributeName, Class binding,
			int fractionDigits) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

}
