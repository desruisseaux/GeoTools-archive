package org.geotools.renderer3d.example;

import org.geotools.feature.*;
import org.geotools.feature.type.NumericAttributeType;
import org.geotools.gui.swing.JMapPane;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.BasicLineStyle;
import org.geotools.renderer3d.Renderer3D;
import org.geotools.renderer3d.impl.Renderer3DImpl;

import javax.swing.*;
import java.awt.Dimension;
import java.util.Collections;

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

    public static void main( String[] args )
    {
        // Create some data
        final MapContext exampleMap = createExampleMap();


        // Create a 3D renderer
        final Renderer3D renderer3D = new Renderer3DImpl( exampleMap );

        // Create a UI with the 3D view from the 3D renderer
        showInFrame( renderer3D.get3DView(), "3D Map View" );


        // Create a 2D renderer for comparsion
        final StreamingRenderer streamingRenderer = new StreamingRenderer();

        // Show the same data in the 2D renderer
        showInFrame( new JMapPane( streamingRenderer, exampleMap ), "2D Map View");
    }

    //======================================================================
    // Private Methods

    private static MapContext createExampleMap()
    {
        // TODO: Read up on Coordinate Reference System, and pass one to the constructor below:
        final MapContext exampleMap = new DefaultMapContext();

        // Some random height coverage data
        // TODO

        // Some random roads
        // TODO

        // Some random lakes and rivers
        // TODO

        // Some random cities containing building polygons
        // TODO

        // Some random coverage color (population density?)
        // TODO

        // A simple initial shape
        final AttributeType roadWidth = new NumericAttributeType( "roadWidth", Double.class, false, 0, 100, new Double(5),null );
        try
        {
            final DefaultFeatureType featureType = new DefaultFeatureType( "road",
                                                                           "renderer3d_example",
                                                                           Collections.singletonList( roadWidth ),
                                                                           Collections.EMPTY_LIST,
                                                                           null );
            final FeatureCollection featureCollection = new DefaultFeatureCollection( "test", featureType );

//            featureCollection.add( )

            exampleMap.addLayer( new DefaultMapLayer(featureCollection, new BasicLineStyle( )));
        }
        catch ( SchemaException e )
        {
            e.printStackTrace();
        }

        return exampleMap;
    }


    private static void showInFrame( final JComponent view3D, final String frameTitle )
    {
        final JFrame frame3D = new JFrame( frameTitle );

        frame3D.getContentPane().add( view3D );

        view3D.setPreferredSize( new Dimension( 800, 600) );
        frame3D.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame3D.pack();
        frame3D.show();
    }

}
