/**
 * 
 */
package org.geotools.data.shapefile;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Lock{
	
	Logger logger=Logger.getLogger("ShapefileLogger.Lock");
	{
		logger.setLevel(Level.FINE);
	}

	public final static int DELAY=200;
	int level=0;
	public synchronized void startRead() throws IOException {
		while( level<0 ){
			try {
				wait(DELAY);
			} catch (InterruptedException e) {
				throw (IOException) new IOException().initCause(e);
			}    				
		}
		assertTrue( level>-1 ); 
		level++;
		logger.fine("Start Read Lock:"+level);
	}
	
	private void assertTrue(boolean b) {
		if (!b)
			throw new AssertionError();
	}
	public synchronized void endRead() {
		assertTrue( level>0 );
		level--;
		logger.fine("End Read Lock:"+level);
		notifyAll();
	}
	public synchronized void startWrite() throws IOException {
		while( level!=0 ){
			try {
				wait(DELAY);
			} catch (InterruptedException e) {
				throw (IOException) new IOException().initCause(e);
			}    		 				
		}
		assertTrue( level==0 );
		level=-1;
		logger.fine("Start Write Lock:"+level);
	}
	public synchronized void endWrite() {
		assertTrue( level==-1 );
		level=0;
		logger.fine("End Write Lock:"+level);
		notifyAll();
	}
}