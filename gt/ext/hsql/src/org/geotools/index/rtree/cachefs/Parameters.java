/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package org.geotools.index.rtree.cachefs;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.Stack;

import org.geotools.index.DataDefinition;
import org.geotools.index.TreeException;

/**
 * @author Tommaso Nolli
 */
public class Parameters {

    private int maxNodeEntries;
    private int minNodeEntries;
    private short splitAlg;
    private DataDefinition dataDef;
    private FileChannel channel;
    private Stack freePages;
    private boolean forceChannel;
    private NodeCache cache;
    private long newNodeOffset;

    public Parameters() {
        this.freePages = new Stack();
        this.cache = new NodeCache();
    }

	/**
	 * @return
	 */
	public FileChannel getChannel() {
		return channel;
	}

	/**
	 * @return
	 */
	public DataDefinition getDataDef() {
		return dataDef;
	}

	/**
	 * @return
	 */
	public int getMaxNodeEntries() {
		return maxNodeEntries;
	}

	/**
	 * @return
	 */
	public int getMinNodeEntries() {
		return minNodeEntries;
	}

	/**
	 * @return
	 */
	public short getSplitAlg() {
		return splitAlg;
	}

	/**
	 * @param channel
	 */
	public void setChannel(FileChannel channel) {
		this.channel = channel;
	}

	/**
	 * @param definition
	 */
	public void setDataDef(DataDefinition definition) {
		dataDef = definition;
	}

	/**
	 * @param i
	 */
	public void setMaxNodeEntries(int i) {
		maxNodeEntries = i;
	}

	/**
	 * @param i
	 */
	public void setMinNodeEntries(int i) {
		minNodeEntries = i;
	}

	/**
	 * @param s
	 */
	public void setSplitAlg(short s) {
		splitAlg = s;
	}

	/**
	 * @return
	 */
	public boolean getForceChannel() {
		return forceChannel;
	}

	/**
	 * @param b
	 */
	public void setForceChannel(boolean b) {
		forceChannel = b;
	}

	/**
	 * @return
	 */
	public Stack getFreePages() {
		return freePages;
	}

	/**
	 * @param stack
	 */
	public void setFreePages(Stack stack) {
		freePages = stack;
	}
    
    public synchronized void setNodeCacheSize(int size) throws TreeException {
        
        if (this.cache != null) {
            this.flushCache();
        }
        
        if (size == 0) {
            this.cache = null;
        } else if (size < 0) {
            this.cache = new NodeCache();
        } else {
            this.cache = new NodeCache(size);
        }
    }
    
    /**
     * Gets a <code>FileSystemNode</code> from the cache, if the node is
     * non there, a new node will be created and added to the cache.
     * @param offset The node offset
     * @return a <code>FileSystemNode</code>
     * @throws IOException
     * @throws TreeException
     */
    public synchronized FileSystemNode getFromCache(long offset) 
    throws IOException, TreeException
    {
        FileSystemNode node = null;
        if (this.cache != null) {
            node = (FileSystemNode)this.cache.get(new Long(offset));
        }
        
        if (node == null) {
            node = new FileSystemNode(this, offset);
            this.putToCache(node);
        }
        
        return node;
    }
    
    /**
     * 
     * @param len
     * @return
     */
    public synchronized long getNewNodeOffset(int len) throws IOException {
        long offset = 0L;
        
        if (this.newNodeOffset == 0L) {
            offset = this.channel.size();
        } else {
            offset = this.newNodeOffset;
        }
        
        this.newNodeOffset = offset + len;
        return offset;
    }
    
    /**
     * Soters a <code>FileSystemNode</code> in the cache.
     * @param node the <code>FileSystemNode</code> to store
     * @throws TreeException
     */
    public synchronized void putToCache(FileSystemNode node)
    throws TreeException 
    {
        if (this.cache != null) {
            // If we have a cache store the node, we'll flush it later
            this.cache.put(new Long(node.getOffset()), node);
        } else {
            // Else flush the node to disk
            node.flush();
        }
    }
    
    /**
     * Removes a node from the cache
     * @param node the node to remove
     */

    public synchronized void removeFromCache(FileSystemNode node) {
        if (this.cache != null) {
            this.cache.remove(node);
        }
    }
    
    /**
     * Flushes all nodes and clears the cache
     * @throws TreeException
     */
    public synchronized void flushCache() throws TreeException {
        if (this.cache == null) {
            return;
        }
        
        Iterator iter = this.cache.keySet().iterator();
        
        while (iter.hasNext()) {
            FileSystemNode element = 
                (FileSystemNode)this.cache.get(iter.next());
            element.flush();
        }
        
        this.cache.clear();
    }
    
}
