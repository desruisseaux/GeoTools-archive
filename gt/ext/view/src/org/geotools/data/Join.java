/*
 * Created on 16-Sep-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data;

import java.net.URI;

/**
 * @author dzwiers
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class Join extends NewQuery{
	
	// used to return the set of inputs to the join
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
