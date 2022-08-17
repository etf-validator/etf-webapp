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
package de.interactive_instruments.etf.testdriver.detection;

import java.util.Collection;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import de.interactive_instruments.MediaType;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectDto;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectTypeDto;
import de.interactive_instruments.etf.model.EID;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class WfsTypeDetector implements XmlTypeDetector {

    @Override
    public XmlTypeDetectionCmd createCmd(final XMLReader xmlReader) {
        return null;
    }

    @Override
    public Collection<MediaType> supportedTypes() {
        return null;
    }

    @Override
    public boolean supportsDetectionByMimeType() {
        return true;
    }

    @Override
    public boolean supportsDetectionByFileExtension() {
        return true;
    }

    @Override
    public boolean supportsDetectionByContent() {
        return true;
    }

    @Override
    public EID getId() {
        return null;
    }

    private static class WfsTypeDetectorCmd implements XmlTypeDetectionCmd {

        private Status status = Status.NEED_MORE_DATA;
        // Wfs title
        private String label;
        // Wfs description
        private String description;
        int nodeLevel = 0;

        @Override
        public Status status() {
            return status;
        }

        @Override
        public void setType(final TestObjectDto dto) {

        }

        @Override
        public Collection<TestObjectTypeDto> getDetectibleTypes() {
            return null;
        }

        @Override
        public void setDocumentLocator(final Locator locator) {

        }

        @Override
        public void startDocument() throws SAXException {
            nodeLevel = 0;
            label = null;
            description = null;
        }

        @Override
        public void endDocument() throws SAXException {

        }

        @Override
        public void startPrefixMapping(final String prefix, final String uri) throws SAXException {

        }

        @Override
        public void endPrefixMapping(final String prefix) throws SAXException {

        }

        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
                throws SAXException {

        }

        @Override
        public void endElement(final String uri, final String localName, final String qName) throws SAXException {

        }

        @Override
        public void characters(final char[] ch, final int start, final int length) throws SAXException {

        }

        @Override
        public void ignorableWhitespace(final char[] ch, final int start, final int length) throws SAXException {

        }

        @Override
        public void processingInstruction(final String target, final String data) throws SAXException {

        }

        @Override
        public void skippedEntity(final String name) throws SAXException {

        }
    }
}
