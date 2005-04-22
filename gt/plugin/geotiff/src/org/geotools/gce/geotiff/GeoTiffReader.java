/*
 * NOTICE OF RELEASE TO THE PUBLIC DOMAIN
 *
 * This work was created by employees of the USDA Forest Service's 
 * Fire Science Lab for internal use.  It is therefore ineligible for 
 * copyright under title 17, section 105 of the United States Code.  You 
 * may treat it as you would treat any public domain work: it may be used,
 * changed, copied, or redistributed, with or without permission of the 
 * authors, for free or for compensation.  You may not claim exclusive 
 * ownership of this code because it is already owned by everyone.  Use this 
 * software entirely at your own risk.  No warranty of any kind is given.
 * 
 * A copy of 17-USC-105 should have accompanied this distribution in the file 
 * 17USC105.html.  If not, you may access the law via the US Government's 
 * public websites: 
 *   - http://www.copyright.gov/title17/92chap1.html#105
 *   - http://www.gpoaccess.gov/uscode/  (enter "17USC105" in the search box.)
 */
package org.geotools.gce.geotiff;

//J2SE dependencies
import java.awt.geom.AffineTransform;
import java.io.IOException;
import javax.imageio.metadata.IIOMetadata;

// Geotools dependencies 
import org.geotools.coverage.grid.GridCoverage2D ; 

// Geoapi dependencies
import org.opengis.coverage.grid.GridCoverage ; 
import org.opengis.coverage.grid.Format ; 
import org.opengis.coverage.grid.GridCoverageReader ; 
import org.opengis.referencing.crs.CRSAuthorityFactory ; 
import org.opengis.referencing.operation.MathTransformFactory ; 
import org.opengis.referencing.operation.MathTransform ; 
import org.opengis.referencing.crs.CoordinateReferenceSystem ; 
import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.FactoryException;

//JAI ImageIO Tools dependencies
import com.sun.media.jai.operator.ImageReadDescriptor;

// JAI dependencies
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;


/**
 * <CODE>GeoTiffReader</CODE> is responsible for exposing the data and 
 * the Georeferencing metadata available to the Geotools library.
 *
 * @author Bryce Nordgren, USDA Forest Service
 */
public class GeoTiffReader implements GridCoverageReader {
    
    /**
     * Number of images read from file.  read() increments this counter and
     * hasMoreGridCoverages() accesses it.
     */    
    private int imagesRead = 0  ;
    
    /**
     * This contains the maximum number of grid coverages in the
     * file/stream.  Until multi-image files are supported, this is
     * going to be 0 or 1.
     */    
    private int maxImages = 0 ;
    
    private GeoTiffFormat creater = null ;
    private Object source  = null ; 
    
    private RenderedOp image = null ; 
    private GeoTiffIIOMetadataAdapter metadata = null ; 
    
    /** Creates a new instance of GeoTiffReader */
    public GeoTiffReader(Format creater, Object source) {
        this.creater = (GeoTiffFormat)creater ; 
        this.source  = source ; 
        this.image = JAI.create("ImageRead", source); 
        
        IIOMetadata temp = (IIOMetadata)(image.getProperty(
          ImageReadDescriptor.PROPERTY_NAME_METADATA_IMAGE));
        metadata = new GeoTiffIIOMetadataAdapter(temp);
    }
    
    public void dispose()  {
        image.dispose() ; 
    }    

    /**
     * No subnames.  Always returns null.
     */    
    public String getCurrentSubname()  {
        return null ; 
    }
    
    public Format getFormat() {
        return creater ; 
    }
    
    public String[] getMetadataNames() throws IOException {
        throw new UnsupportedOperationException("GeoTIFF reader doesn't support metadata manipulation yet");
    }
    
    public String getMetadataValue(String name) throws IOException, MetadataNameNotFoundException {
        throw new UnsupportedOperationException("GeoTIFF reader doesn't support metadata manipulation yet");
    }
    
    public Object getSource() {
        return source; 
    }
    
    /**
     * Returns true if another image remains to be read.  This module
     * currently only supports one image per TIFF file, so the first
     * read will make this method return false.
     * @return true if another grid coverage remains to be read.
     */    
    public boolean hasMoreGridCoverages() {
        return imagesRead < maxImages ; 
    }
    
    /**
     * Always returns null.  No subnames.
     */    
    public String[] listSubNames()  {
        return null ; 
    }

    /**
     * This method reads in the TIFF image, constructs an appropriate CRS,
     * determines the math transform from raster to the CRS model, and 
     * constructs a GridCoverage.
     */
    public GridCoverage read(GeneralParameterValue params) 
      throws IllegalArgumentException, IOException {
        // get the raster -> model transformation
        MathTransform r2m = getRasterToModel() ; 
        
        // get the coordinate reference system
        GeoTiffCoordinateSystemAdapter gtcs = 
          new GeoTiffCoordinateSystemAdapter() ; 
        gtcs.setMetadata(metadata) ; 
        CoordinateReferenceSystem crs = gtcs.createCoordinateSystem() ; 

        return new GridCoverage2D(source.toString(), image, crs, r2m,
            null, null, null) ; 
    }
    
    
    /**
     * There are no guts to this function.  Only single-image TIFF files
     * are supported.
     */    
    public void skip()  {
        // add support for multi image TIFF files later.
        throw new UnsupportedOperationException("No support for multi-image TIFF.") ; 
    }
    
    private MathTransform getRasterToModel() throws GeoTiffException {
        double [] tiePoints = metadata.getModelTiePoints() ; 
        double [] pixScales = metadata.getModelPixelScales() ; 
        int numTiePoints = tiePoints.length / 6 ;
        MathTransform xform = null ;
        
        if (numTiePoints == 1) { 
            // we would really like a Linear transform for just a scale and a
            // translate, but that functionality isn't quite tied in with the 
            // Geotools framework.  For now we put in an AffineTransform that 
            // doesn't rotate.
            try { 
              MathTransformFactory xformFactory = (MathTransformFactory)
                FactoryFinder.getFactory(
                  "org.opengis.referencing.operation.MathTransformFactory", 
                  null) ; 
              xform =xformFactory.createAffineTransform(at) ; 
            } catch (FactoryConfigurationError fce) { 
              GeoTiffException gte = new GeoTiffException(
                metadata, "No math transform factories registered!") ; 
              gte.initCause(fce) ; 
              throw gte ; 
            }
        } else {
            throw new GeoTiffException(metadata, "Unknown Raster to Model configuration.") ;
        }
        return xform ; 
    }
}
