package org.geotools.catalog.postgis;

import org.geotools.catalog.DataStoreService;
import org.geotools.catalog.FeatureSourceGeoResource;

public class PostGISGeoResource extends FeatureSourceGeoResource {

	public PostGISGeoResource( DataStoreService parent, String name ) {
		super(parent, name);
	}

}
