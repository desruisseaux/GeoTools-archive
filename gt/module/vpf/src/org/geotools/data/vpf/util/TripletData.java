/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004, Geotools Project Managment Committee (PMC)
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

package org.geotools.data.vpf.util;


/*
 * TripletData.java
 *
 * Created on 5. juli 2004, 12:51
 *
 * @author  <a href="mailto:knuterik@onemap.org">Knut-Erik Johnsen</a>, Project OneMap
 */
public class TripletData {
    private int current_row_id = -1;
    private int next_tile_id = -1;
    private int next_row_id = -1;

    /** Creates a new instance of TripletData */
    public TripletData() {
    }

    public TripletData(int next_row) {
        setNext_row_id(next_row);
    }

    public TripletData(int next_tile, int next_row) {
        setNext_tile_id(next_tile);
        setNext_row_id(next_row);
    }

    public TripletData(int current_row, int next_tile, int next_row) {
        setCurrent_row_id(current_row);
        setNext_tile_id(next_tile);
        setNext_row_id(next_row);
    }

    /**
     * Getter for property current_row_id.
     * @return Value of property current_row_id.
     */
    public int getCurrent_row_id() {
        return current_row_id;
    }

    /**
     * Setter for property current_row_id.
     * @param current_row_id New value of property current_row_id.
     */
    public void setCurrent_row_id(int current_row_id) {
        this.current_row_id = current_row_id;
    }

    /**
     * Getter for property next_row_id.
     * @return Value of property next_row_id.
     */
    public int getNext_row_id() {
        return next_row_id;
    }

    /**
     * Setter for property next_row_id.
     * @param next_row_id New value of property next_row_id.
     */
    public void setNext_row_id(int next_row_id) {
        this.next_row_id = next_row_id;
    }

    /**
     * Getter for property next_tile_id.
     * @return Value of property next_tile_id.
     */
    public int getNext_tile_id() {
        return next_tile_id;
    }

    /**
     * Setter for property next_tile_id.
     * @param next_tile_id New value of property next_tile_id.
     */
    public void setNext_tile_id(int next_tile_id) {
        this.next_tile_id = next_tile_id;
    }

    public String toString() {
        return current_row_id + ":" + next_row_id + ":" + next_tile_id;
    }
}