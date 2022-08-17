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
package de.interactive_instruments.etf.dal.dao.basex;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.interactive_instruments.IFile;
import de.interactive_instruments.Releasable;
import de.interactive_instruments.TimeUtils;
import de.interactive_instruments.etf.dal.dto.result.TestResultStatus;
import de.interactive_instruments.exceptions.ExcUtils;
import de.interactive_instruments.exceptions.StorageException;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
final class XmlTestResultWriter implements Releasable {

    public static final String ETF_NS = "http://www.interactive-instruments.de/etf/2.0";
    public static final String ETF_RESULT_XSD = "http://resources.etf-validator.net/schema/v2/model/result.xsd";
    public static final String ETF_NS_PREFIX = "etf";
    public static final String ID_PREFIX = "EID";
    public static final String TR_ERROR_LIMIT_EXCEEDED = "TR.errorLimitExceeded";

    private final Deque<ResultModelItem> results = new LinkedList<>();
    private final Map<String, Attachment> attachments = new HashMap<>();
    private final List<Message> messages = new ArrayList<>();
    private final Random random = new Random();
    private final int errorLimit;
    private int errorCount = 0;

    private final XMLStreamWriter writer;

    XmlTestResultWriter(final XMLStreamWriter writer, final int errorLimit) throws XMLStreamException {
        this.writer = writer;
        this.errorLimit = errorLimit;
    }

    private final class ResultModelItem {
        private final String id;
        private final long startTimestamp;
        private final String resultedFrom;

        ResultModelItem(final String id, final long currentTime, final String resultedFrom) {
            this.id = id;
            this.startTimestamp = currentTime;
            this.resultedFrom = resultedFrom;
        }

        void write(final int status, final long stopTimestamp) throws XMLStreamException {
            if (!results.isEmpty()) {
                writer.writeStartElement("parent");
                writer.writeAttribute("ref", ID_PREFIX + results.getLast().id);
                writer.writeEndElement();
            }

            writer.writeStartElement("resultedFrom");
            writer.writeAttribute("ref", ID_PREFIX + resultedFrom);
            writer.writeEndElement();

            writer.writeStartElement("startTimestamp");
            writer.writeCharacters(TimeUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(startTimestamp));
            writer.writeEndElement();

            writer.writeStartElement("duration");
            writer.writeCharacters(String.valueOf(stopTimestamp - startTimestamp));
            writer.writeEndElement();

            writer.writeStartElement("status");
            writer.writeCharacters(TestResultStatus.toString(status));
            writer.writeEndElement();
        }

        public String getResultedFromId() {
            return id;
        }
    }

    private final class Attachment {
        private final IFile attachmentFile;
        private final byte[] base64EncodedContent;
        private final String id;
        private final String label;
        private final String encoding;
        private final String mimeType;
        private final String type;

        public Attachment(final String id, final IFile attachmentFile, final String label, final String encoding,
                final String mimeType, final String type) {
            this.id = id;
            this.attachmentFile = attachmentFile;
            this.base64EncodedContent = null;
            this.label = label;
            this.encoding = encoding;
            this.mimeType = mimeType;
            this.type = type;
        }

        public Attachment(final String id, final byte[] base64EncodedContent, final String label, final String encoding,
                final String mimeType, final String type) {
            this.id = id;
            this.attachmentFile = null;
            this.base64EncodedContent = base64EncodedContent;
            this.label = label;
            this.encoding = encoding;
            this.mimeType = mimeType;
            this.type = type;
        }

        void write() throws XMLStreamException {
            writer.writeStartElement("Attachment");
            if (type != null) {
                writer.writeAttribute("type", type);
            }
            writer.writeAttribute("id", ID_PREFIX + id);

            writer.writeStartElement("label");
            writer.writeCharacters(label);
            writer.writeEndElement();

            writer.writeStartElement("encoding");
            writer.writeCharacters(encoding);
            writer.writeEndElement();

            writer.writeStartElement("mimeType");
            if (mimeType != null) {
                writer.writeCharacters(mimeType);
            } else {
                writer.writeCharacters("text/plain");
            }
            writer.writeEndElement();
            if (attachmentFile != null) {
                writer.writeStartElement("referencedData");
                writer.writeAttribute("href", fileTrippleSlash(attachmentFile));
                writer.writeEndElement();
            } else {
                writer.writeStartElement("embeddedData");
                final char[] convertedChars = new char[base64EncodedContent.length];
                for (int i = 0; i < base64EncodedContent.length; i++) {
                    convertedChars[i] = (char) base64EncodedContent[i];
                }
                writer.writeCharacters(convertedChars, 0, convertedChars.length);
                writer.writeEndElement();
            }

            writer.writeEndElement();
        }
    }

