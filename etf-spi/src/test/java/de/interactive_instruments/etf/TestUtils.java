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
package de.interactive_instruments.etf;

import java.net.URI;
import java.util.Date;

import de.interactive_instruments.SUtils;
import de.interactive_instruments.etf.dal.dto.Dto;
import de.interactive_instruments.etf.dal.dto.MetaDataItemDto;
import de.interactive_instruments.etf.dal.dto.RepositoryItemDto;
import de.interactive_instruments.etf.dal.dto.capabilities.ComponentDto;
import de.interactive_instruments.etf.dal.dto.result.ResultModelItemDto;
import de.interactive_instruments.etf.dal.dto.result.TestResultStatus;
import de.interactive_instruments.etf.dal.dto.test.ExecutableTestSuiteDto;
import de.interactive_instruments.etf.model.EidFactory;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
class TestUtils {

    private TestUtils() {}

    static final ComponentDto COMP_DTO_1;

    static {
        COMP_DTO_1 = new ComponentDto();
        setBasicProperties(COMP_DTO_1, 1);
        COMP_DTO_1.setVendor("ii");
        COMP_DTO_1.setVersion("1.1.0");
    }

    static String toStrWithTrailingZeros(int i) {
        return String.format("%05d", i);
    }

    static String toStrWithTrailingZeros(long i) {
        return String.format("%05d", i);
    }

    static void setBasicProperties(final Dto dto, final long i) {
        final String name = dto.getClass().getSimpleName() + "." + toStrWithTrailingZeros(i);
        dto.setId(EidFactory.getDefault().createUUID(name));
        if (dto instanceof MetaDataItemDto) {
            final MetaDataItemDto mDto = ((MetaDataItemDto) dto);
            mDto.setLabel(name + ".label");
            mDto.setDescription(name + ".description");
        }
        if (dto instanceof RepositoryItemDto) {
            final RepositoryItemDto rDto = ((RepositoryItemDto) dto);
            rDto.setAuthor(name + ".author");
            rDto.setRemoteResource(URI.create("http://notset"));
            rDto.setLocalPath("/");
            rDto.setCreationDate(new Date(0));
            rDto.setVersionFromStr("1.0.0");
            rDto.setItemHash(SUtils.fastCalcHashAsHexStr(name));
        }
        if (dto instanceof ResultModelItemDto) {
            final ResultModelItemDto rDto = ((ResultModelItemDto) dto);
            rDto.setStartTimestamp(new Date(0));
            rDto.setResultStatus(TestResultStatus.FAILED);
            rDto.setDuration(1000);
        }
    }

    static ExecutableTestSuiteDto createEts(final int nr) {
        return createEts(nr, null);
    }

    static ExecutableTestSuiteDto createEts(final int nr, final ComponentDto testDriver) {
        final ExecutableTestSuiteDto ets = new ExecutableTestSuiteDto();
        ets.setLabel("ETS." + String.valueOf(nr));
        ets.setId(EidFactory.getDefault().createUUID(ets.getLabel()));
        ets.setTestDriver(testDriver);
        return ets;
    }
}
