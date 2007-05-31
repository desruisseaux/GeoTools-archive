package org.geotools.data.jdbc.collection;

import java.util.Iterator;

import org.geotools.feature.FeatureIterator;

public class DelegateIterator implements Iterator {
    FeatureIterator features;
    
    public DelegateIterator( FeatureIterator features ){
    	this.features = features;
    }
    public void close(){
    	if( features != null ){
    		features.close();
    		features = null;
    	}
    }
	public boolean hasNext() {
		return features != null && features.hasNext();
	}

	public Object next() {
		return features.next();
	}

	public void remove() {
		features.remove();
	}

}
