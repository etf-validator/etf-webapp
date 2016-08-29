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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import io.swagger.annotations.ApiOperation;

/**
 * @author J. Herrmann ( herrmann <aT) interactive-instruments (doT> de )
 */
@EnableSwagger2
@Configuration
public class SwaggerConfig {

	// http://localhost:8080/v2/api-docs

	private ApiInfo apiInfo() {
		return new ApiInfoBuilder()
				.title("ETF API")
				.description("API for the test framework ETF")
				.contact(new Contact("ETF Team", "https://interactive-instruments.github.io/etf-webapp", "etf@interactive-instruments.de"))
				.license("Apache 2.0")
				.licenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html")
				.version("2.0.0-ALPHA")
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
		//.directModelSubstitute(LocalDate.class, String.class)
		// .genericModelSubstitutes(ResponseEntity.class)
		// .useDefaultResponseMessages(false)
		;
	}
}
