/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.filter.spatial;

import org.geotools.filter.AttributeExpressionImpl;
import org.geotools.filter.AttributeExpressionImpl2;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.GeometryFilterImpl;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.spatial.BBOX;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class BBOXImpl extends GeometryFilterImpl implements BBOX {

	double minx,miny,maxx,maxy;	
	String srs;
	
	public BBOXImpl(FilterFactory factory,Expression e1,Expression e2) {
		super(factory,e1,e2);
		
		//backwards compat with old type system
		this.filterType = GEOMETRY_BBOX;
	}

	public String getPropertyName() {
        PropertyName propertyName = (PropertyName) getExpression1();
        return propertyName.getPropertyName();
	}

	public void setPropertyName(String propertyName) {
        setExpression1( new AttributeExpressionImpl( propertyName ));
	}
	
	public String getSRS() {
		return srs;
	}
	
	public void setSRS(String srs) {
		this.srs = srs;
	}

	public double getMinX() {
		return minx;
	}
	
	public void setMinX(double minx) {
		this.minx = minx;
	}

	public double getMinY() {
		return miny;
	}

	public void setMinY(double miny) {
		this.miny = miny;
	}
	
	public double getMaxX() {
		return maxx;
	}
	
	public void setMaxX(double maxx) {
		this.maxx = maxx;
	}

	public double getMaxY() {
		return maxy;
	}
	
	public void setMaxY(double maxy) {
		this.maxy = maxy;
	}
            
	public boolean evaluate(Object feature) {
		if (feature instanceof SimpleFeature && !validate((SimpleFeature)feature))
			return false;
		
		Geometry left = getLeftGeometry(feature);
		Geometry right = getRightGeometry(feature);
		
		Envelope envLeft = left.getEnvelopeInternal();
		Envelope envRight = right.getEnvelopeInternal();
		
		 if(envRight.contains(envLeft) || envLeft.contains(envRight)) {
             return true;
         } else if(envRight.intersects(envLeft)) {
             return left.intersects(right);
         } else {
             return false;
         }

         // Note that this is a pretty permissive logic
         //  if the type has somehow been mis-set (can't happen externally)
         //  then true is returned in all cases
	}
	
	public Object accept(FilterVisitor visitor, Object extraData) {
		return visitor.visit(this,extraData);
	}

}
