package it.geosolutions.utils.progress;

import it.geosolutions.utils.imagemosaic.MosaicIndexBuilder;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.option.DefaultOption;

public abstract class ProgressManager {

	/**
	 * Private Class which simply fires the events using a copy of the listeners
	 * list in order to avoid problems with listeners that remove themselves or
	 * are removed by someone else
	 */
	protected final static class ProgressEventDispatchThreadEventLauncher
			implements Runnable {

		private ProcessingEvent event;

		private Object[] listeners;

		ProgressEventDispatchThreadEventLauncher() {
		}

		synchronized void setEvent(final ProcessingEvent evt,
				final Object[] listeners) {

			this.listeners = listeners;
			this.event = evt;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			final int numListeners = listeners.length;
            if (event instanceof ExceptionEvent)
                for (int i = 0; i < numListeners; i++)
                    ((ProcessingEventListener) listeners[i])
                            .exceptionOccurred((ExceptionEvent) this.event);
            else
                for (int i = 0; i < numListeners; i++)
                    ((ProcessingEventListener) listeners[i]).getNotification(this.event);
		}

	}

	/**
	 * Options for the command line.
	 */
	protected final List cmdOpts = new ArrayList(5);

	protected final Parser cmdParser = new Parser();

	protected final ArgumentBuilder arguments = new ArgumentBuilder();
    
    /**
     * Set this to false for command line UIs where the delayed event sending may prevent some
     * messages to be seen before the tool exits, to true for real GUI where you don't want
     * the processing to be blocked too long, or when you have slow listeners in general.
     */
    protected final boolean sendDelayedMessages = false;

	/** Event launcher. */
	private ProgressEventDispatchThreadEventLauncher eventLauncher = new ProgressEventDispatchThreadEventLauncher();

	/**
	 * Proper way to stop a thread is not by calling Thread.stop() but by using
	 * a shared variable that can be checked in order to notify a terminating
	 * condition.
	 */
	private volatile boolean stopThread = false;

	/**
	 * List containing all the objects that want to be notified during
	 * processing.
	 */
	private List notificationListeners = new ArrayList();

	/**
	 * Default priority for the underlying {@link Thread}.
	 */
	private static int DEFAULT_PRIORITY = Thread.NORM_PRIORITY;

	protected final DefaultOptionBuilder optionBuilder = new DefaultOptionBuilder();

	protected Group optionsGroup;

	protected CommandLine cmdLine;

	protected Option helpOpt;

	protected DefaultOption priorityOpt;

	/**
	 * Default priority for the underlying {@link Thread}.
	 */
	protected int priority = DEFAULT_PRIORITY;

	protected	DefaultOption versionOpt;

	public ProgressManager() {
		super();
	}

	/**
	 * Adding a listener for the notifications.
	 * 
	 * @param listener
	 */
	public final synchronized void addProcessingEventListener(
			final ProcessingEventListener listener) {
		notificationListeners.add(listener);
	}

	/**
	 * Removing a listener.
	 * 
	 * @param listener
	 */
	public final synchronized void removeProcessingEventListener(
			final ProcessingEventListener listener) {
		notificationListeners.remove(listener);
	}

	/**
	 * Removing all the listeners.
	 * 
	 */
	public final synchronized void removeAllProcessingEventListeners() {
		notificationListeners.clear();

	}

	/**
	 * Firing an event to listeners in order to inform them about what we are
	 * doing and about the percentage of work already carried out.
	 * 
	 * @param string
	 *            The message to show.
	 * @param percentage
	 *            The percentage for the process.
	 */
	protected synchronized void fireEvent(final String string,
			final double percentage) {
		final String newLine = System.getProperty("line.separator");
		final StringBuffer message = new StringBuffer("Thread Name ");
		message.append(Thread.currentThread().getName()).append(newLine);
		message.append(this.getClass().toString()).append(newLine).append(
				string);
		final ProcessingEvent evt = new ProcessingEvent(this, string,
				percentage);
        ProgressEventDispatchThreadEventLauncher eventLauncher = new ProgressEventDispatchThreadEventLauncher();
		eventLauncher.setEvent(evt, this.notificationListeners.toArray());
        sendEvent(eventLauncher);
	}

    protected void sendEvent(ProgressEventDispatchThreadEventLauncher eventLauncher) {
        if(sendDelayedMessages)
            SwingUtilities.invokeLater(eventLauncher);
        else
            eventLauncher.run();
    }
    
    /**
     * Firing an exception event to listeners in order to inform them that processing
     * broke and we can no longer proceed
     * 
     * @param string
     *            The message to show.
     * @param percentage
     *            The percentage for the process.
     * @param ex
     *            the actual exception occurred
     */
    protected synchronized void fireException(final String string,
            final double percentage, Exception ex) {
        final String newLine = System.getProperty("line.separator");
        final StringBuffer message = new StringBuffer("Thread Name ");
        message.append(Thread.currentThread().getName()).append(newLine);
        message.append(this.getClass().toString()).append(newLine).append(
                string);
        final ExceptionEvent evt = new ExceptionEvent(this, string,
                percentage, ex);
        ProgressEventDispatchThreadEventLauncher eventLauncher = new ProgressEventDispatchThreadEventLauncher();
        eventLauncher.setEvent(evt, this.notificationListeners.toArray());
        sendEvent(eventLauncher);
    }
    
    /**
     * Firing an exception event to listeners in order to inform them that processing
     * broke and we can no longer proceed. This is a convenience method, it will call
     * {@link #fireException(String, double, Exception)} with the exception message and -1 as 
     * percentage.
     * 
     * @param ex
     *            the actual exception occurred
     */
    protected synchronized void fireException(Exception ex) {
        fireException(ExceptionEvent.getMessageFromException(ex), -1, ex);
    }

	/**
	 * Should this thread be stopped?
	 * 
	 */
	public final boolean getStopThread() {
		return stopThread;
	}

	/**
	 * Stop this thread.
	 * 
	 * @param stop
	 */
	public final void stopThread() {
		stopThread = true;
	}

	/**
	 * Cleans up the {@link MosaicIndexBuilder}.
	 * 
	 */
	public void dispose() {
		removeAllProcessingEventListeners();
	}
	
	public abstract void run();

}