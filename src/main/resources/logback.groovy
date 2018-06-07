/**
 * Copyright 2017-2018 European Union, interactive instruments GmbH
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This work was supported by the EU Interoperability Solutions for
 * European Public Administrations Programme (http://ec.europa.eu/isa)
 * through Action 1.17: A Reusable INSPIRE Reference Platform (ARE3NA).
 */
package de.interactive_instruments.etf;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.UnsynchronizedAppenderBase
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy
import de.interactive_instruments.IFile
import de.interactive_instruments.properties.PropertyUtils
import org.apache.commons.lang.SystemUtils

import static ch.qos.logback.classic.Level.*

// Log appender
//////////////////////////////////////////////////////////////////////////////////////////////////////

// Rotating logfile
appender("FILE", RollingFileAppender) {
    def logFilePath = detLogFilePath()
    if(logFilePath!=null) {
        file = logFilePath
        System.err.println "Path to ETF log file ${logFilePath}"
    }else{
        def logFile = new File("etf.log")
        file = "etf.log"
        System.err.println "Path to ETF log file ${logFile.absolutePath}"
    }
    rollingPolicy(TimeBasedRollingPolicy) {
        fileNamePattern = "etf-%d{yyyy-MM-dd}.%i.zip"
        timeBasedFileNamingAndTriggeringPolicy(SizeAndTimeBasedFNATP) {
            maxFileSize = "200MB"
        }
        maxHistory = 14
    }
    encoder(PatternLayoutEncoder) {
        pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{5} - %msg%n"
    }
}


// Console log appender
if(detApplicationWebServerDir()==null) {
    appender("STD", ConsoleAppender) {
        encoder(PatternLayoutEncoder) {
            pattern = "%d{HH:mm:ss.SSS} [%thread] %highlight(%-5level) %logger{5} - %msg%n"
        }
    }
}else{
    appender("STD", NullAppender)
}


// Logger and log level definitions
//////////////////////////////////////////////////////////////////////////////////////////////////////

logger("org.apache.tiles", INFO, ["FILE", "STD"], false)
logger("org.deegree", ERROR, ["FILE", "STD"], false)
logger("de.interactive_instruments.etf.testdriver.ComponentClassLoader", ERROR, ["FILE", "STD"], false)
logger("de.interactive_instruments.etf.dal.dao.basex.DtoCache", ERROR, ["FILE", "STD"], false)

if("true"==PropertyUtils.getenvOrProperty("ETF_LOG_DEBUG", "false")) {
    logger("de.interactive_instruments.etf", DEBUG, ["FILE", "STD"], false)
    root(DEBUG, ["FILE", "STD"])
}else{
    logger("de.interactive_instruments.etf", INFO, ["FILE", "STD"], false)
    root(INFO, ["FILE", "STD"])
}

// Helpers
//////////////////////////////////////////////////////////////////////////////////////////////////////
static detApplicationWebServerDir() {
    String appWebServerDir = PropertyUtils.getenvOrProperty("TOMCAT_HOME", null)
    if(appWebServerDir==null) {
        appWebServerDir = PropertyUtils.getenvOrProperty("CATALINA_HOME", null)
    }
    if(appWebServerDir==null) {
        appWebServerDir = PropertyUtils.getenvOrProperty("JETTY_HOME", null)
    }
    return appWebServerDir
}

// Dummy appender
class NullAppender extends UnsynchronizedAppenderBase {
    @Override
    protected void append(final Object eventObject) {}
}

static boolean isDirUsableForLog(final File dir) {
    if(dir.exists() || dir.mkdirs()) {
        final File logFile = new File(dir, "etf.log")
        if (!logFile.exists()) {
            try {
                return logFile.createNewFile()
            } catch (IOException e) {
                // Error creating file
                return false
            }
        } else {
            // Dir exists and file exists
            return true
        }
    }else{
        System.err.println "Insufficient rights to use directory "+dir
        return false
    }
}

// Determine the location of the log file
static detLogFilePath() {
    // Check if ETF_LOG_DIR was set
    final String envLogDir = PropertyUtils.getenvOrProperty("ETF_LOG_DIR",null)
    if(envLogDir!=null) {
        final File logDir = new File(envLogDir)
        if(isDirUsableForLog(logDir)) {
            return logDir.path+"/etf.log"
        }
    }
    // Check if ETF is running in a Tomcat or Jetty
    final String appWebServerDir = detApplicationWebServerDir()
    if(appWebServerDir!=null) {
        final File appWebServerDirLogDir = new File(appWebServerDir,"logs")
        if(isDirUsableForLog(appWebServerDirLogDir)) {
            return appWebServerDirLogDir.path+"/etf.log"
        }
    }

    // Operation system dependent
    if(SystemUtils.IS_OS_LINUX|| SystemUtils.IS_OS_MAC) {
        // Linux && Mac

        // /var/log/etf.log
        final File varLog = new File("/var/log/etf.log")
        if(varLog.exists()) {
            if(varLog.canWrite()) {
                return varLog.path
            }else{
                System.err.println "Insufficient rights to use /var/log/etf.log"
            }
        }

        // Parallel to {ETF_WEBAPP_PROPERTIES_FILE}/../etf.log
        final String propertiesFilePath = PropertyUtils.getenvOrProperty(
                "ETF_WEBAPP_PROPERTIES_FILE",null)
        if(propertiesFilePath!=null) {
            final propFile = new File(propertiesFilePath)
            if(propFile.exists()) {
                final File propLog = new File(propFile.getParentFile(), "logs")
                if(isDirUsableForLog(propLog)) {
                    return propLog.path+"/etf.log"
                }
            }
        }

        // {home}/.etf/log/etf.log
        final File homeDir = new File(System.getProperty("user.home"))
        if(homeDir.exists()) {
            final IFile homeLogDir = new IFile(homeDir, ".etf/log")
            if(isDirUsableForLog(homeLogDir)) {
                return homeLogDir.path+"/etf.log"
            }
        }else{
            return null
        }
    }else{
        // Windows

        // {ALLUSERSPROFILE}/etf/logs
        final File logDir = new File(System.getenv("ALLUSERSPROFILE"), "etf\\logs")
        if(isDirUsableForLog(logDir)) {
            return logDir.path + "\\etf.log"
        }
        return null
    }
}
