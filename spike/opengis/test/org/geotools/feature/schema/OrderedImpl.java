package org.geotools.feature.schema;

import java.util.ArrayList;
import java.util.List;

import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.schema.ChoiceDescriptor;
import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.schema.OrderedDescriptor;

public class OrderedImpl extends AbstractDescriptor implements
		OrderedDescriptor {

	List<Descriptor> sequence;

	public OrderedImpl(List<Descriptor> sequence) {
		this(sequence, 1, 1);
	}

	public OrderedImpl(List<Descriptor> sequence, int max) {
		this(sequence, 1, max);
	}

	public OrderedImpl(List<? extends Descriptor> sequence, int min, int max) {
		super(min, max);
		this.sequence = new ArrayList<Descriptor>(sequence);
		for (Descriptor desc : this.sequence) {
			if (!((desc instanceof AttributeDescriptor)
					|| (desc instanceof OrderedDescriptor) || (desc instanceof ChoiceDescriptor))) {
				throw new IllegalArgumentException(
						"Ordered descriptor does not accepts " + desc);
			}
		}
	}

	public List<? extends Descriptor> sequence() {
		return sequence;
	}

	public int hashCode() {
		return super.hashCode() ^ (37 * sequence.hashCode());
	}

	public boolean equals(Object o) {
		if (!(o instanceof OrderedImpl))
			return false;
		if (!super.equals(o))
			return false;

		OrderedImpl d = (OrderedImpl) o;
		return this.sequence.equals(d.sequence);
	}

}
