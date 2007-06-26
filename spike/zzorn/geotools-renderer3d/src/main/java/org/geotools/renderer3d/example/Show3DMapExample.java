package org.geotools.renderer3d.example;

import org.geotools.map.MapContext;
import org.geotools.renderer3d.Renderer3D;
import org.geotools.renderer3d.impl.Renderer3DImpl;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

/**
 * An example of using the 3D map.
 * <p/>
 * NOTE: Handling mouse events in another thread seems to mess up the mouse input handling
 * (throws lots of exceptions on mouse press).
 * So adding a JMapPane that does that into the same UI as a 3D view does not work.
 * A more recent JME version might fix this, but they require Java 1.5.
 *
 * @author Hans Häggström
 */
public class Show3DMapExample
{

    //======================================================================
    // Public Methods

    //----------------------------------------------------------------------
    // Main Method

    public static void main( String[] args )
    {
        // Create some data
        final ExampleDataGenerator exampleDataGenerator = new ExampleDataGenerator();
        final MapContext exampleMap = exampleDataGenerator.createExampleMap();

        // Create a 3D renderer
        final Renderer3D renderer3D = new Renderer3DImpl( exampleMap );
        final Component mapView3D = renderer3D.get3DView();

        // Build and show the rest of the UI
        createUi( mapView3D );
    }

    //======================================================================
    // Private Methods

    private static void createUi( final Component view3D )
    {
        // Add some JComponents also to demonstrate that the 3D view runs inside swing.
        final JPanel mainPanel = new JPanel( new BorderLayout() );
        mainPanel.add( new JTree(), BorderLayout.WEST );
        mainPanel.add( createMenuBar(), BorderLayout.NORTH );
        mainPanel.add( view3D, BorderLayout.CENTER );

        showInFrame( mainPanel, "3D Map Demo" );
    }


    private static void showInFrame( final Component view3D, final String frameTitle )
    {
        final JFrame frame3D = new JFrame( frameTitle );
        final JPanel container = new JPanel( new BorderLayout() );
        container.setPreferredSize( new Dimension( 800, 600 ) );
        frame3D.getContentPane().add( container );

        container.add( view3D, BorderLayout.CENTER );

        frame3D.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame3D.pack();
        frame3D.show();
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