    private final class Message {
        private final String translationTemplateId;
        private final List<String> arguments;

        public Message(final String translationTemplateId) {
            this.translationTemplateId = translationTemplateId;
            this.arguments = null;
        }

        public Message(final String translationTemplateId, final String[] arguments) {
            if (arguments == null || arguments.length == 0) {
                this.arguments = null;
            } else if (arguments.length % 2 != 0) {
                throw new IllegalStateException("There is at least one invalid token value pair");
            } else {
                this.arguments = Arrays.asList(arguments);
            }
            this.translationTemplateId = translationTemplateId;
        }

        public Message(final String translationTemplateId, final Map<String, String> arguments) {
            this.translationTemplateId = translationTemplateId;
            if (arguments != null && !arguments.isEmpty()) {
                this.arguments = new ArrayList<>();
                for (final Map.Entry<String, String> entry : arguments.entrySet()) {
                    this.arguments.add(entry.getKey());
                    this.arguments.add(entry.getValue());
                }
            } else {
                this.arguments = null;
            }
        }

        void write() throws XMLStreamException {
            writer.writeStartElement("message");
            writer.writeAttribute("ref", translationTemplateId);
            if (arguments != null) {
                writer.writeStartElement("translationArguments");
                for (int i = 0; i < arguments.size(); i += 2) {
                    writer.writeStartElement("argument");
                    writer.writeAttribute("token", arguments.get(i));
                    writer.writeCharacters(arguments.get(i + 1));
                    writer.writeEndElement();

                }
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }
    }

    private String writeEidAndMarkResultModelItem(final String resultedFrom, final long startTimestamp)
            throws XMLStreamException {
        long time = startTimestamp << 32;
        time |= ((startTimestamp & 0xFFFF00000000L) >> 16);
        time |= 0x1000 | ((startTimestamp >> 48) & 0x0FFF);
        final String genId = new UUID(time, random.nextLong()).toString();
        writer.writeAttribute("id", ID_PREFIX + genId);
        results.addLast(new ResultModelItem(genId, startTimestamp, resultedFrom));
        return genId;
    }

    // Start writing results
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String writeStartTestTaskResult(final String resultedFrom, final long startTimestamp, final String testObjectRef)
            throws XMLStreamException {
        writer.writeStartDocument("UTF-8", "1.0");
        writer.writeStartElement(ETF_NS_PREFIX, "TestTaskResult", ETF_NS);
        writer.setPrefix(ETF_NS_PREFIX, ETF_NS);
        writer.writeNamespace(ETF_NS_PREFIX, ETF_NS);
        writer.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        writer.writeAttribute("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation",
                ETF_NS + " " + ETF_RESULT_XSD);
        writer.writeAttribute("xmlns", ETF_NS);
        final String id = UUID.randomUUID().toString();
        writeId(id);
        addResult(id, startTimestamp, resultedFrom);
        writer.writeStartElement("testObject");
        writeRef(testObjectRef);
        writer.writeEndElement(); // testObject
        writer.writeStartElement("testModuleResults");
        return id;
    }

    public String writeStartTestModuleResult(final String resultedFrom, final long startTimestamp) throws XMLStreamException {
        writer.writeStartElement("TestModuleResult");
        final String id = writeEidAndMarkResultModelItem(resultedFrom, startTimestamp);
        writer.writeStartElement("testCaseResults");
        return id;
    }

    public String writeStartTestCaseResult(final String resultedFrom, final long startTimestamp) throws XMLStreamException {
        writer.writeStartElement("TestCaseResult");
        final String id = writeEidAndMarkResultModelItem(resultedFrom, startTimestamp);
        writer.writeStartElement("testStepResults");
        return id;
    }

    public String writeStartTestStepResult(final String resultedFrom, final long startTimestamp) throws XMLStreamException {
        writer.writeStartElement("TestStepResult");
        final String id = writeEidAndMarkResultModelItem(resultedFrom, startTimestamp);
        return id;
    }

    public void writeStartInvokedTests() throws XMLStreamException {
        writer.writeStartElement("invokedTests");
        // Must be called to provoke closing the tag of the invokedTests element
        writer.writeComment("SUB_RESULTS");
    }

    public void writeStartTestAssertionResults() throws XMLStreamException {
        writer.writeStartElement("testAssertionResults");
    }

    public String writeStartTestAssertionResult(final String resultedFrom, final long startTimestamp)
            throws XMLStreamException {
        writer.writeStartElement("TestAssertionResult");
        return writeEidAndMarkResultModelItem(resultedFrom, startTimestamp);
    }

