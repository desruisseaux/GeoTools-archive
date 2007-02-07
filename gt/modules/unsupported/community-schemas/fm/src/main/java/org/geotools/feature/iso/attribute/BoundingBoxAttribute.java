package org.geotools.feature.attribute;

import org.geotools.feature.AttributeImpl;
import org.opengis.feature.type.AttributeType;
import org.opengis.spatialschema.geometry.BoundingBox;

public class BoundingBoxAttribute extends AttributeImpl implements
		org.opengis.feature.simple.BoundingBoxAttribute {

	public BoundingBoxAttribute(BoundingBox content, AttributeType type) {
		super(content,type,null);
	}
}
