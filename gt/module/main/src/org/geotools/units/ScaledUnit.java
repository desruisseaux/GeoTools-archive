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
import org.geotools.resources.XMath;
import org.geotools.resources.rsc.ResourceKeys;
import org.geotools.resources.rsc.Resources;
import org.geotools.resources.units.Units;


/**
 * Classe repr�sentant des unit�s proportionnelles � d'autres unit�s.
 * Les valeurs exprim�es selon les unit�s d'un objet <code>ScaledUnit</code>
 * peuvent �tre converties vers le syst�me d'unit�s {@link #unit} � l'aide de
 * l'�quation suivante:
 *
 * <blockquote><pre>
 * <var>x</var><sub>{@link #unit}</sub>&nbsp;=&nbsp;<var>x</var><sub><code>this</code></sub>&nbsp;*&nbsp;{@link #amount}
 * </pre></blockquote>
 *
 * Les objets <code>ScaledUnit</code> permettent de faire des conversions
 * entre diff�rentes unit�s, par exemple des pieds en m�tres. Cette classe
 * n'ayant pas de constructeur publique, la seule fa�on d'obtenir des unit�s
 * proportionnelles est d'utiliser les m�thodes {@link #getInstance} ou
 * {@link #scale}.
 *
 * @version 1.0
 * @author Steven R. Emmerson
 * @author Bill Hibbard
 * @author Martin Desruisseaux
 *
 * @deprecated Replaced by the {@link javax.units.Unit} framework.
 */
/*public*/ 
final class ScaledUnit extends Unit {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -5387831470112872874L;
    
    /**
     * Inverse d'une petite valeur servant � �viter des erreurs d'arrondissements.
     * Cette valeur est d�finie arbitrairement � 2^24, soit environ 1.678E+7.
     */
    private static final double INV_EPS = 16777216;
    
    /**
     * Le facteur par lequel multiplier les mesures exprim�es
     * selon ces unit�s pour obtenir des mesures exprim�es
     * selon les unit�s {@link #unit}.
     */
    public final double amount;
    
    /**
     * Les unit�s vers lesquelles se feront les conversions.
     * Il ne peut s'agir que d'unit�s de bases (par exemple
     * "m" ou "s") ou d�riv�es (par exemple "m/s").
     */
    public final SimpleUnit unit;
    
