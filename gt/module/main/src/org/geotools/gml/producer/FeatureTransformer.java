/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.gml.producer;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureResults;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollectionIteration;
import org.geotools.feature.FeatureType;
import org.geotools.xml.transform.TransformerBase;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.NamespaceSupport;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;


/**
 * FeatureTransformer provides a mechanism for converting Feature objects into
 * (hopefully) valid gml. This is a work in progress, so please be patient. A
 * simple example of how to use this class follows:
 * <pre>
 *    FeatureCollection collection; // can also use FeatureReader!!
 *   OutputStream out;
 *    FeatureTransformer ft = new FeatureTransformer();
 *    // set the indentation to 4 spaces
 *   ft.setIndentation(4);
 *    // this will allow Features with the FeatureType which has the namespace
 *   // "http://somewhere.org" to be prefixed with xxx...
 *   ft.getFeatureNamespaces().declarePrefix("xxx","http://somewhere.org");
 *    // transform
 *   ft.transform(collection,out);
 * </pre>
 * <b>The above example assumes a homogenous collection of Features whose
 * FeatureType has the namespace "http://somewhere.org"</b> but note that not
 * all DataSources currently provide FeatureTypes with a namespace... There
 * are two other mechanisms for prefixing your Features.<br>
 * 1) Map a specific FeatureType <b>by identity</b> to prefix and nsURI
 * <pre>
 *   FeatureType fc;
 *   FeatureTransformer ft = new FeatureTransformer();
 *   ft.getFeatureTypeNamespaces().declareNamespace(fc,"xxx","http://somewhere.org");
 * </pre>
 * 2) Provide a default namespace for any Features whose FeatureType either has
 * an empty namespace, OR, has not been mapped using the previous method. This
 * is basically a catch-all mechanism.
 * <pre>
 *   FeatureTransformer ft = new FeatureTransformer();
 *   ft.getFeatureTypeNamespaces().declareDefaultNamespace("xxx","http://somewhere.org");
 * </pre>
 * <br/> The collectionNamespace and prefix property refers to the prefix and
 * namespace given to the document root and defualts to
 * wfs,http://www.opengis.wfs.
 *
 * @author Ian Schneider
 * @author Chris Holmes, TOPP
 * @source $URL$
 * @version $Id$
 *
 * @todo Add support for schemaLocation
 */
public class FeatureTransformer extends TransformerBase {
    /** The logger for the filter module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.gml");
    private static Set gmlAtts;
    private String collectionPrefix = "wfs";
    private String collectionNamespace = "http://www.opengis.net/wfs";
    private NamespaceSupport nsLookup = new NamespaceSupport();
    private FeatureTypeNamespaces featureTypeNamespaces = new FeatureTypeNamespaces(nsLookup);
    private SchemaLocationSupport schemaLocation = new SchemaLocationSupport();
    private int maxFeatures = -1;
    private boolean prefixGml = false;
    private boolean featureBounding = false;
    private String srsName;
    private String lockId;
    private int numDecimals = 4;

    public void setCollectionNamespace(String nsURI) {
        collectionNamespace = nsURI;
    }

    public String getCollectionNamespace() {
        return collectionNamespace;
    }

    public void setCollectionPrefix(String prefix) {
        this.collectionPrefix = prefix;
    }

    public String getCollectionPrefix() {
        return collectionPrefix;
    }

    /**
     * Sets the number of decimals to be used in the geometry coordinates of
     * the response.  This allows for more efficient results, since often the
     * storage format itself won't specify as many decimal places as the
     * response might want.  The default is 4, but should generally be set by
     * the user of this class.
     *
     * @param numDecimals the number of significant digits past the decimal to
     *        include in the response.
     */
    public void setNumDecimals(int numDecimals) {
        this.numDecimals = numDecimals;
    }

    public NamespaceSupport getFeatureNamespaces() {
        return nsLookup;
    }

    public FeatureTypeNamespaces getFeatureTypeNamespaces() {
        return featureTypeNamespaces;
    }

