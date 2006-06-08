package edu.psu.geovista.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.io.File;
import java.net.URL;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JToolBar;

import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.SLDParser;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyleFactoryFinder;


public class MapViewer {
    JFrame frame;
    JMapPane mp;
    JToolBar jtb;
    JLabel text;
    public MapViewer(){
        frame=new JFrame("My Map Viewer");
        frame.setBounds(20,20,450,200);
        Container content = frame.getContentPane();
        mp = new JMapPane();
        //mp.addZoomChangeListener(this);
        content.setLayout(new BorderLayout());
        jtb = new JToolBar();
        Action zoomIn = new ZoomInAction(mp);
        Action zoomOut = new ZoomOutAction(mp);
        Action pan = new PanAction(mp);
        Action reset = new ResetAction(mp);
        jtb.add(zoomIn);
        jtb.add(zoomOut);
        jtb.add(pan);
        jtb.addSeparator();
        jtb.add(reset);
        
        content.add(jtb,BorderLayout.NORTH);
        
        
        //JComponent sp = mp.createScrollPane();
        mp.setSize(400,200);
        content.add(mp,BorderLayout.CENTER);
         
        content.doLayout();
        frame.setVisible(true);
        
    }
    
    

    public void load(URL shape, String sld)throws Exception{
        ShapefileDataStore ds = new ShapefileDataStore(shape);
        
        FeatureSource fs = ds.getFeatureSource();
        com.vividsolutions.jts.geom.Envelope env = fs.getBounds();
        System.out.println(env);
        mp.setMapArea(env);
        StyleFactory factory = StyleFactoryFinder.createStyleFactory();
        URL surl = new File(sld).toURL();
        SLDParser stylereader = new SLDParser(factory,surl);
        org.geotools.styling.Style[] style = stylereader.readXML();
        
        
        MapContext context = new DefaultMapContext();
        context.addLayer(fs,style[0]);
        context.getLayerBounds();
        
        
        GTRenderer renderer = new StreamingRenderer();
        mp.setRenderer(renderer);
        mp.setContext(context);
        
        
//        mp.getRenderer().addLayer(new RenderedMapScale());
        frame.repaint();
        frame.doLayout();
    }
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        MapViewer mapV = new MapViewer();
        File f = new File("states.shp");
        String sld = "popshade.sld";
        if(args.length>0){
            f = new File(args[0]); 
            sld=args[1];
            
        }
        
        mapV.load(f.toURL(), sld);
    }

}
