/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.styling;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.filter.ExpressionBuilder;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * TODO: This really needs to be container ready
 * @author jgarnett
 *
 * @source $URL$
 */
public class SLDParser {

	private static final java.util.logging.Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.styling");

	private FilterFactory ff;

	// protected java.io.InputStream instream;
	protected InputSource source;

	private org.w3c.dom.Document dom;

	protected StyleFactory factory;

	private String graphicSt = "Graphic"; // to make pmd to shut up

	private String geomSt = "Geometry"; // to make pmd to shut up

	private String fillSt = "Fill";

	/** useful for detecting relative onlineresources */
	private URL sourceUrl;

	/**
	 * Create a Stylereader - use if you already have a dom to parse.
	 * 
	 * @param factory
	 *            The StyleFactory to use to build the style
	 */
	public SLDParser(StyleFactory factory) {
		this( factory, CommonFactoryFinder.getFilterFactory( GeoTools.getDefaultHints() ));
	}

    public SLDParser(StyleFactory factory, FilterFactory filterFactory) {
        this.factory = factory;
        this.ff = filterFactory;
    }
    
	/**
	 * Creates a new instance of SLDStyler
	 * 
	 * @param factory
	 *            The StyleFactory to use to read the file
	 * @param filename
	 *            The file to be read.
	 * 
	 * @throws java.io.FileNotFoundException -
	 *             if the file is missing
	 */
	public SLDParser(StyleFactory factory, String filename)
			throws java.io.FileNotFoundException {
		this(factory);

		File f = new File(filename);
		setInput(f);
	}

	/**
	 * Creates a new SLDStyle object.
	 * 
	 * @param factory
	 *            The StyleFactory to use to read the file
	 * @param f
	 *            the File to be read
	 * 
	 * @throws java.io.FileNotFoundException -
	 *             if the file is missing
	 */
	public SLDParser(StyleFactory factory, File f)
			throws java.io.FileNotFoundException {
		this(factory);
		setInput(f);
	}

	/**
	 * Creates a new SLDStyle object.
	 * 
	 * @param factory
	 *            The StyleFactory to use to read the file
	 * @param url
	 *            the URL to be read.
	 * 
	 * @throws java.io.IOException -
	 *             if something goes wrong reading the file
	 */
	public SLDParser(StyleFactory factory, java.net.URL url)
			throws java.io.IOException {
		this(factory);
		setInput(url);
	}

	/**
	 * Creates a new SLDStyle object.
	 * 
	 * @param factory
	 *            The StyleFactory to use to read the file
	 * @param s
	 *            The inputstream to be read
	 */
	public SLDParser(StyleFactory factory, java.io.InputStream s) {
		this(factory);
		setInput(s);
	}

	/**
	 * Creates a new SLDStyle object.
	 * 
	 * @param factory
	 *            The StyleFactory to use to read the file
	 * @param r
	 *            The inputstream to be read
	 */
	public SLDParser(StyleFactory factory, java.io.Reader r) {
		this(factory);
		setInput(r);
	}

	/**
	 * set the file to read the SLD from
	 * 
	 * @param filename
	 *            the file to read the SLD from
	 * 
	 * @throws java.io.FileNotFoundException
	 *             if the file is missing
	 */
	public void setInput(String filename) throws java.io.FileNotFoundException {
		File f = new File(filename);
		source = new InputSource(new java.io.FileInputStream(f));
		try {
			sourceUrl = f.toURL();
		} catch (MalformedURLException e) {
			LOGGER.warning("Can't build URL for file " + f.getAbsolutePath());
		}
	}

	/**
	 * Sets the file to use to read the SLD from
	 * 
	 * @param f
	 *            the file to use
	 * 
	 * @throws java.io.FileNotFoundException
	 *             if the file is missing
	 */
	public void setInput(File f) throws java.io.FileNotFoundException {
		source = new InputSource(new java.io.FileInputStream(f));
		try {
			sourceUrl = f.toURL();
		} catch (MalformedURLException e) {
			LOGGER.warning("Can't build URL for file " + f.getAbsolutePath());
		}
	}

	/**
	 * sets an URL to read the SLD from
	 * 
	 * @param url
	 *            the url to read the SLD from
	 * 
	 * @throws java.io.IOException
	 *             If anything goes wrong opening the url
	 */
	public void setInput(java.net.URL url) throws java.io.IOException {
		source = new InputSource(url.openStream());
		sourceUrl = url;
	}

	/**
	 * Sets the input stream to read the SLD from
	 * 
	 * @param in
	 *            the inputstream used to read the SLD from
	 */
	public void setInput(java.io.InputStream in) {
		source = new InputSource(in);
	}

	/**
	 * Sets the input stream to read the SLD from
	 * 
	 * @param in
	 *            the inputstream used to read the SLD from
	 */
	public void setInput(java.io.Reader in) {
		source = new InputSource(in);
	}

	/**
	 * Read the xml inputsource provided and create a Style object for each user
	 * style found
	 * 
	 * @return Style[] the styles constructed.
	 * 
	 * @throws RuntimeException
	 *             if a parsing error occurs
	 */
	public Style[] readXML() {
		javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory
				.newInstance();
		dbf.setNamespaceAware(true);
		try {
			javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
			dom = db.parse(source);
		} catch (javax.xml.parsers.ParserConfigurationException pce) {
			throw new RuntimeException(pce);
		} catch (org.xml.sax.SAXException se) {
			throw new RuntimeException(se);
		} catch (java.io.IOException ie) {
			throw new RuntimeException(ie);
		}

		return readDOM(dom);
	}

	/**
	 * Read the DOM provided and create a Style object for each user style found
	 * 
	 * @param document
	 *            a dom containing the SLD
	 * 
	 * @return Style[] the styles constructed.
	 */
	public Style[] readDOM(org.w3c.dom.Document document) {
		this.dom = document;

		// for our next trick do something with the dom.
		NodeList nodes = findElements(document, "UserStyle");

		if (nodes == null)
			return new Style[0];

		Style[] styles = new Style[nodes.getLength()];

		for (int i = 0; i < nodes.getLength(); i++) {
			styles[i] = parseStyle(nodes.item(i));
		}

		return styles;
	}

	/**
	 * @param document
	 * @param name
	 */
	private NodeList findElements(final org.w3c.dom.Document document,
			final String name) {
		NodeList nodes = document.getElementsByTagNameNS("*", name);

		if (nodes.getLength() == 0) {
			nodes = document.getElementsByTagName(name);
		}

		return nodes;
	}

	private NodeList findElements(final org.w3c.dom.Element element,
			final String name) {
		NodeList nodes = element.getElementsByTagNameNS("*", name);

		if (nodes.getLength() == 0) {
			nodes = element.getElementsByTagName(name);
		}

		return nodes;
	}

