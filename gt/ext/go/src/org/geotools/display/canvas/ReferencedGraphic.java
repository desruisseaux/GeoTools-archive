/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
 * (C) 2005, Institut de Recherche pour le D�veloppement
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
import javax.swing.Action;

// OpenGIS dependencies
import org.opengis.go.display.canvas.Canvas;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

// Geotools dependencies
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.display.event.ReferencedEvent;
import org.geotools.referencing.FactoryFinder;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;


/**
 * A graphic implementation with default support for Coordinate Reference System (CRS) management.
 * This class provides some methods specific to the Geotools implementation of graphic primitive.
 * The {@link org.geotools.display.canvas.ReferencedCanvas} expects instances of this class.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class ReferencedGraphic extends AbstractGraphic {
    /**
     * An envelope that completly encloses the graphic. Note that there is no guarantee
     * that the returned envelope is the smallest bounding box that encloses the graphic,
     * only that the graphic lies entirely within the indicated envelope.
     * <p>
     * The {@linkplain GeneralEnvelope#getCoordinateReferenceSystem coordinate reference system}
     * of this envelope should always be the {@linkplain #getObjectiveCRS objective CRS}.
     */
    private final GeneralEnvelope envelope;

    /**
     * A typical cell dimension for this graphic, or {@code null} if none.
     *
     * @see #getTypicalCellDimension
     * @see #setTypicalCellDimension
     */
    private double[] typicalCellDimension;

    /**
     * Constructs a new graphic using the specified objective CRS.
     *
     * @param  crs The objective coordinate reference system.
     * @throws IllegalArgumentException if {@code crs} is null.
     *
     * @see #setObjectiveCRS
     * @see #setEnvelope
     * @see #setTypicalCellDimension
     * @see #setZOrderHint
     */
    protected ReferencedGraphic(final CoordinateReferenceSystem crs)
            throws IllegalArgumentException
    {
        if (crs == null) {
            throw new IllegalArgumentException(Errors.getResources(getLocale())
                      .getString(ErrorKeys.BAD_ARGUMENT_$2, "crs", crs));
        }
        envelope = new GeneralEnvelope(crs);
        envelope.setToNull();
    }

    /**
     * Returns the objective coordinate reference system.
     */
    public final CoordinateReferenceSystem getObjectiveCRS() {
        return envelope.getCoordinateReferenceSystem();
    }

    /**
     * Sets the objective coordinate refernece system for this graphic. This method is usually
     * invoked in any of the following cases:
     * <p>
     * <ul>
     *   <li>From the graphic constructor.</li>
     *   <li>When this graphic has just been added to a canvas.</li>
     *   <li>When canvas objective CRS is modified.</li>
     * </ul>
     * <p>
     * This method transforms the {@linkplain #getEnvelope envelope} if needed. If a
     * subclass need to transform some additional internal data, it should override the
     * {@link #transform} method.
     * <p>
     * This method fires a {@value org.geotools.display.canvas.DisplayObject#OBJECTIVE_CRS_PROPERTY}
     * property change event.
     *
     * @param  crs The new objective CRS.
     * @throws TransformException If this method do not accept the new CRS. In such case,
     *         this method should keep the old CRS and leaves this graphic in a consistent state.
     */
    protected void setObjectiveCRS(final CoordinateReferenceSystem crs) throws TransformException {
        if (crs == null) {
            throw new IllegalArgumentException(Errors.getResources(getLocale())
                      .getString(ErrorKeys.BAD_ARGUMENT_$2, "crs", crs));
        }
        final CoordinateReferenceSystem oldCRS;
        synchronized (getTreeLock()) {
            oldCRS = getObjectiveCRS();
            if (CRSUtilities.equalsIgnoreMetadata(oldCRS, crs)) {
                /*
                 * If the new CRS is equivalent to the old one (except for metadata), then there
                 * is no need to apply any transformation. Just set the new CRS.  Note that this
                 * step may throws an IllegalArgumentException if the given CRS doesn't have the
                 * expected number of dimensions (actually it should never happen, since we just
                 * said that this CRS is equivalent to the previous one).
                 */
                envelope.setCoordinateReferenceSystem(crs);
            } else {
                /*
                 * If a coordinate transformation is required, gets the math transform preferably
                 * from the Canvas that own this graphic (in order to use any user supplied hints).
                 */
                final MathTransform transform;
                transform = getMathTransform(oldCRS, crs, "ReferencedGraphic", "setObjectiveCRS");
                if (!transform.isIdentity()) {
                    /*
                     * Transforms the envelope, but do not modify yet the 'envelope' field.
                     * This change will be commited only after all computations have been successful.
                     */
                    final GeneralEnvelope newEnvelope;
                    final DirectPosition origin;
                    if (envelope.isNull() || envelope.isInfinite()) {
                        origin = new GeneralDirectPosition(oldCRS);
                        newEnvelope = new GeneralEnvelope(envelope);
                    } else {
                        origin = envelope.getCenter();
                        newEnvelope = CRSUtilities.transform(transform, envelope);
                    }
                    newEnvelope.setCoordinateReferenceSystem(crs);
                    /*
                     * Transforms the cell dimension. Only after all computations are successful,
                     * commit the changes to the 'envelope' and typicalCellDimension' class fields.
                     */
                    double[] cellDimension = typicalCellDimension;
                    if (cellDimension != null) {
                        DirectPosition vector = new GeneralDirectPosition(cellDimension);
                        vector = CRSUtilities.deltaTransform(transform, origin, vector);
                        cellDimension = vector.getCoordinates();
                        for (int i=0; i<cellDimension.length; i++) {
                            cellDimension[i] = Math.abs(cellDimension[i]);
                        }
                    }
                    transform(transform);
                    envelope.setEnvelope(newEnvelope);
                    typicalCellDimension = cellDimension;
                    clearCache();
                }
            }
            listeners.firePropertyChange(OBJECTIVE_CRS_PROPERTY, oldCRS, crs);
            refresh();
        }
    }

    /**
     * Constructs a transform between two coordinate reference systems.
     *
     * @param  sourceCRS The source coordinate reference system.
     * @param  targetCRS The target coordinate reference system.
     * @param  sourceClassName  The caller class name, for logging purpose only.
     * @param  sourceMethodName The caller method name, for logging purpose only.
     * @return A transform from {@code sourceCRS} to {@code targetCRS}.
     * @throws TransformException if the transform can't be created.
     */
    private MathTransform getMathTransform(final CoordinateReferenceSystem sourceCRS,
                                           final CoordinateReferenceSystem targetCRS,
                                           final String sourceClassName,
                                           final String sourceMethodName)
            throws TransformException
    {
        try {
            final Canvas owner = getCanvas();
            if (owner instanceof ReferencedCanvas) {
                return ((ReferencedCanvas) owner).getMathTransform(sourceCRS, targetCRS,
                       sourceClassName, sourceMethodName);
            } else {
                return FactoryFinder.getCoordinateOperationFactory(null)
                       .createOperation(sourceCRS, targetCRS).getMathTransform();
            }
        } catch (FactoryException exception) {
            throw new TransformException(Errors.getResources(getLocale()).format(
                        ErrorKeys.ILLEGAL_COORDINATE_REFERENCE_SYSTEM), exception);
        }
    }

    /**
     * Notifies subclasses that a new {@linkplain #getObjectiveCRS objective CRS} has been set
     * and the internal data should be transformed accordingly. This method is a hook invoked
     * automatically by {@link #setObjectiveCRS}. The default implementation does nothing.
     * Subclasses should override this method if they need to transform their internal data.
     * <p>
     * When {@link #setObjectiveCRS setObjectiveCRS} invokes this method, this {@code Graphic}
     * object still in its old state. This method should have a <cite>all or nothing</cite>
     * behavior: in case of failure, it should throws an exception and leave this {@code Graphic}
     * as if no change were applied at all.
     *
     * @param mt The math transform from the old objective CRS to the new one.
     * @throws TransformException If a transformation failed.
     */
    protected void transform(final MathTransform mt) throws TransformException {
    }

    /**
     * Returns an envelope that completly encloses the graphic. Note that there is no guarantee
     * that the returned envelope is the smallest bounding box that encloses the graphic, only
     * that the graphic lies entirely within the indicated envelope.
     * <p>
     * The default implementation returns a {@linkplain GeneralEnvelope#setToNull null envelope}.
     * Subclasses should compute their envelope and invoke {@link #setEnvelope} as soon as they can.
     *
     * @see #setEnvelope
     */
    public Envelope getEnvelope() {
        synchronized (getTreeLock()) {
            return new GeneralEnvelope(envelope);
        }
    }

    /**
     * Set the envelope for this graphic. Subclasses should invokes this method as soon as they
     * known their envelope.
     * <p>
     * This method fires a {@value org.geotools.display.canvas.DisplayObject#ENVELOPE_PROPERTY}
     * property change event.
     *
     * @throws TransformException if the specified envelope can't be transformed to the
     *         {@linkplain #getObjectiveCRS objective CRS}.
     */
    protected void setEnvelope(final Envelope newEnvelope) throws TransformException {
        final GeneralEnvelope old;
        synchronized (getTreeLock()) {
            CoordinateReferenceSystem sourceCRS = getObjectiveCRS();
            CoordinateReferenceSystem targetCRS = newEnvelope.getLowerCorner().getCoordinateReferenceSystem();
            // TODO: use a shorter path for the above if we allow that in a future GeoAPI version.
            if (targetCRS == null) {
                targetCRS = sourceCRS;
            }
            final MathTransform mt;
            mt = getMathTransform(sourceCRS, targetCRS, "ReferencedGraphic", "setEnvelope");
            old = new GeneralEnvelope(envelope);
            envelope.setEnvelope(CRSUtilities.transform(mt, newEnvelope));
            assert envelope.getCoordinateReferenceSystem() == old.getCoordinateReferenceSystem();
            listeners.firePropertyChange(ENVELOPE_PROPERTY, old, envelope);
        }
    }

    /**
     * Returns a typical cell dimension in terms of {@linkplain #getObjectiveCRS objective CRS}.
     * For images, this is the pixels size in "real world" units. For other kind of graphics, "cell
     * dimension" are to be understood as some dimension representative of the graphic resolution.
     *
     * @param  position The position where to evaluate the cell dimension. In the default
     *         implementation, this position is ignored.
     * @return A typical cell size in {@linkplain #getObjectiveCRS objective CRS},
     *         or {@code null} if none.
     */
    public double[] getTypicalCellDimension(final DirectPosition position) {
        synchronized (getTreeLock()) {
            return (typicalCellDimension!=null) ? (double[]) typicalCellDimension.clone() : null;
        }
    }

    /**
     * Set the typical cell dimension. Subclasses may invoke this method after they computed
     * some typical value. The default implementation of {@link #getTypicalCellDimension}
     * will returns this value for all positions.
     *
     * @param  size A typical cell size, in terms of objective CRS.
     * @throws MismatchedDimensionException if the specified cell size doesn't have the
     *         expected number of dimensions.
     */
    protected void setTypicalCellDimension(final double[] size)
            throws MismatchedDimensionException
    {
        synchronized (getTreeLock()) {
            if (size != null) {
                final int dimension = size.length;
                final int expectedDimension = envelope.getDimension();
                if (dimension != expectedDimension) {
                    throw new MismatchedDimensionException(Errors.getResources(getLocale()).
                              getString(ErrorKeys.MISMATCHED_DIMENSION_$3,
                              new Integer(dimension), new Integer(expectedDimension)));
                }
            }
            final double[] oldSize = typicalCellDimension;
            typicalCellDimension = (size!=null) ? (double[])size.clone() : null;
        }
    }

    /**
     * Returns the string to be used as the tooltip for a given event. The default implementation
     * always returns {@code null}. Subclasses should override this method if they can provide
     * tool tips for some location.
     *
     * @param  event The event.
     * @return The tool tip text, or {@code null} if there is no tool tip for the given location.
     *
     * @see ReferencedCanvas#getToolTipText
     */
    protected String getToolTipText(final ReferencedEvent event) {
        return null;
    }

    /**
     * Returns the action to run when some action occured over this graphic. The default
     * implementation return always {@code null}, which means that no action is defined
     * for this graphic. Subclasses which override this method should check if the event
     * is really located over a visual component of this graphic (for example over a geometry).
     *
     * @param  event The event.
     * @return The action for this graphic, or {@code null} if none.
     *
     * @see ReferencedCanvas#getAction
     */
    protected Action getAction(final ReferencedEvent event) {
        return null;
    }

    /**
     * Formats a value for the specified event position. This method doesn't have to format the
     * coordinate (this is {@link MouseCoordinateFormat#format(GeoMouseEvent)} business). Instead,
     * it is invoked for formatting a value at the specified event position. For example a remote
     * sensing image of <cite>Sea Surface Temperature</cite> (SST) can format the temperature in
     * geophysical units (e.g. "12�C"). The default implementation do nothing and returns
     * {@code false}.
     *
     * @param  event The event.
     * @param  toAppendTo The destination buffer for formatting a value.
     * @return {@code true} if this method has formatted a value, or {@code false} otherwise.
     *
     * @see ReferencedCanvas#format
     * @see MouseCoordinateFormat#format(GeoMouseEvent)
     */
    protected boolean format(final ReferencedEvent event, final StringBuffer toAppendTo) {
        return false;
    }
}
