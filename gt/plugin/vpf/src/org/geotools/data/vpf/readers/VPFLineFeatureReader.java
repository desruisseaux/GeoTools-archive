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

import java.io.File;
import java.io.IOException;

import java.util.HashMap;

import org.geotools.data.vpf.*;
import org.geotools.data.vpf.VPFDataBase;
import org.geotools.data.vpf.ifc.*;
import org.geotools.data.vpf.io.*;
import org.geotools.data.vpf.util.*;

import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;

/**
 * VPFLineFeatureReader.java
 *
 * Created on 13. april 2004, 15:02
 *
 * @author <a href="mailto:knuterik@onemap.org">Knut-Erik Johnsen</a>, Project OneMap
 * @deprecated
 */
public class VPFLineFeatureReader extends VPFReader implements FileConstants {
    private File coverage = null;
    private File table = null;
    private int currentTile = 0;
    private int currentEdge = 0;
    private TableInputStream edgeInput = null;
    private TableRow edgeRow = null;
    private String tmp = null;
    private long features = 0;
    private EdgeData currentEdgeData = null;
    private HashMap currentFeature = null;

    public VPFLineFeatureReader(File directory, String typename, HashMap tiles) {
        super(directory, typename, tiles);

        try {
            this.table = new File(directory, typename + LINE_FEATURE_TABLE);

            this.tableInput = new TableInputStream(table.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() throws IOException {
        super.close();

        if (edgeInput != null) {
            edgeInput.close();
            edgeInput = null;
        }

        currentTile = 0;
        currentEdge = 0;
        edgeInput = null;
        edgeRow = null;
        tmp = null;
        features = 0;
        currentEdgeData = null;
        currentFeature = null;
    }

    public String getFeatureID() {
        return "VMAP-" + typename + ":" + features;
    }

    public void next() throws IOException {
        if (tableRow != null) {
            currentFeature = readFeature(tableRow, type);

            int tileid = ((RowField) currentFeature.get("tile_id")).intValue();

            if (tileid != currentTile) {
                if (edgeInput != null) {
                    edgeInput.close();
                }

                edgeInput = new TableInputStream(
                                    new File(new File(directory, 
                                                      (String) tiles.get("" + tileid)), 
                                             TABLE_EDG).getAbsolutePath());

                edgeRow = (TableRow) edgeInput.readRow();
                currentTile = tileid;
                currentEdge = 1;
            }

            int edgeid = ((RowField) currentFeature.get("edg_id")).intValue();
            edgeRow = (TableRow) edgeInput.readRow(edgeid);
            currentEdgeData = readEdge(edgeRow);

            features++;


            /*
            if ( (features % 10000) == 0 ) {
                System.out.println( "Antall features: " + features );
            }
             */
            makeCurrent();
        }

        tableRow = (TableRow) tableInput.readRow();
    }

    private void makeCurrent() {
        currentData = new Object[getAttributeCount()];

        for (int i = 0; i < (currentData.length - 1); i++) {
            currentData[i] = type.getAttributeType(i)
                                 .parse(currentFeature.get(
                                                type.getAttributeType(i)
                                                    .getName()));
        }

        currentData[currentData.length - 1] = type.getAttributeType(currentData.length - 1)
                                                  .parse(currentEdgeData.get(
                                                                 "coordinates"));
    }

    public Object read(int param) throws IOException, 
                                         ArrayIndexOutOfBoundsException {
        if ((currentEdgeData == null) || (currentFeature == null)) {
            throw new IOException(
                    "No content available - did you remeber to call next?");
        }

        return currentData[param];
    }

    public static void main(String[] args) {
        int features = 0;

        try {
            long start = System.currentTimeMillis();
            VPFDataStore data = new VPFDataStore(
                                        new File("C:\\data\\v0eur\\vmaplv0"));
            VPFFeatureReader reader = null;
//            VPFFeatureReader reader = data.getFeatureReader2("roadl");

            //System.out.println( "Leseren er : " + reader );
            long after = System.currentTimeMillis();

            while (reader.hasNext()) {
                features++;
                reader.next();
            }

            reader.close();

            long end = System.currentTimeMillis();
            System.out.println("Tid brukt: " + (end - start));
            System.out.println("Uten init: " + (end - after));
            System.out.println("Init tid : " + (after - start));
            System.out.println("Antall features: " + features);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}