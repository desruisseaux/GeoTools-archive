/* *    Geotools2 - OpenSource mapping toolkit *    http://geotools.org *    (C) 2002, Geotools Project Managment Committee (PMC) * *    This library is free software; you can redistribute it and/or *    modify it under the terms of the GNU Lesser General Public *    License as published by the Free Software Foundation; *    version 2.1 of the License. * *    This library is distributed in the hope that it will be useful, *    but WITHOUT ANY WARRANTY; without even the implied warranty of *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU *    Lesser General Public License for more details. * */
package org.geotools.data.oracle;

import java.util.ArrayList;import java.util.List;import java.util.logging.Logger;import oracle.sdoapi.OraSpatialManager;import oracle.sdoapi.adapter.GeometryAdapter;import oracle.sdoapi.adapter.GeometryInputTypeNotSupportedException;import oracle.sdoapi.adapter.GeometryOutputTypeNotSupportedException;import oracle.sdoapi.geom.CoordPoint;import oracle.sdoapi.geom.CoordPointImpl;import oracle.sdoapi.geom.CurveString;import oracle.sdoapi.geom.Geometry;import oracle.sdoapi.geom.InvalidGeometryException;import oracle.sdoapi.sref.SpatialReference;import com.vividsolutions.jts.geom.Coordinate;import com.vividsolutions.jts.geom.GeometryCollection;import com.vividsolutions.jts.geom.LinearRing;

/**
 * Adapter class that handles the conversion between SDO and JTS geometry classes.
 *
 * @author Sean Geoghegan, Defence Science and Technology Organisation
 * @author $Author: seangeo $
 * @version $Id: AdapterJTS.java,v 1.4 2003/08/08 07:37:18 seangeo Exp $
 */
public class AdapterJTS implements GeometryAdapter {
    /** Logger for logging messages */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data.oracle.AdapterJTS");

    /** The name of the format we convert to. */
    private static final String FORMAT_NAME = "JTS";

    /** The version of the format to convert - currently JTS-1.3 */
    private static final String FORMAT_VERSION = "1.3";

    /** The Supported output type.  The JTS Geometry class will catch all */
    private static final Class[] SUPPORTED_OUTPUT = {com.vividsolutions.jts.geom.Geometry.class};

    /** Supported input.  We can convert from JTS Geometry */
    private static final Class[] SUPPORTED_INPUT = {com.vividsolutions.jts.geom.Geometry.class};

    /** Supported Pass through - none. */
    private static final Class[] SUPPORTED_PASS_THROUGH = new Class[0];

    /** Factory for creating JTSGeometries */
    private com.vividsolutions.jts.geom.GeometryFactory jtsFactory = null;

    /** Factory for creating SDO Geometries */
    private oracle.sdoapi.geom.GeometryFactory sdoFactory = null;

    /**
     * Creates a new AdapterJTS that uses the default SDO and JTS geometry factories.
     */
    public AdapterJTS() {
        this(new com.vividsolutions.jts.geom.GeometryFactory(),
            OraSpatialManager.getGeometryFactory());
    }

    /**
     * Creates a new AdapterJTS that uses the supplied JTS geometry factory and the default SDO
     * geometry factory.
     *
     * @param jtsFactory The JTS geometry factory.
     */
    public AdapterJTS(com.vividsolutions.jts.geom.GeometryFactory jtsFactory) {
        this(jtsFactory, OraSpatialManager.getGeometryFactory());
    }

    /**
     * Creates a new AdapterJTS that uses the supplied geometry factories.
     *
     * @param jtsFactory The JTS geometry factory.
     * @param sdoFactory The SDO geometry factory.
     */
    public AdapterJTS(com.vividsolutions.jts.geom.GeometryFactory jtsFactory,
        oracle.sdoapi.geom.GeometryFactory sdoFactory) {
        this.jtsFactory = jtsFactory;

        this.sdoFactory = sdoFactory;
    }

    /**
     * Creates a new AdapterJTS that uses the supplied SDO geometry Factory and the default JTS
     * geometry factory.
     *
     * @param sdoFactory The SDO geometry factory.
     */
    public AdapterJTS(oracle.sdoapi.geom.GeometryFactory sdoFactory) {
        this(new com.vividsolutions.jts.geom.GeometryFactory(), sdoFactory);
    }

