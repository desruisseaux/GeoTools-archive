/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2006, Adrian Custer, assigned to the PMC.
 *
 *    This file is hereby placed into the Public Domain. This means anyone is
 *    free to do whatever they wish with this file. Use it well and enjoy!
 */

package org.geotools.demo.introduction;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;
import javax.units.SI;

import org.geotools.catalog.Catalog;
import org.geotools.data.FeatureSource;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.demo.mappane.MapViewer;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureTypes;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.gui.swing.JMapPane;
import org.geotools.gui.swing.PanAction;
import org.geotools.gui.swing.ResetAction;
import org.geotools.gui.swing.SelectAction;
import org.geotools.gui.swing.ZoomInAction;
import org.geotools.gui.swing.ZoomOutAction;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.referencing.CRS;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.datum.DefaultGeodeticDatum;
import org.geotools.referencing.datum.DefaultPrimeMeridian;
import org.geotools.referencing.factory.FactoryGroup;
import org.geotools.referencing.operation.DefaultMathTransformFactory;
import org.geotools.referencing.operation.DefiningConversion;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.SLDParser;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyleFactoryFinder;
import org.geotools.styling.Symbolizer;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.NoSuchIdentifierException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CSFactory;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.operation.Conversion;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * The DemoApp class, through its main() method, is a quick introductory
 * tutorial to each of the major modules of the Geotools library. 
 * 
 * WARNING: This is a work in progress and is incomplete.
 * 
 * WARNING: The code is presented as a long sequential series of steps to 
 *          ensure easy legibility instead of seeking programming elegance. The
 *          code, apart from main(), will run in the order of the methods in the
 *          file; it can therefore be read from top to bottom.
 * 
 * This tutorial shows the following elements: 
 * 
 *     (1) FeatureSource creation:
 *           This creates, through several approaches, the handles which are
 *           used later for the manipulation of data.
 *           
 *         1.1 - a feature source from scratch
 *         1.2 - a feature source from a shapefile
 *         1.3 - a feature source from a WMS/WFS (an image)
 *         
 *     (.) Catalog creation:
 *           This creates a resource through which to handle all the features 
 *           used by a complex application.
 *     
 *     (.) Coordinate Transform creation:
 *           This creates a coordinate operation and uses that to transform the 
 *           data in a feature source to a different Coordinate Referencing 
 *           System.
 *     
 *     (.) Query creation:
 *           This creates a Filter/Expression to query a feature for its 
 *           contents and thereby to subset a feature.
 *     
 *     ...
 *     
 *     (5) Style creation:
 *           This creates the graphical elements which are used to display 
 *           the data.
 *     
 *     (6) Display:
 *           This creates a GUI MapViewerto display the data.
 *           
 *     (7) Image output:
 *           This renders an image to an image buffer and then dumps the image
 *           buffer to a file.
 *           
 *     ...
 *     
 * 
 * 
 * HISTORY: 
 *   This class regroups work from many different tutorials.
 *     Section 1.1 - "Feature from scratch" was inspired by earlier tutorials.
 *     Section 1.2 - "Feature from shapefile" was in Ian's MapViewer class.
 *    
 *     Section 5   - The style demo came from an email by Tom Howe on user-list.
 *     Section 6   - The GUI was inspired by Ian Turton's MapViewer demo.
 *     Section 7   - MakeImage with email advice from David Adler, Aaron B. Parks.
 * 
 * @author  Adrian Custer
 * @version 0.01
 * @since   2.2RC5
 *
 */
public class DemoApp {
    
    
//    private static Logger textlog = 
//            Logger.getLogger("org.geotools.demo.introduction.QuickStart");
    
    /* Switch for projection order. */
    // TODO: clean main when this works
    static final int PROJECT_ON_START    = 0;
    static final int PROJECT_AFTER_START = 1;
//    static int project_from_start = PROJECT_ON_START;
    static int project_from_start = PROJECT_AFTER_START;
    
    /* The handles for the ShapeFile and Styled Layer Descriptor (SLD) files. */
    /*     The data are in demo/mappane/data/ */
    static final String shpName = "/countries.shp";
    static final String sldName = "/countries.sld";
    
