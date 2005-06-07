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
package org.geotools.metadata.iso.citation;

// OpenGIS dependencies
import java.net.MalformedURLException;
import java.net.URI;

// OpenGIS dependencies
import org.opengis.metadata.citation.Address;
import org.opengis.metadata.citation.Contact;
import org.opengis.metadata.citation.OnLineFunction;
import org.opengis.metadata.citation.OnLineResource;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.citation.Role;
import org.opengis.metadata.citation.Telephone;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.metadata.iso.MetadataEntity;
import org.geotools.resources.Utilities;
import org.geotools.util.SimpleInternationalString;


/**
 * Identification of, and means of communication with, person(s) and
 * organizations associated with the dataset.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Toura�vane
 *
 * @since 2.1
 */
public class ResponsiblePartyImpl extends MetadataEntity implements ResponsibleParty {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -2477962229031486552L;

    /**
     * The name of Open Geospatial Consortium as an international string.
     *
     * @todo Localize.
     */
    static final InternationalString OGC_NAME =
            new SimpleInternationalString("Open Geospatial Consortium");

    /**
     * Create a a responsible party metadata entry for OGC involvement.
     * The organisation name is automatically set to "Open Geospatial Consortium".
     *
     * @param role           The OGC role (point of contact, owner, etc.) for a resource.
     * @param function       The OGC function (information, download, etc.) for a resource.
     * @param onlineResource The URI on the resource.
     * @return ResponsibleParty describing OGC involvement
     */ 
    public static ResponsibleParty OGC(final Role role,
                                       final OnLineFunction function,
                                       final URI onlineResource)
    {
        final OnLineResourceImpl resource = new OnLineResourceImpl(onlineResource);
        resource.setFunction(function);
        resource.freeze();
        
        final ContactImpl contact = new ContactImpl(resource);
        contact.freeze();
        
        final ResponsiblePartyImpl ogc = new ResponsiblePartyImpl(role);
        ogc.setOrganisationName(OGC_NAME);
        ogc.setContactInfo(contact);
        ogc.freeze();
        
        return ogc;
    }
    
    /**
     * The <A HREF="http://www.opengeospatial.org">Open Geospatial consortium</A> responsible party.
     * "Open Geospatial consortium" is the new name for "OpenGIS consortium".
     *
     * @see ContactImpl#OGC
     */
    public static ResponsibleParty OGC;
    static {
        final ResponsiblePartyImpl r = new ResponsiblePartyImpl(Role.RESOURCE_PROVIDER);
        r.setOrganisationName(OGC_NAME);
        r.setContactInfo(ContactImpl.OGC);        
        r.freeze();
        OGC = r;
    }
    
    /**
     * The <A HREF="http://www.opengis.org">OpenGIS consortium</A> responsible party.
     * "OpenGIS consortium" is the old name for "Open Geospatial consortium".
     *
     * @see ContactImpl#OPEN_GIS
     *
     * @todo Localize.
     */
    public static ResponsibleParty OPEN_GIS;
    static {
        final ResponsiblePartyImpl r = new ResponsiblePartyImpl(Role.PRINCIPAL_INVESTIGATOR);
        r.setOrganisationName(new SimpleInternationalString("OpenGIS consortium"));
        r.setContactInfo(ContactImpl.OPEN_GIS);
        r.freeze();
        OPEN_GIS = r;
    }
    
    /**
     * The <A HREF="http://www.epsg.org">European Petroleum Survey Group</A> responsible party.
     *
     * @see ContactImpl#EPSG
     */
    public static ResponsibleParty EPSG;
    static {
        final ResponsiblePartyImpl r = new ResponsiblePartyImpl(Role.PRINCIPAL_INVESTIGATOR);
        r.setOrganisationName(new SimpleInternationalString("EPSG"));
        r.setContactInfo(ContactImpl.EPSG);
        r.freeze();
        EPSG = r;
    }

    /**
     * The <A HREF="http://www.remotesensing.org/geotiff/geotiff.html">GeoTIFF</A> responsible
     * party.
     *
     * @see ContactImpl#GEOTIFF
     */
    public static ResponsibleParty GEOTIFF;
    static {
        final ResponsiblePartyImpl r = new ResponsiblePartyImpl(Role.PRINCIPAL_INVESTIGATOR);
        r.setOrganisationName(new SimpleInternationalString("GeoTIFF"));
        r.setContactInfo(ContactImpl.GEOTIFF);
        r.freeze();
        GEOTIFF = r;
    }

    /**
     * The <A HREF="http://www.esri.com">ESRI</A> responsible party.
     *
     * @see ContactImpl#ESRI
     */
    public static ResponsibleParty ESRI;
    static {
        final ResponsiblePartyImpl r = new ResponsiblePartyImpl(Role.PRINCIPAL_INVESTIGATOR);
        r.setOrganisationName(new SimpleInternationalString("ESRI"));
        r.setContactInfo(ContactImpl.ESRI);
        r.freeze();
        ESRI = r;
    }

    /**
     * The <A HREF="http://www.oracle.com">Oracle</A> responsible party.
     *
     * @see ContactImpl#ORACLE
     */
    public static ResponsibleParty ORACLE;
    static {
        final ResponsiblePartyImpl r = new ResponsiblePartyImpl(Role.PRINCIPAL_INVESTIGATOR);
        r.setOrganisationName(new SimpleInternationalString("Oracle"));
        r.setContactInfo(ContactImpl.ORACLE);
        r.freeze();
        ORACLE = r;
    }
    
