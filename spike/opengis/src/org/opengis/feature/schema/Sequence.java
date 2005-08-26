package org.opengis.feature.schema;

import java.util.List;

public interface Sequence extends Schema {
	List<Schema> sequence();
}
