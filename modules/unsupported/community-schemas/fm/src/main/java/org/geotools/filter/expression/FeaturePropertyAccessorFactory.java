package org.geotools.filter.expression;

import java.util.Enumeration;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.geotools.factory.Hints;
import org.geotools.factory.Hints.Key;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.iso.AttributeImpl;
import org.geotools.feature.iso.ComplexAttributeImpl;
import org.geotools.feature.iso.FeatureImpl;
import org.geotools.feature.iso.attribute.BooleanAttribute;
import org.geotools.feature.iso.attribute.GeometricAttribute;
import org.geotools.feature.iso.attribute.NumericAttribute;
import org.geotools.feature.iso.attribute.TemporalAttribute;
import org.geotools.feature.iso.attribute.TextualAttribute;
import org.geotools.feature.iso.simple.SimpleFeatureImpl;
import org.geotools.feature.iso.type.AttributeDescriptorImpl;
import org.geotools.feature.iso.type.FeatureTypeImpl;
import org.geotools.feature.iso.xpath.AttributeDescriptorPropertyHandler;
import org.geotools.feature.iso.xpath.AttributeNodePointerFactory;
import org.geotools.feature.iso.xpath.AttributePropertyHandler;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.xml.sax.helpers.NamespaceSupport;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Creates a namespace aware property accessor for ISO Features.
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
 * @author Gabriel Roldan, Axios Engineering
 * 
 */
public class FeaturePropertyAccessorFactory implements PropertyAccessorFactory {

    /**
     * {@link Hints} key used to pass namespace context to
     * {@link #createPropertyAccessor(Class, String, Class, Hints)} in the form
     * of a {@link NamespaceSupport} instance with the prefix/namespaceURI
     * mappings
     */
    public static final Key NAMESPACE_CONTEXT = new Hints.Key(
            org.xml.sax.helpers.NamespaceSupport.class);

    static {
        // unfortunatley, jxpath only works against concreate classes
        // JXPathIntrospector.registerDynamicClass(DefaultFeature.class,
        // FeaturePropertyHandler.class);
        JXPathContextReferenceImpl.addNodePointerFactory(new AttributeNodePointerFactory());
    }

    /** Single instnace is fine - we are not stateful */
    static PropertyAccessor ATTRIBUTE_ACCESS = new FeaturePropertyAccessor();

    static PropertyAccessor DEFAULT_GEOMETRY_ACCESS = new DefaultGeometryFeaturePropertyAccessor();

    static PropertyAccessor FID_ACCESS = new FidFeaturePropertyAccessor();

    public PropertyAccessor createPropertyAccessor(Class type, String xpath, Class target,
            Hints hints) {

        if (xpath == null)
            return null;

        if (!ComplexAttribute.class.isAssignableFrom(type)
                && !ComplexType.class.isAssignableFrom(type)
                && !AttributeDescriptor.class.isAssignableFrom(type))
            return null; // we only work with simple feature

        if ("".equals(xpath) && target == Geometry.class)
            return DEFAULT_GEOMETRY_ACCESS;

        // check for fid access
        if (xpath.matches("@(\\w+:)?id"))
            return FID_ACCESS;

        // check for simple property access
        // if (xpath.matches("(\\w+:)?(\\w+)")) {
        NamespaceSupport namespaces = null;
        if (hints != null) {
            namespaces = (NamespaceSupport) hints
                    .get(FeaturePropertyAccessorFactory.NAMESPACE_CONTEXT);
        }
        if (namespaces == null) {
            return ATTRIBUTE_ACCESS;
        } else {
            return new FeaturePropertyAccessor(namespaces);
        }
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

    static class DefaultGeometryFeaturePropertyAccessor implements PropertyAccessor {

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
                    geom.setValue(value);
                } else {
                    throw new IllegalArgumentException("Argument is not a geometry: " + value);
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
            JXPathIntrospector.registerDynamicClass(AttributeImpl.class,
                    AttributePropertyHandler.class);
            JXPathIntrospector.registerDynamicClass(GeometricAttribute.class,
                    AttributePropertyHandler.class);
            JXPathIntrospector.registerDynamicClass(BooleanAttribute.class,
                    AttributePropertyHandler.class);
            JXPathIntrospector.registerDynamicClass(NumericAttribute.class,
                    AttributePropertyHandler.class);
            JXPathIntrospector.registerDynamicClass(TemporalAttribute.class,
                    AttributePropertyHandler.class);
            JXPathIntrospector.registerDynamicClass(TextualAttribute.class,
                    AttributePropertyHandler.class);

            JXPathIntrospector.registerDynamicClass(AttributeDescriptorImpl.class,
                    AttributeDescriptorPropertyHandler.class);
            JXPathIntrospector.registerDynamicClass(FeatureTypeImpl.class,
                    AttributeDescriptorPropertyHandler.class);
        }

        private NamespaceSupport namespaces;

        public FeaturePropertyAccessor() {
            namespaces = new NamespaceSupport();
        }

        public FeaturePropertyAccessor(NamespaceSupport namespaces) {
            this.namespaces = namespaces;
        }

        public boolean canHandle(Object object, String xpath, Class target) {
            // xpath = stripPrefix(xpath);

            return object instanceof Attribute || object instanceof AttributeType
                    || object instanceof AttributeDescriptor;

        }

        public Object get(Object object, String xpath, Class target) {
            // xpath = stripPrefix(xpath);

            JXPathContext context = JXPathContext.newContext(object);
            context.setLenient(true);
            Enumeration declaredPrefixes = namespaces.getDeclaredPrefixes();
            while (declaredPrefixes.hasMoreElements()) {
                String prefix = (String) declaredPrefixes.nextElement();
                String uri = namespaces.getURI(prefix);
                context.registerNamespace(prefix, uri);
            }

            Object value = context.getValue(xpath);

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
