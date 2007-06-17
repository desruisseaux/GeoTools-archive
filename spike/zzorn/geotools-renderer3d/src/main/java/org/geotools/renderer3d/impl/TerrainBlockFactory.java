package org.geotools.renderer3d.impl;

import org.geotools.renderer3d.utils.quadtree.NodeDataFactory;
import org.geotools.renderer3d.utils.quadtree.QuadTreeNode;

/**
 * Creates terrain blocks for areas specified by quad tree nodes.
 *
 * @author Hans Häggström
 */
public class TerrainBlockFactory
    implements NodeDataFactory
{

    public Object createNodeDataObject( final QuadTreeNode node )
    {
        return new TerrainBlockImpl( node );
    }
}
