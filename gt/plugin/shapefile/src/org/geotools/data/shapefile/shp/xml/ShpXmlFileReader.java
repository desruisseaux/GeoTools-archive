package org.geotools.data.shapefile.shp.xml;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.vividsolutions.jts.geom.Envelope;

public class ShpXmlFileReader {

    Document dom;
    
    /**
     * Parse metadataFile (currently for bounding box information).
     * <p>
     *  
     * </p>
     * @param metadatFileURL
     * @throws JDOMException
     * @throws IOException
     */
    public ShpXmlFileReader(URL metadatFileURL) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder(false);
        
        URLConnection connection = metadatFileURL.openConnection();

        dom = builder.build(connection.getInputStream());
    }
    
    public Metadata parse() {
        return parseMetadata( dom.getRootElement() );
    }
    
    protected Metadata parseMetadata(Element root) {
        Metadata meta = new Metadata();
        meta.setIdinfo( parseIdInfo( root.getChild("idinfo")) );
        
        return meta;
    }
    protected IdInfo parseIdInfo(Element element ) {
        IdInfo idInfo = new IdInfo();
        
        Element bounding = element.getChild("spdom").getChild("bounding");
        idInfo.setBounding( parseBounding( bounding ) );
        
        Element lbounding = element.getChild("spdom").getChild("lbounding");
        idInfo.setLbounding( parseBounding( lbounding ) );
        
        return idInfo;
    }
    protected Envelope parseBounding( Element bounding ) {
        if( bounding == null ) return new Envelope();
        
        double minX = Double.parseDouble(bounding.getChildText("westbc"));
        double maxX = Double.parseDouble(bounding.getChildText("eastbc"));
        double minY = Double.parseDouble(bounding.getChildText("southbc"));
        double maxY = Double.parseDouble(bounding.getChildText("northbc"));
        
        return new Envelope(minX, maxX, minY, maxY);
    }    
    
}