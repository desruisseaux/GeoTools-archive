/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Management Committee (PMC)
 * (C) 2004, Institut de Recherche pour le Développement
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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.coverage.grid;

// J2SE dependencies
import java.awt.geom.Point2D;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

// JAI dependencies
import javax.media.jai.PlanarImage;
import javax.media.jai.PropertySource;
import javax.media.jai.util.CaselessStringKey;  // For javadoc

// OpenGIS dependencies
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridGeometry;
import org.opengis.coverage.grid.GridPacking;
import org.opengis.coverage.grid.GridRange;
import org.opengis.coverage.grid.GridNotEditableException;
import org.opengis.coverage.grid.InvalidRangeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.DirectPosition;

// Geotools dependencies
import org.geotools.coverage.AbstractCoverage;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;


/**
 * Base class for Geotools implementation of grid coverage.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class AbstractGridCoverage extends AbstractCoverage implements GridCoverage {
    /**
     * The logger for grid coverage operations.
     */
    public static final Logger LOGGER = Logger.getLogger("org.geotools.coverage.grid");

    /**
     * Sources grid coverage, or {@code null} if none. This information is lost during
     * serialization, in order to avoid sending a too large amount of data over the network.
     */
    private final transient List sources;

    /**
     * Constructs a grid coverage using the specified coordinate reference system. If the
     * coordinate reference system is {@code null}, then the subclasses must override
     * {@link #getDimension()}.
     *
     * @param name The grid coverage name.
     * @param crs The coordinate reference system. This specifies the coordinate
     *        system used when accessing a coverage or grid coverage with the
     *        {@code evaluate(...)} methods.
     * @param source The source for this coverage, or {@code null} if none.
     *        Source may be (but is not limited to) a {@link PlanarImage} or an
     *        other {@code AbstractGridCoverage} object.
     * @param properties The set of properties for this coverage, or {@code null} if there is none.
     *        "Properties" in <cite>Java Advanced Imaging</cite> is what OpenGIS calls "Metadata".
     *        Keys are {@link String} objects ({@link CaselessStringKey} are accepted as well),
     *        while values may be any {@link Object}.
     */
    protected AbstractGridCoverage(final CharSequence             name,
                                   final CoordinateReferenceSystem crs,
                                   final PropertySource         source,
                                   final Map                properties)
    {
        super(name, crs, source, properties);
        sources = null;
    }

    /**
     * Constructs a grid coverage with sources. Arguments are the same than for the
     * {@linkplain #AbstractGridCoverage(CharSequence,CoordinateReferenceSystem,PropertySource,Map)
     * previous constructor}, with an additional {@code sources} argument.
     *
     * @param name       The grid coverage name.
     * @param crs        The coordinate reference system.
     * @param sources    The {@linkplain #getSources source data} for a grid coverage,
     *                   or {@code null} if none.
     * @param source     The source for properties for this coverage, or {@code null} if none.
     * @param properties Set of additional properties for this coverage, or {@code null} if there
     *                   is none.
     */
    protected AbstractGridCoverage(final CharSequence             name,
                                   final CoordinateReferenceSystem crs,
                                   final GridCoverage[]        sources,
                                   final PropertySource         source,
                                   final Map                properties)
    {
        super(name, crs, source, properties);
        if (sources != null) {
            switch (sources.length) {
                case 0:  this.sources = null; break;
                case 1:  this.sources = Collections.singletonList(sources[0]); break;
                default: this.sources = Collections.unmodifiableList(
                                        Arrays.asList((GridCoverage[]) sources.clone()));
            }
        } else {
            this.sources = null;
        }
    }

    /**
     * Constructs a new coverage with the same parameters than the specified coverage.
     *
     * @param name The name for this coverage, or {@code null} for the same than {@code coverage}.
     * @param coverage The source coverage.
     */
    protected AbstractGridCoverage(final CharSequence name,
                                   final GridCoverage coverage)
    {
        super(name, coverage);
        sources = Collections.singletonList(coverage);
    }

    /**
     * Returns the source data for a grid coverage. If the {@code GridCoverage} was produced from
     * an underlying dataset, the returned list is an empty list. If the {@code GridCoverage} was
     * produced using {@link org.opengis.coverage.grid.GridCoverageProcessor}, then it should
     * return the source grid coverage of the one used as input to {@code GridCoverageProcessor}.
     * In general the {@code getSources()} method is intended to return the original
     * {@code GridCoverage} on which it depends. This is intended to allow applications
     * to establish what {@code GridCoverage}s will be affected when others are updated,
     * as well as to trace back to the "raw data".
     */
    public List getSources() {
        return (sources!=null) ? sources : Collections.EMPTY_LIST;
    }

    /**
     * Returns {@code true} if grid data can be edited. The default
     * implementation returns {@code false}.
     */
    public boolean isDataEditable() {
        return false;
    }

    /**
     * Returns the number of predetermined overviews for the grid.
     * The default implementation returns 0.
     */
    public int getNumOverviews() {
        return 0;
    }

    /**
     * Returns the grid geometry for an overview. The default implementation always throws
     * an exception, since the default {@linkplain #getNumOverviews number of overviews} is 0.
     *
     * @throws IndexOutOfBoundsException if the specified index is out of bounds.
     */
    public GridGeometry getOverviewGridGeometry(int index) throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException(indexOutOfBounds(index));
    }

    /**
     * Returns a pre-calculated overview for a grid coverage. The default implementation always
     * throws an exception, since the default {@linkplain #getNumOverviews number of overviews}
     * is 0.
     *
     * @throws IndexOutOfBoundsException if the specified index is out of bounds.
     */
    public GridCoverage getOverview(int index) throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException(indexOutOfBounds(index));
    }

    /**
     * Returns information for the packing of grid coverage values.
     * The default implementation throws an {@link UnsupportedOperationException}.
     * We don't know at this time if and when this method will be implemented, since
     * the API is going to change when we will shift to ISO 19123.
     */
    public GridPacking getGridPacking() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Returns a block of grid coverage data for all sample dimensions. 
     * The default implementation throws an {@link UnsupportedOperationException}.
     * We don't know at this time if and when this method will be implemented, since
     * the API is going to change when we will shift to ISO 19123.
     */
    public byte[] getPackedDataBlock(final GridRange range) throws InvalidRangeException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Returns a sequence of byte values for a block.
     * The default implementation throws an {@link UnsupportedOperationException}.
     * We don't know at this time if and when this method will be implemented, since
     * the API is going to change when we will shift to ISO 19123.
     */
    public boolean[] getDataBlock(final GridRange range, boolean[] destination)
            throws InvalidRangeException, ArrayIndexOutOfBoundsException
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Returns a sequence of byte values for a block.
     * The default implementation throws an {@link UnsupportedOperationException}.
     * We don't know at this time if and when this method will be implemented, since
     * the API is going to change when we will shift to ISO 19123.
     */
    public byte[] getDataBlock(final GridRange range, byte[] destination)
            throws InvalidRangeException, ArrayIndexOutOfBoundsException
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Returns a sequence of short values for a block.
     * The default implementation throws an {@link UnsupportedOperationException}.
     * We don't know at this time if and when this method will be implemented, since
     * the API is going to change when we will shift to ISO 19123.
     */
    public short[] getDataBlock(final GridRange range, short[] destination)
            throws InvalidRangeException, ArrayIndexOutOfBoundsException
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Returns a sequence of integer values for a block.
     * The default implementation throws an {@link UnsupportedOperationException}.
     * We don't know at this time if and when this method will be implemented, since
     * the API is going to change when we will shift to ISO 19123.
     */
    public int[] getDataBlock(final GridRange range, int[] destination)
            throws InvalidRangeException, ArrayIndexOutOfBoundsException
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Returns a sequence of float values for a block.
     * The default implementation throws an {@link UnsupportedOperationException}.
     * We don't know at this time if and when this method will be implemented, since
     * the API is going to change when we will shift to ISO 19123.
     */
    public float[] getDataBlock(final GridRange range, final float[] destination)
            throws InvalidRangeException, ArrayIndexOutOfBoundsException
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Returns a sequence of double values for a block.
     * The default implementation throws an {@link UnsupportedOperationException}.
     * We don't know at this time if and when this method will be implemented, since
     * the API is going to change when we will shift to ISO 19123.
     */
    public double[] getDataBlock(final GridRange range, final double[] destination)
            throws InvalidRangeException, ArrayIndexOutOfBoundsException
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Set a block of values for all sample dimensions. The default implementation always throws
     * an exception, since this grid coverage is not editable by default.
     */
    public void setDataBlock(GridRange gridRange, boolean[] values)
            throws InvalidRangeException, GridNotEditableException, ArrayIndexOutOfBoundsException
    {
        throw new GridNotEditableException(); // TODO: provides a localized message.
    }

    /**
     * Set a block of values for all sample dimensions. The default implementation always throws
     * an exception, since this grid coverage is not editable by default.
     */
    public void setDataBlock(GridRange gridRange, byte[] values)
            throws InvalidRangeException, GridNotEditableException, ArrayIndexOutOfBoundsException
    {
        throw new GridNotEditableException(); // TODO: provides a localized message.
    }

    /**
     * Set a block of values for all sample dimensions. The default implementation always throws
     * an exception, since this grid coverage is not editable by default.
     */
    public void setDataBlock(GridRange gridRange, short[] values)
            throws InvalidRangeException, GridNotEditableException, ArrayIndexOutOfBoundsException
    {
        throw new GridNotEditableException(); // TODO: provides a localized message.
    }

    /**
     * Set a block of values for all sample dimensions. The default implementation always throws
     * an exception, since this grid coverage is not editable by default.
     */
    public void setDataBlock(GridRange gridRange, int[] values)
            throws InvalidRangeException, GridNotEditableException, ArrayIndexOutOfBoundsException
    {
        throw new GridNotEditableException(); // TODO: provides a localized message.
    }

    /**
     * Set a block of values for all sample dimensions. The default implementation always throws
     * an exception, since this grid coverage is not editable by default.
     */
    public void setDataBlock(GridRange gridRange, float[] values)
            throws InvalidRangeException, GridNotEditableException, ArrayIndexOutOfBoundsException
    {
        throw new GridNotEditableException(); // TODO: provides a localized message.
    }

    /**
     * Set a block of values for all sample dimensions. The default implementation always throws
     * an exception, since this grid coverage is not editable by default.
     */
    public void setDataBlock(GridRange gridRange, double[] values)
            throws InvalidRangeException, GridNotEditableException, ArrayIndexOutOfBoundsException
    {
        throw new GridNotEditableException(); // TODO: provides a localized message.
    }

    /**
     * Set a block of values for all sample dimensions. The default implementation always throws
     * an exception, since this grid coverage is not editable by default.
     */
    public void setPackedDataBlock(GridRange gridRange, byte[] values)
            throws InvalidRangeException, GridNotEditableException, ArrayIndexOutOfBoundsException
    {
        throw new GridNotEditableException(); // TODO: provides a localized message.
    }

    /**
     * Returns a localized error message for {@link IndexOutOfBoundsException}.
     */
    private String indexOutOfBounds(final int index) {
        return org.geotools.resources.cts.Resources.getResources(getLocale()).getString(
               org.geotools.resources.cts.ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2,
               "index", new Integer(index));
    }

    /**
     * Constructs an error message for a point outside the coverage.
     * This is used for formatting error messages.
     *
     * @param  point The coordinate point to format.
     * @return An error message.
     */
    protected String pointOutsideCoverage(final Point2D point) {
        return pointOutsideCoverage((DirectPosition) new DirectPosition2D(point));
    }

    /**
     * Constructs an error message for a point outside the coverage.
     * This is used for formatting error messages.
     *
     * @param  point The coordinate point to format.
     * @return An error message.
     */
    protected String pointOutsideCoverage(final DirectPosition point) {
        final Locale locale = getLocale();
        return Resources.getResources(locale).
                getString(ResourceKeys.ERROR_POINT_OUTSIDE_COVERAGE_$1, toString(point, locale));
    }

    /**
     * Constructs a string for the specified point.
     * This is used for formatting error messages.
     *
     * @param  point The coordinate point to format.
     * @param  locale The locale for formatting numbers.
     * @return The coordinate point as a string, without '(' or ')' characters.
     */
    static String toString(final Point2D point, final Locale locale) {
        return toString((DirectPosition) new DirectPosition2D(point), locale);
    }

    /**
     * Constructs a string for the specified point.
     * This is used for formatting error messages.
     *
     * @param  point The coordinate point to format.
     * @param  locale The locale for formatting numbers.
     * @return The coordinate point as a string, without '(' or ')' characters.
     */
    static String toString(final DirectPosition point, final Locale locale) {
        final StringBuffer buffer = new StringBuffer();
        final FieldPosition dummy = new FieldPosition(0);
        final NumberFormat format = NumberFormat.getNumberInstance(locale);
        final int       dimension = point.getDimension();
        for (int i=0; i<dimension; i++) {
            if (i != 0) {
                buffer.append(", ");
            }
            format.format(point.getOrdinate(i), buffer, dummy);
        }
        return buffer.toString();
    }
}
