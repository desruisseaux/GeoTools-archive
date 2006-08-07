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

// J2SE direct dependencies
import java.util.Date;

// OpenGIS dependencies
import org.opengis.metadata.maintenance.MaintenanceInformation;
import org.opengis.metadata.maintenance.MaintenanceFrequency;
import org.opengis.metadata.maintenance.ScopeCode;
import org.opengis.metadata.maintenance.ScopeDescription;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.metadata.iso.MetadataEntity;
import org.geotools.resources.Utilities;


/**
 * Information about the scope and frequency of updating.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 *
 * @since 2.1
 */
public class MaintenanceInformationImpl extends MetadataEntity implements MaintenanceInformation {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -8268338804608896671L;

    /**
     * Frequency with which changes and additions are made to the resource after the
     * initial resource is completed.
     */
    private MaintenanceFrequency maintenanceAndUpdateFrequency;

    /**
     * Scheduled revision date for resource, in milliseconds ellapsed
     * since January 1st, 1970. If there is no such date, then this field
     * is set to the special value {@link Long#MIN_VALUE}.
     */
    private long dateOfNextUpdate = Long.MIN_VALUE;

    /**
     * Maintenance period other than those defined, in milliseconds.
     */
    private long userDefinedMaintenanceFrequency;

    /**
     * Scope of data to which maintenance is applied.
     */
    private ScopeCode updateScope;

    /**
     * Additional information about the range or extent of the resource.
     */
    private ScopeDescription updateScopeDescription;

    /**
     * Information regarding specific requirements for maintaining the resource.
     */
    private InternationalString maintenanceNote;
    
    /**
     * Creates a an initially empty maintenance information.
     */
    public MaintenanceInformationImpl() {
    }
    
    /**
     * Creates a maintenance information.
     */
    public MaintenanceInformationImpl(final MaintenanceFrequency maintenanceAndUpdateFrequency) {
        setMaintenanceAndUpdateFrequency(maintenanceAndUpdateFrequency);
    }
    
    /**
     * Returns the frequency with which changes and additions are made to the resource
     * after the initial resource is completed.
     */
    public MaintenanceFrequency getMaintenanceAndUpdateFrequency() {
        return maintenanceAndUpdateFrequency;
    }
    
    /**
     * Set the frequency with which changes and additions are made to the resource
     * after the initial resource is completed.
     */
    public synchronized void setMaintenanceAndUpdateFrequency(final MaintenanceFrequency newValue) {
        checkWritePermission();
        maintenanceAndUpdateFrequency = newValue;
    }

    /**
     * Returns the scheduled revision date for resource.
     */
    public synchronized Date getDateOfNextUpdate() {
        return (dateOfNextUpdate!=Long.MIN_VALUE) ? new Date(dateOfNextUpdate) : null;
    }

    /**
     * Set the scheduled revision date for resource.
     */
    public synchronized void setDateOfNextUpdate(final Date newValue) {
        checkWritePermission();
        dateOfNextUpdate = (newValue!=null) ? newValue.getTime() : Long.MIN_VALUE;
    }

    /**
     * Maintenance period other than those defined.
     *
     * @return The period, in milliseconds.
     */
    public long getUserDefinedMaintenanceFrequency() {
        return userDefinedMaintenanceFrequency;
    }

    /**
     * Maintenance period other than those defined.
     */
    public synchronized void setUserDefinedMaintenanceFrequency(final long newValue) {
        checkWritePermission();
        userDefinedMaintenanceFrequency = newValue;
    }

    /**
     * Scope of data to which maintenance is applied.
     */
    public ScopeCode getUpdateScope() {
        return updateScope;
    }

    /**
     * Scope of data to which maintenance is applied.
     */
    public synchronized void setUpdateScope(final ScopeCode newValue) {
        checkWritePermission();
        updateScope = newValue;
    }

    /**
     * Additional information about the range or extent of the resource.
     */
    public ScopeDescription getUpdateScopeDescription() {
        return updateScopeDescription;
    }

    /**
     * Additional information about the range or extent of the resource.
     */
    public synchronized void setUpdateScopeDescription(final ScopeDescription newValue) {
        checkWritePermission();
        updateScopeDescription = newValue;
    }

    /**
     * Information regarding specific requirements for maintaining the resource.
     */
    public InternationalString getMaintenanceNote() {
        return maintenanceNote;
    }

    /**
     * Information regarding specific requirements for maintaining the resource.
     */
    public synchronized void setMaintenanceNote(final InternationalString newValue) {
        checkWritePermission();
        maintenanceNote = newValue;
    }
    
    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        updateScopeDescription = (ScopeDescription)    unmodifiable(updateScopeDescription);
        maintenanceNote        = (InternationalString) unmodifiable(maintenanceNote);
    }

    /**
     * Compare this maintenance information with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final MaintenanceInformationImpl that = (MaintenanceInformationImpl) object;
            return Utilities.equals(this.maintenanceAndUpdateFrequency, that.maintenanceAndUpdateFrequency  ) &&
                   Utilities.equals(this.updateScope,                   that.updateScope                    ) &&
                   Utilities.equals(this.updateScopeDescription,        that.updateScopeDescription         ) &&
                   Utilities.equals(this.maintenanceNote,               that.maintenanceNote                ) &&
                   (this.userDefinedMaintenanceFrequency             == that.userDefinedMaintenanceFrequency) &&
                   (this.dateOfNextUpdate                            == that.dateOfNextUpdate               );
        }
        return false;
    }

    /**
     * Returns a hash code value for this maintenance information.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (maintenanceAndUpdateFrequency != null) code ^= maintenanceAndUpdateFrequency.hashCode();
        if (updateScope                   != null) code ^= updateScope                  .hashCode();
        return code;
    }

    /**
     * Returns a string representation of this maintenance information.
     */
    public synchronized String toString() {
        if (maintenanceAndUpdateFrequency != null) {
            return maintenanceAndUpdateFrequency.name().toLowerCase().replace('_', ' ');
        }
        return "";
    }
}
