package org.geotools.data.collection;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.geotools.data.DataSourceException;
import org.geotools.data.FeatureReader;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;

/**
 * A FeatureReader that wraps up a normal FeatureIterator.
 * <p>
 * This class is useful for faking (and testing) the Resource based
 * API against in memory datastructures. You are warned that to
 * complete the illusion that Resource based IO is occuring content
 * will be duplicated.
 * </p>
 * @author Jody Garnett, Refractions Research, Inc.
 */
public class DelegateFeatureReader implements FeatureReader {
	FeatureIterator delegate;
	FeatureType schema;
	public DelegateFeatureReader( FeatureType featureType, FeatureIterator features ){
		this.schema = featureType;
		this.delegate = features;
	}
	
	public FeatureType getFeatureType() {
		return schema;
	}

	public Feature next() throws IOException, IllegalAttributeException, NoSuchElementException {
		if (delegate == null) {
            throw new IOException("Feature Reader has been closed");
        }		
        try {
        	return schema.duplicate( delegate.next() );    		
        } catch (NoSuchElementException end) {
            throw new DataSourceException("There are no more Features", end);
        }		
	}

	public boolean hasNext() throws IOException {
		return delegate != null && delegate.hasNext();			
	}

	public void close() throws IOException {
		if( delegate != null ) delegate.close();
		delegate = null;
        schema = null;
	}
	

}
