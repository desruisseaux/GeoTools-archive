/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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
 */
package org.geotools.resources;

// Input/output
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Locale;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

// Geotools dependencies
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;


/**
 * A helper class for parsing command-line arguments. Instance of this class
 * are usually created inside {@code main} methods. For example:
 *
 * <blockquote><pre>
 * public static void main(String[] args) {
 *     Arguments arguments = new Arguments(args);
 * }
 * </pre></blockquote>
 *
 * Then, method likes {@link #getRequiredString} or {@link #getOptionalString} can be used.
 * If a parameter is badly formatted or if a required parameter is not presents, then the
 * method {@link #illegalArgument} will be invoked with a message that describes the error.
 * The default implementation print the localized error message to standard output {@link #out}
 * and exits the virtual machine with a call to {@link System#exit} with error code 1.
 *
 * @since 2.0
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Arguments {
    /**
     * The preference name for default encoding.
     */
    private static final String ENCODING = "console.encoding";

    /**
     * Command-line arguments. Elements are set to
     * {@code null} after they have been processed.
     */
    private final String[] arguments;

    /**
     * Output stream to the console. This output stream will use
     * encoding specified in the <code>"-encoding" argument, if
     * present. Otherwise, encoding will be fetch from user's
     * preference.
     */
    public final PrintWriter out;

    /**
     * Error stream to the console. This output stream will use
     * encoding specified in the <code>"-encoding" argument, if
     * present. Otherwise, encoding will be fetch from user's
     * preference.
     */
    public final PrintWriter err;

    /**
     * The locale. Locale will be fetch from the <code>"-locale"</code>
     * argument, if present. Otherwise, the default locale will be used.
     */
    public final Locale locale;

    /**
     * Construct a console.
     *
     * @param args Command line arguments. Arguments "-encoding" and "-locale" will
     *             be automatically parsed.
     */
    public Arguments(final String[] args) {
        this.arguments     = (String[]) args.clone();
        this.locale        = getLocale(getOptionalString("-locale"));
        String encoding    = getOptionalString("-encoding");
        String destination = getOptionalString("-Xout"); // Non-supported parameter.
        PrintWriter out    = null;
        Exception error    = null;
        try {
            /*
             * If a destination file was specified,  open the file using the platform
             * default encoding or the specified encoding. Do not use encoding stored
             * in preference since they were usually for console encoding.
             */
            if (destination != null) {
                final Writer fileWriter;
                if (encoding != null) {
                    fileWriter = new OutputStreamWriter(new FileOutputStream(destination), encoding);
                } else {
                    fileWriter = new FileWriter(destination);
                }
                out = new PrintWriter(fileWriter);
            } else {
                /*
                 * If output to screen and no encoding has been specified,
                 * fetch the encoding from user's preferences.
                 */
                final Preferences prefs = Preferences.userNodeForPackage(Arguments.class);
                boolean prefEnc = false;
                if (encoding == null) {
                    encoding = prefs.get(ENCODING, null);
                    prefEnc  = true;
                }
                if (encoding != null) {
                    out = new PrintWriter(new OutputStreamWriter(System.out, encoding), true);
                    if (!prefEnc) {
                        prefs.put(ENCODING, encoding);
                    }
                }
            }
        } catch (IOException exception) {
            error = exception;
        }
        if (out == null) {
            out = new PrintWriter(System.out, true);
        }
        this.out = out;
        this.err = new PrintWriter(getWriter(System.err), true);
        if (error != null) {
            illegalArgument(error);
        }
    }

    /**
     * Returns the specified locale.
     *
     * @param  locale The programmatic locale string (e.g. "fr_CA").
     * @return The locale, or the default one if {@code locale} was null.
     * @throws IllegalArgumentException if the locale string is invalid.
     */
    private Locale getLocale(final String locale) throws IllegalArgumentException {
        if (locale != null) {
            final String[] s = Pattern.compile("_").split(locale);
            switch (s.length) {
                case 1:  return new Locale(s[0]);
                case 2:  return new Locale(s[0], s[1]);
                case 3:  return new Locale(s[0], s[1], s[2]);
                default: illegalArgument(new IllegalArgumentException(Errors.format(
                                         ErrorKeys.BAD_LOCALE_$1, locale)));
            }
        }
        return Locale.getDefault();
    }

    /**
     * Returns an optional string value from the command line. This method should be called
     * exactly once for each parameter. Second invocation for the same parameter will returns
     * {@code null}, unless the same parameter appears many times on the command line.
     * <p>
     * Paramater may be instructions like "-encoding cp850" or "-encoding=cp850".
     * Both forms (with or without "=") are accepted. Spaces around the '=' character,
     * if any, are ignored.
     *
     * @param  name The parameter name (e.g. "-encoding"). Name are case-insensitive.
     * @return The parameter value, of {@code null} if there is no parameter
     *         given for the specified name.
     */
    public String getOptionalString(final String name) {
        for (int i=0; i<arguments.length; i++) {
            String arg = arguments[i];
            if (arg != null) {
                arg = arg.trim();
                String value = "";
                int split = arg.indexOf('=');
                if (split >= 0) {
                    value = arg.substring(split+1).trim();
                    arg = arg.substring(0, split).trim();
                }
                if (arg.equalsIgnoreCase(name)) {
                    arguments[i] = null;
                    if (value.length() != 0) {
                        return value;
                    }
                    while (++i < arguments.length) {
                        value = arguments[i];
                        arguments[i] = null;
                        if (value==null) {
                            break;
                        }
                        value = value.trim();
                        if (split>=0) {
                            return value;
                        }
                        if (!value.equals("=")) {
                            return value.startsWith("=") ? value.substring(1).trim() : value;
                        }
                        split = 0;
                    }
                    illegalArgument(new IllegalArgumentException(Errors.getResources(locale).
                                    getString(ErrorKeys.MISSING_PARAMETER_VALUE_$1, arg)));
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Returns an required string value from the command line. This method
     * works like {@link #getOptionalString}, except that it will invokes
     * {@link #illegalArgument} if the specified parameter was not given
     * on the command line.
     *
     * @param  name The parameter name. Name are case-insensitive.
     * @return The parameter value.
     */
    public String getRequiredString(final String name) {
        final String value = getOptionalString(name);
        if (value == null) {
            illegalArgument(new IllegalArgumentException(Errors.getResources(locale).
                            getString(ErrorKeys.MISSING_PARAMETER_$1, name)));
        }
        return value;
    }

    /**
     * Returns an optional integer value from the command line. Numbers are parsed as
     * of the {@link Integer#parseInt(String)} method,  which means that the parsing
     * is locale-insensitive. Locale insensitive parsing is required in order to use
     * arguments in portable scripts.
     *
     * @param  name The parameter name. Name are case-insensitive.
     * @return The parameter value, of {@code null} if there is no parameter
     *         given for the specified name.
     */
    public Integer getOptionalInteger(final String name) {
        final String value = getOptionalString(name);
        if (value != null) try {
            return new Integer(value);
        } catch (NumberFormatException exception) {
            illegalArgument(exception);
        }
        return null;
    }

    /**
     * Returns a required integer value from the command line. Numbers are parsed as
     * of the {@link Integer#parseInt(String)} method,  which means that the parsing
     * is locale-insensitive. Locale insensitive parsing is required in order to use
     * arguments in portable scripts.
     *
     * @param  name The parameter name. Name are case-insensitive.
     * @return The parameter value.
     */
    public int getRequiredInteger(final String name) {
        final String value = getRequiredString(name);
        if (value != null) try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            illegalArgument(exception);
        }
        return 0;
    }

    /**
     * Returns an optional floating-point value from the command line. Numbers are parsed
     * as of the {@link Double#parseDouble(String)} method,  which means that the parsing
     * is locale-insensitive. Locale insensitive parsing is required in order to use
     * arguments in portable scripts.
     *
     * @param  name The parameter name. Name are case-insensitive.
     * @return The parameter value, of {@code null} if there is no parameter
     *         given for the specified name.
     */
    public Double getOptionalDouble(final String name) {
        final String value = getOptionalString(name);
        if (value != null) try {
            return new Double(value);
        } catch (NumberFormatException exception) {
            illegalArgument(exception);
        }
        return null;
    }

    /**
     * Returns a required floating-point value from the command line. Numbers are parsed
     * as of the {@link Double#parseDouble(String)} method, which means that the parsing
     * is locale-insensitive. Locale insensitive parsing is required in order to use
     * arguments in portable scripts.
     *
     * @param  name The parameter name. Name are case-insensitive.
     * @return The parameter value.
     */
    public double getRequiredDouble(final String name) {
        final String value = getRequiredString(name);
        if (value != null) try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            illegalArgument(exception);
        }
        return Double.NaN;
    }

    /**
     * Returns an optional boolean value from the command line.
     * The value, if defined, must be "true" or "false".
     *
     * @param  name The parameter name. Name are case-insensitive.
     * @return The parameter value, of {@code null} if there is no parameter
     *         given for the specified name.
     */
    public Boolean getOptionalBoolean(final String name) {
        final String value = getOptionalString(name);
        if (value != null) {
            if (value.equalsIgnoreCase("true" )) return Boolean.TRUE;
            if (value.equalsIgnoreCase("false")) return Boolean.FALSE;
            illegalArgument(new IllegalArgumentException(value));
        }
        return null;
    }

    /**
     * Returns a required boolean value from the command line.
     * The value must be "true" or "false".
     *
     * @param  name The parameter name. Name are case-insensitive.
     * @return The parameter value.
     */
    public boolean getRequiredBoolean(final String name) {
        final String value = getRequiredString(name);
        if (value != null) {
            if (value.equalsIgnoreCase("true" )) return true;
            if (value.equalsIgnoreCase("false")) return false;
            illegalArgument(new IllegalArgumentException(value));
        }
        return false;
    }

    /**
     * Returns {@code true} if the specified flag is set on the command line.
     * This method should be called exactly once for each flag. Second invocation
     * for the same flag will returns {@code false} (unless the same flag
     * appears many times on the command line).
     *
     * @param  name The flag name.
     * @return {@code true} if this flag appears on the command line, or {@code false}
     *         otherwise.
     */
    public boolean getFlag(final String name) {
        for (int i=0; i<arguments.length; i++) {
            String arg = arguments[i];
            if (arg!=null) {
                arg = arg.trim();
                if (arg.equalsIgnoreCase(name)) {
                    arguments[i] = null;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets a reader for the specified input stream. If the user specified an encoding
     * in some previous run of {@link Arguments}, then this encoding will be used.
     *
     * @param  in The input stream to wrap.
     * @return A {@link Reader} wrapping the specified input stream with the user's
     *         prefered encoding.
     */
    public static Reader getReader(final InputStream in) {
        final String encoding;
        encoding = Preferences.userNodeForPackage(Arguments.class).get(ENCODING, null);
        if (encoding != null) try {
            return new InputStreamReader(in, encoding);
        } catch (UnsupportedEncodingException exception) {
            // Should not occurs, since the character encoding was supported in some previous run...
            Utilities.unexpectedException("org.geotools.resources", "Arguments",
                                          "getReader", exception);
        }
        return new InputStreamReader(in);
    }

    /**
     * Gets a writer for the specified output stream. If the user specified an encoding
     * in some previous run of {@link Arguments}, then this encoding will be used.
     *
     * @param  out The output stream to wrap.
     * @return A {@link Writer} wrapping the specified output stream with the user's
     *         prefered encoding.
     */
    public static Writer getWriter(final OutputStream out) {
        final String encoding;
        encoding = Preferences.userNodeForPackage(Arguments.class).get(ENCODING, null);
        if (encoding != null) try {
            return new OutputStreamWriter(out, encoding);
        } catch (UnsupportedEncodingException exception) {
            // Should not occurs, since the character encoding was supported in some previous run...
            Utilities.unexpectedException("org.geotools.resources", "Arguments",
                                          "getWriter", exception);
        }
        return new OutputStreamWriter(out);
    }

    /**
     * Returns the list of unprocessed arguments. If the number of remaining arguments is
     * greater than the specified maximum, then this method invokes {@link #illegalArgument}.
     *
     * @param  max Maximum remaining arguments autorized.
     * @return An array of remaining arguments. Will never be longer than {@code max}.
     */
    public String[] getRemainingArguments(final int max) {
        int count=0;
        final String[] left = new String[Math.min(max, arguments.length)];
        for (int i=0; i<arguments.length; i++) {
            final String arg = arguments[i];
            if (arg != null) {
                if (count >= max) {
                    illegalArgument(new IllegalArgumentException(Errors.getResources(locale).
                                    format(ErrorKeys.UNKNOW_PARAMETER_$1, arguments[i])));
                }
                left[count++] = arg;
            }
        }
        return (String[]) XArray.resize(left, count);
    }

    /**
     * Prints a summary of the specified exception, without stack trace. This method
     * is invoked when a non-fatal (and somewhat expected) error occured, for example
     * {@link java.io.FileNotFoundException} when the file were specified in argument.
     *
     * @param exception An exception with a message describing the user's error.
     *
     * @since 2.3
     */
    public void printSummary(final Exception exception) {
        final String type = Utilities.getShortClassName(exception);
        String message = exception.getLocalizedMessage();
        if (message == null) {
            message = Vocabulary.format(VocabularyKeys.NO_DETAILS_$1, type);
        } else {
            err.print(type);
            err.print(": ");
        }
        err.println(message);
        err.flush();
    }

    /**
     * Invoked when an the user has specified an illegal parameter. The default
     * implementation prints the localized error message to the standard output
     * {@link #out}, and then exit the virtual machine.  User may override this
     * method if they want a different behavior.
     * <p>
     * This method <em>is not</em> invoked when an anormal error occured (for
     * example an unexpected {@code NullPointerException} in some of developper's
     * module). If such an error occurs, the normal exception mechanism will be used.
     *
     * @param exception An exception with a message describing the user's error.
     */
    protected void illegalArgument(final Exception exception) {
        printSummary(exception);
        System.exit(1);
    }
}
