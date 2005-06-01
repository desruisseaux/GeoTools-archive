/*
 * NOTICE OF RELEASE TO THE PUBLIC DOMAIN
 *
 * This work was created by employees of the USDA Forest Service's
 * Fire Science Lab for internal use.  It is therefore ineligible for
 * copyright under title 17, section 105 of the United States Code.  You
 * may treat it as you would treat any public domain work: it may be used,
 * changed, copied, or redistributed, with or without permission of the
 * authors, for free or for compensation.  You may not claim exclusive
 * ownership of this code because it is already owned by everyone.  Use this
 * software entirely at your own risk.  No warranty of any kind is given. 
 *
 * A copy of 17-USC-105 should have accompanied this distribution in the file
 * 17USC105.html.  If not, you may access the law via the US Government's
 * public websites:
 *   - http://www.copyright.gov/title17/92chap1.html#105
 *   - http://www.gpoaccess.gov/uscode/  (enter "17USC105" in the search box.)
 */
package org.geotools.gce.geotiff;

import org.geotools.factory.FactoryRegistryException;
import org.geotools.factory.Hints;

// geotools dependencies
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.operation.DefaultOperationMethod;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CSAuthorityFactory;
import org.opengis.referencing.cs.CSFactory;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.cs.EllipsoidalCS;
import org.opengis.referencing.datum.DatumAuthorityFactory;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.datum.PrimeMeridian;
import org.opengis.referencing.operation.MathTransform;

// GeoAPI dependencies
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.OperationMethod;

// J2SE dependencies
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.HashMap;
import java.util.Map;
import javax.units.ConversionException;

// JSR-108 (units) dependencies
import javax.units.NonSI;
import javax.units.SI;
import javax.units.Unit;


/**
 * The <code>GeoTiffCoordinateSystemAdapter</code> is responsible for
 * interpreting the metadata provided by the
 * <code>GeoTiffIIOMetadataAdapter</code> for the purposes of constructing a
 * CoordinateSystem object representative of the information found in the
 * tags.
 * 
 * <p>
 * This class implements the flow indicated by the following diagram:
 * </p>
 * 
 * <p align="center">
 * <img src="doc-files/GeoTiffFlow.jpg">
 * </p>
 * 
 * <p>
 * To use this class, the <CODE>GeoTiffReader</CODE> should create an instance
 * with the <code>CoordinateSystemAuthorityFactory</code> specified by the
 * <CODE>GeoTiffFormat</CODE> instance which created the reader.  The image
 * specific metadata should then be set with the appropriate accessor methods.
 * Finally, the <code>createCoordinateSystem()</code> method is called to
 * produce the <code>CoordinateReferenceSystem</code> object specified by the
 * metadata.
 * </p>
 *
 * @author Bryce Nordgren / USDA Forest Service
 */
public class GeoTiffCoordinateSystemAdapter {
    // common "user defined" value for GeoKeys of type "short"
    private static final String USER_DEFINED = "32767";

    // Codes from GeoTIFF spec section 6.3.3.3
    public static final short CT_TransverseMercator = 1;
    public static final short CT_TransverseMercator_Modified_Alaska = 2;
    public static final short CT_ObliqueMercator = 3;
    public static final short CT_ObliqueMercator_Laborde = 4;
    public static final short CT_ObliqueMercator_Rosenmund = 5;
    public static final short CT_ObliqueMercator_Spherical = 6;
    public static final short CT_Mercator = 7;
    public static final short CT_LambertConfConic_2SP = 8;
    public static final short CT_LambertConfConic_Helmert = 9;
    public static final short CT_LambertAzimEqualArea = 10;
    public static final short CT_AlbersEqualArea = 11;
    public static final short CT_AzimuthalEquidistant = 12;
    public static final short CT_EquidistantConic = 13;
    public static final short CT_Stereographic = 14;
    public static final short CT_PolarStereographic = 15;
    public static final short CT_ObliqueStereographic = 16;
    public static final short CT_Equirectangular = 17;
    public static final short CT_CassiniSoldner = 18;
    public static final short CT_Gnomonic = 19;
    public static final short CT_MillerCylindrical = 20;
    public static final short CT_Orthographic = 21;
    public static final short CT_Polyconic = 22;
    public static final short CT_Robinson = 23;
    public static final short CT_Sinusoidal = 24;
    public static final short CT_VanDerGrinten = 25;
    public static final short CT_NewZealandMapGrid = 26;
    public static final short CT_TransvMercator_SouthOriented = 27;

