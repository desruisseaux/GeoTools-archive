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

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterVisitor;

/**
 * Usefull super class that casts a Filter.contains( feature )
 * into a boolean Expression.
 * <p>
 * This allows you to implement expression( schema ) for Filter opperations
 * without implementing the same boiler plate code for expression( schema )
 * all the time.
 * </p> 
 */
public abstract class AbstractFilterExpr extends AbstractExpr {
	public abstract Filter filter( FeatureType schema ) throws IOException;	
	public Expression expression(final FeatureType schema) {	
		/** Boolean expression based on Filter.contains */
		return new Expression(){
			/**
			 * Consider this a Function that
			 * returns filter.contains( feature )
			 */
			public short getType() {
				return Expression.FUNCTION;
			}
			public Object getValue(Feature feature) {
				boolean contains;
				try {
					Filter filter = filter( feature.getFeatureType() );
					contains = filter.contains( feature );
				}
				catch( IOException ignore ){
					contains = false;
				}
				return contains ? Boolean.TRUE : Boolean.FALSE;
			}
			public void accept(FilterVisitor visitor) {
				try {
					filter( schema ).accept( visitor );
				} catch (IOException e) {
				}				
			}					
		};
	}
}