    public void addSchemaLocation(String nsURI, String uri) {
        schemaLocation.setLocation(nsURI, uri);
    }

    /**
     * Used to set the srsName attribute of the Geometries to be turned to xml.
     * For now we can only have all with the same srsName.
     *
     * @param srsName DOCUMENT ME!
     *
     * @task REVISIT: once we have better srs support in our feature model this
     *       should be rethought, as it's a rather blunt approach.
     */
    public void setSrsName(String srsName) {
        this.srsName = srsName;
    }

    /**
     * Used to set a lockId attribute after a getFeatureWithLock.
     *
     * @param lockId The lockId of the lock on the WFS.
     *
     * @task REVISIT: Ian, this is probably the most wfs specific addition. If
     *       you'd like I can subclass and add it there.  It has to be added
     *       as an attribute to FeatureCollection, to report a
     *       GetFeatureWithLock
     */
    public void setLockId(String lockId) {
        this.lockId = lockId;
    }

    /**
     * If Gml Prefixing is enabled then attributes with names that could be
     * prefixed with gml, such as description, pointProperty, and name, will
     * be.  So if an attribute called name is encountered, instead of
     * prepending the default prefix (say gt2:name), it will turn out as
     * gml:name.  Right now this is fairly hacky, as the gml:name,
     * gml:description, ect., should be in the first attributes by default.
     * The actualy geometry encodings will always be prefixed with the proper
     * gml, like gml:coordinates. This only applies to attributes, that could
     * also be part of the features normal schema (for example a pointProperty
     * could be declared in the gt2  namespace, instead of a gml:pointProperty
     * it would be a gt2:pointProperty.
     *
     * @param prefixGml <tt>true</tt> if prefixing gml should be enabled.
     *        Default is disabled, no gml prefixing.
     *
     * @task REVISIT: only prefix name, description, and boundedBy if they
     *       occur in their proper places.  Right now names always get gml
     *       prefixed if the gmlPrefixing is on, which is less than ideal.
     * @task REVISIT: The other approach is to allow for generic mapping, users
     *       would set which attributes they wanted to have different
     *       prefixes.
     */
    public void setGmlPrefixing(boolean prefixGml) {
        this.prefixGml = prefixGml;

        if (prefixGml && (gmlAtts == null)) {
            gmlAtts = new HashSet();
            gmlAtts.add("pointProperty");
            gmlAtts.add("geometryProperty");
            gmlAtts.add("polygonProperty");
            gmlAtts.add("lineStringProperty");
            gmlAtts.add("multiPointProperty");
            gmlAtts.add("multiLineStringProperty");
            gmlAtts.add("multiPolygonProperty");
            gmlAtts.add("description");
            gmlAtts.add("name");

            //boundedBy is done in handleAttribute to make use of the writeBounds
            //code.
        }
    }

    /**
     * Sets whether a gml:boundedBy element should automatically be generated
     * and included.  The element will not be updateable, and is simply
     * derived from the geometries present in the feature.
     * 
     * <p>
     * Note that the <tt>setGmlPrefixing()</tt> interacts with this
     * occasionally, since it will hack in a gml prefix to a boundedBy
     * attribute included in the featureType.  If gml prefixing is on, and
     * featureBounding is on, then the bounds from the attribute will be used.
     * If gml prefixing is off, then that boundedBy attribute will
     * presumably be in its own namespace, and so the automatic gml boundedBy
     * will not conflict, so both will be printed, with the automatic one
     * deriving its bounds from the boundedBy attribute and any other
     * geometries in the feature
     * </p>
     *
     * @param featureBounding <tt>true</tt> if the bounds of the feature should
     *        be automatically calculated and included as a gml:boundedBy in
     *        the gml output.  Note this puts a good bit of bandwidth overhead
     *        on the  output.  Default is <tt>false</tt>
     */
    public void setFeatureBounding(boolean featureBounding) {
        this.featureBounding = featureBounding;
    }

