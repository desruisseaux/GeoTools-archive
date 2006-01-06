/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
package org.geotools.brewer.color;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


/**
 * Contains ColorBrewer palettes and suitability data.
 *
 * @author James Macgill
 * @author Cory Horner, Refractions Research Inc.
 */
public class ColorBrewer {
    
    public static final PaletteType ALL = new PaletteType(true, true, "ALL");
    public static final PaletteType SUITABLE_RANGED = new PaletteType(true, false);
    public static final PaletteType SUITABLE_UNIQUE = new PaletteType(false, true);

    public static final PaletteType SEQUENTIAL = new PaletteType(true, false, "SEQUENTIAL");
    public static final PaletteType DIVERGING = new PaletteType(true, false, "DIVERGING");
    public static final PaletteType QUALITATIVE = new PaletteType(false, true, "QUALITATIVE");
    
    /**
     * @deprecated
     */
    String legendType = null;
    
    String name = null;
    String description = null;
    Hashtable palettes = new Hashtable();;

    /**
     * Creates a new instance of ColorBrewer
     */
    public ColorBrewer() {
    }

    public void registerPalette(BrewerPalette pal) {
        palettes.put(pal.getName(), pal);
    }

    /**
     * Returns the legend type of the colorBrewer
     * @deprecated ColorBrewer no longer knows its legend type, palettes do.
     * @return
     */
    public String getLegendType() {
        return legendType;
    }

    public BrewerPalette[] getPalettes() {
    	Object[] entry = this.palettes.keySet().toArray();
    	BrewerPalette[] palettes = new BrewerPalette[entry.length];
        for (int i = 0; i < entry.length; i++) {
            palettes[i] = (BrewerPalette) getPalette(entry[i].toString());
        }
    	return palettes;
    }
    
    public BrewerPalette[] getPalettes(PaletteType type) {
    	return getPalettes(type, -1);
    }
    
    public BrewerPalette[] getPalettes(PaletteType type, int numClasses) {
    	List palettes = new ArrayList();
    	Object[] entry = this.palettes.keySet().toArray();
        for (int i = 0; i < entry.length; i++) {
            BrewerPalette pal = (BrewerPalette) getPalette(entry[i].toString());
            boolean match = true;
            //filter by number of classes
            if (numClasses > -1) {
            	if (pal.getMaxColors() < numClasses) match = false;	
            }
            if (!pal.getType().isMatch(type)) match = false;
            if (match) {
            	palettes.add(pal);
            }
        }
        return (BrewerPalette[]) palettes.toArray(new BrewerPalette[palettes.size()]);
    }
    
    public BrewerPalette[] getPalettes(PaletteType type, int numClasses, int requiredViewers) {
    	List palettes = new ArrayList();
    	Object[] entry = this.palettes.keySet().toArray();
        for (int i = 0; i < entry.length; i++) {
            BrewerPalette pal = (BrewerPalette) getPalette(entry[i].toString());
            boolean match = true;
            //filter by number of classes
            if (numClasses > -1) {
            	if (pal.getMaxColors() < numClasses) match = false;	
            }
            if (!pal.getType().isMatch(type)) match = false;
            int[] suitability = pal.getPaletteSuitability().getSuitability(numClasses);
            if (isSet(PaletteSuitability.VIEWER_COLORBLIND, requiredViewers) && (suitability[PaletteSuitability.VIEWER_COLORBLIND] != PaletteSuitability.QUALITY_GOOD ))
            	match = false;
            else if (isSet(PaletteSuitability.VIEWER_CRT, requiredViewers) && (suitability[PaletteSuitability.VIEWER_CRT] != PaletteSuitability.QUALITY_GOOD ))
            	match = false;
            else if (isSet(PaletteSuitability.VIEWER_LCD, requiredViewers) && (suitability[PaletteSuitability.VIEWER_LCD] != PaletteSuitability.QUALITY_GOOD ))
            	match = false;
            else if (isSet(PaletteSuitability.VIEWER_PHOTOCOPY, requiredViewers) && (suitability[PaletteSuitability.VIEWER_PHOTOCOPY] != PaletteSuitability.QUALITY_GOOD ))
            	match = false;
            else if (isSet(PaletteSuitability.VIEWER_PRINT, requiredViewers) && (suitability[PaletteSuitability.VIEWER_PRINT] != PaletteSuitability.QUALITY_GOOD ))
            	match = false;
            else if (isSet(PaletteSuitability.VIEWER_PROJECTOR, requiredViewers) && (suitability[PaletteSuitability.VIEWER_PROJECTOR] != PaletteSuitability.QUALITY_GOOD ))
            	match = false;
        	if (match) {
            	palettes.add(pal);
            }
        }
        return (BrewerPalette[]) palettes.toArray(new BrewerPalette[palettes.size()]);
    }
    
