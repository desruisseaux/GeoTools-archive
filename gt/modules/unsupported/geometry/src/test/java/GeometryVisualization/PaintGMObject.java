/*
 * Created on 09/11/2004
 */
package GeometryVisualization;




import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.geotools.geometry.iso.aggregate.MultiSurfaceImpl;
import org.geotools.geometry.iso.coordinate.DirectPositionImpl;
import org.geotools.geometry.iso.primitive.CurveImpl;
import org.geotools.geometry.iso.primitive.PointImpl;
import org.geotools.geometry.iso.primitive.RingImpl;
import org.geotools.geometry.iso.primitive.SurfaceImpl;
import org.geotools.geometry.iso.root.GeometryImpl;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.primitive.Ring;
import org.opengis.spatialschema.geometry.primitive.SurfaceBoundary;

/**
 * @author Sanjay Jena
 *
 */
public class PaintGMObject {

    public static final int TYPE_POINT = 1;
    public static final int TYPE_LINES = 2;
    public static final int TYPE_LINESTRING = 3;

    
    /**
     * Paints an GM_Object
     * @param object
     */
    public static void paint(GeometryImpl object) {
        PaintGMObject p = new PaintGMObject();
        p.startPainting(object);
    }

    
    public PaintGMObject() {
    }
    
    public void startPainting(GeometryImpl object) {

        int object_x[] = null;
        int object_y[] = null;
        int object_type = 0;
        int scale = 3;
        
        /***** GM_Curve *****/
        if (object instanceof CurveImpl) {
            Coords coordArray = getCoordsFromCurve((CurveImpl) object);
            object_x = coordArray.getXArray();
            object_y = coordArray.getYArray();
            object_type = PaintGMObject.TYPE_LINESTRING;

        /***** GM_Point *****/
        } else if (object instanceof PointImpl) {
            object_x = new int[1];
            object_y = new int[1];
            DirectPositionImpl pos = (DirectPositionImpl) ((PointImpl) object).getPosition();
            object_x[0] = (int) pos.getX();
            object_y[0] = (int) pos.getY();

            object_type = PaintGMObject.TYPE_POINT;
            
        } else if (object instanceof SurfaceImpl) {
        	
            object_type = PaintGMObject.TYPE_LINES;

            SurfaceBoundary sb = ((SurfaceImpl) object).getBoundary();
            Ring exterior = (RingImpl) sb.getExterior();
            
            LineList coords = new LineList();
            
            coords.addRingToCoords((RingImpl) exterior);
            
        	List<Ring> interiors = sb.getInteriors();

        	for (int i=0; i<interiors.size(); i++) {
                coords.addRingToCoords((RingImpl) interiors.get(i));
        	}
        	
        	object_x = new int[coords.size()*2];
        	object_y = new int[coords.size()*2];

        	int z = 0;
        	for (int i=0; i<coords.size(); i++) {
        		object_x[z] = coords.getXFrom(i);
        		object_y[z] = coords.getYFrom(i);
        		z++;
        		object_x[z] = coords.getXTo(i);
        		object_y[z] = coords.getYTo(i);
        		z++;
        	}
            
        } else if (object instanceof MultiSurfaceImpl) {

            object_type = PaintGMObject.TYPE_LINES;
            
            Iterator surfaces = ((MultiSurfaceImpl)object).getElements().iterator();

            LineList coords = new LineList();
            
            while (surfaces.hasNext()) {
            	
                SurfaceBoundary sb = ((SurfaceImpl)surfaces.next()).getBoundary();
                Ring exterior = (RingImpl) sb.getExterior();
                
                coords.addRingToCoords((RingImpl) exterior);
                
            	List<Ring> interiors = sb.getInteriors();

            	for (int i=0; i<interiors.size(); i++) {
                    coords.addRingToCoords((RingImpl) interiors.get(i));
            	}
            }
        	
        	object_x = new int[coords.size()*2];
        	object_y = new int[coords.size()*2];

        	int z = 0;
        	for (int i=0; i<coords.size(); i++) {
        		object_x[z] = coords.getXFrom(i);
        		object_y[z] = coords.getYFrom(i);
        		z++;
        		object_x[z] = coords.getXTo(i);
        		object_y[z] = coords.getYTo(i);
        		z++;
        	}

            
        }
        
        	
//        /***** GM_Ring *****/
//        else if (object instanceof RingImpl) {
//        	ArrayList<OrientableCurve> curves = ((RingImpl) object).getGenerators();
//            Coords coordArray[] = new Coords[curves.size()];
//            int numberPoints = 0;
//            /* Create Array with all Coords, each representing a Curve */
//            for (int i=0; i<curves.size(); i++) {
//                coordArray[i] = getCoordsFromCurve((CurveImpl) curves.get(i));
//                numberPoints += coordArray[i].length();
//            }
//            /* Copying all Points from the Coords into one cummulative Array */
//            object_x = new int[numberPoints];
//            object_y = new int[numberPoints];
//            int index=0;
//            for (int j=0; j<coordArray.length; j++) {
//                int tmpX[] = coordArray[j].getXArray();
//                int tmpY[] = coordArray[j].getYArray();
//                for (int k=0; k<coordArray[j].length(); k++) {
//                    object_x[index] = tmpX[k]; 
//                    object_y[index] = tmpY[k];
//                    index++;
//                }
//            }
//            
//            object_type = PaintGMObject.TYPE_LINESTRING;
//
//        /***** GM_Tin *****/
//        } else if (object instanceof TinImpl) {
//        	ArrayList<Triangle> triangles = ((TinImpl)object).getTriangles();
//            object_x = new int[6*triangles.size()];
//            object_y = new int[6*triangles.size()];
//            int index=0;
//            for (int j=0; j<triangles.size(); j++) {
//                PositionImpl c[] = triangles.get(j).getCorners();
//                /* First line */
//                object_x[index] = (int)((DirectPositionImpl)c[0].getDirectPosition()).getX();
//                object_y[index] = (int)((DirectPositionImpl)c[0].getDirectPosition()).getY();
//                index++;
//                object_x[index] = (int)((DirectPositionImpl)c[1].getDirectPosition()).getX();
//                object_y[index] = (int)((DirectPositionImpl)c[1].getDirectPosition()).getY();
//                index++;
//
//                /* Second line */
//                object_x[index] = (int)((DirectPosition)c[1].getDirectPosition()).getX();
//                object_y[index] = (int)((DirectPosition)c[1].getDirectPosition()).getY();
//                index++;
//                object_x[index] = (int)((DirectPosition)c[2].getDirectPosition()).getX();
//                object_y[index] = (int)((DirectPosition)c[2].getDirectPosition()).getY();
//                index++;
//
//                /* Third line */
//                object_x[index] = (int)((DirectPosition)c[2].getDirectPosition()).getX();
//                object_y[index] = (int)((DirectPosition)c[2].getDirectPosition()).getY();
//                index++;
//                object_x[index] = (int)((DirectPosition)c[0].getDirectPosition()).getX();
//                object_y[index] = (int)((DirectPosition)c[0].getDirectPosition()).getY();
//                index++;
//            
//            }
//            object_type = PaintGMObject.TYPE_LINES;
//            
//        }
        
        
        Envelope objEnvelope = object.getEnvelope();
        int height = (int)((DirectPositionImpl)objEnvelope.getUpperCorner()).getY();
        int width  = (int)((DirectPositionImpl)objEnvelope.getUpperCorner()).getX();
        
        /* Displaying Screen */
        ObjectScreen app1 =  new ObjectScreen();
        app1.pack();
        
        app1.paintObject(object_x, object_y, object_type, scale);
        app1.setSize(width*scale+15*scale+20,height*scale+35*scale+20);
        app1.setVisible(true);

        
    }
    
    

