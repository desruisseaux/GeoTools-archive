package org.geotools.gml.schema;

import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

import org.geotools.feature.type.SimpleTypes;
import org.geotools.util.AttributeName;
import org.opengis.feature.schema.DescriptorFactory;
import org.opengis.feature.type.TypeFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.NamespaceSupport;


/**
 * DOCUMENT ME!
 *
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @source $URL$
 * @since 2.3.x
  */
public abstract class AbstractParserHelper{

	protected static final Logger LOGGER = Logger
			.getLogger(AbstractParserHelper.class.getPackage().getName());

	public static final AttributeName SCHEMA = new AttributeName(SimpleTypes.NSURI, "schema");

	public static final AttributeName IMPORT = new AttributeName(SimpleTypes.NSURI, "import");

	public static final AttributeName INCLUDE = new AttributeName(SimpleTypes.NSURI, "include");

	public static final AttributeName ANNOTATION = new AttributeName(SimpleTypes.NSURI, "annotation");

	public static final AttributeName DOCUMENTATION = new AttributeName(SimpleTypes.NSURI, "documentation");

	public static final AttributeName COMPLEXTYPE = new AttributeName(SimpleTypes.NSURI, "complexType");

	public static final AttributeName COMPLEXCONTENT = new AttributeName(SimpleTypes.NSURI, "complexContent");

	public static final AttributeName EXTENSION =  new AttributeName(SimpleTypes.NSURI, "extension");

	public static final AttributeName RESTRICTION = new AttributeName(SimpleTypes.NSURI, "restriction");

	public static final AttributeName ALL = new AttributeName(SimpleTypes.NSURI,"all");

	public static final AttributeName SEQUENCE = new AttributeName(SimpleTypes.NSURI, "sequence");

	public static final AttributeName CHOICE = new AttributeName(SimpleTypes.NSURI, "choice");

	public static final AttributeName ELEMENT = new AttributeName(SimpleTypes.NSURI, "element");

	public static final AttributeName SIMPLETYPE = new AttributeName(SimpleTypes.NSURI, "simpleType");

	public static final AttributeName SIMPLECONTENT = new AttributeName(SimpleTypes.NSURI, "simpleContent");

	public static final AttributeName LIST = new AttributeName(SimpleTypes.NSURI,"list");

	public static final AttributeName UNION = new AttributeName(SimpleTypes.NSURI,"union");

	public static final AttributeName ATTRIBUTE = new AttributeName(SimpleTypes.NSURI, "attribute");

	public static final AttributeName GROUP = new AttributeName(SimpleTypes.NSURI,"group");

	public static final AttributeName ATTRIBUTEGROUP = new AttributeName(SimpleTypes.NSURI, "attributeGroup");
	public static final AttributeName ANY = new AttributeName(SimpleTypes.NSURI, "any");

	/**
	 * A helper that does nothing, usefull to gracefully ignore unsupported elements
	 * @author Gabriel Roldan, Axios Engineering
	 */
	protected static final class NULL_HELPER extends AbstractParserHelper {
		protected Map/*<AttributeName, Class>*/ getHelpers() {
			return Collections.EMPTY_MAP;
		}

		protected AbstractParserHelper getHelper(AttributeName name) {return this;}
		public void startElement(AttributeName name, Attributes attributes)
		throws SAXException {}		
		public Object getProduct(AttributeName name) {return null;}

		protected void init(AttributeName name, Attributes attributes)
				throws SAXException {}

		public void startChild(AttributeName name, Attributes attributes)
				throws SAXException {}

		public void addSubproduct(AttributeName name, Object subProduct) {}
	}

	protected static final class UNSUPPORTED_HELPER extends AbstractParserHelper {
		protected Map/*<AttributeName, Class>*/ getHelpers() {
			return Collections.EMPTY_MAP;
		}

		public Object getProduct(AttributeName name) {return null;}

		protected void init(AttributeName name, Attributes attributes)
				throws SAXException {}

		public void startChild(AttributeName name, Attributes attributes)
				throws SAXException {}

		public void addSubproduct(AttributeName name, Object subProduct) {}
	}

	protected TypeFactory typeFactory;

	protected DescriptorFactory descFactory;

	protected String targetNamespaceUri;

	protected NamespaceSupport namespaces;

	protected Map groups;

	public AbstractParserHelper() {
		//no-op
	}

	/**
	 * Used to track prefix/namespace, must be set by client code.
	 * 
	 * @param targetNamespaceUri
	 * @param namespaces
	 */
	public void setNamespaces(String targetNamespaceUri,
			NamespaceSupport namespaces) {
		this.targetNamespaceUri = targetNamespaceUri;
		this.namespaces = namespaces;
	}

	/**
	 * Used to share factories among instances
	 * 
	 * @param typeFactory
	 * @param descFactory
	 */
	public void setFactories(TypeFactory typeFactory,
			DescriptorFactory descFactory) {
		this.typeFactory = typeFactory;
		this.descFactory = descFactory;
	}
	
	public void setGroups(Map/*<AttributeName, Descriptor>*/groups){
		this.groups = groups;
	}

	/**
	 * Returns the registered helper for childs of name <code>name</code>
	 * 
	 * @param name
	 * @return
	 */
	protected AbstractParserHelper getHelper(AttributeName name) {
		Map/*<AttributeName, Class>*/ helpers = getHelpers();
		Class helperClass = (Class) helpers.get(name);
		if (helperClass == null) {
			throw new IllegalArgumentException(getClass().getName()
					+ ": No manager defined for element " + name);
		}
		try {
			AbstractParserHelper helper;
			helper = (AbstractParserHelper) helperClass.newInstance();
			return helper;
		} catch (Exception e) {
			throw new RuntimeException("the imposible has happened: " + e);
		}
	}

