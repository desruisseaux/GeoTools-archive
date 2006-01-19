/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le D�veloppement
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
package org.geotools.metadata.iso.lineage;

// J2SE direct dependencies
import java.util.Collection;
import java.util.Date;

// OpenGIS dependencies
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.lineage.Source;
import org.opengis.metadata.lineage.ProcessStep;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.metadata.iso.MetadataEntity;
import org.geotools.resources.Utilities;


/**
 * Description of the event, including related parameters or tolerances.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Toura�vane
 *
 * @since 2.1
 */
public class ProcessStepImpl extends MetadataEntity implements ProcessStep {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 4629429337326490722L;
    
    /**
     * Description of the event, including related parameters or tolerances.
     */
    private InternationalString description;

    /**
     * Requirement or purpose for the process step.
     */
    private InternationalString rationale;

    /**
     * Date and time or range of date and time on or over which the process step occurred,
     * in milliseconds ellapsed since January 1st, 1970. If there is no such date, then this
     * field is set to the special value {@link Long#MIN_VALUE}.
     */
    private long date;

    /**
     * Identification of, and means of communication with, person(s) and
     * organization(s) associated with the process step.
     */
    private Collection processors;

    /**
     * Information about the source data used in creating the data specified by the scope.
     */
    private Collection sources;

    /**
     * Creates an initially empty process step.
     */
    public ProcessStepImpl() {
    }

    /**
     * Creates a process step initialized to the given description.
     */
    public ProcessStepImpl(final InternationalString description) {
        setDescription(description);
    }
    
     /**
     * Returns the description of the event, including related parameters or tolerances.
     */
    public InternationalString getDescription() {
        return description;
    }
    
    /**
     * Set the description of the event, including related parameters or tolerances.
     */
    public synchronized void setDescription(final InternationalString newValue) {
        checkWritePermission();
        description = newValue;
    }

    /**
     * Returns the requirement or purpose for the process step.
     */
    public InternationalString getRationale() {
        return rationale;
    }

    /**
     * Set the requirement or purpose for the process step.
     */
    public synchronized void setRationale(final InternationalString newValue) {
        checkWritePermission();
        rationale = newValue;
    }

    /**
     * Returns the date and time or range of date and time on or over which
     * the process step occurred.
     */
    public synchronized Date getDate() {
        return (date!=Long.MIN_VALUE) ? new Date(date) : null;
    }

    /**
     * Set the date and time or range of date and time on or over which the process
     * step occurred.
     */
    public synchronized void setDate(final Date newValue) {
        checkWritePermission();
        date = (newValue!=null) ? newValue.getTime() : Long.MIN_VALUE;
    }

    /**
     * Returns the identification of, and means of communication with, person(s) and
     * organization(s) associated with the process step.
     */
    public synchronized Collection getProcessors() {
        return processors = nonNullCollection(processors, ResponsibleParty.class);
    }

    /**
     * Identification of, and means of communication with, person(s) and
     * organization(s) associated with the process step.
     */
    public synchronized void setProcessors(final Collection newValues) {
        processors = copyCollection(newValues, processors, ResponsibleParty.class);
    }

    /**
     * Returns the information about the source data used in creating the data specified
     * by the scope.
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
        description = (InternationalString)  unmodifiable(description);
        rationale    = (InternationalString) unmodifiable(rationale);
        processors   = (Collection)          unmodifiable(processors);
        sources      = (Collection)          unmodifiable(sources);
    }

    /**
     * Compare this process step with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final ProcessStepImpl that = (ProcessStepImpl) object;
            return Utilities.equals(this.description,  that.description ) &&
                   Utilities.equals(this.rationale,    that.rationale   ) &&
                                   (this.date       == that.date        ) &&
                   Utilities.equals(this.processors,   that.processors  ) &&
                   Utilities.equals(this.sources,      that.sources     )  ;
        }
        return false;
    }

    /**
     * Returns a hash code value for this process step.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (description != null) code ^= description.hashCode();
        if (rationale   != null) code ^= rationale  .hashCode();
        if (processors  != null) code ^= processors .hashCode();
        if (sources     != null) code ^= sources    .hashCode();
        return code;
    }

    /**
     * Returns a string representation of this process step.
     *
     * @todo Provides a more elaborated implementation.
     */
    public String toString() {
        return String.valueOf(description);
    }        
}
