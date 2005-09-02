/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.crs.model;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.units.Unit;

import org.MaBaUtils.C;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.factory.FactoryGroup;
import org.geowidgets.framework.*;
import org.geowidgets.framework.ui.GeneralUIFactory;
import org.opengis.metadata.Identifier;
import org.opengis.referencing.*;
import org.opengis.referencing.crs.*;
import org.opengis.referencing.cs.*;
import org.opengis.referencing.datum.*;
import org.opengis.referencing.operation.*;

/** Uses the GeoTools implementation (and the GeoAPI interfaces) to perform the
 * needed tasks, such as getting default objects, getting the names of available
 * objects and getting the objects for their name. <p>
 * Since the GeoAPI interfaces have a quite limitited usability, especially when
 * it comes to distinguishing between 2D and 3D version of f.e. coordinate systems,
 * some workarounds are implemented to <ul>
 * <li> return only objects of the needed dimensionality (using hints), and to
 * <li> ensure that the "get object by name" functionality works even if the
 * name is ambigous within the class given as parameter, such as for the
 * GeographicCRS interface.
 * </ul>
 * @author Matthias Basler
 *
 */
public class GeoTools_CRSModel implements ICRSModel {
    protected static final Logger LOGGER = GWFactoryFinder.getLoggerFactory().getLogger();
    protected static final GeneralUIFactory UI = GWFactoryFinder.getGeneralUIFactory();
    /** If set to true, the class will try to create every object and only return
     * objects as "supported" if construction did not fail. */
    protected final boolean checkBeforeInsert = false;

    DatumAuthorityFactory dAFactory = FactoryFinder
            .getDatumAuthorityFactory("EPSG", null); //$NON-NLS-1$
    CSAuthorityFactory csAFactory = FactoryFinder.getCSAuthorityFactory("EPSG", null); //$NON-NLS-1$
    CRSAuthorityFactory crsAFactory = FactoryFinder.getCRSAuthorityFactory("EPSG", null); //$NON-NLS-1$
    CoordinateOperationAuthorityFactory opAFactory = FactoryFinder
            .getCoordinateOperationAuthorityFactory("EPSG", null); //$NON-NLS-1$

    DatumFactory dFactory = FactoryFinder.getDatumFactory(null);
    CSFactory csFactory = FactoryFinder.getCSFactory(null);
    CRSFactory crsFactory = FactoryFinder.getCRSFactory(null);
    CoordinateOperationFactory opFactory = FactoryFinder
            .getCoordinateOperationFactory(null);

    protected Map<Class, EPSGEntry> defaults2D = new HashMap<Class, EPSGEntry>();
    protected Map<Class, EPSGEntry> defaults3D = new HashMap<Class, EPSGEntry>();

    protected static ICRSModel me = new GeoTools_CRSModel();

    /** @return the default instance, since this is a singleton class. */
    public static ICRSModel getDefault() {
        return me;
    }

    GeoTools_CRSModel() {
        defaults2D.put(Ellipsoid.class, new EPSGEntry("7030", dAFactory)); //$NON-NLS-1$
        defaults2D.put(PrimeMeridian.class, new EPSGEntry("8901", dAFactory)); //$NON-NLS-1$
        defaults2D.put(GeodeticDatum.class, new EPSGEntry("6326", dAFactory)); //$NON-NLS-1$

        defaults2D.put(EllipsoidalCS.class, new EPSGEntry("6422", csAFactory)); //$NON-NLS-1$
        defaults2D.put(CartesianCS.class, new EPSGEntry("4400", csAFactory)); //$NON-NLS-1$
        defaults2D.put(GeographicCRS.class, new EPSGEntry("4326", crsAFactory)); //$NON-NLS-1$

        defaults2D.put(Conversion.class, new EPSGEntry("16032", opAFactory)); //$NON-NLS-1$
        defaults2D.put(ProjectedCRS.class, new EPSGEntry("32632", crsAFactory)); //$NON-NLS-1$
        //Plate Carree is unfortunately not yet supported
        //defaults2D.put(Conversion.class, opAFactory.getDescriptionText("19968").toString()); //$NON-NLS-1$
        //defaults2D.put(ProjectedCRS.class, crsAFactory.getDescriptionText("32662").toString()); //$NON-NLS-1$

        defaults3D.put(EllipsoidalCS.class, new EPSGEntry("6423", opAFactory)); //$NON-NLS-1$
        defaults3D.put(CartesianCS.class, new EPSGEntry("6500", opAFactory)); //$NON-NLS-1$
        defaults3D.put(GeographicCRS.class, new EPSGEntry("4979", opAFactory)); //$NON-NLS-1$
    }

//******************************************************************************
    /** @return an EPSG entry describing default object of the specified type.
     * @param cl the object's class
     * @param hint hints in cases where the object's class is not sufficient
     * Example: "2D" and "3D" to distinguish between 2D and 3D geographic CRS. */
    public EPSGEntry getDefaultEntry(final Class cl, final String hint) {
        if (hint != null && hint.equals("3D")) //$NON-NLS-1$
        return this.defaults3D.get(cl);
        else return this.defaults2D.get(cl);
    }