    /**
     * Converts an Oracle SDO Geometry into a JTS Geometry.
     *
     * @param outputType The output type.  This must be one of the supported output types.
     * @param geom The SDO Geometry to convert.
     *
     * @return The JTS geometry.
     *
     * @throws InvalidGeometryException Throw in the input geometry cannot be converted.
     * @throws GeometryOutputTypeNotSupportedException Throw if the outputType is not supported.
     *
     * @see oracle.sdoapi.adapter.GeometryAdapter#exportGeometry(java.lang.Class,
     *      oracle.sdoapi.geom.Geometry)
     */
    public Object exportGeometry(Class outputType, Geometry geom)
        throws InvalidGeometryException, GeometryOutputTypeNotSupportedException {
        if (!outputTypeSupported(outputType)) {
            throw new GeometryOutputTypeNotSupportedException("Output not supported");
        }

        com.vividsolutions.jts.geom.Geometry returnGeometry = sdoToJts(geom);

        if (!outputType.isInstance(returnGeometry)) {
            throw new GeometryOutputTypeNotSupportedException(
                "AdapterJTS does not support converting" + "from " + geom.getClass().getName()                 + " to " + outputType.getName());
        }

        return sdoToJts(geom);
    }

    /**
     * Not supported by this implementation.  This adapter does not support converting SDO
     * geometries into existing objects. Calling this method will throw a
     * GeometryOutputTypeNotSupportedException.
     * 
     * <p>
     * This method is not implemented because it is not needed by the OracleDataSource.  Since the
     * OracleDataSource is the only Geotools, entry point into Oracle Spatial Database leaving
     * this as an unsupported operation is safe.
     * </p>
     *
     * @param outputObject The object to output to.
     * @param geom The SDO Geometry to convert.
     *
     * @throws InvalidGeometryException Throw in the input geometry cannot be converted.
     * @throws GeometryOutputTypeNotSupportedException Throw if the outputType is not supported.
     *
     * @see oracle.sdoapi.adapter.GeometryAdapter#exportGeometry(java.lang.Object,
     *      oracle.sdoapi.geom.Geometry)
     */
    public void exportGeometry(Object outputObject, Geometry geom)
        throws InvalidGeometryException, GeometryOutputTypeNotSupportedException {
        throw new GeometryOutputTypeNotSupportedException(
            "AdapterJTS does not support exporting to existing objects");
    }

    /**
     * Converts a JTS geometry to an SDO geometry.
     *
     * @param inputSource The JTS geometry
     *
     * @return An SDO geometry.
     *
     * @throws InvalidGeometryException Throw in the input geometry cannot be converted.
     * @throws GeometryInputTypeNotSupportedException Thrown if the inputType is not supported.
     *
     * @see oracle.sdoapi.adapter.GeometryAdapter#importGeometry(java.lang.Object)
     */
    public Geometry importGeometry(Object inputSource)
        throws InvalidGeometryException, GeometryInputTypeNotSupportedException {
        if (!inputTypeSupported(inputSource.getClass())) {
            throw new GeometryInputTypeNotSupportedException("AdapterJTS does not support "                 + inputSource.getClass().getName());
        }

        return jtsToSdo((com.vividsolutions.jts.geom.Geometry) inputSource);
    }

    /**
     * This method redirects to importGeometry(Object) since dimemsionality is ignored.  We can
     * ignore dimensionality since both JTS and the SDO API only support 3D coordinates, therefore
     * if we try to put a 2D JTS geometry within a 3D SDO Geometry, the z coordinate will be left
     * as 0.
     *
     * @param inputSource The input JTS geometry.
     * @param nDim IGNORED
     *
     * @return The SDO geometry.
     *
     * @throws InvalidGeometryException Throw in the input geometry cannot be converted.
     * @throws GeometryInputTypeNotSupportedException Throw if the input Type is not supported.
     *
     * @see oracle.sdoapi.adapter.GeometryAdapter#importGeometry(java.lang.Object, int)
     */
    public Geometry importGeometry(Object inputSource, int nDim)
        throws InvalidGeometryException, GeometryInputTypeNotSupportedException {
        // ignore dimensionality
        return importGeometry(inputSource);
    }

    /**
     * Determines whether this adapter supports conversion to SDO Geometry from the given class.
     *
     * @param type The class to test for conversion support.
     *
     * @return True if the adapter can convert type to an SDO geometry.
     *
     * @see oracle.sdoapi.adapter.GeometryAdapter#inputTypeSupported(java.lang.Class)
     * @see #getSupportedInputTypes()
     */
    public boolean inputTypeSupported(Class type) {
        return com.vividsolutions.jts.geom.Geometry.class.isAssignableFrom(type);
    }

