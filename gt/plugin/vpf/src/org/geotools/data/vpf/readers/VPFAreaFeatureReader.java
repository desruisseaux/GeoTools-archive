/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004, Geotools Project Managment Committee (PMC)
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

import com.vividsolutions.jts.geom.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.geotools.data.vpf.*;
import org.geotools.data.vpf.VPFDataBase;
import org.geotools.data.vpf.ifc.FileConstants;
import org.geotools.data.vpf.io.*;
import org.geotools.data.vpf.util.*;

import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;
/*
 * VPFAreaFeatureReader.java
 *
 * Created on 14. april 2004, 15:33
 *
 * @author  <a href="mailto:knuterik@onemap.org">Knut-Erik Johnsen</a>, Project OneMap
 */
public class VPFAreaFeatureReader extends VPFReader implements FileConstants {
    private int calls = 0;
    private long features = 0;
    private File table = null;
    private int currentTile = 0;
    private TableInputStream edgeInput = null;
    private TableInputStream faceInput = null;
    private TableInputStream ringInput = null;
    private TableRow edgeRow = null;
    private TableRow faceRow = null;
    private TableRow ringRow = null;
    private String tmp = null;

    //private LineFeature currentLineData = null;
    private HashMap currentFeature = null;
    private HashMap currentFace = null;
    private HashMap currentRing = null;
    private EdgeData currentEdge = null;
    private Polygon current_poly = null;

