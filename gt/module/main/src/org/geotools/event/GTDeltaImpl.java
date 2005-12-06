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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.geotools.event.GTDelta;
import org.geotools.event.GTDeltaVisitor;

/**
 * Describes the extent of changes.
 * 
 * <p>
 * The "delta" acts as a series of bread crumbs allowing you the listener to
 * figure out what changed where. 
 *
 * @since 2.2.M3
 */
public class GTDeltaImpl implements GTDelta {
    private Kind kind;
    private Object affected;
    private List children;

    public GTDeltaImpl(Object victim) {
        this(Kind.CHANGED, victim);
    }

    public GTDeltaImpl(Kind kind, Object affected) {
        this(kind, affected, Collections.EMPTY_LIST);
    }

    public GTDeltaImpl(Object affected, GTDelta delta) {
        this(Kind.NO_CHANGE, affected, Collections.EMPTY_LIST);
    }

    public GTDeltaImpl(Kind kind, Object affected, GTDelta delta) {
        this(kind, affected, Collections.singletonList(delta));
    }

    public GTDeltaImpl(Kind kind, Object affected, List children) {
        this.kind = kind;
        this.affected = affected;
        this.children = children;
    }

    public Kind getKind() {
        return kind;
    }

    public Object getAffected() {
        return affected;
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
