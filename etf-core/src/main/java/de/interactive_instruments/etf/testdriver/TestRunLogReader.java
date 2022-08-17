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
package de.interactive_instruments.etf.testdriver;

import java.io.File;
import java.io.OutputStream;
import java.util.List;

/**
 * Log path read interface
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface TestRunLogReader {
    /**
     * Returns the logger that is used to log information about the progress of the task
     *
     * @return log file
     */
    File getLogFile();

    /**
     * Return messages
     *
     * @param knownPosition
     *            last known position
     * @return log messages as list
     */
    List<String> getLogMessages(final long knownPosition);

    /**
     * Stream messages to an OutputStream
     *
     * @param knownPosition
     *            last known position
     * @param outputStream
     *            target output stream
     */
    void streamLogMessagesTo(final long knownPosition, final OutputStream outputStream);
}
