package org.opengis.feature;

public interface FeatureVisitor {
	public Object visit(Feature feature, Object extraData);
}
