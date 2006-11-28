/**
 * 
 */
package org.geotools.data.grid;

import org.opengis.util.InternationalString;

public interface ConnectionStatus {
    /**
     * InternationalString allows us to provide a human readable
     * description of the problem.
     * <p>
     * Application specific translations will be provided as per usual
     * InternationalString guidelines (to be figured out with Martin). 
     * @return
     */
    InternationalString getMessage();
    
    /**
     * Used to describe processing steps (such as creating an index or breaking
     * an image into tiles) that has been performed as part of the loading process.
     */
    interface Info extends ConnectionStatus {        
    }
    interface Warning extends ConnectionStatus{        
    }
    interface Error extends ConnectionStatus{        
        Throwable getCause();
    }
}