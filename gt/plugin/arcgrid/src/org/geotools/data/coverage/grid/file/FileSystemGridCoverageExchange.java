/*
 * Created on Jun 28, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.coverage.grid.file;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.geotools.catalog.AbstractCatalog;
import org.geotools.catalog.CatalogEntry;
import org.geotools.data.GridFormatFactorySpi;
import org.geotools.data.GridFormatFinder;
import org.geotools.data.coverage.grid.GridCoverageExchange;
import org.geotools.data.coverage.grid.GridCoverageReader;
import org.geotools.data.coverage.grid.GridCoverageWriter;
import org.opengis.coverage.grid.Format;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * @author jeichar
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class FileSystemGridCoverageExchange extends AbstractCatalog implements
		GridCoverageExchange {

	private static Set extensions;
	FormatManager formatManager=FormatManager.getFormatManager();
	
	private File root;

	private boolean recursive;

	private Format[] formats;

	public FileSystemGridCoverageExchange(File root, boolean recursive) {
		this.root = root;
		this.recursive = recursive;
		refresh();
	}

	public void refresh() {
		if (root != null)
			refresh(root, recursive);
	}

	private void refresh(File file, boolean recursive) {

		File[] files = file.listFiles(formatManager.getFileFilter());
		for (int j = 0; j < files.length; j++) {
			if (files[j].isFile())
				entries.add(new FSCatalogEntry(files[j]));
			if (files[j].isDirectory())
				if (recursive)
					refresh(files[j], recursive);

		}
	}

	public void add(File f) {
		if (!entries.contains(f))
			entries.add(new FSCatalogEntry(f));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.gc.exchange.GridCoverageExchange#getFormats()
	 */
	public Format[] getFormats() {
		ArrayList list = new ArrayList();
		if (formats == null) {
			Iterator iter = GridFormatFinder.getAvailableFormats();
			while (iter.hasNext()) {
				GridFormatFactorySpi f = (GridFormatFactorySpi) iter.next();
				list.add(f.createFormat());
			}//while
			formats = new Format[list.size()];
			list.toArray(formats);
		}
		return formats;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.gc.exchange.GridCoverageExchange#getReader(java.lang.Object)
	 */
	public GridCoverageReader getReader(Object source) throws IOException {
		assert (source instanceof CatalogEntry);
		
		CatalogEntry entry=(CatalogEntry) source;
		Format format= ((FileMetadata)entry.getMetadata(0)).getFormat();
		
		GridFormatFactorySpi factory=formatManager.getFactory(format);
		if( factory != null)
		    return factory.createReader(entry.getResource());
		
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.gc.exchange.GridCoverageExchange#getWriter(java.lang.Object,
	 *      org.opengis.coverage.grid.Format)
	 */
	public GridCoverageWriter getWriter(Object destination, Format format)
			throws IOException {
		assert (destination instanceof File) || (destination instanceof FileOutputStream)
		|| (destination instanceof FileWriter);

		GridFormatFactorySpi factory=formatManager.getFactory(format);
		if( factory != null)
		    return factory.createWriter(destination);

		return null;
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.gc.exchange.GridCoverageExchange#dispose()
	 */
	public void dispose() throws IOException {
		// TODO Auto-generated method stub
	}

	/*
	 * (non-Javadoc)
	 * 
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

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageExchange#isAvailable()
     */
    public boolean isAvailable() {
        return true;
    }



}