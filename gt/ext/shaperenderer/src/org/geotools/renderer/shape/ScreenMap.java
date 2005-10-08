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
/**
 *
 */
package org.geotools.renderer.shape;

public class ScreenMap {
    int[] pixels;
    int width;
    int height;

    public ScreenMap(int x, int y) {
        width = x;
        height = y;

        int arraySize = ((x * y) / 32) + 1;
        pixels = new int[arraySize];
    }

    /**
     * Sets location at position x,y to the value.
     */
    public void set(int x, int y, boolean value) {
        if( x<0 || x>width-1 || y<0 || y>height-1 )
            return;
        int bit = bit(x, y);
        int index = bit / 32;
        int offset = bit % 32;
        int mask = 1;
        mask = mask << offset;

        if (value) {
            pixels[index] = pixels[index] | mask;
        } else {
            int tmp = pixels[index];
            tmp = ~tmp;
            tmp = (tmp | mask);
            tmp = ~tmp;
            pixels[index] = tmp;
        }
    }

    /**
     * Returns true if the pixel at location x,y is set or out of bounds.
     */
    public boolean get(int x, int y) {
        if( x<0 || x>width-1 || y<0 || y>height-1 )
            return true;
        int bit = bit(x, y);
        int index = bit / 32;
        int offset = bit % 32;
        int mask = 1 << offset;

        try {
            return ((pixels[index] & mask) != 0) ? true : false;
        } catch (Exception e) {
            System.out.println("" + x + "," + y); //$NON-NLS-1$ //$NON-NLS-2$

            return true;
        }
    }

    private int bit(int x, int y) {
        return (height * y) + x;
    }
}