    private Coords getCoordsFromCurve(CurveImpl curve) {
    	List<DirectPositionImpl> dps = curve.asDirectPositions();
    	
        int xCoordsOut[] = new int[dps.size()];
        int yCoordsOut[] = new int[dps.size()];

        for (int j=0; j<dps.size(); j++) {
            DirectPositionImpl pos = dps.get(j);
            xCoordsOut[j] = (int) pos.getX();
            yCoordsOut[j] = (int) pos.getY();
        }
        
        return new Coords(xCoordsOut, yCoordsOut);
    }
    
    /**
     * Inner Class Coords
     * Storing two arrays. One for X-Coordinates, the other for Y-Coordiantes
     */
    public class Coords {
        private int x[];
        private int y[];
        public Coords(int x[], int y[]) {
            this.x = x;
            this.y = y;
        }
        public int[] getXArray() {
            return this.x;
        }
        public int[] getYArray() {
            return this.y;
        }
        public int length() {
            return x.length;
        }
    }
    
    public class Line {
    	private int x1, x2, y1, y2;
    	
    	public Line(int x1, int y1, int x2, int y2) {
    		this.x1 = x1;
    		this.x2 = x2;
    		this.y1 = y1;
    		this.y2 = y2;
    	}
    	
    	public int getX1() {
    		return this.x1;
    	}
    	
    	public int getX2() {
    		return this.x2;
    	}

    	public int getY1() {
    		return this.y1;
    	}
    	
    	public int getY2() {
    		return this.y2;
    	}
    	
    	
    }
    
    public class LineList {
    	
    	private ArrayList<Line> list = null;
    	
    	public LineList() {
    		this.list = new ArrayList<Line>();
    	}
    	
    	public void add(int x1, int y1, int x2, int y2) {
    		this.list.add(new Line(x1, y1, x2, y2));
    	}
    	
    	public Line getLine(int i) {
    		return this.list.get(i);
    	}
    	
    	public int getXFrom(int i) {
    		return this.list.get(i).getX1();
    	}
    	public int getXTo(int i) {
    		return this.list.get(i).getX2();
    	}
    	public int getYFrom(int i) {
    		return this.list.get(i).getY1();
    	}
    	public int getYTo(int i) {
    		return this.list.get(i).getY2();
    	}

    	public int size() {
    		return this.list.size();
    	}
    	
        public void addRingToCoords(RingImpl ring) {
        	
        	List<DirectPositionImpl> dps = ring.asDirectPositions();
        	
            for (int j=0; j<dps.size()-1; j++) {
                DirectPositionImpl pos = dps.get(j);
                DirectPositionImpl nextpos = dps.get(j+1);
                this.add((int)pos.getX(), (int)pos.getY(), (int)nextpos.getX(), (int)nextpos.getY());
            }
            
            return;
        }


    	
    }

}
