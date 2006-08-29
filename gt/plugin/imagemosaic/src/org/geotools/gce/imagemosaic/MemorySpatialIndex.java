/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2006, Geotools Project Managment Committee (PMC)
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.gce.imagemosaic;

import java.util.List;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * @author Simone Giannecchini
 * @since 2.3
 */
public final class MemorySpatialIndex {

	private final STRtree index;

	private final static PrecisionModel pm = new PrecisionModel();

	public MemorySpatialIndex(FeatureCollection features) {
		final FeatureIterator it = features.features();
		index = new com.vividsolutions.jts.index.strtree.STRtree();
		Feature f;
		Geometry g;
		while (it.hasNext()) {
			f = it.next();
			g = f.getDefaultGeometry();
			index.insert(g.getEnvelopeInternal(), f);
		}
		// force index construction --> STRTrees are build on first call to
		// query
		index.build();

	}

	public List findFeatures(Envelope envelope) {
		return index.query(envelope);

	}

	/**
	 * Builds a linearRing from the passed envelope
	 */
	private static LinearRing geometryFromEnvelope(Envelope env) {
		Coordinate[] ringCoords = new Coordinate[] {
				new Coordinate(env.getMinX(), env.getMinY()),
				new Coordinate(env.getMaxX(), env.getMinY()),
				new Coordinate(env.getMaxX(), env.getMaxY()),
				new Coordinate(env.getMinX(), env.getMaxY()),
				new Coordinate(env.getMinX(), env.getMinY()) };
		LinearRing ring = new LinearRing(ringCoords, pm, 0);

		return ring;
	}

}
