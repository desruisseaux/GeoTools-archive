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
