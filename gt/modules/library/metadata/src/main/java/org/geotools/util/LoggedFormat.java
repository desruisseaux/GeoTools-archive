/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, Geotools Project Managment Committee (PMC)
 *    (C) 2007, Geomatys
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
package org.geotools.util;

import java.text.AttributedCharacterIterator;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;


/**
 * Wraps a {@link Format} object in order to either parse fully a string, or log a warning.
 * This class provides a {@link #parse} method which performs the following tasks:
 * <p>
 * <ul>
 *   <li>Checks if the string was fully parsed and log a warning if it was not. This is
 *       different than the default {@link #parseObject(String)} behavior which check only
 *       if the <em>begining</em> of the string was parsed and ignore any remaining characters.</li>
 *   <li>Ensures that the parsed object is of some specific class specified at construction time.</li>
 *   <li>If the string can't be fully parsed or is not of the expected class, logs a warning.</li>
 * </ul>
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class LoggedFormat<T> extends Format {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 4578880360344271325L;

    /**
     * The wrapped format.
     */
    private final Format format;

    /**
     * The expected type for the parsed values.
     */
    private final Class<T> type;

    /**
     * The logger where to log warnings, or {@code null} if none.
     *
     * @see #setLogger
     */
    private String logger;

    /**
     * The class to declare in as the warning emitter, or {@code null} if none.
     *
     * @see #setCaller
     */
    private String className;

    /**
     * The method to declare in as the warning emitter, or {@code null} if none.
     *
     * @see #setCaller
     */
    private String methodName;

    /**
     * Creates a new format wrapping the specified one.
     *
     * @param format The format to use for parsing and formatting.
     * @param type   The expected type of parsed values.
     */
    protected LoggedFormat(final Format format, final Class<T> type) {
        this.format = format;
        this.type   = type;
    }

    /**
     * Creates a new format wrapping the specified one.
     *
     * @param format The format to use for parsing and formatting.
     * @param type   The expected type of parsed values.
     */
    public static <T> LoggedFormat<T> getInstance(final Format format, final Class<T> type) {
        return new LoggedFormat<T>(format, type);
    }

    /**
     * Sets the logger where to send the warnings eventually emitted by the {@link #parse} method.
     *
     * @param logger The logger where to log warnings, or {@code null} if none.
     */
    public void setLogger(final String logger) {
        this.logger = logger;
    }

    /**
     * Sets the {@linkplain LogRecord#setSourceClassName source class name} and
     * {@linkplain LogRecord#setSourceMethodName source method name} for the warnings
     * eventually emitted by the {@link #parse} method.
     *
     * @param caller The class to declare as the warning emitter, or {@code null} if none.
     * @param method The method to declare as the warning emitter, or {@code null} if none.
     *
     * @todo Use Class.getSimpleName() or something like that when we will be allowed to compile
     *       for J2SE 1.5.
     */
    public void setCaller(final Class<?> caller, final String method) {
        this.className  = (caller != null) ? caller.getName() : null;
        this.methodName = method;
    }

    /**
     * Parses the specified string. If the string can't be parsed, then this method returns
     * {@code null}. If it can be parsed at least partially and is of the kind specified at
     * construction time, then it is returned. If the string has not been fully parsed, then
     * a {@linkplain LogRecord log record} is prepared and logged.
     *
     * @param  text The text to parse, or {@code null}.
     * @return The parsed object, or {@code null} if {@code text} was null or can't be parsed.
     */
    public T parse(String text) {
        if (text == null || (text=text.trim()).length() == 0) {
            return null;
        }
        final ParsePosition position = new ParsePosition(0);
        final Object value = parseObject(text, position);
        int index = position.getIndex();
        final int error = position.getErrorIndex();
        if (error >= 0 && error < index) {
            index = error;
        }
        if (index < text.length()) {
            logWarning(ErrorKeys.UNPARSABLE_STRING_$2, text, text.substring(index));
        } else if (value!=null && !type.isInstance(value)) {
            logWarning(ErrorKeys.ILLEGAL_CLASS_$2, value.getClass(), type);
            return null;
        }
        return type.cast(value);
    }

    /**
     * Parses text from a string to produce an object. This method delegates the work to the
     * {@linkplain Format format} specified at construction time. This method to not perform
     * any logging.
     *
     * @param  text The text to parse.
     * @return An object parsed from the string.
     * @throws ParseException if parsing failed.
     */
    @Override
    public Object parseObject(final String text) throws ParseException {
        return format.parseObject(text);
    }

    /**
     * Parses text from a string to produce an object. This method delegates the work to the
     * {@linkplain Format format} specified at construction time. This method to not perform
     * any logging.
     *
     * @param text The text to parse.
     * @param position Index and error index information.
     * @return An object parsed from the string, or {@code null} in case of error.
     */
    public Object parseObject(final String text, final ParsePosition position) {
        return format.parseObject(text, position);
    }

    /**
     * Formats the specified object. This method delegates the work to the
     * {@linkplain Format format} specified at construction time.
     *
     * @param value      The object to format.
     * @param toAppendTo The buffer where the text is to be appended.
     * @param position   Identifies a field in the formatted text.
     * @return           The string buffer passed in with formatted text appended.
     */
    public StringBuffer format(final Object value, final StringBuffer toAppendTo,
                               final FieldPosition position)
    {
        return format.format(value, toAppendTo, position);
    }

    /**
     * Formats the specified object. This method delegates the work to the
     * {@linkplain Format format} specified at construction time.
     *
     * @param value The object to format.
     * @return The character iterator describing the formatted value.
     */
    @Override
    public AttributedCharacterIterator formatToCharacterIterator(final Object value) {
        return format.formatToCharacterIterator(value);
    }

    /**
     * Logs a warning.
     *
     * @param key  The resource key.
     * @param arg1 First value to format in the message.
     * @param arg1 Second value to format in the message.
     */
    private void logWarning(final int key, final Object arg1, final Object arg2) {
        final LogRecord warning = Errors.getResources(getWarningLocale())
                .getLogRecord(Level.WARNING, key, arg1, arg2);
        if (className != null) {
            warning.setSourceClassName(className);
        }
        if (methodName != null) {
            warning.setSourceMethodName(methodName);
        }
        logWarning(warning);
    }

    /**
     * Logs a warning. This method is invoked automatically by the {@link #parse parse} method
     * when a text can't be fully parsed. The default implementation logs the warning to the
     * logger specified by the last call to the {@link #setLogger setLogger} method. Subclasses
     * may override this method if they want to change the log record before the logging.
     *
     * @param warning The warning to log.
     */
    protected void logWarning(final LogRecord warning) {
        if (logger != null) {
            Logger.getLogger(logger).log(warning);
        }
    }

    /**
     * Returns the locale to use for formatting warnings. The default implementation returns
     * the {@linkplain Locale#getDefault() default locale}.
     */
    protected Locale getWarningLocale() {
        return Locale.getDefault();
    }

    /**
     * Returns a string representation for debugging purpose.
     */
    @Override
    public String toString() {
        final StringBuffer buffer = new StringBuffer(Utilities.getShortClassName(this))
                .append('[').append(Utilities.getShortClassName(format));
        if (logger != null) {
            buffer.append(", logger=").append(logger);
        }
        return buffer.append(']').toString();
    }
}
