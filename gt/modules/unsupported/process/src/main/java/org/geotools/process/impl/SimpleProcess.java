package org.geotools.process.impl;

import java.util.HashMap;
import java.util.Map;

import org.geotools.process.Process;
import org.geotools.process.ProcessFactory;
import org.geotools.util.NullProgressListener;
import org.opengis.util.ProgressListener;

/**
 * Provide an implementation for a simple process (ie so quick and easy it
 * is not going to need to report progress as it goes).
 * 
 * @author Jody
 */
public abstract class SimpleProcess extends AbstractProcess {
    /** Can only run once... should not need to check this but we are being careful */
    private boolean started = false; 
    
    protected SimpleProcess( ProcessFactory factory ){
        super( factory );
    } 
    final public void process( ProgressListener monitor ) {
        if (started) throw new IllegalStateException("Process can only be run once");
        started = true;
        
        if( monitor == null ) monitor = new NullProgressListener();
        try {
            if( monitor.isCanceled() ) return; // respect isCanceled
            process(); 
        }
        catch( Throwable eek){
            monitor.exceptionOccurred( eek );
        }
        finally {
            monitor.complete();            
        }
    }
    /**
     * Implement your own process here.
     * 
     * @throws Exception
     */
    public abstract void process() throws Exception;
 
    /** Used by process implementation to access the input */
    protected Object get( String key ){
        return input.get( key );
    }
}
