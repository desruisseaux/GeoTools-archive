/*
 * Created on 14-Sep-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data;

import java.net.URI;

import org.geotools.filter.Expression;
import org.geotools.filter.Filter;

/**
 * @author dzwiers
 *
 */
public abstract class NewQuery {

    
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
     * The handle attribute is included to allow a client to associate  a
     * mnemonic name to the Query request. The purpose of the handle attribute
     * is to provide an error handling mechanism for locating  a statement
     * that might fail.
     *
     * @return the mnemonic name of the query request.
     */
    public abstract String getHandle();

    /**
     * From WFS Spec:  The version attribute is included in order to
     * accommodate systems that  support feature versioning. A value of ALL
     * indicates that all versions of a feature should be fetched. Otherwise
     * an integer, n, can be specified  to return the n th version of a
     * feature. The version numbers start at '1'  which is the oldest version.
     * If a version value larger than the largest version is specified then
     * the latest version is return. The default action shall be for the query
     * to return the latest version. Systems that do not support versioning
     * can ignore the parameter and return the only version  that they have.
     * 
     * <p>
     * This will not be used for awhile, but at some future point geotools
     * should support feature versioning.  Obviously none do now, nor are any
     * close to supporting it, so perhaps we should just wait and see.  And of
     * course we'd need the corresponding supportsFeatureVersioning in the
     * datasource metadata object.
     * </p>
     *
     * @return the version of the feature to return.
     */
    public abstract String getVersion();

    /**
     * aaime -- this is you hint spot :)
     */
    public abstract Object getCoordinateSystemReprojectHint();
    
    public static class QueryAs{
    	// assume the same namespace as the query ...
    	
    	// the output typename
    	private String typeName;
    	
    	// used to derive a property value ... this allows simple attribute mangling or selection ...
    	private Expression formula;
    	
    	private QueryAs(){}
    	public QueryAs(String typeName, Expression formula){
    		this.typeName = typeName;
    		this.formula = formula;
    	}
    	
		/**
		 * @return Returns the formula.
		 */
		public Expression getFormula() {
			return formula;
		}
		/**
		 * @return Returns the typeName.
		 */
		public String getTypeName() {
			return typeName;
		}
    }
}
