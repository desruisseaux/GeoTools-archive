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

// J2SE dependencies
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.HashMap;
import java.util.Map;

import javax.media.jai.ParameterList;

// JSR-108 (units) dependencies
import javax.units.NonSI ; 
import javax.units.SI ; 
import javax.units.Unit ; 

// GeoAPI dependencies
import org.opengis.referencing.crs.CoordinateReferenceSystem ; 
import org.opengis.referencing.crs.CRSAuthorityFactory ; 
import org.opengis.referencing.cs.CSAuthorityFactory ; 
import org.opengis.referencing.datum.DatumAuthorityFactory ; 
import org.opengis.referencing.datum.DatumFactory ; 
import org.opengis.referencing.datum.GeodeticDatum ; 
import org.opengis.referencing.crs.CRSFactory ; 
import org.opengis.referencing.cs.CSFactory ; 
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.datum.Ellipsoid ; 
import org.opengis.referencing.datum.PrimeMeridian ;  
import org.opengis.referencing.crs.GeographicCRS ; 
import org.opengis.referencing.crs.ProjectedCRS ; 
import org.opengis.referencing.cs.AxisDirection ; 
import org.opengis.referencing.cs.CoordinateSystemAxis ; 
import org.opengis.referencing.cs.EllipsoidalCS ; 
import org.opengis.referencing.operation.OperationMethod ;

// geotools dependencies
import org.geotools.referencing.FactoryFinder ; 
import org.geotools.factory.FactoryRegistryException ; 
import org.geotools.factory.Hints ; 

// Technically, this is mapped to OperationMethod, but it's complicated,
// and it's only relevant when I re-enable user defined projections.
//import org.geotools.cs.Projection;



/**
 * The <code>GeoTiffCoordinateSystemAdapter</code> is responsible for
 * interpreting the metadata provided by the
 * <code>GeoTiffIIOMetadataAdapter</code> for the purposes of
 * constructing a CoordinateSystem object representative of the
 * information found in the tags.
 *
 * <p>
 * This class implements the flow indicated by the following
 * diagram: 
 * <p align="center"><img src="doc-files/GeoTiffFlow.jpg"></p>
 *
 * <p>
 * To use this class, the <CODE>GeoTiffReader</CODE> should create
 * an instance with the <code>CoordinateSystemAuthorityFactory</code>
 * specified by the <CODE>GeoTiffFormat</CODE> instance which
 * created the reader.  The image specific metadata should then
 * be set with the appropriate accessor methods.  Finally, the
 * <code>createCoordinateSystem()</code> method is called to
 * produce the <code>CoordinateReferenceSystem</code> object specified
 * by the metadata.
 * @author Bryce Nordgren / USDA Forest Service
 */
public class GeoTiffCoordinateSystemAdapter {
    
    // common "user defined" value for GeoKeys of type "short"
    private static final String USER_DEFINED = "32767" ;  
    
    // Codes from GeoTIFF spec section 6.3.3.3
    public static final short CT_TransverseMercator                 = 1  ; 
    public static final short CT_TransverseMercator_Modified_Alaska = 2  ; 
    public static final short CT_ObliqueMercator                    = 3  ; 
    public static final short CT_ObliqueMercator_Laborde            = 4  ; 
    public static final short CT_ObliqueMercator_Rosenmund          = 5  ; 
    public static final short CT_ObliqueMercator_Spherical          = 6  ; 
    public static final short CT_Mercator                           = 7  ; 
    public static final short CT_LambertConfConic_2SP               = 8  ; 
    public static final short CT_LambertConfConic_Helmert           = 9  ; 
    public static final short CT_LambertAzimEqualArea               = 10 ; 
    public static final short CT_AlbersEqualArea                    = 11 ; 
    public static final short CT_AzimuthalEquidistant               = 12 ; 
    public static final short CT_EquidistantConic                   = 13 ; 
    public static final short CT_Stereographic                      = 14 ; 
    public static final short CT_PolarStereographic                 = 15 ; 
    public static final short CT_ObliqueStereographic               = 16 ; 
    public static final short CT_Equirectangular                    = 17 ; 
    public static final short CT_CassiniSoldner                     = 18 ; 
    public static final short CT_Gnomonic                           = 19 ; 
    public static final short CT_MillerCylindrical                  = 20 ; 
    public static final short CT_Orthographic                       = 21 ; 
    public static final short CT_Polyconic                          = 22 ; 
    public static final short CT_Robinson                           = 23 ; 
    public static final short CT_Sinusoidal                         = 24 ; 
    public static final short CT_VanDerGrinten                      = 25 ; 
    public static final short CT_NewZealandMapGrid                  = 26 ; 
    public static final short CT_TransvMercator_SouthOriented       = 27 ; 

