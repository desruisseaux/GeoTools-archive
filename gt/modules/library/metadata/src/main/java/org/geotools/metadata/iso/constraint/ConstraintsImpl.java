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
package org.geotools.metadata.iso.constraint;

// J2SE direct dependencies
import java.util.Collection;
import java.util.Iterator;

// OpenGIS dependencies
import org.opengis.util.InternationalString;
import org.opengis.metadata.constraint.Constraints;

// Geotools dependencies
import org.geotools.metadata.iso.MetadataEntity;


/**
 * Restrictions on the access and use of a resource or metadata.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 *
 * @since 2.1
 */
public class ConstraintsImpl extends MetadataEntity implements Constraints {
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
    public ConstraintsImpl() {
    }

    /**
     * Constructs a metadata entity initialized with the values from the specified metadata.
     *
     * @since 2.4
     */
    public ConstraintsImpl(final Constraints source) {
        super(source);
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
}
