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
package org.geotools.jdbc;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.NoSuchElementException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.geotools.data.store.ContentFeatureIterator;


/**
 * Iterator which provides insert access to the underlying dataset.
 * <p>
 * When a call to {@link #next()} occurs, the feature returned is "live" in that
 * all attributes set are tracked, and written to the underlying database with
 * the next call to {@link next()}. Example:
 * <pre>
 *   <code>
 *   FeatureCollection collection = ...;
 *   FeatureIterator inserter = collection.inserter();
 *
 *   try {
 *         //grab a feature
 *         SimpleFeature feature = writer.next();
 *
 *         //set the values
 *         feature.setAttribute( 0, ... );
 *         feature.setAttribute( 1, ... );
 *
 *         //grab another feature (this will write the previous one )
 *         feature = writer.next();
 *
 *         //set more attributes
 *         ....
 *
 *   }
 *   finally {
 *     //always remember to close the iterator
 *     collection.close( writer );
 *   }
 *   </code>
 * </pre>
 * </p>
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class JDBCFeatureInserter extends JDBCFeatureIteratorSupport
    implements ContentFeatureIterator {
    ResultSetFeature last;

    public JDBCFeatureInserter(Statement st, SimpleFeatureType featureType,
        JDBCFeatureCollection collection) {
        super(st, featureType, collection);
    }

    public boolean hasNext() {
        //return true;
        return false;
    }

    public SimpleFeature next() throws NoSuchElementException {
        try {
            if (last == null) {
                last = new ResultSetFeature(rs);
            }

            //init, setting id to null explicity since the feature is yet to be 
            // inserted
            last.init(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return last;
    }

    public void remove() throws IOException {
        //noop
    }

    public void write() throws IOException {
        try {
            dataStore.insert(last, featureType, st.getConnection());
        } catch (SQLException e) {
            throw (IOException) new IOException().initCause(e);
        }
    }

    public void close() {
        //ensure last row is written
        //TODO: make this a more robust check
        next();
        super.close();
    }
}
