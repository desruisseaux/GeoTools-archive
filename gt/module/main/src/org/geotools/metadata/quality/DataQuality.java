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
package org.geotools.metadata.quality;

// J2SE direct dependencies
import java.util.Collections;
import java.util.Set;

import org.geotools.metadata.MetadataEntity;
import org.geotools.resources.Utilities;
import org.geotools.util.CheckedHashSet;
import org.opengis.metadata.lineage.Lineage;
import org.opengis.metadata.quality.Scope;


/**
 * Quality information for the data specified by a data quality scope.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Toura�vane
 */
public class DataQuality extends MetadataEntity
        implements org.opengis.metadata.quality.DataQuality
{
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
    private Set reports;

    /**
     * Non-quantitative quality information about the lineage of the data specified by the scope.
     * Should be provided only if {@linkplain Scope#getLevel scope level} is
     * {@linkplain org.opengis.metadata.maintenance.ScopeCode#DATASET dataset}.
     */
    private Lineage lineage;
    
    /**
     * Construct an initially empty data quality.
     */
    public DataQuality() {
    }

    /**
     * Creates a data quality initialized to the given scope.
     */
    public DataQuality(Scope scope) {
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
    public Set getReports() {
        final Set reports = this.reports; // Avoid synchronization
        return (reports!=null) ? reports : Collections.EMPTY_SET;
    }

    /**
     * Set the quantitative quality information for the data specified by the scope.
     * Should be provided only if {@linkplain Scope#getLevel scope level} is
     * {@linkplain org.opengis.metadata.maintenance.ScopeCode#DATASET dataset}.
     */
    public synchronized void setReports(final Set newValues) {
        checkWritePermission();
        if (reports == null) {
            reports = new CheckedHashSet(Element.class);
        } else {
            reports.clear();
        }
        reports.addAll(newValues);
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
    
    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        scope     = (Scope)   unmodifiable(scope);
        reports   = (Set)     unmodifiable(reports);
        lineage   = (Lineage) unmodifiable(lineage);
    }

    /**
     * Compare this data quality with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final DataQuality that = (DataQuality) object; 
            return Utilities.equals(this.scope,    that.scope   ) &&
                   Utilities.equals(this.reports,  that.reports ) &&
                   Utilities.equals(this.lineage,  that.lineage )  ;
        }
        return false;
    }

    /**
     * Returns a hash code value for this address. For performance reason, this method do
     * not uses all attributes for computing the hash code. Instead, it uses the attributes
     * that are the most likely to be unique.
     */
    public synchronized int hashCode() {
        int code = 0; // (int)serialVersionUID;
        if (scope != null)   code ^= scope  .hashCode();
        if (lineage != null) code ^= lineage.hashCode();
        return code;
    }

    /**
     * Returns a string representation of this data quality.
     */
    public String toString() {
        return String.valueOf(scope);
    }        
}
