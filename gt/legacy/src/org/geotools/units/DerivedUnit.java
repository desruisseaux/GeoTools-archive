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

// Divers
import org.geotools.resources.rsc.ResourceKeys;
import org.geotools.resources.rsc.Resources;


/**
 * Classe repr�sentant une unit� d�riv�e de zero, un ou plusieurs unit�s fondamentales.
 * Les unit�s fondamentales sont d�finies par la classe {@link BaseUnit}. Elles repr�sentent par
 * exemple des mesures de longueurs (m) ou de temps (s). Les objets de la classe <code>DerivedUnit</code>
 * combinent ensemble quelques unit�s fondamentales pour cr�er par exemple des unit�s de vitesses (m/s).
 *
 * @version 1.0
 * @author Steven R. Emmerson
 * @author Bill Hibbard
 * @author Martin Desruisseaux
 *
 * @deprecated Replaced by the {@link javax.units.Unit} framework.
 */
/*public*/ 
final class DerivedUnit extends SimpleUnit {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -4476414709268904273L;
    
    /**
     * Unit� sans dimensions. Cette
     * unit� n'aura aucun symbole.
     */
    static final Unit DIMENSIONLESS=new DerivedUnit().intern();
    
    /**
     * Tableau d'unit�s fondamentales avec leur exposant (par exemple m�). Les diff�rentes
     * m�thodes de la classe <code>DerivedUnit</code> doivent s'assurer qu'une m�me unit�
     * n'appara�t pas deux fois dans ce tableau, et qu'aucune unit� n'a un exposant de 0.
     */
    private final Factor[] factors;
    
    /**
     * Construit une unit� sans dimension.
     */
    private DerivedUnit()
    {this("dimensionless", "", null, new Factor[0]);}
    
    /**
     * Construit une unit� d�riv�e avec le tableau
     * d'unit�s fondamentales sp�cifi� ainsi qu'un
     * certain symbole.
     *
     * @param factors Tableau d'unit�s fondamentales avec leurs
     *        exposants. Ce tableau sera suppos� d�j� simplifi�,
     *        c'est-�-dire que la m�me unit� fondamentale n'y
     *        apparait pas deux fois et aucun exposant n'est 0.
     */
    private DerivedUnit(final Factor[] factors) {
        super(null, UnitFormat.DEFAULT.format(factors, new StringBuffer()).toString(), null);
        this.factors=factors;
    }
    
    /**
     * Construit une unit� d�riv�e avec le tableau
     * d'unit�s fondamentales sp�cifi� ainsi qu'un
     * certain symbole.
     *
     * @param quantityName Nom de la quantit� (exemple: "Speed").
     * @param symbol Symbole de cette unit� d�riv�e. Ce symbole
     *        ne doit pas �tre nul.
     * @param prefix Liste des pr�fix qui peuvent �tre plac�s devant le symbole
     *        <code>symbol</code>, ou <code>null</code> s'il n'y en a pas. Cette
     *        liste sera prise en compte par la m�thode {@link #scale}.
     * @param factors Tableau d'unit�s fondamentales avec leurs
     *        exposants. Ce tableau sera suppos� d�j� simplifi�,
     *        c'est-�-dire que la m�me unit� fondamentale n'y
     *        apparait pas deux fois et aucun exposant n'est 0.
     */
    private DerivedUnit(final String quantityName, final String symbol, final PrefixSet prefix, final Factor[] factors) {
        super(quantityName, symbol, prefix);
        this.factors=factors;
    }
    
    /**
     * Construit une unit� d�riv�e avec le tableau d'unit�s fondamentales sp�cifi�. Un symbole par d�faut
     * sera attribu�. Ce symbole ne sera pas n�cessairement le plus appropri�. Par exemple le symbole
     * "kg*m�/s�" pourrait �tre cr�� � la place de "J" pour les unit�s d'�nergie.
     */
    public static SimpleUnit getInstance(final Factor[] factors) {
        return getInstance(null, null, null, factors);
    }
    
