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
package org.geotools.nature;

// J2SE extensions
import javax.units.SI;
import javax.units.NonSI;
import javax.units.Unit;
import javax.units.UnitFormat;
import javax.units.TransformedUnit;


/**
 * A set of units to use in addition of {@link SI} and {@link NonSI}.
 *
 * @version $Id$
 * @author Desruisseaux
 */
public final class Units {
    /**
     * Do not allows instantiation of this class.
     */
    private Units() {
    }

    /**
     * Pseudo-unit for sexagesimal degree. Numbers in this pseudo-unit has the following format:
     *
     * <cite>sign - degrees - decimal point - minutes (two digits) - integer seconds (two digits) -
     * fraction of seconds (any precision)</cite>.
     *
     * This unit is non-linear and not pratical for computation. Consequently, it should be
     * avoid as much as possible. Unfortunatly, this pseudo-unit is extensively used in the
     * EPSG database (code 9110).
     */
    public static final Unit SEXAGESIMAL_DMS = TransformedUnit.getInstance(NonSI.DEGREE_ANGLE,
                                               new SexagesimalConverter(10000).inverse());
    static {
        UnitFormat.label(SEXAGESIMAL_DMS, "D.MS");
    }

    /**
     * Pseudo-unit for degree - minute - second. Numbers in this pseudo-unit has the following
     * format:
     *
     * <cite>signed degrees (integer) - arc-minutes (integer) - arc-seconds
     * (real, any precision)</cite>.
     *
     * This unit is non-linear and not pratical for computation. Consequently, it should be
     * avoid as much as possible. Unfortunatly, this pseudo-unit is extensively used in the
     * EPSG database (code 9107).
     */
    public static final Unit DEGREE_MINUTE_SECOND = TransformedUnit.getInstance(NonSI.DEGREE_ANGLE,
                                                    new SexagesimalConverter(1).inverse());
    static {
        UnitFormat.label(DEGREE_MINUTE_SECOND, "DMS");
    }
}
