/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.cs;

// OpenGIS dependencies
import java.rmi.RemoteException;

import org.geotools.units.Unit;
import org.opengis.cs.CS_VerticalDatum;


/**
 * Procedure used to measure vertical distances.
 *
 * @source $URL$
 * @version $Id$
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 *
 * @see org.opengis.cs.CS_VerticalDatum
 *
 * @deprecated Replaced by {@link org.geotools.referencing.datum.DefaultVerticalDatum}.
 */
public class VerticalDatum extends Datum {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 1663224345779675117L;
    
    /**
     * Default vertical datum for ellipsoidal heights. Ellipsoidal heights
     * are measured along the normal to the ellipsoid used in the definition
     * of horizontal datum.
     */
    public static final VerticalDatum ELLIPSOIDAL = (VerticalDatum) pool.canonicalize(
                    new VerticalDatum("Ellipsoidal", DatumType.ELLIPSOIDAL));
    
    /**
     * Creates a vertical datum from an enumerated type value.
     *
     * @param name Name to give new object.
     * @param type Type of vertical datum to create.
     *
     * @see org.opengis.cs.CS_CoordinateSystemFactory#createVerticalDatum
     */
    public VerticalDatum(final CharSequence name, final DatumType.Vertical type) {
        super(name, type);
    }
    
    /**
     * Gets the type of the datum as an enumerated code.
     *
     * Note: return type will be changed to {@link DatumType.Vertical}
     *       when we are able to use generic types (with JDK 1.5).
     *
     * @see org.opengis.cs.CS_VerticalDatum#getDatumType()
     */
    public DatumType/*.Vertical*/ getDatumType() {
        return (DatumType.Vertical) super.getDatumType();
    }
    
    /**
     * Fills the part inside "[...]".
     * Used for formatting Well Known Text (WKT).
     */
    String addString(final StringBuffer buffer, final Unit context) {
        super.addString(buffer, context);
        return "VERT_DATUM";
    }
    
    /**
     * Returns an OpenGIS interface for this datum.
     * The returned object is suitable for RMI use.
     *
     * Note: The returned type is a generic {@link Object} in order
     *       to avoid premature class loading of OpenGIS interface.
     */
    final Object toOpenGIS(final Object adapters) throws RemoteException {
        return new Export(adapters);
    }
    
    
    
    
    /////////////////////////////////////////////////////////////////////////
    ////////////////                                         ////////////////
    ////////////////             OPENGIS ADAPTER             ////////////////
    ////////////////                                         ////////////////
    /////////////////////////////////////////////////////////////////////////
    
    /**
     * Wraps a {@link VerticalDatum} object for use with OpenGIS.
     * This class is suitable for RMI use.
     */
    private final class Export extends Datum.Export implements CS_VerticalDatum {
        /**
         * Constructs a remote object.
         */
        protected Export(final Object adapters) throws RemoteException {
            super(adapters);
        }
    }
}
