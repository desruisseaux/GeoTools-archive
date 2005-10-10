package org.geotools.feature.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.feature.Descriptors;
import org.opengis.feature.Attribute;
import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.schema.OrderedDescriptor;
import org.opengis.feature.type.AttributeType;

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
	}

	public List<Descriptor> sequence() {
		return sequence;
	}

	public int hashCode(){
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
		int index = 0;
		List<AttributeType> allowedTypes = Descriptors.types(this);
		for (Attribute att : content) {
			checkAttIsNotNull(index, att);
			checkAttIsOfAllowedType(allowedTypes, index, att);
			index++;
		}
		//Map<AttributeType, Integer>occurrences = new HashMap<AttributeType, Integer>();
		
		index = 0;
		int occurrences = 0;
		for(Descriptor currentContent : this.sequence){
			Attribute att = content.get(index);

		}
	}
}