    // code from GeoTIFF spec section 6.3.2.4
    private static final String PM_Greenwich = "8901" ; 
    
    // authority factory objects
    private CRSAuthorityFactory   crsFactory      = null ; 
    private CSAuthorityFactory    csFactory       = null ;
    private DatumAuthorityFactory datumFactory    = null ; 

    // factories to construct CRS/CS/Datum objects directly.
    private DatumFactory          datumObjFactory = null ; 
    private CRSFactory            crsObjFactory   = null ;
    private CSFactory             csObjFactory    = null ; 
    
    /**
     * Holds value of property metadata.
     */
    private GeoTiffIIOMetadataAdapter metadata = null ; 
    
    // Default values of GeoTiff angular and linear units.
    // these will be modified if the appropriate GeoKeys are 
    // set in the file
    private Unit linearUnit  = SI.METER ; 
    private Unit angularUnit = NonSI.DEGREE_ANGLE ; 
    
    private static final Map mapCoordTrans = new HashMap();
    
    static {
        // initialize the Coordinate Transform map with coordinate transforms 
        // that are supported by the Geotools package.
        // anything listed here must be supported in the createCoordTransformParameterList()
        // method!!
        mapCoordTrans.put(new Short(CT_TransverseMercator),   "Transverse_Mercator") ; 
        mapCoordTrans.put(new Short(CT_AlbersEqualArea),      "Albers_Conic_Equal_Area") ; 
        mapCoordTrans.put(new Short(CT_Orthographic),         "Orthographic") ; 
        mapCoordTrans.put(new Short(CT_PolarStereographic),   "Polar_Stereographic") ; 
        mapCoordTrans.put(new Short(CT_ObliqueStereographic), "Oblique_Stereographic") ; 
    }
    
    /** Creates a new instance of GeoTiffCoordinateSystemAdapter 
     * @param hints a map of hints to locate the authority and object factories. (can be null)
     */
    public GeoTiffCoordinateSystemAdapter(Hints hints) {
      csFactory = FactoryFinder.getCSAuthorityFactory("EPSG", hints) ; 
      crsFactory = FactoryFinder.getCRSAuthorityFactory("EPSG", hints) ; 
      datumFactory = FactoryFinder.getDatumAuthorityFactory("EPSG", hints) ; 

      datumObjFactory = FactoryFinder.getDatumFactory(hints) ; 
      crsObjFactory = FactoryFinder.getCRSFactory(hints) ; 
      csObjFactory = FactoryFinder.getCSFactory(hints) ; 
    }

    /**
     * Returns the CSAuthorityFactory instance used by this 
     * object.
     * @return CSAuthorityFactory in use.
     */
    public CSAuthorityFactory getCSFactory() { 
      return csFactory ; 
    } 
    
    /**
     * Returns the CRSAuthorityFactory instance used by this 
     * object.
     * @return CRSAuthorityFactory in use.
     */
    public CRSAuthorityFactory getCRSFactory() { 
      return crsFactory ; 
    } 
    
    /**
     * Returns the DatumAuthorityFactory instance used by this 
     * object.
     * @return DatumAuthorityFactory in use.
     */
    public DatumAuthorityFactory getDatumFactory() { 
      return datumFactory ; 
    } 
    
