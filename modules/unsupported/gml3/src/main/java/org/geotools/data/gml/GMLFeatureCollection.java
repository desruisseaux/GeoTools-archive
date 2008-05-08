package org.geotools.data.gml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.geotools.data.FeatureReader;
import org.geotools.data.store.DataFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.xml.StreamingParser;

public class GMLFeatureCollection extends DataFeatureCollection {

	GMLTypeEntry entry;
	
	GMLFeatureCollection( GMLTypeEntry entry ) {
		super(null,entry.getFeatureType());
		this.entry = entry;
	}
	
	
	protected Iterator openIterator() throws IOException {
		return new GMLIterator( entry );
	}
	
	protected void closeIterator(Iterator close) throws IOException {
		((GMLIterator)close).close(); 
	}
	
	public ReferencedEnvelope getBounds() {
		//, look for bounds on feature collection
		InputStream input = null;
		try {
			input = entry.parent().document();
		}
		catch( IOException e ) {
			throw new RuntimeException( e );
		}
		
		ReferencedEnvelope bounds = null;
		try {
			StreamingParser parser = 
				new StreamingParser( entry.parent().configuration(), input, "/boundedBy" );
			bounds = (ReferencedEnvelope) parser.parse();
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
				bounds = new ReferencedEnvelope();
				FeatureReader reader = reader();
				if ( !reader.hasNext() ) {
					bounds.setToNull();
				}
				else {
					bounds.init( reader.next().getBounds() );
					while( reader.hasNext() ) {
						bounds.include( reader.next().getBounds() );
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