    /**
     * Cr�e une nouvelle unit� d�riv�e de une ou plusieurs unit�s de bases.
     * Par exemple si <code>SECOND</code> est une unit� de base mesurant
     * le temps en secondes et <code>METRE</code> une unit� de base pour les
     * distances, alors on pourrait cr�er une unit� de vitesse avec le code
     * suivant:
     *
     * <blockquote><pre>
     * &nbsp;Unit METRE_PER_SECOND=DerivedUnit.<strong>getInstance</strong>("speed", "m/s", null, new Factor[]
     * &nbsp;{
     * &nbsp;    Factor.getFactor(METRE,  +1),
     * &nbsp;    Factor.getFactor(SECOND, -1)
     * &nbsp;});
     * </pre></blockquote>
     *
     * @param  quantityName Nom de la quantit� (exemple: "Speed").
     * @param  symbol Le symbole qui repr�sentera cette unit� d�riv�e
     *         (par exemple "J" pour les joules). Si nul, alors un symbole
     *         par d�faut sera cr��. Par exemple le symbole "kg*m�/s�"
     *         pourrait �tre cr�� � la place de "J" pour les unit�s d'�nergie.
     * @param  prefix Liste des pr�fix qui peuvent �tre plac�s devant le symbole
     *         <code>symbol</code>, ou <code>null</code> s'il n'y en a pas. Cette
     *         liste sera prise en compte par la m�thode {@link #scale}.
     * @param  factors Liste des unit�s de bases ainsi que de
     *         leurs exposants qui composeront l'unit� d�riv�es.
     *         Les �l�ments nuls ainsi que ceux qui ont un exposant
     *         de 0 seront ignor�s.
     * @return Une nouvelle unit� d�riv�es. S'il existait d�j�
     *         une unit� d�riv�e qui r�pondait aux sp�cifications,
     *         celle-ci sera retourn�e. Il est possible que cette
     *         m�thode retourne un objet {@link BaseUnit} au lieu
     *         d'un objet {@link DerivedUnit}, si une telle
     *         simplification �tait possible.
     *
     * @see BaseUnit#getInstance
     * @see ScaledUnit#getInstance
     * @see OffsetUnit#getInstance
     */
    public static SimpleUnit getInstance(final String quantityName, final String symbol, final PrefixSet prefix, Factor[] factors) {
        /*
         * Construit un tableau de facteurs dans lequel les doublons auront �t� fusionn�s.
         * Le tableau retourn� sera toujours une copie du tableau original, de sorte que
         * son contenu ne sera pas affect� par d'�ventuels changements du tableau original.
         */
        Factor[] oldFactors=factors;
        factors=new Factor[oldFactors.length];
        System.arraycopy(oldFactors, 0, factors, 0, factors.length);
        for (int i=0; i<factors.length; i++) {
            Factor fi=factors[i];
            if (fi!=null) {
                final BaseUnit ui=fi.baseUnit;
                for (int j=i+1; j<factors.length; j++) {
                    final Factor fj=factors[j];
                    if (fj!=null && ui.equalsIgnoreSymbol(fj.baseUnit)) {
                        factors[i]=fi=Factor.getFactor(ui, fi.power+fj.power);
                        factors[j]=null;
                    }
                }
            }
        }
        int length=0;
        for (int i=0; i<factors.length; i++) {
            final Factor fi=factors[i];
            if (fi!=null && fi.power!=0) {
                length++;
            }
        }
        if (factors.length!=length) {
            oldFactors=factors;
            factors=new Factor[length];
            for (int i=oldFactors.length; --i>=0;) {
                final Factor fi=oldFactors[i];
                if (fi!=null && fi.power!=0) {
                    factors[--length]=fi;
                }
            }
        }
        /*
         * Maintenant que le tableau <code>factors</code> a
         * �t� simplifi�, construit des unit�s avec ce tableau.
         */
        switch (factors.length) {
            case 0: return (SimpleUnit) DIMENSIONLESS;
            case 1: if (factors[0].power==1) return factors[0].baseUnit;
        }
        if (symbol!=null) {
            return (SimpleUnit) new DerivedUnit(quantityName, symbol, prefix, factors).intern();
        } else {
            return (SimpleUnit) new DerivedUnit(factors).internIgnoreSymbol();
        }
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
    public Unit rename(final String symbol, final PrefixSet prefix) {// CAST
        return getInstance(quantityName, symbol, prefix, factors);
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
            case 0:  return DIMENSIONLESS;
            case 1:  return this;
            default: {
                final Factor[] newFactors=new Factor[factors.length];
                for (int i=0; i<newFactors.length; i++) {
                    newFactors[i] = Factor.getFactor(factors[i].baseUnit, factors[i].power*power);
                }
                return getInstance(newFactors);
            }
        }
    }
    
