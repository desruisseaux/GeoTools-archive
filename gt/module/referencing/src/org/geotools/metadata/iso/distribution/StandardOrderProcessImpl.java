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
package org.geotools.metadata.iso.distribution;
 
// J2SE direct dependencies and extensions
import java.util.Date;

// OpenGIS dependencies
import org.opengis.util.InternationalString;
import org.opengis.metadata.distribution.StandardOrderProcess;

// Geotools dependencies
import org.geotools.metadata.iso.MetadataEntity;
import org.geotools.resources.Utilities;


/**
 * Common ways in which the resource may be obtained or received, and related instructions
 * and fee information.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Jody Garnett
 *
 * @since 2.1
 */
public class StandardOrderProcessImpl extends MetadataEntity implements StandardOrderProcess {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -6503378937452728631L;
    
    /**
     * Fees and terms for retrieving the resource.
     * Include monetary units (as specified in ISO 4217).
     */
    private InternationalString fees;

    /**
     * Date and time when the dataset will be available,
     * in milliseconds ellapsed since January 1st, 1970.
     */
    private long plannedAvailableDateTime = Long.MIN_VALUE;

    /**
     * General instructions, terms and services provided by the distributor.
     */
    private InternationalString orderingInstructions;

    /**
     * Typical turnaround time for the filling of an order.
     */
    private InternationalString turnaround;

    /**
     * Constructs an initially empty standard order process.
     */
    public StandardOrderProcessImpl() {
    }

    /**
     * Returns fees and terms for retrieving the resource.
     * Include monetary units (as specified in ISO 4217).
     */
    public InternationalString getFees() {
        return fees;
    }

    /**
     * Set fees and terms for retrieving the resource.
     * Include monetary units (as specified in ISO 4217).
     */
    public synchronized void setFees(final InternationalString newValue) {
        checkWritePermission();
        fees = newValue;
    }

    /**
     * Returns the date and time when the dataset will be available.
     */
    public synchronized Date getPlannedAvailableDateTime() {
        return (plannedAvailableDateTime!=Long.MIN_VALUE) ?
                new Date(plannedAvailableDateTime) : null;
    }

    /**
     * Set the date and time when the dataset will be available.
     */
    public synchronized void setPlannedAvailableDateTime(final Date newValue) {
        checkWritePermission();
        plannedAvailableDateTime = (newValue!=null) ? newValue.getTime() : Long.MIN_VALUE;
    }

    /**
     * Returns general instructions, terms and services provided by the distributor.
     */
    public InternationalString getOrderingInstructions() {
        return orderingInstructions;
    }

    /**
     * Set general instructions, terms and services provided by the distributor.
     */
    public synchronized void setOrderingInstructions(final InternationalString newValue) {
        checkWritePermission();
        orderingInstructions = newValue;
    }

    /**
     * Returns typical turnaround time for the filling of an order.
     */
    public InternationalString getTurnaround() {
        return turnaround;
    }

    /**
     * Set typical turnaround time for the filling of an order.
     */
    public synchronized void setTurnaround(final InternationalString newValue) {
        checkWritePermission();
        turnaround = newValue;
    }
    
    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        fees                 = (InternationalString) unmodifiable(fees);
        orderingInstructions = (InternationalString) unmodifiable(orderingInstructions);
        turnaround           = (InternationalString) unmodifiable(turnaround);
    }

    /**
     * Compare this StandardOrderProcess with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final StandardOrderProcessImpl that = (StandardOrderProcessImpl) object;
            return Utilities.equals(fees,                       that.fees                    ) &&
                                   (plannedAvailableDateTime == that.plannedAvailableDateTime) &&
                   Utilities.equals(orderingInstructions,       that.orderingInstructions    ) &&
                   Utilities.equals(turnaround,                 that.turnaround              ) ;
        }
        return false;
    }

    /**
     * Returns a hash code value for this series.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (fees                      != null) code ^= fees                    .hashCode();
        if (orderingInstructions      != null) code ^= orderingInstructions    .hashCode();
        if (turnaround                != null) code ^= turnaround              .hashCode();
        return code;
    }

    /**
     * Returns a string representation of this series.
     *
     * @todo Provides a more elaborated implementation.
     */
    public String toString() {
        return String.valueOf(orderingInstructions);
    }
}
