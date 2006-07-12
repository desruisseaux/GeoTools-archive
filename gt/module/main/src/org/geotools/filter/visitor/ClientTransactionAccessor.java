package org.geotools.filter.visitor;

import org.geotools.filter.Filter;

/**
 * Provides access to certain transaction state for the {@link PostPreProcessFilterSplittingVisitor}.  This is only required
 * if the transaction is kept on the client and the server is unaware of it.  For example PostGIS would not need to create
 * one.
 * 
 * @author Jesse
 */
public interface ClientTransactionAccessor {

	/**
	 * Returns all the filters indicating deleted feature ANDed together.  This is used to tell the server what features
	 * to NOT return.
	 * 
	 * @return all the filters indicating deleted feature ANDed together. 
	 */
	Filter getDeleteFilter();

	/**
	 * Returns all the filters of updates that affect the attribute in the expression ORed together.
	 * 
	 * @param attributePath the xpath identifier of the attribute.
	 * @return all the filters of updates that affect the attribute in the expression ORed together.
	 */
	Filter getUpdateFilter(String attributePath);

}