/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.event;

/**
 * Indicates a constant immutable data object, may be shared (and will
 * not issue change notifications).
 * 
 * @author Jody Garnett, Refractions Research, Inc.
 *
 * @source $URL$
 */
public class GTConstant implements GTComponent {	
	public static final GTRoot SHARED = GTRoot.NO_PARENT;
	public Object clone() {
		return this; // we are constant
	}
	public GTComponent getParent() {
		return getNote().getParent();
	}

	public void removed(GTDelta delta) {
		throw new IllegalStateException("A child has changed in an immutable Default!");
	}

	public void changed(GTDelta delta) {
		throw new IllegalStateException("A child has changed in an immutable Default!");
	}
	public GTNote getNote() {
		return GTNote.EMPTY;
	}
	public void setNote(GTNote container) {
		// TODO Auto-generated method stub		
	}
}
