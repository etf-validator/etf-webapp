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
import org.eclipse.persistence.mappings.transformers.AttributeTransformer;
import org.eclipse.persistence.sessions.Record;
import org.eclipse.persistence.sessions.Session;

import de.interactive_instruments.SUtils;
import de.interactive_instruments.etf.model.EidFactory;

/**
 * Reads the string form XML and transform it to an EID
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public final class EidAttributeTransformer implements AttributeTransformer {

    private final EidFactory factory = EidFactory.getDefault();

    @Override
    public void initialize(final AbstractTransformationMapping mapping) {}

    @Override
    public Object buildAttributeValue(final Record record, final Object object, final Session session) {
        return factory.createAndPreserveStr(
                SUtils.requireNonNullOrEmpty(((String) record.get("@id")).substring(3),
                        "ID attribute is null or empty"));
    }
}
