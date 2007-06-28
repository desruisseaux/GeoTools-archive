package org.geotools.referencing.operation.builder;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.vecmath.MismatchedSizeException;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.operation.builder.MappedPosition;
import org.geotools.referencing.operation.builder.MathTransformBuilder;
import org.geotools.referencing.operation.transform.LocalizationGrid;
import org.geotools.referencing.operation.transform.WarpGridTransform2D;
import org.opengis.geometry.Envelope;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.geometry.MismatchedReferenceSystemException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;


public class LocalizationGridBuilder extends MathTransformBuilder {
    /**
     * LocalizationGrid that will be computed
     */
    private LocalizationGrid gridShift;

    /**
     * Grid width
     */
    int width;

    /**
     * Grid height
     */
    int height;

    /**
     * Envelope for generated Grid
     */
    Envelope envelope;

    /**
     * GridValues like maxx maxt dx dy etc..
     */
    GridGlobalValues globalValues;
    
    
    /**
     * MAth transform that transgormas generated grid into the Envepole.
     */
    MathTransform gridShiftToEnv;

    /**
     * Grid of x shifts
     */
    private float[][] dxgrid;

    /**
     * Grid of y shifts
     */
    private float[][] dygrid;
    
    private float[] warpPositions;

    /**
     * Construts Builder from 
     * @param vectors
     * @param dx The horizontal spacing between grid cells. 
     * @param dy The vertical spacing between grid cells.
     * @param Envelope Envelope of generated grid.
     * @throws MismatchedSizeException
     * @throws MismatchedDimensionException
     * @throws MismatchedReferenceSystemException
     */
    public LocalizationGridBuilder(List vectors, double dx, double dy,
        Envelope envelope)
        throws MismatchedSizeException, MismatchedDimensionException,
            MismatchedReferenceSystemException {
        super.setMappedPositions(vectors);
      
        globalValues = new GridGlobalValues(
        		envelope.getLowerCorner().getCoordinates()[0],
                envelope.getLowerCorner().getCoordinates()[1],
                envelope.getUpperCorner().getCoordinates()[0],
                envelope.getUpperCorner().getCoordinates()[1], dx, dy);

        this.envelope = envelope;
        gridShift = new LocalizationGrid(width, height);
        //dxgrid = new float[width][height];
        //dygrid = new float[width][height];
        warpPositions = new float[2*(width+1)*(height+1)];
    //    this.gridShiftToEnv = ProjectiveTransform.createScale(2, 0.5);//envelope.getLength(0) / width);
        this.computeGrid();
    //	gridShift.transform((AffineTransform)this.gridShiftToEnv, null);
        
    }

    public LocalizationGridBuilder(double dx, double dy, Envelope envelope) {
        this((List) (new ArrayList()), dx, dy, envelope);
    }

    public LocalizationGridBuilder(int width, int height, List vectors,
        Envelope envelope) {
        this(vectors, envelope.getLength(0) / width,
            envelope.getLength(1) / height, envelope);
       
    }

    private void ensureVectorsInsideEnvelope() {
        /* @TODO - ensure that source MappedPositions are inside the  envelope*/
    }

    /*
     *
     * (non-Javadoc)
     * @see org.geotools.referencing.operation.builder.MathTransformBuilder#computeMathTransform()
     */
    protected MathTransform computeMathTransform() throws FactoryException {
       
       return gridShift.getPolynomialTransform(0);
        
      // return new WarpGridTransform2D(0, (int)globalValues.dx ,width,0, (int)globalValues.dy ,height , warpPositions); 
       // return (new WarpTransform2D((gridShift.getPolynomialTransform(0));
    }
        

    /*
     * (non-Javadoc)
     * @see org.geotools.referencing.operation.builder.MathTransformBuilder#getMinimumPointCount()
     */
    public int getMinimumPointCount() {
        return 1;
    }

    /**
     *
     *
     */
    private void computeGrid() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Point2D shiftVector = calculateShifts(new DirectPosition2D((i * globalValues.dx) +
                            globalValues.xmin,
                            (j * globalValues.dy) + globalValues.ymin));

                double x = i + shiftVector.getX();
                double y = j + shiftVector.getY();

