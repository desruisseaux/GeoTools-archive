package org.geotools.filter.spatial;

import org.geotools.feature.Feature;
import org.geotools.filter.CartesianDistanceFilter;
import org.geotools.filter.FilterFactory;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.spatial.Beyond;

import com.vividsolutions.jts.geom.Geometry;

public class BeyondImpl extends CartesianDistanceFilter implements Beyond {

	public BeyondImpl(FilterFactory factory,Expression e1,Expression e2) {
		super(factory,e1,e2);
		
		//backwards compat with old type system
		this.filterType = GEOMETRY_BEYOND;
	}
	
	public boolean evaluate(Feature feature) {
		if (!validate(feature))
			return false;
		
		Geometry left = getLeftGeometry(feature);
		Geometry right = getRightGeometry(feature);
		
		return !left.isWithinDistance(right, getDistance());
	}
	
	public Object accept(FilterVisitor visitor, Object extraData) {
		return visitor.visit(this,extraData);
	}
	
	 

}
