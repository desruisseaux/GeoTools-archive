/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.util;

// Miscellaneous


/**
 * Monitor the progress of some lengthly operation. This interface makes no
 * assumption about the output device. It may be the standard output stream
 * (see {@link org.geotools.gui.headless.ProgressPrinter} implementation),
 * a window ({@link org.geotools.gui.swing.ProgressWindow}) or mails automatically
 * sent to some address ({@link org.geotools.gui.headless.ProgressMailer}).
 * Additionnaly, this interface provides support for non-fatal warning and
 * exception reports.
 * <br><br>
 * All <code>ProgressListener</code> implementations are multi-thread safe,  even the
 * <cite>Swing</cite> implemention. <code>ProgressListener</code> can be invoked from
 * any thread, which never need to be the <cite>Swing</cite>'s thread. This is usefull
 * for performing lenghtly operation in a background thread. Example:
 *
 * <blockquote><pre>
 * &nbsp;ProgressListener p = new {@link org.geotools.gui.headless.ProgressPrinter}();
 * &nbsp;p.setDecription("Loading data");
 * &nbsp;p.start();
 * &nbsp;for (int j=0; j&lt;1000; j++) {
 * &nbsp;    // ... some process...
 * &nbsp;    if ((j &amp; 255) == 0)
 * &nbsp;        p.progress(j/10f);
 * &nbsp;}
 * &nbsp;p.complete();
 * </pre></blockquote>
 *
 * <strong>Note:</strong> The line <code>if ((j&nbsp;&amp;&nbsp;255)&nbsp;==&nbsp;0)</code>
 * is used for reducing the amount of calls to {@link #progress} (only once every 256 steps).
 * This is not mandatory, but may speed up the process.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see org.geotools.gui.headless.ProgressPrinter
 * @see org.geotools.gui.headless.ProgressMailer
 * @see org.geotools.gui.swing.ProgressWindow
 * @see javax.swing.ProgressMonitor
 */
public interface ProgressListener {
    /**
     * Retourne le message d'�crivant l'op�ration
     * en cours. Si aucun message n'a �t� d�finie,
     * retourne <code>null</code>.
     */
    public abstract String getDescription();

    /**
     * Sp�cifie un message qui d�crit l'op�ration en cours.
     * Ce message est typiquement sp�cifi�e avant le d�but
     * de l'op�ration. Toutefois, cette m�thode peut aussi
     * �tre appel�e � tout moment pendant l'op�ration sans
     * que cela affecte le pourcentage accompli. La valeur
     * <code>null</code> signifie qu'on ne souhaite plus
     * afficher de description.
     */
    public abstract void setDescription(final String description);

    /**
     * Indique que l'op�ration a commenc�e.
     */
    public abstract void started();

    /**
     * Indique l'�tat d'avancement de l'op�ration. Le progr�s est repr�sent� par un
     * pourcentage variant de 0 � 100 inclusivement. Si la valeur sp�cifi�e est en
     * dehors de ces limites, elle sera automatiquement ramen�e entre 0 et 100.
     */
    public abstract void progress(final float percent);

    /**
     * Indique que l'op�ration est termin�e. L'indicateur visuel informant des
     * progr�s sera ramen� � 100% ou dispara�tra, selon l'impl�mentation de la
     * classe d�riv�e. Si des messages d'erreurs ou d'avertissements �taient
     * en attente, ils seront �crits.
     */
    public abstract void complete();

    /**
     * Lib�re les ressources utilis�es par cet objet. Si l'�tat d'avancement
     * �tait affich� dans une fen�tre, cette fen�tre peut �tre d�truite.
     */
    public abstract void dispose();

    /**
     * Envoie un message d'avertissement. Ce message pourra �tre envoy� vers le
     * p�riph�rique d'erreur standard, appara�tre dans une fen�tre ou �tre tout
     * simplement ignor�.
     *
     * @param source Cha�ne de caract�re d�crivant la source de l'avertissement.
     *        Il s'agira par exemple du nom du fichier dans lequel une anomalie
     *        a �t� d�tect�e. Peut �tre nul si la source n'est pas connue.
     * @param margin Texte � placer dans la marge de l'avertissement <code>warning</code>,
     *        ou <code>null</code> s'il n'y en a pas. Il s'agira le plus souvent du num�ro
     *        de ligne o� s'est produite l'erreur dans le fichier <code>source</code>.
     * @param warning Message d'avertissement � �crire.
     */
    public abstract void warningOccurred(String source, String margin, String warning);

    /**
     * Indique qu'une exception est survenue pendant le traitement de l'op�ration.
     * Cette m�thode peut afficher la trace de l'exception dans une fen�tre ou �
     * la console, d�pendemment de la classe d�riv�e.
     */
    public abstract void exceptionOccurred(final Throwable exception);
}
