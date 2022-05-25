/**
 * Copyright 2010-2020 interactive instruments GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.interactive_instruments.etf.testdriver;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.event.LoggingEvent;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.spi.LocationAwareLogger;

import de.interactive_instruments.IFile;

/**
 * Default Test Run Logger based on the slf4j SimpleLogger
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 *
 *
 *         <p>
 *         The logging implementation is taken from the <a href=
 *         "https://github.com/qos-ch/slf4j/blob/master/slf4j-simple/src/main/java/org/slf4j/impl/SimpleLogger.java">slf4j
 *         SimpleLogger</a> class.
 *         </p>
 * @author Ceki G&uuml;lc&uuml;
 * @author <a href="mailto:sanders@apache.org">Scott Sanders</a>
 * @author Rod Waldhoff
 * @author Robert Burrell Donkin
 * @author C&eacute;drik LIME
 *
 */
public class DefaultTestRunLogger extends MarkerIgnoringBase implements TestRunLogger {

    private final String name;
    private final IFile logFile;
    private final PrintStream logFileStream;
    private final int currentLogLevel = LOG_LEVEL_INFO;
    private transient String shortLogName = null;

    private static final long START_TIME = System.currentTimeMillis();
    private static final int LOG_LEVEL_TRACE = LocationAwareLogger.TRACE_INT;
    private static final int LOG_LEVEL_DEBUG = LocationAwareLogger.DEBUG_INT;
    private static final int LOG_LEVEL_INFO = LocationAwareLogger.INFO_INT;
    private static final int LOG_LEVEL_WARN = LocationAwareLogger.WARN_INT;
    private static final int LOG_LEVEL_ERROR = LocationAwareLogger.ERROR_INT;

    private static final boolean SHOW_DATE_TIME = true;
    private static final String DATE_TIME_FORMAT_STR = "dd.MM.yyyy HH:mm:ss";
    private static final DateFormat DATE_FORMATTER = new SimpleDateFormat(DATE_TIME_FORMAT_STR);
    private static final boolean SHOW_THREAD_NAME = false;
    private static final boolean SHOW_LOG_NAME = false;
    private static final boolean SHOW_SHORT_LOG_NAME = false;
    private static final boolean LEVEL_IN_BRACKETS = false;
    private static final String WARN_LEVEL_STRING = "WARN";

    public DefaultTestRunLogger(final IFile logDir, final String name) {
        this.name = name;
        this.logFile = logDir.expandPath(name + ".log");
        try {
            final FileOutputStream fos = new FileOutputStream(logFile);
            this.logFileStream = new PrintStream(fos);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Can not open log path");
        }
    }

    @Override
    public File getLogFile() {
        return logFile;
    }

