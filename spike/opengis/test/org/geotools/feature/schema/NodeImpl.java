package org.geotools.feature.schema;

import org.opengis.feature.schema.Node;
import org.opengis.feature.type.Type;

public class NodeImpl extends AbstractSchema implements Node {
	final Type TYPE;
	public NodeImpl( Type type ){
		TYPE = type;
	}
	public NodeImpl( Type type, int max ){
		super( 0, max );
		TYPE = type;
	}	
	public NodeImpl( Type type, int min, int max ){
		super( min, max );
		TYPE = type;
	}
	public Type getType() {
		return TYPE;
	}
}
