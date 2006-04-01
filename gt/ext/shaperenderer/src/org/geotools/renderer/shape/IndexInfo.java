/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.renderer.shape;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.geotools.data.DataSourceException;
import org.geotools.data.shapefile.indexed.RecordNumberTracker;
import org.geotools.data.shapefile.shp.IndexFile;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.index.Data;
import org.geotools.index.LockTimeoutException;
import org.geotools.index.TreeException;
import org.geotools.index.quadtree.QuadTree;
import org.geotools.index.quadtree.StoreException;
import org.geotools.index.quadtree.fs.FileSystemIndexStore;
import org.geotools.index.rtree.RTree;
import org.geotools.index.rtree.fs.FileSystemPageStore;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Encapsulates index information for a layer in the MapContext. The associated layer can be
 * obtained by
 * 
 * @author jones
 * @source $URL:
 *         http://svn.geotools.org/geotools/branches/2.2.x/ext/shaperenderer/src/org/geotools/renderer/shape/IndexInfo.java $
 */
public class IndexInfo {
    static final byte TREE_NONE = 0;
    static final byte R_TREE = 1;
    static final byte QUAD_TREE = 2;
    final byte treeType;
    final URL treeURL;
    final URL shxURL;
    private RTree rtree;
    private QuadTree qtree;

    public IndexInfo( byte treeType, URL treeURL, URL shxURL ) {
        this.treeType = treeType;
        this.treeURL = treeURL;
        this.shxURL = shxURL;
    }

    /**
     * RTree query
     * 
     * @param bbox
     * @return
     * @throws DataSourceException
     * @throws IOException
     */
    List queryRTree( Envelope bbox ) throws DataSourceException, IOException {
        List goodRecs = null;

        try {
            if ((rtree != null) && !bbox.contains(rtree.getBounds())) {
                goodRecs = rtree.search(bbox);
            }
        } catch (LockTimeoutException le) {
            throw new DataSourceException("Error querying RTree", le);
        } catch (TreeException re) {
            throw new DataSourceException("Error querying RTree", re);
        } finally {
            try {
                rtree.close();
            } catch (Exception ee) {
            }
        }

        return goodRecs;
    }

    /**
     * QuadTree Query
     * 
     * @param bbox
     * @return
     * @throws DataSourceException
     * @throws IOException
     * @throws TreeException DOCUMENT ME!
     */
    Collection queryQuadTree( Envelope bbox ) throws DataSourceException, IOException, TreeException {
        Collection tmp = null;

        try {
            if ((qtree != null) && !bbox.contains(qtree.getRoot().getBounds())) {
                tmp = qtree.search(bbox);
                
                if( tmp!=null && !tmp.isEmpty())
                	return tmp;
        }
            if( qtree!=null )
            	qtree.close();
        }catch (Exception e) {
        	ShapefileRenderer.LOGGER.warning(e.getLocalizedMessage());
		}

    	return null;
    }

    /**
     * Convenience method for opening an RTree index.
     * 
     * @return A new RTree.
     * @throws IOException If an error occurs during creation.
     * @throws DataSourceException DOCUMENT ME!
     */
    RTree openRTree() throws IOException {
        File file = new File(treeURL.getPath());

        RTree ret = null;

        try {
            FileSystemPageStore fps = new FileSystemPageStore(file);
            ret = new RTree(fps);
        } catch (TreeException re) {
            throw new DataSourceException("Error opening RTree", re);
        }

        return ret;
    }

    /**
     * Convenience method for opening a QuadTree index.
     * 
     * @return A new QuadTree
     * @throws StoreException
     */
    QuadTree openQuadTree() throws StoreException {
        File file = new File(treeURL.getPath());
        FileSystemIndexStore store = new FileSystemIndexStore(file);

        try {
			return store.load(openIndexFile());
		}  catch (IOException e) {
			throw new StoreException(e);
		}
    }

