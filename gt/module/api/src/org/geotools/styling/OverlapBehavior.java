package org.geotools.styling;


/**
 * OverlapBehavior tells a system how to behave when multiple raster images in
 * a layer overlap each other, for example with satellite-image scenes. 
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:element name="OverlapBehavior"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;         &quot;OverlapBehavior&quot; tells a
 *              system how to behave when multiple         raster images in
 *              a layer overlap each other, for example with
 *              satellite-image scenes.       &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *      &lt;xsd:complexType&gt;
 *          &lt;xsd:choice&gt;
 *              &lt;xsd:element ref="sld:LATEST_ON_TOP"/&gt;
 *              &lt;xsd:element ref="sld:EARLIEST_ON_TOP"/&gt;
 *              &lt;xsd:element ref="sld:AVERAGE"/&gt;
 *              &lt;xsd:element ref="sld:RANDOM"/&gt;
 *          &lt;/xsd:choice&gt;
 *      &lt;/xsd:complexType&gt;
 *  &lt;/xsd:element&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @author Justin Deoliveira, The Open Planning Project
 *
 * @source $URL$
 */
public interface OverlapBehavior {

	static final OverlapBehavior LATEST_ON_TOP = new OverlapBehavior(){
		public String getValue() {
			return "LATEST_ON_TOP";
		}
	};
	
	static final OverlapBehavior EARLIEST_ON_TOP = new OverlapBehavior(){
		public String getValue() {
			return "EARLIEST_ON_TOP";
		}
	};
	
	static final OverlapBehavior AVERAGE = new OverlapBehavior(){
		public String getValue() {
			return "AVERAGE";
		}
	};
	
	static final OverlapBehavior RANDOM = new OverlapBehavior(){
		public String getValue() {
			return "RANDOM";
		}
	};
	
	String getValue();
}


