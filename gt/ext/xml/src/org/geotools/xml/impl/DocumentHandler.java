package org.geotools.xml.impl;

public interface DocumentHandler extends Handler {

	/**
	 * Returns the element handler for the root element of 
	 * an instance document.
	 * 
	 * @return An element handler, or null.
	 */
	ElementHandler getDocumentElementHandler();
}
