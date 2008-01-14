/**
 * 
 */
package org.geotools.index;

import java.io.IOException;
import java.util.Collection;

/**
 * Tag interface for collection that must be closed 
 * 
 * @author jesse
 */
public interface CloseableCollection<T> extends Collection<T>{

    /**
     * Close the collection so it cleans up its resources
     */
    void close() throws IOException;
}
