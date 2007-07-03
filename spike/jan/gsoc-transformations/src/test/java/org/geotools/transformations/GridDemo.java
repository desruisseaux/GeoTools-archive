package org.geotools.transformations;

import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.media.jai.RasterFactory;
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
import org.geotools.gce.image.WorldImageWriter;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.crs.DefaultDerivedCRS;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.cs.DefaultCartesianCS;
import org.geotools.referencing.operation.DefaultOperationMethod;
import org.geotools.referencing.operation.builder.LocalizationGridBuilder;
import org.geotools.referencing.operation.builder.MappedPosition;
import org.geotools.referencing.operation.transform.AbstractMathTransform;
import org.opengis.geometry.DirectPosition;
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
        //LocalizationGrid gridShift = new LocalizationGrid(4, 4);
 
       /*
        for (int j = 0; j < 4; j++) {
            for (int i = 0; i < 4; i++) {
                double x = i ;//;+ (c- Math.random()*2)/3 ;
                double y = j ;//;-  (Math.random()- Math.random()*2)/3;
                gridShift.setLocalizationPoint(i, j, x, y);
                
            }
        }
        */
        CoordinateReferenceSystem realCRS = null;

        //  CoordinateReferenceSystem gridCRS = null;
        try {
            //  MathTransform2D realToGrid = gridShift.getMathTransform();
            realCRS = DefaultGeographicCRS.WGS84;
            CoordinateReferenceSystem  crs = DefaultEngineeringCRS.CARTESIAN_2D;
            List mp = new ArrayList();
            //GridCoverage2D coverage = GridCoverageExamples.getExample(0);
           // Envelope env = coverage.getEnvelope();
            
                   
          
           
          // MathTransformBuilder builder = new RubberSheetBuilder(mp,quad);
          // MathTransformBuilder builder = new SimilarTransformBuilder(vectors);
          // MathTransform trans = builder.getMathTransform();
            //MathTransform  trans = CRS.findMathTransform( coverage.getCoordinateReferenceSystem(), CRS.decode("EPSG:2027"),true);
           
           // MathTransform  trans1 = gridShift.getMathTransform();//gridBuilder.getMathTransform();
           
            
            
                        
            //CoordinateReferenceSystem gridCRS = CRS.decode("EPSG:4156");
            GridCoverageFactory factory = FactoryFinder.getGridCoverageFactory(null);

            //  GridCoverage coverage = factory.create("s", raster, gridCRS, IdentityTransform.create(2), null);
            URL url = null;

            url = new File("/home/jezekjan/gsoc/geodata/p1010099.tif").toURL();
            
            WorldImageReader reader = new WorldImageReader(url);
            Operations operations = new Operations(null);                               
            GridCoverage2D coverage = (GridCoverage2D)reader.read(null);
            Envelope env = coverage.getEnvelope();
            List vectors = generateMappedPositions(env,10, 0.02);
            
            LocalizationGridBuilder gridBuilder = new LocalizationGridBuilder(vectors, 0.7,0.7, env, coverage.getGridGeometry().getGridToCRS().inverse());
              
            MathTransform  trans = gridBuilder.getMathTransform();
            
            
            System.out.println(((AbstractMathTransform)trans).getParameterDescriptors());
          
   //////******************New reference System***************************///////
            
            CoordinateReferenceSystem gridCRS = new DefaultDerivedCRS("gridCRS",
                    new DefaultOperationMethod(
                 		  trans),
                 		   coverage.getCoordinateReferenceSystem(),
                 		   trans,
                     DefaultCartesianCS.GENERIC_2D);
           
          
   //////******************Show Source***************************///////
                    
            coverage.show();
            
            
            //coverage.gridGeometry.
            //(new GridCoverageFactory()).create("",gridBuilder.getDxGrid(),coverage.getEnvelope()).show();
            //(new GridCoverageFactory()).create("",gridBuilder.getDyGrid(),coverage.getEnvelope()).show();
             
           // Viewer.show((GridCoverage2D)coverage, null);
            
           // Viewer.show((new GridCoverageFactory()).create("",gridBuilder.getDxGrid(),coverage.getEnvelope()), "DX");
            
           // Viewer.show((new GridCoverageFactory()).create("",gridBuilder.getDyGrid(),coverage.getEnvelope()), "DY");
            
         //   GridCoverage2D coverage2 = generateCoverage2D(460,460,coverage.getEnvelope()) ;           
            
            WritableRaster raster = RasterFactory.createBandedRaster(DataBuffer.TYPE_FLOAT,
        (int)coverage.getEnvelope2D().width, (int)coverage.getEnvelope2D().height, 1, null);
         
              
            GridCoverage2D target1 = projectTo((GridCoverage2D)coverage,  gridCRS, (GridGeometry2D)coverage.getGridGeometry(), null, false);
           
            target1.show();
        //    Viewer.show((GridCoverage2D)target1, "target1");
            
            
           // GridCoverage target2 = factory.create("My grayscale coverage",  coverage.image.copyData(), coverage.getCoordinateReferenceSystem(),
           	//	IdentityTransform.create(2) , null, null, null, null, null);
           
            // target2 = projectTo((GridCoverage2D) target2,  gridCRS, (GridGeometry2D)coverage.getGridGeometry(), null, false);
            
            //Viewer.show((GridCoverage2D)target2, "target2");
            // GridCoverage coverage2 = factory.create("s",  raster, gridCRS, IdentityTransform.create(2), null);
            //gridCRS = CRS.decode("EPSG:2065");
        /*    System.out.println(coverage.getCoordinateReferenceSystem());
            System.out.println("Targeeeet");
            System.out.println(gridCRS);
*/
          /*  coverage = (GridCoverage2D) Operations.DEFAULT.resample(coverage,
                    gridCRS);
/**/
           

            WorldImageWriter writer = new WorldImageWriter((Object) (new File(
                        "/home/jezekjan/gsoc/geodata/p.tif")));
            
            writer.write(target1, null);
           
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
