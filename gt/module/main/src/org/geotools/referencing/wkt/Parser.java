/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2002, Institut de Recherche pour le Développement
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
package org.geotools.referencing.wkt;

// J2SE dependencies
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;

// Parsing
import java.util.Locale;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.ParseException;

// Units
import javax.units.Unit;
import javax.units.SI;
import javax.units.NonSI;

// OpenGIS dependencies
import org.opengis.referencing.cs.*;
import org.opengis.referencing.crs.*;
import org.opengis.referencing.datum.*;
import org.opengis.referencing.FactoryException;

// Geotools dependencies
import org.geotools.referencing.FactoryFinder;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Parser for
 * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
 * Known Text</cite> (WKT)</A>.
 * Instances of this class are thread-safe.
 *
 * @version $Id$
 * @author Remi Eve
 * @author Martin Desruisseaux
 *
 * @see <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html">Well Know Text specification</A>
 * @see <A HREF="http://gdal.velocet.ca/~warmerda/wktproblems.html">OGC WKT Coordinate System Issues</A>
 */
public abstract class Parser extends AbstractParser {
    /**
     * The factory to use for creating {@linkplain Datum datum}.
     */
    private final DatumFactory datumFactory;

    /**
     * The factory to use for creating {@linkplain CoordinateSystem coordinate systems}.
     */
    private final CSFactory csFactory;

    /**
     * The factory to use for creating {@linkplain CoordinateReferenceSystem
     * coordinate reference systems}.
     */
    private final CRSFactory crsFactory;

    /**
     * The list of {@linkplain AxisDirection axis directions} from their name.
     */
    private final Map directions;
    
    /**
     * Construct a parser for the specified locale using default factories.
     *
     * @param locale The locale for parsing and formatting numbers.
     */
    public Parser(final Locale locale) {
        this(locale,
             FactoryFinder.getDatumFactory(),
             FactoryFinder.getCSFactory(),
             FactoryFinder.getCRSFactory());
    }
    
    /**
     * Construct a parser for the specified locale using the specified factories.
     *
     * @param locale       The locale for parsing and formatting numbers.
     * @param datumFactroy The factory to use for creating {@linkplain Datum datum}.
     * @param csFactory    The factory to use for creating {@linkplain CoordinateSystem
     *                     coordinate systems}.
     * @param crsFactory   The factory to use for creating {@linkplain CoordinateReferenceSystem
     *                     coordinate reference systems}.
     */
    public Parser(final Locale             locale,
                  final DatumFactory datumFactory,
                  final CSFactory       csFactory,
                  final CRSFactory     crsFactory)
    {
        super(locale);
        this.datumFactory = datumFactory;
        this. csFactory   =    csFactory;
        this.crsFactory   =   crsFactory;

        final AxisDirection[] values = AxisDirection.values();
        directions = new HashMap((int)Math.ceil((values.length+1)/0.75f), 0.75f);
        for (int i=0; i<values.length; i++) {
            directions.put(values[i].name().trim().toUpperCase(), values[i]);
        }
    }

