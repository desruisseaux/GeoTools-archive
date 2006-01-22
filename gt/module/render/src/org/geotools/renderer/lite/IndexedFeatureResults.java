/*
 * Created on 7-gen-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.renderer.lite;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureResults;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * IndexedFeatureReader
 * 
 * @author wolf
 */
public class IndexedFeatureResults implements FeatureResults {
	STRtree index = new STRtree();
	FeatureType schema;
	Envelope bounds;
	int count;
	private Envelope queryBounds;

	public IndexedFeatureResults(FeatureResults results) throws IOException,
			IllegalAttributeException {
		// copy results attributes
		this.schema = results.getSchema();
		
				
		// load features into the index
		FeatureReader reader = null;
		bounds = new Envelope();
		count = 0;
		try {
			reader = results.reader();
			while (reader.hasNext()) {
				Feature f = reader.next();
				Envelope env = f.getDefaultGeometry().getEnvelopeInternal();
				bounds.expandToInclude(env);
				count++;
				index.insert(env, f);
			}
		} finally {
			if(reader != null)
				reader.close();
		}
	}

	/**
	 * @see org.geotools.data.FeatureResults#getSchema()
	 */
	public FeatureType getSchema() throws IOException {
		return this.schema;
	}

	/**
	 * @see org.geotools.data.FeatureResults#reader()
	 */
	public FeatureReader reader(Envelope envelope) throws IOException {
		List results = index.query(envelope);
		final Iterator resultsIterator = results.iterator();
		
		return new FeatureReader() {
			/**
			 * @see org.geotools.data.FeatureReader#getFeatureType()
			 */
			public FeatureType getFeatureType() {
				return schema;
			}

			/**
			 * @see org.geotools.data.FeatureReader#next()
			 */
			public Feature next() throws IOException,
					IllegalAttributeException, NoSuchElementException {
				return (Feature) resultsIterator.next();
			}

			/**
			 * @see org.geotools.data.FeatureReader#hasNext()
			 */
			public boolean hasNext() throws IOException {
				return resultsIterator.hasNext();
			}

			/**
			 * @see org.geotools.data.FeatureReader#close()
			 */
			public void close() throws IOException {
			}
		};
	}

	/**
	 * @see org.geotools.data.FeatureResults#getBounds()
	 */
	public Envelope getBounds() {
		return bounds;
	}

	/**
	 * @see org.geotools.data.FeatureResults#getCount()
	 */
	public int getCount() throws IOException {
		return count;
	}

	/**
	 * @see org.geotools.data.FeatureResults#collection()
	 */
	public FeatureCollection collection() throws IOException {
		FeatureCollection fc = FeatureCollections.newCollection();
		List results = index.query(bounds);
		for (Iterator it = results.iterator(); it.hasNext();) {
			fc.add(it.next());
		}
		return fc;
	}


	/**
	 * @see org.geotools.data.FeatureResults#reader()
	 */
	public FeatureReader reader() throws IOException {
		if(queryBounds != null)
			return reader(queryBounds);
		else
			return reader(bounds);
	}

	/**
	 * @param envelope
	 * @return
	 */
	public void setQueryBounds(Envelope queryBounds) {
		this.queryBounds = queryBounds;
	}
}