/*
 * Created on Jun 28, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.catalog;

import java.io.IOException;
import java.util.Iterator;

import org.geotools.metadata.Metadata;
import org.geotools.metadata.Query;

/**
 * @author jeichar
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DefaultQueryDefinition implements QueryDefinition {

	private Query query;
	
	public DefaultQueryDefinition( Query query ){
		this.query=query;
	}
	
	/* (non-Javadoc)
	 * @see org.geotools.catalog.QueryDefinition#evaluate(org.geotools.catalog.CatalogEntry)
	 */
	public boolean accept(CatalogEntry entry) {
		for (Iterator iter = entry.iterator(); iter.hasNext();) {
			Metadata element = (Metadata) iter.next();
			try{
				if( query.accepts(element) )
					return true;
			}catch(IOException ie){ 
				/* future versions may actually throw exceptions but the current version doesn't so
				* so check the method call: query.filter(fakeFeature) in the method Query.accepts()
				* (if it still exists)
				*/
				throw new RuntimeException("See the Query.accepts()::filter.filter(fakeFeature).  It is not expected to throw an exception...but maybe will one day.");
			}
		}
		return false;
	}

}
