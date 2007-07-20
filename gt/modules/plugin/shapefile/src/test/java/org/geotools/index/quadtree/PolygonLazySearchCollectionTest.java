package org.geotools.index.quadtree;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Iterator;

import org.geotools.data.shapefile.indexed.IndexedShapefileDataStore;
import org.geotools.data.shapefile.indexed.TestCaseSupport;
import org.geotools.data.shapefile.shp.IndexFile;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.index.quadtree.fs.FileSystemIndexStore;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * @author Jesse
 */
public class PolygonLazySearchCollectionTest extends TestCaseSupport {

	private File file;
	private IndexedShapefileDataStore ds;
	private QuadTree tree;
	private Iterator iterator;
	private CoordinateReferenceSystem crs;

	public PolygonLazySearchCollectionTest() throws IOException {
		super("LazySearchIteratorTest");
	}

	protected void setUp() throws Exception {
		super.setUp();
		file=copyShapefiles("shapes/statepop.shp");
		ds=new IndexedShapefileDataStore(file.toURL());
		ds.buildQuadTree(0);
		tree = openQuadTree();
		crs=ds.getSchema().getPrimaryGeometry().getCoordinateSystem();
	}
	
	private QuadTree openQuadTree() throws StoreException{
		FileSystemIndexStore store = new FileSystemIndexStore(sibling(file, "qix"));
		try {

            FileInputStream in = new FileInputStream(sibling(file, "shx"));
            FileChannel channel = in.getChannel();
			return store.load(new IndexFile(channel));
		} catch (IOException e) {
			throw new StoreException(e);
		}
	}
	
	protected void tearDown() throws Exception {
		if (iterator!=null )
			tree.close(iterator);
		tree.close();
		super.tearDown();
		file.getParentFile().delete();
	}
	
	public void testGetAllFeatures() throws Exception {
		ReferencedEnvelope env = new ReferencedEnvelope(-125.5,-66,23.6,53.0, crs);
		LazySearchCollection collection = new LazySearchCollection(tree, env);
		assertEquals(49, collection.size());
	}
	
	public void testGetOneFeatures() throws Exception {
		ReferencedEnvelope env = new ReferencedEnvelope(-70,-68.2,44.5,45.7, crs);
		LazySearchCollection collection = new LazySearchCollection(tree, env);
		assertEquals(10, collection.size());
		
	}
	
	public void testGetNoFeatures() throws Exception {
		ReferencedEnvelope env = new ReferencedEnvelope(0,10,0,10, crs);
		LazySearchCollection collection = new LazySearchCollection(tree, env);
		assertEquals(0, collection.size());		
	}
}
