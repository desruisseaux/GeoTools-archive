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
import java.util.Vector;
import java.util.logging.Level;

import org.geotools.filter.Expression;
import org.geotools.filter.ExpressionBuilder;
import org.geotools.filter.LiteralExpression;
import org.geotools.filter.LiteralExpressionImpl;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class SLDParser {

	private static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger("org.geotools.styling");

	private static final org.geotools.filter.FilterFactory FILTERFACTORY = org.geotools.filter.FilterFactory
			.createFilterFactory();

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
		this.factory = factory;
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
	 * @param s
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
	 * @return
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

		try {
			javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
			dom = db.parse(source);
			// for our next trick do something with the dom.

			NodeList nodes = findElements(dom, "StyledLayerDescriptor");

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

	private StyledLayerDescriptor parseDescriptor(Node root) {
		StyledLayerDescriptor sld = new StyledLayerDescriptor();
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
		UserLayer layer = new UserLayer();
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
			if (childName.equalsIgnoreCase("UserStyle")) {
				Style user = parseStyle(child);
				layer.addUserStyle(user);
			}

			if (childName.equalsIgnoreCase("Name")) {
				String layerName = child.getFirstChild().getNodeValue();
				layer.setName(layerName);
				LOGGER.info("layer name: " + layer.getName());
			}

			if (childName.equalsIgnoreCase("LayerFeatureConstraints")) {
				layer = new UserLayer();
			}

		}

		return layer;
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
	 * @return
	 */
	private NamedLayer parseNamedLayer(Node root) {
		NamedLayer layer = new NamedLayer();

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
	 * @return
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
	 * @return
	 * @deprecated this method is not being used
	 */
	private StyledLayer parseLayer(Node root) {
		StyledLayer layer = null;
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
				layer = new NamedLayer();
			}

			if (childName.equalsIgnoreCase("UserLayer")) {

				layer = new UserLayer();

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

			if (childName.equalsIgnoreCase("Rule")) {
				rules.add(parseRule(child));
			}
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
				NodeList list = child.getChildNodes();
				Node kid = null;

				for (int k = 0; k < list.getLength(); k++) {
					kid = list.item(k);

					if ((kid == null)
							|| (kid.getNodeType() != Node.ELEMENT_NODE)) {
						continue;
					}

					org.geotools.filter.Filter filter = org.geotools.filter.FilterDOMParser
							.parseFilter(kid);

					if (LOGGER.isLoggable(Level.FINEST)) {
						LOGGER
								.finest("filter: "
										+ filter.getClass().toString());
						LOGGER.finest("parsed: " + filter.toString());
					}

					rule.setFilter(filter);
				}
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
				symbol.setLabel(parseCssParameter(child));
			}

			if (childName.equalsIgnoreCase("Font")) {
				fonts.add(parseFont(child));
			}

			if (childName.equalsIgnoreCase("LabelPlacement")) {
				symbol.setLabelPlacement(parseLabelPlacement(child));
			}

			if (childName.equalsIgnoreCase("Halo")) {
				symbol.setHalo(parseHalo(child));
			}
		}

		symbol.setFonts((Font[]) fonts.toArray(new Font[0]));

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
							.parse(child.getNodeValue()));
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
		
		Expression exp = null;
		
		NamedNodeMap atts = root.getAttributes();

		if( atts.getNamedItem("label") != null ) {
			symbol.setLabel(atts.getNamedItem("label").getNodeValue());
		}

		if( atts.getNamedItem("color") != null ) {
			symbol.setColor(FILTERFACTORY.createLiteralExpression(atts.getNamedItem("color").getNodeValue()));
		}

		if( atts.getNamedItem("opacity") != null ) {
			symbol.setOpacity(FILTERFACTORY.createLiteralExpression(atts.getNamedItem("opacity").getNodeValue()));
		}

		if( atts.getNamedItem("quantity") != null ) {
			symbol.setQuantity(FILTERFACTORY.createLiteralExpression(atts.getNamedItem("quantity").getNodeValue()));
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
				Element param = (Element) child;
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
						uri = res;
					}
				}
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

	private Expression parseCssParameter(Node root) {
		Expression ret = null;

		if (LOGGER.isLoggable(Level.FINEST)) {
			LOGGER.finest("parsingCssParam " + root);
		}

		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
				continue;
			}

			if (LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest("about to parse " + child.getLocalName());
			}

			ret = org.geotools.filter.ExpressionDOMParser
					.parseExpression(child);

			break;
		}

		if (LOGGER.isLoggable(Level.FINEST)) {
			LOGGER.finest("no children in CssParam");
		}

		if (ret == null) {
			Element literal = dom.createElement("literal");
			Node child = dom
					.createTextNode(root.getFirstChild().getNodeValue());

			literal.appendChild(child);

			if (LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest("Built new literal " + literal);
			}

			ret = org.geotools.filter.ExpressionDOMParser
					.parseExpression(literal);
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

		Expression rotation = FILTERFACTORY.createLiteralExpression(0.0);
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

		Expression offset = FILTERFACTORY.createLiteralExpression(0.0);
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
	 * @return
	 */
	private Halo parseHalo(Node root) {
		if (LOGGER.isLoggable(Level.FINEST)) {
			LOGGER.finest("parsing halo");
		}
		Halo halo = factory.createHalo(factory.createFill(FILTERFACTORY
				.createLiteralExpression("#FFFFFF")), FILTERFACTORY
				.createLiteralExpression(1.0));

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

