/**
 * 
 */
package org.geotools.gce.imageio.asciigrid.raster;

import java.lang.ref.SoftReference;
import java.util.Vector;

/**
 * This class is responsible for converting a sequence of chars into a a double
 * number.
 * 
 * <p>
 * It is a utility class uses by the {@link AsciiGridRaster} class for
 * converting the input {@link String} into numbers. This class is highly
 * optimized for performances reasons.
 * 
 * <p>
 * This class is not thread safe!!!
 * 
 * 
 * <p>
 * <strong>Usage</strong>
 * <p>
 * 
 * <pre>
 *       	final DoubleConverter converter = new DoubleConverter();
 *         // If I need to load 10 samples, I need to count 9 spaces
 *       	while (...) {
 *       
 *       			ch = read();
 *       		
 *       
 *       			// /////////////////////////////////////////////////////////////////
 *       			// 
 *       			// Pushing the char into the converter
 *       			//
 *       			// /////////////////////////////////////////////////////////////////
 *       			if (converter.pushChar(ch)) {
 *       
 *       				// /////////////////////////////////////////////////////////////////
 *       				// 
 *       				// Get the new value
 *       				//
 *       				// /////////////////////////////////////////////////////////////////
 *       				value = converter.getValue(); 
 *       
 *       				// /////////////////////////////////////////////////////////////////
 *       				// 
 *       				// Consume the new value
 *       				//
 *       				// /////////////////////////////////////////////////////////////////
 *       				...
 *       				...
 *       				...
 *       
 *       			} else {
 *       				// /////////////////////////////////////////////////////////////////
 *       				// 
 *       				// Let's check the EOF.
 *       				//
 *       				// /////////////////////////////////////////////////////////////////
 *       				if (converter.isEof()) {
 *       					...
 *       					...
 *       					...
 *       				}
 *       			}
 *       		}
 * </pre>
 * 
 * <p>
 * It is worth to point out that when using this class it would be great to add
 * to the JVM the hint to augment the lifetime of {@link SoftReference} objects
 * sinche the underlying pool is based on them.
 * <p>
 * 
 * @author Simone Giannecchini
 * 
 */
final class DoubleConverter {
	/**
	 * Default number of {@link DoubleConverter} object to keep in the pool.
	 * 
	 * <p>
	 * I use this value also when enlarging the pool.
	 */
	private static final int CONVERTER_NUM = 50;

	/**
	 * Static pool pf double converters.
	 */
	static Vector pool;

	static {
		pool = new Vector(CONVERTER_NUM);
		enlargePool(CONVERTER_NUM);
	}

	private static void enlargePool(final int num) {
		for (int i = 0; i < num; i++)
			pool.add(new SoftReference(new DoubleConverter()));
	}

	// variables for arithmetic operations
	private double value = 0.0;

	private int valSign = 1;

	private double exponent = 0;

	private int expSign = 1;

	private double multiplier = 0.1;

	// variables to prevent illegal numbers
	private int expChar = 0; // count the E symbols within the same number

	private int decimalPoint = 0; // count the '.' within the same number

	private int digits = 0;

	private int decimalDigits = 0;

	private int expDigits = 0;

	private int prevCh = -1;

	private boolean eof = false;

	private double retValue;

	/**
	 * Constructor.
	 * 
	 */
	private DoubleConverter() {

	}

	/**
	 * Resets the converter.
	 * 
	 * <p>
	 * This method should be called each time a new value is consumed byt using
	 * {@link DoubleConverter#getValue()} method.
	 * 
	 */
	void reset() {
		// Resetting Values
		// variables for arithmetic operations
		value = 0.0;
		valSign = 1;
		exponent = 0;
		expSign = 1;
		multiplier = 0.1;

		// variables to prevent illegal numbers
		expChar = 0; // count the E symbols within the same number
		decimalPoint = 0; // count the '.' within the same number
		digits = 0;
		decimalDigits = 0;
		expDigits = 0;
		eof = false;
		prevCh = -1;

	}

