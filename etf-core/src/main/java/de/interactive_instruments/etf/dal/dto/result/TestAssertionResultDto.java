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
package de.interactive_instruments.etf.dal.dto.result;

import java.util.ArrayList;
import java.util.List;

import de.interactive_instruments.etf.dal.dto.Arguments;
import de.interactive_instruments.etf.dal.dto.translation.TranslationArgumentCollectionDto;

public class TestAssertionResultDto extends ResultModelItemDto {

    // Optional arguments for parameterizable test assertions
    private Arguments arguments;

    private List<TranslationArgumentCollectionDto> messages;

    public TestAssertionResultDto() {}

    TestAssertionResultDto(final TestAssertionResultDto other) {
        super(other);
        this.arguments = other.arguments;
        this.messages = other.messages;
    }

    public Arguments getArguments() {
        return arguments;
    }

    public void setArguments(final Arguments argumentsDto) {
        this.arguments = argumentsDto;
    }

    public List<TranslationArgumentCollectionDto> getMessages() {
        return messages;
    }

    public void setMessages(final List<TranslationArgumentCollectionDto> messages) {
        this.messages = messages;
    }

    public void addMessage(final TranslationArgumentCollectionDto message) {
        if (this.messages == null) {
            this.messages = new ArrayList<>();
        }
        this.messages.add(message);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("TestAssertionResultDto{");
        sb.append("arguments=").append(arguments);
        sb.append(", messages=").append(messages);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public TestAssertionResultDto createCopy() {
        return new TestAssertionResultDto(this);
    }
}
