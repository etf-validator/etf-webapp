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

import java.util.*;

import de.interactive_instruments.SUtils;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public final class ParameterSet implements Parameterizable {

    private String typeName;
    private Map<String, Parameter> parameters;

    public static class ImmutableParameter implements Parameterizable.Parameter {
        private final String name;
        private final String defaultValue;
        private final String description;
        private final String allowedValues;
        private final String type;
        private final boolean required;
        private final Set<String> excludingParameters;

        private ImmutableParameter(final MutableParameter other) {
            this.name = other.getName();
            this.defaultValue = other.getDefaultValue();
            this.description = other.getDescription();
            this.allowedValues = other.getAllowedValues();
            this.type = other.getType();
            this.required = other.isRequired();
            this.excludingParameters = other.getExcludingParameters();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDefaultValue() {
            return defaultValue;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getAllowedValues() {
            return allowedValues;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public boolean isRequired() {
            return required;
        }

        @Override
        public boolean isStatic() {
            return true;
        }

        @Override
        public Set<String> getExcludingParameters() {
            return excludingParameters;
        }
    }

    public static class MutableParameter implements Parameterizable.Parameter {
        private String name;
        private String defaultValue;
        private String description;
        private String allowedValues;
        private String type;
        private boolean required;
        private boolean immutable;
        private String excludingParameters;

        public MutableParameter() {}

        public MutableParameter(final MutableParameter other) {
            this.name = other.name;
            this.defaultValue = other.defaultValue;
            this.description = other.description;
            this.allowedValues = other.allowedValues;
            this.type = other.type;
            this.required = other.required;
            this.excludingParameters = other.excludingParameters;
        }

        public MutableParameter(final Parameter other) {
            this.name = other.getName();
            this.defaultValue = other.getDefaultValue();
            this.description = other.getDescription();
            this.allowedValues = other.getAllowedValues();
            this.type = other.getType();
            this.required = other.isRequired();
            setExcludingParameters(other.getExcludingParameters());
        }

        public MutableParameter(final String name, final String defaultValue) {
            this.name = name;
            this.defaultValue = defaultValue;
        }

        public MutableParameter setName(final String name) {
            this.name = name;
            return this;
        }

        public MutableParameter setDefaultValue(final String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public MutableParameter setDescription(final String description) {
            this.description = description;
            return this;
        }

        public MutableParameter setAllowedValues(final String allowedValues) {
            this.allowedValues = allowedValues;
            return this;
        }

        public MutableParameter setType(final String type) {
            this.type = type;
            return this;
        }

        public MutableParameter setRequired(final boolean required) {
            this.required = required;
            return this;
        }

        public Parameterizable.Parameter setStatic(final boolean immutable) {
            this.immutable = immutable;
            return immutable ? this.toImmutable() : this;
        }

        public MutableParameter setExcludingParameters(final String excludingParameters) {
            this.excludingParameters = excludingParameters;
            return this;
        }

        public MutableParameter setExcludingParameters(final Collection<String> excludingParameters) {
            this.excludingParameters = excludingParameters != null ? SUtils.concatStr(",", excludingParameters) : null;
            return this;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDefaultValue() {
            return defaultValue;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getAllowedValues() {
            return allowedValues;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public boolean isRequired() {
            return required;
        }

        @Override
        public boolean isStatic() {
            return immutable;
        }

        @Override
        public Set<String> getExcludingParameters() {
            return excludingParameters != null ? new HashSet<>(Arrays.asList(excludingParameters.split(","))) : null;
        }

        public String getExcludingParametersAsStr() {
            return excludingParameters;
        }

        public ImmutableParameter toImmutable() {
            return new ImmutableParameter(this);
        }
    }

    public ParameterSet() {
        this.parameters = new LinkedHashMap<>();
    }

    public ParameterSet(final ParameterSet other) {
        this.typeName = other.typeName;
        this.parameters = other.parameters;
    }

    public Collection<String[]> asNameDefaultValuePairs() {
        final List<String[]> pairs = new ArrayList<>(parameters.size());
        parameters.values().forEach(p -> pairs.add(new String[]{p.getName(), p.getDefaultValue()}));
        return pairs;
    }

    @Override
    public String getParamTypeName() {
        return typeName;
    }

    public Collection<Parameter> getParameters() {
        return parameters.values();
    }

    @Override
    public Parameter getParameter(final String name) {
        return parameters.get(name);
    }

    public void addParameter(final String parameterName, final String defaultValue) {
        addParameter(new MutableParameter(parameterName, defaultValue));
    }

    public void addParameter(final Parameter parameter) {
        parameters.put(parameter.getName(), parameter);
    }

    public boolean isEmpty() {
        return parameters.isEmpty();
    }

}
