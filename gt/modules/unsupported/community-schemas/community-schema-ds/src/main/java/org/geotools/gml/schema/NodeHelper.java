package org.geotools.gml.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.geotools.feature.type.SimpleTypes;
import org.geotools.util.AttributeName;
import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Handles the contents of an "element" element.
 * <p>
 * As subproducts accepts the contents of complexType, simpleType.
 * </p>
 * <p>
 * Produces an AttributeDescriptor.
 * </p>
 * 
 * @author gabriel
 * 
 */
class NodeHelper extends AbstractParserHelper {
	private static Map/*<AttributeName, Class>*/ allowedChilds = new HashMap/*<AttributeName, Class>*/();

	/**
	 * element attributes not considered as AttributeDescripto's client properties
	 */
	private static final List KOWN_ATTRIBUTES = new ArrayList();
	
	static {
		allowedChilds.put(ANNOTATION, NULL_HELPER.class);
		allowedChilds.put(COMPLEXTYPE, ComplexTypeHelper.class);
		allowedChilds.put(SIMPLETYPE, SimpleTypeHelper.class);
		
		KOWN_ATTRIBUTES.add("name");
		KOWN_ATTRIBUTES.add("type");
		KOWN_ATTRIBUTES.add("ref");
		KOWN_ATTRIBUTES.add("minOccurs");
		KOWN_ATTRIBUTES.add("maxOccurs");
		KOWN_ATTRIBUTES.add("nillable");
	}

	/** product to create */
	AttributeDescriptor node;

	/** product's type */
	AttributeType nodeType;

	/** product's name */
	AttributeName nodeName, nodeTypeName;
	
	/** nodes client properties (like substitutionGroup?)*/
	Map clientProperties;
	
	int min, max;
	boolean nillable;

	protected Map/*<AttributeName, Class>*/ getHelpers() {
		return allowedChilds;
	}

	public Object getProduct(AttributeName name) {
		if(!ELEMENT.equals(name)){
			throw new IllegalArgumentException("this helper only handles element products");
		}
		if(node != null){
			//already obtained by ref="topLevelElemName"
			return node;
		}
		if (nodeType == null && nodeTypeName == null) {
			if(clientProperties == null || !"true".equals(clientProperties.get("abstract"))){
				throw new IllegalStateException("node's type was not set. Node name: " + nodeName);
			}else{
				nodeType = typeFactory.getDefault(Object.class);
			}
		}
		// @REVISIT: nillable should actually be a property of AttributeDescriptor
		// not of AttributeType.
		// By now, if the element's "nillability" differs from the type one, we'll make the node's type be
		// a subtype of its actual type but maintain the type name
		if(nodeType == null){
			node = new ProxiedTypeDescriptor(typeFactory, nodeName, 
												nodeTypeName, min, max);
		}else{
			if(nillable != nodeType.isNillable().booleanValue()){
				//@REVISIT: this is really ackward, and the main reason because 
				//nillable shall be moved to AttributeDescriptor
				LOGGER.info("WARNING: creating already existing type " + nodeType.getName() + 
						" just because nillability differs!!");
				nodeType = typeFactory.createType(nodeType.getName(), 
						nodeType.getBinding(), nodeType.isIdentified(), nillable,
						null, nodeType);
			}
			node = descFactory.node(nodeName, nodeType, min, max);
		}
		for(Iterator it = clientProperties.entrySet().iterator(); it.hasNext();){
			Map.Entry entry = (Map.Entry)it.next();
			node.putClientProperty(entry.getKey(), entry.getValue());
		}
		return node;
	}

	protected void init(AttributeName name, Attributes attributes)
			throws SAXException {
		LOGGER.entering(getClass().getName(), "init", new Object[]{name, attributes});

		String elementName = attributes.getValue("name");
		String elementRef = attributes.getValue("ref");
		String type = attributes.getValue("type");

		if(elementName == null && elementRef == null){
			throw new IllegalArgumentException(
					"neither name nor ref provided, can't locate or create AttributeDescriptor");
		}
		
		min = minOccurs(attributes);
		max = maxOccurs(attributes);
		
		String nillableAtt = attributes.getValue("nillable");
		//only set nillable if explicitly set, else let it be true
		nillable = nillableAtt == null? true : Boolean.valueOf(nillableAtt).booleanValue();

		if(elementRef != null){
			AttributeName referenceName = deglose(elementRef);
			this.node = typeFactory.getDescriptor(referenceName);
			if(node == null){
				node = new ProxiedAttributeDescriptor(typeFactory, referenceName, min, max);
			}
		}else{
		
			nodeName = new AttributeName(targetNamespaceUri, elementName);
			
			if(type != null){
				nodeTypeName = deglose(type, SimpleTypes.NSURI);
				
	 			try {
					nodeType = typeFactory.getType(nodeTypeName);
				} catch (NoSuchElementException e) {
					LOGGER.fine(nodeTypeName + " not yet registered, delaying " + 
							nodeName + " node's type lookup.");
				}
			}
		
			clientProperties = new HashMap();
			for (int i = 0; i < attributes.getLength(); i++){
				String attName = attributes.getLocalName(i);
				if(!KOWN_ATTRIBUTES.contains(attName)){
					LOGGER.fine("adding client property '" + attName + "'");
					String attValue = attributes.getValue(i);
					clientProperties.put(attName, attValue);
				}
			}
		}
	}

	protected void startChild(AttributeName name, Attributes attributes)
			throws SAXException {
	}

	public void addSubproduct(AttributeName name, Object subProduct) {
		if(subProduct instanceof AttributeType){
			this.nodeType = (AttributeType)subProduct;
		}
	}

}