/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
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
package org.geotools.io.image;

// Miscellaneous
import java.util.Arrays;

import javax.imageio.IIOException;

import org.geotools.resources.Utilities;
import org.geotools.resources.XArray;
import org.geotools.resources.gcs.ResourceKeys;
import org.geotools.resources.gcs.Resources;


/**
 * List of data records in an image. One instance of this class is created by
 * {@link TextRecordImageReader} for every image in a file. A <code>RecordList</code>
 * contains a list of records where each record contains data for one pixel. A record
 * contains usually the following information:
 *
 * <ul>
 *   <li>Pixel's x and y coordinate.</li>
 *   <li>Pixel's values for each band.</li>
 * </li>
 *
 * Those information can appear in arbitrary columns, providing that the column order
 * stay the same for every record in a particular <code>RecordList</code> instance.
 * Records can appear in arbitrary order.
 * <br><br>
 * Data can be floating point value (<code>float</code> type). Current implementation
 * expects pixels distributed on a regular grid. The grid interval will be automatically
 * computed when needed. The interval computation should be accurate even if there is
 * missing and/or duplicated records.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
final class RecordList {
    /**
     * Valeurs minimales des colonnes, ou <code>null</code> si
     * ces valeurs ne sont pas encore connues.  La longueur de
     * de ce tableau est �gale � {@link #dataColumnCount}.
     */
    private double[] min;
    
    /**
     * Valeurs maximales des colonnes, ou <code>null</code> si
     * ces valeurs ne sont pas encore connues.  La longueur de
     * de ce tableau est �gale � {@link #dataColumnCount}.
     */
    private double[] max;
    
    /**
     * Intervals entre les donn�es, ou <code>null</code> si ces valeurs
     * n'ont pas encore �t� calcul�es. La valeur 0 signifie que l'interval
     * pour une colonne en particulier n'a pas encore �t� calcul�e.
     */
    private float[] interval;
    
    /**
     * Tableau des valeurs lues,  ou <code>null</code> si les
     * valeurs n'ont pas encore �t� lues. Ce tableau contient
     * une suite de lignes qui ont chacun un nombre de colonnes
     * �gal � {@link #dataColumnCount}.
     */
    private float[] data;
    
    /**
     * Nombre de colonnes retenues lors de la lecture, ou -1 si ce nombre
     * n'est pas encore connu. Ce nombre de colonnes peut �tre �gal ou
     * inf�rieur � <code>min.length</code> et <code>max.length</code>.
     */
    private int columnCount = -1;
    
    /**
     * Index suivant celui du dernier �l�ment valide de {@link #data}.
     * Ce champ sera augment� � chaque ajout d'une nouvelle ligne. Sa
     * valeur doit �tre un multiple entier de {@link #dataColumnCount}.
     */
    private int upper;
    
    /**
     * Nombre de lignes attendues. Cette information n'est qu'� titre
     * indicative, mais accelerera la lecture si elle est exacte.
     */
    private int expectedLineCount = 1024;
    
    /**
     * Construit un <code>ImageData</code> initiallement vide.
     * La premi�re ligne de donn�es lue d�terminera le nombre
     * de colonnes qui seront retenus pour toutes les lignes
     * suivantes.
     */
    public RecordList() {
    }
    
    /**
     * Construit un <code>ImageData</code> initiallement vide.
     * Pour chaque ligne lue, seule les <code>columnCount</code>
     * premi�res colonnes seront retenus.
     *
     * @param columnCount Nombre de colonnes � retenir lors de la lecture.
     * @param expectedLineCount Nombre de lignes attendues. Cette information
     *        n'est qu'� titre indicative, mais accelerera la lecture si elle
     *        est exacte.
     */
    public RecordList(final int columnCount, final int expectedLineCount) {
        this.columnCount       = columnCount;
        this.expectedLineCount = expectedLineCount;
    }
    
    /**
     * Ajoute une ligne de donn�es.  Si la ligne est plus courte que la longueur
     * attendues, les colonnes manquantes seront consid�r�es comme contenant des
     * <code>NaN</code>.   Si elle est plus longue que la longueur attendue, les
     * colonnes en trop seront ignor�es.
     */
    public void add(final double[] line) {
        if (data==null) {
            if (columnCount<0) columnCount=line.length;
            min  = new double[columnCount]; Arrays.fill(min, Double.POSITIVE_INFINITY);
            max  = new double[columnCount]; Arrays.fill(max, Double.NEGATIVE_INFINITY);
            data = new float [columnCount*expectedLineCount];
        }
        final int limit=Math.min(columnCount, line.length);
        final int nextUpper = upper+columnCount;
        if (nextUpper >= data.length) {
            data = XArray.resize(data, Math.max(nextUpper, data.length+Math.min(data.length, 65536)));
        }
        for (int i=0; i<limit; i++) {
            final double value = line[i];
            if (value<min[i]) min[i]=value;
            if (value>max[i]) max[i]=value;
            data[upper+i] = (float)value;
        }
        Arrays.fill(data, upper+limit, nextUpper, Float.NaN);
        upper = nextUpper;
    }
    
    /**
     * Lib�re la m�moire r�serv�e en trop. Cette m�thode peut �tre appel�e
     * lorsqu'on a termin� de lire les donn�es et qu'on veut les conserver
     * en m�moire pendant encore quelque temps.
     */
    public void trimToSize() {
        if (data!=null) {
            data=XArray.resize(data, upper);
        }
    }
    
    /**
     * Retourne une r�f�rence directe vers les donn�es m�moris�es par cet objet.
     * NE PAS MODIFIER CES DONNEES! Les index valides vont de 0 inclusivement
     * jusqu'� {@link #getDataCount} exclusivement.
     */
    final float[] getData() {
        return data;
    }
    
    /**
     * Retourne le nombre de donn�es qui ont �t� m�moris�es.
     */
    final int getDataCount() {
        return upper;
    }
    
    /**
     * Retourne le nombre de lignes qui ont �t� m�moris�es.
     */
    public int getLineCount() {
        if (columnCount <= 0) {
            return 0;
        }
        assert (upper % columnCount) == 0;
        return upper / columnCount;
    }
    
    /**
     * Retourne le nombre de colonnes, ou
     * -1 si ce nombre n'est pas connu.
     */
    public int getColumnCount() {
        return columnCount;
    }
    
    /**
     * Retourne la valeur minimale de la colonne sp�cifi�e,
     * ou {@link Double#NaN} si cette valeur n'est pas connue.
     */
    public double getMinimum(final int column) {
        return (min!=null && min[column]<=max[column]) ? min[column] : Double.NaN;
    }
    
    /**
     * Retourne la valeur maximale de la colonne sp�cifi�e,
     * ou {@link Double#NaN} si cette valeur n'est pas connue.
     */
    public double getMaximum(final int column) {
        return (max!=null && max[column]>=min[column]) ? max[column] : Double.NaN;
    }
    
    /**
     * Retourne l'interval entre les points de la colonne sp�cifi�e, en supposant que les
     * points se trouvent � un interval r�gulier. Si ce n'est pas le cas, une exception
     * sera lanc�e.
     *
     * @param  column Colonne dont on veut l'interval entre les points.
     * @param  eps Petit facteur de tol�rance (par exemple 1E-6).
     * @throws IIOException si les points de la colonne sp�cifi�e
     *         ne sont pas distribu�s � un interval r�gulier.
     */
    private float getInterval(final int column, final float eps) throws IIOException {
        if (interval==null) {
            if (columnCount<=0) {
                return Float.NaN;
            }
            interval = new float[columnCount];
        }
        if (interval[column]!=0) {
            return interval[column];
        }
        /*
         * Obtient toutes les valeurs de la colonne
         * sp�cifi�e en ordre croissant.
         */
        int count=0;
        final float[] array = new float[getLineCount()];
        for (int i=column; i<upper; i+=columnCount) {
            array[count++] = data[i];
        }
        assert count==array.length;
        Arrays.sort(array);
        /*
         * Elimine les doublons. Lorsque des doublons seront trouv�s, ils iront de
         * <code>lower</code> � <code>upper</code> <strong>inclusivement</strong>.
         */
        int upper = count-1;
        int lower = count;
        while (--lower>=1) {
            if (array[upper] != array[lower-1]) {
                if (upper != lower) {
                    System.arraycopy(array, upper, array, lower, count-upper);
                    final int oldCount = count;
                    count -= (upper-lower);
                    Arrays.fill(array, count, oldCount, Float.NaN); // Par prudence.
                }
                upper = lower-1;
            }
        }
        if (upper!=lower) {
            System.arraycopy(array, upper, array, lower, count-upper);
            final int oldCount = count;
            count -= (upper-lower);
            Arrays.fill(array, count, oldCount, Float.NaN); // Par prudence.
        }
        /*
         * Recherche le plus petit interval entre deux points. V�rifie ensuite que
         * l'interval entre tous les points est un multiple entier de cet interval
         * minimal (on tient compte ainsi des �ventuels donn�es manquantes).
         */
        float delta=Float.POSITIVE_INFINITY;
        for (int i=1; i<count; i++) {
            final float d=array[i]-array[i-1];
            assert d>0;
            if (d<delta) delta=d;
        }
        for (int i=1; i<count; i++) {
            float e=(array[i]-array[i-1])/delta;
            if (Math.abs(e-Math.rint(e)) > eps) {
                throw new IIOException(Resources.format(ResourceKeys.ERROR_NOT_A_GRID));
            }
        }
        return interval[column] = Float.isInfinite(delta) ? Float.NaN : delta;
    }
    
    /**
     * Retourne le nombre de points distincts dans la colonne sp�cifi�e. Cette m�thode
     * �limine d'abord tous les doublons avant d'effectuer le comptage. Elle v�rifie
     * aussi que les points restants sont espac�s � un interval r�gulier, et lancera
     * une exception si ce n'est pas le cas. S'il y a des trous dans les donn�es, il
     * seront pris en compte comme si un point s'y �tait trouv�.
     *
     * @param  column Colonne dont on veut le nombre de points distincts.
     * @param  eps Petit facteur de tol�rance (par exemple 1E-6).
     * @throws IIOException si les points de la colonne sp�cifi�e
     *         ne sont pas distribu�s � un interval r�gulier.
     */
    public int getPointCount(final int column, final float eps) throws IIOException {
        return (int)Math.round((getMaximum(column)-getMinimum(column))/getInterval(column, eps)) +1;
    }
    
    /**
     * Retourne un r�sum� des informations que contient cet objet. Le r�sum� contiendra
     * notamment les valeurs minimales et maximales de chaque colonnes.
     *
     * @param  xColumn Colonne des <var>x</var>, ou -1 s'il n'est pas connu.
     * @param  yColumn Colonne des <var>y</var>, ou -1 s'il n'est pas connu.
     * @param  eps Petit facteur de tol�rance (par exemple 1E-6).
     * @return Cha�ne de caract�res r�sumant l'�tat des donn�es.
     */
    public String toString(final int xColumn, final int yColumn, final float eps) {
        float xCount = Float.NaN;
        float yCount = Float.NaN;
        if (xColumn>=0) try {
            xCount = getPointCount(xColumn, eps);
        } catch (IIOException exception) {
            // Ignore.
        }
        if (yColumn>=0) try {
            yCount = getPointCount(yColumn, eps);
        } catch (IIOException exception) {
            // Ignore.
        }
        final StringBuffer buffer = new StringBuffer();
        buffer.append(Resources.format(ResourceKeys.POINT_COUNT_$3, new Integer(upper), new Float(xCount), new Float(yCount)));
        return buffer.toString();
    }
    
    /**
     * Retourne une cha�ne de caract�res repr�sentant cet objet.
     * Cette cha�ne indiquera le nombre de lignes et de colonnes
     * m�moris�es.
     */
    public String toString() {
        final StringBuffer buffer=new StringBuffer(Utilities.getShortClassName(this));
        buffer.append('[');
        buffer.append(getLineCount());
        buffer.append("\u00A0\u00D7\u00A0");
        buffer.append(getColumnCount());
        buffer.append(']');
        return buffer.toString();
    }
}
