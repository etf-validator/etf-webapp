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
package de.interactive_instruments.etf.dal.dto.result;

import java.util.Collection;

public enum TestResultStatus {

    // The ordinal() function is used for faster int to enum conversion:
    // new enum constants have to be added at the END !!!

    /**
     *
     * PASSED, if all status values are PASSED
     *
     * ordinal: 0
     */
    PASSED,

    /**
     * FAILED, if at least one status value is FAILED
     *
     * ordinal: 1
     */
    FAILED,

    /**
     * SKIPPED, if at least one status value is SKIPPED because a test case depends on another test case which has the
     * status FAILED or SKIPPED
     *
     * ordinal: 2
     */
    SKIPPED,

    /**
     * NOT_APPLICABLE if at least one status value is NOT_APPLICABLE, in the case the test object does not provide the
     * capabilities for executing the test
     *
     * ordinal: 3
     */
    NOT_APPLICABLE,

    /**
     * INFO, if at least one status value is INFO
     *
     * ordinal: 4
     */
    INFO,

    /**
     * WARNING, if at least one status value is WARNING
     *
     * ordinal: 5
     */
    WARNING,

    /**
     * UNDEFINED, in all other cases
     *
     * ordinal: 6
     */
    UNDEFINED,

    /**
     * PASSED_MANUAL, if at least one status value is PASSED_MANUAL (if the test is not automated and the user has to
     * validate results manually based on instructions in the report) and all others are values are PASSED
     *
     * ordinal: 7
     */
    PASSED_MANUAL,

    /**
     * INTERNAL_ERROR, if at least one status value is INTERNAL_ERROR in the case the test engine throws an unexpected error
     * that forces the test run to stop
     *
     * ordinal: 8
     */
    INTERNAL_ERROR;

    public int value() {
        return ordinal();
    }

    public static TestResultStatus valueOf(int i) {
        return TestResultStatus.values()[i];
    }

    public static String toString(int i) {
        return TestResultStatus.values()[i].toString();
    }

    public static TestResultStatus aggregateStatus(final Collection<TestResultStatus> status) {
        if (status == null || status.size() == 0) {
            return TestResultStatus.UNDEFINED;
        }
        return aggregateStatus(status.toArray(new TestResultStatus[status.size()]));
    }

    public static TestResultStatus aggregateStatus(final TestResultStatus... status) {
        if (status == null || status.length == 0) {
            return TestResultStatus.UNDEFINED;
        } else if (status.length == 1) {
            return status[0];
        }
        int currentStatus = status[0].ordinal();
        for (int i = 1; i < status.length; i++) {
            final int s = 10 * currentStatus + status[i].ordinal();
            // iterate over the status codes and compare the current status with the next status code
            switch (s) {
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 81:
            case 71:
            case 61:
            case 51:
            case 41:
            case 31:
            case 21:
                // Directly return FAILED 8 - * or * - 8 FAILED
                return TestResultStatus.FAILED;
            // list of status codes that are overwritten by the next status code
            case 80:
            case 82:
            case 83:
            case 84:
            case 85:
            case 86:
            case 87:
            case 88:
                // Ignore INTERNAL_ERROR 8 - * (except FAILED)
            case 00:
            case 03:
                // Ignore PASSED 0 - NOT_APPLICABLE
            case 20:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
                // Ignore SKIPPED 2 - * (except FAILED, INTERNAL_ERROR)
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
                // Ignore NOT_APPLICABLE 3 - * (except PASSED, FAILED, INTERNAL_ERROR, SKIPPED)
            case 40:
            case 44:
            case 46:
            case 47:
                // Ignore INFO 4 - * (except FAILED, INTERNAL_ERROR, SKIPPED, NOT_APPLICABLE, WARNING)
            case 50:
            case 54:
            case 55:
            case 56:
            case 57:
                // Ignore WARNING 5 - * (except FAILED, INTERNAL_ERROR, SKIPPED, NOT_APPLICABLE)
            case 70:
                break;
            default:
                currentStatus = status[i].ordinal();
                break;
            }
        }
        return TestResultStatus.values()[currentStatus];
    }
}
