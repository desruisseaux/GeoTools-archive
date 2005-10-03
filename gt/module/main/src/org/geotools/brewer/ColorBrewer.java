/*
 * ColorBrewer.java
 *
 * Created on 02 October 2005, 11:04
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.geotools.brewer.color;

import java.awt.Color;
import java.io.IOException;
import java.util.Hashtable;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author James
 */
public class ColorBrewer {
    Hashtable palettes;
    /** Creates a new instance of ColorBrewer */
    public ColorBrewer() {
    }
    
    public void registerPalette(BrewerPalette pal){
        palettes.put(pal.getName(), pal);
    }
    
    public BrewerPalette getPalette(String name){
        return (BrewerPalette)palettes.get(name);
    }
    
    protected void loadPalettes(){
        try {
            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(getClass().getResourceAsStream("resuources/sequential.xml") );
            
            BrewerPalette seq = new BrewerPalette();
            seq.setName(document.getElementsByTagName("name").item(0).getFirstChild().toString());
            SampleScheme scheme = new SampleScheme();
            
            
            
            System.out.println(document.getElementsByTagName("sample").getLength());
            
            
            NodeList samples = document.getElementsByTagName("sample");
            for(int i=0; i< samples.getLength(); i++){
                Node sample = samples.item(i);
                int size = Integer.parseInt(sample.getAttributes().getNamedItem("size").getNodeValue());
                String values = sample.getFirstChild().toString();
                int list[] = new int[size];
                StringTokenizer tok = new StringTokenizer(values);
                for(int j=0; j<size;j++){
                    list[i]=Integer.parseInt(tok.nextToken(","));
                }
                scheme.setSampleScheme(size, list);
            }
            NodeList palettes = document.getElementsByTagName("palette");
            for(int i=0; i< palettes.getLength(); i++){
                BrewerPalette pal = new BrewerPalette();
                NodeList paletteInfo = palettes.item(i).getChildNodes();
                for(int j=0; j<paletteInfo.getLength(); j++){
                    Node item = paletteInfo.item(j);
                    if(item.getNodeName().equals("name")){
                        pal.setName(item.getFirstChild().toString());
                    }
                    if(item.getNodeName().equals("rgb")){
                        StringTokenizer oTok = new StringTokenizer(item.getFirstChild().toString());
                        Color[] colors = new Color[oTok.countTokens()];
                        String entry = oTok.nextToken(":");
                        for(int k=0;k<colors.length;k++){
                            StringTokenizer iTok = new StringTokenizer(entry);
                            int r = Integer.parseInt(iTok.nextToken(",").trim());
                            int g = Integer.parseInt(iTok.nextToken(",").trim());
                            int b = Integer.parseInt(iTok.nextToken(",").trim());
                            colors[k] = new Color(r,g,b);
                        }
                        pal.setColors(colors);
                    }
                    
                }
                
                
            }
            
            
            
        } catch (SAXException sxe) {
            // Error generated during parsing)
            Exception  x = sxe;
            if (sxe.getException() != null)
                x = sxe.getException();
            x.printStackTrace();
            
        } catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            pce.printStackTrace();
            
        } catch (IOException ioe) {
            // I/O error
            ioe.printStackTrace();
        }
        
        
    }
    
}
