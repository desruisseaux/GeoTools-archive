/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.caching.grid;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import org.opengis.filter.Filter;
import org.geotools.caching.AbstractFeatureCache;
import org.geotools.caching.CacheOversizedException;
import org.geotools.caching.FeatureCacheException;
import org.geotools.caching.FeatureCollectingVisitor;
import org.geotools.caching.util.Generator;
import org.geotools.data.FeatureStore;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.spatial.BBOXImpl;


public class GridFeatureCacheTest extends TestCase {
    static FeatureCollection dataset;
    static int numdata = 100;
    static List filterset;
    static Filter unitsquarefilter;
    static Envelope unitsquare;
    static int numfilters = 10;

    static {
        Generator gen = new Generator(1, 1);
        dataset = new DefaultFeatureCollection("Test", Generator.type);

        for (int i = 0; i < numdata; i++) {
            Feature f = gen.createFeature(i);
            dataset.add(f);
        }

        filterset = new ArrayList(numfilters);

        for (int i = 0; i < numfilters; i++) {
            Coordinate point = Generator.pickRandomPoint(new Coordinate(0.5, 0.5), 0.5, 0.5);
            Filter f = Generator.createBboxFilter(point, 0.2, 0.2);
            filterset.add(f);
        }

        unitsquarefilter = Generator.createBboxFilter(new Coordinate(0.5, 0.5), 1, 1);
        unitsquare = AbstractFeatureCache.extractEnvelope((BBOXImpl) unitsquarefilter);
    }

    MemoryDataStore ds;
    GridFeatureCache cache;

    protected void setUp() {
        try {
            ds = new MemoryDataStore();
            ds.createSchema(dataset.getFeatureType());
            ds.addFeatures(dataset);
            cache = new GridFeatureCache((FeatureStore) ds.getFeatureSource(
                        dataset.getFeatureType().getTypeName()), 100, 1000);
        } catch (FeatureCacheException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void testExtractEnvelope() {
        BBOXImpl filter = (BBOXImpl) Generator.createBboxFilter(new Coordinate(0.1, 0.9), 0.2, 0.3);
        Envelope e = AbstractFeatureCache.extractEnvelope(filter);
        Envelope c = new Envelope(0, 0.2, 0.75, 1.05);
        assertEquals(c, e);
    }

    public void testConvert() {
    }

    public static Test suite() {
        return new TestSuite(GridFeatureCacheTest.class);
    }

    public void testRegister() {
        Filter f = (Filter) filterset.get(0);
        Envelope e = AbstractFeatureCache.extractEnvelope((BBOXImpl) f);
        List matches = cache.match(e);
        assertEquals(1, matches.size());
        assertTrue(((Envelope) matches.get(0)).contains(e));
        cache.register(e);
        matches = cache.match(e);
        assertTrue(matches.isEmpty());
        cache.remove(e);
        matches = cache.match(e);
        assertEquals(1, matches.size());
        assertTrue(((Envelope) matches.get(0)).contains(e));
    }

    public void testPut() throws CacheOversizedException {
        cache.put(dataset);

        FeatureCollectingVisitor v = new FeatureCollectingVisitor(dataset.getFeatureType());
        cache.tracker.intersectionQuery(AbstractFeatureCache.convert(unitsquare), v);
        assertEquals(dataset.size(), v.getCollection().size());
    }

    public void testPeek() throws CacheOversizedException {
        cache.put(dataset);

        FeatureCollection fc = cache.peek(unitsquare);
        assertEquals(dataset.size(), fc.size());
    }

    public void testGet() throws IOException {
        FeatureCollection fc = cache.get(unitsquare);
        assertEquals(dataset.size(), fc.size());

        List matches = cache.match(unitsquare);
        assertTrue(matches.isEmpty());
    }

    public void testOversized() {
        // TODO
    }

    public void testGetFeatures() throws IOException {
        FeatureCollection fc = cache.getFeatures(unitsquarefilter);
        assertEquals(dataset.size(), fc.size());

        List matches = cache.match(unitsquare);
        assertTrue(matches.isEmpty());
    }

    public void testClear() throws IOException {
        Envelope e = AbstractFeatureCache.extractEnvelope((BBOXImpl) filterset.get(0));
        cache.get(e);
        cache.clear();

        List matches = cache.match(e);
        assertEquals(1, matches.size());
        assertTrue(((Envelope) matches.get(0)).contains(e));
    }

    public void testGetBounds() throws IOException {
        Envelope env = cache.getBounds();
        assertEquals(dataset.getBounds(), env);
    }

    public void testEviction() {
        // TODO:
    }
}