    // Finish results
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private String writeResultModelItem(final int status, final long stopTimestamp) throws XMLStreamException {
        final ResultModelItem resultModelItem = results.removeLast();
        resultModelItem.write(status, stopTimestamp);
        writer.writeEndElement();
        return resultModelItem.getResultedFromId();
    }

    public String writeEndTestTaskResult(final String testModelItemId, final int status, final long stopTimestamp)
            throws XMLStreamException, FileNotFoundException, StorageException {
        writer.writeEndElement();
        if (!attachments.isEmpty()) {
            writer.writeStartElement("attachments");
            for (final Attachment attachment : attachments.values()) {
                attachment.write();
            }
            attachments.clear();
            writer.writeEndElement();
        } else {
            throw new IllegalStateException("At least the log file is required as attachment");
        }
        final String id = writeResultModelItem(status, stopTimestamp);
        return id;
    }

    public String writeEndTestModuleResult(final String testModelItemId, final int status, final long stopTimestamp)
            throws XMLStreamException {
        writer.writeEndElement();
        return writeResultModelItem(status, stopTimestamp);
    }

    public String writeEndTestCaseResult(final String testModelItemId, final int status, final long stopTimestamp)
            throws XMLStreamException {
        writer.writeEndElement();
        return writeResultModelItem(status, stopTimestamp);
    }

    public String writeEndTestStepResult(final String testModelItemId, final int status, final long stopTimestamp)
            throws XMLStreamException {
        return writeResultModelItem(status, stopTimestamp);
    }

    public void writeEndTestAssertionResults() throws XMLStreamException {
        writer.writeEndElement();
    }

    public void writeEndInvokedTests() throws XMLStreamException {
        writer.writeEndElement();
    }

    public String writeEndTestAssertionResult(final String testModelItemId, final int status, final long stopTimestamp)
            throws XMLStreamException {
        return writeResultModelItem(status, stopTimestamp);
    }

    public void flush() throws XMLStreamException {
        writer.flush();
    }

    public void close() throws XMLStreamException {
        writer.writeEndDocument();
        writer.flush();
        writer.close();
    }

    public String currentResultItemId() {
        return results != null ? results.getLast().id : null;
    }

    @Override
    public void release() {
        try {
            close();
        } catch (XMLStreamException e) {
            ExcUtils.suppress(e);
        }
    }

    void addMessage(final String translationTemplateId) {
        if (++errorCount <= errorLimit) {
            messages.add(new Message(translationTemplateId));
        }
    }

    void addMessage(final String translationTemplateId, final Map<String, String> tokenValuePairs) {
        if (++errorCount <= errorLimit) {
            messages.add(new Message(translationTemplateId, tokenValuePairs));
        }
    }

    void addMessage(final String translationTemplateId, final String... tokensAndValues) {
        if (++errorCount <= errorLimit) {
            messages.add(new Message(translationTemplateId, tokensAndValues));
        }
    }

    private void writeId(final String id) throws XMLStreamException {
        writer.writeAttribute("id", ID_PREFIX + id);
    }

    private void writeRef(final String ref) throws XMLStreamException {
        writer.writeAttribute("ref", ID_PREFIX + ref);
    }

    private void addResult(final String id, final long startTimestamp, final String resultedFrom) {
        results.addLast(new ResultModelItem(id, startTimestamp, resultedFrom));
    }

    void addAttachment(final String eid, final IFile attachmentFile, final String label, final String encoding,
            final String mimeType, final String type) {
        attachments.put(eid, new Attachment(eid, attachmentFile, label, encoding, mimeType, type));
    }

    void addAttachment(final String eid, final byte[] base64EncodedContent, final String label, final String encoding,
            final String mimeType, final String type) {
        attachments.put(eid, new Attachment(eid, base64EncodedContent, label, encoding, mimeType, type));
    }

