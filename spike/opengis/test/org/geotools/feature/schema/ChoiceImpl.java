package org.geotools.feature.schema;

import java.util.List;

import org.opengis.feature.schema.Schema;

public class ChoiceImpl extends AbstractSchema implements Schema.Choice {
	List<Schema> options;
	public ChoiceImpl( List<Schema> options ){
		this.options = options;
	}
	public ChoiceImpl( List<Schema> options, int max ){
		super( max );
		this.options = options;
	}	
	public ChoiceImpl( List<Schema> options, int min, int max ){
		super( min, max );
		this.options = options;
	}
	public List<Schema> options() {
		return options;
	}

}
