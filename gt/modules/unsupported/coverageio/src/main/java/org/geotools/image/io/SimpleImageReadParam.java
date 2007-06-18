/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, Geotools Project Managment Committee (PMC)
 *    (C) 2007, Geomatys
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
 */
package org.geotools.image.io;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.io.IOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageTypeSpecifier;
import javax.media.jai.ComponentSampleModelJAI;
import javax.media.jai.util.Range;
import org.geotools.resources.XArray;
import org.geotools.resources.XMath;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.image.ComponentColorModelJAI;
import org.geotools.util.NumberRange;


/**
 * Default parameters for {@link SimpleImageReader}. This class provides convenience methods for
 * {@linkplain #setDestinationType setting the destination type} from expected minimum and maximum
 * values, and from a color palette.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class SimpleImageReadParam extends ImageReadParam {
    /**
     * The name of the color palette.
     */
    private String palette;

    /**
     * The range of valid values for every source bands.
     */
    private NumberRange[] sourceRanges;

    /**
     * The range of valid values for every destination bands.
     */
    private NumberRange[] destinationRanges;

    /**
     * Creates a new, initially empty, set of parameters.
     */
    public SimpleImageReadParam() {
    }

    /**
     * Ensures that the specified band number is valid.
     */
    private static void ensureValidBand(final int band) throws IllegalArgumentException {
        if (band < 0) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.BAD_BAND_NUMBER_$1, new Integer(band)));
        }
    }

    /**
     * Convenience method returning the destination band for the specified source band.
     *
     * @param band The source band number to be converted into a destination band number.
     * @return The destination band number.
     * @throws IllegalArgumentException if no destination band was found for the given source band.
     */
    public int sourceToDestBand(final int band) {
        ensureValidBand(band);
        if (sourceBands == null) {
            return (destinationBands != null) ? destinationBands[band] : band;
        }
        for (int i=0; i<sourceBands.length; i++) {
            if (sourceBands[i] == band) {
                return (destinationBands != null) ? destinationBands[i] : i;
            }
        }
        throw new IllegalArgumentException(Errors.format(ErrorKeys.BAD_BAND_NUMBER_$1, new Integer(band)));
    }

    /**
     * Convenience method returning the source band for the specified destination band.
     *
     * @param band The destination band number to be converted into a source band number.
     * @return The destination band number.
     * @throws IllegalArgumentException if no source band was found for the given destination band.
     */
    public int destToSourceBand(final int band) throws IllegalArgumentException {
        ensureValidBand(band);
        if (destinationBands == null) {
            return (sourceBands != null) ? sourceBands[band] : band;
        }
        for (int i=0; i<destinationBands.length; i++) {
            if (destinationBands[i] == band) {
                return (sourceBands != null) ? sourceBands[i] : i;
            }
        }
        throw new IllegalArgumentException(Errors.format(ErrorKeys.BAD_BAND_NUMBER_$1, new Integer(band)));
    }

    /**
     * Sets the range of valid values expected for the specified band.
     *
     * @param ranges  The range array to update.
     * @param band    The band number to update.
     * @param range   The range of expected values.
     */
    private static NumberRange[] setRange(NumberRange[] ranges, final int band, final NumberRange range) {
        ensureValidBand(band);
        if (ranges == null) {
            ranges = new NumberRange[Math.max(band + 1, 4)];
        } else if (ranges.length <= band) {
            ranges = (NumberRange[]) XArray.resize(ranges, Math.max(ranges.length*2, band + 1));
        }
        ranges[band] = range;
        return ranges;
    }

    /**
     * Returns the range of valid values expected for the specified band.
     *
     * @param  ranges  The range array.
     * @param  band The band number.
     * @return The range of expected values, or {@code null} if none.
     */
    private static NumberRange getRange(final NumberRange[] ranges, final int band) {
        ensureValidBand(band);
        return (ranges != null && band < ranges.length) ? ranges[band] : null;
    }

    /**
     * Sets the range of valid values expected for the specified source band. The {@code band}
     * argument is a source band number ignoring any {@link #setSourceBands} setting. In other
     * words, this is the source band at the very begining of the "band number transformation
     * chain".
     * <p>
     * The destination range corresponding to the same band can be set with
     * <code>{@linkplain #setDestinationRange setDestinationRange}(@linkplain
     * #sourceToDestBand sourceToDestBand}(band), destinationRange)</code>.
     *
     * @param band  The band number in the source image.
     * @param range The range of expected values.
     */
    public void setSourceRange(final int band, final NumberRange range) {
        sourceRanges = setRange(sourceRanges, band, range);
    }

    /**
     * Returns the range of valid values expected for the specified source band. This is the value
     * previously set by <code>{@linkplain #setSourceRange setSourceRange}(band, ...)</code>. If no
     * such value was set, then this method returns the same range than the corresponding
     * destination band (i.e. the conversion from source to destination pixel values is
     * assumed an identity operation).
     *
     * @param  band The band number in the source image.
     * @return The range of expected values, or {@code null} if none.
     */
    public NumberRange getSourceRange(int band) {
        NumberRange range = getRange(sourceRanges, band);
        if (range == null) {
            band  = sourceToDestBand(band);
            range = getRange(destinationRanges, band);
        }
        return range;
    }

    /**
     * Sets the range of valid values expected for the specified destination band. The {@code band}
     * argument is a destination band number taking in account the {@link #setDestinationBands}
     * setting. In other words, this is the destination band at the very end of the "band number
     * transformation chain".
     * <p>
     * The source range corresponding to the same band can be set with
     * <code>{@linkplain #setSourceRange setSourceRange}(@linkplain
     * #destToSourceBand destToSourceBand}(band), sourceRange)</code>.
     *
     * @param band  The band number in the source image.
     * @param range The range of expected values.
     */
    public void setDestinationRange(final int band, final NumberRange range) {
        sourceRanges = setRange(sourceRanges, band, range);
    }

    /**
     * Returns the range of valid values expected for the specified destination band. This is the
     * value previously set by <code>{@linkplain #setDestinationRange setDestinationRange}(band,
     * ...)</code>. If no such value was set, then this method returns the same range than the
     * corresponding source band (i.e. the conversion from source to destination pixel values is
     * assumed an identity operation).
     *
     * @param  band The band number in the source image.
     * @return The range of expected values, or {@code null} if none.
     */
    public NumberRange getDestinationRange(int band) {
        NumberRange range = getRange(destinationRanges, band);
        if (range == null) {
            band  = destToSourceBand(band);
            range = getRange(sourceRanges, band);
        }
        return range;
    }

    /**
     * Set the color palette as one of the names provided by the
     * {@linkplain PaletteFactory#getDefault default palette factory}.
     *
     * @see PaletteFactory#getAvailableNames
     */
    public void setPalette(final String palette) {
        this.palette = palette;
    }

    /**
     * Returns a grayscale color space for the specified range of values.
     * This color space is suitable for floating point values.
     *
     * @param  range    The range of values, or {@code null} if unknown.
     * @param  numBands The number of bands.
     * @return A default color space scaled to fit data.
     */
    private static ColorSpace getColorSpace(final Range range, final int numBands) {
        if (range != null) {
            final Comparable minimum = range.getMinValue();
            final Comparable maximum = range.getMaxValue();
            if ((minimum instanceof Number) && (maximum instanceof Number)) {
                final float minValue = ((Number) minimum).floatValue();
                final float maxValue = ((Number) maximum).floatValue();
                if (minValue < maxValue && !Float.isInfinite(minValue) &&
                                           !Float.isInfinite(maxValue))
                {
                    return new ScaledColorSpace(numBands, minValue, maxValue);
                }
            }
        }
        return ColorSpace.getInstance(ColorSpace.CS_GRAY);
    }

    /**
     * Creates a grayscale image type for the specified range of values.
     * The image type is suitable for floating point values.
     *
     * @param  dateType The data type as one of {@link DataBuffer} constants.
     * @param  range    The range of values, or {@code null} if unknown.
     * @param  numBands The number of bands.
     * @return A default color space scaled to fit data.
     */
    static ImageTypeSpecifier getImageTypeSpecifier(final int dataType,
            final Range range, final int numBands)
    {
        final int[] bankIndices = new int[numBands];
        final int[] bandOffsets = new int[numBands];
        for (int i=numBands; --i>=0;) {
            bankIndices[i] = i;
        }
        final ColorSpace colorSpace = getColorSpace(range, numBands);
        if (SimpleImageReader.USE_JAI_MODEL) {
            final ColorModel cm = new ComponentColorModelJAI(
                    colorSpace, null, false, false, Transparency.OPAQUE, dataType);
            return new ImageTypeSpecifier(cm, new ComponentSampleModelJAI(
                    dataType, 1, 1, 1, 1, bankIndices, bandOffsets));
        } else {
            return ImageTypeSpecifier.createBanded(
                    colorSpace, bankIndices, bandOffsets, dataType, false, false);
        }
    }

    /**
     * Creates a image type for the specified range of values.
     * The image type is suitable for floating point values.
     */
    static ImageTypeSpecifier getImageTypeSpecifier(final int    dataType,
                                                    final Range  range,
                                                    final int    numBands,
                                                    final String paletteName,
                                                    final boolean compact)
            throws IOException
    {
        final int maxAllowed;
        switch (dataType) {
            default: {
                return getImageTypeSpecifier(dataType, range, numBands);
            }
            case DataBuffer.TYPE_BYTE: {
                maxAllowed = 0xFF;
                break;
            }
            case DataBuffer.TYPE_USHORT: {
                maxAllowed = 0xFFFF;
                break;
            }
        }
        int validMin = 0;
        int validMax = maxAllowed;
        if (range != null) {
            final Comparable minimum = range.getMinValue();
            final Comparable maximum = range.getMaxValue();
            if (minimum instanceof Number) {
                validMin = ((Number) minimum).intValue();
                if (!range.isMinIncluded()) {
                    validMin++; // Must be inclusive
                }
            }
            if (maximum instanceof Number) {
                validMax = ((Number) maximum).intValue();
                if (range.isMaxIncluded()) {
                    validMax++; // Must be exclusive
                }
            }
        }
        final PaletteFactory factory = PaletteFactory.getDefault();
        final Palette palette = factory.getPalette(paletteName, validMin, validMax,
                compact ? validMax : maxAllowed);
        return palette.getImageTypeSpecifier();
    }
}
