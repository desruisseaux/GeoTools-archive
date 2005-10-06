/**
 * 
 */
package org.opengis.feature.schema;

import java.util.List;

public interface OrderedDescriptor extends Descriptor {
	public List<? extends Descriptor> sequence(); 
}