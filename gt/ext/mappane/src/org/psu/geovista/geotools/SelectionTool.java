package org.psu.geovista.geotools;

import com.vividsolutions.jts.geom.Envelope;
import edu.psu.geovista.geotools.filter.model.FilterModel;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import javax.swing.event.EventListenerList;
import org.geotools.filter.expression.BBoxExpression;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.IllegalFilterException;
import org.geotools.geometry.JTS;
import org.geotools.gui.swing.JMapPane;
import org.geotools.gui.swing.event.GeoMouseEvent;
import org.geotools.gui.tools.AbstractTool;
import org.geotools.gui.tools.Tool;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
//import org.geotools.pt.CoordinatePoint;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.referencing.FactoryFinder;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

//import org.geotools.ct.TransformException;

/**
 * 
 * @author jfc173
 */
public class SelectionTool extends AbstractTool implements Tool {
    protected FilterModel model;

    /** The Mouse button press down point in a click/drag operation */
    protected GeneralDirectPosition pressPoint;

    /** The Mouse button release point in a click/drag operation */
    protected GeneralDirectPosition releasePoint;

    protected GeneralDirectPosition mouseLocation;

    protected Point2D transformedPress, transformedRelease;

    protected FilterFactory fact = FilterFactoryFinder.createFilterFactory();

    protected BBoxExpression exp;

    protected Envelope bboxEnv, screenBox;

    protected Filter filter;

    protected EventListenerList ell = new EventListenerList();

    /** Creates a new instance of SelectionTool */
    public SelectionTool() {
        setName("Select");
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        model = new FilterModel();// needs to be replaced by user.
    }

    public void addMouseListener(Component component, MapContext context) {
        super.addMouseListener(component, context, this);
    }

    public void addMouseMotionListener(Component component, MapContext context)
            throws IllegalArgumentException {
        super.addMouseMotionListener(component, context, this);
    }

