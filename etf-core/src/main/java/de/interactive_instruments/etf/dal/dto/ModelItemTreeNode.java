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

import java.util.List;

import de.interactive_instruments.etf.model.EidMap;
import de.interactive_instruments.etf.model.ModelItemWithParent;

/**
 * Todo: move to model package
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface ModelItemTreeNode<T> extends ModelItemWithParent<ModelItemDto> {

    List<? extends T> getChildren();

    EidMap<? extends T> getChildrenAsMap();

    // Todo: remove from interface (when moved to model package)
    void addChild(final T child);

    void setChildren(final List<? extends T> children);
}
