package org.geotools.gui.tools;

import com.vividsolutions.jts.geom.Envelope;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.logging.Logger;
import javax.swing.event.MouseInputAdapter;
import javax.swing.JComponent;
import org.geotools.ct.Adapters;
import org.geotools.ct.MathTransformFactory;
import org.geotools.ct.MathTransform;
import org.geotools.ct.TransformException;
import org.geotools.gui.swing.event.GeoMouseEvent;
import org.geotools.gui.tools.MouseToolImpl;
//import org.geotools.gui.widget.Widget;
import org.geotools.map.BoundingBox;
import org.geotools.map.Context;
import org.geotools.pt.CoordinatePoint;

public class PanToolImpl extends AbstractToolImpl implements PanTool {

    private static final Logger LOGGER = Logger.getLogger(
        "org.geotools.gui.tools.PanToolImpl");

    private Adapters adapters = Adapters.getDefault();

    /**
     * Construct a PanTool.
     * @version $Id: PanToolImpl.java,v 1.13 2003/03/28 19:08:51 camerons Exp $
     * @author Cameron Shorter
     */
    public PanToolImpl(){
        setName("Pan");
    }
    
    /**
     * Set up Click/Pan.
     * Pan the map so that the new extent has the click point in the middle
     * of the map.
     * @param e The mouse clicked event.
     * @throws IllegalStateException If context has not been
     * initialized yet.
     * @task HACK The transform doesn't seem to take account of CoordinateSystem
     * so it would be possible to create Coordinates which are outside real
     * world coordinates.
     * @task TODO Use an AffineTransform type interface to Bbox instead of
     * setAreaOfInterest
     */
    public void mouseClicked(MouseEvent e) {
        GeoMouseEvent geoMouseEvent=(GeoMouseEvent)e;
        AffineTransform at = new AffineTransform();
        Envelope aoi=context.getBbox().getAreaOfInterest();
        
        try {
            // The real world coordinates of the mouse click
            CoordinatePoint mousePoint=geoMouseEvent.getMapCoordinate(null);

            // The real world coordinates of the AreaOfInterest
            Point2D minP=new Point2D.Double(aoi.getMinX(),aoi.getMinY());
            Point2D maxP=new Point2D.Double(aoi.getMaxX(),aoi.getMaxY());

            // Calculate the panning translation.
            // PannedPoint=OrigPoint+ClickedPoint-Midpoint
            at.translate(
                mousePoint.getOrdinate(0)-(minP.getX()+maxP.getX())/2,
                mousePoint.getOrdinate(1)-(minP.getY()+maxP.getY())/2);

            MathTransform transform=
                MathTransformFactory.getDefault().createAffineTransform(at);
            
            context.getBbox().transform(adapters.export(transform));
        } catch (TransformException t) {
            LOGGER.warning(
            "Transform exception prevented mouseClicks from being processed");
        }
    }

    /*
     * Set up Click and Drap Pan.
     */
    //public void ...
    
    
    /**
     * Register this tool to receive MouseEvents from <code>component<code>.
     * @param component The tool will process mouseEvents from this component.
     * @param context The Context that will be changed by this Tool.
     * @throws IllegalArgumentException if an argument is <code>null</code>
     * or the tool is being assigned a different context to before.
     */
    public void addMouseListener(Component component, Context context)
    {
        super.addMouseListener(
            component,context,this);
    }   
}
