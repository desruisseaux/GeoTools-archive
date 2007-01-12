package org.geotools.data.postgis.table;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.geotools.data.postgis.PostGISContent;
import org.geotools.data.store.ContentState;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureList;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.visitor.FeatureVisitor;
import org.geotools.util.ProgressListener;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class ViewFeatureCollection implements FeatureCollection {

    public ViewFeatureCollection(PostGISContent content, ContentState state, Filter filter) {
    }

    public void accepts(FeatureVisitor visitor, ProgressListener progress)
            throws IOException {
    }

    public void addListener(CollectionListener listener)
            throws NullPointerException {
    }

    public void close(FeatureIterator close) {
    }

    public void close(Iterator close) {
    }

    public FeatureIterator features() {
        return null;
    }

    public FeatureType getFeatureType() {
        return null;
    }

    public FeatureType getSchema() {
        return null;
    }

    public void removeListener(CollectionListener listener)
            throws NullPointerException {
    }

    public FeatureList sort(SortBy order) {
        return null;
    }

    public FeatureCollection subCollection(Filter filter) {
        return null;
    }

    public Iterator iterator() {
        return null;
    }

    public void purge() {
    }

    public boolean add(Object arg0) {
        return false;
    }

    public boolean addAll(Collection arg0) {
        return false;
    }

    public void clear() {
    }

    public boolean contains(Object o) {
        return false;
    }

    public boolean containsAll(Collection arg0) {
        return false;
    }

    public boolean isEmpty() {
        return false;
    }

    public boolean remove(Object o) {
        return false;
    }

    public boolean removeAll(Collection arg0) {
        return false;
    }

    public boolean retainAll(Collection arg0) {
        return false;
    }

    public int size() {
        return 0;
    }

    public Object[] toArray() {
        return null;
    }

    public Object[] toArray(Object[] arg0) {
        return null;
    }

    public Object getAttribute(String xPath) {
        return null;
    }

    public Object getAttribute(int index) {
        return null;
    }

    public Object[] getAttributes(Object[] attributes) {
        return null;
    }

    public Envelope getBounds() {
        return null;
    }

    public Geometry getDefaultGeometry() {
        return null;
    }

    public String getID() {
        return null;
    }

    public int getNumberOfAttributes() {
        return 0;
    }

    public void setAttribute(int position, Object val)
            throws IllegalAttributeException, ArrayIndexOutOfBoundsException {
    }

    public void setAttribute(String xPath, Object attribute)
            throws IllegalAttributeException {
    }

    public void setDefaultGeometry(Geometry geometry)
            throws IllegalAttributeException {
    }

}
