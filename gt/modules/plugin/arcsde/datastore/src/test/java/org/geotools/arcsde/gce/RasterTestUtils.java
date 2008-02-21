package org.geotools.arcsde.gce;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.media.jai.PlanarImage;

public class RasterTestUtils {

    public static boolean imageEquals(RenderedImage image, String filename) throws IOException {
        BufferedImage original = PlanarImage.wrapRenderedImage(image).getAsBufferedImage();
        InputStream in = org.geotools.test.TestData.url(null, "raster-images/" + filename)
                .openStream();
        BufferedImage expected = ImageIO.read(in);

        for (int xpos = 0; xpos < expected.getWidth(); xpos++) {
            for (int ypos = 0; ypos < expected.getHeight(); ypos++) {
                if (original.getRGB(xpos, ypos) != expected.getRGB(xpos, ypos))
                    return false;
            }
        }
        return true;
    }

}
