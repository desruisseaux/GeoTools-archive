package org.geotools.data;

/**
 * @author dzwiers
 */
public interface Join extends Query{	
	/**
     * Used to return the set of inputs to the join.
     * 
     */
	Query[] getQueries();
}
