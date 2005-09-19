/**
 * 
 */
package org.opengis.feature.schema;

import java.util.List;

public interface OrderedSchema extends Schema {
	public List<Schema> sequence(); 
}