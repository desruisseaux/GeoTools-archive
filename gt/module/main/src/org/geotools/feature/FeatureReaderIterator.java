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
package org.geotools.feature;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.geotools.data.FeatureReader;

/**
 * An iterator that wraps around a FeatureReader.  
 * The Iterator's hasNext() will return false if the wrapped feature reader's 
 * hasNext method throws an exception.  If next() throws an exeption a NoSuchElementException
 * will be thrown.
 *  
 * @author jeichar
 * @source $URL$
 */
public class FeatureReaderIterator implements Iterator {

	private FeatureReader reader;

	/**
	 * 
	 */
	public FeatureReaderIterator(FeatureReader reader) {
		this.reader=reader;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		try{
			return reader.hasNext();
		}catch (Exception e) {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	public Object next() {
		try{
			return reader.next();
		}catch (Exception e) {
			throw new NoSuchElementException("Exception raised during next: "+e.getLocalizedMessage());
		}
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		throw new UnsupportedOperationException("Iterator is read only");
	}
	
	public void close(){
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
