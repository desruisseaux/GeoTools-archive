package org.geotools.data.wms;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.vividsolutions.jts.geom.Envelope;

public class ShapefileMetadataReader {

    Document metadataDocument;
    
    public ShapefileMetadataReader(URL metadatFileURL) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder(false);
        
        URLConnection connection = metadatFileURL.openConnection();

        metadataDocument = builder.build(connection.getInputStream());
    }
    
    public Envelope getBoundingBox() {
        Element bounding = metadataDocument.getRootElement().getChild("idinfo").getChild("spdom").getChild("bounding");
        double minX = Double.parseDouble(bounding.getChildText("westbc"));
        double maxX = Double.parseDouble(bounding.getChildText("eastbc"));
        double minY = Double.parseDouble(bounding.getChildText("southbc"));
        double maxY = Double.parseDouble(bounding.getChildText("northbc"));
        
        return new Envelope(minX, maxX, minY, maxY);
    }
    
    
}
