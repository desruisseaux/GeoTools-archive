/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.renderer.lite;

// J2SE dependencies
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

import javax.media.jai.ImageMIPMap;

import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.RenderedCoverage;
import org.geotools.coverage.processing.Operations;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.operation.matrix.MatrixFactory;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.geometry.XAffineTransform;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridRange;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.spatialschema.geometry.Envelope;


/**
 * A helper class for rendering {@link GridCoverage} objects. Still doesn't
 * support grid coverage SLD stylers
 *
 * @author Martin Desruisseaux
 * @author Andrea Aime
 * @source $URL$
 * @version $Id$
 *
 * @task Add support for SLD stylers
 */
public final class GridCoverageRenderer {
    /** Tells if we should try an optimisation using pyramidal images. */
    private static final boolean USE_PYRAMID = false;

    /**
     * Decimation factor for image. A value of 0.5 means that each level in the
     * image pyramid will contains an image with half the resolution of
     * previous level. This value is used only if {@link #USE_PYRAMID} is
     * <code>true</code>.
     */
    private static final double DOWN_SAMPLER = 0.5;

    /**
     * Natural logarithm of {@link #DOWN_SAMPLER}. Used only if {@link
     * #USE_PYRAMID} is <code>true</code>.
     */
    private static final double LOG_DOWN_SAMPLER = Math.log(DOWN_SAMPLER);

    /**
     * Minimum size (in pixel) for use of pyramidal image. Images smaller than
     * this size will not use pyramidal images, since it would not give many
     * visible benefict. Used only if {@link #USE_PYRAMID} is
     * <code>true</code>.
     */
    private static final int MIN_SIZE = 256;

    /** The grid coverage to be rendered. */
    private final GridCoverage gridCoverage;

    /** The Display (User defined) CRS **/
	private final CoordinateReferenceSystem destinationCRS;

    /**
     * A list of multi-resolution images. Image at level 0 is identical to
     * {@link GridCoverage#getRenderedImage()}.  Other levels contains the
     * image at lower resolution for faster rendering.
     */
    private final ImageMIPMap images;

    /** Maximum amount of level to use for multi-resolution images. */
    private final int maxLevel;

    /**
     * The renderered image that represents the grid coverage according to  the
     * current style setting
     */
    private RenderedImage image;
    
    protected  final static CoordinateOperationFactory opFactory = FactoryFinder.getCoordinateOperationFactory(null);
    
    /**
     * Creates a new GridCoverageRenderer object.
     *
     * @param gridCoverage DOCUMENT ME!
     */
    public GridCoverageRenderer(final GridCoverage gridCoverage, final CoordinateReferenceSystem destinationCRS) {
        this.gridCoverage = gridCoverage;
        this.destinationCRS = (destinationCRS != null ? destinationCRS : gridCoverage.getCoordinateReferenceSystem());

        if (gridCoverage instanceof GridCoverage2D) {
        	image = ((GridCoverage2D) gridCoverage).geophysics(false).getRenderedImage();
        }else if (gridCoverage instanceof RenderedCoverage) {
            image = ((RenderedCoverage) gridCoverage).getRenderedImage();
        } 

        if (USE_PYRAMID) {
            AffineTransform at = AffineTransform.getScaleInstance(
                    DOWN_SAMPLER, DOWN_SAMPLER);
            images = new ImageMIPMap(image, at, null);
        } else {
            images = null;
        }

        double maxSize = Math.max(image.getWidth(), image.getHeight());
        int logLevel = (int) (Math.log(MIN_SIZE / maxSize) / LOG_DOWN_SAMPLER);
        maxLevel = Math.max(logLevel, 0);
    }

