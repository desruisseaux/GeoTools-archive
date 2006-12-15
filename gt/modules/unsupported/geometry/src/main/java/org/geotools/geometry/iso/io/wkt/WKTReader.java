/*
 * This implementation of the OGC Feature Geometry Abstract Specification
 * (ISO 19107) is a project of the University of Applied Sciences Cologne
 * (Fachhochschule Köln) in collaboration with GeoTools and GeoAPI.
 *
 * Copyright (C) 2006 University of Applied Sciences Köln
 *                    (Fachhochschule Köln) and GeoTools
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * For more information, contact:
 *
 *     Prof. Dr. Jackson Roehrig
 *     Institut für Technologie in den Tropen
 *     Fachhochschule Köln
 *     Betzdorfer Strasse 2
 *     D-50679 Köln
 *     Jackson.Roehrig@fh-koeln.de
 *
 *     Sanjay Dominik Jena
 *     san.jena@gmail.com
 *
 */
/*
 * This class was copied from the JTS Topology Suite Version 1.7.2
 * of Vivid Solutions and modified and reused in this library under
 * the terms of GNU Lesser General Public Licence.
 * The original copyright of the Vivid Solutions JTS is stated as follows:
 *
 *------------------------------------------------------------------------
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 *------------------------------------------------------------------------
 */ 


package org.geotools.geometry.iso.io.wkt;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.geotools.geometry.iso.util.AssertionFailedException;
import org.opengis.spatialschema.geometry.geometry.GeometryFactory;
import org.opengis.spatialschema.geometry.geometry.LineString;
import org.opengis.spatialschema.geometry.geometry.Position;
import org.opengis.spatialschema.geometry.primitive.Curve;
import org.opengis.spatialschema.geometry.primitive.CurveSegment;
import org.opengis.spatialschema.geometry.primitive.OrientableCurve;
import org.opengis.spatialschema.geometry.primitive.Point;
import org.opengis.spatialschema.geometry.primitive.PrimitiveFactory;
import org.opengis.spatialschema.geometry.primitive.Ring;
import org.opengis.spatialschema.geometry.primitive.Surface;
import org.opengis.spatialschema.geometry.primitive.SurfaceBoundary;
import org.opengis.spatialschema.geometry.Geometry;

/**
 * 
 * @author sanjay
 * 
 */
public class WKTReader {

	private static final String EMPTY = "EMPTY";

	private static final String COMMA = ",";

	private static final String L_PAREN = "(";

	private static final String R_PAREN = ")";

	private PrimitiveFactory primitiveFactory;

	private GeometryFactory coordinateFactory;

	private StreamTokenizer tokenizer;

	/**
	 * Creates a reader that creates objects using the given
	 * {@link GeometryFactory}.
	 * 
	 * @param geometryFactory
	 *            the factory used to create <code>Geometry</code>s.
	 */
	public WKTReader(PrimitiveFactory aPrimitiveFactory,
			GeometryFactory aCoordinateFactory) {
		this.primitiveFactory = aPrimitiveFactory;
		this.coordinateFactory = aCoordinateFactory;
	}

	/**
	 * Reads a Well-Known Text representation of a {@link Geometry} from a
	 * {@link String}.
	 * 
	 * @param wellKnownText
	 *            one or more <Geometry Tagged Text>strings (see the OpenGIS
	 *            Simple Features Specification) separated by whitespace
	 * @return a <code>Geometry</code> specified by <code>wellKnownText</code>
	 * @throws ParseException
	 *             if a parsing problem occurs
	 */
	public Geometry read(String wellKnownText) throws ParseException {
		StringReader reader = new StringReader(wellKnownText);
		try {
			return read(reader);
		} finally {
			reader.close();
		}
	}

