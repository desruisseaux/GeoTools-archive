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
/*
 * Created on Aug 3, 2004
 *
 */
package org.geotools.data.vpf.file;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Iterator;

import org.geotools.data.AbstractDataStore;
import org.geotools.data.FeatureReader;
import org.geotools.feature.FeatureType;
import org.geotools.feature.SchemaException;


/**
 * A data store for VPF files. Any file can be retrieved from here.
 * If you want joins (for example features with their geometries), 
 * you will have to look elsewhere.
 * Hopefully some one will take advantage of this class to provide 
 * the full functionality.
 * @author <a href="mailto:jeff@ionicenterprise.com">Jeff Yutzler</a>
 */
public class VPFFileStore extends AbstractDataStore {
    /**
     * A collection of files which are available
     * Don't ask me how/when to close them!
     */
    private AbstractMap files;

    /**
     * Default constructor. Nothing special
     *
     */
    public VPFFileStore() {
        files = new HashMap();
    }

    /* (non-Javadoc)
     * @see org.geotools.data.AbstractDataStore#getTypeNames()
     */
    public String[] getTypeNames() {
        String[] result = new String[files.size()];
        int counter = 0;
        VPFFile currentFile;
        Iterator iter = files.keySet().iterator();

        while (iter.hasNext()) {
            currentFile = (VPFFile) iter.next();
            result[counter] = currentFile.getTypeName();
        }

        return result;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.AbstractDataStore#getSchema(java.lang.String)
     */
    public FeatureType getSchema(String pathName) throws IOException {
        FeatureType result = null;

        if (files.containsKey(pathName)) {
            result = (FeatureType) files.get(pathName);
        } else {
            try {
                result = new VPFFile(pathName);
            } catch (SchemaException exc) {
                throw new IOException("Schema error in path: " + pathName
                    + "\n" + exc.getMessage());
            }

            files.put(pathName, result);
        }

        return result;
    }

    // How on earth does one get from the query to this method?
    /* (non-Javadoc)
     * @see org.geotools.data.AbstractDataStore#getFeatureReader(java.lang.String)
     */
    protected FeatureReader getFeatureReader(String pathName)
        throws IOException {
        return new VPFFileFeatureReader((VPFFile) getSchema(pathName));
    }
}
