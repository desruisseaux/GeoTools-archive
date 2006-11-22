package org.geotools.data.gml;

import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.store.ActiveTypeEntry;
import org.geotools.feature.FeatureType;

public class GMLTypeEntry extends ActiveTypeEntry {

	public GMLTypeEntry(DataStore parent, FeatureType schema, Map metadata) {
		super(parent, schema, metadata);
	}
	
	GMLDataStore parent() {
		return (GMLDataStore) parent;
	}
	
	public FeatureSource createFeatureSource() {
		return new GMLFeatureSource( this );
	}
	
}
