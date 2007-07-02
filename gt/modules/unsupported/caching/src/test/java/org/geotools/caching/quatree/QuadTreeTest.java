package org.geotools.caching.quatree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.caching.Generator;
import org.geotools.caching.spatialindex.spatialindex.IData;
import org.geotools.caching.spatialindex.spatialindex.INode;
import org.geotools.caching.spatialindex.spatialindex.IVisitor;
import org.geotools.caching.spatialindex.spatialindex.Region;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;

import com.vividsolutions.jts.geom.Envelope;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class QuadTreeTest extends TestCase {
	
	protected FeatureType type ;
    protected List data;
    protected QuadTree tree ;

    protected List createDataSet(int numberOfData) {
        //System.out.println("=== Creating Data Set");
        Generator gen = new Generator(1000, 1000);
        type = gen.getFeatureType();

        List ret = new ArrayList();

        for (int i = 0; i < numberOfData; i++) {
            ret.add(gen.createFeature(i));
        }

        return ret;
    }
	
	protected void setUp() {
		data = createDataSet(2000) ;
		tree = new QuadTree(new Region(new double[] {0d, 0d}, new double[] {1000d, 1000d})) ;
	}
	
    public static Test suite() {
        return new TestSuite(QuadTreeTest.class);
    }

    public void testInsertData() {
    	for (Iterator it = data.iterator() ; it.hasNext(); ) {
    		Feature f = (Feature) it.next() ;
    		tree.insertData(f.getID().getBytes(), toRegion(f.getBounds()), f.hashCode()) ;
    	}
    }
    
    public void testCountQuery() {
    	testInsertData() ;
    	CountingVisitor v1 = new CountingVisitor() ;
    	tree.intersectionQuery(
    			new Region(new double[] {0d, 0d}, new double[] {1000d, 1000d}),
    			v1) ;
    	//System.out.println("Nodes = " + v1.nodes + " ; Data = " + v1.data) ;
    	// some data overlap in the tree, so we may count more than actual
    	assertTrue(v1.data >= 2000) ;
    	CountingVisitor v2 = new CountingVisitor() ;
    	tree.intersectionQuery(
    			new Region(new double[] {0d, 0d}, new double[] {1000d, 1000d}),
    			v2) ;
    	//System.out.println("Nodes = " + v2.nodes + " ; Data = " + v2.data) ;
    	assertEquals(v1.data, v2.data) ;
    	assertEquals(v2.nodes, v2.nodes) ;
    }
    
    public void testIntersectionQuery() {
    	testInsertData() ;
    	YieldingVisitor v = new YieldingVisitor() ;
    	long start = System.currentTimeMillis() ;
    	tree.intersectionQuery(
    			new Region(new double[] {0d, 0d}, new double[] {1000d, 1000d}), v) ;
    	long q1 = System.currentTimeMillis()-start ;
    	assertEquals(2000, v.yields.size()) ;
    	v = new YieldingVisitor() ;
    	start = System.currentTimeMillis() ;
    	tree.intersectionQuery(
    			new Region(new double[] {250d, 250d}, new double[] {500d, 500d}), v) ;
    	long q2 = System.currentTimeMillis()-start ;
    	assertTrue(v.yields.size() < 2000) ;
    	/* Runtime context may cause this to fail ...
    	   but this is what we expect of an index */
    	// assertTrue(q2 < q1) ;
    	if (q2 >= q1) {
    		Logger.getLogger("org.geotools.caching.quadtree").log(Level.SEVERE, "Index not fast as expected.") ;
    	}
    }
    
    public void testContainementQuery() {
    	Region r = new Region(new double[] {10, 15}, new double[] {15,20}) ;
    	tree.insertData(null, r, 0) ;
    	NodeEnvelopeVisitor v = new NodeEnvelopeVisitor() ;
    	tree.containmentQuery(r, v) ;
    	assertTrue(v.lastNode.getShape().contains(r)) ;
    }
    
    /** Transform a JTS Envelope to a Region
    *
    * @param e JTS Envelope
    * @return
    */
   protected static Region toRegion(final Envelope e) {
       Region r = new Region(new double[] { e.getMinX(), e.getMinY() },
               new double[] { e.getMaxX(), e.getMaxY() });

       return r;
   }
   
   class CountingVisitor implements IVisitor {

	   int data = 0 ;
	   int nodes = 0 ;

	   public void visitData(IData d) {
		   data++ ;
		   //System.out.println(new String(d.getData())) ;
	   }

	   public void visitNode(INode n) {
		   nodes++ ;
	   }
	   
   }
   
   class YieldingVisitor implements IVisitor {
	   
	   HashMap yields = new HashMap() ;

	   public void visitData(IData d) {
		   yields.put(new String(d.getData()), null) ;
	   }

	   public void visitNode(INode n) {
		   // do nothing
	   }
   
   }
   
   class NodeEnvelopeVisitor implements IVisitor {
	   
	   INode lastNode = null ;

	   public void visitData(IData d) {
		   // TODO Auto-generated method stub

	   }

	   public void visitNode(INode n) {
		   lastNode = n ;
	   }
	   
   }
   
}