    // code from GeoTIFF spec section 6.3.2.4
    private static final String PM_Greenwich = "EPSG:8901";
    private static final HashMap mapCoordTrans = new HashMap();

    // Create one MathTransformFactory for all Geotiff instances.
    private static final MathTransformFactory mtf = FactoryFinder
        .getMathTransformFactory(null);

    static {
        // initialize the Coordinate Transform map with coordinate transforms 
        // that are supported by the Geotools package.
        // anything listed here must be supported in the 
        // createCoordTransformParameterList() method!!
        mapCoordTrans.put(new Short(CT_TransverseMercator),
            "Transverse_Mercator");
        mapCoordTrans.put(new Short(CT_AlbersEqualArea),
            "Albers_Conic_Equal_Area");
        mapCoordTrans.put(new Short(CT_Orthographic), "Orthographic");
        mapCoordTrans.put(new Short(CT_PolarStereographic),
            "Polar_Stereographic");
        mapCoordTrans.put(new Short(CT_ObliqueStereographic),
            "Oblique_Stereographic");
    }

    // authority factory objects
    private CRSAuthorityFactory crsFactory = null;
    private CSAuthorityFactory csFactory = null;
    private DatumAuthorityFactory datumFactory = null;

    // factories to construct CRS/CS/Datum objects directly.
    private DatumFactory datumObjFactory = null;
    private CRSFactory crsObjFactory = null;
    private CSFactory csObjFactory = null;

    /** Holds value of property metadata. */
    private GeoTiffIIOMetadataAdapter metadata = null;

    // Default values of GeoTiff angular and linear units.
    // these will be modified if the appropriate GeoKeys are 
    // set in the file
    private Unit linearUnit = SI.METER;
    private Unit angularUnit = NonSI.DEGREE_ANGLE;

    /**
     * Creates a new instance of GeoTiffCoordinateSystemAdapter
     *
     * @param hints a map of hints to locate the authority and object
     *        factories. (can be null)
     */
    public GeoTiffCoordinateSystemAdapter(Hints hints) {
        // CRS factory is the only one which _has_ to exist.
        crsFactory = FactoryFinder.getCRSAuthorityFactory("EPSG", hints);

        // these are optional, but will reduce the functionality if they are
        // not found.
        csFactory = null;
        datumFactory = null;

        try {
            csFactory = FactoryFinder.getCSAuthorityFactory("EPSG", hints);
            datumFactory = FactoryFinder.getDatumAuthorityFactory("EPSG", hints);
        } catch (FactoryRegistryException fre) {
            ; // do nothing.  Leave them set to null!
        }

        // These should always exist!
        datumObjFactory = FactoryFinder.getDatumFactory(hints);
        crsObjFactory = FactoryFinder.getCRSFactory(hints);
        csObjFactory = FactoryFinder.getCSFactory(hints);
    }

    /**
     * Returns the CSAuthorityFactory instance used by this  object.
     *
     * @return CSAuthorityFactory in use.
     */
    public CSAuthorityFactory getCSFactory() {
        return csFactory;
    }

    /**
     * Returns a copy of the internal coordinate transformation map. This is
     * used for testing purposes only and should remain package  private.
     *
     * @return clone of the coordinate transformation map used by this  class.
     */
    static Map getCoordTransMap() {
        return (Map) (mapCoordTrans.clone());
    }

    /**
     * Returns the CRSAuthorityFactory instance used by this  object.
     *
     * @return CRSAuthorityFactory in use.
     */
    public CRSAuthorityFactory getCRSFactory() {
        return crsFactory;
    }

    /**
     * Returns the DatumAuthorityFactory instance used by this  object.
     *
     * @return DatumAuthorityFactory in use.
     */
    public DatumAuthorityFactory getDatumFactory() {
        return datumFactory;
    }

