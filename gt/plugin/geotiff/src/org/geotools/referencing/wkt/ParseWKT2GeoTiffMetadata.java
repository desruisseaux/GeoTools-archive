/*
 * Created on 2-mag-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.referencing.wkt;

import org.esa.beam.util.geotiff.GeoTIFFCodes;
import org.esa.beam.util.geotiff.GeoTIFFMetadata;

import org.geotools.metadata.iso.citation.CitationImpl;

import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.AbstractIdentifiedObject;
import org.geotools.referencing.NamedIdentifier;
import org.geotools.referencing.datum.BursaWolfParameters;
import org.geotools.referencing.factory.FactoryGroup;
import org.geotools.referencing.wkt.AbstractParser;
import org.geotools.referencing.wkt.Element;

import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.util.Singleton;

import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchIdentifierException;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CompoundCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.DerivedCRS;
import org.opengis.referencing.crs.EngineeringCRS;
import org.opengis.referencing.crs.GeocentricCRS;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.crs.VerticalCRS;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CSFactory;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.datum.Datum;
import org.opengis.referencing.datum.DatumAuthorityFactory;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.EngineeringDatum;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.datum.PrimeMeridian;
import org.opengis.referencing.datum.VerticalDatum;
import org.opengis.referencing.datum.VerticalDatumType;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.OperationMethod;

import java.text.ParseException;
import java.text.ParsePosition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.units.NonSI;
import javax.units.SI;
import javax.units.Unit;


/**
 * @author simone giannecchini
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 *
 * @since 2.2
 *
 * @todo This class is basically a copy of {@link Parser} with calls to {@link GeoTIFFMetadata} in
 *       method body. It would be nice if, instead of setting up {@link GeoTIFFMetadata} during WKT
 *       parsing, we could parse a WKT in the usual way, and then use a different class which
 *       analyse the {@link CoordinateReferenceSystem} object produced. It would be more general
 *       (it would work for any source, not just WKT) and avoid duplication (e.g. bug fixes in WKT
 *       parser not ported in this class). See GEOT-690.
 * @source $URL$
 */
public class ParseWKT2GeoTiffMetadata extends AbstractParser {
    /**
         * Comment for <code>serialVersionUID</code>
         */
    private static final long serialVersionUID = 3976739159681675316L;

    /**
    * A list of predefined coordinate system axis. Returning a pre-defined constant
    * help to compare successfully two CS using {@code equalsIgnoreMetadata} method.
    * Axis appears in preferred order for WKT.
    */
    private static final CoordinateSystemAxis[] GEOTOOLS_AXIS = {
            org.geotools.referencing.cs.DefaultCoordinateSystemAxis.LONGITUDE,
            org.geotools.referencing.cs.DefaultCoordinateSystemAxis.LATITUDE,
            org.geotools.referencing.cs.DefaultCoordinateSystemAxis.GEODETIC_LONGITUDE,
            org.geotools.referencing.cs.DefaultCoordinateSystemAxis.GEODETIC_LATITUDE,
            org.geotools.referencing.cs.DefaultCoordinateSystemAxis.EASTING,
            org.geotools.referencing.cs.DefaultCoordinateSystemAxis.WESTING,
            org.geotools.referencing.cs.DefaultCoordinateSystemAxis.NORTHING,
            org.geotools.referencing.cs.DefaultCoordinateSystemAxis.SOUTHING,
            org.geotools.referencing.cs.DefaultCoordinateSystemAxis.X,
            org.geotools.referencing.cs.DefaultCoordinateSystemAxis.Y,
            org.geotools.referencing.cs.DefaultCoordinateSystemAxis.Z,
            org.geotools.referencing.cs.DefaultCoordinateSystemAxis.GEOCENTRIC_X,
            org.geotools.referencing.cs.DefaultCoordinateSystemAxis.GEOCENTRIC_Y,
            org.geotools.referencing.cs.DefaultCoordinateSystemAxis.GEOCENTRIC_Z,
            org.geotools.referencing.cs.DefaultCoordinateSystemAxis.ALTITUDE,
            org.geotools.referencing.cs.DefaultCoordinateSystemAxis.DEPTH,
            org.geotools.referencing.cs.DefaultCoordinateSystemAxis.ELLIPSOIDAL_HEIGHT,
            org.geotools.referencing.cs.DefaultCoordinateSystemAxis.GRAVITY_RELATED_HEIGHT,
            org.geotools.referencing.cs.DefaultCoordinateSystemAxis.SPHERICAL_LONGITUDE,
            org.geotools.referencing.cs.DefaultCoordinateSystemAxis.SPHERICAL_LATITUDE,
            org.geotools.referencing.cs.DefaultCoordinateSystemAxis.GEOCENTRIC_RADIUS
        };

    /**
     * A predefined empty axis array.
     */
    private static final CoordinateSystemAxis[] NO_AXIS = new CoordinateSystemAxis[0];

    /**
     * The mapping between WKT element name and the object class to be created.
     * Will be created by {@link #getClassOf} only when first needed.
     */
    private static Map types;