    /**
     * Paint this grid coverage. The caller must ensure that
     * <code>graphics</code> has an affine transform mapping "real world"
     * coordinates in the coordinate system given by {@link
     * #getCoordinateSystem}.
     *
     * @param graphics the <code>Graphics</code> context in which to paint
     *
     * @throws UnsupportedOperationException if the transformation from grid to
     *         coordinate system in the GridCoverage is not an AffineTransform
     */
    public void paint(final Graphics2D graphics) {
    	
    	/**
    	 * STEP 1
    	 * setting the destination crs for the display device
    	 * 
    	 */
    	final CoordinateReferenceSystem displayCRS = this.destinationCRS;
    	
    	/**
    	 * STEP 2
    	 * converting the envelope of the provided coverage to the destiantion crs\
    	 * WHEN NEEDED!
    	 */
    	GeneralEnvelope newEnvelope = new GeneralEnvelope(
    			(GeneralDirectPosition) this.gridCoverage.getEnvelope().getLowerCorner(),
    			(GeneralDirectPosition) this.gridCoverage.getEnvelope().getUpperCorner()
    	);
    	newEnvelope.setCoordinateReferenceSystem(displayCRS);
    	try{
	    	//checking if we need tp transform coordinate reference system from source to display
	          //getting an operation between source and destination crs
	        final CoordinateOperation operation=opFactory.createOperation(
	        		this.gridCoverage.getCoordinateReferenceSystem(),
					displayCRS);
	        final MathTransform crsTransform=operation.getMathTransform();
	        
	
	        if( !crsTransform.isIdentity() ) {
	        	newEnvelope = CRSUtilities.transform(crsTransform, newEnvelope);
	        	newEnvelope.setCoordinateReferenceSystem(displayCRS);
	        }
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
    	
    	/**
    	 * STEP 3
    	 * Creating the parameters for the reprojection of the coverage.
    	 */
    	GridGeometry2D newGridGeometry = new GridGeometry2D(
    			this.gridCoverage.getGridGeometry().getGridRange(),
				(MathTransform) crsToDeviceGeometry(this.gridCoverage.getGridGeometry().getGridRange(), newEnvelope),
				newEnvelope.getCoordinateReferenceSystem()
    	);
    	GridCoverage2D prjGridCoverage = (GridCoverage2D) Operations.DEFAULT.resample(
    			this.gridCoverage,
    			displayCRS,
				newGridGeometry,
				null
    	);

        final MathTransform2D mathTransform =(MathTransform2D) prjGridCoverage.getGridGeometry().getGridToCoordinateSystem();

        if (!(mathTransform instanceof AffineTransform)) {
            throw new UnsupportedOperationException(
                "Non-affine transformations not yet implemented"); // TODO
        }

        final AffineTransform gridToDevice = new AffineTransform((AffineTransform) mathTransform);

        //gridToDevice.concatenate(crsToDeviceGeometry(this.gridCoverage.getGridGeometry().getGridRange(), newEnvelope));
        
        if (images == null) {
        	AffineTransform transform = new AffineTransform(gridToDevice);
            transform.translate(-0.5, -0.5); // Map to upper-left corner.

            try {
                graphics.drawRenderedImage(image, transform);
            } catch (Exception e) {
                image = getGoodImage(image);
                graphics.drawRenderedImage(image, transform);
            }

        } else {
            /*
             * Compute the most appropriate level as a function of the required
             * resolution
             */
            AffineTransform transform = graphics.getTransform();
            transform.concatenate(gridToDevice);

            double maxScale = Math.max(
                    XAffineTransform.getScaleX0(transform),
                    XAffineTransform.getScaleY0(transform));
            int level = Math.min(
                    maxLevel, (int) (Math.log(maxScale) / LOG_DOWN_SAMPLER));

            if (level < 0) {
                level = 0;
            }

            /*
             * If using an inferior resolution to speed up painting adjust
             * georeferencing
             */
            transform.setTransform(gridToDevice);

            if (level != 0) {
                final double scale = Math.pow(DOWN_SAMPLER, -level);
                transform.scale(scale, scale);
            }

            transform.translate(-0.5, -0.5); // Map to upper-left corner.
            graphics.drawRenderedImage(images.getImage(level), transform);
        }
    }

	/**
	 * @param gridRange
	 * @return
	 */
	private AffineTransform crsToDeviceGeometry(final GridRange gridRange, final GeneralEnvelope userRange) {
        final int dimension = gridRange.getDimension();
    	final CoordinateSystem cs = userRange.getCoordinateReferenceSystem().getCoordinateSystem();
    	boolean lonFirst = true;
    	
    	if (cs.getAxis(0).getDirection().absolute().equals(AxisDirection.NORTH)) {
    		lonFirst = false;
    	}
    	final boolean swapXY = false;
        // latitude index
        final int latIndex = lonFirst ? 1 : 0;

        final AxisDirection latitude = cs.getAxis(latIndex).getDirection();
        final AxisDirection longitude = cs.getAxis((latIndex + 1) % 2).getDirection();
        final boolean[] reverse = new boolean[] {false, true};
        
        /* 
         * Setup the multi-dimensional affine transform for use with OpenGIS.
         * According OpenGIS specification, transforms must map pixel center.
         * This is done by adding 0.5 to grid coordinates.
         */
        final Matrix matrix = MatrixFactory.create(dimension+1);
        for (int i=0; i<dimension; i++) {
            // NOTE: i is a dimension in the 'gridRange' space (source coordinates).
            //       j is a dimension in the 'userRange' space (target coordinates).
            int j = i;
            if (swapXY && j<=1) {
                j = 1-j;
            }
            double scale = userRange.getLength(j) / gridRange.getLength(i);
            double offset;
            if (reverse==null || !reverse[j]) {
                offset = userRange.getMinimum(j);
            } else {
                scale  = -scale;
                offset = userRange.getMaximum(j);
            }
            offset -= scale * (gridRange.getLower(i)-0.5);
            matrix.setElement(j, j,         0.0   );
            matrix.setElement(j, i,         scale );
            matrix.setElement(j, dimension, offset);
        }
        return (AffineTransform) ProjectiveTransform.create(matrix);
	}

	/**
     * Work around a bug in older JDKs
     *
     * @param img The JAI rendered image
     *
     * @return a buffered image that can be handled by Java2D without problems
     */
    private RenderedImage getGoodImage(RenderedImage img) {
        BufferedImage good = new BufferedImage(
                img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = (Graphics2D) good.getGraphics();
        g2d.drawRenderedImage(img, new AffineTransform());

        return good;
    }
}
