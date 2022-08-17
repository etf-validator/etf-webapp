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
package de.interactive_instruments.etf.dal.dto.capabilities;

import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class ResourceDto {

    private CredentialDto credential;
    private String name;
    private String uri;

    public ResourceDto() {

    }

    public ResourceDto(final String name, final String uri) throws URISyntaxException {
        this.name = name;
        setUri(uri);
    }

    public ResourceDto(final String name, final URI uri) {
        this.name = name;
        setUri(uri);
    }

    public ResourceDto(final String name, final String uri, final CredentialDto credential) throws URISyntaxException {
        this.credential = credential;
        this.name = name;
        setUri(uri);
    }

    public ResourceDto(final String name, final URI uri, final CredentialDto credential) {
        this.credential = credential;
        this.name = name;
        setUri(uri);
    }

    public CredentialDto getCredential() {
        return credential;
    }

    public void setCredential(final CredentialDto credential) {
        this.credential = credential;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public URI getUri() {
        return URI.create(uri);
    }

    public void setUri(final URI uri) {
        this.uri = uri.toString();
    }

    public void setUri(final String uri) throws URISyntaxException {
        this.uri = new URI(uri).toString();
    }
}
