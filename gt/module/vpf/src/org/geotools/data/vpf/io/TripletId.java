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
package org.geotools.data.vpf.io;

import org.geotools.data.vpf.util.*;


/**
 * Class TripletId.java is responsible for
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @author <a href="mailto:knuterik@onemap.org">Knut-Erik Johnsen</a>, Project OneMap
 * @version 1.0.0
 */
public class TripletId {
    /**
     * Describe variable <code>rawData</code> here.
     *
     */
    private byte[] rawData = null;
    private int currentByte = 0;

    /**
     * Creates a new <code>TripletId</code> instance.
     *
     * @param data a <code>byte[]</code> value
     */
    public TripletId(byte[] data) {
        rawData = data;
    }

    public byte[] getData() {
        return rawData;
    }

    /**
     * Describe <code>toString</code> method here.
     *
     * @return a <code>String</code> value
     */
    public String toString() {
        return (rawData == null)
               ? "NULL" : new String(rawData, 1, rawData.length - 1);
    }

    public TripletData parseBytes() {
        currentByte = 1;

        int[] pieces = new int[3];
        byte definition = rawData[0];
        pieces[0] = (definition >> 2) & 3;
        pieces[1] = (definition >> 4) & 3;
        pieces[2] = (definition >> 6) & 3;

        int size = 0;

        for (int i = 0; i < pieces.length; i++) {
            switch (pieces[i]) {
            case 0:
                break;

            case 1:
                size++;

                break;

            case 2:
                size++;

                break;

            case 3:
                size++;

                break;
            }
        }

        switch (size) {
        case 1:
            return new TripletData(parseByte(2));

        case 2:
            return new TripletData(parseByte(2), parseByte(1));

        case 3:
            return new TripletData(parseByte(2), parseByte(1), parseByte(0));

        default:
            System.out.println("FUCK OFF");
        }

        return null;
    }

    private int parseByte(int index) {
        byte definition = rawData[0];
        int size = (definition >> ((index + 1) * 2)) & 3;
        int tmp = 0;
        int tmpByte;

        switch (size) {
        case 1:
            return fixByte(rawData[currentByte++]);

        case 2:
            tmp = fixByte(rawData[currentByte++]);

            return (fixByte(rawData[currentByte++]) * 256) + tmp;

        case 3:
            tmp = fixByte(rawData[currentByte++]);
            tmp += (fixByte(rawData[currentByte++]) * 256);
            tmp += (fixByte(rawData[currentByte++]) * 256 * 256);

            return (fixByte(rawData[currentByte++]) * 256 * 256 * 256) + tmp;
        }

        return -1;
    }

    private int fixByte(byte fixit) {
        int tmpInt = fixit;

        if (fixit < 0) {
            tmpInt = 256 + fixit;
        }

        return tmpInt;
    }

    /**
     * Describe <code>calculateDataSize</code> method here.
     *
     * @param definition a <code>byte</code> value
     * @return an <code>int</code> value
     */
    public static int calculateDataSize(byte definition) {
        int[] pieces = new int[3];
        pieces[0] = (definition >> 2) & 3;
        pieces[1] = (definition >> 4) & 3;
        pieces[2] = (definition >> 6) & 3;

        int size = 0;

        for (int i = 0; i < pieces.length; i++) {
            switch (pieces[i]) {
            case 0:
                break;

            case 1:
                size++;

                break;

            case 2:
                size += 2;

                break;

            case 3:
                size += 4;

                break;

            default:
                System.out.println("Tripled id size decoding error");
                System.out.println("tripled definition: " + definition);
                System.out.println("piece 0: " + pieces[0]);
                System.out.println("piece 1: " + pieces[1]);
                System.out.println("piece 2: " + pieces[2]);

                break;
            }
        }

        return size;
    }
}