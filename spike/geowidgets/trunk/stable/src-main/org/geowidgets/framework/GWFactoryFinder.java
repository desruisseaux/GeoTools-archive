/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.framework;

import org.geowidgets.crs.model.*;
import org.geowidgets.framework.logging.LoggerFactory;
import org.geowidgets.framework.ui.*;
import org.geowidgets.units.model.EPSG_UnitModel;
import org.geowidgets.units.model.IUnitModel;

/** Returns standard factories and standard objects for common interfaces
 * in GeoWidgets (and partly GeoTools).
 * @author Matthias Basler
 */
public abstract class GWFactoryFinder {

    //UI factories
    protected static GeneralUIFactory generalUIFactory;

    /** @return the UI factory for all widget-toolkit-independed issues. */
    public static GeneralUIFactory getGeneralUIFactory() {
        if (generalUIFactory == null) generalUIFactory = GeneralUIFactory.getDefault();
        return generalUIFactory;
    }

    /** @param newUIFactory the new UI factory for all widget-toolkit-independed issues. */
    public static void setGeneralUIFactory(GeneralUIFactory newUIFactory) {
        if (Util.ensureNonNull(newUIFactory)) generalUIFactory = newUIFactory;
    }

    protected static GeneralSwingUIFactory generalSwingUIFactory;

    /** @return the UI factory for all Swing specific issues. */
    public static GeneralSwingUIFactory getGeneralSwingUIFactory() {
        if (generalSwingUIFactory == null)
            generalSwingUIFactory = GeneralSwingUIFactory.getDefault();
        return generalSwingUIFactory;
    }

    /** @param newUIFactory the new UI factory for all Swing specific issues. */
    public static void setGeneralSwingUIFactory(GeneralSwingUIFactory newUIFactory) {
        if (Util.ensureNonNull(newUIFactory)) generalSwingUIFactory = newUIFactory;
    }

    protected static CRS_SwingUIFactory crs_SwingUIFactory;

    /** @return the UI factory for all Swing-specific issues in the CRS assembly widget. */
    public static CRS_SwingUIFactory getCRS_SwingUIFactory() {
        if (crs_SwingUIFactory == null)
            crs_SwingUIFactory = CRS_SwingUIFactory.getDefault();
        return crs_SwingUIFactory;
    }

    /** @param newUIFactory the new UI factory for all Swing-specific issues in the CRS assembly widget. */
    public static void setCRS_SwingUIFactory(CRS_SwingUIFactory newUIFactory) {
        if (Util.ensureNonNull(newUIFactory)) crs_SwingUIFactory = newUIFactory;
    }

    protected static GeneralSWTUIFactory generalSWTUIFactory;

    /** @return the UI factory for all SWT specific issues. */
    public static GeneralSWTUIFactory getGeneralSWTUIFactory() {
        if (generalSWTUIFactory == null)
            generalSWTUIFactory = GeneralSWTUIFactory.getDefault();
        return generalSWTUIFactory;
    }

    /** @param newUIFactory the new UI factory for all SWT specific issues. */
    public static void setGeneralSWTUIFactory(GeneralSWTUIFactory newUIFactory) {
        if (Util.ensureNonNull(newUIFactory)) generalSWTUIFactory = newUIFactory;
    }

    //Logger factory
    protected static LoggerFactory loggingFactory;

    /** @return the factory to query the GeoWidget's preconfigured logger object from. */
    public static LoggerFactory getLoggerFactory() {
        if (loggingFactory == null) loggingFactory = LoggerFactory.getDefault();
        return loggingFactory;
    }

    /** @param newLoggingFactory the new factory to query the GeoWidget's preconfigured logger object from. */
    public static void setLoggerFactory(LoggerFactory newLoggingFactory) {
        if (Util.ensureNonNull(newLoggingFactory)) loggingFactory = newLoggingFactory;
    }

    //Models
    protected static IUnitModel comboModel;

    /** @return the model for the unit dropdown widget. */
    public static IUnitModel getUnitModel() {
        if (comboModel == null) comboModel = EPSG_UnitModel.getDefault();
        return comboModel;
    }

    /** @param newComboModel the new model for the unit dropdown widget. */
    public static void setUnitModel(IUnitModel newComboModel) {
        if (Util.ensureNonNull(newComboModel)) comboModel = newComboModel;
    }

    protected static IAxisDirectionModel axisModel;

    /** @return the model for the axis direction dropdown widget. */
    public static IAxisDirectionModel getAxisDirectionModel() {
        if (axisModel == null) axisModel = GeoTools_AxisDirectionModel.getDefault();
        return axisModel;
    }

    /** @param newAxisModel the new model for the axis direction dropdown widget. */
    public static void setAxisDirectionModel(IAxisDirectionModel newAxisModel) {
        if (Util.ensureNonNull(newAxisModel)) axisModel = newAxisModel;
    }

    protected static ICRSModel crsModel;

    /** @return the model for the CRS assembly widgets. */
    public static ICRSModel getCRSModel() {
        if (crsModel == null) crsModel = GeoTools_CRSModel.getDefault();
        return crsModel;
    }

    /** @param newCRSModel the new model for the CRS assembly widgets. */
    public static void setCRSModel(ICRSModel newCRSModel) {
        if (Util.ensureNonNull(newCRSModel)) crsModel = newCRSModel;
    }
}