    /**
     * Convenience method for opening a ShapefileReader.
     * 
     * @return An IndexFile
     * @throws IOException
     */
    IndexFile openIndexFile() throws IOException {
        ReadableByteChannel rbc = getReadChannel(shxURL);

        if (rbc == null) {
            return null;
        }

        // return new IndexFile(rbc, this.useMemoryMappedBuffer);
        return new IndexFile(rbc, false);
    }

    /**
     * Obtain a ReadableByteChannel from the given URL. If the url protocol is file, a FileChannel
     * will be returned. Otherwise a generic channel will be obtained from the urls input stream.
     * 
     * @param url DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    private ReadableByteChannel getReadChannel( URL url ) throws IOException {
        ReadableByteChannel channel = null;

        if (url.getProtocol().equals("file")) {
            File file = new File(url.getFile());

            if (!file.exists() || !file.canRead()) {
                throw new IOException("File either doesn't exist or is unreadable : " + file);
            }

            FileInputStream in = new FileInputStream(file);
            channel = in.getChannel();
        } else {
            InputStream in = url.openConnection().getInputStream();
            channel = Channels.newChannel(in);
        }

        return channel;
    }

    //
    // public ShapefileReader.Record getNextRecord(ShapefileReader shpreader, Envelope bbox) throws
    // Exception {
    // if( treeType== IndexInfo.TREE_GRX || treeType== TREE_QIX){
    //
    // List goodRecs = null;
    // try {
    // goodRecs = queryTree(bbox);
    // } catch (TreeException e) {
    // throw new IOException("Error querying index: " +
    // e.getMessage());
    // }
    // }
    // ShapefileReader.Record record = shpreader
    // .nextRecord();
    // return record;
    // }
    private Collection queryTree( Envelope bbox ) throws IOException, TreeException {
        if (treeType == IndexInfo.R_TREE) {
            return queryRTree(bbox);
        } else if (treeType == IndexInfo.QUAD_TREE) {
            return queryQuadTree(bbox);
        }

        // should not happen
        return null;
    }

    static class Reader implements RecordNumberTracker {
        private ShapefileReader shp;
        Iterator goodRecs;
        private int recno = 1;
		private Data next;
		private IndexInfo info;

        public Reader( IndexInfo info, ShapefileReader reader, Envelope bbox ) throws IOException {
            shp = reader;

            try {
            	
                if (info.treeType == R_TREE) {
                    info.rtree = info.openRTree();
                } else if (info.treeType == QUAD_TREE) {
                    info.qtree = info.openQuadTree();
                }

                Collection queryTree = info.queryTree(bbox);
                if( queryTree!=null )
                	goodRecs = queryTree.iterator();
            } catch (Exception e) {
                ShapefileRenderer.LOGGER.log(Level.FINE,
                        "Exception occured attempting to use indexing:", e);
                goodRecs = null;
            }

            this.info=info;
        }

        public int getRecordNumber() {
            return this.recno;
        }
        public boolean hasNext() throws IOException {
            if (this.goodRecs != null) {
            	if( next!=null )
            		return true;
                if (this.goodRecs.hasNext()) {
                	
                    next=(Data)goodRecs.next();
                    this.recno = ((Integer) next.getValue(0)).intValue();
                    return true;
                }
                return false;
            }

            return shp.hasNext();
        }

        public ShapefileReader.Record next() throws IOException {
        	if( !hasNext() )
        		throw new IndexOutOfBoundsException("No more features in reader");
            if (this.goodRecs != null) {

                Long l = (Long) next.getValue(1);
                ShapefileReader.Record record = shp.recordAt(l.intValue());
                next=null;
                return record;
            }
            recno++;
            return shp.nextRecord();
        }

        public void close() throws IOException {
            shp.close();
            try {
            	if( info.qtree!=null ){
					info.qtree.close(goodRecs);
					info.qtree.close();
            	}
			} catch (StoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
}
