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

import java.net.URI;
import java.net.URISyntaxException;

import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;


/**
 * Class VPFDataSourceFactory.java is responsible for
 * 
 * <p>
 * Created: Fri Mar 28 15:54:32 2003
 * </p>
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @author <a href="mailto:knuterik@onemap.org">Knut-Erik Johnsen</a>, Project OneMap
 * @version 2.0.0
 */
public class VPFDataStoreFactory implements DataStoreFactorySpi {
    public VPFDataStoreFactory() {
    }

    public String getDescription() {
        return "Vector Product Format data store implementation.";
    }

    public boolean canProcess(Map hashMap) {
        if (!hashMap.containsKey("dir")) {
            return false;
        }

        String dir = (String) hashMap.get("dir");

        try {
            File file = new File(dir);

            //System.out.println( "Filen eksisterer...: " + file.exists() );
            //System.out.println( "Filen er et dir....: " + file.isDirectory());
            //System.out.println( "Svaret blir........: " + (file.exists() && file.isDirectory()) );
            return (file.exists() && file.isDirectory());
        } catch (Exception e) {
            return false;
        }
    }

    public DataStore createDataStore(Map params) throws IOException {
        return create(params);
    }

    private DataStore create(Map params) throws IOException {
        //System.out.println( "CREATEDATASTORE" );
        String dir = (String) params.get("dir");

        return new VPFDataStore(new File(dir));
    }

    public DataStore createNewDataStore(Map params) throws java.io.IOException {
        return create(params);
    }

    public Param[] getParametersInfo() {
        //System.out.println( "GETPARAMETERSINFO" );
        Param directory = new Param("dir", String.class, 
                                    "Directory containting dht file", true);

        return new Param[] { directory,  };
    }

    public boolean isAvailable() {
        return true;
    }
}