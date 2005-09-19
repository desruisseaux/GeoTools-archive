package org.opengis.feature.schema;

import java.util.Collection;
import java.util.List;

import org.opengis.feature.type.AttributeType;

public interface SchemaFactory {
	public AttributeDescriptor node( AttributeType type, int min, int max );
	public AllSchema all( Collection<Schema> all, int min, int max );
	public OrderedSchema ordered( List<Schema> sequence, int min, int max );
	public ChoiceSchema choice( Collection<Schema> options, int min, int max );
}