    /**
     * This method creates a <code>CoordinateReferenceSystem</code> object from
     * the metadata which has been set earlier.  If it cannot create the
     * <code>CoordinateReferenceSystem</code>, then one of three exceptions is
     * thrown to indicate the error.
     *
     * @return the <code>CoordinateReferenceSystem</code> object representing
     *         the file data
     *
     * @throws IOException if there is unexpected data in the GeoKey tags.
     * @throws NullPointerException if the <code>crsFactory</code> or
     *         <code>metadata</code> are uninitialized
     * @throws UnsupportedOperationException if the coordinate system specified
     *         by the GeoTiff file is not supported.
     */
    public CoordinateReferenceSystem createCoordinateSystem()
        throws IOException {
        // check if the prerequsite data are set.
        if ((crsFactory == null) || (metadata == null)) {
            throw new NullPointerException(
                "EPSG CRS factory and metadata must be set!");
        }

        CoordinateReferenceSystem cs = null;

        // the first thing to check is the Model Type.
        // is it "Projected" or is it "Geographic"?
        // "Geocentric" is not supported.
        int modelType = getGeoKeyAsInt(metadata.GTModelTypeGeoKey);

        switch (modelType) {
        case GeoTiffIIOMetadataAdapter.ModelTypeProjected:
            cs = createProjectedCoordinateSystem();

            break;

        case GeoTiffIIOMetadataAdapter.ModelTypeGeographic:
            cs = createGeographicCoordinateSystem();

            break;

        default:
            throw new UnsupportedOperationException(
                "Only Geographic & Projected Systems are supported.  "
                + "(not code " + modelType + ")");
        }

        return cs;
    }

    private ProjectedCRS createProjectedCoordinateSystem()
        throws IOException {
        ProjectedCRS pcs = null;

        // get the projection code
        String projCode = metadata.getGeoKey(GeoTiffIIOMetadataAdapter.ProjectedCSTypeGeoKey);

        // if it's user defined, there's a lot of work to do
        if (projCode.equals(USER_DEFINED)) {
            pcs = createUserDefinedPCS();

            // if it's not user defined, just use the EPSG factory to create the
            // coordinate system
        } else {
            try {
                if (!projCode.startsWith("EPSG")
                        && !projCode.startsWith("epsg")) {
                    projCode = "EPSG:" + projCode;
                }

                pcs = crsFactory.createProjectedCRS(projCode);
            } catch (FactoryException fe) {
                throw new UnsupportedOperationException(
                    "Invalid EPSG code in ProjectedCSTypeGeoKey");
            }
        }

        return pcs;
    }

    private GeographicCRS createGeographicCoordinateSystem()
        throws IOException {
        GeographicCRS gcs = null;

        // get the projection code
        String geogCode = metadata.getGeoKey(GeoTiffIIOMetadataAdapter.GeographicTypeGeoKey);

        // if it's user defined, there's a lot of work to do
        if (geogCode.equals(USER_DEFINED)) {
            gcs = createUserDefinedGCS();

            // if it's not user defined, just use the EPSG factory to create the
            // coordinate system
        } else {
            try {
                gcs = crsFactory.createGeographicCRS("EPSG:" + geogCode);
            } catch (FactoryException fe) {
                GeoTiffException gte = new GeoTiffException(metadata,
                        "Invalid EPSG code in GeographicTypeGeoKey");
                gte.initCause(fe);
                throw gte;
            }
        }

        // set the angular unit specified by this GeoTIFF file
        angularUnit = gcs.getCoordinateSystem().getAxis(0).getUnit();

        return gcs;
    }

    private int getGeoKeyAsInt(int key) throws IOException {
        int retval = 0;

        try {
            retval = Integer.parseInt(metadata.getGeoKey(key));
        } catch (NumberFormatException ne) {
            IOException io = new StreamCorruptedException("GeoTIFF tag " + key
                    + " not a number");
            io.initCause(ne);
        }

        return retval;
    }

    /**
     * Getter for property metadata.
     *
     * @return Value of property metadata.
     */
    public GeoTiffIIOMetadataAdapter getMetadata() {
        return this.metadata;
    }

    /**
     * Setter for property metadata.
     *
     * @param metadata New value of property metadata.
     */
    public void setMetadata(GeoTiffIIOMetadataAdapter metadata) {
        this.metadata = metadata;
    }

