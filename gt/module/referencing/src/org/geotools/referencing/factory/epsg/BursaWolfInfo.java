/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
 * (C) 2005, Institut de Recherche pour le Développement
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
package org.geotools.referencing.factory.epsg;


/**
 * Private structure for {@link FactoryUsingSQL#createBursaWolfParameters} usage.
 * 
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class BursaWolfInfo {
    /** CO.COORD_OP_CODE        */ final String operation;
    /** CO.COORD_OP_METHOD_CODE */ final int    method;
    /** CRS1.DATUM_CODE         */ final String target;

    /** Fill a structure with the specified values. */
    BursaWolfInfo(final String operation, final int method, final String target) {
        this.operation = operation;
        this.method    = method;
        this.target    = target;
    }

    /**
     * MUST returns the operation code. This is required by {@link FactoryUsingSQL#sort}.
     */
    public String toString() {
        return operation;
    }
}
