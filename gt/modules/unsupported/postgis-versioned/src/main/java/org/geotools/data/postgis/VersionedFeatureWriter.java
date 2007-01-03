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
package org.geotools.data.postgis;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureListenerManager;
import org.geotools.data.FeatureLockException;
import org.geotools.data.FeatureWriter;
import org.geotools.data.jdbc.JDBCUtils;
import org.geotools.data.jdbc.MutableFIDFeature;
import org.geotools.data.postgis.fidmapper.VersionedFIDMapper;
import org.geotools.feature.AttributeType;
import org.geotools.feature.DefaultFeatureType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.geometry.jts.JTS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * A feature writer that handles versioning using two slave feature writers to expire old features
 * and create new revisions of the features
 * 
 * @author aaime
 * @since 2.4
 * 
 */
class VersionedFeatureWriter implements FeatureWriter {

    private static Long NON_EXPIRED = new Long(Long.MAX_VALUE);

    private FeatureWriter updateWriter;

    private FeatureWriter appendWriter;

    private FeatureType featureType;

    private Feature oldFeature;

    private Feature newFeature;

    private Feature liveFeature;

    private VersionedJdbcTransactionState state;

    private VersionedFIDMapper mapper;

    private FeatureListenerManager listenerManager;

    private boolean autoCommit;

    /**
     * Builds a new feature writer
     * 
     * @param updateWriter
     * @param appendWriter
     * @param featureType
     *            the outside visible feature type
     * @param mapper
     * @param autoCommit
     *            if true, the transaction need to be committed once the writer is closed
     */
    public VersionedFeatureWriter(FeatureWriter updateWriter, FeatureWriter appendWriter,
            FeatureType featureType, VersionedJdbcTransactionState state,
            VersionedFIDMapper mapper, boolean autoCommit) {
        this.updateWriter = updateWriter;
        this.appendWriter = appendWriter;
        this.featureType = featureType;
        this.state = state;
        this.mapper = mapper;
        this.autoCommit = autoCommit;
    }

    public void setFeatureListenerManager(FeatureListenerManager listenerManager) {
        this.listenerManager = listenerManager;
    }

    public void close() throws IOException {
        if (updateWriter != null)
            updateWriter.close();
        appendWriter.close();

        // double check, state.getTransaction() will return null if the transaction
        // has already been closed
        if (autoCommit && state.getTransaction() != null) {
            state.getTransaction().commit();
            state.getTransaction().close();
        }
    }

    public FeatureType getFeatureType() {
        return featureType;
    }

    public boolean hasNext() throws IOException {
        appendWriter.hasNext();
        if (updateWriter != null)
            return updateWriter.hasNext();
        else
            return false;
    }

    public Feature next() throws IOException {
        Feature original = null;
        if (updateWriter != null && updateWriter.hasNext()) {
            oldFeature = updateWriter.next();
            newFeature = appendWriter.next();
            original = oldFeature;
            state.expandDirtyBounds(getLatLonFeatureEnvelope(oldFeature));
        } else {
            oldFeature = null;
            newFeature = appendWriter.next();
            original = newFeature;
        }

        try {
            liveFeature = DataUtilities.reType(featureType, original);
            // if the feature it brand new, it'll have a random fid, not a
            // proper one, keep using
            // it, we cannot un-version it
            String unversionedId = liveFeature.getID();
            if (oldFeature != null)
                unversionedId = mapper.getUnversionedFid(liveFeature.getID());
            liveFeature = new MutableFIDFeature((DefaultFeatureType) featureType, liveFeature
                    .getAttributes(new Object[featureType.getAttributeCount()]), unversionedId);
            return liveFeature;
        } catch (IllegalAttributeException e) {
            throw new DataSourceException("Error casting versioned feature to external one. "
                    + "Should not happen, there's a bug at work", e);
        }
    }

    /**
     * Computes a feature's envelope, using all geometry attributes, and returns an envelop in WGS84
     * 
     * @param oldFeature
     * @return
     * @throws TransformException
     */
    public Envelope getLatLonFeatureEnvelope(Feature feature) throws IOException {
        try {
            Envelope result = new Envelope();
            FeatureType ft = feature.getFeatureType();
            for (int i = 0; i < ft.getAttributeCount(); i++) {
                AttributeType at = ft.getAttributeType(i);
                if (at instanceof GeometryAttributeType) {
                    GeometryAttributeType gat = (GeometryAttributeType) at;
                    CoordinateReferenceSystem crs = gat.getCoordinateSystem();

                    Geometry geom = (Geometry) feature.getAttribute(i);
                    if (geom != null) {
                        Envelope env = geom.getEnvelopeInternal();
                        if (crs != null)
                            env = JTS.toGeographic(env, crs);
                        result.expandToInclude(env);
                    }
                }
            }
            return result;
        } catch (TransformException e) {
            throw new DataSourceException(
                    "Error computing lat/long envelope of the current feature. "
                            + "This is needed to update the changeset bbox", e);
        }
    }

