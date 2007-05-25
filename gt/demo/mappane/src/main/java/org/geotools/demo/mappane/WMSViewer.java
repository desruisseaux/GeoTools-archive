/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.demo.mappane;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageInputStreamImpl;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.data.FeatureSource;
import org.geotools.data.ows.CRSEnvelope;
import org.geotools.data.ows.Layer;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.request.GetMapRequest;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.Envelope2D;
import org.geotools.gui.swing.JMapPane;
import org.geotools.gui.swing.PanAction;
import org.geotools.gui.swing.ResetAction;
import org.geotools.gui.swing.SelectAction;
import org.geotools.gui.swing.ZoomInAction;
import org.geotools.gui.swing.ZoomOutAction;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.SLDParser;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.sun.media.imageioimpl.plugins.jpeg2000.ImageInputStreamWrapper;
import com.vividsolutions.jts.geom.Envelope;

/**
 * Sample application that may be used to try JMapPane from the command line.
 * 
 * @author Ian Turton
 */
public class WMSViewer implements ActionListener {
	JFrame frame;

	JMapPane mp;

	JToolBar jtb;

	JLabel text;

	final JFileChooser jfc = new JFileChooser();

	public WMSViewer() {
		frame = new JFrame("My WMS Viewer");
		frame.setBounds(20, 20, 450, 200);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		Container content = frame.getContentPane();
		mp = new JMapPane();
		// mp.addZoomChangeListener(this);
		content.setLayout(new BorderLayout());
		jtb = new JToolBar();

		JButton load = new JButton("Load file");
		load.addActionListener(this);
		jtb.add(load);
		Action zoomIn = new ZoomInAction(mp);
		Action zoomOut = new ZoomOutAction(mp);
		Action pan = new PanAction(mp);
		Action select = new SelectAction(mp);
		Action reset = new ResetAction(mp);
		jtb.add(zoomIn);
		jtb.add(zoomOut);
		jtb.add(pan);
		jtb.addSeparator();
		jtb.add(reset);
		jtb.addSeparator();
		jtb.add(select);
		final JButton button = new JButton();
		button.setText("CRS");
		button.setToolTipText("Change map prjection");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				String code = JOptionPane.showInputDialog(button,
						"Coordinate Reference System:", "EPSG:4326");
				if (code == null)
					return;
				try {
					CoordinateReferenceSystem crs = CRS.decode(code);
					setCRS(crs);
				} catch (Exception fe) {
					fe.printStackTrace();
					JOptionPane.showMessageDialog(button, fe.getMessage(), fe
							.getClass().toString(), JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
		});
		jtb.add(button);

		content.add(jtb, BorderLayout.NORTH);

		// JComponent sp = mp.createScrollPane();
		mp.setSize(400, 200);
		content.add(mp, BorderLayout.CENTER);

		content.doLayout();
		frame.setVisible(true);

	}

	/**
	 * Method used to set the current map projection.
	 * 
	 * @param crs
	 *            A new CRS for the mappnae.
	 */
	public void setCRS(CoordinateReferenceSystem crs) {
		mp.getContext().setAreaOfInterest(mp.getContext().getAreaOfInterest(),
				crs);
		mp.setReset(true);
		mp.repaint();
	}

	public void load(GridCoverage2D grid) throws Exception {

		Envelope2D env = grid.getEnvelope2D();
		Envelope en = new Envelope(env.x, env.y, env.width, env.height);
		mp.setMapArea(en);
		StyleFactory factory = CommonFactoryFinder.getStyleFactory(null);

		// SLDParser stylereader = new SLDParser(factory,sld);
		// org.geotools.styling.Style[] style = stylereader.readXML();

		CoordinateReferenceSystem crs = grid.getCoordinateReferenceSystem();
		if (crs == null)
			crs = DefaultGeographicCRS.WGS84;
		MapContext context = new DefaultMapContext(crs);
		context.addLayer(grid, factory.createStyle());
		context.getLayerBounds();
		mp.setHighlightLayer(context.getLayer(0));

		GTRenderer renderer;
		if (false) {
			renderer = new StreamingRenderer();
			HashMap hints = new HashMap();
			hints.put("memoryPreloadingEnabled", Boolean.TRUE);
			renderer.setRendererHints(hints);
		} else {
			renderer = new StreamingRenderer();
			HashMap hints = new HashMap();
			hints.put("memoryPreloadingEnabled", Boolean.FALSE);
			renderer.setRendererHints(hints);
		}
		mp.setRenderer(renderer);
		mp.setContext(context);

		// mp.getRenderer().addLayer(new RenderedMapScale());
		frame.repaint();
		frame.doLayout();
	}

	public static URL aquireURL(String target) {
		if (new File(target).exists()) {
			try {
				return new File(target).toURL();
			} catch (MalformedURLException e) {
			}
		}
		try {
			return new URL(target);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	public void actionPerformed(ActionEvent e) {
		WMSChooser wmc = new WMSChooser(frame, "Select WMS", true);
		int returnVal = wmc.getLayer();
		if (returnVal == -1) {
			return;
		}
		WebMapServer wms = wmc.getWms();
		Layer l = (Layer) wmc.getLayers().get(returnVal);
		GetMapRequest mapRequest = wms.createGetMapRequest();
		mapRequest.addLayer(l);

		mapRequest.setDimensions(mp.getWidth(), mp.getHeight());
		mapRequest.setFormat("image/png");
		Set srs = l.getSrs();
		String crs;
		if (srs.contains("EPSG:4326")) {// really we should get the underlying map pane CRS
			crs = "EPSG:4326";
		} else {
			crs = (String) srs.iterator().next();
		}
		System.out.println("crs = " + crs);
		HashMap bboxes = l.getBoundingBoxes();
		Set keys = bboxes.keySet();
		String k="";
		for(Iterator it = keys.iterator();it.hasNext();k=(String)it.next()) {
			System.out.println(k+" -> "+bboxes.get(k));
		}
		
		CRSEnvelope bb = (CRSEnvelope) bboxes.get(crs);
		if (bb == null) {// something bad happened
			bb= l.getLatLonBoundingBox();
			bb.setEPSGCode("EPSG:4326"); // for some reason WMS doesn't set this.
			
		} 
		// fix the bounds for the shape of the window.
		
		System.out.println(bb.toString());
		mapRequest.setBBox(bb);

		URL request = mapRequest.getFinalURL();
		System.out.println(request.toString());
		try {
			
			String type = request.openConnection().getContentType();
			if (type.equalsIgnoreCase("image/png")) {
				BufferedImage image = ImageIO.read(request.openStream());
				GridCoverageFactory gcf = new GridCoverageFactory();
				Envelope2D env = new Envelope2D(bb
						.getCoordinateReferenceSystem(), bb.getMinX(), bb
						.getMinY(), bb.getLength(0), bb.getLength(1));
				GridCoverage2D grid = gcf.create(l.getTitle(), image, env);
				load(grid);
			} else {
				System.out.println("error content type is " + type);
				if (type.startsWith("text")) {
					String line = "";
					BufferedReader br = new BufferedReader(
							new InputStreamReader(request.openStream()));
					while ((line = br.readLine()) != null) {
						System.out.println(line);
					}
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		WMSViewer mapV = new WMSViewer();

	}
}
