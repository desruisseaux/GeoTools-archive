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
/*
 * Created on 7-apr-2004
 */
package org.geotools.data.jdbc;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureListenerManager;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.jdbc.fidmapper.FIDMapper;
import org.geotools.feature.DefaultFeatureType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;

import com.vividsolutions.jts.geom.Envelope;


/**
 * JDBCDataStore implementation of the FeatureWriter interface
 *
 * @author aaime
 * @source $URL$
 */
public class JDBCFeatureWriter implements FeatureWriter {
    /** The logger for the filter module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.data.jdbc");
    protected QueryData queryData;
    protected FeatureReader reader;
    protected Feature live; // current for FeatureWriter
    protected Feature current; // copy of live returned to user
    protected FeatureListenerManager listenerManager = new FeatureListenerManager();
    protected boolean closed;
    protected Object[] fidAttributes;

    public JDBCFeatureWriter(FeatureReader reader, QueryData queryData) {
        this.reader = reader;
        this.queryData = queryData;
    }

    /**
     * @see org.geotools.data.FeatureWriter#getFeatureType()
     */
    public FeatureType getFeatureType() {
        return reader.getFeatureType();
    }

    /**
     * @see org.geotools.data.FeatureWriter#next()
     */
    public Feature next() throws IOException {
        if (reader == null) {
            throw new IOException("FeatureWriter has been closed");
        }

        FeatureType featureType = getFeatureType();

        if (hasNext()) {
            try {
                live = reader.next();
                current = featureType.duplicate(live);
                LOGGER.finer("Calling next on writer");
            } catch (IllegalAttributeException e) {
                throw new DataSourceException("Unable to edit " + live.getID()
                    + " of " + featureType.getTypeName(), e);
            }
        } else {
            //			new content
            live = null;

            try {
                Feature temp = DataUtilities.template(featureType);

                /* Here we create a Feature with a Mutable FID.
                 * We use data utilities to create a default set of attributes
                 * for the feature and these are copied into the a new
                 * MutableFIDFeature.  Thsi can probably be improved later,
                 * there is also a dependency on DefaultFeatureType here since
                 * DefaultFeature depends on it and MutableFIDFeature extends default
                 * feature.  This may be an issue if someone reimplements the Feature
                 * interfaces.  It could address by providing a full implementation
                 * of Feature in MutableFIDFeature at a later date.
                 *
                 */
                current = new MutableFIDFeature((DefaultFeatureType) featureType,
                        temp.getAttributes(
                            new Object[temp.getNumberOfAttributes()]), null);

                if (useQueryDataForInsert()) {
                    queryData.startInsert();
                }
            } catch (IllegalAttributeException e) {
                throw new DataSourceException(
                    "Unable to add additional Features of "
                    + featureType.getTypeName(), e);
            } catch (SQLException e) {
                throw new DataSourceException("Unable to move to insert row. "
                    + e.getMessage(), e);
            }
        }

        return current;
    }

    /**
     * Returns true if QueryData is used to insert rows, false if some other
     * means is used
     *
     * @return
     */
    protected boolean useQueryDataForInsert() {
        return true;
    }

    /**
     * @see org.geotools.data.FeatureWriter#remove()
     */
    public void remove() throws IOException {
        if (closed) {
            throw new IOException("FeatureWriter has been closed");
        }

        if (current == null) {
            throw new IOException("No feature available to remove");
        }

        if (live != null) {
            LOGGER.fine("Removing " + live);

            Envelope bounds = live.getBounds();
            live = null;
            current = null;

            try {
                queryData.deleteCurrentRow();
                listenerManager.fireFeaturesRemoved(getFeatureType()
                                                        .getTypeName(),
                    queryData.getTransaction(), bounds, false);
            } catch (SQLException sqle) {
                String message = "problem deleting row";

                if (queryData.getTransaction() != Transaction.AUTO_COMMIT) {
                    queryData.getTransaction().rollback();
                    message += "(transaction canceled)";
                }

                throw new DataSourceException(message, sqle);
            }
        } else {
            // cancel add new content
            current = null;
        }
    }

