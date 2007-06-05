package org.geotools.caching;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.geotools.caching.Generator;
import org.geotools.caching.HashMapInternalStore;
import org.geotools.caching.IInternalStore;
import org.geotools.caching.SimpleHashMapInternalStore;
import org.geotools.feature.Feature;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class InternalStoreTest extends TestCase {
	
	public static Test suite() {
		return new TestSuite(InternalStoreTest.class) ;
	}
	
	public void testSimpleHashMapInternalStore() {
		SimpleHashMapInternalStore simple = new SimpleHashMapInternalStore() ;
		testStore(simple) ;
	}
	
	public void testHashMapInternalStore() {
		SimpleHashMapInternalStore simple = new SimpleHashMapInternalStore() ;
		HashMapInternalStore hash = new HashMapInternalStore(100, simple) ;
		testStore(hash) ;
	}
	
	protected void testStore(IInternalStore tested) {
		Random rand = new Random() ;
		List features = new ArrayList() ;
		Generator gen = new Generator(1000, 1000) ;
		for (int i = 0; i < 1000; i++) {
			Feature f = gen.createFeature(i) ;
			features.add(f) ;
			tested.put(f) ;
		}
		for (int i = 0; i < 10000; i++) {
			int e = rand.nextInt(1000) ;
			Feature f = (Feature) features.get(e) ;
			Feature retrieved = tested.get(f.getID()) ;
			assertTrue(f.equals(retrieved)) ;
		}
	}

}
