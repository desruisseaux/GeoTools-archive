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


/**
 * Classe repr�sentant les unit�s fondamentales d'un syst�me d'unit�s.
 * Le syst�me international (SI) n'a que quelques unit�s fondamentales, � partir desquelles
 * sont construites toutes les autres unit�s. Les unit�s fondamentales sont du syst�me SI sont:
 *
 * <ul>
 *     <li>Les Amp�res pour le courant �lectrique</li>
 *     <li>Les Candelas pour l'intensit� lumineuse</li>
 *     <li>Les degr�s Kelvin pour la temp�rature</li>
 *     <li>Les kilogrammes pour la masse</li>
 *     <li>Les m�tres pour la longueur</li>
 *     <li>Les secondes pour le temps</li>
 * </ul>
 *
 * Il existe aussi d'autres mesures qui sont sans unit�s, mais pour lesquelles
 * il est pratique de leur affecter des pseudo-unit�s fondamentales. Les radians, les moles et
 * les mesures de salinit� en sont des exemples.
 *
 * @version 1.0
 * @author Steven R. Emmerson
 * @author Bill Hibbard
 * @author Martin Desruisseaux
 *
 * @deprecated Replaced by the {@link javax.units.Unit} framework.
 */
/*public*/ 
final class BaseUnit extends SimpleUnit {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -2736303035387288589L;
    
    /**
     * Construit une unit� fondamentale.
     *
     * @param quantityName Nom de la quantit� (exemple: "Mass").
     * @param symbol Symbole de l'unit� (exemple: "kg").
     */
    private BaseUnit(final String quantityName, final String symbol, final PrefixSet prefix) {
        super(quantityName, symbol, prefix);
    }
    
    /**
     * Cr�e une nouvelle unit� de base. Les unit�s de base n'existent
     * normalement qu'en nombre restreint et servent de "briques" �
     * toutes les autres unit�s. Avant de cr�er une nouvelle unit�
     * avec cette m�thode, v�rifiez s'il ne s'agit pas en r�alit�
     * d'une unit� d�riv�e.
     *
     * @param quantityName Le nom de la quantit� associ�e � l'unit�.
     *        Des exemples de noms seraient "mass", "electric current",
     *        "temperature", etc.
     *
     * @param symbol Le symbole des unit�s (par exemple "kg" pour la masse).
     *        Ce symbole est obligatoire et ne doit pas �tre <code>null</code>.
     *
     * @param prefix Liste des pr�fix pouvant �tre utilis�s avec le symbole
     *        <code>symbol</code>, ou <code>null</code> s'il n'y en a pas.
     *
     * @return Une unit� de base associ�e � la quantit� <code>quantityName</code>
     *         avec le symbole <code>symbol</code> et les pr�fix <code>prefix</code>.
     *
     * @see DerivedUnit#getInstance
     * @see ScaledUnit#getInstance
     * @see OffsetUnit#getInstance
     */
    public static BaseUnit getInstance(final String quantityName, final String symbol, final PrefixSet prefix) {
        return (BaseUnit) new BaseUnit(quantityName, symbol, prefix).intern();
    }
    
    /**
     * Retourne le symbole {@link #symbol} sans son pr�fix. Cette m�thode retourne habituellement
     * {@link #symbol}, ce qui est correct pour la presque totalit� des unit�s <code>BaseUnit</code>.
     * Dans le syst�me SI, la seule exception notable (qui justifie � elle seule l'existence de cette
     * m�thode) est le kilogramme (symbole "kg").
     */
    String getUnprefixedSymbol() {
        return symbol.equals("kg") ? "g" : super.getUnprefixedSymbol();
    }
    
    /**
     * Renvoie une unit� identique � celle-ci, mais
     * avec un nouveau symbole et de nouveaux pr�fix.
     *
     * @param  symbol Nouveau symbole repr�sentant cette unit�. Si ce
     *         param�tre est nul, un symbole par d�faut sera cr��.
     * @param  prefix Liste des pr�fix autoris�s pour le symbole.
     * @return La m�me unit�, mais avec le nouveau symbole. Peut �tre
     *         <code>this</code>, mais ne sera jamais <code>null</code>.
     */
    public Unit rename(final String symbol, final PrefixSet prefix) { // CAST
        return getInstance(quantityName, symbol, prefix);
    }
    
