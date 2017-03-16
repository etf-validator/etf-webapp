/**
 * Copyright 2010-2017 interactive instruments GmbH
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
