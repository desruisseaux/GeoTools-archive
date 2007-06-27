package org.geotools.renderer3d.example;

import org.geotools.feature.*;
import org.geotools.feature.type.NumericAttributeType;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.styling.BasicLineStyle;

import java.util.Collections;

/**
 * A simple test data generator.
 *
 * @author Hans Häggström
 */
public class ExampleDataGenerator
{


    /**
     * @return an map context with generated test data.
     */
    public MapContext createExampleMap()
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
        final AttributeType roadWidth = new NumericAttributeType( "roadWidth",
                                                                  Double.class,
                                                                  false,
                                                                  0,
                                                                  100,
                                                                  new Double( 5 ),
                                                                  null );
        try
        {
            final DefaultFeatureType featureType = new DefaultFeatureType( "road",
                                                                           "renderer3d_example",
                                                                           Collections.singletonList( roadWidth ),
                                                                           Collections.EMPTY_LIST,
                                                                           null );
            final FeatureCollection featureCollection = new DefaultFeatureCollection( "test", featureType );

//            featureCollection.add( )

            exampleMap.addLayer( new DefaultMapLayer( featureCollection, new BasicLineStyle() ) );
        }
        catch ( SchemaException e )
        {
            e.printStackTrace();
        }

        return exampleMap;
    }

}
