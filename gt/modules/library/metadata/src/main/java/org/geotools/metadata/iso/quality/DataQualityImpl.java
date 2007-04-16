/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2004, Institut de Recherche pour le D�veloppement
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
package org.geotools.metadata.iso.quality;

// J2SE direct dependencies
import java.util.Collection;

// OpenGIS dependencies
import org.opengis.metadata.lineage.Lineage;
import org.opengis.metadata.quality.DataQuality;
import org.opengis.metadata.quality.Element;
import org.opengis.metadata.quality.Scope;

// Geotools dependencies
import org.geotools.metadata.iso.MetadataEntity;
import org.geotools.resources.Utilities;


/**
 * Quality information for the data specified by a data quality scope.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Toura�vane
 */
public class DataQualityImpl extends MetadataEntity implements DataQuality {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 7964896551368382214L;
    
    /**
     * The specific data to which the data quality information applies.
     */
    private Scope scope;

    /**
     * Quantitative quality information for the data specified by the scope.
     * Should be provided only if {@linkplain Scope#getLevel scope level} is
     * {@linkplain org.opengis.metadata.maintenance.ScopeCode#DATASET dataset}.
     */
    private Collection reports;

    /**
     * Non-quantitative quality information about the lineage of the data specified by the scope.
     * Should be provided only if {@linkplain Scope#getLevel scope level} is
     * {@linkplain org.opengis.metadata.maintenance.ScopeCode#DATASET dataset}.
     */
    private Lineage lineage;
    
    /**
     * Constructs an initially empty data quality.
     */
    public DataQualityImpl() {
    }

    /**
     * Constructs a metadata entity initialized with the values from the specified metadata.
     *
     * @since 2.4
     */
    public DataQualityImpl(final DataQuality source) {
        super(source);
    }

    /**
     * Creates a data quality initialized to the given scope.
     */
    public DataQualityImpl(Scope scope) {
        setScope(scope);
    }
    
    /**
     * The specific data to which the data quality information applies.
     */
    public Scope getScope() {
        return scope;
    }

    /**
     * Set the specific data to which the data quality information applies.
     */
    public synchronized void setScope(final Scope newValue) {
        checkWritePermission();
        scope = newValue;
    }

    /**
     * Quantitative quality information for the data specified by the scope.
     * Should be provided only if {@linkplain Scope#getLevel scope level} is
     * {@linkplain org.opengis.metadata.maintenance.ScopeCode#DATASET dataset}.
     */
    public synchronized Collection getReports() {
        return reports = nonNullCollection(reports, Element.class);
    }

    /**
     * Set the quantitative quality information for the data specified by the scope.
     * Should be provided only if {@linkplain Scope#getLevel scope level} is
     * {@linkplain org.opengis.metadata.maintenance.ScopeCode#DATASET dataset}.
     */
    public synchronized void setReports(final Collection newValues) {
        reports = copyCollection(newValues, reports, Element.class);
    }

    /**
     * Non-quantitative quality information about the lineage of the data specified by
     * the scope. Should be provided only if {@linkplain Scope#getLevel scope level} is
     * {@linkplain org.opengis.metadata.maintenance.ScopeCode#DATASET dataset}.
     */
    public Lineage getLineage() {
        return lineage;
    }

    /**
     * Set the non-quantitative quality information about the lineage of the data specified
     * by the scope. Should be provided only if {@linkplain Scope#getLevel scope level} is
     * {@linkplain org.opengis.metadata.maintenance.ScopeCode#DATASET dataset}.
     */
    public synchronized void setLineage(final Lineage newValue) {
        checkWritePermission();
        lineage = newValue;
    }
}
