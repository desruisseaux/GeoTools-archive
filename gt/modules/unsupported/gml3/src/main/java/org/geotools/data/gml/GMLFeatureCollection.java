package org.geotools.data.gml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.geotools.data.FeatureReader;
import org.geotools.data.store.DataFeatureCollection;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.xml.StreamingParser;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Envelope;

public class GMLFeatureCollection extends DataFeatureCollection {

	GMLTypeEntry entry;
	
	GMLFeatureCollection( GMLTypeEntry entry ) {
		this.entry = entry;
	}
	
	public FeatureType getSchema() {
		return entry.getFeatureType();
	}
	
	protected Iterator openIterator() throws IOException {
		return new GMLIterator( entry );
	}
	
	protected void closeIterator(Iterator close) throws IOException {
		((GMLIterator)close).close(); 
	}
	
	public Envelope getBounds() {
		//, look for bounds on feature collection
		InputStream input = null;
		try {
			input = entry.parent().document();
		}
		catch( IOException e ) {
			throw new RuntimeException( e );
		}
		
		Envelope bounds = null;
		try {
			StreamingParser parser = 
				new StreamingParser( entry.parent().configuration(), input, "/boundedBy" );
			bounds = (Envelope) parser.parse();
		} 
		catch( Exception e ) {
			throw new RuntimeException( e );
		}
		finally {
			try {
				input.close();
			} 
			catch (IOException e) { }
		}
	
		if ( bounds == null ) {
			//bounds must have not been declared, calculate manually
			try {
				bounds = new Envelope();
				FeatureReader reader = reader();
				if ( !reader.hasNext() ) {
					bounds.setToNull();
				}
				else {
					bounds.init( reader.next().getBounds() );
					while( reader.hasNext() ) {
						bounds.expandToInclude( reader.next().getBounds() );
					}
				}
				
				reader.close();
			}
			catch( Exception e ) {
				throw new RuntimeException( e );
			}
		}
		
		return bounds;
	}

	public int getCount() throws IOException {
		FeatureReader reader = reader();
		int count = 0;
		
		try {
			while( reader.hasNext() ) { 
				reader.next();
				count++;
			}
		} 
		catch( Exception e ) {
			throw (IOException) new IOException().initCause( e );
		}
		finally {
			reader.close();
		}
		
		return count;
	}

	public FeatureCollection collection() throws IOException {
		return this;
	}
	
	
}
