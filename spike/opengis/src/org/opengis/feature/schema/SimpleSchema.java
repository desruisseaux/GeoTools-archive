package org.opengis.feature.schema;

import org.opengis.feature.type.Type;

public interface SimpleSchema extends Schema {
	Type getType();
}