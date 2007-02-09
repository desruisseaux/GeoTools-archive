package org.geotools.data.h2;

import org.geotools.data.store.ContentEntry;
import org.opengis.feature.type.TypeName;

public class H2ContentEntry extends ContentEntry {

	protected H2ContentEntry(H2DataStore dataStore, TypeName typeName) {
		super(dataStore, typeName);
	}

}
