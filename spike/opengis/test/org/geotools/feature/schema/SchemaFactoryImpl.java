package org.geotools.feature.schema;

import java.util.Collection;
import java.util.List;

import org.opengis.feature.schema.Schema;
import org.opengis.feature.schema.SchemaFactory;
import org.opengis.feature.type.AttributeType;

/**
 * Construct Schema.
 * 
 * @author Jody Garnett
 *
 */
public class SchemaFactoryImpl implements SchemaFactory {
	public NodeImpl node( AttributeType type, int min, int max ){
		return new NodeImpl( type, min, max );
	}
	public AllImpl all( Collection<Schema> all, int min, int max ){
		return new AllImpl( all, min, max );
	}
	public OrderedImpl ordered( List<Schema> sequence, int min, int max ){
		return new OrderedImpl( sequence, min, max );
	}
	public ChoiceImpl choice( Collection<Schema> options, int min, int max ){
		return new ChoiceImpl( options, min, max );
	}
}
