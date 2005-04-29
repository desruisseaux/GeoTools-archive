//
// This is an exeriment!
//
package org.geotools.feature;


/**
 * Implementaton of *just* FeatureFactory for use by a FeatureType instance.
 * <p>
 * The only thing this class does is create features according to the provided
 * schema. Schemas can not change after being edited, so the fact that the feature
 * has a reference to its schema is not a problem.
 * </p>
 * <p>
 * <ul>Observations and Questions:
 * <li>This separation of schema from feature factory is the correct thing to do
 *     now that we have hints. This is needed to allow uDig to force DataStores to
 *     create a Feature instance that implements the Adaptable interface.
 * <li>With a bit of open source co-operation we could spit out JUMP or Degree
 *     features. I am getting tired of the java GIS lack of interoptability
 *     and last leg of the geotools design will let us do something about it.
 * <li>Q: How does FeatureCollection change things?
 * <br>A: I need to create a parent feature as a constructor
 * <li>
 * <li>Q: Can I use just the parent?
 * <br>A:It should be possible for parents that only allow
 *     a single child feature type. The parent Feature knows its Schema, the schema
 *       describes the type of the child schema it supports.
 * </ul>
 * </p>
 * <p>
 * <ul>Actual Decisions:
 * <li>Single Inheirentence - see discusion on the use of index that follows
 * <li>FeatureType attribute list is for that node in the type tree, it is not a complete
 *     list matching the provided objects used for construction.
 * <li>To get the complete list you will need start at the top and work down, taking
 *     overrides into account
 * <li>Where attribute type specifies multiplicity an array is provided for that slot
 *     int the creation array.
 * <li>Overrides are cumlative, any facets (Filter restrictions) are combined with the AND opperation
 * </ul>
 * </p>
 * <p>Notes on the use of Index with respect to type inheritence:</br>
 * Given the above actual decision we can provide a consistence "index" for each attribute type in a single
 * in heiritence feature type heirarchy. An index associated with a parent will indicate the location in the
 * create value list for information for itself and all child types.
 * </p>
 * <p>
 * Multiple inheiritence is not considered by this FeatureFactoryImpl, to do multiple inheritence cannot
 * be consistently defined using our existing API. However some things are clear for next time when we actualy
 * do set up multiple inheritence.  AttributeType overrides restrictions are still combined with AND, conflicts between
 * attributes from different parents are where the trouble is at.  The only answer is to kill the index idea, and
 * force everyone to use real AttributeTypes to look up their information (aka strongly typed index).
 * </p>
 * <p>
 * Recomendations:<br>
 * I recommend that we provide the AttributeType based lookup, and deprecate the index methods for the Geotools 2.1 timeframe.
 * This would allow us to provide type safty now, and allow us to to multiple inheirtience in the Geotools 2.2 timeframe
 * when the index methods are used. (This recomendation is based on a lost attempt at multiple inheirtence, one really cannot
 * produce a consistent implementation based on index).
 * </p>
 * @author Jody Garnett
 * @since 0.6.0
 */
public class FeatureFactoryImpl implements FeatureFactory2 {

	/* (non-Javadoc)
	 * @see org.geotools.feature.FeatureFactory2#create(org.geotools.feature.FeatureType, org.geotools.feature.AttributeType[], java.lang.Object[])
	 */
	public Feature create(FeatureType schema, AttributeType[] type, Object[] value) {
		return null; // implementation of FeatureImpl is required
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.FeatureFactory2#create(org.geotools.feature.FeatureType, java.lang.Object[])
	 */
	public Feature create(FeatureType flatSchema, Object[] values) {
		return null; // implementation of DefaultFeature would work fine!		
	}	
}
