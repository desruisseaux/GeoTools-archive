package org.geotools.referencing.operation.builder;

import java.awt.image.RenderedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.media.jai.RenderedOp;

import org.geotools.coverage.FactoryFinder;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.processing.AbstractProcessor;
import org.geotools.coverage.processing.DefaultProcessor;
import org.geotools.coverage.processing.Operations;
import org.geotools.factory.Hints;
import org.geotools.gce.image.WorldImageReader;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultDerivedCRS;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.cs.DefaultCartesianCS;
import org.geotools.referencing.operation.DefaultOperationMethod;
import org.geotools.referencing.operation.transform.AbstractMathTransform;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;


public class GridDemo {
	
	static public List/*<MappedPositions>*/ generateMappedPositions(Envelope env, int number, double deltas){
		List/*<MappedPositions>*/ vectors = new ArrayList();
		double minx = env.getLowerCorner().getCoordinates()[0];
		double miny = env.getLowerCorner().getCoordinates()[1];
		
		double maxx = env.getUpperCorner().getCoordinates()[0];
		double maxy = env.getUpperCorner().getCoordinates()[1];
		
		for (int i = 0; i < number;i++){
			double x = minx+Math.random()*(maxx-minx);
			double y = miny+Math.random()*(maxy-miny);
			vectors.add(
					new MappedPosition(new DirectPosition2D(x , y),							                               
		                              new DirectPosition2D(x+Math.random()*deltas-Math.random()*deltas,
		                            		               y+Math.random()*deltas-Math.random()*deltas)));
		}
		
		return vectors;
	}
	static public GridCoverage2D generateCoverage2D(int row, int cells, Envelope env){
		 float[][] raster = new float[row][cells];
		for (int j = 0; j < row; j++) {
            for (int i = 0; i < cells; i++) {                
               	raster[j][i]=0;
            }
        }
		for (int j = 1; j < row-1; j=j+20) {
            for (int i = 1; i < cells-1; i++) {                
               	raster[j][i]=100;
               	raster[j+1][i]=60;
            	raster[j-1][i]=60;
            }
        }
		for (int j = 1; j < row-1; j++) {
            for (int i = 1; i < cells-1; i=i+20) {                
               	raster[j][i]=100;
              	raster[j][i+1]=60;
            	raster[j][i-1]=60;
            	
            }
        }
		
		GridCoverage2D cov = (new GridCoverageFactory()).create("name", raster, env);

		return cov;
		
	}
    public static void main(String[] args) {
     
        CoordinateReferenceSystem realCRS = null;

  
        try {
            //  MathTransform2D realToGrid = gridShift.getMathTransform();
            realCRS = DefaultGeographicCRS.WGS84;
            CoordinateReferenceSystem  crs = DefaultEngineeringCRS.CARTESIAN_2D;
            List mp = new ArrayList();
       
            GridCoverageFactory factory = FactoryFinder.getGridCoverageFactory(null);

            URL url = null;

             url = new File("/home/jezekjan/gsoc/geodata/p1010099.tif").toURL();             
             
           // url = new File("/media/sda5/Dokumenty/geodata/rasters/Mane_3_1_4.tif").toURL();
            WorldImageReader reader = new WorldImageReader(url);
            Operations operations = new Operations(null);                               
            GridCoverage2D coverage = (GridCoverage2D)reader.read(null);
            Envelope env = coverage.getEnvelope();
            
           //coverage = GridCoverageExamples.getExample(0);
            
            System.out.println(coverage.getGridGeometry().getGridRange());
            List vectors = generateMappedPositions(env,40, 0.03);
            
            WarpGridBuilder gridBuilder = new TPSGridBuilder(vectors, 0.003,0.003, env, coverage.getGridGeometry().getGridToCRS().inverse());
              
            (new GridCoverageFactory()).create("DX",gridBuilder.getDxGrid(),coverage.getEnvelope()).show();
            (new GridCoverageFactory()).create("DY",gridBuilder.getDyGrid(),coverage.getEnvelope()).show();
         
            MathTransform  trans = gridBuilder.getMathTransform();
                 
   //////******************New reference System***************************///////
            
            CoordinateReferenceSystem gridCRS = new DefaultDerivedCRS("gridCRS",
                    new DefaultOperationMethod(
                 		  trans),
                 		   coverage.getCoordinateReferenceSystem(),
                 		   trans,
                     DefaultCartesianCS.GENERIC_2D);
           
          
   //////******************Show Source***************************///////
                    
           // coverage.show();
            
            Envelope envelope = CRS.transform(coverage.getGridGeometry().getGridToCRS().inverse(),coverage.getEnvelope());
             
            GridCoverage2D target1 = projectTo((GridCoverage2D)coverage,  gridCRS, (GridGeometry2D)coverage.getGridGeometry(), null, false);
           
            target1.show();
         
/*
         WorldImageWriter writer = new WorldImageWriter((Object) (new File(
                      "/home/jezekjan/gsoc/geodata/p.tif")));
            
           writer.write(target1, null);*/
           
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static GridCoverage2D projectTo(final GridCoverage2D coverage,
        final CoordinateReferenceSystem targetCRS,
        final GridGeometry2D geometry, final Hints hints,
        final boolean useGeophysics) {
        final AbstractProcessor processor = (hints != null)
            ? new DefaultProcessor(hints) : AbstractProcessor.getInstance();
        final String arg1;
        final Object value1;
        final String arg2;
        final Object value2;

        if (targetCRS != null) {
            arg1 = "CoordinateReferenceSystem";
            value1 = targetCRS;

            if (geometry != null) {
                arg2 = "GridGeometry";
                value2 = geometry;
            } else {
                arg2 = "InterpolationType";
                value2 = "bilinear";
            }
        } else {
            arg1 = "GridGeometry";
            value1 = geometry;
            arg2 = "InterpolationType";
            value2 = "bilinear";
        }

        GridCoverage2D projected = coverage.geophysics(useGeophysics);
        final ParameterValueGroup param = processor.getOperation("Resample")
                                                   .getParameters();
        param.parameter("Source").setValue(projected);
        param.parameter(arg1).setValue(value1);
        param.parameter(arg2).setValue(value2);
      
        projected = (GridCoverage2D) processor.doOperation(param);

        final RenderedImage image = projected.getRenderedImage();
        projected = projected.geophysics(false);

        String operation = null;

        if (image instanceof RenderedOp) {
            operation = ((RenderedOp) image).getOperationName();
            AbstractProcessor.LOGGER.fine("Applied \"" + operation +
                "\" JAI operation.");
        }
        
                
        
           // Viewer.show(projected, operation);
        return projected;
       // return operation;
    }
}