    private ProjectedCRS createUserDefinedPCS() throws IOException {
        ProjectedCRS pcs = null;

        // get the projection code
        String projCode = metadata.getGeoKey(GeoTiffIIOMetadataAdapter.ProjectionGeoKey);

        // if it's user defined, there's a lot of work to do
        if (projCode.equals(USER_DEFINED)) {
            // the order of the following calls is important!!
            // 1] Build the GCS and define the angular units.
            // 2] Determine the linear units specified in the GEOTIFF file.
            // 3] Build the CartesianCS.
            // 4] Build the MathTransform & OperationMethod.
            // 5] Build the Projected Coordinate System from the components
            GeographicCRS gcs = createGeographicCoordinateSystem();
            linearUnit = createUnit(GeoTiffIIOMetadataAdapter.ProjLinearUnitsGeoKey,
                    GeoTiffIIOMetadataAdapter.ProjLinearUnitSizeGeoKey,
                    SI.METER, SI.METER);

            try {
                CartesianCS cart = createCartesianCS();
                MathTransform base2derived = createUserDefinedProjection(gcs);
                OperationMethod proj = new DefaultOperationMethod(base2derived);

                Map props = new HashMap();
                props.put("name", "[GeoTiff] Projected CRS");

                pcs = crsObjFactory.createProjectedCRS(props, proj, gcs,
                        base2derived, cart);
            } catch (FactoryException fe) {
                IOException io = new GeoTiffException(metadata,
                        "Error constructing user defined PCS.");
                io.initCause(fe);
                throw io;
            }

            // if it's not user defined, just use the EPSG factory to create the
            // coordinate system
        } else {
            try {
                pcs = crsFactory.createProjectedCRS(projCode);
            } catch (FactoryException fe) {
                throw new UnsupportedOperationException(
                    "Invalid EPSG code in ProjectedCSTypeGeoKey");
            }
        }

        return pcs;
    }

    /**
     * Creates a cartesian CS with units specified in the GeoTiff file.
     * The first axis is always Easting and the second axis is always 
     * northing.  The linear units are specified by the GeoTIFF file.
     *
     * @return CartesianCS with easting and northing axes and the 
     *    correct linear units.
     *
     * @throws FactoryException if it can't create either of the coordinate
     *     system axes or the Cartesian coordinate system.
     */
    private CartesianCS createCartesianCS() throws FactoryException {
        // Create the easting axis
        Map props = new HashMap();
        props.put("name", "[GeoTIFF] Easting");

        CoordinateSystemAxis easting = csObjFactory.createCoordinateSystemAxis(props,
                "Easting", AxisDirection.EAST, linearUnit);

        // Create the Northing axis
        props = new HashMap();
        props.put("name", "[GeoTIFF] Northing");

        CoordinateSystemAxis northing = csObjFactory.createCoordinateSystemAxis(props,
                "Northing", AxisDirection.NORTH, linearUnit);

        // Create the cartesian coordinate system
        props = new HashMap();
        props.put("name", "[GeoTIFF] Cartesian Coordinate System");

        return csObjFactory.createCartesianCS(props, easting, northing);
    }

    private PrimeMeridian createPrimeMeridian() throws IOException {
        // look up the prime meridian:
        // + could be an EPSG code
        // + could be user defined
        // + not defined = greenwich 
        String pmCode = metadata.getGeoKey(GeoTiffIIOMetadataAdapter.GeogPrimeMeridianGeoKey);
        PrimeMeridian pm = null;

        try {
            if (pmCode != null) {
                if (pmCode.equals(USER_DEFINED)) {
                    try {
                        String pmValue = metadata.getGeoKey(GeoTiffIIOMetadataAdapter.GeogPrimeMeridianLongGeoKey);
                        double pmNumeric = Double.parseDouble(pmValue);
                        Map props = new HashMap();
                        props.put("name", "User Defined GEOTIFF Prime Meridian");
                        pm = datumObjFactory.createPrimeMeridian(props,
                                pmNumeric, angularUnit);
                    } catch (NumberFormatException nfe) {
                        IOException io = new GeoTiffException(metadata,
                                "Invalid user-defined prime meridian spec.");
                        io.initCause(nfe);
                        throw io;
                    }
                } else {
                    if (datumFactory == null) {
                        throw new GeoTiffException(metadata,
                            "This geotiff file requires a DatumAuthorityFactory");
                    }

                    pm = datumFactory.createPrimeMeridian("EPSG:" + pmCode);
                }
            } else {
                if (datumFactory == null) {
                    throw new GeoTiffException(metadata,
                        "This geotiff file requires a DatumAuthorityFactory");
                }

                pm = datumFactory.createPrimeMeridian(PM_Greenwich);
            }
        } catch (FactoryException fe) {
            IOException io = new GeoTiffException(metadata,
                    "[GeoTIFF] Invalid prime meridian spec.");
            io.initCause(fe);
            throw io;
        }

        return pm;
    }

