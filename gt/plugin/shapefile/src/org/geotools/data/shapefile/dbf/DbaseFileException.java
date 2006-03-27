package org.geotools.data.shapefile.dbf;

import org.geotools.data.DataSourceException;
/**
 * Thrown when an error relating to the shapefile
 * occurs.
 * @source $URL$
 */
public class DbaseFileException extends DataSourceException {

	private static final long serialVersionUID = -6890880438911014652L;
	public DbaseFileException(String s){
        super(s);
    }
    public DbaseFileException(String s,Throwable cause){
        super(s,cause);
    }
}




