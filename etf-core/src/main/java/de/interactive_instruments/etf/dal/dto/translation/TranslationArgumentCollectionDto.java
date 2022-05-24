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
package de.interactive_instruments.etf.dal.dto.translation;

import java.util.HashMap;
import java.util.Map;

import de.interactive_instruments.etf.dal.dto.Dto;

/**
 * Collection of token/value pairs that are used to replace tokens in TranslationTemplates
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class TranslationArgumentCollectionDto extends Dto {

    private String translationTemplateName;

    private Map<String, Argument> arguments;

    public TranslationArgumentCollectionDto() {}

    private TranslationArgumentCollectionDto(final TranslationArgumentCollectionDto other) {
        this.translationTemplateName = other.translationTemplateName;
        this.arguments = other.arguments;
    }

    public static class Argument {
        String token;
        String value;

        public Argument() {

        }

        public Argument(final String token, final String value) {
            this.token = token;
            this.value = value;
        }

        public String getToken() {
            return token;
        }

        public String getValue() {
            return value;
        }
    }

    public Map<String, Argument> getTokenValues() {
        return arguments;
    }

    public void addTokenValue(final String token, String value) {
        if (arguments == null) {
            arguments = new HashMap<>();
        }
        arguments.put(token, new Argument(token, value));
    }

    /**
     * Name of the Translation Template for which the token replacements are applied
     *
     * @return String reference name
     */
    public String getRefTemplateName() {
        return translationTemplateName;
    }

    public void setRefTemplateName(final String translationTemplateName) {
        this.translationTemplateName = translationTemplateName;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("TranslationArgumentCollectionDto{");
        sb.append("translationTemplateName='").append(translationTemplateName).append('\'');
        sb.append(", arguments=").append(arguments);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public TranslationArgumentCollectionDto createCopy() {
        return new TranslationArgumentCollectionDto(this);
    }
}
