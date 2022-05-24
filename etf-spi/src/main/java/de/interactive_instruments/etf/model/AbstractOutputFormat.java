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

/**
 * Abstract OutputFormat
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public abstract class AbstractOutputFormat implements OutputFormat {

    private final String label;
    private final EID id;

    protected AbstractOutputFormat(final String label) {
        this.label = label;
        this.id = EidFactory.getDefault().createUUID(label);
    }

    @Override
    public EID getId() {
        return id;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public int compareTo(final OutputFormat o) {
        return getId().compareTo(o.getId());
    }
}
