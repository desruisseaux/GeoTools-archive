package org.geotools.feature.schema;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.opengis.feature.schema.AllDescriptor;
import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.schema.DescriptorFactory;
import org.opengis.feature.type.AttributeType;

/**
 * Construct Descriptor.
 * 
 * @author Jody Garnett
 * 
 */
public class DescriptorFactoryImpl implements DescriptorFactory {
	public NodeImpl node(AttributeType type, int min, int max) {
		return new NodeImpl(type, min, max);
	}

	public AllImpl all(Set<AttributeDescriptor> all, int min, int max) {
		return new AllImpl(all, min, max);
	}

	public OrderedImpl ordered(List<Descriptor> sequence, int min, int max) {
		return new OrderedImpl(sequence, min, max);
	}

	public ChoiceImpl choice(Set<Descriptor> options, int min, int max) {
		return new ChoiceImpl(options, min, max);
	}
}
