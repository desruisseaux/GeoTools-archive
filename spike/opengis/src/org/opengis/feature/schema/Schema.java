package org.opengis.feature.schema;

import java.util.List;

/**
 * Schema captures the order of types withing a ComplexType.
 * <p>
 * Order is defined by a tree structure of the following:
 * <ul>
 * <li>Node - indicating a Type with multiplicity information
 * <li>Schema.Choice - indicating a choice between options
 * <lI>Schema.Sequence - indicating a perscribed order is required
 * <li>Schema.All - indicating an unordered set
 * </ul>
 * @author Jody Garnett
 *
 */
public interface Schema {
	public int getMinOccurs();
	public int getMaxOccurs();
	// restrictions on content have moved over to type for reuse

	public interface Ordered extends Schema {
		public List<Schema> sequence(); 
	}
	public interface Choice extends Schema {
		public List<Schema> options();
	}
	public interface All extends Schema {
		public List<Schema> all();
	}
}