    /**
     * �l�ve cette unit� � une puissance enti�re.
     *
     * @param power La puissance � laquelle �lever cette unit�.
     * @return Les unit�s r�sultant de l'�l�vation des unit�s
     *         <code>this</code> � la puissance <code>power</code>.
     *
     * @see #multiply
     * @see #divide
     * @see #scale
     * @see #shift
     */
    public Unit pow(final int power) {
        switch (power) {
            case 0:  return DerivedUnit.DIMENSIONLESS;
            case 1:  return this;
            default: return DerivedUnit.getInstance(new Factor[] {Factor.getFactor(this, power)});
        }
    }
    
    /**
     * Multiplie cette unit� par une autre unit�.
     *
     * @param  that L'unit� par laquelle multiplier cette unit�.
     * @return Le produit de <code>this</code> par <code>that</code>.
     * @throws UnitException Si l'unit� <code>that</code> est de la
     *         classe {@link OffsetUnit} ou d'une autre classe invalide.
     *
     * @see #pow
     * @see #divide
     * @see #scale
     * @see #shift
     */
    public Unit multiply(final Unit that) throws UnitException {
        return that.inverseMultiply(this);
    }
    
    /**
     * Multiply a derived unit by a base unit
     * (<code>that</code>*<code>this</code>).
     */
    Unit inverseMultiply(final DerivedUnit that) {
        return that.multiply(new Factor[] {Factor.getFactor(this, +1)});
    }
    
    /**
     * Multiply a base unit by another base unit
     * (<code>that</code>*<code>this</code>).
     */
    Unit inverseMultiply(final BaseUnit that) throws UnitException {
        final Factor[] factors;
        if (equalsIgnoreSymbol(that)) {
            factors=new Factor[] {
                Factor.getFactor(that, 2)
            };
        } else {
            factors=new Factor[] {
                Factor.getFactor(that, 1),
                Factor.getFactor(this, 1)
            };
        }
        return DerivedUnit.getInstance(factors);
    }
    
    /**
     * Divise cette unit� par une autre unit�.
     *
     * @param that L'unit� par laquelle diviser cette unit�.
     * @return Le quotient de <code>this</code> par <code>that</code>.
     * @throws UnitException Si l'unit� <code>that</code> est de la
     *         classe {@link OffsetUnit} ou d'une autre classe invalide.
     *
     * @see #pow
     * @see #multiply
     * @see #scale
     * @see #shift
     */
    public Unit divide(final Unit that) throws UnitException {
        return that.inverseDivide(this);
    }
    
    /**
     * Divide a derived unit by a base unit
     * (<code>that</code>/<code>this</code>).
     */
    Unit inverseDivide(final DerivedUnit that) {
        return that.multiply(new Factor[] {Factor.getFactor(this, -1)});
    }
    
    /**
     * Divide a base unit by another base unit
     * (<code>that</code>/<code>this</code>).
     */
    Unit inverseDivide(final BaseUnit that) throws UnitException {
        if (!equalsIgnoreSymbol(that)) {
            return DerivedUnit.getInstance(new Factor[] {
                Factor.getFactor(that, +1),
                Factor.getFactor(this, -1)
            });
        } else {
            return DerivedUnit.DIMENSIONLESS;
        }
    }
    
    /**
     * Indique si les unit�s <code>this</code> et <code>that</code> sont compatibles.
     * Si elles le sont, alors les m�thodes <code>convert</code> ne lanceront jamais
     * d'exception pour ces unit�s.
     *
     * @param that Autre unit�s avec laquelle on veut
     *        v�rifier si ces unit�s sont compatibles.
     * @return <code>true</code> Si l'on garantie que les m�thodes
     *         <code>convert</code> ne lanceront pas d'exceptions.
     */
    public boolean canConvert(final Unit        that) {return that.canConvert(this);} // Do not move in superclass
           boolean canConvert(final DerivedUnit that) {return that.canConvert(this);} // Do not move in superclass
           boolean canConvert(final BaseUnit    that) {return equalsIgnoreSymbol(that);}
    
    /**
     * Effectue la conversion d'une mesure exprim�e selon d'autres unit�s. Par
     * exemple <code>METRE.convert(1,&nbsp;FOOT)</code> retournera <code>0.3048</code>.
     *
     * @param value La valeur exprim�e selon les autres unit�s (<code>fromUnit</code>).
     * @param fromUnit Les autres unit�s.
     * @return La valeur convertie selon ces unit�s (<code>this</code>).
     * @throws UnitException Si les unit�s ne sont pas compatibles.
     */
    public double convert(final double value, final Unit        fromUnit) throws UnitException {return fromUnit.inverseConvert(value, this);} // Do not move in superclass
           double convert(final double value, final DerivedUnit fromUnit) throws UnitException {return fromUnit.inverseConvert(value, this);} // Do not move in superclass
           double convert(final double value, final BaseUnit    fromUnit) throws UnitException {if (!equalsIgnoreSymbol(fromUnit)) throw incompatibleUnitsException(fromUnit); return value;}
    
