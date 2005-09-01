package org.geotools.feature.schema;

import java.util.List;

import org.opengis.feature.schema.Schema;

public class AllImpl extends AbstractSchema implements Schema.All {
	List<Schema> all;
	public AllImpl( List<Schema> all ){
		this.all = all;
	}
	public AllImpl( List<Schema> all, int max ){
		super( max );
		this.all = all;
	}	
	public AllImpl( List<Schema> all, int min, int max ){
		super( min, max );
		this.all = all;
	}
	public List<Schema> all() {
		return all;
	}

}
