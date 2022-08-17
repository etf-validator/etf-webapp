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
package de.interactive_instruments.etf.model.capabilities;

import java.util.Collection;
import java.util.List;

import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectDto;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectTypeDto;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidHolder;
import de.interactive_instruments.etf.model.EidHolderWithParent;
import de.interactive_instruments.etf.model.ExpressionType;
import de.interactive_instruments.io.FileContentFilterHolder;

/**
 * A Test Object Type describes a {@link TestObjectDto} and may possess information how the type can be detected.
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface TestObjectType extends FileContentFilterHolder, EidHolder, EidHolderWithParent<TestObjectType> {

    String getLabel();

    String getDescription();

    TestObjectType getParent();

    List<TestObjectTypeDto> getSubTypes();

    String getDetectionExpression();

    ExpressionType getDetectionExpressionType();

    String getLabelExpression();

    ExpressionType getLabelExpressionType();

    String getDescriptionExpression();

    ExpressionType getDescriptionExpressionType();

    default boolean isInstanceOf(final EID testObjectTypeId) {
        if (testObjectTypeId.equals(getId())) {
            return true;
        }
        for (TestObjectType parent = getParent(); parent != null; parent = parent.getParent()) {
            if (testObjectTypeId.equals(parent.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a Test Object Type equals the passed Test Object Type or is a subtype of it
     *
     * @param testObjectType
     *            Test Object Type
     * @return true if Test Object Type equals or is a subtype of it
     */
    default boolean isInstanceOf(final TestObjectType testObjectType) {
        return isInstanceOf(testObjectType.getId());
    }

    /**
     * Returns true if the provided Test Object Type collection contains the Test Object Types or it is a subtype of it.
     *
     * @param testObjectTypes
     *            list of Test Object Types
     * @return true if list contains the Test Object Type or the Test Object Type is a subtype of one of the types, false
     *         otherwise
     */
    default boolean isInstanceOf(final Collection<? extends TestObjectType> testObjectTypes) {
        for (final TestObjectType testObjectType : testObjectTypes) {
            if (testObjectType.isInstanceOf(this)) {
                return true;
            }
        }
        return false;

    }
}
