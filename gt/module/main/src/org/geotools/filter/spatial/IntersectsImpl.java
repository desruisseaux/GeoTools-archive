package org.geotools.filter.spatial;

import org.geotools.feature.Feature;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.GeometryFilterImpl;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.spatial.Intersects;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class IntersectsImpl extends GeometryFilterImpl implements Intersects {

	public IntersectsImpl(FilterFactory factory,Expression e1,Expression e2) {
		super(factory,e1,e2);
		
		// backwards compat with type system
		this.filterType = GEOMETRY_INTERSECTS;
	}
	
	public boolean evaluate(Feature feature) {
		if (!validate(feature))
			return false;
		
		Geometry left = getLeftGeometry(feature);
		Geometry right = getRightGeometry(feature);
		
		Envelope envLeft = left.getEnvelopeInternal();
		Envelope envRight = right.getEnvelopeInternal();
		
		if(envRight.intersects(envLeft))
            return left.intersects(right);
        else
            return false;
	}
	
	public Object accept(FilterVisitor visitor, Object extraData) {
		return visitor.visit(this,extraData);
	}

}