    /**
     * This method creates a <code>CoordinateReferenceSystem</code> object
     * from the metadata which has been set earlier.  If it
     * cannot create the <code>CoordinateReferenceSystem</code>, then one of
     * three exceptions is thrown to indicate the error.
     * @return the <code>CoordinateReferenceSystem</code> object representing the
     * file data
     * @throws StreamCorruptedException if there is unexpected data in the GeoKey tags.
     * @throws NullPointerException if the <code>csFactory</code>, <code>datumFactory</code>,
     *          <code>crsFactory</code> or <code>metadata</code> are uninitialized
     * @throws UnsupportedOperationException if the coordinate system specified by the GeoTiff file
     * is not supported.
     */    
    public CoordinateReferenceSystem createCoordinateSystem() throws IOException {
        // check if the prerequsite data are set.
        if ((crsFactory==null) || (metadata==null) || (csFactory==null) || (datumFactory==null)) {
            throw new NullPointerException("EPSG factories and metadata must be set!") ; 
        }
        
        CoordinateReferenceSystem cs = null ;  
        
        
        // the first thing to check is the Model Type.
        // is it "Projected" or is it "Geographic"?
        // "Geocentric" is not supported.
        int modelType = getGeoKeyAsInt(metadata.GTModelTypeGeoKey) ; 
        switch (modelType) { 

            case GeoTiffIIOMetadataAdapter.ModelTypeProjected:
                cs = createProjectedCoordinateSystem() ; 
                break ; 

            case GeoTiffIIOMetadataAdapter.ModelTypeGeographic:
                cs = createGeographicCoordinateSystem() ; 
                break ; 

            default:
                throw new UnsupportedOperationException(
                    "Only Geographic & Projected Systems are supported.  " +
                    "(not code " + modelType + ")") ; 
        }
        
        return cs ; 
        
    }
    
    private ProjectedCRS createProjectedCoordinateSystem() throws IOException {
        ProjectedCRS pcs = null ; 

        // get the projection code
        String projCode = metadata.getGeoKey(GeoTiffIIOMetadataAdapter.ProjectedCSTypeGeoKey) ; 

        // if it's user defined, there's a lot of work to do
        if (projCode.equals(USER_DEFINED)) {
            pcs = createUserDefinedPCS() ; 
        
        // if it's not user defined, just use the EPSG factory to create the
        // coordinate system
        } else {
            try { 
                pcs = crsFactory.createProjectedCRS(projCode) ;
            } catch (FactoryException fe) {
                throw new UnsupportedOperationException("Invalid EPSG code in ProjectedCSTypeGeoKey") ;
            }
        }
        
        return pcs ; 
    }
    
    private GeographicCRS createGeographicCoordinateSystem() throws IOException {
        GeographicCRS gcs = null ; 

        // get the projection code
        String geogCode = metadata.getGeoKey(GeoTiffIIOMetadataAdapter.GeographicTypeGeoKey) ; 

        // if it's user defined, there's a lot of work to do
        if (geogCode.equals(USER_DEFINED)) {
            gcs = createUserDefinedGCS() ; 
        
        // if it's not user defined, just use the EPSG factory to create the
        // coordinate system
        } else {
            try { 
                gcs = crsFactory.createGeographicCRS(geogCode) ;
            } catch (FactoryException fe) {
                throw new UnsupportedOperationException("Invalid EPSG code in GeographicTypeGeoKey") ;
            }
        }
        
        // set the angular unit specified by this GeoTIFF file
        angularUnit = gcs.getCoordinateSystem().getAxis(0).getUnit() ; 
        
        return gcs ; 
    }
    
    private int getGeoKeyAsInt(int key) throws IOException {
        int retval = 0 ; 
        
        try { 
            retval = Integer.parseInt(metadata.getGeoKey(key));
        } catch (NumberFormatException ne) { 
            IOException io = new StreamCorruptedException("GeoTIFF tag "+key+" not a number") ; 
            io.initCause(ne) ; 
        }
        
        return retval ; 
    }
    