    /**
     * Determines whether this adapter can convert SDO geometries to the given type.
     *
     * @param type The class to test for conversion support
     *
     * @return True if the adapter can convert SDO geometries to type.
     *
     * @see oracle.sdoapi.adapter.GeometryAdapter#outputTypeSupported(java.lang.Class)
     * @see #getSupportedOutputTypes()
     */
    public boolean outputTypeSupported(Class type) {
        return com.vividsolutions.jts.geom.Geometry.class.isAssignableFrom(type);
    }

    /**
     * Always returns false since this adpater does not support any passthrough conversion.
     *
     * @param type The type to check
     *
     * @return Always false.
     *
     * @see oracle.sdoapi.adapter.GeometryAdapter#passthroughOutputTypeSupported(java.lang.Class)
     */
    public boolean passthroughOutputTypeSupported(Class type) {
        return false;
    }

    /**
     * Returns the supported input types.
     *
     * @return The types that this adapter can convert to SDO Geometry objects. In this case we
     *         support importing from com.vividsolutions.jts.geom.Geometry objects.
     *
     * @see oracle.sdoapi.adapter.GeometryAdapter#getSupportedInputTypes()
     */
    public Class[] getSupportedInputTypes() {
        return SUPPORTED_INPUT;
    }

    /**
     * Returns the supported output types.
     *
     * @return The types that this adapter can convert SDO Geometry object to. In this case we
     *         support exporting to com.vividsolutiions.jts.geom.Geometry objects.
     *
     * @see oracle.sdoapi.adapter.GeometryAdapter#getSupportedOutputTypes()
     */
    public Class[] getSupportedOutputTypes() {
        return SUPPORTED_OUTPUT;
    }

    /**
     * Gets the supported pass through output types.  This adapter does not support pass through
     * types.
     *
     * @return An empty Class[].
     *
     * @see oracle.sdoapi.adapter.GeometryAdapter#getSupportedPassthroughOutputTypes()
     */
    public Class[] getSupportedPassthroughOutputTypes() {
        return SUPPORTED_PASS_THROUGH;
    }

    /**
     * Gets the format name that this adapter supports.
     *
     * @return The format name.  In this case "JTS".
     *
     * @see oracle.sdoapi.adapter.GeometryAdapter#getFormatName()
     */
    public String getFormatName() {
        return FORMAT_NAME;
    }

    /**
     * Gets the format version.
     *
     * @return The format version.  In this case "1.3"
     *
     * @see oracle.sdoapi.adapter.GeometryAdapter#getFormatVersion()
     */
    public String getFormatVersion() {
        return FORMAT_VERSION;
    }

    /**
     * Sets the default SRS.
     *
     * @param sr The new default spatial reference system id.
     *
     * @see oracle.sdoapi.adapter.GeometryAdapter#setDefaultSRS(oracle.sdoapi.sref.SpatialReference)
     */
    public void setDefaultSRS(SpatialReference sr) {
        sdoFactory.setSpatialReference(sr);
    }

