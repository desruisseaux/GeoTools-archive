/*
 * Created on Jun 28, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.coverage.grid.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

import org.geotools.catalog.CatalogEntry;
import org.geotools.data.GridFormatFactorySpi;
import org.geotools.data.arcgrid.ArcGridFormatFactory;
import org.geotools.metadata.Metadata;
import org.opengis.coverage.grid.Format;

/**
 * @author jeichar
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FSCatalogEntry implements CatalogEntry{
	
	private File resource;
	private FileMetadata metadata;
	private static Properties properties;
	
	public FSCatalogEntry( File f ){
		resource=f;
		String ext=f.getName();
		ext=ext.substring(ext.lastIndexOf('.')+1);
		metadata=new FileMetadataImpl(f, FSCatalogEntry.getFormat(ext));
	}
	
	/* (non-Javadoc)
	 * @see org.geotools.catalog.CatalogEntry#getDataName()
	 */
	public String getDataName() {
		return resource.getPath();
	}

	/* (non-Javadoc)
	 * @see org.geotools.catalog.CatalogEntry#getMetaDataEntityNames()
	 */
	public String[] getMetadataNames() {
		String[] n=new String[1];
		n[0]=resource.getPath();
		return n;
	}

	/* (non-Javadoc)
	 * @see org.geotools.catalog.CatalogEntry#getMetadata(java.lang.String)
	 */
	public Metadata getMetadata(String name) {
		return metadata;
	}

	/* (non-Javadoc)
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

	/* (non-Javadoc)
	 * @see org.geotools.catalog.CatalogEntry#getNumMetaDataEntity()
	 */
	public int getNumMetadata() {
		return 1;
	}

	/* (non-Javadoc)
	 * @see org.geotools.catalog.CatalogEntry#getMetadata(int)
	 */
	public Metadata getMetadata(int index) {
		if( index < 0)
			return null;
		return metadata;
	}
	
	public static Format getFormat(String extension){

	    GridFormatFactorySpi factory=new ArcGridFormatFactory();
	    
		return factory.createFormat();
	}
	
	private class FSIterator implements Iterator{

		boolean next=false;
		
		/* (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {}

		/* (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			if( next )
				return false;
			return true;
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		public Object next() {
			if ( next )
				return null;
			return metadata;
		}
		
	}
}
