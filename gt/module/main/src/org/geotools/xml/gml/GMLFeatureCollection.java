/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.xml.gml;

import org.geotools.feature.DefaultFeatureCollection;

import com.vividsolutions.jts.geom.Envelope;

/**
 * @author dzwiers
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 * @source $URL$
 */
public class GMLFeatureCollection extends DefaultFeatureCollection {
	private Envelope bounds;
    
	protected GMLFeatureCollection(String id, Envelope b){
		super(id,null);
		bounds = b;
	}
	/* (non-Javadoc)
	 * @see org.geotools.feature.FeatureCollection#getBounds()
	 */
	public Envelope getBounds() {
		return bounds;
	}
}
