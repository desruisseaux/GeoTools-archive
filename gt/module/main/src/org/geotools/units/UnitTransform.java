/*
 * Units - Temporary implementation for Geotools 2
 * Copyright (C) 1998 University Corporation for Atmospheric Research (Unidata)
 *               1998 Bill Hibbard & al. (VisAD)
 *               1999 P�ches et Oc�ans Canada
 *               2000 Institut de Recherche pour le D�veloppement
 *               2002 Centre for Computational Geography
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Library General Public
 *    License as published by the Free Software Foundation; either
 *    version 2 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *    Library General Public License for more details (http://www.gnu.org/).
 *
 *
 *    This package is inspired from the units package of VisAD.
 *    Unidata and Visad's work is fully acknowledged here.
 *
 *                   THIS IS A TEMPORARY CLASS
 *
 *    This is a placeholder for future <code>Unit</code> class.
 *    This skeleton will be removed when the real classes from
 *    JSR-108: Units specification will be publicly available.
 */
package org.geotools.units;

// Entr�s/sorties
import java.io.ObjectStreamException;
import java.io.Serializable;

import org.geotools.resources.Utilities;
import org.geotools.util.WeakHashSet;


/**
 * Repr�sente une transformation entre deux unit�s. Par convention, tout
 * les objets <code>UnitTransform</code> sont toujours imutables. Il est
 * donc s�curitaire de partager plusieurs r�f�rences vers le m�me objet.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 *
 * @deprecated Replaced by the {@link javax.units.Unit} framework.
 */