	/**
	 * Pushes a new character to this converter.
	 * 
	 * <p>
	 * The converter parses the char and check if we have a new value. The value
	 * is not computed unless requested for performance reasons. Often it is
	 * needed to throw away values that are not needed because of subsampling or
	 * the like, hence it is a waste of resource explicitly computing them.
	 * 
	 * <p>
	 * It is worth to point out that after getting a false value it is crucial
	 * to check the {@link DoubleConverter#isEof()} to see if we reached to eof
	 * condition.
	 * 
	 * @param newChar
	 *            to parse.
	 * @return true if there is a value to get, false otherwise.
	 * @see {@link DoubleConverter#isEof()}
	 */
	boolean pushChar(final int newChar) {
		boolean retVal = false;
		// check if we read a white space or similar
		if ((newChar != 32) && (newChar != 10) && (newChar != 13)
				&& (newChar != 9) && (newChar != 0)) {
			if ((prevCh == 32) // ' '
					|| (prevCh == 10) // '\r'
					|| (prevCh == 13) // '\n'
					|| (prevCh == 9) // '\t'
					|| (prevCh == 0)) {
				// //
				//
				// End of whitespaces. I need to convert read bytes
				// in a double value and I set it as a sample of the
				// raster, if subsampling allows it.
				//
				// //

				// //
				//
				// Checks on the read value
				//
				// //
				if (((decimalPoint == 1) && (decimalDigits == 0))
						|| ((expChar == 1) && (expDigits == 0))
						|| ((digits == 0) && (decimalDigits == 0) && (expDigits > 0))) {
					// Illegal numbers. Example: 14.E8 ****
					// 12.5E **** E10
					throw new NumberFormatException(
							"An Illegal number was found:\nDigits = " + digits
									+ "\nDecimalPoints = " + decimalPoint
									+ "\nDecimalDigits = " + decimalDigits
									+ "\nE Symbols = " + expChar
									+ "\nDigits after E Symbol = " + expDigits
									+ "\n");
				}

				// we found a value
				retVal = true;
				compute();
				reset();

			}

			// //
			//
			// Analysis of current byte for next value
			//
			// //
			switch (newChar) {
			case 48: // '0'
			case 49: // '1'
			case 50: // '2'
			case 51: // '3'
			case 52: // '4'
			case 53: // '5'
			case 54: // '6'
			case 55: // '7'
			case 56: // '8'
			case 57: // '9'

				if ((decimalPoint == 0) && (expChar == 0)) {
					value = (value * 10) + (newChar - 48);
					digits++;
				} else if (expChar == 1) {
					exponent = (exponent * 10) + (newChar - 48);
					expDigits++;
				} else {
					value += ((newChar - 48) * multiplier);
					multiplier /= 10.0;
					decimalDigits++;
				}

				break;

			case 46: // '.'

				if (expChar > 0) {
					throw new NumberFormatException(
							"A Decimal point can't exists after the E symbol within the same number\n");
				}

				decimalPoint++; // The "++" prevents invalid number

				if (decimalPoint > 1) {
					throw new NumberFormatException(
							"The number contains more than 1 decimal point!\n");
				}

				// Illegal Number handled example:
				// 12.3.45
				multiplier = 0.1;

				break;

			case 45: // '-'

				if ((prevCh == 69) || (prevCh == 101)) {
					expSign = -1;
				} else {
					valSign = -1;
				}

				break;

			case 43: // '+'

				if ((prevCh == 69) || (prevCh == 101)) {
					expSign = 1;
				} else {
					valSign = 1;
				}

				break;

			case 42: // '*' NoData in GRASS Format
				value = Double.NaN;

				break;

			case 69: // 'E'
			case 101: // 'e'
				expChar++; // The "++" prevents invalid number

				if (expChar > 1) {
					throw new NumberFormatException(
							"The number contains more than one 'E' symbol !\n");
				}

				// Illegal Number handled example:
				// 12.6E23E45
				exponent = 0;
				expSign = 1;
				expDigits = 0;

				break;

			case -1:
				eof = true;
				break;

			default:
				throw new NumberFormatException(new StringBuffer(
						"Invalid data value was found. ASCII CODE : ").append(
						newChar).toString());
			}

		}
		prevCh = newChar; // store this byte for some checks
		return retVal;

	}

	/**
	 * Returns a value, if any was advertized by the
	 * {@link DoubleConverter#pushChar(int)} method.
	 * 
	 * @return the compuited value;
	 */
	void compute() {

		// If there is an exponent, I update the value
		if (exponent != 0) {
			value *= Math.pow(10.0, exponent * expSign);
		}
		// Applying the proper sign.
		retValue = value * valSign;
	}

	/**
	 * Did we find an EOF?
	 * 
	 * @return
	 */
	boolean isEof() {
		return eof;
	}

	/**
	 * Retrieves the current value if {@link #pushChar(int)} returned true.
	 * 
	 * @return
	 */
	double getValue() {
		return retValue;
	}

	/**
	 * Retrieves a poole {@link DoubleConverter} object.
	 * 
	 * @return
	 */
	static DoubleConverter acquire() {
		synchronized (pool) {
			SoftReference r;
			Object o;
			while (pool.size() > 0) {
				r = (SoftReference) pool.remove(0);
				o = r.get();
				if (o != null)
					return (DoubleConverter) o;

			}
			// we did not find any
			enlargePool(CONVERTER_NUM - 1);
			return new DoubleConverter();
		}
	}

	/**
	 * Reacquire a pooled {@link DoubleConverter}.
	 * 
	 * @param c
	 */
	static void release(DoubleConverter c) {

		pool.add(new SoftReference(c));
	}
}
