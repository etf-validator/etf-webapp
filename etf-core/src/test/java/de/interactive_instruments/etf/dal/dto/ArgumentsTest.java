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

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class ArgumentsTest {

    @Test
    public void testValueResolving() {
        final Map<String, String> properties = new HashMap<>();
        properties.put("KEY", "VALUE");
        properties.put("Variable_1", "Value_of_Variable_1");
        properties.put("Variable_2", "Value_of_Variable_2");
        properties.put("Variable_3", "Value_of_Variable_3");
        properties.put("Variable_4", "${Variable_1}/Value_of_Variable_4");
        properties.put("Variable_5", "${Variable_unknown}");
        properties.put("Variable_6", "{$Value_of_Variable_6}/${}/${{a}}");
        properties.put("Variable_7", "${Variable_1}/${Variable_2}/${Variable_4}");
        properties.put("Variable_8", "${Variable_8}");
        properties.put("Variable_9", "${Variable_9}/Value_of_Variable_9");
        properties.put("Variable_10", "${Variable_1}/Value_of_Variable_10/${Variable_10}");

        final Arguments arguments = new Arguments(properties);

        assertEquals("VALUE", arguments.value("KEY"));
        assertEquals(null, arguments.value("UNKNOWN"));
        assertEquals("Value_of_Variable_1", arguments.value("Variable_1"));
        assertEquals("Value_of_Variable_1/Value_of_Variable_4", arguments.value("Variable_4"));
        assertEquals("${Variable_unknown}", arguments.value("Variable_5"));
        assertEquals("{$Value_of_Variable_6}/${}/${{a}}", arguments.value("Variable_6"));
        assertEquals("Value_of_Variable_1/Value_of_Variable_2/Value_of_Variable_1/Value_of_Variable_4",
                arguments.value("Variable_7"));
        assertEquals("${Variable_8}", arguments.value("Variable_8"));
        assertEquals("${Variable_9}/Value_of_Variable_9", arguments.value("Variable_9"));
        assertEquals("Value_of_Variable_1/Value_of_Variable_10/${Variable_10}", arguments.value("Variable_10"));
    }
}