                gridShift.setLocalizationPoint(i, j, x, y);
                warpPositions[2*(i+1)*(j+1)-1] =  (float)x;
                warpPositions[2*(i+1)*(j+1)]   =  (float)y;
             //   dxgrid[i][j] = (new Double(shiftVector.getX())).floatValue();
             //   dygrid[i][j] = (new Double(shiftVector.getY())).floatValue();
            }
        }
        
    }

    public float[][] getDxGrid() {
        return dxgrid;
    }

    public float[][] getDyGrid() {
        return dygrid;
    }

    /**
     * Calculates the real point shift from the iregular pairs of MappedPositions using
     * Inverse distance weighting interpolation. The distance is cartesian.
     * @param p position where we reqaire the shift to be calculated
     * @return
     */
    private Point2D calculateShifts(Point2D p) {
        HashMap nearest = getNearestMappedPositions(p, 2);

        double dx;
        double sumdx = 0;
        double dy = 0;
        double sumdy = 0;
        double sumweight = 0;

        for (Iterator i = nearest.keySet().iterator(); i.hasNext();) {
            MappedPosition mp = (MappedPosition) i.next();
            double distance = ((Double) nearest.get(mp)).doubleValue();
            double weight = (1 / Math.pow(distance, 2));

            if (distance > 0.000001) {
                sumdx = sumdx +
                    ((mp.getSource().getCoordinates()[0] -
                    mp.getTarget().getCoordinates()[0]) * weight);
                sumdy = sumdy +
                    ((mp.getSource().getCoordinates()[1] -
                    mp.getTarget().getCoordinates()[1]) * weight);

                sumweight = sumweight + weight;
            } else {
                dx = (mp.getSource().getCoordinates()[0] -
                    mp.getTarget().getCoordinates()[0]);
                dy = (mp.getSource().getCoordinates()[1] -
                    mp.getTarget().getCoordinates()[1]);

                return (new DirectPosition2D(dx, dy));
            }
        }

        dx = sumdx / sumweight;
        dy = sumdy / sumweight;

        return (new DirectPosition2D(dx, dy));
    }

    /**
     *
     * @param p
     * @param maxdistance
     * @return
     */
    private HashMap getNearestMappedPositions(Point2D p, double maxdistance) {
        return getNearestMappedPositions(p, maxdistance,
            this.getSourcePoints().length);
    }

    /**
     *
     * @param p
     * @param maxnumber
     * @return
     */
    private HashMap getNearestMappedPositions(Point2D p, int maxnumber) {
        return getNearestMappedPositions(p,
            (envelope.getLength(0) + envelope.getLength(1)),
            this.getSourcePoints().length);
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
    private HashMap getNearestMappedPositions(Point2D p, double maxdistance,
        int maxnumber) {
        HashMap nearest = new HashMap();
        MappedPosition mp = null;

        List l = this.getMappedPositions();

        for (Iterator i = this.getMappedPositions().iterator(); i.hasNext();) {
            mp = (MappedPosition) i.next();

            double dist = p.distance((Point2D) mp.getSource());

            if ((dist < maxdistance) && (nearest.size() < maxnumber)) {
                nearest.put(mp, new Double(dist));
            }
        }

        return nearest;
    }
   
    private class GridGlobalValues {
        /**
        * The minimum longitude value covered by this grid (decimal degrees)
        */
        private double xmin;

        /**
         * The minimum latitude value covered by this grid (decimal degrees)
         */
        private double ymin;

        /**
         * The maximum longitude value covered by this grid (decimal degrees)
         */
        private double xmax;

        /**
         * The maximum latitude value covered by this grid (decimal degrees)
         */
        private double ymax;

        /**
         * The difference between longitude grid points (decimal degrees)
         */
        private double dx;

        /**
         * The difference between latitude grid points (decimal degrees)
         */
        private double dy;

        /**
         * The horizontal spacing between grid cells.
         */
        private int XStep;

        /**
         *         The vertical spacing between grid cells.
         */
        private int YStep;

        /**
         * TODO -Replace this with ParametrValueGroup 
             * Constructs ahelper class for handling global parameters.
             * @param xmin xmin
             * @param ymin ymin
             * @param xmax xmax
             * @param ymax ymax
             * @param dx dx
             * @param dy dy
             */
        public GridGlobalValues(double xmin, double ymin, double xmax,
            double ymax, double dx, double dy) {
            super();
            this.xmin = xmin;
            this.ymin = ymin;
            this.xmax = xmax;
            this.ymax = ymax;
            this.dx = dx;
            this.dy = dy;

            height = (int) Math.floor((xmax - xmin) / dx);
            width = (int) Math.floor((ymax - ymin) / dy);
        }

        public double getDx() {
            return dx;
        }

        public void setDx(double dx) {
            this.dx = dx;
        }

        public double getDy() {
            return dy;
        }

        public void setDy(double dy) {
            this.dy = dy;
        }

        public double getXmax() {
            return xmax;
        }

        public void setXmax(double xmax) {
            this.xmax = xmax;
        }

        public double getXmin() {
            return xmin;
        }

        public void setXmin(double xmin) {
            this.xmin = xmin;
        }

        public double getYmax() {
            return ymax;
        }

        public void setYmax(double ymax) {
            this.ymax = ymax;
        }

        public double getYmin() {
            return ymin;
        }

        public void setYmin(double ymin) {
            this.ymin = ymin;
        }

        public int getGridWidth() {
            return width;
        }

        public int getGridHeight() {
            return height;
        }

        public int getXStep() {
            return XStep;
        }

        public void setXStep(int step) {
            XStep = step;
        }

        public int getYStep() {
            return YStep;
        }

        public void setYStep(int step) {
            YStep = step;
        }
    }
}
