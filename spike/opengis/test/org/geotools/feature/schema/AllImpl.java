package org.geotools.feature.schema;

import java.util.Collection;

import org.opengis.feature.schema.AllSchema;
import org.opengis.feature.schema.Schema;

public class AllImpl extends AbstractSchema implements AllSchema {
	Collection<Schema> all;
	public AllImpl( Collection<Schema> all ){
		this.all = all;
	}
	public AllImpl( Collection<Schema> all, int max ){
		super( max );
		this.all = all;
	}	
	public AllImpl( Collection<Schema> all, int min, int max ){
		super( min, max );
		this.all = all;
	}
	public Collection<Schema> all() {
		return all;
	}

}