    public VPFAreaFeatureReader(File directory, String typename, HashMap tiles) {
        super(directory, typename, tiles);

        try {
            this.table = new File(directory, typename + AREA_FEATURE_TABLE);

            this.tableInput = new TableInputStream(table.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getFeatureID() {
        return "VMAP-" + typename + ":" + features;
    }

    public void close() throws IOException {
        super.close();

        if (edgeInput != null) {
            edgeInput.close();
            edgeInput = null;
        }

        if (faceInput != null) {
            faceInput.close();
            faceInput = null;
        }

        if (ringInput != null) {
            ringInput.close();
            ringInput = null;
        }

        calls = 0;
        currentTile = 0;
        features = 0;

        edgeRow = null;
        faceRow = null;
        ringRow = null;

        currentFeature = null;
        currentFace = null;
        currentRing = null;
        currentEdge = null;
    }

    public Object read(int i) throws IOException, ArrayIndexOutOfBoundsException {
        if (currentData == null) {
            throw new IOException(
                    "No content available - did you remeber to call next?");
        }

        return currentData[i];
    }

    public void next() throws IOException {
        calls++;

        //if ( calls != 2846 ) { // && calls != 4173 && calls != 7405 && calls != 7406 && calls != 11768 && calls != 17406 && calls != 17408) { 
        if (tableRow != null) {
            currentFeature = readFeature(tableRow, type);

            int tileid = ((RowField) currentFeature.get("tile_id")).getAsInt();

            if (tileid != currentTile) {
                if (edgeInput != null) {
                    edgeInput.close();
                    edgeInput = null;
                }

                if (faceInput != null) {
                    faceInput.close();
                    faceInput = null;
                }

                if (ringInput != null) {
                    ringInput.close();
                    ringInput = null;
                }


                //System.out.println( "Vi bytter tile til id: " + tileid + " : " + (String) tiles.get( "" + tileid ) );
                faceInput = new TableInputStream( new File(new File(directory, (String) tiles.get("" + tileid)), "fac").getAbsolutePath());
                edgeInput = new TableInputStream(new File(new File(directory, (String) tiles.get("" + tileid)), "edg").getAbsolutePath());
                ringInput = new TableInputStream(new File(new File(directory, (String) tiles.get("" + tileid)), "rng").getAbsolutePath());
                currentTile = tileid;
            }

            int fac_id = ((RowField) currentFeature.get("fac_id")).getAsInt();
            faceRow = (TableRow) faceInput.readRow(fac_id);

            currentFace = readFace(faceRow);

            int ring_id = ((Integer) currentFace.get("ring_ptr")).intValue();

            ringRow = (TableRow) ringInput.readRow(ring_id);
            currentRing = readRing(ringRow);

            ArrayList coords_in_ring = new ArrayList();
            int rings = 0;

            ArrayList rings_in_poly = new ArrayList();

            while (((Integer) currentRing.get("face_id")).intValue() == fac_id) {
                rings++;
                coords_in_ring.clear();

                EdgeData tmpEdge = null;
                TripletData tmpTriplet = null;
                int start_edge = ((Integer) currentRing.get("start_edge")).intValue();

                int cur_tile = currentTile;
                int next_row = start_edge;
                int next_tile = -1;

                int cur_face = fac_id;

                /*
                int last_face = -1;
                int last_last_tile = -1;
                int last_tile = currentTile;
                Point last_end = null;
                 */
                boolean change = false;
                boolean first = true;

                //boolean left = false;
                //boolean right = false;
                int lines = 0;
                int prev_node = -1;
                boolean go_on = true;

                do {
                    lines++;


                    /*
                    //int tmp_face = -1;
                    if ( next_tile != -1 ) {
                        System.out.println( "Bytter tile: " + calls );
                        cur_tile = next_tile;
                        change = true;
                                            
                        edgeRow = (TableRow) edgeInput.readRow( tmpTriplet.getCurrent_row_id() );
                        tmpEdge = readEdge( edgeRow );
                                            
                        TripletData right = (TripletData) tmpEdge.get( "right_face");
                        TripletData left = (TripletData) tmpEdge.get( "right_face");
                                            
                        if ( right.getNext_tile_id() != -1 ) {
                            if ( right.getNext_row_id() != 1 ) {
                                cur_face = right.getNext_row_id();
                            } else {
                                throw new IOException( "Only boundry too universe: right" );
                            }
                        } else if ( left.getNext_tile_id() != -1 ) {
                            if ( left.getNext_row_id() != 1 ) {
                                cur_face = left.getNext_row_id();
                            } else {
                                throw new IOException( "Only boundry too universe: left" );
                            }
                        } //else {
                           // throw new IOException( "No external face found" );
                        //}
                                            
                        edgeInput.close();
                        edgeInput = null;
                        edgeInput = new TableInputStream( new File( new File( directory, (String) tiles.get( "" + next_tile )), "edg" ).getAbsolutePath() );
                    }
                    */
                    edgeRow = (TableRow) edgeInput.readRow(next_row);
                    tmpEdge = readEdge(edgeRow);

                    int right_face = ((TripletData) tmpEdge.get("right_face")).getNext_row_id();
                    int left_face = ((TripletData) tmpEdge.get("left_face")).getNext_row_id();

                    if (right_face == cur_face) {
                        tmpTriplet = (TripletData) tmpEdge.get("right_edge");
                        LineString ls = (LineString) tmpEdge.get("coordinates");

                        for (int i = 0; i < ls.getNumPoints(); i++) {
                            coords_in_ring.add(ls.getCoordinateN(i));
                        }
                    } else if (left_face == cur_face) {
                        tmpTriplet = (TripletData) tmpEdge.get("left_edge");

                        LineString ls = (LineString) tmpEdge.get("coordinates");

                        for (int i = ls.getNumPoints() - 1; i >= 0; i--) {
                            coords_in_ring.add(ls.getCoordinateN(i));
                        }
                    } else {
                        throw new IOException("The face isn't on any side!!" + 
                                              calls);

                        //System.out.println( "The face isn't on any side!! " + calls );
                    }

                    //next_tile = tmpTriplet.getNext_tile_id();
                    if (tmpTriplet.getCurrent_row_id() != -1) {
                        next_row = tmpTriplet.getCurrent_row_id();
                    } else {
                        next_row = tmpTriplet.getNext_row_id();
                    }

                    if (next_row == start_edge) {
                        if ((cur_tile == currentTile) || 
                                (next_tile == currentTile)) {
                            go_on = false;
                        }
                    }

                    //if ( lines % 100 == 0 ) System.out.println( "Linjer: " + lines );
                } while (go_on); //next_row != start_edge );// || ( currentTile != cur_tile && next_row != start_edge ) );

                if (change) {
                    System.out.println("Bytter tilbake");
                    edgeInput.close();
                    edgeInput = null;
                    File tmp = new File(directory, (String) tiles.get("" + currentTile));
                    edgeInput = new TableInputStream( new File(tmp, "edg").getAbsolutePath());
                }

                rings_in_poly.add(coords_in_ring);

                ringRow = (TableRow) ringInput.readRow(++ring_id);

                if (ringRow == null) {
                    break;
                }

                currentRing = readRing(ringRow);
            }

            //System.out.println( "Antall ringer: " + rings_in_poly.size() );
            GeometryFactory gf = new GeometryFactory();
            Coordinate[] outer_ring = new Coordinate[0];
            ArrayList shortlist = (ArrayList) rings_in_poly.remove(0);

            //shortlist.add( shortlist.get(0));
            try {
                outer_ring = (Coordinate[]) shortlist.toArray(outer_ring);

                LinearRing[] inner = null;

                if (rings_in_poly.size() > 0) {
                    //System.out.println( "Lagt til inner ring" );
                    inner = new LinearRing[rings_in_poly.size()];

                    Coordinate[] inner_ring = new Coordinate[0];

                    for (int i = 0; i < inner.length; i++) {
                        shortlist = (ArrayList) rings_in_poly.get(i);
                        inner_ring = (Coordinate[]) shortlist.toArray(inner_ring);
                        inner[i] = gf.createLinearRing(inner_ring);
                    }
                }

                current_poly = gf.createPolygon(gf.createLinearRing(outer_ring), inner);
            } catch (IllegalArgumentException e) {
                System.out.println("Error in polygon creation " + calls);
                e.printStackTrace();
            }

            makeCurrent();

            //}
        }

        tableRow = (TableRow) tableInput.readRow();
    }

    private void makeCurrent() {
        currentData = new Object[getAttributeCount()];

        int nr = currentData.length - 1;

        for (int i = 0; i < nr; i++) {
            currentData[i] = type.getAttributeType(i).parse(currentFeature.get(type.getAttributeType(i).getName()));
        }

        currentData[nr] = type.getAttributeType(nr).parse(current_poly);
    }

    public static void main(String[] args) {
        int features = 0;

        try {
            VPFDataStore data = new VPFDataStore( new File("C:\\data\\v0eur\\vmaplv0"));
            VPFFeatureReader reader = data.getFeatureReader2("builtupa");
            System.out.println("Leseren er : " + reader);

            while (reader.hasNext()) {
                features++;

                if (features != 2846) {
                    reader.next();

                    //System.out.println( "Antall features: " + features );
                } else {
                    reader.next();
                }
            }

            System.out.println("Antall features: " + features);
            reader.close();
        } catch (Exception e) {
            System.out.println("Antall features: " + features);
            e.printStackTrace();
        }
    }
}