    /**
     * Looks up the Geodetic Datum as specified in the GeoTIFF file.   The
     * geotools definition of the geodetic datum includes both  an ellipsoid
     * and a prime meridian, but the code in the GeoTIFF file does NOT include
     * the prime meridian, as it is specified  separately.  This code
     * currently does not support user defined datum.
     *
     * @return GeodeticDatum as described in the geotiff file.
     *
     * @throws GeoTiffException for any error.
     */
    private GeodeticDatum createGeodeticDatum() throws IOException {
        // lookup the datum (w/o PrimeMeridian), error if "user defined"
        GeodeticDatum datum = null;
        String datumCode = metadata.getGeoKey(GeoTiffIIOMetadataAdapter.GeogGeodeticDatumGeoKey);

        if ((datumCode == null) || (datumCode.equals(USER_DEFINED))) {
            throw new GeoTiffException(metadata,
                "A user defined Geographic Coordinate system "
                + "must include a predefined datum!");
        }

        // The DatumAuthorityFactory must exist.
        if (datumFactory == null) {
            throw new GeoTiffException(metadata,
                "This geotiff file requires a DatumAuthorityFactory");
        }

        try {
            datum = (GeodeticDatum) (datumFactory.createDatum("EPSG:"
                    + datumCode));
        } catch (FactoryException fe) {
            throw new GeoTiffException(metadata, "Problem creating datum.");
        } catch (ClassCastException cce) {
            throw new GeoTiffException(metadata,
                "Datum code (" + datumCode + ") must be a horizontal datum!");
        }

        return datum;
    }

    /**
     * The GeoTIFF spec requires that a user defined GCS be comprised of the
     * following:
     * 
     * <ul>
     * <li>
     * a citation
     * </li>
     * <li>
     * a datum definition
     * </li>
     * <li>
     * a prime meridian definition (if not Greenwich)
     * </li>
     * <li>
     * an angular unit definition (if not degrees)
     * </li>
     * </ul>
     * 
     *
     * @return User defined GCS.
     *
     * @throws GeoTiffException wrapped around a FactoryException
     */
    private GeographicCRS createUserDefinedGCS() throws IOException {
        // lookup the angular units used in this file
        angularUnit = createUnit(GeoTiffIIOMetadataAdapter.GeogAngularUnitsGeoKey,
                GeoTiffIIOMetadataAdapter.GeogAngularUnitSizeGeoKey, SI.RADIAN,
                NonSI.DEGREE_ANGLE);

        // lookup the Prime Meridian.
        PrimeMeridian pm = createPrimeMeridian();

        // lookup the Geodetic datum
        GeodeticDatum datum = createGeodeticDatum();

        GeographicCRS gcs = null;

        try {
            // property map is reused
            Map props = new HashMap();

            // ensure the Datum contains the PrimeMeridian specified in the file.
            props.put("name", "[GeoTIFF] Datum for GCS");
            datum = datumObjFactory.createGeodeticDatum(props,
                    datum.getEllipsoid(), pm);

            // make a lat/lon Ellipsoidal CS
            props.put("name", "Latitude");

            CoordinateSystemAxis lat = csObjFactory.createCoordinateSystemAxis(props,
                    "lat", AxisDirection.NORTH, angularUnit);
            props.put("name", "Longitude");

            CoordinateSystemAxis lon = csObjFactory.createCoordinateSystemAxis(props,
                    "lon", AxisDirection.EAST, angularUnit);
            props.put("name", "[GeoTIFF] Lat/Lon CS");

            EllipsoidalCS ecs = csObjFactory.createEllipsoidalCS(props, lon, lat);

            // make the user defined GCS from all the components...
            props.put("name", "[GeoTIFF] User defined GCS");
            gcs = crsObjFactory.createGeographicCRS(props, datum, ecs);
        } catch (FactoryException fe) {
            IOException io = new GeoTiffException(metadata,
                    "Error constructing user defined GCS");
            io.initCause(fe);
            throw io;
        }

        return gcs;
    }

