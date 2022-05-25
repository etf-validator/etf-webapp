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
package de.interactive_instruments.etf.dal.dto;

/**
 * Model item Data transfer object
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public abstract class ModelItemDto<T extends ModelItemDto> extends Dto {
    protected T parent;

    public ModelItemDto() {}

    public ModelItemDto(final ModelItemDto<T> other) {
        this.id = other.id;
        this.parent = other.parent;
    }

    public T getParent() {
        return parent;
    }

    public void setParent(final T value) {
        this.parent = value;
    }

    String getParentTypeName() {
        return parent != null ? parent.getTypeName() : null;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ModelItemDto{");
        sb.append("id=").append(getId());
        sb.append(", parent=").append(parent != null ? parent.getId() : null);
        sb.append('}');
        return sb.toString();
    }
}
