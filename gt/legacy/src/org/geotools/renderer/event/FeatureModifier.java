/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.renderer.event;

// J2SE dependencies
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.image.RenderedImage;


/**
 * Defines methods for modifying the appearance of features on the map (for example when
 * highlighting). Users should typically create instances of this class by extending
 * {@link FeatureModifier.Adapter} and overriding the required methods. As an example, the
 * following code creates a <code>FeatureModifier</code> that will render map features
 * in blue:
 * <blockquote><pre>
 * &nbsp;new FeatureModifier.Adapter() {
 * &nbsp;    public Paint getPaint(Paint paint, RenderingContext context) { 
 * &nbsp;      return Color.BLUE; 
 * &nbsp;    }
 * &nbsp;}
 * </pre></blockquote>
 *
 * The interface currently deals with all fields contained in
 * {@link org.geotools.renderer.style.LineStyle2D},
 * {@link org.geotools.renderer.style.PolygonStyle2D},
 * {@link org.geotools.renderer.style.MarkStyle2D} and
 * {@link org.geotools.renderer.style.GraphicStyle2D}.
 *
 * @version $Id: FeatureModifier.java 5670 2004-05-16 17:35:30Z desruisseaux $
 * @author Julian Elliott
 */
public interface FeatureModifier {
    /**
     * Returns a modified shape object for a particular feature.
     *
     * @param  shape The default <code>Shape</code> for the current feature.
     * @param  context The rendering context, which contains the current map scale.
     * @return The <code>Shape</code> object to use for the current feature.
     */
    public Shape getShape(Shape shape, RenderingContext context);

    /**
     * Returns a modified paint object for a particular feature.
     *
     * @param  paint The default <code>Paint</code> for the current feature.
     * @param  context The rendering context, which contains the current map scale.
     * @return The <code>Paint</code> object to use for the current feature.
     */
    public Paint getPaint(Paint paint, RenderingContext context);

    /**
     * Returns a modified awt stroke object for a particular feature.
     *
     * @param  stroke The default <code>Stroke</code> for the current feature.
     * @param  context The rendering context, which contains the current map scale.
     * @return The new <code>Stroke</code> object to use for the current feature.
     */
    public Stroke getStroke(Stroke stroke, RenderingContext context);

    /**
     * Returns a modified rotation (for mark, graphic and text features).
     *
     * @param  rotation The default rotation for the current feature.
     * @param  context The rendering context, which contains the current map scale.
     * @return The rotation to use for the current feature.
     */
    public float getRotation(float rotation, RenderingContext context);

    /**
     * Returns a modified opacity (for graphic features).
     *
     * @param  opacity The default opacity for the current feature.
     * @param  context The rendering context, which contains the current map scale.
     * @return The opacity to use for the current feature.
     */
    public float getOpacity(float opacity, RenderingContext context);

    /**
     * Returns a modified size (for graphic features).
     *
     * @param  size The original size of graphic images
     * @param  context The rendering context, which contains the current map scale.
     * @return The size of graphic images to use for the current feature.
     */
    public int getSize(int size, RenderingContext context);

    /**
     * Returns a modified rendered image (for graphic features).
     *
     * @param  image The default rendered image for the current feature.
     * @param  context The rendering context, which contains the current map scale.
     * @return Tthe rendered image to use for the current feature.
     */
    public RenderedImage getRenderedImage(RenderedImage image, RenderingContext context);

    /**
     * A base (identity) implementation of the {@link FeatureModifier} interface.
     * Users should typically extend this to obtain the behaviour they want.
     *
     * @version $Id: FeatureModifier.java 5670 2004-05-16 17:35:30Z desruisseaux $
     * @author Julian Elliott
     */
    public static class Adapter implements FeatureModifier {
        /**
         * Default constructor.
         */
        public Adapter() {
        }

        /**
         * {@inheritDoc}. The default implementation returns the shape unchanged.
         */
        public Shape getShape(Shape shape, RenderingContext context) {
            return shape;
        }

        /**
         * {@inheritDoc}. The default implementation returns the paint unchanged.
         */
        public Paint getPaint(Paint paint, RenderingContext context) {
            return paint;
        }

        /**
         * {@inheritDoc}. The default implementation returns the stroke unchanged.
         */
        public Stroke getStroke(Stroke stroke, RenderingContext context) {
            return stroke;
        }

        /**
         * {@inheritDoc}. The default implementation returns the rotation unchanged.
         */
        public float getRotation(float rotation, RenderingContext context) {
            return rotation;
        }

        /**
         * {@inheritDoc}. The default implementation returns the opacity unchanged.
         */
        public float getOpacity(float opacity, RenderingContext context) {
            return opacity;
        }

        /**
         * {@inheritDoc}. The default implementation returns the size unchanged.
         */
        public int getSize(int size, RenderingContext context) {
            return size;
        }

        /**
         * {@inheritDoc}. The default implementation returns the image unchanged.
         */
        public RenderedImage getRenderedImage(RenderedImage image, RenderingContext context) {
            return image;
        }
    }
}
