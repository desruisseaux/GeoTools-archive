/*
 * Created on 16-Sep-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data;

import java.net.URI;

import org.geotools.data.NewQuery.QueryAs;
import org.geotools.filter.Filter;

/**
 * @author dzwiers
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class Join {
	
	// used to return the set of inputs to the join
	public abstract NewQuery[] getQueries();

    /**
     * TODO: should this be ANY_URI
     */
	public static final URI NO_NAMESPACE = null;
    
    /** So getMaxFeatures does not return null we use a very large number. */
    public static final int DEFAULT_MAX = Integer.MAX_VALUE;
    
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
     * The resulting property names from executing this query (note: output name/value may be the result of an expression ... @see QueryAs). 
     */
    public String[] getPropertyNames(){
    	QueryAs[] qa = getProperties();
    	if(qa == null)
    		return null;
    	String[] r = new String[qa.length];
    	for(int i=0;i<r.length;i++)
    		r[i] = qa[i]==null?null:qa[i].getTypeName();
    	return r;
    }
    
    /**
     * The property mappings for this query (note: output name/value may be the result of an expression ... @see QueryAs).
     */
    public abstract QueryAs[] getProperties();

    /**
     * Convenience method to determine if the query should use the full schema
     * (all properties) of the data source for the features returned.  This
     * method is equivalent to if (query.getProperties() == null), but allows
     * for more clarity on the part of datasource implementors, so they do not
     * need to examine and use null values.  All Query implementations should
     * return true for this function if getProperties returns null.
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
     * The Filter can be used to define constraints on a query.  If no Filter
     * is present then the query is unconstrained and all feature instances
     * should be retrieved.
     *
     * @return The filter that defines constraints on the query.
     */
    public abstract Filter getFilter();

    /**
     * The new typeName
     */
    public abstract String getTypeName();

    /**
     * The new namespace.
     */
    public abstract URI getNamespace();

    /**
     * aaime -- this is you hint spot :)
     */
    public abstract Object getCoordinateSystemReprojectHint();
}