    /**
     * �l�ve ces unit�s � une puissance fractionnaire. Cette m�thode est utile entre
     * autre pour prendre la racine carr� d'un nombre, ce qui revient � l'�lever � la
     * puissance �.
     *
     * @param power La puissance � laquelle �lever cette unit�.
     * @return Les unit�s r�sultant de l'�l�vation des unit�s
     *         <code>this</code> � la puissance <code>power</code>.
     * @throws UnitException Si cette unit� ne peut pas �tre �lev�e
     *         � une puissance non-enti�re.
     */
    public Unit pow(final double power) throws UnitException {
        final int integer=(int) power;
        if (integer==power) return pow(integer);
        final Factor[] newFactors=new Factor[factors.length];
        for (int i=0; i<newFactors.length; i++) {
            final float floatPower=(float) (factors[i].power*power); // Round result
            final int integerPower=(int) floatPower;
            if (integerPower==floatPower) {
                newFactors[i]=Factor.getFactor(factors[i].baseUnit, integerPower);
            } else {
                throw new UnitException(Resources.format(
                                        ResourceKeys.ERROR_BAD_UNIT_POWER_$2,
                                        new Double(power), this), this, null);
            }
        }
        return getInstance(newFactors);
    }
    
    /**
     * Multiplie cette unit� par une autre unit�.
     *
     * @param that L'unit� par laquelle multiplier cette unit�.
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
     * Multiply a base unit by a derived unit
     * (<code>that</code>*<code>this</code>).
     */
    Unit inverseMultiply(final BaseUnit that) throws UnitException {
        return multiply(new Factor[] {Factor.getFactor(that, 1)}, factors, +1);
    }
    
    /**
     * Multiply a derived unit by another derived unit
     * (<code>that</code>*<code>this</code>).
     */
    Unit inverseMultiply(final DerivedUnit that) throws UnitException {
        return that.multiply(factors);
    }
    
    /**
     * Retourne une unit� qui r�sulte de la multiplication de
     * <code>this</code> par les facteurs <code>f</code>.
     */
    final SimpleUnit multiply(final Factor[] f) {
        return multiply(factors, f, +1);
    }
    
