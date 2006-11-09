package org.geotools.tile;

/**
 * Describes the scale of a TileSet, with derrivative information.
 * 
 * @author Jody Garnett
 */
public class ZoomLevel {
    private int scaleDenominator;
    private int rows;
    private int cols;
    
    public ZoomLevel( int scaleDeonmoniator, int rows, int cols ){
        this.scaleDenominator = scaleDeonmoniator;
        this.rows = rows;
        this.cols = cols;
    }
    /**
     * Tile scale is 1/ScaleDenominator
     * 
     * @return
     */
    int getScaleDenominator(){
        return scaleDenominator;
    }    
    /** Number of rows at this scale */
    int getNumberOfRows(){
        return rows;
    }
    /** Number of cols at this scale */
    int getNumberOfColumns(){
        return cols;
    }
}