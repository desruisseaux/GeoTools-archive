/**
 * 
 */
package org.opengis.feature.schema;

import java.util.Collection;

public interface ChoiceSchema extends Schema {
	public Collection<Schema> options();
}