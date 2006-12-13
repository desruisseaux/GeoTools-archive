/*
 * This implementation of the OGC Feature Geometry Abstract Specification
 * (ISO 19107) is a project of the University of Applied Sciences Cologne
 * (Fachhochschule Köln) in collaboration with GeoTools and GeoAPI.
 *
 * Copyright (C) 2006 University of Applied Sciences Köln
 *                    (Fachhochschule Köln) and GeoTools
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * For more information, contact:
 *
 *     Prof. Dr. Jackson Roehrig
 *     Institut für Technologie in den Tropen
 *     Fachhochschule Köln
 *     Betzdorfer Strasse 2
 *     D-50679 Köln
 *     Jackson.Roehrig@fh-koeln.de
 *
 *     Sanjay Dominik Jena
 *     san.jena@gmail.com
 *
 */
/*
 * This class was copied from the JTS Topology Suite Version 1.7.2
 * of Vivid Solutions and modified and reused in this library under
 * the terms of GNU Lesser General Public Licence.
 * The original copyright of the Vivid Solutions JTS is stated as follows:
 *
 *------------------------------------------------------------------------
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 *------------------------------------------------------------------------
 */ 



package org.geotools.geometry.iso.index;

import java.util.List;

import org.geotools.geometry.iso.topograph2D.Envelope;

/**
 * The basic operations supported by classes implementing spatial index
 * algorithms.
 * <p>
 * A spatial index typically provides a primary filter for range rectangle
 * queries. A secondary filter is required to test for exact intersection. The
 * secondary filter may consist of other kinds of tests, such as testing other
 * spatial relationships.
 * 
 */
public interface SpatialIndex {
	/**
	 * Adds a spatial item with an extent specified by the given
	 * {@link Envelope} to the index
	 */
	void insert(Envelope itemEnv, Object item);

	/**
	 * Queries the index for all items whose extents intersect the given search
	 * {@link Envelope} Note that some kinds of indexes may also return objects
	 * which do not in fact intersect the query envelope.
	 * 
	 * @param searchEnv
	 *            the envelope to query for
	 * @return a list of the items found by the query
	 */
	List query(Envelope searchEnv);

	/**
	 * Queries the index for all items whose extents intersect the given search
	 * {@link Envelope}, and applies an {@link ItemVisitor} to them. Note that
	 * some kinds of indexes may also return objects which do not in fact
	 * intersect the query envelope.
	 * 
	 * @param searchEnv
	 *            the envelope to query for
	 * @param visitor
	 *            a visitor object to apply to the items found
	 */
	void query(Envelope searchEnv, ItemVisitor visitor);

	/**
	 * Removes a single item from the tree.
	 * 
	 * @param itemEnv
	 *            the Envelope of the item to remove
	 * @param item
	 *            the item to remove
	 * @return <code>true</code> if the item was found
	 */
	boolean remove(Envelope itemEnv, Object item);

}
