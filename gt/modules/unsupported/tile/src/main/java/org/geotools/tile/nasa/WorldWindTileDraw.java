package org.geotools.tile.nasa;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.geometry.Envelope2D;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.tile.TileDraw;

public class WorldWindTileDraw extends TileDraw {
    int placeholder=0;
    int tile=0;
    
    static GridCoverageFactory factory = new GridCoverageFactory();
    
    public GridCoverage2D drawPlaceholder( int row, int col ) {
        placeholder++;
        
        Envelope2D rectangle = createRectangle( row, col );
        RenderedImage image=createImage();
        return factory.create( "Grid"+row+"x"+col, image, rectangle );            
    }
    RenderedImage createImage( int row, int col ){
        BufferedImage image = new BufferedImage( 90, 90, BufferedImage.TYPE_INT_ARGB );
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setBackground( col <= 2 ? Color.BLUE : Color.RED );
        
        int x = col % 2 == 0 ? 0 : 90;
        int startAngle = row == 1 ? ( x )
                                     : ( col % 2 == 0 ? 180 : 270 );
        int y = row == 1 ? 90 : 0;
        g.fillArc(x,y,180,180,startAngle,90);

        g.setColor( Color.BLACK );
        String message = row+","+col;
        g.drawString( message, 30, 45 );
        return image;
    }
    
    RenderedImage createImage(){
        BufferedImage image = new BufferedImage( 1, 1, BufferedImage.TYPE_INT_ARGB );
        return image;
    }
    
    Envelope2D createRectangle( int row, int col ){
        double x = (row * 90.0) - 90.0;
        double y = (row * 90.0) - 180.0;
        double w = 90.0;
        double h = 90.0;
        
        return new Envelope2D( DefaultGeographicCRS.WGS84,x, y, w, h );
    }

    public GridCoverage2D drawTile( int row, int col ) {
        tile++;
        
        Envelope2D rectangle = createRectangle( row, col );
        RenderedImage image=createImage( row, col );
        return factory.create( "Grid"+row+"x"+col, image, rectangle ); 
    }        
}
