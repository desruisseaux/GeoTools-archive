/*
 * Created on Jun 28, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.catalog;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * @author jeichar
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class AbstractCatalog implements Catalog {

	protected ArrayList entries=new ArrayList();
	
	/* (non-Javadoc)
	 * @see org.opengis.catalog.Catalog#query(org.opengis.catalog.QueryDefinition)
	 */
	public QueryResult query(QueryDefinition arg0) {
		DefaultQueryResult result= new DefaultQueryResult();
		for (Iterator iter = iterator(); iter.hasNext();) {
			CatalogEntry element = (CatalogEntry) iter.next();
			if( arg0.accept(element) )
				result.add(element);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.opengis.catalog.Catalog#iterator()
	 */
	public Iterator iterator() {
		return entries.iterator();
	}

}
