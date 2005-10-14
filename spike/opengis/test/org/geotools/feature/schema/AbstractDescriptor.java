package org.geotools.feature.schema;

import java.util.List;

import org.opengis.feature.Attribute;
import org.opengis.feature.schema.Descriptor;

public abstract class AbstractDescriptor implements Descriptor {
	protected int minOccurs;

	protected int maxOccurs;

	/**
	 * Creates a descriptor with default cardinality of 1..1
	 * 
	 */
	public AbstractDescriptor() {
		this(1, 1);
	}

	/**
	 * Creates a descriptor with cardinality 1..<code>max</code>
	 * 
	 * @param max
	 */
	public AbstractDescriptor(int max) {
		this(1, max);
	}

	public AbstractDescriptor(int min, int max) {
		if ((min < 0) || (max < 0) || (max < min))
			throw new IllegalArgumentException(
					"min("
							+ min
							+ ") and max("
							+ max
							+ ") must be positive integers and max must be greater than or equal to min");

		minOccurs = min;
		maxOccurs = max;
	}

	public int getMinOccurs() {
		return minOccurs;
	}

	public int getMaxOccurs() {
		return maxOccurs;
	}

	/**
	 * Calculates hashCode based on min and max occurs. Subclases may use it and
	 * extend with their own contents
	 */
	public int hashCode() {
		return 17 + (37 * minOccurs + 37 * maxOccurs);
	}

	public boolean equals(Object o) {
		if (!(o instanceof AbstractDescriptor))
			return false;
		AbstractDescriptor d = (AbstractDescriptor) o;
		return minOccurs == d.minOccurs && maxOccurs == d.maxOccurs;
	}

	public void validate(List<Attribute> content) throws NullPointerException,
			IllegalArgumentException {
		DescriptorValidator.validate(this, content);
	}
}
