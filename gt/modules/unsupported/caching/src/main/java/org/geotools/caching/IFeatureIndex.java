package org.geotools.caching;

import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;


public interface IFeatureIndex extends FeatureSource {

	public abstract void add(Feature f) ;
	
	public abstract Feature get(String featureID) ;
	
	public abstract void remove(String featureID) ;
	
	public abstract void flush() ;
	
	public abstract void clear() ;
	
	public abstract FeatureCollection getFeatures(Query q) ;
	
	public abstract FeatureSource getView(Query q) ;
	
}
