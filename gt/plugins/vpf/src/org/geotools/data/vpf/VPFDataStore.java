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
package org.geotools.data.vpf;

import java.io.File;
import java.io.IOException;

import org.geotools.data.AbstractDataStore;
import org.geotools.data.FeatureReader;
import org.geotools.feature.FeatureType;
import org.geotools.feature.SchemaException;


/**
 * Class <code>VPFDataSource</code> implements
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @author <a href="mailto:knuterik@onemap.org">Knut-Erik Johnsen</a>, Project
 *         OneMap
 * @version $Id$
 * @deprecated
 */
public class VPFDataStore extends AbstractDataStore {
    /** Comment for <code>dataBase</code> */
    private VPFDataBase dataBase = null;

    /**
     * Default constructor
     *
     * @param file 
     *
     * @throws IOException
     */
    public VPFDataStore(File file) throws IOException, SchemaException {
        dataBase = new VPFDataBase(file);
    }

    //    public VPFFeatureReader getFeatureReader2(String typename) throws java.io.IOException {
    //        return new VPFFeatureReader(typename, dataBase, VPFSchemaCreator.getSchema(typename));
    //    }
    //

    /* (non-Javadoc)
     * @see org.geotools.data.AbstractDataStore#getFeatureReader(java.lang.String)
     */
    protected FeatureReader getFeatureReader(String typename)
        throws java.io.IOException {
        // Find the appropriate feature type, make a reader for it, and reset its stream
        // TODO
        FeatureReader result = null;
        return result;
//        return new VPFFeatureReader(typename, dataBase,
//            VPFSchemaCreator.getSchema(typename));
    }

    /*
     *  (non-Javadoc)
     * @see org.geotools.data.DataStore#getSchema(java.lang.String)
     */
    public FeatureType getSchema(String str) throws IOException {
        // TODO
        return VPFSchemaCreator.getSchema(str);
    }

    /*
     *  (non-Javadoc)
     * @see org.geotools.data.DataStore#getTypeNames()
     */
    public String[] getTypeNames() {
        
        // TODO
        return VPFSchemaCreator.getTypeNames();
    }
}
