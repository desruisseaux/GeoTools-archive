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
package org.geotools.data.vpf.readers;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import org.geotools.data.vpf.VPFFeatureType;
import org.geotools.data.vpf.file.VPFFile;
import org.geotools.data.vpf.file.VPFFileFactory;
import org.geotools.data.vpf.ifc.FileConstants;
import org.geotools.data.vpf.io.TripletId;
import org.geotools.feature.Feature;
import org.geotools.feature.IllegalAttributeException;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;


/**
 * Creates Geometries for area objects
 *
 * @author <a href="mailto:jeff@ionicenterprise.com">Jeff Yutzler</a>
 */
public class AreaGeometryFactory extends VPFGeometryFactory
    implements FileConstants {
    /* (non-Javadoc)
     * @see com.ionicsoft.wfs.jdbc.geojdbc.module.vpf.VPFGeometryFactory#createGeometry(java.lang.String, int, int)
     */
    public void createGeometry(VPFFeatureType featureType, Feature values)
        throws SQLException, IOException, IllegalAttributeException {
        int tempEdgeId;
        boolean isLeft = false;
        Coordinate previousCoordinate = null;
        Coordinate coordinate = null;
        List coordinates = null;
        Polygon result = null;
        GeometryFactory geometryFactory = new GeometryFactory();
        LinearRing outerRing = null;
        List innerRings = new Vector();

        // Get face information
        //TODO: turn these column names into constants
        int faceId = Integer.parseInt(values.getAttribute("fac_id").toString());

        // Retrieve the tile directory
        String baseDirectory = featureType.getFeatureClass().getDirectoryName();
        String tileDirectory = baseDirectory;

        // If the primitive table is there, this coverage is not tiled
        if (!new File(tileDirectory.concat(File.separator).concat(FACE_PRIMITIVE))
                .exists()) {
            Short tileId = new Short(Short.parseShort(
                        values.getAttribute("tile_id").toString()));
            tileDirectory = tileDirectory.concat(File.separator)
                                         .concat(featureType.getFeatureClass()
                                                            .getCoverage()
                                                            .getLibrary()
                                                            .getTileMap()
                                                            .get(tileId)
                                                            .toString()).trim();
        }

        // all edges from this tile that use the face
        String edgeTableName = tileDirectory.concat(File.separator).concat(EDGE_PRIMITIVE);
        VPFFile edgeFile = VPFFileFactory.getInstance().getFile(edgeTableName);

        // Get the rings
        String faceTableName = tileDirectory.concat(File.separator).concat(FACE_PRIMITIVE);
        VPFFile faceFile = VPFFileFactory.getInstance().getFile(faceTableName);
        faceFile.reset();

        String ringTableName = tileDirectory.concat(File.separator).concat(RING_TABLE);
        VPFFile ringFile = VPFFileFactory.getInstance().getFile(ringTableName);
        ringFile.reset();

        Feature faceFeature = faceFile.readFeature();

        while (faceFeature != null) {
            if (faceFeature.getAttribute("id").equals(new Integer(faceId))) {
                coordinates = new LinkedList();

                int ringId = Integer.parseInt(faceFeature.getAttribute(
                            "ring_ptr").toString());

                // Get the starting edge
                int startEdgeId = ((Number) ringFile.getRowFromId("id", ringId)
                                                    .getAttribute("start_edge"))
                    .intValue();
                int nextEdgeId = startEdgeId;

                while (nextEdgeId > 0) {
                    Feature edgeRow = edgeFile.getRowFromId("id", nextEdgeId);

                    if (faceId == ((TripletId) edgeRow.getAttribute("left_face"))
                            .getId()) {
                        isLeft = true;
                    } else if (faceId == ((TripletId) edgeRow.getAttribute(
                                "right_face")).getId()) {
                        isLeft = false;
                    } else {
                        throw new SQLException(
                            "This edge is not part of this face.");
                    }

                    // Get the geometry of the edge and add it to our line geometry
                    LineString edgeGeometry = (LineString) edgeRow.getAttribute(
                            "coordinates");

                    if (isLeft) {
                        // We must take the coordinate values backwards
                        for (int inx = edgeGeometry.getNumPoints() - 1;
                                inx >= 0; inx--) {
                            coordinate = edgeGeometry.getCoordinateSequence()
                                                     .getCoordinate(inx);

                            if ((previousCoordinate == null)
                                    || (!coordinate.equals3D(previousCoordinate))) {
                                coordinates.add(coordinate);
                                previousCoordinate = coordinate;
                            }
                        }
                    } else {
                        for (int inx = 0; inx < edgeGeometry.getNumPoints();
                                inx++) {
                            coordinate = edgeGeometry.getCoordinateSequence()
                                                     .getCoordinate(inx);

                            if ((previousCoordinate == null)
                                    || (!coordinate.equals3D(previousCoordinate))) {
                                coordinates.add(coordinate);
                                previousCoordinate = coordinate;
                            }
                        }
                    }

                    TripletId triplet = (TripletId) edgeRow.getAttribute(isLeft
                            ? "left_edge" : "right_edge");
                    tempEdgeId = triplet.getId();

                    if ((tempEdgeId == startEdgeId)
                            || (tempEdgeId == nextEdgeId)) {
                        nextEdgeId = 0;
                    } else {
                        // Here is where we need to consider crossing tiles
                        //                        if(triplet.getTileId() == 0){
                        nextEdgeId = tempEdgeId;

                        //                        }else {
                        //                            nextEdgeId = triplet.getNextId();
                        //                          Integer tileId = new Integer(triplet.getTileId());
                        //                            tileDirectory = tileDirectory.concat(File.separator).concat(featureType.getFeatureClass().getCoverage().getModule().getTileMap().get(tileId).toString()).trim();
                        //                        }
                    }
                }

                // The dorks at JTS insist that you explicitly close your rings. Ugh.
                if (!coordinate.equals(coordinates.get(0))) {
                    coordinates.add(coordinates.get(0));
                }

                Coordinate[] coordinateArray = new Coordinate[coordinates.size()];

                for (int cnx = 0; cnx < coordinates.size(); cnx++) {
                    coordinateArray[cnx] = (Coordinate) coordinates.get(cnx);
                }

                LinearRing ring = null;

                ring = geometryFactory.createLinearRing(coordinateArray);

                if (outerRing == null) {
                    outerRing = ring;
                } else {
                    // I haven't found any data to test this yet. 
                    // If you do and it works, remove this comment.
                    innerRings.add(ring);
                }
            }

            if (faceFile.hasNext()) {
                faceFeature = faceFile.readFeature();
            } else {
                faceFeature = null;
            }
        }

        if (innerRings.isEmpty()) {
            result = geometryFactory.createPolygon(outerRing, null);
        } else {
            LinearRing[] ringArray = new LinearRing[innerRings.size()];

            for (int cnx = 0; cnx < innerRings.size(); cnx++) {
                ringArray[cnx] = (LinearRing) innerRings.get(cnx);
            }

            result = geometryFactory.createPolygon(outerRing, ringArray);
        }

        values.setDefaultGeometry(result);
    }
}
