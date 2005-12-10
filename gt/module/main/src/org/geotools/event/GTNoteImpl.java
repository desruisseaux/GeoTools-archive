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

public class GTNoteImpl implements GTNote {
    GTComponent notificationParent = GTRoot.NO_PARENT;
    protected String notificationName = "";
    protected int notificationPosition = GTDelta.NO_INDEX;

    public GTNoteImpl() {
    }

    public GTNoteImpl(String notificationName, int notificationPosition) {
        this.notificationName = notificationName;
        this.notificationPosition = notificationPosition;
    }

    public GTNoteImpl(GTComponent parent, String notificationName,
        int notificationPosition) {
        this.notificationParent = parent;
        this.notificationName = notificationName;
        this.notificationPosition = notificationPosition;
    }

    public GTComponent getParent() {
        return notificationParent;
    }

    public void setParent(GTComponent newParent) {
        if (newParent == null) {
            newParent = GTRoot.NO_PARENT;
        }

        if (notificationParent != GTRoot.NO_PARENT) {
            throw new IllegalStateException(
                "Please remove from existing parent first");
        }

        notificationParent = newParent;
    }

    public void setNotificationName(String name) {
        notificationName = name;
    }

    public String getNotificationName() {
        return notificationName;
    }

    public void setNotificationPosition(int index) {
        notificationPosition = index;
    }

    public int getNotificationPosition() {
        return notificationPosition;
    }
}
