package org.geotools.feature.schema;

import java.util.List;

import org.opengis.feature.schema.OrderedDescriptor;
import org.opengis.feature.schema.Descriptor;

public class OrderedImpl extends AbstractDescriptor implements OrderedDescriptor {
	List<Descriptor> sequence;
	public OrderedImpl( List<Descriptor> sequence ){
		this.sequence = sequence;
	}
	public OrderedImpl( List<Descriptor> sequence, int max ){
		super( max );
		this.sequence = sequence;
	}	
	public OrderedImpl( List<Descriptor> sequence, int min, int max ){
		super( min, max );
		this.sequence = sequence;
	}
	public List<Descriptor> sequence() {
		return sequence;
	}

}