    /**
     * Getter for property metadata.
     * @return Value of property metadata.
     */
    public GeoTiffIIOMetadataAdapter getMetadata() {
        return this.metadata;
    }
    
    /**
     * Setter for property metadata.
     * @param metadata New value of property metadata.
     */
    public void setMetadata(GeoTiffIIOMetadataAdapter metadata) {
        this.metadata = metadata;
    }
    
    private ProjectedCRS createUserDefinedPCS() throws IOException {
        ProjectedCRS pcs = null ; 

        // get the projection code
        String projCode = metadata.getGeoKey(GeoTiffIIOMetadataAdapter.ProjectionGeoKey) ; 

        // if it's user defined, there's a lot of work to do
        if (projCode.equals(USER_DEFINED)) {
          throw new GeoTiffException(metadata, "User defined projections not supported") ;
          /*
            // the order of the following calls is important!!
            // 1] Build the GCS and define the angular units.
            // 2] Determine the linear units specified in the GEOTIFF file.
            // 3] Build the Projection to be applied to the data
            // 4] Build the Projected Coordinate System from the components
            GeographicCRS gcs = createGeographicCoordinateSystem() ; 
            linearUnit = createUnit(GeoTiffIIOMetadataAdapter.ProjLinearUnitsGeoKey,
                GeoTiffIIOMetadataAdapter.ProjLinearUnitSizeGeoKey, Unit.METRE, Unit.METRE) ; 
            Projection proj = createUserDefinedProjection(gcs) ;
            try { 
                pcs = CoordinateSystemFactory.getDefault().createProjectedCoordinateSystem(
                   proj.getClassName(), gcs, proj, linearUnit, AxisInfo.X, AxisInfo.Y) ;
            } catch (FactoryException fe) { 
                IOException io = new GeoTiffException(metadata, "Error constructing user defined PCS.") ; 
                io.initCause(fe) ; 
                throw io ; 
            }
          */
        
        // if it's not user defined, just use the EPSG factory to create the
        // coordinate system
        } else {
            try { 
                pcs = crsFactory.createProjectedCRS(projCode) ;
            } catch (FactoryException fe) {
                throw new UnsupportedOperationException("Invalid EPSG code in ProjectedCSTypeGeoKey") ;
            }
        }
        
        return pcs ; 
    }


    private PrimeMeridian createPrimeMeridian() throws IOException { 
        // look up the prime meridian:
        // + could be an EPSG code
        // + could be user defined
        // + not defined = greenwich 
        String pmCode = metadata.getGeoKey(GeoTiffIIOMetadataAdapter.GeogPrimeMeridianGeoKey) ;
        PrimeMeridian pm = null ; 
        try { 
          if (pmCode != null) { 
            if (pmCode.equals(USER_DEFINED)) { 
              try {
                  String pmValue = metadata.getGeoKey(GeoTiffIIOMetadataAdapter.GeogPrimeMeridianLongGeoKey) ;
                  double pmNumeric = Double.parseDouble(pmValue) ; 
                  Map props = new HashMap() ; 
                  props.put("name", "User Defined GEOTIFF Prime Meridian") ; 
                  pm = datumObjFactory.createPrimeMeridian(props, pmNumeric, angularUnit) ; 
              } catch (NumberFormatException nfe) { 
                  IOException io = new GeoTiffException(metadata, "Invalid user-defined prime meridian spec.") ;
                  io.initCause(nfe) ; 
                  throw io ; 
              }            
            } else { 
              pm = datumFactory.createPrimeMeridian(pmCode) ; 
            }
          } else { 
            pm = datumFactory.createPrimeMeridian(PM_Greenwich) ; 
          }
        } catch (FactoryException fe) { 
          IOException io = new GeoTiffException(metadata, "[GeoTIFF] Invalid prime meridian spec.") ;
          io.initCause(fe) ;
          throw io ;
        }
      return pm ; 
    }