	/**
	 * Reads a Well-Known Text representation of a {@link Geometry} from a
	 * {@link Reader}.
	 * 
	 * @param reader
	 *            a Reader which will return a <Geometry Tagged Text> string
	 *            (see the OpenGIS Simple Features Specification)
	 * @return a <code>Geometry</code> read from <code>reader</code>
	 * @throws ParseException
	 *             if a parsing problem occurs
	 */
	public Geometry read(Reader reader) throws ParseException {
		tokenizer = new StreamTokenizer(reader);
		// set tokenizer to NOT parse numbers
		tokenizer.resetSyntax();
		tokenizer.wordChars('a', 'z');
		tokenizer.wordChars('A', 'Z');
		tokenizer.wordChars(128 + 32, 255);
		tokenizer.wordChars('0', '9');
		tokenizer.wordChars('-', '-');
		tokenizer.wordChars('+', '+');
		tokenizer.wordChars('.', '.');
		tokenizer.whitespaceChars(0, ' ');
		tokenizer.commentChar('#');

		try {
			return readGeometryTaggedText();
		} catch (IOException e) {
			throw new ParseException(e.toString());
		}
	}

	/**
	 * Returns the next array of <code>Coordinate</code>s in the stream.
	 * 
	 * @param tokenizer
	 *            tokenizer over a stream of text in Well-known Text format. The
	 *            next element returned by the stream should be L_PAREN (the
	 *            beginning of "(x1 y1, x2 y2, ..., xn yn)") or EMPTY.
	 * @return the next array of <code>Coordinate</code>s in the stream, or
	 *         an empty array if EMPTY is the next element returned by the
	 *         stream.
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws ParseException
	 *             if an unexpected token was encountered
	 */
	private Coordinate[] getCoordinates() throws IOException, ParseException {
		String nextToken = getNextEmptyOrOpener();
		if (nextToken.equals(EMPTY)) {
			return new Coordinate[] {};
		}
		ArrayList coordinates = new ArrayList();
		coordinates.add(getPreciseCoordinate());
		nextToken = getNextCloserOrComma();
		while (nextToken.equals(COMMA)) {
			coordinates.add(getPreciseCoordinate());
			nextToken = getNextCloserOrComma();
		}
		Coordinate[] array = new Coordinate[coordinates.size()];
		return (Coordinate[]) coordinates.toArray(array);
	}

	private Coordinate getPreciseCoordinate() throws IOException,
			ParseException {
		Coordinate coord = new Coordinate();
		coord.x = getNextNumber();
		coord.y = getNextNumber();
		if (isNumberNext()) {
			coord.z = getNextNumber();
		}
		// precisionModel.makePrecise(coord);
		return coord;
	}

	private boolean isNumberNext() throws IOException {
		int type = tokenizer.nextToken();
		tokenizer.pushBack();
		return type == StreamTokenizer.TT_WORD;
	}

