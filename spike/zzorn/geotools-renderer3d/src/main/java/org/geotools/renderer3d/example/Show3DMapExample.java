package org.geotools.renderer3d.example;

import org.geotools.gui.swing.JMapPane;
import org.geotools.map.MapContext;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.renderer3d.Renderer3D;
import org.geotools.renderer3d.Renderer3DImpl;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;

/**
 * An example of using the 3D map.
 *
 * @author Hans Häggström
 */
public class Show3DMapExample
{

    //======================================================================
    // Public Methods

    //----------------------------------------------------------------------
    // Main Method

    public static void main( String[] args ) throws IOException
    {
        // Create some data
        final ExampleDataGenerator exampleDataGenerator = new ExampleDataGenerator();
        final MapContext exampleMap = exampleDataGenerator.createExampleMap();

        // Create a 3D renderer
        final Renderer3D renderer3D = new Renderer3DImpl( exampleMap );
        final Component mapView3D = renderer3D.get3DView();

        // Create a 2D renderer with the same data for comparsion
        final StreamingRenderer streamingRenderer = new StreamingRenderer();
        final JMapPane mapView2D = new JMapPane( streamingRenderer, exampleMap );
        mapView2D.setMapArea( exampleMap.getLayerBounds() );
        mapView2D.setState( JMapPane.Pan );
        mapView2D.setCursor( new Cursor( Cursor.MOVE_CURSOR ) );

        // Build and show the rest of the UI
        createUi( mapView3D, mapView2D );
    }

    //======================================================================
    // Private Methods

    private static void createUi( final Component view3D, final JMapPane view2D )
    {
        // Add some JComponents also to demonstrate that the 3D view runs inside swing.
        final JPanel mainPanel = new JPanel( new BorderLayout() );
        mainPanel.add( new JTree(), BorderLayout.WEST );
        mainPanel.add( createMenuBar(), BorderLayout.NORTH );

        final JPanel view3DHolder = new JPanel( new BorderLayout() );
        view3DHolder.add( view3D, BorderLayout.CENTER );

        final JSplitPane splitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, view3DHolder, view2D );
        splitPane.setDividerLocation( 500 );
        splitPane.setOneTouchExpandable( true );

        mainPanel.add( splitPane, BorderLayout.CENTER );

        showInFrame( mainPanel, "3D Map Demo" );
    }


    private static void showInFrame( final Component view, final String frameTitle )
    {
        final JFrame frame3D = new JFrame( frameTitle );
        final JPanel container = new JPanel( new BorderLayout() );
        container.setPreferredSize( new Dimension( 800, 600 ) );
        frame3D.getContentPane().add( container );
        container.add( view, BorderLayout.CENTER );

        frame3D.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame3D.pack();
        frame3D.setVisible( true );
    }


    private static JMenuBar createMenuBar()
    {
        final JMenuBar menuBar = new JMenuBar();

        final JMenu menu = new JMenu( "3D View Demo" );
        menuBar.add( menu );

        menu.add( new AbstractAction( "Exit" )
        {

            public void actionPerformed( final ActionEvent e )
            {
                System.exit( 0 );
            }

        } );

        return menuBar;
    }

}
