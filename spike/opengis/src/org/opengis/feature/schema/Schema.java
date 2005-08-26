package org.opengis.feature.schema;

import java.util.List;

public interface Schema {
	int getMinOccurs();

	int getMaxOccurs();

	// boolean isNillable(); -- moved to Type for FlatFeature

	/**
	 * Provides restrictions (such as max string length).
	 * <p>
	 * By
	 * </p>
	 */
	List<Facet> facets();
}