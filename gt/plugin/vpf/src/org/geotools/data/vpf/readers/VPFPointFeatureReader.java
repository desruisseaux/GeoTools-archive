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
import org.geotools.data.vpf.ifc.FileConstants;
import org.geotools.data.vpf.io.RowField;
import org.geotools.data.vpf.io.TableInputStream;
import org.geotools.data.vpf.io.TableRow;
import org.geotools.data.vpf.util.PointData;
import org.geotools.data.vpf.util.PrimitiveDataFactory;

import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;

/**
 * VPFPointFeatureReader.java
 *
 * Created on 22. april 2004, 14:49
 *
 * @author  <a href="mailto:knuterik@onemap.org">Knut-Erik Johnsen</a>, Project OneMap
 * @deprecated
 */
public class VPFPointFeatureReader extends VPFReader implements FileConstants {
    private File table = null;
    private int currentTile = 0;
    private TableInputStream pointInput = null;
    private TableRow pointRow = null;
    private HashMap currentFeature = null;
    private PointData currentPointData = null;

    /** Creates a new instance of VPFPointFeatureReader */
    public VPFPointFeatureReader(File directory, String typename, 
                                 java.util.HashMap tiles) {
        super(directory, typename, tiles);

        try {
            this.table = new File(directory, typename + POINT_FEATURE_TABLE);

            this.tableInput = new TableInputStream(table.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() throws IOException {
        super.close();

        if (pointInput != null) {
            pointInput.close();
            pointInput = null;
        }

        pointRow = null;
        currentFeature = null;
        currentPointData = null;
        currentTile = 0;
    }

    public String getFeatureID() {
        return "VMAP-" + typename + ":" + currentPointData.get("id");
    }

    public void next() throws IOException {
        if (tableRow != null) {
            currentFeature = readFeature(tableRow, type);

            int tileid = ((RowField) currentFeature.get("tile_id")).intValue();

            if (tileid != currentTile) {
                if (pointInput != null) {
                    pointInput.close();
                }

                pointInput = new TableInputStream(
                                     new File(new File(directory, 
                                                       (String) tiles.get("" + tileid)), 
                                              TABLE_END).getAbsolutePath());

                pointRow = (TableRow) pointInput.readRow();
                currentTile = tileid;
            }

            int edgeid = ((RowField) currentFeature.get("end_id")).intValue();
            pointRow = (TableRow) pointInput.readRow(edgeid);
            currentPointData = readPoint(pointRow);

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
                                                  .parse(currentPointData.get(
                                                                 "coordinate"));
    }

    public Object read(int param) throws IOException, 
                                         ArrayIndexOutOfBoundsException {
        if ((currentPointData == null) || (currentFeature == null)) {
            throw new IOException(
                    "No content available - did you remeber to call next?");
        }

        return currentData[param];
    }

    public static void main(String[] args) {
        int features = 0;

        try {
            VPFDataStore data = new VPFDataStore(
                                        new File("C:\\data\\v0eur\\vmaplv0"));
            VPFFeatureReader reader = null;
//            VPFFeatureReader reader = data.getFeatureReader2("dangerp");

            System.out.println("Leseren er : " + reader);

            while (reader.hasNext()) {
                features++;
                reader.next();
            }

            System.out.println("Antall features: " + features);
            reader.close();
        } catch (Exception e) {
            System.out.println("Antall features: " + features);
            e.printStackTrace();
        }
    }
}