    static File shpFile;
    static URL shpURL;
    static File sldFile;
    static URL sldURL;
        
        
    /* The name of the website */
    static final URL webUrl = null;  
    
    /* The filename for the output image */
    static final String imageFileEnd = "image.png";
    
    /* FeatureSource variables */
    static FeatureSource memFS, shpFS, webFS;
    
    /* StyledLayerDescriptor variables */
    static Style memStyl;
    static Style shpStyl;
    static Style webStyl;
    
    /* DataLayer variables */
    
    /* Cartographic variables */
    static final Envelope envlp_NoEdges = new Envelope(-170.0,170.0,-80.0,80.0);
    static CoordinateReferenceSystem projCRS = null;

    /* GUI frame, pane and extras */
    static JFrame frame;
    static JMapPane jmp;
    static JPanel visPanel;
    static ScrollPane infoSP;
    static JToolBar jtb;
    static JLabel text;
    static JButton quitButton;
    static JButton createButton;
    static JButton styleButton;
    static JButton renderButton;
    static JButton projectButton;
    static JButton filterButton;
    static JButton captureButton;
    static JButton saveButton;
    static JButton commitButton;
    static JButton analyzeButton;
    static JTextArea textArea;
    
    /* Display elements */
    static MapContext context;
    static GTRenderer renderer;
    static com.vividsolutions.jts.geom.Envelope worldbounds;
    
    /* demo class */
    static Demo demo = new Demo();
    
    /* catalog */
    static Catalog catalog;
    
    /*
     * Create the Demo's GUI.
     * 
     * Geotools users can skip this swing code, go to create_FeatureSource...
     * 
     */
    public static void create_Geotools_DemoGUI(){

        frame=new JFrame("Geotools Demo");
        frame.setBounds(20,20,800,500);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//        frame.setBackground(Color.cyan);
        
        Container contentPane = frame.getContentPane();
        BoxLayout layout = new BoxLayout(contentPane, BoxLayout.X_AXIS);
        contentPane.setLayout(layout);
//        contentPane.setBackground(Color.red);
        
        JPanel buttonPanel = new JPanel();
//        buttonPanel.setBackground(Color.blue);
        buttonPanel.setVisible(true);
        visPanel = new JPanel();
//        visPanel.setBackground(Color.gray);
        visPanel.setVisible(true);
        
        contentPane.add(Box.createRigidArea(new Dimension(10, 0)));
        contentPane.add(buttonPanel);
        contentPane.add(Box.createRigidArea(new Dimension(10, 0)));
        contentPane.add(visPanel);
        contentPane.add(Box.createRigidArea(new Dimension(10, 0)));
        
        /* The action button Panel */
        JPanel actionButtonPanel = new JPanel();
//        actionButtonPanel.setBackground(Color.green);
        actionButtonPanel.setVisible(true);
        
        BoxLayout aBPlayout = new BoxLayout(actionButtonPanel, BoxLayout.Y_AXIS);
        actionButtonPanel.setLayout(aBPlayout);
        
        
        int BUTTON_WIDTH = 100;
        createButton = new JButton("1. Create Features");
        createButton.setMinimumSize(new Dimension(BUTTON_WIDTH,1));
        actionButtonPanel.add(createButton);
        actionButtonPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        styleButton = new JButton("2. Style Features");
        styleButton.setEnabled(false);
        styleButton.setMinimumSize(new Dimension(BUTTON_WIDTH,1));
        actionButtonPanel.add(styleButton);
        actionButtonPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        renderButton = new JButton("3. Render Map");
        renderButton.setEnabled(false);
        renderButton.setMinimumSize(new Dimension(BUTTON_WIDTH,1));
        actionButtonPanel.add(renderButton);
        actionButtonPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        projectButton = new JButton("4. Project Map");
        projectButton.setEnabled(false);
        projectButton.setMinimumSize(new Dimension(BUTTON_WIDTH,1));
        actionButtonPanel.add(projectButton);
        actionButtonPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        filterButton = new JButton("5. Filter Features");
        filterButton.setEnabled(false);
        filterButton.setMinimumSize(new Dimension(BUTTON_WIDTH,1));
        actionButtonPanel.add(filterButton);
        actionButtonPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        captureButton = new JButton("6. Capture Image");
        captureButton.setEnabled(false);
        captureButton.setMinimumSize(new Dimension(BUTTON_WIDTH,1));
        actionButtonPanel.add(captureButton);
        actionButtonPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        saveButton = new JButton("7. Save to file");
        saveButton.setEnabled(false);
        saveButton.setMinimumSize(new Dimension(BUTTON_WIDTH,1));
        actionButtonPanel.add(saveButton);
        actionButtonPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        commitButton = new JButton("8. Commit to WFS");
        commitButton.setEnabled(false);
        commitButton.setMinimumSize(new Dimension(BUTTON_WIDTH,1));
        actionButtonPanel.add(commitButton);
        actionButtonPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        analyzeButton = new JButton("9. Analyze network");
        analyzeButton.setEnabled(false);
        analyzeButton.setMinimumSize(new Dimension(BUTTON_WIDTH,1));
        actionButtonPanel.add(analyzeButton);
        
        /* The button Panel */
        BoxLayout buttonPanelBoxLayout = new BoxLayout(buttonPanel,BoxLayout.Y_AXIS);
        buttonPanel.setLayout(buttonPanelBoxLayout);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        //TODO: verify the file can be found
        java.net.URL imgURL = 
            DemoApp.class.getResource("/GeotoolsBoxLogo.png");
//        System.out.println(imgURL);
        ImageIcon icon = new ImageIcon(imgURL,"The Geotools Logo");
        JLabel iconLabel = new JLabel(icon);
        buttonPanel.add(iconLabel);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        buttonPanel.add(actionButtonPanel);
        buttonPanel.add(Box.createVerticalGlue());
        JButton quitButton = new JButton("QUIT");
        buttonPanel.add(quitButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        quitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                frame.dispose();
                
              }
          });
        
        createButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                DemoApp.createButton.setEnabled(false);
                DemoApp.styleButton.setEnabled(true);
                DemoApp.createFeatureSourceFromScratch();
                DemoApp.createFeatureSourceFromShapefile();
