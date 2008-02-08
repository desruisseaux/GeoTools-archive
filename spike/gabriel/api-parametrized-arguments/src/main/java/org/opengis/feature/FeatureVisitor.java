package org.opengis.feature;

public interface FeatureVisitor<F extends Feature> {
	public Object visit(F feature, Object extraData);
}
