package org.geotools.kml.bindings;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.geotools.styling.FeatureTypeStyle;

/**
 * Simple container for holding styles by uri.
 * <p>
 * This is lame as it is just a hash map in memory. It should really be an 
 * embedded db that serializes / deserializes out to disk.
 * </p>
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class StyleMap {

    protected Map map = Collections.synchronizedMap(new HashMap());
    
    protected void put( URI uri, FeatureTypeStyle style ) { 
        map.put(uri,style);
    }
    
    protected FeatureTypeStyle get( URI uri ) {
        return (FeatureTypeStyle) map.get(uri);
    }
}
