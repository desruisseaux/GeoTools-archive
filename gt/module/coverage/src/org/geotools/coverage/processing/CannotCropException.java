package org.geotools.coverage.processing;

/**
 * 
 * @author Simone Giannecchini
 * 
 */
public class CannotCropException extends RuntimeException {

	public CannotCropException(String message, Throwable exception) {
		super(message,exception);
	}

	public CannotCropException(String message) {
		super(message);
	}

}
