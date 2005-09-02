/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.units.model;

import java.util.List;

import javax.units.Unit;

import org.opengis.referencing.FactoryException;

/** The model for the unit combo box. This is the class doing the actual work
 * or delegating it to other GeoTools classes. */
public interface IUnitModel {
    /** All units: linear and angular. */
    public static int UNIT_ALL = -1;
    /** Linear units only. */
    public static int UNIT_LINEAR = 0;
    /** Angular units only. */
    public static int UNIT_ANGULAR = 1;

    /** @return the currently supported units in a preferrably localized form.
     * The returned list can be sorted afterwards, if needed.
     * @param unitType linear/angular/both. Use the UNIT_XXX constants. */
    List<String> getSupportedUnits(int unitType);

    /** @return the unit that is most commonly used, e.g. meter for units of length.
     * It must be one of the Strings returned as supported units.
     * @param unitType linear/angular/both. Use the UNIT_XXX constants. */
    String getDefaultUnit(int unitType);

    /** @return the unit by its name or alias.
     * @param name the unit's name as returned in getSupportedUnits(...).  
     * @throws FactoryException if the construction of the unit failed */
    Unit getUnit(String name) throws FactoryException;
    
    /** @return the specified unit's full name, if known, or otherwise the
     * unit's abbreviation.
     * @param u a unit whose label is needed
     * @throws FactoryException if the construction of the unit failed */
    String getUnitName(Unit u) throws FactoryException;
}
