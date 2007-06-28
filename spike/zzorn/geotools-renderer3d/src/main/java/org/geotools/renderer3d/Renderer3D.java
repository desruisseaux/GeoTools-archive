package org.geotools.renderer3d;

import com.jme.scene.Spatial;
import org.geotools.map.MapContext;

import java.awt.Component;

/**
 * A 3D map renderer.
 *
 * @author Hans Häggström
 */
public interface Renderer3D
{

    /**
     * @return the map data to render in the 3D view.
     */
    MapContext getMapContext();

    /**
     * @param mapContext the map data to render in the 3D view.
     */
    void setMapContext( MapContext mapContext );

    /**
     * @return the 3D view UI component.
     */
    Component get3DView();

    /**
     * @return the 3D scenegraph node containing the terrain.
     */
    Spatial get3DNode();
}
