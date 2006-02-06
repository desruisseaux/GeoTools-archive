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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.display.canvas;

// J2SE dependencies
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

// Java2D dependencies for javadoc only (except RenderingHints).
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.GraphicsConfiguration;
import java.awt.geom.AffineTransform;

// OpenGIS dependencies
import org.opengis.util.InternationalString;
import org.opengis.go.display.DisplayFactory;
import org.opengis.go.display.canvas.Canvas;
import org.opengis.go.display.canvas.CanvasHandler;
import org.opengis.go.display.canvas.CanvasListener;
import org.opengis.go.display.event.EventManager;
import org.opengis.go.display.primitive.Graphic;
import org.opengis.referencing.cs.CartesianCS;    // For javadoc
import org.opengis.referencing.cs.SphericalCS;    // For javadoc
import org.opengis.referencing.crs.DerivedCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.operation.IncompatibleOperationException;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.Envelope;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Logging;
import org.geotools.resources.i18n.LoggingKeys;
import org.geotools.resources.UnmodifiableArrayList;
import org.geotools.display.primitive.AbstractGraphic;


/**
 * Manages the display and user manipulation of {@link Graphic} instances. A newly constructed
 * {@code Canvas} is initially empty. To make something appears, {@link Graphic}s must be added
 * using one of {@code add(...)} methods. The visual content depends of the {@code Graphic}
 * subclass. The contents are usually symbols, features or images, but some implementations
 * can also manage non-geographic elements like a map scale.
 * <p>
 * This base class and the more specialized {@link ReferencedCanvas} subclass do not assume a
 * two-dimensional rendering. Those same base classes could be used for a 3D rendering engine
 * as well. {@link ReferencedCanvas2D} is the first subclass targeting specifically 2D rendering.
 * <p>
 * In Geotools implementation, there is four {@linkplain CoordinateReferenceSystem coordinate
 * reference systems} involved in rendering. {@code AbstractCanvas} declares abstract methods
 * for three of them, but the actual CRS management is performed in the {@link ReferencedCanvas}
 * subclass. The CRS are enumerated below (arrows are {@linkplain MathTransform transforms}):
 * 
 * <p align="center">                       data CRS  &nbsp; <img src="doc-files/right.png">
 * &nbsp; {@linkplain #getObjectiveCRS objective CRS} &nbsp; <img src="doc-files/right.png">
 * &nbsp; {@linkplain #getDisplayCRS     display CRS} &nbsp; <img src="doc-files/right.png">
 * &nbsp; {@linkplain #getDeviceCRS       device CRS}
 * </p>
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class AbstractCanvas extends DisplayObject implements Canvas {
    /**
     * The finest rendering resolution in unit of {@linkplain #getDisplayCRS display CRS}.
     * In <cite>Java2D</cite>, those units are usually dots (1/72 of inch).
     * If a geometry to be rendered has a finer resolution, it will be decimated in order to
     * speed up the rendering. By convention, a resolution of 0 means the finest resolution
     * available.
     */
    public static final RenderingHints.Key FINEST_RESOLUTION = new HintKey(0, Number.class);

    /**
     * The required rendering resolution in unit of {@linkplain #getDisplayCRS display CRS}.
     * In <cite>Java2D</cite>, those units are usually dots (1/72 of inch).
     * If a geometry has been decimated to a worst resolution, it will be resampled in order to get
     * a more acceptable resolution. This value should be greater than {@link #FINEST_RESOLUTION}.
     */
    public static final RenderingHints.Key REQUIRED_RESOLUTION = new HintKey(1, Number.class);

    /**
     * {@link Boolean#TRUE} if the canvas is allowed to prefetch data before to paint graphics.
     * Prefetching data may speed up rendering on machine with more than one processor. If this
     * hint is not provided, then the canvas shall prefetch data if and only if the machine has
     * at least two processors.
     *
     * @see PlanarImage#prefetchTiles
     * @see Runtime#availableProcessors
     */
    public static final RenderingHints.Key PREFETCH = new HintKey(2, Boolean.class);

    /**
     * A comparator for sorting {@link Graphic} objects by increasing <var>z</var> order.
     */
    private static final Comparator COMPARATOR = new Comparator() {
        public int compare(final Object graphic1, final Object graphic2) {
            return Double.compare(((Graphic)graphic1).getZOrderHint(),
                                  ((Graphic)graphic2).getZOrderHint());
        }
    };

    /**
     * A listener to be notified when a graphic property changed.
     */
    private static final PropertyChangeListener PROPERTIES_LISTENER = new PropertyChangeListener() {
        public void propertyChange(final PropertyChangeEvent event) {
            final Object source = event.getSource();
            if (source instanceof Graphic) {
                final AbstractGraphic graphic = (AbstractGraphic) source;
                final Canvas target = ((DisplayObject) graphic).owner;
                if (target instanceof AbstractCanvas) {
                    ((AbstractCanvas) target).graphicPropertyChanged(graphic, event);
                }
            }
        }
    };

    /**
     * List of classes that provides rendering hints as public static fields.
     * This is used by {@link #toRenderingHintKey}.
     */
    private static final String[] HINT_CLASSES = {
        "java.awt.RenderingHints",
        "org.geotools.factory.Hints",
        "org.geotools.display.canvas.AbstractCanvas",
        "javax.media.jai.JAI"
    };

    /**
     * The next {@link #UID} number.
     *
     * @todo Delete and replace by {@link java.util.UUID} once we will be allowed to compile
     *       for J2SE 1.5.
     */
    private static volatile int nextUID;

    /**
     * The display factory associated with this canvas, or {@code null} if none.
     */
    private final DisplayFactory factory;

    /**
     * An unique identifier of this {@code Canvas}, which is assigned by the implementation.
     * The UID is immutable and may be used to retrieve a particular {@code Canvas} from the
     * {@link DisplayFactory}.
     */
    private final String UID;

    /**
     * The title assigned to this canvas, or {@code null} if none. It may be either an instance
     * of {@link String} or {@link InternationalString}.
     */
    private CharSequence title;

    /**
     * The set of {@link Graphic}s to display. Keys and values are identical; values are used as
     * a way to recognize existing graphics that are equals to the {@linkplain #add added} ones.
     * <p>
     * This map must preserve the order in which the user added graphics. This order must be
     * preserved no matter how {@link #sortedGraphics} reorder graphics. This is because we
     * want to preserve to {@link #add} contract even when z-value hints change.
     */
    private final Map/*<Graphic,Graphic>*/ graphics = new LinkedHashMap();

    /**
     * The set of {@link Graphic}s to display, sorted in increasing <var>z</var> value. If
     * {@code null}, then {@code Collections.sort(graphics, COMPARATOR)} need to be invoked
     * and its content copied into {@code sortedGraphics}.
     *
     * @see #getGraphics
     */
    private transient List/*<Graphic>*/ sortedGraphics;

    /**
     * A set of rendering hints.
     *
     * @see #PREFETCH
     * @see #FINEST_RESOLUTION
     * @see #REQUIRED_RESOLUTION
     * @see Hints#COORDINATE_OPERATION_FACTORY
     * @see RenderingHints#KEY_RENDERING
     * @see RenderingHints#KEY_COLOR_RENDERING
     * @see RenderingHints#KEY_INTERPOLATION
     */
    protected final Hints hints;

    /**
     * {@code true} if the canvas is allowed to prefetch data before to paint graphics.
     * Prefetching data may speed up rendering on machine with more than one processor.
     */
    private boolean prefetch = (Runtime.getRuntime().availableProcessors() >= 2);

    /**
     * The rendering resolutions, in units of {@linkplain #getDisplayCRS display CRS}
     * (usually 1/72 of inch). A larger resolution speed up rendering, while a smaller
     * resolution draw more precise map. The value can be set with {@link #setImplHint}.
     */
    private double minResolution, maxResolution;

    /**
     * {@code true} if this canvas has {@value #GRAPHICS_PROPERTY} properties listeners.
     *
     * @see #listenersChanged
     */
    private boolean hasGraphicsListeners;

    /**
     * Creates an initially empty canvas.
     *
     * @param factory The display factory associated with this canvas, or {@code null} if none.
     * @param hints   The initial set of hints, or {@code null} if none.
     */
    public AbstractCanvas(final DisplayFactory factory, final Hints hints) {
        this.factory   = factory;
        this.UID       = Utilities.getShortClassName(this) + '-' + String.valueOf(++nextUID);
        this.hints     = new Hints(hints);
    }

    /**
     * Returns the display factory associated with this canvas, or {@code null} if none.
     */
    public DisplayFactory getFactory() {
        return factory;
    }

    /**
     * Returns the unique identifier of this {@code Canvas}, which is assigned by the
     * implementation. The UID is immutable and may be used to retrieve a particular
     * {@code Canvas} from the {@link DisplayFactory}.
     * <p>
     * <strong>Note:</strong> In current implementation, a UID is valid only during the
     * current execution of the Java Virtual Machine. There is no warranty that UID will
     * stay the same in an other execution of the same program. It may also change in future
     * Geotools versions.
     * 
     * @return the UID of this {@code Canvas}.
     */
    public String getUID() {
        return UID;
    }

    /**
     * Returns the title assigned to this {@code Canvas}, or {@code null} if none. If the title
     * was {@linkplain #setTitle(InternationalString) defined as an international string}, then
     * this method returns the title in the {@linkplain #getLocale current locale}.
     */
    public synchronized String getTitle() {
        return (title instanceof InternationalString) ?
            ((InternationalString) title).toString(getLocale()) : (String) title;
    }

    /**
     * Sets the title of this {@code Canvas}. The title of a {@code Canvas}
     * may or may not be displayed on the titlebar of an application's window.
     * <p>
     * This method fires a {@value #TITLE_PROPERTY}
     * {@linkplain PropertyChangeEvent property change event}.
     */
    public void setTitle(final String title) {
        final CharSequence old;
        synchronized (this) {
            old = this.title;
            this.title = title;
        }
        listeners.firePropertyChange(TITLE_PROPERTY, old, title);
    }

    /**
     * Sets the localized title of this {@code Canvas}. The title of a {@code Canvas}
     * may or may not be displayed on the titlebar of an application's window.
     * <p>
     * This method fires a {@value #TITLE_PROPERTY}
     * {@linkplain PropertyChangeEvent property change event}.
     */
    public void setTitle(final InternationalString title) {
        final CharSequence old;
        synchronized (this) {
            old = this.title;
            this.title = title;
        }
        listeners.firePropertyChange(TITLE_PROPERTY, old, title);
    }

    /**
     * Adds the given {@code Graphic} to this {@code Canvas}. This implementation respect the
     * <var>z</var>-order retrieved by calling {@code Graphic.getGraphicStyle().getZOrderHint()}.
     * When two added {@code Graphic}s have the same <var>z</var>-order, the most recently added
     * will be on top.
     * <p>
     * Most {@code Canvas} do not draw anything as long as at least one graphic hasn't be added.
     * In Geotools implementation, an {@link AbstractGraphic} can be added to only one
     * {@code AbstractCanvas} object. If the specified graphic has already been added to
     * an other canvas, then this method creates a clone before to add the graphic.
     * <p>
     * This method fires a {@value #GRAPHICS_PROPERTY}
     * {@linkplain PropertyChangeEvent property change event}.
     *
     * @param  graphic Graphic to add to this canvas. This method call will be ignored if
     *         {@code graphic} has already been added to this canvas.
     * @return The graphic added. This is usually the supplied graphic, but may also be a
     *         new one if this method cloned the graphic.
     * @throws IllegalArgumentException If {@code graphic} has already been added to an other
     *         {@code Canvas} and the graphic is not cloneable.
     *
     * @see #remove
     * @see #removeAll
     * @see #getGraphics
     */
    public Graphic add(Graphic graphic) throws IllegalArgumentException {
        final List oldGraphics;
        synchronized (this) {
            oldGraphics = sortedGraphics; // May be null.
            if (graphic instanceof AbstractGraphic) {
                AbstractGraphic candidate = (AbstractGraphic) graphic;
                if (((DisplayObject) candidate).owner == this) {
                    /*
                     * The supplied graphic is already part of this canvas.
                     * There is nothing to do.
                     */
                    assert graphics.containsKey(candidate) : candidate;
                    return candidate;
                }
                assert !graphics.containsKey(candidate) : candidate;
                if (((DisplayObject) candidate).owner != null) try {
                    graphic = candidate = (AbstractGraphic) ((DisplayObject) candidate).clone();
                } catch (CloneNotSupportedException e) {
                    throw new IllegalArgumentException(
                            Errors.format(ErrorKeys.CANVAS_NOT_OWNER_$1, graphic.getName()));
                    // TODO: Add the cause when we will be allowed to compile for J2SE 1.5.
                }
                ((DisplayObject) candidate).owner = this;
                candidate.addPropertyChangeListener(PROPERTIES_LISTENER);
            }
            /*
             * Add the new graphic in the 'graphics' array. The array will growth as needed and
             * 'sortedGraphics' is set to null  so that the array will be resorted when needed.
             * If an identical graphic (in the sense of Object.equals(....)) existed prior this
             * method call, then the previous graphic instance will be kept (instead of the new
             * supplied one) but reordered as if it was just added.
             */
            final Graphic previous = (Graphic) graphics.put(graphic, graphic);
            if (previous != null) {
                graphic = previous;
                graphics.put(graphic, graphic);
            }
            sortedGraphics = null;
            graphic.setVisible(true);
        }
        if (hasGraphicsListeners) {
            listeners.firePropertyChange(GRAPHICS_PROPERTY, oldGraphics, null);
        }
        return graphic;
    }

    /**
     * Adds the given {@code Graphic} to this {@code Canvas}, immediately placing the
     * {@code Graphic} in an editing/drawing mode.  A {@code Graphic} added as editable
     * may or may not be visible when it is added, as it may wait for user input to define
     * the {@code Graphic}'s values through mouse gestures or key input.
     */
    public Graphic addAsEditable(Graphic graphic) {
        graphic = add(graphic);
        graphic.setAutoEdit(true);
        return graphic;
    }

    /**
     * Removes the given {@code Graphic} from this {@code Canvas}. Note that if the graphic is
     * going to be added back to the same canvas later, then it is more efficient to invoke
     * <code>{@link Graphic#setVisible Graphic.setVisible}(false)</code> instead.
     * <p>
     * This method fires a {@value #GRAPHICS_PROPERTY}
     * {@linkplain PropertyChangeEvent property change event}.
     *
     * @param  graphic The graphic to remove. This method call will be ignored if {@code graphic}
     *         has already been removed from this canvas.
     * @throws IllegalArgumentException If {@code graphic} is owned by an other {@code Canvas}
     *         than {@code this}.
     *
     * @see #add
     * @see #removeAll
     * @see #getGraphics
     */
    public void remove(final Graphic graphic) throws IllegalArgumentException {
        final List oldGraphics;
        synchronized (this) {
            oldGraphics = sortedGraphics; // May be null.
            if (graphic instanceof AbstractGraphic) {
                final AbstractGraphic candidate = (AbstractGraphic) graphic;
                if (((DisplayObject) candidate).owner == null) {
                    assert !graphics.containsKey(candidate) : candidate;
                    return;
                }
                if (((DisplayObject) candidate).owner != this) {
                    assert !graphics.containsKey(candidate) : candidate;
                    throw new IllegalArgumentException(Errors.format(
                                ErrorKeys.CANVAS_NOT_OWNER_$1, candidate.getName()));
                }
                assert Thread.holdsLock(candidate.getTreeLock());
                candidate.setVisible(false);
                candidate.removePropertyChangeListener(PROPERTIES_LISTENER);
                ((DisplayObject) candidate).clearCache();
                ((DisplayObject) candidate).owner = null;
            } else {
                if (!graphics.containsKey(graphic)) {
                    return;
                }
                graphic.setVisible(false);
            }
            if (graphics.remove(graphic) != graphic) {
                throw new AssertionError(graphic); // Should never happen.
            }
        }
        if (hasGraphicsListeners) {
            listeners.firePropertyChange(GRAPHICS_PROPERTY, oldGraphics, null);
        }
    }

    /**
     * Remove all graphics from this canvas.
     * <p>
     * This method fires a {@value #GRAPHICS_PROPERTY}
     * {@linkplain PropertyChangeEvent property change event}.
     *
     * @see #add
     * @see #remove
     * @see #getGraphics
     */
    public void removeAll() {
        final List oldGraphics;
        synchronized (this) {
            oldGraphics = sortedGraphics; // May be null.
            for (final Iterator it=graphics.keySet().iterator(); it.hasNext();) {
                final Graphic graphic = (Graphic) it.next();
                graphic.setVisible(false);
                if (graphic instanceof AbstractGraphic) {
                    final AbstractGraphic candidate = (AbstractGraphic) graphic;
                    assert Thread.holdsLock(candidate.getTreeLock());
                    candidate.removePropertyChangeListener(PROPERTIES_LISTENER);
                    ((DisplayObject) candidate).clearCache();
                    ((DisplayObject) candidate).owner = null;
                }
            }
            clearCache();
        }
        if (hasGraphicsListeners) {
            listeners.firePropertyChange(GRAPHICS_PROPERTY, oldGraphics, null);
        }
    }

    /**
     * Returns all graphics in this canvas. The returned list is sorted in increasing
     * {@linkplain Graphic#getZOrderHint z-order}: element at index 0 contains the first
     * graphic to be drawn.
     * <p>
     * This method returns an unmodifiable snapshot of current canvas state.
     * {@linkplain #add Adding} or {@linkplain #remove removing} graphics will
     * not affect the content of previous list returned by previous call to this method.
     */
    public synchronized List/*<Graphic>*/ getGraphics() {
        if (sortedGraphics == null) {
            final Set keys = graphics.keySet();
            final Graphic[] list = (Graphic[]) keys.toArray(new Graphic[keys.size()]);
            Arrays.sort(list, COMPARATOR);
            sortedGraphics = new UnmodifiableArrayList(list);
        }
        assert sortedGraphics.size() == graphics.size();
        assert graphics.keySet().containsAll(sortedGraphics);
        return sortedGraphics;
    }

    /**
     * Invoked automatically when a graphic registered in this canvas changed. Subclasses can
     * override this method if they need to react to some graphic change events, but should
     * always invoke {@code super.graphicPropertyChanged(graphic, event)}.
     *
     * @param graphic The graphic that changed.
     * @param event   The property change event.
     *
     * @todo Need to register only for the properties of interests.
     */
    protected void graphicPropertyChanged(final AbstractGraphic graphic,
                                          final PropertyChangeEvent event)
    {
        final String propertyName = event.getPropertyName();
        if (propertyName.equalsIgnoreCase("zOrderHint")) {
            synchronized (this) {
                sortedGraphics = null; // Will force a new sorting according z-order.
            }
            return;
        }
    }

    /**
     * Adds the {@code EventManager} subinterface if it not currently in
     * the {@code Canvas}'s collection of {@code EventManager}s.
     *
     * @todo Not yet implemented.
     */
    public void addEventManager(final EventManager eventManager) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the {@code EventManager} subinterface, based on the class type.
     * 
     * @param  type The class type of the {@code EventManager} subinterface.
     * @return a class that implements the requested {@code EventManager} subinterface,
     *         or {@code null} if there is no implementing class.
     *
     * @todo Not yet implemented.
     */
    public EventManager findEventManager(final Class type) {
        return null;
    }

    /**
     * Method that may be called when the {@code EventManager}s of a {@code Canvas} are no longer
     * needed. Implementations may use this method to release resources or to return the object to
     * an object pool. It is an error to reference any {@code EventManager}s of a {@code Canvas}
     * after this method has been called.
     *
     * @todo Not yet implemented.
     */
    public void disposeEventManagers() {
    }

    /**
     * Adds the given listener that will be notified when the state of this
     * {@code Canvas} has changed.
     *
     * @todo Not yet implemented.
     */
    public void addCanvasListener(final CanvasListener listener) {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes the given listener.
     *
     * @todo Not yet implemented.
     */
    public void removeCanvasListener(final CanvasListener listener) {
        throw new UnsupportedOperationException();
    }

    /**
     * Enables the given {@code CanvasHandler}, removing the current handler (if any).
     *
     * @todo Not yet implemented.
     */
    public void enableCanvasHandler(final CanvasHandler handler) {
//      if (handler != activeHandler) {
//          if (activeHandler != null) {
//              removeCanvasHandler(activeHandler);
//          }
//          activeHandler = handler;
//          activeController = new CanvasController(this);
//          activeHandler.handlerEnabled(activeController);
//      }
        throw new UnsupportedOperationException();
    }

    /**
     * Removes the given {@code CanvasHandler} from this {@code Canvas}.
     *
     * @todo Not yet implemented.
     */
    public void removeCanvasHandler(CanvasHandler handler) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the currently active {@code CanvasHandler} or null if no handler is active.
     *
     * @todo Not yet implemented.
     */
    public CanvasHandler getActiveCanvasHandler() {
        return null;
    }

    /**
     * Synonymous for {@link #getDisplayCRS} provided for the {@link Canvas} contract.
     */
    public final CoordinateReferenceSystem getDisplayCoordinateReferenceSystem() {
        final DerivedCRS displayCRS = getDisplayCRS();
        assert Utilities.equals(displayCRS.getBaseCRS(), getObjectiveCRS()) : displayCRS;
        assert Utilities.equals(displayCRS.getCoordinateSystem(),
                            getDeviceCRS().getCoordinateSystem()) : displayCRS;
        return displayCRS;
    }

    /**
     * Synonymous for {@link #getObjectiveCRS} provided for the {@link Canvas} contract.
     */
    public final CoordinateReferenceSystem getObjectiveCoordinateReferenceSystem() {
        return getObjectiveCRS();
    }

    /**
     * Synonymous for <code>{@linkplain #setObjectiveCRS setObjectiveCRS}(crs)</code> provided
     * for the {@link Canvas} contract.
     *
     * @deprecated Use {@link #setObjectiveCRS} instead. A coordinate transformation may fails,
     *             but unfortunatly the method signature for this method can't declare a
     *             {@link TransformException} for such case.
     */
    public final void setObjectiveCoordinateReferenceSystem(final CoordinateReferenceSystem crs) {
        try {
            setObjectiveCRS(crs);
        } catch (TransformException e) {
            // TODO: Add the cause when we will be allowed to compile for J2SE 1.5.
            throw new IllegalArgumentException(e.getLocalizedMessage());
        }
    }

    /**
     * Sets the objective Coordinate Reference System for this {@code Canvas}.
     *
     * @param  crs the objective Coordinate Reference System
     * @param  objectiveToDisplay the trasformation that converts between this objective Coordinate
     *         Reference System and the Canvas display Coordinate Reference System.
     * @param  displayToObjective the trasformation that converts between the Canvas display
     *         Coordinate Reference System and this objective Coordinate Reference System.
     * @throws IncompatibleOperationException when the specified transformation does not apply to
     *         either the objective or the display Coordinate Reference Systems.
     *
     * @deprecated Use {@link #setObjectiveCRS setObjectiveCRS} followed by
     *             {@link #setObjectiveToDisplayTransform setObjectiveToDisplayTransform} instead.
     *             The former is typically invoked only once, while the later is invoked everytime
     *             the zoom changes.
     */
    public void setObjectiveCoordinateReferenceSystem(final CoordinateReferenceSystem crs,
                                                      final MathTransform objectiveToDisplay, 
                                                      final MathTransform displayToObjective)
            throws IncompatibleOperationException
    {
        try {
            if (objectiveToDisplay.equals(displayToObjective.inverse())) {
                throw new IncompatibleOperationException(null, null);
            }
        } catch (NoninvertibleTransformException exception) {
            throw new IncompatibleOperationException(exception.getLocalizedMessage(), "inverse");
        }
        try {
            setObjectiveToDisplayTransform(objectiveToDisplay);
        } catch (TransformException e) {
            throw new IncompatibleOperationException(e.getLocalizedMessage(), "createDerivedCRS");
        }
        setObjectiveCoordinateReferenceSystem(crs);
    }

    /**
     * Returns the objective Coordinate Reference System (the projection of a georeferenced CRS)
     * for this {@code Canvas}. This is the "real world" CRS used for displaying all graphics.
     * Note that underlying data in graphic primitives don't need to be in this CRS.
     * Transformations will applied on the fly as needed at rendering time.
     */
    public abstract CoordinateReferenceSystem getObjectiveCRS();

    /**
     * Sets the objective Coordinate Reference System for this {@code Canvas}.
     *
     * @param  crs The objective coordinate reference system.
     * @throws TransformException If the data can't be transformed.
     */
    public abstract void setObjectiveCRS(final CoordinateReferenceSystem crs) throws TransformException;

    /**
     * Returns the Coordinate Reference System associated with the display of this {@code Canvas}.
     * Its {@linkplain CoordinateReferenceSystem#getCoordinateSystem coordinate system} corresponds
     * to the geometry of the display device. It is usually a {@linkplain CartesianCS cartesian} one
     * for video monitor, but may also be a {@linkplain SphericalCS spherical} one for planetarium.
     * <p>
     * When rendering on a flat screen using <cite>Java2D</cite>, axis are oriented as in the
     * {@linkplain Graphics2D Java2D space}: coordinates are in "dots" (about 1/72 of inch),
     * <var>x</var> values increasing right and <var>y</var> values increasing <strong>down</strong>.
     * <p>
     * In the Geotools implementation, the display CRS must be
     * {@linkplain DerivedCRS#getBaseCRS derived from} the
     * {@linkplain #getObjectiveCRS objective CRS}. The
     * {@linkplain DerivedCRS#getConversionFromBase conversion from base} is usually an
     * {@linkplain AffineTransform affine transform} with the scale terms proportional to the map
     * {@linkplain ReferencedCanvas#getScale scale factor}. The
     * {@linkplain AffineTransform#getScaleY y scale value} is often negative because of the
     * <var>y</var> axis oriented toward down.
     *
     * @see ReferencedCanvas#setDisplayCRS
     */
    public abstract DerivedCRS getDisplayCRS();

    /**
     * Returns the Coordinate Reference System associated with the device of this {@code Canvas}.
     * The device CRS is related to the {@linkplain #getDisplayCRS display CRS} in a device
     * dependent (but zoom independent) way.
     * <p>
     * When rendering on screen, device CRS and {@linkplain #getDisplayCRS display CRS} are usually
     * identical. Those CRS differ more often during printing, in which case the <cite>display to
     * device</cite> transform depends on the printer resolution. For example in the specific case
     * of <cite>Java2D</cite>, the {@linkplain #getDisplayCRS display CRS} is defined in such a way
     * that one display unit is approximatively equals to 1/72 of inch no matter what the printer
     * resolution is. The display CRS is then what <cite>Java2D</cite> calls {@linkplain Graphics2D
     * user space}, and the <cite>display to device</cite> transform is the {@linkplain
     * GraphicsConfiguration#getDefaultTransform transform mapping display units do device units}.
     * <p>
     * The default implementation returns the {@linkplain #getDisplayCRS display CRS}, i.e. assumes
     * that the <cite>display to device</cite> transform is the identity transform. Subclasses need
     * to override this method if they can manage device-dependent transformations. In any cases,
     * the device {@linkplain CoordinateReferenceSystem#getCoordinateSystem coordinate system} must
     * be the same one then the display coordinate system (not to be confused with coordinate
     * <em>reference</em> system).
     * <p>
     * Most users will deal with the {@linkplain #getDisplayCRS display CRS} rather than this
     * device CRS.
     *
     * @see ReferencedCanvas#setDeviceCRS
     */
    public DerivedCRS getDeviceCRS() {
        return getDisplayCRS();
    }

    /**
     * Sets the {@linkplain #getObjectiveCRS objective} to {@linkplain #getDisplayCRS display}
     * transform to the specified transform. This method is typically invoked by subclasses
     * every time the zoom change.
     * <p>
     * Note that some subclasses may require that the transform is affine.
     *
     * @param  transform The {@linkplain #getObjectiveCRS objective} to
     *         {@linkplain #getDisplayCRS display} transform.
     * @throws TransformException if the transform can not be set to the specified value.
     */
    public abstract void setObjectiveToDisplayTransform(final MathTransform transform)
            throws TransformException;

    /**
     * Returns {@code true} if this canvas is allowed to prefetch data in background thread.
     */
    final boolean allowPrefetch() {
        return prefetch;
    }

    /**
     * Returns a rendering hint.
     *
     * @param  key The hint key (e.g. {@link #FINEST_RESOLUTION}).
     * @return The hint value for the specified key, or {@code null} if none.
     */
    public synchronized Object getRenderingHint(final RenderingHints.Key key) {
        return hints.get(key);
    }

    /**
     * Adds a rendering hint. Hints provides optional information used by some rendering code.
     *
     * @param key   The hint key (e.g. {@link #FINEST_RESOLUTION}).
     * @param value The hint value. A {@code null} value remove the hint.
     *
     * @see #FINEST_RESOLUTION
     * @see #REQUIRED_RESOLUTION
     * @see #PREFETCH
     * @see Hints#COORDINATE_OPERATION_FACTORY
     * @see RenderingHints#KEY_RENDERING
     * @see RenderingHints#KEY_COLOR_RENDERING
     * @see RenderingHints#KEY_INTERPOLATION
     */
    public synchronized void setRenderingHint(final RenderingHints.Key key, final Object value) {
        if (value != null) {
            if (!value.equals(hints.put(key, value))) {
                clearCache(); // Invokes only if the new value is different than the previous one.
            }
        } else {
            if (hints.remove(key) != null) {
                clearCache();
            }
        }
        if (FINEST_RESOLUTION.equals(key)) {
            if (value != null) {
                minResolution = ((Number) hints.get(key)).floatValue();
                if (minResolution >= 0) {
                    if (minResolution > maxResolution) {
                        maxResolution = minResolution;
                    }
                    return;
                }
            }
            minResolution = 0;
            return;
        }
        if (REQUIRED_RESOLUTION.equals(key)) {
            if (value != null) {
                maxResolution = ((Number) hints.get(key)).floatValue();
                if (maxResolution >= 0) {
                    if (maxResolution < minResolution) {
                        minResolution = maxResolution;
                    }
                    return;
                }
            }
            maxResolution = minResolution;
            return;
        }
        if (PREFETCH.equals(key)) {
            if (value != null) {
                prefetch = ((Boolean) value).booleanValue();
            } else {
                prefetch = (Runtime.getRuntime().availableProcessors() >= 2);
            }
            return;
        }
    }

    /**
     * Returns the rendering hint associated with the hint name. The default implementation looks
     * for a rendering hint key of the given name in some known classes like {@link RenderingHints}
     * and {@link javax.media.jai.JAI}, and invokes {@link #getRenderingHint} with that key.
     * 
     * @param  name the name of the hint.
     * @return The hint value for the specified key, or {@code null} if none.
     */
    public Object getImplHint(final String name) throws IllegalArgumentException {
        return getRenderingHint(toRenderingHintKey(name, "getImplHint"));
    }

    /**
     * Sets a rendering hint for implementation or platform specific rendering information.
     * The default implementation looks for a rendering hint key of the given name in some
     * known classes like {@link RenderingHints} and {@link javax.media.jai.JAI}, and invokes
     * {@link #getRenderingHint} with that key. For example the two following method calls are
     * close to equivalent:
     * <p>
     * <ol>
     *   <li><pre>setRenderingHint({@linkplain javax.media.jai.JAI#KEY_TILE_CACHE}, value);</pre></li>
     *   <li><pre>setImplHint("KEY_TILE_CACHE", value);</pre></li>
     * </ol>
     * <p>
     * The main differences are that approach 1 is more type-safe but will fails on a machine
     * without JAI installation, while approach 2 is not type-safe but will silently ignore
     * the hint on a machine without JAI installation. Likewise, a user can write for example
     * <code>setImplHint("FINEST_RESOLUTION", value)</code> for setting the {@link #FINEST_RESOLUTION}
     * hint without dependency to the {@code AbstractCanvas} Geotools implementation.
     *
     * @param name  the name of the hint.
     * @param value The hint value. A {@code null} value remove the hint.
     */
    public void setImplHint(final String name, final Object value) {
        final RenderingHints.Key key = toRenderingHintKey(name, "setImplHint");
        if (key != null) {
            setRenderingHint(key, value);
        } else {
            getLogger().fine(Logging.getResources(getLocale()).getString(
                    LoggingKeys.HINT_IGNORED_$1, name));
        }
    }

    /**
     * Returns the rendering hint key for the specified name.
     *
     * @param  name       The key name.
     * @param  methodName The caller name, for logging purpose only.
     * @return A rendering hint key of the given name, or {@code null}
     *         if no key were found for the given name.
     */
    private static RenderingHints.Key toRenderingHintKey(String name, final String methodName) {
        if (true) {
            /*
             * Converts the name in upper case, adding '_' as needed.
             * For example "someName" will be converted as "SOME_NAME".
             */
            final int length = name.length();
            final StringBuffer buffer = new StringBuffer(length);
            for (int i=0; i<length; i++) {
                char c = name.charAt(i);
                if (Character.isUpperCase(c)) {
                    if (i!=0 && Character.isLowerCase(name.charAt(i-1))) {
                        buffer.append('_');
                    }
                } else {
                    c = Character.toUpperCase(c);
                }
                buffer.append(c);
            }
            name = buffer.toString();
        }
        /*
         * Now searchs for the public static constants defined in some known classes.
         */
        for (int i=0; i<HINT_CLASSES.length; i++) {
            try {
                return (RenderingHints.Key) Class.forName(HINT_CLASSES[i]).getField(name).get(null);
            } catch (Exception e) {
                /*
                 * May be SecurityException, ClassNotFoundException, NoSuchFieldException,
                 * IllegalAccessException, NullPointerException, ClassCastException and more...
                 * We ignore all of them and just try the next class.
                 */
                final LogRecord record = new LogRecord(Level.FINEST, name);
                record.setSourceClassName("AbstractCanvas");
                record.setSourceMethodName(methodName);
                record.setThrown(e);
                LOGGER.log(record);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    protected void listenersChanged() {
        super.listenersChanged();
        hasGraphicsListeners = listeners.hasListeners(GRAPHICS_PROPERTY);
    }

    /**
     * Invoked when an unexpected exception occured. This exception may happen while a rendering
     * is in process, so this method should not popup any dialog box and returns fast. The default
     * implementation sends a record to the {@linkplain #getLogger() logger}.
     *
     * @param  sourceClassName  The caller's class name, for logging purpose.
     * @param  sourceMethodName The caller's method name, for logging purpose.
     * @param  exception        The exception.
     */
    protected void handleException(final String sourceClassName,
                                   final String sourceMethodName,
                                   final Exception exception)
    {
        Utilities.unexpectedException(getLogger().getName(),
                sourceClassName, sourceMethodName, exception);
    }

    /**
     * Clears all cached data. Invoking this method may help to release some resources for other
     * applications. It should be invoked when we know that the map is not going to be rendered
     * for a while. For example it may be invoked from {@link java.applet.Applet#stop}. Note
     * that this method doesn't changes the renderer setting; it will just slow down the first
     * rendering after this method call.
     *
     * @see #dispose
     */
    protected void clearCache() {
        assert Thread.holdsLock(getTreeLock());
        final List/*<Graphic>*/ graphics = getGraphics();
        for (int i=graphics.size(); --i>=0;) {
            final Graphic graphic = (Graphic) graphics.get(i);
            if (graphic instanceof DisplayObject) {
                ((DisplayObject) graphic).clearCache();
            }
        }
        super.clearCache();
    }

    /**
     * Method that may be called when a {@code Canvas} is no longer needed. {@code AbstractCanvas}
     * defines this method to invoke {@link Graphic#dispose} for all graphics. The results
     * of referencing a canvas or any of its graphics after a call to {@code dispose()} are
     * undefined.
     * <p>
     * Subclasses may use this method to release resources or to return the object to an object
     * pool. It is an error to reference a {@code Canvas} after its dispose method has been called.
     *
     * @see AbstractGraphic#dispose
     * @see PlanarImage#dispose
     */
    public synchronized void dispose() {
        final List/*<Graphic>*/ graphics = getGraphics();
        removeAll();
        for (int i=graphics.size(); --i>=0;) {
            final Graphic graphic = (Graphic) graphics.get(i);
            graphic.dispose();
        }
        super.dispose();
    }

    /**
     * Returns a string representation of this canvas and all its {@link Graphic}s.
     * The {@linkplain BufferedCanvas2D#getOffscreenBuffered offscreen buffer type},
     * if any, appears in the right column. This method is for debugging purpose
     * only and may change in any future version.
     */
    public synchronized String toString() {
        final List/*<Graphic>*/ graphics = getGraphics();
        final String lineSeparator = System.getProperty("line.separator", "\n");
        final StringBuffer buffer = new StringBuffer(Utilities.getShortClassName(this));
        buffer.append("[\"");
        buffer.append(getTitle());
        buffer.append("\", ");
        buffer.append(graphics.size());
        buffer.append(" graphics]");
        buffer.append(lineSeparator);
        int maxLength = 0;
        final String[] names = new String[graphics.size()];
        for (int i=0; i<names.length; i++) {
            final Graphic graphic = (Graphic) graphics.get(i);
            final String name = names[i] = String.valueOf(graphic).trim();
            final int length = name.length();
            if (length > maxLength) {
                maxLength = length;
            }
        }
        for (int i=0; i<names.length; i++) {
            final Graphic graphic = (Graphic) graphics.get(i);
            buffer.append("    ");
            buffer.append(names[i]);
            final String ext = toStringExt(graphic);
            if (ext != null) {
                buffer.append(Utilities.spaces(maxLength-names[i].length() + 3));
                buffer.append('(');
                buffer.append(ext);
                buffer.append(')');
            }
            buffer.append(lineSeparator);
        }
        return buffer.toString();
    }

    /**
     * Returns extended information to print for the given graphic in the {@link #toString}
     * method. This method is mostly a hook for {@link BufferedCanvas2D}, which overrides it.
     */
    String toStringExt(final Graphic graphic) {
        return null;
    }
}