    /** @return the default object itself for the specified type.
     * @param cl the object's class
     * @param hint hints in cases where the object's class is not sufficient
     * Example: "2D" and "3D" to distinguish between 2D and 3D geographic CRS. */
    public <T extends IdentifiedObject> T getDefaultObject(final Class<T> cl,
            final String hint) throws FactoryException {
        EPSGEntry entry = getDefaultEntry(cl, hint);
        return cl.cast(createObject(entry.getCode()));
    }

    protected Map<Class, List<EPSGEntry>> objectCache2D = new HashMap<Class, List<EPSGEntry>>();
    protected Map<Class, List<EPSGEntry>> objectCache3D = new HashMap<Class, List<EPSGEntry>>();

    /** Fetch the names of available objects for the specified class.
     * Following hints are currently used: <ul>
     * <li> "2D"/"3D" for GeographicCRS, EllipsoidalCS and CartesianCS </li>
     * </ul>
     * This implementation uses a cache for all object lists and returns
     * a copy of the appropriate list in the cache or creates the list
     * if it is not yet in the cache. The returned list can be modified. */
    public List<EPSGEntry> getSupportedObjects(
            final Class<? extends IdentifiedObject> cl, final String hint)
            throws FactoryException {
        //we support no standard coordinate system axes
        if (CoordinateSystemAxis.class.isAssignableFrom(cl))
            return new Vector<EPSGEntry>();

        //Let's check the cache first:
        int dim = (hint != null && hint.equals("3D")) ? 3 : 2; //$NON-NLS-1$
        List<EPSGEntry> result;
        result = (dim == 3) ? objectCache3D.get(cl) : objectCache2D.get(cl);
        if (result != null) return new Vector<EPSGEntry>(result);//Return A COPY!

        //Nothing in the cache ... create the list
        Set<String> codes = null;
        AuthorityFactory af = getAuthorityFactoryFor(cl);
        codes = af.getAuthorityCodes(cl);

        //Just to speed things up later
        boolean isGCRS = (GeographicCRS.class.isAssignableFrom(cl));
        boolean isCS = (CoordinateSystem.class.isAssignableFrom(cl));

        result = new Vector<EPSGEntry>();
        if (codes != null && codes.size() != 0) {
            for (String code : codes) {
                boolean isOK = true;

                //Now check for correct dimension, where ambigous
                if (isGCRS && !correctGCRSDimension(code, dim)) isOK = false;
                if (isCS && !correctCSDimension(code, dim)) isOK = false;

                //If model is configured so, check every object if it is REALLY supported.
                //Always check conversion, because there are lots of unsupported ones.
                if (this.checkBeforeInsert || Conversion.class.isAssignableFrom(cl)) {
                    try {
                        af.createObject(code);
                    } catch (Exception e) {
                        String msg = Res.get(Res.WIDGETS, "err.CreateObject", code); //$NON-NLS-1$
                        LOGGER.log(Level.FINE, msg + " - " + e.toString(), e); //$NON-NLS-1$
                        isOK = false;
                    }
                }
                if (isOK) {
                    try {
                        String name = af.getDescriptionText(code).toString();
                        result.add(new EPSGEntry(code, name));
                    } catch (Exception e) {
                        String msg = Res.get(Res.WIDGETS, "err.GetName", code); //$NON-NLS-1$
                        LOGGER.log(Level.FINE, msg + " - " + e.toString(), e); //$NON-NLS-1$
                    }
                }
            }
        }
        if (dim == 3) objectCache3D.put(cl, result);
        else objectCache2D.put(cl, result);
        return new Vector<EPSGEntry>(result);//Return A COPY!
    }