    /**
     * The list of {@linkplain AxisDirection axis directions} from their name.
     */
    private final Map directions;

    /**
     * WKT string to parse
     */
    private String WKT;

    /**
     * Metadata to fill.
     */
    private GeoTIFFMetadata metadata;
    private CSFactory csObjFactory;
    private CRSFactory crsObjFactory;
    private DatumFactory datumObjFactory;

    /**
     * Constructs a parser using the default set of symbols and factories.
     */
    public ParseWKT2GeoTiffMetadata(final String WKT, GeoTIFFMetadata metadata) {
        this(WKT, metadata, Symbols.DEFAULT);
    }

    /**
     * @param symbols
     */
    public ParseWKT2GeoTiffMetadata(final String WKT, GeoTIFFMetadata metadata,
        Symbols symbols) {
        super(symbols);
        this.WKT = WKT;
        this.metadata = metadata;

        final AxisDirection[] values = AxisDirection.values();
        directions = new HashMap((int) Math.ceil((values.length + 1) / 0.75f),
                0.75f);

        for (int i = 0; i < values.length; i++) {
            directions.put(values[i].name().trim().toUpperCase(), values[i]);
        }
    }

    /**
     * Parses a coordinate reference system element.
     *
     * @param  text The text to be parsed.
     * @return The coordinate reference system.
     * @throws ParseException if the string can't be parsed.
     */
    public void parseCoordinateReferenceSystem() throws ParseException {
        //get the tree
        final Element element = getTree(this.WKT, new ParsePosition(0));

        //parse the tree and set the metadata
        parseCoordinateReferenceSystem(element);
        element.close();
    }

    /**
     * Parses a coordinate reference system element.
     *
     * @param  parent The parent element.
     * @return The next element as a {@link CoordinateReferenceSystem} object.
     * @throws ParseException if the next element can't be parsed.
     */
    private void parseCoordinateReferenceSystem(final Element element)
        throws ParseException {
        final Object key = element.peek();

        if (key instanceof Element) {
            final String keyword = ((Element) key).keyword.trim().toUpperCase(symbols.locale);

            if ("GEOGCS".equals(keyword)) {
                parseGeoGCS(element);
            }

            if ("PROJCS".equals(keyword)) {
                parseProjCS(element);
            }

            ////
            //            if ("GEOCCS".equals(keyword)) {
            //                parseGeoCCS(element);
            //            }
            //
            //            if ("VERT_CS".equals(keyword)) {
            //                parseVertCS(element);
            //            }
            //
            //            if ("LOCAL_CS".equals(keyword)) {
            //                parseLocalCS(element);
            //            }
            //
            //            if ("COMPD_CS".equals(keyword)) {
            //                parseCompdCS(element);
            //            }
            //
            //            if ("FITTED_CS".equals(keyword)) {
            //                parseFittedCS(element);
            //            }
            return;
        }

        throw element.parseFailed(null,
            Errors.format(ErrorKeys.UNKNOW_TYPE_$1, key));
    }

    /**
     * Parses a "PROJCS" element.
     * This element has the following pattern:
     *
     * <blockquote><code>
     * PROJCS["<name>", <geographic cs>, <projection>, {<parameter>,}*,
     *        <linear unit> {,<twin axes>}{,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "PROJCS" element as a {@link ProjectedCRS} object.
     * @throws ParseException
     * @throws ParseException if the "GEOGCS" element can't be parsed.
     */
    private void parseProjCS(final Element parent) throws ParseException {
    	//user defined projected coordinate reference system.
        metadata.addGeoShortParam(GeoTIFFCodes.ProjectedCSTypeGeoKey, 32767);

        Element element = parent.pullElement("PROJCS");
        String name = element.pullString("name");
        metadata.addGeoAscii(GeoTIFFCodes.GeogCitationGeoKey, name);
        //parse the projection field
        Element elem = element.pullElement("PROJECTION");
        metadata.addGeoAscii(GeoTIFFCodes.PCSCitationGeoKey,elem.pullString("name"));;
        metadata.addGeoShortParam(GeoTIFFCodes.ProjCoordTransGeoKey, 32767);
        metadata.addGeoAscii(GeoTIFFCodes.PCSCitationGeoKey,"");

        //parse thelinear unit
        parseUnit(element,1);
       
        //parse all the parameters
        parseProjParams(element);
        
        //parse the interior geographic crs
        parseGeoGCS(element);        
    }

    /**
     * This fucntion is needed in order to parse all the possible values for the
     * params a certain projection may need.
	 * @param element
	 */
	private void parseProjParams(Element element) {
		
		
	}

