package org.geotools.feature;

import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.geotools.feature.impl.AttributeImpl;
import org.geotools.feature.impl.ComplexAttributeImpl;
import org.geotools.feature.impl.FeatureImpl;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.feature.simple.SimpleFeatureTypeImpl;
import org.geotools.feature.type.AttributeTypeImpl;
import org.geotools.feature.type.ComplexTypeImpl;
import org.geotools.feature.type.FeatureCollectionTypeImpl;
import org.geotools.feature.type.FeatureTypeImpl;
import org.geotools.feature.type.GeometryTypeImpl;
import org.geotools.feature.xpath.AttributePropertyHandler;
import org.geotools.feature.xpath.AttributeTypePropertyHandler;
import org.opengis.feature.Attribute;
import org.opengis.feature.type.AttributeType;

/**
 * Utility class to evaluate XPath expressions against an Attribute instance,
 * which may be any Attribute, wether it is simple, complex, a feature, etc.
 * <p>
 * At the difference of the Filter subsistem, which works against Attribute
 * contents (for example to evaluate a coparison filter), the XPath subsistem,
 * for which this class is the single entry point, works against Attribute
 * instances. That is, the result of an XPath expression, if a single value, is
 * an Attribtue, not the attribute content, or a List of Attributes, for
 * instance.
 * </p>
 * 
 * @author Gabriel Roldan
 * 
 * TODO: register namespaces in JXPathContext
 */
public class XPath {
	private static final Logger LOGGER = Logger.getLogger(XPath.class
			.getPackage().getName());

	static {
		JXPathIntrospector.registerDynamicClass(AttributeImpl.class,
				AttributePropertyHandler.class);
		JXPathIntrospector.registerDynamicClass(ComplexAttributeImpl.class,
				AttributePropertyHandler.class);
		JXPathIntrospector.registerDynamicClass(FeatureImpl.class,
				AttributePropertyHandler.class);
		JXPathIntrospector.registerDynamicClass(SimpleFeatureImpl.class,
				AttributePropertyHandler.class);
		LOGGER.finer("Registered " + AttributePropertyHandler.class.getName()
				+ " to handle geotools Attribute xpath expressions");
		
		
		JXPathIntrospector.registerDynamicClass(AttributeTypeImpl.class,
				AttributeTypePropertyHandler.class);
		JXPathIntrospector.registerDynamicClass(ComplexTypeImpl.class,
				AttributeTypePropertyHandler.class);
		JXPathIntrospector.registerDynamicClass(GeometryTypeImpl.class,
				AttributeTypePropertyHandler.class);
		JXPathIntrospector.registerDynamicClass(FeatureTypeImpl.class,
				AttributeTypePropertyHandler.class);
		JXPathIntrospector.registerDynamicClass(SimpleFeatureTypeImpl.class,
				AttributeTypePropertyHandler.class);
		JXPathIntrospector.registerDynamicClass(FeatureCollectionTypeImpl.class,
				AttributeTypePropertyHandler.class);
		LOGGER.finer("Registered " + AttributeTypePropertyHandler.class.getName()
				+ " to handle geotools AttributeType xpath expressions");
	}

	public static Object get(final AttributeType type, final String xpathExpression) {
		JXPathContext ctx = JXPathContext.newContext(type);

		Object retVal = ctx.getValue(xpathExpression);

		return retVal;
	}

	/**
	 * Applies the xpath expression given by <code>xpath</code> to the
	 * contents of <code>att</code> and returns the result.
	 * <p>
	 * Note that due to an imposition in JXPath, the Attribute passed as
	 * argument is treated as the root element.
	 * </p>
	 * 
	 * @param att
	 *            the Attribute to which apply the XPath expression
	 * @param xpathExpression
	 *            an XPath expression as supported by <a
	 *            href="http://jakarta.apache.org/commons/jxpath/">JXPath</a>
	 * @return an Attribute or List<Attribute>, depending on the
	 *         <code>xpathExpression</code> resolving to a single or multiple
	 *         values, or <code>null</code> if the expression matched nothing.
	 */
	public static Object get(final Attribute att, final String xpathExpression) {
		JXPathContext ctx = JXPathContext.newContext(att);

		Object retVal = ctx.getValue(xpathExpression);

		return retVal;
	}

	public static Object get(final Map<String, String>namesapces, final Attribute att, final String xpathExpression) {
		JXPathContext ctx = JXPathContext.newContext(att);

		for(Map.Entry<String, String> e : namesapces.entrySet()){
			ctx.registerNamespace(e.getKey(), e.getValue());
		}

		Object retVal = ctx.getValue(xpathExpression);

		return retVal;
	}

	public static void set(final Attribute att, final String xpath, Object value) {
		JXPathContext.newContext(att).setValue(xpath, value);
	}
}
