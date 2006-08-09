/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2002, Refractions Reserach Inc.
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
package org.geotools.graph.build.feature;

import org.geotools.feature.Feature;
import org.geotools.graph.build.GraphGenerator;
import org.geotools.graph.build.basic.BasicGraphGenerator;
import org.geotools.graph.structure.Graphable;

/**
 * Builds a graph from feature objects.
 * <p>
 * This graph generator decorates another graph generator which 
 * builds a graph from geometries. 
 * </p>
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class FeatureGraphGenerator extends BasicGraphGenerator {

	/**
	 * The underling "geometry" building graph generator
	 */
	GraphGenerator decorated;
	
	public FeatureGraphGenerator( GraphGenerator decorated ) {
		this.decorated = decorated;
	}
	
	public Graphable add( Object obj ) {
		Feature feature = (Feature) obj;
		Graphable g = decorated.add( feature.getDefaultGeometry() );
		g.setObject( feature );
	
		return g;
	}
	
	public Graphable remove( Object obj ) {
		Feature feature = (Feature) obj;
		return decorated.remove( feature.getDefaultGeometry() );
	}
	
	public Graphable get(Object obj) {
		Feature feature = (Feature) obj;
		return decorated.get( feature.getDefaultGeometry() );
	}
}
