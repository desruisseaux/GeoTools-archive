package it.geosolutions.utils.progress;

import java.util.EventListener;

/**
 * @author Simone Giannecchini
 * 
 */
public interface ProcessingEventListener extends EventListener {

	public void getNotification(final ProcessingEvent event);
    
    public void exceptionOccurred(final ExceptionEvent event);

}