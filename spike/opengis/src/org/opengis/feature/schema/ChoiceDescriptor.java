/**
 * 
 */
package org.opengis.feature.schema;

import java.util.Set;

public interface ChoiceDescriptor extends Descriptor {
	public Set<Descriptor> options();
}