    public org.geotools.xml.transform.Translator createTranslator(
        ContentHandler handler) {
        FeatureTranslator t = createTranslator(handler, collectionPrefix,
                collectionNamespace, featureTypeNamespaces, schemaLocation);
        java.util.Enumeration prefixes = nsLookup.getPrefixes();

        //setGmlPrefixing(true);
        t.setNumDecimals(numDecimals);
        t.setGmlPrefixing(prefixGml);
        t.setSrsName(srsName);
        t.setLockId(lockId);
        t.setFeatureBounding(featureBounding);

        while (prefixes.hasMoreElements()) {
            String prefix = prefixes.nextElement().toString();
            String uri = nsLookup.getURI(prefix);
            t.getNamespaceSupport().declarePrefix(prefix, uri);
        }

        return t;
    }

    /**
     * Template method for creating the translator.
     * @return
     */
    protected FeatureTranslator createTranslator( 
		ContentHandler handler, String prefix, String ns, 
		FeatureTypeNamespaces featureTypeNamespaces, SchemaLocationSupport schemaLocationSupport
	) {
    	return new FeatureTranslator( handler, prefix, ns, featureTypeNamespaces, schemaLocationSupport );
    }
    
    public static class FeatureTypeNamespaces {
        Map lookup = new HashMap();
        NamespaceSupport nsSupport;
        String defaultPrefix = null;

        public FeatureTypeNamespaces(NamespaceSupport nsSupport) {
            this.nsSupport = nsSupport;
        }

        public void declareDefaultNamespace(String prefix, String nsURI) {
            defaultPrefix = prefix;
            nsSupport.declarePrefix(prefix, nsURI);
        }

        public void declareNamespace(FeatureType type, String prefix,
            String nsURI) {
            lookup.put(type, prefix);
            nsSupport.declarePrefix(prefix, nsURI);
        }

        public String findPrefix(FeatureType type) {
            String pre = (String) lookup.get(type);

            if (pre == null) {
                pre = defaultPrefix;
            }

            return pre;
        }

        public String toString() {
            return "FeatureTypeNamespaces[Default: " + defaultPrefix
            + ", lookUp: " + lookup;
        }
    }

