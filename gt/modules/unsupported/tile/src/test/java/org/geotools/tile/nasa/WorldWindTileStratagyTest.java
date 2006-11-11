package org.geotools.tile.nasa;

import java.awt.image.RenderedImage;
import java.net.URI;
import java.net.URL;
import java.util.Set;
import java.util.SortedSet;

import javax.media.jai.widget.ScrollingImagePanel;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import junit.framework.TestCase;

import org.geotools.geometry.Envelope2D;
import org.geotools.resources.TestData;
import org.geotools.tile.ZoomLevel;

import sun.rmi.runtime.GetThreadPoolAction;

public class WorldWindTileStratagyTest extends TestCase {
    WorldWindTileStratagy ww;
    private ZoomLevel ZERO;
    private int COLS;
    private int ROWS;
    private ZoomLevel ONE;
    
    /**
     * Test as per:
     * "http://worldwind25.arc.nasa.gov/tile/tile.aspx?T=bmng.topo.bathy.200411&L=4&X=30&Y=63"
     */
    protected void setUp() throws Exception {
        super.setUp();
        URI uri = new URI("http://worldwind25.arc.nasa.gov/tile/tile.aspx");
        ww = new WorldWindTileStratagy( uri );        
        SortedSet set = ww.getLevels();
        ZERO = (ZoomLevel) set.iterator().next();
        COLS = 129;
        ROWS = 67;
        ONE = ZERO.zoom(1);    
    }
    
    public void testDataModel(){
        assertNotNull( ww.getBounds() );
        assertEquals( 512, ww.getTileSize() );
        SortedSet set = ww.getLevels();
        ZoomLevel zero = (ZoomLevel) set.iterator().next();
        
        assertEquals( COLS, zero.getNumberOfColumns() );
        assertEquals( ROWS, zero.getNumberOfRows() );
        
        
        Envelope2D bounds = ww.getBounds();
        Envelope2D sample = ww.requestTileBounds(zero, ROWS/2, COLS/2);
        
        assertTrue( "sample in bounds", bounds.contains( sample ));
        
        Envelope2D first = ww.requestTileBounds(zero, 0, 0 );
        assertTrue( bounds.contains( first ));
        assertEquals( bounds.x, first.x, 0.0 );
        assertEquals( bounds.y, first.y, 0.0 );
        
        ZoomLevel one = zero.zoom(1);        
        assertEquals( COLS*2, one.getNumberOfColumns() );
        assertEquals( ROWS*2, one.getNumberOfRows() );        
        assertTrue( "set complete", set.contains( one ));
        
        Envelope2D sample2 = ww.requestTileBounds(one, ROWS, COLS );
        assertTrue( "Level One sample in bounds", bounds.contains( sample2 ));
        assertTrue( "Level One sample contained", sample.contains( sample2 ));
    }
    /** This is an online test. */
    public void testTileAccess() throws Exception { 
        RenderedImage image = ww.requestTileImage( ONE, 1, 1 );
        assertNotNull( "content available", image );
        
        performImageTest( image );
        assertNotNull( "content available", image );
    }
    
    private void performImageTest( RenderedImage image ) throws Exception {
        if(TestData.isInteractiveTest()){
            show( getName(), image );
            Thread.sleep(1000);
        }
        else {
            image.getData();
        }
    }
    public void XtestShow() throws Exception {
        URL sampleRequest = 
            new URL("http://mapserver.refractions.net/worldwind_demo_wms.php?T=BCData2&L=0&X=5&Y=15");
        RenderedImage image = ww.requetTileImage( sampleRequest );        
        performImageTest( image );
    }
    public void XtestShow2() throws Exception {
        URL sampleRequest =
            new URL("http://worldwind25.arc.nasa.gov/tile/tile.aspx?T=bmng.topo.bathy.200411&L=1&X=3&Y=7");
        RenderedImage image = ww.requetTileImage( sampleRequest );
        
        performImageTest( image );            
    }
    public void show(final String title, RenderedImage image ) {
        final JFrame frame = new JFrame(String.valueOf(getName()));
        
        final ScrollingImagePanel panel = new ScrollingImagePanel(image, 512,
                512);
        frame.setTitle(title);
        frame.getContentPane().add(new JScrollPane(panel));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame.pack();
                frame.setVisible(true);
            }
        });

    }
}
