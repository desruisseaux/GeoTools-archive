/**
 * 
 */
package org.geotools.data.shapefile;

import java.util.logging.Logger;

public class StreamLogging {
    private static final Logger LOGGER=Logger.getLogger("org.geotools.data.shapefile");

	private String name;
	private int open=0;
	/**
	 * The name that will appear in the debug message
	 * @param name
	 */
	public StreamLogging( String name){
		this.name=name;
	}
	
	/**
	 * Call when reader or writer is opened
	 */
	public synchronized void open(){
		open++;
		LOGGER.finest(name+" has been opened. Number open: "+open);
	}
	
	public synchronized void close(){
		open--;
		LOGGER.finest(name+" has been closed. Number open: "+open);
	}
	
}
