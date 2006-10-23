/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.index.quadtree;

import java.io.IOException;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

import com.vividsolutions.jts.geom.Envelope;

/**
 * A collection that will open and close the QuadTree and find the next id in
 * the index.
 * 
 * @author Jesse
 * 
 */
public class LazySearchCollection extends AbstractCollection implements
		Collection {

	private QuadTree tree;

	private Envelope bounds;

	public LazySearchCollection(QuadTree tree, Envelope bounds) {
		this.tree = tree;
		this.bounds = bounds;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractCollection#iterator()
	 */
	public Iterator iterator() {
		LazySearchIterator object;
		try {
			object = new LazySearchIterator(tree.getRoot().copy(), tree.getIndexfile(), bounds);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		tree.registerIterator(object);
		return object;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractCollection#size()
	 */
	public int size() {
		return -1;
	}

}
