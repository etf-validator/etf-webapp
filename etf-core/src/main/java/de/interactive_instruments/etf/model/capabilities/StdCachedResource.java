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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import de.interactive_instruments.exceptions.ExcUtils;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class StdCachedResource implements CachedResource {

    private byte[] cache;
    private final Resource wrapped;

    public StdCachedResource(final Resource resource) {
        wrapped = resource.createCopy();
        if (resource instanceof CachedResource) {
            try {
                this.cache = ((CachedResource) resource).getFromCache();
            } catch (IOException ign) {
                // Ignore here, will be thrown when getFromCache() is called
                ExcUtils.suppress(ign);
            }
        }
    }

    private StdCachedResource(final StdCachedResource other) {
        this.cache = other.cache;
        this.wrapped = other.wrapped;
    }

    public byte[] getFromCache() {
        if (cache == null) {
            try {
                recache();
            } catch (IOException ign) {
                ExcUtils.suppress(ign);
            }
        }
        return cache;
    }

    public byte[] recache() throws IOException {
        cache = wrapped.getBytes();
        return cache;
    }

    @Override
    public boolean recacheIfModified() throws IOException {
        return false;
    }

    @Override
    public InputStream openStream() throws IOException {
        return new ByteArrayInputStream(getFromCache());
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }

    @Override
    public URI getUri() {
        return wrapped.getUri();
    }

    @Override
    public long getContentLength() throws IOException {
        return getFromCache().length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return getFromCache();
    }

    @Override
    public boolean exists() {
        return wrapped.exists();
    }

    @Override
    public void release() {
        cache = null;
        wrapped.release();
    }

    @Override
    public StdCachedResource createCopy() {
        return new StdCachedResource(this);
    }
}
