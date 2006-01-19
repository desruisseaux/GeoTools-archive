/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2002, Institut de Recherche pour le Développement
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.coverage;

// J2SE and JAI dependencies
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.Random;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Geotools dependencies
import org.geotools.coverage.grid.Viewer;
import org.geotools.resources.XMath;


/**
 * Tests the {@link ScaledColorSpace} implementation.
 * This is a visual test when run on the command line.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ScaledColorSpaceTest extends TestCase {
    /**
     * Random number generator for this test.
     */
    private static final Random random = new Random(5078987324568283L);

    /**
     * The minimal and maximal values to renderer.
     */
    private double minimum, maximum;

    /**
     * The scaled color space to test.
     */
    private ScaledColorSpace colors;

    /**
     * The image to use for test.
     */
    private RenderedImage image;

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(ScaledColorSpaceTest.class);
    }
    
    /**
     * Constructs a test case with the given name.
     */
    public ScaledColorSpaceTest(final String name) {
        super(name);
    }

    /**
     * Set up common objects used for all tests.
     */
    protected void setUp() throws Exception {
        super.setUp();
        minimum = random.nextDouble()*100;
        maximum = random.nextDouble()*200 + minimum + 10;
        colors  = new ScaledColorSpace(0, 1, minimum, maximum);

        final int transparency = Transparency.OPAQUE;
        final int datatype     = DataBuffer.TYPE_FLOAT;
        final ColorModel model = new ComponentColorModel(colors, false, false, transparency, datatype);
        final WritableRaster data = model.createCompatibleWritableRaster(200,200);
        final BufferedImage image = new BufferedImage(model, data, false, null);
        for (int x=data.getWidth(); --x>=0;) {
            for (int y=data.getHeight(); --y>=0;) {
                double v = XMath.hypot((double)x/data.getWidth() - 0.5,
                                       (double)y/data.getWidth() - 0.5);
                v = v*(maximum-minimum) + minimum;
                data.setSample(x,y,0,v);
            }
        }
        this.image = image;
    }

    /**
     * Test the color space.
     */
    public void testColorSpace() {
        assertEquals(minimum, colors.getMinValue(0), 1E-4);
        assertEquals(maximum, colors.getMaxValue(0), 1E-4);

        final float[] array = new float[1];
        final double step = (maximum-minimum) / 256;
        for (double x=minimum; x<maximum; x+=step) {
            array[0] = (float)x;
            assertEquals(x, colors.fromRGB(colors.toRGB(array))[0], 1E-3);
        }
    }

    /**
     * Run the visual test.
     */
    public static void main(final String[] args) throws Exception {
        final ScaledColorSpaceTest test = new ScaledColorSpaceTest(null);
        test.setUp();
        test.testColorSpace();
        Viewer.show(test.image);
    }
}
