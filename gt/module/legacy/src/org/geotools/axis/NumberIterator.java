/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2000, Institut de Recherche pour le D�veloppement
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
package org.geotools.axis;

// Miscellaneous
import java.text.NumberFormat;
import java.util.Locale;

import org.geotools.resources.XMath;


/**
 * It�rateur balayant les barres et �tiquettes de graduation d'un axe.
 * Cet it�rateur retourne les positions des graduations � partir de la
 * valeur minimale jusqu'� la valeur maximale.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
class NumberIterator implements TickIterator {
    /**
     * Petite quantit� utilis�e pour �viter les
     * erreurs d'arrondissements dans les comparaisons.
     */
    private static final double EPS = 1E-8;

    /**
     * Valeur de la premi�re graduation principale.
     * Cette valeur est fix�e par {@link #init}.
     */
    private double minimum;

    /**
     * Valeur limite des graduations. La derni�re
     * graduation n'aura pas n�cessairement cette
     * valeur. Cette valeur est fix�e par {@link #init}.
     */
    private double maximum;

    /**
     * Intervalle entre deux graduations principales.
     * Cette valeur est fix�e par {@link #init}.
     */
    private double increment;

    /**
     * Longueur de l'axe (en points). Cette information
     * est conserv�e afin d'�viter de refaire toute la
     * proc�dure {@link #init} si les param�tres n'ont
     * pas chang�s.
     */
    private float visualLength;

    /**
     * Espace � laisser (en points) entre les graduations principales.
     * Cette information est conserv�e afin d'�viter de refaire toute
     * la proc�dure {@link #init} si les param�tres n'ont pas chang�s.
     */
    private float visualTickSpacing;

    /**
     * Nombre de sous-divisions dans une graduation principale.
     * Cette valeur est fix�e par {@link #init}.
     */
    private int subTickCount;

    /**
     * Index de la premi�re sous-graduation
     * dans la premi�re graduation principale.
     * Cette valeur est fix�e par {@link #init}.
     */
    private int subTickStart;

    /**
     * Index de la graduation principale en cours de tra�age.
     * Cette valeur commence � 0 et sera modifi�e � chaque
     * appel � {@link #next}.
     */
    private int tickIndex;

    /**
     * Index de la graduation secondaire en cours de tra�age. Cette
     * valeur va de 0 inclusivement jusqu'� {@link #subTickCount}
     * exclusivement. Elle sera modifi�e � chaque appel � {@link #next}.
     */
    private int subTickIndex;

    /**
     * Valeur de la graduation principale ou secondaire actuelle.
     * Cette valeur sera modifi�e � chaque appel � {@link #next}.
     */
    private double value;

    /**
     * Format � utiliser pour �crire les �tiquettes de graduation. Ce format ne
     * sera construit que la premi�re fois o� {@link #currentLabel} sera appel�e.
     */
    private transient NumberFormat format;

    /**
     * Indique si {@link #format} est valide. Le format peut
     * devenir invalide si {@link #init} a �t� appel�e. Dans
     * ce cas, il peut falloir changer le nombre de chiffres
     * apr�s la virgule qu'il �crit.
     */
    private transient boolean formatValid;

    /**
     * Conventions � utiliser pour
     * le formatage des nombres.
     */
    private Locale locale;

    /**
     * Construit un it�rateur par d�faut. La m�thode {@link #init}
     * <u>doit</u> �tre appel�e avant que cet it�rateur ne soit
     * utilisable.
     *
     * @param locale Conventions � utiliser pour le formatage des nombres.
     */
    protected NumberIterator(final Locale locale) {
        this.locale = locale;
    }

    /**
     * Initialise l'it�rateur.
     *
     * @param minimum           Valeur minimale de la premi�re graduation.
     * @param maximum           Valeur limite des graduations. La derni�re
     *                          graduation n'aura pas n�cessairement cette valeur.
     * @param visualLength      Longueur visuelle de l'axe sur laquelle tracer la graduation.
     *                          Cette longueur doit �tre exprim�e en pixels ou en points.
     * @param visualTickSpacing Espace � laisser visuellement entre deux marques de graduation.
     *                          Cet espace doit �tre exprim� en pixels ou en points (1/72 de pouce).
     */
    protected void init(      double minimum,
                        final double maximum,
                        final float  visualLength,
                        final float  visualTickSpacing)
    {
        if (minimum           == this.minimum      &&
            maximum           == this.maximum      &&
            visualLength      == this.visualLength &&
            visualTickSpacing == this.visualTickSpacing)
        {
            rewind();
            return;
        }
        AbstractGraduation.ensureFinite ("minimum",           minimum);
        AbstractGraduation.ensureFinite ("maximum",           maximum);
        AbstractGraduation.ensureFinite ("visualLength",      visualLength); // May be 0.
        AbstractGraduation.ensureNonNull("visualTickSpacing", visualTickSpacing);
        this.visualLength      = visualLength;
        this.visualTickSpacing = visualTickSpacing;
        /*
         * Estime le pas qui donnera au moins l'espacement sp�cifi� entre
         * chaque graduation.  D�termine ensuite si ce pas est de l'ordre
         * des dizaines, centaines ou autre et on ram�nera temporairement
         * ce pas � l'ordre des unit�es.
         */
        double increment = (maximum-minimum)*(visualTickSpacing/visualLength);
        final double factor = XMath.pow10((int)Math.floor(XMath.log10(increment)));
        increment /= factor;
        if (Double.isNaN(increment) || Double.isInfinite(increment) || increment==0) {
            this.minimum      = minimum;
            this.maximum      = maximum;
            this.increment    = Double.NaN;
            this.value        = Double.NaN;
            this.tickIndex    = 0;
            this.subTickIndex = 0;
            this.subTickStart = 0;
            this.subTickCount = 1;
            this.formatValid  = false;
            return;
        }
        /*
         * Le pas se trouve maintenant entre 1 et 10. On l'ajuste maintenant
         * pour lui donner des valeurs qui ne sont habituellement pas trop
         * difficiles � lire.
         */
        final int subTickCount;
        if      (increment <= 1.0) {increment = 1.0; subTickCount=5;}
        else if (increment <= 2.0) {increment = 2.0; subTickCount=4;}
        else if (increment <= 2.5) {increment = 2.5; subTickCount=5;}
        else if (increment <= 4.0) {increment = 4.0; subTickCount=4;}
        else if (increment <= 5.0) {increment = 5.0; subTickCount=5;}
        else                       {increment =10.0; subTickCount=5;}
        increment = increment*factor;
        /*
         * Arrondie maintenant le minimum sur une des graduations principales.
         * D�termine ensuite combien de graduations secondaires il faut sauter
         * sur la premi�re graduation principale.
         */
        final double tmp = minimum;
        minimum          = Math.floor(minimum/increment+EPS)*increment;
        int subTickStart = (int)Math.ceil((tmp-minimum-EPS)*(subTickCount/increment));
        final int  extra = subTickStart / subTickCount;
        minimum      += extra*increment;
        subTickStart -= extra*subTickCount;

        this.increment    = increment;
        this.subTickCount = subTickCount;
        this.maximum      = maximum + Math.abs(maximum*EPS);
        this.minimum      = minimum;
        this.subTickStart = subTickStart;
        this.subTickIndex = subTickStart;
        this.tickIndex    = 0;
        this.value        = minimum + increment*(subTickStart/(double)subTickCount);
        this.formatValid  = false;
    }

    /**
     * Indique s'il reste des graduations � retourner. Cette m�thode retourne <code>true</code>
     * tant que {@link #currentValue} ou {@link #currentLabel} peuvent �tre appel�es.
     */
    public boolean hasNext() {
        return value <= maximum;
    }

    /**
     * Indique si la graduation courante est une graduation majeure.
     *
     * @return <code>true</code> si la graduation courante est une
     *         graduation majeure, ou <code>false</code> si elle
     *         est une graduation mineure.
     */
    public boolean isMajorTick() {
        return subTickIndex == 0;
    }

    /**
     * Returns the position where to draw the current tick.  The position is scaled
     * from the graduation's minimum to maximum.    This is usually the same number
     * than {@link #currentValue}. The mean exception is for logarithmic graduation,
     * in which the tick position is not proportional to the tick value.
     */
    public double currentPosition() {
        return value;
    }

    /**
     * Retourne la valeur de la graduation courante. Cette m�thode
     * peut �tre appel�e pour une graduation majeure ou mineure.
     */
    public double currentValue() {
        return value;
    }

    /**
     * Retourne l'�tiquette de la graduation courante. On n'appele g�n�ralement
     * cette m�thode que pour les graduations majeures, mais elle peut aussi
     * �tre appel�e pour les graduations mineures. Cette m�thode retourne
     * <code>null</code> s'il n'y a pas d'�tiquette pour la graduation courante.
     */
    public String currentLabel() {
        if (!formatValid) {
            if (format == null) {
                format = NumberFormat.getNumberInstance(locale);
            }
            /*
             * Trouve le nombre de chiffres apr�s la virgule n�cessaires pour repr�senter les
             * �tiquettes de la graduation. Impose une limite de six chiffres, limite qui pourrait
             * �tre atteinte notamment avec les nombres p�riodiques (par exemple des intervalles
             * de temps exprim�s en fractions de jours).
             */
            int precision;
            double step = Math.abs(increment);
            for (precision=0; precision<6; precision++) {
                final double check = Math.rint(step*1E+4) % 1E+4;
                if (!(check > step*EPS)) { // 'step' may be NaN
                    break;
                }
                step *= 10;
            }
            format.setMinimumFractionDigits(precision);
            format.setMaximumFractionDigits(precision);
            formatValid = true;
        }
        return format.format(currentValue());
    }

    /**
     * Passe � la graduation suivante.
     */
    public void next() {
        if (++subTickIndex >= subTickCount) {
            subTickIndex = 0;
            tickIndex++;
        }
        // On n'utilise pas "+=" afin d'�viter les erreurs d'arrondissements.
        value = minimum + increment * (tickIndex + subTickIndex / (double)subTickCount);
    }

    /**
     * Passe directement � la graduation majeure suivante.
     */
    public void nextMajor() {
        subTickIndex = 0;
        value = minimum + increment * (++tickIndex);
    }

    /**
     * Replace l'it�rateur sur la premi�re graduation.
     */
    public void rewind() {
        tickIndex    = 0;
        subTickIndex = subTickStart;
        value        = minimum + increment*(subTickStart/(double)subTickCount);
    }

    /**
     * Retourne les conventions � utiliser pour
     * �crire les �tiquettes de graduation.
     */
    public final Locale getLocale() {
        return locale;
    }

    /**
     * Modifie les conventions � utiliser pour
     * �crire les �tiquettes de graduation.
     */
    public final void setLocale(final Locale locale) {
        if (!locale.equals(this.locale)) {
            this.locale = locale;
            this.format = null;
            formatValid = false;
        }
    }
}
