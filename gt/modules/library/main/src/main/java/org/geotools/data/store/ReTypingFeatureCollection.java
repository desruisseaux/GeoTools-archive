package org.geotools.data.store;

import java.io.IOException;
import java.util.Iterator;

import org.geotools.data.FeatureReader;
import org.geotools.data.collection.DelegateFeatureReader;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.collection.DecoratingFeatureCollection;
import org.geotools.feature.collection.DelegateFeatureIterator;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * FeatureCollection decorator which decorates a feature collection "re-typing" 
 * its schema based on attributes specified in a query.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class ReTypingFeatureCollection extends DecoratingFeatureCollection
	implements FeatureCollection {

	SimpleFeatureType featureType;
    
	public ReTypingFeatureCollection ( FeatureCollection delegate, SimpleFeatureType featureType ) {
		super(delegate);
		this.featureType = featureType;
	}
	
	public SimpleFeatureType getSchema() {
	    return featureType;
	}
	
	public FeatureReader reader() throws IOException {
		return new DelegateFeatureReader( getSchema(), features() );
	}
	
	public FeatureIterator features() {
		return new DelegateFeatureIterator( this, iterator() );
	}

	public void close(FeatureIterator close) {
		close.close();
	}

	public Iterator iterator() {
		return new ReTypingIterator( delegate.iterator(), delegate.getSchema(), featureType );
	}
	
	public void close(Iterator close) {
		ReTypingIterator reType = (ReTypingIterator) close;
		delegate.close( reType.getDelegate() );
	}
}
