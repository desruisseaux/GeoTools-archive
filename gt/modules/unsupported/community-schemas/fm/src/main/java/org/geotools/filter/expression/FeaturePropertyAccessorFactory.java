package org.geotools.filter.expression;

import java.util.List;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.geotools.factory.Hints;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.iso.ComplexAttributeImpl;
import org.geotools.feature.iso.FeatureImpl;
import org.geotools.feature.iso.simple.SimpleFeatureImpl;
import org.geotools.feature.iso.type.AttributeDescriptorImpl;
import org.geotools.feature.iso.type.FeatureTypeImpl;
import org.geotools.feature.iso.xpath.AttributeDescriptorPropertyHandler;
import org.geotools.feature.iso.xpath.AttributePropertyHandler;
import org.opengis.feature.Attribute;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyType;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Creates a property accessor for ISO Features.
 * <p>
 * The created accessor handles a small subset of xpath expressions, a
 * non-nested "name" which corresponds to a feature attribute, and "@id",
 * corresponding to the feature id.
 * </p>
 * <p>
 * THe property accessor may be run against {@link org.geotools.feature.Feature},
 * or against {@link org.geotools.feature.FeatureType}. In the former case the
 * feature property value is returned, in the latter the feature property type
 * is returned.
 * </p>
 * 
 * @author Justin Deoliveira, The Open Planning Project
 * 
 */
public class FeaturePropertyAccessorFactory implements PropertyAccessorFactory {

    /** Single instnace is fine - we are not stateful */
    static PropertyAccessor ATTRIBUTE_ACCESS = new FeaturePropertyAccessor();

    static PropertyAccessor DEFAULT_GEOMETRY_ACCESS = new DefaultGeometryFeaturePropertyAccessor();

    static PropertyAccessor FID_ACCESS = new FidFeaturePropertyAccessor();

    public PropertyAccessor createPropertyAccessor(Class type, String xpath,
            Class target, Hints hints) {

        if (xpath == null)
            return null;

        if (!Feature.class.isAssignableFrom(type)
                && !FeatureType.class.isAssignableFrom(type)
                && !AttributeDescriptor.class.isAssignableFrom(type))
            return null; // we only work with simple feature

        if ("".equals(xpath) && target == Geometry.class)
            return DEFAULT_GEOMETRY_ACCESS;

        // check for fid access
        if (xpath.matches("@(\\w+:)?id"))
            return FID_ACCESS;

        // check for simple property acess
        // if (xpath.matches("(\\w+:)?(\\w+)")) {
        return ATTRIBUTE_ACCESS;
        // }

        // return null;
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
    static class FidFeaturePropertyAccessor implements PropertyAccessor {

        public boolean canHandle(Object object, String xpath, Class target) {
            // we only work against feature, not feature type
            return object instanceof Attribute && xpath.matches("@(\\w+:)?id");
        }

        public Object get(Object object, String xpath, Class target) {
            Attribute feature = (Attribute) object;
            return feature.getID();
        }

        public void set(Object object, String xpath, Object value, Class target)
                throws IllegalAttributeException {
            throw new IllegalAttributeException("feature id is immutable");
        }
    }

    static class DefaultGeometryFeaturePropertyAccessor implements
            PropertyAccessor {

        public boolean canHandle(Object object, String xpath, Class target) {
            if (!"".equals(xpath))
                return false;

            if (target != Geometry.class || target != GeometryAttribute.class)
                return false;

            return (object instanceof Feature || object instanceof FeatureType);
        }

        public Object get(Object object, String xpath, Class target) {
            if (object instanceof Feature) {
                return ((Feature) object).getDefaultGeometry();
            }
            if (object instanceof FeatureType) {
                return ((FeatureType) object).getDefaultGeometry();
            }

            return null;
        }

        public void set(Object object, String xpath, Object value, Class target)
                throws IllegalAttributeException {

            if (object instanceof Feature) {
                final Feature f = (Feature) object;
                GeometryAttribute geom;
                if (value instanceof GeometryAttribute) {
                    geom = (GeometryAttribute) value;
                    f.setDefaultGeometry(geom);
                } else if (value instanceof Geometry) {
                    geom = f.getDefaultGeometry();
                    geom.set(value);
                } else {
                    throw new IllegalArgumentException(
                            "Argument is not a geometry: " + value);
                }
            }
            if (object instanceof FeatureType) {
                throw new IllegalAttributeException("feature type is immutable");
            }
        }
    }

    static class FeaturePropertyAccessor implements PropertyAccessor {
        static {
            // TODO: use a wrapper public class for Feature in order to
            // support any implementation. Reason being that JXPath works
            // over concrete classes and hence we cannot set it up over the
            // interface
            JXPathIntrospector.registerDynamicClass(FeatureImpl.class,
                    AttributePropertyHandler.class);
            JXPathIntrospector.registerDynamicClass(SimpleFeatureImpl.class,
                    AttributePropertyHandler.class);
            JXPathIntrospector.registerDynamicClass(ComplexAttributeImpl.class,
                    AttributePropertyHandler.class);

            JXPathIntrospector.registerDynamicClass(
                    AttributeDescriptorImpl.class,
                    AttributeDescriptorPropertyHandler.class);
            JXPathIntrospector.registerDynamicClass(FeatureTypeImpl.class,
                    AttributeDescriptorPropertyHandler.class);
        }

        public boolean canHandle(Object object, String xpath, Class target) {
            // xpath = stripPrefix(xpath);

            return object instanceof Feature || object instanceof FeatureType
                    || object instanceof AttributeDescriptor;

        }

        public Object get(Object object, String xpath, Class target) {
            // xpath = stripPrefix(xpath);

            JXPathContext context = JXPathContext.newContext(object);
            context.setLenient(true);

            Object value = context.getValue(xpath);

            if (value != null) {
                if (object instanceof Feature) {
                    assert value instanceof Attribute || value instanceof List;
                } else {
                    assert value instanceof PropertyType;
                }
            }
            return value;
        }

        public void set(Object object, String xpath, Object value, Class target)
                throws IllegalAttributeException {
            // xpath = stripPrefix(xpath);

            if (object instanceof FeatureType) {
                throw new IllegalAttributeException("feature type is immutable");
            }

            JXPathContext context = JXPathContext.newContext(object);
            context.setLenient(true);
            context.setValue(xpath, value);

            assert value == context.getValue(xpath);
        }
    }

}