    /**
     * Looks up the Geodetic Datum as specified in the GeoTIFF file.  
     * The geotools definition of the geodetic datum includes both 
     * an ellipsoid and a prime meridian, but the code in the GeoTIFF
     * file does NOT include the prime meridian, as it is specified 
     * separately.
     * 
     * This code currently does not support user defined datum.
     */
    private GeodeticDatum createGeodeticDatum() throws IOException { 

        // lookup the datum (w/o PrimeMeridian), error if "user defined"
        GeodeticDatum datum = null ; 
        String datumCode = metadata.getGeoKey(GeoTiffIIOMetadataAdapter.GeogGeodeticDatumGeoKey) ;
        if ((datumCode == null) || (datumCode.equals(USER_DEFINED))) { 
            throw new GeoTiffException(metadata, "A user defined Geographic Coordinate system "+
                "must include a predefined datum!") ; 
        }
        try { 
            datum = (GeodeticDatum)(datumFactory.createDatum(datumCode)) ; 
        } catch (FactoryException fe) { 
            throw new GeoTiffException(metadata, "Problem creating datum.") ; 
        } catch (ClassCastException cce) { 
            throw new GeoTiffException(metadata, "Datum code ("+datumCode+") must be a horizontal datum!") ; 
        }

        return datum ; 
    }
        

    /**
     * The GeoTIFF spec requires that a user defined GCS be comprised
     * of the following: 
     * <ul>
     * <li> a citation
     * <li> a datum definition
     * <li> a prime meridian definition (if not Greenwich)
     * <li> an angular unit definition (if not degrees)
     * </ul>
     */
    private GeographicCRS createUserDefinedGCS() throws IOException {

        // lookup the angular units used in this file
        angularUnit = createUnit(GeoTiffIIOMetadataAdapter.GeogAngularUnitsGeoKey,
            GeoTiffIIOMetadataAdapter.GeogAngularUnitSizeGeoKey, SI.RADIAN, NonSI.DEGREE_ANGLE) ; 
        
        // lookup the Prime Meridian.
        PrimeMeridian pm = createPrimeMeridian() ; 

        // lookup the Geodetic datum
        GeodeticDatum datum = createGeodeticDatum() ; 

        GeographicCRS gcs = null ; 
        try {
            // property map is reused
            Map props = new HashMap() ;

            // ensure the Datum contains the PrimeMeridian specified in the file.
            props.put("name", "[GeoTIFF] Datum for GCS") ; 
            datum = datumObjFactory.createGeodeticDatum(props, datum.getEllipsoid(), pm) ; 

            // make a lat/lon Ellipsoidal CS
            props.put("name", "Latitude") ; 
            CoordinateSystemAxis lat = 
              csObjFactory.createCoordinateSystemAxis(props, "lat", 
                AxisDirection.NORTH, angularUnit) ; 
            props.put("name", "Longitude") ; 
            CoordinateSystemAxis lon = 
              csObjFactory.createCoordinateSystemAxis(props, "lon", 
                AxisDirection.EAST, angularUnit) ; 
            props.put("name", "[GeoTIFF] Lat/Lon CS") ; 
            EllipsoidalCS ecs = csObjFactory.createEllipsoidalCS(props, lon, lat) ; 

            // make the user defined GCS from all the components...
            props.put("name", "[GeoTIFF] User defined GCS") ; 
            gcs = crsObjFactory.createGeographicCRS(props, datum, ecs) ; 
        } catch (FactoryException fe) { 
            IOException io = new GeoTiffException(metadata, "Error constructing user defined GCS") ; 
            io.initCause(fe)  ;
            throw io ; 
        }
        
        return gcs ; 
    }
    
