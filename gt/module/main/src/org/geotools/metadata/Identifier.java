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
package org.geotools.metadata;

// OpenGIS direct dependencies
import org.geotools.resources.Utilities;
import org.opengis.metadata.citation.Citation;


/**
 * Value uniquely identifying an object within a namespace.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class Identifier extends MetadataEntity
        implements org.opengis.metadata.Identifier
{
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
    public Identifier() {
    }

    /**
     * Creates an identifier initialized to the given code.
     */
    public Identifier(final String code) {
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
     * @return The version, or <code>null</code> if not available.
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
     * @return The authority, or <code>null</code> if not available.
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
            final Identifier that = (Identifier) object;
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
