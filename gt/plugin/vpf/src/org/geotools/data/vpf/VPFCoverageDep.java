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
import java.util.*;

import org.geotools.data.DataSourceException;
import org.geotools.data.vpf.ifc.*;
import org.geotools.data.vpf.io.*;
import org.geotools.feature.FeatureType;
import org.geotools.feature.SchemaException;

/*
 * VPFCoverage.java
 *
 * Created on 19. april 2004, 15:06
 *
 * @author  <a href="mailto:knuterik@onemap.org">Knut-Erik Johnsen</a>, Project OneMap
 */
public class VPFCoverageDep implements VPFCoverageIfc, VPFLibraryIfc, FileConstants, FeatureClassTypes, DataTypesDefinition  {
    private final Collection featureClasses;
    private final File directory;
    private final String name;
    private final String description;
    private final int topology;
    private final VPFDataBase base;

    /** Creates a new instance of VPFCoverage */
    public VPFCoverageDep(TableRow tr, File directory, VPFDataBase base)
                throws IOException {
        this.base = base;
        featureClasses = new Vector();
        name = tr.get(FIELD_COVERAGE_NAME).toString();
        description = tr.get(VPFCoverageIfc.FIELD_DESCRIPTION).toString();
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
//            VPFFile 
            List list = vpfTable.readAllRows();
            vpfTable.close();
//            Iterator iter = 
//            while();
//            TableRow[] featureclass_tmp = (TableRow[]) list.toArray(
//                                                    new TableRow[list.size()]);
//            classes = new VPFFeatureClass[featureclass_tmp.length];

//            for (int i = 0; i < featureclass_tmp.length; i++) {
//                String classname = featureclass_tmp[i].get(FIELD_CLASS).toString();
//
//                classes[i] = new VPFFeatureClass(featureclass_tmp[i], directory, 
//                        base);
//                classes[i] = new VPFFeatureClass(featureclass_tmp[i], directory, 
//                        base);
//            }
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
//        base.setTilingSchema(hm);
    }

    public VPFFeatureClassDep getFeatureClass(String typename) {
        VPFFeatureClassDep result = null;
        VPFFeatureClassDep temp;
        Iterator iter = featureClasses.iterator();
        while(iter.hasNext()){
            temp = (VPFFeatureClassDep)iter.next();
            if(temp.getName().equals(typename)){
                result = temp;
                break;
            }
        }

//        if (classes != null) {
//            for (int i = 0; i < classes.size(); i++) {
//                tmp = classes[i];
//
//                if (tmp != null) {
//                    if (tmp.getName().equals(typename)) {
//                        return tmp;
//                    }
//                }
//            }
//        }

        return null;
    }

    public Collection getFeatureClasses() {
        return featureClasses;
    }

    public String toString() {
        return "This is the Coverage: " + name + "\n" + 
               "It's description is : " + description + "\n" + 
               "Topology level is   : " + topology + "\n";
    }
    private final HashMap featureTypes = new HashMap();

    public FeatureType getSchema(String featuretype)
                                 throws DataSourceException {
        try {
            
            Object type = featureTypes.get(featuretype);

            if (type == null) {
                throw new SchemaException("Schema not found");
            }

            return (FeatureType) type;
        } catch (SchemaException e) {
            e.printStackTrace();
            throw new DataSourceException(featuretype + 
                                          " schema not available", e);
        }
    }

//    public static String[] getTypeNames() {
//        Vector v = new Vector(featureTypes.keySet());
//        String[] tmp = new String[v.size()];
//
//        for (int i = 0; i < tmp.length; i++) {
//            tmp[i] = (String) v.elementAt(i);
//        }
//
//        return tmp;
//    }
//
//    public static void addSchema(FeatureType type, String featurename) {
//        featureTypes.put(featurename, type);
//    }
}