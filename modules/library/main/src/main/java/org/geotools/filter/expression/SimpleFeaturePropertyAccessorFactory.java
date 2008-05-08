package org.geotools.filter.expression;

import java.util.regex.Pattern;

import org.geotools.factory.Hints;
import org.geotools.feature.IllegalAttributeException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Creates a property accessor for simple features.
 * <p>
 * The created accessor handles a small subset of xpath expressions, a
 * non-nested "name" which corresponds to a feature attribute, and "@id",
 * corresponding to the feature id.
 * </p>
 * <p>
 * THe property accessor may be run against {@link SimpleFeature}, or 
 * against {@link SimpleFeature}. In the former case the feature property 
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
    static Pattern idPattern = Pattern.compile("@(\\w+:)?id");
    static Pattern propertyPattern = Pattern.compile("(\\w+:)?(\\w+)");

    public PropertyAccessor createPropertyAccessor(Class type, String xpath,
            Class target, Hints hints) {

    	if ( xpath == null ) 
    		return null;
    	
        if (!SimpleFeature.class.isAssignableFrom(type) && !SimpleFeatureType.class.isAssignableFrom(type))
            return null; // we only work with simple feature

        //if ("".equals(xpath) && target == Geometry.class)
        if ("".equals(xpath))
            return DEFAULT_GEOMETRY_ACCESS;

        //check for fid access
        if (idPattern.matcher(xpath).matches())
            return FID_ACCESS;

        //check for simple property acess
        if (propertyPattern.matcher(xpath).matches()) {
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
     * Access to SimpleFeature Identifier.
     * 
     * @author Jody Garnett, Refractions Research Inc.
     */
    static class FidSimpleFeaturePropertyAccessor implements PropertyAccessor {        
        public boolean canHandle(Object object, String xpath, Class target) {
        	//we only work against feature, not feature type
            return object instanceof SimpleFeature && xpath.matches("@(\\w+:)?id");
        }
        public Object get(Object object, String xpath, Class target) {
            SimpleFeature feature = (SimpleFeature) object;
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
        	
//        	if ( target != Geometry.class ) 
//        		return false;
        	
        	if ( !( object instanceof SimpleFeature || object instanceof SimpleFeatureType ) ) {
        		return false;
        	}
        	
        	return true;
            
        }
        public Object get(Object object, String xpath, Class target) {
        	if ( object instanceof SimpleFeature ) {
        		return ((SimpleFeature) object).getDefaultGeometry();
        	}
        	if ( object instanceof SimpleFeatureType ) {
        		return ((SimpleFeatureType)object).getDefaultGeometry();
        	}
            
        	return null;
        }

        public void set(Object object, String xpath, Object value, Class target)
                throws IllegalAttributeException {
            
        	if ( object instanceof SimpleFeature ) {
        		((SimpleFeature) object).setDefaultGeometry( (Geometry) value );
        	}
        	if ( object instanceof SimpleFeatureType ) {
        		throw new IllegalAttributeException("feature type is immutable");
        	}
        	
        }
    }

    static class SimpleFeaturePropertyAccessor implements PropertyAccessor {
        public boolean canHandle(Object object, String xpath, Class target) {
        	xpath = stripPrefix(xpath);
        	
        	if ( object instanceof SimpleFeature ) {
        		return ((SimpleFeature) object).getAttribute( xpath ) != null;
        	}
        	
        	if ( object instanceof SimpleFeatureType ) {
        		return ((SimpleFeatureType) object).getAttribute( xpath ) != null;
        	}
        	
        	return false;
        }
        
        public Object get(Object object, String xpath, Class target) {
        	xpath = stripPrefix(xpath);
        	
        	if ( object instanceof SimpleFeature ) {
        		return ((SimpleFeature) object).getAttribute( xpath );
        	}
        	
        	if ( object instanceof SimpleFeatureType ) {
        		return ((SimpleFeatureType) object).getAttribute( xpath );
        	}
        	
        	return null;
        }

        public void set(Object object, String xpath, Object value, Class target)
                throws IllegalAttributeException {
        	xpath = stripPrefix(xpath);
        	
        	if ( object instanceof SimpleFeature ) {
        		((SimpleFeature) object).setAttribute( xpath, value );
        	}
        	
        	if ( object instanceof SimpleFeatureType ) {
        		throw new IllegalAttributeException("feature type is immutable");    
        	}
        	
        }
    }

}
