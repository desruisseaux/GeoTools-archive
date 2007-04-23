/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.geotools.parameter;

// J2SE dependencies
import java.util.Map;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Logger;
import java.awt.image.RenderedImage;

// JAI dependencies
import javax.media.jai.JAI;
import javax.media.jai.RegistryElementDescriptor;
import javax.media.jai.registry.RenderedRegistryMode;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.util.GenericName;
import org.opengis.metadata.citation.Citation;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterDescriptor;

// Geotools dependencies
import org.geotools.metadata.iso.citation.Citations;


/**
 * Tests the wrapper for JAI's parameters.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class ImagingParametersTest extends TestCase {
    /**
     * Run the suite from the command line.
     */
    public static void main(final String[] args) {
        org.geotools.util.Logging.GEOTOOLS.forceMonolineConsoleOutput();
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(ImagingParametersTest.class);
    }

    /**
     * Constructs a test case.
     */
    public ImagingParametersTest(String testName) {
        super(testName);
    }

    /**
     * Tests {@link ImagingParameters}.
     */
    public void testDescriptors() {
        final String author = Citations.JAI.getTitle().toString();
        final String vendor = "com.sun.media.jai";
        final String mode   = RenderedRegistryMode.MODE_NAME;
        final RegistryElementDescriptor   descriptor;
        final ImagingParameterDescriptors parameters;
        descriptor = JAI.getDefaultInstance().getOperationRegistry().getDescriptor(mode, "AddConst");
        parameters = new ImagingParameterDescriptors(descriptor);
        final GenericName alias = (GenericName) parameters.getAlias().iterator().next();
        /*
         * Tests the operation-wide properties.
         */
        assertEquals   ("Name",  "AddConst", parameters.getName().getCode());
        assertEquals   ("Authority", author, parameters.getName().getAuthority().getTitle().toString());
        assertEquals   ("Vendor",    vendor, alias     .getScope().toString());
        assertNotNull  ("Version",           parameters.getName().getVersion());
        assertLocalized("Vendor",            alias     .getScope().toInternationalString());
        assertLocalized("Remarks",           parameters.getRemarks());
        assertTrue     ("Remarks",           parameters.getRemarks().toString().trim().length() > 0);
        /*
         * Tests the properties for a specific parameter in the parameter group.
         */
        final ParameterDescriptor param = (ParameterDescriptor) parameters.descriptor("constants");
        assertEquals   ("Name",   "constants",    param.getName().getCode());
        assertEquals   ("Type",   double[].class, param.getValueClass());
        assertEquals   ("Default", 1, ((double[]) param.getDefaultValue()).length);
        assertNull     ("Minimum",                param.getMinimumValue());
        assertNull     ("Maximum",                param.getMaximumValue());
        assertNull     ("Valid values",           param.getValidValues());
        assertLocalized("Remarks",                param.getRemarks());
        assertFalse(parameters.getRemarks().toString().trim().equalsIgnoreCase(
                         param.getRemarks().toString().trim()));
        /*
         * Tests parameter values.
         */
        final ImagingParameters values = (ImagingParameters) parameters.createValue();
        for (int i=0; i<20; i++) {
            final ParameterValue before = values.parameter("constants");
            if ((i % 5)==0) {
                values.parameters.setParameter("constants", new double[]{i});
            } else {
                values.parameter("constants").setValue(new double[]{i});
            }
            assertTrue(Arrays.equals(values.parameter("constants").doubleValueList(),
                          (double[]) values.parameters.getObjectParameter("constants")));
            assertSame(before, values.parameter("constants"));
        }
        assertNotNull(values.toString());
        /*
         * Tests clone. Requires J2SE 1.5 or above.
         */
        if (System.getProperty("java.version").compareTo("1.5") >= 0) {
            final ImagingParameters copy = (ImagingParameters) values.clone();
            assertNotSame("clone", values, copy);
            assertNotSame("clone", values.parameters, copy.parameters);
            if (false) {
                // NOTE: As of J2SE 1.5 and JAI 1.1, ParameterBlockJAI
                //       doesn't implements the 'equals' method.
                assertEquals("clone", values.parameters, copy.parameters);
                assertEquals("clone", values, copy);
            }
        } else {
            Logger.getLogger("org.geotools.parameter")
                       .fine("Clone test skipped for pre-1.5 Java version.");
        }
    }

    /**
     * Ensures that the specified character sequence created from JAI parameters preserve the
     * localization infos.
     */
    private static void assertLocalized(final String name, final CharSequence title) {
        assertTrue(name, title instanceof ImagingParameterDescription);
    }
}
