package org.geotools.feature.schema;

import java.util.Collection;
import java.util.List;

import org.opengis.feature.schema.ChoiceDescriptor;
import org.opengis.feature.schema.Descriptor;

public class ChoiceImpl extends AbstractSchema implements ChoiceDescriptor {
	Collection<Descriptor> options;
	public ChoiceImpl( Collection<Descriptor> options ){
		this.options = options;
	}
	public ChoiceImpl( Collection<Descriptor> options, int max ){
		super( max );
		this.options = options;
	}	
	public ChoiceImpl( Collection<Descriptor> options, int min, int max ){
		super( min, max );
		this.options = options;
	}
	public Collection<Descriptor> options() {
		return options;
	}

}
