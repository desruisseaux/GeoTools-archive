/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
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
package org.geotools.xml.ogc;

import java.io.IOException;
import java.io.StringWriter;

import javax.naming.OperationNotSupportedException;

import org.geotools.filter.BetweenFilter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.IllegalFilterException;
import org.geotools.xml.DocumentWriter;

/**
 *  For now just writes the expression built.
 *  
 * @author David Zwiers, Refractions Research
 */
public class ExpresionEncodeTest extends FilterTestSupport {

    /** Constructor with test name. */
    String dataFolder = "";

    public ExpresionEncodeTest(String testName) {
        super(testName);

        //_log.getLoggerRepository().setThreshold(Level.DEBUG);
        LOGGER.finer("running XMLEncoderTests");
        
        dataFolder = System.getProperty("dataFolder");

        if (dataFolder == null) {
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder = "file:////" + "tests/unit/testData"; //url.toString();
            LOGGER.finer("data folder is " + dataFolder);
        }
    }
    
    public void testPropBetweenFilter() throws IllegalFilterException, OperationNotSupportedException, IOException{
        FilterFactory ff = FilterFactory.createFilterFactory();
        BetweenFilter bf = ff.createBetweenFilter();
        bf.addLeftValue(ff.createLiteralExpression(60000));
        bf.addMiddleValue(ff.createAttributeExpression(testSchema,"testDouble"));
        bf.addRightValue(ff.createLiteralExpression(200000));
        

        StringWriter output = new StringWriter();
        DocumentWriter.writeFragment(bf,
            FilterSchema.getInstance(), output, null);
        
        System.out.println(output);
    }
}