//                DemoApp.create_FeatureSource_fromWeb();
//                DemoApp.create_FeatureSource_fromDatabase();
                
              }
          });
        styleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                DemoApp.styleButton.setEnabled(false);
                DemoApp.renderButton.setEnabled(true);
                DemoApp.create_Styles_forEach_Feature();
                
              }
          });
        renderButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                DemoApp.renderButton.setEnabled(false);
                DemoApp.projectButton.setEnabled(true);
                DemoApp.initialize_JMapPane();
                try{
                    DemoApp.load_JMapPane();
                } catch (Exception ex){
                    System.err.println("Could not load the JMapPane: "+ ex);
                }
                
                
                
              }
          });
        projectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                DemoApp.projectButton.setEnabled(false);
                DemoApp.filterButton.setEnabled(true);
                DemoApp.create_ProjectedCRS_from_DefaultGeogCRS();
                DemoApp.display_projected_as_Mercator();
                
              }
          });
        filterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                DemoApp.filterButton.setEnabled(false);
                DemoApp.captureButton.setEnabled(true);
//                frame.dispose();
                
              }
          });
        captureButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                DemoApp.captureButton.setEnabled(false);
                DemoApp.saveButton.setEnabled(true);
//                DemoApp.capture_as_image();
                
              }
          });
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                DemoApp.saveButton.setEnabled(false);
                DemoApp.commitButton.setEnabled(true);
//                frame.dispose();
                
              }
          });
        commitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                commitButton.setEnabled(false);
                analyzeButton.setEnabled(true);
//                frame.dispose();
                
              }
          });
        analyzeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                analyzeButton.setEnabled(false);
//                frame.dispose();
                
              }
          });

        
        /* The info Text Area */
        textArea = new JTextArea();
        textArea.append("Welcome to the Geotools Demo.\n\n");
        textArea.append("Click on the \"Create\" button to start.\n\n");
        infoSP = new ScrollPane();
        infoSP.add(textArea);
        
	//TOOD: use a Logger to output to the textArea. Not sure how to do this
	//without using classes: annonymous inner class?
