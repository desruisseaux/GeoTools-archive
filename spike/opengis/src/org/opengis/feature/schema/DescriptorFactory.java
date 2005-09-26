package org.opengis.feature.schema;

import java.util.Collection;
import java.util.List;

import org.opengis.feature.type.AttributeType;

public interface DescriptorFactory {
	public AttributeDescriptor node( AttributeType type, int min, int max );
	public AllDescriptor all( Collection<Descriptor> all, int min, int max );
	public OrderedDescriptor ordered( List<Descriptor> sequence, int min, int max );
	public ChoiceDescriptor choice( Collection<Descriptor> options, int min, int max );
}
