package org.geotools.gml.schema;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.geotools.feature.schema.OrderedImpl;
import org.geotools.feature.type.GMLTypes;
import org.geotools.feature.type.SimpleTypes;
import org.geotools.util.AttributeName;
import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.simple.SimpleDescriptor;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Handles the contents of a complexType element.
 * <p>
 * As subproducts accepts the products of complexContent, choice, all,
 * sequence, attribute.
 * </p>
 * <p>
 * Produces a {@linkplain Descriptor}
 * </p>
 * 
 * @author gabriel
 * 
 */
class ComplexTypeHelper extends AbstractParserHelper {
	private static Map/*<AttributeName, Class>*/ allowedChilds = new HashMap/*<AttributeName, Class>*/();

	static {
		allowedChilds.put(ANNOTATION, NULL_HELPER.class);
		allowedChilds.put(ATTRIBUTEGROUP, NULL_HELPER.class);
		allowedChilds.put(COMPLEXCONTENT, NULL_HELPER.class);
		allowedChilds.put(EXTENSION, NULL_HELPER.class);
		allowedChilds.put(RESTRICTION, NULL_HELPER.class);
		allowedChilds.put(SEQUENCE, DescriptorHelper.class);
		allowedChilds.put(CHOICE, DescriptorHelper.class);
		allowedChilds.put(ALL, DescriptorHelper.class);
		allowedChilds.put(ATTRIBUTE, NULL_HELPER.class);
		allowedChilds.put(SIMPLECONTENT, SimpleContentHelper.class);
	}

	private Descriptor descriptor;

	private org.opengis.feature.AttributeName typeName;

	/**
	 * provided as subproduct by a complexContent child
	 */
	private String superTypeName;
	
	/**
	 * May be set on addSubproduct when childName was "simpleContent".
	 * In that case getProduct() returns a simple type, not a complex one.
	 */
	private AttributeType simpleContent;

	/**
	 * 
	 */
	private AttributeName derivationMethod = EXTENSION;

	private boolean identified = false;
	private boolean isAbstract = false;
	
	protected Map/*<AttributeName, Class>*/ getHelpers() {
		return allowedChilds;
	}

	/**
	 * Overrides getHelper to ignore complexContent, extension, and
	 * restriction, by returning <code>this</code>.
	 * 
	 * @see org.geotools.gml.schema.AbstractParserHelper#getHelper(javax.xml.namespace.AttributeName)
	 */
	protected AbstractParserHelper getHelper(AttributeName name) {
		if (COMPLEXCONTENT.equals(name) || EXTENSION.equals(name)
				|| RESTRICTION.equals(name)) {
			return this;
		}
		return super.getHelper(name);
	}

	/**
	 * Produces the ComplexType
	 */
	public Object getProduct(AttributeName name) {
		if (EXTENSION.equals(name) || RESTRICTION.equals(name)
				|| COMPLEXCONTENT.equals(name)) {
			return null;
		}
		LOGGER.finest("creating type " + this.typeName
				+ " with descriptor " + this.descriptor);
		
		if(simpleContent != null){
			AttributeType type;
			Class binding = simpleContent.getBinding();
			boolean identified = simpleContent.isIdentified();
			Boolean nillable = simpleContent.isNillable();
			Set restrictions = simpleContent.getRestrictions();
			type = typeFactory.createType(typeName, binding, identified, 
					nillable.booleanValue(), restrictions, simpleContent);
			return type;
		}else{
			
			ComplexType type;
			ComplexType superType = null;
	
			if (superTypeName != null) {
				AttributeName superName;
				String localName;
				LOGGER.fine("looking for " + typeName + "'s supertype: "
						+ superTypeName);
				int prefixIdx = superTypeName.indexOf(':');
				String nsPrefix;
				if (prefixIdx > 0) {
					nsPrefix = superTypeName.substring(0, prefixIdx);
					localName = superTypeName.substring(prefixIdx + 1);
				} else {
					localName = superTypeName;
					nsPrefix = ((AttributeName)this.typeName).getPrefix();
				}
				
				String nsUri = super.namespaces.getURI(nsPrefix);
				superName = new AttributeName( nsPrefix, nsUri, localName);
			
				try{
					superType = (ComplexType) typeFactory.getType(superName);
				}catch(NoSuchElementException e){
					superType = new ProxiedComplexType(superName, typeFactory);
				}
				LOGGER.finest("found super type: " + superType);
			}
	
			boolean nillable = true;
			Set/*<Filter>*/ restrictions = null;
	
			if(descriptor == null){
				descriptor = new OrderedImpl(Collections.EMPTY_LIST);
				LOGGER.fine("descriptor not set for complex type " + typeName);
			}
			if( (descriptor instanceof SimpleDescriptor) && 
					(superType == null || superType instanceof SimpleFeatureType)){
				type = typeFactory.createFeatureType(typeName, 
							((SimpleDescriptor)descriptor).sequence(), null,
							restrictions, (SimpleFeatureType)superType, isAbstract);
			}else{
				type = typeFactory.createType(typeName, descriptor, identified,
						nillable, restrictions, superType, isAbstract);
			}
			LOGGER.fine(type.toString());
			return type;
		}
	}

	protected void init(AttributeName name, Attributes attributes)
			throws SAXException {
		if (COMPLEXTYPE.equals(name)) {
			String typeName = attributes.getValue("name");
			
			LOGGER.finer(this + " init " + name + " " + typeName);
			if (typeName != null) {
				// TODO: add application schema namespace
				this.typeName = new AttributeName(
					this.targetNamespaceUri, typeName
				);
				LOGGER.fine("init complex type " + this.typeName);
			} else {
				LOGGER.fine("type name not provided as complexType attribute");
				this.typeName = SimpleTypes.ANONYMOUS;
			}
			this.isAbstract = "true".equals(attributes.getValue("abstract"));
		} else if (EXTENSION.equals(name) || RESTRICTION.equals(name)) {
			derivationMethod = name;
			superTypeName = attributes.getValue("base");
		} else if (COMPLEXCONTENT.equals(name)) {
			// no-op.
		}
	}

	protected void startChild(AttributeName childName, Attributes attributes)
			throws SAXException {
		if(ATTRIBUTE.equals(childName)){
			//only handle gml:id to establish the identified attribute flag
			String idAtt = attributes.getValue(SimpleTypes.NSURI, "ref");
			if(idAtt == null){
				idAtt = attributes.getValue("ref");
			}
			String gmlPrefix = namespaces.getPrefix(GMLTypes.GML_NSURI);
			if((gmlPrefix + ":id").equals(idAtt) ){
				LOGGER.fine("type is identified");
				this.identified = true;
			}else if(idAtt != null){
				LOGGER.warning("Unknown value for attribute, ref=" + idAtt);
			}
		}
	}

	public void addSubproduct(AttributeName name, Object subProduct) {
		if (EXTENSION.equals(name) 
				|| RESTRICTION.equals(name)
				|| COMPLEXCONTENT.equals(name)
				|| ANNOTATION.equals(name)
				|| ATTRIBUTEGROUP.equals(name)) {
			// TODO
		} else if (ATTRIBUTE.equals(name)) {
			// TODO: handle attributes, at least id?
		} else if(SIMPLECONTENT.equals(name)){
			this.simpleContent = (AttributeType)subProduct;
			
		} else {
			// choice, sequence, or all
			descriptor = (Descriptor) subProduct;
		}
	}

}