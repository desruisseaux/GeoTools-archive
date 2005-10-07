package org.geotools.feature.schema;

import java.util.ArrayList;
import java.util.List;

import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.schema.OrderedDescriptor;

public class OrderedImpl extends AbstractDescriptor implements
		OrderedDescriptor {

	List<Descriptor> sequence;

	public OrderedImpl(List<Descriptor> sequence) {
		this.sequence = new ArrayList<Descriptor>(sequence);
	}

	public OrderedImpl(List<Descriptor> sequence, int max) {
		super(max);
		this.sequence = new ArrayList<Descriptor>(sequence);
	}

	public OrderedImpl(List<Descriptor> sequence, int min, int max) {
		super(min, max);
		this.sequence = new ArrayList<Descriptor>(sequence);
	}

	public List<Descriptor> sequence() {
		return sequence;
	}

}
