package org.geotools.renderer3d;

import com.jme.scene.Spatial;
import org.geotools.map.MapContext;
import org.geotools.renderer3d.impl.NavigationGesture;

import java.awt.Component;

/**
 * A 3D map renderer.
 *
 * @author Hans H�ggstr�m
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

    /**
     * Adds the specified NavigationGesture.
     *
     * @param addedNavigationGesture should not be null or already added.
     */
    void addNavigationGesture( NavigationGesture addedNavigationGesture );

    /**
     * Removes the specified NavigationGesture.
     *
     * @param removedNavigationGesture should not be null, and should be present.
     */
    void removeNavigationGesture( NavigationGesture removedNavigationGesture );

    /**
     * Removes all current navigation gestures.
     * Useful if you want to remove the default gestures, in order to add your own custom ones.
     */
    void removeAllNavigationGestures();
}
