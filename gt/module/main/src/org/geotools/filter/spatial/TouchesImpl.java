package org.geotools.filter.spatial;

import org.geotools.feature.Feature;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.GeometryFilterImpl;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.spatial.Touches;

import com.vividsolutions.jts.geom.Geometry;

public class TouchesImpl extends GeometryFilterImpl implements Touches {

	public TouchesImpl(FilterFactory factory,Expression e1,Expression e2) {
		super(factory,e1,e2);
		
		// backwards compat with old type system
		this.filterType = GEOMETRY_TOUCHES;
	}
	
	public boolean evaluate(Feature feature) {
		if (!validate(feature))
			return false;
		
		Geometry left = getLeftGeometry(feature);
		Geometry right = getRightGeometry(feature);
		
		return left.touches(right);
	}
	
	public Object accept(FilterVisitor visitor, Object extraData) {
		return visitor.visit(this,extraData);
	}

}
