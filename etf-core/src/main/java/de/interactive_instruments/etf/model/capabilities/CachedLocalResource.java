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
package de.interactive_instruments.etf.model.capabilities;

import java.io.IOException;
import java.net.URI;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class CachedLocalResource extends LocalResource implements CachedResource {

    private byte[] cache;
    private long timestamp;

    CachedLocalResource(final String name, final URI uri) {
        super(name, uri);
        timestamp = file.lastModified();
    }

    CachedLocalResource(final Resource resource) {
        super(resource.getName(), resource.getUri());
    }

    private CachedLocalResource(final CachedLocalResource other) {
        super(other);
        this.cache = other.cache;
        this.timestamp = other.timestamp;
    }

    @Override
    public byte[] getFromCache() {
        return cache;
    }

    @Override
    public byte[] recache() throws IOException {
        cache = super.getBytes();
        return cache;
    }

    @Override
    public boolean recacheIfModified() throws IOException {
        if (cache == null) {
            recache();
        }
        if (timestamp != file.lastModified()) {
            timestamp = file.lastModified();
            return true;
        }
        return false;
    }

    @Override
    public CachedLocalResource createCopy() {
        return new CachedLocalResource(this);
    }
}
