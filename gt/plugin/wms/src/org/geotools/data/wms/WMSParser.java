/*
 * Created on Aug 21, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms;

import java.util.Enumeration;

import org.geotools.data.wms.capabilities.Capabilities;
import org.jdom.Element;

/**
 * @author Kefka
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface WMSParser {
	public static final int NO = 0;
	public static final int GENERIC = 1;
	public static final int CUSTOM = 2;
	
	/**
	 * 
	 * @param element
	 * @return an Enumeration consisting of NO, GENERIC, or CUSTOM
	 */
	public Enumeration canProcess(Element element);
	public Capabilities constructCapabilities(Element capabilitiesElement);
}
