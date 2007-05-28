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
import java.awt.RenderingHints;
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
import org.geotools.map.MapLayer;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.SLDParser;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
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

	GTRenderer renderer;

	MapContext context;

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
		// Action select = new SelectAction(mp);
		Action reset = new ResetAction(mp);
		jtb.add(zoomIn);
		jtb.add(zoomOut);
		jtb.add(pan);
		jtb.addSeparator();
		jtb.add(reset);
		jtb.addSeparator();
		// jtb.add(select);
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

	public void load(WMSMapLayer layer) throws Exception {

		Envelope2D env = layer.getGrid().getEnvelope2D();
		Envelope en = new Envelope(env.x, env.y, env.width, env.height);
		mp.setMapArea(en);

		CoordinateReferenceSystem crs = layer.getGrid()
				.getCoordinateReferenceSystem();
		if (crs == null)
			crs = DefaultGeographicCRS.WGS84;
		if (context == null) {
			context = new DefaultMapContext(crs);
		}
		//this allows us to listen for resize events and ask for the right size image
		mp.addComponentListener(layer);
		context.addLayer(layer);
		context.addMapBoundsListener(layer);
		// System.out.println(context.getLayerBounds());
		// mp.setHighlightLayer(context.getLayer(0));

		if (renderer == null) {
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
				RenderingHints rhints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
				((StreamingRenderer)renderer).setJava2DHints(rhints);
			}
			mp.setRenderer(renderer);
			mp.setContext(context);
		}
		// mp.getRenderer().addLayer(new RenderedMapScale());
		frame.repaint();
		frame.doLayout();
	}

	public void actionPerformed(ActionEvent e) {
		WMSChooser wmc = new WMSChooser(frame, "Select WMS", true);
		int returnVal = wmc.getLayer();
		if (returnVal == -1) {
			return;
		}
		WebMapServer wms = wmc.getWms();
		Layer l = (Layer) wmc.getLayers().get(returnVal);
		WMSMapLayer layer = new WMSMapLayer(wms, l);
		try {
			load(layer);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		WMSViewer mapV = new WMSViewer();

	}
}
