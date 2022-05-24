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
package de.interactive_instruments.etf.dal.dto.capabilities;

import de.interactive_instruments.etf.dal.dto.ModelItemDto;
import de.interactive_instruments.etf.dal.dto.RepositoryItemDto;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class CredentialDto extends RepositoryItemDto {

    private ModelItemDto applicableTo;
    private String applicableUri;
    private byte[] cipher;

    public CredentialDto() {}

    private CredentialDto(final CredentialDto other) {
        super(other);
        this.applicableTo = other.applicableTo;
        this.applicableUri = other.applicableUri;
        this.cipher = other.cipher;
    }

    public ModelItemDto getApplicableTo() {
        return applicableTo;
    }

    public void setApplicableTo(final ModelItemDto applicableTo) {
        this.applicableTo = applicableTo;
    }

    public String getApplicableUri() {
        return applicableUri;
    }

    public void setApplicableUri(final String applicableUri) {
        this.applicableUri = applicableUri;
    }

    public byte[] getCipher() {
        return cipher;
    }

    public void setCipher(final byte[] cipher) {
        this.cipher = cipher;
    }

    @Override
    public CredentialDto createCopy() {
        return new CredentialDto(this);
    }
}
