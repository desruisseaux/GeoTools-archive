package org.geotools.data.gml;

import java.io.IOException;

import org.geotools.data.store.AbstractFeatureSource2;
import org.geotools.data.store.ReprojectingFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class GMLFeatureSource extends AbstractFeatureSource2 {

	public GMLFeatureSource(GMLTypeEntry entry) {
		super(entry);
	}

	public FeatureCollection getFeatures() throws IOException {
		return new GMLFeatureCollection( (GMLTypeEntry) entry );
	}

	protected FeatureCollection reproject(FeatureCollection features, CoordinateReferenceSystem source, CoordinateReferenceSystem target) {
		ReprojectingFeatureCollection reprojected = 
			(ReprojectingFeatureCollection) super.reproject( features, source, target );
		reprojected.setTransformer( new GMLGeometryCoordinateSequenceTransformer() );
		return reprojected;
	}
	
}
