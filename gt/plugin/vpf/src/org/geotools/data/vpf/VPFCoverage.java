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

package org.geotools.data.vpf;

import java.io.*;

import java.util.HashMap;
import java.util.List;

import org.geotools.data.vpf.ifc.*;
import org.geotools.data.vpf.io.*;

/*
 * VPFCoverage.java
 *
 * Created on 19. april 2004, 15:06
 *
 * @author  <a href="mailto:knuterik@onemap.org">Knut-Erik Johnsen</a>, Project OneMap
 */
public class VPFCoverage implements VPFCoverageIfc, FileConstants {
    private VPFFeatureClass[] classes = null;
    private File directory = null;
    private String name = null;
    private String description = null;
    private int topology = 0;
    private VPFDataBase base = null;

    /** Creates a new instance of VPFCoverage */
    public VPFCoverage(TableRow tr, File directory, VPFDataBase base)
                throws IOException {
        this.base = base;
        name = tr.get(FIELD_COVERAGE_NAME).getAsString().trim();
        description = tr.get(FIELD_DESCRIPTION).getAsString().trim();
        topology = tr.get(FIELD_LEVEL).shortValue();
        this.directory = new File(directory, name);
        setFeatureClasses();
    }

    private void setFeatureClasses() throws IOException {
        if (!name.equals("rference") && !name.equals("libref") && 
                !name.equals("tileref") && !name.equals("dq")) {
            //System.out.println( "Navnet er: " + name);
            String vpfTableName = new File(directory, 
                                           FEATURE_CLASS_ATTRIBUTE_TABLE).toString();
            TableInputStream vpfTable = new TableInputStream(vpfTableName);
            List list = vpfTable.readAllRows();
            vpfTable.close();

            TableRow[] featureclass_tmp = (TableRow[]) list.toArray(
                                                    new TableRow[list.size()]);
            classes = new VPFFeatureClass[featureclass_tmp.length];

            for (int i = 0; i < featureclass_tmp.length; i++) {
                classes[i] = new VPFFeatureClass(featureclass_tmp[i], directory, 
                                                 base);
            }
        } else if (name.equals("tileref")) {
            createTilingSchema();
        }
    }

    private void createTilingSchema() throws IOException {
        File tilefile = new File(directory, "tilereft.tft");

        HashMap hm = new HashMap();

        TableInputStream testInput = new TableInputStream(
                                             tilefile.getAbsolutePath());
        TableRow row = (TableRow) testInput.readRow();
        String tmp = null;
        StringBuffer buff = null;

        while (row != null) {
            tmp = row.get(FIELD_TILE_NAME).toString().trim();

            if ((tmp != null) && (tmp.length() > 0)) {
                tmp = tmp.toLowerCase();
                buff = new StringBuffer();
                buff.append(tmp.charAt(0));

                for (int i = 1; i < tmp.length(); i++) {
                    buff.append(File.separator);
                    buff.append(tmp.charAt(i));
                }

                hm.put(row.get(FIELD_TILE_ID).toString().trim(), 
                       buff.toString());


                //System.out.println( new File( coverage, tmp.charAt(0) + File.separator + tmp.charAt(1) ).getAbsolutePath() );
                row = (TableRow) testInput.readRow();
            }
        }

        testInput.close();
        base.setTilingSchema(hm);
    }

    public VPFFeatureClass getFeatureClass(String typename) {
        VPFFeatureClass tmp = null;

        if (classes != null) {
            for (int i = 0; i < classes.length; i++) {
                tmp = classes[i];

                if (tmp != null) {
                    if (tmp.getName().equals(typename)) {
                        return tmp;
                    }
                }
            }
        }

        return null;
    }

    public VPFFeatureClass[] getFeatureClasses() {
        return classes;
    }

    public String toString() {
        return "This is the Coverage: " + name + "\n" + 
               "It's description is : " + description + "\n" + 
               "Topology level is   : " + topology + "\n";
    }
}