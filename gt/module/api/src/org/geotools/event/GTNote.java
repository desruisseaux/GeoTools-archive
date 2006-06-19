/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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

public interface GTNote {
    public static GTNote EMPTY = new GTNote() {
        public GTComponent getParent() {
            throw new IllegalStateException("Invalid root");
        }

        public void setParent(GTComponent newParent) {
            throw new IllegalStateException("Invalid GTNote (you need to create a new instance)");
        }

        public void setNotificationName(String name) {
            throw new IllegalStateException("Invalid GTNote (you need to create a new instance)");
        }

        public String getNotificationName() {
            return "";
        }

        public void setNotificationPosition(int index) {
            throw new IllegalStateException("Invalid GTNote (you need to create a new instance)");
        }

        public int getNotificationPosition() {
            return GTDelta.NO_INDEX;
        }
        public String toString() {
        	return "NO_PARENT";
        }
    };

	/**
	 * Used to locate our parent.
	 * <p>
	 * This method will return a "NULLObject", called GTRoot.NO_PARENT when
	 * no parent is present, client code should never have to be concerned
	 * this method return <code>null</code>.
	 * 
	 * @return Parent GTComponent or GTRoot.NO_PARENT if none
	 */
	GTComponent getParent();

	/**
	 * Used to set the parent, and associated placement information.
	 * 
	 * @param newParent GTComponent or NULLGTRoot if none
	 */
	void setParent(GTComponent newParent );		
	
	/** Indicate name used during notification */
	public void setNotificationName( String name );
	
	/** Indicate name used during notification */	
	public String getNotificationName();
	
	/** Indicate name position used during notification */	
	public void setNotificationPosition( int index );
	
	/** Indicate position used during notification */	
	public int getNotificationPosition();
	
}
