/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, Geotools Project Managment Committee (PMC)
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
package org.geotools.util.logging;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.regex.Pattern;


/**
 * An adapter that redirect all Java logging events to an other logging framework. This
 * class redefines the {@link #severe(String) severe}, {@link #warning(String) warning},
 * {@link #info(String) info}, {@link #config(String) config}, {@link #fine(String) fine},
 * {@link #finer(String) finer} and {@link #finest(String) finest} methods as <em>abstract</em>
 * ones. Subclasses should implement those methods in order to map Java logging levels to
 * the backend logging framework.
 * <p>
 * All {@link #log(Level,String) log} methods are overriden in order to redirect to one of the
 * above-cited methods. Note that this is the opposite approach than the Java logging framework
 * one, which implemented everything on top of {@link Logger#log(LogRecord)}. This adapter is
 * defined in terms of {@link #severe(String) severe} &hellip; {@link #finest(String) finest}
 * methods instead because external frameworks like <a href="http://commons.apache.org/logging/">Commons-logging</a>
 * don't work with {@link LogRecord}, and sometime provides nothing else than convenience methods
 * equivalent to {@link #severe(String) severe} &hellip; {@link #finest(String) finest}.
 * <p>
 * <b>Restrictions</b><br>
 * Because the configuration is expected to be fully controled by the external logging
 * framework, this adapter disables all following features:
 * <ul>
 *   <li>{@link #addHandler},
 *       since the handling is performed by the external framework.</li>
 *
 *   <li>{@link #setUseParentHandlers},
 *       since this adapter never delegates to the parent handlers (because the parent
 *       logger belongs to Java logging and we avoid mixing frameworks).</li>
 *
 *   <li>{@link #setFilter} except for {@link #log(LogRecord)},
 *       because it may be argued that filtering belong to the external logging framework, and
 *       because it is difficult to query efficiently the filter in this {@code LoggerAdapter}
 *       architecture (e.g. we would need to make sure that {@link Filter#isLoggable} is invoked
 *       only once even if a {@code log} call is cascaded into many other {@code log} calls, and
 *       this test must works in multi-threads environment).</li>
 *
 *   <li>{@link #getResourceBundle}
 *       because this logger is always created without resource bundles. Localization must be
 *       performed through explicit calls to a {@code logrb} or {@link #log(LogRecord)} method.
 *       This is suffisient for GeoTools needs, which performs all localizations through the
 *       later. Note that those methods will be slower with this {@code LoggerAdapter} than with
 *       the default Java logging framework because this adapter will localizes and formats the
 *       record immediately instead of letting the {@linkplain Handler} performs this work only
 *       if needed.</li>
 * </ul>
 * <p>
 * <b>Logging levels</b><br>
 * If a log record {@linkplain Level level} is not one of the predefined ones, then this class
 * maps to the first level below the specified one. For example if a log record has some level
 * between {@link Level#FINE FINE} and {@link Level#FINER FINER}, then the {@link #finer finer}
 * method will be invoked. See {@link #isLoggable} for implementation tips taking advantage of
 * this rule.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see Logging
 */
public abstract class LoggerAdapter extends Logger {
    /**
     * The pattern to use for detecting {@link MessageFormat}.
     */
    private static final Pattern MESSAGE_FORMAT = Pattern.compile("\\{\\d+\\}");

    /**
     * Creates a new logger.
     *
     * @param name The logger name.
     */
    protected LoggerAdapter(final String name) {
        super(name, null);
        super.setUseParentHandlers(false); // Don't invoke the overriden method.
    }

    /**
     * Sets the level for this logger. Subclasses must redirect the call to the external
     * logging framework, or do nothing if the level can not be changed programmatically.
     */
    @Override
    public abstract void setLevel(Level level);

    /**
     * Returns the level for this logger. Subclasses shall get this level from the
     * external logging framework.
     */
    @Override
    public abstract Level getLevel();

    /**
     * Returns the level for {@link #entering}, {@link #exiting} and {@link #throwing} methods.
     * The default implementation returns {@link Level#FINER}, which is consistent with the
     * value used in the Java logging framework. Subclasses should override this method if
     * a different debug level is wanted.
     */
    protected Level getDebugLevel() {
        return Level.FINER;
    }

