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
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureReader;
import org.geotools.data.postgis.fidmapper.VersionedFIDMapper;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;

/**
 * Provides forward only access to the feature differences
 * 
 * @author Administrator
 * 
 */
public class FeatureDiffReader {

    private FeatureReader fvReader;

    private FeatureReader tvReader;

    private String fromVersion;

    private String toVersion;

    private Feature fvFeature;

    private Feature tvFeature;

    private FeatureReader deletedReader;

    private FeatureReader createdReader;

    private FeatureType externalFeatureType;

    private VersionedFIDMapper mapper;

    private FeatureDiff lastDiff;

    public FeatureDiffReader(String fromVersion, String toVersion, FeatureType externalFeatureType,
            VersionedFIDMapper mapper, FeatureReader createdReader, FeatureReader deletedReader,
            FeatureReader fvReader, FeatureReader tvReader) {
        this.fromVersion = fromVersion;
        this.toVersion = toVersion;
        this.externalFeatureType = externalFeatureType;
        this.mapper = mapper;
        this.createdReader = createdReader;
        this.deletedReader = deletedReader;
        this.fvReader = fvReader;
        this.tvReader = tvReader;
    }

    /**
     * Reads the next FeatureDifference
     * 
     * @return The next FeatureDifference
     * 
     * @throws IOException
     *             If an error occurs reading the FeatureDifference.
     * @throws NoSuchElementException
     *             If there are no more Features in the Reader.
     */
    public FeatureDiff next() throws IOException, NoSuchElementException {
        // check we have something, and force reader mantainance as well, so that
        // we make sure finished ones are nullified
        try {
            if (!hasNext())
                throw new NoSuchElementException("No more diffs in this reader");

            if (createdReader != null) {
                // all attributes in the external type become changes
                Feature f = createdReader.next();
                Map changes = new HashMap();
                for (int i = 0; i < externalFeatureType.getAttributeCount(); i++) {
                    String attName = externalFeatureType.getAttributeType(i).getName();
                    changes.put(attName, f.getAttribute(attName));
                }
                String id = mapper.getUnversionedFid(f.getID());
                return new FeatureDiff(id, fromVersion, toVersion, FeatureDiff.CREATED, changes);
            } else if (deletedReader != null) {
                // no changes, we just need the id
                Feature f = deletedReader.next();
                String id = mapper.getUnversionedFid(f.getID());
                return new FeatureDiff(id, fromVersion, toVersion, FeatureDiff.DELETED, null);
            } else {
                FeatureDiff diff = lastDiff;
                lastDiff = null;
                return diff;
            }
        } catch (IllegalAttributeException e) {
            throw new DataSourceException("Could not properly load the fetures to diff: " + e);
        }

    }

    /**
     * Query whether this FeatureDiffReader has another FeatureDiff.
     * 
     * @return True if there are more differences to be read. In other words, true if calls to next
     *         would return a feature rather than throwing an exception.
     * 
     * @throws IOException
     *             If an error occurs determining if there are more Features.
     */
    public boolean hasNext() throws IOException {
        // we first scan created, then removed, then the two that need to be diffed (which are
        // guaranteed to be parallel, so check just one)
        if (createdReader != null) {
            if (createdReader.hasNext()) {
                return true;
            } else {
                createdReader.close();
                createdReader = null;
            }
        }
        if (deletedReader != null) {
            if (deletedReader.hasNext()) {
                return true;
            } else {
                deletedReader.close();
                deletedReader = null;
            }
        }
        try {
            // this is harder... we may have features that have changed between fromVersion and
            // toVersion, but which are equal in those two (typical case, rollback). So we really
            // need to compute the diff and move forward if there's no difference at all
            if(lastDiff != null)
                return true;
            if (fvReader != null && tvReader != null) {
                while (true) {
                    if (!fvReader.hasNext()) {
                        lastDiff = null;
                        fvReader.close();
                        tvReader.close();
                        return false;
                    }
                    // compute field by field difference
                    Feature from = fvReader.next();
                    Feature to = tvReader.next();
                    Map changes = new HashMap();
                    for (int i = 0; i < externalFeatureType.getAttributeCount(); i++) {
                        String attName = externalFeatureType.getAttributeType(i).getName();
                        Object toAttribute = to.getAttribute(attName);
                        Object fromAttribute = from.getAttribute(attName);
                        if (!DataUtilities.attributesEqual(fromAttribute, toAttribute)) {
                            changes.put(attName, toAttribute);
                        }
                    }
                    if (!changes.isEmpty()) {
                        String id = mapper.getUnversionedFid(from.getID());
                        lastDiff = new FeatureDiff(id, fromVersion, toVersion,
                                FeatureDiff.MODIFIED, changes);
                        return true;
                    }
                }
            } else {
                return false; // closed;
            }
        } catch (IllegalAttributeException e) {
            throw new DataSourceException("Could not properly load the fetures to diff: " + e);
        }
    }

    /**
     * Release the underlying resources associated with this stream.
     * 
     * @throws IOException
     *             DOCUMENT ME!
     */
    public void close() throws IOException {
        if (createdReader != null) {
            createdReader.close();
            createdReader = null;
        }
        if (deletedReader != null) {
            deletedReader.close();
            deletedReader = null;
        }
        if (fvReader != null) {
            fvReader.close();
            fvReader = null;
        }
        if (tvReader != null) {
            tvReader.close();
            tvReader = null;
        }
    }
}
