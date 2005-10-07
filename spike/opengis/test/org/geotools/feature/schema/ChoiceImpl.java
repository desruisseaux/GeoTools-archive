package org.geotools.feature.schema;

import java.util.HashSet;
import java.util.Set;

import org.opengis.feature.schema.ChoiceDescriptor;
import org.opengis.feature.schema.Descriptor;

public class ChoiceImpl extends AbstractDescriptor implements ChoiceDescriptor {
	Set<Descriptor> options;
	public ChoiceImpl( Set<Descriptor> options ){
		this.options = new HashSet<Descriptor>( options );
	}
	public ChoiceImpl( Set<Descriptor> options, int max ){
		super( max );
		this.options = new HashSet<Descriptor>( options );
	}	
	public ChoiceImpl( Set<Descriptor> options, int min, int max ){
		super( min, max );
		this.options = new HashSet<Descriptor>( options );
	}
	public Set<Descriptor> options() {
		return options;
	}

}