    @Override
    public List<String> getLogMessages(final long firstMessagePos) {
        final List<String> output = new ArrayList<>(48);
        long skip = 0;
        try (final BufferedReader br = new BufferedReader(new FileReader(logFile))) {
            while (skip++ < firstMessagePos && br.readLine() != null) {
                // skip lines
            }
            for (String line; (line = br.readLine()) != null;) {
                output.add(line);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        return output;
    }

    @Override
    public void streamLogMessagesTo(final long position, final OutputStream outputStream) {
        long skip = 0;
        try (final BufferedReader br = new BufferedReader(new FileReader(logFile))) {
            while (skip++ < position && br.readLine() != null) {}
            for (String line; (line = br.readLine()) != null;) {
                outputStream.write(line.getBytes());
            }
            outputStream.flush();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * This is our internal implementation for logging regular (non-parameterized) log messages.
     *
     * @param level
     *            One of the LOG_LEVEL_XXX constants defining the log level
     * @param message
     *            The message itself
     * @param t
     *            The exception whose stack trace should be logged
     */
    private void log(int level, String message, Throwable t) {
        if (!isLevelEnabled(level)) {
            return;
        }

        final StringBuilder buf = new StringBuilder(32);

        // Append date-time if so configured
        if (SHOW_DATE_TIME) {
            if (DATE_FORMATTER != null) {
                buf.append(getFormattedDate());
                buf.append(' ');
            } else {
                buf.append(System.currentTimeMillis() - START_TIME);
                buf.append(' ');
            }
        }

        // Append current thread name if so configured
        if (SHOW_THREAD_NAME) {
            buf.append('[');
            buf.append(Thread.currentThread().getName());
            buf.append("] ");
        }

        if (LEVEL_IN_BRACKETS)
            buf.append('[');

        // Append a readable representation of the log level
        switch (level) {
        case LOG_LEVEL_TRACE:
            buf.append("TRACE");
            break;
        case LOG_LEVEL_DEBUG:
            buf.append("DEBUG");
            break;
        case LOG_LEVEL_INFO:
            // buf.append("INFO");
            buf.append("-");
            break;
        case LOG_LEVEL_WARN:
            buf.append(WARN_LEVEL_STRING);
            break;
        case LOG_LEVEL_ERROR:
            buf.append("ERROR");
            break;
        }
        if (LEVEL_IN_BRACKETS)
            buf.append(']');
        buf.append(' ');

        // Append the name of the log instance if so configured
        if (SHOW_SHORT_LOG_NAME) {
            if (shortLogName == null)
                shortLogName = computeShortName();
            buf.append(shortLogName).append(" - ");
        } else if (SHOW_LOG_NAME) {
            buf.append(name).append(" - ");
        }

        // Append the message
        buf.append(message);

        write(buf, t);

    }

    void write(StringBuilder buf, Throwable t) {
        logFileStream.println(buf.toString());
        if (t != null) {
            t.printStackTrace(logFileStream);
        }
        logFileStream.flush();
    }

    private String getFormattedDate() {
        Date now = new Date();
        String dateText;
        synchronized (DATE_FORMATTER) {
            dateText = DATE_FORMATTER.format(now);
        }
        return dateText;
    }

    private String computeShortName() {
        return name.substring(name.lastIndexOf(".") + 1);
    }

    /**
     * For formatted messages, first substitute arguments and then log.
     *
     * @param level
     * @param format
     * @param arg1
     * @param arg2
     */
    private void formatAndLog(int level, String format, Object arg1, Object arg2) {
        if (!isLevelEnabled(level)) {
            return;
        }
        FormattingTuple tp = MessageFormatter.format(format, arg1, arg2);
        log(level, tp.getMessage(), tp.getThrowable());
    }

    /**
     * For formatted messages, first substitute arguments and then log.
     *
     * @param level
     * @param format
     * @param arguments
     *            a list of 3 ore more arguments
     */
    private void formatAndLog(int level, String format, Object... arguments) {
        if (!isLevelEnabled(level)) {
            return;
        }
        FormattingTuple tp = MessageFormatter.arrayFormat(format, arguments);
        log(level, tp.getMessage(), tp.getThrowable());
    }

    /**
     * Is the given log level currently enabled?
     *
     * @param logLevel
     *            is this level enabled?
     * @return true if log level is enabled, false otherwise
     */
    protected boolean isLevelEnabled(int logLevel) {
        // log level are numerically ordered so can use simple numeric
        // comparison
        return (logLevel >= currentLogLevel);
    }

    /** Are {@code trace} messages currently enabled? */
    public boolean isTraceEnabled() {
        return isLevelEnabled(LOG_LEVEL_TRACE);
    }

    /**
     * A simple implementation which logs messages of level TRACE according to the format outlined above.
     */
    public void trace(String msg) {
        log(LOG_LEVEL_TRACE, msg, null);
    }

    /**
     * Perform single parameter substitution before logging the message of level TRACE according to the format outlined
     * above.
     */
    public void trace(String format, Object param1) {
        formatAndLog(LOG_LEVEL_TRACE, format, param1, null);
    }

    /**
     * Perform double parameter substitution before logging the message of level TRACE according to the format outlined
     * above.
     */
    public void trace(String format, Object param1, Object param2) {
        formatAndLog(LOG_LEVEL_TRACE, format, param1, param2);
    }

    /**
     * Perform double parameter substitution before logging the message of level TRACE according to the format outlined
     * above.
     */
    public void trace(String format, Object... argArray) {
        formatAndLog(LOG_LEVEL_TRACE, format, argArray);
    }

    /** Log a message of level TRACE, including an exception. */
    public void trace(String msg, Throwable t) {
        log(LOG_LEVEL_TRACE, msg, t);
    }

    /** Are {@code debug} messages currently enabled? */
    public boolean isDebugEnabled() {
        return isLevelEnabled(LOG_LEVEL_DEBUG);
    }

    /**
     * A simple implementation which logs messages of level DEBUG according to the format outlined above.
     */
    public void debug(String msg) {
        log(LOG_LEVEL_DEBUG, msg, null);
    }

    /**
     * Perform single parameter substitution before logging the message of level DEBUG according to the format outlined
     * above.
     */
    public void debug(String format, Object param1) {
        formatAndLog(LOG_LEVEL_DEBUG, format, param1, null);
    }

    /**
     * Perform double parameter substitution before logging the message of level DEBUG according to the format outlined
     * above.
     */
    public void debug(String format, Object param1, Object param2) {
        formatAndLog(LOG_LEVEL_DEBUG, format, param1, param2);
    }

    /**
     * Perform double parameter substitution before logging the message of level DEBUG according to the format outlined
     * above.
     */
    public void debug(String format, Object... argArray) {
        formatAndLog(LOG_LEVEL_DEBUG, format, argArray);
    }

    /** Log a message of level DEBUG, including an exception. */
    public void debug(String msg, Throwable t) {
        log(LOG_LEVEL_DEBUG, msg, t);
    }

    /** Are {@code info} messages currently enabled? */
    public boolean isInfoEnabled() {
        return isLevelEnabled(LOG_LEVEL_INFO);
    }

    /**
     * A simple implementation which logs messages of level INFO according to the format outlined above.
     */
    public void info(String msg) {
        log(LOG_LEVEL_INFO, msg, null);
    }

    /**
     * Perform single parameter substitution before logging the message of level INFO according to the format outlined
     * above.
     */
    public void info(String format, Object arg) {
        formatAndLog(LOG_LEVEL_INFO, format, arg, null);
    }

    /**
     * Perform double parameter substitution before logging the message of level INFO according to the format outlined
     * above.
     */
    public void info(String format, Object arg1, Object arg2) {
        formatAndLog(LOG_LEVEL_INFO, format, arg1, arg2);
    }

    /**
     * Perform double parameter substitution before logging the message of level INFO according to the format outlined
     * above.
     */
    public void info(String format, Object... argArray) {
        formatAndLog(LOG_LEVEL_INFO, format, argArray);
    }

    /** Log a message of level INFO, including an exception. */
    public void info(String msg, Throwable t) {
        log(LOG_LEVEL_INFO, msg, t);
    }

    /** Are {@code warn} messages currently enabled? */
    public boolean isWarnEnabled() {
        return isLevelEnabled(LOG_LEVEL_WARN);
    }

    /**
     * A simple implementation which always logs messages of level WARN according to the format outlined above.
     */
    public void warn(String msg) {
        log(LOG_LEVEL_WARN, msg, null);
    }

    /**
     * Perform single parameter substitution before logging the message of level WARN according to the format outlined
     * above.
     */
    public void warn(String format, Object arg) {
        formatAndLog(LOG_LEVEL_WARN, format, arg, null);
    }

    /**
     * Perform double parameter substitution before logging the message of level WARN according to the format outlined
     * above.
     */
    public void warn(String format, Object arg1, Object arg2) {
        formatAndLog(LOG_LEVEL_WARN, format, arg1, arg2);
    }

    /**
     * Perform double parameter substitution before logging the message of level WARN according to the format outlined
     * above.
     */
    public void warn(String format, Object... argArray) {
        formatAndLog(LOG_LEVEL_WARN, format, argArray);
    }

    /** Log a message of level WARN, including an exception. */
    public void warn(String msg, Throwable t) {
        log(LOG_LEVEL_WARN, msg, t);
    }

    /** Are {@code error} messages currently enabled? */
    public boolean isErrorEnabled() {
        return isLevelEnabled(LOG_LEVEL_ERROR);
    }

    /**
     * A simple implementation which always logs messages of level ERROR according to the format outlined above.
     */
    public void error(String msg) {
        log(LOG_LEVEL_ERROR, msg, null);
    }

    /**
     * Perform single parameter substitution before logging the message of level ERROR according to the format outlined
     * above.
     */
    public void error(String format, Object arg) {
        formatAndLog(LOG_LEVEL_ERROR, format, arg, null);
    }

    /**
     * Perform double parameter substitution before logging the message of level ERROR according to the format outlined
     * above.
     */
    public void error(String format, Object arg1, Object arg2) {
        formatAndLog(LOG_LEVEL_ERROR, format, arg1, arg2);
    }

    /**
     * Perform double parameter substitution before logging the message of level ERROR according to the format outlined
     * above.
     */
    public void error(String format, Object... argArray) {
        formatAndLog(LOG_LEVEL_ERROR, format, argArray);
    }

    /** Log a message of level ERROR, including an exception. */
    public void error(String msg, Throwable t) {
        log(LOG_LEVEL_ERROR, msg, t);
    }

    public void log(LoggingEvent event) {
        int levelInt = event.getLevel().toInt();

        if (!isLevelEnabled(levelInt)) {
            return;
        }
        FormattingTuple tp = MessageFormatter.arrayFormat(event.getMessage(), event.getArgumentArray(), event.getThrowable());
        log(levelInt, tp.getMessage(), event.getThrowable());
    }
}
