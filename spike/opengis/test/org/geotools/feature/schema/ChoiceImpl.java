package org.geotools.feature.schema;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opengis.feature.Attribute;
import org.opengis.feature.schema.ChoiceDescriptor;
import org.opengis.feature.schema.Descriptor;

public class ChoiceImpl extends AbstractDescriptor implements ChoiceDescriptor {
	Set<Descriptor> options;

	public ChoiceImpl(Set<? extends Descriptor> options) {
		this.options = new HashSet<Descriptor>(options);
	}

	public ChoiceImpl(Set<? extends Descriptor> options, int max) {
		super(max);
		this.options = new HashSet<Descriptor>(options);
	}

	public ChoiceImpl(Set<? extends Descriptor> options, int min, int max) {
		super(min, max);
		this.options = new HashSet<Descriptor>(options);
	}

	public Set<Descriptor> options() {
		return options;
	}

	public int hashCode() {
		return super.hashCode() ^ (37 * options.hashCode());
	}

	public boolean equals(Object o) {
		if (!(o instanceof ChoiceImpl))
			return false;
		if (!super.equals(o))
			return false;

		ChoiceImpl d = (ChoiceImpl) o;
		return this.options.equals(d.options);
	}

	public void validate(List<Attribute> content) throws NullPointerException,
			IllegalArgumentException {

	}
}
