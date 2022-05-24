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
package de.interactive_instruments.etf.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import de.interactive_instruments.SUtils;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface Parameterizable {

    interface Parameter {

        String getName();

        String getDefaultValue();

        String getDescription();

        String getAllowedValues();

        String getType();

        boolean isRequired();

        boolean isStatic();

        Set<String> getExcludingParameters();

        default boolean validate(final String value) {
            if ((SUtils.isNullOrEmpty(getType()) || "string".equals(getType())) && !SUtils.isNullOrEmpty(getAllowedValues())) {
                return Pattern.compile(getAllowedValues()).matcher(value).matches();
            } else if ("choice".equals(getType())) {
                final Set<String> allowedValues = new HashSet<>(Arrays.asList(getAllowedValues().split("\\|")));
                return allowedValues.contains(value);
            } else if ("multichoice".equals(getType())) {
                final Set<String> allowedValues = new HashSet<>(Arrays.asList(getAllowedValues().split("\\|")));
                final Set<String> values = new HashSet<>(Arrays.asList(value.split("[,|]")));
                return values.stream().noneMatch(v -> !allowedValues.contains(v));
            } else if ("integer".equals(getType())) {
                try {
                    Integer.parseInt(value);
                } catch (NumberFormatException | NullPointerException e) {
                    return false;
                }
                return true;
            } else if ("double".equals(getType())) {
                try {
                    Double.parseDouble(value);
                } catch (NumberFormatException | NullPointerException e) {
                    return false;
                }
                return true;
            } else if ("boolean".equals(getType())) {
                return value.matches("true|false");
            }
            return true;
        }
    }

    String getParamTypeName();

    Collection<Parameter> getParameters();

    Parameter getParameter(String name);

}
