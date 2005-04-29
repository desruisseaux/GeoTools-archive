package org.geotools.feature;

/**
 * Experiment land, these changes will be made to FeatureFactory interface unless any can
 * think of a reason against.
 * <p>
 * <ul>Q & A:
 * <li>Q: How not to break code?
 * <br>A: Move the FeatureFactory methods of create( Object[]) and create(Object[], String id) to FeatureType.
 *        The only code scene in the wild makes use of FeatureType.create( ... ) directly. We will have to
 *        stop FeatureType of extending FeatureFactory though (they *are* different after all).
 * <li>
 * </ul>
 * </p>
 * <p>
 * This method is expected to pass on any hints provided via a Map passed to the Constructor, in the event
 * a only a no argument constructor is found FeatureFactory will log a warning, in the Geotools 2.2 timeframe
 * this will be upgraded to an error.
 * </p>
 * @author Jody Garnett
 */
public interface FeatureFactory2 {
	/**
	 * AttributeType typesafe Feature creations.
	 * <p>
	 * This is option one, it works for both FeatureType and FlatFeatureType.
	 * Duplicate AttributeType are of course allowed (as long as the
	 * provided FeatureType supports this).
	 * </p>
	 * @param type List of AttributeType in creation order
	 * @param value List of attribute value in creation order
	 * @return Feature create according to the provided schema.
	 */
	Feature create( FeatureType schema, AttributeType type[], Object value[] );

	/**
	 * Convience method for FlatFeatureType for feature creation.
	 * <p>
	 * This is a convience method to ease the transition phase for developers
	 * making use of a FlatFeatureType.
	 * </p>
	 * <p>
	 * The use of this method should be considered to be *exactly* the same as:
	 * <pre><code>
	 * factory.create( flatSchema, flatSchema.getAttributeTypes(), values ); 
	 * </code></pre>
	 * A you can see this is only valid for FlatFeature schema in which the
	 * results of getAttributeTypes() describes all the parameters required
	 * for creation. For most shape and database base content this is a valid
	 * assumption.
	 * </p>
	 * @param type List of AttributeType in creation order
	 * @param value List of attribute value in creation order
	 * @return Feature create according to the provided schema.
	 */
	Feature create( FeatureType flatSchema, Object values[] );
	
}