    public void addAttachmentRefs(final List<String> testStepAttachmentIds) throws XMLStreamException {
        writer.writeStartElement("attachments");
        for (int i = 0, testStepAttachmentIdsSize = testStepAttachmentIds.size(); i < testStepAttachmentIdsSize; i++) {
            writer.writeStartElement("attachment");
            writeRef(testStepAttachmentIds.get(i));
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    public void finalizeMessages() throws XMLStreamException {
        if (!messages.isEmpty()) {
            writer.writeStartElement("messages");
            for (final Message message : this.messages) {
                message.write();
            }
            if (errorCount > errorLimit) {
                // ... and {errorCount} more messages
                writer.writeStartElement("message");
                writer.writeAttribute("ref", TR_ERROR_LIMIT_EXCEEDED);
                writer.writeStartElement("translationArguments");
                writer.writeStartElement("argument");
                writer.writeAttribute("token", "errorCount");
                writer.writeCharacters(String.valueOf(errorCount - errorLimit));
                writer.writeEndElement();
                writer.writeEndElement();
                writer.writeEndElement();
            }
            errorCount = 0;
            writer.writeEndElement();
            messages.clear();
        }
    }

    final boolean isErrorLimitExceeded() {
        return errorCount >= errorLimit;
    }

    static private String fileTrippleSlash(final File file) {
        final String str = file.toURI().toString();
        return "file://" + str.substring(5, str.length());
    }

    static void internalError(
            final XMLStreamWriter errorWriter, final String resultedFrom, final String testObjectRef,
            final String errorMessage, final File logFile,
            final String errorAttachmentId, final IFile errorFile, final String mimeType) throws XMLStreamException {

        errorWriter.writeStartDocument("UTF-8", "1.0");
        errorWriter.writeStartElement(ETF_NS_PREFIX, "TestTaskResult", ETF_NS);
        errorWriter.setPrefix(ETF_NS_PREFIX, ETF_NS);
        errorWriter.writeNamespace(ETF_NS_PREFIX, ETF_NS);
        errorWriter.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        errorWriter.writeAttribute("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation",
                ETF_NS + " " + ETF_RESULT_XSD);
        errorWriter.writeAttribute("xmlns", ETF_NS);
        errorWriter.writeAttribute("id", ID_PREFIX + UUID.randomUUID().toString());

        errorWriter.writeStartElement("testObject");
        errorWriter.writeAttribute("ref", ID_PREFIX + testObjectRef);
        errorWriter.writeEndElement();

        errorWriter.writeStartElement("errorMessage");
        errorWriter.writeCharacters(errorMessage);
        errorWriter.writeEndElement();

        errorWriter.writeStartElement("attachments");

        errorWriter.writeStartElement("Attachment");
        errorWriter.writeAttribute("id", ID_PREFIX + errorAttachmentId);
        errorWriter.writeAttribute("type", "LogFile");

        errorWriter.writeStartElement("label");
        errorWriter.writeCharacters("Log file");
        errorWriter.writeEndElement();

        errorWriter.writeStartElement("encoding");
        errorWriter.writeCharacters("UTF-8");
        errorWriter.writeEndElement();

        errorWriter.writeStartElement("mimeType");
        errorWriter.writeCharacters("text/plain");
        errorWriter.writeEndElement();

        errorWriter.writeStartElement("referencedData");
        errorWriter.writeAttribute("href", fileTrippleSlash(logFile));
        errorWriter.writeEndElement();
        // end log file attachment
        errorWriter.writeEndElement();

        if (errorFile != null) {
            errorWriter.writeStartElement("Attachment");
            errorWriter.writeAttribute("id", ID_PREFIX + UUID.randomUUID().toString());
            errorWriter.writeAttribute("type", "internalError");

            errorWriter.writeStartElement("label");
            errorWriter.writeCharacters("Internal error");
            errorWriter.writeEndElement();

            errorWriter.writeStartElement("encoding");
            errorWriter.writeCharacters("UTF-8");
            errorWriter.writeEndElement();

            errorWriter.writeStartElement("mimeType");
            errorWriter.writeCharacters(mimeType);
            errorWriter.writeEndElement();

            errorWriter.writeStartElement("referencedData");
            errorWriter.writeAttribute("href", fileTrippleSlash(errorFile));
            errorWriter.writeEndElement();
            // end error file attachment
            errorWriter.writeEndElement();
        }
        // end attachments
        errorWriter.writeEndElement();

        errorWriter.writeStartElement("resultedFrom");
        errorWriter.writeAttribute("ref", ID_PREFIX + resultedFrom);
        errorWriter.writeEndElement();

        errorWriter.writeStartElement("startTimestamp");
        errorWriter.writeCharacters(TimeUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(System.currentTimeMillis()));
        errorWriter.writeEndElement();

        errorWriter.writeStartElement("duration");
        errorWriter.writeCharacters("1");
        errorWriter.writeEndElement();

        errorWriter.writeStartElement("status");
        errorWriter.writeCharacters(TestResultStatus.toString(TestResultStatus.INTERNAL_ERROR.value()));
        errorWriter.writeEndElement();

        errorWriter.writeEndElement();
        errorWriter.writeEndDocument();
        errorWriter.flush();
        errorWriter.close();
    }
}
