package org.geotools.referencing.operation.builder;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.geotools.geometry.DirectPosition2D;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

public class IDWGridBuilder extends WarpGridBuilder {

	public IDWGridBuilder(List vectors, double dx, double dy, Envelope envelope,
	        MathTransform realToGrid) throws TransformException{
		super(vectors, dx, dy, envelope, realToGrid); 
		
	}
	protected float[] computeWarpGrid(ParameterValueGroup WarpParams) {
	      //  ParameterValueGroup WarpParams = globalValues.WarpGridParameters;
		float[] warpPositions =  (float[]) WarpParams.parameter("warpPositions").getValue();
	        for (int i = 0; i <= WarpParams.parameter("yNumCells").intValue(); i++) {
	            for (int j = 0; j <= WarpParams.parameter("xNumCells").intValue(); j++) {
	                Point2D shiftVector = calculateShift(new DirectPosition2D(WarpParams.parameter(
	                                "xStart").intValue()
	                            + (j * WarpParams.parameter("xStep").intValue()),
	                            WarpParams.parameter("yStart").intValue()
	                            + (i * WarpParams.parameter("yStep").intValue())));

	                double x = shiftVector.getX() + (j * WarpParams.parameter("xStep").intValue())
	                    + WarpParams.parameter("xStart").intValue();
	                double y = shiftVector.getY() + (i * WarpParams.parameter("yStep").intValue())
	                    + WarpParams.parameter("yStart").intValue();

	                warpPositions[(i * ((1 + WarpParams.parameter("xNumCells").intValue()) * 2))
	                + (2 * j)] = (float) x;

	                warpPositions[(i * ((1 + WarpParams.parameter("xNumCells").intValue()) * 2))
	                + (2 * j) + 1] = (float) y;
	            }                        
	        }       
	      
	        return warpPositions;
	    }
	
	  /**
     * Calculates the real point shift from the iregular pairs of MappedPositions using
     * Inverse distance weighting interpolation. The distance is cartesian.
     * @param p position where we requaired the shift to be calculated
     * @return
     */
    private Point2D calculateShift(Point2D p) {
        double maxdist = 50000;
    	
        HashMap nearest = getNearestMappedPositions(p, maxdist);

        double dx;
        double sumdx = 0;
        double dy = 0;
        double sumdy = 0;
        double sumweight = 0;

        for (Iterator i = nearest.keySet().iterator(); i.hasNext();) {
            MappedPosition mp = (MappedPosition) i.next();
            double distance = ((Double) nearest.get(mp)).doubleValue();
            double weight = (1 / Math.pow(distance, 2));

            if (distance > 0.00001) {
                sumdx = sumdx
                    + ((mp.getSource().getCoordinates()[0] - mp.getTarget().getCoordinates()[0]) * weight);
                sumdy = sumdy
                    + ((mp.getSource().getCoordinates()[1] - mp.getTarget().getCoordinates()[1]) * weight);

                sumweight = sumweight + weight;
            } else {
                dx = (mp.getSource().getCoordinates()[0] - mp.getTarget().getCoordinates()[0]);
                dy = (mp.getSource().getCoordinates()[1] - mp.getTarget().getCoordinates()[1]);

                return (new DirectPosition2D(dx, dy));
            }
        }

        dx = sumdx / sumweight;
        dy = sumdy / sumweight;

        return (new DirectPosition2D(dx, dy));
    }    

   
    /**
     * Computes nearest points.
     * @param p
     * @param maxdistance
     * @param number
     * @return
     *
     * @todo consider some indexing mechanism for finding the nearest positions
     */
    private HashMap getNearestMappedPositions(Point2D p, double maxdistance) {
        HashMap nearest = new HashMap();
        MappedPosition mp = null;

        for (Iterator i = this.getMappedPositions().iterator(); i.hasNext();) {
            mp = (MappedPosition) i.next();

            double dist = p.distance((Point2D) mp.getSource());

            if ((dist < maxdistance)) {
                nearest.put(mp, new Double(dist));               
            }
        }               
        
        return nearest;
    }


}
