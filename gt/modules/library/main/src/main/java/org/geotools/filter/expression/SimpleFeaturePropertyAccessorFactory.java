package org.geotools.filter.expression;

import org.geotools.factory.Hints;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Creates a property accessor for simple features.
 * <p>
 * The created accessor handles a small subset of xpath expressions, a
 * non-nested "name" which corresponds to a feature attribute, and "@id",
 * corresponding to the feature id.
 * </p>
 * <p>
 * THe property accessor may be run against {@link org.geotools.feature.Feature}, or 
 * against {@link org.geotools.feature.FeatureType}. In the former case the feature property 
 * value is returned, in the latter the feature property type is returned. 
 * </p>
 * 
 * @author Justin Deoliveira, The Open Planning Project
 * 
 */
public class SimpleFeaturePropertyAccessorFactory implements
        PropertyAccessorFactory {

    /** Single instnace is fine - we are not stateful */
    static PropertyAccessor ATTRIBUTE_ACCESS = new SimpleFeaturePropertyAccessor();
    static PropertyAccessor DEFAULT_GEOMETRY_ACCESS = new DefaultGeometrySimpleFeaturePropertyAccessor();
    static PropertyAccessor FID_ACCESS = new FidSimpleFeaturePropertyAccessor();

    public PropertyAccessor createPropertyAccessor(Class type, String xpath,
            Class target, Hints hints) {

    	if ( xpath == null ) 
    		return null;
    	
        if (!Feature.class.isAssignableFrom(type) && !FeatureType.class.isAssignableFrom(type))
            return null; // we only work with simple feature

        if ("".equals(xpath) && target == Geometry.class)
            return DEFAULT_GEOMETRY_ACCESS;

        //check for fid access
        if (xpath.matches("@(\\w+:)?id"))
            return FID_ACCESS;

        //check for simple property acess
        if (xpath.matches("(\\w+:)?(\\w+)") ) {
        	return ATTRIBUTE_ACCESS;	
        }
        
        return null;
    }

    /**
     * We strip off namespace prefix, we need new feature model to do this
     * property
     * <ul>
     * <li>BEFORE: foo:bar
     * <li>AFTER: bar
     * </ul>
     * 
     * @param xpath
     * @return xpath with any XML prefixes removed
     */
    static String stripPrefix(String xpath) {
        int split = xpath.indexOf(":");
        if (split != -1) {
            return xpath.substring(split + 1);
        }
        return xpath;
    }

    /**
     * Access to Feature Identifier.
     * 
     * @author Jody Garnett, Refractions Research Inc.
     */
    static class FidSimpleFeaturePropertyAccessor implements PropertyAccessor {        
        public boolean canHandle(Object object, String xpath, Class target) {
        	//we only work against feature, not feature type
            return object instanceof Feature && xpath.matches("@(\\w+:)?id");
        }
        public Object get(Object object, String xpath, Class target) {
            Feature feature = (Feature) object;
            return feature.getID();
        }

        public void set(Object object, String xpath, Object value, Class target)
                throws IllegalAttributeException {
            throw new IllegalAttributeException("feature id is immutable");            
        }
    }
    static class DefaultGeometrySimpleFeaturePropertyAccessor implements PropertyAccessor {
        
        public boolean canHandle(Object object, String xpath, Class target) {
        	if ( !"".equals( xpath ) )
        		return false;
        	
        	if ( target != Geometry.class ) 
        		return false;
        	
        	if ( !( object instanceof Feature || object instanceof FeatureType ) ) {
        		return false;
        	}
        	
        	return true;
            
        }
        public Object get(Object object, String xpath, Class target) {
        	if ( object instanceof Feature ) {
        		return ((Feature) object).getDefaultGeometry();
        	}
        	if ( object instanceof FeatureType ) {
        		return ((FeatureType)object).getDefaultGeometry();
        	}
            
        	return null;
        }

        public void set(Object object, String xpath, Object value, Class target)
                throws IllegalAttributeException {
            
        	if ( object instanceof Feature ) {
        		((Feature) object).setDefaultGeometry( (Geometry) value );
        	}
        	if ( object instanceof FeatureType ) {
        		throw new IllegalAttributeException("feature type is immutable");
        	}
        	
        }
    }

    static class SimpleFeaturePropertyAccessor implements PropertyAccessor {
        public boolean canHandle(Object object, String xpath, Class target) {
        	xpath = stripPrefix(xpath);
        	
        	if ( object instanceof Feature ) {
        		return ((Feature) object).getAttribute( xpath ) != null;
        	}
        	
        	if ( object instanceof FeatureType ) {
        		return ((FeatureType) object).getAttributeType( xpath ) != null;
        	}
        	
        	return false;
        }
        
        public Object get(Object object, String xpath, Class target) {
        	xpath = stripPrefix(xpath);
        	
        	if ( object instanceof Feature ) {
        		return ((Feature) object).getAttribute( xpath );
        	}
        	
        	if ( object instanceof FeatureType ) {
        		return ((FeatureType) object).getAttributeType( xpath );
        	}
        	
        	return null;
        }

        public void set(Object object, String xpath, Object value, Class target)
                throws IllegalAttributeException {
        	xpath = stripPrefix(xpath);
        	
        	if ( object instanceof Feature ) {
        		((Feature) object).setAttribute( xpath, value );
        	}
        	
        	if ( object instanceof FeatureType ) {
        		throw new IllegalAttributeException("feature type is immutable");    
        	}
        	
        }
    }

}
