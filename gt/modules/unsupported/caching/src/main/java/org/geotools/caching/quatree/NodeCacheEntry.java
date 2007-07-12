package org.geotools.caching.quatree;

import org.geotools.caching.CacheEntry;
import org.geotools.caching.spatialindex.spatialindex.INode;

import java.util.ArrayList;


public class NodeCacheEntry implements CacheEntry {
    protected ArrayList linkedFeatures = new ArrayList();
    private final INode node;
    private final Integer key;
    private int hits;
    private long creationTime;
    private long lastAccessTime;
    private boolean valid = false;

    public NodeCacheEntry(INode node) {
        this.node = node;
        key = new Integer(node.getIdentifier());
        hits = 0;
        creationTime = System.currentTimeMillis();
        lastAccessTime = creationTime;
    }

    public long getCost() {
        // TODO Auto-generated method stub
        return -1;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public long getExpirationTime() {
        // TODO Auto-generated method stub
        return -1;
    }

    public int getHits() {
        return hits;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public long getLastUpdateTime() {
        return -1;
    }

    public long getVersion() {
        // TODO Auto-generated method stub
        return -1;
    }

    public boolean isValid() {
        return valid;
    }

    public void invalidate() {
        valid = false;
        linkedFeatures.clear();
    }

    public void setValid() {
        valid = true;
    }

    public Object getKey() {
        return key;
    }

    public Object getValue() {
        return node;
    }

    public Object setValue(Object arg0) {
        throw new UnsupportedOperationException();
    }

    public void hit() {
        hits++;
        lastAccessTime = System.currentTimeMillis();
    }
}
