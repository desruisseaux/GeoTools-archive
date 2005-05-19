package org.geotools.styling;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;

import org.geotools.data.memory.MemoryDataStore;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureTypeFactory;




import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeBuilder;

import org.geotools.filter.ExpressionDOMParser;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.vividsolutions.jts.geom.Geometry;

public class SLDInlineFeatureParser
{
	
	/** hash table that takes a epsg# to its definition**/
	private static Hashtable SRSLookup = new Hashtable();
	

	public FeatureType  featureType=null;
	public MemoryDataStore dataStore = null;
	Node  rootNode = null;
	ArrayList features= new ArrayList();
	CoordinateReferenceSystem  SRS = null; // default EPSG#.
	
	private static int uniqueNumber = 0;
	
	public SLDInlineFeatureParser(Node root) throws Exception
	{
		//handle FeatureCollection or Feature Tag
		
		boolean isFeatureCollection = false;
		
		if (!(isSimple(root)))  //make sure this isnt too complex to parse easily.
		{
			throw new Exception("couldnt parse the SLD Inline features!");//shouldnt get here
		}
		Node fc = getNode(root,"FeatureCollection");
		if (fc != null)
		{
			isFeatureCollection = true;
			root = fc;//decend down one level
		}
				
		
		featureType = makeFeatureType(root,isFeatureCollection);
		if (featureType == null)
			throw new Exception("SLD InlineFeature Parser - couldnt determine a FeatureType.  See help for whats supported.");//shouldnt get here
		
		makeFeatures(root,isFeatureCollection);
		if (features.size() ==0)
			throw new Exception("SLD InlineFeature Parser - didnt find any features!");
			
		buildStore();
	}

	/**
	 *   1. we have a FeatureType (cf. makeFeatureType)
	 *   2. we iterate through either the featureCollection or the _Feature set
	 *   3.      we build a Feature for each element of the set
	 *   4. we stick it in the features list
	 * 
	 *  For example:
	 * 
	 *    <InlineFeature>
	 *       <Dave>
	 *            ...
	 *       </Dave>
	 *       <Dave>
	 *             ...
	 *       </Dave>
	 *    </InlineFeature>
	 * 
	 *    --- OR ----
	 * 
	 * <InlineFeature>
	 *    <FeatureCollection>
	 *       <featureMember>
	 *          <Dave>
	 *               ...
	 *          </Dave>
	 *       </featureMember>
	 *       <featureMember>
	 *          <Dave>
	 *               ...
	 *         </Dave>
	 *       </featureMember>
	 *    </FeatureCollection>
	 * </InlineFeature>
	 * 	
	 * 
	 * @param root will point at either "<InlineFeature>" or "<FeatureCollection>
	 */
	private void makeFeatures(Node root,boolean isCollection) throws Exception
	{
		//iterate through each of the elements inside the root		
		
		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) 
		{
			Node child = children.item(i);

			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) 
			{
				continue;
			}
			String childName = child.getLocalName();
			if (childName == null)
				childName = child.getNodeName();
			if (childName.equalsIgnoreCase("boundedBy"))  //be nice and ignore this
				continue; 
			
			if (isCollection)
			{
				//decend into the featureMember
				child = descend(child);
			}
			if (child ==null)
				throw new Exception("SLD inlineFeature Parser - couldnt extract a feature from the dom.");
			
