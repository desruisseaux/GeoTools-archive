/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2004, Institut de Recherche pour le Développement
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
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.metadata.iso.maintenance;

// J2SE dependencies
import java.util.Set;
import java.util.Collections;

// OpenGIS dependencies
import org.opengis.metadata.maintenance.ScopeDescription;

// Geotools dependencies
import org.geotools.metadata.iso.MetadataEntity;


/**
 * Description of the class of information covered by the information.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 *
 * @since 2.1
 */
public class ScopeDescriptionImpl extends MetadataEntity implements ScopeDescription {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -5671299759930976286L;

    /**
     * Dataset to which the information applies.
     */
    private String dataset;

    /**
     * Class of information that does not fall into the other categories to
     * which the information applies.
     */
    private String other;

    /**
     * Creates an initially empty scope description.
     */
    public ScopeDescriptionImpl() {
    }

    /**
     * Constructs a metadata entity initialized with the values from the specified metadata.
     *
     * @since 2.4
     */
    public ScopeDescriptionImpl(final ScopeDescription source) {
        super(source);
    }

    /**
     * Returns the attributes to which the information applies.
     *
     * @todo Not yet implemented.
     */
    public Set getAttributes() {
        return Collections.EMPTY_SET;
    }

    /**
     * Returns the features to which the information applies.
     *
     * @todo Not yet implemented.
     */
    public Set getFeatures() {
        return Collections.EMPTY_SET;
    }

    /**
     * Returns the feature instances to which the information applies.
     *
     * @todo Not yet implemented.
     */
    public Set getFeatureInstances() {
        return Collections.EMPTY_SET;
    }

    /**
     * Returns the attribute instances to which the information applies.
     *
     * @since 2.4
     *
     * @todo Not yet implemented.
     */
    public Set getAttributeInstances() {
        return Collections.EMPTY_SET;
    }

    /**
     * Returns the dataset to which the information applies.
     *
     * @since 2.4
     */
    public String getDataset() {
        return dataset;
    }

    /**
     * Set the dataset to which the information applies.
     *
     * @since 2.4
     */
    public synchronized void setDataset(final String newValue) {
        checkWritePermission();
        dataset = newValue;
    }

    /**
     * Returns the class of information that does not fall into the other categories to
     * which the information applies.
     *
     * @since 2.4
     */
    public String getOther() {
        return other;
    }

    /**
     * Set the class of information that does not fall into the other categories to
     * which the information applies.
     *
     * @since 2.4
     */
    public synchronized void setOther(final String newValue) {
        checkWritePermission();
        other = newValue;
    }
}
