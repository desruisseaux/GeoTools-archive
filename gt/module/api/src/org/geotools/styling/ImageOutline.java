package org.geotools.styling;

import org.geotools.event.GTComponent;

/**
 * ImageOutline specifies how individual source rasters in a multi-raster set
 * (such as a set of satellite-image scenes) should be outlined to make the 
 * individual-image locations visible. 
 * 
 *  &lt;xsd:element name="ImageOutline"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;         &quot;ImageOutline&quot; specifies
 *              how individual source rasters in         a multi-raster set
 *              (such as a set of satellite-image scenes)         should be
 *              outlined to make the individual-image locations visible.       &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *      &lt;xsd:complexType&gt;
 *          &lt;xsd:choice&gt;
 *              &lt;xsd:element ref="sld:LineSymbolizer"/&gt;
 *              &lt;xsd:element ref="sld:PolygonSymbolizer"/&gt;
 *          &lt;/xsd:choice&gt;
 *      &lt;/xsd:complexType&gt;
 *  &lt;/xsd:element&gt; 
 *  
 * @author Justin Deoliveira, The Open Planning Project
 *
 * @source $URL$
 */
public interface ImageOutline extends GTComponent {

	/**
	 * Returns the symbolizer of the image outline.
	 * 
	 * @return One of {@see PolygonSymbolizer},{@see LineSymbolizer}.
	 */
	Symbolizer getSymbolizer();
	
	/**
	 * Sets the symbolizer of the image outline.
	 * 
	 * @param symbolizer The new symbolizer, one of {@see PolygonSymbolizer},{@see LineSymbolizer}.
	 */
	void setSymbolizer(Symbolizer symbolizer);
}
