/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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
package org.geotools.resources;

// J2SE and JAI dependencies
import java.util.Collection;
import java.util.Iterator;
import java.awt.image.RenderedImage;
import javax.media.jai.PropertySource;

// OpenGIS dependencies
import org.opengis.coverage.SampleDimension;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridGeometry;
import org.opengis.coverage.grid.GridRange;
import org.opengis.referencing.operation.MathTransform1D;
import org.opengis.spatialschema.geometry.Envelope;

// Geotools dependencies
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.InvalidGridGeometryException;
import org.geotools.coverage.grid.RenderedCoverage;
import org.geotools.geometry.GeneralEnvelope;


/**
 * A set of utilities methods for the Grid Coverage package. Those methods are not really
 * rigorous; must of them should be seen as temporary implementations.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @since 2.0
 */
public final class GCSUtilities {
    /**
     * Do not allows instantiation of this class.
     */
    private GCSUtilities() {
    }

    ///////////////////////////////////////////////////////////////////
    ////////                                                   ////////
    ////////        GridGeometry / GridRange / Envelope        ////////
    ////////                                                   ////////
    ///////////////////////////////////////////////////////////////////

    /**
     * Returns {@code true} if the specified geometry has a valid grid range.
     */
    public static boolean hasGridRange(final GridGeometry geometry) {
        if (geometry != null) try {
            return geometry.getGridRange() != null;
        } catch (InvalidGridGeometryException exception) {
            // Ignore.
        }
        return false;
    }

    /**
     * Returns {@code true} if the specified geometry
     * has a valid "grid to coordinate system" transform.
     */
    public static boolean hasTransform(final GridGeometry geometry) {
        if (geometry != null) try {
            return geometry.getGridToCoordinateSystem() != null;
        } catch (InvalidGridGeometryException exception) {
            // Ignore.
        }
        return false;
    }

    /**
     * Cast the specified grid range into an envelope. This is sometime used before to transform
     * the envelope using {@link CTSUtilities#transform(MathTransform, Envelope)}.
     */
    public static Envelope toEnvelope(final GridRange gridRange) {
        final int dimension = gridRange.getDimension();
        final double[] lower = new double[dimension];
        final double[] upper = new double[dimension];
        for (int i=0; i<dimension; i++) {
            lower[i] = gridRange.getLower(i);
            upper[i] = gridRange.getUpper(i);
        }
        return new GeneralEnvelope(lower, upper);
    }

    /**
     * Cast the specified envelope into a grid range. This is sometime used after the envelope
     * has been transformed using {@link CTSUtilities#transform(MathTransform, Envelope)}. The
     * floating point values are rounded toward the nearest integer.
     * <br><br>
     * <strong>Note about conversion of floating point values to integers:</strong><br>
     * In previous versions, we used {@link Math#floor} and {@link Math#ceil} in order to
     * make sure that the grid range encompass all the envelope (something similar to what
     * <cite>Java2D</cite> does when casting {@link java.awt.geom.Rectangle2D} to
     * {@link java.awt.Rectangle}). But it had the undesirable effect of changing image width.
     * For example the range {@code [-0.25  99.75]} were changed to {@code [-1  100]},
     * which is not what the {@link javax.media.jai.operator.AffineDescriptor Affine} operation
     * expects for instance. Rounding to nearest integer produces better results. Note that the
     * rounding mode do not alter the significiance of the "Resample" operation, since this
     * operation will respect the "grid to coordinate system" transform no matter what the
     * grid range is.
     */
    public static GridRange toGridRange(final Envelope envelope) {
        final int dimension = envelope.getDimension();
        final int[] lower = new int[dimension];
        final int[] upper = new int[dimension];
        for (int i=0; i<dimension; i++) {
            // See "note about conversion of floating point values to integers" in the JavaDoc.
            lower[i] = (int)Math.round(envelope.getMinimum(i));
            upper[i] = (int)Math.round(envelope.getMaximum(i));
        }
        return new GeneralGridRange(lower, upper);
    }




    //////////////////////////////////////////////////////////////////////
    ////////                                                      ////////
    ////////    GridCoverage / SampleDimension / RenderedImage    ////////
    ////////                                                      ////////
    //////////////////////////////////////////////////////////////////////

    /**
     * Returns {@code true} if at least one of the specified sample dimensions has a
     * {@linkplain SampleDimension#getSampleToGeophysics sample to geophysics} transform
     * which is not the identity transform.
     */
    public static boolean hasTransform(final SampleDimension[] sampleDimensions) {
        for (int i=sampleDimensions.length; --i>=0;) {
            SampleDimension sd = sampleDimensions[i];
            if (sd instanceof GridSampleDimension) {
                sd = ((GridSampleDimension) sd).geophysics(false);
            }
            MathTransform1D tr = sd.getSampleToGeophysics();
            return tr!=null && !tr.isIdentity();
        }
        return false;
    }

    /**
     * Returns {@code true} if the specified grid coverage or any of its source
     * uses the following image.
     */
    public static boolean uses(final GridCoverage coverage, final RenderedImage image) {
        if (coverage != null) {
            if (coverage instanceof RenderedCoverage) {
                if (((RenderedCoverage) coverage).getRenderedImage() == image) {
                    return true;
                }
            }
            final Collection sources = coverage.getSources();
            if (sources != null) {
                for (final Iterator it=sources.iterator(); it.hasNext();) {
                    if (uses((GridCoverage) it.next(), image)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns the visible band in the specified {@link RenderedImage} or {@link PropertySource}.
     * This method fetch the {@code "GC_VisibleBand"} property. If this property is undefined,
     * then the visible band default to the first one.
     *
     * @param  image The image for which to fetch the visible band, or {@code null}.
     * @return The visible band.
     */
    public static int getVisibleBand(final Object image) {
        Object candidate = null;
        if (image instanceof RenderedImage) {
            candidate = ((RenderedImage) image).getProperty("GC_VisibleBand");
        } else if (image instanceof PropertySource) {
            candidate = ((PropertySource) image).getProperty("GC_VisibleBand");
        }
        if (candidate instanceof Integer) {
            return ((Integer) candidate).intValue();
        }
        return 0;
    }
}
