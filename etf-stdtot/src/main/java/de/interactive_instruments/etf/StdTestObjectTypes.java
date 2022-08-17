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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

import de.interactive_instruments.IFile;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectTypeDto;
import de.interactive_instruments.etf.model.*;

/**
 * Standard Test Object Types.
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 * @author Clemens Portele ( portele aT interactive-instruments doT de )
 */
public class StdTestObjectTypes {

    // Supported Test Object Types
    public static final TestObjectTypeDto WEB_SERVICE_TOT = new TestObjectTypeDto();
    private static EID WEB_SERVICE_ID = EidFactory.getDefault().createAndPreserveStr("88311f83-818c-46ed-8a9a-cec4f3707365");

    public static final TestObjectTypeDto OGC_API_TOT = new TestObjectTypeDto();
    private static EID OGC_API_ID = EidFactory.getDefault()
            .createAndPreserveStr("c44261aa-02a9-4b21-9621-88c21032552c");

    public static final TestObjectTypeDto OGC_API_PROCESSES_1_TOT = new TestObjectTypeDto();
    private static EID OGC_API_PROCESSES_1_ID = EidFactory.getDefault()
            // org.ogc.ogcapi.processes-1:1.0
            .createAndPreserveStr("d576744d-95b1-374e-a19a-fdac61a2c226");

    public static final TestObjectTypeDto OGC_API_FEATURES_1_TOT = new TestObjectTypeDto();
    private static EID OGC_API_FEATURES_1_ID = EidFactory.getDefault()
            // org.ogc.ogcapi.features-1:1.0
            .createAndPreserveStr("63b1072e-90e6-317e-accb-3cd59d037e20");

    private static final TestObjectTypeDto WFS_TOT = new TestObjectTypeDto();
    private static EID WFS_ID = EidFactory.getDefault().createAndPreserveStr("db12feeb-0086-4006-bc74-28f4fdef0171");

    private static final TestObjectTypeDto WFS_2_0_TOT = new TestObjectTypeDto();
    private static EID WFS_2_0_ID = EidFactory.getDefault().createAndPreserveStr("9b6ef734-981e-4d60-aa81-d6730a1c6389");

    private static final TestObjectTypeDto WFS_1_1_TOT = new TestObjectTypeDto();
    private static EID WFS_1_1_ID = EidFactory.getDefault().createAndPreserveStr("bc6384f3-2652-4c7b-bc45-20cec488ecd0");

    private static final TestObjectTypeDto WFS_1_0_TOT = new TestObjectTypeDto();
    private static EID WFS_1_0_ID = EidFactory.getDefault().createAndPreserveStr("8a560e6a-043f-42ca-b0a3-31b115899593");

    private static final TestObjectTypeDto WMS_TOT = new TestObjectTypeDto();
    private static EID WMS_ID = EidFactory.getDefault().createAndPreserveStr("bae0df71-0553-438d-938f-028b53ba8aa7");

    private static final TestObjectTypeDto WMS_1_3_TOT = new TestObjectTypeDto();
    private static EID WMS_1_3_ID = EidFactory.getDefault().createAndPreserveStr("9981e87e-d642-43b3-ad5f-e77469075e74");

    private static final TestObjectTypeDto WMS_1_1_TOT = new TestObjectTypeDto();
    private static EID WMS_1_1_ID = EidFactory.getDefault().createAndPreserveStr("d1836a8d-9909-4899-a0bc-67f512f5f5ac");

    private static final TestObjectTypeDto WMTS_TOT = new TestObjectTypeDto();
    private static EID WMTS_ID = EidFactory.getDefault().createAndPreserveStr("380b969c-215e-46f8-a4e9-16f002f7d6c3");

    private static final TestObjectTypeDto WMTS_1_0_TOT = new TestObjectTypeDto();
    private static EID WMTS_1_0_ID = EidFactory.getDefault().createAndPreserveStr("ae35f7cd-86d9-475a-aa3a-e0bfbda2bb5f");

    private static final TestObjectTypeDto WCS_TOT = new TestObjectTypeDto();
    private static EID WCS_ID = EidFactory.getDefault().createAndPreserveStr("df841ddd-20d4-4551-8bc2-a4f7267e39e0");

    private static final TestObjectTypeDto WCS_2_0_TOT = new TestObjectTypeDto();
    private static EID WCS_2_0_ID = EidFactory.getDefault().createAndPreserveStr("dac58b52-3ffd-4eb5-96e3-64723d8f0f51");

    private static final TestObjectTypeDto WCS_1_1_TOT = new TestObjectTypeDto();
    private static EID WCS_1_1_ID = EidFactory.getDefault().createAndPreserveStr("824596fa-ec04-4314-bf1a-f1e6ee119bf0");

    private static final TestObjectTypeDto WCS_1_0_TOT = new TestObjectTypeDto();
    private static EID WCS_1_0_ID = EidFactory.getDefault().createAndPreserveStr("4d4bffed-0a18-43d3-98f4-f5e7055b02e4");

    private static final TestObjectTypeDto SOS_TOT = new TestObjectTypeDto();
    private static EID SOS_ID = EidFactory.getDefault().createAndPreserveStr("adeb8bc4-c49b-4704-ba88-813aea5de31d");

    private static final TestObjectTypeDto SOS_2_0_TOT = new TestObjectTypeDto();
    private static EID SOS_2_0_ID = EidFactory.getDefault().createAndPreserveStr("f897f313-55f0-4e51-928a-0e9869f5a1d6");

    private static final TestObjectTypeDto CSW_TOT = new TestObjectTypeDto();
    private static EID CSW_ID = EidFactory.getDefault().createAndPreserveStr("18bcbc68-56b9-4e8e-b0d1-90de324d0cc8");

