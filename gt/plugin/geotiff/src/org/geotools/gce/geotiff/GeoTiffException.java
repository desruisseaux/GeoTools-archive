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

import java.io.IOException ; 
import java.io.StringWriter ; 
import java.io.PrintWriter ; 
import java.util.HashMap ; 
import java.util.Map ; 
import javax.imageio.metadata.IIOMetadata ; 

/**
 * This exception is thrown when the problem with reading the GeoTiff
 * file has to do with constructing either the raster to model
 * transform, or the coordinate system.  A GeoTiffException:
 *
 * <p>
 * <ul>
 * <li> encapsulates the salient information in the GeoTiff tags,
 *     making the values available as read only properties.
 * <li> sends the appropriate log message to the log stream
 * <li> produces a readable message property for later retrieval
 * </ul>
 *
 * <p>
 *
 * This exception is expected to be thrown when there is absolutely
 * nothing wrong with the GeoTiff file which produced it.  In this
 * case, the exception is reporting an unsupported coordinate
 * system description or raster to model transform, or some other
 * unrecognized configuration of the GeoTIFF tags.  By doing so,
 * it attempts to record enough information so that the maintainers
 * can support it in the future.
 * @author Bryce Nordgren / USDA Forest Service
 */
public class GeoTiffException extends IOException {
    

    private GeoTiffIIOMetadataAdapter metadata = null ; 
    private GeoTiffIIOMetadataAdapter.GeoKeyRecord[] geoKeys = null ; 
    
    /**
     * Creates a new instance of <code>GeoTiffException</code> without detail message.
     */
    public GeoTiffException(GeoTiffIIOMetadataAdapter metadata) {
        this(metadata, "") ;
    }
    
    
    /**
     * Constructs an instance of <code>GeoTiffException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public GeoTiffException(GeoTiffIIOMetadataAdapter metadata, String msg) {
        super(msg);
        this.metadata = metadata ; 
        int numGeoKeys = metadata.getNumGeoKeys() ; 
        if (numGeoKeys > 0) {
            geoKeys = new GeoTiffIIOMetadataAdapter.GeoKeyRecord [numGeoKeys] ; 
            for (int i=0; i < numGeoKeys; i++) { 
                geoKeys[i] = metadata.getGeoKeyRecordByIndex(i) ; 
            }
        }
    }
    
    /**
     * Getter for property modelPixelScales.
     * @return Value of property modelPixelScales.
     */
    public double[] getModelPixelScales() {
        return metadata.getModelPixelScales();
    }
    
    /**
     * Getter for property modelTiePoints.
     * @return Value of property modelTiePoints.
     */
    public double[] getModelTiePoints() {
        return metadata.getModelTiePoints();
    }
    
    /**
     * Getter for property modelTransformation.
     * @return Value of property modelTransformation.
     */
    public double[] getModelTransformation() {
        return metadata.getModelTransformation();
    }
    
    /**
     * Getter for property geoKeys.
     * @return Value of property geoKeys.
     */
    public GeoTiffIIOMetadataAdapter.GeoKeyRecord[] getGeoKeys() {
        return this.geoKeys;
    }
    
    public String getMessage() {
        StringWriter text = new StringWriter(1024) ; 
        PrintWriter message = new PrintWriter(text) ; 
        
        //Header 
        message.println("GEOTIFF Module Error Report") ; 
        
        // start with the message the user specified
        message.println(super.getMessage());
        
        // do the model pixel scale tags
        message.print("ModelPixelScaleTag: ") ;
        double modelPixelScales[] = getModelPixelScales() ; 
        if (modelPixelScales != null ) {
            message.println("["+modelPixelScales[0]+","+modelPixelScales[1]+
                  "," + modelPixelScales[2]+"]") ; 
        } else {
            message.println("NOT AVAILABLE") ; 
        }
        
        // do the model tie point tags
        message.print("ModelTiePointTag: ");
        double []modelTiePoints = getModelTiePoints() ; 
        int numTiePoints = modelTiePoints.length ; 
        message.println("(" + numTiePoints/6 + " tie points)") ;
        for (int i=0 ;i < (numTiePoints/6); i++) {
            int j = i*6 ; 
            message.print("TP #" + i + ": ") ; 
            message.print("["+ modelTiePoints[j]) ; 
            message.print(","+ modelTiePoints[j+1]) ; 
            message.print(","+ modelTiePoints[j+2]) ; 
            message.print("] -> ["+ modelTiePoints[j+3]) ; 
            message.print(","+ modelTiePoints[j+4]) ; 
            message.println(","+ modelTiePoints[j+5] + "]") ; 
        }

        // do the transformation tag
        message.print("ModelTransformationTag: ") ;
        double [] modelTransformation = getModelTransformation() ; 
        if (modelTransformation != null) {
            message.println("[") ;
            for (int i=0; i<4 ; i++) {
                int j = i*4 ; 
                message.print(" [" + modelTransformation[j]) ; 
                message.print(","+modelTransformation[j+1]) ; 
                message.print(","+modelTransformation[j+2]) ; 
                message.println(","+modelTransformation[j+3]+"]");
            }
            message.println("]");
        } else {
            message.println("NOT AVAILABLE") ;
        }
        
        //do all the GeoKeys
        int numTags = geoKeys.length ; 
        for (int i=0; i < numTags; i++) {
            message.print("GeoKey #"+(i+1)+ ": ");  
            message.println("Key = "+geoKeys[i].getKeyID()+", Value = "+metadata.getGeoKey(geoKeys[i].getKeyID())) ; 
        }
        
        String msg = text.toString() ; 
        
        message.close() ; 
        return msg ;
    }
    
}
