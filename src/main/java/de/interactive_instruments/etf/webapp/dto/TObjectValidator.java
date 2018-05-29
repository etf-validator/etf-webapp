/**
 * Copyright 2017-2018 European Union, interactive instruments GmbH
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This work was supported by the EU Interoperability Solutions for
 * European Public Administrations Programme (http://ec.europa.eu/isa)
 * through Action 1.17: A Reusable INSPIRE Reference Platform (ARE3NA).
 */
package de.interactive_instruments.etf.webapp.dto;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectDto;

public class TObjectValidator implements Validator {
	@Override
	public boolean supports(Class<?> clasz) {
		return TestObjectDto.class.isAssignableFrom(clasz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "label",
				"l.enter.label", "Please enter a label!");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "description",
				"l.enter.description", "Please enter a description!");

		final TestObjectDto to = (TestObjectDto) target;

		final String regex = to.properties().getProperty("regex");
		if (regex != null && !regex.isEmpty()) {

			try {
				Pattern.compile(regex);
			} catch (PatternSyntaxException e) {
				// Try to quote it
				final String quotedRegex = Pattern.quote(regex);
				try {
					Pattern.compile(regex);
					// Set the usable pattern
					to.properties().setProperty("regex", quotedRegex);
				} catch (PatternSyntaxException eQuoted) {
					// Throw the unquoted error message
					errors.reject("l.invalid.regex", new Object[]{e.getMessage()},
							"Der reguläre Ausdruck ist fehlerhaft: {}");
				}
			}
		}

	}

}
