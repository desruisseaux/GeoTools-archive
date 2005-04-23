/*
 * Created on Apr 22, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.feature;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @author Jody Garnett
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FeatureImpl implements Feature {

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#getParent()
	 */
	public FeatureCollection getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#setParent(org.geotools.feature.FeatureCollection)
	 */
	public void setParent(FeatureCollection collection) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#getFeatureType()
	 */
	public FeatureType getFeatureType() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#getID()
	 */
	public String getID() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#getAttributes(java.lang.Object[])
	 */
	public Object[] getAttributes(Object[] attributes) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String xPath) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#getAttribute(int)
	 */
	public Object getAttribute(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#setAttribute(int, java.lang.Object)
	 */
	public void setAttribute(int position, Object val)
			throws IllegalAttributeException, ArrayIndexOutOfBoundsException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#getNumberOfAttributes()
	 */
	public int getNumberOfAttributes() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String xPath, Object attribute)
			throws IllegalAttributeException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#getDefaultGeometry()
	 */
	public Geometry getDefaultGeometry() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#setDefaultGeometry(com.vividsolutions.jts.geom.Geometry)
	 */
	public void setDefaultGeometry(Geometry geometry)
			throws IllegalAttributeException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#getBounds()
	 */
	public Envelope getBounds() {
		// TODO Auto-generated method stub
		return null;
	}

}