	/**
	 * Parses the next number in the stream. Numbers with exponents are handled.
	 * 
	 * @param tokenizer
	 *            tokenizer over a stream of text in Well-known Text format. The
	 *            next token must be a number.
	 * @return the next number in the stream
	 * @throws ParseException
	 *             if the next token is not a valid number
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	private double getNextNumber() throws IOException, ParseException {
		int type = tokenizer.nextToken();
		switch (type) {
		case StreamTokenizer.TT_WORD: {
			try {
				return Double.parseDouble(tokenizer.sval);
			} catch (NumberFormatException ex) {
				throw new ParseException("Invalid number: " + tokenizer.sval);
			}
		}
		}
		parseError("number");
		return 0.0;
	}

	/**
	 * Returns the next EMPTY or L_PAREN in the stream as uppercase text.
	 * 
	 * @param tokenizer
	 *            tokenizer over a stream of text in Well-known Text format. The
	 *            next token must be EMPTY or L_PAREN.
	 * @return the next EMPTY or L_PAREN in the stream as uppercase text.
	 * @throws ParseException
	 *             if the next token is not EMPTY or L_PAREN
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	private String getNextEmptyOrOpener() throws IOException, ParseException {
		String nextWord = getNextWord();
		if (nextWord.equals(EMPTY) || nextWord.equals(L_PAREN)) {
			return nextWord;
		}
		parseError(EMPTY + " or " + L_PAREN);
		return null;
	}

	/**
	 * Returns the next R_PAREN or COMMA in the stream.
	 * 
	 * @param tokenizer
	 *            tokenizer over a stream of text in Well-known Text format. The
	 *            next token must be R_PAREN or COMMA.
	 * @return the next R_PAREN or COMMA in the stream
	 * @throws ParseException
	 *             if the next token is not R_PAREN or COMMA
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	private String getNextCloserOrComma() throws IOException, ParseException {
		String nextWord = getNextWord();
		if (nextWord.equals(COMMA) || nextWord.equals(R_PAREN)) {
			return nextWord;
		}
		parseError(COMMA + " or " + R_PAREN);
		return null;
	}

	/**
	 * Returns the next R_PAREN in the stream.
	 * 
	 * @param tokenizer
	 *            tokenizer over a stream of text in Well-known Text format. The
	 *            next token must be R_PAREN.
	 * @return the next R_PAREN in the stream
	 * @throws ParseException
	 *             if the next token is not R_PAREN
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	private String getNextCloser() throws IOException, ParseException {
		String nextWord = getNextWord();
		if (nextWord.equals(R_PAREN)) {
			return nextWord;
		}
		parseError(R_PAREN);
		return null;
	}

	/**
	 * Returns the next word in the stream.
	 * 
	 * @param tokenizer
	 *            tokenizer over a stream of text in Well-known Text format. The
	 *            next token must be a word.
	 * @return the next word in the stream as uppercase text
	 * @throws ParseException
	 *             if the next token is not a word
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	private String getNextWord() throws IOException, ParseException {
		int type = tokenizer.nextToken();
		switch (type) {
		case StreamTokenizer.TT_WORD:

			String word = tokenizer.sval;
			if (word.equalsIgnoreCase(EMPTY))
				return EMPTY;
			return word;

		case '(':
			return L_PAREN;
		case ')':
			return R_PAREN;
		case ',':
			return COMMA;
		}
		parseError("word");
		return null;
	}

	/**
	 * Throws a formatted ParseException for the current token.
	 * 
	 * @param expected
	 *            a description of what was expected
	 * @throws ParseException
	 * @throws AssertionFailedException
	 *             if an invalid token is encountered
	 */
	private void parseError(String expected) throws ParseException {
		// throws Asserts for tokens that should never be seen
		if (tokenizer.ttype == StreamTokenizer.TT_NUMBER)
			throw new ParseException("Unexpected NUMBER token");
		if (tokenizer.ttype == StreamTokenizer.TT_EOL)
			throw new ParseException("Unexpected EOL token");

		String tokenStr = tokenString();
		throw new ParseException("Expected " + expected + " but found "
				+ tokenStr);
	}

	/**
	 * Gets a description of the current token
	 * 
	 * @return a description of the current token
	 */
	private String tokenString() {
		switch (tokenizer.ttype) {
		case StreamTokenizer.TT_NUMBER:
			return "<NUMBER>";
		case StreamTokenizer.TT_EOL:
			return "End-of-Line";
		case StreamTokenizer.TT_EOF:
			return "End-of-Stream";
		case StreamTokenizer.TT_WORD:
			return "'" + tokenizer.sval + "'";
		}
		return "'" + (char) tokenizer.ttype + "'";
	}

	/**
	 * Creates a <code>Geometry</code> using the next token in the stream.
	 * 
	 * @param tokenizer
	 *            tokenizer over a stream of text in Well-known Text format. The
	 *            next tokens must form a &lt;Geometry Tagged Text&gt;.
	 * @return a <code>Geometry</code> specified by the next token in the
	 *         stream
	 * @throws ParseException
	 *             if the coordinates used to create a <code>Polygon</code>
	 *             shell and holes do not form closed linestrings, or if an
	 *             unexpected token was encountered
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	private Geometry readGeometryTaggedText() throws IOException,
			ParseException {
		String type = getNextWord();

		if (type.equals(WKTConstants.WKT_POINT)) {
			return readPointText();
		} else if (type.equalsIgnoreCase(WKTConstants.WKT_CURVE)) {
			return readLineStringText();
		} else if (type.equalsIgnoreCase(WKTConstants.WKT_SURFACE)) {
			return readPolygonText();
		}
		throw new ParseException("Unknown geometry type: " + type);
	}

	/**
	 * Creates a <code>Point</code> using the next token in the stream.
	 * 
	 * @param tokenizer
	 *            tokenizer over a stream of text in Well-known Text format. The
	 *            next tokens must form a &lt;Point Text&gt;.
	 * @return a <code>Point</code> specified by the next token in the stream
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws ParseException
	 *             if an unexpected token was encountered
	 */
	private Point readPointText() throws IOException, ParseException {
		String nextToken = getNextEmptyOrOpener();
		if (nextToken.equals(EMPTY)) {
			return null;
		}
		Point point = primitiveFactory.createPoint(this.getPreciseCoordinate()
				.getCoordinates());
		getNextCloser();
		return point;
	}