	public StyledLayerDescriptor parseSLD() {
		javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory
				.newInstance();
		dbf.setNamespaceAware(true);
		
		try {
			javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
			dom = db.parse(source);
			// for our next trick do something with the dom.

			//NodeList nodes = findElements(dom, "StyledLayerDescriptor");

			StyledLayerDescriptor sld = parseDescriptor(dom
					.getDocumentElement());// should only be one per file
			return sld;

		} catch (javax.xml.parsers.ParserConfigurationException pce) {
			throw new RuntimeException(pce);
		} catch (org.xml.sax.SAXException se) {
			throw new RuntimeException(se);
		} catch (java.io.IOException ie) {
			throw new RuntimeException(ie);
		}
	}

	public StyledLayerDescriptor parseDescriptor(Node root) {
		StyledLayerDescriptor sld = factory.createStyledLayerDescriptor();
		// StyledLayer layer = null;
		// LineSymbolizer symbol = factory.createLineSymbolizer();

		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
				continue;
			}
			String childName = child.getLocalName();
			if (childName == null) {
				childName = child.getNodeName();
			}
			if (childName.equalsIgnoreCase("Name")) {
				sld.setName(child.getFirstChild().getNodeValue());
			}

			if (childName.equalsIgnoreCase("Title")) {
				sld.setTitle(child.getFirstChild().getNodeValue());
			}

			if (childName.equalsIgnoreCase("Abstract")) {
				sld.setAbstract(child.getFirstChild().getNodeValue());
			}

			if (childName.equalsIgnoreCase("NamedLayer")) {
				NamedLayer layer = parseNamedLayer(child);
				sld.addStyledLayer(layer);
			}

