package org.geotools.data;

import java.net.URI;

import org.geotools.feature.AttributeType;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;

/**
 * @author dzwiers
 *
 */
public interface Query {
    
    /**
     * TODO: should this be ANY_URI
     */
	public static final URI NO_NAMESPACE = null;
    
    /**
     * So getMaxFeatures does not return null we use a very large number.
     * 
     */
    public static final int DEFAULT_MAX = -1;
    
    /**
     * Ask for no properties when used with setPropertyNames.
     * <p>
     * Note the query will still return a result - limited to FeatureIDs.
     * </p>
     */
    public static final QueryAs[] NO_ATTRIBUTES = new QueryAs[0];
    
    /**
     * Ask for all properties when used with setPropertyNames.
     */
    public static final QueryAs[] ALL_ATTRIBUTES = null;
    
    /**
     * The property mappings for this query.
     * <p>
     * Note: output name/value may be the result of an expression ...
     * 
     * @see QueryAs
     * </p>
     */
    QueryAs[] getProperties();

    /**
     * The optional maxFeatures can be used to limit the number of features
     * that a query request retrieves.  If no maxFeatures is specified then
     * all features should be returned.
     * 
     * <p>
     * This is the only method that is not directly out of the Query element in
     * the WFS spec.  It is instead a part of a GetFeature request, which can
     * hold one or more queries.  But each of those in turn will need a
     * maxFeatures, so it is needed here.
     * </p>
     *
     * @return the max features the getFeature call should return.
     */
    int getMaxFeatures();

    /**
     * A starting offset (taken from Catalog Specification)
     * <p>
     * This recomendation allows us to step through feature content in bite sized
     * chunks (well getMaxFeatures() sized chunks).
     * </p>
     * 
     * @return starting offset (counting from 0)
     */
    int getStartingOffset();
    
    /**
     * The Filter can be used to define constraints on a query.  If no Filter
     * is present then the query is unconstrained and all feature instances
     * should be retrieved.
     * <p>
     * TODO: This is a Filter as defined by the OGC Filter Specification 58XXXX
     * </p>
     * @return The filter that defines constraints on the query.
     */
    Filter getFilter();

    /**
     * The typeName attribute is used to indicate the name of the feature type
     * to be queried.  If no typename is specified, then the default typeName
     * should be returned from the dataStore.  If the datasstore only
     * supports one feature type then this part of the query may be ignored.
     * 
     * @return the name of the feature type to be returned with this query.
     */
    String getTypeName();

    /**
     * The namespace attribute is used to indicate the namespace of the
     * schema being represented. 
     * 
     * @return the gml namespace of the feature type to be returned with this query
     */
    URI getNamespace();

    /**
     * aaime -- this is you hint spot :)
     * <p>
     * interface magic extends wish {
     *    GeometryFactory geomFactory();
     *    CoordinateReferenceSystem force();
     *    CoordinateReferenceSystem reproject();
     * }
     * </p>
     */
    Object getCoordinateSystemReprojectHint();
    
    /**
     * Provides a mapping from from an expression to attribute.
     * <p>
     * (JG: This idea is the best! I really would like late binding though)
     * </p>
     * @author dzwiers
     */
    public static interface QueryAs{
		/**
		 * @return Returns the formula.
		 */
		Expression getFormula();
		
		/**
		 * @return Returns the attributeName.
		 */
		String getAttributeName();
		
		/**
		 * 
		 * @return The AttributeType
		 */
		AttributeType getAttributeType();
    }
}
