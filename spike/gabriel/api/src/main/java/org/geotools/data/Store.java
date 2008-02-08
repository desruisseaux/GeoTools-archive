/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.data;

import java.io.IOException;
import java.util.Set;

import org.opengis.feature.Feature;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.identity.FeatureId;
import org.geotools.feature.FeatureCollection;

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
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/spike/gabriel/api/src/main/java/org/geotools/data/Store.java $
 * @version $Id$
 */
public interface Store<T extends FeatureType, F extends Feature, C extends FeatureCollection<T, F>>
        extends Source<T, F> {
    /**
     * Adds all features from the passed feature collection to the datasource.
     * 
     * @param collection
     *            The collection of features to add.
     * @return the FeatureIds of the newly added features.
     * 
     * @throws IOException
     *             if anything goes wrong.
     */
    Set<FeatureId> addFeatures(C collection) throws IOException;

    /**
     * Removes all of the features specificed by the passed filter from the
     * collection.
     * 
     * @param filter
     *            An OpenGIS filter; specifies which features to remove.
     * 
     * @throws IOException
     *             If anything goes wrong.
     */
    void removeFeatures(Filter filter) throws IOException;

    /**
     * Modifies the passed attribute types with the passed objects in all
     * features that correspond to the passed OGS filter.
     * 
     * @param type
     *            The attributes to modify.
     * @param value
     *            The values to put in the attribute types.
     * @param filter
     *            An OGC filter to note which attributes to modify.
     * 
     * @throws IOException
     *             if the attribute and object arrays are not eqaul length, if
     *             the object types do not match the attribute types, or if
     *             there are backend errors.
     */

    // void modifyFeatures(AttributeType[] type, Object[] value, Filter filter)
    // throws IOException;
    void modifyFeatures(AttributeDescriptor[] type, Object[] value, Filter filter)
            throws IOException;

    /**
     * Modifies the passed attribute types with the passed objects in all
     * features that correspond to the passed OGS filter. A convenience method
     * for single attribute modifications.
     * 
     * @param type
     *            The attributes to modify.
     * @param value
     *            The values to put in the attribute types.
     * @param filter
     *            An OGC filter to note which attributes to modify.
     * 
     * @throws IOException
     *             If modificaton is not supported, if the object type do not
     *             match the attribute type.
     */

    // void modifyFeatures(AttributeType type, Object value, Filter filter)
    // throws IOException;
    void modifyFeatures(AttributeDescriptor type, Object value, Filter filter) throws IOException;

    /**
     * Deletes the all the current Features of this datasource and adds the new
     * collection. Primarily used as a convenience method for file datasources.
     * 
     * @param reader -
     *            the collection to be written
     * 
     * @throws IOException
     *             if there are any datasource errors.
     */
    void setFeatures(Reader<T, F> reader) throws IOException;

    /**
     * Provides a transaction for commit/rollback control of this FeatureStore.
     * 
     * <p>
     * This method operates as a replacement for setAutoCommitMode. When a
     * transaction is provided you are no longer automatically committing.
     * </p>
     * 
     * <p>
     * In order to return to AutoCommit mode supply the Transaction.AUTO_COMMIT
     * to this method. Since this represents a return to AutoCommit mode the
     * previous Transaction will be commited.
     * </p>
     * 
     * @param transaction
     *            DOCUMENT ME!
     */
    void setTransaction(Transaction transaction);

    /**
     * Used to access the Transaction this DataSource is currently opperating
     * against.
     * 
     * <p>
     * Example Use: adding features to a road DataSource
     * </p>
     * 
     * <pre><code>
     * Transaction t = roads.getTransaction();
     * try {
     *     roads.addFeatures(features);
     *     roads.getTransaction().commit();
     * } catch (IOException erp) {
     *     //something went wrong;
     *     roads.getTransaction().rollback();
     * }
     * </code></pre>
     * 
     * @return Transaction in use, or <code>Transaction.AUTO_COMMIT</code>
     */
    Transaction getTransaction();

    /**
     * Low level API used to update content in place.
     * <p>
     * You can use getFeatureWriter( Query.ALL ) to visit all contents.
     * <p>
     * 
     * @param filter
     *            Used to select the features to update
     * @return Writer used to update content in place
     * @throws IOException
     */
    Writer<T, F> getFeatureWriter(Query query) throws IOException;

    //Writer<T,F> getFeatureWriter(Filter filter) throws IOException; //
    // perhaps only this?
    /**
     * Low level API used to insert additional content.
     * <p>
     * Please note that feature "order" is not fixed, internally features may be
     * stored a TreeSet, or a Spatial index, or usually located in a spatial
     * database.
     * 
     * @return
     * @throws IOException
     */
    //Writer<T, F> getFeatureWriterInsert() throws IOException;
}