	/**
     * Parses a "GEOGCS" element. This element has the following pattern:
     *
     * <blockquote><code>
     * GEOGCS["<name>", <datum>, <prime meridian>, <angular unit>  {,<twin axes>} {,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "GEOGCS" element as a {@link GeographicCRS} object.
     * @throws ParseException if the "GEOGCS" element can't be parsed.
     */
    private void parseGeoGCS(final Element parent) throws ParseException {
        //get the GEO_GCS
        Element element = parent.pullElement("GEOGCS");

        //user defined geographic coordinate reference system.
        metadata.addGeoShortParam(GeoTIFFCodes.GeographicTypeGeoKey, 32767);

        //get the name of the gcs which will become a citation for the user define crs
        String name = element.pullString("name");
        metadata.addGeoAscii(GeoTIFFCodes.GeogCitationGeoKey, name);

        //geodetic datum
        parseDatum(element);
        parseUnit(element,0);
        parsePrimem(element);
        
        Element el=element.pullElement("AXIS");
        el=element.pullElement("AXIS");
        element.close();
    }

    /**
     * Parses a "DATUM" element. This element has the following pattern:
     *
     * <blockquote><code>
     * DATUM["<name>", <spheroid> {,<to wgs84>} {,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @param  meridian the prime meridian.
     * @return The "DATUM" element as a {@link GeodeticDatum} object.
     * @throws ParseException if the "DATUM" element can't be parsed.
     */
    private void parseDatum(final Element parent) throws ParseException {
        //set the datum as user defined
        metadata.addGeoShortParam(GeoTIFFCodes.GeogGeodeticDatumGeoKey, 32767);

        Element element = parent.pullElement("DATUM");
        String name = element.pullString("name");

        //set the name
        metadata.addGeoAscii(GeoTIFFCodes.GeogCitationGeoKey, name);

        //user definde ellipsoid
        metadata.addGeoShortParam(GeoTIFFCodes.GeogEllipsoidGeoKey, 32767);
        parseSpheroid(element);
        element.close();
    }

    /**
     * Parses a "SPHEROID" element. This element has the following pattern:
     *
     * <blockquote><code>
     * SPHEROID["<name>", <semi-major axis>, <inverse flattening> {,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "SPHEROID" element as an {@link Ellipsoid} object.
     * @throws ParseException if the "SPHEROID" element can't be parsed.
     */
    private void parseSpheroid(final Element parent) throws ParseException {
        Element element = parent.pullElement("SPHEROID");
        String name = element.pullString("name");

        //setting the name
        metadata.addGeoAscii(GeoTIFFCodes.GeogCitationGeoKey, name);

        double semiMajorAxis = element.pullDouble("semiMajorAxis");

        //setting semimajor axis
        metadata.addGeoDoubleParam(GeoTIFFCodes.GeogSemiMajorAxisGeoKey,
            semiMajorAxis);

        double inverseFlattening = element.pullDouble("inverseFlattening");

        //setting inverse flattening
        metadata.addGeoDoubleParam(GeoTIFFCodes.GeogInvFlatteningGeoKey,
            inverseFlattening);

        element.close();
    }

    /**
     * Parses a "PRIMEM" element. This element has the following pattern:
     *
     * <blockquote><code>
     * PRIMEM["<name>", <longitude> {,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @param  angularUnit The contextual unit.
     * @return The "PRIMEM" element as a {@link PrimeMeridian} object.
     * @throws ParseException if the "PRIMEM" element can't be parsed.
     */
    private void parsePrimem(final Element parent) throws ParseException {
        Element element = parent.pullElement("PRIMEM");

        //user defined
        metadata.addGeoShortParam(GeoTIFFCodes.GeogPrimeMeridianGeoKey, 32767);

        //citation
        String name = element.pullString("name");
        metadata.addGeoAscii(GeoTIFFCodes.GeogCitationGeoKey, name);

        //longitude
        double longitude = element.pullDouble("longitude");
        metadata.addGeoDoubleParam(GeoTIFFCodes.GeogPrimeMeridianLongGeoKey,
            longitude);
        element.close();
    }

    /**
     * Parses an "UNIT" element.
     * This element has the following pattern:
     *
     * <blockquote><code>
     * UNIT["<name>", <conversion factor> {,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @param  unit The contextual unit. Usually {@link SI#METRE} or {@link SI#RADIAN}.
     * @return The "UNIT" element as an {@link Unit} object.
     * @throws ParseException if the "UNIT" can't be parsed.
     *
     * @todo Authority code is currently ignored. We may consider to create a subclass of
     *       {@link Unit} which implements {@link IdentifiedObject} in a future version.
     */
    private void parseUnit(final Element parent,int model) throws ParseException {
        final Element element = parent.pullElement("UNIT");

        //user defined
        metadata.addGeoShortParam(
        		model==0?GeoTIFFCodes.GeogAngularUnitsGeoKey:GeoTIFFCodes.ProjLinearUnitsGeoKey,
        				32767);

        //citation
        final String name = element.pullString("name");
        metadata.addGeoAscii(
        		model==0?GeoTIFFCodes.GeogCitationGeoKey:GeoTIFFCodes.PCSCitationGeoKey,
        				name);

        final double factor = element.pullDouble("factor");
        metadata.addGeoDoubleParam(
        		model==0?GeoTIFFCodes.GeogAngularUnitSizeGeoKey:GeoTIFFCodes.ProjLinearUnitSizeGeoKey,
            factor);
        element.close();
    }

	protected Object parse(Element arg0) throws ParseException {
		// TODO Auto-generated method stub
		return null;
	}
}
