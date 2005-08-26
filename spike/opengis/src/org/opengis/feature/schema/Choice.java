package org.opengis.feature.schema;

import java.util.List;

public interface Choice extends Schema {
	List<Schema> choices();
}
