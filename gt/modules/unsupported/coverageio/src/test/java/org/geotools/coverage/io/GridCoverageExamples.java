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
package org.geotools.coverage.io;

// J2SE dependencies and extensions
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import junit.framework.Assert;

// OpenGIS and Geotools dependencies
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.coverage.Category;
import org.geotools.coverage.FactoryFinder;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.test.TestData;
import org.geotools.util.NumberRange;


/**
 * A factory for sample {@link GridCoverage2D}, which may be used for tests
 * in other modules. This factory is a trimmed copy of a class of the same
 * name in the {@code coverage} module. We made this copy because we currently
 * can't leverage test classes defined in an other module.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class GridCoverageExamples extends Assert {
    /**
     * Do not allows instantiation of this class.
     */
    private GridCoverageExamples() {
    }

    /**
     * Returns the number of available image which may be used as example.
     */
    public static int getNumExamples() {
        return 1;
    }

    /**
     * Returns a {@link GridCoverage} which may be used as a "real world" example.
     *
     * @param  number The example number. Numbers are numeroted from
     *               0 to {@link #getNumExamples()} exclusive.
     * @return The "real world" grid coverage.
     * @throws IOException if an I/O operation was needed and failed.
     */
    public static GridCoverage2D getExample( final int number) throws IOException {
        final GridCoverageFactory factory = FactoryFinder.getGridCoverageFactory(null);
        final String                   path;
        final Category[]         categories;
        final CoordinateReferenceSystem crs;
        final Rectangle2D            bounds;
        final GridSampleDimension[]   bands;
        switch (number) {
            default: {
                throw new IllegalArgumentException(String.valueOf(number));
            }
            case 0: {
                //unit = "°C";
                path = "QL95209.png";
                crs  = DefaultGeographicCRS.WGS84;
                categories = new Category[] {
                    new Category("Coast line", Color.decode("#000000"), new NumberRange(  0,   0)),
                    new Category("Cloud",      Color.decode("#C3C3C3"), new NumberRange(  1,   9)),
                    new Category("Unused",     Color.decode("#822382"), new NumberRange( 10,  29)),
                    new Category("Sea Surface Temperature", null,       new NumberRange( 30, 219), 0.1, 10.0),
                    new Category("Unused",     Color.decode("#A0505C"), new NumberRange(220, 239)),
                    new Category("Land",       Color.decode("#D2C8A0"), new NumberRange(240, 254)),
                    new Category("No data",    Color.decode("#FFFFFF"), new NumberRange(255, 255)),
                };
                // 41°S - 5°N ; 35°E - 80°E  (450 x 460 pixels)
                bounds = new Rectangle2D.Double(35, -41, 45, 46);
                bands = new GridSampleDimension[] {
                        new GridSampleDimension("Measure", categories, null)
                    };
                break;
            }
        }
        final GeneralEnvelope envelope = new GeneralEnvelope(bounds);
        final RenderedImage      image = ImageIO.read(TestData.getResource(GridCoverage2D.class, path));
        final String          filename = new File(path).getName();
        envelope.setCoordinateReferenceSystem(crs);
        return (GridCoverage2D) factory.create(filename, image, envelope, bands, null, null);
    }
}
