/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.units.model;

import java.util.*;

import javax.units.Unit;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

/** An abstract class that can serve as the base for different implementations
 * of the IUnitModel interface, no matter from what database they get the
 * supported units and their names.
 * 
 * @author Matthias Basler
 */
public abstract class _UnitCombiBoxModel implements IUnitModel {
    protected static _UnitCombiBoxModel me;

    /** Maps the supported names to linear Unit objects. */
    protected Map<String, Unit> lUnits = new HashMap<String, Unit>();
    /** Maps the supported names to angular Unit objects. */
    protected Map<String, Unit> aUnits = new HashMap<String, Unit>();

    /** This method must care about filling the Name-Unit map with
     * all supported units. */
    protected abstract void fillUnits();

    public abstract String getDefaultUnit(int unitType);

    public List<String> getSupportedUnits(int unitType) {
        List<String> l = new ArrayList<String>();
        if (unitType == UNIT_ALL || unitType == UNIT_LINEAR)
            l.addAll(lUnits.keySet());
        if (unitType == UNIT_ALL || unitType == UNIT_ANGULAR)
            l.addAll(aUnits.keySet());
        return l;
    }

    public Unit getUnit(String name) throws NoSuchAuthorityCodeException {
        try {
            Unit u = lUnits.get(name);
            return (u == null) ? aUnits.get(name) : u;
        } catch (Exception e) {
            throw new NoSuchAuthorityCodeException(e.getMessage(),
                    "GeoTools", name);} //$NON-NLS-1$
    }
    
    //Maybe it would be better to use BiMaps to store the mappings,
    //would be faster for this purpose.
    public String getUnitName(Unit u) throws FactoryException {
        boolean isLUnit = lUnits.containsValue(u);
        boolean isAUnit = aUnits.containsValue(u);
        if (!isLUnit && !isAUnit) return u.toString();
        Map<String, Unit> m = (isLUnit)? lUnits : aUnits;
        //Checking each entry
        for (String s : m.keySet()){
            if (m.get(s).equals(u)) return s;
        }
        return null;
    }    
}
