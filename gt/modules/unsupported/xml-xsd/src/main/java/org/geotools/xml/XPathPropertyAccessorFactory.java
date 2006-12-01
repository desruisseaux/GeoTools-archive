package org.geotools.xml;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathContextFactory;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.geotools.factory.Hints;
import org.geotools.feature.Feature;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.expression.PropertyAccessor;
import org.geotools.filter.expression.PropertyAccessorFactory;
import org.geotools.xml.impl.jxpath.FeatureNodeFactory;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * PropertyAccessorFactory used to create property accessors which can handle 
 * xpath expressions against instances of {@link Feature}.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class XPathPropertyAccessorFactory implements PropertyAccessorFactory {

	/**
	 * Namespace support hint
	 */
	public static Hints.Key NAMESPACE_SUPPORT = new Hints.Key( NamespaceSupport.class );
	
	static {
		//unfortunatley, jxpath only works against concreate classes
		//JXPathIntrospector.registerDynamicClass(DefaultFeature.class, FeaturePropertyHandler.class);
		JXPathContextReferenceImpl.addNodePointerFactory( new FeatureNodeFactory() );
	}
	
	public PropertyAccessor createPropertyAccessor(Class type, String xpath,
			Class target, Hints hints) {
		
		if ( Feature.class.isAssignableFrom( type ) ) {
			return new XPathPropertyAcessor( );
		}
		
		return null;
	}

	static class XPathPropertyAcessor implements PropertyAccessor {

		public boolean canHandle(Object object, String xpath, Class target) {
			//TODO: some check for a valid xpath expression
			return true;
		}
		
		public Object get(Object object, String xpath, Class target) {
			return context( object ).getValue( xpath );
		}

		public void set(Object object, String xpath, Object value, Class target) throws IllegalAttributeException {
			context( object ).setValue( xpath, value );
		}
		
		JXPathContext context( Object object ) {
			JXPathContext context = JXPathContextFactory.newInstance().newContext( null, object );
			context.setLenient( true );
			
			return context;
		}
		
	}
}
