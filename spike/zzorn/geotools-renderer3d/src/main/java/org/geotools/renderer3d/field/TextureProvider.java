package org.geotools.renderer3d.field;

import org.geotools.renderer3d.utils.BoundingRectangle;

/**
 * Something that provides ground texture data for requested areas.
 * The textures may be loaded in the background in a separate thread.
 *
 * @author Hans Häggström
 */
public interface TextureProvider
{

    /**
     * Start creating or loading a texture of the specified width and height for the specified world area.
     * Provide the texture to the specified listener when ready.
     *
     * @param area            the world area to create the texture for. TODO: What units are the coordinates given in?
     * @param textureWidth    width of the texture to retrieve
     * @param textureHeight   height of the texture to retrieve
     * @param textureListener a listener that should be called back when the texture is ready.
     */
    void requestTexture( BoundingRectangle area, int textureWidth, int textureHeight, TextureListener textureListener );
}