//        OutputStream os = new  anOutputStream() extends OutputStream {
//              public void write( int b ) throws IOException {
//                  // append the data as characters to the JTextArea control
//                  DemoApp.textArea.append( String.valueOf( ( char )b ) );
//              }
//        };
//        StreamHandler sh = new StreamHandler(os , new Formatter());
        
        /* The visuals Panel */
        BoxLayout visPanelBoxLayout = new BoxLayout(visPanel,BoxLayout.Y_AXIS);
        visPanel.setLayout(visPanelBoxLayout);
        visPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        visPanel.add(infoSP);
        visPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        
        contentPane.setVisible(true);
        contentPane.doLayout();
        frame.doLayout();
        frame.setVisible(true);
        frame.repaint();
        
        
    }
    
    
    
    // TODO: where is the DefaultGeometry determined?
    /*
     * Create London from scratch! Well, only a FeatureSource for the city.
     * 
     * Create:
     *   (1) The attribute data:     Geometry, Others
     *   (2) The meta 'Type' data for the Attributes
     *   (3) The meta feature 'Type'
     *   (4) The feature
     *   (5) The MemoryDataStore from which to get a FeatureSource
     */
    public static void createFeatureSourceFromScratch(){
        
        textArea.append("Start: Create FeatureSource from scratch.\n");
        
        try {
			memFS = demo.createFeatureSourceFromScratch();
		} catch (IOException e) {
			e.printStackTrace();
			textArea.append( "Exception: "+ e.getMessage() );
		}

        textArea.append("  End: Created FeatureSource from scratch.\n");
    }

    
    
    /*
     * Use a Shapefile!
     * 
     * Create a FeatureSource from a Shapefile format file. 
     */
    public static void createFeatureSourceFromShapefile(){

        textArea.append("Start: Create FeatureSource from shapefile.\n");
        
        try {
        	shpFS = demo.loadShapefileFeatureSource( );
	    } 
        catch (IOException e) {
			e.printStackTrace();
			textArea.append( "Exception: "+ e.getMessage() );
		}
         textArea.append("  End: Create FeatureSource from shapefile.\n");
    }
    
    
    
    // TODO:
    /*
     * Create a FeatureSource from a web resource.
     */
    public static void create_FeatureSource_fromWeb(){
        

        textArea.append("Start: Create FeatureSource from web server.\n");
	
        textArea.append("Oops!: Not yet implemented\n");

        textArea.append("  End: Created FeatureSource from web server.\n");
        
    }

    // TODO: If we can guarantee a Database will be available
    /*
     * Create a FeatureSource from a database.
     */
    public static void create_FeatureSource_fromDatabase(){
        

        textArea.append("Start: Create FeatureSource from web server.\n");
	
        textArea.append("Oops!: Not yet implemented\n");

        textArea.append("  End: Created FeatureSource from web server.\n");
        
    }
    
    /*
     * Create the catalog
     */
    public static void create_a_Catalog(){
        
        textArea.append("Start: Create a Catalog.\n");
	
        textArea.append("Oops!: Not yet implemented\n");

        textArea.append("  End: Created a Catalog.\n");
    }

    /*
     * Create the styles that will be used for the creation of the MapLayers.
     */
    public static void create_Styles_forEach_Feature(){
        
        textArea.append("Start: Create the Styled Layer Descriptors.\n");
        
        memStyl      = demo.createStyleFromScratch();

        try {
			shpStyl 	 = demo.createStyleFromFile();
		} 
        catch (IOException e) {
			e.printStackTrace();
			textArea.append( "Exception: " + e.getMessage() );
        }

        textArea.append("  End: Created the Styled Layer Descriptors.\n");
    }

    
    
    /*
     * Create a GUI map displayer.
     * 
     * This is all Swing stuff for the JMapPane.
     * 
     */
    public static void initialize_JMapPane(){
        textArea.append("Start: Initialize the GUI.\n");
//        frame=new JFrame("My Map Viewer");
//        frame.setBounds(20,20,1080,600);
////        frame.setBackground(Color.cyan);
//        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//        
//        Container content = frame.getContentPane();
////        content.setBackground(Color.magenta);
//        content.setLayout(new BorderLayout());
        
        Panel mapGUI = new Panel();
        mapGUI.setLayout(new BorderLayout());
        jmp = new JMapPane();
        jmp.setBackground(Color.white);
//        jmp.setSize(20,100);
        
        /* The toolbar */
        jtb = new JToolBar();
        Action zoomIn = new ZoomInAction(jmp);
        Action zoomOut = new ZoomOutAction(jmp);
        Action pan = new PanAction(jmp);
        Action select = new SelectAction(jmp);
        Action reset = new ResetAction(jmp);
        jtb.add(zoomIn);
        jtb.add(zoomOut);
        jtb.add(pan);
        jtb.addSeparator();
        jtb.add(reset);
        jtb.addSeparator();
        jtb.add(select);
        final JButton button= new JButton();
        button.setText("CRS");
        button.setToolTipText("Change map prjection");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              
              String code = JOptionPane.showInputDialog( button, "Coordinate Reference System:", "EPSG:4326" ); 
              try{
                 CoordinateReferenceSystem crs = CRS.decode( code );
                 jmp.getContext().setAreaOfInterest(jmp.getContext().getAreaOfInterest(),crs);
                 jmp.setReset(true);
                 jmp.repaint();       
                   
                }
                catch(FactoryException fe){
                 JOptionPane.showMessageDialog( button, fe.getMessage(), fe.getClass().toString(), JOptionPane.ERROR_MESSAGE );
                 return;
                }
            }
        });
        jtb.add(button);
        mapGUI.add(jtb,BorderLayout.NORTH);
        mapGUI.add(jmp);
	
        final int W = visPanel.getWidth();
        infoSP.setSize(new Dimension(300,60));        
        BoxLayout visPanelBoxLayout = new BoxLayout(visPanel,BoxLayout.Y_AXIS);
        visPanel.setLayout(visPanelBoxLayout);
        visPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        visPanel.add(infoSP);
	visPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        visPanel.add(mapGUI);
        visPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        frame.getContentPane().doLayout();
        infoSP.setSize(new Dimension(3,3));
        frame.getContentPane().doLayout();
        frame.setVisible(true);

        textArea.append("  End: Initialized the GUI.\n");
        
    }
    
    
    /*
     * Display features onto the screen.
     * 
     * This is a very crude example, showing only how do display a map. The 
     * core class, JMapPane, also has a toolbar which is not shown here. See
     * the demo/gui for more details.
     */
    public static void load_JMapPane()throws Exception{
        

        textArea.append("Start: Load the map.\n");
        
        /* Renderer */
        renderer = new StreamingRenderer();
        
        /* Context */
        context = new DefaultMapContext(); //WGS84 by default
        //TODO: remove testing
        switch (project_from_start){
            case PROJECT_ON_START:
        		ReferencedEnvelope llEnvelope = new ReferencedEnvelope(envlp_NoEdges, DefaultGeographicCRS.WGS84);
        		ReferencedEnvelope projEnvelope = llEnvelope.transform(projCRS, true);
        		context.setAreaOfInterest(projEnvelope);
            case PROJECT_AFTER_START:
                context.setAreaOfInterest(envlp_NoEdges);
        }
//        context.setAreaOfInterest(envlp_NoEdges);
        context.addLayer(memFS,memStyl);
        context.addLayer(shpFS,shpStyl);
//      context.addLayer(webFS,webStyl);
//      context.addLayer(dbFS,dbStyl);

        /* Add to JMapPane */
        jmp.setRenderer(renderer);
        jmp.setContext(context);
        
        /* Configure JMapPane */
        jmp.setHighlightLayer(context.getLayer(0));
//        jmp.setSize(200,600);
        //TODO: Set boundary to all that's visible, disabled for projection
//        jmp.setMapArea(context.getLayerBounds());
        jmp.setMapArea(envlp_NoEdges);

        /* Paint */
        frame.repaint();
        frame.doLayout();

        textArea.append("  End: Loaded the map.\n");
    }
    
    
    /*
     * Filter the features by spatial extent.
     */
    public static void filterFeatures(){

        textArea.append("Start: Filter the features.\n");
        textArea.append("  End: Filtered the features.\n");
        
    }

  
    
    
    /*
     * Create a Mercator ProjectedCRS from DefaultGeogCRS.
     */
    public static void create_ProjectedCRS_from_DefaultGeogCRS(){
        
        textArea.append("Start: Create ProjectedCRS from DefaultGeographicCRS.\n");
        
        /* Properties of the Projected CRS */
        Map props = new HashMap();
        props.put(IdentifiedObject.NAME_KEY, "My arbitrary name"); // Mandatory
//        props.put(ReferenceSystem.VALID_AREA_KEY,e); // Optional
        
        
        /* Geographic CoordinateReferenceSystem */
        //TODO: this is hard coded below because the compiler doesn't work.
        CoordinateReferenceSystem geogCRS = 
            org.geotools.referencing.crs.DefaultGeographicCRS.WGS84;
        
        
        /* Defining Conversion: Name, Parameters */
        final String dcName = "A Mercator";
        /* Parameters for the Mercator */
        DefaultMathTransformFactory mtf = new DefaultMathTransformFactory();
        ParameterValueGroup pvg = null;
        try {
            pvg = mtf.getDefaultParameters("Mercator_1SP");
        } catch (NoSuchIdentifierException nsiex){
            System.err.println("On DefaultPrameterGroup creation: "+ nsiex.getMessage());
        }
        //Start Test Output
//            ParameterDescriptorGroup dg = pvg.getDescriptor()
//            for (GeneralParameterDescriptor descriptor : dg.descriptors()) {
//                System.out.println(descriptor.getName().getCode());
//            }
        //End Test Output
        DefiningConversion dc = new DefiningConversion(dcName,pvg);
        //TODO: Added to make the compiler happy, could merge with above.
        Conversion c = (Conversion) dc;
        
        
        
        /* Coordinate System */
        Map map = new HashMap();
        CSFactory csFactory = FactoryFinder.getCSFactory(null);
        CoordinateSystemAxis xAxis = null;
        CoordinateSystemAxis yAxis = null;
        CartesianCS worldCS = null;
        try {
            map.clear();
            map.put("name", "Cartesian X axis");
            xAxis = csFactory.createCoordinateSystemAxis(map, "X", AxisDirection.EAST, SI.METER);
            map.clear();
            map.put("name", "Cartesian Y axis");
            yAxis = csFactory.createCoordinateSystemAxis(map, "Y", AxisDirection.NORTH, SI.METER);
            map.clear();
            map.put("name", "Cartesian CS");
            worldCS = csFactory.createCartesianCS(map, xAxis, yAxis);
        } catch (FactoryException fex) {
            System.err.println("On cartesianCS creation: " + fex.getMessage());
        }
        
        /* Projected CRS */
        FactoryGroup fg = new FactoryGroup(null);
        try{
           projCRS = fg.createProjectedCRS(props,
                        org.geotools.referencing.crs.DefaultGeographicCRS.WGS84,
                                            c,
                                            worldCS);
//           //TODO: figure out why this breaks but above works.
//           projCRS = fg.createProjectedCRS(props,
//                   geogCRS,
//                   dc,
//                   worldCS);
        } catch (FactoryException fex) {
            System.err.println("On projectedCRS creation: " + fex.getMessage());
        }
//        System.out.println(projCRS.toWKT())

        textArea.append("  End: Created ProjectedCRS from DefaultGeographicCRS.\n");
    }
    
    
    
    
