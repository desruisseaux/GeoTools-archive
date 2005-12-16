/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.data;

import java.io.IOException;
import java.util.Set;

import org.geotools.feature.AttributeType;
import org.geotools.filter.Filter;


/**
 * Provides storage of data for Features.
 * 
 * <p>
 * Individual shapefiles, database tables, etc. are modified through this
 * interface.
 * </p>
 * 
 * <p>
 * This is a prototype DataSource replacement please see FeatureSource for more
 * information.
 * </p>
 *
 * @author Jody Garnett
 * @author Ray Gallagher
 * @author Rob Hranac, TOPP
 * @author Chris Holmes, TOPP
 * @version $Id: FeatureStore.java,v 1.2 2003/12/01 22:00:47 cholmesny Exp $
 */
public interface FeatureStore extends FeatureSource {
    /**
     * Adds all features from the passed feature collection to the datasource.
     *
     * @param reader The reader from which to add the features.
     *
     * @return the FeatureIds of the newly added features.
     *
     * @throws IOException if anything goes wrong.
     */
    Set addFeatures(FeatureReader reader) throws IOException;

    /**
     * Removes all of the features specificed by the passed filter from the
     * collection.
     *
     * @param filter An OpenGIS filter; specifies which features to remove.
     *
     * @throws IOException If anything goes wrong.
     */
    void removeFeatures(Filter filter) throws IOException;

    /**
     * Modifies the passed attribute types with the passed objects in all
     * features that correspond to the passed OGS filter.
     *
     * @param type The attributes to modify.
     * @param value The values to put in the attribute types.
     * @param filter An OGC filter to note which attributes to modify.
     *
     * @throws IOException if the attribute and object arrays are not eqaul
     *         length, if the object types do not match the attribute types,
     *         or if there are backend errors.
     */
    void modifyFeatures(AttributeType[] type, Object[] value, Filter filter)
        throws IOException;

    /**
     * Modifies the passed attribute types with the passed objects in all
     * features that correspond to the passed OGS filter.  A convenience
     * method for single attribute modifications.
     *
     * @param type The attributes to modify.
     * @param value The values to put in the attribute types.
     * @param filter An OGC filter to note which attributes to modify.
     *
     * @throws IOException If modificaton is not supported, if the object type
     *         do not match the attribute type.
     */
    void modifyFeatures(AttributeType type, Object value, Filter filter)
        throws IOException;

    /**
     * Deletes the all the current Features of this datasource and adds the new
     * collection.  Primarily used as a convenience method for file
     * datasources.
     *
     * @param reader - the collection to be written
     *
     * @throws IOException if there are any datasource errors.
     */
    void setFeatures(FeatureReader reader) throws IOException;

    /**
     * Provides a transaction for commit/rollback control of this FeatureStore.
     * 
     * <p>
     * This method operates as a replacement for setAutoCommitMode.  When a
     * transaction is provided you are no longer automatically committing.
     * </p>
     * 
     * <p>
     * In order to return to AutoCommit mode supply the Transaction.AUTO_COMMIT
     * to this method. Since this represents a return to AutoCommit mode the
     * previous Transaction will be commited.
     * </p>
     *
     * @param transaction DOCUMENT ME!
     */
    void setTransaction(Transaction transaction);

    /**
     * Used to access the Transaction this DataSource is currently opperating
     * against.
     * 
     * <p>
     * Example Use: adding features to a road DataSource
     * </p>
     * <pre><code>
     * Transaction t = roads.getTransaction();
     * try{
     *     roads.addFeatures( features );
     *     roads.getTransaction().commit();
     * }
     * catch( IOException erp ){
     *     //something went wrong;
     *     roads.getTransaction().rollback();
     * }
     * </code></pre>
     *
     * @return Transaction in use, or <code>Transaction.AUTO_COMMIT</code>
     */
    Transaction getTransaction();
}
