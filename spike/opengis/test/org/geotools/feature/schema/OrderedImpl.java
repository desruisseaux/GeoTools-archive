package org.geotools.feature.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.feature.Descriptors;
import org.opengis.feature.Attribute;
import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.schema.ChoiceDescriptor;
import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.schema.OrderedDescriptor;
import org.opengis.feature.type.AttributeType;

import com.sun.javaws.security.SunSecurityUtil;

public class OrderedImpl extends AbstractDescriptor implements
		OrderedDescriptor {

	List<Descriptor> sequence;

	public OrderedImpl(List<Descriptor> sequence) {
		this(sequence, 1, 1);
	}

	public OrderedImpl(List<Descriptor> sequence, int max) {
		this(sequence, 1, max);
	}

	public OrderedImpl(List<Descriptor> sequence, int min, int max) {
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

	public List<Descriptor> sequence() {
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

	public void validate(List<Attribute> content) throws NullPointerException,
			IllegalArgumentException {
		if (content == null) {
			throw new NullPointerException("content");
		}

		// empty sequences are allowed, in such a case, content should be empty
		if (this.sequence.isEmpty()) {
			if (content.size() > 0) {
				throw new IllegalArgumentException(
						"Sequence is empty, content not allowed");
			}
			return;
		}
		SequenceValidator validator = new SequenceValidator();
		validator.validateSequence(this, content);
	}

	private static class SequenceValidator {

		public List<Attribute> processDescriptor(Descriptor schema,
				List<Attribute> content) {
			final int min = schema.getMinOccurs();
			final int max = schema.getMaxOccurs();
			if (schema instanceof AttributeDescriptor) {
				AttributeType type = ((AttributeDescriptor) schema).getType();
				return processType(content, type, min, max);
			} else if (schema instanceof ChoiceDescriptor) {
				Set<Descriptor> options = ((ChoiceDescriptor) schema).options();
				return processChoice(options, content, min, max);
			} else {
				List<? extends Descriptor> sequence = ((OrderedDescriptor) schema)
						.sequence();
				return processSequence(sequence, content, min, max);
			}
		}

		public List<Attribute> processSequence(
				List<? extends Descriptor> sequence, List<Attribute> content,
				int min, int max) {

			int count = 0;
			List<Attribute> remaining = content;

			while (true) {
				for (Descriptor desc : sequence) {
					remaining = processDescriptor(desc, remaining);
				}
				if(count < max){
					count++;
				}else{
					break;
				}
				if(count == max || remaining.size() == 0){
					break;
				}
			}

			return remaining;
		}

		private List<Attribute> processChoice(Set<Descriptor> allowableContent,
				List<Attribute> content, int min, int max) {
			if (min == 0 && max == 0) {
				return content;
			}
			int count = 0;

			for (Attribute att : content) {
				AttributeType type = att.getType();
				if (allowableContent.contains(type)) {
					if (count < max) {
						count++;
					} else {
						break;
					}
				} else {
					break;
				}
			}
			if (count < min) {
				throw new IllegalArgumentException("Expected at least " + min
						+ " occurrences of " + allowableContent);
			}

			if (count == 0)
				return content;

			return content.subList(count, content.size());
		}

		/**
		 * process a minimun of <code>min</code> and a maximun of
		 * <code>max</code> consecutive occurrencies of Attributes of type
		 * <code>expectedType</code> and return the remaining attributes. Only
		 * fails if first attribute is not of the expected type or minOccurs has
		 * not been satisfied. Never exceeds maxOccurs.
		 * 
		 * @param content
		 * @param expectedType
		 * @param min
		 * @param max
		 * @return
		 */
		private List<Attribute> processType(List<Attribute> content,
				AttributeType expectedType, int min, int max) {
			int count = 0;
			for (Attribute att : content) {
				AttributeType attType = att.getType();
				if (attType.equals(expectedType)) {
					count++;
					if (count == max) {
						break;
					}
				} else {
					break;
				}
			}
			if (count < min) {
				throw new IllegalArgumentException("got " + count
						+ " occurrences of " + expectedType.getName()
						+ ". Expected at least " + min);
			}
			if (count == 0)
				return content;
			return content.subList(count, content.size());
		}

		/**
		 * Validates the sequence of Attributes in <code>content</code> until
		 * the <code>schema.sequence()</code> gets exhausted, and returns the
		 * list of remaining attributes.
		 * 
		 * @param schema
		 * @param content
		 * @return
		 */
		public void validateSequence(OrderedDescriptor schema,
				List<Attribute> content) {

			final List<? extends Descriptor> descriptors = schema.sequence();
			final int min = schema.getMinOccurs();
			final int max = schema.getMaxOccurs();

			List<Attribute> remaining = processSequence(descriptors, content,
					min, max);

			if (remaining.size() > 0) {
				throw new IllegalArgumentException(
						"Extra content found beyond the specified in the schema: "
								+ remaining);
			}
		}
	}
}
