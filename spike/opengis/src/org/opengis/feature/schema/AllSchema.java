/**
 * 
 */
package org.opengis.feature.schema;

import java.util.Collection;

public interface AllSchema extends Schema {
	public Collection<Schema> all();
}