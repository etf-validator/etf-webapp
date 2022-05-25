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
package de.interactive_instruments.etf.webapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.*;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import de.interactive_instruments.etf.webapp.controller.EtfConfig;
import de.interactive_instruments.etf.webapp.conversion.ObjectMapperFactory;
import io.swagger.annotations.ApiOperation;

/**
 * URLs: - http://localhost:8080/swagger-ui.html - http://localhost:8080/v2/api-docs
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
@EnableSwagger2
@Configuration
public class SwaggerConfig {

    @Autowired
    private ObjectMapperFactory objectMapperFactory;

    public final static String STATUS_TAG_NAME = "1. Service Status";
    private final static Tag statusTag = new Tag(STATUS_TAG_NAME, "Monitor service workload and health");

    public final static String SERVICE_CAP_TAG_NAME = "2. Service Capabilities";
    private final static Tag capabilitiesTag = new Tag(SERVICE_CAP_TAG_NAME, "Retrieve test framework metadata ");

    public final static String TEST_OBJECTS_TAG_NAME = "3. Manage Test Objects";
    private final static Tag testObjectsTag = new Tag(TEST_OBJECTS_TAG_NAME, "Define Test Objects and upload test data");

    public final static String TEST_RUNS_TAG_NAME = "4. Manage Test Runs";
    private final static Tag testRunsTag = new Tag(TEST_RUNS_TAG_NAME, "Start and control test runs");

    public final static String TEST_RESULTS_TAG_NAME = "5. Test Run Results";
    private final static Tag testResultsTag = new Tag(TEST_RESULTS_TAG_NAME, "Retrieve test results");

    private ApiInfo v2Info() {
        return new ApiInfoBuilder()
                .title("ETF Web API")
                .description(
                        "This is an interactive documentation and a web user interface for interacting with the Web API version 2 of the test framework "
                                + "[ETF](http://etf-validator.net). "
                                + "This semi-automatic generated documentation covers basic functionality, but consulting the [API Documentation](http://docs.etf-validator.net/v2.0/Developer_manuals/WEB-API.html) "
                                + "may be required to get a deeper understanding of the ETF model and further procedures. "
                                + "Issues can be reported in [GitHub]"
                                + "(https://github.com/etf-validator/etf-webapp/issues/new?title=[webapi-v2]%20&body=Please%20don%27t%20delete%20the%20[webapi-v2]"
                                + "%20text%20in%20the%20title.%20This%20text%20can%20be%20deleted.&labels=module:%20Web%20UI%20/%20controller%20layer). "
                                + "\n\n"
                                + "Content negotiation is not supported and therefore JSON is always returned for endpoints without file extension. "
                                + "For most operations, a link to the XML response schema is provided in the implementation nodes."
                                + "JSON responses are derived from XML the response schema, based on this [stylesheet](https://github.com/bramstein/xsltjson#basic-output-default)."
                                + "\n\n"
                                + "[Back to user interface]("
                                + EtfConfig.getInstance().getProperty(EtfConfig.ETF_WEBAPP_BASE_URL)
                                + "/#home)")
                .contact(new Contact("ETF", "https://www.etf-validator.net",
                        "etf@interactive-instruments.de"))
                .license("European Public License 1.2")
                .licenseUrl("https://joinup.ec.europa.eu/page/eupl-text-11-12")
                .version("2.1.0")
                .build();
    }

    @Bean
    public Docket v2Api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(v2Info())
                .select()
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .paths(PathSelectors.any())
                .build()
                .pathMapping("/")
                .tags(statusTag, capabilitiesTag, testRunsTag, testObjectsTag, testResultsTag)
                .useDefaultResponseMessages(false).groupName("ETF-v2");
    }

    @Bean
    UiConfiguration uiConfig() {
        return UiConfigurationBuilder.builder()
                .deepLinking(true)
                .displayOperationId(false)
                .defaultModelsExpandDepth(-1)
                .defaultModelExpandDepth(1)
                .defaultModelRendering(ModelRendering.EXAMPLE)
                .displayRequestDuration(false)
                .docExpansion(DocExpansion.NONE)
                .filter(false)
                .maxDisplayedTags(5)
                .operationsSorter(OperationsSorter.ALPHA)
                .showExtensions(false)
                .tagsSorter(TagsSorter.ALPHA)
                .supportedSubmitMethods(UiConfiguration.Constants.DEFAULT_SUBMIT_METHODS)
                .build();
    }
}