	/**
	 * Creates a <code>LineString</code> using the next token in the stream.
	 * 
	 * @param tokenizer
	 *            tokenizer over a stream of text in Well-known Text format. The
	 *            next tokens must form a &lt;LineString Text&gt;.
	 * @return a <code>LineString</code> specified by the next token in the
	 *         stream
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws ParseException
	 *             if an unexpected token was encountered
	 */
	private Curve readLineStringText() throws IOException, ParseException {
		return (Curve) this.createCurve(this.getCoordinates());
	}

	/**
	 * Creates a <code>LinearRing</code> using the next token in the stream.
	 * 
	 * @param tokenizer
	 *            tokenizer over a stream of text in Well-known Text format. The
	 *            next tokens must form a &lt;LineString Text&gt;.
	 * @return a <code>LinearRing</code> specified by the next token in the
	 *         stream
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws ParseException
	 *             if the coordinates used to create the <code>LinearRing</code>
	 *             do not form a closed linestring, or if an unexpected token
	 *             was encountered
	 */
	private Ring readLinearRingText() throws IOException, ParseException {
		List<OrientableCurve> curves = new ArrayList<OrientableCurve>();
		curves.add(this.createCurve(this.getCoordinates()));
		return this.primitiveFactory.createRing(curves);

	}

	/**
	 * Creates a <code>Polygon</code> using the next token in the stream.
	 * 
	 * @param tokenizer
	 *            tokenizer over a stream of text in Well-known Text format. The
	 *            next tokens must form a &lt;Polygon Text&gt;.
	 * @return a <code>Polygon</code> specified by the next token in the
	 *         stream
	 * @throws ParseException
	 *             if the coordinates used to create the <code>Polygon</code>
	 *             shell and holes do not form closed linestrings, or if an
	 *             unexpected token was encountered.
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	private Surface readPolygonText() throws IOException, ParseException {

		String nextToken = getNextEmptyOrOpener();

		if (nextToken.equals(EMPTY)) {
			return this.primitiveFactory.createSurface((SurfaceBoundary) null);
		}

		ArrayList<Ring> holes = new ArrayList<Ring>();
		Ring shell = this.readLinearRingText();
		nextToken = getNextCloserOrComma();
		while (nextToken.equals(COMMA)) {
			Ring hole = readLinearRingText();
			holes.add(hole);
			nextToken = getNextCloserOrComma();
		}
		SurfaceBoundary sfb = this.primitiveFactory.createSurfaceBoundary(
				shell, holes);
		return this.primitiveFactory.createSurface(sfb);
	}

	/**
	 * Creates a curve from a Coordinate array
	 * 
	 * @param aCoords
	 * @return
	 */
	private OrientableCurve createCurve(Coordinate[] aCoords) {
		List<Position> points = new ArrayList<Position>();
		for (int i = 0; i < aCoords.length; i++) {
			points
					.add(this.coordinateFactory
							.createPosition(this.coordinateFactory
									.createDirectPosition((aCoords[i]
											.getCoordinates()))));
		}
		// Create List of CurveSegment´s (LineString´s)
		LineString lineString = this.coordinateFactory.createLineString(points);
		List<CurveSegment> segments = new ArrayList<CurveSegment>();
		segments.add(lineString);
		return this.primitiveFactory.createCurve(segments);
	}

}
