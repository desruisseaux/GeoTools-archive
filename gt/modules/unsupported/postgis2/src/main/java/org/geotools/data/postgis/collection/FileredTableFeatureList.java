package org.geotools.data.postgis.collection;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

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

public class FileredTableFeatureList implements FeatureList {

    public FileredTableFeatureList(PostGISContent content, ContentState state, Filter filter, SortBy sort) {
    }

    public FeatureList subList(Filter filter) {
        return null;
    }

    public boolean add(Object arg0) {
        return false;
    }

    public void add(int arg0, Object arg1) {
    }

    public boolean addAll(Collection arg0) {
        return false;
    }

    public boolean addAll(int arg0, Collection arg1) {
        return false;
    }

    public void clear() {
    }

    public boolean contains(Object arg0) {
        return false;
    }

    public boolean containsAll(Collection arg0) {
        return false;
    }

    public Object get(int arg0) {
        return null;
    }

    public int indexOf(Object arg0) {
        return 0;
    }

    public boolean isEmpty() {
        return false;
    }

    public Iterator iterator() {
        return null;
    }

    public int lastIndexOf(Object arg0) {
        return 0;
    }

    public ListIterator listIterator() {
        return null;
    }

    public ListIterator listIterator(int arg0) {
        return null;
    }

    public boolean remove(Object arg0) {
        return false;
    }

    public Object remove(int arg0) {
        return null;
    }

    public boolean removeAll(Collection arg0) {
        return false;
    }

    public boolean retainAll(Collection arg0) {
        return false;
    }

    public Object set(int arg0, Object arg1) {
        return null;
    }

    public int size() {
        return 0;
    }

    public List subList(int arg0, int arg1) {
        return null;
    }

    public Object[] toArray() {
        return null;
    }

    public Object[] toArray(Object[] arg0) {
        return null;
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

    public void purge() {
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
