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

import com.vividsolutions.jts.geom.*;

import java.io.*;

import java.util.List;

import org.geotools.data.DataSourceException;
import org.geotools.data.vpf.ifc.*;
import org.geotools.data.vpf.io.*;
import org.geotools.data.vpf.readers.*;

import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;

/*
 * VPFFeatureClass.java
 *
 * Created on 21. april 2004, 15:33
 *
 * @author <a href="mailto:knuterik@onemap.org">Knut-Erik Johnsen</a>, Project OneMap
 */
public class VPFFeatureClass implements FeatureClassTypes, DataTypesDefinition {
    private AttributeType geometry = null;
    private FeatureType type = null;
    private VPFReader reader = null;
    private String classname = null;
    private String description = null;
    private String type_ext = null;

    /** Creates a new instance of VPFFeatureClass */
    public VPFFeatureClass(TableRow tr, File directory, VPFDataBase base) throws IOException {
        //this.base = base;
        //this.directory = directory;
        classname = tr.get(FIELD_CLASS).toString();

        String tmp = tr.get(FIELD_TYPE).toString();
        char featureType = tmp.charAt(0);
        description = tr.get(FIELD_DESCRIPTION).toString();

        // This really needs to be fixed!!!!! 
        if (featureType == FEATURE_POINT) {
            reader = new VPFPointFeatureReader(directory, classname,base.getTilingSchema());
            geometry = AttributeTypeFactory.newAttributeType("geometry", Point.class);
            type_ext = ".pft";
        } else if (featureType == FEATURE_LINE) {
            reader = new VPFLineFeatureReader(directory, classname, base.getTilingSchema());
            geometry = AttributeTypeFactory.newAttributeType("geometry", LineString.class);
            type_ext = ".lft";
        } else if (featureType == FEATURE_AREA) {
            reader = new VPFAreaFeatureReader(directory, classname, base.getTilingSchema());
            geometry = AttributeTypeFactory.newAttributeType("geometry", Polygon.class);
            type_ext = ".aft";
        } else if (featureType == FEATURE_TEXT) {
            reader = new VPFTextFeatureReader(directory, classname, base.getTilingSchema());
            geometry = AttributeTypeFactory.newAttributeType("geometry", Point.class);
            type_ext = ".tft";
        } else {
            throw new IOException("Unrecognised 'featuretype':"+featureType );
        }

        readHeader(directory);
        VPFSchemaCreator.addSchema(type, classname);
    }

    public void readHeader(File directory) {
        try {
            TableInputStream input = new TableInputStream(directory.getAbsolutePath()+File.separator + classname + type_ext);

            TableHeader head = (TableHeader) input.getHeader();

            List defs = head.getColumnDefs();
            TableColumnDef cdef = null;

            // Make room for the actual feature as well as the attributes
            AttributeType[] attributes = new AttributeType[defs.size() + 1];

            for (int i = 0; i < defs.size(); i++) {
                cdef = (TableColumnDef) defs.get(i);

                switch (cdef.getType()) {
                case DATA_TEXT:
                case DATA_LEVEL1_TEXT:
                case DATA_LEVEL2_TEXT:
                case DATA_LEVEL3_TEXT:
                    attributes[i] = AttributeTypeFactory.newAttributeType(cdef.getName(), String.class);
                    break;
                case DATA_SHORT_FLOAT:
                case DATA_LONG_FLOAT:
                    attributes[i] = AttributeTypeFactory.newAttributeType(cdef.getName(), Double.class);
                    break;
                case DATA_SHORT_INTEGER:
                case DATA_LONG_INTEGER:
                    attributes[i] = AttributeTypeFactory.newAttributeType(cdef.getName(), Integer.class);
                    break;
                default:
                    System.out.println("Unknown type: " + cdef.getType());
                    break;
                }
            }
            attributes[attributes.length - 1] = geometry;
            type = FeatureTypeFactory.newFeatureType(attributes, classname);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FeatureType getFeatureType() {
        return type;
    }

    public VPFReader getReader() throws DataSourceException {
        reader.initReader();
        return reader;
    }

    public String toString() {
        return "Classname: " + classname + " Description: " + description + 
               "Reader: " + reader.getClass().getName();
    }

    public String getName() {
        return classname;
    }
}