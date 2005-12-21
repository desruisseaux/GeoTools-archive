package org.geotools.util;

/**
 * A default progress listener implementation suitable for
 * subclassing.
 * <p>
 * This implementation supports cancelation. The default
 * implementations of the other methods do nothing.
 * </p>
 */
public class NullProgressListener implements ProgressListener {
	boolean canceled = false;
	String description;
	
	public NullProgressListener() {
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String arg0) {
		this.description = arg0;
	}

	public void started() {
		//do nothing
	}

	public void progress(float arg0) {
		//do nothing
	}

	public void complete() {
		//do nothing
	}

	public void dispose() {
		//do nothing
	}

	public void warningOccurred(String arg0, String arg1, String arg2) {
		//do nothing
	}

	public void exceptionOccurred(Throwable arg0) {
		//do nothing
	}

	public void setCanceled(boolean cancel) {
		this.canceled = cancel;
	}
	
	public boolean isCanceled() {
		return canceled;
	}
}
