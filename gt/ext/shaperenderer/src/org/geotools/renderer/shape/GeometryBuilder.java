/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.renderer.shape;

import java.nio.ByteBuffer;

import org.opengis.referencing.operation.MathTransform2D;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Parses a shapefile record and creates a geometry that is in the desired coordinateReference
 * 
 * @author jeichar
 * @since 2.1.x
 */
public class GeometryBuilder {
    /** Represents a Null shape (id = 0). */
    public static final int NULL = 0;
    /** Represents a Point shape (id = 1). */
    public static final int POINT = 1;
    /** Represents a PointZ shape (id = 11). */
    public static final int POINTZ = 11;
    /** Represents a PointM shape (id = 21). */
    public static final int POINTM = 21;
    /** Represents an Arc shape (id = 3). */
    public static final int ARC = 3;
    /** Represents an ArcZ shape (id = 13). */
    public static final int ARCZ = 13;
    /** Represents an ArcM shape (id = 23). */
    public static final int ARCM = 23;
    /** Represents a Polygon shape (id = 5). */
    public static final int POLYGON = 5;
    /** Represents a PolygonZ shape (id = 15). */
    public static final int POLYGONZ = 15;
    /** Represents a PolygonM shape (id = 25). */
    public static final int POLYGONM = 25;
    /** Represents a MultiPoint shape (id = 8). */
    public static final int MULTIPOINT = 8;
    /** Represents a MultiPointZ shape (id = 18). */
    public static final int MULTIPOINTZ = 18;
    /** Represents a MultiPointZ shape (id = 28). */
    public static final int MULTIPOINTM = 28;

    /** Represents an Undefined shape (id = -1). */
    public static final int UNDEFINED = -1;
    double[][] coords;

    // if needed in future otherwise all references to a z are commented out.
    // double[][] z;

    int numParts;

    private Envelope geomBBox;

    /**
     * creates a geometry from the buffer or skips the geometry if it does not intersect with the
     * geometry
     * 
     * @param buffer
     * @param type
     * @return
     */
    public Geometry readMultiLine( ByteBuffer buffer, int type, Envelope bbox, MathTransform2D mt ) {
        if (type == NULL) {
            return null;
        }
        int dimensions = (type == ARCZ) ? 3 : 2;
        // read bounding box
        double minx = buffer.getDouble();
        double miny = buffer.getDouble();
        double maxx = buffer.getDouble();
        double maxy = buffer.getDouble();
        geomBBox = new Envelope(minx, maxx, miny, maxy);

        if (!bbox.intersects(geomBBox)) {
            skipMultiLineGeom(buffer, dimensions);
            return null;
        }

        numParts = buffer.getInt();
        int numPoints = buffer.getInt(); // total number of points

        int[] partOffsets = new int[numParts];

        // points = new Coordinate[numPoints];
        for( int i = 0; i < numParts; i++ ) {
            partOffsets[i] = buffer.getInt();
        }
        // read the first two coordinates and start building the coordinates
        coords = new double[numParts][];

        // if needed in future otherwise all references to a z are commented out.
        // if( dimensions==3 )
        // z=new double[numParts][];

        int finish, start = 0;
        int length = 0;
        // boolean clonePoint = false;
        for( int part = 0; part < numParts; part++ ) {
            start = partOffsets[part];

            if (part == (numParts - 1)) {
                finish = numPoints;
            } else {
                finish = partOffsets[part + 1];
            }

            length = finish - start;
            // if (length == 1) {
            // length = 2;
            // clonePoint = true;
            // } else {
            // clonePoint = false;
            // }
            coords[part] = new double[length * 2];
            for( int i = 0; i < length; i++ ) {
                coords[part][i] = buffer.getDouble();
                coords[part][i + 1] = buffer.getDouble();
            }
            try {
                mt.transform(coords[part], 0, coords[part], 0, coords[part].length);
            } catch (Exception e) {
                ShapeRenderer.LOGGER.severe("could not transform coordinates"
                        + e.getLocalizedMessage());
            }
            // if(clonePoint) {
            // builder.setOrdinate(builder.getOrdinate(0, 0), 0, 1);
            // builder.setOrdinate(builder.getOrdinate(1, 0), 1, 1);
            // }

        }

        // if we have another coordinate, read and add to the coordinate
        // sequences
        if (dimensions == 3) {
            // z min, max
            buffer.position(buffer.position() + 2 * 8 + 8 * numPoints);
            // for (int part = 0; part < numParts; part++) {
            // start = partOffsets[part];
            //
            // if (part == (numParts - 1)) {
            // finish = numPoints;
            // } else {
            // finish = partOffsets[part + 1];
            // }
            //
            // length = finish - start;
            // // if (length == 1) {
            // // length = 2;
            // // clonePoint = true;
            // // } else {
            // // clonePoint = false;
            // // }
            //
            // for (int i = 0; i < length; i++) {
            // builder.setOrdinate(lines[part], buffer.getDouble(), 2, i);
            // }
            //
            // }
        }
        return new Geometry(type, coords, geomBBox);
    }