    /**
     * Retourne une unit� qui r�sulte de la multiplication des
     * facteurs <code>f1</code> par les facteurs <code>f2</code>.
     */
    private static SimpleUnit multiply(final Factor[] f1, final Factor[] f2, final int power2) {
        final Factor[] factors=new Factor[f1.length + f2.length];
        System.arraycopy(f1, 0, factors, 0,         f1.length);
        System.arraycopy(f2, 0, factors, f1.length, f2.length);
        switch (power2) {
            case +1: break;
            case -1: for (int i=f1.length; i<factors.length; i++) factors[i]=factors[i].inverse(); break;
            default: throw new IllegalArgumentException(String.valueOf(power2));
        }
        return getInstance(factors);
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
     * Divide a base unit by a derived unit
     * (<code>that</code>/<code>this</code>).
     */
    Unit inverseDivide(final BaseUnit that) throws UnitException {
        return multiply(new Factor[] {Factor.getFactor(that, 1)}, factors, -1);
    }
    
    /**
     * Divide a derived unit by another derived unit
     * (<code>that</code>/<code>this</code>).
     */
    Unit inverseDivide(final DerivedUnit that) throws UnitException {
        return multiply(that.factors, factors, -1);
    }
    
    /**
     * Indicate whether or not this unit has the same dimensionality
     * or the reciprocal dimensionality as a base unit. Is is required
     * that <code>this.compareDimensionality(that)</code> give the same
     * result as <code>that.compareDimensionality(this)</code>.
     *
     * @param that The base unit.
     * @return <code>+1</code> if both units have the same dimensionality.<br>
     *         <code>-1</code> if the unit dimensionalities are reciprocals of
     *                         each other (for example <i>length</i>/<i>time</i>
     *                         and <i>time</i>/<i>length</i>).
     *         <code> 0</code> if the unit dimensionalities are neither the same
     *                         or reciprocals each other.
     */
    private int compareDimensionality(final BaseUnit that) {
        if (factors.length==1) {
            final Factor factor=factors[0];
            if (factor.baseUnit.equalsIgnoreSymbol(that)) {
                switch (factor.power) {
                    case -1: return -1;
                    case +1: return +1;
                }
            }
        }
        return 0;
    }
    
    /**
     * Indicate whether or not this unit has the same dimensionality
     * or the reciprocal dimensionality as a derived unit. Is is required
     * that <code>this.compareDimensionality(that)</code> give the same
     * result as <code>that.compareDimensionality(this)</code>.
     *
     * @param that The derived unit.
     * @return <code>+1</code> if both units have the same dimensionality.<br>
     *         <code>-1</code> if the unit dimensionalities are reciprocals of
     *                         each other (for example <i>length</i>/<i>time</i>
     *                         and <i>time</i>/<i>length</i>).
     *         <code> 0</code> if the unit dimensionalities are neither the same
     *                         or reciprocals each other.
     */
    private int compareDimensionality(final DerivedUnit that) {
        int result=0;
        int count=that.factors.length;
        final Factor[] cmpFactors=new Factor[count];
        System.arraycopy(that.factors, 0, cmpFactors, 0, count);
  loop: for (int i=0; i<factors.length; i++) {
            final Factor factor=factors[i];
            for (int j=0; j<cmpFactors.length; j++) {
                final int cmp=factor.compareDimensionality(cmpFactors[j]);
                if (cmp!=0) {
                    if (result!=cmp) {
                        if (result!=0) {
                            return 0;
                        }
                        result=cmp;
                    }
                    count--;
                    cmpFactors[j]=null;
                    continue loop;
                }
            }
            return 0;
        }
        if (count !=0) return 0; // if the two units don't have the same number of base units
        if (result==0) return 1; // if we compared two dimensionless units (the for loop has not been executed)
        return result;
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
           boolean canConvert(final BaseUnit    that) {return compareDimensionality(that)!=0;}
           boolean canConvert(final DerivedUnit that) {return compareDimensionality(that)!=0;}
    
    /**
     * Effectue la conversion d'une mesure exprim�e selon d'autres unit�s. Par
     * exemple <code>METRE_PER_SECOND.convert(1,&nbsp;KILOMETRE_PER_HOUR)</code>
     * retournera <code>0.2778</code>.
     *
     * @param value La valeur exprim�e selon les autres unit�s (<code>fromUnit</code>).
     * @param fromUnit Les autres unit�s.
     * @return La valeur convertie selon ces unit�s (<code>this</code>).
     * @throws UnitException Si les unit�s ne sont pas compatibles.
     */
    public double convert(final double value, final Unit fromUnit) throws UnitException {
        return fromUnit.inverseConvert(value, this); // Do not move in superclass
    }
    
    double convert(final double value, final BaseUnit fromUnit) throws UnitException {
        switch (compareDimensionality(fromUnit)) {
            case -1: return 1/value;
            case +1: return value;
            default: throw incompatibleUnitsException(fromUnit);
        }
    }
    
    double convert(final double value, final DerivedUnit fromUnit) throws UnitException {
        switch (compareDimensionality(fromUnit)) {
            case -1: return 1/value;
            case +1: return value;
            default: throw incompatibleUnitsException(fromUnit);
        }
    }
    
    /**
     * Effectue sur-place la conversion de mesures exprim�es selon d'autres
     * unit�s. Les valeurs converties remplaceront les anciennes valeurs.
     *
     * @param  values En entr�, les valeurs exprim�es selon les autres unit�s
     *         (<code>fromUnit</code>). En sortie, les valeurs exprim�es selon ces
     *         unit�s (<code>this</code>).
     * @param  fromUnit Les autres unit�s.
     * @throws UnitException Si les unit�s ne sont pas compatibles. Dans ce
     *         cas, aucun �l�ment de <code>values</code> n'aura �t� modifi�.
     */
    public void convert(final double[] values, final Unit fromUnit) throws UnitException {
        fromUnit.inverseConvert(values, this); // Do not move in superclass
    }
    
    void convert(final double[] values, final BaseUnit fromUnit) throws UnitException {
        switch (compareDimensionality(fromUnit)) {
            case -1: for (int i=0; i<values.length; i++) values[i]=1/values[i];
            case +1: break;
            default: throw incompatibleUnitsException(fromUnit);
        }
    }
    
    void convert(final double[] values, final DerivedUnit fromUnit) throws UnitException {
        switch (compareDimensionality(fromUnit)) {
            case -1: for (int i=0; i<values.length; i++) values[i]=1/values[i];
            case +1: break;
            default: throw incompatibleUnitsException(fromUnit);
        }
    }
    
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
    public void convert(final float[] values, final Unit fromUnit) throws UnitException {
        fromUnit.inverseConvert(values, this); // Do not move in superclass
    }
    
    void convert(final float[] values, final BaseUnit fromUnit) throws UnitException {
        switch (compareDimensionality(fromUnit)) {
            case -1: for (int i=0; i<values.length; i++) values[i]=1/values[i];
            case +1: break;
            default: throw incompatibleUnitsException(fromUnit);
        }
    }
    
    void convert(final float[] values, final DerivedUnit fromUnit) throws UnitException {
        switch (compareDimensionality(fromUnit)) {
            case -1: for (int i=0; i<values.length; i++) values[i]=1/values[i];
            case +1: break;
            default: throw incompatibleUnitsException(fromUnit);
        }
    }
    
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
    public UnitTransform getTransform(final Unit fromUnit) throws UnitException {
        return fromUnit.getInverseTransform(this); // Do not move in superclass
    }
    
    UnitTransform getTransform(final BaseUnit fromUnit) throws UnitException {
        switch (compareDimensionality(fromUnit)) {
            case -1: return InverseTransform   .getInstance(fromUnit, this);
            case +1: return IdentityTransform  .getInstance(fromUnit, this);
            default: throw  this.incompatibleUnitsException(fromUnit);
        }
    }
    
    UnitTransform getTransform(final DerivedUnit fromUnit) throws UnitException {
        switch (compareDimensionality(fromUnit)) {
            case -1: return InverseTransform   .getInstance(fromUnit, this);
            case +1: return IdentityTransform  .getInstance(fromUnit, this);
            default: throw  this.incompatibleUnitsException(fromUnit);
        }
    }
    
    /**
     * Convertit une mesure vers d'autre unit�s. Par exemple
     * <code>METRE_PER_SECOND.inverseConvert(1,&nbsp;KILOMETRE_PER_HOUR)</code>
     * retournera <code>3.6</code>. Cette m�thode est l'inverse de la m�thode
     * {@link #convert(double,Unit)}.
     *
     * @param  value La valeur exprim�e selon ces unit�s (<code>this</code>).
     * @param  toUnit Les autres unit�s.
     * @return La valeur convertie selon les autres unit�s (<code>toUnit</code>).
     * @throws UnitException Si les unit�s ne sont pas compatibles.
     */
    protected double inverseConvert(final double value, final Unit toUnit) throws UnitException {
        return toUnit.convert(value, this); // Do not move in superclass
    }
    
    double inverseConvert(final double value, final BaseUnit toUnit) throws UnitException {
        switch (compareDimensionality(toUnit)) {
            case -1: return 1/value;
            case +1: return value;
            default: throw toUnit.incompatibleUnitsException(this);
        }
    }
    
    double inverseConvert(final double value, final DerivedUnit toUnit) throws UnitException {
        switch (compareDimensionality(toUnit)) {
            case -1: return 1/value;
            case +1: return value;
            default: throw toUnit.incompatibleUnitsException(this);
        }
    }
    
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
    protected void inverseConvert(final double[] values, final Unit toUnit) throws UnitException {
        toUnit.convert(values, this); // Do not move in superclass
    }
    
    void inverseConvert(final double[] values, final BaseUnit toUnit) throws UnitException {
        switch (compareDimensionality(toUnit)) {
            case -1: for (int i=0; i<values.length; i++) values[i]=1/values[i];
            case +1: break;
            default: throw toUnit.incompatibleUnitsException(this);
        }
    }
    
    void inverseConvert(final double[] values, final DerivedUnit toUnit) throws UnitException {
        switch (compareDimensionality(toUnit)) {
            case -1: for (int i=0; i<values.length; i++) values[i]=1/values[i];
            case +1: break;
            default: throw toUnit.incompatibleUnitsException(this);
        }
    }
    
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
    protected void inverseConvert(final float[] values, final Unit toUnit) throws UnitException {
        toUnit.convert(values, this); // Do not move in superclass
    }
    
    void inverseConvert(final float[] values, final BaseUnit toUnit) throws UnitException {
        switch (compareDimensionality(toUnit)) {
            case -1: for (int i=0; i<values.length; i++) values[i]=1/values[i];
            case +1: break;
            default: throw toUnit.incompatibleUnitsException(this);
        }
    }
    
    void inverseConvert(final float[] values, final DerivedUnit toUnit) throws UnitException {
        switch (compareDimensionality(toUnit)) {
            case -1: for (int i=0; i<values.length; i++) values[i]=1/values[i];
            case +1: break;
            default: throw toUnit.incompatibleUnitsException(this);
        }
    }
    
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
    protected UnitTransform getInverseTransform(final Unit toUnit) throws UnitException {
        return toUnit.getTransform(this); // Do not move in superclass
    }
    
    UnitTransform getInverseTransform(final BaseUnit toUnit) throws UnitException {
        switch (compareDimensionality(toUnit)) {
            case -1: return InverseTransform     .getInstance(this, toUnit);
            case +1: return IdentityTransform    .getInstance(this, toUnit);
            default: throw  toUnit.incompatibleUnitsException(this);
        }
    }
    
    UnitTransform getInverseTransform(final DerivedUnit toUnit) throws UnitException {
        switch (compareDimensionality(toUnit)) {
            case -1: return InverseTransform     .getInstance(this, toUnit);
            case +1: return IdentityTransform    .getInstance(this, toUnit);
            default: throw  toUnit.incompatibleUnitsException(this);
        }
    }
    
    /**
     * Indique si deux unit�s sont �gales, en ignorant leurs symboles. Il n'est pas n�cessaire que
     * les unit�s fondamentales y apparaissent dans le m�me ordre. Par exemple, "m�*s" sera consid�r�
     * identique � "s*m�".
     */
    public boolean equalsIgnoreSymbol(final Unit unit) {
        return (unit instanceof DerivedUnit) && compareDimensionality((DerivedUnit) unit)==1;
    }
    
    /**
     * Retourne un code
     * pour cette unit�.
     */
    public int hashCode() {
        int code=92718538;
        for (int i=0; i<factors.length; i++) {
            code += factors[i].hashCode();
        }
        return code;
    }
}
