/*
 * Created on 26-Jun-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.geotools.data;

import java.io.IOException;
import java.util.List;

import org.geotools.feature.FeatureType;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;


final class DefaultFeatureView implements FeatureView {
	private final FeatureSource fs;

	public DefaultFeatureView(FeatureSource fs) {
		super();
		this.fs = fs;
	}

	public FeatureType getSchema() {
		return fs.getSchema();
	}

	public FeatureReader reader() throws IOException {
		return fs.getFeatures().reader();
	}

	public Envelope bounds() {
		Envelope bounds = null;
		try {
			bounds = fs.getBounds();
		}
		catch( IOException ignore ){}
		if( bounds != null ) return bounds;
		try {
			return fs.getFeatures().getBounds();
		} catch (IOException e) {
			return null;
		}
	}

	public int count() {
		int count = -1;
		try {
			count = fs.getCount( Query.ALL );
		}
		catch( IOException ignore ){}
		if( count != -1 ) return count;
		try {
			return fs.getFeatures().getCount();
		} catch (IOException e) {
			return -1;
		}
	}

	public FeatureView name(String typeName) {
		return null;
	}

	public FeatureView prefix(String prefix) {
		// TODO Auto-generated method stub
		return null;
	}

	public FeatureView cs(CoordinateReferenceSystem crs) {
		// TODO Auto-generated method stub
		return null;
	}

	public FeatureView reproject(CoordinateReferenceSystem crs) {
		// TODO Auto-generated method stub
		return null;
	}

	public FeatureView as(String[] attributes) {
		// TODO Auto-generated method stub
		return null;
	}

	public FeatureView as(String attribute, Expression expr) {
		// TODO Auto-generated method stub
		return null;
	}

	public FeatureView as(String attribute, String xpath) {
		// TODO Auto-generated method stub
		return null;
	}

	public FeatureView as(As[] as) {
		// TODO Auto-generated method stub
		return null;
	}

	public FeatureView as(List asList) {
		// TODO Auto-generated method stub
		return null;
	}

	public FeatureView filter(Filter filter) {
		// TODO Auto-generated method stub
		return null;
	}

	public FeatureView join(FeatureView view) {
		// TODO Auto-generated method stub
		return null;
	}

	public FeatureView join(FeatureView view, Filter expression) {
		// TODO Auto-generated method stub
		return null;
	}
}