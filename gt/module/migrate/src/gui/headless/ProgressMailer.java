/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2001, Institut de Recherche pour le D�veloppement
 * (C) 1999, P�ches et Oc�ans Canada
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill
 *             mailto:j.macgill@geog.leeds.ac.uk
 *
 *     FRANCE: Surveillance de l'Environnement Assist�e par Satellite
 *             Institut de Recherche pour le D�veloppement / US-Espace
 *             mailto:seasnet@teledetection.fr
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 */
package org.geotools.gui.headless;

// Java Mail
import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.geotools.resources.Utilities;
import org.geotools.resources.gui.ResourceKeys;
import org.geotools.resources.gui.Resources;
import org.geotools.util.ProgressListener;


/**
 * Informe l'utilisateur des progr�s d'une op�ration en envoyant
 * des courriers �lectroniques � intervalles r�gulier.
 *
 * @version $Id: ProgressMailer.java,v 1.2 2003/05/13 11:01:39 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class ProgressMailer implements ProgressListener {
    /**
     * Nom de l'op�ration en cours. Le pourcentage
     * sera �cris � la droite de ce nom.
     */
    private String description;

    /**
     * Langue � utiliser pour le formattage.
     */
    private final Locale locale;

    /**
     * Session � utiliser pour
     * envoyer des courriels.
     */
    private final Session session;

    /**
     * Addresse des personnes � qui envoyer
     * un rapport sur les progr�s.
     */
    private final Address[] address;

    /**
     * Laps de temps entre deux courriers �lectroniques
     * informant des progr�s. On attendra que ce laps de
     * temps soit �coul�s avant d'envoyer un nouveau courriel.
     */
    private long timeInterval = 3*60*60*1000L;

    /**
     * Date et heure � laquelle envoyer le prochain courriel.
     */
    private long nextTime;

    /**
     * Construit un objet qui informera des
     * progr�s en envoyant des courriels.
     *
     * @param  host Nom du serveur � utiliser pour envoyer des courriels.
     * @param  address Adresse � laquelle envoyer les messages.
     * @throws AddressException si l'adresse sp�cifi�e n'est pas dans un format valide.
     */
    public ProgressMailer(final String host, final String address) throws AddressException {
        this(Session.getDefaultInstance(properties(host)), new InternetAddress[] {
                new InternetAddress(address)});
    }

    /**
     * Construit un objet qui informera des
     * progr�s en envoyant des courriels.
     *
     * @param session Session � utiliser pour envoyer des courriels.
     */
    public ProgressMailer(final Session session, final Address[] address) {
        this.session = session;
        this.address = address;
        this.locale  = Locale.getDefault();
        nextTime = System.currentTimeMillis();
    }

    /**
     * Retourne un ensemble de propri�t�s
     * n�cessaires pour ouvrir une session.
     *
     * @param host Nom du serveur � utiliser pour envoyer des courriels.
     */
    private static final Properties properties(final String host) {
        final Properties props = new Properties();
        props.setProperty("mail.smtp.host", host);
        return props;
    }

    /**
     * Retourne le laps de temps minimal entre deux courriers �lectroniques
     * informant des progr�s. On attendra que ce laps de temps soit �coul�s
     * avant d'envoyer un nouveau courriel.
     *
     * @return Intervalle de temps en millisecondes.
     */
    public long getTimeInterval() {
        return timeInterval;
    }

    /**
     * Sp�cifie le laps de temps minimal entre deux courriers �lectroniques
     * informant des progr�s. On attendra que ce laps de temps soit �coul�s
     * avant d'envoyer un nouveau courriel. Par d�faut, un courriel n'est
     * envoy� qu'une fois tous les heures.
     *
     * @param interval Intervalle de temps en millisecondes.
     */
    public synchronized void setTimeInterval(final long interval) {
        this.timeInterval = interval;
    }

    /**
     * Retourne le message d'�crivant l'op�ration
     * en cours. Si aucun message n'a �t� d�finie,
     * retourne <code>null</code>.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sp�cifie un message qui d�crit l'op�ration en cours.
     * Ce message est typiquement sp�cifi�e avant le d�but
     * de l'op�ration. Toutefois, cette m�thode peut aussi
     * �tre appel�e � tout moment pendant l'op�ration sans
     * que cela affecte le pourcentage accompli. La valeur
     * <code>null</code> signifie qu'on ne souhaite plus
     * afficher de description.
     */
    public synchronized void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Envoie le message sp�cifi� par courrier �lectronique.
     *
     * @param method Nom de la m�thode qui appelle celle-ci.
     *        Cette information est utilis�e pour produire
     *        un message d'erreur en cas d'�chec.
     * @param subjectKey Cl� du sujet: {@link ResourceKeys#PROGRESS},
     *        {@link ResourceKeys#WARNING} ou {@link ResourceKeys#EXCEPTION}.
     * @param messageText Message � envoyer par courriel.
     */
    private void send(final String method, final int subjectKey, final String messageText) {
        try {
            final Message message = new MimeMessage(session);
            message.setFrom();
            message.setRecipients(Message.RecipientType.TO, address);
            message.setSubject(Resources.format(subjectKey));
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
     * @param method Nom de la m�thode qui appelle celle-ci.
     *        Cette information est utilis�e pour produire
     *        un message d'erreur en cas d'�chec.
     * @param percent Pourcentage effectu� (entre 0 et 100).
     */
    private void send(final String method, final float percent) {
        final Runtime      system = Runtime.getRuntime();
        final float   MEMORY_UNIT = (1024f*1024f);
        final float    freeMemory = system.freeMemory()  / MEMORY_UNIT;
        final float   totalMemory = system.totalMemory() / MEMORY_UNIT;
        final Resources resources = Resources.getResources(null);
        final NumberFormat format = NumberFormat.getPercentInstance(locale);
        final StringBuffer buffer = new StringBuffer(description!=null ?
                description : resources.getString(ResourceKeys.PROGRESSION));
        buffer.append(": "); format.format(percent/100, buffer, new FieldPosition(0));
        buffer.append('\n');
        buffer.append(resources.getString(ResourceKeys.MEMORY_HEAP_SIZE_$1,
                                          new Float(totalMemory)));
        buffer.append('\n');
        buffer.append(resources.getString(ResourceKeys.MEMORY_HEAP_USAGE_$1,
                                          new Float(1-freeMemory/totalMemory)));
        buffer.append('\n');
        send(method, ResourceKeys.PROGRESSION, buffer.toString());
    }

    /**
     * Envoie un courrier �lectronique indiquant
     * que l'op�ration vient de commencer.
     */
    public synchronized void started() {
        send("started", 0);
    }

    /**
     * Envoie un courrier �lectronique informant des progr�s de l'op�ration.
     * Cette information ne sera pas n�cessairement prise en compte. Cette
     * m�thode n'envoie des rapport qu'� des intervalles de temps assez espac�s
     * (par d�faut 3 heure) afin de ne pas innonder l'utilisateur de courriels.
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
     * Envoie un courrier �lectronique indiquant
     * que l'op�ration vient de se terminer.
     */
    public synchronized void complete() {
        send("complete", 100);
    }

    /**
     * Lib�re les ressources utilis�es par cet objet.
     * L'impl�mentation par d�faut ne fait rien.
     */
    public void dispose() {
    }

    /**
     * Envoie un message d'avertissement. Ce message
     * sera envoy�e par courrier �lectronique.
     *
     * @param source Cha�ne de caract�re d�crivant la source de l'avertissement.
     *        Il s'agira par exemple du nom du fichier dans lequel une anomalie
     *        a �t� d�tect�e. Peut �tre nul si la source n'est pas connue.
     * @param margin Texte � placer dans la marge de l'avertissement <code>warning</code>,
     *        ou <code>null</code> s'il n'y en a pas. Il s'agira le plus souvent du num�ro
     *        de ligne o� s'est produite l'erreur dans le fichier <code>source</code>.
     * @param warning Message d'avertissement � �crire.
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
        send("warningOccurred", ResourceKeys.WARNING, buffer.toString());
    }

    /**
     * Indique qu'une exception est survenue pendant le traitement de l'op�ration.
     * L'impl�mentation par d�faut envoie la trace de l'exception par courrier
     * �lectronique.
     */
    public synchronized void exceptionOccurred(final Throwable exception) {
        final CharArrayWriter buffer = new CharArrayWriter();
        exception.printStackTrace(new PrintWriter(buffer));
        send("exceptionOccurred", ResourceKeys.EXCEPTION, buffer.toString());
    }
}