    /**
     * Effectue sur-place la conversion de mesures exprim�es selon d'autres
     * unit�s. Les valeurs converties remplaceront les anciennes valeurs.
     *
     * @param  values En entr�, les valeurs exprim�es selon les autres unit�s
     *         (<code>fromUnit</code>). En sortie, les valeurs exprim�es selon
     *         ces unit�s (<code>this</code>).
     * @param  fromUnit Les autres unit�s.
     * @throws UnitException Si les unit�s ne sont pas compatibles. Dans ce
     *         cas, aucun �l�ment de <code>values</code> n'aura �t� modifi�.
     */
    public void convert(final double[] values, final Unit        fromUnit) throws UnitException {fromUnit.inverseConvert(values, this);} // Do not move in superclass
           void convert(final double[] values, final DerivedUnit fromUnit) throws UnitException {fromUnit.inverseConvert(values, this);} // Do not move in superclass
           void convert(final double[] values, final BaseUnit    fromUnit) throws UnitException {if (!equalsIgnoreSymbol(fromUnit)) throw incompatibleUnitsException(fromUnit);}
    
    /**
     * Effectue sur-place la conversion de mesures exprim�es selon d'autres
     * unit�s. Les valeurs converties remplaceront les anciennes valeurs.
     * Notez que d'importantes erreurs d'arrondissement peuvent survenir
     * si <code>fromUnit</code> est de la classe {@link OffsetUnit}.
     *
     * @param  values En entr�, les valeurs exprim�es selon les autres
     *         unit�s (<code>fromUnit</code>). En sortie, les valeurs exprim�es
     *         selon ces unit�s (<code>this</code>).
     * @param  fromUnit Les autres unit�s.
     * @throws UnitException Si les unit�s ne sont pas compatibles. Dans ce
     *         cas, aucun �l�ment de <code>values</code> n'aura �t� modifi�.
     */
    public void convert(final float[] values, final Unit        fromUnit) throws UnitException {fromUnit.inverseConvert(values, this);} // Do not move in superclass
           void convert(final float[] values, final DerivedUnit fromUnit) throws UnitException {fromUnit.inverseConvert(values, this);} // Do not move in superclass
           void convert(final float[] values, final BaseUnit    fromUnit) throws UnitException {if (!equalsIgnoreSymbol(fromUnit)) throw incompatibleUnitsException(fromUnit);}
    
    /**
     * Retourne un objet qui saura convertir selon ces unit�s les valeurs exprim�es
     * selon d'autres unit�s. Cette m�thode est avantageuse si on pr�voie fa�re
     * plusieurs conversions, car la transformation � utiliser est d�termin�e une
     * fois pour toute.
     *
     * @param  fromUnit Unit�s � partir de lesquel faire les conversions.
     * @return Une transformation des unit�s <code>fromUnit</code>
     *         vers les unit�s <code>this</code>. Cette m�thode ne
     *         retourne jamais <code>null</code>.
     * @throws UnitException Si les unit�s ne sont pas compatibles.
     */
    public UnitTransform getTransform(final Unit        fromUnit) throws UnitException {return fromUnit.getInverseTransform(this);} // Do not move in superclass
           UnitTransform getTransform(final DerivedUnit fromUnit) throws UnitException {return fromUnit.getInverseTransform(this);} // Do not move in superclass
           UnitTransform getTransform(final BaseUnit    fromUnit) throws UnitException {return IdentityTransform.getInstance(fromUnit, this);}
    
    /**
     * Convertit une mesure vers d'autre unit�s. Par exemple
     * <code>METRE.inverseConvert(1,&nbsp;FOOT)</code> retournera
     * <code>3.2808</code>. Cette m�thode est l'inverse de la m�thode
     * {@link #convert(double,Unit)}.
     *
     * @param value La valeur exprim�e selon ces unit�s (<code>this</code>).
     * @param toUnit Les autres unit�s.
     * @return La valeur convertie selon les autres unit�s (<code>toUnit</code>).
     * @throws UnitException Si les unit�s ne sont pas compatibles.
     */
    protected double inverseConvert(final double value, final Unit        toUnit) throws UnitException {return toUnit.convert(value, this);} // Do not move in superclass
              double inverseConvert(final double value, final DerivedUnit toUnit) throws UnitException {return toUnit.convert(value, this);} // Do not move in superclass
              double inverseConvert(final double value, final BaseUnit    toUnit) throws UnitException {if (!equalsIgnoreSymbol(toUnit)) throw toUnit.incompatibleUnitsException(this); return value;}
    
