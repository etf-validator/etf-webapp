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
import java.util.Objects;
import java.util.Set;

import de.interactive_instruments.SUtils;

/**
 * A named collection which holds TranslationTemplates {@link TranslationTemplateDto} of one message type in different
 * languages.
 *
 * A template can be accessed through its language: {@link LangTranslationTemplateCollectionDto#getByLanguage(String)}
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class LangTranslationTemplateCollectionDto {

    /**
     * Name of this collection
     */
    private String name;

    /**
     * Maps a language {@link TranslationTemplateDto#language} to a {@link TranslationTemplateDto}
     */
    private Map<String, TranslationTemplateDto> translationTemplates;

    /**
     * Private Ctor for JAXB
     */
    private LangTranslationTemplateCollectionDto() {}

    /**
     * Create a LangTranslationTemplateCollectionDto and add one initial TranslationTemplateDto
     *
     * @param translationTemplate
     */
    LangTranslationTemplateCollectionDto(final TranslationTemplateDto translationTemplate) {
        Objects.requireNonNull(translationTemplate);
        this.name = SUtils.requireNonNullOrEmpty(translationTemplate.getName(),
                "TranslationTemplate name is null or empty");
        this.translationTemplates = new HashMap<>();
        this.translationTemplates.put(translationTemplate.getLanguage(), translationTemplate);
    }

    /**
     * Returns the name of this collection
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Add a template by its name to the collection
     *
     * @param templateDto
     *            TranslationTemplateDto to add
     */
    void add(final TranslationTemplateDto templateDto) {
        translationTemplates.put(
                SUtils.requireNonNullOrEmpty(templateDto.getLanguage(), "TranslationTemplate name is null or empty"),
                templateDto);
    }

    /**
     * Return a TranslationTemplate by its language
     *
     * @param language
     *            ISO language code
     *
     * @return TranslationTemplateDto
     */
    public TranslationTemplateDto getByLanguage(final String language) {
        return translationTemplates.get(language);
    }

    /**
     * Returns all available languages
     *
     * @return Set of Strings
     */
    public Set<String> getLanguages() {
        return translationTemplates.keySet();
    }
}
