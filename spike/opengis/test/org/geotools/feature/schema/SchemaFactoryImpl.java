package org.geotools.feature.schema;

import java.util.List;

import org.opengis.feature.schema.Schema;
import org.opengis.feature.schema.SchemaFactory;
import org.opengis.feature.type.Type;

/**
 * Construct Schema.
 * 
 * @author Jody Garnett
 *
 */
public class SchemaFactoryImpl implements SchemaFactory {
	public NodeImpl node( Type type, int min, int max ){
		return new NodeImpl( type, min, max );
	}
	public AllImpl all( List<Schema> all, int min, int max ){
		return new AllImpl( all, min, max );
	}
	public OrderedImpl ordered( List<Schema> sequence, int min, int max ){
		return new OrderedImpl( sequence, min, max );
	}
	public ChoiceImpl choice( List<Schema> options, int min, int max ){
		return new ChoiceImpl( options, min, max );
	}
}
