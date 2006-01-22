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

import java.io.File;
import java.util.Iterator;

import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.data.coverage.grid.UnknownFormat;
import org.opengis.coverage.grid.Format;

/**
 * A CatalogEntry that refers to a file and a metadata that describes the file
 *
 * The files should be a GridCoverage file
 *
 * @author $author$
 * @version $Revision: 1.9 $
 */
public class FSCatalogEntry {
    private File resource;
    private FileMetadata metadata;

    /**
     * Creates a new FSCatalogEntry object.
     *
     * @param f a GridCoverage file
     */
    public FSCatalogEntry(File f, org.opengis.coverage.grid.Format[] formats) {
        resource = f;


        for (int i = 0; i < formats.length; i++) {
            Format format = formats[i];
            if( ((AbstractGridFormat)format).accepts(f) )
                metadata = new FileMetadataImpl(f, format);
        }
        if( metadata==null )
            metadata = new FileMetadataImpl(f, new UnknownFormat());
    }

    /**
     * @see opengis.catalog.CatalogEntry#getDataName()
     */
    public String getDataName() {
        return resource.getPath();
    }

    /**
     * @see opengis.catalog.CatalogEntry#getMetaDataEntityNames()
     */
    public String[] getMetadataNames() {
        String[] n = new String[1];
        n[0] = resource.getPath();

        return n;
    }

    /**
     * @see opengis.catalog.CatalogEntry#getMetadata(java.lang.String)
     */
    public FileMetadata getMetadata(String name) {
        return metadata;
    }

    /**
     * @see opengis.catalog.CatalogEntry#getResource()
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
     * @see opengis.catalog.CatalogEntry#getNumMetaDataEntity()
     */
    public int getNumMetadata() {
        return 1;
    }

    /* (non-Javadoc)
     * @see org.geotools.catalog.CatalogEntry#getMetadata(int)
     */
    public FileMetadata getMetadata(int index) {
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
