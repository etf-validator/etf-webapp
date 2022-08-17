/**
 * Copyright 2010-2022 interactive instruments GmbH
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
package de.interactive_instruments.etf

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import de.interactive_instruments.IFile
import de.interactive_instruments.properties.PropertyUtils
import org.apache.commons.lang.SystemUtils

import static ch.qos.logback.classic.Level.*

// Log appender
//////////////////////////////////////////////////////////////////////////////////////////////////////

appender("STD", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} [%thread] %highlight(%-5level) %logger{5} - %msg%n"
    }
}


// Logger and log level definitions
//////////////////////////////////////////////////////////////////////////////////////////////////////

logger("de.interactive_instruments.etf.component.loaders.DefaultItemRegistry", DEBUG, ["STD"], false)