    /**
     * 
     * @return
     */
    public String[] getPaletteNames() {
        Object[] keys = palettes.keySet().toArray();
        String[] paletteList = new String[keys.length];

        for (int i = 0; i < keys.length; i++) {
            paletteList[i] = keys[i].toString();
        }

        return paletteList;
    }

    /**
     * Generates an array of palette names for palettes which have at least x
     * classes and at most y classes.
     *
     * @param minClasses x
     * @param maxClasses y
     *
     * @return filtered string array of palette names
     */
    public String[] getPaletteNames(int minClasses, int maxClasses) {
        Object[] keys = palettes.keySet().toArray();
        Set paletteSet = new HashSet();

        //generate the set of palette names
        for (int i = 0; i < keys.length; i++) {
            BrewerPalette thisPalette = (BrewerPalette) palettes.get(keys[i]);
            int numColors = thisPalette.getMaxColors();

            if ((numColors >= minClasses) && (numColors <= maxClasses)) {
                paletteSet.add(thisPalette.getName());
            }
        }

        //convert set to string array
        String[] paletteList = new String[paletteSet.size()];
        Object[] paletteObjList = paletteSet.toArray();

        for (int i = 0; i < paletteSet.size(); i++) {
            paletteList[i] = (String) paletteObjList[i];
        }

        return paletteList;
    }

    public BrewerPalette getPalette(String name) {
        return (BrewerPalette) palettes.get(name);
    }

    /**
     * Loads the default ColorBrewer palettes.
     * @throws IOException 
     *
     */
    public void loadPalettes() throws IOException {
    	loadPalettes(SEQUENTIAL);
    	loadPalettes(DIVERGING);
    	loadPalettes(QUALITATIVE);
    }
    
    /**
     * Loads one set of ColorBrewer palettes. 
     * @param type 
     * @throws IOException 
     */
    public void loadPalettes(PaletteType type) throws IOException {
    	if (type.equals(ALL)) {
    		loadPalettes();
    		return;
    	} else if (type.equals(SUITABLE_RANGED)) {
    		loadPalettes(SEQUENTIAL);
        	loadPalettes(DIVERGING);
    		return;
    	} else if (type.equals(SUITABLE_UNIQUE)) {
    		loadPalettes(QUALITATIVE);
    		return;
    	}
    	if (type.getName() == null) return;
    	String paletteSet = type.getName().toLowerCase();
        URL url = getClass().getResource("resources/" + paletteSet + ".xml");
        InputStream stream = url.openStream();
        load(stream, type);
    }
    
    /**
     * 
     * @param XMLinput
     * @param type identifier for palettes. use "new PaletteType();"
     */
    public void loadPalettes(InputStream XMLinput, PaletteType type) {
    	load(XMLinput, type);
    }
    