    /**
     * @see org.geotools.data.FeatureWriter#write()
     */
    public void write() throws IOException {
        if (closed) {
            throw new IOException("FeatureWriter has been closed");
        }

        if (current == null) {
            throw new IOException("No feature available to write");
        }

        LOGGER.fine("write called, live is " + live + " and cur is " + current);

        if (live != null) {
            if (live.equals(current)) {
                // no modifications made to current
                live = null;
                current = null;
            } else {
                try {
                    doUpdate(live, current);
                } catch (SQLException sqlException) {
                    queryData.close(sqlException);
                    throw new DataSourceException("Error updating row",
                        sqlException);
                }

                Envelope bounds = new Envelope();
                bounds.expandToInclude(live.getBounds());
                bounds.expandToInclude(current.getBounds());
                listenerManager.fireFeaturesChanged(getFeatureType()
                                                        .getTypeName(),
                    queryData.getTransaction(), bounds, false);
                live = null;
                current = null;
            }
        } else {
            LOGGER.fine("doing insert in jdbc featurewriter");

            try {
                doInsert((MutableFIDFeature) current);
            } catch (SQLException e) {
                throw new DataSourceException("Row adding failed.", e);
            }

            listenerManager.fireFeaturesAdded(getFeatureType().getTypeName(),
                queryData.getTransaction(), current.getBounds(), false);
            current = null;
        }
    }

    protected void doUpdate(Feature live, Feature current)
        throws IOException, SQLException {
        try {
            // Can we create for array getAttributes more efficiently?
            for (int i = 0; i < current.getNumberOfAttributes(); i++) {
                Object currAtt = current.getAttribute(i);
                Object liveAtt = live.getAttribute(i);

                if ((live == null)
                        || !DataUtilities.attributesEqual(currAtt, liveAtt)) {
                    if (LOGGER.isLoggable(Level.INFO)) {
                        LOGGER.info("modifying att# " + i + " to " + currAtt);
                    }

                    queryData.write(i, currAtt);
                }
            }
        } catch (IOException ioe) {
            String message = "problem modifying row";

            if (queryData.getTransaction() != Transaction.AUTO_COMMIT) {
                queryData.getTransaction().rollback();
                message += "(transaction canceled)";
            }

            throw ioe;
        }

        queryData.updateRow();
    }

    /**
     * Inserts a feature into the database.
     * 
     * <p>
     * This method should both insert a Feature, and update its FID in case the
     * FIDMapper works over database generated ids like  autoincrement fields,
     * sequences, and object ids.
     * </p>
     * 
     * <p>
     * Postgis needs to do this seperately.  With updates it can just override
     * the geometry stuff, using a direct sql update statement, but for
     * inserts it can't update a row that doesn't exist yet.
     * </p>
     *
     * @param mutable
     *
     * @throws IOException
     * @throws SQLException
     */
    protected void doInsert(MutableFIDFeature mutable)
        throws IOException, SQLException {
        queryData.startInsert();

        // primary key generation            
        FIDMapper mapper = queryData.getMapper();

        // read the new fid into the Feature 
        if ((mapper.getColumnCount() > 0)
                && !mapper.returnFIDColumnsAsAttributes()) {
            String ID = mapper.createID(queryData.getConnection(), mutable, null);
            fidAttributes = mapper.getPKAttributes(ID);

            if (fidAttributes != null) {
                mutable.setID(ID);

                for (int i = 0; i < fidAttributes.length; i++) {
                    Object fidAttribute = fidAttributes[i];

                    // if a column is of type auto increment, we should not update it
                    if (!mapper.isAutoIncrement(i)) {
                        queryData.writeFidColumn(i, fidAttribute);
                    }
                }
            }
        }

        // set up attributes and write row
        for (int i = 0; i < current.getNumberOfAttributes(); i++) {
            Object currAtt = current.getAttribute(i);

            queryData.write(i, currAtt);
        }

        queryData.doInsert();

        // should the ID be generated during an insert, we need to read it back
        // and set it into the feature
        if (((mapper.getColumnCount() > 0) && mapper.hasAutoIncrementColumns())) {
            fidAttributes = new Object[mapper.getColumnCount()];

            for (int i = 0; i < fidAttributes.length; i++) {
                fidAttributes[i] = queryData.readFidColumn(i);
            }

            mutable.setID(mapper.getID(fidAttributes));
        }
    }

    /**
     * @see org.geotools.data.FeatureWriter#hasNext()
     */
    public boolean hasNext() throws IOException {
        if (queryData.isClosed()) {
            throw new IOException("Feature writer is closed");
        }

        return reader.hasNext();
    }

    /**
     * @see org.geotools.data.FeatureWriter#close()
     */
    public void close() throws IOException {
        //changed this from throwing an exception if already closed to just
        //issuing a warning.  Mysql was having trouble with this, but I see
        //no great harm in not throwing an exception, since this will only
        //be in clean-up.
        if (queryData.isClosed()) {
            LOGGER.warning("Feature writer calling close when queryData is " +
                        " already closed");
        } else {
            reader.close();
	}
    }
}
