/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, GeoTools Project Management Committee (PMC)
 *    (C) 2007, GeoSolutions S.A.S.
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
package org.geotools.coverageio.gdal.jp2mrsid;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverageio.gdal.BaseGDALGridCoverage2DReader;
import org.geotools.coverageio.gdal.jp2kak.JP2KReader;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.test.TestData;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;

/**
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini (simboss), GeoSolutions
 *
 * Testing {@link JP2KReader}
 */
public final class JP2MrSIDTest extends AbstractJP2MrSIDTestCase {
    /**
     * file name of a valid JP2K sample data to be used for tests.
     */
    private final static String fileName = "bogota.jp2";

    /**
     * Creates a new instance of JP2KTest
     *
     * @param name
     */
    public JP2MrSIDTest(String name) {
        super(name);
    }

    public static final void main(String[] args) throws Exception {
        junit.textui.TestRunner.run(JP2MrSIDTest.class);
    }

    public void test() throws Exception {
        if (!testingEnabled()) {
            return;
        }

        final BaseGDALGridCoverage2DReader reader = new JP2MrSIDReader(TestData.file(this, fileName));
        final ParameterValue gg = (ParameterValue) ((AbstractGridFormat) reader.getFormat()).READ_GRIDGEOMETRY2D
            .createValue();
        final GeneralEnvelope oldEnvelope = reader.getOriginalEnvelope();
        gg.setValue(new GridGeometry2D(reader.getOriginalGridRange(), oldEnvelope));

        final GridCoverage2D gc = (GridCoverage2D) reader.read(new GeneralParameterValue[] { gg });

        assertNotNull(gc);

        if (TestData.isInteractiveTest()) {
            gc.show();
        } else {
            gc.getRenderedImage().getData();
        }

        if (TestData.isInteractiveTest()) {
            // printing CRS information
            LOGGER.info(gc.getCoordinateReferenceSystem().toWKT());
            LOGGER.info(gc.getEnvelope().toString());
        }
    }
}
