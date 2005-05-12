/*
 * Created on 2005. 4. 20
 *
 */
package org.geotools.data.gml;

import java.io.*;
import java.net.URI;

import org.geotools.data.AbstractDataStore;
import org.geotools.data.FeatureReader;
import org.geotools.feature.FeatureType;
import org.geotools.xml.gml.FCBuffer;
import org.xml.sax.SAXException;

/**
 * 
 * (DataStore class for handling a directory of GML files.
 * Each file should have the extension .gml or .xml and
 * contain the data of one layer (one file - one layer).)
 * ...CURRENTLY this is just one dir - one layer...
 * 
 * The datastore is "Read Only" as it should only be used
 * to open GML files in Geoserver. There are better techniques
 * to write GML file than the one used in this class to read the
 * data. 
 * 
 * @author adanselm
 * @author dzwiers
 * 
 * TODO Check for a cleaner way to do
 */
public class GMLDataStore extends AbstractDataStore {

	protected File directory;
	private URI uri;
	private FCBuffer fcbuffer = null;
	
	/* Sets the store's directory.
	 * Mark the store as read-only.
	 */
	public GMLDataStore(URI udir){
		super(false); //does not allow writing
		
		File dir = new File(udir.getPath());
		if( !dir.isDirectory() ){
			throw new IllegalArgumentException(dir + " is not a Directory");
		}
		directory = dir;
	}
	
	/* Gets the name of all the layers from the filenames
	 * in the directory.
	 * @see org.geotools.data.DataStore#getTypeNames()
	 */
	public String[] getTypeNames() throws IOException {
		// Create a filter
		FilenameFilter f = new FilenameFilter(){
			public boolean accept(File dir, String name){
				return name.endsWith(".gml") || name.endsWith(".xml");
			}
		};
		
		// Get the list of files
		String list[] = directory.list(f);
		
		for(int i=0;i < list.length;i++){
			list[i] = list[i].substring(0, list[i].lastIndexOf('.'));
		}
		
		return list;
	}

	/* Provides access to a FeatureType referenced by a type name.
	 * 
	 * Basically it parses the file using SAX, gets the featureReader
	 * and returns the result of the getFeatureType() method of
	 * the featureReader.
	 * 
	 * @see org.geotools.data.DataStore#getSchema(java.lang.String)
	 */
	public FeatureType getSchema(String typeName) throws IOException {
		// TODO CHECK LEAKS & CLEAN
		
		//if buffer on gml file already opened, just return the reader
		if((fcbuffer != null)&&(typeName == fcbuffer.getFeatureType().getTypeName()))
			return fcbuffer.getFeatureType();
		
		// dirty temporary way to support multiple extensions
		// I really don't like this kind of "if" forests...
		File file = new File( directory, typeName+".gml");
		if(!file.exists()){
			file = new File(directory, typeName+".xml");
		}
		if(!file.exists())
			throw new IOException("GML file doesn't exist: "+file.getName());
		
		// file to parse
		uri = file.toURI();
		
		FeatureType ft = null;
		try {
			fcbuffer = (FCBuffer)FCBuffer.getFeatureReader(uri,10,10000);
	    } catch (SAXException e) {
	    	throw new IOException(e.toString());
	    }
	    ft = fcbuffer.getFeatureType();
	    
	    return ft;
	}
	
	/* Returns a reference on the featureReader of the current FCBuffer.
	 * 
	 * @see org.geotools.data.AbstractDataStore#getFeatureReader(java.lang.String)
	 */
	protected FeatureReader getFeatureReader(String typeName)
			throws IOException {
		//Q: FCBuffer never closed : memory leak?
		//A: this is not a concern, as the http connection will clean up after itself, 
		//and the buffer is not cached, so it will clean up too (just make sure as a 
		//user you don't cache the reader ...
		
		//XMLSAXHandler.setLogLevel(Level.FINEST);
		//XSISAXHandler.setLogLevel(Level.FINEST);
		//XMLElementHandler.setLogLevel(Level.FINEST);
		//XSIElementHandler.setLogLevel(Level.FINEST);
		
		try{
			return (FeatureReader)FCBuffer.getFeatureReader(uri,10,10000);
		} catch (SAXException sxe){
			sxe.printStackTrace();
			return null;
		}
	}

}