    private OperationMethod createUserDefinedProjection(GeographicCRS gcs) throws IOException {
        throw new GeoTiffException(metadata, 
          "User Defined Projection not supported!") ; 
        /*
        String coordTrans = metadata.getGeoKey(GeoTiffIIOMetadataAdapter.ProjCoordTransGeoKey) ; 
        
        // throw descriptive exception if ProjCoordTransGeoKey not defined
        if (coordTrans == null) { 
            throw new StreamCorruptedException("User defined projections must specify"+
              " coordinate transformation code in ProjCoordTransGeoKey") ; 
        }
        
        // the coordinate transformations specified by this GeoKey are 
        // NOT EPSG standards.  The codes in the static map are from 
        // section 6.3.3.3 of the GeoTIFF standard.
        Projection proj = null ;
        try {
            Short codeCT = Short.valueOf(coordTrans) ; 
            String classification = (String)(mapCoordTrans.get(codeCT)); 
            
            // was the code valid? 
            if (classification == null) {
                throw new UnsupportedOperationException("The coordinate transformation code " + 
                  "specified in the ProjCoordTransGeoKey (" + coordTrans +") is not currently supported") ; 
            }
            
            // read in the parameters specific to this projection
            ParameterList params = createCoordTransformParameterList(codeCT, classification, gcs) ; 
            
            try { 
                proj = CoordinateSystemFactory.getDefault().createProjection(
                    "User Defined GeoTIFF projection", classification, params) ; 
            } catch (FactoryException fe) { 
                IOException ioe = new GeoTiffException(metadata, "Error creating user defined projection from GeoTIFF tags.") ; 
                ioe.initCause(fe) ; 
            }
            
        } catch (NumberFormatException nfe) { 
            IOException ioe = new GeoTiffException(metadata, "Bad data in ProjCoordTransGeoKey") ; 
            ioe.initCause(nfe) ; 
            throw ioe ; 
        }
        
        
        return proj ; 
        */
    }
    
