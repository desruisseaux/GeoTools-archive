package org.geotools.event;

/**
 * Indicates a constant immutable data object, may be shared (and will
 * not issue change notifications).
 * 
 * @author Jody Garnett, Refractions Research, Inc.
 *
 */
public class GTConstant implements GTComponent {	
	public static final GTRoot SHARED = GTRoot.NO_PARENT;
	public Object clone() {
		return this; // we are constant
	}
	public GTComponent getParent() {
		return SHARED;
	}
	public void setParent(GTComponent newParent) {
	}
	public void setNotificationName(String name) {
	}
	public String getNotificationName() {
		return "";
	}
	public void setNotificationPosition(int index) {
	}
	public int getNotificationPosition() {
		return GTDelta.NO_INDEX;
	}
	public void removed(GTDelta delta) {
		throw new IllegalStateException("A child has changed in an immutable Default!");
	}

	public void changed(GTDelta delta) {
		throw new IllegalStateException("A child has changed in an immutable Default!");
	}
}
