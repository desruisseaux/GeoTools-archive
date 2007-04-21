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
public class LineLazySearchCollectionTest extends TestCaseSupport {

	private File file;
	private IndexedShapefileDataStore ds;
	private QuadTree tree;
	private Iterator iterator;
	private CoordinateReferenceSystem crs;

	public LineLazySearchCollectionTest() throws IOException {
		super("LazySearchIteratorTest");
	}

	protected void setUp() throws Exception {
		super.setUp();
		file=copyShapefiles("shapes/streams.shp");
		ds=new IndexedShapefileDataStore(file.toURL());
		ds.buildQuadTree(0);
		tree = openQuadTree();
		crs=ds.getSchema().getDefaultGeometry().getCoordinateSystem();
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
		ReferencedEnvelope env = new ReferencedEnvelope(585000,610000,4910000,4930000, crs);
		LazySearchCollection collection = new LazySearchCollection(tree, env);
		assertEquals(116, collection.size());
	}
	
	public void testGetOneFeatures() throws Exception {
		ReferencedEnvelope env = new ReferencedEnvelope(588993,589604,4927443,4927443, crs);
		LazySearchCollection collection = new LazySearchCollection(tree, env);
		assertEquals(14, collection.size());
		
	}
	
	public void testGetNoFeatures() throws Exception {
		ReferencedEnvelope env = new ReferencedEnvelope(592211, 597000, 4910947, 4913500, crs);
		LazySearchCollection collection = new LazySearchCollection(tree, env);
		assertEquals(0, collection.size());		
	}
}
