/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2001, Institut de Recherche pour le D�veloppement
 *    (C) 1999, P�ches et Oc�ans Canada
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.gui.headless;

// J2SE dependencies
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;

// Formatting
import java.io.PrintWriter;
import java.io.CharArrayWriter;
import java.text.NumberFormat;
import java.text.FieldPosition;

// Java Mail
import javax.mail.Session;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.AddressException;

// Geotools dependencies
import org.geotools.util.ProgressListener;
import org.geotools.util.SimpleInternationalString;
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;
import org.opengis.util.InternationalString;


/**
 * Reports progress by sending email to the specified address at regular interval.
 *
 * @since 2.0
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ProgressMailer implements ProgressListener {
    /**
     * Nom de l'op�ration en cours. Le pourcentage sera �cris � la droite de ce nom.
     */
    private String description;

    /**
     * Langue � utiliser pour le formattage.
     */
    private final Locale locale;

    /**
     * Session � utiliser pour envoyer des courriels.
     */
    private final Session session;

    /**
     * Adresses des personnes � qui envoyer un rapport sur les progr�s.
     */
    private final Address[] address;

    /**
     * Laps de temps entre deux courriers �lectroniques informant des progr�s.
     * On attendra que ce laps de temps soit �coul�s avant d'envoyer un nouveau courriel.
     */
    private long timeInterval = 3*60*60*1000L;

    /**
     * Date et heure � laquelle envoyer le prochain courriel.
     */
    private long nextTime;

    /**
     * {@code true} if the action has been canceled.
     */
    private volatile boolean canceled;

    /**
     * Creates an objects reporting progress to the specified email address.
     *
     * @param  host The server to use for sending emails.
     * @param  address Email adress where to send progress reports.
     * @throws AddressException if the specified address use an invalid syntax.
     */
    public ProgressMailer(final String host, final String address) throws AddressException {
        this(Session.getDefaultInstance(properties(host)), new InternetAddress[] {
                new InternetAddress(address)});
    }

    /**
     * Creates an objects reporting progress to the specified email adresses.
     *
     * @param session Session to use for sending emails.
     * @param address
     */
    public ProgressMailer(final Session session, final Address[] address) {
        this.session = session;
        this.address = address;
        this.locale  = Locale.getDefault();
        nextTime = System.currentTimeMillis();
    }

    /**
     * Retourne un ensemble de propri�t�s n�cessaires pour ouvrir une session.
     *
     * @param host Nom du serveur � utiliser pour envoyer des courriels.
     */
    private static final Properties properties(final String host) {
        final Properties props = new Properties();
        props.setProperty("mail.smtp.host", host);
        return props;
    }

    /**
     * Returns the time laps (in milliseconds) between two emails.
     */
    public long getTimeInterval() {
        return timeInterval;
    }

    /**
     * Set the time laps (in milliseconds) between two emails.
     * The default value is 3 hours.
     */
    public synchronized void setTimeInterval(final long interval) {
        this.timeInterval = interval;
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return description;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Envoie le message sp�cifi� par courrier �lectronique.
     *
     * @param method Nom de la m�thode qui appelle celle-ci. Cette information
     *        est utilis�e pour produire un message d'erreur en cas d'�chec.
     * @param subjectKey Cl� du sujet: {@link ResourceKeys#PROGRESS},
     *        {@link ResourceKeys#WARNING} ou {@link ResourceKeys#EXCEPTION}.
     * @param messageText Message � envoyer par courriel.
     */
    private void send(final String method, final int subjectKey, final String messageText) {
        try {
            final Message message = new MimeMessage(session);
            message.setFrom();
            message.setRecipients(Message.RecipientType.TO, address);
            message.setSubject(Vocabulary.getResources(locale).getString(subjectKey));
            message.setSentDate(new Date());
            message.setText(messageText);
            Transport.send(message);
        } catch (MessagingException exception) {
            final LogRecord warning = new LogRecord(Level.WARNING,
                    "CATCH "+Utilities.getShortClassName(exception));
            warning.setSourceClassName(getClass().getName());
            warning.setSourceMethodName(method);
            warning.setThrown(exception);
            Logger.getLogger("org.geotools.gui.progress").log(warning);
        }
    }

    /**
     * Envoie par courrier �lectronique un rapport des progr�s.
     *
     * @param method Nom de la m�thode qui appelle celle-ci. Cette information
     *        est utilis�e pour produire un message d'erreur en cas d'�chec.
     * @param percent Pourcentage effectu� (entre 0 et 100).
     */
    private void send(final String method, final float percent) {
        final Runtime       system = Runtime.getRuntime();
        final float    MEMORY_UNIT = (1024f*1024f);
        final float     freeMemory = system.freeMemory()  / MEMORY_UNIT;
        final float    totalMemory = system.totalMemory() / MEMORY_UNIT;
        final Vocabulary resources = Vocabulary.getResources(locale);
        final NumberFormat  format = NumberFormat.getPercentInstance(locale);
        final StringBuffer  buffer = new StringBuffer(description!=null ?
                description : resources.getString(VocabularyKeys.PROGRESSION));
        buffer.append(": "); format.format(percent/100, buffer, new FieldPosition(0));
        buffer.append('\n');
        buffer.append(resources.getString(VocabularyKeys.MEMORY_HEAP_SIZE_$1,
                                          new Float(totalMemory)));
        buffer.append('\n');
        buffer.append(resources.getString(VocabularyKeys.MEMORY_HEAP_USAGE_$1,
                                          new Float(1-freeMemory/totalMemory)));
        buffer.append('\n');
        send(method, VocabularyKeys.PROGRESSION, buffer.toString());
    }

    /**
     * Send an emails saying that the operation started.
     */
    public synchronized void started() {
        send("started", 0);
    }

    /**
     * Notifies progress. This method will send an email only if at least the amount
     * of time specified by {@link #setTimeInterval} is ellapsed since the last email.
     */
    public synchronized void progress(float percent) {
        final long time = System.currentTimeMillis();
        if (time > nextTime) {
            nextTime = time + timeInterval;
            if (percent <  1f) percent =  1f;
            if (percent > 99f) percent = 99f;
            send("progress", percent);
        }
    }

    /**
     * Send an emails saying that the operation finished.
     */
    public synchronized void complete() {
        send("complete", 100);
    }

    /**
     * Releases any resource used by this object.
     */
    public void dispose() {
    }

    /**
     * {@inheritDoc}
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * {@inheritDoc}
     */
    public void setCanceled(final boolean canceled) {
        this.canceled = canceled;
    }

    /**
     * Send a warning by email.
     */
    public synchronized void warningOccurred(final String source,
                                             final String margin,
                                             final String warning)
    {
        final StringBuffer buffer=new StringBuffer();
        if (source != null) {
            buffer.append(source);
            if (margin != null) {
                buffer.append(" (");
                buffer.append(margin);
                buffer.append(')');
            }
            buffer.append(": ");
        } else if (margin != null) {
            buffer.append(margin);
            buffer.append(": ");
        }
        buffer.append(warning);
        send("warningOccurred", VocabularyKeys.WARNING, buffer.toString());
    }

    /**
     * Send an exception stack trace by email.
     */
    public synchronized void exceptionOccurred(final Throwable exception) {
        final CharArrayWriter buffer = new CharArrayWriter();
        exception.printStackTrace(new PrintWriter(buffer));
        send("exceptionOccurred", VocabularyKeys.EXCEPTION, buffer.toString());
    }

    public void setTask( InternationalString task ) {
        setDescription( task.toString() );
    }
    public InternationalString getTask() {
        return new SimpleInternationalString( getDescription() );
    }
}