    /**
     * Construit une unit� proportionnelle � l'unit� de base ou d�riv�e
     * sp�cifi�e. <code>new&nbsp;Scale(0.44704,&nbsp;unit)</code> cr��
     * une unit� de milles/heure si <code>unit</code> repr�sentait des
     * m�tres/seconde.
     *
     * @param amount Le facteur proportionnel.
     * @param unit L'unit� de base ou d�riv�e associ�e.
     * @param symbol Le symbole associ� � cette unit�. Ne doit pas �tre nul.
     * @param prefix Liste des pr�fix pouvant �tre plac�s devant le symbole
     *        <code>symbol</code>, ou <code>null</code> s'il n'y en a pas.
     */
    private ScaledUnit(final double amount, final SimpleUnit unit, final String symbol, final PrefixSet prefix) {
        super(symbol, prefix);
        this.amount = amount;
        this.unit   = unit;
        
        if (unit==null) {
            throw new NullPointerException(Resources.format(ResourceKeys.ERROR_NO_UNIT));
        }
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount==0) {
            throw new IllegalArgumentException(Resources.format(
                                               ResourceKeys.ERROR_NOT_DIFFERENT_THAN_ZERO_$1,
                                               new Double(amount)));
        }
    }
    
    /**
     * Cr�e une nouvelle unit� de m�me dimension que l'unit� sp�cifi�e,
     * mais dont les donn�es devront �tre multipli�e par un facteur.
     * Les conversions d'unit�s se feront par l'�quation suivante:
     *
     * <center>
     *     <var>x</var><sub><code>unit</code></sub> =
     *     <var>x</var><sub><code>new</code></sub> * <code>amount</code>
     * </center>
     *
     * o� <var>x</var><sub><code>unit</code></sub> repr�sente une quantit� mesur�e
     * selon les unit�s <code>unit</code> et <var>x</var><sub><code>new</code></sub>
     * repr�sente une quantit� mesur�e selon les unit�s retourn�es par cette m�thode.
     *
     * Les unit�s cr�es par cette m�thode <code>getInstance</code> servent
     * � passer d'un syst�me de d'unit�s � l'autre. Par exemple on pourrait
     * construire des unit�s de miles anglais avec le code suivant:
     *
     * <blockquote><pre>
     *     Unit MILE=ScaledUnit.<strong>getInstance</strong>(1609, METRE);
     * </blockquote></pre>
     *
     * Ce code signifie qu'il faudra compter 1609 m�tres dans chaque mille anglais.
     *
     * @param  amount Facteur par lequel il faudra multiplier les mesures
     *         exprim�es selon les nouvelles unit�s pour les convertir dans
     *         les unit�s <code>unit</code>.
     * @param  unit Unit�s proportionnelles aux unit�s � cr�er. Les nouvelles
     *         unit�s cr��es repr�senteront <code>amount</code> de ces unit�s.
     * @return Les unit�s cr��es, ou <code>unit</code> si le
     *         param�tre <code>amount</code> est �gal � 1.
     *
     * @see BaseUnit#getInstance
     * @see DerivedUnit#getInstance
     * @see OffsetUnit#getInstance
     */
    public static Unit getInstance(final double amount, final SimpleUnit unit) {
        return getInstance(amount, unit, null, null);
    }
    
    /**
     * Cr�e une nouvelle unit� de m�me dimension que l'unit� sp�cifi�e,
     * mais dont les donn�es devront �tre multipli�e par un facteur.
     * Les conversions d'unit�s se feront par l'�quation suivante:
     *
     * <center>
     *     <var>x</var><sub><code>unit</code></sub> =
     *     <var>x</var><sub><code>new</code></sub> * <code>amount</code>
     * </center>
     *
     * o� <var>x</var><sub><code>unit</code></sub> repr�sente une quantit� mesur�e
     * selon les unit�s <code>unit</code> et <var>x</var><sub><code>new</code></sub>
     * repr�sente une quantit� mesur�e selon les unit�s retourn�es par cette m�thode.
     *
     * Les unit�s cr�es par cette m�thode <code>getInstance</code> servent
     * � passer d'un syst�me de d'unit�s � l'autre. Par exemple on pourrait
     * construire des unit�s de miles anglais avec le code suivant:
     *
     * <blockquote><pre>
     *     Unit MILE=ScaledUnit.<strong>getInstance</strong>(1609, METRE, "mile", null);
     * </blockquote></pre>
     *
     * Ce code signifie qu'il faudra compter 1609 m�tres dans chaque mille anglais.
     *
     * @param  amount Facteur par lequel il faudra multiplier les mesures
     *         exprim�es selon les nouvelles unit�s pour les convertir dans
     *         les unit�s <code>unit</code>.
     * @param  unit Unit�s proportionnelles aux unit�s � cr�er. Les nouvelles
     *         unit�s cr��es repr�senteront <code>amount</code> de ces unit�s.
     * @param  symbol Le symbole qui repr�sentera les unit�s cr��es. Si
     *         nul, alors un symbole par d�faut sera cr��. Dans l'exemple
     *         pr�c�dent, ce symbole par d�faut serait "\u00D71609&nbsp;m"
     * @param  prefix Liste des pr�fix pouvant �tre plac�s devant le symbole
     *         <code>symbol</code>, ou <code>null</code> s'il n'y en a pas.
     * @return Les unit�s cr��es, ou <code>unit</code> si le
     *         param�tre <code>amount</code> est �gal � 1.
     *
     * @see BaseUnit#getInstance
     * @see DerivedUnit#getInstance
     * @see OffsetUnit#getInstance
     */
    public static Unit getInstance(double amount, final SimpleUnit unit, String symbol, final PrefixSet prefix) {
        /*
         * Si <code>amount</code> est presqu'une puissance de 10, arrondi
         * � la puissance de 10 la plus proche. Cette �tape vise � r�duire
         * certaines erreurs d'arrondissement.
         */
        final double power = Math.rint(XMath.log10(amount)*INV_EPS)/INV_EPS;
        if (power==Math.rint(power)) {
            amount=XMath.pow10(power);
        }
        /*
         * Retourne les unit�s.
         */
        if (amount==1) {
            if (unit==null) {
                throw new NullPointerException(Resources.format(ResourceKeys.ERROR_NO_UNIT));
            }
            if (symbol!=null) {
                // TODO: Que faire si le symbole sp�cifi�
                //       n'est pas le symbole de 'unit'?
            }
            return unit;
        }
        if (symbol==null) {
            symbol = UnitFormat.DEFAULT.formatScaled(amount, unit, new StringBuffer()).toString();
            return new ScaledUnit(amount, unit, symbol, null).internIgnoreSymbol();
        } else {
            return new ScaledUnit(amount, unit, symbol, prefix).intern();
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
    public Unit rename(final String symbol, final PrefixSet prefix) { // CAST
        return getInstance(amount, unit, symbol, prefix);
    }
    
    /**
     * Retourne le nom de l'unit� dans la langue de l'utilisateur.
     * Par exemple le symbole "cm" sera traduit par "centim�tre"
     * dans la langue fran�aise. Si aucun nom n'est disponible
     * pour l'unit� courante, retourne simplement son symbole.
     */
    public String getLocalizedName() {
        PrefixSet prefix=unit.prefix;
        String    unpref=unit.getUnprefixedSymbol();
        if (prefix!=null && symbol.endsWith(unpref)) {
            final Prefix p=prefix.getPrefix(symbol.substring(0, symbol.length()-unpref.length()));
            if (p!=null) {
                return p.getLocalizedName()+Units.localize(unpref);
            }
        }
        return super.getLocalizedName();
    }
    
    /**
     * Retourne la quantit� que repr�sente cette unit�. Les quantit�s sont des cha�nes de
     * caract�res qui d�crivent le param�tre physique mesur�, comme "mass" ou "speed". Si
     * aucune quantit� n'est d�finie pour cette unit�, retourne <code>null</code>.
     */
    public String getQuantityName() {
        return unit.getQuantityName();
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
            default: return getInstance(Math.pow(amount, power), /*CAST*/ (SimpleUnit) unit.pow(power));
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
        if (integer==power) {
            return pow(integer);
        }
        return getInstance(Math.pow(amount, power), /*CAST*/ (SimpleUnit) unit.pow(power));
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
     * Multiply a base unit by a scaled unit
     * (<code>that</code>*<code>this</code>).
     */
    Unit inverseMultiply(final BaseUnit that) throws UnitException {
        final SimpleUnit unitThatThis = (SimpleUnit) unit.inverseMultiply(that);
        return (unitThatThis!=unit) ? getInstance(amount, unitThatThis) : this;
    }
    
    /**
     * Multiply a derived unit by a scaled unit
     * (<code>that</code>*<code>this</code>).
     */
    Unit inverseMultiply(final DerivedUnit that) throws UnitException {
        final SimpleUnit unitThatThis = (SimpleUnit) unit.inverseMultiply(that);
        return (unitThatThis!=unit) ? getInstance(amount, unitThatThis) : this;
    }
    
    /**
     * Multiply a scaled unit by a scaled unit
     * (<code>that</code>*<code>this</code>).
     */
    Unit inverseMultiply(final ScaledUnit that) throws UnitException {
        final double newAmount = that.amount*this.amount;
        final SimpleUnit unitThatThis = (SimpleUnit) that.unit.multiply(unit);
        return getInstance(newAmount, unitThatThis);
    }
    
    /**
     * Divise cette unit� par une autre unit�.
     *
     * @param  that L'unit� par laquelle diviser cette unit�.
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
     * Divise a base unit by a scaled unit
     * (<code>that</code>/<code>this</code>).
     */
    Unit inverseDivide(final BaseUnit that) throws UnitException {
        final SimpleUnit unitThatThis = /*CAST*/ (SimpleUnit) unit.inverseDivide(that);
        return (unitThatThis!=unit) ? getInstance(amount, unitThatThis) : this;
    }
    
    /**
     * Divide a derived unit by a scaled unit
     * (<code>that</code>/<code>this</code>).
     */
    Unit inverseDivide(final DerivedUnit that) throws UnitException {
        final SimpleUnit unitThatThis = /*CAST*/ (SimpleUnit) unit.inverseDivide(that);
        return (unitThatThis!=unit) ? getInstance(amount, unitThatThis) : this;
    }
    
    /**
     * Divide a scaled unit by a scaled unit
     * (<code>that</code>/<code>this</code>).
     */
    Unit inverseDivide(final ScaledUnit that) throws UnitException {
        final double newAmount = that.amount/this.amount;
        final SimpleUnit unitThatThis = /*CAST*/ (SimpleUnit) that.unit.divide(unit);
        return getInstance(newAmount, unitThatThis);
    }
    
    /**
     * Cr�e une nouvelle unit� proportionnelle � cette unit�. Par exemple
     * pour convertir en kilom�tres des mesures exprim�es en m�tres, il
     * faut les diviser par 1000. On peut exprimer cette relation par le
     * code <code>Unit&nbsp;km=metre.scale(1000)</code>.
     *
     * @param  amount Facteur par lequel il faudra diviser les valeurs
     *         exprim�es selon ces unit�s pour obtenir des valeurs
     *         exprim�es selon les nouvelles unit�s.
     * @return Les nouvelles unit�s.
     *
     * @see #pow
     * @see #multiply
     * @see #divide
     * @see #shift
     */
    public Unit scale(final double amount) {
        return (amount==1) ?  this : getInstance(this.amount*amount, unit);
    }
    
    /**
     * Cr�e une nouvelle unit� d�cal�e par rapport � cette unit�. Par exemple
     * pour convertir des degr�s Kelvin en degr�s Celsius, il faut soustraire
     * 273.15 aux degr�s Kelvin. On peut exprimer cette relation par le code
     * <code>Unit&nbsp;celsius=kelvin.shift(273.15)</code>.
     *
     * @param  offset Constante � soustraire aux valeurs exprim�es selon ces
     *         unit�s pour obtenir des valeurs exprim�es selon les nouvelles
     *         unit�s.
     * @return Les nouvelles unit�s.
     *
     * @see #pow
     * @see #multiply
     * @see #divide
     * @see #scale
     */
    public Unit shift(final double offset) {
        return (offset==0) ? this : OffsetUnit.getInstance(offset, this);
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
    public boolean canConvert(final Unit that) {
        return unit.canConvert(that);
    }
    
    /**
     * Effectue la conversion d'une mesure exprim�e selon d'autres unit�s. Par
     * exemple <code>METRE.convert(1,&nbsp;FOOT)</code> retournera <code>0.3048</code>.
     *
     * @param value    La valeur exprim�e selon les autres unit�s (<code>fromUnit</code>).
     * @param fromUnit Les autres unit�s.
     * @return         La valeur convertie selon ces unit�s (<code>this</code>).
     * @throws         UnitException Si les unit�s ne sont pas compatibles.
     */
    public double convert(final double value, final Unit fromUnit) throws UnitException {
        if (fromUnit==this) {
            return value; // sligh optimization
        }
        return unit.convert(value, fromUnit)/amount;
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
        if (!equalsIgnoreSymbol(fromUnit)) {
            unit.convert(values, fromUnit);
            for (int i=0; i<values.length; i++) {
                values[i] /= amount;
            }
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
        if (!equalsIgnoreSymbol(fromUnit)) {
            unit.convert(values, fromUnit);
            for (int i=0; i<values.length; i++) {
                values[i] = (float) (values[i]/amount);
            }
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
        if (!equalsIgnoreSymbol(fromUnit)) {
            final UnitTransform tr=unit.getTransform(fromUnit);
            if (tr instanceof ScaledTransform) {
                return ScaledTransform.getInstance(fromUnit, this, ((ScaledTransform) tr).amount*amount);
            } else {
                return CompoundTransform.getInstance(tr, ScaledTransform.getInstance(unit, this, amount));
            }
        }  else {
            return IdentityTransform.getInstance(fromUnit, this);
        }
    }
    
    /**
     * Convertit une mesure vers d'autre unit�s. Par exemple
     * <code>METRE.inverseConvert(1,&nbsp;FOOT)</code> retournera
     * <code>3.2808</code>. Cette m�thode est l'inverse de la m�thode
     * {@link #convert(double,Unit)}.
     *
     * @param value  La valeur exprim�e selon ces unit�s (<code>this</code>).
     * @param toUnit Les autres unit�s.
     * @return       La valeur convertie selon les autres unit�s (<code>toUnit</code>).
     * @throws       UnitException Si les unit�s ne sont pas compatibles.
     */
    protected double inverseConvert(final double value, final Unit toUnit) throws UnitException {
        if (toUnit==this) {
            return value; // sligh optimization
        }
        return unit.inverseConvert(value*amount, toUnit);
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
        if (!equalsIgnoreSymbol(toUnit)) {
            if (unit.canConvert(toUnit)) {
                for (int i=0; i<values.length; i++) {
                    values[i] *= amount;
                }
                unit.inverseConvert(values, toUnit);
            } else {
                throw toUnit.incompatibleUnitsException(this);
            }
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
        if (!equalsIgnoreSymbol(toUnit)) {
            if (unit.canConvert(toUnit)) {
                for (int i=0; i<values.length; i++) {
                    values[i] = (float) (values[i]*amount);
                }
                unit.inverseConvert(values, toUnit);
            } else {
                throw toUnit.incompatibleUnitsException(this);
            }
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
        if (!equalsIgnoreSymbol(toUnit)) {
            final UnitTransform tr=InverseTransform.getInstance(unit, toUnit);
            if (tr instanceof ScaledTransform) {
                final double amount=((ScaledTransform) tr).amount/this.amount;
                return ScaledTransform.getInstance(this, toUnit, amount);
            }  else {
                return CompoundTransform.getInstance(ScaledTransform.getInstance(this, unit, 1/amount), tr);
            }
        } else {
            return IdentityTransform.getInstance(this, toUnit);
        }
    }
    
    /**
     * Indique si deux unit�s sont �gales, en ignorant leurs symboles. Le
     * champs {@link #symbol} de chacune des deux unit�s ne sera pas pris
     * en compte.
     */
    public boolean equalsIgnoreSymbol(final Unit unit) {
        if (unit instanceof ScaledUnit) {
            final ScaledUnit that = (ScaledUnit) unit;
            return Double.doubleToLongBits(this.amount)==Double.doubleToLongBits(that.amount) &&
            this.unit.equalsIgnoreSymbol(that.unit);
        } else {
            return false;
        }
    }
    
    /**
     * Retourne un code
     * pour cette unit�.
     */
    public int hashCode() {
        final long code = Double.doubleToLongBits(amount);
        return (int) code ^ (int) (code >>> 32) ^ unit.hashCode();
    }
}
