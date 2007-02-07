package org.geotools.gml.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.geotools.util.AttributeName;
import org.opengis.feature.schema.AllDescriptor;
import org.opengis.feature.schema.ChoiceDescriptor;
import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.schema.OrderedDescriptor;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Handles the contents of choice, all and sequence elements.
 * <p>
 * As subproducts accepts the products of choice, sequence, element.
 * </p>
 * <p>
 * Produces a Descriptor
 * </p>
 * 
 * @author gabriel
 * 
 */
class DescriptorHelper extends AbstractParserHelper {
	private static Map/*<AttributeName, Class>*/ allowedChilds = 
		new HashMap/*<AttributeName, Class>*/();

	static {
		allowedChilds.put(ANNOTATION, NULL_HELPER.class);
		allowedChilds.put(ANY, NULL_HELPER.class);
		allowedChilds.put(GROUP, NULL_HELPER.class);
		allowedChilds.put(ELEMENT, NodeHelper.class);
		allowedChilds.put(CHOICE, DescriptorHelper.class);
		allowedChilds.put(SEQUENCE, DescriptorHelper.class);
	}

	List/*<Descriptor>*/ subProducts;

	/**
	 * this descriptor type. One of choice, sequence, all
	 */
	AttributeName descriptorType;

	int min = 1, max = 1;

	protected Map/*<AttributeName, Class>*/ getHelpers() {
		return allowedChilds;
	}

	public Object getProduct(AttributeName name) {
		Descriptor descriptor = null;
		if (SEQUENCE.equals(descriptorType)) {
			descriptor = descFactory.ordered(subProducts, min, max);
		} else if (ELEMENT.equals(descriptorType)) {
			System.out.println(subProducts);

			// descriptor = descFactory.node(allContents, min, max);
		} else if (CHOICE.equals(descriptorType)) {
			descriptor = descFactory.choice(new HashSet/*<Descriptor>*/(
					subProducts), min, max);
		} else {
			throw new IllegalStateException("Unkown descriptor type: "
					+ descriptorType);
		}
		return descriptor;
	}

	protected void init(AttributeName name, Attributes attributes)
			throws SAXException {
		LOGGER.finer(this + " init " + name);
		descriptorType = name;
		min = minOccurs(attributes);
		max = maxOccurs(attributes);
		this.subProducts = new ArrayList/*<Descriptor>*/();
	}

	protected void startChild(AttributeName name, Attributes attributes)
			throws SAXException {
		if(GROUP.equals(name)){
			String gname = attributes.getValue("ref");
			if(gname == null){
				throw new NullPointerException("group reference not set");
			}
			AttributeName groupName = deglose(gname);
			if(!this.groups.containsKey(groupName)){
				throw new NoSuchElementException("group not found: " + groupName);
			}
			Descriptor descriptor = (Descriptor)groups.get(groupName);
			Collection groupElements;
			if(descriptor instanceof OrderedDescriptor){
				groupElements = ((OrderedDescriptor)descriptor).sequence();
			}else if(descriptor instanceof ChoiceDescriptor){
				groupElements =((ChoiceDescriptor)descriptor).options();
			}else if(descriptor instanceof AllDescriptor){
				groupElements =((AllDescriptor)descriptor).all();
			}else{
				throw new IllegalArgumentException("unkown descriptor: " + descriptor);
			}
			this.subProducts.addAll(groupElements);
		}
	}

	public void addSubproduct(AttributeName name, Object subProduct) {
		if(ELEMENT.equals(name) || CHOICE.equals(name) || SEQUENCE.equals(name) || ALL.equals(name)){
			this.subProducts.add((Descriptor) subProduct);
		}
	}

}