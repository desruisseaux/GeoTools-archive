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
 */
package org.geotools.gui.headless;

// J2SE Input/output
import java.io.PrintWriter;
import java.text.BreakIterator;
import java.text.NumberFormat;

// Geotools dependencies
import org.geotools.resources.Arguments;
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;
import org.geotools.util.ProgressListener;


/**
 * Prints progress report of a lengtly operation to an output stream. Progress are reported
 * as percentage on a single line. This class can also prints warning, which is useful for
 * notifications without stoping the lenghtly task.
 *
 * @since 2.0
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ProgressPrinter implements ProgressListener {
    /**
     * Nom de l'op�ration en cours. Le pourcentage sera �cris � la droite de ce nom.
     */
    private String description;

    /**
     * Flot utilis� pour l'�criture de l'�tat d'avancement d'un
     * processus ainsi que pour les �critures des commentaires.
     */
    private final PrintWriter out;

    /**
     * Indique si le caract�re '\r' ram�ne au d�but de la ligne courante sur
     * ce syst�me. On supposera que ce sera le cas si le syst�me n'utilise
     * pas la paire "\r\n" pour changer de ligne (comme le system VAX-VMS).
     */
    private final boolean CR_supported;

    /**
     * Longueur maximale des lignes. L'espace utilisable sera un peu
     * moindre car quelques espaces seront laiss�s en d�but de ligne.
     */
    private final int maxLength;

    /**
     * Nombre de caract�res utilis�s lors de l'�criture de la derni�re ligne.
     * Ce champ est mis � jour par la m�thode {@link #carriageReturn} chaque
     * fois que l'on d�clare que l'on vient de terminer l'�criture d'une ligne.
     */
    private int lastLength;

    /**
     * Position � laquelle commencer � �crire le pourcentage. Cette information
     * est g�r�e automatiquement par la m�thode {@link #progress}. La valeur -1
     * signifie que ni le pourcentage ni la description n'ont encore �t� �crits.
     */
    private int percentPosition = -1;

    /**
     * Dernier pourcentage �crit. Cette information est utilis�e
     * afin d'�viter d'�crire deux fois le m�me pourcentage, ce
     * qui ralentirait inutilement le syst�me. La valeur -1 signifie
     * qu'on n'a pas encore �crit de pourcentage.
     */
    private float lastPercent = -1;

    /**
     * Format � utiliser pour �crire les pourcentages.
     */
    private NumberFormat format;

    /**
     * Objet utilis� pour couper les lignes correctements lors de l'affichage
     * de messages d'erreurs qui peuvent prendre plusieurs lignes.
     */
    private BreakIterator breaker;

    /**
     * Indique si cet objet a d�j� �crit des avertissements. Si
     * oui, on ne r��crira pas le gros titre "avertissements".
     */
    private boolean hasPrintedWarning;

    /**
     * Source du dernier message d'avertissement. Cette information est
     * conserv�e afin d'�viter de r�p�ter la source lors d'�ventuels
     * autres messages d'avertissements.
     */
    private String lastSource;

    /**
     * {@code true} if the action has been canceled.
     */
    private volatile boolean canceled;

    /**
     * Constructs a new object sending progress reports to the
     * {@linkplain java.lang.System#out standard output stream}.
     * The maximal line length is assumed 80 characters.
     */
    public ProgressPrinter() {
        this(new PrintWriter(Arguments.getWriter(System.out)));
    }

    /**
     * Constructs a new object sending progress reports to the specified stream.
     * The maximal line length is assumed 80 characters.
     */
    public ProgressPrinter(final PrintWriter out) {
        this(out, 80);
    }

    /**
     * Constructs a new object sending progress reports to the specified stream.
     *
     * @param out The output stream.
     * @param maxLength The maximal line length. This is used by {@link #warningOccurred}
     *        for splitting longer lines into many lines.
     */
    public ProgressPrinter(final PrintWriter out, final int maxLength) {
        this.out = out;
        this.maxLength = maxLength;
        final String lineSeparator = System.getProperty("line.separator", "\n");
        CR_supported=(lineSeparator!=null && lineSeparator.equals("\r\n"));
    }

    /**
     * Efface le reste de la ligne (si n�cessaire) puis repositionne le curseur au d�but
     * de la ligne. Si les retours chariot ne sont pas support�s, alors cette m�thode va
     * plut�t passer � la ligne suivante. Dans tous les cas, le curseur se trouvera au
     * d�but d'une ligne et la valeur {@code length} sera affect� au champ
     * {@link #lastLength}.
     *
     * @param length Nombre de caract�res qui ont �t� �crit jusqu'� maintenant sur cette ligne.
     *        Cette information est utilis�e pour ne mettre que le nombre d'espaces n�cessaires
     *        � la fin de la ligne.
     */
    private void carriageReturn(final int length) {
        if (CR_supported && length<maxLength) {
            for (int i=length; i<lastLength; i++)  {
                out.print(' ');
            }
            out.print('\r');
            out.flush();
        } else {
            out.println();
        }
        lastLength = length;
    }

    /**
     * Ajoute des points � la fin de la ligne jusqu'� repr�senter
     * le pourcentage sp�cifi�. Cette m�thode est utilis�e pour
     * repr�senter les progr�s sur un terminal qui ne supporte
     * pas les retours chariots.
     *
     * @param percent Pourcentage accompli de l'op�ration. Cette
     *        valeur doit obligatoirement se trouver entre 0 et
     *        100 (�a ne sera pas v�rifi�).
     */
    private void completeBar(final float percent) {
        final int end = (int) ((percent/100)*((maxLength-2)-percentPosition)); // Round toward 0.
        while (lastLength < end) {
            out.print('.');
            lastLength++;
        }
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
     * {@inheritDoc}
     */
    public synchronized void started() {
        int length = 0;
        if (description != null) {
            out.print(description);
            length=description.length();
        }
        if (CR_supported) {
            carriageReturn(length);
        }
        out.flush();
        percentPosition   = length;
        lastPercent       = -1;
        lastSource        = null;
        hasPrintedWarning = false;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void progress(float percent) {
        if (percent<0  ) percent=0;
        if (percent>100) percent=100;
        if (CR_supported) {
            /*
             * Si le p�riph�rique de sortie supporte les retours chariot,
             * on �crira l'�tat d'avancement comme un pourcentage apr�s
             * la description, comme dans "Lecture des donn�es (38%)".
             */
            if (percent != lastPercent) {
                if (format == null) {
                    format = NumberFormat.getPercentInstance();
                }
                final String text = format.format(percent/100.0);
                int length = text.length();
                percentPosition = 0;
                if (description != null) {
                    out.print(description);
                    out.print(' ');
                    length += (percentPosition=description.length())+1;
                }
                out.print('(');
                out.print(text);
                out.print(')');
                length += 2;
                carriageReturn(length);
                lastPercent=percent;
            }
        } else {
            /*
             * Si le p�riph�rique ne supporte par les retours chariots, on
             * �crira l'�tat d'avancement comme une s�rie de points plac�s
             * apr�s la description, comme dans "Lecture des donn�es......"
             */
            completeBar(percent);
            lastPercent=percent;
            out.flush();
        }
    }

    /**
     * Notifies this listener that the operation has finished. The progress indicator will
     * shows 100% or disaspears. If warning messages were pending, they will be printed now.
     */
    public synchronized void complete() {
        if (!CR_supported) {
            completeBar(100);
        }
        carriageReturn(0);
        out.flush();
    }

    /**
     * Release any resource hold by this object.
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
     * Prints a warning. The first time this method is invoked, the localized word "WARNING" will
     * be printed in the middle of a box. If a source is specified, it will be printed only if it
     * is not the same one than the source of the last warning. If a marging is specified, it will
     * be printed of the left side of the first line of the warning message.
     *
     * @param source The source of the warning, or {@code null} if none. This is typically the
     *        filename in process of being parsed.
     * @param margin Text to write on the left side of the warning message, or {@code null} if none.
     *        This is typically the line number where the error occured in the {@code source} file.
     * @param warning The warning message. If this string is longer than the maximal length
     *        specified at construction time (80 characters by default), then it will be splitted
     *        in as many lines as needed and indented according the marging width.
     */
    public synchronized void warningOccurred(final String source, String margin,
                                             final String warning)
    {
        carriageReturn(0);
        if (!hasPrintedWarning) {
            printInBox(Vocabulary.format(VocabularyKeys.WARNING));
            hasPrintedWarning=true;
        }
        if (!Utilities.equals(source, lastSource)) {
            out.println();
            out.println(source!=null ? source : Vocabulary.format(VocabularyKeys.UNTITLED));
            lastSource=source;
        }
        /*
         * Proc�de � l'�criture de l'avertissement avec (de fa�on optionnelle)
         * quelque chose dans la marge (le plus souvent un num�ro de ligne).
         */
        String prefix="    ";
        String second=prefix;
        if (margin != null) {
            margin = trim(margin);
            if (margin.length() != 0) {
                final StringBuffer buffer = new StringBuffer(prefix);
                buffer.append('(');
                buffer.append(margin);
                buffer.append(") ");
                prefix=buffer.toString();
                buffer.setLength(0);
                second=Utilities.spaces(prefix.length());
            }
        }
        int width=maxLength-prefix.length()-1;
        if (breaker == null) {
            breaker=BreakIterator.getLineInstance();
        }
        breaker.setText(warning);
        int start=breaker.first(), end=start, nextEnd;
        while ((nextEnd=breaker.next()) != BreakIterator.DONE) {
            while (nextEnd-start > width) {
                if (end <= start) {
                    end=Math.min(nextEnd, start+width);
                }
                out.print(prefix);
                out.println(warning.substring(start, end));
                prefix=second;
                start=end;
            }
            end=Math.min(nextEnd, start+width);
        }
        if (end>start) {
            out.print(prefix);
            out.println(warning.substring(start, end));
        }
        if (!CR_supported && description!=null) {
            out.print(description);
            completeBar(lastPercent);
        }
        out.flush();
    }

    /**
     * Prints an exception stack trace in a box.
     */
    public synchronized void exceptionOccurred(final Throwable exception) {
        carriageReturn(0);
        printInBox(Vocabulary.format(VocabularyKeys.EXCEPTION));
        exception.printStackTrace(out);
        hasPrintedWarning = false;
        out.flush();
    }

    /**
     * Retourne la cha�ne {@code margin} sans les
     * �ventuelles parenth�ses qu'elle pourrait avoir
     * de part et d'autre.
     */
    private static String trim(String margin) {
        margin = margin.trim();
        int lower = 0;
        int upper = margin.length();
        while (lower<upper && margin.charAt(lower+0)=='(') lower++;
        while (lower<upper && margin.charAt(upper-1)==')') upper--;
        return margin.substring(lower, upper);
    }

    /**
     * �crit dans une bo�te entour� d'ast�rix le texte sp�cifi� en argument.
     * Ce texte doit �tre sur une seule ligne et ne pas comporter de retour
     * chariot. Les dimensions de la bo�te seront automatiquement ajust�es.
     * @param text Texte � �crire (une seule ligne).
     */
    private void printInBox(String text) {
        int length = text.length();
        for (int pass=-2; pass<=2; pass++) {
            switch (Math.abs(pass)) {
                case 2: for (int j=-10; j<length; j++) out.print('*');
                        out.println();
                        break;

                case 1: out.print("**");
                        for (int j=-6; j<length; j++) out.print(' ');
                        out.println("**");
                        break;

                case 0: out.print("**   ");
                        out.print(text);
                        out.println("   **");
                        break;
            }
        }
    }
}
