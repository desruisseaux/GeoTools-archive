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
package org.geotools.data.wfs.demo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 *  summary sentence.
 * <p>
 * Paragraph ...
 * </p><p>
 * Responsibilities:
 * <ul>
 * <li>
 * <li>
 * </ul>
 * </p><p>
 * Example:<pre><code>
 * PostDemo x = new PostDemo( ... );
 * TODO code example
 * </code></pre>
 * </p>
 * @author dzwiers
 * @since 0.6.0
 */
public class PostDemo {
    public static void main(String[] args) throws IOException{
        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
             "<DescribeFeatureType xmlns=\"http://www.opengis.net/wfs\" " +
             "xmlns:gml=\"http://www.opengis.net/gml\" " +
             "xmlns:ogc=\"http://www.opengis.net/ogc\" version=\"1.0.0\" " +
             "service=\"WFS\" outputFormat=\"XMLSCHEMA\">" +
             "<TypeName>van:Airport</TypeName></DescribeFeatureType>";
        
        System.out.println(s+"\n\n\n");
        URL url = new URL("http://wfs.galdosinc.com:8680/wfs/http?Request=GetCapabilities&service=WFS");

        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Content-type", "application/xml");
        
        url.openConnection().connect();
        Writer w = new OutputStreamWriter(connection.getOutputStream());
        w.write(s);
        w.flush();
        w.close();
        BufferedReader r = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        while(r.ready()){
            System.out.print((String)r.readLine());
        }
    }

}
