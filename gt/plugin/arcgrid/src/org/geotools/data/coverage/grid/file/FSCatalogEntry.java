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
package org.geotools.data.coverage.grid.file;

import org.geotools.catalog.CatalogEntry;
import org.geotools.metadata.Metadata;
import java.io.File;
import java.util.Iterator;


/**
 * A CatalogEntry that refers to a file and a metadata that describes the file
 * 
 * The files should be a GridCoverage file
 *
 * @author $author$
 * @version $Revision: 1.9 $
 */
public class FSCatalogEntry implements CatalogEntry {
    FormatManager formatManager = FormatManager.getFormatManager();
    private File resource;
    private FileMetadata metadata;

    /**
     * Creates a new FSCatalogEntry object.
     *
     * @param f a GridCoverage file
     */
    public FSCatalogEntry(File f) {
        resource = f;

        String ext = f.getName();
        ext = ext.substring(ext.lastIndexOf('.') + 1);
        metadata = new FileMetadataImpl(f, formatManager.getFormat(f));
    }

    /**
     * @see org.geotools.catalog.CatalogEntry#getDataName()
     */
    public String getDataName() {
        return resource.getPath();
    }

    /**
     * @see org.geotools.catalog.CatalogEntry#getMetaDataEntityNames()
     */
    public String[] getMetadataNames() {
        String[] n = new String[1];
        n[0] = resource.getPath();

        return n;
    }

    /**
     * @see org.geotools.catalog.CatalogEntry#getMetadata(java.lang.String)
     */
    public Metadata getMetadata(String name) {
        return metadata;
    }

    /**
     * @see org.geotools.catalog.CatalogEntry#getResource()
     */
    public Object getResource() {
        return resource;
    }

    /* (non-Javadoc)
     * @see org.geotools.catalog.CatalogEntry#iterator()
     */
    public Iterator iterator() {
        return new FSIterator();
    }

    /**
     * @see org.geotools.catalog.CatalogEntry#getNumMetaDataEntity()
     */
    public int getNumMetadata() {
        return 1;
    }

    /* (non-Javadoc)
     * @see org.geotools.catalog.CatalogEntry#getMetadata(int)
     */
    public Metadata getMetadata(int index) {
        if (index < 0) {
            return null;
        }

        return metadata;
    }

    /**
     * An iterator that will iterate over the *one* metadata contained by the catalog entry
     *
     * @author $author$
     * @version $Revision: 1.9 $
     */
    private class FSIterator implements Iterator {
        boolean next = false;

        /**
         * @see java.util.Iterator#remove()
         */
        public void remove() {
        }

        /**
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            if (next) {
                return false;
            }

            return true;
        }

        /**
         * @see java.util.Iterator#next()
         */
        public Object next() {
            if (next) {
                return null;
            }

            return metadata;
        }
    }
}
