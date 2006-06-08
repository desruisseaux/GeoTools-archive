package edu.psu.geovista.swing;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import javax.swing.JPanel;

import org.geotools.map.MapContext;
import org.geotools.renderer.GTRenderer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class JMapPane extends JPanel implements MouseListener {
    /**
     * what renders the map
     */
    GTRenderer renderer;

    /**
     * the map context to render
     */
    MapContext context;

    /**
     * the area of the map to draw
     */
    Envelope mapArea;

    public static final int Reset = 0;

    public static final int ZoomIn = 1;

    public static final int ZoomOut = 2;

    public static final int Pan = 3;

    private int state = ZoomIn;

    private double zoomFactor = 2.0;

    public JMapPane() {
        this.addMouseListener(this);
    }

    public JMapPane(GTRenderer render, MapContext context) {
        this(null, true, render, context);
    }

    public JMapPane(LayoutManager layout, boolean isDoubleBuffered,
            GTRenderer render, MapContext context) {
        super(layout, isDoubleBuffered);
        this.renderer = render;
        this.context = context;
        this.addMouseListener(this);
    }

    public GTRenderer getRenderer() {
        return renderer;
    }

    public void setRenderer(GTRenderer renderer) {
        this.renderer = renderer;
        if (this.context != null) {
            renderer.setContext(this.context);
        }
    }

    public MapContext getContext() {
        return context;
    }

    public void setContext(MapContext context) {
        this.context = context;
        if (renderer != null) {
            renderer.setContext(this.context);
        }
    }

    public Envelope getMapArea() {
        return mapArea;
    }

    public void setMapArea(Envelope mapArea) {
        this.mapArea = mapArea;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
        System.out.println("State: " + state);
    }

    public double getZoomFactor() {
        return zoomFactor;
    }

    public void setZoomFactor(double zoomFactor) {
        this.zoomFactor = zoomFactor;
    }

    
protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // TODO: fix the bounds aspect ratio here!
        Rectangle r = getBounds();

        double mapWidth = mapArea.getWidth();
        double mapHeight = mapArea.getHeight();
        double scaleX = r.getWidth() / mapArea.getWidth();
        double scaleY = r.getHeight() / mapArea.getHeight();
        double scale = 1.0;
        if (scaleX < scaleY) {
            scale = scaleX;
        }else{
            scale=scaleY;
        }
            double deltaX = Math.abs((r.getWidth() / scale) - mapWidth);
            double deltaY = Math.abs((r.getHeight() / scale) - mapHeight);
    /*        System.out.println("delta x "+deltaX);
            System.out.println("delta y "+deltaY);*/
            Coordinate ll = new Coordinate(mapArea.getMinX() - (deltaX / 2.0),
                    mapArea.getMinY()  - (deltaY / 2.0));
            Coordinate ur = new Coordinate(mapArea.getMaxX() + (deltaX / 2.0),
                    mapArea.getMaxY()+ (deltaY / 2.0));
            mapArea = new Envelope(ll,ur);        

        renderer.paint((Graphics2D) g, r, mapArea);
    }    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub
        // System.out.println("before area "+mapArea+"\nw:"+mapArea.getWidth()+"
        // h:"+mapArea.getHeight());
        double x = (double) e.getX();
        double y = (double) e.getY();
        double width = mapArea.getWidth();
        double height = mapArea.getHeight();
        double width2 = mapArea.getWidth() / 2.0;
        double height2 = mapArea.getHeight() / 2.0;

        double mapX = (x * width / (double) this.getBounds().width)
                + mapArea.getMinX();
        double mapY = ((this.getBounds().getHeight() - y) * height / (double) this
                .getBounds().height)
                + mapArea.getMinY();
        /*
         * System.out.println(""+x+"->"+mapX);
         * System.out.println(""+y+"->"+mapY);
         */
        /*
         * Coordinate ll = new Coordinate(mapArea.getMinX(), mapArea.getMinY());
         * Coordinate ur = new Coordinate(mapArea.getMaxX(), mapArea.getMaxY());
         */
        double zlevel = 1.0;
        switch (state) {
        case Pan:
            zlevel = 1.0;
            break;
        case ZoomIn:
            zlevel = zoomFactor;
            break;
        case ZoomOut:
            zlevel = 1.0 / zoomFactor;
            break;
        }
        Coordinate ll = new Coordinate(mapX - (width2 / zlevel), mapY
                - (height2 / zlevel));
        Coordinate ur = new Coordinate(mapX + (width2 / zlevel), mapY
                + (height2 / zlevel));

        mapArea = new Envelope(ll, ur);
        // System.out.println("after area "+mapArea+"\nw:"+mapArea.getWidth()+"
        // h:"+mapArea.getHeight());
        repaint();
    }

    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub

    }

}
