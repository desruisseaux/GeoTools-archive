package org.geotools.feature.schema;

import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;

public class NodeImpl extends AbstractDescriptor implements AttributeDescriptor {
	final AttributeType TYPE;

	public NodeImpl(AttributeType type) {
		this(type, 1, 1);
	}

	public NodeImpl(AttributeType type, int max) {
		this(type, 1, max);
	}

	public NodeImpl(AttributeType type, int min, int max) throws NullPointerException{
		super(min, max);
		if (type == null) {
			throw new NullPointerException();
		}
		TYPE = type;
	}

	public AttributeType getType() {
		return TYPE;
	}
}