    /**
     * The <A HREF="http://www.geotools.org">Geotools</A> project.
     *
     * @see ContactImpl#GEOTOOLS
     */
    public static ResponsibleParty GEOTOOLS;
    static {
        final ResponsiblePartyImpl r = new ResponsiblePartyImpl(Role.PRINCIPAL_INVESTIGATOR);
        r.setOrganisationName(new SimpleInternationalString("Geotools"));
        r.setContactInfo(ContactImpl.GEOTOOLS);
        r.freeze();
        GEOTOOLS = r;
    }

    /**
     * Name of the responsible person- surname, given name, title separated by a delimiter.
     */
    private String individualName;

    /**
     * Name of the responsible organization.
     */    
    private InternationalString organisationName;

    /**
     * Role or position of the responsible person
     */
    private InternationalString positionName;
    
    /**
     * Address of the responsible party.
     */
    private Contact contactInfo;
    
    /**
     * Function performed by the responsible party.
     */
    private Role role;

    /**
     * Constructs an initially empty responsible party.
     */
    public ResponsiblePartyImpl() {
    }

    /**
     * Constructs a responsability party with the given role.
     */
    public ResponsiblePartyImpl(final Role role) {
        setRole(role);
    }

    /**
     * Returns the name of the responsible person- surname, given name, title separated by a delimiter.
     * Only one of {@code individualName}, {@link #getOrganisationName organisationName}
     * and {@link #getPositionName positionName} should be provided.
     */
    public String getIndividualName() {
        return individualName;
    }
    
    /**
     * Set the name of the responsible person- surname, given name, title separated by a delimiter.
     * Only one of {@code individualName}, {@link #getOrganisationName organisationName}
     * and {@link #getPositionName positionName} should be provided.
     */
    public synchronized void setIndividualName(final String newValue) {
        checkWritePermission();
        individualName = newValue;
    }
    
    /**
     * Returns the name of the responsible organization.
     * Only one of {@link #getIndividualName individualName}, </code>organisationName</code>
     * and {@link #getPositionName positionName} should be provided.
     */
    public InternationalString getOrganisationName() {
        return organisationName;
    }

    /**
     * Set the name of the responsible organization.
     * Only one of {@link #getIndividualName individualName}, </code>organisationName</code>
     * and {@link #getPositionName positionName} should be provided.
     */
    public synchronized void setOrganisationName(final InternationalString newValue) {
        checkWritePermission();
        organisationName = newValue;
    }
    
    /**
     * Returns the role or position of the responsible person
     * Only one of {@link #getIndividualName individualName},
     * {@link #getOrganisationName organisationName} and {@code positionName}
     * should be provided.
     */
    public InternationalString getPositionName() {
        return positionName;
    }

    /**
     * set the role or position of the responsible person
     * Only one of {@link #getIndividualName individualName},
     * {@link #getOrganisationName organisationName} and {@code positionName}
     * should be provided.
     */
    public synchronized void setPositionName(final InternationalString newValue) {
        checkWritePermission();
        positionName = newValue;
    }
    
    /**
     * Returns the address of the responsible party.
     */
    public Contact getContactInfo() {
        return contactInfo;
    }
    
    /**
     * Set the address of the responsible party.
     */
    public synchronized void setContactInfo(final Contact newValue) {
        checkWritePermission();
        contactInfo = newValue;
    }
    
    /**
     * Returns the function performed by the responsible party.
     */
    public Role getRole() {
        return role;
    }
    
    /**
     * Set the function performed by the responsible party.
     */
    public synchronized void setRole(final Role newValue) {
        checkWritePermission();
        role = newValue;
    }
    
    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        organisationName = (InternationalString) unmodifiable(organisationName);
        positionName     = (InternationalString) unmodifiable(positionName);
        contactInfo      = (Contact)             unmodifiable(contactInfo);
    }

    /**
     * Compare this responsible party with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final ResponsiblePartyImpl that = (ResponsiblePartyImpl) object;
            return Utilities.equals(this.individualName,   that.individualName  ) &&
                   Utilities.equals(this.organisationName, that.organisationName) &&
                   Utilities.equals(this.positionName,     that.positionName    ) &&
                   Utilities.equals(this.contactInfo,      that.contactInfo     ) &&
                   Utilities.equals(this.role,             that.role            );
        }
        return false;
    }

    /**
     * Returns a hash code value for this responsible party. For performance reason, this method
     * do not uses all attributes for computing the hash code. Instead, it uses the attributes
     * that are the most likely to be unique.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (individualName != null) code ^= individualName.hashCode();
        if (contactInfo    != null) code ^= contactInfo   .hashCode();
        return code;
    }

    /**
     * Returns a string representation of this responsible party.
     */
    public synchronized String toString() {
        final StringBuffer buffer = new StringBuffer();
        if (individualName != null) {
            buffer.append(individualName);
            if (role != null) {
                buffer.append(" (");
                buffer.append(role.name().toLowerCase().replace('_', ' '));
                buffer.append(')');
            }
        }
        if (organisationName != null) {
            appendLineSeparator(buffer);
            buffer.append(organisationName);
        }
        if (contactInfo != null) {
            appendLineSeparator(buffer);
            buffer.append(contactInfo);
        }
        return buffer.toString();
    }
}
