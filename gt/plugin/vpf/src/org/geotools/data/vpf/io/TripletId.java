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

//import org.geotools.data.vpf.util.*;

import org.geotools.data.vpf.exc.VPFDataFormatException;

/**
 * Class TripletId.java is responsible for
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @author <a href="mailto:knuterik@onemap.org">Knut-Erik Johnsen</a>, Project OneMap
 * @version 1.0.0
 */
public class TripletId extends Number {
    /** 
     * The raw data that can be decomposed into as many as three separate numbers
     */
    private byte[] rawData = null;

    /**
     * Creates a new <code>TripletId</code> instance.
     *
     * @param data a <code>byte[]</code> value
     */
    public TripletId(byte[] data) {
        rawData = data;
    }

    /**
     * Describe <code>toString</code> method here.
     *
     * @return a <code>String</code> value
     */
    public String toString() {
        String result = new String();
        try {
            if(getIdLength() > 0) {
                result = new Integer(getId()).toString();
            }
            if(getTileIdLength() > 0) {
                result = result.concat("%").concat(new Integer(getTileId()).toString()).trim();
            }
            if(getNextIdLength() > 0) {
                result = result.concat("%").concat(new Integer(getNextId()).toString()).trim();
            }
        } catch (RuntimeException exp) {
            throw new VPFDataFormatException("This triplet is invalid.", exp);
        }
        return result;
    }

    private int getIdLength() {
        return (rawData[0] >> 6) & 3;
    }

    private int getTileIdLength() {
        return (rawData[0] >> 4) & 3;
    }

    private int getNextIdLength() {
        return (rawData[0] >> 2) & 3;
    }

    /**
     * @return Returns the ID, the first number of the triplet 
     */
    public int getId() {
        int result = 0;
        int length = getIdLength();
        int piece;

        if (length > 0) {
            try {
                for (int inx = 0; inx < length; inx++) {
                	piece = rawData[inx + 1];
                	// Convert bytes from signed to unsigned
                	if(piece < 0) {
                		piece += -2 * (Byte.MIN_VALUE);
                	}
                    result += piece << (8 * inx);
                }
            } catch (RuntimeException exp) {
                exp.printStackTrace();
                result = 0;
            }
        }

        return result;
    }
    /**
     * @return Returns the Tile ID, the second number of the triplet 
     */
    public int getTileId() {
        int result = 0;
        int length = getTileIdLength();
        int piece;

        if (length > 0) {
            int rowIdLength = getIdLength();

            try {
                for (int inx = 0; inx < length; inx++) {
                	piece = rawData[inx + rowIdLength + 1];
                	if(piece < 0) {
                		piece += 2 * Byte.MAX_VALUE;
                	}
                    result += piece << (8 * inx);
                }
            } catch (RuntimeException exp) {
                exp.printStackTrace();
                result = 0;
            }
        }

        return result;
    }
    /**
     * @return Returns the Next ID, the third number of the triplet 
     */
    public int getNextId() {
        int result = 0;
        int length = getTileIdLength();
        int piece;

        if (length > 0) {
            int prevLength = getIdLength() + getTileIdLength();

            try {
                for (int inx = 0; inx < length; inx++) {
                	piece = rawData[inx + prevLength + 1];
                	if(piece < 0) {
                		piece += 2 * Byte.MAX_VALUE;
                	}
                    result += piece << (8 * inx);
                }
            } catch (RuntimeException exp) {
                exp.printStackTrace();
                result = 0;
            }
        }

        return result;
    }

    /**
     * Describe <code>calculateDataSize</code> method here.
     *
     * @param definition a <code>byte</code> value
     *
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
	/* (non-Javadoc)
	 * @see java.lang.Number#doubleValue()
	 */
	public double doubleValue() {
		return new Integer(getId()).doubleValue();
	}
	/* (non-Javadoc)
	 * @see java.lang.Number#floatValue()
	 */
	public float floatValue() {
		return new Integer(getId()).floatValue();
	}
	/* (non-Javadoc)
	 * @see java.lang.Number#intValue()
	 */
	public int intValue() {
		return getId();
	}
	/* (non-Javadoc)
	 * @see java.lang.Number#longValue()
	 */
	public long longValue() {
		return new Integer(getId()).longValue();
	}
	/* (non-Javadoc)
	 * @see java.lang.Number#byteValue()
	 */
    public byte byteValue() {
        return new Integer(getId()).byteValue();
    }

	/* (non-Javadoc)
	 * @see java.lang.Number#shortValue()
	 */
    public short shortValue() {
        return new Integer(getId()).shortValue();
    }
}