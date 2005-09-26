package org.opengis.feature.schema;

import java.util.List;

public interface SimpleDescriptor extends OrderedDescriptor {
	
	/**
	 * Provides a List<AttributeDescriptor> where each attribute Descriptor
	 * has multiplicity 1:1.
	 * <p>
	 * This is used to programatically indicate simple content.
	 */
	public List<Descriptor> sequence();

}
