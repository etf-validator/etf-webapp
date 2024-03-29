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
package de.interactive_instruments.etf.webapp.dto;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class DocumentationConstants {

    public final static String EID_EXAMPLE = "EID12bb90ca-ee02-4f79-9dd9-63dff6d8e150";
    public final static String EID_DESCRIPTION = "The ETF ID is an 36 characters long "
            + "hexadecimal Universally Unique Identifier prefixed with 'EID', e.g. " + EID_EXAMPLE;
    public final static String EID_LIST_EXAMPLE = "EID12bb90ca-ee02-4f79-9dd9-63dff6d8e150, 6c56cec7-649d-4aa8-9f95-563c9c043dce";

    public final static String TEST_RUN_LABEL_DESCRIPTION = "A label for the test run.";
    public final static String TEST_RUN_LABEL_EXAMPLE = "Test run on 15:00 - 01.01.2017 with Conformance classes X,Y and Z";

    public final static String TEST_OBJECT_LABEL_DESCRIPTION = "A label for the test object.";
    public final static String TEST_OBJECT_LABEL_EXAMPLE = "Partial delivery of spatial data";

    public final static String TEST_TASK_DESCRIPTION = "A Test Task bundles exactly one Executable Test Suite which is executed "
            + "against exactly one Test Object with a set of user specified arguments.";

    public final static String LIMIT_DESCRIPTION = "The limit indicates the maximum number of items to return in an ETF item collection. "
            + "Values less equal 0 will be silently defaulted to the value 100.";

    public final static String OFFSET_DESCRIPTION = "The offset indicates the starting position of this request in relation to the complete set of unpaginated items. "
            + "Values less than 0 will be silently defaulted to the value 0.";

    public final static String FIELDS_DESCRIPTION = "If set to a value other than '*' a partial response with only the selected fields is returned. "
            + "Only fields on the highest level can be selected and should be separated with a comma. Unknown fields are silently ignored. "
            + "As also mandatory fields can be filtered the response may not validate against the default schema. ";

    public final static String ETF_ITEM_COLLECTION_DESCRIPTION = "Items are returned in an "
            + "[ETF item collection](https://services.interactive-instruments.de/etf/schemadoc/service_xsd.html#EtfItemCollection).";
}
