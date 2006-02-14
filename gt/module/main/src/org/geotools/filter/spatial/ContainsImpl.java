package org.geotools.filter.spatial;

import org.geotools.feature.Feature;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.GeometryFilterImpl;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.spatial.Contains;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class ContainsImpl extends GeometryFilterImpl implements Contains {

	public ContainsImpl(FilterFactory factory,Expression e1,Expression e2) {
		super(factory,e1,e2);
		
		this.filterType = GEOMETRY_CONTAINS;
	}
	
	public boolean evaluate(Feature feature) {
		if (!validate(feature))
			return false;
		
		Geometry left = getLeftGeometry(feature);
		Geometry right = getRightGeometry(feature);
		
		Envelope envLeft = left.getEnvelopeInternal();
		Envelope envRight = right.getEnvelopeInternal();
		
		if(envLeft.contains(envRight))
            return left.contains(right);
        
        return false;
	}
	
	public Object accept(FilterVisitor visitor, Object extraData) {
		return visitor.visit(this,extraData);
	}
}
