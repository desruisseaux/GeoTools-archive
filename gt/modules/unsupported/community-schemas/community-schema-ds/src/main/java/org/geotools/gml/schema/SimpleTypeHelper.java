package org.geotools.gml.schema;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.geotools.feature.type.SimpleTypes;
import org.geotools.util.AttributeName;
import org.opengis.feature.type.AttributeType;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Handles the contents of a simpleType element.
 * <p>
 * As subproducts accepts the products of SimpleContentHelper.
 * </p>
 * <p>
 * Produces an AttributeType
 * </p>
 * 
 * @author gabriel
 * 
 */
class SimpleTypeHelper extends AbstractParserHelper {
	private static Map/*<AttributeName, Class>*/ allowedChilds = new HashMap/*<AttributeName, Class>*/();

	static {
		allowedChilds.put(ANNOTATION, NULL_HELPER.class);
		allowedChilds.put(UNION, NULL_HELPER.class);
		allowedChilds.put(LIST, NULL_HELPER.class);
		allowedChilds.put(RESTRICTION, SimpleContentHelper.class);
	}

	private AttributeName typeName;

	private AttributeType baseType;
	
	private String xsdTypeName;

	protected Map/*<AttributeName, Class>*/ getHelpers() {
		return allowedChilds;
	}

	/**
	 * TODO: improve the type mapping
	 */
	public Object getProduct(AttributeName name) {
		if(typeName == null){
			return null;
		}
		AttributeType type = null;
		//hack: this default values are due to the lack of
		//support for union, list, etc
		Class binding = String.class;
		boolean identified = false;
		Boolean nillable = Boolean.TRUE;
		Set restrictions = null;
		
		if(baseType == null){
			baseType = typeFactory.getDefault(String.class);
		}else{
			binding = baseType.getBinding();
			identified = baseType.isIdentified();
			nillable = baseType.isNillable();
			restrictions = baseType.getRestrictions();
		}
		type = typeFactory.createType(typeName, binding, identified, 
				nillable.booleanValue(), restrictions, baseType);
		return type;
	}

	protected void init(AttributeName name, Attributes attributes)
			throws SAXException {
		String typeName = attributes.getValue("name");
		if(typeName == null){
			throw new IllegalArgumentException("type name not found" + name);
		}
		this.typeName = deglose(typeName);
	}

	protected void startChild(AttributeName name, Attributes attributes)
			throws SAXException {
		if(RESTRICTION.equals(name)){
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
			this.baseType = typeFactory.getType(baseName);
		}
	}

	public void addSubproduct(AttributeName name, Object subProduct) {
		// TODO: add facets
	}

}