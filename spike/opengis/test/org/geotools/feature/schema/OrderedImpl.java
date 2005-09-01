package org.geotools.feature.schema;

import java.util.List;

import org.opengis.feature.schema.Schema;

public class OrderedImpl extends AbstractSchema implements Schema.Ordered {
	List<Schema> sequence;
	public OrderedImpl( List<Schema> sequence ){
		this.sequence = sequence;
	}
	public OrderedImpl( List<Schema> sequence, int max ){
		super( max );
		this.sequence = sequence;
	}	
	public OrderedImpl( List<Schema> sequence, int min, int max ){
		super( min, max );
		this.sequence = sequence;
	}
	public List<Schema> sequence() {
		return sequence;
	}

}
