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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Iterator;

import javax.units.NonSI;
import javax.units.SI;

import org.geotools.resources.rsc.ResourceKeys;
import org.geotools.resources.rsc.Resources;
import org.geotools.resources.units.Quantities;
import org.geotools.resources.units.Units;
import org.geotools.util.WeakHashSet;


/**
 * Placeholder for future <code>Unit</code> class. This
 * skeleton will be removed when the real classes (from
 * <A HREF="http://www.jcp.org/jsr/detail/108.jsp">JSR-108:
 * Units specification</A>) will be publicly available.
 * <br><br>
 * <strong>IMPORTANT: future version will NOT be compatible
 * will this one. Remind, this is a temporary class!</strong>
 *
 * @version $Id$
 * @author Steven R. Emmerson
 * @author Bill Hibbard
 * @author Martin Desruisseaux
 *
 * @deprecated Replaced by the {@link javax.units.Unit} framework.
 */
public abstract class Unit implements Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 8745958719541785628L;
    
    /**
     * Banque des objets qui ont �t� pr�c�demment cr��s et
     * enregistr�s par un appel � la m�thode {@link #intern}.
     */
    private static final WeakHashSet pool=Prefix.pool; // Must be first!
    
    /**
     * Convenience constant for dimensionless unit.
     */
    public static final Unit DIMENSIONLESS = DerivedUnit.DIMENSIONLESS;
    
    /**
     * Convenience constant for base unit of angle.
     * Not a SI unit, but provides here for convenience.
     *
     * @deprecated Replaced by {@link SI#RADIAN}.
     */
    public static final Unit RADIAN = get("rad");
    
    /**
     * Convenience constant for unit of angle.
     * Not a SI unit, but provides here for convenience.
     *
     * @deprecated Replaced by {@link NonSI#DEGREE_ANGLE}.
     */
    public static final Unit DEGREE = get("\u00b0");
    
    /**
     * Convenience constant for base unit of length.
     *
     * @deprecated Replaced by {@link SI#METER}.
     */
    public static final Unit METRE = get("m");
    
    /**
     * Convenience constant for derived unit of length.
     *
     * @deprecated Replaced by <code>SI.KILO({@linkplain SI#METER})</code>.
     */
    public static final Unit KILOMETRE = METRE.scale(1000);
    
    /**
     * Convenience constant for base unit of time.
     *
     * @deprecated Replaced by {@link SI#SECOND}.
     */
    public static final Unit SECOND = get("s");
    
    /**
     * Convenience constant for unit of time.
     * Not a SI unit, but provides here for convenience.
     *
     * @deprecated Replaced by <code>SI.MILLI({@linkplain SI#SECOND})</code>.
     */
    public static final Unit MILLISECOND = get("ms");
    
    /**
     * Convenience constant for unit of time.
     * Not a SI unit, but provides here for convenience.
     *
     * @deprecated Replaced by {@link NonSI#DAY}.
     */
    public static final Unit DAY = get("d");
    
    /**
     * Convenience constant for base unit of mass.
     *
     * @deprecated Replaced by {@link SI#KILOGRAM}.
     */
    public static final Unit KILOGRAM = get("kg");

    /**
     * Unit of arc-second. Used by the EPSG database.
     *
     * @deprecated Replaced by {@link NonSI#SECOND_ANGLE}.
     */
    public static final Unit ARC_SECOND = DEGREE.scale(1.0/3600);
    
    /**
     * Convenience constant for "Degrees Minutes Secondes" unit.
     * For example, this "unit" convert 12.5� into 123000 (i.e.
     * the concatenation of 12�30'00"). In a strict sence, this
     * is a formatting issue rather than an unit transformation
     * issue. Such transformation would be better handle by the
     * {@link org.geotools.measure.AngleFormat} class. However, this
     * "unit" appears really often in the EPSG database, and we
     * need it for interoperability with legacy libraries.
     */
    public static final Unit DMS = (Unit) new DMSUnit(1).intern();
    
    /**
     * Convenience constant for "Degrees dot Minutes Secondes" unit.
     * For example, this "unit" convert 12.5� into 12.3 (i.e.
     * the concatenation of 12�30'00"). In a strict sence, this
     * is a formatting issue rather than an unit transformation
     * issue. Such transformation would be better handle by the
     * {@link org.geotools.measure.AngleFormat} class. However, this
     * "unit" appears really often in the EPSG database, and we
     * need it for interoperability with legacy libraries.
     */
    public static final Unit SEXAGESIMAL_DEGREE = (Unit) new DMSUnit(10000).intern();

    /**
     * Returns this unit as a <code>javax.units</code> object.
     */
    public final javax.units.Unit toJSR108() {
        if (equals(RADIAN     )) return SI.RADIAN;
        if (equals(DEGREE     )) return NonSI.DEGREE_ANGLE;
        if (equals(METRE      )) return SI.METER;
        if (equals(KILOMETRE  )) return SI.KILO(SI.METER);
        if (equals(SECOND     )) return SI.SECOND;
        if (equals(MILLISECOND)) return SI.MILLI(SI.SECOND);
        if (equals(DAY        )) return NonSI.DAY;
        if (equals(KILOGRAM   )) return SI.KILO(SI.KILOGRAM);
        if (equals(ARC_SECOND )) return NonSI.SECOND_ANGLE;
        throw new UnsupportedOperationException(toString());
    }
    
    /**
     * Symbole des unit�s de cet objet <code>Unit</code> (par exemple "kg").
     * Ce champs sera initialis� lors de la construction de chaque objet
     * <code>Unit</code> et ne sera jamais nul. Ce symbole peut commencer
     * par un des pr�fix �num�r�s dans le champ <code>prefix</code>. C'est
     * le cas par exemple des symboles "kg" (kilogramme) et "km" (kilom�tre).
     */
    /*public*/ final String symbol;
    
    /**
     * Ensemble des pr�fix qui peuvent �tre combin�s avec le symbole de l'unit�.
     * Cet ensemble peut contenir par exemple les pr�fix "milli" (m), "centi" (c) et
     * "kilo" (k) qui, combin�s avec les m�tres (m), donneront les millim�tres (mm),
     * centim�tres (cm) ou kilom�tre (km). Ce champ intervient lors des appels � la
     * m�thode {@link #scale}. Il peut �tre nul si aucun pr�fix n'est autoris� pour
     * le symbole.
     */
    /*public*/ final PrefixSet prefix;
    
    /**
     * Construit une unit� qui aura le symbole sp�cifi�.
     * @param  symbol Symbole de ces unit�s (ne doit pas �tre nul).
     * @param  prefix Ensemble des pr�fix utilisables avec {@link #symbol},
     *         ou <code>null</code> s'il n'y en a aucun. Cet ensemble sera
     *         affect� au champ {@link #prefix} et interviendra lors des
     *         appels � la m�thode {@link #scale}.
     * @throws NullPointerException Si <code>symbol</code> est nul.
     */
    /*protected*/ Unit(final String symbol, final PrefixSet prefix) throws NullPointerException {
        this.symbol=symbol.trim();
        this.prefix=prefix;
    }
    
    /**
     * Retourne les unit�s qui correspondent au symbole sp�cifi�. Si plus d'une
     * unit� correspond au symbole sp�cifi�, une unit� arbitraire sera choisie.
     * Si aucune unit� n'a �t� trouv�e, alors cette m�thode retourne <code>null</code>.
     *
     * @param  symbol Symbole des unit�s recherch�es. Cet argument ne doit pas �tre nul.
     * @return Les unit�s demand�es.
     */
    public static Unit get(final String symbol) {
        Object unit=null;
        Unit[] units=null;
        for (int i=0; i<3; i++) {
            switch (i) {
                case 2: units=getDefaultUnits(); // fallthrough
                case 1: unit=UnitFormat.DEFAULT.parse(symbol); break;
                case 0: unit=getCached(symbol);                break;
            }
            if (unit instanceof Unit) {
                return (Unit) unit;
            }
        }
        return null;
    }
    
    /**
     * Retourne l'ensemble des unit�s pr�d�finies par d�faut. Les unit�s seront
     * retourn�es sans ordre particulier. Si les unit�s par d�faut n'ont pas pu
     * �tre obtenues, cette m�thode retourne <code>null</code>.
     */
    private static Unit[] getDefaultUnits() {
        final InputStream in=Unit.class.getClassLoader().getResourceAsStream(UnitSet.PATHNAME);
        if (in!=null) try {
            final ObjectInputStream oin=new ObjectInputStream(in);
            final Unit[] units=(Unit[]) oin.readObject();
            oin.close();
            /*
             * Appelle 'intern()' simplement par pr�caution.
             * En principe, ce n'est pas n�cessaire.
             */
            for (int i=0; i<units.length; i++) {
                units[i]=units[i].intern();
            }
            return units;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }
    
    /**
     * Recherche une unit� correspondant au symbole sp�cifi�. Cette m�thode est temporaire.
     * Il serait plus efficace d'utiliser un objet {@link java.util.HashMap} qui ferait
     * correspondre les symboles avec des unit�s.
     */
    static Unit getCached(final String symbol) {
        for (final Iterator it=pool.iterator(); it.hasNext();) {
            final Object object=it.next();
            if (object instanceof Unit) {
                final Unit unit=(Unit) object;
                if (unit.symbol.equals(symbol)) return unit;
            }
        }
        return null;
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
    public abstract Unit rename(final String symbol, final PrefixSet prefix);
    
    /**
     * Retourne le nom de l'unit� dans la langue de l'utilisateur.
     * Par exemple le symbole "m" sera traduit par "m�tre" dans la
     * langue fran�aise. Si aucun nom n'est disponible pour l'unit�
     * courante, retourne simplement son symbole.
     */
    public String getLocalizedName() {
        return Units.localize(symbol);
    }
    
    /**
     * Retourne la quantit� que repr�sente cette unit�. Les quantit�s sont des cha�nes de
     * caract�res qui d�crivent le param�tre physique mesur�, comme "mass" ou "speed". Si
     * aucune quantit� n'est d�finie pour cette unit�, retourne <code>null</code>.
     */
    public abstract String getQuantityName();
    
    /**
     * Retourne la quantit� que repr�sente cette unit� dans la langue de l'utilisateur.
     * Les quantit�s sont des cha�nes de caract�res qui d�crivent le param�tre physique
     * mesur�, comme "masse" ou "vitesse". Si aucune quantit� n'est d�finie pour cette
     * unit�, retourne <code>null</code>.
     */
    public String getLocalizedQuantityName() {
        return Quantities.localize(getQuantityName());
    }
    
    /**
     * �l�ve ces unit�s � une puissance enti�re. Notez que ce ne sont pas toutes les
     * unit�s qui peuvent �tre �lev�es � une puissance. Par exemple les unit�s de
     * temp�rature en degr�s Celcius (�C), en Fahrenheit (�F) et la densit� sigma-T
     * ne peuvent pas �tre �lev�es � une puissance autre que 0 et 1.
     *
     *
     * L'impl�mentation par d�faut retourne une unit� sans dimension ou <code>this</code>
     * selon que <code>power</code> ait la valeur 0 ou 1 respectivement, et lance une exception
     * dans tous les autres cas.
     *
     * @param power La puissance � laquelle �lever cette unit�.
     * @return Les unit�s r�sultant de l'�l�vation des unit�s
     *         <code>this</code> � la puissance <code>power</code>.
     * @throws UnitException Si ces unit�s ne peuvent �tre
     *         �lev�es � une puissance autre que 0 et 1.
     *
     * @see #multiply
     * @see #divide
     * @see #scale
     * @see #shift
     */
    public Unit pow(final int power) throws UnitException {
        switch (power) {
            case 0:  return DerivedUnit.DIMENSIONLESS;
            case 1:  return this;
            default: throw new UnitException(Resources.format(
                                             ResourceKeys.ERROR_BAD_UNIT_POWER_$2,
                                             new Integer(power), this), this, null);
        }
    }
    
    /**
     * �l�ve ces unit�s � une puissance fractionnaire. Cette m�thode est utile entre
     * autre pour prendre la racine carr�e d'un nombre, ce qui revient � l'�lever � la
     * puissance �. L'impl�mentation par d�faut appele la m�thode {@link #pow(int)}
     * pour les puissances enti�res, et lance une exception dans tous les autres cas.
     *
     * @param power La puissance � laquelle �lever cette unit�.
     * @return Les unit�s r�sultant de l'�l�vation des unit�s
     *         <code>this</code> � la puissance <code>power</code>.
     * @throws UnitException Si ces unit�s ne peuvent �tre
     *         �lev�es � une puissance autre que 0 et 1.
     */
    public Unit pow(final double power) throws UnitException {
        final int integer=(int) power;
        if (integer==power) {
            return pow(integer);
        }
        throw new UnitException(Resources.format(
                                ResourceKeys.ERROR_BAD_UNIT_POWER_$2,
                                new Double(power), this), this, null);
    }
    
    /**
     * Multiplie cette unit� par une autre unit�.
     * L'impl�mentation par d�faut retourne <code>this</code> si <code>that</code> est
     * �gal � une unit� sans dimension, et lance une exception dams tous les autres cas.
     *
     * @param that L'unit� par laquelle multiplier cette unit�.
     * @return Le produit de <code>this</code> par <code>that</code>.
     * @throws UnitException Si les unit�s <code>this</code>
     *         <code>that</code> ne peuvent pas �tre multipli�es.
     *
     * @see #pow
     * @see #divide
     * @see #scale
     * @see #shift
     */
    public Unit multiply(final Unit that) throws UnitException {
        if (DerivedUnit.DIMENSIONLESS.equals(that)) {
            return this;
        }
        throw illegalUnitOperationException(that);
    }
    /** Overrided by {@link SimpleUnit}.*/ Unit inverseMultiply(BaseUnit    that) throws UnitException {throw that.illegalUnitOperationException(this);}
    /** Overrided by {@link SimpleUnit}.*/ Unit inverseMultiply(DerivedUnit that) throws UnitException {throw that.illegalUnitOperationException(this);}
    /** Overrided by {@link SimpleUnit}.*/ Unit inverseMultiply(ScaledUnit  that) throws UnitException {throw that.illegalUnitOperationException(this);}
    
    /**
     * Divise cette unit� par une autre unit�.
     * L'impl�mentation par d�faut retourne <code>this</code> si <code>that</code> est
     * �gal � une unit� sans dimension, et lance une exception dams tous les autres cas.
     *
     * @param that L'unit� par laquelle diviser cette unit�.
     * @return Le quotient de <code>this</code> par <code>that</code>.
     * @throws UnitException Si les unit�s <code>this</code>
     *         <code>that</code> ne peuvent pas �tre divis�es.
     *
     * @see #pow
     * @see #multiply
     * @see #scale
     * @see #shift
     */
    public Unit divide(final Unit that) throws UnitException {
        if (DerivedUnit.DIMENSIONLESS.equals(that)) {
            return this;
        }
        throw illegalUnitOperationException(that);
    }
    /** Overrided by {@link SimpleUnit}.*/ Unit inverseDivide(BaseUnit    that) throws UnitException {throw that.illegalUnitOperationException(this);}
    /** Overrided by {@link SimpleUnit}.*/ Unit inverseDivide(DerivedUnit that) throws UnitException {throw that.illegalUnitOperationException(this);}
    /** Overrided by {@link SimpleUnit}.*/ Unit inverseDivide(ScaledUnit  that) throws UnitException {throw that.illegalUnitOperationException(this);}
    
    /**
     * Cr�e une nouvelle unit� proportionnelle � cette unit�. Par exemple
     * pour convertir en kilom�tres des mesures exprim�es en m�tres, il
     * faut les diviser par 1000. On peut exprimer cette relation par le
     * code <code>Unit&nbsp;KILOMETRE=METRE.scale(1000)</code>.
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
    public abstract Unit scale(double amount);
    
    /**
     * Cr�e une nouvelle unit� d�cal�e par rapport � cette unit�. Par exemple
     * pour convertir des degr�s Kelvin en degr�s Celsius, il faut soustraire
     * 273.15 aux degr�s Kelvin. On peut exprimer cette relation par le code
     * <code>Unit&nbsp;CELCIUS=KELVIN.shift(273.15)</code>.
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
    public abstract Unit shift(double offset);
    
    /**
     * Indique si les unit�s <code>this</code> et <code>that</code> sont compatibles.
     * Si elles le sont, alors les m�thodes <code>convert</code> ne lanceront jamais
     * d'exception pour ces unit�s. Toutes les classes du paquet <code>org.geotools.units</code>
     * garantissent que <code>this.canConvert(that)</code> donnera toujours le m�me
     * r�sultat que <code>that.canConvert(this)</code>. Si vous �crivez vos propres
     * classes d�riv�es de <code>Unit</code>, vous devrez vous assurer que cette
     * condition reste respect�e. Mais �vitez d'appeller <code>that.canConvert(this)</code>
     * � l'int�rieur de cette m�thode sous peine de tomber dans une boucle sans fin.
     *
     * @param that Autre unit�s avec laquelle on veut
     *        v�rifier si ces unit�s sont compatibles.
     * @return <code>true</code> Si l'on garantie que les m�thodes
     *         <code>convert</code> ne lanceront pas d'exceptions.
     */
    public abstract boolean canConvert(Unit        that);
    /**SimpleUnit*/ boolean canConvert(BaseUnit    that) {return canConvert((Unit) that);}
    /**SimpleUnit*/ boolean canConvert(DerivedUnit that) {return canConvert((Unit) that);}
    
    /**
     * Effectue la conversion d'une mesure exprim�e selon d'autres unit�s. Par
     * exemple <code>METRE.convert(1,&nbsp;FOOT)</code> retournera <code>0.3048</code>.
     *
     * @param value La valeur exprim�e selon les autres unit�s (<code>fromUnit</code>).
     * @param fromUnit Les autres unit�s.
     * @return La valeur convertie selon ces unit�s (<code>this</code>).
     * @throws UnitException Si les unit�s ne sont pas compatibles.
     */
    public abstract double convert(double value, Unit        fromUnit) throws UnitException;
    /**SimpleUnit*/ double convert(double value, BaseUnit    fromUnit) throws UnitException {return convert(value, (Unit) fromUnit);}
    /**SimpleUnit*/ double convert(double value, DerivedUnit fromUnit) throws UnitException {return convert(value, (Unit) fromUnit);}
    
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
    public abstract void convert(double[] values, Unit        fromUnit) throws UnitException;
    /**SimpleUnit*/ void convert(double[] values, BaseUnit    fromUnit) throws UnitException {convert(values, (Unit) fromUnit);}
    /**SimpleUnit*/ void convert(double[] values, DerivedUnit fromUnit) throws UnitException {convert(values, (Unit) fromUnit);}
    
    /**
     * Effectue sur-place la conversion de mesures exprim�es selon d'autres
     * unit�s. Les valeurs converties remplaceront les anciennes valeurs.
     *
     * @param  values En entr�, les valeurs exprim�es selon les autres
     *         unit�s (<code>fromUnit</code>). En sortie, les valeurs exprim�es
     *         selon ces unit�s (<code>this</code>).
     * @param  fromUnit Les autres unit�s.
     * @throws UnitException Si les unit�s ne sont pas compatibles. Dans ce
     *         cas, aucun �l�ment de <code>values</code> n'aura �t� modifi�.
     */
    public abstract void convert(float[] values, Unit        fromUnit) throws UnitException;
    /**SimpleUnit*/ void convert(float[] values, BaseUnit    fromUnit) throws UnitException {convert(values, (Unit) fromUnit);}
    /**SimpleUnit*/ void convert(float[] values, DerivedUnit fromUnit) throws UnitException {convert(values, (Unit) fromUnit);}
    
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
    public abstract UnitTransform getTransform(Unit        fromUnit) throws UnitException;
    /**SimpleUnit*/ UnitTransform getTransform(BaseUnit    fromUnit) throws UnitException {return getTransform((Unit) fromUnit);}
    /**SimpleUnit*/ UnitTransform getTransform(DerivedUnit fromUnit) throws UnitException {return getTransform((Unit) fromUnit);}
    
    /**
     * Convertit une mesure vers d'autre unit�s. Par exemple
     * <code>METRE.inverseConvert(1,&nbsp;FOOT)</code> retournera
     * <code>3.2808</code>. Cette m�thode est l'inverse de la m�thode
     * {@link #convert(double,Unit)}. Bien que n'�tant pas destin�e �
     * �tre appell�e directement, les classes d�riv�es devront quand
     * m�me la d�finir pour un fonctionnement correcte.
     *
     * @param  value La valeur exprim�e selon ces unit�s (<code>this</code>).
     * @param  toUnit Les autres unit�s.
     * @return La valeur convertie selon les autres unit�s (<code>toUnit</code>).
     * @throws UnitException Si les unit�s ne sont pas compatibles.
     */
    /*protected*/ abstract double inverseConvert(double value, Unit        toUnit) throws UnitException;
    /**SimpleUnit*/        double inverseConvert(double value, BaseUnit    toUnit) throws UnitException {return inverseConvert(value, (Unit) toUnit);}
    /**SimpleUnit*/        double inverseConvert(double value, DerivedUnit toUnit) throws UnitException {return inverseConvert(value, (Unit) toUnit);}
    
    /**
     * Effectue sur-place la conversion de mesures vers d'autres unit�s.
     * Les valeurs converties remplaceront les anciennes valeurs. Cette
     * m�thode est l'inverse de la m�thode {@link #convert(double[],Unit)}.
     * Bien que n'�tant pas destin�e � �tre appell�e directement, les classes
     * d�riv�es devront quand m�me la d�finir pour un fonctionnement correcte.
     *
     * @param  values En entr�, les valeur exprim�es selon ces unit�s
     *         (<code>this</code>). En sortie, les valeurs exprim�es
     *         selon les autres unit�s (<code>toUnit</code>).
     * @param  toUnit Les autres unit�s.
     * @throws UnitException Si les unit�s ne sont pas compatibles. Dans ce
     *         cas, aucun �l�ment de <code>values</code> n'aura �t� modifi�.
     */
    /*protected*/ abstract void inverseConvert(double[] values, Unit        toUnit) throws UnitException;
    /**SimpleUnit*/        void inverseConvert(double[] values, BaseUnit    toUnit) throws UnitException {inverseConvert(values, (Unit) toUnit);}
    /**SimpleUnit*/        void inverseConvert(double[] values, DerivedUnit toUnit) throws UnitException {inverseConvert(values, (Unit) toUnit);}
    
    /**
     * Effectue sur-place la conversion de mesures vers d'autres unit�s.
     * Les valeurs converties remplaceront les anciennes valeurs. Cette
     * m�thode est l'inverse de la m�thode {@link #convert(float[],Unit)}.
     * Bien que n'�tant pas destin�e � �tre appell�e directement, les classes
     * d�riv�es devront quand m�me la d�finir pour un fonctionnement correcte.
     *
     * @param  values En entr�, les valeur exprim�es selon ces unit�s
     *         (<code>this</code>). En sortie, les valeurs exprim�es
     *         selon les autres unit�s (<code>toUnit</code>).
     * @param  toUnit Les autres unit�s.
     * @throws UnitException Si les unit�s ne sont pas compatibles. Dans ce
     *         cas, aucun �l�ment de <code>values</code> n'aura �t� modifi�.
     */
    /*protected*/ abstract void inverseConvert(float[] values, Unit        toUnit) throws UnitException;
    /**SimpleUnit*/        void inverseConvert(float[] values, BaseUnit    toUnit) throws UnitException {inverseConvert(values, (Unit) toUnit);}
    /**SimpleUnit*/        void inverseConvert(float[] values, DerivedUnit toUnit) throws UnitException {inverseConvert(values, (Unit) toUnit);}
    
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
    /*protected*/ abstract UnitTransform getInverseTransform(Unit        toUnit) throws UnitException;
    /**SimpleUnit*/        UnitTransform getInverseTransform(BaseUnit    toUnit) throws UnitException {return getInverseTransform((Unit) toUnit);}
    /**SimpleUnit*/        UnitTransform getInverseTransform(DerivedUnit toUnit) throws UnitException {return getInverseTransform((Unit) toUnit);}
    
    /**
     * Retourne une exception � lancer lorsque
     * l'op�ration demand�e n'est pas permise.
     */
    final UnitException illegalUnitOperationException(Unit that) {
        return new UnitException(Resources.format(
                                 ResourceKeys.ERROR_BAD_UNIT_OPERATION_$2, this, that), this, that);
    }
    
    /**
     * Retourne une exception � lancer lorsque
     * les unit�s ne sont pas compatibles.
     */
    final UnitException incompatibleUnitsException(Unit that) {
        return new UnitException(Resources.format(
                                 ResourceKeys.ERROR_NON_CONVERTIBLE_UNITS_$2, this, that), this, that);
    }
    
    /**
     * Apr�s la lecture binaire, v�rifie si les
     * unit�es lues existaient d�j� en m�moire.
     */
    final Object readResolve() throws ObjectStreamException {
        return intern();
    }
    
    /**
     * Retourne un exemplaire unique de cette unit�. Une banque d'unit�s, initialement
     * vide, est maintenue de fa�on interne par la classe <code>Unit</code>. Lorsque la
     * m�thode <code>intern</code> est appel�e, elle recherchera des unit�s �gales �
     * <code>this</code> au sens de la m�thode {@link #equals}. Si de telles unit�s
     * sont trouv�es, elles seront retourn�es. Sinon, les unit�s <code>this</code>
     * seront ajout�es � la banque de donn�es en utilisant une r�f�rence faible
     * et cette m�thode retournera <code>this</code>.
     * <br><br>
     * De cette m�thode il s'ensuit que pour deux unit�s <var>u</var> et <var>v</var>,
     * la condition <code>u.intern()==v.intern()</code> sera vrai si et seulement si
     * <code>u.equals(v)</code> est vrai.
     */
    /*protected*/ final Unit intern() {
        return (Unit) pool.canonicalize(this);
    }
    
    /**
     * Retourne un exemplaire unique de cette unit�, quel que soit son symbole. Une banque d'unit�s,
     * initialement vide, est maintenue de fa�on interne par la classe <code>Unit</code>. Lorsque la
     * m�thode <code>internIgnoreSymbol</code> est appel�e, elle recherchera des unit�s �gales �
     * <code>this</code> au sens de la m�thode {@link #equalsIgnoreSymbol}. Si de telles unit�s
     * sont trouv�es, elles seront retourn�es. Sinon, les unit�s <code>this</code> seront ajout�es
     * � la banque de donn�es en utilisant une r�f�rence faible et cette m�thode retournera <code>this</code>.
     * <br><br>
     * De cette m�thode il s'ensuit que pour deux unit�s <var>u</var> et <var>v</var>,
     * la condition <code>u.internIgnoreSymbol()==v.internIgnoreSymbol()</code> sera
     * g�n�ralement vrai si <code>u.equalsIgnoreSymbol(v)</code> est vrai. Toutefois,
     * si la banque de donn�es contient plusieurs unit�s identiques en tout sauf leurs
     * symboles, alors il n'y a aucune garantie de quelle unit� sera choisie par cette
     * m�thode.
     */
    /*protected*/ final Unit internIgnoreSymbol() {
        synchronized (pool) {
            final Object canonical = pool.get(new Unamed());
            if (canonical instanceof Unit) {
                return (Unit) canonical;
            }
            pool.add(this);
            return this;
        }
    }
    
    /**
     * Indique si deux unit�s sont �gales et utilisent le m�me symbole.
     */
    public boolean equals(final Object unit) {
        if (unit!=null) {
            if (unit instanceof Unamed) {
                return unit.equals(this);
            }
            if (getClass().equals(unit.getClass())) {
                final Unit cast = (Unit) unit;
                if (symbol.equals(cast.symbol)) {
                    if (equalsIgnoreSymbol(cast)) {
                        if (prefix==cast.prefix) return true;
                        if (prefix==null)        return false;
                        return prefix.equals(cast.prefix);
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Indique si deux unit�s sont �gales, en ignorant leurs symboles. Le
     * champs {@link #symbol} de chacune des deux unit�s ne sera pas pris
     * en compte.
     */
    /*public*/ abstract boolean equalsIgnoreSymbol(Unit unit);
    
    /**
     * Retourne un code propre � cette unit�. Le calcul de
     * ce code ne devrait pas prendre en compte le symbole
     * de l'unit�.
     */
    public abstract int hashCode();
    
    /**
     * Retourne les symboles de ces unit�s, par exemple "m/s".
     * S'il existe un symbole particulier pour la langue de
     * l'utilisateur, ce symbole sera retourn�.
     */
    public String toString() {
        return symbol;
    }
    
    
    
    
    /**
     * Enveloppe des unit�s qui seront compar�es sans tenir compte des symboles.
     * Cette classe est utilis�e par {@link #internIgnoreSymbol} afin de puiser
     * dans la banque d'unit�s {@link #pool} n'importe quel unit� de dimensions
     * appropri�es, quel que soit son symbole.
     *
     * @version 1.0
     * @author Martin Desruisseaux
     */
    private final class Unamed {
        /**
         * Renvoie le code des
         * unit�s envelopp�es.
         */
        public int hashCode() {
            return Unit.this.hashCode();
        }
        
        /**
         * V�rifie si les unit�s envelopp�es sont
         * �gales aux unit�s sp�cifi�es, sans tenir
         * compte de leurs symboles respectifs.
         */
        public boolean equals(final Object obj) {
            return (obj instanceof Unit) && equalsIgnoreSymbol((Unit) obj);
        }
    }
}
