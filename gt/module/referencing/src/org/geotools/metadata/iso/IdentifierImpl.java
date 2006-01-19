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
package org.geotools.metadata.iso;

// OpenGIS direct dependencies
import org.opengis.metadata.Identifier;
import org.opengis.metadata.citation.Citation;
import org.geotools.resources.Utilities;


/**
 * Value uniquely identifying an object within a namespace.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 *
 * @since 2.1
 */
public class IdentifierImpl extends MetadataEntity implements Identifier {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 7459062382170865919L;
    
    /**
     * Alphanumeric value identifying an instance in the namespace.
     */
    private String code;

    /**
     * Identifier of the version of the associated code space or code, as specified
     * by the code space or code authority. This version is included only when the
     * {@linkplain #getCode code} uses versions.
     * When appropriate, the edition is identified by the effective date, coded using
     * ISO 8601 date format.
     */
    private String version;

    /**
     * Organization or party responsible for definition and maintenance of the
     * {@linkplain #getCode code}.
     */
    private Citation authority;    
    
    /**
     * Construct an initially empty identifier.
     */
    public IdentifierImpl() {
    }

    /**
     * Creates an identifier initialized to the given code.
     */
    public IdentifierImpl(final String code) {
        setCode(code);
    }

    /**
     * Creates an identifier initialized to the given authority and code.
     *
     * @since 2.2
     */
    public IdentifierImpl(final Citation authority, final String code) {
        setAuthority(authority);
        setCode(code);
    }

    /**
     * Alphanumeric value identifying an instance in the namespace.
     *
     * @return The code.
     */
    public String getCode() {
        return code;
    }

    /**
     * Set the alphanumeric value identifying an instance in the namespace.
     */
    public synchronized void setCode(final String newValue) {
        checkWritePermission();
        code = newValue;
    }

    /**
     * Identifier of the version of the associated code, as specified
     * by the code space or code authority. This version is included only when the
     * {@linkplain #getCode code} uses versions.
     * When appropriate, the edition is identified by the effective date, coded using
     * ISO 8601 date format.
     *
     * @return The version, or {@code null} if not available.
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * Set an identifier of the version of the associated code.
     */
    public synchronized void setVersion(final String newValue) {
        checkWritePermission();
        version = newValue;
    }

    /**
     * Organization or party responsible for definition and maintenance of the
     * {@linkplain #getCode code}.
     *
     * @return The authority, or {@code null} if not available.
     */
    public Citation getAuthority() {
        return authority;        
    }

    /**
     * Set the organization or party responsible for definition and maintenance of the
     * {@linkplain #getCode code}.
     */
    public synchronized void setAuthority(final Citation newValue) {
        checkWritePermission();
        authority = newValue;
    }
    
    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        authority = (Citation) unmodifiable(authority);
    }

    /**
     * Compare this Identifier with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final IdentifierImpl that = (IdentifierImpl) object;
            return Utilities.equals(this.code,      that.code      ) &&
                   Utilities.equals(this.version,   that.version   ) &&
                   Utilities.equals(this.authority, that.authority );
        }
        return false;
    }

    /**
     * Returns a hash code value for this address. For performance reason, this method do
     * not uses all attributes for computing the hash code. Instead, it uses the attributes
     * that are the most likely to be unique.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (this.code != null) code ^= this.code.hashCode();
        if (authority != null) code ^= authority.hashCode();
        return code;
    }
}