    protected boolean correctGCRSDimension(String epsgCode, int dim) {
        //Check by code (faster, but not so future-proof!)
        Integer c = Integer.parseInt(epsgCode);
        if (dim == 2 & ((c >= 4001 && c <= 4326) || (c >= 4600 && c <= 4904)))
            return true;
        if (dim == 3 & ((c >= 4327 && c <= 4388) || (c >= 4931 && c < 5000)))
            return true;
        return false;
    }

    protected boolean correctCSDimension(String epsgCode, int dim) {
        //Check by creation
        try {
            CoordinateSystem cs = csAFactory.createCoordinateSystem(epsgCode);
            return (dim == cs.getDimension());
        } catch (FactoryException fe) {
            String msg = Res.get(Res.WIDGETS, "err.CreateObject", epsgCode); //$NON-NLS-1$
            LOGGER.log(Level.FINE, msg);
            return false;
        }
    }

    public String getFormattedDescription(final Class cl, final String epsgCode)
            throws FactoryException {
        IdentifiedObject o = null;
        String result = ""; //$NON-NLS-1$
        String delim = Res.get(Res.CRS, "delimiter") + " "; //$NON-NLS-1$ //$NON-NLS-2$
        String ret = UI.getMultiLineNewLine();
        if (Ellipsoid.class.isAssignableFrom(cl)) {
            Ellipsoid el = this.createEllipsoid(epsgCode);
            String unit = " " + el.getAxisUnit().toString(); //$NON-NLS-1$
            //Apply rounding? e.g. M2.round(el.getSemiMajorAxis(), 2);
            result += ret + Res.get(Res.CRS, "x.SemiMajorAxis") + delim //$NON-NLS-1$
                    + el.getSemiMajorAxis() + unit; 
            result += ret + Res.get(Res.CRS, "x.SemiMinorAxis") + delim //$NON-NLS-1$
                    + el.getSemiMinorAxis() + unit; 
            result += ret + Res.get(Res.CRS, "x.InvFlat") + delim //$NON-NLS-1$
                    + el.getInverseFlattening() + unit; 
            o = el;
        } else if (PrimeMeridian.class.isAssignableFrom(cl)) {
            PrimeMeridian pm = this.createPrimeMeridian(epsgCode);
            String unit = " " + pm.getAngularUnit().toString(); //$NON-NLS-1$
            result += ret + Res.get(Res.CRS, "x.GreenwichLong") + delim //$NON-NLS-1$
                    + pm.getGreenwichLongitude() + unit;             
            o = pm;
        } else if (GeodeticDatum.class.isAssignableFrom(cl)) {
            GeodeticDatum gd = this.createGeodeticDatum(epsgCode);
            result += ret + Res.get(Res.CRS, "x.EL") + delim  //$NON-NLS-1$
                    + gd.getEllipsoid().getName().getCode();
            result += ret + Res.get(Res.CRS, "x.PM") + delim  //$NON-NLS-1$
                    + gd.getPrimeMeridian().getName().getCode();
            if (gd.getAnchorPoint() != null) {
                String anchor = gd.getAnchorPoint().toString();
                int i = anchor.indexOf("Fundamental point: "); //$NON-NLS-1$
                if (i == 0) anchor = anchor.substring(19);
                //If that didn't work, maybe this helps:
                anchor.replace("Fundamental point:", ""); //$NON-NLS-1$ //$NON-NLS-2$
                result += ret + Res.get(Res.CRS, "x.GD.Origin") + delim + anchor; //$NON-NLS-1$
            }
            result += ret + Res.get(Res.CRS, "x.AreaOfUse") + delim  //$NON-NLS-1$
                    + gd.getValidArea().getDescription().toString();
            o = gd;
        } else if (EllipsoidalCS.class.isAssignableFrom(cl)) {
            EllipsoidalCS eCS = this.createEllipsoidalCS(epsgCode);
            //Axes are explained in the name, no need to list them explicitely
            /* for (int i = 0; i < eCS.getDimension(); ++i){
             result += ret + Res.get(Res.CRS, "x.Axis") //$NON-NLS-1$
             + " " + i + delim + eCS.getAxis(i).getAbbreviation(); //$NON-NLS-1$
             }*/
            o = eCS;
        } else if (CartesianCS.class.isAssignableFrom(cl)) {
            CartesianCS cCS = this.createCartesianCS(epsgCode);
            //Axes are explained in the name, no need to list them explicitely
            /*for (int i = 0; i < cCS.getDimension(); ++i){
             result += ret + Res.get(Res.CRS, "x.Axis") //$NON-NLS-1$
             + " "  + i + delim + cCS.getAxis(i).getAbbreviation(); //$NON-NLS-1$
             } */
            o = cCS;
        } else if (GeographicCRS.class.isAssignableFrom(cl)) {
            GeographicCRS gCRS = this.createGeographicCRS(epsgCode);
            result += ret + Res.get(Res.CRS, "x.CS") + delim //$NON-NLS-1$
                    + gCRS.getCoordinateSystem().getName().getCode();
            result += ret + Res.get(Res.CRS, "x.GD") + delim //$NON-NLS-1$
                    + gCRS.getDatum().getName().getCode();
            result += ret + Res.get(Res.CRS, "x.Scope") + delim //$NON-NLS-1$
                    + gCRS.getScope().toString();
            result += ret + Res.get(Res.CRS, "x.AreaOfUse") + delim //$NON-NLS-1$
                    + gCRS.getValidArea().getDescription().toString();
            o = gCRS;
        } else if (ProjectedCRS.class.isAssignableFrom(cl)) {
            ProjectedCRS pCRS = this.createProjectedCRS(epsgCode);
            result += ret + Res.get(Res.CRS, "x.GCRS") + delim//$NON-NLS-1$
                    + pCRS.getBaseCRS().getName().getCode(); 
            result += ret + Res.get(Res.CRS, "x.CS") + delim//$NON-NLS-1$
                    + pCRS.getCoordinateSystem().getName().getCode();
            result += ret + Res.get(Res.CRS, "x.PRO") + delim//$NON-NLS-1$
                    + pCRS.getConversionFromBase().getName().getCode();
            result += ret + Res.get(Res.CRS, "x.Scope") + delim//$NON-NLS-1$
                    + pCRS.getScope().toString(); 
            result += ret + Res.get(Res.CRS, "x.AreaOfUse") + delim//$NON-NLS-1$
                    + pCRS.getValidArea().getDescription().toString();
            o = pCRS;
        } else if (Conversion.class.isAssignableFrom(cl)) {
            Conversion conv = this.createConversion(epsgCode);
            result += ret + Res.get(Res.CRS, "x.METHOD") + delim//$NON-NLS-1$
                + conv.getMethod().getName().getCode();
            result += ret + Res.get(Res.CRS, "x.Scope") + delim//$NON-NLS-1$
                + conv.getScope().toString();
            result += ret + Res.get(Res.CRS, "x.AreaOfUse") + delim//$NON-NLS-1$
                + conv.getValidArea().getDescription().toString();
            o = conv;
        }

        if (o != null) {
            String temp = Res.get(Res.CRS, "x.EPSGCode") + delim + getCode(o); //$NON-NLS-1$
            temp += ret + Res.get(Res.CRS, "x.Name") + delim + getName(o); //$NON-NLS-1$
            result = temp + result;
        }
        if (result.length() > 0) return UI.getMultiLinePrefix() + result
                + UI.getMultiLineSuffix();
        else return Res.get(Res.CRS, "nodesc"); //"No description available" //$NON-NLS-1$
    }

