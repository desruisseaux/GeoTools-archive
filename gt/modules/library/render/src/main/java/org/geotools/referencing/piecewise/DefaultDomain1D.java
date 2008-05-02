/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.referencing.piecewise;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Locale;
import java.util.MissingResourceException;

import org.geotools.renderer.i18n.Vocabulary;
import org.geotools.renderer.i18n.VocabularyKeys;
import org.geotools.util.NumberRange;
import org.geotools.util.SimpleInternationalString;
import org.opengis.util.InternationalString;

/**
 * Convenience implementation of the    {@link Domain1D}    interface.
 * @author    Simone Giannecchini
 */
public class DefaultDomain1D<T extends DomainElement1D>  extends AbstractList<T> implements Domain1D<T>{

	/*
	 * (non-Javadoc)
	 * @see org.geotools.referencing.piecewise.Domain1D#getDomainElements()
	 */
	public T[] getDomainElements() {
		return (T[]) elements.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.referencing.piecewise.Domain1D#getName()
	 */
	/**
     * @return
     * @uml.property  name="name"
     */
	public synchronized InternationalString getName() {
		if (name == null) {
			final StringBuffer buffer = new StringBuffer(30);
			final Locale locale = Locale.getDefault();
			if (main != null) {
				buffer.append(main.getName().toString(locale));
			} else {
				buffer.append('(');
				buffer.append(Vocabulary.getResources(locale).getString(
						VocabularyKeys.UNTITLED));
				buffer.append(')');
			}
			name = SimpleInternationalString.wrap(buffer.toString());
		}
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.referencing.piecewise.Domain1D#getRange()
	 */
	public NumberRange<?> getApproximateDomainRange() {
		synchronized (elements) {
			// @todo TODO should I include the NaN value?
			if (range == null) {
				NumberRange<?> range = null;
				for (DomainElement1D element:elements) {
					final NumberRange<?> extent = (NumberRange<?>) element.getRange();
					if (!Double.isNaN(extent.getMinimum())&& !Double.isNaN(extent.getMaximum())) {
						if (range != null) {
							range = NumberRange.wrap(range.union(extent));
						} else {
							range = extent;
						}
					}
				}
				this.range = range;
			}
			return range;
		}
	}

	/**
     * The list of elements. This list most be sorted in increasing order of left range element.
     * @uml.property  name="elements"
     * @uml.associationEnd  multiplicity="(0 -1)"
     */
	private DefaultDomainElement1D[] elements;

	/**
	 * {@code  true} if there is gaps between elements, or {@code  false}
	 * otherwise. A gap is found if for example the range of value is [-9999 ..
	 * -9999] for the first domain element and [0 .. 1000] for the second one.
	 */
	private boolean hasGaps;

	/**
	 * The "main" domain element, or {@code  null} if there is none. The main
	 * domain element is the quantitative domain element with the widest range of sample
	 * values.
	 */
	private DefaultDomainElement1D main;

	/**
     * List of   {@link #inputMinimum}   values for each domain element in  {@link #elements}   . This array <strong>must</strong> be in increasing order. Actually, this is the need to sort this array that determines the element order in   {@link #elements}   .
     * @uml.property  name="minimums"
     */
	private double[] minimums;

	/**
     * The name for this domain element list. Will be constructed only when first needed.
     * @see  #getName
     * @uml.property  name="name"
     */
	private InternationalString name;

	/**
     * The range of values in this domain element list. This is the union of the range of values of every elements, excluding   {@code      NaN}   values. This field will be computed only when first requested.
     * @uml.property  name="range"
     */
	private NumberRange<?> range;

	/**
	 * Constructor for {@link DefaultDomain1D}.
	 * 
	 * @param inDomainElements
	 *            {@link DomainElement1D} objects that make up this list.
	 */
	public DefaultDomain1D(DefaultDomainElement1D[] inDomainElements) {
		init(inDomainElements);
	}

	/**
	 * @param inDomainElements
	 * @throws IllegalArgumentException
	 * @throws MissingResourceException
	 */
	private void init(DefaultDomainElement1D[] inDomainElements)
			throws IllegalArgumentException, MissingResourceException {
		// @todo TODOCHECK ME
		if (inDomainElements == null)
			inDomainElements = new DefaultDomainElement1D[] { new DefaultPassthroughPiecewiseTransform1DElement("p0") };

		// /////////////////////////////////////////////////////////////////////
		//
		// input checks
		//
		// /////////////////////////////////////////////////////////////////////
		PiecewiseUtilities.ensureNonNull("DomainElement1D[]", inDomainElements);

		// /////////////////////////////////////////////////////////////////////
		//
		// Get the input elements and check that the type is correct.
		//
		// /////////////////////////////////////////////////////////////////////
		this.elements = (DefaultDomainElement1D[]) inDomainElements.clone();

		// /////////////////////////////////////////////////////////////////////
		//
		// Sort the input elements.
		//
		// /////////////////////////////////////////////////////////////////////
		if (this.elements != null)
			Arrays.sort(this.elements);

		// /////////////////////////////////////////////////////////////////////
		//
		// Construct the array of minimum values. During the loop, we make sure
		// there is no overlapping in input and output.
		//
		// /////////////////////////////////////////////////////////////////////
		hasGaps = false;
		minimums = new double[elements.length];
		for (int i = 0; i < elements.length; i++) {
			final DefaultDomainElement1D c = elements[i];
			final double inMinimum = minimums[i] = c.getInputMinimum();
			if (i != 0) {
				assert !(inMinimum < minimums[i - 1]) : inMinimum;
				// Use '!' to accept NaN.
				final DefaultDomainElement1D previous = elements[i - 1];
				if (PiecewiseUtilities.compare(inMinimum, previous
						.getInputMaximum()) <= 0) {
					PiecewiseUtilities.domainElementsOverlap(elements, i);
				}
				// Check if there is a gap between this domain element and the
				// previous one.
				if (!Double.isNaN(inMinimum)
						&& inMinimum != ((NumberRange<?>) previous.getRange()).getMaximum(false)) {
					hasGaps = true;
				}
			}
		}


		/*
		 * Search for what seems to be the "main" domain element. This loop looks for
		 * the quantitative domain element (if there is one) with the widest range of
		 * sample values.
		 */
		double range = 0;
		DefaultDomainElement1D main = null;
		for (int i = elements.length; --i >= 0;) {
			final DefaultDomainElement1D candidate = elements[i];
			if (Double.isInfinite(candidate.getInputMinimum())
					&& Double.isInfinite(candidate.getInputMaximum())) {
				range = Double.POSITIVE_INFINITY;
				main = candidate;
				continue;
			}
			final double candidateRange = candidate.getInputMaximum()
					- candidate.getInputMinimum();
			if (candidateRange >= range) {
				range = candidateRange;
				main = candidate;
			}

		}
		this.main = main;

		// postcondition
		assert PiecewiseUtilities.isSorted(elements);
	}

	/**
	 * Returns the domain element of the specified sample value. If no domain element fits,
	 * then this method returns {@code null}.
	 * 
	 * @param value
	 *            The value.
	 * @return The domain element of the supplied value, or {@code null}.
	 */
	public T getDomainElement(final double value) {

		int i = getDomainElementIndex(value);

		// //
		//
		// Checks
		//
		// //
		if (i < 0)
			return null;
		DefaultDomainElement1D domainElement1D;
		if (i > elements.length)
			return null;

		// //
		//
		// First of all let's check if we spotted a break point in out domains
		// element. If so the index we got is not an insertion point but it is
		// an actual domain element index. This happens when we catch precisely
		// a minimum element for a domain.
		//
		// //
		if (i < elements.length) {
			domainElement1D = elements[i];
			if (domainElement1D.contains(value))
				return (T) domainElement1D;
			// if the index was 0, unles we caught the smallest minimum we have
			// got something smaller than the leftmost domain
			if (i == 0)
				return null;
		}
		// //
		//
		// Ok, now we know that we did not precisely caught a minimum for a
		// domain, we therefore got an insertion point. This means that, unless
		// we have fallen into a gap we need to subtract 1 to check for
		// inclusion in the right domain.
		//
		// //
		domainElement1D = elements[i - 1];
		if (domainElement1D.contains(value))
			return (T) domainElement1D;

		// //
		//
		// Well, if we get here, we have definitely fallen into a gap or the
		// value is beyond the limits of the last domain, too bad....
		//
		// //
		assert i >= elements.length || hasGaps : value;
		return null;
	}

	/**
	 * @param sample
	 * @return
	 */
	private int getDomainElementIndex(final double sample) {
		int i = -1;
		// Special 'binarySearch' for NaN
		i = PiecewiseUtilities.binarySearch(minimums, sample);

		if (i >= 0) {
			// The value is exactly equals to one of minimum,
			// or is one of NaN values. There is nothing else to do.
			assert Double.doubleToRawLongBits(sample) == Double
					.doubleToRawLongBits(minimums[i]);
			return i;
		}

		assert i == Arrays.binarySearch(minimums, sample) : i;
		// 'binarySearch' found the index of "insertion point" (-(insertion
		// point) - 1). The
		// insertion point is defined as the point at which the key would be
		// inserted into the list: the index of the first element greater than
		// the key, or list.size(), if all elements in the list are less than
		// the specified key. Note that this guarantees that the return value
		// will be >= 0 if and only if the key is found.
		i = -i - 1;
		return i;
	}

	// ////////////////////////////////////////////////////////////////////////////////////////
	// ////// ////////
	// ////// I M P L E M E N T A T I O N O F List I N T E R F A C E ////////
	// ////// ////////
	// ////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the number of elements in this list.
	 */
	public int size() {
		return elements.length;
	}

	/**
	 * Returns the element at the specified position in this list.
	 */
	public T get(final int i) {
		return (T) elements[i];
	}

	/**
	 * Returns all elements in this {@code }.
	 */
	public Object[] toArray() {
		return (DomainElement1D[]) elements.clone();
	}

	/**
	 * Compares the specified object with this domain element list for equality. If
	 * the two objects are instances of the {@link DefaultDomain1D} class, then
	 * the test check for the equality of the single elements.
	 */
	public boolean equals(final Object object) {
		if (object.getClass().equals(this.getClass())) {
			final DefaultDomain1D<?> that = (DefaultDomain1D<?>) object;
			if (Arrays.equals(this.elements, that.elements)) {
				assert Arrays.equals(this.minimums, that.minimums);
				return true;
			}
			return false;
		}
		return super.equals(object);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.referencing.piecewise.Domain1D#hasGaps()
	 */
	public boolean hasGaps() {
		return hasGaps;
	}

	/**
     * Return what seems to be the main   {@link DomainElement1D}   for this list.
     * @return   what seems to be the main   {@link DomainElement1D}   for this list.
     * @uml.property  name="main"
     */
	public DomainElement1D getMain() {
		return main;
	}

	/**
     * @return
     * @uml.property  name="minimums"
     */
	public double[] getMinimums() {
		return (double[]) minimums.clone();
	}

	public boolean contains(Object o) {
		if (o instanceof Double) {
			return getDomainElement(((Double) o).doubleValue()) != null;
		}
		return false;
	}

}