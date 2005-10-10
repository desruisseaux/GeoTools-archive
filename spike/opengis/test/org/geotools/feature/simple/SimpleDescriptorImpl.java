package org.geotools.feature.simple;

import java.util.ArrayList;
import java.util.List;

import org.geotools.feature.schema.AbstractDescriptor;
import org.geotools.feature.schema.OrderedImpl;
import org.opengis.feature.Attribute;
import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.simple.SimpleDescriptor;
import org.opengis.feature.type.ComplexType;

/**
 * Content descriptor for simple Features limits content multiplicity to 1:1,
 * attribute types multiplicity to 0:1 or 1:1, and content to a sequence of non
 * complex types.
 * 
 * @since 2.2
 * @author Gabriel Roldan, Axios Engineering
 */
public class SimpleDescriptorImpl extends AbstractDescriptor implements
		SimpleDescriptor {
	List<AttributeDescriptor> sequence;

	public SimpleDescriptorImpl(List<AttributeDescriptor> sequence)
			throws IllegalArgumentException {
		this.sequence = new ArrayList<AttributeDescriptor>(sequence);

		for (AttributeDescriptor node : sequence) {
			if (node.getMinOccurs() > 1 || node.getMaxOccurs() > 1) {
				throw new IllegalArgumentException("Attribute "
						+ node.getType().getName() + " has multiplicity "
						+ node.getMinOccurs() + ":" + node.getMaxOccurs()
						+ " which is not allowed for Simple Features");
			}

			if (node.getType() instanceof ComplexType) {
				throw new IllegalArgumentException(
						"Attribute "
								+ node.getType().getName()
								+ " is complex, which is not allowed for Simple Features");
			}
		}
	}

	public int hashCode() {
		return super.hashCode() ^ (37 * sequence.hashCode());
	}

	public boolean equals(Object o) {
		if (!(o instanceof SimpleDescriptorImpl))
			return false;
		if (!super.equals(o))
			return false;

		SimpleDescriptorImpl d = (SimpleDescriptorImpl) o;
		return this.sequence.equals(d.sequence);
	}

	/**
	 * Provides a List<AttributeDescriptor> where each attribute Descriptor has
	 * multiplicity 1:1.
	 * <p>
	 * This is used to programatically indicate simple content.
	 */
	public List<AttributeDescriptor> sequence() {
		return sequence;
	}

	public void validate(List<Attribute> content) throws NullPointerException,
			IllegalArgumentException {
		// TODO: implement (same as ordered, or get this actually extend
		// ordered)
	}
}
