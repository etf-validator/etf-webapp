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
package de.interactive_instruments.etf.dal.dto.test;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import de.interactive_instruments.etf.dal.dto.MetaDataItemDto;
import de.interactive_instruments.etf.dal.dto.ModelItemTreeNode;
import de.interactive_instruments.etf.model.DefaultEidMap;
import de.interactive_instruments.etf.model.EidMap;
import de.interactive_instruments.properties.Properties;

/**
 * Test Model Item Dto
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public abstract class TestModelItemDto extends MetaDataItemDto implements ModelItemTreeNode<TestModelItemDto> {

    protected EidMap<TestModelItemDto> children;

    protected Properties properties;

    protected TestModelItemDto() {}

    protected TestModelItemDto(final TestModelItemDto other) {
        super(other);
        if (other.children != null) {
            this.children = other.children.createCopy();
        }
        this.properties = other.properties;
    }

    @Override
    public List<? extends TestModelItemDto> getChildren() {
        return children != null ? children.values().stream().collect(Collectors.toList()) : null;
    }

    @Override
    public EidMap<? extends TestModelItemDto> getChildrenAsMap() {
        return children;
    }

    @Override
    public void addChild(final TestModelItemDto child) {
        if (this.children == null) {
            this.children = new DefaultEidMap<>();
        }
        Objects.requireNonNull(child);
        Objects.requireNonNull(child.getId(), "Cannot add item whose ID is null");
        child.setParent(this);
        this.children.put(child.getId(), child);
    }

    @Override
    public void setChildren(final List<? extends TestModelItemDto> children) {
        if (children != null) {
            children.forEach(c -> {
                c.setParent(this);
                addChild(c);
            });
        }
    }

    public Properties properties() {
        if (properties == null) {
            properties = new Properties();
        }
        return properties;
    }

    public void properties(final Properties properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("TestModelItemDto{");
        sb.append("id=").append(getId());
        sb.append(", parent=").append(parent != null ? parent.getId() : null);
        sb.append(", label=").append(label);
        sb.append(", description=").append(description);
        sb.append(", reference=").append(reference);
        sb.append(", children=").append(children != null ? children.size() : "none");
        sb.append('}');
        return sb.toString();
    }
}
