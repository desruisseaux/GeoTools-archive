package org.geotools.renderer.shape;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Collections;
import java.util.List;

import org.geotools.data.DataSourceException;
import org.geotools.data.shapefile.shp.IndexFile;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.index.Data;
import org.geotools.index.DataDefinition;
import org.geotools.index.LockTimeoutException;
import org.geotools.index.TreeException;
import org.geotools.index.quadtree.QuadTree;
import org.geotools.index.quadtree.StoreException;
import org.geotools.index.quadtree.fs.FileSystemIndexStore;
import org.geotools.index.rtree.RTree;
import org.geotools.index.rtree.fs.FileSystemPageStore;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Encapsulates index information for a layer in the MapContext.
 * 
 *  The associated layer can be obtained by 
 * @author jones
 */
public class IndexInfo {

    static final byte TREE_NONE = 0;
    static final byte R_TREE = 1;
    static final byte QUAD_TREE = 2;
	final byte treeType;
	final URL treeURL;
	final URL shxURL;
	private IndexFile indexFile;
	private RTree rtree;
	private QuadTree qtree;
	
	public IndexInfo(byte treeType, URL treeURL, URL shxURL) {
		this.treeType=treeType;
		this.treeURL=treeURL;
		this.shxURL=shxURL;
		

	}
	/**
     * RTree query
     * @param bbox
     * @return
     * @throws DataSourceException
     * @throws IOException
     */
    List queryRTree(Envelope bbox) 
    throws DataSourceException, IOException 
    {
        List goodRecs = null;
        try {
            if (rtree != null && !bbox.contains(rtree.getBounds())) {
                goodRecs = rtree.search(bbox);
            }
        } catch (LockTimeoutException le) {
            throw new DataSourceException("Error querying RTree", le);
        } catch (TreeException re) {
            throw new DataSourceException("Error querying RTree", re);
        } finally {
            try { rtree.close(); } catch (Exception ee) {}
        }
        return goodRecs;
    }
    
    /**
     * QuadTree Query
     * @param bbox
     * @return
     * @throws DataSourceException
     * @throws IOException
     */
    List queryQuadTree(Envelope bbox) 
    throws DataSourceException, IOException, TreeException 
    {
        List tmp = null;
        List goodRecs = null;
        try {
            if (qtree != null && 
                !bbox.contains(qtree.getRoot().getBounds())) 
            {
                tmp = qtree.search(bbox);

                if (tmp.size() > 0) {
                    // WARNING: QuadTree records number begins from 0
                    
                    Collections.sort(tmp);
                    DataDefinition def = new DataDefinition("US-ASCII");
                    def.addField(Integer.class);
                    def.addField(Long.class);
                    
                    Data data = null;
                    Integer recno = null;
                    for (int i = 0; i < tmp.size(); i++) {
                        recno = (Integer)tmp.get(i);
                        data = new Data(def);
                        data.addValue(new Integer(recno.intValue() + 1));
                        data.addValue(
                            new Long(
                                 indexFile.getOffsetInBytes(recno.intValue())));
                    }
                }
            }
            
        } catch (StoreException le) {
            throw new DataSourceException("Error querying QuadTree",
                                          le);
        } finally {
            try { qtree.close(); } catch (Exception ee) {}
            try { indexFile.close(); } catch (Exception ee) {}
        }
        
        return goodRecs;
    }

    /**
     * Convenience method for opening an RTree index.
     *
     * @return A new RTree.
     *
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
     * @return A new QuadTree
     * @throws StoreException
     */
    QuadTree openQuadTree() throws StoreException {
        File file = new File(treeURL.getPath());
        FileSystemIndexStore store = new FileSystemIndexStore(file);
        return store.load();
    }
    
    /**
     * Convenience method for opening a ShapefileReader.
     * @return An IndexFile
     * @throws IOException
     */
    IndexFile openIndexFile() throws IOException {
        ReadableByteChannel rbc = getReadChannel(shxURL);
        if (rbc == null) {
            return null;
        }

        //return new IndexFile(rbc, this.useMemoryMappedBuffer);
        return new IndexFile(rbc, false);
    }

    /**
     * Obtain a ReadableByteChannel from the given URL. If the url protocol is
     * file, a FileChannel will be returned. Otherwise a generic channel will
     * be obtained from the urls input stream.
     *
     * @param url DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    private ReadableByteChannel getReadChannel(URL url)
        throws IOException {
        ReadableByteChannel channel = null;

        if (url.getProtocol().equals("file")) {
            File file = new File(url.getFile());

            if (!file.exists() || !file.canRead()) {
                throw new IOException(
                    "File either doesn't exist or is unreadable : " + file);
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
//	public ShapefileReader.Record getNextRecord(ShapefileReader shpreader, Envelope bbox) throws Exception {
//	if( treeType== IndexInfo.TREE_GRX ||  treeType== TREE_QIX){
//
//		List goodRecs = null;
//            try {
//                goodRecs = queryTree(bbox);
//            } catch (TreeException e) {
//                throw new IOException("Error querying index: " + 
//                                      e.getMessage());
//        }
//        	}
//	ShapefileReader.Record record = shpreader
//		.nextRecord();
//		return record;
//	}

	private List queryTree(Envelope bbox) throws IOException, TreeException {
        if (treeType == IndexInfo.R_TREE) {
            return queryRTree(bbox);
        } else if (treeType == IndexInfo.QUAD_TREE) {
            return queryQuadTree(bbox);
        }
        // should not happen
        return null;
	}

    
	static class Reader {
		private ShapefileReader shp;
		List goodRecs;
		private int cnt;

		public Reader(IndexInfo info, ShapefileReader reader, Envelope bbox) {
			shp=reader;
            try {
                if( info.treeType==R_TREE )
    				info.rtree=info.openRTree();
    			else if( info.treeType==QUAD_TREE)
    				info.qtree=info.openQuadTree();
                info.indexFile=info.openIndexFile();
                goodRecs = info.queryTree(bbox);
            } catch (Exception e) {
            	ShapefileRenderer.LOGGER.fine("Exception occured attempting to use indexing:"+e.toString());
            	goodRecs=null;
            }
            
		}
		
        public boolean hasNext() throws IOException {
            
            if (this.goodRecs != null) {
                return this.cnt < this.goodRecs.size(); 
            }
            
            return shp.hasNext();

        }

        public ShapefileReader.Record next() throws IOException {
            
            if (this.goodRecs != null) {
                Data data = (Data)this.goodRecs.get(this.cnt);
                
                
                Long l = (Long)data.getValue(1);
                ShapefileReader.Record record=shp.recordAt(l.intValue());
                
                this.cnt++;
                return record;
            }
            
            return shp.nextRecord();

        }

		public void close() throws IOException {
			shp.close();
		}
	}
}