public abstract class UnitTransform implements Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 59496814325077015L;

    /**
     * Banque des objets qui ont �t� pr�c�demment cr��s et
     * enregistr�s par un appel � la m�thode {@link #intern}.
     */
    private static final WeakHashSet pool=Prefix.pool;

    /**
     * Unit� selon laquelle seront
     * exprim�es les valeurs initiales.
     */
    public final Unit fromUnit;

    /**
     * Unit� selon laquelle seront
     * exprim�es les valeurs finales.
     */
    public final Unit toUnit;

    /**
     * Construit un objet qui aura la charge de convertir
     * des donn�es exprim�es selon les unit�s sp�cifi�es.
     */
    /*protected*/ UnitTransform(final Unit fromUnit, final Unit toUnit) {
        this.fromUnit = fromUnit;
        this.toUnit   = toUnit;
    }

    /**
     * Indique si cette transformation affine repr�sente une transformation id�ntit�e.
     * L'impl�mentation par d�faut retourne <code>toUnit.equalsIgnoreSymbol(fromUnit)</code>.
     */
    public boolean isIdentity() {
        return toUnit.equalsIgnoreSymbol(fromUnit);
    }

    /**
     * Effectue la conversion d'unit�s d'une valeur.
     * @param value Valeur exprim�e selon les unit�s {@link #fromUnit}.
     * @return Valeur exprim�e selon les unit�s {@link #toUnit}.
     */
    public abstract double convert(double value);

    /**
     * Effectue la conversion d'unit�s d'un tableaux de valeurs.
     * L'impl�mentation par d�faut appelle {@link #convert(double)}
     * dans une boucle. Les classes d�riv�es devraient red�finir cette
     * m�thode avec une impl�mentation plus efficace.
     *
     * @param values Valeurs exprim�es selon les unit�s {@link #fromUnit}.
     *        Elles seront converties sur place en valeurs exprim�es selon
     *        les unit�s {@link #toUnit}.
     */
    public void convert(final double[] values) {
        for (int i=0; i<values.length; i++) {
            values[i]=convert(values[i]);
        }
    }

    /**
     * Effectue la conversion d'unit�s d'un tableaux de valeurs.
     * L'impl�mentation par d�faut appelle {@link #convert(double)}
     * dans une boucle. Les classes d�riv�es devraient red�finir cette
     * m�thode avec une impl�mentation plus efficace.
     *
     * @param values Valeurs exprim�es selon les unit�s {@link #fromUnit}.
     *        Elles seront converties sur place en valeurs exprim�es selon
     *        les unit�s {@link #toUnit}.
     */
    public void convert(final float[] values) {
        for (int i=0; i<values.length; i++) {
            values[i]=(float) convert(values[i]);
        }
    }

    /**
     * Effectue la conversion inverse d'unit�s d'une valeur.
     * @param value Valeur exprim�e selon les unit�s {@link #toUnit}.
     * @return Valeur exprim�e selon les unit�s {@link #fromUnit}.
     */
    public abstract double inverseConvert(double value);

    /**
     * Effectue la conversion inverse d'unit�s d'un tableaux de valeurs.
     * L'impl�mentation par d�faut appelle {@link #inverseConvert(double)}
     * dans une boucle. Les classes d�riv�es devraient red�finir cette
     * m�thode avec une impl�mentation plus efficace.
     *
     * @param values Valeurs exprim�es selon les unit�s {@link #toUnit}.
     *        Elles seront converties sur place en valeurs exprim�es selon
     *        les unit�s {@link #fromUnit}.
     */
    public void inverseConvert(final double[] values) {
        for (int i=0; i<values.length; i++) {
            values[i]=inverseConvert(values[i]);
        }
    }

    /**
     * Effectue la conversion inverse d'unit�s d'un tableaux de valeurs.
     * L'impl�mentation par d�faut appelle {@link #inverseConvert(double)}
     * dans une boucle. Les classes d�riv�es devraient red�finir cette
     * m�thode avec une impl�mentation plus efficace.
     *
     * @param values Valeurs exprim�es selon les unit�s {@link #toUnit}.
     *        Elles seront converties sur place en valeurs exprim�es selon
     *        les unit�s {@link #fromUnit}.
     */
    public void inverseConvert(final float[] values) {
        for (int i=0; i<values.length; i++) {
            values[i]=(float) inverseConvert(values[i]);
        }
    }

    /**
     * Retourne un exemplaire unique de cette transformation. Une banque de
     * transformation, initialement vide, est maintenue de fa�on interne par
     * la classe <code>UnitTransform</code>. Lorsque la m�thode <code>intern</code>
     * est appell�e, elle recherchera une transformation �gale � <code>this</code>
     * au sens de la m�thode {@link #equals}. Si une telle transformation fut trouv�e,
     * elle sera retourn�e. Sinon, la trsnsformation <code>this</code> sera ajout�e �
     * la banque de donn�es en utilisant une r�f�rence faible et cette m�thode retournera
     * <code>this</code>.
     *
     * <p>De cette m�thode il s'ensuit que pour deux transformations <var>u</var> et <var>v</var>,
     * la condition <code>u.intern()==v.intern()</code> sera vrai si et seulement si
     * <code>u.equals(v)</code> est vrai.</p>
     */
    /*public*/ final UnitTransform intern() {
        return (UnitTransform) pool.canonicalize(this);
    }

    /**
     * Indique si cet objet est identique � l'objet sp�cifi�.
     * Les deux objets seront considir�s identiques s'ils
     * sont de la m�me classe et font les conversions � partir
     * de et vers les m�mes unit�s.
     */
    public boolean equals(final Object o) {
        if (o!=null && getClass().equals(o.getClass())) {
            final UnitTransform ut=(UnitTransform) o;
            return fromUnit.equals(ut.fromUnit) && toUnit.equals(ut.toUnit);
        }
        return false;
    }

    /**
     * Retourne un code repr�sentant
     * cette transformation d'unit�s.
     */
    public int hashCode() {
        return fromUnit.hashCode() ^ toUnit.hashCode();
    }

    /**
     * Retourne une cha�ne de caract�res repr�sentant
     * cette transformation. La cha�ne sera de la forme
     *
     * <code>UnitTransform[km/h&nbsp;-->&nbsp;m/s]</code>
     */
    public String toString() {
        return Utilities.getShortClassName(this)+'['+fromUnit+" --> "+toUnit+']';
    }
    
    /**
     * Apr�s la lecture d'une transformation, v�rifie si cette transformation appara�t
     * d�j� dans la banque des unit�s <code>pool</code>. Si oui, l'exemplaire de la banque
     * sera retourn� plut�t que de garder inutilement la transformation courante comme
     * copie.
     */
    final Object readResolve() throws ObjectStreamException {
        return intern();
    }
}
