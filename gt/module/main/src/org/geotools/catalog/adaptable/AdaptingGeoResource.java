package org.geotools.catalog.adaptable;

import java.io.IOException;
import java.util.List;

import org.geotools.catalog.GeoResource;
import org.geotools.catalog.GeoResourceInfo;
import org.geotools.util.ProgressListener;

public class AdaptingGeoResource extends AdaptingResolve implements GeoResource {

	AdaptingGeoResource(GeoResource resolve, ResolveAdapterFactoryFinder finder) {
		super(resolve, finder);
	}

	public GeoResourceInfo getInfo(ProgressListener monitor) throws IOException {
		return resource().getInfo( monitor );
	}
	
	protected GeoResource resource() {
		return (GeoResource) resolve;
	}

}
