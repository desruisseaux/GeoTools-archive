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
package org.geotools.caching;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import com.vividsolutions.jts.geom.Envelope;
import org.opengis.filter.Filter;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.filter.spatial.BBOXImpl;
import org.geotools.index.Data;
import org.geotools.index.DataDefinition;
import org.geotools.index.LockTimeoutException;
import org.geotools.index.TreeException;
import org.geotools.index.rtree.PageStore;
import org.geotools.index.rtree.RTree;
import org.geotools.index.rtree.memory.MemoryPageStore;


public class MemoryFeatureIndex implements FeatureIndex {
    private static final DataDefinition df = createDataDefinition();
    private RTree tree = createTree();
    private final InternalStore internalStore;
    private final long capacity;
    private long indexCount = 0;
    private final FeatureType type;
    private Query currentQuery = Query.ALL;

    public MemoryFeatureIndex(FeatureType type, long capacity) {
        this.internalStore = new SimpleHashMapInternalStore();
        this.capacity = capacity;
        this.type = type;
    }

    public void add(Feature f) {
        if (internalStore.contains(f)) {
            return;
        }

        Data d = new Data(df);

        try {
            d.addValue(f.getID());
            tree.insert(f.getBounds(), d);
            internalStore.put(f);
            indexCount++;
        } catch (TreeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (LockTimeoutException e) {
            e.printStackTrace();
        }
    }

    public void clear() {
        try {
            tree.close();
        } catch (TreeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        tree = createTree();
        internalStore.clear();
    }

    public void flush() {
        // TODO Auto-generated method stub
    }

    public Feature get(String featureID) {
        /*Filter f = FilterFactoryFinder.createFilterFactory().createFidFilter(featureID) ;
           try {
                   FeatureSource fs = internalStore.getView(new DefaultQuery(type.getTypeName(), f)) ;
                   FeatureCollection fc = fs.getFeatures() ;
                   if (fc.isEmpty())
                           // TODO throw appropriate exception, so we can handle the case
                           return null ;
                   FeatureIterator i = fc.features() ;
                   Feature ret = i.next() ;
                   fc.close(i) ;
                   return ret ;
           } catch (IOException e) {
                   // TODO Auto-generated catch block
                   e.printStackTrace();
           } catch (SchemaException e) {
                   // TODO Auto-generated catch block
                   e.printStackTrace();
           }
           return null ;*/
        Feature f = (Feature) internalStore.get(featureID);

        // TODO test if we get null, and do something ...
        return f;
    }

    public FeatureCollection getFeatures(Query q) {
        Filter f = q.getFilter();
        FeatureCollection fc = new DefaultFeatureCollection(null, type);
        boolean refine = (f instanceof BBOXImpl);

        for (Iterator i = getCandidates(q).iterator(); i.hasNext();) {
            Feature next = (Feature) i.next();

            if (refine || f.evaluate(next)) {
                fc.add(next);
            }
        }

        return fc;
    }

    private Collection getCandidates(Query q) {
        Filter f = q.getFilter();

        if (f instanceof BBOXImpl) {
            List candidates = new ArrayList();
            BBOXImpl bb = (BBOXImpl) f;
            Envelope env = new Envelope(bb.getMinX(), bb.getMaxX(), bb.getMinY(), bb.getMaxY());

            try {
                List results = tree.search(env);

                for (Iterator r = results.iterator(); r.hasNext();) {
                    Data d = (Data) r.next();
                    String fid = (String) d.getValue(0);
                    candidates.add(internalStore.get(fid));
                }

                return candidates;
            } catch (TreeException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (LockTimeoutException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return internalStore.getAll();
    }

    public void remove(String featureID) {
        Envelope env = ((Feature) internalStore.get(featureID)).getBounds();

        try {
            tree.delete(env);
        } catch (TreeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (LockTimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        internalStore.remove(featureID);
        indexCount--;
    }

    public FeatureSource getView(Query q) {
        return new IndexView(this, q);
    }

    private static RTree createTree() {
        try {
            PageStore ps = new MemoryPageStore(df, 8, 4, PageStore.SPLIT_QUADRATIC);
            RTree tree = new RTree(ps);

            return tree;
        } catch (TreeException e) {
            throw (RuntimeException) new RuntimeException().initCause(e);
        }
    }

    private static DataDefinition createDataDefinition() {
        DataDefinition df = new DataDefinition("US-ASCII");
        df.addField(256);

        return df;
    }

    public void addFeatureListener(FeatureListener arg0) {
        // TODO Auto-generated method stub
    }

    public Envelope getBounds() throws IOException {
        try {
            return tree.getBounds();
        } catch (TreeException e) {
            throw (IOException) new IOException().initCause(e);
        }
    }

    public Envelope getBounds(Query q) throws IOException {
        FeatureCollection fc = this.getFeatures(q);

        return fc.getBounds();
    }

    public int getCount(Query q) throws IOException {
        FeatureCollection fc = this.getFeatures(q);

        return fc.size();
    }

    public DataStore getDataStore() {
        // TODO Auto-generated method stub
        return null;
    }

    public FeatureCollection getFeatures() throws IOException {
        // TODO Auto-generated method stub
        return this.getFeatures(Query.ALL);
    }

    public FeatureCollection getFeatures(Filter arg0) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public FeatureType getSchema() {
        // TODO Auto-generated method stub
        return type;
    }

    public void removeFeatureListener(FeatureListener arg0) {
        // TODO Auto-generated method stub
    }
}
