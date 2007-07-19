package org.geotools.caching;

import org.geotools.data.FeatureStore;
import org.geotools.feature.FeatureCollection;

import com.vividsolutions.jts.geom.Envelope;

public interface FeatureCache extends FeatureStore {

	public void clear() ;
	
	public void put(FeatureCollection fc, Envelope e) ;
	
	public FeatureCollection get(Envelope e) ;
	
	public FeatureCollection peek(Envelope e) ;
	
	public void remove(Envelope e) ;
	
}
