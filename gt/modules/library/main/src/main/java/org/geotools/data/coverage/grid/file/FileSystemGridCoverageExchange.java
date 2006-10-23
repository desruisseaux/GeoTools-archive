/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.data.coverage.grid.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.data.coverage.grid.GridFormatFinder;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageExchange;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;


/**
 * A GridCoverageExchange that searches a directory (or a branch if recursive is set to true)
 * and creates a set of catalog entries with a FileMetadata for each entry.
 *
 * Is both a GridCoverageExchange and a Catalog
 *
 * @author $author$
 * @author <a href="mailto:simboss_ml@tiscali.it">Simone Giannecchini (simboss)</a>
 * @source $URL$
 * @version $Revision: 1.9 $
 */
public class FileSystemGridCoverageExchange implements GridCoverageExchange {
    private File root;
    private boolean recursive;
    private Format[] formats;
    private List entries;
    /**
     * Creates a new FileSystemGridCoverageExchange object.
     */
    public FileSystemGridCoverageExchange() {
        formats=GridFormatFinder.getFormatArray();
    }

    /**
     * Creates a FileSystemGridCoverageExchange that
     */
    public FileSystemGridCoverageExchange( File root ) {
        this.root=root;
        formats=GridFormatFinder.getFormatArray();
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
        	entries=new ArrayList();
    		refresh(root);
        }
    }

    private void refresh(File file) {
    	if (file.isDirectory()) {
			File[] files = file.listFiles(new FormatFileFilter(formats,recursive));
			final int length=files.length;
			for (int j = 0; j < length; j++) {
			    if (files[j].isFile()) {
			        entries.add(new FSCatalogEntry(files[j],formats));
			    }
			
			    if (files[j].isDirectory()) {
			        if (recursive) {
			            refresh(files[j]);
			        }
			    }
			}
    	} else if (file.isFile()) {
	        entries.add(new FSCatalogEntry(file,formats));
    	}

    }

    /**
     * Add one file to the Catalog/Exchange
     *
     * @param f The File to be added to the catalog
     */
    public void add(File f) {
        if (!entries.contains(f)) {
            entries.add(new FSCatalogEntry(f, formats));
        }
    }

    /**
     * @see org.geotools.gc.exchange.GridCoverageExchange#getFormats()
     */
    public Format[] getFormats() {
        Format[] f=new Format[formats.length];
        System.arraycopy(formats,0,f,0,formats.length);
        return f;
    }

    /**
     * @see org.geotools.gc.exchange.GridCoverageExchange#getReader(java.lang.Object)
     */
    public GridCoverageReader getReader(Object source)
        throws IOException {
        assert (source instanceof FSCatalogEntry);

        FSCatalogEntry entry = (FSCatalogEntry) source;
        Format format = entry.getMetadata(0).getFormat();

        return ((AbstractGridFormat)format).getReader(source);
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

        return ((AbstractGridFormat)format).getWriter(destination);
    }

    /**
     * @see org.geotools.gc.exchange.GridCoverageExchange#dispose()
     */
    public void dispose() throws IOException {
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
        	refresh();
        }

        if ((url != null) && (url.getFile().length() > 0)) {
            root = new File(url.getFile());
            refresh(root);

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
            refresh(root);
        }
    }

	public List getFiles() {
		return Collections.unmodifiableList( entries );
	}
}
