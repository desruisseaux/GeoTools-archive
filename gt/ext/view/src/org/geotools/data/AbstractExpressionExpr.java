 /*
  *    Geotools2 - OpenSource mapping toolkit
  *    http://geotools.org
  *    (C) 2002, Geotools Project Managment Committee (PMC)
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
  *
  */
package org.geotools.data;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterVisitor;
import org.geotools.filter.IllegalFilterException;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Useful super class that casts expressions into a Filter.
 * <p>
 * To turn Expression into a Filter I am going to follow the usual
 * Perl defaults of "true".
 * <ul>
 * <li>Boolean.TRUE is true
 * <li>Non empty String is true
 * <li>Non zero is true
 * <li>Non zero length array is true
 * <li>Non isEmpty Collection/Map is true
 * <li>Non isNull Envelope is true
 * </ul>
 * Where true means that the generated Filter will accept the results.
 * </p>
 */
public abstract class AbstractExpressionExpr extends AbstractExpr {
	public abstract Expression expression( FeatureType schema ) throws IOException;
	public Filter filter(final FeatureType schema){
		return new Filter(){
			public boolean contains(Feature feature) {
				Expression expression;
				try {
					expression = expression( schema );
				} catch (IOException e) {
					return false;
				}
				Object value = expression.getValue( feature );
				if( value == null ) return false;
				if( value instanceof Boolean) {
					return ((Boolean)value).booleanValue();
				}
				if( value instanceof Number ){
					return ((Number)value).doubleValue() != 0.0;
				}
				if( value instanceof String ){
					return ((String)value).length() != 0; 
				}
				if( value.getClass().isArray() ){
					return Array.getLength( value ) != 0;
				}
				if( value instanceof Collection ){
					return !((Collection)value).isEmpty();
				}
				if( value instanceof Map ){
					return !((Map)value).isEmpty();
				}
				if( value instanceof Envelope ){
					return ((Envelope)value).isNull();						
				}
				return false;
			}
			public Filter and(Filter filter) {
				try {
					return factory.createLogicFilter( this, filter, Filter.LOGIC_AND );
				} catch (IllegalFilterException e) {
					return null;
				}				
			}

			public Filter or(Filter filter) {
				try {
					return factory.createLogicFilter( this, filter, Filter.LOGIC_OR );
				} catch (IllegalFilterException e) {
					return null;
				}
			}

			public Filter not() {
				try {
					return factory.createLogicFilter( this, Filter.LOGIC_NOT );
				} catch (IllegalFilterException e) {
					return null;
				}
			}

			public short getFilterType() {
				return 0; // TODO: What is the value for "custom"?
			}
			public void accept(FilterVisitor visitor) {
				try {
					expression( schema ).accept( visitor );
				} catch (IOException e) {					
				}				
			}			
		};
	}
}