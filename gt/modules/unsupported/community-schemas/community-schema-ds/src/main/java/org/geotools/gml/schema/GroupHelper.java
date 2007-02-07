package org.geotools.gml.schema;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.geotools.util.AttributeName;
import org.opengis.feature.schema.Descriptor;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Handles the contents of a "group" element.
 * <p>
 * As subproducts accepts the contents of choice, all, and sequence.
 * </p>
 * <p>
 * Produces a singleton Map&gt;AttributeName,Descriptor&gt;, where the key
 * is the group name
 * </p>
 * 
 * @author gabriel
 * 
 */
class GroupHelper extends AbstractParserHelper {
	private static Map/*<AttributeName, Class>*/ allowedChilds = new HashMap/*<AttributeName, Class>*/();
	
	static {
		allowedChilds.put(ANNOTATION, NULL_HELPER.class);
		allowedChilds.put(ALL, DescriptorHelper.class);
		allowedChilds.put(CHOICE, DescriptorHelper.class);
		allowedChilds.put(SEQUENCE, DescriptorHelper.class);
	}

	AttributeName groupName;
	Descriptor descriptor;
	
	protected Map/*<AttributeName, Class>*/ getHelpers() {
		return allowedChilds;
	}

	public Object getProduct(AttributeName name) {
		if(groupName == null){
			throw new NullPointerException("groupName");
		}
		if(descriptor == null){
			throw new NullPointerException("descriptor");
		}
		return Collections.singletonMap(groupName, descriptor);
	}

	protected void init(AttributeName name, Attributes attributes)
			throws SAXException {
		String gname = attributes.getValue("name");
		this.groupName = deglose(gname);
	}

	protected void startChild(AttributeName name, Attributes attributes)
			throws SAXException {
	}

	public void addSubproduct(AttributeName name, Object subProduct) {
		if(ANNOTATION.equals(name)){
			//no-op
		}else{
			if(!(subProduct instanceof Descriptor)){
				throw new IllegalArgumentException("expected Descriptor, got " + subProduct);
			}
		}
		this.descriptor = (Descriptor)subProduct;
	}

}