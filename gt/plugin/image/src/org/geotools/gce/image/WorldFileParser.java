/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, 2004 Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.gce.image;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.logging.*;

/**
 * Extends the generic property parser to allow for world files which don't bother to have 
 * nice keys for a parser to deal with.
 *
 * @author  iant
 */
public class WorldFileParser extends org.geotools.io.coverage.PropertyParser {
    boolean loaded = false;
    int dimension = 4; // just a default...
    static private Logger LOGGER = Logger.getLogger("org.geotools.coverage");
    /** Creates a new instance of WorldFileParser */
    public WorldFileParser(File worldfile) throws IOException{
        super();
        load(worldfile);
    }
    
    protected void load(final BufferedReader in) throws IOException {
        if(loaded) return ;
        
        super.clear();
        this.addAlias(X_MINIMUM, "x-origin");
        this.addAlias(Y_MINIMUM, "y-origin");
        this.addAlias(X_MAXIMUM, "x-max");
        this.addAlias(Y_MAXIMUM, "y-max");
        this.addAlias(X_RESOLUTION, "x-res");
        this.addAlias(Y_RESOLUTION, "y-res");
        double x,y,mx,my;
        double xres,yres;
        String line;
        // parse world file
        
        BufferedReader r = new BufferedReader(in);
        line = r.readLine();
        xres = Double.parseDouble(line.trim());
        
        // skip two rotation coords - we could check they really are zero ?
        line = r.readLine();
        line = r.readLine();
        line = r.readLine();
        yres = Double.parseDouble(line); // yres is always -ve in a world file
        line = r.readLine();
        x = Double.parseDouble(line);
        line = r.readLine();
        my = Double.parseDouble(line);
        
        
        Double X,MY, XRes, YRes;
        X = new Double(x);
        MY = new Double(my);
        XRes = new Double(xres);
        YRes = new Double(yres);
        LOGGER.fine("adding XMin " + X);
        LOGGER.fine("adding YMax " + MY);
        this.add("x-origin", X);
        this.add("y-max", MY);
        LOGGER.fine("adding X-res " + XRes);
        LOGGER.fine("adding Y-res " + YRes);
        this.add("x-res", XRes);
        this.add("y-res", YRes);
        try{
            // set a geocentric coord sys

            CoordinateSystemFactory csf = CoordinateSystemFactory.getDefault();
            GeographicCoordinateSystem gcs = csf.createGeographicCoordinateSystem("test",HorizontalDatum.WGS84); 
            this.addAlias(this.UNITS, "units-i");
            this.add("units-i", "degrees");

            this.addAlias(this.DATUM, "datum-i");
            this.add("datum-i",gcs.getHorizontalDatum());
            
            this.addAlias(this.HEIGHT, "height-i");
            this.addAlias(this.WIDTH, "Width-i");
            
        } catch (org.opengis.referencing.FactoryException fe){
            System.out.println("Factory Exception " + fe);
            LOGGER.severe("Factory Exception " + fe);
        }
        loaded = true;
    }
    
    public synchronized void clear() { 
        /* do nothing for the time being */
    }
    
    public void setHeight(int height){
        LOGGER.fine("setting height " + height);
        try{
            this.add("height-i", new Integer(height));
            this.add("y-origin", new Double(this.getAsDouble(this.Y_MAXIMUM)+(double)height*this.getAsDouble(this.Y_RESOLUTION)));
            LOGGER.fine("ymin now " + this.getAsDouble(this.Y_MINIMUM));
        } catch (PropertyException e){
            LOGGER.severe("property exception " + e);
        }
    }
    public void setWidth(int width){
        LOGGER.fine("setting width " + width);
        try{    
            this.add("width-i", new Integer(width));
//            this.add("x-max", new Double((double)width*this.getAsDouble(this.X_RESOLUTION)));
//            LOGGER.fine("Xmax now " + this.getAsDouble(this.X_MAXIMUM));
        } catch (PropertyException e){
            LOGGER.severe("property exception " + e);
        }
    }
    
    public int getDimension(){
        // this should be an array of 3 or 4 depending on transparency - I think.
        return this.dimension;
    }
    
    public void setDimension(int dimension) {
        this.dimension = dimension;
    }
    
    /**
     * Returns the sample dimensions for each band of the {@link GridCoverage}
     * to be read. If sample dimensions are not know, then this method returns
     * <code>null</code>. The default implementation always returns <code>null</code>.
     *
     * @throws PropertyException if the operation failed.
     */
    public SampleDimension[] getSampleDimensions() throws PropertyException {
        
        int dimension = getDimension();
        SampleDimension[] sd = new SampleDimension[dimension];
        for(int i = 0; i< dimension; i++){
            sd[i] = new SampleDimension();
        }
       
        return sd;
            
    }
}