    /**
     * Gets the default SpatialReference.
     *
     * @return The SpatialReference of the SDO geometry Factory.
     */
    public SpatialReference getDefaultSRS() {
        return sdoFactory.getSpatialReference();
    }

//J-    /**     * Converts an SDO Geometry to a JTS Geomtry.     *     * @param geom The SDO Geometry.     *     * @return The JTS Geometry.     *     * @throws InvalidGeometryException If the geometry can no be converted.     */    private com.vividsolutions.jts.geom.Geometry sdoToJts(Geometry geom)        throws InvalidGeometryException {        Class geometryType = geom.getGeometryType();        com.vividsolutions.jts.geom.Geometry output = null;                if (geometryType.equals(oracle.sdoapi.geom.Point.class)) {            oracle.sdoapi.geom.Point point = (oracle.sdoapi.geom.Point) geom;            output = jtsFactory.createPoint(new Coordinate(point.getX(),                                                           point.getY(),                                                           point.getZ()));        } else if (geometryType.equals(oracle.sdoapi.geom.LineString.class)) {            oracle.sdoapi.geom.LineString line = (oracle.sdoapi.geom.LineString) geom;            output = jtsFactory.createLineString(coordPointsToCoordinates(line.getPointArray(),                                                                     line.getDimensionality()));        } else if (geometryType.equals(oracle.sdoapi.geom.CurveString.class)) {            oracle.sdoapi.geom.CurveString curve = (oracle.sdoapi.geom.CurveString) geom;            output = jtsFactory.createLineString(curveStringToCoordinates(curve));        } else if (geometryType.equals(oracle.sdoapi.geom.Polygon.class)) {            oracle.sdoapi.geom.Polygon polygon = (oracle.sdoapi.geom.Polygon) geom;            Coordinate[] exteriorRingCoords = curveStringToCoordinates(polygon.getExteriorRing());            LinearRing exterior = jtsFactory.createLinearRing(exteriorRingCoords);            LinearRing[] interiors = curveStringsToLinearRings(polygon.getInteriorRingArray());            output = jtsFactory.createPolygon(exterior, interiors);        } else if (geometryType.equals(oracle.sdoapi.geom.CurvePolygon.class)) {            oracle.sdoapi.geom.CurvePolygon curvePolygon = (oracle.sdoapi.geom.CurvePolygon) geom;            Coordinate[] exteriorRingCoords = curveStringToCoordinates(curvePolygon.getExteriorRing());            LinearRing exterior = jtsFactory.createLinearRing(exteriorRingCoords);            LinearRing[] interiors = curveStringsToLinearRings(curvePolygon.getInteriorRingArray());            output = jtsFactory.createPolygon(exterior, interiors);        } else if (oracle.sdoapi.geom.GeometryCollection.class.isAssignableFrom(geometryType)) {            // This is a catch all for geometry collections            oracle.sdoapi.geom.GeometryCollection geomCollection =                                     (oracle.sdoapi.geom.GeometryCollection) geom;            List geometries = new ArrayList(geomCollection.getNumGeometries());            for (int i = 0; i < geomCollection.getNumGeometries(); i++) {                geometries.add(sdoToJts(geomCollection.getGeometryAt(i)));            }            output = jtsFactory.buildGeometry(geometries);        } else {            String message = "Got a geometry that I don't know how to handle: "                             +  geometryType.getName();            LOGGER.warning(message);            throw new InvalidGeometryException(message);        }        /* Can set this at the end because it is just an internal ID number */        output.setSRID(geom.getSpatialReference().getID());        return output;    }//J+

    /**
     * Converts an array of SDO CoordPoints to an Array of JTS Coordinates.
     *
     * @param coordPoints The array of SDO CoordPoints.
     * @param nDim The dimensionality of the coordinates.
     *
     * @return The Array of JTS Coordinates.
     *
     * @throws InvalidGeometryException If the coordPoints are invalid.
     */
    private Coordinate[] coordPointsToCoordinates(CoordPoint[] coordPoints, int nDim)
        throws InvalidGeometryException {
        if ((nDim != 3) && (nDim != 2)) {
            String message = "nDim was not 2 or 3. nDim = " + nDim;

            LOGGER.warning(message);

            throw new InvalidGeometryException(message);
        }

        Coordinate[] coordinates = new Coordinate[coordPoints.length];

        for (int i = 0; i < coordPoints.length; i++) {
            CoordPoint cp = coordPoints[i];
            coordinates[i] = new Coordinate(cp.getX(), cp.getY(), cp.getZ());
        }

        return coordinates;
    }

    /**
     * Converts an SDO CurveString to an array of JTS Coordinates.
     *
     * @param cs The SDO CurveString.
     *
     * @return An Array of JTS Coordinates.
     *
     * @throws InvalidGeometryException If the CurveString is invalid.
     */
    private Coordinate[] curveStringToCoordinates(CurveString cs) throws InvalidGeometryException {
        return coordPointsToCoordinates(cs.getPointArray(), cs.getDimensionality());
    }

    /**
     * Converts SDO CurveStrings to JTS LineStrings.
     *
     * @param curveStrings An array of SDO CurveStrings to convert.
     *
     * @return An array of JTS LineStrings.
     *
     * @throws InvalidGeometryException If the CurveString is invalid.
     */
    private LinearRing[] curveStringsToLinearRings(CurveString[] curveStrings)
        throws InvalidGeometryException {
        if (curveStrings == null) {
            return new LinearRing[0];
        }

        LinearRing[] rings = new LinearRing[curveStrings.length];

        for (int i = 0; i < curveStrings.length; i++) {
            rings[i] = jtsFactory.createLinearRing(curveStringToCoordinates(curveStrings[i]));
        }

        return rings;
    }

