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

/**
 * A resource with an internal cache for faster access.
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface CachedResource extends Resource {

    /**
     * Invokes {@link CachedResource#recache()} if cache is empty.
     *
     * @return cached bytes
     * @throws IOException
     *             if {@link CachedResource#recache()} fails
     */
    byte[] getFromCache() throws IOException;

    /**
     * Rebuild cache.
     *
     * @return new fetched bytes
     * @throws IOException
     *             if fetching failed
     */
    byte[] recache() throws IOException;

    /**
     * Invokes {@link CachedResource#recache()} if the resource has changed since the last call of this method.
     *
     * @return true if resource changed, false otherwise
     * @throws IOException
     *             if {@link CachedResource#recache()} fails
     */
    boolean recacheIfModified() throws IOException;

    @Override
    CachedResource createCopy();
}
