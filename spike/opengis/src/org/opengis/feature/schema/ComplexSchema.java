package org.opengis.feature.schema;

import java.util.List;

import org.opengis.feature.type.ComplexType;

public interface ComplexSchema extends Schema {
	ComplexType getType();
	List<Schema> schema();
}