    /** @return the first identifier, which usually is the object's code
     * or null of there are no identifiers. 
     * @param o some georeferencing-related object, such as CRS, CS, datums, ... */
    protected static String getCode(final IdentifiedObject o) {
        Iterator<Identifier> it = o.getIdentifiers().iterator();
        if (!it.hasNext()) return null;
        return it.next().getCode();
    }

    /** @return the primary name of the object, (often defined by the EPSG database).
     * @param o some georeferencing-related object, such as CRS, CS, datums, ... */
    protected static String getName(final IdentifiedObject o) {
        return o.getName().getCode();
    }

    public EPSGEntry getEntryFor(final IdentifiedObject o) {
        String code = getCode(o);
        String name = getName(o);
        if (code == null) {
            if (name == null) return EPSGEntry.OTHER;
            else return new EPSGEntry(name);
        }

        /* NOTE Note that we use the crsAFactory here, no matter what kind of object
         * this is. This works well for the geotools factories, since they are
         * all in one object. But for other implementations? */
        if (name == null) {
            try {
                name = this.crsAFactory.getDescriptionText(code).toString();
            } catch (Exception e) {
                String msg = Res.get(Res.WIDGETS, "err.GetName", code); //$NON-NLS-1$
                LOGGER.log(Level.FINE, msg + " - " + e.toString(), e); //$NON-NLS-1$
            }
        }
        return new EPSGEntry(code, name);
    }