    /**
     * Returns {@code true} if the specified level is loggable.
     * <p>
     * <b>Implementation tip</b><br>
     * Given that {@link Level#intValue} for all predefined levels are documented in the {@link Level}
     * specification and are multiple of 100, given that integer divisions are rounded toward zero and
     * given rule documented in this class javadoc, then logging levels can be efficiently mapped to
     * predefined levels using {@code switch} statements as below. This statement has good chances to
     * be compiled to the {@code tableswitch} bytecode rather than {@code lookupswitch} (see
     * <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Compiling.doc.html#14942">Compiling
     * Switches</a> in <cite>The Java Virtual Machine Specification</cite>).
     *
     * <blockquote><pre>
     * @SuppressWarnings("fallthrough")
     * public boolean isLoggable(Level level) {
     *     final int n = level.intValue();
     *     switch (n / 100) {
     *         default: if (n &lt; 0) return false; // fallthrough otherwise.
     *         case 10: return isSevereLoggable ();
     *         case  9: return isWarningLoggable();
     *         case  8: return isInfoLoggable   ();
     *         case  7: return isConfigLoggable ();
     *         case  6: // fallthrough
     *         case  5: return isFineLoggable   ();
     *         case  4: return isFinerLoggable  ();
     *         case  3: return isFinestLoggable ();
     *         case  2: // fallthrough
     *         case  1: // fallthrough
     *         case  0: return false; // Logging OFF
     *     }
     * }
     * </pre></blockquote>
     */
    @Override
    public abstract boolean isLoggable(Level level);

    /**
     * Logs a {@link Level#SEVERE SEVERE} message.
     */
    @Override
    public abstract void severe(String message);

    /**
     * Logs a {@link Level#SEVERE SEVERE} message with an exception. The default implementation
     * discards the exception and invokes <code>{@linkplain #severe severe}(message)}</code>.
     * Subclasses should override this method if the external framework supports exception
     * loggings.
     * <p>
     * This method is protected rather than public because users should never invoke
     * this method directly. Doing so would require a cast from {@code Logger} to
     * {@code LoggerAdapter}, which would broke any program wanting to work directly
     * with the Java logging framework.
     */
    protected void severe(String message, Throwable thrown) {
        severe(message);
    }

    /**
     * Logs a {@link Level#WARNING WARNING} message.
     */
    @Override
    public abstract void warning(String message);

    /**
     * Logs a {@link Level#WARNING WARNING} message with an exception. The default implementation
     * discards the exception and invokes <code>{@linkplain #warning warning}(message)}</code>.
     * <p>
     * This method is protected for the same reason than {@link #severe(String,Throwable) severe}.
     */
    protected void warning(String message, Throwable thrown) {
        warning(message);
    }

    /**
     * Logs an {@link Level#INFO INFO} message.
     */
    @Override
    public abstract void info(String message);

    /**
     * Logs a {@link Level#INFO INFO} message with an exception. The default implementation
     * discards the exception and invokes <code>{@linkplain #info info}(message)}</code>.
     * <p>
     * This method is protected for the same reason than {@link #severe(String,Throwable) severe}.
     */
    protected void info(String message, Throwable thrown) {
        info(message);
    }

    /**
     * Logs an {@link Level#CONFIG CONFIG} message.
     */
    @Override
    public abstract void config(String message);

    /**
     * Logs a {@link Level#CONFIG CONFIG} message with an exception. The default implementation
     * discards the exception and invokes <code>{@linkplain #config config}(message)}</code>.
     * <p>
     * This method is protected for the same reason than {@link #severe(String,Throwable) severe}.
     */
    protected void config(String message, Throwable thrown) {
        config(message);
    }

    /**
     * Logs a {@link Level#FINE FINE} message.
     */
    @Override
    public abstract void fine(String message);

    /**
     * Logs a {@link Level#FINE FINE} message with an exception. The default implementation
     * discards the exception and invokes <code>{@linkplain #fine fine}(message)}</code>.
     * <p>
     * This method is protected for the same reason than {@link #severe(String,Throwable) severe}.
     */
    protected void fine(String message, Throwable thrown) {
        fine(message);
    }

    /**
     * Logs a {@link Level#FINER FINER} message.
     */
    @Override
    public abstract void finer(String message);

    /**
     * Logs a {@link Level#FINER FINER} message with an exception. The default implementation
     * discards the exception and invokes <code>{@linkplain #finer finer}(message)}</code>.
     * <p>
     * This method is protected for the same reason than {@link #severe(String,Throwable) severe}.
     */
    protected void finer(String message, Throwable thrown) {
        finer(message);
    }

    /**
     * Logs a {@link Level#FINEST FINEST} message.
     */
    @Override
    public abstract void finest(String message);

    /**
     * Logs a {@link Level#FINEST FINEST} message with an exception. The default implementation
     * discards the exception and invokes <code>{@linkplain #finest finest}(message)}</code>.
     * <p>
     * This method is protected for the same reason than {@link #severe(String,Throwable) severe}.
     */
    protected void finest(String message, Throwable thrown) {
        finest(message);
    }

