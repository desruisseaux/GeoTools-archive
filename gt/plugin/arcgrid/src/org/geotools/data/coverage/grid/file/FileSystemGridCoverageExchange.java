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

import org.geotools.catalog.AbstractCatalog;
import org.geotools.catalog.CatalogEntry;
import org.geotools.data.coverage.grid.GridCoverageExchange;
import org.geotools.data.coverage.grid.GridCoverageReader;
import org.geotools.data.coverage.grid.GridCoverageWriter;
import org.geotools.data.coverage.grid.GridFormatFactorySpi;
import org.opengis.coverage.grid.Format;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;


/**
 * A GridCoverageExchange that searches a directory (or a branch if recursive is set to true)
 * and creates a set of catalog entries with a FileMetadata for each entry.
 * 
 * Is both a GridCoverageExchange and a Catalog
 *
 * @author $author$
 * @version $Revision: 1.9 $
 */
public class FileSystemGridCoverageExchange extends AbstractCatalog
    implements GridCoverageExchange {
    private static Set extensions;
    FormatManager formatManager = FormatManager.getFormatManager();
    private File root;
    private boolean recursive;
    private Format[] formats;

    /**
     * Creates a new FileSystemGridCoverageExchange object.
     */
    public FileSystemGridCoverageExchange() {
    }

    /**
     * Examines the associated directories again to see if any changes have been made
     * 
     * Not very efficient at the moment.  Should be updated in the future.
     * Each time it is called all the current are discarded and new ones are built
     * 
     */
    public void refresh() {
        if (root != null) {
            refresh(root, recursive);
        }
    }

    private void refresh(File file, boolean recursive) {
        entries=new ArrayList();
        File[] files = file.listFiles(formatManager.getFileFilter());

        for (int j = 0; j < files.length; j++) {
            if (files[j].isFile()) {
                entries.add(new FSCatalogEntry(files[j]));
            }

            if (files[j].isDirectory()) {
                if (recursive) {
                    refresh(files[j], recursive);
                }
            }
        }
    }

    /**
     * Add one file to the Catalog/Exchange
     *
     * @param f The File to be added to the catalog
     */
    public void add(File f) {
        if (!entries.contains(f)) {
            entries.add(new FSCatalogEntry(f));
        }
    }

    /**
     * @see org.geotools.gc.exchange.GridCoverageExchange#getFormats()
     */
    public Format[] getFormats() {
        ArrayList list = new ArrayList();

        return formatManager.getFormats();
    }

    /**
     * @see org.geotools.gc.exchange.GridCoverageExchange#getReader(java.lang.Object)
     */
    public GridCoverageReader getReader(Object source)
        throws IOException {
        assert (source instanceof CatalogEntry);

        CatalogEntry entry = (CatalogEntry) source;
        Format format = ((FileMetadata) entry.getMetadata(0)).getFormat();

        GridFormatFactorySpi factory = formatManager.getFactory(format);

        if (factory != null) {
            return factory.createReader(entry.getResource());
        }

        return null;
    }

    /**
     * @see org.geotools.gc.exchange.GridCoverageExchange#getWriter(java.lang.Object,
     *      org.opengis.coverage.grid.Format)
     */
    public GridCoverageWriter getWriter(Object destination, Format format)
        throws IOException {
        assert (destination instanceof File)
        || (destination instanceof FileOutputStream)
        || (destination instanceof FileWriter);

        GridFormatFactorySpi factory = formatManager.getFactory(format);

        if (factory != null) {
            return factory.createWriter(destination);
        }

        return null;
    }

    /**
     * @see org.geotools.gc.exchange.GridCoverageExchange#dispose()
     */
    public void dispose() throws IOException {
    }

    /**
     * @see org.opengis.catalog.Catalog#add(org.opengis.catalog.CatalogEntry)
     */
    public void add(CatalogEntry entry) throws IllegalStateException {
        throw new IllegalStateException(
            "Only the FileSystemGridCoverageExchange has permissions to add CatalogEntries");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opengis.catalog.Catalog#remove(org.opengis.catalog.CatalogEntry)
     */
    public void remove(CatalogEntry entry) throws IllegalStateException {
        throw new IllegalStateException(
            "Only the FileSystemGridCoverageExchange has permissions to remove CatalogEntries");
    }

    /**
     * @see org.geotools.data.coverage.grid.GridCoverageExchange#isAvailable()
     */
    public boolean isAvailable() {
        return true;
    }

    /**
     * @see org.geotools.data.coverage.grid.GridCoverageExchange#accepts(java.net.URL)
     */
    public boolean setDataSource(Object datasource) {
        URL url = null;

        if (datasource instanceof String) {
            try {
                url = new URL((String) datasource);
            } catch (MalformedURLException e) {
            }
        } else if (datasource instanceof URL) {
            url = (URL) datasource;
        } else if (datasource instanceof File) {
            root = (File) datasource;
            refresh(root, recursive);
        }

        if ((url != null) && (url.getFile().length() > 0)) {
            root = new File(url.getFile());
            refresh(root, recursive);

            return true;
        }

        return false;
    }

    /**
     * Return true if the root and all subdirectories are searched for files
     * 
     * @return true if the root and all subdirectories are searched for files
     * 		false otherwise
     */
    public boolean isRecursive() {
        return recursive;
    }

    /**
     * Sets whether the entire branch starting at root is part of the catalog or just
     * a single directory
     * 
     * @param recursive True means that root and all subdirectories are searched files
     *                 false means just the root is searched
     */
    public void setRecursive(boolean recursive) {
        this.recursive = recursive;

        if (root != null) {
            refresh(root, recursive);
        }
    }
}
