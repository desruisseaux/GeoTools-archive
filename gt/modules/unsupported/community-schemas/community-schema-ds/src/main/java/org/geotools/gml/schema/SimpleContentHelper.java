package org.geotools.gml.schema;

import java.util.HashMap;
import java.util.Map;

import org.geotools.feature.type.SimpleTypes;
import org.geotools.util.AttributeName;
import org.opengis.feature.type.AttributeType;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Handles the contents of a restriction element for a simple type.
 * <p>
 * Handles itself the subproducts in order to not having to create a
 * AbstractParserHelper for each kind of facet.
 * </p>
 * <p>
 * Produces a Set&lt;Filte&gt; representing the facets of a simple type.
 * </p>
 * 
 * @author gabriel
 * 
 */
class SimpleContentHelper extends AbstractParserHelper {
	private static Map/*<AttributeName, Class>*/ allowedChilds = new HashMap/*<AttributeName, Class>*/();

	static {
		allowedChilds.put(EXTENSION, NULL_HELPER.class);
		allowedChilds.put(RESTRICTION, NULL_HELPER.class);
		allowedChilds.put(ANNOTATION, SimpleContentHelper.class);
		allowedChilds.put(new AttributeName(SimpleTypes.NSURI, "enumeration"),
				SimpleContentHelper.class);
		allowedChilds.put(new AttributeName(SimpleTypes.NSURI, "fractionDigits"),
				SimpleContentHelper.class);
		allowedChilds.put(new AttributeName(SimpleTypes.NSURI, "length"),
				SimpleContentHelper.class);
		allowedChilds.put(new AttributeName(SimpleTypes.NSURI, "minInclusive"),
				SimpleContentHelper.class);
		allowedChilds.put(new AttributeName(SimpleTypes.NSURI, "minExclusive"),
				SimpleContentHelper.class);
		allowedChilds.put(new AttributeName(SimpleTypes.NSURI, "maxInclusive"),
				SimpleContentHelper.class);
		allowedChilds.put(new AttributeName(SimpleTypes.NSURI, "maxExclusive"),
				SimpleContentHelper.class);
		allowedChilds.put(new AttributeName(SimpleTypes.NSURI, "maxLength"),
				SimpleContentHelper.class);
		allowedChilds.put(new AttributeName(SimpleTypes.NSURI, "minLength"),
				SimpleContentHelper.class);
		allowedChilds.put(new AttributeName(SimpleTypes.NSURI, "pattern"),
				SimpleContentHelper.class);
		allowedChilds.put(new AttributeName(SimpleTypes.NSURI, "totalDigits"),
				SimpleContentHelper.class);
		allowedChilds.put(new AttributeName(SimpleTypes.NSURI, "whiteSpace"),
				SimpleContentHelper.class);
		allowedChilds.put(new AttributeName(SimpleTypes.NSURI, ""),
				SimpleContentHelper.class);
	}

	private AttributeType attributeType;

	protected Map/*<AttributeName, Class>*/ getHelpers() {
		return allowedChilds;
	}

	public Object getProduct(AttributeName name) {
		AttributeType attributeType = this.attributeType;
		this.attributeType = null;
		return attributeType;
	}

	protected void init(AttributeName name, Attributes attributes)
			throws SAXException {
		LOGGER.finer(this + " init " + name);
	}

	protected void startChild(AttributeName name, Attributes attributes)
			throws SAXException {
		LOGGER.finer(this + " startChild " + name);
		if(EXTENSION.equals(name) || RESTRICTION.equals(name)){
			String base = attributes.getValue("base");
			if(base == null){
				throw new IllegalArgumentException("no base defined for " + name);
			}
			AttributeName baseName;
			if(base.indexOf(":") > 0){
				baseName = deglose(base);
			}else{
				String NS = SimpleTypes.NSURI;
				baseName = new AttributeName(NS, base);
			}
			this.attributeType = typeFactory.getType(baseName);
			LOGGER.finest("do not forget to implement " + name + " for " + this.attributeType);
		}
	}

	public void addSubproduct(AttributeName name, Object subProduct) {
	}

}