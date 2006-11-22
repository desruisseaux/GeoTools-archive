package org.geotools.data.gml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.geotools.feature.Feature;
import org.geotools.xml.StreamingParser;

public class GMLIterator implements Iterator {

	/**
	 * type entry
	 */
	GMLTypeEntry entry;
	/**
	 * The parser + input
	 */
	InputStream input;
	StreamingParser parser;
	
	/**
	 * The next feature 
	 */
	Feature feature;
	
	GMLIterator( GMLTypeEntry entry ) throws IOException {

		this.entry = entry;
		
		try {
			input = entry.parent().document();
			parser = new StreamingParser( 
				entry.parent().configuration(), input, "//" + entry.getTypeName()  
			);
		} 
		catch( Exception e ) {
			throw (IOException) new IOException().initCause( e );
		}
	}
	
	public Object next() {
		return feature;
	}

	public boolean hasNext() {
		feature = (Feature) parser.parse();
		return feature != null;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	public void close() throws IOException {
		input.close();
		input = null;
		parser = null;
		feature = null;
	}
}
