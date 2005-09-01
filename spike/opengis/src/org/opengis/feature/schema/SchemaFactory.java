package org.opengis.feature.schema;

import java.util.List;

import org.opengis.feature.type.Type;


public interface SchemaFactory {
	public Node node( Type type, int min, int max );
	public Schema.All all( List<Schema> all, int min, int max );
	public Schema.Ordered ordered( List<Schema> sequence, int min, int max );
	public Schema.Choice choice( List<Schema> options, int min, int max );
}
