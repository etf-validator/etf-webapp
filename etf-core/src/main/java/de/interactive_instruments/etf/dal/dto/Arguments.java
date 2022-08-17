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
package de.interactive_instruments.etf.dal.dto;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import de.interactive_instruments.SUtils;
import de.interactive_instruments.StringInterpolation;
import de.interactive_instruments.etf.model.Parameterizable;
import de.interactive_instruments.properties.PropertyHolder;

/**
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class Arguments {

    private String applicableParamTypeName;
    private Map<String, Argument> values = new HashMap<>();
    private static Pattern variablePattern = Pattern.compile("(\\$\\{[^${},;\\s]+\\})");

    public Arguments(final PropertyHolder holder) {
        holder.forEach(p -> values.put(p.getKey(), new Argument(p.getKey(), p.getValue())));
    }

    public Arguments() {}

    public Arguments(final Map<String, String> map) {
        map.entrySet().forEach(e -> values.put(e.getKey(), new Argument(e.getKey(), e.getValue())));
    }

    public static class Argument {
        private String name;
        private String value;

        public Argument() {}

        public Argument(final String name, final String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }

    public class ArgumentParameterSet {

        private final Parameterizable parameterizable;
        private final boolean applicable;

        ArgumentParameterSet(Parameterizable parameterizable) {
            this.parameterizable = parameterizable;
            applicable = applicableParamTypeName != null && applicableParamTypeName.equals(parameterizable.getParamTypeName());
            checkValidParams();
        }

        private void checkValidParams() {
            if (applicable) {
                // todo
            }
        }

        public String value(final String name) {
            if (applicable) {
                final Argument argumentValue = values.get(name);
                if ((argumentValue == null || argumentValue.getValue() == null) &&
                        parameterizable.getParameter(name) != null) {
                    parameterizable.getParameter(name).getDefaultValue();
                }
            }
            return values.get(name).getValue();
        }

        /**
         * Does not return any null values
         *
         * @return key value pairs
         */
        public Map<String, String> values() {
            if (applicable) {
                final Map<String, String> map = new LinkedHashMap<>();
                for (final Parameterizable.Parameter parameter : parameterizable.getParameters()) {
                    final String val = values.get(parameter.getName()).getValue();
                    if (val == null) {
                        if (parameter.getDefaultValue() != null) {
                            map.put(parameter.getName(), parameter.getDefaultValue());
                        }
                    } else {
                        map.put(parameter.getName(), val);
                    }
                }
                return map;
            } else {
                final Map<String, String> vals = new HashMap<>();
                values.entrySet().forEach(e -> vals.put(e.getKey(), e.getValue().getValue()));
                return vals;
            }
        }
    }

    public void setValue(final String name, final String value) {
        this.values.put(name, new Argument(name, value));
    }

    public ArgumentParameterSet argumentParameterSet(Parameterizable parameterizable) {
        return new ArgumentParameterSet(parameterizable);
    }

    public boolean isEmpty() {
        if (!values().isEmpty()) {
            return values.size() == 1 && SUtils.isNullOrEmpty(
                    values.values().iterator().next().getName());
        }
        return true;
    }

    private String resolveValue(final Argument argument) {
        if (argument == null) {
            return null;
        }
        return StringInterpolation.resolve(argument.getValue(), key -> {
            final Argument a = values.get(key);
            return a != null ? a.getValue() : null;
        });
    }

    public boolean containsName(final String name) {
        return this.values.containsKey(name);
    }

    // TODO tmp
    public String value(final String name) {
        final Argument argument = values.get(name);
        return resolveValue(argument);
    }

    // TODO tmp
    public Map<String, String> values() {
        final Map<String, String> vals = new HashMap<>();
        values.entrySet().forEach(a -> vals.put(a.getKey(), resolveValue(a.getValue())));
        return vals;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Arguments{");
        sb.append("applicableParamTypeName='").append(applicableParamTypeName).append('\'');
        sb.append(", values={");
        for (final Map.Entry<String, String> entry : values().entrySet()) {
            sb.append(entry.getKey());
            sb.append('=');
            sb.append(entry.getValue());
            sb.append(' ');
        }
        sb.append("}}");
        return sb.toString();
    }
}
