/*
 * Created on 6-Oct-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.xml.gml;

import org.geotools.feature.DefaultFeatureCollection;

import com.vividsolutions.jts.geom.Envelope;

/**
 * @author dzwiers
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GMLFeatureCollection extends DefaultFeatureCollection {

	private Envelope bounds;
	
	private GMLFeatureCollection(){}
	protected GMLFeatureCollection(Envelope b){
		bounds = b;
	}
	/* (non-Javadoc)
	 * @see org.geotools.feature.FeatureCollection#getBounds()
	 */
	public Envelope getBounds() {
		return bounds;
	}
}