    /**
     * Parses an <strong>optional</strong> "AUTHORITY" element.
     * This element has the following pattern:
     *
     * <blockquote><code>
     * AUTHORITY["<name>", "<code>"]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @param  parentName The name of the parent object being parsed.
     * @return A properties map with the parent name and the optional autority code.
     * @throws ParseException if the "AUTHORITY" can't be parsed.
     */
    private static Map parseAuthority(final Element parent, final String parentName)
            throws ParseException
    {
        final Map properties = new HashMap(7);
        properties.put("name", parentName);
        final Element element = parent.pullOptionalElement("AUTHORITY");
        if (element != null) {
            properties.put("authority", element.pullString("name"));
            properties.put("code",      element.pullString("code"));
            element.close();
        }
        return properties;
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
     * @param  unit The contextual unit. Usually {@link NonSI#DEGREE_ANGLE} or {@link SI#METRE}.
     * @return The "UNIT" element as an {@link Unit} object.
     * @throws ParseException if the "UNIT" can't be parsed.
     *
     * @todo Authority code is currently ignored. We may consider to create a subclass of
     *       {@link Unit} which implements {@link Info} in a future version.
     */
    private static Unit parseUnit(final Element parent, final Unit unit)
            throws ParseException
    {
        final Element element = parent.pullElement("UNIT");
        final String     name = element.pullString("name");
        final double   factor = element.pullDouble("factor");
        final Map  properties = parseAuthority(element, name);
        element.close();
        return unit.multiply(factor);
    }

    /**
     * Parses an "AXIS" element.
     * This element has the following pattern:
     *
     * <blockquote><code>
     * AXIS["<name>", NORTH | SOUTH | EAST | WEST | UP | DOWN | OTHER]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @param  unit The contextual unit. Usually {@link NonSI#DEGREE_ANGLE} or {@link SI#METRE}.
     * @param  required <code>true</code> if the axis is mandatory,
     *         or <code>false</code> if it is optional.
     * @return The "AXIS" element as a {@link CoordinateSystemAxis} object, or <code>null</code>
     *         if the axis was not required and there is no axis object.
     * @throws ParseException if the "AXIS" element can't be parsed.
     */
    private CoordinateSystemAxis parseAxis(final Element parent, final Unit unit,
                                           final boolean required)
            throws ParseException
    {
        final Element element;
        if (required) {
            element = parent.pullElement("AXIS");
        } else {
            element = parent.pullOptionalElement("AXIS");
            if (element == null) {
                return null;
            }
        }
        final String         name = element.pullString     ("name");
        final Element orientation = element.pullVoidElement("orientation");
        element.close();
        final AxisDirection direction = (AxisDirection) directions.get(orientation.keyword.trim().toUpperCase());
        if (direction == null) {
            throw element.parseFailed(null,
                  Resources.format(ResourceKeys.ERROR_UNKNOW_TYPE_$1, orientation));
        }
        try {
            return csFactory.createCoordinateSystemAxis(Collections.singletonMap("name", name),
                                                        name, direction, unit);
        } catch (FactoryException exception) {
            throw element.parseFailed(exception, null);
        }
    }

    /**
     * Parses a "PRIMEM" element. This element has the following pattern:
     *
     * <blockquote><code>
     * PRIMEM["<name>", <longitude> {,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @param  unit The contextual unit.
     * @return The "PRIMEM" element as a {@link PrimeMeridian} object.
     * @throws ParseException if the "PRIMEM" element can't be parsed.
     */
    private PrimeMeridian parsePrimem(final Element parent, Unit unit) throws ParseException {
        Element   element = parent.pullElement("PRIMEM");
        String       name = element.pullString("name");
        double  longitude = element.pullDouble("longitude");
        Map    properties = parseAuthority(element, name);
        element.close();
        try {
            return datumFactory.createPrimeMeridian(properties, longitude, unit);
        } catch (FactoryException exception) {
            throw element.parseFailed(exception, null);
        }
    }

//    /**
//     * Parses an <strong>optional</strong> "TOWGS84" element.
//     * This element has the following pattern:
//     *
//     * <blockquote><code>
//     * TOWGS84[<dx>, <dy>, <dz>, <ex>, <ey>, <ez>, <ppm>]
//     * </code></blockquote>
//     *
//     * @param  parent The parent element.
//     * @return The "TOWGS84" element as a {@link WGS84ConversionInfo} object,
//     *         or <code>null</code> if no "TOWGS84" has been found.
//     * @throws ParseException if the "TOWGS84" can't be parsed.
//     */
//    private static WGS84ConversionInfo parseToWGS84(final Element parent)
//        throws ParseException 
//    {          
//        final Element element = parent.pullOptionalElement("TOWGS84");
//        if (element == null) {
//            return null;
//        }
//        final WGS84ConversionInfo info = new WGS84ConversionInfo();
//        info.dx  = element.pullDouble("dx");
//        info.dy  = element.pullDouble("dy");
//        info.dz  = element.pullDouble("dz");
//        info.ex  = element.pullDouble("ex");
//        info.ey  = element.pullDouble("ey");
//        info.ez  = element.pullDouble("ez");
//        info.ppm = element.pullDouble("ppm");
//        element.close();
//        return info;
//    }

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
    private Ellipsoid parseSpheroid(final Element parent) throws ParseException {       
        Element          element = parent.pullElement("SPHEROID");
        String              name = element.pullString("name");
        double     semiMajorAxis = element.pullDouble("semiMajorAxis");
        double inverseFlattening = element.pullDouble("inverseFlattening");
        Map           properties = parseAuthority(element, name);
        element.close();
        if (inverseFlattening == 0) {
            // Inverse flattening nul is an OGC convention for a sphere.
            inverseFlattening = Double.POSITIVE_INFINITY;
        }
        try {
            return datumFactory.createFlattenedSphere(properties, semiMajorAxis, inverseFlattening, SI.METER);
        } catch (FactoryException exception) {
            throw element.parseFailed(exception, null);
        }
    }

//    /**
//     * Parses a "PROJECTION" element. This element has the following pattern:
//     *
//     * <blockquote><code>
//     * PROJECTION["<name>" {,<authority>}]
//     * </code></blockquote>
//     *
//     * @param  parent The parent element.
//     * @param  ellipsoid The ellipsoid, or <code>null</code> if none.
//     * @param  unit The linear unit of the parent PROJCS element, or <code>null</code> if none.
//     * @return The "PROJECTION" element as a {@link Projection} object.
//     * @throws ParseException if the "PROJECTION" element can't be parsed.
//     */
//    private Projection parseProjection(final Element parent, final Ellipsoid ellipsoid, final Unit unit)
//        throws ParseException
//    {                
//        final Element   element = parent.pullElement("PROJECTION");
//        final String  classname = element.pullString("name");
//        final CharSequence name = parseAuthority(element, classname);
//        element.close();
//                
//        // Set the list of parameters. NOTE: Parameters are defined in
//        // the parent Element (usually a "PROJCS" element), not in this
//        // "PROJECTION" element.
//        final ParameterList parameters = crsFactory.createProjectionParameterList(classname);
//        Element param;
//        while ((param=parent.pullOptionalElement("PARAMETER")) != null) {
//            String paramName  = param.pullString("name");
//            double paramValue = param.pullDouble("value");
//            Unit   paramUnit  = DescriptorNaming.getParameterUnit(paramName);
//            if (unit!=null && paramUnit!=null && paramUnit.canConvert(unit)) {
//                paramValue = paramUnit.convert(paramValue, unit);
//            }
//            parameters.setParameter(paramName, paramValue);
//        }
//        if (ellipsoid != null) {
//            final Unit axisUnit = ellipsoid.getAxisUnit();
//            parameters.setParameter("semi_major", Unit.METRE.convert(ellipsoid.getSemiMajorAxis(), axisUnit));
//            parameters.setParameter("semi_minor", Unit.METRE.convert(ellipsoid.getSemiMinorAxis(), axisUnit));
//        }
//        try {
//            return crsFactory.createProjection(name, classname, parameters);
//        } catch (FactoryException exception) {
//            throw element.parseFailed(exception, null);
//        }
//    }
//
//    /**
//     * Parses a "DATUM" element. This element has the following pattern:
//     *
//     * <blockquote><code>
//     * DATUM["<name>", <spheroid> {,<to wgs84>} {,<authority>}]
//     * </code></blockquote>
//     *
//     * @param  parent The parent element.
//     * @return The "DATUM" element as a {@link HorizontalDatum} object.
//     * @throws ParseException if the "DATUM" element can't be parsed.
//     */
//    private HorizontalDatum parseDatum(final Element parent) throws ParseException {
//        Element             element = parent.pullElement("DATUM");
//        CharSequence           name = element.pullString("name");
//        Ellipsoid         ellipsoid = parseSpheroid(element);
//        WGS84ConversionInfo toWGS84 = parseToWGS84(element); // Optional; may be null.
//        name = parseAuthority(element, name);
//        element.close();
//        try {
//            return crsFactory.createHorizontalDatum(name, DatumType.GEOCENTRIC, ellipsoid, toWGS84);
//        } catch (FactoryException exception) {
//            throw element.parseFailed(exception, null);
//        }
//    }        

    /**
     * Parses a "VERT_DATUM" element. This element has the following pattern:
     *
     * <blockquote><code>
     * VERT_DATUM["<name>", <datum type> {,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "VERT_DATUM" element as a {@link VerticalDatum} object.
     * @throws ParseException if the "VERT_DATUM" element can't be parsed.
     */
    private VerticalDatum parseVertDatum(final Element parent) throws ParseException {        
        Element element = parent.pullElement("VERT_DATUM");
        String     name = element.pullString ("name");
        final int datum = element.pullInteger("datum");
        Map  properties = parseAuthority(element, name);
        element.close();
        final VerticalDatumType type = org.geotools.referencing.datum.VerticalDatum
                                       .getVerticalDatumTypeFromLegacyCode(datum);
        if (type == null) {
            throw element.parseFailed(null,
                  Resources.format(ResourceKeys.ERROR_UNKNOW_TYPE_$1, new Integer(datum)));
        }
        try {
            return datumFactory.createVerticalDatum(properties, type);
        } catch (FactoryException exception) {
            throw element.parseFailed(exception, null);
        }
    }

    /**
     * Parses a "LOCAL_DATUM" element. This element has the following pattern:
     *
     * <blockquote><code>
     * LOCAL_DATUM["<name>", <datum type> {,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "LOCAL_DATUM" element as an {@link EngineeringDatum} object.
     * @throws ParseException if the "LOCAL_DATUM" element can't be parsed.
     *
     * @todo The vertical datum type is currently ignored.
     */
    private EngineeringDatum parseLocalDatum(final Element parent) throws ParseException {
        Element element = parent.pullElement("LOCAL_DATUM");
        String     name = element.pullString ("name");
        final int datum = element.pullInteger("datum");
        Map  properties = parseAuthority(element, name);
        element.close();
        try {
            return datumFactory.createEngineeringDatum(properties);
        } catch (FactoryException exception) {
            throw element.parseFailed(exception, null);
        }
    }

    /**
     * Parses a "LOCAL_CS" element.
     * This element has the following pattern:
     *
     * <blockquote><code>
     * LOCAL_CS["<name>", <local datum>, <unit>, <axis>, {,<axis>}* {,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "LOCAL_CS" element as an {@link EngineeringCRS} object.
     * @throws ParseException if the "LOCAL_CS" element can't be parsed.
     *
     * @todo The coordinate system used is always a Geotools implementation, since we don't
     *       know which method to invokes in the {@link CSFactory} (is it a cartesian
     *       coordinate system? a spherical one? etc.).
     */
    private EngineeringCRS parseLocalCS(final Element parent) throws ParseException {        
        Element           element = parent.pullElement("LOCAL_CS");
        String               name = element.pullString("name");
        EngineeringDatum    datum = parseLocalDatum(element);
        Unit                 unit = parseUnit(element, SI.METER);
        CoordinateSystemAxis axis = parseAxis(element, unit, true);
        List                 list = new ArrayList();
        do {
            list.add(axis);
            axis = parseAxis(element, unit, false);
        } while (axis != null);
        Map properties = parseAuthority(element, name);
        element.close();

        final CoordinateSystem cs;
        cs = new org.geotools.referencing.cs.CoordinateSystem(
                 Collections.singletonMap("name", name),
                 (CoordinateSystemAxis[]) list.toArray(new CoordinateSystemAxis[list.size()]));
        try {
            return crsFactory.createEngineeringCRS(properties, datum, cs);
        } catch (FactoryException exception) {
            throw element.parseFailed(exception, null);
        }
    }        

    /**
     * Parses a "GEOCCS" element.
     * This element has the following pattern:
     *
     * <blockquote><code>
     * GEOCCS["<name>", <datum>, <prime meridian>,  <linear unit>
     *        {,<axis> ,<axis> ,<axis>} {,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "GEOCCS" element as a {@link GeocentricCRS} object.
     * @throws ParseException if the "GEOCCS" element can't be parsed.
     */
//    private GeocentricCRS parseGeoCCS(final Element parent) throws ParseException {        
//        Element        element = parent.pullElement("GEOCCS");
//        String            name = element.pullString("name");
//        HorizontalDatum  datum = parseDatum (element);
//        PrimeMeridian meridian = parsePrimem(element, NonSI.DEGREE_ANGLE);
//        Unit              unit = parseUnit  (element, SI.METER);
//        AxisInfo[] axes = new AxisInfo[3];
//        axes[0] = parseAxis(element, false);
//        if (axes[0] != null) {
//            axes[1] = parseAxis(element, true);
//            axes[2] = parseAxis(element, true);
//        }
//        else {
//            axes = GeocentricCoordinateSystem.DEFAULT_AXIS;
//        }
//        name = parseAuthority(element, name);                
//        element.close();
//        try {
//            return crsFactory.createGeocentricCoordinateSystem(name, unit, datum, meridian, axes);
//        } catch (FactoryException exception) {
//            throw element.parseFailed(exception, null);
//        }
//    }        

    /**
     * Parses an <strong>optional</strong> "VERT_CS" element.
     * This element has the following pattern:
     *
     * <blockquote><code>
     * VERT_CS["<name>", <vert datum>, <linear unit>, {<axis>,} {,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "VERT_CS" element as a {@link VerticalCRS} object.
     * @throws ParseException if the "VERT_CS" element can't be parsed.
     */
    private VerticalCRS parseVertCS(final Element parent) throws ParseException { 
        final Element element = parent.pullElement("VERT_CS");
        if (element == null) {
            return null;
        }
        String               name = element.pullString("name");
        VerticalDatum       datum = parseVertDatum(element);
        Unit                 unit = parseUnit(element, SI.METER);
        CoordinateSystemAxis axis = parseAxis(element, unit, false);
        Map            properties = parseAuthority(element, name);
        element.close();
        if (axis == null) {
            axis = org.geotools.referencing.cs.CoordinateSystemAxis.ALTITUDE;
        }
        try {
            return crsFactory.createVerticalCRS(properties, datum,
                    csFactory.createVerticalCS(Collections.singletonMap("name", name), axis));
        } catch (FactoryException exception) {
            throw element.parseFailed(exception, null);
        }
    }

//    /**
//     * Parses a "GEOGCS" element. This element has the following pattern:
//     *
//     * <blockquote><code>
//     * GEOGCS["<name>", <datum>, <prime meridian>, <angular unit>  {,<twin axes>} {,<authority>}]
//     * </code></blockquote>
//     *
//     * @param  parent The parent element.
//     * @return The "GEOGCS" element as a {@link GeographicCoordinateSystem} object.
//     * @throws ParseException if the "GEOGCS" element can't be parsed.
//     */
//    private GeographicCoordinateSystem parseGeoGCS(final Element parent) throws ParseException {
//        Element        element = parent.pullElement("GEOGCS");
//        CharSequence      name = element.pullString("name");
//        HorizontalDatum  datum = parseDatum(element);
//        Unit              unit = parseUnit(element, Unit.RADIAN);
//        PrimeMeridian meridian = parsePrimem(element, unit);
//        AxisInfo         axis0 = parseAxis(element, false);
//        AxisInfo         axis1;
//        if (axis0 != null) {
//            axis1 = parseAxis(element, true);
//        } else {
//            axis0 = AxisInfo.LONGITUDE;
//            axis1 = AxisInfo.LATITUDE;
//        }
//        name = parseAuthority(element, name);
//        element.close();
//        try {
//            return crsFactory.createGeographicCoordinateSystem(name, unit, datum, meridian, axis0, axis1);
//        } catch (FactoryException exception) {
//            throw element.parseFailed(exception, null);
//        }
//    }
//
//    /**
//     * Parses a "PROJCS" element.
//     * This element has the following pattern:
//     *
//     * <blockquote><code>
//     * PROJCS["<name>", <geographic cs>, <projection>, {<parameter>,}*,
//     *        <linear unit> {,<twin axes>}{,<authority>}]
//     * </code></blockquote>
//     *
//     * @param  parent The parent element.
//     * @return The "PROJCS" element as a {@link ProjectedCoordinateSystem} object.
//     * @throws ParseException if the "GEOGCS" element can't be parsed.
//     */
//    private ProjectedCoordinateSystem parseProjCS(final Element parent) throws ParseException {
//        Element                element = parent.pullElement("PROJCS");
//        CharSequence              name = element.pullString("name");
//        GeographicCoordinateSystem gcs = parseGeoGCS(element);
//        Ellipsoid            ellipsoid = gcs.getHorizontalDatum().getEllipsoid();
//        Unit                      unit = parseUnit(element, Unit.METRE);
//        Projection          projection = parseProjection(element, ellipsoid, unit);
//        AxisInfo                 axis0 = parseAxis(element, false);
//        AxisInfo                 axis1;
//        if (axis0 != null) {
//            axis1 = parseAxis(element, true);
//        } else {
//            axis0 = AxisInfo.X;
//            axis1 = AxisInfo.Y;
//        }
//        name = parseAuthority(element, name);
//        element.close();
//        try {
//            return crsFactory.createProjectedCoordinateSystem(name, gcs, projection, unit, axis0, axis1);      
//        } catch (FactoryException exception) {
//            throw element.parseFailed(exception, null);
//        }
//    }        
//
//    /**
//     * Parses a "COMPD_CS" element.
//     * This element has the following pattern:
//     *
//     * <blockquote><code>
//     * COMPD_CS["<name>", <head cs>, <tail cs> {,<authority>}]
//     * </code></blockquote>
//     *
//     * @param  parent The parent element.
//     * @return The "COMPD_CS" element as a {@link CompoundCoordinateSystem} object.
//     * @throws ParseException if the "COMPD_CS" element can't be parsed.
//     */
//    private CompoundCoordinateSystem parseCompdCS(final Element parent) throws ParseException
//    {        
//        Element         element = parent.pullElement("COMPD_CS");
//        CharSequence       name = element.pullString("name");
//        CoordinateSystem headCS = parseCoordinateSystem(element);
//        CoordinateSystem tailCS = parseCoordinateSystem(element);
//        name = parseAuthority(element, name);
//        element.close();
//        try {
//            return crsFactory.createCompoundCoordinateSystem(name, headCS, tailCS);
//        } catch (FactoryException exception) {
//            throw element.parseFailed(exception, null);
//        }
//    }
//    
//    /**
//     * Parses a coordinate system element.
//     *
//     * @param  parent The parent element.
//     * @return The next element as a {@link CoordinateSystem} object.
//     * @throws ParseException if the next element can't be parsed.
//     */
//    private CoordinateSystem parseCoordinateSystem(final Element element) throws ParseException
//    {
//        final Object key = element.peek();
//        if (key instanceof Element) {
//            final String keyword = ((Element) key).keyword.trim().toUpperCase(locale);
//            if (  "GEOGCS".equals(keyword)) return parseGeoGCS (element);
//            if (  "PROJCS".equals(keyword)) return parseProjCS (element);
//            if (  "GEOCCS".equals(keyword)) return parseGeoCCS (element);
//            if ( "VERT_CS".equals(keyword)) return parseVertCS (element);
//            if ("LOCAL_CS".equals(keyword)) return parseLocalCS(element);
//            if ("COMPD_CS".equals(keyword)) return parseCompdCS(element);
//        }
//        throw element.parseFailed(null, Resources.format(ResourceKeys.ERROR_UNKNOW_TYPE_$1, key));
//    }
//
//    /**
//     * Parses the next element in the specified <cite>Well Know Text</cite> (WKT) tree.
//     *
//     * @param  element The element to be parsed.
//     * @return The object.
//     * @throws ParseException if the element can't be parsed.
//     */
//    protected Object parse(final Element element) throws ParseException {
//        final Object key = element.peek();
//        if (key instanceof Element) {
//            final String keyword = ((Element) key).keyword.trim().toUpperCase(locale);
//            if (       "AXIS".equals(keyword)) return parseAxis      (element, true);
//            if (     "PRIMEM".equals(keyword)) return parsePrimem    (element, Unit.DEGREE);
//            if (    "TOWGS84".equals(keyword)) return parseToWGS84   (element);
//            if (   "SPHEROID".equals(keyword)) return parseSpheroid  (element);
//            if (      "DATUM".equals(keyword)) return parseDatum     (element);
//            if ( "VERT_DATUM".equals(keyword)) return parseVertDatum (element);
//            if ("LOCAL_DATUM".equals(keyword)) return parseLocalDatum(element);
//        }
//        return parseCoordinateSystem(element);
//    }
//
//    /**
//     * Parses a coordinate system element.
//     *
//     * @param  text The text to be parsed.
//     * @return The coordinate system.
//     * @throws ParseException if the string can't be parsed.
//     */
//    public CoordinateSystem parseCoordinateSystem(final String text) throws ParseException {
//        final Element element = getTree(text, new ParsePosition(0));
//        final CoordinateSystem cs = parseCoordinateSystem(element);
//        element.close();
//        return cs;
//    }
//
//    /**
//     * Format the specified object. Current implementation just append {@link Object#toString},
//     * since the <code>toString()</code> implementation for most {@link org.geotools.cs.Info}
//     * objects is to returns a WKT.
//     *
//     * @todo Provides pacakge private <code>Info.toString(AbstractParser)</code> implementations.
//     *       It would allows us to invoke <code>((Info)obj).toString(this)</code> here.
//     */
//    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
//        return toAppendTo.append(obj);
//    }
}
