/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.metadata.lineage;

// J2SE direct dependencies
import java.util.Collection;

// OpenGIS dependencies
import org.opengis.metadata.lineage.ProcessStep;
import org.opengis.metadata.lineage.Source;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.metadata.MetadataEntity;
import org.geotools.resources.Utilities;


/**
 * Information about the events or source data used in constructing the data specified by
 * the scope or lack of knowledge about lineage.
 *
 * Only one of {@linkplain #getStatement statement}, {@linkplain #getProcessSteps process steps}
 * and {@link #getSources sources} should be provided.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class Lineage extends MetadataEntity implements org.opengis.metadata.lineage.Lineage {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 3351230301999744987L;
    
    /**
     * General explanation of the data producer’s knowledge about the lineage of a dataset.
     * Should be provided only if
     * {@linkplain org.geotools.metadata.quality.Scope#getLevel scope level} is
     * {@linkplain org.opengis.metadata.maintenance.ScopeCode#DATASET dataset} or
     * {@linkplain org.opengis.metadata.maintenance.ScopeCode#SERIES series}.
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
     * Construct an initially empty lineage.
     */
    public Lineage() {
    }

    /**
     * Returns the general explanation of the data producer’s knowledge about the lineage
     * of a dataset.
     * Should be provided only if
     * {@linkplain org.geotools.metadata.quality.Scope#getLevel scope level} is
     * {@linkplain org.opengis.metadata.maintenance.ScopeCode#DATASET dataset} or
     * {@linkplain org.opengis.metadata.maintenance.ScopeCode#SERIES series}.
     */
    public InternationalString getStatement() {
        return statement;
    }
    
    /**
     * Set the general explanation of the data producer’s knowledge about the lineage
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

    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        statement    = (InternationalString) unmodifiable(statement);
        processSteps = (Collection)          unmodifiable(processSteps);
        sources      = (Collection)          unmodifiable(sources);
    }

    /**
     * Compare this Lineage with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final Lineage that = (Lineage) object;
            return Utilities.equals(this.statement,    that.statement    ) &&
                   Utilities.equals(this.processSteps, that.processSteps ) &&
                   Utilities.equals(this.sources,      that.sources      );
        }
        return false;
    }

    /**
     * Returns a hash code value for this series.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (statement    != null) code ^= statement   .hashCode();
        if (processSteps != null) code ^= processSteps.hashCode();
        if (sources      != null) code ^= sources     .hashCode();
        return code;
    }

    /**
     * Returns a string representation of this lineage.
     *
     * @todo Provides a more elaborated implementation.
     */
    public String toString() {
        return String.valueOf(statement);
    }        
}
