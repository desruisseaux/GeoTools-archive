package org.geotools.process.impl;

import java.util.HashMap;
import java.util.Map;

import org.geotools.process.Process;
import org.geotools.process.ProcessFactory;
import org.geotools.util.NullProgressListener;
import org.opengis.util.ProgressListener;

/**
 * Provide an implementation of the process method to implement your own Process.
 * <p>
 * This is a straight forward abstract process that has all the fields filled in.
 * </p>
 * @author Jody
 */
public class AbstractProcess implements Process {
    
    protected ProcessFactory factory;
    protected Map<String, Object> result = new HashMap<String, Object>();
    protected Map<String, Object> input;

    protected AbstractProcess( ProcessFactory factory ){
        this.factory = factory;
    }    
    public ProcessFactory getFactory() {
        return factory;
    }
    public Map<String, Object> getResult() {
        return result;
    }

    public void process( ProgressListener monitor ) {
        // implement your own process here
        // you can refer to input
        // and result as needed
    }
    
    public void setInput( Map<String, Object> input ) {
        this.input = input;
    }
 
}
