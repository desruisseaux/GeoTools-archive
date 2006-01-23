/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.gui.swing;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.GraphicsEnvironment;
import java.io.File;

import javax.swing.JFrame;

import junit.framework.Test;
import junit.framework.TestCase;

import org.geotools.gui.swing.sldeditor.style.StyleEditorChooser;
import org.geotools.styling.SLDParser;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactoryFinder;
import org.geotools.resources.TestData;


/**
 *
 * @source $URL$
 * @version $Id$
 * @author wolf
 */
public class LegendEditorTest extends TestCase {
    /**
     * {@code true} for enabling {@code println} statements. By default {@code true}
     * when running from the command line, and {@code false} when running by Maven.
     */
    private static boolean verbose;

    /**
     * The context which contains this maps data
     */
    public LegendEditorTest(String testName) {
        super(testName);
    }
    
    public void testLegend() throws Exception {
        SLDParser sld = null;
        
        File sldFile = TestData.file(this, "color.sld");
        sld = new SLDParser(StyleFactoryFinder.createStyleFactory(), sldFile);
        
        Style[] styles = sld.readXML();
        if (verbose) {
            System.out.println("Style loaded");
        }
        long start = System.currentTimeMillis();
        StyleEditorChooser sec = new StyleEditorChooser(null, styles[0]);
        
        if (verbose) {
            System.out.println("Style editor created in " + (System.currentTimeMillis() - start));
        }    
        // Create frame
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        JFrame frame = new JFrame();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                System.exit(0);
            }
        });
        frame.setContentPane(sec);

        frame.pack();
        frame.setVisible(true);
        Thread.currentThread().sleep(500);
        frame.dispose();
    }
    
    public static void main(java.lang.String[] args) {
        verbose = true;
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new junit.framework.TestSuite(LegendEditorTest.class);
    }
}
