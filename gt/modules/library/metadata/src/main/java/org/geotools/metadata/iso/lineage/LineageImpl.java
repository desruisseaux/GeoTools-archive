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
package org.geotools.metadata.iso.lineage;

// J2SE direct dependencies
import java.util.Collection;

// OpenGIS dependencies
import org.opengis.metadata.maintenance.ScopeCode;
import org.opengis.metadata.lineage.Lineage;
import org.opengis.metadata.lineage.ProcessStep;
import org.opengis.metadata.lineage.Source;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.metadata.iso.MetadataEntity;
import org.geotools.metadata.iso.quality.ScopeImpl;
import org.geotools.resources.Utilities;


/**
 * Information about the events or source data used in constructing the data specified by
 * the scope or lack of knowledge about lineage.
 *
 * Only one of {@linkplain #getStatement statement}, {@linkplain #getProcessSteps process steps}
 * and {@link #getSources sources} should be provided.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Toura�vane
 *
 * @since 2.1
 */
public class LineageImpl extends MetadataEntity implements Lineage {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 3351230301999744987L;
    
    /**
     * General explanation of the data producer�s knowledge about the lineage of a dataset.
     * Should be provided only if {@linkplain ScopeImpl#getLevel scope level} is
     * {@linkplain ScopeCode#DATASET dataset} or {@linkplain ScopeCode#SERIES series}.
     */
    private InternationalString statement;

    /**
     * Information about an event in the creation process for the data specified by the scope.
     */
    private Collection processSteps;

    /**
     * Information about the source data used in creating the data specified by the scope.
     */
    private Collection sources;

    /**
     * Constructs an initially empty lineage.
     */
    public LineageImpl() {
    }

    /**
     * Constructs a metadata entity initialized with the values from the specified metadata.
     *
     * @since 2.4
     */
    public LineageImpl(final Lineage source) {
        super(source);
    }

    /**
     * Returns the general explanation of the data producer�s knowledge about the lineage
     * of a dataset. Should be provided only if {@linkplain ScopeImpl#getLevel scope level}
     * is {@linkplain ScopeCode#DATASET dataset} or {@linkplain ScopeCode#SERIES series}.
     */
    public InternationalString getStatement() {
        return statement;
    }
    
    /**
     * Set the general explanation of the data producer�s knowledge about the lineage
     * of a dataset.
     */
    public synchronized void setStatement(final InternationalString newValue) {
        checkWritePermission();
        statement = newValue;
    }

    /**
     * Returns the information about an event in the creation process for the data
     * specified by the scope.
     */
    public synchronized Collection getProcessSteps() {
        return processSteps = nonNullCollection(processSteps, ProcessStep.class);
    }

    /**
     * Set information about an event in the creation process for the data specified
     * by the scope.
     */
    public synchronized void setProcessSteps(final Collection newValues)  {
        processSteps = copyCollection(newValues, processSteps, ProcessStep.class);
    }

    /**
     * Information about the source data used in creating the data specified by the scope.
     */
    public synchronized Collection getSources() {
        return sources = nonNullCollection(sources, Source.class);
    }

    /**
     * Information about the source data used in creating the data specified by the scope.
     */
    public synchronized void setSources(final Collection newValues) {
        sources = copyCollection(newValues, sources, Source.class);
    }
}
