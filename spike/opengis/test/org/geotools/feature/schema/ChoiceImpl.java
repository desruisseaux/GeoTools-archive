package org.geotools.feature.schema;

import java.util.Collection;
import java.util.List;

import org.opengis.feature.schema.ChoiceSchema;
import org.opengis.feature.schema.Schema;

public class ChoiceImpl extends AbstractSchema implements ChoiceSchema {
	Collection<Schema> options;
	public ChoiceImpl( Collection<Schema> options ){
		this.options = options;
	}
	public ChoiceImpl( Collection<Schema> options, int max ){
		super( max );
		this.options = options;
	}	
	public ChoiceImpl( Collection<Schema> options, int min, int max ){
		super( min, max );
		this.options = options;
	}
	public Collection<Schema> options() {
		return options;
	}

}
