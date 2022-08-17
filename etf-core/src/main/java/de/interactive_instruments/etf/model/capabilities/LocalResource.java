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
package de.interactive_instruments.etf.model.capabilities;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.commons.io.IOUtils;

import de.interactive_instruments.IFile;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class LocalResource implements Resource {

    private final String name;
    protected final IFile file;

    public LocalResource(final String name, final IFile file) {
        this.name = name;
        this.file = file;
    }

    public LocalResource(LocalResource resource) {
        this.name = resource.name;
        this.file = resource.file;
    }

    /**
     * Package ctor for the {@link Resource#create(String, URI)} method
     *
     * @param name
     *            Resource name
     * @param uri
     *            file URI
     */
    LocalResource(final String name, final URI uri) {
        this.name = name;
        this.file = new IFile(uri, name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public URI getUri() {
        return file.toURI();
    }

    public IFile getFile() {
        return file;
    }

    @Override
    public long getContentLength() throws IOException {
        return file.length();
    }

    @Override
    public InputStream openStream() throws IOException {
        return file.getInputStream();
    }

    @Override
    public byte[] getBytes() throws IOException {
        return IOUtils.toByteArray(openStream());
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

    @Override
    public void release() {
        // nothing to do
    }

    @Override
    public LocalResource createCopy() {
        return new LocalResource(this);
    }
}