    private static final TestObjectTypeDto CSW_3_0_TOT = new TestObjectTypeDto();
    private static EID CSW_3_0_ID = EidFactory.getDefault().createAndPreserveStr("b2a780a8-5bba-4780-bcd5-c8c909ac407d");

    private static final TestObjectTypeDto CSW_2_0_2_TOT = new TestObjectTypeDto();
    private static EID CSW_2_0_2_ID = EidFactory.getDefault().createAndPreserveStr("4b0fb35d-10f0-47df-bc0b-6d4548035ae2");

    private static final TestObjectTypeDto CSW_2_0_2_EBRIM_1_0_TOT = new TestObjectTypeDto();
    private static EID CSW_2_0_2_EBRIM_1_0_ID = EidFactory.getDefault()
            .createAndPreserveStr("9b101002-e65e-4d96-ac45-fcb95ac6f507");

    private static final TestObjectTypeDto ATOM_TOT = new TestObjectTypeDto();
    private static EID ATOM_ID = EidFactory.getDefault().createAndPreserveStr("49d881ae-b115-4b91-aabe-31d5791bce52");

    private static final TestObjectTypeDto DOCUMENTS_TOT = new TestObjectTypeDto();
    private static EID DOCUMENTS_ID = EidFactory.getDefault().createAndPreserveStr("bec4dd69-72b9-498e-a693-88e3d59d2552");

    public static final TestObjectTypeDto XML_DOCUMENTS_TOT = new TestObjectTypeDto();
    private static EID XML_DOCUMENTS_ID = EidFactory.getDefault().createAndPreserveStr("810fce18-4bf5-4c6c-a972-6962bbe3b76b");

    private static final TestObjectTypeDto GML_FEATURE_COLLECTION_TOT = new TestObjectTypeDto();
    private static EID GML_FEATURE_COLLECTION_ID = EidFactory.getDefault()
            .createAndPreserveStr("e1d4a306-7a78-4a3b-ae2d-cf5f0810853e");

    private static final TestObjectTypeDto WFS20_FEATURE_COLLECTION_TOT = new TestObjectTypeDto();
    private static EID WFS20_FEATURE_COLLECTION_ID = EidFactory.getDefault()
            .createAndPreserveStr("a8a1b437-0ebf-454c-8204-bcf0b8548d8c");

    private static final TestObjectTypeDto GML32_FEATURE_COLLECTION_TOT = new TestObjectTypeDto();
    private static EID GML32_FEATURE_COLLECTION_ID = EidFactory.getDefault()
            .createAndPreserveStr("c8aaacd7-df33-4d64-89af-fabeae63a958");

    private static final TestObjectTypeDto GML31_GML21_FEATURE_COLLECTION_TOT = new TestObjectTypeDto();
    private static EID GML31_GML21_FEATURE_COLLECTION_ID = EidFactory.getDefault()
            .createAndPreserveStr("123b2f9b-c9f4-4379-8bf1-e9a656a14bd0");

    private static final TestObjectTypeDto INSPIRE_SPATIAL_DATASET_TOT = new TestObjectTypeDto();
    private static EID INSPIRE_SPATIAL_DATASET_ID = EidFactory.getDefault()
            .createAndPreserveStr("057d7919-d7b8-4d77-adb8-0d3118b3d220");

    private static final TestObjectTypeDto CITYGML20_CITY_MODEL_TOT = new TestObjectTypeDto();
    private static EID CITYGML20_CITY_MODEL_ID = EidFactory.getDefault()
            .createAndPreserveStr("3e3639b1-f6b7-4d62-9160-963cfb2ea300");

    private static final TestObjectTypeDto CITYGML10_CITY_MODEL_TOT = new TestObjectTypeDto();
    private static EID CITYGML10_CITY_MODEL_ID = EidFactory.getDefault()
            .createAndPreserveStr("d9371e42-2bf4-420c-84a5-4ab9055a8706");

    private static final TestObjectTypeDto METADATA_RECORDS_TOT = new TestObjectTypeDto();
    private static EID METADATA_RECORDS_ID = EidFactory.getDefault()
            .createAndPreserveStr("5a60dded-0cb0-4977-9b06-16c6c2321d2e");

    private static final TestObjectTypeDto SHAPE_TOT = new TestObjectTypeDto();
    private static EID SHAPE_TOT_ID = EidFactory.getDefault()
            .createAndPreserveStr("f91277ec-bbd9-49da-88ff-7b494f1f558d");

    private static final TestObjectTypeDto CSV_TOT = new TestObjectTypeDto();
    private static EID CSV_TOT_ID = EidFactory.getDefault()
            .createAndPreserveStr("213c68ef-c603-47b9-bf63-444e5dd92976");

    private static final TestObjectTypeDto HK_TOT = new TestObjectTypeDto();
    private static EID HK_TOT_ID = EidFactory.getDefault()
            .createAndPreserveStr("fcfc1daf-837b-41bc-953f-f8fb0acd783d");

    private static final TestObjectTypeDto HU_TOT = new TestObjectTypeDto();
    private static EID HU_TOT_ID = EidFactory.getDefault()
            .createAndPreserveStr("f2ac8656-6f12-4bf6-85e7-18dacb1452e1");

    private static final String owsLabelExpression = "/*/*[local-name() = 'ServiceIdentification' or local-name() = 'Service' ][1]/*[local-name() = 'Title'][1]/text()";
    private static final String owsDescriptionExpression = "(/*/*[local-name() = 'ServiceIdentification' or local-name() = 'Service'][1]/*[local-name() = 'Abstract'][1]/text())[1]";

