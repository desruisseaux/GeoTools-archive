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

import org.geotools.resources.Utilities;
import org.geotools.units.Unit;
import org.opengis.cs.CS_Datum;
import org.opengis.cs.CS_DatumType;


/**
 * A set of quantities from which other quantities are calculated.
 * It may be a textual description and/or a set of parameters describing the
 * relationship of a coordinate system to some predefined physical locations
 * (such as center of mass) and physical directions (such as axis of spin).
 * It can be defined as a set of real points on the earth that have coordinates.
 * For example, a datum can be thought of as a set of parameters defining
 * completely the origin and orientation of a coordinate system with respect
 * to the earth.  The definition of the datum may also include the temporal
 * behavior (such as the rate of change of the orientation of the coordinate
 * axes).
 *
 * @version $Id$
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 *
 * @see org.opengis.cs.CS_Datum
 *
 * @deprecated Replaced by {@link org.geotools.referencing.datum.Datum}.
 */
public class Datum extends Info {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 2175857309476007487L;
    
    /**
     * The datum type.
     */
    private final DatumType type;
    
    /**
     * Constructs a new datum with the
     * specified name and datum type.
     *
     * @param name The datum name.
     * @param type The datum type.
     */
    public Datum(final CharSequence name, final DatumType type) {
        super(name);
        this.type = type;
        ensureNonNull("type", type);
    }
    
    /**
     * Gets the type of the datum as an enumerated code.
     *
     * @see org.opengis.cs.CS_Datum#getDatumType()
     *
     * @deprecated Replaced by {@link org.geotools.referencing.datum.VerticalDatum#getVerticalDatumType}
     *             for the vertical case. No replacement for other cases.
     */
    public DatumType getDatumType() {
        return type;
    }
    
    /**
     * Compare this datum with the specified object for equality.
     *
     * @param  object The object to compare to <code>this</code>.
     * @param  compareNames <code>true</code> to comparare the {@linkplain #getName name},
     *         {@linkplain #getAlias alias}, {@linkplain #getAuthorityCode authority
     *         code}, etc. as well, or <code>false</code> to compare only properties
     *         relevant to transformations.
     * @return <code>true</code> if both objects are equal.
     */
    public boolean equals(final Info object, final boolean compareNames) {
        if (super.equals(object, compareNames)) {
            final Datum that = (Datum) object;
            return Utilities.equals(this.type, that.type);
        }
        return false;
    }

    /**
     * Returns a hash value for this datum. {@linkplain #getName Name},
     * {@linkplain #getAlias alias}, {@linkplain #getAuthorityCode authority code}
     * and the like are not taken in account. In other words, two datums
     * will return the same hash value if they are equal in the sense of
     * <code>{@link #equals equals}(Info, <strong>false</strong>)</code>.
     *
     * @return The hash code value. This value doesn't need to be the same
     *         in past or future versions of this class.
     */
    public int hashCode() {
        int code = (int)serialVersionUID;
        final DatumType type = getDatumType();
        if (type != null) {
            code += type.hashCode();
        }
        return code;
    }
    
    /**
     * Fills the part inside "[...]". Used for formatting Well Known Text (WKT).
     * Note: All subclasses will override this method, but only {@link HorizontalDatum}
     *       will <strong>not</strong> invokes this parent method, because horizontal
     *       datum do not write the datum type.
     */
    String addString(final StringBuffer buffer, final Unit context) {
        buffer.append(", ");
        buffer.append(type.getValue());
        return "DATUM";
    }
    
    /**
     * Returns an OpenGIS interface for this datum.
     * The returned object is suitable for RMI use.
     *
     * Note: The returned type is a generic {@link Object} in order
     *       to avoid premature class loading of OpenGIS interface.
     */
    Object toOpenGIS(final Object adapters) throws RemoteException {
        return new Export(adapters);
    }
    
    
    
    
    /////////////////////////////////////////////////////////////////////////
    ////////////////                                         ////////////////
    ////////////////             OPENGIS ADAPTER             ////////////////
    ////////////////                                         ////////////////
    /////////////////////////////////////////////////////////////////////////
    
    /**
     * Wrap a {@link Datum} object for use with OpenGIS.
     * This class is suitable for RMI use.
     */
    class Export extends Info.Export implements CS_Datum {
        /**
         * Constructs a remote object.
         */
        protected Export(final Object adapters) throws RemoteException {
            super(adapters);
        }
        
        /**
         * Gets the type of the datum as an enumerated code.
         */
        public CS_DatumType getDatumType() throws RemoteException {
            return adapters.export(Datum.this.getDatumType());
        }
    }
}
