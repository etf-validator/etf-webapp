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
package de.interactive_instruments.etf.dal.dto.translation;

import java.util.Objects;

/**
 * A named object that holds a string which may contain one or multiple tokens in a specific language. A Translation
 * Template is used to translate messages.
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class TranslationTemplateDto {

    private String name;
    private String language;
    private String strWithTokens;
    private static String IDENTIFIER_PREFIX = "TR.";

    /**
     * Private Ctor for JAXB
     */
    TranslationTemplateDto() {}

    public TranslationTemplateDto(final String name, final String language, final String strWithTokens) {
        if (Objects.requireNonNull(name).startsWith(IDENTIFIER_PREFIX)) {
            this.name = name;
        } else {
            this.name = IDENTIFIER_PREFIX + name;
        }
        this.language = language;
        this.strWithTokens = strWithTokens;
    }

    /**
     * Returns the name of this template
     *
     * @return the name of this template
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the language of the message with tokens
     *
     * @return the language of the message with tokens
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Returns a message which may contain tokens, in this form "Error, object has invalid ID: {$ID_TOKEN}"
     *
     * @return a message which may contain tokens
     */
    public String getStrWithTokens() {
        return strWithTokens;
    }
}