//    /*
//     * Create a Mercator ProjectedCRS from Well-Known Text.
//     */
//    public static void projectedCRSfromWKT(){
//        
//        textArea.append("");
//        CRSFactory crsFactory = FactoryFinder.getCRSFactory(null);
//        String wkt = "PROJCS[\"Mercator Attempt\", "
//                       + "GEOGCS[\"WGS84\", "
//                           + "DATUM[\"WGS84\", "
//                           + "SPHEROID[\"WGS84\", 6378137.0, 298.257223563]], "
//                           + "PRIMEM[\"Greenwich\", 0.0], "
//                           + "UNIT[\"degree\",0.017453292519943295], "
//                           + "AXIS[\"Longitude\",EAST], "
//                           + "AXIS[\"Latitude\",NORTH]], "
//                       + "PROJECTION[\"Mercator_1SP\"], "
//                       + "PARAMETER[\"semi_major\", 6378137.0], "
//                       + "PARAMETER[\"semi_minor\", 6356752.314245179], "
//                       + "PARAMETER[\"central_meridian\", 0.0], "
//                       + "PARAMETER[\"scale_factor\", 1.0], "
//                       + "PARAMETER[\"false_easting\", 0.0], "
//                       + "PARAMETER[\"false_northing\", 0.0], "
//                       + "UNIT[\"metre\",1.0], "
//                       + "AXIS[\"x\",EAST], "
//                       + "AXIS[\"y\",NORTH]]";
//        CoordinateReferenceSystem prjCRS=null;
//        try{
//            prjCRS = crsFactory.createFromWKT(wkt);
//        } catch (FactoryException fe){
//            System.err.println("On prjCRS creation a FactoryException :"+fe.getMessage());
//        }
//        Envelope e = new Envelope(-170.0,170.0,-80.0,80.0);
//        context.setAreaOfInterest(e, prjCRS);
//        
//        textArea.append("");
//    }
 
    
    
    
    /*
     * A Mercator Projected Map.
     * 
     * Reproject features on the screen.
     * 
     * 
     */
    //TODO: figure out why this doesn't work.!
    public static void display_projected_as_Mercator(){
    	try {
	        textArea.append("Start: Project the map.\n");
	        
			ReferencedEnvelope llEnvelope = new ReferencedEnvelope(envlp_NoEdges, DefaultGeographicCRS.WGS84);
			ReferencedEnvelope projEnvelope = llEnvelope.transform(projCRS, true);
			context.setAreaOfInterest(projEnvelope);
	        
	        jmp.setContext(context);
	
	        jmp.setMapArea(projEnvelope);
	
	        frame.repaint();
	        frame.doLayout();
	
	        textArea.append("  End: Projected the map.\n");
    	} catch(Exception te) {
    		textArea.append("Error occurred during projection");
    	}
    }
        
    
    
    
    
    
    
    /*
     * Make graphical image files, one from scratch and the other from the 
     * jmappane contents.
     *   TODO: add to catalog---great for pre/post transform comparisons
     *   TODO: clean this up, isolate resolution and size
     *   
     */
    public static void capture_as_image(){

        textArea.append("Start: Capture an image.\n");
        /*
         * 1. Create an image from scratch
         */
        //Size of the final image, will be too big for the input
        int w = 1800;
        int h = 800;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, w, h);
        
        //TODO: HACK HACK HACK  need a real pixel to world transform
        AffineTransform trsf = new AffineTransform(new double[]{1.0,1.0,1.0,1.0});

        //      DefaultMathTransformFactory dmtf = new DefaultMathTransformFactory();
