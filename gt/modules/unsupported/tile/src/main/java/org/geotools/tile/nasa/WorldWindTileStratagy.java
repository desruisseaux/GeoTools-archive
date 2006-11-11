package org.geotools.tile.nasa;

import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.media.jai.JAI;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.tile.ZoomLevel;
import org.geotools.util.NullProgressListener;
import org.opengis.util.ProgressListener;

public class WorldWindTileStratagy {
    private URL server;

    public WorldWindTileStratagy( URI uri ) {
        try {
            server = uri.toURL();
        } catch (MalformedURLException e) {
        }
    }

    static URI uri( String uri ) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            return null;
        }
    }
    /**
     * Harded coded from example: <West> <Value>-168.67</Value> </West> <South> <Value>17.84</Value></South>
     * <East> <Value>-65.15</Value> </East> <North> <Value>71.55</Value> </North>
     * 
     * @return
     */
    Envelope2D getBounds() {
        return createBounds(-168.67, 17.84, -65.15, 71.55);
    }
    int getTileSize() {
        return 512;
    }
    /**
     * Harded coded from example: <LevelZeroTileSizeDegrees>0.8</LevelZeroTileSizeDegrees>
     * 
     * @return Set of ZoomLevel
     */
    SortedSet getLevels() {
        double level0angle = 0.8;
        Envelope2D bbox = getBounds();
        int rows = (int) (bbox.height / level0angle);
        int cols = (int) (bbox.width / level0angle);
        int scale = scaleDenominator(level0angle, getTileSize());

        ZoomLevel level0 = new ZoomLevel(scale, rows, cols);
        SortedSet set = new TreeSet();
        set.add(level0);
        set.add(level0.zoom(1));
        set.add(level0.zoom(2));
        set.add(level0.zoom(3));
        set.add(level0.zoom(4));
        return set;
    }

    /**
     * Calculate scale denominator.
     * 
     * @param angle Angle of world tile represents
     * @param tileSize Size of tile in pixels
     * @return
     */
    int scaleDenominator( double angle, int tileSize ) {
        return (int) (tileSize / angle);
    }

    static Envelope2D createBounds( double west, double south, double east, double north ) {
        double x = west;
        double y = south;
        double w = east - west;
        double h = north - south;
        return new Envelope2D(DefaultGeographicCRS.WGS84, x, y, w, h);
    }

    /** List of TileMaps */
    List getContents( ProgressListener monitor ) {
        if (monitor == null)
            monitor = new NullProgressListener();

        return null;
    }
    /** Position in the provided ZoomLevel */
    private DirectPosition2D location( ZoomLevel level, int row, int col ) {
        Envelope2D bbox = getBounds();
        double dx = bbox.width * level.getColRatio(col);
        double dy = bbox.height * level.getRowRatio(row);
        double x = bbox.x + dx;
        double y = bbox.y + dy;
        return new DirectPosition2D(DefaultGeographicCRS.WGS84, x, y);
    }

    public Envelope2D requestTileBounds( ZoomLevel level, int row, int col ) {
        DirectPosition2D location1 = location(level, row, col);
        DirectPosition2D location2 = location(level, row + 1, col + 1);
        return new Envelope2D(location1, location2);
    }

    /**
     * Request an individual tile.
     * 
     * <ImageFileExtension>jpg</ImageFileExtension>
     * <ImageTileService>
     *    <ServerUrl>http://worldwind25.arc.nasa.gov/tile/tile.aspx</ServerUrl>
     *    <DataSetName>102</DataSetName>
     *    <ServerLogoFilePath>Data\Icons\Interface\\usgs.png</ServerLogoFilePath>
     * </ImageTileService>
     */
    public RenderedImage requestTileImage( ZoomLevel level, int row, int col ) {
        try {
            URL request = requestURL("bmng.topo.bathy.200411", level, row, col); 
            System.out.println( request );
            return requetTileImage( request );
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    RenderedImage requetTileImage( URL request ){
        RenderedImage image = JAI.create( "url", request );
        return image;
    }
    private URL requestURL( String data, ZoomLevel zoomLevel, int row, int col ) throws IOException {
        String request =
            MessageFormat.format("{0}?T={1}&L={2}&X={3}&Y={4}", new Object[]{
                    server, data,
                new Integer(levelNumber(zoomLevel)), new Integer(col), new Integer(row)});
        
        try {            
            return new URL( request );
        } catch (MalformedURLException e) {
            throw (IOException) new IOException( "Invalid request:"+request).initCause( e );
        }
    }
    private int levelNumber( ZoomLevel zoomLevel ) {
        Set set = getLevels();
        int level = 0;
        for( Iterator i = set.iterator(); i.hasNext(); level++ ) {
            if (zoomLevel.equals(i.next())) {
                return level;
            }
        }
        return -1;
    }
}
