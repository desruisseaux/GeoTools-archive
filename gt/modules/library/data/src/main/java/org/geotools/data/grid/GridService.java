package org.geotools.data.grid;

import java.io.IOException;
import java.util.List;

import org.geotools.catalog.Service;
import org.geotools.util.ProgressListener;

public interface GridService extends Service {

    /**
     * Describes the connection status of the "preferred" resource,
     * ie the same one described by the Service.getMessage().
     * 
     * TODO steal description of this concept from uDig javadocs 
     */
    List/**ConnectionStatus*/ getConnectionStatus(); // TODO: Consider this as an extention to Serice that let's us hack at warnings in a user friendly fashion
    
    /**
     * Provide access to our preferred resourc (ie GridAccess)
     * <p>
     * This method is called by our implementation of resolve( GridAccess.class, PorgressListener )
     * </p>
     * @return GridAccess
     */
    GridAccess access();
    
    /**
     * @return List<GridGeoResource>
     */
    List members( ProgressListener monitor ) throws IOException;
}
