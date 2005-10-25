package org.geotools.brewer.color;


import java.io.IOException;


/**
 * Contains the suitability information for a single palette.
 *
 * @author Cory Horner, Refractions
 */
public class PaletteSuitability {
    /** Suitability = GOOD */
    public static final int GOOD = 3;

    /** Suitability = UNKNOWN */
    public static final int UNKNOWN = 2;

    /** Suitability = DOUBTFUL */
    public static final int DOUBTFUL = 1;

    /** Suitability = BAD */
    public static final int BAD = 0;

    /** ViewerType = Suitable for the colorblind? */
    public static final int COLORBLIND = 0;

    /** ViewerType = Suitable for photocopiers? */
    public static final int PHOTOCOPY = 1;

    /** ViewerType = Suitable for overhead projectors (lcd)? */
    public static final int PROJECTOR = 2;

    /** ViewerType = Suitable for LCD monitors? */
    public static final int LCD = 3;

    /** ViewerType = Suitable for CRT monitors? */
    public static final int CRT = 4;

    /** ViewerType = Suitable for colour printing? */
    public static final int PRINT = 5;

    /**
     * Contains the suitability data for this palette.  First index is the
     * number of colors - 2.  Second index is the viewer type. Values are the
     * suitability value.
     * 
     * <p>
     * Viewer Types: PaletteSuitability.COLORBLIND, PHOTOCOPY, PROJECTOR, LCD,
     * CRT, or PRINT
     * </p>
     * 
     * <p>
     * Suitability: PaletteSuitability.GOOD, UNKNOWN, DOUBTFUL, or BAD
     * </p>
     */
    private int[][] paletteSuitability = new int[11][6];

    /**
     * The maximum number of colors this palette can support (minimum is
     * assumed to be 2).
     */
    private int maxColors = 0;

    public PaletteSuitability() {
    }

    /**
     * Indexed getter for property paletteSuitability. For this palette, this
     * returns an array containing the integer values for all 6 suitabilities.
     *
     * @param numClasses The number of colors to determine the suitability for
     *
     * @return int array; index = PaletteSuitability.COLORBLIND, PHOTOCOPY,
     *         PROJECTOR, LCD, CRT, or PRINT; values =
     *         PaletteSuitability.GOOD, UNKNOWN, DOUBTFUL, or BAD.
     */
    public int[] getSuitability(int numClasses) {
        return paletteSuitability[numClasses - 2];
    }

    /**
     * Indexed getter for the property paletteSuitability.  For the selected
     * palette and viewerType, this returns the integer value of the
     *
     * @param numClasses number of colours in this palette
     * @param viewerType PaletteSuitability.COLORBLIND, PHOTOCOPY, PROJECTOR,
     *        LCD, CRT, or PRINT.
     *
     * @return PaletteSuitability.GOOD, UNKNOWN, DOUBTFUL, or BAD.
     */
    public int getSuitability(int numClasses, int viewerType) {
        return paletteSuitability[numClasses - 2][viewerType];
    }

    /**
     * 
     *
     * @param numClasses Index of the property.
     * @param suitability New value of the property at<CODE>index</CODE>.
     *
     * @throws IOException
     */
    public void setSuitability(int numClasses, String[] suitability)
        throws IOException {
        //update max number of classes
        if (numClasses > maxColors) {
            maxColors = numClasses;
        }

        //convert G,D,B,? --> int
        if (suitability.length == 6) {
            for (int i = 0; i < 6; i++) {
                if (suitability[i].equals("G")) {
                    paletteSuitability[numClasses - 2][i] = GOOD;
                } else if (suitability[i].equals("D")) {
                    paletteSuitability[numClasses - 2][i] = DOUBTFUL;
                } else if (suitability[i].equals("B")) {
                    paletteSuitability[numClasses - 2][i] = BAD;
                } else {
                    paletteSuitability[numClasses - 2][i] = UNKNOWN;
                }
            }
        } else {
            throw new IOException("wrong number of items in suitability list");
        }
    }

    public int getMaxColors() {
        return maxColors;
    }
}
