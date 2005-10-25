package org.geotools.brewer.color;


/**
 *
 * @author James
 */
public class SampleScheme {
    int[][] sampleScheme = new int[9][];
    /** Creates a new instance of SampleScheme */
    public SampleScheme() {
    }

    /**
     * Indexed getter for property sampleScheme.
     * @param index Index of the property.
     * @return Value of the property at <CODE>index</CODE>.
     */
    public int[] getSampleScheme(int length) {

        return this.sampleScheme[length-2];
    }

    /**
     * Indexed setter for property sampleScheme.
     * @param index Index of the property.
     * @param sampleScheme New value of the property at <CODE>index</CODE>.
     */
    public void setSampleScheme(int length, int[] sampleScheme) {

        this.sampleScheme[length-2] = sampleScheme;
    }
    
}