    private MathTransform createUserDefinedProjection(GeographicCRS gcs)
        throws IOException {
        String coordTrans = metadata.getGeoKey(GeoTiffIIOMetadataAdapter.ProjCoordTransGeoKey);

        // throw descriptive exception if ProjCoordTransGeoKey not defined
        if (coordTrans == null) {
            throw new GeoTiffException(metadata,
                "User defined projections must specify"
                + " coordinate transformation code in ProjCoordTransGeoKey");
        }

        // the coordinate transformations specified by this GeoKey are
        // NOT EPSG standards.  The codes in the static map are from
        // section 6.3.3.3 of the GeoTIFF standard.
        MathTransform projXform = null;

        try {
            Short codeCT = Short.valueOf(coordTrans);
            String classification = (String) (mapCoordTrans.get(codeCT));

            // was the code valid?
            if (classification == null) {
                throw new GeoTiffException(metadata,
                    "The coordinate transformation code "
                    + "specified in the ProjCoordTransGeoKey (" + coordTrans
                    + ") is not currently supported");
            }

            // read in the parameters specific to this projection
            ParameterValueGroup params = createCoordTransformParameterList(codeCT,
                    classification, gcs);

            // Create the "base to derived" math transform, parameterized 
            // by "param".
            try {
                projXform = mtf.createParameterizedTransform(params);
            } catch (FactoryException fe) {
                IOException ioe = new GeoTiffException(metadata,
                        "Math Transform creation error.");
                ioe.initCause(fe);
            }
        } catch (NumberFormatException nfe) {
            IOException ioe = new GeoTiffException(metadata,
                    "Bad data in ProjCoordTransGeoKey");
            ioe.initCause(nfe);
            throw ioe;
        }

        return projXform;
    }

    /**
     * This code creates a <code>Unit</code> object out of the
     * <code>ProjLinearUnitsGeoKey</code> and the
     * <code>ProjLinearUnitSizeGeoKey</code>.  The unit may either be
     * specified as a standard EPSG recognized unit, or may be user defined.
     *
     * @param key GeoKey value
     * @param userDefinedKey user defined geokey value
     * @param base &quot;System Unit&quot; (radians or meter)
     * @param def default value
     *
     * @return <code>Unit</code> object representative of the tags in the file.
     *
     * @throws IOException if the<code>ProjLinearUnitsGeoKey</code> is not
     *         specified or if unit is user defined and
     *         <code>ProjLinearUnitSizeGeoKey</code> is either not defined or
     *         does not contain a number.
     */
    private Unit createUnit(int key, int userDefinedKey, Unit base, Unit def)
        throws IOException {
        String unitCode = metadata.getGeoKey(key);

        // if not defined, return the default
        if (unitCode == null) {
            return def;
        }

        // if specified, retrieve the appropriate unit code.
        Unit retval = null;

        if (unitCode.equals(USER_DEFINED)) {
            try {
                String unitSize = metadata.getGeoKey(userDefinedKey);

                // throw descriptive exception if required key is not there.
                if (unitSize == null) {
                    throw new GeoTiffException(metadata,
                        "Must define unit length when using a user "
                        + "defined unit");
                }

                double sz = Double.parseDouble(unitSize);
                retval = base.multiply(sz);
            } catch (NumberFormatException nfe) {
                IOException ioe = new GeoTiffException(metadata,
                        "Bad user defined unit size.");
                ioe.initCause(nfe);
                throw ioe;
            }
        } else {
            try {
                // first check that the csFactory exists.
                if (csFactory == null) {
                    throw new GeoTiffException(metadata,
                        "This GeoTIFF file requires a CSAuthorityFactory.");
                }

                retval = csFactory.createUnit("EPSG:" + unitCode);
            } catch (FactoryException fe) {
                IOException io = new GeoTiffException(metadata,
                        "Error with EPSG Unit specification.");
                io.initCause(fe);
                throw io;
            }
        }

        return retval;
    }

