/**
 * 
 */
package org.opengis.feature.schema;

import java.util.Collection;

public interface AllDescriptor extends Descriptor {
	public Collection<Descriptor> all();
}