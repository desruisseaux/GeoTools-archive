/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le D�veloppement
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
 */
package org.geotools.ct.proj;

// Geotools dependencies
import org.geotools.cs.CoordinateSystem;


/**
 * Thrown by {@link MapProjection} when a map projection failed because the point is
 * outside the envelope of validity. Bounds are usually 90�S to 90�N and 180�W to 180�E.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see CoordinateSystem#getDefaultEnvelope
 *
 * @deprecated Replaced by {@link org.geotools.referencing.operation.projection.PointOutsideEnvelopeException}.
 */
public class PointOutsideEnvelopeException extends ProjectionException {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -4384490413772200352L;
    
    /**
     * Constructs a new exception with no detail message.
     */
    public PointOutsideEnvelopeException() {
        super();
    }
    
    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message The error message.
     */
    public PointOutsideEnvelopeException(final String message) {
        super(message);
    }
}
