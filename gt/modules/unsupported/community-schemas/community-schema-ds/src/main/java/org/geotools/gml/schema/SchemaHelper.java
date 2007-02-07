package org.geotools.gml.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.geotools.util.AttributeName;
import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Hanldes the content of top level schema element.
 * <p>
 * As subproducts, accepts the products of import, include, complexType,
 * simpleType, and top level element.
 * </p>
 * <p>
 * Produces a List of AttributeType
 * </p>
 * 
 * @author gabriel
 * 
 */
class SchemaHelper extends AbstractParserHelper {
	private static Map/*<AttributeName, Class>*/ allowedChilds = 
		new HashMap/*<AttributeName, Class>*/();

	/**
	 * Holds parsed types and top level AttributeDescriptors
	 */
	private List parsedContent = new ArrayList();

	static {
		allowedChilds.put(ATTRIBUTE, NULL_HELPER.class);
		allowedChilds.put(ATTRIBUTEGROUP, NULL_HELPER.class);
		allowedChilds.put(IMPORT, NULL_HELPER.class);
		allowedChilds.put(INCLUDE, NULL_HELPER.class);
		allowedChilds.put(ANNOTATION, NULL_HELPER.class);
		allowedChilds.put(DOCUMENTATION, NULL_HELPER.class);
		allowedChilds.put(COMPLEXTYPE, ComplexTypeHelper.class);
		allowedChilds.put(SIMPLETYPE, SimpleTypeHelper.class);
		allowedChilds.put(ELEMENT, NodeHelper.class);
		allowedChilds.put(GROUP, GroupHelper.class);
	}

	private String childName;

	public String getTargetNamespaceUri() {
		return targetNamespaceUri;
	}

	protected Map/*<AttributeName, Class>*/ getHelpers() {
		return allowedChilds;
	}

	public Object getProduct(AttributeName name) {
		return parsedContent;
	}

	protected void init(AttributeName name, Attributes attributes) throws SAXException {
		LOGGER.finest(this + " init " + name);
		this.targetNamespaceUri = attributes.getValue("targetNamespace");
		LOGGER.finer("target namespace: " + targetNamespaceUri);
	}

	/**
	 * @param name
	 *            may be import, include, complexType, simpleType, or element
	 */
	protected void startChild(AttributeName name, Attributes attributes)
			throws SAXException {
		if (COMPLEXTYPE.equals(name)) {
			childName = attributes.getValue("name");
		} else if (SIMPLETYPE.equals(name)) {
			childName = attributes.getValue("name");
		}

	}

	public void addSubproduct(AttributeName name, Object subProduct) {
		if (IMPORT.equals(name)) {
			// TODO: implement importing
		} else if (INCLUDE.equals(name)) {
			// TODO: implement include
		} else if (ANNOTATION.equals(name)) {
			// TODO: implement annotation
		} else if (ATTRIBUTE.equals(name)) {
			// TODO: implement attribute
		} else if (ATTRIBUTEGROUP.equals(name)) {
			// TODO: implement attribute
		} else if (GROUP.equals(name)) {
			this.groups.putAll((Map)subProduct);
		} else if (COMPLEXTYPE.equals(name) || SIMPLETYPE.equals(childName)) {
			if(subProduct != null){
				AttributeType newType = (AttributeType) subProduct;
				org.opengis.feature.AttributeName typeName = newType.getName();
				parsedContent.add(newType);
				try{
					AttributeType existent = typeFactory.getType(typeName);
					LOGGER.info("type already exists, avoiding: " + existent.getName());
				}catch(NoSuchElementException e){
					LOGGER.fine("registering new type " + newType);
					typeFactory.registerType(newType);
				}
			}
		} else if (ELEMENT.equals(name)) {
			AttributeDescriptor topLevelNode = (AttributeDescriptor) subProduct;
			parsedContent.add(topLevelNode);
			typeFactory.registerDescriptor(topLevelNode);
		} else if (SIMPLETYPE.equals(name)) {
			AttributeType type = (AttributeType) subProduct;
			parsedContent.add(type);
			typeFactory.registerType(type);
		}  else {
			throw new IllegalArgumentException(String.valueOf(name));
		}
	}

}