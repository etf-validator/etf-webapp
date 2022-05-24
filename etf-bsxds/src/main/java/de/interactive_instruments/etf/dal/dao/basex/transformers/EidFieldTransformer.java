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
package de.interactive_instruments.etf.dal.dao.basex.transformers;

import org.eclipse.persistence.mappings.foundation.AbstractTransformationMapping;
import org.eclipse.persistence.mappings.transformers.FieldTransformer;
import org.eclipse.persistence.sessions.Session;

import de.interactive_instruments.etf.dal.dao.basex.BsxDataStorage;
import de.interactive_instruments.etf.dal.dto.Dto;

/**
 * Writes an EID to XML as String
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public final class EidFieldTransformer implements FieldTransformer {

    @Override
    public void initialize(final AbstractTransformationMapping mapping) {
        mapping.getFieldTransformations().get(0).getField().setPrimaryKey(true);
        mapping.getFieldTransformations().get(0).getField().setType(String.class);
    }

    @Override
    public Object buildFieldValue(final Object instance, final String fieldName, final Session session) {
        return BsxDataStorage.ID_PREFIX + ((Dto) instance).getId();
    }
}
