/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2002, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.coverage.grid;

// J2SE dependencies
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

// JAI dependencies
import javax.media.jai.GraphicsJAI;
import javax.media.jai.PlanarImage;

// OpenGIS dependencies
import org.opengis.parameter.ParameterValueGroup;

// Geotools dependencies
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.processing.AbstractProcessor;
import org.geotools.resources.Arguments;
import org.geotools.resources.Utilities;


/**
 * A very simple viewer for {@link GridCoverage2D}. This viewer provides no zoom
 * capability, no user interaction and ignores the coordinate system. It is just
 * for quick test of grid coverage.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Viewer extends JPanel {
    /**
     * The image to display.
     */
    private final RenderedImage image;

    /**
     * The main sample dimension, or {@code null} if none.
     * Used by {@link #printPalette} for printing categories.
     */
    private GridSampleDimension categories;

    /**
     * The transform from grid to coordinate system.
     * Usually an identity transform for this simple viewer.
     */
    private final AffineTransform gridToCoordinateSystem = new AffineTransform();

    /**
     * The location for the next frame window.
     */
    private static int location;

    /**
     * Constructs a viewer for the specified image.
     *
     * @param coverage The image to display.
     */
    public Viewer(RenderedImage image) {
        image = this.image = PlanarImage.wrapRenderedImage(image);
        gridToCoordinateSystem.translate(-image.getMinX(), -image.getMinY());
        setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
    }

    /**
     * Constructs a viewer for the specified grid coverage.
     *
     * @param coverage The coverage to display.
     */
    public Viewer(final GridCoverage2D coverage) {
        this(coverage.getRenderedImage());
        categories = (GridSampleDimension) coverage.getSampleDimension(0);
    }

    /**
     * Paint this component.
     */
    public void paintComponent(final Graphics graphics) {
        super.paintComponent(graphics);
        final GraphicsJAI g = GraphicsJAI.createGraphicsJAI((Graphics2D) graphics, this);
        g.drawRenderedImage(image, gridToCoordinateSystem);
    }

    /**
     * A convenience method showing an image. The application
     * will be terminated when the user close the frame.
     *
     * @param  coverage The coverage to display.
     * @return The viewer, for information.
     */
    public static Viewer show(final RenderedImage image) {
        return show(new Viewer(image), null);
    }

    /**
     * A convenience method showing a grid coverage. The application
     * will be terminated when the user close the frame.
     *
     * @param  coverage The coverage to display.
     * @return The viewer, for information.
     */
    public static Viewer show(final GridCoverage2D coverage) {
        return show(coverage, null);
    }

    /**
     * A convenience method showing a grid coverage. The application
     * will be terminated when the user close the frame.
     *
     * @param  coverage The coverage to display.
     * @param  title The window title.
     * @return The viewer, for information.
     */
    public static Viewer show(final GridCoverage2D coverage, final String title) {
        final StringBuffer buffer = new StringBuffer();
        if (title != null) {
            buffer.append(title);
            buffer.append(" - ");
        }
        buffer.append(coverage.getName().toString(JComponent.getDefaultLocale()));
        if (coverage != coverage.geophysics(true)) {
            buffer.append(" (packed)");
        } else if (coverage != coverage.geophysics(false)) {
            buffer.append(" (geophysics)");
        }
        return show(new Viewer(coverage), buffer.toString());
    }

    /**
     * A convenience method showing a grid coverage. The application
     * will be terminated when the user close the frame.
     *
     * @param  viewer The viewer to display.
     * @param  title  The frame title, or {@code null} if none.
     * @return The viewer, for convenience.
     */
    private static Viewer show(final Viewer viewer, final String title) {
        final JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(location, location);
        frame.getContentPane().add(viewer);
        frame.pack();
        frame.setVisible(true);
        location += 64;
        return viewer;
    }

    /**
     * Prints the color palette to the specified output stream. First, the color model
     * name is displayed. Next, if the color model is an {@link IndexColorModel}, then the
     * RGB codes are written for all samples values. Category names or geophysics values,
     * if any are written after each sample values.
     *
     * @param out The writer where to print the palette.
     */
    public void printPalette(final PrintWriter out) {
        final Locale locale = getLocale();
        final ColorModel model = image.getColorModel();
        out.print(Utilities.getShortClassName(model));
        out.println(':');
        if (model instanceof IndexColorModel) {
            out.println();
            out.println("Sample  Colors              Category or geophysics value");
            out.println("------  ----------------    ----------------------------");
            final IndexColorModel palette = (IndexColorModel) model;
            final int size = palette.getMapSize();
            final byte[] R = new byte[size];
            final byte[] G = new byte[size];
            final byte[] B = new byte[size];
            palette.getReds  (R);
            palette.getGreens(G);
            palette.getBlues (B);
            for (int i=0; i<size; i++) {
                format(out,   i);  out.print(":    RGB[");
                format(out, R[i]); out.print(',');
                format(out, G[i]); out.print(',');
                format(out, R[i]); out.print(']');
                if (categories != null) {
                    final String label = categories.getLabel(i, locale);
                    if (label != null) {
                        out.print("    ");
                        out.print(label);
                    }
                }
                out.println();
            }
        } else {
            out.println(model.getColorSpace());
        }
    }

    /**
     * Format a unsigned byte to the specified output stream.
     * The number will be right-justified in a cell of 3 spaces width.
     *
     * @param The writer where to print the number.
     * @param value The number to format.
     */
    private static void format(final PrintWriter out, final byte value) {
        format(out, ((int)value) & 0xFF);
    }

    /**
     * Format an integer to the specified output stream.
     * The number will be right-justified in a cell of 3 spaces width.
     *
     * @param The writer where to print the number.
     * @param value The number to format.
     */
    private static void format(final PrintWriter out, final int value) {
        final String str = String.valueOf(value);
        out.print(Utilities.spaces(3-str.length()));
        out.print(str);
    }

    /**
     * Load and display one of examples grid coverages.
     */
    public static void main(String[] args) throws IOException {
        final Arguments arguments = new Arguments(args);
        final PrintWriter     out = arguments.out;
        final Locale       locale = arguments.locale;
        final String    operation = arguments.getOptionalString ("-operation");
        final Boolean  geophysics = arguments.getOptionalBoolean("-geophysics");
        final boolean     palette = arguments.getFlag           ("-palette");
        args = arguments.getRemainingArguments(1);
        if (args.length == 0) {
            out.println("Usage: Viewer [options] example");
            out.println();
            out.print("Where \"example\" is the number of the requested example (0 to ");
            out.print(GridCoverageTest.getNumExamples()-1);
            out.println(" inclusive)");
            out.println("and [options] includes:");
            out.println();
            out.println("  -operation=[s]  An operation name to apply (e.g. \"GradientMagniture\").");
            out.println("                  For a list of available operations, run the following:");
            out.println("                  java org.geotools.coverage.processing.DefaultProcessor");
            out.println("  -geophysics=[b] Set to 'true' or 'false' for requesting a \"geophysics\"");
            out.println("                  version of data or an indexed version, respectively.");
            out.println("  -palette        Dumps RGB codes to standard output.");
            out.flush();
            return;
        }
        GridCoverage2D coverage = GridCoverageTest.getExample(Integer.parseInt(args[0]));
        if (geophysics != null) {
            coverage = coverage.geophysics(geophysics.booleanValue());
        }
        if (operation != null) {
            final AbstractProcessor processor = AbstractProcessor.getInstance();
            final ParameterValueGroup param = processor.getOperation(operation).getParameters();
            param.parameter("Source").setValue(coverage);
            coverage = (GridCoverage2D) processor.doOperation(param);
        }
        Viewer viewer = new Viewer(coverage);
        viewer.setLocale(locale);
        viewer = show(viewer, coverage.getName().toString(locale));
        if (palette) {
            viewer.printPalette(out);
        }
        out.flush();
    }
}