    private void skipMultiLineGeom( ByteBuffer buffer, int dimensions ) {
        numParts = buffer.getInt();
        int numPoints = buffer.getInt(); // total number of points

        // skip partOffsets
        buffer.position(buffer.position() + numParts * 4);

        // skip x y points;
        buffer.position(buffer.position() + numPoints * 4);

        // if we have another coordinate, read and add to the coordinate
        // sequences
        if (dimensions == 3) {
            // skip z min, max and z points
            buffer.position(buffer.position() + 2 * 8 + 8 * numPoints);
        }
    }

    public Geometry readMultiPoint( ByteBuffer buffer, int type, Envelope bbox, MathTransform2D mt ) {
        if (type == NULL) {
            return null;
        }

        int dimensions = (type == MULTIPOINTZ) ? 3 : 2;

        // read bounding box
        double minx = buffer.getDouble();
        double miny = buffer.getDouble();
        double maxx = buffer.getDouble();
        double maxy = buffer.getDouble();
        geomBBox = new Envelope(minx, maxx, miny, maxy);

        if (!bbox.intersects(geomBBox)) {
            skipMultiPointGeom(buffer, dimensions);
            return null;
        }

        int numpoints = buffer.getInt();
        coords = new double[numpoints][];
        for( int t = 0; t < numpoints; t++ ) {
            coords[t] = new double[]{buffer.getDouble(), buffer.getDouble()};
            try {
                mt.transform(coords[t], 0, coords[t], 0, coords[t].length);
            } catch (Exception e) {
                ShapeRenderer.LOGGER
                        .severe("could not transform coordinates" + e.getLocalizedMessage());
            }
        }
        if (type == MULTIPOINTZ) {
            buffer.position(buffer.position() + 2 * 8 + numpoints * 8);
        }

        return new Geometry(type, coords, geomBBox);
    }

    private void skipMultiPointGeom( ByteBuffer buffer, int dimensions ) {

        int numpoints = buffer.getInt();
        // skip x y
        buffer.position(buffer.position() + numpoints * 16);
        // skip z
        if (dimensions == 3)
            buffer.position(buffer.position() + numpoints * 8);
    }

    public Geometry readPoint( ByteBuffer buffer, int type, Envelope bbox, MathTransform2D mt ) {
        if (type == NULL) {
            return null;
        }

        coords = new double[1][];
        coords[0] = new double[]{buffer.getDouble(),buffer.getDouble()};
        double z = Double.NaN;
        geomBBox = new Envelope(coords[0][0], coords[0][0], coords[0][1], coords[0][1]);
        try {
            mt.transform(coords[0], 0, coords[0], 0, coords[0].length);
        } catch (Exception e) {
            ShapeRenderer.LOGGER
                    .severe("could not transform coordinates" + e.getLocalizedMessage());
        }

        if (type == POINTM) {
            buffer.getDouble();
        }

        if (type == POINTZ) {
            buffer.getDouble();
        }

        if( !bbox.intersects(geomBBox) )
            return null;
        
        return new Geometry(type, coords, geomBBox);
    }

}
