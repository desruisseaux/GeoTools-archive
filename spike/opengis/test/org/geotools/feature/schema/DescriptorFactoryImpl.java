package org.geotools.feature.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.geotools.feature.Descriptors;
import org.geotools.feature.simple.SimpleDescriptorImpl;
import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.schema.DescriptorFactory;
import org.opengis.feature.schema.OrderedDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;

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

	/**
	 * If simple content returns SimpleDescriptorImpl, else OrderedImpl
	 */
	public OrderedDescriptor  ordered(List<Descriptor> sequence, int min, int max) {
		if(min == 1 && max == 1){
			List<AttributeDescriptor>simpleSequence = new ArrayList<AttributeDescriptor>(sequence.size());
			for(Descriptor d : sequence){
				if(d.getMinOccurs() != 1 || d.getMaxOccurs() != 1 || !(d instanceof AttributeDescriptor)){
					break;
				}
				AttributeDescriptor ad = (AttributeDescriptor)d;
				if(ad.getType() instanceof ComplexType){
					break;
				}
				if(simpleSequence.contains(ad)){
					break;
				}
				simpleSequence.add(ad);
			}
			return new SimpleDescriptorImpl(simpleSequence);
		}
		return new OrderedImpl(sequence, min, max);
	}

	public ChoiceImpl choice(Set<? extends Descriptor> options, int min, int max) {
		return new ChoiceImpl(options, min, max);
	}
}