//      try{
//              trsf = dmtf.createAffineTransform(new Matrix2(1,1,1,1));
//      } catch (Exception e){
//              ;
//      }
//              transform =
//                      renderer.worldToScreenTransform(
//                                                              g,
//                                                              new Rectangle(0, 0, w, h),
//                                                              worldbounds);
                
        renderer.paint(g, new Rectangle(0, 0, w, h), trsf);
        try{
            ImageIO.write(image, "png", new File("workspace/gtdemo-new-"+imageFileEnd));
        } catch (IOException ioex) {
            System.err.println("IO Exception on image file write: "+ ioex);
        }
        g.dispose();
        
        /*
         * 2. Create an image from the jmappane contents
         */
        //spit the image out to a file
        int ww = jmp.getWidth()+40;
        int hh = jmp.getHeight()+40;
        BufferedImage imageOut = new BufferedImage(ww, hh, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = imageOut.createGraphics();
        g2.setColor(Color.gray);
        g2.fillRect(0, 0, ww, hh);
        jmp.paint(g2);
        try{
            ImageIO.write(imageOut, "png", new File("workspace/gtdemo-jmp-"+imageFileEnd));
        } catch (IOException ioex) {
            System.err.println("IO Exception on image file write: "+ ioex);
        }
        g2.dispose();

        textArea.append("  End: Captured an image.\n");
    }
    
    /**
     * This is main() the only real function in this QuickStart tutorial. 
     * The class works sequentially through every step
     * @param args
     */
    public static void main(String[] args) throws Exception {
    	
    	
        System.out.println("DemoApp Tutorial: Start...");
        
            System.out.println("Start: Create the Demo's GUI.");
        create_Geotools_DemoGUI();
            System.out.println("  End: Created the Demo's GUI.");
        
        System.out.println("DemoApp Tutorial: End of non-GUI thread.");
    }

}