    /**
     * Outputs gml without any fancy indents or newlines.
     */
    public static class FeatureTranslator extends TranslatorSupport
        implements FeatureCollectionIteration.Handler {
        String fc = "FeatureCollection";
        GeometryTransformer.GeometryTranslator geometryTranslator;
        String memberString;
        String currentPrefix;
        FeatureTypeNamespaces types;
        boolean prefixGml = false;
        boolean featureBounding = false;
        String srsName = null;
        String lockId = null;
        ContentHandler handler;
        private boolean running = true;

        /**
         * Constructor with handler.
         *
         * @param handler the handler to use.
         * @param prefix DOCUMENT ME!
         * @param ns DOCUMENT ME!
         * @param types DOCUMENT ME!
         * @param schemaLoc DOCUMENT ME!
         */
        public FeatureTranslator(ContentHandler handler, String prefix,
            String ns, FeatureTypeNamespaces types,
            SchemaLocationSupport schemaLoc) {
            super(handler, prefix, ns, schemaLoc);
            geometryTranslator = new GeometryTransformer.GeometryTranslator(handler);
            this.types = types;
            this.handler = handler;
            getNamespaceSupport().declarePrefix(geometryTranslator
                .getDefaultPrefix(), geometryTranslator.getDefaultNamespace());
            memberString = geometryTranslator.getDefaultPrefix()
                + ":featureMember";
        }

        void setGmlPrefixing(boolean prefixGml) {
            this.prefixGml = prefixGml;
        }

        void setFeatureBounding(boolean bounding) {
            this.featureBounding = bounding;
        }

        void setSrsName(String srsName) {
            this.srsName = srsName;
        }

        void setNumDecimals(int numDecimals) {
            geometryTranslator = new GeometryTransformer.GeometryTranslator(handler,
                    numDecimals);
        }

        void setUseDummyZ(boolean useDummyZ) {
            geometryTranslator = new GeometryTransformer.GeometryTranslator(handler,
                    geometryTranslator.getNumDecimals(), useDummyZ);
        }

        public void setLockId(String lockId) {
            this.lockId = lockId;
        }

        public FeatureTypeNamespaces getFeatureTypeNamespaces() {
        	return types;
        }
        
        public void encode(Object o) throws IllegalArgumentException {
            try {
                if (o instanceof FeatureCollection) {
                    FeatureCollection fc = (FeatureCollection) o;
                    FeatureCollectionIteration.iteration(this, fc);
                } else if (o instanceof FeatureReader) {
                    // THIS IS A HACK FOR QUICK USE
                    FeatureReader r = (FeatureReader) o;

                    startFeatureCollection();

                    handleFeatureReader(r);

                    endFeatureCollection();
                } else if (o instanceof FeatureResults) {
                    FeatureResults fr = (FeatureResults) o;
                    startFeatureCollection();
                    writeBounds(fr.getBounds());
                    handleFeatureReader(fr.reader());
                    endFeatureCollection();
                } else if (o instanceof FeatureResults[]) {
                    //Did FeatureResult[] so that we are sure they're all the same type.
                    //Could also consider collections here...  
                    FeatureResults[] results = (FeatureResults[]) o;
                    Envelope bounds = new Envelope();

                    for (int i = 0; i < results.length; i++) {
                        bounds.expandToInclude(results[i].getBounds());
                    }

                    startFeatureCollection();
                    writeBounds(bounds);

                    for (int i = 0; i < results.length; i++) {
                        handleFeatureReader(results[i].reader());
                    }

                    endFeatureCollection();
                } else {
                    throw new IllegalArgumentException("Cannot encode " + o);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace(System.out);
                throw new RuntimeException("error reading FeatureResults", ioe);
            }
        }

        public void handleFeatureReader(FeatureReader r)
            throws IOException {
            try {
                while (r.hasNext() && running) {
                    Feature f = r.next();
                    handleFeature(f);

                    FeatureType t = f.getFeatureType();

                    for (int i = 0, ii = f.getNumberOfAttributes(); i < ii;
                            i++) {
                        handleAttribute(t.getAttributeType(i), f.getAttribute(i));
                    }

                    endFeature(f);
                }
            } catch (Exception ioe) {
                throw new RuntimeException("Error reading Features", ioe);
            } finally {
                if (r != null) {
                    LOGGER.finer("closing reader " + r);
                    r.close();
                }
            }
        }

        public void startFeatureCollection() {
            try {
                String element = (getDefaultPrefix() == null) ? fc
                                                              : (getDefaultPrefix()
                    + ":" + fc);
                AttributesImpl atts = new AttributesImpl();

                if (lockId != null) {
                    atts.addAttribute("", "lockId", "lockId", "", lockId);
                }

                contentHandler.startElement("", "", element, atts);

            } catch (SAXException se) {
                throw new RuntimeException(se);
            }
        }

        public void endFeatureCollection() {
            end(fc);
        }

        /**
         * Prints up the gml for a featurecollection.
         *
         * @param collection DOCUMENT ME!
         */
        public void handleFeatureCollection(FeatureCollection collection) {
            startFeatureCollection();
            writeBounds(collection.getBounds());
        }

        /**
         * writes the <code>gml:boundedBy</code> element to output based on
         * <code>fc.getBounds()</code>
         *
         * @param bounds
         *
         * @throws RuntimeException if it is thorwn while writing the element
         *         or coordinates
         */
        public void writeBounds(Envelope bounds) {
            try {
                String boundedBy = geometryTranslator.getDefaultPrefix() + ":"
                    + "boundedBy";
                String box = geometryTranslator.getDefaultPrefix() + ":"
                    + "Box";
                contentHandler.startElement("", "", boundedBy, NULL_ATTS);
                geometryTranslator.encode(bounds, srsName);
                contentHandler.endElement("", "", boundedBy);
            } catch (SAXException se) {
                throw new RuntimeException(se);
            }
        }

        /**
         * Sends sax for the ending of a feature collection.
         *
         * @param collection DOCUMENT ME!
         */
        public void endFeatureCollection(FeatureCollection collection) {
            endFeatureCollection();
        }

        /**
         * Sends sax for the ending of a feature.
         *
         * @param f DOCUMENT ME!
         *
         * @throws RuntimeException DOCUMENT ME!
         */
        public void endFeature(Feature f) {
            try {
                String name = f.getFeatureType().getTypeName();

                if (currentPrefix != null) {
                    name = currentPrefix + ":" + name;
                }

                contentHandler.endElement("", "", name);
                contentHandler.endElement("", "", memberString);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * handles sax for an attribute.
         *
         * @param type DOCUMENT ME!
         * @param value DOCUMENT ME!
         *
         * @throws RuntimeException DOCUMENT ME!
         */
        public void handleAttribute(AttributeType type, Object value) {
            try {
                if (value != null) {
                    String name = type.getName();

                    //HACK: this should be user configurable, along with the

                    //other gml substitutions I shall add.

                    if (prefixGml //adding this in since the extra boundedBy
                            //hacking should only need to be done for the weird
                        //cite tests, and having this check before the string
                        //equals should get us better performance.  Albeit
                        //very slightly, but this method gets called millions
                            && (name.equals("boundedBy")
                            && Geometry.class.isAssignableFrom(value.getClass()))) {

                        writeBounds(((Geometry) value).getEnvelopeInternal());
                    } else {
                        String thisPrefix = currentPrefix;

                        if (prefixGml && gmlAtts.contains(name)) {
                            thisPrefix = "gml";
                        }

                        if (thisPrefix != null) {
                            name = thisPrefix + ":" + name;
                        }

                        contentHandler.startElement("", "", name, NULL_ATTS);

                        if (Geometry.class.isAssignableFrom(value.getClass())) {
                            geometryTranslator.encode((Geometry) value, srsName);
                        } else {
                            String text = value.toString();
                            contentHandler.characters(text.toCharArray(), 0,
                                text.length());
                        }

                        contentHandler.endElement("", "", name);
                    }
                }

                //REVISIT: xsi:nillable is the proper xml way to handle nulls,
                //but OGC people are fine with just leaving it out.       
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Handles sax for a feature.
         *
         * @param f DOCUMENT ME!
         *
         * @throws RuntimeException DOCUMENT ME!
         */
        public void handleFeature(Feature f) {
            try {
                contentHandler.startElement("", "", memberString, NULL_ATTS);

                FeatureType type = f.getFeatureType();
                String name = type.getTypeName();
                currentPrefix = getNamespaceSupport().getPrefix(f.getFeatureType()
                                                                 .getNamespace()
                                                                 .toString());

                if (currentPrefix == null) {
                    currentPrefix = types.findPrefix(f.getFeatureType());
                }

                if (currentPrefix == null) {
                    throw new RuntimeException(
                        "Could not locate namespace for FeatureType : "
                        + type.getTypeName() + ":" + type.getNamespace()
                        + "look up in: " + types);
                }

                if (currentPrefix != null) {
                    name = currentPrefix + ":" + name;
                }

                Attributes fidAtts = encodeFeatureId( f );

                contentHandler.startElement("", "", name, fidAtts);

                if (featureBounding) {
                    //HACK pt.2 see line 511, if the cite stuff wanted to hack
                    //in a boundedBy geometry, we don't want to do it twice.
                    //So if 
                    if (prefixGml && (f.getAttribute("boundedBy") != null)) {
                        //do nothing, since our hack will handle it.
                    } else {
                        writeBounds(f.getBounds());
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        protected Attributes encodeFeatureId( Feature f ) {
        	AttributesImpl fidAtts = new org.xml.sax.helpers.AttributesImpl();
            String fid = f.getID();

            if (fid != null) {
                fidAtts.addAttribute("", "fid", "fid", "fids", fid);
            }
            
            return fidAtts;
        }
        
    }
}
