/*
 * Created on 11-set-2004
 */
package org.geotools.index.rtree.cachefs;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author Tommaso Nolli
 */
public class NodeCache extends LinkedHashMap {

    private static final Logger LOGGER = 
        Logger.getLogger("org.geotools.index.rtree");
    
    private final int maxElements;
    
    /**
     * Constructor
     */
    public NodeCache() {
        this(100);
    }
    
    /**
     * Constructor
     * @param capacity the capacity of the cache
     */
    public NodeCache(int capacity) {
        super(capacity);
        this.maxElements = capacity;
    }
    
    protected boolean removeEldestEntry(Entry eldest) {
        boolean ret = this.size() > this.maxElements;
        
        if (ret) {
            try {
                ((FileSystemNode)eldest.getValue()).flush();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        
        return ret;
    }
}
