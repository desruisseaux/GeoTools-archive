/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.event;

import org.geotools.event.GTDelta;
import org.geotools.event.GTDeltaVisitor;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * Describes the extent of changes.
 * 
 * <p>
 * The "delta" acts as a series of bread crumbs allowing you the listener to
 * figure out what changed where.
 * </p>
 *
 * @since 2.2.M3
 */
public class GTDeltaImpl implements GTDelta {
    private String name;
    private int position;
    private Kind kind;
    private Object value;
    private Object oldValue;
    private List children;

    /**
     * Does the right thing, aka magic.
     * 
     * <p>
     * This constructor is provided to make things easier, if your experience
     * is not magic please skip this and try the next constructor.
     * </p>
     * 
     * <p>
     * Magic:
     * 
     * <ul>
     * <li>
     * NO_CHANGE: before == null     after == null
     * </li>
     * <li>
     * NO_CHANGE:         before == after
     * </li>
     * <li>
     * REMOVED:   before == obj      after == null
     * </li>
     * <li>
     * ADDED:     before == null     after == obj
     * </li>
     * <li>
     * CHANGED:           before != after
     * </li>
     * </ul>
     * </p>
     *
     * @param notification
     * @param before
     * @param after
     */
    public GTDeltaImpl(GTNote notification, Object before, Object after) {
        this(notification, magic(before, after), before, after);
    }

    /**
     * Create a delta, with no children.
     *
     * @param notification
     * @param kind
     * @param value
     * @param oldValue
     */
    public GTDeltaImpl(GTNote notification, Kind kind, Object value,
        Object oldValue) {
        this(notification, kind, value, oldValue, Collections.EMPTY_LIST);

        //this(name, NO_INDEX, kind, value, oldValue, Collections.EMPTY_LIST);
    }

    /**
     * Create a delta with with a list element child delta.
     * 
     * <p>
     * This can be used to communicate a batch of changes, such as adding and
     * removing sections from a list, or the results of a transformation.
     * </p>
     * 
     * <p></p>
     *
     * @param notification
     * @param kind
     * @param value
     * @param oldValue
     * @param delta
     */
    public GTDeltaImpl(GTNote notification, Kind kind, Object value,
        Object oldValue, GTDelta delta) {
        this(notification, kind, value, oldValue,
            Collections.singletonList(delta));
    }

    /**
     * Create a delta, completely specifying all values.
     *
     * @param notification
     * @param kind
     * @param value
     * @param oldValue
     * @param children
     */
    public GTDeltaImpl(GTNote notification, Kind kind, Object value,
        Object oldValue, List children) {
        this.name = notification.getNotificationName();
        this.position = notification.getNotificationPosition();
        this.kind = kind;
        this.value = value;
        this.oldValue = oldValue;
        this.children = children;
    }

    private static Kind magic(Object before, Object after) {
        if ((before == null) && (after == null)) {
            return Kind.NO_CHANGE;
        }

        if (before == after) {
            return Kind.NO_CHANGE;
        }

        if ((before != null) && (after == null)) {
            return Kind.REMOVED;
        }

        if ((before == null) && (after != null)) {
            return Kind.ADDED;
        }

        if (before != after) {
            return Kind.CHANGED;
        }

        return Kind.CHANGED;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    public Kind getKind() {
        return kind;
    }

    public Object getValue() {
        return value;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public List getChildren() {
        return children;
    }

    public void accept(GTDeltaVisitor visitor) {
        boolean visitChildren = visitor.visit(this);

        if (!visitChildren) {
            return;
        }

        for (Iterator i = children.iterator(); i.hasNext();) {
            GTDelta delta = (GTDelta) i.next();
            delta.accept(visitor);
        }
    }
}
