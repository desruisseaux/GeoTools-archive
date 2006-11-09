package org.geotools.tile;

interface TileCache {
    /**
     * Create a TileRange capturing the provided range.
     * <p>
     * The TileRange will be returned immidiately, the TileCache will schedule calling the provided TileDraw
     * class when TileRange.load( monitor ) is called.
     * <p>
     * The definition of row/cols is defined according the provided TileDraw stratagy object.
     * 
     * @param draw
     * @param row
     * @param col
     * @param rows
     * @param cols
     * @return
     */
    TileRange createRange( TileDraw draw, int row, int col, int rows, int cols );
    
}