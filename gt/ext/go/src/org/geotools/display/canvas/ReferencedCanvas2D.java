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

// J2SE dependencies and extensions
import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.swing.Action;
import javax.units.Unit;
import javax.units.SI;
import javax.units.NonSI;
import javax.units.ConversionException;

// OpenGIS dependencies
import org.opengis.go.display.DisplayFactory;
import org.opengis.go.display.primitive.Graphic;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.crs.DerivedCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.DirectPosition;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.i18n.Logging;
import org.geotools.resources.i18n.LoggingKeys;
import org.geotools.resources.geometry.XRectangle2D;
import org.geotools.resources.geometry.XAffineTransform;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.TransformedDirectPosition;
import org.geotools.referencing.operation.matrix.Matrix3;
import org.geotools.display.event.ReferencedEvent;


/**
 * A canvas implementation with default support for two-dimensional CRS management. This
 * default implementation uses <cite>Java2D</cite> geometry objects like {@link Shape} and
 * {@link AffineTransform}, which are somewhat lightweight objects. There is no dependency
 * toward AWT toolkit in this class (which means that this class can be used as a basis for
 * SWT renderer as well), and this class does not assume a rectangular widget.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class ReferencedCanvas2D extends ReferencedCanvas {
    /**
     * The affine transform from the {@linkplain #getObjectiveCRS objective CRS} to the
     * {@linkplain #getDisplayCRS display CRS}. This transform is zoom dependent, but device
     * independent.
     * <p>
     * If a subclass changes the values provided in this matrix, then it should invokes
     * {@link #updateObjectiveToDeviceTransforms} in order to reflect those changes into
     * this canvas CRS, and <code>{@linkplain #zoomChanged zoomChanged}(change)</code>
     * in order to notify listeners.
     *
     * @see #getObjectiveCRS
     * @see #getDisplayCRS
     * @see #getObjectiveToDisplayTransform
     * @see #updateObjectiveToDeviceTransforms
     * @see #zoomChanged
     */
    protected final AffineTransform objectiveToDisplay = new AffineTransform();

    /**
     * The {@link #objectiveToDisplay} affine transform as a matrix.
     * Used by {@link #updateObjectiveToDeviceTransforms} only.
     */
    private final Matrix3 objectiveToDisplayMatrix = new Matrix3();

    /**
     * The affine transform from the {@linkplain #getDisplayCRS display CRS} to the {@linkplain
     * #getDeviceCRS device CRS}. This transform is set as if no clipping were performed by
     * <cite>Swing</cite>. When the output device is the screen, this transform contains
     * only <var>x</var> and <var>y</var> translation terms, which are the {@link Rectangle#x}
     * and {@link Rectangle#y} values of the {@linkplain #getDisplayBounds display bounds}
     * respectively. This transform is often the identity transform.
     * <p>
     * This transform zoom independent, but device dependent (the converse of
     * {@link #objectiveToDisplay}).
     * <p>
     * If a subclass changes the values provided in this matrix, then it should invokes
     * {@link #updateObjectiveToDeviceTransforms} in order to reflect those changes into
     * this canvas CRS.
     *
     * @see #getDisplayCRS
     * @see #getDeviceCRS
     * @see #updateObjectiveToDeviceTransforms
     */
    protected final AffineTransform displayToDevice = new AffineTransform();

    /**
     * The {@link #displayToDevice} affine transform as a matrix.
     * Used by {@link #updateObjectiveToDeviceTransforms} only.
     */
    private final Matrix3 displayToDeviceMatrix = new Matrix3();

    /**
     * The affine transform from the units used in the {@linkplain #getObjectiveCRS objective CRS}
     * to "dots" units. A dots is equals to 1/72 of inch. This transform is basically nothing else
     * than an unit conversion; This transform is used as a convenient step in the computation of a
     * realistic {@linkplain #getScale scale factor}.
     * <p>
     * This affine transform is computed automatically when {@link #setObjectiveCRS} is invoked.
     * Users don't need to change its value.
     *
     * @see #updateNormalizationFactor
     */
    protected final AffineTransform normalizeToDots = new AffineTransform();

    /**
     * The display bounds. Initially set to an infinite rectangle.
     * This field should never be null.
     */
    private Shape displayBounds = XRectangle2D.INFINITY;

    /**
     * The widget area (in {@linkplain #getDisplayCRS display coordinates}) enqueued for painting,
     * or {@code null} if no painting is in process. This field is set indirectly at the begining
     * of {@link BufferedCanvas2D#paint}, and reset to {@code null} as soon as this canvas has been
     * painted. This information is used by {@link #repaint} in order to avoid repainting twice the
     * same area.
     */
    private transient Shape dirtyArea;

    /**
     * Creates an initially empty canvas with a default CRS.
     *
     * @param factory The display factory associated with this canvas, or {@code null} if none.
     */
    protected ReferencedCanvas2D(final DisplayFactory factory) {
        super(factory, 2);
        // The following must be invoked here instead than in super-class because
        // 'normalizeToDots' is not yet assigned when the super-class constructor
        // is run.
        updateNormalizationFactor(getObjectiveCRS());
    }

    /**
     * Sets the objective Coordinate Reference System for this {@code Canvas}.
     * If the specified CRS has more than two dimensions, then it must be a
     * {@linkplain org.opengis.referencing.crs.CompoundCRS compound CRS} with
     * a two dimensional head.
     */
    public void setObjectiveCRS(final CoordinateReferenceSystem crs) throws TransformException {
        super.setObjectiveCRS(CRSUtilities.getCRS2D(crs));
    }

    /**
     * Returns a rectangle that completly encloses all {@linkplain ReferencedGraphic#getEnvelope
     * graphic envelopes} managed by this canvas. Note that there is no guarantee that the returned
     * rectangle is the smallest bounding box that encloses the canvas, only that the canvas lies
     * entirely within the indicated rectangle.
     * <p>
     * This envelope is different from
     * {@link org.geotools.display.canvas.map.DefaultMapState#getEnvelope}, since the later returns
     * an envelope that encloses only the <em>visible</em> canvas area and is scale-dependent. This
     * {@code ReferencedCanvas2D.getEnvelope2D()} method is scale-independent. Both envelopes are
     * equal if the {@linkplain #getScale scale} is choosen in such a way that all graphics can fit
     * in the {@linkplain #getDisplayBounds canvas visible area}.
     *
     * @return The envelope for this canvas in terms of {@linkplain #getObjectiveCRS objective CRS}.
     *
     * @see #getEnvelope
     * @see org.geotools.display.canvas.map.DefaultMapState#getEnvelope
     */
    public Rectangle2D getEnvelope2D() {
        return super.getEnvelope2D();
    }

    /**
     * Returns the display bounds in terms of {@linkplain #getDisplayCRS display CRS}.
     * The display shape doesn't need to be {@linkplain Rectangle rectangular}. The
     * display bounds is often {@link java.awt.Component#getBounds()}.
     * <p>
     * If the display bounds is unknown, then this method returns a shape with infinite extends.
     * This method should never returns {@code null}.
     */
    public Shape getDisplayBounds() {
        return displayBounds;
    }

    /**
     * Sets the display bounds in terms of {@linkplain #getDisplayCRS display CRS}.
     * The display shape doesn't need to be {@linkplain Rectangle rectangular}.
     * <p>
     * This method fires a {@value org.geotools.display.canvas.DisplayObject#DISPLAY_BOUNDS_PROPERTY}
     * property change event.
     */
    public synchronized void setDisplayBounds(Shape bounds) {
        if (bounds == null) {
            bounds = XRectangle2D.INFINITY;
        }
        final Shape old;
        old = displayBounds;
        displayBounds = bounds;
        listeners.firePropertyChange(DISPLAY_BOUNDS_PROPERTY, old, bounds);
    }

    /**
     * Transforms the specified rectangle from the {@linkplain #getObjectiveCRS objective CRS} to
     * the {@linkplain #getDisplayCRS display CRS} used by <cite>Java2D</cite>. The transformation
     * used is the {@link #objectiveToDisplay} affine transform, which is usually the transform
     * used the last time that the {@link BufferedCanvas2D#paint} method was invoked.
     *
     * @param  bounds The rectangle in terms of {@linkplain #getObjectiveCRS objective CRS}.
     * @return The rectangle in terms of {@linkplain #getDisplayCRS display CRS}.
     */
    protected final Rectangle objectiveToDisplay(final Rectangle2D bounds) {
        assert Thread.holdsLock(this);
        return (Rectangle) XAffineTransform.transform(objectiveToDisplay, bounds, new Rectangle());
    }

    /**
     * Advises that at least a portion of this canvas need to be repainted. This canvas will not be
     * repainted immediately, but at some later time depending on the widget implementation. This
     * {@code repaint(...)} method can be invoked from any thread; it doesn't need to be the
     * <cite>Swing</cite> thread.
     * <p>
     * Usually only one of {@code objectiveArea} and {@code displayArea} arguments is provided. If
     * both arguments are non-null, then this method repaint the {@linkplain Rectangle#add union}
     * of those rectangles in display coordinates.
     * <p>
     * This method is invoked by {@link ReferencedGraphic2D#refresh()} and usually don't need to be
     * invoked directly.
     *
     * @param graphic The graphic to repaint, or {@code null} if unknown.
     * @param objectiveArea The dirty region to repaint in terms of
     *        {@linkplain #getObjectiveCRS objective CRS}, or {@code null}.
     * @paral displayArea The dirty region to repaint in terms of
     *        {@linkplain #getDisplayCRS display CRS}, or {@code null}.
     */
    public abstract void repaint(Graphic graphic, Rectangle2D objectiveArea, Rectangle displayArea);

    /**
     * Returns {@code true} if the specified area is scheduled for painting. More specifically,
     * returns {@code true} if {@link #paintStarted} has been invoked, {@link #paintFinished} has
     * not yet been invoked, and the dirty area given to {@code paintStarted} encloses completly
     * the area given to this {@code isDirtyArea} method.
     *
     * @param  area The area to test, in terms of {@linkplain #getDisplayCRS display CRS}.
     * @return {@code true} if the specified area is already in process of being painted.
     */
    final boolean isDirtyArea(final Rectangle area) {
        assert Thread.holdsLock(this);
        if (dirtyArea == null) {
            return true;
        }
        if (area == null) {
            return dirtyArea.equals(XRectangle2D.INFINITY);
        }
        return dirtyArea.contains(area);
    }

    /**
     * Invoked when this canvas is about to be painted. Subclasses should invokes this method
     * at the begining of their {@code paint} method.
     *
     * @param dirtyArea The area which is about the be painted, in terms of
     *        {@linkplain #getDisplayCRS display CRS}.
     */
    protected void paintStarted(final Shape dirtyArea) {
        assert Thread.holdsLock(this);
        this.dirtyArea = dirtyArea;
    }

    /**
     * Invoked when this canvas painting finished, either successfully or after a failure.
     * Subclasses should invokes this method at the end of their {@code paint} method,
     * typically in a {@code finally} block.
     *
     * @param success {@code true} if the rendering has been successful, or {@code false}
     *        if a failure occured.
     */
    protected void paintFinished(final boolean success) {
        assert Thread.holdsLock(this);
        dirtyArea = null;
    }

    /**
     * Returns {@code true} if the given coordinate is visible on this {@code Canvas}. The default
     * implementation checks if the coordinate (transformed in terms of {@linkplain #getDisplayCRS
     * display CRS}) is inside the {@linkplain #getDisplayBounds display bounds}.
     */
    public synchronized boolean isVisible(final DirectPosition coordinate) {
        final GeneralDirectPosition position;
        try {
            position = toDisplayPosition(coordinate);
        } catch (TransformException e) {
            /*
             * A typical reason for transformation failure is a coordinate point outside the area
             * of validity. If the specified point is outside the area of validity of the CRS used
             * by this canvas, then we can reasonably assume that it is outside the canvas envelope
             * as well.
             */
            return false;
        }
        return getDisplayBounds().contains(position.ordinates[0],
                                           position.ordinates[1]);
    }

    /**
     * {@inheritDoc}
     */
    public boolean format(final ReferencedEvent event, final StringBuffer toAppendTo) {
        return ((Boolean) visit(new GraphicVisitor2D.Format(event, toAppendTo), event)).booleanValue();
    }

    /**
     * {@inheritDoc}
     */
    public String getToolTipText(final ReferencedEvent event) {
        return (String) visit(new GraphicVisitor2D.ToolTipText(event), event);
    }

    /**
     * {@inheritDoc}
     */
    public Action getAction(final ReferencedEvent event) {
        return (Action) visit(new GraphicVisitor2D.Action(event), event);
    }

    /**
     * Updates {@link #normalizeToDots} affine transform for the specified
     * {@linkplain #getObjectiveCRS objective coordinate reference system}.
     * This method is invoked automatically by {@link #setObjectiveCRS}.
     * Users don't need to invoke this method directly, but subclasses may
     * override it.
     *
     * @param crs The new objective CRS.
     */
    protected void updateNormalizationFactor(final CoordinateReferenceSystem crs) {
        super.updateNormalizationFactor(crs);
        final Ellipsoid ellipsoid = CRSUtilities.getHeadGeoEllipsoid(crs);
        final CoordinateSystem cs = crs.getCoordinateSystem();
        normalizeToDots.setToScale(getNormalizationFactor(cs.getAxis(0).getUnit(), ellipsoid),
                                   getNormalizationFactor(cs.getAxis(0).getUnit(), ellipsoid));
    }

    /**
     * Returns the amount of "dots" in one unit of the specified unit. There is 72 dots in one
     * inch, and 2.54/100 inchs in one metre.  The {@code unit} argument must be a linear or
     * an angular unit.
     *
     * @param unit The unit. If {@code null}, then the unit will be assumed to be metres or
     *        degrees depending of whatever {@code ellipsoid} is {@code null} or not.
     * @param ellipsoid The ellipsoid if the CRS is geographic, or {@code null} otherwise.
     */
    private double getNormalizationFactor(Unit unit, final Ellipsoid ellipsoid) {
        double m = 1;
        try {
            if (ellipsoid != null) {
                if (unit == null) {
                    unit = NonSI.DEGREE_ANGLE;
                }
                /*
                 * Converts an angular unit to a linear one.   An ellipsoid has two axis that we
                 * could use. For the WGS84 ellipsoid, the semi-major axis results in a nautical
                 * mile of 1855.32 metres  while  the semi-minor axis results in a nautical mile
                 * of 1849.10 metres. The average of semi-major and semi-minor axis results in a
                 * nautical mile of 1852.21 metres, which is pretty close to the internationaly
                 * agreed length (1852 metres). This is consistent with the definition of nautical
                 * mile, which is the length of an angle of 1 minute along the meridian at 45° of
                 * latitude.
                 */
                m = unit.getConverterTo(SI.RADIAN).convert(m) *
                        0.5*(ellipsoid.getSemiMajorAxis() + ellipsoid.getSemiMinorAxis());
                unit = ellipsoid.getAxisUnit();
            }
            if (unit != null) {
                m = unit.getConverterTo(SI.METER).convert(m);
            }
        } catch (ConversionException exception) {
            /*
             * A unit conversion failed. Since this normalizing factor is used only for computing a
             * scale, it is not crucial to the renderer working. Log a warning message and continue.
             * We keep the m value computed so far, which will be assumed to be a length in metres.
             */
            final LogRecord record = Logging.getResources(getLocale()).getLogRecord(Level.WARNING,
                                                          LoggingKeys.UNEXPECTED_UNIT_$1, unit);
            record.setSourceClassName ("ReferencedCanvas2D");
            record.setSourceMethodName("setObjectiveCRS");
            record.setThrown(exception);
            getLogger().log(record);
        }
        return 7200/2.54 * m;
    }

    /**
     * Invokes <code>{@linkplain #setObjectiveToDeviceTransforms
     * setObjectiveToDeviceTransforms}{@linkplain #objectiveToDisplay},
     * {@linkplain #displayToDevice})</code> with the current affine transform values.
     * Subclasses should invoke this method when they changed the {@link #objectiveToDisplay}
     * or the {@link #displayToDevice} affine transform. This method does nothing if the
     * current affine transform values didn't changed since the last time this method was
     * invoked.
     *
     * @return {@code true} if at least one of {@link #objectiveToDisplay} and
     *         {@link #displayToDevice} affine transforms changed since the last
     *         time this method was invoked.
     * @throws TransformException if this method failed to setup the {@linkplain #getDisplayCRS
     *         display} and {@linkplain #getDeviceCRS device} CRS from the affine transforms.
     *
     * @see #setObjectiveToDeviceTransforms
     *
     * @todo Consider deleting this method. We could add new new Matrix implementation extending
     *       {@code AffineTransform} instead.
     */
    protected boolean updateObjectiveToDeviceTransforms() throws TransformException {
        assert Thread.holdsLock(this);
        if (displayToDeviceMatrix.equalsAffine(displayToDevice)) {
            if (objectiveToDisplayMatrix.equalsAffine(objectiveToDisplay)) {
                // No change detected.
                return false;
            } else {
                // Only the objective to display transform changed.
                objectiveToDisplayMatrix.setMatrix(objectiveToDisplay);
                setObjectiveToDisplayTransform(objectiveToDisplayMatrix);
                return true;
            }
        } else {
            // The display to device transform changed too (this is less frequent).
            displayToDeviceMatrix.setMatrix(displayToDevice);
            objectiveToDisplayMatrix.setMatrix(objectiveToDisplay);
            setObjectiveToDeviceTransforms(objectiveToDisplayMatrix, displayToDeviceMatrix);
            return true;
        }
    }

    /**
     * Notifies all listeners that the {@link #objectiveToDisplay} transform changed. This change
     * is more often the consequence of some zoom action. The {@code change} argument can be
     * computed as below:
     *
     * <blockquote><pre>
     * AffineTransform change = <var>oldObjectiveToDisplay</var>.createInverse();
     * change.preConcatenate(<var>newObjectiveToTransform</var>);
     * </pre></blockquote>
     *
     * The default implementation invokes <code>{@linkplain ReferencedGraphic2D#zoomChanged
     * zoomChanged}(change)</code> for all registered {@link ReferencedGraphic2D}.
     *
     * @param change The zoom <strong>change</strong> in terms of
     *        {@linkplain #getDisplayCRS display CRS}, or {@code null} if unknown.
     *
     * @see ReferencedGraphic2D#zoomChanged
     */
    protected void zoomChanged(final AffineTransform change) {
        assert Thread.holdsLock(this);
        final List/*<Graphic>*/ graphics = getGraphics();
        for (int i=graphics.size(); --i>=0;) {
            final Graphic graphic = (Graphic) graphics.get(i);
            if (graphic instanceof ReferencedGraphic2D) {
                ((ReferencedGraphic2D) graphic).zoomChanged(change);
            }
        }
    }
}