			Feature f = parseFeature(child,featureType);
			features.add(f);
		}
	}

	/**
	 *  simple - child points to a <featureMember>, we want it to point to the element inside!
	 * 
	 *  in general, this will find the 1st element node inside the node.
	 * 
	 * @param child
	 * @return
	 */
	private Node descend(Node root) 
	{
		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) 
		{
			Node child = children.item(i);

			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) 
			{
				continue;
			}
			return child;
		}
		return null; //nothing inside
	}

	/**
	 *  Parse the feature pointed to by this node.
	 * 
	 *  See the makeFeatureType() function - this is very similiar except it does a little parsing.
	 * 
	 * 
	 * @param feature - points to the actual feature ie. "<Person>"
	 * @param featureType
	 * @return
	 */
	private Feature parseFeature(Node feature, FeatureType featureType) throws Exception
	{
		Object[] nullAtts = new Object[featureType.getAttributeCount()];  // initialized to nulls
		Feature f= featureType.create(nullAtts);
		
		NodeList children = feature.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) 
		{
			Node child = children.item(i);
			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
				continue;
			}
			String childName = child.getLocalName();
			if (childName == null)
			{
				childName = child.getNodeName();
			}	

			Object value = getValue(child);
			try{
				f.setAttribute(childName,value);	
			}
			catch (Exception e)
			{
				e.printStackTrace(); // we hid this from the user
			}
		}
		return f;
	}

	/**
	 *  Given a node, determine if its a geometry or a string attribute
	 *  return the corresponding value.
	 * @param child
	 * @return
	 */
	private Object getValue(Node root) 
	{
		NodeList children = root.getChildNodes();
		StringBuffer strVal = new StringBuffer();
		
		for (int i = 0; i < children.getLength(); i++) 
		{
			Node child = children.item(i);
			if ((child == null) ) 
			{
				continue;
			}
			if (child.getNodeType() == Node.TEXT_NODE)
			{
				strVal.append(child.getNodeValue());  //might get here multiple times -- see sax spec
			}
			//we have a nested element!  Assume its a geometry
			if (child.getNodeType() == Node.ELEMENT_NODE)
			{
				return parseGeometry(child);
			}
		}
		return strVal;
	}

	/**
	 *  <Person>
	 *     <location>
	 *       <gml:Point>...
	 *       </gml:Point>
	 *     </location>
	 *   </Person>
	 * 
	 *  Decend a level and then pass it off to the geometry parser.
	 *  
	 * NOTE: also handles SRS information:
	 * 
	 * <gml:Point srsName="http://www.opengis.net/gml/srs/epsg.xml#4326">
	 * 
	 *  TODO: handle more than just epsg for CRS
	 * 
	 * @param root  -- points to "<gml:Point>"
	 * @return
	 */
	private Geometry parseGeometry(Node root) 
	{
		NamedNodeMap atts = root.getAttributes();
		if (SRS == null) //try to avoid parsing more than once.
		{
			Node srsName = atts.getNamedItem("srsName");
			if  ( srsName != null) 
			{
				parseSRS(srsName.getNodeValue());
			}
		}
		return ExpressionDOMParser.parseGML(root);
	}

	/**
	 *  expected input:
	 *  "http://www.opengis.net/gml/srs/epsg.xml#4326"
	 *   NOTE: only supports epsg#s.
	 * @param srs
	 */
	private void parseSRS(String srs) 
	{
		 if (srs ==null)
		 	return;
		 String epsgCode = srs.substring(srs.indexOf('#')+1);
		 try {
		 	int srsnum = Integer.parseInt(epsgCode);
		    SRS= getSRS(srsnum);
		 }
		 catch (Exception e)
		 {
		 	System.out.println("couldnt decode SRS - EPSG:"+epsgCode+". currently only supporting EPSG #"); // dont report to user - just put in log
		 }		
	}

	/**
	 * 
	 * Checks to make sure we're not going to shoot ourselves in the foot.
	 *    if InlineFeature has a FeatureCollection, then thats the only node
	 *           ie. no set of FeatureCollections or FeatureCollection mixed with a set of Feature 
	 * 
	 *    Other stuff as we think of them.
	 * 
	 * @param root SLD root node -- "InlineFeature"
	 * @return true if okay, otherwise exception
	 */
	private boolean isSimple(Node root)  throws Exception
	{
		// if there's a <FeatureCollection>, thats the only child
		// if there was a <FeatureCollection>, then descend into it
		// check to make sure there are only <featureMember> in it
	
			int  foundFeature = 0;
			int  foundFC      = 0;
			
			Node fcNode = null;
			String featureName  = null;
			
			NodeList children = root.getChildNodes();

			for (int i = 0; i < children.getLength(); i++) 
			{
				Node child = children.item(i);

				if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) 
				{
					continue;
				}
				String childName = child.getLocalName();
				if (childName == null)
					childName = child.getNodeName();
		
				if (childName.equalsIgnoreCase("FeatureCollection"))
				{
					(foundFC)++;
					fcNode = child;
				}
				else 
				{
					if (featureName == null)
						featureName = childName;
					if (!(childName.equalsIgnoreCase(featureName)))
						throw new Exception ("SLD inline feature parser  - it appear that there is >1 feature type present.  I got a "+ childName+ " when I was expecting a "+featureName+" tag");
				}
			}
			if (foundFC >1)
				throw new Exception ("SLD - UserLayer, inline feature parser - found >1 FeatureCollection.  Not supported");
			if ( (foundFC >0) && (foundFeature>0) )
				throw new Exception ("SLD - UserLayer, inline feature parser - found  FeatureCollection and featureMembers.  Not supported");


			if (foundFC ==0)
				return true;
			
			 featureName  = null;
			 
			//otherwise decend into the featurecollection and check to make sure it only contains features
			children = fcNode.getChildNodes();

			for (int i = 0; i < children.getLength(); i++) 
			{
				Node child = children.item(i);

				if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) 
				{
					continue;
				}
				String childName = child.getLocalName();
				if (childName == null)
					childName = child.getNodeName();
				if (childName.equalsIgnoreCase("featureMember"))
					foundFeature++;
				else if (childName.equalsIgnoreCase("boundedBy"))
				{
					//this is okay -- we'll be nice and ignore it.
				}
				else if (childName.equalsIgnoreCase("FeatureCollection"))
				{
					throw new Exception ("SLD - UserLayer, inline feature parser - found a node of type FeatureCollection.  Expected a featureMember - dont support nested collections.");				

				}
				else 
					throw new Exception ("SLD - UserLayer, inline feature parser - found a node of type '"+child.getLocalName()+"' and dont understand it.  Expected a featureMember.");				
			}

			return true;		
	}

	/**
	 * 
	 */
	private void buildStore() 
	{
		dataStore = new MemoryDataStore( (Feature[]) features.toArray(new Feature[features.size()]));		
	}

	/**
	 *  1. get an actual Feature Node 
	 *  2. go through each of its subtags
	 *  3. if that subtag contains a geometry, then it an attribute of type geometry, otherwise string
	 * 
	 * NOTE: we set the namespace to be "http://temp.inline.feature.sld.com"
	 * NOTE: we set the featuretype name to be whatever the enclosing tag is.  For example:
	 * <FeatureCollection>
	 *   <gml:featureMember>
     *       <tiger:point_landmark fid="point_landmark.490053">
     *            <tiger:wkb_geometry>
     *   	         <gml:Point srsName="http://www.opengis.net/gml/srs/epsg.xml#4326">
     *   	             <gml:coordinates decimal="." cs="," ts=" ">-73.983597,40.736308</gml:coordinates>
     *   	         </gml:Point>
     *   	      </tiger:wkb_geometry>
     *   	      <tiger:laname>Cabrini Medical Center</tiger:laname>
     *       </tiger:point_landmark>
     *   <gml:featureMember>
	 * </FeatureCollection>
	 * 
	 *   Would have a Featuretype name of "tiger:wkb_geometry" with 2 attributes:
	 *       wkb_geometry  -- geometry
	 *       laname        -- string
	 * 
	 * @param root
	 */
	private FeatureType makeFeatureType(Node root,boolean isCollection) throws Exception
	{
		Node feature = null;
		//get a Feature node
		Node featureMember =root;
		if (isCollection)
			featureMember = getNode(root,"featureMember");
		
		//next node under featureMember what we want.  Unless its a boundedBy, in which case we dont want it.
		NodeList children = featureMember.getChildNodes();

				// look for next element that's not "boundedBy"
		for (int i = 0; i < children.getLength(); i++) 
		{
			Node child = children.item(i);
			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
				continue;
			}
			String childName = child.getLocalName();
			if (childName == null) {
				childName = child.getNodeName();
			}
			if (!(childName.equalsIgnoreCase("boundedBy")) )
			{
					feature = child;
					break;
			}
		}
		if (feature == null)
			throw new Exception ("couldnt find a Feature in the Inline Features!");
		
		//okay, we have a feature, we now need to figure out its feature type.
		// method:
		//   step through each node, its name is a new element in the Feature
		//   we look for any internal tags (nesting).  There there are, we check to see if its a geometry
		//   otherwise the type is string. 
		//   simple!
		
		String featureName = feature.getLocalName();
		if (featureName == null) {
			featureName = feature.getNodeName();
		}
		
		//DJB:I considered making each featuretype unique (thats the uniquenumber), but decided against
		//    it so that the standard feature type filtering stuff would work ("<FeatureTypeStyle>" 
		//    and <FeatureTypeConsraint>
		  FeatureTypeBuilder build = FeatureTypeFactory.newInstance(featureName);  
		  build.setName(featureName);  
		  build.setNamespace(new URI("http://temp.inline.feature.sld.com"));  

		  
		children = feature.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) 
		{
			Node child = children.item(i);
			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
				continue;
			}
			String childName = child.getLocalName();
			if (childName == null)
			{
				childName = child.getNodeName();
			}	
			AttributeType attType = null;
			//okay, have a tag, check to see if its a geometry
			if (isGeometry(child))
			{
				attType = AttributeTypeFactory.newAttributeType(childName,Geometry.class);
			}
			else
			{
				attType = AttributeTypeFactory.newAttributeType(childName,String.class);
			}
			build.addType(attType);	
		}
		return build.getFeatureType();
	}
	
	
	
	/**
	 *  looks for a nested attribute - assumes that this is a geometry.
	 *  TODO: be much smarter
	 * @param child
	 * @return
	 */
	private boolean isGeometry(Node root) 
	{
		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) 
		{
			Node child = children.item(i);
			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
				continue;
			}
			//we have a nested element!
			return true;
		}
		return false;
	}

	/**
	 *   Give a node and the name of a child of that node, find its (string) value.
	 *   This doesnt do anything complex.
	 *  
	 * @param node
	 * @param childName
	 * @return
	 */
	public Node getNode(Node parentNode, String wantedChildName)
	{
		NodeList children = parentNode.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) 
		{
			Node child = children.item(i);

			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
				continue;
			}
			String childName = child.getLocalName();
			if (childName == null) {
				childName = child.getNodeName();
			}
			if (childName.equalsIgnoreCase(wantedChildName)) 
			{
				return child;
			}
		}
		return null;
	}
	
	public synchronized  int getUID()
	{
		return uniqueNumber++;
	}
	
	/**
     *  simple way of getting epsg #.
     *  We cache them so that we dont have to keep reading the DB or the epsg.properties file.
     *   I cannot image a system with more than a dozen CRSs in it...
     * 
     * @param epsg
     * @return
     */
    private CoordinateReferenceSystem getSRS(int epsg) throws Exception
    {
    	CoordinateReferenceSystem result = (CoordinateReferenceSystem) SRSLookup.get(  new Integer(epsg) );
    	if (result == null)
    	{
    		//make and add to hash
    		result = CRS.decode("EPSG:"+epsg);
    		SRSLookup.put( new Integer(epsg)  , result);
    	}
    	return result;
    }
    
}