	/**
	 * Asserts that this content helper has a registered helper to handle
	 * childs of xml type <code>name</code> and then calls {@link #startChild(AttributeName, Attributes)}
	 * @param name
	 * @param attributes
	 * @throws SAXException
	 * @throws IllegalArgumentException if <code>name</code> has no registered
	 * parsing helper for this instance (!getHelpers().containsKey(name)).
	 */
	public void startElement(AttributeName name, Attributes attributes)
			throws SAXException {
		if (!getHelpers().containsKey(name)) {
			throw new IllegalArgumentException("Element " + name
					+ " is unknown for " + getClass().getName());
		}
		startChild(name, attributes);
	}

	protected abstract Map/*<AttributeName, Class>*/ getHelpers();

	protected abstract void startChild(AttributeName name, Attributes attributes)
			throws SAXException;

	/**
	 * Tells this handler to start creating content of XSD type <cpde>name</code>.
	 * <p>
	 * As the same helper implementatino may be responsible to handle more
	 * than one element type, the <code>name</code> parameter serves as
	 * a discriminator. For example, the same helper may be responsible
	 * of handling &lt;sequence&gt;, &lt;choice&gt; and &lt;all&gt;
	 * </p>
	 * 
	 * @param name the xml tag name for which to start creating content
	 * @param attributes attributes of xml tag
	 * @throws SAXException
	 */
	protected abstract void init(AttributeName name, Attributes attributes)
			throws SAXException;

	/**
	 * Tells this content helper to include the result of subproduct
	 * named <code>name</code> as the value <code>subProduct</code>.
	 * <p>
	 * For example, if it is a content helper for &lt;element&gt; parsing,
	 * <code>subproduct</code> may be an {@link org.opengis.feature.type.AttributeType},
	 * if <code>name</code> is "simpleType" or "complexType".
	 * </p>
	 * @param name the xsd element name of the parsed subproduct
	 * @param subProduct the parsed subproduct for <code>name</code>
	 */
	public abstract void addSubproduct(AttributeName name, Object subProduct);

	/**
	 * Returns the product of this parser helper.
	 * <p>
	 * Product may be an AttributeDescriptor if this is a helper
	 * to handle &lt;element&gt;s, an AttributeType if its a
	 * helper to handle &lt;complexType&gt; or &lt;simpleType&gt;, etc.
	 * </p>
	 * @param name
	 * @return
	 */
	public abstract Object getProduct(AttributeName name);

	/**
	 * Extracts <code>minOccurs</code> from <code>attributes</code>,
	 * returning default value <code>1</code> if not found.
	 * 
	 * @param attributes
	 * @return
	 */
	protected int minOccurs(Attributes attributes) {
		String minOccurs = attributes.getValue("minOccurs");
		int min = 1;
		if (minOccurs != null) {
			min = Integer.parseInt(minOccurs);
		}
		return min;
	}

	/**
	 * Extracts <code>maxOccurs</code> from <code>attributes</code>,
	 * returning default value <code>1</code> if not found.
	 * 
	 * @param attributes
	 * @return
	 */
	protected int maxOccurs(Attributes attributes) {
		String maxOccurs = attributes.getValue("maxOccurs");
		int max = 1;
		if ("unbounded".equals(maxOccurs)) {
			max = Integer.MAX_VALUE;
		} else if (maxOccurs != null) {
			max = Integer.parseInt(maxOccurs);
		}
		return max;
	}

	
	/**
	 * Receives a namespace prefixed (i.e., "gml:boolean") and returns an
	 * AttributeName properly composed of the namespace registered for that
	 * prefix and the prefixedName's localname.
	 * <p>
	 * If no namespace prefix is found, returned AttributeName has {@link #targetNamespaceUri}
	 * namespace.
	 * </p> 
	 * 
	 * @param prefixedName name with or without namespace prefix. If no namespace
	 * prefix is found, returned AttributeName has {@link #targetNamespaceUri}
	 * namespace.
	 * @return the AttributeName for <code>prefixedName</code>
	 */
	protected AttributeName deglose(String prefixedName){
		return deglose(prefixedName, targetNamespaceUri);
	}
	
	/**
	 * Receives a namespace prefixed (i.e., "gml:boolean") and returns an
	 * AttributeName properly composed of the namespace registered for that
	 * prefix and the prefixedName's localname.
	 * <p>
	 * If no namespace prefix is found, <code>defaultNamespace</code> is used
	 * as the AttributeName's namespace.
	 * </p> 
	 * 
	 * @param prefixedName name with or without namespace prefix.
	 * @return the AttributeName for <code>prefixedName</code>
	 */
	protected AttributeName deglose(String prefixedName, String defaultNamespace){
		AttributeName name = null;
		
		if(prefixedName == null)return null;
		
		String localName;
		int prefixIdx = prefixedName.indexOf(':');
		String nsPrefix;
		String nsUri;
		if (prefixIdx > 0) {
			nsPrefix = prefixedName.substring(0, prefixIdx);
			localName = prefixedName.substring(prefixIdx + 1);
			nsUri = namespaces.getURI(nsPrefix);
		} else {
			nsUri = defaultNamespace;
			localName = prefixedName;
		}
		
		name = new AttributeName(namespaces.getPrefix(nsUri), nsUri, localName);
		
		return name;
	}
	
	public String toString() {
		String name = getClass().getName();
		int idx = name.lastIndexOf('.');
		if (idx > -1) {
			name = name.substring(++idx);
		}
		return name;
	}
}