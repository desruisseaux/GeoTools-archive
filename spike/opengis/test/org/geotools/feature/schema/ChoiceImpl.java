package org.geotools.feature.schema;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.opengis.feature.schema.AllDescriptor;
import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.schema.ChoiceDescriptor;
import org.opengis.feature.schema.Descriptor;

public class ChoiceImpl extends AbstractDescriptor implements ChoiceDescriptor {
	Set<Descriptor> options;

	public ChoiceImpl(Set<? extends Descriptor> options) {
		this(options, 1, 1);
	}

	public ChoiceImpl(Set<? extends Descriptor> options, int max) {
		this(options, 1, max);
	}

	public ChoiceImpl(Set<? extends Descriptor> options, int min, int max) {
		super(min, max);
		Set<Descriptor> tmpOpts = new HashSet<Descriptor>(options);
		Set<QName> attNames = new HashSet<QName>();
		for (Descriptor schema : tmpOpts) {
			if (schema instanceof AttributeDescriptor) {
				AttributeDescriptor node = (AttributeDescriptor) schema;
				if (attNames.contains(node.getType().getName())) {
					throw new IllegalArgumentException(
							"Duplicated attribute names not allowed. "
									+ node.getType().getName()
									+ " found more than one time");
				}
				attNames.add(node.getType().getName());
			}
			if(schema instanceof AllDescriptor){
				throw new IllegalArgumentException(
						"AllDescriptors can't be options of a choice. Found " + schema);
			}
		}
		this.options = Collections.unmodifiableSet(tmpOpts);
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

}
