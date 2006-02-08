/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
 * (C) 2005, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.display.canvas;

// J2SE Utilities
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.Locale;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;

// J2SE Graphics
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.AlphaComposite;

// J2SE Geometries
import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

// J2SE Images
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;

// J2SE widgets
import java.awt.Frame;
import java.awt.Dialog;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.IllegalComponentStateException;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;
import java.beans.PropertyChangeEvent;

// JAI dependencies
import javax.media.jai.GraphicsJAI;

// OpenGIS dependencies
import org.opengis.go.display.DisplayFactory;
import org.opengis.go.display.primitive.Graphic;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.crs.DerivedCRS;

// Geotools dependencies
import org.geotools.util.RangeSet;
import org.geotools.resources.GraphicsUtilities;
import org.geotools.resources.geometry.XRectangle2D;
import org.geotools.resources.geometry.XAffineTransform;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;
import org.geotools.resources.i18n.Logging;
import org.geotools.resources.i18n.LoggingKeys;


/**
 * Default canvas implementation using {@link Graphics2D}. Rendering can optionnaly make use
 * of {@link Image} buffers.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class BufferedCanvas2D extends ReferencedCanvas2D {
    /**
     * {@code true} for enabling usage of {@link GraphicsJAI}.
     */
    private static final boolean ENABLE_JAI = true;

    /**
     * If different than 1, then calls to the {@linkplain #zoomChanged} method will be performed
     * with an affine transform expanded by this factor. This is used in order to avoid rounding
     * error in calculation of widget {@link java.awt.geom.Area} that need to be refreshed.
     */
    private static final double SCALE_ZOOM_CHANGE = 1.000001;

    /**
     * The offscreen buffers.  There is one {@link VolatileImage} or {@link BufferedImage}
     * for each range in the {@link #offscreenZRanges} set. If non-null, this array length
     * must be equals to the size of {@link #offscreenZRanges}.
     *
     * @see #setOffscreenBuffered
     */
    private Image[] offscreenBuffers;

    /**
     * Tells if an offscreen buffer is of type {@link VolatileImage}.
     * If {@code false}, then it is of type {@link BufferedImage}.
     */
    private boolean[] offscreenIsVolatile;

    /**
     * Tells if an offscreen buffer need to be repaint. This array length must be
     * equals to the length of {@link #offscreenBuffers}.
     */
    private boolean[] offscreenNeedRepaint;

    /**
     * The ranges of {@linkplain GraphicPrimitive2D#getZOrderHint z-order} for which to create
     * an offscreen buffer. There is one offscreen buffer (usually a {@link VolatileImage})
     * for each range of z-orders.
     *
     * @see #setOffscreenBuffered
     */
    private RangeSet offscreenZRanges;

    /**
     * Statistics about rendering. Used for logging messages only.
     */
    private transient RenderingStatistics statistics;

    /**
     * {@code true} if the map is printed instead of painted on screen. When printing, graphics
     * should block until all data are available instead of painting only available data and
     * invokes {@link GraphicPrimitive2D#refresh()} later.
     */
    private transient boolean isPrinting;

    /**
     * The component owner, or {@code null} if none. This is used for managing
     * repaint request (see {@link GraphicPrimitive2D#refresh}) or mouse events.
     */
    private final Component owner;

    /**
     * Listener for events of interest to this canvas. Events may come
     * from any {@link GraphicPrimitive2D} or from the {@link Component}.
     */
    private final ComponentListener listener = new ComponentListener();

    /**
     * Updates the enclosing canvas according various AWT events.
     */
    private final class ComponentListener extends ComponentAdapter {
        /** Invoked when the component's size changes. */
        public void componentResized(final ComponentEvent event) {
            synchronized (BufferedCanvas2D.this) {
                checkDisplayBounds();
                zoomChanged(null);
            }
        }

        /** Invoked when the component's position changes. */
        public void componentMoved(final ComponentEvent event) {
            synchronized (BufferedCanvas2D.this) {
                checkDisplayBounds();
                zoomChanged(null); // Translation term has changed.
            }
        }

        /** Invoked when the component has been made invisible. */
        public void componentHidden(final ComponentEvent event) {
            synchronized (BufferedCanvas2D.this) {
                clearCache();
            }
            // As a symetrical approach,  it would be nice to invoke 'prefetch(...)' inside
            // 'componentShown(...)' too. But we don't know for sure what the widget bounds
            // and the zoom will be. We are better to wait until 'paint(...)' is invoked.
        }
    }

    /**
     * Creates an initially empty canvas with a default objective CRS.
     *
     * @param factory The display factory associated with this canvas, or {@code null} if none.
     * @param owner   The component owner, or {@code null} if none.
     */
    public BufferedCanvas2D(final DisplayFactory factory, final Component owner) {
        super(factory);
        this.owner = owner;
        if (owner != null) {
            owner.addComponentListener(listener);
        }
    }

    /**
     * Returns the title assigned to this {@code Canvas}.
     * If no title were {@linkplain #setTitle explicitly set}, then
     * this method returns the title of the window which contains this canvas.
     */
    public String getTitle() {
        String title = super.getTitle();
        if (title == null) {
            for (Component c=owner; c!=null; c=c.getParent()) {
                if (c instanceof Frame) {
                    title = ((Frame) c).getTitle();
                    break;
                }
                if (c instanceof Dialog) {
                    title = ((Dialog) c).getTitle();
                    break;
                }
                if (c instanceof JInternalFrame) {
                    title = ((JInternalFrame) c).getTitle();
                    break;
                }
            }
        }
        return title;
    }

    /**
     * Returns the locale for this object. The default implementation returns the locale of the
     * {@link Component} that own this canvas, if any. Otherwise, a default locale will be returned.
     *
     * @see Component#getLocale
     * @see JComponent#getDefaultLocale
     * @see Locale#getDefault
     */
    public Locale getLocale() {
        if (owner != null) try {
            return owner.getLocale();
        } catch (IllegalComponentStateException exception) {
            // Not yet added to a containment hierarchy. Ignore...
            if (owner instanceof JComponent) {
                return JComponent.getDefaultLocale();
            }
        }
        return super.getLocale();
    }

    /**
     * Returns the display bounds in terms of {@linkplain #getDisplayCRS display CRS}.
     * If no bounds were {@linkplain #setDisplayBounds explicitly set}, then this method
     * returns the {@linkplain Component#getBounds() widget bounds}.
     */
    public synchronized Shape getDisplayBounds() {
        Shape bounds = super.getDisplayBounds();
        if (bounds.equals(XRectangle2D.INFINITY) && owner!=null) {
            bounds = owner.getBounds();
        }
        return bounds;
    }

    /**
     * Invoked when the display bounds may have changed as a result of component resizing.
     */
    private void checkDisplayBounds() {
        if (super.getDisplayBounds().equals(XRectangle2D.INFINITY)) {
            listeners.firePropertyChange(DISPLAY_BOUNDS_PROPERTY, null, null);
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized Graphic add(Graphic graphic) {
        graphic = super.add(graphic);
        flushOffscreenBuffer(graphic.getZOrderHint());
        repaint(); // Must be invoked last
        return graphic;
    }

    /**
     * Removes the given {@code Graphic} from this canvas.
     */
    public synchronized void remove(final Graphic graphic) {
        repaint(); // Must be invoked first
        flushOffscreenBuffer(graphic.getZOrderHint());
        super.remove(graphic);
    }

    /**
     * Remove all graphics from this canvas.
     */
    public synchronized void removeAll() {
        repaint(); // Must be invoked first
        flushOffscreenBuffers();
        super.removeAll();
    }

    /**
     * Invoked automatically when a graphic registered in this canvas changed.
     */
    protected void graphicPropertyChanged(final AbstractGraphic graphic,
                                          final PropertyChangeEvent event)
    {
        super.graphicPropertyChanged(graphic, event);
        final String propertyName = event.getPropertyName();
        if (propertyName.equalsIgnoreCase(Z_ORDER_HINT_PROPERTY)) {
            final Object value = event.getOldValue();
            if (value instanceof Number) {
                final double oldZOrder = ((Number) value).doubleValue();
                flushOffscreenBuffer(oldZOrder);
            }
        }
    }

    /**
     * Checks the state of the given image. If {@code image} is null, then this method returns
     * {@link VolatileImage#IMAGE_INCOMPATIBLE}. Otherwise, if {@code image} is an instance of
     * {@link VolatileImage}, then this method invokes {@link VolatileImage#validate}. Otherwise,
     * (usually the {@link BufferedImage} case) this method returns {@link VolatileImage#IMAGE_OK}.
     *
     * @param  image The image.
     * @param  config The graphics configuration.
     * @return The state, as one of {@link VolatileImage} constants.
     */
    private static int validate(final Image image, final GraphicsConfiguration config) {
        if (image == null) {
            return VolatileImage.IMAGE_INCOMPATIBLE;
        }
        if (image instanceof VolatileImage) {
            return ((VolatileImage) image).validate(config);
        }
        return VolatileImage.IMAGE_OK;
    }

    /**
     * Returns {@code true} if rendering data was lost since last validate call.
     */
    private static boolean contentsLost(final Image image) {
        if (image instanceof VolatileImage) {
            return ((VolatileImage) image).contentsLost();
        }
        return false;
    }

    /**
     * Prints this canvas and all visible graphics it contains. This method is similar to
     * <code>{@linkplain #paint paint}(output, zoom)</code>, but is more appropriate when
     * the output device is a printer instead of a video device. Note that rendering using
     * the {@code print} method is usually slower than rendering using the {@code paint}
     * method.
     */
    public synchronized void print(final Graphics2D output, final AffineTransform zoom) {
        isPrinting = true;
        try {
            paint(output, zoom);
        } finally {
            isPrinting = false;
        }
    }

    /**
     * Paints this canvas and all visible graphics it contains. Before to invoke this method,
     * <code>{@link #setDisplayBounds setDisplayBounds}(bounds)</code> must be invoked at least
     * once, where {@code bounds} is typically the value returned by
     * {@link org.geotools.gui.swing.ZoomPane#getZoomableBounds}.
     *
     * @param output The <cite>Java2D</cite> graphics handler to draw to.
     * @param zoom A transform which converts "World coordinates" in {@linkplain #getObjectiveCRS
     *             objective CRS} to output coordinates in {@linkplain #getDisplayCRS display CRS}.
     *             This transform is usually provided by {@link org.geotools.gui.swing.ZoomPane#zoom}.
     */
    public synchronized void paint(Graphics2D output, final AffineTransform zoom) {
        assert EventQueue.isDispatchThread(); // Rendering must occurs in the Swing thread.
        if (statistics == null) {
            statistics = new RenderingStatistics(getLogger());
        }
        statistics.init();
        if (ENABLE_JAI) {
            output = GraphicsJAI.createGraphicsJAI(output, owner);
        }
        /*
         * Sets a flag for avoiding some "refresh()" events while we are actually painting.
         * For example some implementation of the GraphicPrimitive2D.paint(...) method may
         * detects changes since the last rendering and invokes some kind of invalidate(...)
         * methods before the graphic rendering begin. Invoking those methods may cause in some
         * indirect way a call to GraphicPrimitive2D.refresh(), which will trig an other widget
         * repaint. This second repaint is usually not needed, since Graphics usually managed
         * to update their informations before they start their rendering. Consequently,
         * disabling repaint events while we are painting help to reduces duplicated rendering.
         */
        final Rectangle displayBounds = getDisplayBounds().getBounds();
        Rectangle          clipBounds = output.getClipBounds();
        Rectangle2D         dirtyArea = XRectangle2D.INFINITY;
        if (clipBounds == null) {
            clipBounds = displayBounds;
        } else if (displayBounds.contains(clipBounds)) {
            dirtyArea = clipBounds;
        }
        paintStarted(dirtyArea);
        /*
         * If the zoom has changed, send a notification to all graphics before to start the
         * rendering. Graphics will update their cache, which is used in order to decide if
         * a graphic needs to be repainted or not. Note that some graphics may change their
         * state, which may results in a new 'paint' event to be fired.  But because of the
         * 'dirtyArea' flag above, some 'paint' event will be intercepted in order to avoid
         * repainting the same area twice.
         */
        final GraphicsConfiguration config = output.getDeviceConfiguration();
        if (!objectiveToDisplay.equals(zoom)) {
            /*
             * Computes the change as an affine transform, and send the notification.
             * Optionnaly scale slightly the change in order to avoid rounding errors
             * in calculation of widget area that need to be refreshed.
             */
            try {
                final AffineTransform change = objectiveToDisplay.createInverse();
                change.preConcatenate(zoom);
                if (SCALE_ZOOM_CHANGE != 1) {
                    final double centerX = displayBounds.getCenterX();
                    final double centerY = displayBounds.getCenterY();
                    change.translate(      centerX,           centerY);
                    change.scale(SCALE_ZOOM_CHANGE, SCALE_ZOOM_CHANGE);
                    change.translate(     -centerX,          -centerY);
                }
                zoomChanged(change);
            } catch (NoninvertibleTransformException exception) {
                /*
                 * Should not happen. If it happen anyway, declare that everything must be
                 * repainted. It will be slower, but will not prevent the renderer to work.
                 */
                handleException("BufferedCanvas2D", "paint", exception);
                zoomChanged(null);
            }
            try {
                /*
                 * Computes the new scale factor. This scale factor takes in account the real
                 * size of the rendering device (e.g. the screen), but is only as accurate as
                 * the information supplied by the underlying system.
                 */
                final AffineTransform normalize = zoom.createInverse();
                normalize.concatenate(config.getNormalizingTransform());
                normalize.preConcatenate(normalizeToDots);
                setScale(1 / XAffineTransform.getScale(normalize));
            } catch (NoninvertibleTransformException exception) {
                handleException("BufferedCanvas2D", "paint", exception);
            }
            /*
             * Now takes in account the zoom change.
             */
            objectiveToDisplay.setTransform(zoom);
        }
        /*
         * If the zoom or the device changed, then the 'displayCRS' and 'deviceCRS' must be
         * recreated. Failure to create those CRS will make the rendering process impossible.
         * In such case, we will paint the stack trace right into the component and exit from
         * this method.
         */
        displayToDevice.setToTranslation(-displayBounds.x, -displayBounds.y);
        try {
            updateObjectiveToDeviceTransforms();
        } catch (TransformException exception) {
            GraphicsUtilities.paintStackTrace(output, displayBounds, exception);
            paintFinished(false);
            return;
        }
        /*
         * Safety check: if the component size changed, then we will need to dispose old
         * offscreen image buffers and recreate new ones (since we can't rescale images).
         */
        final int offscreenCount = (!isPrinting && offscreenZRanges!=null) ?
                                                   offscreenZRanges.size() : 0;
        for (int i=0; i<offscreenCount; i++) {
            final Image buffer = offscreenBuffers[i];
            if (buffer != null) {
                if (buffer.getWidth (owner) != displayBounds.width ||
                    buffer.getHeight(owner) != displayBounds.height)
                {
                    buffer.flush();
                    offscreenBuffers    [i] = null;
                    offscreenNeedRepaint[i] = true;
                }
            }
        }
        /*
         * Draw all graphics, starting with the one with the lowest <var>z</var> value. Before
         * to start the actual drawing,  we will notify all graphics that they are about to be
         * drawn. Some graphics may spend one or two threads for pre-computing data.
         */
        final List/*<Graphic>*/ graphics = getGraphics();
        final int graphicCount = graphics.size();
        boolean success = false;
        output.addRenderingHints(hints);
        final RenderingContext context = new RenderingContext(this, displayBounds, isPrinting);
        try {
            context.setGraphics(output, zoom);
            if (allowPrefetch()) {
                // Prepare data in background threads. While we are painting
                // one graphic, next graphics will be pre-computed.
                for (int i=0; i<graphicCount; i++) {
                    final Graphic candidate = (Graphic) graphics.get(i);
                    if (candidate instanceof GraphicPrimitive2D) {
                        ((GraphicPrimitive2D) candidate).prefetch(context);
                    }
                }
            }
            int    offscreenIndex = -1;                  // Index of current offscreen buffer.
            double minZOrder = Double.NEGATIVE_INFINITY; // The minimum z-value for current buffer.
            double maxZOrder = Double.NaN;               // The maximum z-value for current buffer.
            for (int graphicIndex=0; graphicIndex<graphicCount; graphicIndex++) {
                int graphicIndexUp = graphicIndex;
                Graphic candidate = (Graphic) graphics.get(graphicIndex);
                if (!(candidate instanceof GraphicPrimitive2D)) {
                    continue;
                }
                final GraphicPrimitive2D graphic = (GraphicPrimitive2D) candidate;
                final double zOrder = graphic.getZOrderHint();
                while (zOrder >= minZOrder) {
                    if (!(zOrder <= maxZOrder)) {
                        if (++offscreenIndex < offscreenCount) {
                            minZOrder = offscreenZRanges.getMinValueAsDouble(offscreenIndex);
                            maxZOrder = offscreenZRanges.getMaxValueAsDouble(offscreenIndex);
                            continue;
                        } else {
                            minZOrder = Double.NaN;
                            maxZOrder = Double.NaN;
                            break;
                        }
                    }
                    assert offscreenZRanges.indexOfRange(new Double(zOrder)) == offscreenIndex;
                    /*
                     * We have found the begining of a range to be rendered using offscreen
                     * buffer. Search the index of the last graphic in this range, exclusive.
                     */
                    while (++graphicIndexUp < graphicCount) {
                        candidate = (Graphic) graphics.get(graphicIndexUp);
                        if (!(candidate.getZOrderHint() <= maxZOrder)) {
                            break;
                        }
                    }
                    break;
                }
                /*
                 * If the current graphic is not part of any offscreen buffer,
                 * render it directly in the Swing's output handler.
                 */
                if (graphicIndex == graphicIndexUp) {
                    try {
                        paint(graphic, context, clipBounds);
                    } catch (TransformException exception) {
                        handleException("GraphicPrimitive2D", "paint", exception);
                    } catch (RuntimeException exception) {
                        handleException("GraphicPrimitive2D", "paint", exception);
                    }
                    continue;
                }
                /*
                 * The range of graphic index to render offscreen goes from 'graphicIndex'
                 * inclusive to 'graphicIndexUp' exclusive. If the image is still valid,
                 * paint it immediately. Otherwise, performs the rendering offscreen.
                 */
                boolean createFromComponent = (owner != null);
                Rectangle bufferClip = clipBounds;
                Image buffer = offscreenBuffers[offscreenIndex];
renderOffscreen:while (true) {
                    switch (validate(buffer, config)) {
                        //
                        // Image incompatible or inexistant: recreates an empty
                        // one and restarts the loop from the 'validate' check.
                        //
                        case VolatileImage.IMAGE_INCOMPATIBLE: {
                            if (buffer != null) {
                                buffer.flush();
                                buffer = null;
                            }
                            if (createFromComponent) {
                                createFromComponent = false;
                                if (offscreenIsVolatile[offscreenIndex]) {
                                    buffer = owner.createVolatileImage(
                                                   displayBounds.width, displayBounds.height);
                                }
                            }
                            if (buffer == null) {
                                if (offscreenIsVolatile[offscreenIndex]) {
                                    buffer = config.createCompatibleVolatileImage(
                                                    displayBounds.width, displayBounds.height);
                                } else {
                                    buffer = config.createCompatibleImage(
                                                    displayBounds.width, displayBounds.height,
                                                    Transparency.TRANSLUCENT);
                                }
                            }
                            offscreenBuffers[offscreenIndex] = buffer;
                            bufferClip = displayBounds;
                            continue;
                        }
                        //
                        // Image preserved: paint it immediately if no graphic changed
                        // since last rendering, or repaint only the damaged area.
                        //
                        case VolatileImage.IMAGE_OK: {
                            if (!offscreenNeedRepaint[offscreenIndex]) {
                                // REVISIT: Which AlphaComposite to use here?
                                output.drawImage(buffer, displayBounds.x,
                                                         displayBounds.y, owner);
                                if (contentsLost(buffer)) {
                                    // Upcomming 'validate' will falls in IMAGE_RESTORED case.
                                    continue;
                                }
                                // Rendering finished for this offscreen buffer.
                                break renderOffscreen;
                            }
                            // Repaint only the damaged area.
                            break;
                        }
                        //
                        // Contents has been lost: we need to repaint the whole image.
                        //
                        case VolatileImage.IMAGE_RESTORED: {
                            bufferClip = displayBounds;
                            break;
                        }
                    }
                    /*
                     * At this point, we know that some area need to be repainted.
                     * Reset a transparent background on the area to be repainted,
                     * and invokes GraphicPrimitive2D.update(...) for each graphics
                     * to be included in this offscreen buffer.
                     */
                    final Graphics2D graphicsOff = (Graphics2D) buffer.getGraphics();
                    final Composite oldComposite = graphicsOff.getComposite();
                    graphicsOff.addRenderingHints(hints);
                    graphicsOff.translate(-displayBounds.x, -displayBounds.y);
                    if (offscreenIsVolatile[offscreenIndex]) {
                        // HACK: We should use the transparent color in all cases. However,
                        //       as of J2SE 1.4, VolatileImage doesn't supports transparency.
                        //       We have to use some opaque color in the main time.
                        // TODO: Delete this hack when J2SE 1.5 will be available.
                        //       Avoid filling if the image has just been created.
                        graphicsOff.setComposite(AlphaComposite.Src);
                    } else {
                        graphicsOff.setComposite(AlphaComposite.Clear);
                    }
                    graphicsOff.setColor(owner!=null ? owner.getBackground() : Color.WHITE);
                    graphicsOff.fill(bufferClip);
                    graphicsOff.setComposite(oldComposite); // REVISIT: is it the best composite?
                    graphicsOff.setColor(owner!=null ? owner.getForeground() : Color.BLACK);
                    graphicsOff.clip(bufferClip); // Information needed by some graphics.
                    context.setGraphics(graphicsOff, zoom);
                    offscreenNeedRepaint[offscreenIndex] = false;
                    for (int i=graphicIndex; i<graphicIndexUp; i++) {
                        candidate = (Graphic) graphics.get(i);
                        if (candidate instanceof GraphicPrimitive2D) try {
                            paint((GraphicPrimitive2D) candidate, context, bufferClip);
                        } catch (Exception exception) {
                            /*
                             * An exception occured in user code. Do not try anymore to use
                             * offscreen buffer for this graphic; render it in the "ordinary"
                             * loop instead. If this exception still occurs, it will be the
                             * "ordinary" loop's job to handle it.
                             */
                            context.disposeGraphics();
                            buffer.flush();
                            offscreenBuffers[offscreenIndex] = buffer = null;
                            graphicIndexUp = graphicIndex; // Force re-rendering of this graphic.
                            minZOrder    = Double.NaN;  // Disable offscreen for all graphics.
                            handleOffscreenException(candidate, exception);
                            break renderOffscreen;
                        }
                        if (contentsLost(buffer)) {
                            break;
                        }
                    }
                    context.disposeGraphics();
                }
                /*
                 * The offscreen buffer has been successfully rendered (or we failed because
                 * of an exception in user's code). If the ordinary (clipped) context was
                 * modified, it will be restored in the "ordinary" loop when first needed.
                 */
                graphicIndex = graphicIndexUp-1;
            }
            success = true;
        } finally {
            context.setGraphics(null, null);
            paintFinished(success);
        }
        statistics.finish(this);
    }

    /**
     * {@linkplain GraphicPrimitive2D#paint Paints} the specified graphic and
     * {@linkplain GraphicPrimitive2D#setDisplayBounds update its display bounds}.
     * If the specified graphic is not {@linkplain GraphicPrimitive2D#getVisible visible}
     * or if {@code clipBounds} doesn't intersect the
     * {@linkplain GraphicPrimitive2D#getDisplayBounds graphic display bounds},
     * then this method do nothing.
     *
     * @param graphic    The graphics to paint.
     * @param context    Information relative to the rendering context.
     * @param clipBounds The area to paint in terms of display CRS, or {@code null}.
     * @throws TransformException If a coordinate transformation failed during the rendering
     *         process.
     */
    private void paint(final GraphicPrimitive2D graphic,
                       final RenderingContext   context,
                       final Rectangle          clipBounds)
            throws TransformException
    {
        assert Thread.holdsLock(this);
        if (graphic.getVisible()) {
            final Shape paintedArea = graphic.getDisplayBounds();
            if (paintedArea==null || clipBounds==null || paintedArea.intersects(clipBounds)) {
                context.paintedArea = null;
                graphic.paint(context);
                graphic.setDisplayBounds(context.paintedArea);
            }
        }
    }

    /**
     * Declares that the {@link Component} need to be repainted. This method can be invoked
     * from any thread (it doesn't need to be the <cite>Swing</cite> thread). Note that this
     * method doesn't invoke any {@link #flushOffscreenBuffer} method; this is up to the caller
     * to invokes the appropriate method.
     */
    private void repaint() {
        final Component owner = this.owner;
        if (owner != null) {
            if (EventQueue.isDispatchThread()) {
                owner.repaint();
            } else {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        repaint();
                    }
                });
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @todo Check for dirty area should take zOrder in account.
     */
    public void repaint(final Graphic     graphic,
                        final Rectangle2D objectiveArea,
                        final Rectangle   displayArea)
    {
        // Do NOT synchronize before next block.
        /*
         * If the current thread is not the Swing thread, schedule for
         * execution in the Swing thread and returns immediatement.
         */
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    repaint(graphic, objectiveArea, displayArea);
                }
            });
            return;
        }
        /*
         * At this point, we know that we are running in the Swing thread. Computes the union of
         * (transformed) objective and display rectangle.  Note: we performs this computation in
         * the Swing thread in order to make sure that we use the affine transform from the last
         * painting.
         */
        final Rectangle bounds;
        synchronized (this) {
            if (objectiveArea != null) {
                if (objectiveArea.equals(XRectangle2D.INFINITY)) {
                    bounds = null;
                } else {
                    bounds = objectiveToDisplay(objectiveArea);
                    if (displayArea != null) {
                        bounds.add(displayArea);
                    }
                }
            } else if (displayArea != null) {
                bounds = displayArea;
            } else {
                return; // Both display and objective area are null: nothing to repaint.
            }
            /*
             * Now we know the display bounding box to repaint. If this area is already in process
             * of being painted (i.e. 'paintStarted' has been invoked but not yet 'paintFinished'),
             * do not send an other 'repaint' event in order to avoid to paint the exact same thing
             * twice. This optimization occurs when a graphic changed its state after the renderer
             * started to paint but before the paint process reached the graphic. The graphic may
             * have changed its state as a result of a "scale" property change event.
             */
            if (isDirtyArea(bounds)) {
                return;
            }
            /*
             * Flush the offscreen buffers and send the repaint event. The paint method
             * will be invoked by Swing at some later, widget-dependent, time.
             */
            if (graphic != null) {
                flushOffscreenBuffer(graphic.getZOrderHint());
            } else {
                flushOffscreenBuffers();
            }
            if (owner != null) {
                if (bounds != null) {
                    owner.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
                } else {
                    owner.repaint();
                }
            }
        }
        /*
         * We are done. Log a debug string if the logging level is finest. The code below
         * is almost never run, except when investigating for performance bootleneck.
         */
        final Logger logger = getLogger();
        if (logger.isLoggable(Level.FINEST)) {
            final Logging resources = Logging.getResources(getLocale());
            final String name = (graphic!=null) ? graphic.getName()
                                                : Vocabulary.format(VocabularyKeys.UNKNOW);
            final LogRecord record;
            if (bounds != null) {
                record = resources.getLogRecord(Level.FINEST,
                                LoggingKeys.SEND_REPAINT_EVENT_$5, new Object[] {name,
                                new Integer(bounds.x), new Integer(bounds.x+bounds.width-1),
                                new Integer(bounds.y), new Integer(bounds.y+bounds.height-1)});
            } else {
                record = resources.getLogRecord(Level.FINEST,
                                LoggingKeys.SEND_REPAINT_EVENT_$1, name);
            }
            record.setSourceClassName("BufferedCanvas2D");
            record.setSourceMethodName("repaint");
            logger.log(record);
        }
    }

    /**
     * Invoked when the {@linkplain #objectiveToDisplay objective to display transform} changed.
     * This method updates cached informations like the envelope in every graphics.
     *
     * @param change The zoom <strong>change</strong> in terms of {@linkplain #getDisplayCRS
     *        display CRS}, or {@code null} if unknown. If {@code null}, then all graphics will
     *        be fully redrawn during the next rendering (i.e. all offscreen buffers are flushed).
     *
     * @see GraphicPrimitive2D#zoomChanged
     *
     * @todo Rename as {@code scaleChanged} and expect a {@code ScaleChangeEvent} argument with
     *       old and new scale, affine transform change and affine transform change scaled.
     */
    protected void zoomChanged(final AffineTransform change) {
        if (change!=null && change.isIdentity()) {
            return;
        }
        flushOffscreenBuffers();
        if (change == null) {
            // Paranoiac clean only if there is a major change in display.
            if (offscreenBuffers != null) {
                Arrays.fill(offscreenBuffers, null);
            }
        }
        super.zoomChanged(change);
    }

    /**
     * Logs a warning message when the offscreen rendering failed for some graphics.
     * The message is logged with a low level (FINE rather than WARNING) because an
     * other attempt will be done using the default rendering loop (without offscreen buffer).
     *
     * @param graphic The graphic for which the offscreen rendering failed.
     * @param exception The exception.
     */
    private void handleOffscreenException(final Graphic graphic, final Exception exception) {
        final Locale locale = getLocale();
        final LogRecord record = Logging.getResources(locale).getLogRecord(Level.FINE,
                LoggingKeys.OFFSCREEN_RENDERING_FAILED_$1, graphic.getName());
        record.setSourceClassName("BufferedCanvas2D");
        record.setSourceMethodName("paint");
        record.setThrown(exception);
        getLogger().log(record);
    }

    /**
     * Returns the offscreen buffer type for the given {@linkplain Graphic#getZOrderHint z-order}.
     * This is the value of the {@code type} argument given to the last call to
     * {@link #setOffscreenBuffered setOffscreenBuffered(...)} for a range that contains
     * the supplied {@code zOrder} value.
     *
     * @param  zOrder The z-order to query.
     * @return One of {@link ImageType#NONE}, {@link ImageType#VOLATILE} or
     *         {@link ImageType#BUFFERED} enumeration.
     */
    public synchronized ImageType getOffscreenBuffered(final double zOrder) {
        if (offscreenZRanges != null) {
            final int index = offscreenZRanges.indexOfRange(new Double(zOrder));
            if (index >= 0) {
                return offscreenIsVolatile[index] ? ImageType.VOLATILE : ImageType.BUFFERED;
            }
        }
        return ImageType.NONE;
    }

    /**
     * Set the offscreen buffer type for the range that contains the given
     * {@linkplain Graphic#getZOrderHint z-order}.
     */
    private void setOffscreenBuffered(final double zOrder, final ImageType type) {
        final int index = offscreenZRanges.indexOfRange(new Double(zOrder));
        if (index >= 0) {
            offscreenIsVolatile[index] = ImageType.VOLATILE.equals(type);
        }
    }

    /**
     * Enables or disables the use of offscreen buffer for all {@linkplain Graphic graphics}
     * in the given range of {@linkplain Graphic#getZOrderHint z-orders}. When enabled, all
     * graphics in the given range will be rendered once in an offscreen buffer (for example an
     * {@link VolatileImage}); the image will then been reused as much as possible. The offscreen
     * buffer may be invalidate at any time by some external event (including a call to any of
     * {@link Graphic#refresh()} methods) and will be recreated as needed. Using offscreen
     * buffer for background graphics that do not change often (e.g. a background map) help to make
     * the GUI more responsive to frequent changes in foreground graphics (e.g. a glass pane with
     * highlighted selections).
     * <p>
     * An arbitrary amount of ranges can be specified. Each <strong>distinct</strong> range will
     * use its own offscreen buffer. This means that if this method is invoked twice for enabling
     * buffering in overlapping range of z-values, then the union of the two ranges will shares
     * the same offscreen image.
     *
     * @param lower The lower z-order, inclusive.
     * @param upper The upper z-order, inclusive.
     * @param type  {@link ImageType#VOLATILE} or {@link ImageType#BUFFERED} for enabling offscreen
     *              buffering for the specified range, or {@link ImageType#NONE} for disabling it.
     */
    public synchronized void setOffscreenBuffered(final double lower,
                                                  final double upper,
                                                  ImageType type)
    {
        /*
         * Save the references to the old images and their status (type, need repaint, etc.).
         * We will try to reuse existing images after the range set has been updated.
         */
        final Map       oldIndexMap;
        final Image[]   oldBuffers = offscreenBuffers;
        final boolean[] oldTypes   = offscreenIsVolatile;
        final boolean[] oldNeeds   = offscreenNeedRepaint;
        if (offscreenZRanges == null) {
            if (ImageType.NONE.equals(type)) {
                return;
            }
            offscreenZRanges = new RangeSet(Double.class);
            oldIndexMap      = Collections.EMPTY_MAP;
        } else {
            int index=0;
            oldIndexMap = new HashMap();
            for (final Iterator it=offscreenZRanges.iterator(); it.hasNext();) {
                if (oldIndexMap.put(it.next(), new Integer(index++)) != null) {
                    throw new AssertionError(); // Should not happen
                }
            }
            assert index == offscreenBuffers.length : index;
        }
        /*
         * Update the range set, and rebuild the offscreen buffers array.
         */
        final ImageType lowerType;
        final ImageType upperType;
        if (ImageType.NONE.equals(type)) {
            lowerType = getOffscreenBuffered(lower);
            upperType = getOffscreenBuffered(upper);
            offscreenZRanges.remove(lower, upper);
        } else {
            lowerType = upperType = type;
            offscreenZRanges.add(lower, upper);
        }
        offscreenBuffers     = new Image[offscreenZRanges.size()];
        offscreenIsVolatile  = new boolean[offscreenBuffers.length];
        offscreenNeedRepaint = new boolean[offscreenBuffers.length];
        int index = 0;
        for (final Iterator it=offscreenZRanges.iterator(); it.hasNext(); index++) {
            final Integer oldInteger = (Integer) oldIndexMap.remove(it.next());
            if (oldInteger != null) {
                final int oldIndex = oldInteger.intValue();
                offscreenBuffers    [index] = oldBuffers[oldIndex];
                offscreenIsVolatile [index] = oldTypes  [oldIndex];
                offscreenNeedRepaint[index] = oldNeeds  [oldIndex];
            }
        }
        assert index == offscreenBuffers.length : index;
        setOffscreenBuffered(lower, lowerType);
        setOffscreenBuffered(upper, upperType);
        /*
         * Release resources used by remaining (now unused) images.
         */
        for (final Iterator it=oldIndexMap.values().iterator(); it.hasNext();) {
            final Image image = oldBuffers[((Integer) it.next()).intValue()];
            if (image != null) {
                image.flush();
            }
        }
    }

    /**
     * Signal that a graphic at the given {@linkplain Graphic#getZOrderHint z-order} need
     * to be repainted. If an offscreen buffer were allocated for this z-order, it may be
     * flushed.
     *
     * @param zOrder The z-order of the offscreen buffer to flush.
     */
    private void flushOffscreenBuffer(final double zOrder) {
        if (offscreenZRanges != null) {
            final int index = offscreenZRanges.indexOfRange(new Double(zOrder));
            if (index >= 0) {
                offscreenNeedRepaint[index] = true;
            }
        }
    }

    /**
     * Flush all offscreen buffers.
     */
    private void flushOffscreenBuffers() {
        assert Thread.holdsLock(this);
        if (offscreenBuffers != null) {
            for (int i=0; i<offscreenBuffers.length; i++) {
                final Image image = offscreenBuffers[i];
                if (image != null) {
                    image.flush();
                }
            }
            Arrays.fill(offscreenNeedRepaint, true);
        }
    }

    /**
     * Returns extended information to print for the given graphic in the {@link #toString} method.
     */
    String toStringExt(final Graphic graphic) {
        final ImageType type = getOffscreenBuffered(graphic.getZOrderHint());
        return ImageType.NONE.equals(type) ? super.toStringExt(graphic) : type.name();
    }

    /**
     * Clears all cached data. Invoking this method may help to release some resources for other
     * applications. It should be invoked when we know that the map is not going to be rendered
     * for a while. Note that this method doesn't changes the renderer setting; it will just slow
     * down the first rendering after this method call.
     */
    protected void clearCache() {
        flushOffscreenBuffers();
        if (offscreenBuffers != null) {
            Arrays.fill(offscreenBuffers, null);
        }
        statistics = null;
        super.clearCache();
    }

    /**
     * Method that may be called when a {@code Canvas} is no longer needed. The results
     * of referencing a canvas or any of its graphics after a call to {@code dispose()}
     * are undefined.
     */
    public void dispose() {
        flushOffscreenBuffers();
        offscreenZRanges     = null;
        offscreenBuffers     = null;
        offscreenIsVolatile  = null;
        offscreenNeedRepaint = null;
        super.dispose();
    }
}
