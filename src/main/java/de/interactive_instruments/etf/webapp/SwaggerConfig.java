/**
 * Copyright 2010-2016 interactive instruments GmbH
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
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import de.interactive_instruments.etf.webapp.conversion.ObjectMapperFactory;
import io.swagger.annotations.ApiOperation;

/**
 * URLs:
 * - http://localhost:8080/swagger-ui.html
 * - http://localhost:8080/v2/api-docs
 *
 * @author J. Herrmann ( herrmann <aT) interactive-instruments (doT> de )
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

	// http://localhost:8080/v2/api-docs

	private ApiInfo apiInfo() {
		return new ApiInfoBuilder()
				.title("ETF API")
				.description(
						"This is an interactive documentation and a web user interface for interacting with the Web API version 2 BETA of the test framework "
						+ "[ETF](https://interactive-instruments.github.io/etf-webapp). "
						+ "This semi-automatic generated documentation covers basic functionality, but consulting the [Wiki](https://github.com/interactive-instruments/etf-webapp/wiki/Web%20API) "
						+ "may be required to get a deeper understanding of the ETF model and further procedures. "
						+ "Issues can be reported in [GitHub]"
						+ "(https://github.com/interactive-instruments/etf-webapp/issues/new?title=[webapi-v2-beta]%20&body=Please%20don%27t%20delete%20the%20"
						+ "[webapi-v2-beta]%20text%20in%20the%20title.%20This%20text%20can%20be%20deleted.&labels=webapi). "
						+ "  "
						+ "Content negotiation is currently not implemented and therefore JSON is always returned for endpoints without file extension. "
						+ "Please note that the API is not final and may undergo further changes before being released. ")
				.contact(new Contact("ETF Team", "https://interactive-instruments.github.io/etf-webapp", "etf@interactive-instruments.de"))
				.license("Apache 2.0")
				.licenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html")
				.version("2.0.0-BETA")
				.build();
	}

	@Bean
	public Docket etfApi() {
		return new Docket(DocumentationType.SWAGGER_2)
				.apiInfo(apiInfo())
				.select()
				.apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
				.paths(PathSelectors.any())
				.build()
				.pathMapping("/")
				.tags(statusTag, capabilitiesTag, testRunsTag, testObjectsTag, testResultsTag)
				.useDefaultResponseMessages(false)
		//.directModelSubstitute(LocalDate.class, String.class)
		// .genericModelSubstitutes(ResponseEntity.class)
		// .useDefaultResponseMessages(false)
		;
	}
}
