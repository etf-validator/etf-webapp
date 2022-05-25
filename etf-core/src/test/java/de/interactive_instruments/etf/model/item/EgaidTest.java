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
package de.interactive_instruments.etf.model.item;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import de.interactive_instruments.etf.dal.dto.test.ExecutableTestSuiteDto;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class EgaidTest {

    @Test
    public void testSetEgaid() {
        final ExecutableTestSuiteDto dto = new ExecutableTestSuiteDto();
        dto.setEgaId("egaid.eu.inspire.validation.ets.downloadservices.atompredefined");
        assertEquals("eu.inspire.validation.ets.downloadservices", dto.getEgaId().getGroupId());
        assertEquals("atompredefined", dto.getEgaId().getArtifactId());
        assertEquals(null, dto.getEgaId().getVersion());
        assertEquals(null, dto.getVersion());
    }

    @Test
    public void testSetEgaidWithVersion1() {
        final ExecutableTestSuiteDto dto = new ExecutableTestSuiteDto();
        dto.setEgaId("egaid.eu.inspire.validation.ets.downloadservices.atompredefined:1.0.0");
        assertEquals("eu.inspire.validation.ets.downloadservices", dto.getEgaId().getGroupId());
        assertEquals("atompredefined", dto.getEgaId().getArtifactId());
        assertEquals("1.0.0", dto.getEgaId().getVersion().getAsString());
        assertEquals("1.0.0", dto.getVersion().getAsString());
    }

    @Test
    public void testSetEgaidNotOverwriteVersion() {
        final ExecutableTestSuiteDto dto = new ExecutableTestSuiteDto();
        dto.setVersionFromStr("1.0.2");
        dto.setEgaId("egaid.eu.inspire.validation.ets.downloadservices.atompredefined");
        assertEquals("eu.inspire.validation.ets.downloadservices", dto.getEgaId().getGroupId());
        assertEquals("atompredefined", dto.getEgaId().getArtifactId());
        assertEquals("1.0.2", dto.getEgaId().getVersion().getAsString());
        assertEquals("1.0.2", dto.getVersion().getAsString());
    }

    @Test
    public void testEgaidRef() {
        final ExecutableTestSuiteDto dto = new ExecutableTestSuiteDto();
        dto.setVersionFromStr("1.0.2");
        dto.setEgaId("egaid.eu.inspire.validation.ets.downloadservices.atompredefined");
        assertEquals("eu.inspire.validation.ets.downloadservices.atompredefined:1.0.2", dto.getEgaId().getEgaIdRef());
    }
}
