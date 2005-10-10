package org.geotools.feature.schema;

import java.util.List;

import org.opengis.feature.Attribute;
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
	
	public int hashCode(){
		return (37 * minOccurs + 37 * maxOccurs ) ^ TYPE.hashCode();
	}
	
	public boolean equals(Object o){
		if(!(o instanceof NodeImpl))
			return false;
		if(!super.equals(o))
			return false;
		
		NodeImpl d = (NodeImpl)o;
		return this.TYPE.equals(d.TYPE);
	}	
	
	public void validate(List<Attribute> content) throws NullPointerException,
	IllegalArgumentException {
		//no-op
	}
}