    public void remove() throws IOException {
        // if the feature is new, we have nothing to remove
        if (oldFeature == null) {
            throw new IOException("No feature available to remove");
        }

        listenerManager.fireFeaturesRemoved(getFeatureType().getTypeName(), state.getTransaction(),
                oldFeature.getBounds(), false);
        expireOldFeature();
    }

    private void expireOldFeature() throws IOException, DataSourceException {
        // else, we have to expire the old feature and not write a new revision
        // of it
        try {
            oldFeature.setAttribute("expired", new Long(state.getRevision()));
            updateWriter.write();
        } catch (IllegalAttributeException e) {
            throw new DataSourceException("Error writing expiration tag on old feature. "
                    + "Should not happen, there's a bug at work.", e);
        } catch (FeatureLockException fle) {
            // we have to mangle the id here too
            String unversionedFid = mapper.getUnversionedFid(fle.getFeatureID());
            FeatureLockException mangled = new FeatureLockException(fle.getMessage(),
                    unversionedFid, fle.getCause());
            throw mangled;
        }
    }

    public void write() throws IOException {
        Statement st = null;
        try {
            // ... copy attributes
            if (oldFeature != null) {
                // if there is an old feature, make sure to write a new revision only if the
                // feauture was modified
                boolean dirty = false;
                for (int i = 0; i < liveFeature.getNumberOfAttributes(); i++) {
                    AttributeType at = liveFeature.getFeatureType().getAttributeType(i);
                    Object newValue = liveFeature.getAttribute(at.getName());
                    Object oldValue = oldFeature.getAttribute(at.getName());
                    newFeature.setAttribute(at.getName(), newValue);
                    if (!DataUtilities.attributesEqual(newValue, oldValue)) {
                        dirty = true;
                    }
                }
                if (!dirty)
                    return;
                // if we have the old feature, just expire it
                expireOldFeature();
            } else {
                for (int i = 0; i < liveFeature.getNumberOfAttributes(); i++) {
                    AttributeType at = liveFeature.getFeatureType().getAttributeType(i);
                    newFeature.setAttribute(at.getName(), liveFeature.getAttribute(at.getName()));
                }
            }

            // set revision and expired,
            newFeature.setAttribute("expired", NON_EXPIRED);
            newFeature.setAttribute("revision", new Long(state.getRevision()));

            // ... set FID to the old one
            // TODO: check this, I'm not sure this is the proper handling
            String id = null;
            if (oldFeature != null) {
                id = liveFeature.getID().substring(featureType.getTypeName().length() + 1) + "&"
                        + state.getRevision();
            } else if (!mapper.hasAutoIncrementColumns()) {
                id = mapper.createID(state.getConnection(), newFeature, null);
            }
            // transfer generated id values to the primary key attributes
            if (id != null) {
                ((MutableFIDFeature) newFeature).setID(id);

                Object[] pkatts = mapper.getPKAttributes(id);
                for (int i = 0; i < pkatts.length; i++) {
                    newFeature.setAttribute(mapper.getColumnName(i), pkatts[i]);
                }
            }

            // ... write
            state.expandDirtyBounds(getLatLonFeatureEnvelope(newFeature));
            appendWriter.write();

            // if the id is auto-generated, gather it from the db
            if (oldFeature == null && mapper.hasAutoIncrementColumns()) {
                st = state.getConnection().createStatement();
                id = mapper.createID(state.getConnection(), newFeature, st);
            }

            // ... make sure the newly generated id is set into the live
            // feature, and that it's typed, too
            ((MutableFIDFeature) newFeature).setID(id);
            ((MutableFIDFeature) liveFeature).setID(mapper.getUnversionedFid(id));

            // ... and finally notify the user
            if (oldFeature != null) {
                Envelope bounds = oldFeature.getBounds();
                bounds.expandToInclude(newFeature.getBounds());
                listenerManager.fireFeaturesChanged(getFeatureType().getTypeName(), state
                        .getTransaction(), bounds, false);
            } else {
                listenerManager.fireFeaturesAdded(getFeatureType().getTypeName(), state
                        .getTransaction(), newFeature.getBounds(), false);
            }
        } catch (IllegalAttributeException e) {
            throw new DataSourceException("Error writing expiration tag on old feature. "
                    + "Should not happen, there's a bug at work.", e);
        } catch (SQLException e) {
            throw new DataSourceException(
                    "Error creating a new statement for primary key generation", e);
        } finally {
            JDBCUtils.close(st);
        }
    }

}
