package org.geotools.tile.cache;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.tile.TileDraw;
import org.geotools.util.NullProgressListener;
import org.geotools.util.SimpleInternationalString;
import org.opengis.util.ProgressListener;

/**
 * Default that simply caches on TileRange.
 * <p>
 * </p>
 * @author Jody Garnett, Refractions Research Inc.
 */
public class SimpleTileCache implements TileCache {
    
    DirectTileRange cached = null;
    
    public TileRange createRange( TileDraw draw, Rectangle range ) {
        DirectTileRange tileRange = new DirectTileRange( draw, range );        
        if( tileRange.equals( cached )){
            return cached;            
        }
        else {
            cached = tileRange;
        }
        return tileRange;
    }

    public void flushTiles( TileDraw draw ) {
        cached = null;
    }

    public void retireTiles( TileDraw draw ) {
        cached = null;
    }
    
    public void close() {
        cached = null;
    }
    
    class DirectTileRange implements TileRange {
        private TileDraw draw;
        private Rectangle range;
        /**
         * List of GridCoverage2D defined by this TileRange.
         * <p>
         * List is used in order to provide the following mapping:
         * tiles.get( 
         * </p>
         */
        private List tiles;
        private boolean isLoaded;

        /**
         * Tile range will be created with "placeholders"; to retrive content
         * use the load method.
         * 
         * @param draw Stratagy object used to produce GridCoverages
         * @param range Range of tiles to produce
         */
        DirectTileRange( TileDraw draw, Rectangle range ){            
            this.draw = draw;
            this.range = range;
            this.isLoaded = false;
            this.tiles = createClearRange(draw, range);            
        }
        
        private List createClearRange( TileDraw draw, Rectangle range ) {
            List clear = new ArrayList( range.width*range.height);
            for( int row = (int)range.x; row<=range.getMaxX(); row++){
                for( int col = (int)range.y; row<=range.getMaxY(); row++){
                    GridCoverage2D placeholder = draw.drawPlaceholder( row, col );
                    clear.add( placeholder );
                }
            }
            return clear;
        }
        
        public Envelope2D getBounds() {
            Envelope2D bounds = null;
            for( Iterator i=tiles.iterator();i.hasNext();){
                GridCoverage2D tile = (GridCoverage2D) i.next();
                if( bounds == null){
                    bounds = new Envelope2D( tile.getEnvelope() );                    
                }
                else {
                    bounds.add( tile.getEnvelope2D() );
                }
            }
            return bounds;
        }
        public boolean equals( Object obj ) {
            if( obj == this ) return true;
            if( obj == null || !(obj instanceof DirectTileRange)){
                return false;
            }            
            DirectTileRange other = (DirectTileRange) obj;
            return this.draw == other.draw && this.range == other.range;
        }
        public int hashCode() {
            return draw.hashCode() | range.hashCode() << 3;
        }
        /**
         * Set up available GridCoverage2d.
         * <p>
         * An entry is provided for every tile, even if just a placeholder.
         */
        public Set getTiles() {
            HashSet set = new HashSet( tiles );
            return Collections.unmodifiableSet( set);
        }

        public boolean isLoaded() {
            return isLoaded;
        }
        /**
         * Load tiles; this will replace existing placeholders.
         */
        public void load( ProgressListener monitor ) {
            fetchTiles(monitor, "Loading " ); 
            this.isLoaded = true;          
        }

        /**
         * Refresh tiles; this will update existing contents.
         */
        public void refresh( ProgressListener monitor ) {            
            if( isLoaded ){
                fetchTiles(monitor, "Refresh" );
            }
            else {
                if( monitor == null ) monitor = new NullProgressListener();
                monitor.setTask( new SimpleInternationalString("Load already in progress"));
                monitor.isCanceled();
            }
        }

        private void fetchTiles( ProgressListener monitor, String message ) {
            if( monitor == null ) monitor = new NullProgressListener();
            
            int count =0;
            int total = range.width * range.height;
            for( int row = (int)range.x; row<=range.getMaxX(); row++){
                for( int col = (int)range.y; row<=range.getMaxY(); row++){
                    monitor.setTask( new SimpleInternationalString( message + " "+row+","+col));                    
                    GridCoverage2D tile = draw.drawTile(row, col );
                    tiles.set( count, tile );
                    count++;
                    monitor.progress( (float) count / (float) total );                    
                }
            }
            monitor.complete();
        }                
    }
}