			if (childName.equalsIgnoreCase("UserLayer")) {
				StyledLayer layer = parseUserLayer(child);
				sld.addStyledLayer(layer);
			}
		}

		return sld;
	}

	private StyledLayer parseUserLayer(Node root) {
		UserLayer layer = new UserLayerImpl();
		// LineSymbolizer symbol = factory.createLineSymbolizer();

		NodeList children = root.getChildNodes();
		List layerFeatureConstraints = new ArrayList();
		for (int i = 0; i < children.getLength(); i++) 
		{
			Node child = children.item(i);
			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) 
			{
				continue;
			}
			String childName = child.getLocalName();
			if (childName == null) {
				childName = child.getNodeName();
			}
			if (childName.equalsIgnoreCase("InlineFeature"))
			{
			    parseInlineFeature(child,layer);
			}
			if (childName.equalsIgnoreCase("UserStyle")) 
			{
				Style user = parseStyle(child);
				layer.addUserStyle(user);
			}

			if (childName.equalsIgnoreCase("Name")) 
			{
				String layerName = child.getFirstChild().getNodeValue();
				layer.setName(layerName);
				LOGGER.info("layer name: " + layer.getName());
			}
			
			if(childName.equalsIgnoreCase("RemoteOWS")) {
			    RemoteOWS remoteOws = parseRemoteOWS(child);
			    layer.setRemoteOWS(remoteOws);
			}

			if (childName.equalsIgnoreCase("LayerFeatureConstraints")) 
			{
				 layer.setLayerFeatureConstraints(parseLayerFeatureConstraints(child));
			}

		}
		
		return layer;
	}

	private FeatureTypeConstraint[] parseLayerFeatureConstraints(Node root) {
        List featureTypeConstraints = new ArrayList();
        
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) 
        {
            Node child = children.item(i);
            if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) 
            {
                continue;
            }
            String childName = child.getLocalName();
            if(childName.equalsIgnoreCase("FeatureTypeConstraint")) {
                final FeatureTypeConstraint ftc = parseFeatureTypeConstraint(child);
                if(ftc != null)
                    featureTypeConstraints.add(ftc);
            }
        }
        return (FeatureTypeConstraint[]) featureTypeConstraints.toArray(new FeatureTypeConstraint[featureTypeConstraints.size()]);
    }

    private FeatureTypeConstraint parseFeatureTypeConstraint(Node root) {
        FeatureTypeConstraint ftc = new FeatureTypeConstraintImpl();
        
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) 
        {
            Node child = children.item(i);
            if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) 
            {
                continue;
            }
            String childName = child.getLocalName();
            if(childName.equalsIgnoreCase("FeatureTypeName")) {
                ftc.setFeatureTypeName(child.getFirstChild().getNodeValue());
            } else if(childName.equalsIgnoreCase("Filter")) {
                ftc.setFilter(parseFilter(child));
            }
        }
        ftc.setExtents(new Extent[0]);
        if(ftc.getFeatureTypeName() == null)
            return null;
        else
            return ftc;
    }

    private RemoteOWS parseRemoteOWS(Node root) {
	    RemoteOWS ows = new RemoteOWSImpl();

        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) 
            {
                continue;
            }
            String childName = child.getLocalName();
            
            if(childName.equalsIgnoreCase("Service")) {
                ows.setService(child.getFirstChild().getNodeValue());
            } else if(childName.equalsIgnoreCase("OnlineResource")) {
                ows.setOnlineResource(parseOnlineResource(child));
            }
        }
        return ows;
    }

    /**
	 * 
	 * @param child
	 * @param layer
	 */
	private void parseInlineFeature(Node root, UserLayer layer) 
	{		
		try{
			SLDInlineFeatureParser inparser = new SLDInlineFeatureParser(root);
			layer.setInlineFeatureDatastore(inparser.dataStore);
			layer.setInlineFeatureType(inparser.featureType);
		}
		catch (Exception e)
		{
			throw (IllegalArgumentException) new IllegalArgumentException().initCause(e);
		}
		
	}

	/**
	 * Parses a NamedLayer.
	 * <p>
	 * The NamedLayer schema is:
	 * <pre><code>
	 * <xsd:element name="NamedLayer">
	 *  <xsd:annotation>
	 *   <xsd:documentation> A NamedLayer is a layer of data that has a name advertised by a WMS. </xsd:documentation>
	 *  </xsd:annotation>
	 *  <xsd:complexType>
	 *   <xsd:sequence>
	 *    <xsd:element ref="sld:Name"/>
	 *    <xsd:element ref="sld:LayerFeatureConstraints" minOccurs="0"/>
	 *    <xsd:choice minOccurs="0" maxOccurs="unbounded">
	 *     <xsd:element ref="sld:NamedStyle"/>
	 *     <xsd:element ref="sld:UserStyle"/>
	 *    </xsd:choice>
	 *   </xsd:sequence>
	 *  </xsd:complexType>
	 * </xsd:element>
	 * </code></pre>
	 * </p>
	 * @param root
	 */
	private NamedLayer parseNamedLayer(Node root) {
		NamedLayer layer = new NamedLayerImpl();

		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
				continue;
			}
			String childName = child.getLocalName();
			if (childName == null) {
				childName = child.getNodeName();
			}

			if (childName.equalsIgnoreCase("Name")) {
				layer.setName(child.getFirstChild().getNodeValue());
			}
			
			if (childName.equalsIgnoreCase("NamedStyle")) {
				NamedStyle style = parseNamedStyle(child);				
				layer.addStyle(style);
			}

			if (childName.equalsIgnoreCase("UserStyle")) {
				Style user = parseStyle(child);
				layer.addStyle(user);
			}

			if (childName.equalsIgnoreCase("LayerFeatureConstraints")) {
				throw new UnsupportedOperationException("LayerFeatureConstraints pending of implementation");			}
			}

		return layer;
	}

	/**
	 * Parses a NamedStyle from node.
	 * <p>
	 * A NamedStyle is used to refer to a style that has a name in a WMS,
	 * and is defined as:
	 * <pre><code>
	 * <xsd:element name="NamedStyle">
	 *  <xsd:annotation>
	 *   <xsd:documentation> A NamedStyle is used to refer to a style that has a name in a WMS. </xsd:documentation>
	 *  </xsd:annotation>
	 *  <xsd:complexType>
	 *   <xsd:sequence>
	 *    <xsd:element ref="sld:Name"/>
	 *   </xsd:sequence>
	 *  </xsd:complexType>
	 * </xsd:element>
	 * </code></pre>
	 * </p>
	 * @param n
	 */
	public NamedStyle parseNamedStyle(Node n) {
		if (dom == null) {
			try {
				javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory
						.newInstance();
				javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
				dom = db.newDocument();
			} catch (javax.xml.parsers.ParserConfigurationException pce) {
				throw new RuntimeException(pce);
			}
		}

		NamedStyle style = factory.createNamedStyle();

		NodeList children = n.getChildNodes();

		if (LOGGER.isLoggable(Level.FINEST)) {
			LOGGER.finest("" + children.getLength() + " children to process");
		}

		for (int j = 0; j < children.getLength(); j++) {
			Node child = children.item(j);

			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)
					|| (child.getFirstChild() == null)) {
				continue;
			}
			if (LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest("processing " + child.getLocalName());
			}
			String childName = child.getLocalName();
			if (childName == null) {
				childName = child.getNodeName();
			}
			if (childName.equalsIgnoreCase("Name")) {
				style.setName(child.getFirstChild().getNodeValue());
			}
		}
		return style;
	}

	/**
	 * 
	 * @param root
	 * @deprecated this method is not being used
	 */
	private StyledLayerImpl parseLayer(Node root) {
		StyledLayerImpl layer = null;
		// LineSymbolizer symbol = factory.createLineSymbolizer();

		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
				continue;
			}
			String childName = child.getLocalName();
			if (childName == null) {
				childName = child.getNodeName();
			}
			if (childName.equalsIgnoreCase("NamedLayer")) {
				layer = new NamedLayerImpl();
			}

			if (childName.equalsIgnoreCase("UserLayer")) {

				layer = new UserLayerImpl();

				// symbol.setStroke(parseStroke(child));
			}
		}

		return layer;
	}

	/**
	 * build a style for the Node provided
	 * 
	 * @param n
	 *            the node which contains the style to be parsed.
	 * 
	 * @return the Style constructed.
	 * 
	 * @throws RuntimeException
	 *             if an error occurs setting up the parser
	 */
	public Style parseStyle(Node n) {
		if (dom == null) {
			try {
				javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory
						.newInstance();
				javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
				dom = db.newDocument();
			} catch (javax.xml.parsers.ParserConfigurationException pce) {
				throw new RuntimeException(pce);
			}
		}

		Style style = factory.createStyle();

		NodeList children = n.getChildNodes();

		if (LOGGER.isLoggable(Level.FINEST)) {
			LOGGER.finest("" + children.getLength() + " children to process");
		}

		for (int j = 0; j < children.getLength(); j++) {
			Node child = children.item(j);

			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)
					|| (child.getFirstChild() == null)) {
				continue;
			}
			// System.out.println("The child is: " + child.getNodeName() + " or
			// " + child.getLocalName() + " prefix is " +child.getPrefix());
			if (LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest("processing " + child.getLocalName());
			}
			String childName = child.getLocalName();
			if (childName == null) {
				childName = child.getNodeName();
			}
			if (childName.equalsIgnoreCase("Name")) {
				style.setName(child.getFirstChild().getNodeValue());
			}

			if (childName.equalsIgnoreCase("Title")) {
				style.setTitle(child.getFirstChild().getNodeValue());
			}

			if (childName.equalsIgnoreCase("Abstract")) {
				style.setAbstract(child.getFirstChild().getNodeValue());
			}
			
			if (childName.equalsIgnoreCase("IsDefault")) {
                style.setDefault(Boolean.valueOf(child.getFirstChild().getNodeValue()).booleanValue());
            }

			if (childName.equalsIgnoreCase("FeatureTypeStyle")) {
				style.addFeatureTypeStyle(parseFeatureTypeStyle(child));
			}
		}

		return style;
	}

	private FeatureTypeStyle parseFeatureTypeStyle(Node style) {
		if (LOGGER.isLoggable(Level.FINEST)) {
			LOGGER.finest("Parsing featuretype style " + style.getLocalName());
		}

		FeatureTypeStyle ft = factory.createFeatureTypeStyle();

		ArrayList rules = new ArrayList();
		ArrayList sti = new ArrayList();
		NodeList children = style.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
				continue;
			}

			if (LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest("processing " + child.getLocalName());
			}
			String childName = child.getLocalName();
			if (childName == null) {
				childName = child.getNodeName();
			}
			if (childName.equalsIgnoreCase("Name")) {
				ft.setName(child.getFirstChild().getNodeValue());
			}

			if (childName.equalsIgnoreCase("Title")) {
				ft.setTitle(child.getFirstChild().getNodeValue());
			}

			if (childName.equalsIgnoreCase("Abstract")) {
				ft.setAbstract(child.getFirstChild().getNodeValue());
			}

			if (childName.equalsIgnoreCase("FeatureTypeName")) {
				ft.setFeatureTypeName(child.getFirstChild().getNodeValue());
			}

			if (childName.equalsIgnoreCase("SemanticTypeIdentifier")) {
				sti.add(child.getFirstChild().getNodeValue());
			}

			if (childName.equalsIgnoreCase("Rule")) {
				rules.add(parseRule(child));
			}
		}
		
		if (sti.size() > 0) {
			ft.setSemanticTypeIdentifiers((String[]) sti.toArray(new String[0]));
		}
		ft.setRules((Rule[]) rules.toArray(new Rule[0]));

		return ft;
	}

	private Rule parseRule(Node ruleNode) {
		if (LOGGER.isLoggable(Level.FINEST)) {
			LOGGER.finest("Parsing rule " + ruleNode.getLocalName());
		}

		Rule rule = factory.createRule();
		ArrayList symbolizers = new ArrayList();
		NodeList children = ruleNode.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
				continue;
			}
			String childName = child.getLocalName();
			if (childName == null) {
				childName = child.getNodeName();
			}
			
			if (childName.indexOf(':') != -1)
	        {
	        	//the DOM parser wasnt properly set to handle namespaces...
	        	childName = childName.substring(childName.indexOf(':')+1);
	        }
			 
			if (LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest("processing " + child.getLocalName());
			}

			if (childName.equalsIgnoreCase("Name")) {
				rule.setName(child.getFirstChild().getNodeValue());
			}

			if (childName.equalsIgnoreCase("Title")) {
				rule.setTitle(child.getFirstChild().getNodeValue());
			}

			if (childName.equalsIgnoreCase("Abstract")) {
				rule.setAbstract(child.getFirstChild().getNodeValue());
			}

			if (childName.equalsIgnoreCase("MinScaleDenominator")) {
				rule.setMinScaleDenominator(Double.parseDouble(child
						.getFirstChild().getNodeValue()));
			}

			if (childName.equalsIgnoreCase("MaxScaleDenominator")) {
				rule.setMaxScaleDenominator(Double.parseDouble(child
						.getFirstChild().getNodeValue()));
			}

			if (childName.equalsIgnoreCase("Filter")) {
                Filter filter = parseFilter(child);
                rule.setFilter(filter);
			}

			if (childName.equalsIgnoreCase("ElseFilter")) {
				rule.setIsElseFilter(true);
			}

			if (childName.equalsIgnoreCase("LegendGraphic")) {
				findElements(((Element) child), graphicSt);
				NodeList g = findElements(((Element) child), graphicSt);
				ArrayList legends = new ArrayList();

				for (int k = 0; k < g.getLength(); k++) {
					legends.add(parseGraphic(g.item(k)));
				}

				rule.setLegendGraphic((Graphic[]) legends
						.toArray(new Graphic[0]));
			}

			if (childName.equalsIgnoreCase("LineSymbolizer")) {
				symbolizers.add(parseLineSymbolizer(child));
			}

			if (childName.equalsIgnoreCase("PolygonSymbolizer")) {
				symbolizers.add(parsePolygonSymbolizer(child));
			}

			if (childName.equalsIgnoreCase("PointSymbolizer")) {
				symbolizers.add(parsePointSymbolizer(child));
			}

			if (childName.equalsIgnoreCase("TextSymbolizer")) {
				symbolizers.add(parseTextSymbolizer(child));
			}

			if (childName.equalsIgnoreCase("RasterSymbolizer")) {
				symbolizers.add(parseRasterSymbolizer(child));
			}
		}

		rule.setSymbolizers((Symbolizer[]) symbolizers
				.toArray(new Symbolizer[0]));

		return rule;
	}

    private Filter parseFilter(Node child) {
        // this sounds stark raving mad, but this is actually how the dom parser works...
        // instead of passing in the parent element, pass in the first child and its
        // siblings will also be parsed
        Node firstChild = child.getFirstChild();
        while (firstChild != null && firstChild.getNodeType() != Node.ELEMENT_NODE) {
            //advance to the first actual element (rather than whitespace)
            firstChild = firstChild.getNextSibling();
        }
        Filter filter = org.geotools.filter.FilterDOMParser.parseFilter(firstChild);
        return filter;
    }

	/**
	 * parses the SLD for a linesymbolizer
	 * 
	 * @param root
	 *            a w2c Dom Node
	 * 
	 * @return the linesymbolizer
	 */
	private LineSymbolizer parseLineSymbolizer(Node root) {
		LineSymbolizer symbol = factory.createLineSymbolizer();
		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
				continue;
			}
			String childName = child.getLocalName();
			if (childName == null) {
				childName = child.getNodeName();
			}
			if (childName.equalsIgnoreCase(geomSt)) {
				symbol.setGeometryPropertyName(parseGeometryName(child));
			}

			if (childName.equalsIgnoreCase("Stroke")) {
				symbol.setStroke(parseStroke(child));
			}
		}

		return symbol;
	}

	/**
	 * parses the SLD for a polygonsymbolizer
	 * 
	 * @param root
	 *            w3c dom node
	 * 
	 * @return the polygon symbolizer
	 */
	private PolygonSymbolizer parsePolygonSymbolizer(Node root) {
		PolygonSymbolizer symbol = factory.createPolygonSymbolizer();
		symbol.setFill((Fill) null);
		symbol.setStroke((Stroke) null);

		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
				continue;
			}
			String childName = child.getLocalName();
			if (childName == null) {
				childName = child.getNodeName();
			}
			if (childName.equalsIgnoreCase(geomSt)) {
				symbol.setGeometryPropertyName(parseGeometryName(child));
			}

			if (childName.equalsIgnoreCase("Stroke")) {
				symbol.setStroke(parseStroke(child));
			}

			if (childName.equalsIgnoreCase(fillSt)) {
				symbol.setFill(parseFill(child));
			}
		}

		return symbol;
	}

	/**
	 * parses the SLD for a text symbolizer
	 * 
	 * @param root
	 *            w3c dom node
	 * 
	 * @return the TextSymbolizer
	 */
	private TextSymbolizer parseTextSymbolizer(Node root) {
	    TextSymbolizer symbol = factory.createTextSymbolizer();
		symbol.setFill(null);

		ArrayList fonts = new ArrayList();
		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
				continue;
			}
			String childName = child.getLocalName();
			if (childName == null) {
				childName = child.getNodeName();
			}
			if (childName.equalsIgnoreCase(geomSt)) {
				symbol.setGeometryPropertyName(parseGeometryName(child));
			}

			if (childName.equalsIgnoreCase(fillSt)) {
				symbol.setFill(parseFill(child));
			}

			if (childName.equalsIgnoreCase("Label")) {
				LOGGER.finest("parsing label " + child.getNodeValue());
				// the label parser should preserve whitespaces, so
				// we call parseCssParameter with trimWhiteSpace=false
				symbol.setLabel(parseCssParameter(child,false));
				if (symbol.getLabel() == null)
				{
					LOGGER.warning("parsing TextSymbolizer node - couldnt find anything in the Label element!");
				}
			}

			if (childName.equalsIgnoreCase("Font")) {
				fonts.add(parseFont(child));
			}

			if (childName.equalsIgnoreCase("LabelPlacement")) {
				symbol.setPlacement(parseLabelPlacement(child));
			}

			if (childName.equalsIgnoreCase("Halo")) {
				symbol.setHalo(parseHalo(child));
			}
			if (childName.equalsIgnoreCase("Graphic")) {
			    LOGGER.finest("Parsing non-standard Graphic in TextSymbolizer");
				if (symbol instanceof TextSymbolizer2)
				{
					((TextSymbolizer2)symbol).setGraphic(parseGraphic(child));
				}
			}
			
			if (childName.equalsIgnoreCase("Abstract")) {
			    LOGGER.finest("Parsing non-standard Abstract in TextSymbolizer");
			    if(symbol instanceof TextSymbolizer2)
			        ((TextSymbolizer2)symbol).setAbstract(parseCssParameter(child, false));
			}
			
			if (childName.equalsIgnoreCase("Description")) {
                LOGGER.finest("Parsing non-standard Description in TextSymbolizer");
                if(symbol instanceof TextSymbolizer2)
                    ((TextSymbolizer2)symbol).setDescription(parseCssParameter(child, false));
            }
			
			if (childName.equalsIgnoreCase("OtherText")) {
                LOGGER.finest("Parsing non-standard OtherText in TextSymbolizer");
                if(symbol instanceof TextSymbolizer2)
                    ((TextSymbolizer2)symbol).setOtherText(parseOtherText(child));
            }
			
			
			if (childName.equalsIgnoreCase("priority")) 
			{
				symbol.setPriority(parseCssParameter(child));
			}
			if (childName.equalsIgnoreCase("vendoroption")) 
			{
				parseVendorOption(symbol,child);
			}
			
		}

		symbol.setFonts((Font[]) fonts.toArray(new Font[0]));

		return symbol;
	}
	
	private OtherText parseOtherText(Node root) {
	    // TODO: add methods to the factory to create OtherText instances
	    OtherText ot = new OtherTextImpl();
	    final Node targetAttribute = root.getAttributes().getNamedItem("target");
	    if(targetAttribute == null)
	        throw new IllegalArgumentException("OtherLocation does not have the " +
	        		"required 'target' attribute");
        String target = targetAttribute.getNodeValue();
        Expression text = parseCssParameter(root, true);
        ot.setTarget(target);
        ot.setText(text);
        return ot;
	}
	

	/**
	 *   adds the key/value pair from the node ("<VendorOption name="...">...</VendorOption>").
	 *   This can be generalized for other symbolizers in the future
	 * @param symbol
	 * @param child
	 */
	private void parseVendorOption(TextSymbolizer symbol, Node child) 
	{
		String key =child.getAttributes().getNamedItem("name").getNodeValue();
		String value  =child.getFirstChild().getNodeValue();
		
		symbol.addToOptions(key,value);		
	}

	private Expression parseLiteral(Node root) {
		NodeList children = root.getChildNodes();
		for(int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if((child == null) || child.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			String childName = child.getLocalName();
			if(childName == null) {
				childName = child.getNodeName();
			}
			if(childName.equalsIgnoreCase("Literal")) {
				try {
					return (Expression) ExpressionBuilder.parse(child.getFirstChild().getNodeValue());
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}
		return null;
	}
	/**
	 * parses the SLD for a text symbolizer
	 * 
	 * @param root
	 *            w3c dom node
	 * 
	 * @return the TextSymbolizer
	 */
	private RasterSymbolizer parseRasterSymbolizer(Node root) {
		RasterSymbolizer symbol = factory.getDefaultRasterSymbolizer();
		// symbol.setGraphic(null);

		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
				continue;
			}
			String childName = child.getLocalName();
			if (childName == null) {
				childName = child.getNodeName();
			}

			if (childName.equalsIgnoreCase(geomSt)) {
				symbol.setGeometryPropertyName(parseGeometryName(child));
			}

			if (childName.equalsIgnoreCase("Opacity")) {
				try {
					symbol.setOpacity((Expression) ExpressionBuilder
							.parse(child.getFirstChild().getNodeValue()));
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

			if (childName.equalsIgnoreCase("ChannelSelection")) {
				symbol.setChannelSelection(parseChannelSelection(child));
			}

			if (childName.equalsIgnoreCase("OverlapBehavior")) {
				try {
					symbol.setOverlap((Expression) ExpressionBuilder
							.parse(child.getFirstChild().getNodeValue()));
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

			if (childName.equalsIgnoreCase("ColorMap")) {
				symbol.setColorMap(parseColorMap(child));
			}
			
			if (childName.equalsIgnoreCase("ContrastEnhancement")) {
				symbol.setContrastEnhancement(parseContrastEnhancement(child));
			}
			
			if (childName.equalsIgnoreCase("ShadedRelief")) {
				symbol.setShadedRelief(parseShadedRelief(child));
			}
			
			if (childName.equalsIgnoreCase("ImageOutline")) {
				symbol.setImageOutline(parseLineSymbolizer(child));
			}			
		}

		return symbol;
	}

	private ColorMapEntry parseColorMapEntry(Node root) {
		ColorMapEntry symbol = factory.createColorMapEntry();
		
		//Expression exp = null;
		
		NamedNodeMap atts = root.getAttributes();

		if( atts.getNamedItem("label") != null ) {
			symbol.setLabel(atts.getNamedItem("label").getNodeValue());
		}

		if( atts.getNamedItem("color") != null ) {
			symbol.setColor(ff.literal(atts.getNamedItem("color").getNodeValue()));
		}

		if( atts.getNamedItem("opacity") != null ) {
			symbol.setOpacity(ff.literal(atts.getNamedItem("opacity").getNodeValue()));
		}

		if( atts.getNamedItem("quantity") != null ) {
			symbol.setQuantity(ff.literal(atts.getNamedItem("quantity").getNodeValue()));
		}
		
		return symbol;
	}
	
	private ColorMap parseColorMap(Node root) {
		ColorMap symbol = factory.createColorMap();
		
		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
				continue;
			}
			String childName = child.getLocalName();
			if (childName == null) {
				childName = child.getNodeName();
			}

			if (childName.equalsIgnoreCase("ColorMapEntry")) {
				symbol.addColorMapEntry(parseColorMapEntry(child));
			}
		}
		
		return symbol;
	}
	
	private SelectedChannelType parseSelectedChannel(Node root) {
		SelectedChannelType symbol = new SelectedChannelTypeImpl();

		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
				continue;
			}
			String childName = child.getLocalName();
			if (childName == null) {
				childName = child.getNodeName();
			}

			if (childName.equalsIgnoreCase("SourceChannelName")) {
				symbol.setChannelName(child.getNodeValue());
			}

			if (childName.equalsIgnoreCase("ContrastEnhancement")) {
				try {
					symbol.setContrastEnhancement((Expression) ExpressionBuilder
							.parse(child.getNodeValue()));
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}

		return symbol;
	}
	
	private ChannelSelection parseChannelSelection(Node root) {
		List channels = new LinkedList();  
		
		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
				continue;
			}
			String childName = child.getLocalName();
			if (childName == null) {
				childName = child.getNodeName();
			}

			if (childName.equalsIgnoreCase("GrayChannel")) {
				channels.add(parseSelectedChannel(child));
			}

			if (childName.equalsIgnoreCase("RedChannel")) {
				channels.add(parseSelectedChannel(child));
			}

			if (childName.equalsIgnoreCase("GreenChannel")) {
				channels.add(parseSelectedChannel(child));
			}

			if (childName.equalsIgnoreCase("BlueChannel")) {
				channels.add(parseSelectedChannel(child));
			}
		}

		ChannelSelection dap = factory.createChannelSelection((SelectedChannelType[])channels.toArray(new SelectedChannelType[channels.size()]));
		
		return dap;
	}
	
	private ContrastEnhancement parseContrastEnhancement(Node root) {
		ContrastEnhancement symbol = new ContrastEnhancementImpl();
		
		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
				continue;
			}
			String childName = child.getLocalName();
			if (childName == null) {
				childName = child.getNodeName();
			}
			if (childName.equalsIgnoreCase("Normalize")) {
				symbol.setNormalize();
			}

			if (childName.equalsIgnoreCase("Histogram")) {
				symbol.setHistogram();
			}

			if (childName.equalsIgnoreCase("GammaValue")) {
				try {
					symbol.setGammaValue((Expression) ExpressionBuilder
							.parse(child.getNodeValue()));
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}
		
		return symbol;
	}
	
	private ShadedRelief parseShadedRelief(Node root) {
		ShadedRelief symbol = new ShadedReliefImpl();

		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
				continue;
			}
			String childName = child.getLocalName();
			if (childName == null) {
				childName = child.getNodeName();
			}
			if (childName.equalsIgnoreCase("BrightnessOnly")) {
				symbol.setBrightnessOnly(Boolean.getBoolean(child.getFirstChild().getNodeValue()));
			}

			if (childName.equalsIgnoreCase("ReliefFactor")) {
				try {
					symbol.setReliefFactor((Expression) ExpressionBuilder
							.parse(child.getNodeValue()));
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}

		return symbol;
	}
	
	/**
	 * parses the SLD for a point symbolizer
	 * 
	 * @param root
	 *            a w3c dom node
	 * 
	 * @return the pointsymbolizer
	 */
	private PointSymbolizer parsePointSymbolizer(Node root) {
		PointSymbolizer symbol = factory.getDefaultPointSymbolizer();
		// symbol.setGraphic(null);

		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
				continue;
			}
			String childName = child.getLocalName();
			if (childName == null) {
				childName = child.getNodeName();
			}
			if (childName.equalsIgnoreCase(geomSt)) {
				symbol.setGeometryPropertyName(parseGeometryName(child));
			}

			if (childName.equalsIgnoreCase(graphicSt)) {
				symbol.setGraphic(parseGraphic(child));
			}
		}

		return symbol;
	}

	private Graphic parseGraphic(Node root) {
		if (LOGGER.isLoggable(Level.FINEST)) {
			LOGGER.finest("processing graphic " + root);
		}

		Graphic graphic = factory.getDefaultGraphic();

		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
				continue;
			}
			String childName = child.getLocalName();
			if (childName == null) {
				childName = child.getNodeName();
			}
			if (childName.equalsIgnoreCase(geomSt)) {
				graphic.setGeometryPropertyName(parseGeometryName(child));
			}

			if (childName.equalsIgnoreCase("ExternalGraphic")) {
				LOGGER.finest("parsing extgraphic " + child);
				graphic.addExternalGraphic(parseExternalGraphic(child));
			}

			if (childName.equalsIgnoreCase("Mark")) {
				graphic.addMark(parseMark(child));
			}

			if (childName.equalsIgnoreCase("opacity")) {
				graphic.setOpacity(parseCssParameter(child));
			}

			if (childName.equalsIgnoreCase("size")) {
				graphic.setSize(parseCssParameter(child));
			}

			if (childName.equalsIgnoreCase("displacement")) {
				graphic.setDisplacement(parseDisplacement(child));
			}

			if (childName.equalsIgnoreCase("rotation")) {
				graphic.setRotation(parseCssParameter(child));
			}
		}

		return graphic;
	}

	private String parseGeometryName(Node root) {
		if (LOGGER.isLoggable(Level.FINEST)) {
			LOGGER.finest("parsing GeometryName");
		}

		String ret = null;
		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
				continue;
			}

			ret = parseCssParameter(child).toString();
		}

		return ret;
	}

	private Mark parseMark(Node root) {
		if (LOGGER.isLoggable(Level.FINEST)) {
			LOGGER.finest("parsing mark");
		}

		Mark mark = factory.createMark();
		mark.setFill(null);
		mark.setStroke(null);

		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
				continue;
			}
			String childName = child.getLocalName();
			if (childName == null) {
				childName = child.getNodeName();
			}
			if (childName.equalsIgnoreCase("Stroke")) {
				mark.setStroke(parseStroke(child));
			}

			if (childName.equalsIgnoreCase(fillSt)) {
				mark.setFill(parseFill(child));
			}

			if (childName.equalsIgnoreCase("WellKnownName")) {
				LOGGER.finest("setting mark to "
						+ child.getFirstChild().getNodeValue());
				mark.setWellKnownName(parseCssParameter(child));
			}
		}

		return mark;
	}

	private ExternalGraphic parseExternalGraphic(Node root) {
		if (LOGGER.isLoggable(Level.FINEST)) {
			LOGGER.finest("processing external graphic ");
		}

		String format = "";
		String uri = "";
		Map paramList = new HashMap();

		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
				continue;
			}
			String childName = child.getLocalName();
			if (childName == null) {
				childName = child.getNodeName();
			}
			if (childName.equalsIgnoreCase("OnLineResource")) {
				uri = parseOnlineResource(child);
			}

			if (childName.equalsIgnoreCase("format")) {
				LOGGER.finest("format child is " + child);
				LOGGER.finest("seting ExtGraph format "
						+ child.getFirstChild().getNodeValue());
				format = (child.getFirstChild().getNodeValue());
			}
			if (childName.equalsIgnoreCase("customProperty")) {
				LOGGER.finest("custom child is " + child);
				String propName = child.getAttributes().getNamedItem("name")
						.getNodeValue();
				LOGGER.finest("seting custom property " + propName + " to "
						+ child.getFirstChild().getNodeValue());
				Expression value = parseCssParameter(child);
				paramList.put(propName, value);

			}
		}

		URL url = null;
		try {
			url = new URL(uri);
		} catch (MalformedURLException mfe) {
			LOGGER.fine("Looks like " + uri + " is a relative path..");
			if (sourceUrl != null) {
				try {
					url = new URL(sourceUrl, uri);
				} catch (MalformedURLException e) {
					LOGGER.warning("can't parse " + uri + " as relative to"
							+ sourceUrl.toExternalForm());
				}
			}
		}

		ExternalGraphic extgraph;
		if (url == null) {
			extgraph = factory.createExternalGraphic(uri, format);
		} else {
			extgraph = factory.createExternalGraphic(url, format);
		}
		extgraph.setCustomProperties(paramList);
		return extgraph;
	}

    private String parseOnlineResource(Node root) {
        Element param = (Element) root;
        org.w3c.dom.NamedNodeMap map = param.getAttributes();

        LOGGER.finest("attributes " + map.toString());

        for (int k = 0; k < map.getLength(); k++) {
        	String res = map.item(k).getNodeValue();
        	String name = map.item(k).getNodeName();
        	// if(name == null){
        	// name = map.item(k).getNodeName();
        	// }
        	if (LOGGER.isLoggable(Level.FINEST)) {
        		LOGGER.finest("processing attribute " + name + "="
        				+ res);
        	}

        	// TODO: process the name space properly
        	if (name.equalsIgnoreCase("xlink:href")) {
        		LOGGER.finest("seting ExtGraph uri " + res);
        		return res;
        	}
        }
        return null;
    }

	private Stroke parseStroke(Node root) {
		Stroke stroke = factory.getDefaultStroke();
		NodeList list = findElements(((Element) root), "GraphicFill");

		if (list.getLength() > 0) {
			LOGGER.finest("stroke: found a graphic fill " + list.item(0));

			NodeList kids = list.item(0).getChildNodes();

			for (int i = 0; i < kids.getLength(); i++) {
				Node child = kids.item(i);

				if ((child == null)
						|| (child.getNodeType() != Node.ELEMENT_NODE)) {
					continue;
				}
				String childName = child.getLocalName();
				if (childName == null) {
					childName = child.getNodeName();
				}
				if (childName.equalsIgnoreCase(graphicSt)) {
					Graphic g = parseGraphic(child);
					LOGGER.finest("setting stroke graphicfill with " + g);
					stroke.setGraphicFill(g);
				}
			}
		}

		list = findElements(((Element) root), "GraphicStroke");

		if (list.getLength() > 0) {
			LOGGER.finest("stroke: found a graphic stroke " + list.item(0));

			NodeList kids = list.item(0).getChildNodes();

			for (int i = 0; i < kids.getLength(); i++) {
				Node child = kids.item(i);

				if ((child == null)
						|| (child.getNodeType() != Node.ELEMENT_NODE)) {
					continue;
				}
				String childName = child.getLocalName();
				if (childName == null) {
					childName = child.getNodeName();
				}
				if (childName.equalsIgnoreCase(graphicSt)) {
					Graphic g = parseGraphic(child);
					LOGGER.finest("setting stroke graphicStroke with " + g);
					stroke.setGraphicStroke(g);
				}
			}
		}

		list = findElements(((Element) root), "CssParameter");

		for (int i = 0; i < list.getLength(); i++) {
			Node child = list.item(i);

			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
				continue;
			}

			if (LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest("now I am processing " + child);
			}

			Element param = (Element) child;
			org.w3c.dom.NamedNodeMap map = param.getAttributes();

			if (LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest("attributes " + map.toString());
			}

			for (int k = 0; k < map.getLength(); k++) {
				String res = map.item(k).getNodeValue();

				if (LOGGER.isLoggable(Level.FINEST)) {
					LOGGER.finest("processing attribute " + res);
				}

				if (res.equalsIgnoreCase("stroke")) {
					stroke.setColor(parseCssParameter(child));
				}

				if (res.equalsIgnoreCase("width")
						|| res.equalsIgnoreCase("stroke-width")) {
					stroke.setWidth(parseCssParameter(child));
				}

				if (res.equalsIgnoreCase("opacity")
						|| res.equalsIgnoreCase("stroke-opacity")) {
					stroke.setOpacity(parseCssParameter(child));
				}

				if (res.equalsIgnoreCase("linecap")
						|| res.equalsIgnoreCase("stroke-linecap")) {
					// since these are system-dependent just pass them through
					// and hope.
					stroke.setLineCap(parseCssParameter(child));
				}

				if (res.equalsIgnoreCase("linejoin")
						|| res.equalsIgnoreCase("stroke-linejoin")) {
					// since these are system-dependent just pass them through
					// and hope.
					stroke.setLineJoin(parseCssParameter(child));
				}

				if (res.equalsIgnoreCase("dasharray")
						|| res.equalsIgnoreCase("stroke-dasharray")) {
					String dashString = child.getFirstChild().getNodeValue();
					StringTokenizer stok = new StringTokenizer(dashString, " ");
					float[] dashes = new float[stok.countTokens()];

					for (int l = 0; l < dashes.length; l++) {
						dashes[l] = Float.parseFloat(stok.nextToken());
					}

					stroke.setDashArray(dashes);
				}

				if (res.equalsIgnoreCase("dashoffset")
						|| res.equalsIgnoreCase("stroke-dashoffset")) {
					stroke.setDashOffset(parseCssParameter(child));
				}
			}
		}

		return stroke;
	}

	private Fill parseFill(Node root) {
		if (LOGGER.isLoggable(Level.FINEST)) {
			LOGGER.finest("parsing fill ");
		}

		Fill fill = factory.getDefaultFill();
		NodeList list = findElements(((Element) root), "GraphicFill");

		if (list.getLength() > 0) {
			LOGGER.finest("fill found a graphic fill " + list.item(0));

			NodeList kids = list.item(0).getChildNodes();

			for (int i = 0; i < kids.getLength(); i++) {
				Node child = kids.item(i);

				if ((child == null)
						|| (child.getNodeType() != Node.ELEMENT_NODE)) {
					continue;
				}
				String childName = child.getLocalName();
				if (childName == null) {
					childName = child.getNodeName();
				}
				if (childName.equalsIgnoreCase(graphicSt)) {
					Graphic g = parseGraphic(child);
					LOGGER.finest("setting fill graphic with " + g);
					fill.setGraphicFill(g);
				}
			}
		}

		list = findElements(((Element) root), "CssParameter");

		for (int i = 0; i < list.getLength(); i++) {
			Node child = list.item(i);

			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
				continue;
			}

			Element param = (Element) child;
			org.w3c.dom.NamedNodeMap map = param.getAttributes();

			if (LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest("now I am processing " + child);
			}

			if (LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest("attributes " + map.toString());
			}

			for (int k = 0; k < map.getLength(); k++) {
				String res = map.item(k).getNodeValue();

				if (LOGGER.isLoggable(Level.FINEST)) {
					LOGGER.finest("processing attribute " + res);
				}

				if (res.equalsIgnoreCase(fillSt)) {
					fill.setColor(parseCssParameter(child));
				}

				if (res.equalsIgnoreCase("opacity")
						|| res.equalsIgnoreCase("fill-opacity")) {
					fill.setOpacity(parseCssParameter(child));
				}
			}
		}

		if (LOGGER.isLoggable(Level.FINEST)) {
			LOGGER.finest("fill graphic " + fill.getGraphicFill());
		}

		return fill;
	}

	/**
	 * Concatenates the given expressions (through the strConcat FunctionFilter expression) 
	 * @param left 
	 * @param right
	 * @return
	 */
	private Expression manageMixed(Expression left,Expression right) {
		if(left==null)
			return right;
		if(right==null)
			return left;
		Function mixed=ff.function("strConcat",new Expression[] {left,right});
		return mixed;
	}
	/**
	 * Parses a css parameter. Default implementation trims whitespaces from text nodes.
	 * @param root node to parse
	 * @return
	 */
	private Expression parseCssParameter(Node root) {
		return parseCssParameter(root,true);
	}
	/**
	 * Parses a css parameter. You can choose if the parser must trim whitespace 
	 * from text nodes or not.
	 * @param root node to parse
	 * @param trimWhiteSpace true to trim whitespace from text nodes
	 * @return
	 */
	private Expression parseCssParameter(Node root,boolean trimWhiteSpace) {
		Expression ret = null;

		if (LOGGER.isLoggable(Level.FINEST)) {
			LOGGER.finest("parsingCssParam " + root);
		}

		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			// Added mixed="true" management through concatenation of text and expression nodes
			if ((child == null)) {
				continue;
			} else if(child.getNodeType()==Node.TEXT_NODE) {
				String value = child.getNodeValue();
				// trim whitespace if asked to do so
				value = (value != null && trimWhiteSpace) ? value.trim() : value;
				if(value!=null && value.length()!=0) {
					Element literal = dom.createElement("literal");
					Node text = dom .createTextNode(value);
	
					literal.appendChild(text);

			if (LOGGER.isLoggable(Level.FINEST)) {
						LOGGER.finest("Built new literal " + literal);
			}
					// add the text node as a literal
					ret = manageMixed(ret,org.geotools.filter.ExpressionDOMParser
							.parseExpression(literal));
		}
			} else if(child.getNodeType()==Node.ELEMENT_NODE) {

		if (LOGGER.isLoggable(Level.FINEST)) {
					LOGGER.finest("about to parse " + child.getLocalName());
		}
				// add the element node as an expression
				ret = manageMixed(ret,org.geotools.filter.ExpressionDOMParser
						.parseExpression(child));
			} else
				continue;

			}

		if (ret==null && LOGGER.isLoggable(Level.FINEST)) {
			LOGGER.finest("no children in CssParam");
		}

		return ret;
	}

	private Font parseFont(Node root) {
		if (LOGGER.isLoggable(Level.FINEST)) {
			LOGGER.finest("parsing font");
		}

		Font font = factory.getDefaultFont();
		NodeList list = findElements(((Element) root), "CssParameter");

		for (int i = 0; i < list.getLength(); i++) {
			Node child = list.item(i);

			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
				continue;
			}

			Element param = (Element) child;
			org.w3c.dom.NamedNodeMap map = param.getAttributes();

			for (int k = 0; k < map.getLength(); k++) {
				String res = map.item(k).getNodeValue();

				if (res.equalsIgnoreCase("font-family")) {
					font.setFontFamily(parseCssParameter(child));
				}

				if (res.equalsIgnoreCase("font-style")) {
					font.setFontStyle(parseCssParameter(child));
				}

				if (res.equalsIgnoreCase("font-size")) {
					font.setFontSize(parseCssParameter(child));
				}

				if (res.equalsIgnoreCase("font-weight")) {
					font.setFontWeight(parseCssParameter(child));
				}
			}
		}

		return font;
	}

	private LabelPlacement parseLabelPlacement(Node root) {
		if (LOGGER.isLoggable(Level.FINEST)) {
			LOGGER.finest("parsing labelPlacement");
		}

		LabelPlacement ret = null;
		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
				continue;
			}
			String childName = child.getLocalName();
			if (childName == null) {
				childName = child.getNodeName();
			}
			if (childName.equalsIgnoreCase("PointPlacement")) {
				ret = parsePointPlacement(child);
			}

			if (childName.equalsIgnoreCase("LinePlacement")) {
				ret = parseLinePlacement(child);
			}
		}

		return ret;
	}

	private PointPlacement parsePointPlacement(Node root) {
		if (LOGGER.isLoggable(Level.FINEST)) {
			LOGGER.finest("parsing pointPlacement");
		}

		Expression rotation = ff.literal(0.0);
		AnchorPoint ap = null;
		Displacement dp = null;

		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
				continue;
			}
			String childName = child.getLocalName();
			if (childName == null) {
				childName = child.getNodeName();
			}
			if (childName.equalsIgnoreCase("AnchorPoint")) {
				ap = (parseAnchorPoint(child));
			}

			if (childName.equalsIgnoreCase("Displacement")) {
				dp = (parseDisplacement(child));
			}

			if (childName.equalsIgnoreCase("Rotation")) {
				rotation = (parseCssParameter(child));
			}
		}

		LOGGER.fine("setting anchorPoint " + ap);
		LOGGER.fine("setting displacement " + dp);

		PointPlacement dpp = factory.createPointPlacement(ap, dp, rotation);

		return dpp;
	}

	private LinePlacement parseLinePlacement(Node root) {
		if (LOGGER.isLoggable(Level.FINEST)) {
			LOGGER.finest("parsing linePlacement");
		}

		Expression offset = ff.literal(0.0);
		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
				continue;
			}
			String childName = child.getLocalName();
			if (childName == null) {
				childName = child.getNodeName();
			}
			if (childName.equalsIgnoreCase("PerpendicularOffset")) {
				offset = parseCssParameter(child);
			}
		}

		LinePlacement dlp = factory.createLinePlacement(offset);

		return dlp;
	}

	private AnchorPoint parseAnchorPoint(Node root) {
		if (LOGGER.isLoggable(Level.FINEST)) {
			LOGGER.finest("parsing anchorPoint");
		}

		Expression x = null;
		Expression y = null;

		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
				continue;
			}
			String childName = child.getLocalName();
			if (childName == null) {
				childName = child.getNodeName();
			}
			if (childName.equalsIgnoreCase("AnchorPointX")) {
				x = (parseCssParameter(child));
			}

			if (childName.equalsIgnoreCase("AnchorPointY")) {
				y = (parseCssParameter(child));
			}
		}

		AnchorPoint dap = factory.createAnchorPoint(x, y);

		return dap;
	}

	private Displacement parseDisplacement(Node root) {
		if (LOGGER.isLoggable(Level.FINEST)) {
			LOGGER.finest("parsing displacment");
		}

		Expression x = null;
		Expression y = null;
		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
				continue;
			}
			String childName = child.getLocalName();
			if (childName == null) {
				childName = child.getNodeName();
			}
			if (childName.equalsIgnoreCase("DisplacementX")) {
				x = (parseCssParameter(child));
			}

			if (childName.equalsIgnoreCase("DisplacementY")) {
				y = (parseCssParameter(child));
			}
		}

		Displacement dd = factory.createDisplacement(x, y);

		return dd;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param root
	 * 
	 */
	private Halo parseHalo(Node root) {
		if (LOGGER.isLoggable(Level.FINEST)) {
			LOGGER.finest("parsing halo");
		}
		Halo halo = factory.createHalo(factory.createFill(ff
				.literal("#FFFFFF")), ff
				.literal(1.0));

		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
				continue;
			}
			String childName = child.getLocalName();
			if (childName == null) {
				childName = child.getNodeName();
			}
			if (childName.equalsIgnoreCase(fillSt)) {
				halo.setFill(parseFill(child));
			}

			if (childName.equalsIgnoreCase("Radius")) {
				halo.setRadius(parseCssParameter(child));
			}
		}

		return halo;
	}
	
}

