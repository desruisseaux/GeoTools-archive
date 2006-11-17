package org.geotools.tile;

/**
 * Describes the scale of a TileSet, with derrivative information.
 * 
 * @author Jody Garnett
 */
public final class ZoomLevel implements Comparable {
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
    public int getScaleDenominator(){
        return scaleDenominator;
    }    
    /** Number of rows at this scale */
    public int getNumberOfRows(){
        return rows;
    }
    
    /** Number of cols at this scale */
    public int getNumberOfColumns(){
        return cols;
    }
    public double getRowRatio( int row ){
        return (double)row/(double)rows;
    }
    public double getColRatio( int col ){
        return (double)col/(double)cols;
    }
    public ZoomLevel zoom( int amount ){
        if( amount == 0 ) return this;
        else if( amount > 0 ){
            int zoom = amount+1;
            return new ZoomLevel( scaleDenominator*zoom, rows*zoom, cols*zoom);
        }
        else {
            int zoom = 1-amount;
            return new ZoomLevel( scaleDenominator/zoom, rows/zoom, cols/zoom);    
        }
    }
    public int compareTo( Object o ) {
        if( o == null || !(o instanceof ZoomLevel )){
            return 0;
        }
        ZoomLevel other = (ZoomLevel) o;
        if( scaleDenominator == other.scaleDenominator ) return 0;        
        return scaleDenominator < other.scaleDenominator ? -1 : 1;
    }
    public int hashCode() {
        return scaleDenominator | rows << 2 | cols << 4;
    }
    public boolean equals( Object o ) {
        if( o == null || !(o instanceof ZoomLevel )){
            return false;
        }
        ZoomLevel other = (ZoomLevel) o;
        return scaleDenominator == other.scaleDenominator &&
               rows == other.rows && cols == other.cols;
    }
    public String toString() {
        return "ZoomLevel["+cols+"x"+rows+" scale:"+scaleDenominator+"]";
    }
}