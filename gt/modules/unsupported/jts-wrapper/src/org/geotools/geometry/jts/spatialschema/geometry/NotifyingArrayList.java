package org.geotools.geometry.jts.spatialschema.geometry;

import org.geotools.geometry.jts.JTSGeometry;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Helper class that notifies the containing geometry when the list has changed
 * so that it can invalidate any cached JTS objects it had.
 */
public class NotifyingArrayList extends ArrayList {
    private JTSGeometry parent;

    public NotifyingArrayList(JTSGeometry parent) {
        this.parent = parent;
    }

    public void add(int index, Object element) {
        super.add(index, element);
        if (parent != null) parent.invalidateCachedJTSPeer();
    }
    public boolean add(Object o) {
        boolean result = super.add(o);
        if (parent != null) parent.invalidateCachedJTSPeer();
        return result;
    }
    public boolean addAll(Collection c) {
        boolean result = super.addAll(c);
        if (parent != null) parent.invalidateCachedJTSPeer();
        return result;
    }
    public boolean addAll(int index, Collection c) {
        boolean result = super.addAll(index, c);
        if (parent != null) parent.invalidateCachedJTSPeer();
        return result;
    }
    public void clear() {
        super.clear();
        if (parent != null) parent.invalidateCachedJTSPeer();
    }
    public Object remove(int index) {
        Object result = super.remove(index);
        if (parent != null) parent.invalidateCachedJTSPeer();
        return result;
    }
    public Object set(int index, Object element) {
        Object result = super.set(index, element);
        if (parent != null) parent.invalidateCachedJTSPeer();
        return result;
    }
}