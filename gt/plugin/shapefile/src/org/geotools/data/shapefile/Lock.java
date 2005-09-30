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
package org.geotools.data.shapefile;

import java.util.*;


class RWNode {
    static final int READER = 0;
    static final int WRITER = 1;
    Thread t;
    int state;
    int nAcquires;

    RWNode(Thread t, int state) {
        this.t = t;
        this.state = state;
        nAcquires = 0;
    }
}


public class Lock {
    private Vector waiters;

    public Lock() {
        waiters = new Vector();
    }

    private int firstWriter() {
        Enumeration e;
        int index;

        for (index = 0, e = waiters.elements(); e.hasMoreElements(); index++) {
            RWNode node = (RWNode) e.nextElement();

            if (node.state == RWNode.WRITER) {
                return index;
            }
        }

        return Integer.MAX_VALUE;
    }

    private int getIndex(Thread t) {
        Enumeration e;
        int index;

        for (index = 0, e = waiters.elements(); e.hasMoreElements(); index++) {
            RWNode node = (RWNode) e.nextElement();

            if (node.t == t) {
                return index;
            }
        }

        return -1;
    }

    public synchronized void lockRead() {
        RWNode node;
        Thread me = Thread.currentThread();
        int index = getIndex(me);

        if (index == -1) {
            node = new RWNode(me, RWNode.READER);
            waiters.addElement(node);
        } else {
            node = (RWNode) waiters.elementAt(index);
        }

        while (getIndex(me) > firstWriter()) {
            try {
                wait();
            } catch (Exception e) {
            }
        }

        node.nAcquires++;
    }

    public synchronized void lockWrite() {
        RWNode node;
        Thread me = Thread.currentThread();
        int index = getIndex(me);

        if (index == -1) {
            node = new RWNode(me, RWNode.WRITER);
            waiters.addElement(node);
        } else {
            node = (RWNode) waiters.elementAt(index);

            if (node.state == RWNode.READER) {
                throw new IllegalArgumentException("Upgrade lock");
            }

            node.state = RWNode.WRITER;
        }

        while (getIndex(me) != 0) {
            try {
                wait();
            } catch (Exception e) {
            }
        }

        node.nAcquires++;
    }

    public synchronized void unlock() {
        RWNode node;
        Thread me = Thread.currentThread();
        int index;
        index = getIndex(me);

        if (index > firstWriter()) {
            throw new IllegalArgumentException("Lock not held");
        }

        node = (RWNode) waiters.elementAt(index);
        node.nAcquires--;

        if (node.nAcquires == 0) {
            waiters.removeElementAt(index);
            notifyAll();
        }
    }
}
