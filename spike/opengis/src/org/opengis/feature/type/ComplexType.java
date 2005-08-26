package org.opengis.feature.type;

import java.util.List;

import org.opengis.feature.schema.Schema;

public interface ComplexType extends Type {
	/**
	 * Allowable content, indicates containment not validation .
	 * <p>
	 * This information may be considered derrived from schema().
	 */
	List<Type> types();

	/** Access to validation constraints */
	List<Schema> schema();
}
