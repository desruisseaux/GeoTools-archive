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
package org.geotools.metadata.constraint;

// J2SE direct dependencies
import java.util.Collection;
import java.util.Iterator;

// OpenGIS dependencies
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.metadata.MetadataEntity;
import org.geotools.resources.Utilities;


/**
 * Restrictions on the access and use of a resource or metadata.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class Constraints extends MetadataEntity
       implements org.opengis.metadata.constraint.Constraints
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 7197823876215294777L;
    
    /**
     * Limitation affecting the fitness for use of the resource. Example, "not to be used for
     * navigation".
     */
    private Collection useLimitation;

    /**
     * Constructs an initially empty constraints.
     */
    public Constraints() {
    }

    /**
     * Returns the limitation affecting the fitness for use of the resource. Example, "not to be used for
     * navigation".
     */
    public synchronized Collection getUseLimitation() {
        return useLimitation = nonNullCollection(useLimitation, InternationalString.class);
    }

    /**
     * Set the limitation affecting the fitness for use of the resource. Example, "not to be used for
     * navigation".
     */
    public synchronized void setUseLimitation(final Collection newValues) {
        useLimitation = copyCollection(newValues, useLimitation, InternationalString.class);
    }

    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        useLimitation = (Collection) unmodifiable(useLimitation);
    }

    /**
     * Compare this constraints with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final Constraints that = (Constraints) object;
            return Utilities.equals(this.useLimitation, that.useLimitation);
        }
        return false;
    }

    /**
     * Returns a hash code value for this constraints.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (useLimitation != null) code ^= useLimitation.hashCode();
        return code;
    }

    /**
     * Returns a string representation of this constraints.
     */
    public synchronized String toString() {
        final StringBuffer buffer = new StringBuffer();
        if (useLimitation != null) {
            for (final Iterator it=useLimitation.iterator(); it.hasNext();) {
                appendLineSeparator(buffer);
                buffer.append(it.next());
            }
        }
        return buffer.toString();
    }
}