    //Ellipsoid
    public Ellipsoid createEllipsoid(final String name, final String sa, final String sb,
            final String sInvFlat, final Unit lUnit) throws FactoryException {
        if (Util.isEmpty(sa) || (Util.isEmpty(sb) && Util.isEmpty(sInvFlat)))
            throw new FactoryException(Res.get("err.ElParams")); //$NON-NLS-1$
        double a = C.getdoubleX(sa, 0);
        double b = C.getdoubleX(sb, 0);
        double invFlat = C.getdoubleX(sInvFlat, 0);
        return createEllipsoid(name, a, b, invFlat, lUnit);
    }

    public Ellipsoid createEllipsoid(final String name, final double a, final double b,
            final double invFlat, final Unit lUnit) throws FactoryException {
        if (invFlat == 0.0) return dFactory.createEllipsoid(Collections.singletonMap(
                "name", name), a, b, lUnit); //$NON-NLS-1$
        else return dFactory.createFlattenedSphere(
                Collections.singletonMap("name", name), a, invFlat, lUnit); //$NON-NLS-1$
    }

    public Ellipsoid createEllipsoid(final String epsgCode) throws FactoryException {
        return dAFactory.createEllipsoid(epsgCode);
    }

    //PrimeMeridian
    public PrimeMeridian createPrimeMeridian(final String name, final double lon,
            final Unit aUnit) throws FactoryException {
        return dFactory.createPrimeMeridian(
                Collections.singletonMap("name", name), lon, aUnit); //$NON-NLS-1$
    }

    public PrimeMeridian createPrimeMeridian(final String epsgCode)
            throws FactoryException {
        return dAFactory.createPrimeMeridian(epsgCode);
    }

    //GeodeticDatum
    public GeodeticDatum createGeodeticDatum(final String name, final Ellipsoid el,
            final PrimeMeridian pm) throws FactoryException {
        return dFactory.createGeodeticDatum(
                Collections.singletonMap("name", name), el, pm); //$NON-NLS-1$        
    }

    public GeodeticDatum createGeodeticDatum(final String epsgCode)
            throws FactoryException {
        return dAFactory.createGeodeticDatum(epsgCode);
    }

    //CoordinateSystemAxis
    public CoordinateSystemAxis createCoordinateSystemAxis(final String name,
            final String abb, final AxisDirection direction, final Unit unit)
            throws FactoryException {
        return csFactory.createCoordinateSystemAxis(Collections
                .singletonMap("name", name), abb, direction, unit); //$NON-NLS-1$
    }

    public EllipsoidalCS createEllipsoidalCS(String name, CoordinateSystemAxis axis0,
            CoordinateSystemAxis axis1) throws FactoryException {
        return csFactory.createEllipsoidalCS(
                Collections.singletonMap("name", name), axis0, axis1); //$NON-NLS-1$
    }

    public EllipsoidalCS createEllipsoidalCS(String name, CoordinateSystemAxis axis0,
            CoordinateSystemAxis axis1, CoordinateSystemAxis axis2)
            throws FactoryException {
        return csFactory.createEllipsoidalCS(
                Collections.singletonMap("name", name), axis0, axis1, axis2); //$NON-NLS-1$
    }

