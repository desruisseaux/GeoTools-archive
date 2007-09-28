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

import java.awt.RenderingHints;
import java.io.IOException;
import java.util.Set;

import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Envelope;


/**
 * Highlevel API for Features from a specific location.
 *
 * <p>
 * Individual Shapefiles, databases tables , etc. are referenced through this
 * interface. Compare and constrast with DataStore.
 * </p>
 *
 * <p>
 * Differences from DataStore:
 * </p>
 *
 * <ul>
 * <li>
 * This is a prototype DataSource replacement, the Transaction methods have
 * been moved to an external object, and the locking api has been intergrated.
 * </li>
 * <li>
 * FeatureCollection has been replaced with FeatureResult as we do not wish to
 * indicate that results can be stored in memory.
 * </li>
 * <li>
 * FeatureSource has been split into three interfaces, the intention is to use
 * the instanceof opperator to check capabilities rather than the previous
 * DataSourceMetaData.
 * </li>
 * </ul>
 *
 *
 * @author Jody Garnett
 * @author Ray Gallagher
 * @author Rob Hranac, TOPP
 * @author Chris Holmes, TOPP
 * @source $URL$
 * @version $Id$
 */
public interface FeatureSource {
    /**
     * Access to the DataStore implementing this FeatureStore.
     *
     * <p>
     * You may use this to access such as <code>namespace</code> provided by
     * DataStore.
     * </p>
     *
     * @return DataStore implementing this FeatureStore
     */
    DataStore getDataStore();

    /**
     * Adds a listener to the list that's notified each time a change to the
     * FeatureStore occurs.
     *
     * @param listener FeatureListener
     */
    void addFeatureListener(FeatureListener listener);

    /**
     * Removes a listener from the list that's notified each time a change to
     * the FeatureStore occurs.
     *
     * @param listener FeatureListener
     */
    void removeFeatureListener(FeatureListener listener);

    /**
     * Loads features from the datasource into the returned FeatureResults,
     * based on the passed query.
     *
     * @param query a datasource query object.  It encapsulates requested
     *        information, such as typeName, maxFeatures and filter.
     *
     * @return Collection The collection to put the features into.
     *
     * @throws IOException For all data source errors.
     *
     * @see Query
     */
    FeatureCollection getFeatures(Query query) throws IOException;

    /**
     * Loads features from the datasource into the returned FeatureResults,
     * based on the passed filter.
     *
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     *        <tt>null</tt> is not allowed, use Filter.INCLUDE instead.
     *
     * @return Collection The collection to put the features into.
     *
     * @throws IOException For all data source errors.
     */
    FeatureCollection getFeatures(Filter filter) throws IOException;

    /**
     * Loads all features from the datasource into the return FeatureResults.
     *
     * <p>
     * Filter.INCLUDE can also be used to get all features.  Calling this function
     * is equivalent to using {@link Query#ALL}
     * </p>
     *
     * @return Collection The collection to put the features into.
     *
     * @throws IOException For all data source errors.
     */
    FeatureCollection getFeatures() throws IOException;

    /**
     * Retrieves the featureType that features extracted from this datasource
     * will be created with.
     *
     * <p>
     * The schema returned is the LCD supported by all available Features. In
     * the common case of shapfiles and database table this schema will match
     * that of every feature available. In the degenerate GML case this will
     * simply reflect the gml:AbstractFeatureType.
     * </p>
     *
     * @return the schema of features created by this datasource.
     *
     * @task REVISIT: Our current FeatureType model is not yet advanced enough
     *       to handle multiple featureTypes.  Should getSchema take a
     *       typeName now that a query takes a typeName, and thus DataSources
     *       can now support multiple types? Or just wait until we can
     *       programmatically make powerful enough schemas?
     * @task REVISIT: we could also just use DataStore to capture multi
     *       FeatureTypes?
     */
    //FeatureType getSchema();
    SimpleFeatureType getSchema();

    /**
     * Gets the bounding box of this datasource.
     *
     * <p>
     * With getBounds(Query) this becomes a convenience method for
     * getBounds(Query.ALL), that is the bounds for all features contained
     * here.
     * </p>
     *
     * <p>
     * If getBounds() returns <code>null</code> due to expense consider using
     * <code>getFeatures().getBounds()</code> as a an alternative.
     * </p>
     *
     * @return The bounding box of the datasource or null if unknown and too
     *         expensive for the method to calculate.
     *
     * @throws IOException if there are errors getting the bounding box.
     *
     * @task REVISIT: Do we need this or can we use getBounds( Query.ALL )?
     */
    ReferencedEnvelope getBounds() throws IOException;

    /**
     * Gets the bounding box of the features that would be returned by this
     * query.
     *
     * <p>
     * To retrieve the bounds of the DataSource please use <code>getBounds(
     * Query.ALL )</code>.
     * </p>
     *
     * <p>
     * This method is needed if we are to stream features to a gml out, since a
     * FeatureCollection must have a boundedBy element.
     * </p>
     *
     * <p>
     * If getBounds(Query) returns <code>null</code> due to expense consider
     * using <code>getFeatures(Query).getBounds()</code> as a an alternative.
     * </p>
     *
     * @param query Contains the Filter and MaxFeatures to find the bounds for.
     *
     * @return The bounding box of the datasource or null if unknown and too
     *         expensive for the method to calculate or any errors occur.
     *
     * @throws IOException DOCUMENT ME!
     */
    Envelope getBounds(Query query) throws IOException;

    /**
     * Gets the number of the features that would be returned by this query.
     *
     * <p></p>
     *
     * <p>
     * If getBounds(Query) returns <code>-1</code> due to expense consider
     * using <code>getFeatures(Query).getCount()</code> as a an alternative.
     * </p>
     *
     * @param query Contains the Filter and MaxFeatures to find the bounds for.
     *
     * @return The number of Features provided by the Query or <code>-1</code>
     *         if count is too expensive to calculate or any errors or occur.
     *
     * @throws IOException if there are errors getting the count
     */
    int getCount(Query query) throws IOException;

    /**
     * Returns the set of hints this {@link FeatureSource} is able to support.<p>
     * Hints are to be specified in the {@link Query}, for each data access where they
     * may be required.<br>
     * Depending on the actual value provide by the user, the {@link FeatureSource}
     * may decide not to honor the hint.
     * @return a set of {@link RenderingHints#Key} objects (eventually empty, never null).
     */
    public Set /*<RenderingHints.Key>*/ getSupportedHints();
}
