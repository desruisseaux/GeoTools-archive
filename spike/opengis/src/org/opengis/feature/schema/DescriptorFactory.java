package org.opengis.feature.schema;

import java.util.List;
import java.util.Set;

import org.opengis.feature.type.AttributeType;

public interface DescriptorFactory {
	public AttributeDescriptor node( AttributeType type, int min, int max );
	public AllDescriptor all( Set<AttributeDescriptor> all, int min, int max );
	public OrderedDescriptor ordered( List<Descriptor> sequence, int min, int max );
	public ChoiceDescriptor choice( Set<Descriptor> options, int min, int max );
}
