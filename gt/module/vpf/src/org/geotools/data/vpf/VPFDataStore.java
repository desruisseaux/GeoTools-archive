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

import java.io.File;
import java.io.IOException;

import java.util.logging.Logger;

import org.geotools.data.AbstractDataStore;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;

import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;

import org.geotools.filter.Filter;


/**
 * Class <code>VPFDataSource</code> implements
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @author <a href="mailto:knuterik@onemap.org">Knut-Erik Johnsen</a>, Project OneMap
 * @version $Id$
 */
public class VPFDataStore extends AbstractDataStore implements DataStore {
    private VPFDataBase dataBase = null;

    public VPFDataStore(File file) throws IOException {
        dataBase = new VPFDataBase(file);        
    }

    public VPFFeatureReader getFeatureReader2(String typename)
                                       throws java.io.IOException {
        System.out.println("GETFEATUREREADER");

        return new VPFFeatureReader(typename, dataBase, 
                                    VPFSchemaCreator.getSchema(typename));
    }

    protected FeatureReader getFeatureReader(String typename)
                                      throws java.io.IOException {
        System.out.println("GETFEATUREREADER");

        return new VPFFeatureReader(typename, dataBase, 
                                    VPFSchemaCreator.getSchema(typename));
    }

    public FeatureType getSchema(String str) throws IOException {
        System.out.println("GETSCHEMA");

        return VPFSchemaCreator.getSchema(str);
    }

    public String[] getTypeNames() {
        System.out.println("GETTYPENAMES");

        return VPFSchemaCreator.getTypeNames();
    }
}