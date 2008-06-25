/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.wps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import net.opengis.ows11.CodeType;
import net.opengis.ows11.Ows11Factory;
import net.opengis.wps.ComplexDataType;
import net.opengis.wps.DataInputsType1;
import net.opengis.wps.DataType;
import net.opengis.wps.ExecuteType;
import net.opengis.wps.InputType;
import net.opengis.wps.WpsFactory;

import org.geotools.xml.Encoder;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import junit.framework.TestCase;

public class ExecuteTest extends TestCase {

	/*
	 * Try doing an execute request from the example xsd and parse it
	 */
	public void testExecute() throws IOException, SAXException, ParserConfigurationException {
		   URL url = new URL("http://schemas.opengis.net/wps/1.0.0/examples/51_wpsExecute_request_ResponseDocument.xml");
		   BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		   org.geotools.xml.Parser parser = new org.geotools.xml.Parser(new WPSConfiguration());
		   Object obj = parser.parse(in);
		   assertNotNull(obj);
	}
        
        public void testExecuteEncode() throws Exception {
            WpsFactory f = WpsFactory.eINSTANCE;
            ExecuteType ex = f.createExecuteType();
            
            CodeType id = Ows11Factory.eINSTANCE.createCodeType();
            ex.setIdentifier( id );
            id.setValue( "foo" );
            
            DataInputsType1 inputs = f.createDataInputsType1();
            ex.setDataInputs(inputs);
            
            InputType in = f.createInputType();
            inputs.getInput().add( in );
        
            DataType data = f.createDataType();
            in.setData( data );
            
            ComplexDataType cd = f.createComplexDataType();
            data.setComplexData( cd );
                
            cd.getData().add( new GeometryFactory().createPoint( new Coordinate( 1, 2 ) ) );
            
            Encoder e = new Encoder( new WPSConfiguration() );
            e.setIndenting( true );
            e.encode( ex, WPS.Execute, System.out );
        }
}
