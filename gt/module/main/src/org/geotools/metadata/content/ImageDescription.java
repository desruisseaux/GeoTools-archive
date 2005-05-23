/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
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
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.metadata.content;

// J2SE extensions
import org.geotools.resources.Utilities;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.content.ImagingCondition;


/**
 * Location of the responsible individual or organization.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 * @deprecated Renamed as {@code ImageDescriptionImpl} in {@code org.geotools.metadata.iso} subpackage.
 */
public class ImageDescription extends CoverageDescription
       implements org.opengis.metadata.content.ImageDescription
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -6168624828802439062L;
    
    /**
     * Illumination elevation measured in degrees clockwise from the target plane at
     * intersection of the optical line of sight with the Earth’s surface. For images from a
     * scanning device, refer to the centre pixel of the image.
     */
    private Number illuminationElevationAngle;
    
    /**
     * Illumination azimuth measured in degrees clockwise from true north at the time the
     * image is taken. For images from a scanning device, refer to the centre pixel of the image.
     */
    private Number illuminationAzimuthAngle;
    
    /**
     * Conditions affected the image.
     */
    private ImagingCondition imagingCondition;
    
    /**
     * Specifies the image quality.
     */
    private Identifier imageQualityCode;
    
    /**
     * Area of the dataset obscured by clouds, expressed as a percentage of the spatial extent.
     */
    private Number cloudCoverPercentage;
    
    /**
     * Image distributor’s code that identifies the level of radiometric and geometric
     * processing that has been applied.
     */
    private Identifier processingLevelCode;
    
    /**
     * Count of the number the number of lossy compression cycles performed on the image.
     * <code>null</code> if the information is not provided.
     */
    private Integer compressionGenerationQuantity;
    
    /**
     * Indication of whether or not triangulation has been performed upon the image.
     * <code>null</code> if the information is not provided.
     */
    private Boolean triangulationIndicator;
    
    /**
     * Indication of whether or not the radiometric calibration information for generating the
     * radiometrically calibrated standard data product is available.
     */
    private boolean radiometricCalibrationDataAvailable;
    
    /**
     * Indication of whether or not constants are available which allow for camera calibration
     * corrections.
     */
    private boolean cameraCalibrationInformationAvailable;
    
    /**
     * Indication of whether or not Calibration Reseau information is available.
     */
    private boolean filmDistortionInformationAvailable;
    
    /**
     * Indication of whether or not lens aberration correction information is available.
     */
    private boolean lensDistortionInformationAvailable;

    /**
     * Constructs an initially empty image description.
     */
    public ImageDescription() {
    }

    /**
     * Returns the illumination elevation measured in degrees clockwise from the target plane at
     * intersection of the optical line of sight with the Earth’s surface. For images from a
     * scanning device, refer to the centre pixel of the image.
     */
    public Number getIlluminationElevationAngle() {
        return illuminationElevationAngle;
    }

    /**
     * Set the illumination elevation measured in degrees clockwise from the target plane at
     * intersection of the optical line of sight with the Earth’s surface. For images from a
     * scanning device, refer to the centre pixel of the image.
     */
    public synchronized void setIlluminationElevationAngle(final Number newValue) {
        checkWritePermission();
        illuminationElevationAngle = newValue;
    }

    /**
     * Returns the illumination azimuth measured in degrees clockwise from true north at the time
     * the image is taken. For images from a scanning device, refer to the centre pixel of the
     * image.
     */
    public Number getIlluminationAzimuthAngle() {
        return illuminationAzimuthAngle;
    }

    /**
     * Set the illumination azimuth measured in degrees clockwise from true north at the time the
     * image is taken. For images from a scanning device, refer to the centre pixel of the image.
     */
    public synchronized void setIlluminationAzimuthAngle(final Number newValue) {
        checkWritePermission();
        illuminationAzimuthAngle = newValue;
    }

    /**
     * Returns the conditions affected the image.
     */
    public ImagingCondition getImagingCondition() {
        return imagingCondition;
    }

    /**
     * Set the conditions affected the image.
     */
    public synchronized void setImagingCondition(final ImagingCondition newValue) {
        checkWritePermission();
        imagingCondition = newValue;
    }

    /**
     * Returns the specifies the image quality.
     */
    public Identifier getImageQualityCode() {
        return imageQualityCode;
    }

    /**
     * Set the specifies the image quality.
     */
    public synchronized void setImageQualityCode(final Identifier newValue) {
        checkWritePermission();
        imageQualityCode = newValue;
    }

    /**
     * Returns the area of the dataset obscured by clouds, expressed as a percentage of the spatial
     * extent.
     */
    public Number getCloudCoverPercentage() {
        return cloudCoverPercentage;
    }

    /**
     * Set the area of the dataset obscured by clouds, expressed as a percentage of the spatial
     * extent.
     */
    public synchronized void setCloudCoverPercentage(final Number newValue) {
        checkWritePermission();
        cloudCoverPercentage = newValue;
    }

    /**
     * Returns the image distributor’s code that identifies the level of radiometric and geometric
     * processing that has been applied.
     */
    public Identifier getProcessingLevelCode() {
        return processingLevelCode;
    }

    /**
     * Set the image distributor’s code that identifies the level of radiometric and geometric
     * processing that has been applied.
     */
    public synchronized void setProcessingLevelCode(final Identifier newValue) {
        checkWritePermission();
        processingLevelCode = newValue;
    }

    /**
     * Returns the count of the number the number of lossy compression cycles performed on the
     * image. Returns <code>null</code> if the information is not provided.
     */
    public Integer getCompressionGenerationQuantity() {
        return compressionGenerationQuantity;
    }

    /**
     * Set the count of the number the number of lossy compression cycles performed on the image.
     */
    public synchronized void setCompressionGenerationQuantity(final Integer newValue) {
        checkWritePermission();
        compressionGenerationQuantity = newValue;
    }

    /**
     * Returns the indication of whether or not triangulation has been performed upon the image.
     * Returns <code>null</code> if the information is not provided.
     */
    public Boolean getTriangulationIndicator() {
        return triangulationIndicator;
    }

    /**
     * Set the indication of whether or not triangulation has been performed upon the image.
     */
    public synchronized void setTriangulationIndicator(final Boolean newValue) {
        checkWritePermission();
        triangulationIndicator = newValue;
    }

    /**
     * Returns theiIndication of whether or not the radiometric calibration information for
     * generating the radiometrically calibrated standard data product is available.
     */
    public boolean isRadiometricCalibrationDataAvailable() {
        return radiometricCalibrationDataAvailable;
    }

    /**
     * Set the indication of whether or not the radiometric calibration information for generating
     * the radiometrically calibrated standard data product is available.
     */
    public synchronized void setRadiometricCalibrationDataAvailable(final boolean newValue) {
        checkWritePermission();
        radiometricCalibrationDataAvailable = newValue;
    }

    /**
     * Returns the indication of whether or not constants are available which allow for camera
     * calibration corrections.
     */
    public boolean isCameraCalibrationInformationAvailable() {
        return cameraCalibrationInformationAvailable;
    }

    /**
     * Set the indication of whether or not constants are available which allow for camera
     * calibration corrections.
     */
    public synchronized void setCameraCalibrationInformationAvailable(final boolean newValue) {
        checkWritePermission();
        cameraCalibrationInformationAvailable = newValue;
    }

    /**
     * Returns the indication of whether or not Calibration Reseau information is available.
     */
    public boolean isFilmDistortionInformationAvailable() {
        return filmDistortionInformationAvailable;
    }

    /**
     * Set the indication of whether or not Calibration Reseau information is available.
     */
    public synchronized void setFilmDistortionInformationAvailable(final boolean newValue) {
        checkWritePermission();
        filmDistortionInformationAvailable = newValue;
    }

    /**
     * Returns the indication of whether or not lens aberration correction information is available.
     */
    public boolean isLensDistortionInformationAvailable() {
        return lensDistortionInformationAvailable;
    }

    /**
     * Set the indication of whether or not lens aberration correction information is available.
     */
    public synchronized void setLensDistortionInformationAvailable(final boolean newValue) {
        checkWritePermission();
        lensDistortionInformationAvailable = newValue;
    }
    
    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        illuminationElevationAngle = (Number)     unmodifiable(illuminationElevationAngle);
        illuminationAzimuthAngle   = (Number)     unmodifiable(illuminationAzimuthAngle);
        imageQualityCode           = (Identifier) unmodifiable(imageQualityCode);
        cloudCoverPercentage       = (Number)     unmodifiable(cloudCoverPercentage);
        processingLevelCode        = (Identifier) unmodifiable(processingLevelCode);
    }

    /**
     * Compare this image description with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final ImageDescription that = (ImageDescription) object;
            return Utilities.equals(this.illuminationElevationAngle, that.illuminationElevationAngle) &&
                   Utilities.equals(this.illuminationAzimuthAngle,   that.illuminationAzimuthAngle  ) &&
                   Utilities.equals(this.imagingCondition,           that.imagingCondition          ) &&
                   Utilities.equals(this.imageQualityCode,           that.imageQualityCode          );
        }
        return false;
    }

    /**
     * Returns a hash code value for this image description. For performance reason, this method
     * do not uses all attributes for computing the hash code. Instead, it uses the attributes
     * that are the most likely to be unique.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (illuminationElevationAngle != null)  code ^= illuminationElevationAngle.hashCode();
        if (illuminationAzimuthAngle   != null)  code ^= illuminationAzimuthAngle  .hashCode();
        return code;
    }
}