    public EllipsoidalCS createEllipsoidalCS(String epsgCode) throws FactoryException {
        return csAFactory.createEllipsoidalCS(epsgCode);
    }

    public CartesianCS createCartesianCS(String name, CoordinateSystemAxis axis0,
            CoordinateSystemAxis axis1) throws FactoryException {
        return csFactory.createCartesianCS(
                Collections.singletonMap("name", name), axis0, axis1); //$NON-NLS-1$
    }

    public CartesianCS createCartesianCS(String name, CoordinateSystemAxis axis0,
            CoordinateSystemAxis axis1, CoordinateSystemAxis axis2)
            throws FactoryException {
        return csFactory.createCartesianCS(
                Collections.singletonMap("name", name), axis0, axis1, axis2); //$NON-NLS-1$
    }

    public CartesianCS createCartesianCS(String epsgCode) throws FactoryException {
        return csAFactory.createCartesianCS(epsgCode);
    }

    public GeographicCRS createGeographicCRS(String name, GeodeticDatum gd,
            EllipsoidalCS eCS) throws FactoryException {
        return crsFactory.createGeographicCRS(
                Collections.singletonMap("name", name), gd, eCS); //$NON-NLS-1$
    }

    public GeographicCRS createGeographicCRS(String epsgCode) throws FactoryException {
        return crsAFactory.createGeographicCRS(epsgCode);
    }

    public Conversion createConversion(String epsgCode) throws FactoryException {
        return (Conversion) opAFactory.createCoordinateOperation(epsgCode);
    }

    public ProjectedCRS createProjectedCRS(String name, GeographicCRS gCRS,
            CartesianCS cCS, Conversion conv) throws FactoryException {
        FactoryGroup fg = new FactoryGroup();
        return fg.createProjectedCRS(
                Collections.singletonMap("name", name), gCRS, conv, cCS); //$NON-NLS-1$
        //The one below were if we had a "complete" conversion, not a defining conversion:
        //return crsFactory.createProjectedCRS(Collections.singletonMap("name", name), conv.getMethod(), gCRS, conv.getMathTransform(), cCS); //$NON-NLS-1$
    }

    public ProjectedCRS createProjectedCRS(String epsgCode) throws FactoryException {
        return crsAFactory.createProjectedCRS(epsgCode);
    }

    public IdentifiedObject createObject(String epsgCode) throws FactoryException {
        IdentifiedObject o;
        o = createObject(epsgCode, dAFactory);
        if (o != null) return o;
        o = createObject(epsgCode, csAFactory);
        if (o != null) return o;
        o = createObject(epsgCode, crsAFactory);
        if (o != null) return o;
        o = createObject(epsgCode, opAFactory);
        if (o != null) return o;
        throw new FactoryException(Res.get("err.CreateObject")); //$NON-NLS-1$
    }

    protected IdentifiedObject createObject(String epsgCode, AuthorityFactory af) {
        try {
            return af.createObject(epsgCode);
        } catch (FactoryException fe) {
            return null;
        }//Ignore. It was just a try...
    }

    protected AuthorityFactory getAuthorityFactoryFor(Class<? extends IdentifiedObject> cl)
            throws FactoryException {
        AuthorityFactory af = null;
        if (Ellipsoid.class.isAssignableFrom(cl)
                || PrimeMeridian.class.isAssignableFrom(cl)
                || Datum.class.isAssignableFrom(cl)) {
            af = dAFactory;
        } else if (CoordinateSystem.class.isAssignableFrom(cl) ||
        //CoordinateSystemAxis.class.isAssignableFrom(cl) ||
                Unit.class.isAssignableFrom(cl)) {
            af = csAFactory;
        } else if (CoordinateReferenceSystem.class.isAssignableFrom(cl)) {
            af = crsAFactory;
        } else if (Conversion.class.isAssignableFrom(cl)) {
            af = opAFactory;
        } else {
            //NullPointerException would arise, so we catch it ourselves.
            throw new FactoryException(Res.get(Res.WIDGETS,
                    "err.NoFactory", cl.toString())); //$NON-NLS-1$
        }
        return af;
    }
}