    /**
     * Logs a method entry to the {@linkplain #getDebugLevel debug level}. Compared to the
     * default {@link Logger}, this implementation bypass the level check in order to let
     * the backing logging framework do its own check.
     */
    @Override
    public void entering(final String sourceClass, final String sourceMethod) {
        logp(getDebugLevel(), sourceClass, sourceMethod, "ENTRY");
    }

    /**
     * Logs a method entry to the {@linkplain #getDebugLevel debug level} with one parameter.
     * Compared to the default {@link Logger}, this implementation bypass the level check in
     * order to let the backing logging framework do its own check.
     */
    @Override
    public void entering(String sourceClass, String sourceMethod, Object param) {
        logp(getDebugLevel(), sourceClass, sourceMethod, "ENTRY {0}", param);
    }

    /**
     * Logs a method entry to the {@linkplain #getDebugLevel debug level} with many parameters.
     * Compared to the default {@link Logger}, this implementation bypass the level check in
     * order to let the backing logging framework do its own check.
     */
    @Override
    public void entering(final String sourceClass, final String sourceMethod, final Object[] params) {
        final String message;
        if (params == null) {
            message = "ENTRY";
        } else switch (params.length) {
            case 0: message = "ENTRY";         break;
            case 1: message = "ENTRY {0}";     break;
            case 2: message = "ENTRY {0} {1}"; break;
            default: {
                final StringBuilder builder = new StringBuilder("ENTRY");
                for (int i=0; i<params.length; i++) {
                    builder.append(" {").append(i).append('}');
                }
                message = builder.toString();
                break;
            }
        }
        logp(getDebugLevel(), sourceClass, sourceMethod, message, params);
    }

    /**
     * Logs a method return to the {@linkplain #getDebugLevel debug level}. Compared to the
     * default {@link Logger}, this implementation bypass the level check in order to let
     * the backing logging framework do its own check.
     */
    @Override
    public void exiting(final String sourceClass, final String sourceMethod) {
        logp(getDebugLevel(), sourceClass, sourceMethod, "RETURN");
    }

    /**
     * Logs a method return to the {@linkplain #getDebugLevel debug level}. Compared to the
     * default {@link Logger}, this implementation bypass the level check in order to let
     * the backing logging framework do its own check.
     */
    @Override
    public void exiting(String sourceClass, String sourceMethod, Object result) {
        logp(getDebugLevel(), sourceClass, sourceMethod, "RETURN {0}", result);
    }

    /**
     * Logs a method failure to the {@linkplain #getDebugLevel debug level}. Compared to the
     * default {@link Logger}, this implementation bypass the level check in order to let
     * the backing logging framework do its own check.
     */
    @Override
    public void throwing(String sourceClass, String sourceMethod, Throwable thrown) {
        logp(getDebugLevel(), sourceClass, sourceMethod, "THROW", thrown);
    }

    /**
     * Logs a record. The default implementation delegates to
     * {@link #logrb(Level,String,String,String,String,Object[]) logrb}.
     * <p>
     * If a {@linkplain #setFilter filter is set}, then this method checks if the record
     * {@linkplain Filter#isLoggable is loggable}. Note that this is <strong>not</strong>
     * the check for logging {@linkplain Level level}. This is rather a check based on
     * user-specified criterions. The check for logging level will be left to the wrapped
     * logging framework.
     */
    @Override
    public void log(final LogRecord record) {
        final Filter filter = getFilter();
        if (filter != null && !filter.isLoggable(record)) {
            return;
        }
        Level     level        = record.getLevel();
        String    sourceClass  = record.getSourceClassName();
        String    sourceMethod = record.getSourceMethodName();
        String    bundleName   = record.getResourceBundleName();
        String    message      = record.getMessage();
        Object[]  params       = record.getParameters();
        Throwable thrown       = record.getThrown();
        ResourceBundle bundle  = record.getResourceBundle();
        boolean   localized    = false;
        if (bundle != null) try {
            message   = bundle.getString(message);
            localized = true; // Sets only if the above succeed.
        } catch (MissingResourceException e) {
            // The default Formatter.messageFormat implementation ignores this exception
            // and uses the bundle key as the message, so we mimic its behavior here.
        }
        final boolean useThrown = (thrown != null) && (params == null || params.length == 0);
        if (localized) {
            // The message is already localized.
            if (useThrown) {
                logp(level, sourceClass, sourceMethod, message, thrown);
            } else {
                logp(level, sourceClass, sourceMethod, message, params);
            }
        } else {
            // The message needs to be localized. The bundle was null but maybe bundleName is not.
            // Futhermore subclass may have overriden the 'logrb' methods.
            if (useThrown) {
                logrb(level, sourceClass, sourceMethod, bundleName, message, thrown);
            } else {
                logrb(level, sourceClass, sourceMethod, bundleName, message, params);
            }
        }
    }

