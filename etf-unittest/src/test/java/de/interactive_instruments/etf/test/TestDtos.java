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
package de.interactive_instruments.etf.test;

import java.net.URI;
import java.util.*;

import de.interactive_instruments.SUtils;
import de.interactive_instruments.etf.dal.dto.Dto;
import de.interactive_instruments.etf.dal.dto.MetaDataItemDto;
import de.interactive_instruments.etf.dal.dto.RepositoryItemDto;
import de.interactive_instruments.etf.dal.dto.capabilities.*;
import de.interactive_instruments.etf.dal.dto.result.*;
import de.interactive_instruments.etf.dal.dto.run.TestRunDto;
import de.interactive_instruments.etf.dal.dto.run.TestTaskDto;
import de.interactive_instruments.etf.dal.dto.test.*;
import de.interactive_instruments.etf.dal.dto.translation.TranslationArgumentCollectionDto;
import de.interactive_instruments.etf.dal.dto.translation.TranslationTemplateBundleDto;
import de.interactive_instruments.etf.dal.dto.translation.TranslationTemplateDto;
import de.interactive_instruments.etf.model.EidFactory;
import de.interactive_instruments.etf.model.ParameterSet;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public final class TestDtos {

    public final static TestObjectDto TO_DTO_1;

    public final static TestObjectTypeDto TOT_DTO_1;
    public final static TestObjectTypeDto TOT_DTO_2;
    public final static TestObjectTypeDto TOT_DTO_3;

    public final static TagDto TAG_DTO_1;
    public final static TagDto TAG_DTO_2;
    public final static TagDto TAG_DTO_3;

    public final static TestRunDto TR_DTO_1;

    public final static TestTaskDto TASK_DTO_1;
    public final static TestTaskDto TASK_DTO_2;

    public static final TestItemTypeDto ASSERTION_TYPE_1;
    public static final TestItemTypeDto TESTSTEP_TYPE_2;

    public static final ExecutableTestSuiteDto ETS_DTO_1;
    public static final ExecutableTestSuiteDto ETS_DTO_2;
    public static final ExecutableTestSuiteDto ETS_DTO_3;

    public static final TestTaskResultDto TTR_DTO_1;
    public static final TestTaskResultDto TTR_DTO_2;

    public static final TranslationTemplateBundleDto TTB_DTO_1;

    public static final ComponentDto COMP_DTO_1;

    static {

        COMP_DTO_1 = new ComponentDto();
        setBasicProperties(COMP_DTO_1, 1);
        COMP_DTO_1.setVendor("ii");
        COMP_DTO_1.setVersion("1.1.0");
        COMP_DTO_1.setId(EidFactory.getDefault().createAndPreserveStr("4dddc9e2-1b21-40b7-af70-6a2d156ad130"));

        TAG_DTO_1 = new TagDto();
        setBasicProperties(TAG_DTO_1, 1);

        TAG_DTO_2 = new TagDto();
        setBasicProperties(TAG_DTO_2, 2);

        TAG_DTO_3 = new TagDto();
        setBasicProperties(TAG_DTO_3, 3);

        TO_DTO_1 = new TestObjectDto();
        setBasicProperties(TO_DTO_1, 1);
        TO_DTO_1.setRemoteResource(null);

        TOT_DTO_1 = new TestObjectTypeDto();
        setBasicProperties(TOT_DTO_1, 1);

        TOT_DTO_2 = new TestObjectTypeDto();
        setBasicProperties(TOT_DTO_2, 2);

        TOT_DTO_3 = new TestObjectTypeDto();
        setBasicProperties(TOT_DTO_3, 3);
        TOT_DTO_3.setId(EidFactory.getDefault().createAndPreserveStr("e1d4a306-7a78-4a3b-ae2d-cf5f0810853e"));

        TO_DTO_1.addTestObjectType(TOT_DTO_1);
        final ResourceDto resourceDto = new ResourceDto();
        resourceDto.setName("Resource.1");
        resourceDto.setUri(URI.create("http://nowhere.com"));
        TO_DTO_1.addResource(resourceDto);
        TO_DTO_1.setTags(new ArrayList<TagDto>() {
            {
                add(TAG_DTO_1);
            }
        });

        TTB_DTO_1 = new TranslationTemplateBundleDto();
        setBasicProperties(TTB_DTO_1, 1);
        TTB_DTO_1.setId(EidFactory.getDefault().createAndPreserveStr("70a263c0-0ad7-42f2-9d4d-0d8a4ca71b52"));

        final List<TranslationTemplateDto> translationTemplateDtos = new ArrayList<TranslationTemplateDto>() {
            {
                final TranslationTemplateDto template1En = new TranslationTemplateDto(
                        "TR.Template.1", Locale.ENGLISH.toLanguageTag(),
                        "TR.Template.1 with three tokens: {TOKEN.3} {TOKEN.1} {TOKEN.2}");
                final TranslationTemplateDto template1De = new TranslationTemplateDto(
                        "TR.Template.1", Locale.GERMAN.toLanguageTag(),
                        "TR.Template.1 mit drei tokens: {TOKEN.3} {TOKEN.1} {TOKEN.2}");
                final TranslationTemplateDto template2En = new TranslationTemplateDto(
                        "TR.Template.2", Locale.ENGLISH.toLanguageTag(),
                        "TR.Template.2 with three tokens: {TOKEN.5} {TOKEN.4} {TOKEN.6}");
                final TranslationTemplateDto template2De = new TranslationTemplateDto(
                        "TR.Template.2", Locale.GERMAN.toLanguageTag(),
                        "TR.Template.2 mit drei tokens: {TOKEN.5} {TOKEN.4} {TOKEN.6}");
                add(template1En);
                add(template1De);
                add(template2En);
                add(template2De);
            }
        };
        TTB_DTO_1.addTranslationTemplates(translationTemplateDtos);

        ASSERTION_TYPE_1 = new TestItemTypeDto();
        setBasicProperties(ASSERTION_TYPE_1, 1);

        TESTSTEP_TYPE_2 = new TestItemTypeDto();
        setBasicProperties(TESTSTEP_TYPE_2, 2);

        final ParameterSet parameterSet = new ParameterSet();
        parameterSet.addParameter(new ParameterSet.MutableParameter("Parameter.1.key", "Parameter.1.value"));
        parameterSet.addParameter(new ParameterSet.MutableParameter("Parameter.2.key", "Parameter.2.value"));

        // ETS DTO 1
        ETS_DTO_1 = new ExecutableTestSuiteDto();
        ETS_DTO_1.setTranslationTemplateBundle(TTB_DTO_1);
        ETS_DTO_1.setParameters(parameterSet);
        ETS_DTO_1.setTags(new ArrayList<TagDto>() {
            {
                add(TAG_DTO_1);
                add(TAG_DTO_2);
            }
        });
        createEtsStructure(ETS_DTO_1, 1);

        TTR_DTO_1 = new TestTaskResultDto();
        createResultStructure(TTR_DTO_1, ETS_DTO_1, 1);

        TASK_DTO_1 = new TestTaskDto();
        setBasicProperties(TASK_DTO_1, 1);
        TASK_DTO_1.setTestObject(TO_DTO_1);
        TASK_DTO_1.setExecutableTestSuite(ETS_DTO_1);
        TASK_DTO_1.setTestTaskResult(TTR_DTO_1);

        // ETS DTO 2
        ETS_DTO_2 = new ExecutableTestSuiteDto();
        ETS_DTO_2.setTranslationTemplateBundle(TTB_DTO_1);
        ETS_DTO_2.setParameters(parameterSet);
        ETS_DTO_2.setTags(new ArrayList<TagDto>() {
            {
                add(TAG_DTO_2);
                add(TAG_DTO_3);
            }
        });
        createEtsStructure(ETS_DTO_2, 2);

        TTR_DTO_2 = new TestTaskResultDto();
        createResultStructure(TTR_DTO_2, ETS_DTO_2, 2);

        TASK_DTO_2 = new TestTaskDto();
        setBasicProperties(TASK_DTO_2, 2);
        TASK_DTO_2.setTestObject(TO_DTO_1);
        TASK_DTO_2.setExecutableTestSuite(ETS_DTO_2);
        TASK_DTO_2.setTestTaskResult(TTR_DTO_2);

        // ETS DTO 3
        ETS_DTO_3 = new ExecutableTestSuiteDto();
        ETS_DTO_3.setTranslationTemplateBundle(TTB_DTO_1);
        ETS_DTO_3.setParameters(parameterSet);
        ETS_DTO_3.setTags(new ArrayList<TagDto>() {
            {
                add(TAG_DTO_2);
                add(TAG_DTO_3);
            }
        });
        createEtsStructure(ETS_DTO_3, 3);
        ETS_DTO_3.setDisabled(true);

        TR_DTO_1 = new TestRunDto();
        setBasicProperties(TR_DTO_1, 1);
        TR_DTO_1.setLabel("Tag." + toStrWithTrailingZeros(1) + ".label");
        TR_DTO_1.setStartTimestamp(new Date(0));
        TR_DTO_1.setDefaultLang(Locale.ENGLISH.toLanguageTag());
        TR_DTO_1.setLogPath("/tr.log");
        TR_DTO_1.setTestTasks(new ArrayList<TestTaskDto>() {
            {
                add(TASK_DTO_1);
                add(TASK_DTO_2);
            }
        });
    }

    static String toStrWithTrailingZeros(int i) {
        return String.format("%05d", i);
    }

    static String toStrWithTrailingZeros(long i) {
        return String.format("%05d", i);
    }

    public static void setBasicProperties(final Dto dto, final long i) {
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
            rDto.setLocalPath("/");
            rDto.setRemoteResource(URI.create("http://notset"));
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

    static private final int testModuleSize = 5;
    static private final int testCaseSize = 3;
    static private final int testStepSize = 3;
    static private final int testAssertionSize = 3;

    public static void createResultStructure(final TestTaskResultDto ttrDto, final ExecutableTestSuiteDto etsDto, final int i) {
        setBasicProperties(ttrDto, i);
        ttrDto.setResultedFrom(etsDto);
        ttrDto.setTestObject(TO_DTO_1);
        final AttachmentDto logFile = new AttachmentDto();
        setBasicProperties(logFile, 1);
        logFile.setLabel("Log file");
        logFile.setReferencedData(URI.create("http://logfile"));
        logFile.setEncoding("UTF-8");
        logFile.setMimeType("text/plain");
        ttrDto.addAttachment(logFile);

        long idR = i * 1111111111111L;

        // Create Test Suite Results
        final List<TestModuleResultDto> testSuiteResultDtos = new ArrayList<TestModuleResultDto>();
        for (int tsi = 0; tsi < testModuleSize; tsi++) {
            final TestModuleResultDto testSuiteResultDto = new TestModuleResultDto();
            setBasicProperties(testSuiteResultDto, idR--);
            testSuiteResultDto.setParent(ttrDto);
            testSuiteResultDto.setResultedFrom(etsDto.getTestModules().get(tsi));

            final List<TestCaseResultDto> testCaseResultDtos = new ArrayList<>();
            for (int tci = 0; tci < testCaseSize; tci++) {
                final TestCaseResultDto testCaseResultDto = new TestCaseResultDto();
                setBasicProperties(testCaseResultDto, idR--);
                testCaseResultDto.setParent(testSuiteResultDto);
                testCaseResultDto.setResultedFrom(
                        ((TestModuleDto) testSuiteResultDto.getResultedFrom()).getTestCases().get(tci));

                final List<TestStepResultDto> testStepResultDtos = new ArrayList<>();
                for (int tsti = 0; tsti < testStepSize; tsti++) {
                    final TestStepResultDto testStepResultDto = new TestStepResultDto();
                    setBasicProperties(testStepResultDto, idR--);
                    testStepResultDto.setParent(testCaseResultDto);
                    testStepResultDto.setResultedFrom(
                            ((TestCaseDto) testCaseResultDto.getResultedFrom()).getTestSteps().get(tsti));

                    final List<TestAssertionResultDto> testAssertionResultDtos = new ArrayList<>();
                    for (int ta = 0; ta < testAssertionSize; ta++) {
                        final TestAssertionResultDto testAssertionResultDto = new TestAssertionResultDto();
                        setBasicProperties(testAssertionResultDto, idR--);
                        testAssertionResultDto.setParent(testStepResultDto);
                        testAssertionResultDto.setResultedFrom(
                                ((TestStepDto) testStepResultDto.getResultedFrom()).getTestAssertions().get(ta));

                        testAssertionResultDto.setMessages(new ArrayList<TranslationArgumentCollectionDto>() {
                            {
                                final TranslationArgumentCollectionDto messages1 = new TranslationArgumentCollectionDto();
                                messages1.setRefTemplateName("TR.Template.1");
                                messages1.addTokenValue("TOKEN.1", "Value1");
                                messages1.addTokenValue("TOKEN.2", "Value2");
                                messages1.addTokenValue("TOKEN.3", "Value3");
                                final TranslationArgumentCollectionDto messages2 = new TranslationArgumentCollectionDto();
                                messages2.setRefTemplateName("TR.Template.2");
                                messages2.addTokenValue("TOKEN.4", "Value4");
                                messages2.addTokenValue("TOKEN.5", "Value5");
                                messages2.addTokenValue("TOKEN.6", "Value6");
                                add(messages1);
                                add(messages2);
                            }
                        });

                        testAssertionResultDtos.add(testAssertionResultDto);
                    }
                    testStepResultDto.setTestAssertionResults(testAssertionResultDtos);
                    testStepResultDtos.add(testStepResultDto);
                }
                testCaseResultDto.setTestStepResults(testStepResultDtos);
                testCaseResultDtos.add(testCaseResultDto);

            }
            testSuiteResultDto.setTestCaseResults(testCaseResultDtos);
            testSuiteResultDtos.add(testSuiteResultDto);
        }
        ttrDto.setTestModuleResults(testSuiteResultDtos);
    }

    public static void createEtsStructure(final ExecutableTestSuiteDto etsDto) {
        createEtsStructure(etsDto, new Random().nextLong());
    }

    public static void createEtsStructure(ExecutableTestSuiteDto etsDto, int i) {
        createEtsStructure(etsDto, (long) i);
    }

    public static void createEtsStructure(ExecutableTestSuiteDto etsDto, long i) {
        setBasicProperties(etsDto, i);
        etsDto.setTestDriver(COMP_DTO_1);
        etsDto.setSupportedTestObjectTypes(new ArrayList<TestObjectTypeDto>() {
            {
                add(TOT_DTO_1);
            }
        });

        long idE = i * 11111111L;

        // Create Test Modules
        final List<TestModuleDto> testModuleDtos = new ArrayList<TestModuleDto>();
        for (int tmi = 0; tmi < testModuleSize; tmi++) {
            final TestModuleDto testModuleDto = new TestModuleDto();
            setBasicProperties(testModuleDto, (i + 1) + (tmi + 1) * 1000);
            testModuleDto.setParent(etsDto);

            final List<TestCaseDto> testCaseDtos = new ArrayList<>();
            for (int tci = 0; tci < testCaseSize; tci++) {
                final TestCaseDto testCaseDto = new TestCaseDto();
                setBasicProperties(testCaseDto, idE++);
                testCaseDto.setParent(testModuleDto);

                final List<TestStepDto> testStepDtos = new ArrayList<>();
                for (int tsti = 0; tsti < testStepSize; tsti++) {
                    final TestStepDto testStepDto = new TestStepDto();
                    setBasicProperties(testStepDto, idE++);
                    testStepDto.setParent(testCaseDto);
                    testStepDto.setStatementForExecution("ExecutionStatement");
                    testStepDto.setType(TESTSTEP_TYPE_2);

                    final List<TestAssertionDto> testAssertionDtos = new ArrayList<>();
                    for (int ta = 0; ta < testAssertionSize; ta++) {
                        final TestAssertionDto testAssertionDto = new TestAssertionDto();
                        setBasicProperties(testAssertionDto, idE++);
                        testAssertionDto.setParent(testStepDto);
                        testAssertionDto.setExpectedResult("ExpectedResult");
                        testAssertionDto.setExpression("Expression");
                        testAssertionDto.setType(ASSERTION_TYPE_1);
                        testAssertionDto.addTranslationTemplateWithName("TR.Template.1");
                        testAssertionDto.addTranslationTemplateWithName("TR.Template.2");
                        testAssertionDtos.add(testAssertionDto);
                    }
                    testStepDto.setTestAssertions(testAssertionDtos);
                    testStepDtos.add(testStepDto);
                }
                testCaseDto.setTestSteps(testStepDtos);
                testCaseDtos.add(testCaseDto);

            }
            testModuleDto.setTestCases(testCaseDtos);
            testModuleDtos.add(testModuleDto);
        }
        etsDto.setTestModules(testModuleDtos);
    }

}