    private void load(InputStream stream, PaletteType type) {
    	try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(stream);
			this.name = fixToString(document.getElementsByTagName("name").item(
					0).getFirstChild().toString());
			this.description = fixToString(document.getElementsByTagName(
					"description").item(0).getFirstChild().toString());

			SampleScheme scheme = new SampleScheme();

			NodeList samples = document.getElementsByTagName("sample");

			for (int i = 0; i < samples.getLength(); i++) {
				Node sample = samples.item(i);
				int size = Integer.parseInt(sample.getAttributes()
						.getNamedItem("size").getNodeValue());
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
						pal
								.setName(fixToString(item.getFirstChild()
										.toString()));
					}

					if (item.getNodeName().equals("description")) {
						pal.setDescription(fixToString(item.getFirstChild()
								.toString()));
					}

					if (item.getNodeName().equals("colors")) {
						StringTokenizer oTok = new StringTokenizer(
								fixToString(item.getFirstChild().toString()));
						int numColors = 0;
						Color[] colors = new Color[15];

						for (int k = 0; k < 15; k++) { 
							if (!oTok.hasMoreTokens()) {
								break;
							}

							String entry = oTok.nextToken(":");
							StringTokenizer iTok = new StringTokenizer(entry);
							int r = Integer
									.parseInt(iTok.nextToken(",").trim());
							int g = Integer
									.parseInt(iTok.nextToken(",").trim());
							int b = Integer
									.parseInt(iTok.nextToken(",").trim());
							colors[numColors] = new Color(r, g, b);
							numColors++;
						}

						pal.setColors(colors);
					}

					if (item.getNodeName().equals("suitability")) {
						NodeList schemeSuitability = item.getChildNodes();

						for (int k = 0; k < schemeSuitability.getLength(); k++) {
							Node palScheme = schemeSuitability.item(k);

							if (palScheme.getNodeName().equals("scheme")) {
								int paletteSize = Integer.parseInt(palScheme
										.getAttributes().getNamedItem("size")
										.getNodeValue());

								String values = fixToString(palScheme
										.getFirstChild().toString());
								String[] list = new String[6];
								StringTokenizer tok = new StringTokenizer(
										values);

								// obtain all 6 values, which should each be
								// G=GOOD, D=DOUBTFUL, B=BAD, or ?=UNKNOWN.
								for (int m = 0; m < 6; m++) {
									list[m] = tok.nextToken(",");
								}

								suitability.setSuitability(paletteSize, list);
							}
						}
					}
				}

				pal.setType(type);
				pal.setColorScheme(scheme);
				pal.setPaletteSuitability(suitability);
				registerPalette(pal); // add the palette
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
	 * Loads the appropriate set of palettes into the ColorBrewer
	 * 
	 * @deprecated constants are now gone, use "SEQUENTIAL", "DIVERGING", or "QUALITATIVE" rather than ColorBrewer.SEQUENTIAL, DIVERGING, or QUALITATIVE
	 * @param legendType "SEQUENTIAL", "DIVERGING", or "QUALITATIVE"
	 */
    public void loadPalettes(String legendType) {
        this.legendType = legendType; // save palette name
        this.palettes = new Hashtable(); // reset palette

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            String palette = legendType.toLowerCase();
            URL url = getClass().getResource("resources/" + palette + ".xml");
            InputStream stream = url.openStream();
            Document document = builder.parse(stream);
            this.name = fixToString(document.getElementsByTagName("name").item(0)
                                            .getFirstChild().toString());
            this.description = fixToString(document.getElementsByTagName(
                        "description").item(0).getFirstChild().toString());

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

                    if (item.getNodeName().equals("description")) {
                        pal.setDescription(fixToString(
                                item.getFirstChild().toString()));
                    }

                    if (item.getNodeName().equals("colors")) {
                        StringTokenizer oTok = new StringTokenizer(fixToString(
                                    item.getFirstChild().toString()));
                        int numColors = 0;
                        Color[] colors = new Color[15];

                        for (int k = 0; k < 15; k++) { //alternate condition: "oTok.countTokens() > 0"

                            if (!oTok.hasMoreTokens()) {
                                break;
                            }

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

                                String values = fixToString(palScheme.getFirstChild()
                                                                     .toString());
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

                //pal.setType(legendType);
                pal.setColorScheme(scheme);
                pal.setPaletteSuitability(suitability);
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
     *
     * @return
     */
    private String fixToString(String input) {
        if (input.startsWith("[") && input.endsWith("]")) {
            input = input.substring(1, input.length() - 1); //remove []
            input = input.replaceAll("#text: ", ""); //remove "#text: " 
        }
        return input;
    }

    public String getName() {
    	return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void reset() {
        legendType = null;
        name = null;
        description = null;
        palettes = new Hashtable();
    }
    
    public boolean isSet(int singleValue, int multipleValue) {
    	return ((singleValue & multipleValue) != 0);
    }
}
