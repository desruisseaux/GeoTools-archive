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
package org.geotools.demos.export;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.geotools.data.DataStore;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.view.DefaultView;
import org.geotools.feature.FeatureType;
import org.geotools.feature.SchemaException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 * DOCUMENT ME!
 *
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 */
public class ExportParameters {
    /** DOCUMENT ME! */
    private static final Logger LOGGER = Logger.getLogger(ExportParameters.class.getPackage()
                                                                                .getName());

    /** DOCUMENT ME! */
    private FeatureSource featureSource;

    /** DOCUMENT ME! */
    private DataStore destDataStore;

    /** DOCUMENT ME! */
    private CoordinateReferenceSystem reprojectCRS;

    /** DOCUMENT ME! */
    private CoordinateReferenceSystem overrideCRS;

    /** DOCUMENT ME! */
    private String newTypeName;

    //private AttributeType[] newAttributes;

    /**
     * Evaluates if the fields holded are enough to make the export.
     *
     * @return DOCUMENT ME!
     */
    public boolean hasEnoughInfo() {
        return (featureSource != null) && (destDataStore != null);
    }

    /**
     * Builds and returns a FeatureSource for the given export options.
     * 
     * <p>
     * If needed, the returned FeatureSource will be a wrapper for exposing the
     * needed schema/query
     * </p>
     *
     * @return
     *
     * @throws IllegalStateException if:<br>
     *         1- Some of the mandatory parameters (featureSource and
     *         destDataStore) has not been setted.<br>
     *         2- The feature source does not defines a CRS and reprojectCRS
     *         has been specified but not an overrideCRS.<br>
     * @throws SchemaException if the new schema can't be created from the
     *         original one
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public FeatureSource createExportSource()
        throws IllegalStateException, SchemaException {
        if (getFeatureSource() == null) {
            throw new IllegalArgumentException("No feature source setted");
        }

        if (getDestDataStore() == null) {
            throw new IllegalArgumentException(
                "No destination data store setted");
        }

        FeatureSource exportSource = getFeatureSource();
        FeatureType sourceSchema = exportSource.getSchema();

        CoordinateReferenceSystem sourceCRS = sourceSchema.getDefaultGeometry()
                                                          .getCoordinateSystem();

        if ((reprojectCRS != null) && (sourceCRS == null)
                && (overrideCRS == null)) {
            throw new IllegalArgumentException(
                "A reproject CRS has been setted but the feature source "
                + " does not exposes a CRS nor a override CRS has been specified");
        }

        FeatureSource newSource = exportSource;

        DefaultQuery query = new DefaultQuery(sourceSchema.getTypeName());
        List props = new ArrayList();
        for(int i = 0; i < sourceSchema.getAttributeCount(); i++){
        	props.add(sourceSchema.getAttributeType(i).getName());
        }
        query.setPropertyNames(props);

        if (overrideCRS != null) {
            LOGGER.info("Setting override CRS " + overrideCRS);
            query.setCoordinateSystem(overrideCRS);
        }

        if (reprojectCRS != null) {
            LOGGER.info("Setting reproject CRS " + reprojectCRS);
            query.setCoordinateSystemReproject(reprojectCRS);
        }

        if (newTypeName != null) {
            LOGGER.info("Setting type name " + newTypeName);
            query.setTypeName(newTypeName);
        }

        if (query != null) {
            newSource = new DefaultView(featureSource, query);
        }

        LOGGER.info("Original schema: " + sourceSchema + 
        		"\nNew schema: " + newSource.getSchema());
        return newSource;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the destDataStore.
     */
    public DataStore getDestDataStore() {
        return destDataStore;
    }

    /**
     * DOCUMENT ME!
     *
     * @param destDataStore The destDataStore to set.
     */
    public void setDestDataStore(DataStore destDataStore) {
        this.destDataStore = destDataStore;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the featureSource.
     */
    public FeatureSource getFeatureSource() {
        return featureSource;
    }

    /**
     * DOCUMENT ME!
     *
     * @param featureSource The featureSource to set.
     */
    public void setFeatureSource(FeatureSource featureSource) {
        this.featureSource = featureSource;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the newTypeName.
     */
    public String getNewTypeName() {
        return newTypeName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param newTypeName The newTypeName to set.
     */
    public void setNewTypeName(String newTypeName) {
        this.newTypeName = newTypeName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the outCRS.
     */
    public CoordinateReferenceSystem getReprojectCRS() {
        return reprojectCRS;
    }

    /**
     * DOCUMENT ME!
     *
     * @param reprojectCRS The outCRS to set.
     */
    public void setReprojectCRS(CoordinateReferenceSystem reprojectCRS) {
        this.reprojectCRS = reprojectCRS;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the outCRS.
     */
    public CoordinateReferenceSystem getOverrideCRS() {
        return overrideCRS;
    }

    /**
     * DOCUMENT ME!
     *
     * @param overrideCRS The outCRS to set.
     */
    public void setOverrideCRS(CoordinateReferenceSystem overrideCRS) {
        this.overrideCRS = overrideCRS;
    }
}
