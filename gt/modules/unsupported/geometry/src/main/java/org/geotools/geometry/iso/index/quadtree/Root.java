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



package org.geotools.geometry.iso.index.quadtree;

import org.geotools.geometry.iso.topograph2D.Coordinate;
import org.geotools.geometry.iso.topograph2D.Envelope;
import org.geotools.geometry.iso.util.Assert;

/**
 * QuadRoot is the root of a single Quadtree. It is centred at the origin, and
 * does not have a defined extent.
 * 
 * @version 1.7.2
 */
public class Root extends NodeBase {

	// the singleton root quad is centred at the origin.
	private static final Coordinate origin = new Coordinate(0.0, 0.0);

	public Root() {
	}

	/**
	 * Insert an item into the quadtree this is the root of.
	 */
	public void insert(Envelope itemEnv, Object item) {
		int index = getSubnodeIndex(itemEnv, origin);
		// if index is -1, itemEnv must cross the X or Y axis.
		if (index == -1) {
			add(item);
			return;
		}
		/**
		 * the item must be contained in one quadrant, so insert it into the
		 * tree for that quadrant (which may not yet exist)
		 */
		Node node = subnode[index];
		/**
		 * If the subquad doesn't exist or this item is not contained in it,
		 * have to expand the tree upward to contain the item.
		 */

		if (node == null || !node.getEnvelope().contains(itemEnv)) {
			Node largerNode = Node.createExpanded(node, itemEnv);
			subnode[index] = largerNode;
		}
		/**
		 * At this point we have a subquad which exists and must contain
		 * contains the env for the item. Insert the item into the tree.
		 */
		insertContained(subnode[index], itemEnv, item);
		// System.out.println("depth = " + root.depth() + " size = " +
		// root.size());
		// System.out.println(" size = " + size());
	}

	/**
	 * insert an item which is known to be contained in the tree rooted at the
	 * given QuadNode root. Lower levels of the tree will be created if
	 * necessary to hold the item.
	 */
	private void insertContained(Node tree, Envelope itemEnv, Object item) {
		Assert.isTrue(tree.getEnvelope().contains(itemEnv));
		/**
		 * Do NOT create a new quad for zero-area envelopes - this would lead to
		 * infinite recursion. Instead, use a heuristic of simply returning the
		 * smallest existing quad containing the query
		 */
		boolean isZeroX = IntervalSize.isZeroWidth(itemEnv.getMinX(), itemEnv
				.getMaxX());
		boolean isZeroY = IntervalSize.isZeroWidth(itemEnv.getMinX(), itemEnv
				.getMaxX());
		NodeBase node;
		if (isZeroX || isZeroY)
			node = tree.find(itemEnv);
		else
			node = tree.getNode(itemEnv);
		node.add(item);
	}

	protected boolean isSearchMatch(Envelope searchEnv) {
		return true;
	}

}
