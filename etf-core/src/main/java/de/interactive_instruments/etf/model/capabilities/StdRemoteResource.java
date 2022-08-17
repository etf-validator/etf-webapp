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
import java.net.URLConnection;

import de.interactive_instruments.io.ConnectionUtils;
import de.interactive_instruments.io.PConn;
import de.interactive_instruments.io.ResourceModificationCheck;

/**
 * Immutable DefaultResource which does not expose the credentials
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class StdRemoteResource implements RemoteResource {

    private final String name;
    private final PConn preparedConnection;
    private ResourceModificationCheck check;

    StdRemoteResource(final String name, final PConn preparedConnection) {
        this.name = name;
        this.preparedConnection = preparedConnection;
    }

    private StdRemoteResource(final StdRemoteResource other) {
        this.name = other.name;
        this.preparedConnection = other.getPreparedConnection();
    }

    public boolean isModificationCheckInitialized() {
        return check != null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getContentLength() throws IOException {
        final URLConnection c = preparedConnection.openConnection();
        return c.getContentLengthLong();
    }

    @Override
    public InputStream openStream() throws IOException {
        return preparedConnection.openStream();
    }

    @Override
    public byte[] getBytes() throws IOException {
        return ConnectionUtils.contentAsByteArray(
                preparedConnection.openConnection());
    }

    @Override
    public boolean exists() {
        return preparedConnection.exists();
    }

    @Override
    public PConn getPreparedConnection() {
        return this.preparedConnection;
    }

    @Override
    public ResourceModificationCheck getModificationCheck() throws IOException {
        if (check == null) {
            check = new ResourceModificationCheck(this.preparedConnection);
        }
        return check;
    }

    @Override
    public void release() {
        check = null;
    }

    @Override
    public StdRemoteResource createCopy() {
        return new StdRemoteResource(this);
    }

    @Override
    public RemoteResource createNewWith(final PConn preparedConnection) {
        return new StdRemoteResource(name, preparedConnection);
    }
}
