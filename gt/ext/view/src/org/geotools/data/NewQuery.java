package org.geotools.data;

import java.net.URI;

import org.geotools.feature.FeatureType;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.IllegalFilterException;

/**
 * @author dzwiers
 *
 */
public abstract class NewQuery {
    
    /**
     * TODO: should this be ANY_URI
     * (JG:xml is in main so you could point to it)
     */
	public static final URI NO_NAMESPACE = null;
    
    /**
     * So getMaxFeatures does not return null we use a very large number.
     * 
     * TODO: The existing implementation does that - I would perfer to use -1 to prevent
     * any misunderstanding.
     */
    public static final int DEFAULT_MAX = Integer.MAX_VALUE;
    
    /**
     * Ask for no properties when used with setPropertyNames.
     * <p>
     * Note the query will still return a result - limited to FeatureIDs.
     * </p>
     * (JG: This looks like a great improvement)
     */
    public static final QueryAs[] NO_ATTRIBUTES = new QueryAs[0];
    
    /**
     * Ask for all properties when used with setPropertyNames.
     */
    public static final QueryAs[] ALL_ATTRIBUTES = null;

    /**
     * The resulting property names from executing this query (note: output name/value may be the result of an expression ... @see QueryAs).
     * <p>
     * Note this is a convience method - does not *have* to be here.
     * </p> 
     */
    public String[] getPropertyNames(){
    	QueryAs[] qa = getProperties();
    	if(qa == null)
    		return null;
    	String[] r = new String[qa.length];
    	for(int i=0;i<r.length;i++)
    		r[i] = qa[i]==null?null:qa[i].getAttributeName();
    	return r;
    }
    
    /**
     * The property mappings for this query.
     * <p>
     * Note: output name/value may be the result of an expression ...
     * 
     * @see QueryAs
     * </p>
     */
    public abstract QueryAs[] getProperties();

    /**
     * Convenience method to determine if the query should use the full schema
     * (all properties) of the data source for the features returned.
     * <p>
     * This method is equivalent to if (query.getProperties() == null), but allows
     * for more clarity on the part of datasource implementors, so they do not
     * need to examine and use null values.  All Query implementations should
     * return true for this function if getProperties returns null.
     *</p>
     *
     * @return if all datasource attributes should be included in the schema of
     *         the returned FeatureCollection.
     */
    public boolean retrieveAllProperties(){
    	return getProperties()==ALL_ATTRIBUTES;
    }

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
    public abstract int getMaxFeatures();

    /**
     * A starting offset (taken from Catalog Specification)
     * <p>
     * This recomendation allows us to step through feature content in bite sized
     * chunks (well getMaxFeatures() sized chunks).
     * </p>
     * 
     * @return starting offset (counting from 0)
     */
    public abstract int getStartingOffset();
    
    /**
     * The Filter can be used to define constraints on a query.  If no Filter
     * is present then the query is unconstrained and all feature instances
     * should be retrieved.
     * <p>
     * TODO: This is a Filter as defined by the OGC Filter Specification 58XXXX
     * </p>
     * @return The filter that defines constraints on the query.
     */
    public abstract Filter getFilter();

    /**
     * The typeName attribute is used to indicate the name of the feature type
     * to be queried.  If no typename is specified, then the default typeName
     * should be returned from the dataStore.  If the datasstore only
     * supports one feature type then this part of the query may be ignored.
     * 
     * @return the name of the feature type to be returned with this query.
     */
    public abstract String getTypeName();

    /**
     * The namespace attribute is used to indicate the namespace of the
     * schema being represented. 
     * 
     * @return the gml namespace of the feature type to be returned with this query
     */
    public abstract URI getNamespace();

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
    public abstract Object getCoordinateSystemReprojectHint();
    
    /**
     * Provides a mapping from from an expression to attribute.
     * <p>
     * (JG: This idea is the best! I really would like late binding though)
     * </p>
     * @author davidz
     */
    public static class QueryAs{
    	// assume the same namespace as the query ...
    	
    	// the output attributeName
    	private String attributeName;
    	
    	// used to derive a property value ... this allows simple attribute mangling or selection ...    	
    	private Expression formula;
    	
    	private QueryAs(){}
    	
    	public QueryAs(String attributeName, Expression formula){
    		this.attributeName = attributeName;
    		this.formula = formula;
    	}
    	
    	/**
    	 * Direct mapping of attribute with no changes 
    	 * @throws IllegalFilterException
    	 */
    	public QueryAs(FeatureType schema, String attribute ) throws IllegalFilterException{
    		this.attributeName = attribute;
    		FilterFactory factory = FilterFactory.createFilterFactory();
    		formula = factory.createAttributeExpression( schema, attribute );    		
    	}
    	
		/**
		 * @return Returns the formula.
		 */
		public Expression getFormula() {
			return formula;
		}
		/**
		 * @return Returns the attributeName.
		 */
		public String getAttributeName() {
			return attributeName;
		}
    }
}
