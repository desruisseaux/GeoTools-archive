package org.geotools.gml.schema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.util.AttributeName;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Hanldes the top level schema element.
 * <p>
 * As subproducts, accepts the products of {@linkplain #SchemaHelper}
 * </p>
 * <p>
 * Produces a List of AttributeType
 * </p>
 * 
 * @author gabriel
 * 
 */
class SchemaParserHelper extends AbstractParserHelper {
	private static Map/*<AttributeName, Class>*/ allowedChilds = new HashMap/*<AttributeName, Class>*/();

	static {
		allowedChilds.put(SCHEMA, SchemaHelper.class);
	}

	private List/*<AttributeType>*/ parsedTypes;

	protected Map/*<AttributeName, Class>*/ getHelpers() {
		return allowedChilds;
	}

	public Object getProduct(AttributeName name) {
		return parsedTypes;
	}

	protected void init(AttributeName name, Attributes attributes)
			throws SAXException {
		LOGGER.finer(this + " init " + name);
	}

	protected void startChild(AttributeName name, Attributes attributes)
			throws SAXException {
		if (!SCHEMA.equals(name)) {
			throw new IllegalArgumentException(name + ". Expected "
					+ SCHEMA);
		}
	}

	public void addSubproduct(AttributeName name, Object subProduct) {
		if (SCHEMA.equals(name)) {
			this.parsedTypes = (List/*<AttributeType>*/) subProduct;
		} else {
			throw new IllegalArgumentException(String.valueOf(name));
		}
	}

}