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

import org.geotools.util.WeakHashSet;


/**
 * Classe repr�sentant une unit� de base et la valeur de son exposant.
 * Cette classe est utilis�e en argument par la m�thode {@link DerivedUnit#getInstance}.
 * Elle n'a pas de constructeur publique; il n'existe pas d'autres moyen de cr�er de
 * nouveaux objets <code>Factor</code> que d'utiliser la m�thode statique
 * {@link #getFactor}.
 *
 * @version 1.0
 * @author Steven R. Emmerson
 * @author Bill Hibbard
 * @author Martin Desruisseaux
 *
 * @deprecated Replaced by the {@link javax.units.Unit} framework.
 */
/*public*/ 
final class Factor implements Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -7448171508684390207L;
    
    /**
     * Banque des objets qui ont �t� pr�c�demment cr��s et
     * enregistr�s par un appel � la m�thode {@link #intern}.
     */
    private static final WeakHashSet pool=Prefix.pool;
    
    /**
     * L'unit� de base de ce facteur.
     */
    public final BaseUnit baseUnit;
    
    /**
     * La valeur de l'exposant associ�e
     * � l'unit� de base de ce facteur.
     */
    public final int power;
    
    /**
     * Construit un facteur qui repr�sentera une
     * unit� de base �lev�e � la puissance sp�cifi�e.
     *
     * @param baseUnit L'unit� de base.
     * @param power La puissance � laquelle �lever l'unit� de base.
     */
    private Factor(final BaseUnit baseUnit, final int power) {
        if (baseUnit!=null) {
            this.baseUnit = baseUnit;
            this.power    = power;
        } else {
            throw new NullPointerException();
        }
    }
    
    /**
     * Retourne un facteur qui repr�sentera une
     * unit� de base �lev�e � la puissance sp�cifi�e.
     *
     * @param baseUnit L'unit� de base.
     * @param power La puissance � laquelle �lever l'unit� de base.
     */
    public static Factor getFactor(final BaseUnit baseUnit, final int power) {
        return new Factor(baseUnit, power).intern();
    }
    
    /**
     * Retourne un facteur dont l'exposant de l'unit�
     * est de signe inverse de celui de ce facteur.
     */
    public final Factor inverse() {
        return getFactor(baseUnit, -power);
    }
    
    /**
     * Retourne le symbole de ce facteur. Ce sera le
     * symbole de l'unit� de base avec son exposant.
     * Par exemple "m", "m�" ou "kg^-1".
     */
    public String toString() {
        return UnitFormat.DEFAULT.format(this, new StringBuffer()).toString();
    }
    
    /**
     * Indique si ce facteur est identique ou r�ciproque au facteur sp�cifi�.
     *
     * @param  that L'autre facteur (peut �tre nul).
     * @return <code>+1</code> Si les deux facteurs sont identiques.<br>
     *         <code>-1</code> Si les deux facteurs sont r�ciproques (par exemple <i>s</i> et 1/<i>s</i>).<br>
     *         <code> 0</code> Si les deux facteurs ne sont ni identiques ni r�ciproques, ou si <code>that</code> est nul.
     */
    final int compareDimensionality(final Factor that) {
        if (that!=null && baseUnit.equals(that.baseUnit)) {
            if (power == +that.power) return +1;
            if (power == -that.power) return -1;
        }
        return 0;
    }
    
    /**
     * V�rifie si ce facteur est identique au facteur sp�cifi�. Cette m�thode retourne <code>true</code>
     * si les deux facteurs utilisent les m�mes unit�s {@link #baseUnits} avec la m�me puissance {@link
     * #power}.
     */
    final boolean equals(final Factor factor) {
        return power==factor.power && baseUnit.equals(factor.baseUnit);
    }
    
    /**
     * V�rifie si ce facteur est identique � l'objet sp�cifi�. Cette m�thode retourne <code>true</code>
     * si <code>object</code> est aussi un objet <code>Factor</code> et si les deux facteurs utilisent
     * les m�mes unit�s {@link #baseUnit} avec la m�me puissance {@link #power}.
     */
    public boolean equals(final Object object) {
        return (object==this) || // slight optimisation
               ((object instanceof Factor) && equals((Factor) object));
    }
    
    /**
     * Retourne un code � peu pr�s
     * unique pour ce facteur.
     */
    public int hashCode() {
        return baseUnit.hashCode()+power;
    }
    
    /**
     * Retourne un exemplaire unique de ce facteur. Une banque de facteurs, initialement
     * vide, est maintenue de fa�on interne par la classe <code>Unit</code>. Lorsque la
     * m�thode <code>intern</code> est appel�e, elle recherchera un facteur �gale �
     * <code>this</code> au sens de la m�thode {@link #equals}. Si un tel facteur est
     * trouv�, il sera retourn�. Sinon, le facteur <code>this</code> sera ajout� � la
     * banque de donn�es en utilisant une r�f�rence faible et cette m�thode retournera
     * <code>this</code>.
     * <br><br>
     * De cette m�thode il s'ensuit que pour deux facteurs <var>u</var> et <var>v</var>,
     * la condition <code>u.intern()==v.intern()</code> sera vrai si et seulement si
     * <code>u.equals(v)</code> est vrai.
     */
    private final Factor intern() {
        return (Factor) pool.canonicalize(this);
    }
    
    /**
     * Apr�s la lecture d'un facteur, v�rifie si ce facteur
     * appara�t d�j� dans la banque {@link Unit#pool}.
     * Si oui, l'exemplaire de la banque sera retourn� plut�t
     * que de garder inutilement le facteur courant comme copie.
     */
    final Object readResolve() throws ObjectStreamException {
        return intern();
    }
}
