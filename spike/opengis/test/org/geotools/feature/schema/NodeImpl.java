package org.geotools.feature.schema;

import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;

public class NodeImpl extends AbstractSchema implements AttributeDescriptor {
	final AttributeType TYPE;
	public NodeImpl( AttributeType type ){
		TYPE = type;
	}
	public NodeImpl( AttributeType type, int max ){
		super( 0, max );
		TYPE = type;
	}	
	public NodeImpl( AttributeType type, int min, int max ){
		super( min, max );
		TYPE = type;
	}
	public AttributeType getType() {
		return TYPE;
	}
}
