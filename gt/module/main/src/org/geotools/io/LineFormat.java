/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le D�veloppement
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
package org.geotools.io;

// Text format
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Locale;

import org.geotools.resources.ClassChanger;
import org.geotools.resources.XArray;
import org.geotools.resources.rsc.ResourceKeys;
import org.geotools.resources.rsc.Resources;


/**
 * Parse a line of text data. This class is mostly used for parsing lines in a matrix or a table.
 * Each column may contains numbers, dates, or other objects parseable by some {@link Format}
 * implementations. The example below reads dates in the first column and numbers in all
 * remaining columns.
 *
 * <blockquote><pre>
 * final LineParser parser=new LineFormat(new Format[] {
 *     {@link java.text.DateFormat#getDateTimeInstance()},
 *     {@link java.text.NumberFormat#getNumberInstance()}
 * });
 * </pre></blockquote>
 *
 * <code>LineFormat</code> may be used for reading a matrix with an unknow number of columns,
 * while requiring that all lines have the same number of columns. The example below gets the
 * number of columns while reading the first line, and ensure that all subsequent lines have
 * the same number of columns. If one line violate this condition, then a {@link ParseException}
 * will be thrown. The check if performed by the <code>getValues(double[])</code> method when
 * the <code>data</code> array is non-nul.
 *
 * <blockquote><pre>
 * &nbsp;double[] data=null;
 * &nbsp;final {@link java.io.BufferedReader} in = new {@link java.io.BufferedReader}(new {@link java.io.FileReader}("MATRIX.TXT"));
 * &nbsp;for ({@link String} line; (line=in.readLine())!=null;) {
 * &nbsp;    parser.setLine(line);
 * &nbsp;    data = parser.getValues(data);
 * &nbsp;    // ... process 'data' here ...
 * &nbsp;});
 * </pre></blockquote>
 *
 * This code can work as well with dates instead of numbers. In this case, the values returned
 * will be microseconds ellapsed since January 1st, 1970.
 * <br><br>
 * A {@link ParseException} may be thrown because a string can't be parsed, because an object
 * can't be converted into a number or because a line don't have the expected number of columns.
 * In all case, it is possible to gets the index of the first problem found using
 * {@link ParseException#getErrorOffset}.
 *
 * @todo This class is intented to be a subclass of {@link Format}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class LineFormat {
    /**
     * Nombre de donn�es valides dans le tableau {@link #data}.
     * Il s'agit du nombre de donn�es lues lors du dernier appel
     * de la m�thode {@link #setLine(String)}.
     */
    private int count;

    /**
     * Donn�es lus lors du dernier appel de la m�thode {@link #setLine(String)}.
     * Ces donn�es seront restitu�s par des appels � {@link #getValues(float[])}.
     */
    private Object[] data;

    /**
     * Tableau de formats � utiliser. Chaque format de ce tableau correspond � une
     * colonne. Par exemple la donn�e <code>data[4]</code> aura �t� lu avec le format
     * <code>format[4]</code>. Il n'est toutefois pas obligatoire qu'il y ait autant
     * de format que de colonnes. Si {@link #data} et plus long que {@link #format},
     * alors le dernier format sera r�utilis� pour toutes les colonnes restantes.
     */
    private final Format[] format;

    /**
     * Objet {@link ParsePosition} utilis� lors de la lecture pour sp�cifier quelle
     * partie de la cha�ne doit �tre interpr�t�e.
     */
    private final ParsePosition position=new ParsePosition(0);

    /**
     * Index du caract�re auquel commen�aient les �l�ments qui ont �t� lus. Par exemple
     * <code>index[0]</code> contient l'index du premier caract�re qui a �t� lu pour la
     * donn�e <code>data[0]</code>, et ainsi de suite. Ce tableau doit <u>toujours</u>
     * avoir une longueur de <code>{@link #data}.length + 1</code>. Le dernier �l�ment
     * de ce tableau sera la longueur de la ligne.
     */
    private int[] limits;

    /**
     * Derni�re ligne de texte � avoir �t� sp�cifi�e � la m�thode {@link #setLine(String)}.
     */
    private String line;

    /**
     * Constructs a new line parser for the default locale.
     */
    public LineFormat() {
        this(NumberFormat.getNumberInstance());
    }

    /**
     * Construit un objet qui lira des nombres �crits selon les convention du
     * pays sp�cifi�. Par exemple on peut sp�cifier {@link Locale#US} pour lire
     * des nombres qui utilisent le point comme s�parateur d�cimal.
     */
    public LineFormat(final Locale locale) {
        this(NumberFormat.getNumberInstance(locale));
    }

    /**
     * Construit un objet qui lira des dates, des nombres ou
     * tous autres objets �crits selon le format sp�cifi�.
     *
     * @param format Format � utiliser.
     * @throws NullPointerException si <code>format</code> est nul.
     */
    public LineFormat(final Format format) throws NullPointerException {
        this.data   = new Object[16];
        this.limits = new int   [16+1];
        this.format = new Format[] {format};
        if (format==null) {
            final Integer one=new Integer(1);
            throw new NullPointerException(Resources.format(ResourceKeys.ERROR_NULL_FORMAT_$2, one, one));
        }
    }

    /**
     * Construit un objet qui lira des dates, des nombres ou tous autres objets �crits selon
     * les formats sp�cifi�s. Le tableau de format sp�cifi� en argument donne les formats
     * attendus des premi�res colonnes. Par exemple <code>formats[0]</code> donne le format
     * de la premi�re colonne, <code>formats[1]</code> donne le format de la deuxi�me colonne,
     * etc. S'il y a plus de colonnes que de formats sp�cifi�s, le dernier format sera r�utilis�
     * pour toutes les colonnes restantes.
     *
     * @param formats Tableau de formats � utiliser.
     * @throws NullPointerException si <code>formats</code> est nul ou si si un des formats est nul.
     */
    public LineFormat(final Format[] formats) throws NullPointerException {
        this.data   = new Object[formats.length];
        this.format = new Format[formats.length];
        this.limits = new int   [formats.length+1];
        System.arraycopy(formats, 0, format, 0, formats.length);
        for (int i=0; i<format.length; i++) {
            if (format[i]==null) {
                throw new NullPointerException(Resources.format(ResourceKeys.ERROR_NULL_FORMAT_$2, new Integer(i+1), new Integer(format.length)));
            }
        }
    }

    /**
     * Oublie toute les donn�es m�moris�es. Le prochain appel
     * de la m�thode {@link #getValueCount} retournera 0.
     */
    public synchronized void clear() {
        line=null;
        Arrays.fill(data, null);
        count=0;
    }

    /**
     * D�fini la prochaine ligne qui sera � interpr�ter.
     *
     * @param  line Ligne � interpr�ter.
     * @return Nombre d'�l�ments trouv�s dans la ligne. Cette information peut
     *         aussi �tre obtenue par un appel � {@link #getValueCount}.
     * @throws ParseException si des �l�ments n'ont pas pu �tre interpr�t�s.
     */
    public int setLine(final String line) throws ParseException {
        return setLine(line, 0, line.length());
    }

    /**
     * D�fini la prochaine ligne qui sera � interpr�ter.
     *
     * @param  line  Ligne � interpr�ter.
     * @param  lower Index du premier caract�re de <code>line</code> � prendre en compte.
     * @param  upper Index suivant celui du dernier caract�re de <code>line</code> � prendre en compte.
     * @return Nombre d'�l�ments trouv�s dans la ligne. Cette information peut
     *         aussi �tre obtenue par un appel � {@link #getValueCount}.
     * @throws ParseException si des �l�ments n'ont pas pu �tre interpr�t�s.
     */
    public synchronized int setLine(final String line, int lower, final int upper)
        throws ParseException
    {
        /*
         * Retient la ligne que l'utilisateur nous demande
         * de lire et oublie toutes les anciennes valeurs.
         */
        this.line=line;
        Arrays.fill(data, null);
        count=0;
        /*
         * Proc�de au balayage de toutes les valeurs qui se trouvent sur la ligne sp�cifi�e.
         * Le balayage s'arr�tera lorsque <code>lower</code> aura atteint <code>upper</code>.
         */
  load: while (true) {
            while (true) {
                if (lower >= upper) {
                    break load;
                }
                if (!Character.isWhitespace(line.charAt(lower))) break;
                lower++;
            }
            /*
             * Proc�de � la lecture de la donn�e. Si la lecture �choue, on produira un message d'erreur
             * qui appara�tra �ventuellement en HTML afin de pouvoir souligner la partie fautive.
             */
            position.setIndex(lower);
            final Object datum=format[Math.min(count, format.length-1)].parseObject(line, position);
            final int next=position.getIndex();
            if (datum==null || next<=lower) {
                final int error = position.getErrorIndex();
                int end = error;
                while (end<upper && !Character.isWhitespace(line.charAt(end))) end++;
                throw new ParseException(Resources.format(ResourceKeys.ERROR_PARSE_EXCEPTION_$2, line.substring(lower, end).trim(), line.substring(error, Math.min(error+1, end))), error);
            }
            /*
             * M�morise la nouvelle donn�e, en agrandissant
             * l'espace r�serv�e en m�moire si c'est n�cessaire.
             */
            if (count >= data.length) {
                data   = XArray.resize(data,   count+Math.min(count, 256));
                limits = XArray.resize(limits, data.length+1);
            }
            limits[count] = lower;
            data[count++] = datum;
            lower = next;
        }
        limits[count]=lower;
        return count;
    }

    /**
     * Retourne le nombre de donn�es trouv�es dans la derni�re
     * ligne � avoir �t� sp�cifi�e � {@link #setLine(String)}.
     */
    public synchronized int getValueCount() {
        return count;
    }

    /**
     * Modifie ou ajoute une valeur. L'index de la valeur doit �tre compris de
     * 0 � {@link #getValueCount} inclusivement. Si l'index est �gal au nombre
     * de donn�es retourn� par {@link #getValueCount}, alors <code>value</code>
     * sera ajout� � la fin des donn�es existante et une colonne sera ajout�e.
     *
     * @param  index Index de la donn�e � modifier ou ajouter.
     * @param  value Nouvelle valeur � retenir.
     * @throws ArrayIndexOutOfBoundsException si l'index est en dehors de la plage permise.
     */
    public synchronized void setValue(final int index, final Object value)
        throws ArrayIndexOutOfBoundsException
    {
        if (index > count) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        if (value == null) {
            throw new NullPointerException();
        }
        if (index == count) {
            if (index == data.length) {
                data = XArray.resize(data, index+Math.min(index, 256));
            }
            count++;
        }
        data[index]=value;
    }

    /**
     * Retourne la valeur � l'index sp�cifi�. Cet index doit �tre
     * compris de 0 inclusivement jusqu'� {@link #getValueCount}
     * exclusivement.
     *
     * @param  index Index de la donn�e demand�e.
     * @return Valeur � l'index demand�.
     * @throws ArrayIndexOutOfBoundsException si l'index est en dehors de la plage permise.
     */
    public synchronized Object getValue(final int index) throws ArrayIndexOutOfBoundsException {
        if (index < count) {
            return data[index];
        }
        throw new ArrayIndexOutOfBoundsException(index);
    }

    /**
     * Retourne sous forme de nombre la valeur � l'index <code>index</code>.
     *
     * @param  index Index de la valeur demand�e.
     * @return La valeur demand�e sous forme d'objet {@link Number}.
     * @throws ParseException si la valeur n'est pas convertible en objet {@link Number}.
     */
    private Number getNumber(final int index) throws ParseException {
        Exception error = null;
        if (data[index] instanceof Comparable) {
            try
            {
                return ClassChanger.toNumber((Comparable)data[index]);
            } catch (ClassNotFoundException exception) {
                error = exception;
            }
        }
        ParseException exception = new ParseException(Resources.format(
                                       ResourceKeys.ERROR_UNPARSABLE_NUMBER_$1,
                                       data[index]), limits[index]);
        if (error!=null) {
            exception.initCause(error);
        }
        throw exception;
    }

    /**
     * Copie vers le tableau sp�cifi� les valeurs lues dans la ligne. Cette m�thode peut �tre
     * appel�e apr�s {@link #setLine(String)} pour copier vers <code>array</code> les valeurs
     * qui ont �t� lues. Si <code>array</code> est nul, cette m�thode cr�era et retournera un
     * tableau qui aura la longueur tout juste suffisante pour contenir toutes les donn�es.
     * Mais si <code>array</code> est non-nul, alors cette m�thode exigera que la longueur du
     * tableau soit �gale au nombre de donn�es.
     *
     * @param  array Tableau dans lequel copier les valeurs.
     * @return <code>array</code> s'il �tait non-nul, ou un tableau nouvellement
     *         cr�� avec la bonne longueur si <code>array</code> �tait nul.
     * @throws ParseException si <code>array</code> �tait non-nul et que sa longueur
     *         ne correspond pas au nombre de donn�es lues, ou si une des donn�es lues
     *         n'est pas convertible en nombre.
     */
    public synchronized double[] getValues(double[] array) throws ParseException {
        if (array!=null) {
            checkLength(array.length);
        } else {
            array=new double[count];
        }
        for (int i=0; i<count; i++) {
            array[i] = getNumber(i).doubleValue();
        }
        return array;
    }

    /**
     * Copie vers le tableau sp�cifi� les valeurs lues dans la ligne. Cette m�thode peut �tre
     * appel�e apr�s {@link #setLine(String)} pour copier vers <code>array</code> les valeurs
     * qui ont �t� lues. Si <code>array</code> est nul, cette m�thode cr�era et retournera un
     * tableau qui aura la longueur tout juste suffisante pour contenir toutes les donn�es.
     * Mais si <code>array</code> est non-nul, alors cette m�thode exigera que la longueur du
     * tableau soit �gale au nombre de donn�es.
     *
     * @param  array Tableau dans lequel copier les valeurs.
     * @return <code>array</code> s'il �tait non-nul, ou un tableau nouvellement
     *         cr�� avec la bonne longueur si <code>array</code> �tait nul.
     * @throws ParseException si <code>array</code> �tait non-nul et que sa longueur
     *         ne correspond pas au nombre de donn�es lues, ou si une des donn�es lues
     *         n'est pas convertible en nombre.
     */
    public synchronized float[] getValues(float[] array) throws ParseException {
        if (array!=null) {
            checkLength(array.length);
        } else {
            array=new float[count];
        }
        for (int i=0; i<count; i++) {
            array[i]=getNumber(i).floatValue();
        }
        return array;
    }

    /**
     * Copie vers le tableau sp�cifi� les valeurs lues dans la ligne. Cette m�thode peut �tre
     * appel�e apr�s {@link #setLine(String)} pour copier vers <code>array</code> les valeurs
     * qui ont �t� lues. Si <code>array</code> est nul, cette m�thode cr�era et retournera un
     * tableau qui aura la longueur tout juste suffisante pour contenir toutes les donn�es.
     * Mais si <code>array</code> est non-nul, alors cette m�thode exigera que la longueur du
     * tableau soit �gale au nombre de donn�es.
     *
     * @param  array Tableau dans lequel copier les valeurs.
     * @return <code>array</code> s'il �tait non-nul, ou un tableau nouvellement
     *         cr�� avec la bonne longueur si <code>array</code> �tait nul.
     * @throws ParseException si <code>array</code> �tait non-nul et que sa longueur
     *         ne correspond pas au nombre de donn�es lues, ou si une des donn�es lues
     *         n'est pas convertible en nombre entier de type <code>long</code>.
     */
    public synchronized long[] getValues(long[] array) throws ParseException {
        if (array!=null) {
            checkLength(array.length);
        } else {
            array=new long[count];
        }
        for (int i=0; i<count; i++) {
            final Number n = getNumber(i);
            if ((array[i]=n.longValue()) != n.doubleValue()) {
                throw notAnInteger(i);
            }
        }
        return array;
    }

    /**
     * Copie vers le tableau sp�cifi� les valeurs lues dans la ligne. Cette m�thode peut �tre
     * appel�e apr�s {@link #setLine(String)} pour copier vers <code>array</code> les valeurs
     * qui ont �t� lues. Si <code>array</code> est nul, cette m�thode cr�era et retournera un
     * tableau qui aura la longueur tout juste suffisante pour contenir toutes les donn�es.
     * Mais si <code>array</code> est non-nul, alors cette m�thode exigera que la longueur du
     * tableau soit �gale au nombre de donn�es.
     *
     * @param  array Tableau dans lequel copier les valeurs.
     * @return <code>array</code> s'il �tait non-nul, ou un tableau nouvellement
     *         cr�� avec la bonne longueur si <code>array</code> �tait nul.
     * @throws ParseException si <code>array</code> �tait non-nul et que sa longueur
     *         ne correspond pas au nombre de donn�es lues, ou si une des donn�es lues
     *         n'est pas convertible en nombre entier de type <code>int</code>.
     */
    public synchronized int[] getValues(int[] array) throws ParseException {
        if (array!=null) {
            checkLength(array.length);
        } else {
            array=new int[count];
        }
        for (int i=0; i<count; i++) {
            final Number n = getNumber(i);
            if ((array[i]=n.intValue()) != n.doubleValue()) {
                throw notAnInteger(i);
            }
        }
        return array;
    }

    /**
     * Copie vers le tableau sp�cifi� les valeurs lues dans la ligne. Cette m�thode peut �tre
     * appel�e apr�s {@link #setLine(String)} pour copier vers <code>array</code> les valeurs
     * qui ont �t� lues. Si <code>array</code> est nul, cette m�thode cr�era et retournera un
     * tableau qui aura la longueur tout juste suffisante pour contenir toutes les donn�es.
     * Mais si <code>array</code> est non-nul, alors cette m�thode exigera que la longueur du
     * tableau soit �gale au nombre de donn�es.
     *
     * @param  array Tableau dans lequel copier les valeurs.
     * @return <code>array</code> s'il �tait non-nul, ou un tableau nouvellement
     *         cr�� avec la bonne longueur si <code>array</code> �tait nul.
     * @throws ParseException si <code>array</code> �tait non-nul et que sa longueur
     *         ne correspond pas au nombre de donn�es lues, ou si une des donn�es lues
     *         n'est pas convertible en nombre entier de type <code>short</code>.
     */
    public synchronized short[] getValues(short[] array) throws ParseException {
        if (array!=null) {
            checkLength(array.length);
        } else {
            array=new short[count];
        }
        for (int i=0; i<count; i++) {
            final Number n = getNumber(i);
            if ((array[i]=n.shortValue()) != n.doubleValue()) {
                throw notAnInteger(i);
            }
        }
        return array;
    }

    /**
     * Copie vers le tableau sp�cifi� les valeurs lues dans la ligne. Cette m�thode peut �tre
     * appel�e apr�s {@link #setLine(String)} pour copier vers <code>array</code> les valeurs
     * qui ont �t� lues. Si <code>array</code> est nul, cette m�thode cr�era et retournera un
     * tableau qui aura la longueur tout juste suffisante pour contenir toutes les donn�es.
     * Mais si <code>array</code> est non-nul, alors cette m�thode exigera que la longueur du
     * tableau soit �gale au nombre de donn�es.
     *
     * @param  array Tableau dans lequel copier les valeurs.
     * @return <code>array</code> s'il �tait non-nul, ou un tableau nouvellement
     *         cr�� avec la bonne longueur si <code>array</code> �tait nul.
     * @throws ParseException si <code>array</code> �tait non-nul et que sa longueur
     *         ne correspond pas au nombre de donn�es lues, ou si une des donn�es lues
     *         n'est pas convertible en nombre entier de type <code>byte</code>.
     */
    public synchronized byte[] getValues(byte[] array) throws ParseException {
        if (array!=null) {
            checkLength(array.length);
        } else {
            array=new byte[count];
        }
        for (int i=0; i<count; i++) {
            final Number n = getNumber(i);
            if ((array[i]=n.byteValue()) != n.doubleValue()) {
                throw notAnInteger(i);
            }
        }
        return array;
    }

    /**
     * V�rifie si le nombre de donn�es lues correspond au nombre de donn�es
     * attendues. Si ce n'est pas le cas, une exception sera lanc�e.
     *
     * @throws ParseException si le nombre de donn�es lues ne correspond pas au nombre de donn�es attendues.
     */
    private void checkLength(final int expected) throws ParseException {
        if (count!=expected) {
            final int lower=limits[Math.min(count, expected  )];
            final int upper=limits[Math.min(count, expected+1)];
            throw new ParseException(Resources.format(count<expected ?
                                     ResourceKeys.ERROR_LINE_TOO_SHORT_$2 :
                                     ResourceKeys.ERROR_LINE_TOO_LONG_$3,
                                     new Integer(count), new Integer(expected),
                                     line.substring(lower,upper).trim()), lower);
        }
    }

    /**
     * Creates an exception for a value not being an integer.
     *
     * @param  i the value index.
     * @return The exception.
     */
    private ParseException notAnInteger(final int i) {
        return new ParseException(Resources.format(ResourceKeys.ERROR_NOT_AN_INTEGER_$1,
                                                   line.substring(limits[i], limits[i+1])),
                                                   limits[i]);
    }

    /**
     * Retourne les donn�es sous forme de cha�ne de caract�res. Toutes
     * les donn�es seront format�es en utilisant les formats d�clar�s au
     * constructeur. Les colonnes seront s�par�es par des tabulations.
     * Il n'y aura pas de retour chariot � la fin de la ligne.
     */
    public String toString() {
        final FieldPosition field=new FieldPosition(0);
        StringBuffer buffer=new StringBuffer();
        for (int i=0; i<count; i++) {
            if (i!=0) {
                buffer.append('\t');
            }
            buffer = format[Math.min(format.length-1, i)].format(data[i], buffer, field);
        }
        return buffer.toString();
    }
}
