/*
 * Created on 19-ago-2004
 */
package org.geotools.index.quadtree;


/**
 * @author Tommaso Nolli
 */
public interface IndexStore {

    /**
     * Stores a <code>QuadTree</code> 
     * @param tree the <code>QuadTree</code> to store
     * @throws StoreException
     */
    public void store(QuadTree tree) throws StoreException;
    
    /**
     * Loads a <code>QuadTree</code>
     * @return the loaded <code>QuadTree</code>
     * @throws StoreException
     */
    public QuadTree load() throws StoreException;
    
}