    //J-    /**     * Converts a JTS Geometry to an SDO Geometry.     *     * @param geom The JTS geometry to convert.     *     * @return The SDO geometry.     *     * @throws InvalidGeometryException If the geometry created from the     * JTS geometry is invalid.     *     * @task REVISIT: Need to work out how to set the SRID properly. JTS stores     *       the SRID as an integer. When converting from an SDO to a JTS we     *       can just set this to the SRID from the SDO.  However, since SRIDs     *       need to map to the SRID table in Oracle, when converting from a     *       JTS we don't know if the SRID exists because it could have been     *       set on the JTS geom anywhere.  At this stage, just hope the     *       sdoFactory chooses a sensible default.     */    private oracle.sdoapi.geom.Geometry jtsToSdo(com.vividsolutions.jts.geom.Geometry geom)            throws InvalidGeometryException {        Class geometryType = geom.getClass();        oracle.sdoapi.geom.Geometry output = null;        if (geometryType.equals(com.vividsolutions.jts.geom.Point.class)) {            com.vividsolutions.jts.geom.Point point = (com.vividsolutions.jts.geom.Point) geom;            Coordinate coordinate = point.getCoordinate();            output = sdoFactory.createPoint(coordinatesToCoordPoint(coordinate, point.getDimension()));        } else if (geometryType.equals(com.vividsolutions.jts.geom.LineString.class)) {            com.vividsolutions.jts.geom.LineString line = (com.vividsolutions.jts.geom.LineString) geom;            output = lineStringJtsToSdo(line);        } else if (geometryType.equals(com.vividsolutions.jts.geom.Polygon.class)) {            com.vividsolutions.jts.geom.Polygon polygon = (com.vividsolutions.jts.geom.Polygon) geom;            oracle.sdoapi.geom.LineString exteriorRing = lineStringJtsToSdo(polygon.getExteriorRing());            oracle.sdoapi.geom.LineString[] interiorRings =                     new oracle.sdoapi.geom.LineString[polygon.getNumInteriorRing()];            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {                interiorRings[i] = lineStringJtsToSdo(polygon.getInteriorRingN(i));            }            output = sdoFactory.createPolygon(exteriorRing, interiorRings);        } else if (GeometryCollection.class.isAssignableFrom(geom.getClass())) {            GeometryCollection geomCollection = (GeometryCollection) geom;            ArrayList sdoGeometries = new ArrayList();            for (int i = 0; i < geomCollection.getNumGeometries(); i++) {                sdoGeometries.add(jtsToSdo(geomCollection.getGeometryN(i)));            }            Geometry[] sdoGeomArray = (Geometry[]) sdoGeometries.toArray(new oracle.sdoapi.geom.Geometry[0]);            output = sdoFactory.createGeometryCollection(sdoGeomArray);        } else {            String message = "Got a geometry that I don't know how to handle: "                         + geometryType.getName();            LOGGER.warning(message);            throw new InvalidGeometryException(message);        }        return output;    }    //J+

    /**
     * Converts a JTS coordinate to an SDO CoordPoint.
     *
     * @param coordinate The JTS coordinate.
     * @param dim The dimensionality of the coordinate.
     *
     * @return The new SDO CoordPoint.
     */
    private CoordPoint coordinatesToCoordPoint(Coordinate coordinate, int dim) {
        CoordPointImpl coordPoint = new CoordPointImpl(coordinate.x, coordinate.y, coordinate.z);

        return coordPoint;
    }

    /**
     * Converts a JTS LineString into an SDO LineString.
     *
     * @param line The JTS LineString to convert.
     *
     * @return The new SDO LineString.
     *
     * @throws InvalidGeometryException If the LineString is invalid.
     */
    private oracle.sdoapi.geom.LineString lineStringJtsToSdo(
        com.vividsolutions.jts.geom.LineString line) throws InvalidGeometryException {
        Coordinate[] coordinates = line.getCoordinates();

        CoordPoint[] coordPoints = new CoordPoint[coordinates.length];

        for (int i = 0; i < coordinates.length; i++) {
            coordPoints[i] = coordinatesToCoordPoint(coordinates[i], line.getDimension());
        }

        return sdoFactory.createLineString(coordPoints);
    }
}
