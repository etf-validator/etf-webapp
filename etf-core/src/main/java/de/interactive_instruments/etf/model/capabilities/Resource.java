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
import java.io.InputStream;
import java.net.URI;

import de.interactive_instruments.Copyable;
import de.interactive_instruments.Credentials;
import de.interactive_instruments.Releasable;
import de.interactive_instruments.UriUtils;
import de.interactive_instruments.io.PConn;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface Resource extends Releasable, Copyable<Resource> {

    String getName();

    URI getUri();

    long getContentLength() throws IOException;

    InputStream openStream() throws IOException;

    byte[] getBytes() throws IOException;

    boolean exists();

    static Resource create(final String name, final URI uri, final Credentials credentials) {
        if (UriUtils.isFile(uri)) {
            return new LocalResource(name, uri);
        } else {
            return new StdRemoteResource(name, PConn.create(uri, credentials));
        }
    }

    static CachedResource toCached(final Resource resource) {
        if (resource instanceof CachedResource) {
            return (CachedResource) resource;
        } else if (resource instanceof RemoteResource) {
            return toCached((RemoteResource) resource);
        } else if (resource instanceof LocalResource) {
            return toCached((LocalResource) resource);
        } else {
            return new StdCachedResource(resource);
        }
    }

    static CachedRemoteResource toCached(final RemoteResource resource) {
        if (resource instanceof CachedRemoteResource) {
            return (CachedRemoteResource) resource;
        }
        return new CachedRemoteResource(resource);
    }

    static CachedLocalResource toCached(final LocalResource resource) {
        if (resource instanceof CachedLocalResource) {
            return (CachedLocalResource) resource;
        }
        return new CachedLocalResource(resource);
    }
}
