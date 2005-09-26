package org.geotools.feature.schema;

import java.util.Collection;

import org.opengis.feature.schema.AllDescriptor;
import org.opengis.feature.schema.Descriptor;

public class AllImpl extends AbstractSchema implements AllDescriptor {
	Collection<Descriptor> all;
	public AllImpl( Collection<Descriptor> all ){
		this.all = all;
	}
	public AllImpl( Collection<Descriptor> all, int max ){
		super( max );
		this.all = all;
	}	
	public AllImpl( Collection<Descriptor> all, int min, int max ){
		super( min, max );
		this.all = all;
	}
	public Collection<Descriptor> all() {
		return all;
	}

}
