package org.geotools.data;

import java.net.URI;

/**
 * @author dzwiers
 */
public abstract class Join extends NewQuery{	
	/**
     * Used to return the set of inputs to the join.
     * 
     */
	public abstract NewQuery[] getQueries();

    /**
     * The new typeName
     */
    public abstract String getTypeName();

    /**
     * The new namespace.
     */
    public abstract URI getNamespace();
}
