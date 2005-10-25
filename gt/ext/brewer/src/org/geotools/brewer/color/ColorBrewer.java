package org.geotools.brewer.color;


import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


/**
 * DOCUMENT ME!
 *
 * @author James
 */
public class ColorBrewer {
    public static final String SEQUENTIAL = "SEQUENTIAL";
    public static final String DIVERGING = "DIVERGING";
    public static final String QUALITATIVE = "QUALITATIVE";
    String legendType;
    Hashtable palettes;

    /**
     * Creates a new instance of ColorBrewer
     */
    public ColorBrewer() {
    }

    public void registerPalette(BrewerPalette pal) {
        palettes.put(pal.getName(), pal);
    }

    public String getLegendType() {
        return legendType;
    }

    public String[] getPaletteNames() {
    	Object[] keys = palettes.keySet().toArray();
    	String[] paletteList = new String[keys.length];
    	for (int i = 0; i < keys.length; i++) {
    		paletteList[i] = keys[i].toString();
    	}
        return paletteList;
    }

    public BrewerPalette getPalette(String name) {
        return (BrewerPalette) palettes.get(name);
    }

    /**
     * Loads the appropriate palette into the ColorBrewer
     *
     * @param legendType ColorBrewer.SEQUENTIAL, DIVERGING, or QUALITATIVE
     */
    public void loadPalettes(String legendType) {
        this.legendType = legendType; //save palette name
        this.palettes = new Hashtable(); //reset palette

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream stream = getClass().getResourceAsStream("resources/"
                    + legendType.toLowerCase() + ".xml");
            Document document = builder.parse(stream);
            BrewerPalette seq = new BrewerPalette();
            seq.setName(fixToString(document.getElementsByTagName("name").item(0)
                                .getFirstChild().toString()));

            SampleScheme scheme = new SampleScheme();

            NodeList samples = document.getElementsByTagName("sample");

            for (int i = 0; i < samples.getLength(); i++) {
                Node sample = samples.item(i);
                int size = Integer.parseInt(sample.getAttributes()
                                                  .getNamedItem("size")
                                                  .getNodeValue());
                String values = fixToString(sample.getFirstChild().toString());
                int[] list = new int[size];
                StringTokenizer tok = new StringTokenizer(values);

                for (int j = 0; j < size; j++) {
                    list[j] = Integer.parseInt(tok.nextToken(","));
                }

                scheme.setSampleScheme(size, list);
            }

            NodeList palettes = document.getElementsByTagName("palette");

            for (int i = 0; i < palettes.getLength(); i++) {
                BrewerPalette pal = new BrewerPalette();
                PaletteSuitability suitability = new PaletteSuitability();
                NodeList paletteInfo = palettes.item(i).getChildNodes();

                for (int j = 0; j < paletteInfo.getLength(); j++) {
                    Node item = paletteInfo.item(j);

                    if (item.getNodeName().equals("name")) {
                        pal.setName(fixToString(item.getFirstChild().toString()));
                    }

                    if (item.getNodeName().equals("colors")) {
                        StringTokenizer oTok = new StringTokenizer(fixToString(item.getFirstChild().toString()));
                        int numColors = 0;
                        Color[] colors = new Color[13];
                        for (int k = 0; k < 13; k++) { //alternate condition: "oTok.countTokens() > 0"
                            String entry = oTok.nextToken(":");
                            StringTokenizer iTok = new StringTokenizer(entry);
                            int r = Integer.parseInt(iTok.nextToken(",").trim());
                            int g = Integer.parseInt(iTok.nextToken(",").trim());
                            int b = Integer.parseInt(iTok.nextToken(",").trim());
                            colors[numColors] = new Color(r, g, b);
                            numColors++;
                        }

                        pal.setColors(colors);
                    }

                    if (item.getNodeName().equals("suitability")) {
                        NodeList schemeSuitability = item.getChildNodes();
                        for (int k = 0; k < schemeSuitability.getLength();
                                k++) {
                            Node palScheme = schemeSuitability.item(k);
                            if (palScheme.getNodeName().equals("scheme")) {
	                            int paletteSize = Integer.parseInt(palScheme.getAttributes()
	                                                                     .getNamedItem("size")
	                                                                     .getNodeValue());
	                            
	                            String values = fixToString(palScheme.getFirstChild().toString());
	                            String[] list = new String[6];
	                            StringTokenizer tok = new StringTokenizer(values);
	
	                            //obtain all 6 values, which should each be G=GOOD, D=DOUBTFUL, B=BAD, or ?=UNKNOWN.
	                            for (int m = 0; m < 6; m++) {
	                                list[m] = tok.nextToken(",");
	                            }
	                            suitability.setSuitability(paletteSize, list);
                            }
                        }
                    }
                }

                pal.setType(legendType);
                pal.sampler = scheme;
                pal.suitability = suitability;
                registerPalette(pal); //add the palette
            }
        } catch (SAXException sxe) {
            // Error generated during parsing)
            Exception x = sxe;

            if (sxe.getException() != null) {
                x = sxe.getException();
            }

            x.printStackTrace();
        } catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            pce.printStackTrace();
        } catch (IOException ioe) {
            // I/O error
            ioe.printStackTrace();
        }
    }
    
    /**
	 * Converts "[#text: 1,2,3]" to "1,2,3".
	 * 
	 * <p>
	 * This is a brutal hack for fixing the org.w3c.dom API. Under j1.4
	 * Node.toString() returns "1,2,3", under j1.5 Node.toString() returns
	 * "[#text: 1,2,3]".
	 * </p>
	 * 
	 * @param input
	 * @return
	 */
    private String fixToString(String input) {
    	if (input.startsWith("[")) {
    		input = input.substring(1, input.length()-1); //remove []
    		input = input.replaceAll("#text: ",""); //remove "#text: " 
    	}
    	return input;
    	
    }
}
