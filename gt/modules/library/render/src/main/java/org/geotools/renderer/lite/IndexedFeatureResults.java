/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.renderer.lite;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureResults;
import org.geotools.data.store.DataFeatureCollection;
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
 * @source $URL$
 */
public final class IndexedFeatureResults extends DataFeatureCollection implements FeatureCollection {
	STRtree index = new STRtree();
	FeatureType schema;
	Envelope bounds;
	int count;
	private Envelope queryBounds;

	public IndexedFeatureResults(FeatureCollection results) throws IOException,
			IllegalAttributeException {
		// copy results attributes
		this.schema = results.getSchema();
		
				
		// load features into the index
		FeatureReader reader = null;
		bounds = new Envelope();
		count = 0;
		try {
			reader = results.reader();
			Feature f;
			Envelope env;
			while (reader.hasNext()) {
				f = reader.next();
				env = f.getDefaultGeometry().getEnvelopeInternal();
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
	public FeatureType getSchema() {
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
	 * @param queryBounds an Envelope defining the boundary of the query
	 * 
	 */
	public void setQueryBounds(Envelope queryBounds) {
		this.queryBounds = queryBounds;
	}
}
