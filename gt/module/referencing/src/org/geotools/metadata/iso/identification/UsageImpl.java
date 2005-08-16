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
package org.geotools.metadata.iso.identification;

// J2SE direct dependencies
import java.util.Collection;
import java.util.Date;

// OpenGIS dependencies
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.identification.Usage;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.metadata.iso.MetadataEntity;
import org.geotools.resources.Utilities;


/**
 * Brief description of ways in which the resource(s) is/are currently used.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 *
 * @since 2.1
 */
public class UsageImpl extends MetadataEntity implements Usage {
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = 4059324536168287490L;

    /**
     * Brief description of the resource and/or resource series usage.
     */
    private InternationalString specificUsage;

    /**
     * Date and time of the first use or range of uses of the resource and/or resource series.
     * Values are milliseconds ellapsed since January 1st, 1970,
     * or {@link Long#MIN_VALUE} if this value is not set.
     */
    private long usageDate = Long.MIN_VALUE;

    /**
     * Applications, determined by the user for which the resource and/or resource series
     * is not suitable.
     */
    private InternationalString userDeterminedLimitations;

    /**
     * Identification of and means of communicating with person(s) and organization(s)
     * using the resource(s).
     */
    private Collection userContactInfo;
    
    /**
     * Constructs an initially empty usage.
     */
    public UsageImpl() {
    }

    /**
     * Creates an usage initialized to the specified values.
     */
    public UsageImpl(final InternationalString specificUsage,
                 final Collection          userContactInfo)
    {
        setUserContactInfo(userContactInfo);
        setSpecificUsage  (specificUsage  );
    }
    
    /**
     * Brief description of the resource and/or resource series usage.
     */
    public InternationalString getSpecificUsage() {
        return specificUsage;
    }

   /**
    * Set a brief description of the resource and/or resource series usage.
    */
    public synchronized void setSpecificUsage(final InternationalString newValue) {
        checkWritePermission();
        specificUsage = newValue;
    }

    /**
     * Date and time of the first use or range of uses of the resource and/or resource series.
     */
    public synchronized Date getUsageDate() {
        return (usageDate!=Long.MIN_VALUE) ? new Date(usageDate) : null;
    }

    /**
     * Set the date and time of the first use.
     */
    public synchronized void setUsageDate(final Date newValue)  {
        checkWritePermission();
        usageDate = (newValue!=null) ? newValue.getTime() : Long.MIN_VALUE;
    }

    /**
     * Applications, determined by the user for which the resource and/or resource series
     * is not suitable.
     */
    public InternationalString getUserDeterminedLimitations() {
        return userDeterminedLimitations;
    }

    /**
     * Set applications, determined by the user for which the resource and/or resource series
     * is not suitable.
     */
    public synchronized void setUserDeterminedLimitations(final InternationalString newValue) {
        checkWritePermission();
        this.userDeterminedLimitations = newValue;
    }

    /**
     * Identification of and means of communicating with person(s) and organization(s)
     * using the resource(s).
     */
    public synchronized Collection getUserContactInfo() {
        return userContactInfo = nonNullCollection(userContactInfo, ResponsibleParty.class);
    }
    
    /**
     * Set identification of and means of communicating with person(s) and organization(s)
     * using the resource(s).
     */
    public synchronized void setUserContactInfo(final Collection newValues) {
        userContactInfo = copyCollection(newValues, userContactInfo, ResponsibleParty.class);
    }    
    
    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        specificUsage             = (InternationalString) unmodifiable(specificUsage);
        userDeterminedLimitations = (InternationalString) unmodifiable(userDeterminedLimitations);
        userContactInfo           = (Collection)          unmodifiable(userContactInfo);
    }

    /**
     * Compare this Usage with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final UsageImpl that = (UsageImpl) object;
            return                  this.usageDate               == that.usageDate                   &&
                   Utilities.equals(this.specificUsage,             that.specificUsage             ) &&
                   Utilities.equals(this.userDeterminedLimitations, that.userDeterminedLimitations ) &&
                   Utilities.equals(this.userContactInfo,           that.userContactInfo           )   ;
        }
        return false;
    }

    /**
     * Returns a hash code value for this usage.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (specificUsage             != null) code ^= specificUsage            .hashCode();
        if (userDeterminedLimitations != null) code ^= userDeterminedLimitations.hashCode();
        if (userContactInfo           != null) code ^= userContactInfo          .hashCode();
        return code;
    }
 
    /**
     * Returns a string representation of this usage.
     */
    public String toString() {
        return String.valueOf(specificUsage);
    }
}