    // Supported Test Object Types
    public final static EidMap<TestObjectTypeDto> types = new DefaultEidMap<>(
            Collections.unmodifiableMap(new LinkedHashMap<EID, TestObjectTypeDto>() {

                // default fallback if DocumentBuilder does not throw an exception and the URI starts with 'http'
                {
                    WEB_SERVICE_TOT.setLabel("Web service");
                    WEB_SERVICE_TOT.setId(WEB_SERVICE_ID);
                    WEB_SERVICE_TOT.setDescription("Any service with an interface using HTTP(S).");
                    put(WEB_SERVICE_ID, WEB_SERVICE_TOT);
                }
                {
                    OGC_API_TOT.setLabel("OGC API");
                    OGC_API_TOT.setId(OGC_API_ID);
                    OGC_API_TOT.setParent(WEB_SERVICE_TOT);
                    OGC_API_TOT.setDescription("A web service implementing the OGC API which defines modular "
                            + "API building blocks to spatially enable Web APIs in a consistent way.");
                    put(OGC_API_ID, OGC_API_TOT);
                }
                {
                    OGC_API_PROCESSES_1_TOT.setLabel("OGC API - Processes 1.0");
                    OGC_API_PROCESSES_1_TOT.setId(OGC_API_PROCESSES_1_ID);
                    OGC_API_PROCESSES_1_TOT.setParent(OGC_API_TOT);
                    OGC_API_PROCESSES_1_TOT.setDescription("A web service implementing the 'OGC API - Processes' which "
                            + "enables the execution of computing processes and the retrieval of metadata describing "
                            + "their purpose and functionality.");
                    OGC_API_PROCESSES_1_TOT.setDefaultPathAndQuery("conformance");
                    OGC_API_PROCESSES_1_TOT.setDetectionExpression(
                            "$.conformsTo[?(@ =~ /http://www.opengis.net/spec/ogcapi-processes.*/.*/i )]",
                            ExpressionType.JSONPATH);
                    OGC_API_PROCESSES_1_TOT.setLabelExpression("$.info.title", ExpressionType.JSONPATH);
                    OGC_API_PROCESSES_1_TOT.setDescriptionExpression("$.info.description", ExpressionType.JSONPATH);
                    OGC_API_PROCESSES_1_TOT.setUriDetectionExpression("\\/api");
                    OGC_API_PROCESSES_1_TOT.setMimeTypes(
                            Collections.singletonList("application/json"));
                    put(OGC_API_PROCESSES_1_ID, OGC_API_PROCESSES_1_TOT);
                }
                {
                    OGC_API_FEATURES_1_TOT.setLabel("OGC API - Features 1 - 1.0");
                    OGC_API_FEATURES_1_TOT.setId(OGC_API_FEATURES_1_ID);
                    OGC_API_FEATURES_1_TOT.setParent(OGC_API_TOT);
                    OGC_API_FEATURES_1_TOT.setDescription("A web service implementing the 'OGC API - Features' which "
                            + " is a multi-part standard for querying geospatial information on the web");
                    OGC_API_FEATURES_1_TOT.setDefaultPathAndQuery("conformance");
                    // deactivated for TestBed 17
                    OGC_API_FEATURES_1_TOT.setDetectionExpression(
                            "$.conformsTo[?(@ =~ /http://www.opengis.net/spec/ogcapi---features-1/1.0.*/i )]",
                            ExpressionType.JSONPATH);
                    // OGC_API_FEATURES_1_TOT.setDetectionExpression("$.conformsTo[?(@ =~
                    // /http://www.opengis.net/spec/ogcapi-features-1/1.0.*/i )]", ExpressionType.JSONPATH);
                    OGC_API_FEATURES_1_TOT.setLabelExpression("$.info.title", ExpressionType.JSONPATH);
                    OGC_API_FEATURES_1_TOT.setDescriptionExpression("$.info.description", ExpressionType.JSONPATH);
                    OGC_API_FEATURES_1_TOT.setUriDetectionExpression("\\/api");
                    OGC_API_FEATURES_1_TOT.setMimeTypes(
                            Collections.singletonList("application/json"));
                    put(OGC_API_FEATURES_1_ID, OGC_API_FEATURES_1_TOT);
                }
                {
                    WFS_TOT.setLabel("OGC Web Feature Service");
                    WFS_TOT.setId(WFS_ID);
                    WFS_TOT.setParent(WEB_SERVICE_TOT);
                    WFS_TOT.setDescription("A web service implementing the OGC Web Feature Service standard.");
                    WFS_TOT.setUriDetectionExpression("\\/wfs\\?|service=wfs");
                    put(WFS_ID, WFS_TOT);
                }
                {
                    WFS_2_0_TOT.setLabel("OGC Web Feature Service 2.0");
                    WFS_2_0_TOT.setId(WFS_2_0_ID);
                    WFS_2_0_TOT.setParent(WFS_TOT);
                    WFS_2_0_TOT.setDescription(
                            "A web service implementing OGC Web Feature Service 2.0 and OGC Filter Encoding 2.0.");
                    WFS_2_0_TOT.setDetectionExpression("boolean(/*[local-name() = 'WFS_Capabilities' and "
                            + "namespace-uri() = 'http://www.opengis.net/wfs/2.0'])", ExpressionType.XPATH);
                    WFS_2_0_TOT.setLabelExpression(owsLabelExpression, ExpressionType.XPATH);
                    WFS_2_0_TOT.setDescriptionExpression(owsDescriptionExpression, ExpressionType.XPATH);
                    // The GetCapabilities request includes both the "AcceptVersions" and the "version" parameters.
                    // Strictly, for WFS 2.0 / OWS Common 1.1, only the "AcceptVersions" parameter is specified.
                    // However, for backward compatibility, the "version" parameter, which has been used in earlier
                    // versions of the WFS standard, is still included as a deprecated parameter in OWS Common 1.1.
                    // OWS Common 1.1.0 states: "A server may also optionally implement the old-style version negotiation
                    // mechanism so that old clients that send GetCapabilities requests containing a 'version' parameter
                    // can be served." To cover both the old and the newer version negotiation approach, the request
                    // includes both parameters.
                    WFS_2_0_TOT
                            .setDefaultPathAndQuery("?request=GetCapabilities&service=WFS&AcceptVersions=2.0.0&version=2.0.0");
                    WFS_2_0_TOT.setUriDetectionExpression("(service=wfs.*(version=2\\.0\\.|acceptversions=2\\.0\\.))|"
                            + "((version=2\\.0\\.|acceptversions=2\\.0\\.).*service=wfs)");
                    put(WFS_2_0_ID, WFS_2_0_TOT);
                }
                {
                    WFS_1_1_TOT.setLabel("OGC Web Feature Service 1.1");
                    WFS_1_1_TOT.setId(WFS_1_1_ID);
                    WFS_1_1_TOT.setParent(WFS_TOT);
                    WFS_1_1_TOT.setDescription(
                            "A web service implementing OGC Web Feature Service 1.1 and OGC Filter Encoding 1.1.");
                    WFS_1_1_TOT.setDetectionExpression("boolean(/*[local-name() = 'WFS_Capabilities' and "
                            + "namespace-uri() = 'http://www.opengis.net/wfs' and starts-with(@version, '1.1') ])",
                            ExpressionType.XPATH);
                    WFS_1_1_TOT.setLabelExpression(owsLabelExpression, ExpressionType.XPATH);
                    WFS_1_1_TOT.setDescriptionExpression(owsDescriptionExpression, ExpressionType.XPATH);
                    WFS_1_1_TOT.setDefaultPathAndQuery("?request=GetCapabilities&service=WFS&version=1.1.0");
                    WFS_1_1_TOT.setUriDetectionExpression("(service=wfs.*version=1\\.1\\.)|(version=1\\.1\\..*service=wfs)");
                    put(WFS_1_1_ID, WFS_1_1_TOT);
                }
                {
                    WFS_1_0_TOT.setLabel("OGC Web Feature Service 1.0");
                    WFS_1_0_TOT.setId(WFS_1_0_ID);
                    WFS_1_0_TOT.setParent(WFS_TOT);
                    WFS_1_0_TOT.setDescription(
                            "A web service implementing OGC Web Feature Service 1.0 and OGC Filter Encoding 1.0.");
                    WFS_1_0_TOT.setDetectionExpression("boolean(/*[local-name() = 'WFS_Capabilities' and "
                            + "namespace-uri() = 'http://www.opengis.net/wfs' and @version='1.0.0'])", ExpressionType.XPATH);
                    WFS_1_0_TOT.setLabelExpression(owsLabelExpression, ExpressionType.XPATH);
                    WFS_1_0_TOT.setDescriptionExpression(owsDescriptionExpression, ExpressionType.XPATH);
                    WFS_1_0_TOT.setDefaultPathAndQuery("?request=GetCapabilities&service=WFS&version=1.0.0");
                    WFS_1_0_TOT.setUriDetectionExpression("(service=wfs.*version=1\\.0\\.)|(version=1\\.0\\..*service=wfs)");
                    put(WFS_1_0_ID, WFS_1_0_TOT);
                }
                {
                    WMS_TOT.setLabel("OGC Web Map Service");
                    WMS_TOT.setId(WMS_ID);
                    WMS_TOT.setParent(WEB_SERVICE_TOT);
                    WMS_TOT.setDescription("A web service implementing the OGC Web Map Service standard.");
                    WMS_TOT.setLabelExpression(owsLabelExpression, ExpressionType.XPATH);
                    WMS_TOT.setDescriptionExpression(owsDescriptionExpression, ExpressionType.XPATH);
                    WFS_TOT.setUriDetectionExpression("\\/wms\\?|service=wms");
                    put(WMS_ID, WMS_TOT);
                }
                {
                    WMS_1_3_TOT.setLabel("OGC Web Map Service 1.3");
                    WMS_1_3_TOT.setId(WMS_1_3_ID);
                    WMS_1_3_TOT.setParent(WMS_TOT);
                    WMS_1_3_TOT.setDescription("A web service implementing OGC Web Map Service 1.3.");
                    WMS_1_3_TOT.setDetectionExpression("boolean(/*[local-name() = 'WMS_Capabilities' and "
                            + "namespace-uri() = 'http://www.opengis.net/wms' and @version = '1.3.0'])", ExpressionType.XPATH);
                    WMS_1_3_TOT.setLabelExpression(owsLabelExpression, ExpressionType.XPATH);
                    WMS_1_3_TOT.setDescriptionExpression(owsDescriptionExpression, ExpressionType.XPATH);
                    WMS_1_3_TOT.setDefaultPathAndQuery("?request=GetCapabilities&service=WMS&version=1.3.0");
                    WMS_1_3_TOT.setUriDetectionExpression("(service=wms.*version=1\\.3\\.)|(version=1\\.3\\..*service=wms)");
                    put(WMS_1_3_ID, WMS_1_3_TOT);
                }
                {
                    WMS_1_1_TOT.setLabel("OGC Web Map Service 1.1");
                    WMS_1_1_TOT.setId(WMS_1_1_ID);
                    WMS_1_1_TOT.setParent(WMS_TOT);
                    WMS_1_1_TOT.setDescription("A web service implementing OGC Web Map Service 1.1.");
                    WMS_1_1_TOT.setDetectionExpression("boolean(/*[local-name() = 'WMT_MS_Capabilities' and "
                            + "@version = '1.1.1'])", ExpressionType.XPATH);
                    WMS_1_1_TOT.setLabelExpression(owsLabelExpression, ExpressionType.XPATH);
                    WMS_1_1_TOT.setDescriptionExpression(owsDescriptionExpression, ExpressionType.XPATH);
                    WMS_1_1_TOT.setDefaultPathAndQuery("?request=GetCapabilities&service=WMS&version=1.1.0");
                    WMS_1_1_TOT.setUriDetectionExpression("(service=wms.*version=1\\.1\\.)|(version=1\\.1\\..*service=wms)");
                    put(WMS_1_1_ID, WMS_1_1_TOT);
                }
                {
                    WMTS_TOT.setLabel("OGC Web Map Tile Service");
                    WMTS_TOT.setId(WMTS_ID);
                    WMTS_TOT.setParent(WEB_SERVICE_TOT);
                    WMTS_TOT.setDescription("A web service implementing the OGC Web Map Tile Service standard.");
                    put(WMTS_ID, WMTS_TOT);
                }
                {
                    WMTS_1_0_TOT.setLabel("OGC Web Map Tile Service 1.0");
                    WMTS_1_0_TOT.setId(WMTS_1_0_ID);
                    WMTS_1_0_TOT.setParent(WMTS_TOT);
                    WMTS_1_0_TOT.setDescription("A web service implementing OGC Web Map Tile Service 1.0.");
                    WMTS_1_0_TOT.setDetectionExpression("boolean(/*[local-name() = 'Capabilities' and "
                            + "namespace-uri() = 'http://www.opengis.net/wmts/1.0'])", ExpressionType.XPATH);
                    WMTS_1_0_TOT.setLabelExpression(owsLabelExpression, ExpressionType.XPATH);
                    WMTS_1_0_TOT.setDescriptionExpression(owsDescriptionExpression, ExpressionType.XPATH);
                    put(WMTS_1_0_ID, WMTS_1_0_TOT);
                }
                {
                    WCS_TOT.setLabel("OGC Web Coverage Service");
                    WCS_TOT.setId(WCS_ID);
                    WCS_TOT.setParent(WEB_SERVICE_TOT);
                    WCS_TOT.setDescription("A web service implementing the OGC Web Coverage Service standard.");
                    put(WCS_ID, WCS_TOT);
                }
                {
                    WCS_2_0_TOT.setLabel("OGC Web Coverage Service 2.0");
                    WCS_2_0_TOT.setId(WCS_2_0_ID);
                    WCS_2_0_TOT.setParent(WCS_TOT);
                    WCS_2_0_TOT.setDescription("A web service implementing OGC Web Coverage Service 2.0.");
                    WCS_2_0_TOT.setDetectionExpression("boolean(/*[local-name() = 'Capabilities' and "
                            + "namespace-uri() = 'http://www.opengis.net/wcs/2.0'])", ExpressionType.XPATH);
                    WCS_2_0_TOT.setLabelExpression(owsLabelExpression, ExpressionType.XPATH);
                    WCS_2_0_TOT.setDescriptionExpression(owsDescriptionExpression, ExpressionType.XPATH);
                    put(WCS_2_0_ID, WCS_2_0_TOT);
                }
                {
                    WCS_1_1_TOT.setLabel("OGC Web Coverage Service 1.1");
                    WCS_1_1_TOT.setId(WCS_1_1_ID);
                    WCS_1_1_TOT.setParent(WCS_TOT);
                    WCS_1_1_TOT.setDescription("A web service implementing OGC Web Coverage Service 1.1.");
                    WCS_1_1_TOT.setDetectionExpression("boolean(/*[local-name() = 'Capabilities' and "
                            + "namespace-uri() = 'http://www.opengis.net/wcs/1.1'])", ExpressionType.XPATH);
                    WCS_1_1_TOT.setLabelExpression(owsLabelExpression, ExpressionType.XPATH);
                    WCS_1_1_TOT.setDescriptionExpression(owsDescriptionExpression, ExpressionType.XPATH);
                    put(WCS_1_1_ID, WCS_1_1_TOT);
                }
                {
                    WCS_1_0_TOT.setLabel("OGC Web Coverage Service 1.0");
                    WCS_1_0_TOT.setId(WCS_1_0_ID);
                    WCS_1_0_TOT.setParent(WCS_TOT);
                    WCS_1_0_TOT.setDescription("A web service implementing OGC Web Coverage Service 1.0.");
                    WCS_1_0_TOT.setDetectionExpression("boolean(/*[local-name() = 'WCS_Capabilities' and "
                            + "namespace-uri() = 'http://www.opengis.net/wcs'])", ExpressionType.XPATH);
                    WCS_1_0_TOT.setLabelExpression(owsLabelExpression, ExpressionType.XPATH);
                    WCS_1_0_TOT.setDescriptionExpression(owsDescriptionExpression, ExpressionType.XPATH);
                    put(WCS_1_0_ID, WCS_1_0_TOT);
                }
                {
                    SOS_TOT.setLabel("OGC Sensor Observation Service");
                    SOS_TOT.setId(SOS_ID);
                    SOS_TOT.setParent(WEB_SERVICE_TOT);
                    SOS_TOT.setDescription("A web service implementing the OGC Sensor Observation Service standard.");
                    put(SOS_ID, SOS_TOT);
                }
                {
                    SOS_2_0_TOT.setLabel("OGC Sensor Observation Service 2.0");
                    SOS_2_0_TOT.setId(SOS_2_0_ID);
                    SOS_2_0_TOT.setParent(SOS_TOT);
                    SOS_2_0_TOT.setDescription("A web service implementing OGC Sensor Observation Service 2.0.");
                    SOS_2_0_TOT.setDetectionExpression("boolean(/*[local-name() = 'Capabilities' and "
                            + "namespace-uri() = 'http://www.opengis.net/sos/2.0'])", ExpressionType.XPATH);
                    SOS_2_0_TOT.setLabelExpression(owsLabelExpression, ExpressionType.XPATH);
                    SOS_2_0_TOT.setDescriptionExpression(owsDescriptionExpression, ExpressionType.XPATH);
                    put(SOS_2_0_ID, SOS_2_0_TOT);
                }
                {
                    CSW_TOT.setLabel("OGC Catalogue Service");
                    CSW_TOT.setId(CSW_ID);
                    CSW_TOT.setParent(WEB_SERVICE_TOT);
                    CSW_TOT.setDescription("A web service implementing the OGC Catalogue Service standard.");
                    put(CSW_ID, CSW_TOT);
                }
                {
                    CSW_3_0_TOT.setLabel("OGC Catalogue Service 3.0");
                    CSW_3_0_TOT.setId(CSW_3_0_ID);
                    CSW_3_0_TOT.setParent(CSW_TOT);
                    CSW_3_0_TOT.setDescription("A web service implementing OGC Catalogue Service 3.0");
                    CSW_3_0_TOT.setDetectionExpression("boolean(/*[local-name() = 'Capabilities' and "
                            + "namespace-uri() = 'http://www.opengis.net/cat/csw/3.0'])", ExpressionType.XPATH);
                    CSW_3_0_TOT.setLabelExpression(owsLabelExpression, ExpressionType.XPATH);
                    CSW_3_0_TOT.setDescriptionExpression(owsDescriptionExpression, ExpressionType.XPATH);
                    put(CSW_3_0_ID, CSW_3_0_TOT);
                }
                {
                    CSW_2_0_2_TOT.setLabel("OGC Catalogue Service 2.0.2");
                    CSW_2_0_2_TOT.setId(CSW_2_0_2_ID);
                    CSW_2_0_2_TOT.setParent(CSW_TOT);
                    CSW_2_0_2_TOT.setDescription("A web service implementing OGC Catalogue Service 2.0.2.");
                    CSW_2_0_2_TOT.setDetectionExpression("boolean(/*[local-name() = 'Capabilities' and "
                            + "namespace-uri() = 'http://www.opengis.net/cat/csw/2.0.2'])", ExpressionType.XPATH);
                    CSW_2_0_2_TOT.setLabelExpression(owsLabelExpression, ExpressionType.XPATH);
                    CSW_2_0_2_TOT.setDescriptionExpression(owsDescriptionExpression, ExpressionType.XPATH);
                    put(CSW_2_0_2_ID, CSW_2_0_2_TOT);
                }
                {
                    CSW_2_0_2_EBRIM_1_0_TOT.setLabel("OGC CSW-ebRIM Registry Service 1.0");
                    CSW_2_0_2_EBRIM_1_0_TOT.setId(CSW_2_0_2_EBRIM_1_0_ID);
                    CSW_2_0_2_EBRIM_1_0_TOT.setParent(CSW_TOT);
                    CSW_2_0_2_EBRIM_1_0_TOT.setDescription("A web service implementing the CSW-ebRIM Registry Service 1.0");
                    CSW_2_0_2_EBRIM_1_0_TOT.setDetectionExpression("boolean(/*[local-name() = 'Capabilities' and "
                            + "namespace-uri() = 'http://www.opengis.net/cat/wrs/1.0'])", ExpressionType.XPATH);
                    CSW_2_0_2_EBRIM_1_0_TOT.setLabelExpression(owsLabelExpression, ExpressionType.XPATH);
                    CSW_2_0_2_EBRIM_1_0_TOT.setDescriptionExpression(owsDescriptionExpression, ExpressionType.XPATH);
                    put(CSW_2_0_2_EBRIM_1_0_ID, CSW_2_0_2_EBRIM_1_0_TOT);
                }
                {
                    ATOM_TOT.setLabel("Atom feed");
                    ATOM_TOT.setId(ATOM_ID);
                    ATOM_TOT.setParent(WEB_SERVICE_TOT);
                    ATOM_TOT.setDescription(
                            "A feed implementing the Atom Syndication Format that can be accessed using HTTP(S).");
                    ATOM_TOT.setDetectionExpression(
                            "boolean(/*[local-name() = 'feed' and namespace-uri() = 'http://www.w3.org/2005/Atom'])",
                            ExpressionType.XPATH);
                    ATOM_TOT.setLabelExpression("/*[local-name() = 'feed' and namespace-uri() = 'http://www.w3.org/2005/Atom']"
                            + "/*[local-name() = 'title' and namespace-uri() = 'http://www.w3.org/2005/Atom']",
                            ExpressionType.XPATH);
                    ATOM_TOT.setDescriptionExpression(
                            "/*[local-name() = 'feed' and namespace-uri() = 'http://www.w3.org/2005/Atom']"
                                    + "/*[local-name() = 'subtitle' and namespace-uri() = 'http://www.w3.org/2005/Atom']",
                            ExpressionType.XPATH);
                    put(ATOM_ID, ATOM_TOT);
                }
                // not used yet
                {
                    DOCUMENTS_TOT.setLabel("Set of documents");
                    DOCUMENTS_TOT.setId(DOCUMENTS_ID);
                    DOCUMENTS_TOT.setDescription("A set of documents.");
                    XML_DOCUMENTS_TOT.filenameFilter(
                            Pattern.compile("(?!(\\.DS_Store|\\.git.*|\\.localized|desktop.ini|Thumbs.db|__MACOSX)).*"));
                    put(DOCUMENTS_ID, DOCUMENTS_TOT);
                }
                // default fallback if DocumentBuilder does not throw an exception and the URI starts with 'file'
                {
                    XML_DOCUMENTS_TOT.setLabel("Set of XML documents");
                    XML_DOCUMENTS_TOT.setId(XML_DOCUMENTS_ID);
                    XML_DOCUMENTS_TOT.setParent(DOCUMENTS_TOT);
                    XML_DOCUMENTS_TOT.setDescription("A set of XML documents.");
                    XML_DOCUMENTS_TOT.setMimeTypes(new ArrayList<String>() {
                        {
                            add("application/xml");
                            add("text/xml");
                        }
                    });
                    XML_DOCUMENTS_TOT.filenameFilter(IFile.getRegexForExtension("xml"));
                    put(XML_DOCUMENTS_ID, XML_DOCUMENTS_TOT);
                }
                {
                    GML_FEATURE_COLLECTION_TOT.setLabel("GML feature collections");
                    GML_FEATURE_COLLECTION_TOT.setId(GML_FEATURE_COLLECTION_ID);
                    GML_FEATURE_COLLECTION_TOT.setParent(XML_DOCUMENTS_TOT);
                    GML_FEATURE_COLLECTION_TOT
                            .setDescription("A set of XML documents. Each document contains a GML feature collection.");
                    GML_FEATURE_COLLECTION_TOT.filenameFilter(Pattern.compile("(.*[^\\s]+(\\.(?i)(gml|xml))$)"));
                    GML_FEATURE_COLLECTION_TOT.setMimeTypes(new ArrayList<String>() {
                        {
                            add("application/xml");
                            add("text/xml");
                            add("application/gml+xml");
                        }
                    });
                    GML_FEATURE_COLLECTION_TOT.setDetectionExpression("boolean(/*[local-name() = 'FeatureCollection'])",
                            ExpressionType.XPATH);
                    put(GML_FEATURE_COLLECTION_ID, GML_FEATURE_COLLECTION_TOT);
                }
                {
                    WFS20_FEATURE_COLLECTION_TOT.setLabel("WFS 2.0 feature collections");
                    WFS20_FEATURE_COLLECTION_TOT.setId(WFS20_FEATURE_COLLECTION_ID);
                    WFS20_FEATURE_COLLECTION_TOT.setParent(GML_FEATURE_COLLECTION_TOT);
                    WFS20_FEATURE_COLLECTION_TOT
                            .setDescription("A set of XML documents. Each document contains a WFS 2.0 feature collection.");
                    WFS20_FEATURE_COLLECTION_TOT.setDetectionExpression("boolean(/*[local-name() = 'FeatureCollection' and "
                            + "namespace-uri() = 'http://www.opengis.net/wfs/2.0'])", ExpressionType.XPATH);
                    put(WFS20_FEATURE_COLLECTION_ID, WFS20_FEATURE_COLLECTION_TOT);
                }
                {
                    GML32_FEATURE_COLLECTION_TOT.setLabel("GML 3.2 feature collections");
                    GML32_FEATURE_COLLECTION_TOT.setId(GML32_FEATURE_COLLECTION_ID);
                    GML32_FEATURE_COLLECTION_TOT.setParent(GML_FEATURE_COLLECTION_TOT);
                    GML32_FEATURE_COLLECTION_TOT
                            .setDescription("A set of XML documents. Each document contains a GML 3.2 feature collection.");
                    GML32_FEATURE_COLLECTION_TOT.setDetectionExpression("boolean(/*[local-name() = 'FeatureCollection' and "
                            + "namespace-uri() = 'http://www.opengis.net/gml/3.2'])", ExpressionType.XPATH);
                    put(GML32_FEATURE_COLLECTION_ID, GML32_FEATURE_COLLECTION_TOT);
                }
                {
                    GML31_GML21_FEATURE_COLLECTION_TOT.setLabel("GML 2.1/GML 3.1 feature collections");
                    GML31_GML21_FEATURE_COLLECTION_TOT.setId(GML31_GML21_FEATURE_COLLECTION_ID);
                    GML31_GML21_FEATURE_COLLECTION_TOT.setParent(GML_FEATURE_COLLECTION_TOT);
                    GML31_GML21_FEATURE_COLLECTION_TOT.setDescription(
                            "A set of XML documents. Each document contains a GML 2.1 or GML 3.1 feature collection.");
                    GML31_GML21_FEATURE_COLLECTION_TOT
                            .setDetectionExpression("boolean(/*[local-name() = 'FeatureCollection' and "
                                    + "namespace-uri() = 'http://www.opengis.net/gml'])", ExpressionType.XPATH);
                    put(GML31_GML21_FEATURE_COLLECTION_ID, GML31_GML21_FEATURE_COLLECTION_TOT);
                }
                {
                    INSPIRE_SPATIAL_DATASET_TOT.setLabel("INSPIRE SpatialDataSet documents");
                    INSPIRE_SPATIAL_DATASET_TOT.setId(INSPIRE_SPATIAL_DATASET_ID);
                    INSPIRE_SPATIAL_DATASET_TOT.setParent(GML_FEATURE_COLLECTION_TOT);
                    INSPIRE_SPATIAL_DATASET_TOT
                            .setDescription("A set of XML documents. Each document contains an INSPIRE SpatialDataSet.");
                    INSPIRE_SPATIAL_DATASET_TOT.setDetectionExpression("boolean(/*[local-name() = 'SpatialDataSet' and "
                            + "starts-with(namespace-uri(), 'http://inspire.ec.europa.eu/schemas/base/')])",
                            ExpressionType.XPATH);
                    put(INSPIRE_SPATIAL_DATASET_ID, INSPIRE_SPATIAL_DATASET_TOT);
                }
                {
                    CITYGML20_CITY_MODEL_TOT.setLabel("CityGML 2.0 CityModel");
                    CITYGML20_CITY_MODEL_TOT.setId(CITYGML20_CITY_MODEL_ID);
                    CITYGML20_CITY_MODEL_TOT.setParent(GML_FEATURE_COLLECTION_TOT);
                    CITYGML20_CITY_MODEL_TOT
                            .setDescription("A set of XML documents. Each document contains a CityGML 2.0 CityModel.");
                    CITYGML20_CITY_MODEL_TOT.setDetectionExpression("boolean(/*[local-name() = 'CityModel' and "
                            + "namespace-uri() = 'http://www.opengis.net/citygml/2.0'])", ExpressionType.XPATH);
                    put(CITYGML20_CITY_MODEL_ID, CITYGML20_CITY_MODEL_TOT);
                }
                {
                    CITYGML10_CITY_MODEL_TOT.setLabel("CityGML 1.0 CityModel");
                    CITYGML10_CITY_MODEL_TOT.setId(CITYGML10_CITY_MODEL_ID);
                    CITYGML10_CITY_MODEL_TOT.setParent(GML_FEATURE_COLLECTION_TOT);
                    CITYGML10_CITY_MODEL_TOT
                            .setDescription("A set of XML documents. Each document contains a CityGML 1.0 CityModel.");
                    CITYGML10_CITY_MODEL_TOT.setDetectionExpression("boolean(/*[local-name() = 'CityModel' and "
                            + "namespace-uri() = 'http://www.opengis.net/citygml/1.0'])", ExpressionType.XPATH);
                    put(CITYGML10_CITY_MODEL_ID, CITYGML10_CITY_MODEL_TOT);
                }
                {
                    METADATA_RECORDS_TOT.setLabel("Metadata records");
                    METADATA_RECORDS_TOT.setId(METADATA_RECORDS_ID);
                    METADATA_RECORDS_TOT.setParent(XML_DOCUMENTS_TOT);
                    METADATA_RECORDS_TOT.setDescription(
                            "A set of XML documents. Each document contains one or more gmd:MD_Metadata elements.");
                    METADATA_RECORDS_TOT.setDetectionExpression(
                            "boolean(/*["
                                    + "(local-name() = 'GetRecordsResponse' and starts-with(namespace-uri(), 'http://www.opengis.net/cat/csw/')) or "
                                    + "(local-name() = 'GetRecordByIdResponse' and starts-with(namespace-uri(), 'http://www.opengis.net/cat/csw/')) or "
                                    + "(local-name() = 'MD_Metadata' and namespace-uri() = 'http://www.isotc211.org/2005/gmd')"
                                    + "])",
                            ExpressionType.XPATH);
                    put(METADATA_RECORDS_ID, METADATA_RECORDS_TOT);
                }
                {
                    SHAPE_TOT.setLabel("Shapefile feature collections");
                    SHAPE_TOT.setId(SHAPE_TOT_ID);
                    SHAPE_TOT.setParent(DOCUMENTS_TOT);
                    SHAPE_TOT.setDescription(
                            "The Shapefile format is a vector data format for storing geometric location and associated attribute information. "
                                    + "The format consists of a collection of at least three files with a common filename prefix.");
                    SHAPE_TOT.filenameFilter(Pattern.compile("(.*[^\\s]+(\\.(?i)(shp|dbf|shx))$)"));
                    SHAPE_TOT.setMimeTypes(new ArrayList<String>() {
                        {
                            add("application/x-shapefile");
                            add("application/x-dbf");
                        }
                    });
                    put(SHAPE_TOT_ID, SHAPE_TOT);
                }
                {
                    CSV_TOT.setLabel("CSV feature collections");
                    CSV_TOT.setId(CSV_TOT_ID);
                    CSV_TOT.setParent(DOCUMENTS_TOT);
                    CSV_TOT.setDescription(
                            "A file with comma-separated values for storing geometric location and associated attribute information.");
                    CSV_TOT.filenameFilter(Pattern.compile("(.*[^\\s]+(\\.(?i)(csv))$)"));
                    CSV_TOT.setMimeTypes(Collections.singletonList("text/csv"));
                    put(CSV_TOT_ID, CSV_TOT);
                }
                {
                    HK_TOT.setLabel("Hauskoordinaten");
                    HK_TOT.setId(HK_TOT_ID);
                    HK_TOT.setParent(CSV_TOT);
                    HK_TOT.setDescription(
                            "Drei bis vier CSV-Dateien, mit Bundeslandkürzel und txt-Endung");
                    HK_TOT.filenameFilter(Pattern.compile(
                            "(?i)^(adressen|info|schluessel|umschluessel)-(bb|be|bw|by|hb|he|hh|mv|ni|nw|rp|sh|sl|sn|st|th)\\.(txt|csv)$"));
                    HK_TOT.setMimeTypes(new ArrayList<String>() {
                        {
                            add("text/plain");
                            add("text/csv");
                        }
                    });
                    put(HK_TOT_ID, HK_TOT);
                }
                {
                    HU_TOT.setLabel("Hausumringe");
                    HU_TOT.setId(HU_TOT_ID);
                    HU_TOT.setParent(SHAPE_TOT);
                    HU_TOT.setDescription(
                            "Drei Shapedatein und eine Infodatei mit Bundeslandkürzel");
                    HU_TOT.filenameFilter(Pattern.compile(
                            "(?i)^(info-(bb|be|bw|by|hb|he|hh|mv|ni|nw|rp|sh|sl|sn|st|th)\\.txt)|(gebaeude-(bb|be|bw|by|hb|he|hh|mv|ni|nw|rp|sh|sl|sn|st|th)\\.(shp|dbf|shx))$"));
                    HU_TOT.setMimeTypes(new ArrayList<String>() {
                        {
                            add("application/x-shapefile");
                            add("application/x-dbf");
                            add("text/plain");
                        }
                    });
                    put(HU_TOT_ID, HU_TOT);
                }
            }));
}