    private ParameterValueGroup createCoordTransformParameterList(Short code,
        String xformName, GeographicCRS gcs) throws IOException {
        // initialize the parameter list
        ParameterValueGroup params = null;

        try {
            params = mtf.getDefaultParameters(xformName);

            // get the semimajor and semiminor axes from the gcs
            // The following cast is VERY WIERD.  A gcs should return a 
            // GeodeticDatum, according to the javadocs.  However, the 
            // cast is required because it's returning a Datum instead.
            Ellipsoid e = ((GeodeticDatum) (gcs.getDatum())).getEllipsoid();
            params.parameter("semi_minor").setValue(e.getSemiMinorAxis());
            params.parameter("semi_major").setValue(e.getSemiMajorAxis());

            // if latitude of origin is specified, use it.
            addGeoKeyToParameterList("latitude_of_origin", params,
                GeoTiffIIOMetadataAdapter.ProjNatOriginLatGeoKey, false,
                SI.RADIAN, angularUnit);

            // if false easting is specified, use it.
            addGeoKeyToParameterList("false_easting", params,
                GeoTiffIIOMetadataAdapter.ProjFalseEastingGeoKey, false,
                SI.METER, linearUnit);

            // if false northing is specified, use it.
            addGeoKeyToParameterList("false_northing", params,
                GeoTiffIIOMetadataAdapter.ProjFalseNorthingGeoKey, false,
                SI.METER, linearUnit);

            // if central meridian is specified, use it.
            addGeoKeyToParameterList("central_meridian", params,
                GeoTiffIIOMetadataAdapter.ProjCenterLongGeoKey, false,
                SI.RADIAN, angularUnit);

            // if scale factor is specified, use it.
            addGeoKeyToParameterList("scale_factor", params,
                GeoTiffIIOMetadataAdapter.ProjScaleAtNatOriginGeoKey, false,
                null, null);

            // if 1st standard parallel is specified, use it
            addGeoKeyToParameterList("standard_parallel_1", params,
                GeoTiffIIOMetadataAdapter.ProjStdParallel1GeoKey, false,
                SI.RADIAN, angularUnit);

            // if 2nd standard parallel is specified, use it.
            addGeoKeyToParameterList("standard_parallel_2", params,
                GeoTiffIIOMetadataAdapter.ProjStdParallel2GeoKey, false,
                SI.RADIAN, angularUnit);

            // verify that the required parameters have been read in...
            ParameterValue test;

            switch (code.shortValue()) {
            case CT_AlbersEqualArea:

                // Albers equal area requires two standard parallels
                test = params.parameter("standard_parallel_1");
                test = params.parameter("standard_parallel_2");

                break;

            case CT_TransverseMercator:

                // requires scale factor and false easting, read in above
                test = params.parameter("scale_factor");
                test = params.parameter("false_easting");

                break;

            case CT_Orthographic:

                // requires latitude of origin, read in above
                test = params.parameter("latitude_of_origin");

                break;

            case CT_PolarStereographic:
            case CT_ObliqueStereographic:

                // both require only scale_factor, read in above
                test = params.parameter("scale_factor");

                break;

            default:
                throw new GeoTiffException(metadata,
                    "Unrecognized coordinate system code.");
            }
        } catch (ParameterNotFoundException pnf) {
            IOException io = new GeoTiffException(metadata,
                    "Required parameter not specified for " + xformName);
            io.initCause(pnf);
            throw io;
        } catch (FactoryException fe) {
            IOException io = new GeoTiffException(metadata,
                    "Cannot find default parameters for: " + xformName);
            io.initCause(fe);
            throw io;
        }

        return params;
    }

    private void addGeoKeyToParameterList(String name,
        ParameterValueGroup params, int key, boolean mandatory, Unit base,
        Unit from) throws IOException {
        double numeric = 0.; // carries data from "try" to "catch" blocks

        try {
            String value = metadata.getGeoKey(key);

            if (value != null) {
                numeric = Double.parseDouble(value);

                // take care of units conversion if needed.
                // will throw a ConversionException if no conversion
                // can be found.
                if ((base != null) && (from != null) && !from.equals(base)) {
                    numeric = from.getConverterTo(base).convert(numeric);
                }

                params.parameter(name).setValue(numeric);
            } else if (mandatory) {
                throw new GeoTiffException(metadata,
                    "Mandatory GeoKey is missing");
            }
        } catch (NumberFormatException nfe) {
            IOException ioe = new GeoTiffException(metadata,
                    "Bad data in numeric field.");
            ioe.initCause(nfe);
            throw ioe;
        } catch (InvalidParameterValueException ipv) {
            IOException ioe = new GeoTiffException(metadata,
                    "Bad parameter value " + name + " = "
                    + Double.toString(numeric) + ".");
            ioe.initCause(ipv);
            throw ioe;
        } catch (ConversionException ce) {
            IOException ioe = new GeoTiffException(metadata,
                    "Cannot convert from " + from.toString() + " to "
                    + base.toString() + ".");
            ioe.initCause(ce);
            throw ioe;
        }
    }
}
