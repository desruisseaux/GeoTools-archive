package org.geotools.renderer3d.impl;

import com.jme.math.Vector3f;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Box;
import org.geotools.renderer3d.utils.quadtree.QuadTreeNode;

/**
 * @author Hans Häggström
 */
public class TerrainBlockImpl
        implements TerrainBlock
{

    //======================================================================
    // Private Fields

    private final QuadTreeNode myQuadTreeNode;
    private Box mySpatial;

    //======================================================================
    // Public Methods

    //----------------------------------------------------------------------
    // Constructors

    public TerrainBlockImpl( final QuadTreeNode quadTreeNode )
    {
        myQuadTreeNode = quadTreeNode;
    }

    //----------------------------------------------------------------------
    // TerrainBlock Implementation


    public Spatial getSpatial()
    {
        if ( mySpatial == null )
        {
            mySpatial = createSpatial();
        }

        return mySpatial;
    }

    private Box createSpatial()
    {
        final Box box = new Box( toString(),
                                 new Vector3f( (float) myQuadTreeNode.getX1(),
                                               (float) myQuadTreeNode.getY1(),
                                               10 ),
                                 new Vector3f( (float) myQuadTreeNode.getX2(),
                                               (float) myQuadTreeNode.getY2(),
                                               11 ) );
        box.setRandomColors();
        return box;
    }

}

