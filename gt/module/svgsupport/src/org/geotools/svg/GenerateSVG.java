/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package org.geotools.svg;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.geotools.map.MapContext;
import org.geotools.renderer.lite.LiteRenderer;
import org.w3c.dom.Document;

import com.vividsolutions.jts.geom.Envelope;

/**
 * This is a simple support class which allows you to generate an SVG file from a map.
 *
 * To use, setup a Map object with the layers you want to render, create an envelope for the
 * region to be drawn and pass in an OutputStream (probably attached to a new file) for the
 * resulting SVG information to be stored in.
 *
 * Optionaly you can change the default size of the SVG cavas (in effect increasing the resolution)
 * by calling setCavasSize before calling go.
 *
 * @author James Macgill, PennState
 * @version $Id$
 */
public class GenerateSVG {
    private static Logger LOGGER = Logger
        .getLogger("org.geotools.svgsupport");
    private Dimension canvasSize = new Dimension(300, 300);

    /**
     * Creates a new instance of GenerateSVG.
     */
    public GenerateSVG() {
    }

    /** Generate an SVG document from the supplied information.
     * Note, call setCavasSize first if you want to change the default output size.
     * @param map Contains the layers (features + styles) to be rendered
     * @param env The portion of the map to generate an SVG from
     * @param out Stream to write the resulting SVG out to (probable should be a new file)
     * @throws IOException Should anything go wrong whilst writing to 'out'
     * @throws ParserConfigurationException If critical XML tools are missing from the classpath
     */    
    public void go(MapContext map, Envelope env, OutputStream out) throws IOException, ParserConfigurationException {
        SVGGeneratorContext ctx = setupContext();
        ctx.setComment("Generated by GeoTools2 with Batik SVG Generator");

        SVGGraphics2D g2d = new SVGGraphics2D(ctx,
                true);

        g2d.setSVGCanvasSize(getCanvasSize());

        renderMap(map, env, g2d);
        LOGGER.finest("writing to file");
        g2d.stream(new OutputStreamWriter(out, "UTF-8"));
    }

    /**
     * DOCUMENT ME!
     *
     * @param map
     * @param env
     * @param g2d
     */
    private void renderMap(final MapContext map, final Envelope env, final SVGGraphics2D g2d) throws IOException {
        LiteRenderer renderer = new LiteRenderer(map);
        Rectangle outputArea = new Rectangle(g2d.getSVGCanvasSize());
        Envelope dataArea = map.getLayerBounds();
        AffineTransform at = renderer.worldToScreenTransform(dataArea, outputArea);
        LOGGER.finest("rendering map");
        renderer.paint(g2d, outputArea, at);
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     *
     * @throws FactoryConfigurationError
     * @throws ParserConfigurationException
     */
    private SVGGeneratorContext setupContext() throws FactoryConfigurationError, ParserConfigurationException {
        Document document = null;

        DocumentBuilderFactory dbf = DocumentBuilderFactory
            .newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();

        // Create an instance of org.w3c.dom.Document
        document = db.getDOMImplementation().createDocument(null, "svg", null);

        // Set up the context
        SVGGeneratorContext ctx = SVGGeneratorContext
            .createDefault(document);

        return ctx;
    }

    public Dimension getCanvasSize() {
        return this.canvasSize;
    }

    public void setCanvasSize(final Dimension size) {
        this.canvasSize = size;
    }
}
