package org.opengis.feature.schema;

import org.opengis.feature.type.Type;


/**
 * Indicating an entry for a perscribed Type.
 * <p>
 * Please note the associated type may itself be complex, that has no effect
 * on the order required by the schema being described now.
 * </p>
 * @author Jody Garnett
 */
public interface Node extends Schema {
	Type getType();
	
	/**
	 * Used to mark default Geometry.
	 * <p>
	 * Really the concept of default is an open ended of metadata that people
	 * would like to associate with Nodes in their schema.
	 * </p>
	 * We should consider either:
	 * <ul>
	 * <li>Node Markers: letting ComplexTypes (other then FeatureType) provide markers on nodes
	 *     that mean something to them.
	 * <li>Complex Markers: Force ComplexType to define its own hints to how it wants its contents
	 *     to be understood.
	 * </ul>
	 * It is really too bad that Attribute means "element" of a Complex, attribute woudl be a better
	 * term for these markers.
	 * <p>
	 * It is likely that the idea of a default Geometry is not worth the pain it causes to our model.
	 * FeatureType can always just provide it as a first class concern not derrived from node content.
	 */
	//interface Default extends Node {
	//}
}