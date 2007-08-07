package org.geotools.renderer3d.terrainblock;

import com.jme.math.Vector3f;
import com.jme.scene.Spatial;

/**
 * A square area of ground, containing the data required for the 3D view, at some resolution.
 *
 * @author Hans Häggström
 */
public interface TerrainBlock
{
    /**
     * @return the 3D node containing this terrain block.
     */
    Spatial getSpatial();

    /**
     * @return the center of the block at local ground level.
     */
    Vector3f getCenter();

}