    /**
     * This code creates an <code>org.geotools.units.Unit</code> object
     * out of the <code>ProjLinearUnitsGeoKey</code> and the
     * <code>ProjLinearUnitSizeGeoKey</code>.  The unit may either
     * be specified as a standard EPSG recognized unit, or may be
     * user defined.
     * @return <code>Unit</code> object representative of the tags in
     * the file.
     * @throws StreamCorruptedException if the <code>ProjLinearUnitsGeoKey</code> is not specified or if
     * unit is user defined and
     * <code>ProjLinearUnitSizeGeoKey</code> is either not defined
     * or does not contain a number.
     */    
    private Unit createUnit(int key, int userDefinedKey, Unit base, Unit def) throws IOException {
        String unitCode = metadata.getGeoKey(key) ; 
        
        // if not defined, return the default
        if (unitCode == null) { 
            return def ; 
        }
        
        // if specified, retrieve the appropriate unit code.
        Unit retval = null ; 
        if (unitCode.equals(USER_DEFINED)) {
            try { 
                String unitSize = metadata.getGeoKey(userDefinedKey) ;
                
                // throw descriptive exception if required key is not there.
                if (unitSize == null) {
                    throw new GeoTiffException(metadata, 
                      "Must define unit length when using a user " +
                      "defined unit") ; 
                }
                double sz = Double.parseDouble(unitSize) ; 
                retval = base.multiply(sz) ; 
            } catch (NumberFormatException nfe) {
                IOException ioe = new GeoTiffException(metadata, "Bad user defined unit size.") ; 
                ioe.initCause(nfe) ; 
                throw ioe ; 
            }
        } else {
            try {
                retval = csFactory.createUnit(unitCode) ; 
            } catch (FactoryException fe) { 
                IOException io = new GeoTiffException(metadata, "Error with EPSG Unit specification.") ; 
                io.initCause(fe) ;
                throw io ; 
            }
        }
        
        return retval ; 
    }
    
/*
    private ParameterList createCoordTransformParameterList(Short code, 
        String classification, GeographicCRS gcs) throws IOException {
        // initialize the parameter list
        ParameterList params = 
            CoordinateSystemFactory.getDefault().createProjectionParameterList(classification) ; 
        
        // get the semimajor and semiminor axes from the gcs
        Ellipsoid e = gcs.getDatum().getEllipsoid() ; 
        params.setParameter("semi_minor", e.getSemiMinorAxis()) ; 
        params.setParameter("semi_major", e.getSemiMajorAxis()) ; 
        
        // if latitude of origin is specified, use it.
        addGeoKeyToParameterList("latitude_of_origin", params, 
            GeoTiffIIOMetadataAdapter.ProjNatOriginLatGeoKey, false, 
            Unit.RADIAN, angularUnit) ;

        // if false easting is specified, use it.
        addGeoKeyToParameterList("false_easting", params, 
            GeoTiffIIOMetadataAdapter.ProjFalseEastingGeoKey, false,
            Unit.METRE, linearUnit) ;

        // if false northing is specified, use it.
        addGeoKeyToParameterList("false_northing", params, 
            GeoTiffIIOMetadataAdapter.ProjFalseNorthingGeoKey, false,
            Unit.METRE, linearUnit) ;

        // if central meridian is specified, use it.
        addGeoKeyToParameterList("central_meridian", params, 
            GeoTiffIIOMetadataAdapter.ProjCenterLongGeoKey, false,
            Unit.RADIAN, angularUnit) ;

        // if scale factor is specified, use it.
        addGeoKeyToParameterList("scale_factor", params, 
            GeoTiffIIOMetadataAdapter.ProjScaleAtNatOriginGeoKey, false, null, null) ;

        // if 1st standard parallel is specified, use it
        addGeoKeyToParameterList("standard_parallel_1", params, 
            GeoTiffIIOMetadataAdapter.ProjStdParallel1GeoKey, false,
            Unit.RADIAN, angularUnit) ;
        
        // if 2nd standard parallel is specified, use it.
        addGeoKeyToParameterList("standard_parallel_2", params, 
            GeoTiffIIOMetadataAdapter.ProjStdParallel2GeoKey, false,
            Unit.RADIAN, angularUnit) ;

        // read the CT specific parameters.
        double test = 0 ;
        try { 
            switch (code.shortValue()) { 
                case CT_AlbersEqualArea: 
                    // Albers equal area requires two standard parallels
                    test = params.getDoubleParameter("standard_parallel_1") ; 
                    test = params.getDoubleParameter("standard_parallel_2") ; 
                    break  ;

                case CT_TransverseMercator:
                    // requires scale factor and false easting, read in above
                    test = params.getDoubleParameter("scale_factor") ; 
                    test = params.getDoubleParameter("false_easting") ; 
                    break ; 

                case CT_Orthographic:
                    // requires latitude of origin, read in above
                    test = params.getDoubleParameter("latitude_of_origin") ; 
                    break ; 

                case CT_PolarStereographic:
                case CT_ObliqueStereographic:
                    // both require only scale_factor, read in above
                    test = params.getDoubleParameter("scale_factor") ; 
                    break ; 

                default:
                    throw new GeoTiffException(metadata, "Unrecognized coordinate system code.") ;

            }
        } catch (IllegalArgumentException iae) { 
            IOException io = new GeoTiffException(metadata, "Required parameter not specified for "+classification) ; 
            io.initCause(iae); 
            throw io ; 
        }
        return params ; 
    }
*/

   /* 
    private void addGeoKeyToParameterList(String name, ParameterList params, 
                                int key, boolean mandatory, Unit base, Unit from) throws IOException {
        try {
            String value = metadata.getGeoKey(key) ; 
            if (value != null) {
                double numeric = Double.parseDouble(value) ;
                
                // take care of units conversion if needed.
                if ( (base != null) && (from != null)) {
                    numeric = base.convert(numeric, from) ; 
                }
                params.setParameter(name, numeric) ; 
            } else if (mandatory) { 
                throw new GeoTiffException(metadata, "Mandatory GeoKey is missing") ; 
            }
        } catch (NumberFormatException nfe) {
            IOException ioe = new GeoTiffException(metadata, "Bad data in numeric field.") ; 
            ioe.initCause(nfe) ; 
            throw ioe ; 
        } 
    }
   */
    
}