    public void setFilterModel(FilterModel model) {
        this.model = model;
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     * 
     */
    public void mousePressed(MouseEvent e) {
        try {
            // System.out.println("Selection tool thinks mouse was pressed.");
            pressPoint = ((GeoMouseEvent) e).getMapCoordinate(pressPoint);
            //JMapPane mpi = (JMapPane) e.getSource();
            transformedPress = e.getPoint();
        } catch (TransformException t) {
            throw new RuntimeException(
                    "Transform exception prevented mouse click from being processed");
        }
    }

    /**
     * Invoked when a mouse button is pressed on a component and then dragged.
     * <code>MOUSE_DRAGGED</code> events will continue to be delivered to the
     * component where the drag originated until the mouse button is released
     * (regardless of whether the mouse position is within the bounds of the
     * component).
     * <p>
     * Due to platform-dependent Drag&Drop implementations,
     * <code>MOUSE_DRAGGED</code> events may not be delivered during a native
     * Drag&Drop operation.
     * 
     */
    public void mouseDragged(MouseEvent e) {
        try {
            // System.out.println("Selection tool thinks mouse is dragging.");
            releasePoint = ((GeoMouseEvent) e).getMapCoordinate(releasePoint);
            JMapPane mpi = (JMapPane) e.getSource();
            transformedRelease = e.getPoint(); //mpi.toScreenPoint(releasePoint.toPoint2D());
            System.err.println("press: " + transformedPress);
            System.err.println("release: " + transformedRelease + " geo:"
                    + releasePoint + " evt" + e.getPoint());
            processBBox(mpi);
            updateModel(filter, e, true);
        } catch (TransformException te) {
            throw new RuntimeException(
                    "Transform exception while dragging the mouse: "
                            + te.getMessage());
        }
    }

    protected void updateModel(Filter f, MouseEvent e, boolean active) {
        if (e.isShiftDown()) {
            model.expandFilter(filter, active);
        } else if (e.isControlDown()) {
            model.subtractFilter(filter, active);
        } else {
            model.setFilter(filter, active);
        }
    }

    public void mouseMoved(MouseEvent e) {
        try {
            mouseLocation = ((GeoMouseEvent) e).getMapCoordinate(mouseLocation);
            fireIndication();
        } catch (TransformException te) {
            throw new RuntimeException(
                    "Transform exception while moving the mouse: "
                            + te.getMessage());
        }
    }

    /**
     * Invoked when a mouse button has been released on a component.
     * 
     */
    public void mouseReleased(MouseEvent e) {
        try {
            // System.out.println("Selection tool thinks mouse has released.");
            releasePoint = ((GeoMouseEvent) e).getMapCoordinate(releasePoint);
            //JMapPane mpi = (JMapPane) e.getSource();
            transformedRelease = e.getPoint();//mpi.toScreenPoint(releasePoint.toPoint2D());
            processBBox((JMapPane) e.getSource());
            updateModel(filter, e, false);
            screenBox = null;
        } catch (TransformException te) {
            throw new RuntimeException(
                    "Transform exception while dragging the mouse: "
                            + te.getMessage());
        }
    }

    public void mouseClicked(MouseEvent e) {
        try {
            // System.out.println("Selection tool thinks mouse was clicked.");
            releasePoint = ((GeoMouseEvent) e).getMapCoordinate(releasePoint);
            filter = fact.createGeometryFilter(Filter.GEOMETRY_CONTAINS);
            // Adding 0.1 to the release point coordinates in order to avoid an
            // empty bounding box is a bit hackish
            // but it works.... For that matter, it's probably better that I
            // somehow use a point instead of a bbox.
            Envelope env = new Envelope(pressPoint.toPoint2D().getX(),
                    releasePoint.toPoint2D().getX() + 0.1, pressPoint
                            .toPoint2D().getY(), releasePoint.toPoint2D()
                            .getY() + 0.1);
            exp = fact.createBBoxExpression(env);
            ((GeometryFilter) filter).addRightGeometry(exp);
            fireEvent("filter");
            updateModel(filter, e, false);
            screenBox = null;
        } catch (TransformException te) {
            throw new RuntimeException(
                    "Transform exception while clicking the mouse: "
                            + te.getMessage());
        } catch (IllegalFilterException ife) {
            throw new RuntimeException(
                    "Illegal filter exception while creating the selection point.");
        }
    }

    protected void processBBox(JMapPane mpi) {
        try {
            bboxEnv = new Envelope(pressPoint.toPoint2D().getX(), releasePoint
                    .toPoint2D().getX(), pressPoint.toPoint2D().getY(),
                    releasePoint.toPoint2D().getY());
            screenBox = new Envelope(transformedPress.getX(),
                    transformedRelease.getX(), transformedPress.getY(),
                    transformedRelease.getY());

            /*
             * MultiMapPaneImpl mm = (MultiMapPaneImpl)mpi; MapContext context =
             * mm.getContext(); MapLayer l =
             * context.getLayer(context.getLayerCount()-1);
             * 
             * CoordinateReferenceSystem layerCrs =
             * l.getFeatureSource().getSchema().getDefaultGeometry().getCoordinateSystem();
             * CoordinateReferenceSystem contextCrs =
             * context.getCoordinateReferenceSystem(); if ((contextCrs != null) &&
             * (layerCrs != null) && !contextCrs.equals(layerCrs)) { try{
             * MathTransform transform =
             * FactoryFinder.getCoordinateOperationFactory(null)
             * .createOperation(contextCrs, layerCrs).getMathTransform();
             * //MathTransform transform = CRS.transform(contextCrs,layerCrs);
             * 
             * if (transform != null) {
             * 
             * bboxEnv = JTS.transform(bboxEnv, transform,20); //JTS. } }
             * catch(Exception e){ //will have to leave it untransformed! } }
             * 
             */
            exp = fact.createBBoxExpression(bboxEnv);
            GeometryFilter gFilter = fact
                    .createGeometryFilter(Filter.GEOMETRY_DISJOINT);
            gFilter.addLeftGeometry(exp);
            filter = gFilter.not();
            fireEvent("filter");
        } catch (IllegalFilterException ife) {
            throw new RuntimeException(
                    "Illegal filter exception while creating the selection bounding box: "
                            + ife.getMessage());
        }
    }

    public Filter getFilter() {
        return filter;
    }

    public Envelope getBounds() {
        return bboxEnv;
    }

    public GeneralDirectPosition getIndicationLocation() {
        return mouseLocation;
    }

    public void addActionListener(ActionListener sl) {
        ell.add(ActionListener.class, sl);
    }

    public void removeActionListener(ActionListener sl) {
        ell.remove(ActionListener.class, sl);
    }

    public void addSelectionFilterChangeListener(
            SelectionFilterChangeListener scl) {
        ell.add(SelectionFilterChangeListener.class, scl);
    }

    public void fireEvent(String s) {
        Object[] listeners = ell.getListenerList();
        int numListeners = listeners.length;
        ActionEvent se = new ActionEvent(this, 42, s);
        for (int i = 0; i < numListeners; i++) {
            if (listeners[i] == ActionListener.class) {
                // pass the event to the listeners event dispatch method
                ((ActionListener) listeners[i + 1]).actionPerformed(se);
            }
        }

    }

    public void fireSelectionFilterEvent(int type, boolean isFinished) {
        SelectionFilterChangeEvent sce = new SelectionFilterChangeEvent(filter,
                this, type, isFinished);
        Object[] listeners = ell.getListenerList();
        int numListeners = listeners.length;
        // ActionEvent se = new ActionEvent(this, 42, s);
        for (int i = 0; i < numListeners; i++) {
            if (listeners[i] == SelectionFilterChangeListener.class) {
                // pass the event to the listeners event dispatch method
                ((SelectionFilterChangeListener) listeners[i + 1])
                        .selectionChanged(sce);
            }
        }
    }

    public void fireIndication() {
        // System.out.println("Selection tool firing indication location.");
        Object[] listeners = ell.getListenerList();
        int numListeners = listeners.length;
        ActionEvent se = new ActionEvent(this, 42, "indication");
        for (int i = 0; i < numListeners; i++) {
            if (listeners[i] == ActionListener.class) {
                // pass the event to the listeners event dispatch method
                ((ActionListener) listeners[i + 1]).actionPerformed(se);
            }
        }
    }

    public void paint(Graphics g) {
        // System.out.println("Selection tool should be painting this box:" +
        // screenBox);
        if (screenBox != null) {
            g.drawRect((int) Math.round(screenBox.getMinX()), (int) Math
                    .round(screenBox.getMinY()), (int) Math.round(screenBox
                    .getWidth()), (int) Math.round(screenBox.getHeight()));
        }

    }

}
