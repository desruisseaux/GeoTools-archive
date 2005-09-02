/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.units.model;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.units.Unit;

import org.geotools.referencing.FactoryFinder;
import org.geowidgets.framework.GWFactoryFinder;
import org.geowidgets.framework.Res;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.cs.CSAuthorityFactory;

/** This implementation uses existing GeoTools methods/functions only,
 * that is, it is quite conservative. */
public class EPSG_UnitModel extends _UnitCombiBoxModel {
    protected static final Logger LOGGER = GWFactoryFinder.getLoggerFactory().getLogger();
    protected CSAuthorityFactory csFactory 
            = FactoryFinder.getCSAuthorityFactory("EPSG", null); //$NON-NLS-1$

    protected static EPSG_UnitModel me = new EPSG_UnitModel();

    /** @return the default instance of this implementation. This is a singleton. */
    public static IUnitModel getDefault() {
        return me;
    }

    protected EPSG_UnitModel() {
        fillUnits();
    }

    protected void fillUnits() {
        try {
            Set<String> codes = csFactory.getAuthorityCodes(Unit.class);
            for (String code : codes) {
                try {
                    Unit unit = csFactory.createUnit(code);
                    String name = csFactory.getDescriptionText(code).toString();
                    int c = new Integer(code);
                    if (c < 9100) {
                        super.lUnits.put(name, unit);
                    } else if (c > 9100 && c < 9200) {
                        super.aUnits.put(name, unit);
                    }
                } catch (Exception e) {
                    String msg = Res.get(Res.WIDGETS, "err.CreateUnit", code); //$NON-NLS-1$
                    LOGGER.log(Level.FINE, msg);
                }
            }
        } catch (FactoryException fe) {
            LOGGER.log(Level.WARNING, "err.UnitCodes", fe); //$NON-NLS-1$
        }
    }

    public String getDefaultUnit(int unitType) {
        try {
            String u = (unitType == IUnitModel.UNIT_ANGULAR) ? "9101" : "9001"; //$NON-NLS-1$//$NON-NLS-2$
            return csFactory.getDescriptionText(u).toString();
        } catch (FactoryException fe) {
            LOGGER.log(Level.WARNING, "err.UnitCodes", fe); //$NON-NLS-1$
            return null;
        }
    }
}