    /**
     * Logs a record at the specified level. The default implementation delegates to one of the
     * {@link #severe(String) severe}, {@link #warning(String) warning}, {@link #info(String) info},
     * {@link #config(String) config}, {@link #fine(String) fine}, {@link #finer(String) finer} or
     * {@link #finest(String) finest} methods according the supplied level.
     */
    @Override
    @SuppressWarnings("fallthrough")
    public void log(final Level level, final String message) {
        final int n = level.intValue();
        switch (n / 100) {
            default: if (n < 0)        break; // Fallthrough otherwise.
            case 10: severe (message); break;
            case  9: warning(message); break;
            case  8: info   (message); break;
            case  7: config (message); break;
            case  6:
            case  5: fine   (message); break;
            case  4: finer  (message); break;
            case  3: finest (message); break;
            case  2: /* Logging OFF */
            case  1: /* Logging OFF */
            case  0: /* Logging OFF */ break;
        }
    }

    /**
     * Logs a record at the specified level. The default implementation delegates to one of the
     * {@link #severe(String,Throwable) severe}, {@link #warning(String,Throwable) warning},
     * {@link #info(String,Throwable) info}, {@link #config(String,Throwable) config},
     * {@link #fine(String,Throwable) fine}, {@link #finer(String,Throwable) finer} or
     * {@link #finest(String,Throwable) finest} methods according the supplied level.
     */
    @Override
    @SuppressWarnings("fallthrough")
    public void log(final Level level, final String message, final Throwable thrown) {
        final int n = level.intValue();
        switch (n / 100) {
            default: if (n < 0)                break; // Fallthrough otherwise.
            case 10: severe (message, thrown); break;
            case  9: warning(message, thrown); break;
            case  8: info   (message, thrown); break;
            case  7: config (message, thrown); break;
            case  6:
            case  5: fine   (message, thrown); break;
            case  4: finer  (message, thrown); break;
            case  3: finest (message, thrown); break;
            case  2: /* Logging OFF */
            case  1: /* Logging OFF */
            case  0: /* Logging OFF */         break;
        }
    }

    /**
     * Logs a record at the specified level. The defaut implementation delegates to
     * <code>{@linkplain #log(Level,String,Object[]) log}(level, message, params)</code>
     * where the {@code params} array is built from the {@code param} object.
     */
    @Override
    public void log(final Level level, final String message, final Object param) {
        log(level, message, asArray(param));
    }

    /**
     * Logs a record at the specified level.
     * The defaut implementation formats the message immediately, then delegates to
     * <code>{@linkplain #log(Level,String) log}(level, message)</code>.
     */
    @Override
    public void log(final Level level, final String message, final Object[] params) {
        log(level, format(message, params));
    }

    /**
     * Logs a record at the specified level. The defaut implementation discards
     * the source class and source method, then delegates to
     * <code>{@linkplain #log(Level,String) log}(level, message)</code>.
     */
    @Override
    public void logp(final Level level, final String sourceClass, final String sourceMethod,
                     final String message)
    {
        log(level, message);
    }

    /**
     * Logs a record at the specified level. The defaut implementation discards
     * the source class and source method, then delegates to
     * <code>{@linkplain #log(Level,String,Throwable) log}(level, message, thrown)</code>.
     */
    @Override
    public void logp(final Level level, final String sourceClass, final String sourceMethod,
                     final String message, final Throwable thrown)
    {
        log(level, message, thrown);
    }

    /**
     * Logs a record at the specified level. The defaut implementation delegates to
     * <code>{@linkplain #logp(Level,String,String,String,Object[]) logp}(level, sourceClass,
     * sourceMethod, message, params)</code> where the {@code params} array is built from the
     * {@code param} object.
     * <p>
     * Note that {@code sourceClass} and {@code sourceMethod} will be discarted unless the
     * target {@link #logp(Level,String,String,String) logp} method has been overriden.
     */
    @Override
    public void logp(final Level level, final String sourceClass, final String sourceMethod,
                     final String message, final Object param)
    {
        logp(level, sourceClass, sourceMethod, message, asArray(param));
    }

