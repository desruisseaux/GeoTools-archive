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
public class DefaultQueryResult extends ArrayList implements QueryResult {

	/* (non-Javadoc)
	 * @see org.geotools.catalog.QueryResult#metaDataIterator()
	 */
	public Iterator iterator() {
		return super.iterator();
	}

	/* (non-Javadoc)
	 * @see org.geotools.catalog.QueryResult#getNumEntries()
	 */
	public int getNumEntries() {
		return size();
	}

	/* (non-Javadoc)
	 * @see org.geotools.catalog.QueryResult#getEntry(int)
	 */
	public CatalogEntry getEntry(int index) {
		return (CatalogEntry) get(index);
	}

}
