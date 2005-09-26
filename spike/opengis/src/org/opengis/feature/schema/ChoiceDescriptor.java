/**
 * 
 */
package org.opengis.feature.schema;

import java.util.Collection;

public interface ChoiceDescriptor extends Descriptor {
	public Collection<Descriptor> options();
}