    /**
     * Logs a record at the specified level. The defaut implementation formats the message
     * immediately, then delegates to <code>{@linkplain #logp(Level,String,String,String)
     * logp}(level, sourceClass, sourceMethod, message)</code>.
     * <p>
     * Note that {@code sourceClass} and {@code sourceMethod} will be discarted unless the
     * target {@link #logp(Level,String,String,String) logp} method has been overriden.
     */
    @Override
    public void logp(final Level level, final String sourceClass, final String sourceMethod,
                     final String message, final Object[] params)
    {
        logp(level, sourceClass, sourceMethod, format(message, params));
    }

    /**
     * Logs a localizable record at the specified level. The defaut implementation localizes the
     * message immediately, then delegates to <code>{@linkplain #logp(Level,String,String,String)
     * logp}(level, sourceClass, sourceMethod, message)</code>.
     */
    @Override
    public void logrb(final Level level, final String sourceClass, final String sourceMethod,
                      final String bundleName, final String message)
    {
        logp(level, sourceClass, sourceMethod, localize(bundleName, message));
    }

    /**
     * Logs a localizable record at the specified level. The defaut implementation localizes the
     * message immediately, then delegates to <code>{@linkplain #logp(Level,String,String,String,
     * Throwable) logp}(level, sourceClass, sourceMethod, message, thrown)</code>.
     */
    @Override
    public void logrb(final Level level, final String sourceClass, final String sourceMethod,
                      final String bundleName, final String message, final Throwable thrown)
    {
        logp(level, sourceClass, sourceMethod, localize(bundleName, message), thrown);
    }

    /**
     * Logs a localizable record at the specified level. The defaut implementation localizes the
     * message immediately, then delegates to <code>{@linkplain #logp(Level,String,String,String,
     * Object) logp}(level, sourceClass, sourceMethod, message, param)</code>.
     */
    @Override
    public void logrb(final Level level, final String sourceClass, final String sourceMethod,
                      final String bundleName, final String message, final Object param)
    {
        logp(level, sourceClass, sourceMethod, localize(bundleName, message), param);
    }

    /**
     * Logs a localizable record at the specified level. The defaut implementation localizes the
     * message immediately, then delegates to <code>{@linkplain #logp(Level,String,String,String,
     * Object[]) logp}(level, sourceClass, sourceMethod, message, params)</code>.
     */
    @Override
    public void logrb(final Level level, final String sourceClass, final String sourceMethod,
                      final String bundleName, String message, final Object[] params)
    {
        logp(level, sourceClass, sourceMethod, localize(bundleName, message), params);
    }

    /**
     * Do nothing since this logger adapter does not supports handlers.
     * The configuration should be fully controlled by the wrapped logging framework
     * (e.g. <a href="http://commons.apache.org/logging/">Commons-logging</a>) instead,
     * which is not expected to use handlers.
     */
    @Override
    public void addHandler(Handler handler) {
    }

    /**
     * Do nothing since this logger adapter does not support handlers.
     */
    @Override
    public void removeHandler(Handler handler) {
    }

    /**
     * Do nothing since this logger never use parent handlers.
     */
    @Override
    public void setUseParentHandlers(boolean useParentHandlers) {
    }

    /**
     * Do nothing since this logger adapter does not support arbitrary parents.
     */
    @Override
    public void setParent(Logger parent) {
    }

    /**
     * Wraps the specified object in an array. This is a helper method for
     * {@code log(..., Object)} methods that delegate their work to {@code log(..., Object[])}
     */
    private static Object[] asArray(final Object param) {
        return (param != null) ? new Object[] {param} : null;
    }

    /**
     * Formats the specified message. This is a helper method for
     * {@code log(..., Object[])} methods that delegate their work to {@code log(...)}
     */
    private static String format(String message, final Object[] params) {
        if (params != null && params.length != 0) {
            if (MESSAGE_FORMAT.matcher(message).find()) try {
                message = MessageFormat.format(message, params);
            } catch (IllegalArgumentException e) {
                // The default Formatter.messageFormat implementation ignores this exception
                // and uses the pattern as the message, so we mimic its behavior here.
            }
        }
        return message;
    }

    /**
     * Localize the specified message. This is a helper method for
     * {@code logrb(...)} methods that delegate their work to {@code logp(...)}
     */
    private static String localize(final String bundleName, String message) {
        if (bundleName != null) try {
            message = ResourceBundle.getBundle(bundleName).getString(message);
        } catch (MissingResourceException e) {
            // The default Formatter.messageFormat implementation ignores this exception
            // and uses the bundle key as the message, so we mimic its behavior here.
        }
        return message;
    }
}
