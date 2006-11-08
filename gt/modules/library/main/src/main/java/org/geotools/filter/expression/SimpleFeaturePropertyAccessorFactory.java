package org.geotools.filter.expression;

import org.geotools.factory.Hints;
import org.geotools.feature.Feature;
import org.geotools.feature.IllegalAttributeException;

/**
 * Creates a property accessor for simple features.
 * <p>
 * The created accessor handles a small subset of xpath expressions, a non-nested 
 * "name" which corresponds to a feature attribute, and "@id", corresponding to 
 * the feature id.
 * </p>
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class SimpleFeaturePropertyAccessorFactory implements
		PropertyAccessorFactory {

	public PropertyAccessor createPropertyAccessor(Class type, String xpath, Hints hints) {
		
		if ( Feature.class.isAssignableFrom( type ) ) {
			return new SimpleFeaturePropertyAccessor();
		}
		
		return null;
	}

	static class SimpleFeaturePropertyAccessor implements PropertyAccessor {

		public boolean canHandle(Object object, String xpath) {
			
			Feature feature = (Feature) object;
			
			//1. check for @[<prefix>:]id
			if ( xpath.matches( "@(\\w+:)?id" ) ) 
				return true;
			
			//2. check if the xpath matches an attribute in the feature type
			xpath = simple( xpath );
			if ( feature.getFeatureType().getAttributeType( xpath ) != null ) 
				return true;
			
			return false;
		}
		
		public Object get(Object object, String xpath) {
			
			Feature feature = (Feature) object;
		
			if ( xpath.matches( "@(\\w+:)?id" ) ) {
				return feature.getID();
			}
			
			xpath = simple( xpath );
			if ( feature.getFeatureType().getAttributeType( xpath ) != null ) {
				return feature.getAttribute( xpath );
			}
			
			throw new IllegalArgumentException( "Could not handle expression:" + xpath );
		}

		public void set(Object object, String xpath, Object value) 
			throws IllegalAttributeException {
			
			Feature feature = (Feature) object;
			
			if ( xpath.matches( "@(\\w+:)?id" ) ) {
				throw new IllegalAttributeException( "feature id is immutable" );
			}
			
			xpath = simple( xpath );
			if ( feature.getFeatureType().getAttributeType( xpath ) != null ) {
				feature.setAttribute( xpath, value );
				return;
			}
			
			throw new IllegalArgumentException( "Could not handle expression:" + xpath );
		}
		
		
		protected String simple( String xpath ) {
			if ( xpath.indexOf(":") != -1 ) {
				//JD: we strip off namespace prefix, we need new feature model 
				// to do this property
				xpath = xpath.substring(xpath.indexOf(":") + 1);
	        }
			
			return xpath;
		}
	}
}
