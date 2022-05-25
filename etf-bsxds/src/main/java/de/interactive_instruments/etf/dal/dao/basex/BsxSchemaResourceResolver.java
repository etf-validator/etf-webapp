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
package de.interactive_instruments.etf.dal.dao.basex;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import de.interactive_instruments.exceptions.ExcUtils;

/**
 * Resolver for schemas inside the jar package
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
final class BsxSchemaResourceResolver implements LSResourceResolver {

    private static class Input implements LSInput {

        private final String publicId;

        private final String systemId;

        private final BufferedInputStream inputStream;

        public String getPublicId() {
            return publicId;
        }

        public void setPublicId(String publicId) {
            throw new IllegalStateException("Not allowed");
        }

        public String getBaseURI() {
            return null;
        }

        public InputStream getByteStream() {
            return null;
        }

        public boolean getCertifiedText() {
            return false;
        }

        public Reader getCharacterStream() {
            return null;
        }

        public String getEncoding() {
            return null;
        }

        public String getStringData() {
            synchronized (inputStream) {
                try {
                    final byte[] input = new byte[inputStream.available()];
                    inputStream.read(input);
                    return new String(input, "UTF-8");
                } catch (IOException e) {
                    ExcUtils.suppress(e);
                    throw new IllegalStateException("Cannot access schema file: " + e);
                }
            }
        }

        public void setBaseURI(String baseURI) {}

        public void setByteStream(InputStream byteStream) {}

        public void setCertifiedText(boolean certifiedText) {}

        public void setCharacterStream(Reader characterStream) {}

        public void setEncoding(String encoding) {}

        public void setStringData(String stringData) {}

        public String getSystemId() {
            return systemId;
        }

        public void setSystemId(String systemId) {
            throw new IllegalStateException("Not allowed");
        }

        public Input(String publicId, String sysId, InputStream input) {
            this.publicId = publicId;
            this.systemId = sysId;
            this.inputStream = new BufferedInputStream(input);
        }
    }

    public LSInput resolveResource(String type, String namespaceURI,
            String publicId, String systemId, String baseURI) {
        final String path;
        if (!systemId.startsWith("../")) {
            path = "schema/model/" + systemId;
        } else {
            path = "schema/" + systemId.substring(3);
        }
        final InputStream resourceAsStream = this.getClass().getClassLoader()
                .getResourceAsStream(path);
        return new Input(publicId, systemId, resourceAsStream);
    }
}