    /**
     * Effectue sur-place la conversion de mesures vers d'autres unit�s.
     * Les valeurs converties remplaceront les anciennes valeurs. Cette
     * m�thode est l'inverse de la m�thode {@link #convert(double[],Unit)}.
     *
     * @param  values En entr�, les valeur exprim�es selon ces unit�s
     *         (<code>this</code>). En sortie, les valeurs exprim�es
     *         selon les autres unit�s (<code>toUnit</code>).
     * @param  toUnit Les autres unit�s.
     * @throws UnitException Si les unit�s ne sont pas compatibles. Dans ce
     *         cas, aucun �l�ment de <code>values</code> n'aura �t� modifi�.
     */
    protected void inverseConvert(final double[] values, final Unit        toUnit) throws UnitException {toUnit.convert(values, this);} // Do not move in superclass
              void inverseConvert(final double[] values, final DerivedUnit toUnit) throws UnitException {toUnit.convert(values, this);} // Do not move in superclass
              void inverseConvert(final double[] values, final BaseUnit    toUnit) throws UnitException {if (!equalsIgnoreSymbol(toUnit)) throw toUnit.incompatibleUnitsException(this);}
    
    /**
     * Effectue sur-place la conversion de mesures vers d'autres unit�s.
     * Les valeurs converties remplaceront les anciennes valeurs. Cette
     * m�thode est l'inverse de la m�thode {@link #convert(float[],Unit)}.
     *
     * @param  values En entr�, les valeur exprim�es selon ces unit�s
     *         (<code>this</code>). En sortie, les valeurs exprim�es
     *         selon les autres unit�s (<code>toUnit</code>).
     * @param  toUnit Les autres unit�s.
     * @throws UnitException Si les unit�s ne sont pas compatibles. Dans ce
     *         cas, aucun �l�ment de <code>values</code> n'aura �t� modifi�.
     */
    protected void inverseConvert(final float[] values, final Unit        toUnit) throws UnitException {toUnit.convert(values, this);} // Do not move in superclass
              void inverseConvert(final float[] values, final DerivedUnit toUnit) throws UnitException {toUnit.convert(values, this);} // Do not move in superclass
              void inverseConvert(final float[] values, final BaseUnit    toUnit) throws UnitException {if (!equalsIgnoreSymbol(toUnit)) throw toUnit.incompatibleUnitsException(this);}
    
    /**
     * Retourne un objet qui saura convertir selon d'autres unit�s les
     * valeurs exprim�es selon ces unit�s. Cette m�thode est l'inverse
     * de {@link #getTransform}.
     *
     * @param  toUnit Unit�s vers lesquel faire les conversions.
     * @return Une transformation des unit�s <code>this</code>
     *         vers les unit�s <code>toUnit</code>. Cette m�thode
     *         ne retourne jamais <code>null</code>.
     * @throws UnitException Si les unit�s ne sont pas compatibles.
     */
    protected UnitTransform getInverseTransform(final Unit        toUnit) throws UnitException {return toUnit.getTransform(this);} // Do not move in superclass
              UnitTransform getInverseTransform(final DerivedUnit toUnit) throws UnitException {return toUnit.getTransform(this);} // Do not move in superclass
              UnitTransform getInverseTransform(final BaseUnit    toUnit) throws UnitException {return IdentityTransform.getInstance(this, toUnit);}
    
    /**
     * V�rifie si cette unit� est identique � une autre. Deux unit�s sont consid�r�s
     * identiques s'ils ont le m�me champ {@link #quantityName}. Des symboles et prefix
     * diff�rents sont autoris�s.
     */
    final boolean equalsIgnoreSymbol(final BaseUnit unit) {
        return (unit==this) || quantityName.equals(unit.quantityName);
    }
    
    /**
     * Indique si deux unit�s sont �gales, en ignorant leurs symboles.
     * La comparaison ne prend en compte que les quantit�s telles que
     * retourn�es par {@link #getQuantityName}.
     */
    public boolean equalsIgnoreSymbol(final Unit unit) {
        return (unit instanceof BaseUnit) && equalsIgnoreSymbol((BaseUnit) unit);
    }
    
    /**
     * Retourne un code
     * pour cette unit�.
     */
    public int hashCode() {
        return quantityName.hashCode();
    }
}
