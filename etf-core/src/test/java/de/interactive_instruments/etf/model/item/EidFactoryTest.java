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
package de.interactive_instruments.etf.model.item;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidFactory;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class EidFactoryTest {

    @Test
    public void testFactory() {

        final String id1Str = "No UUID";
        final String id1AsUuidStr = "a402ff87-805b-3875-bf17-eddd0bba21d9";
        final EID id1 = EidFactory.getDefault().createAndPreserveStr(id1Str);

        assertEquals(id1, id1.getId());
        assertEquals(id1AsUuidStr, id1.toUuid().toString());

        assertTrue(id1.equals(id1));
        assertTrue(id1.equals(id1.getId()));

        assertFalse(id1AsUuidStr.equals(id1));
        assertFalse(id1AsUuidStr.equals(id1.getId()));
        assertTrue(id1AsUuidStr.equals(id1.toUuid().toString()));

        assertTrue(id1.equals(UUID.fromString(id1AsUuidStr)));

        assertTrue(UUID.fromString(id1AsUuidStr).equals(id1.toUuid()));
        assertFalse(UUID.fromString(id1AsUuidStr).equals(id1));

        final String uuidAsStr2 = "99b01a1a-5423-49a9-8c22-27519a95d9bd";
        final EID id2 = EidFactory.getDefault().createAndPreserveStr(uuidAsStr2);
        assertEquals(uuidAsStr2, id2.getId());
        assertEquals(uuidAsStr2, id2.toUuid().toString());

    }
}
