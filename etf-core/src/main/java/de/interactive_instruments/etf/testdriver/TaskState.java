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
package de.interactive_instruments.etf.testdriver;

/**
 * TaskState
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface TaskState {

    enum STATE {
        /**
         * Created. Start state.
         *
         * Possible transitions to: INITIALIZING
         *
         */
        CREATED(0),

        /**
         * The task is initializing.
         *
         * Possible transitions to: INITIALIZED
         */
        INITIALIZING(7),

        /**
         * Task is initialized.
         *
         * Possible transitions to: RUNNING
         */
        INITIALIZED(13),

        /**
         * The task is running and is working on the job.
         *
         * Possible transitions to: COMPLETED, FAILED, CANCELING
         */
        RUNNING(19),

        /**
         * The task successfully competed the Job.
         *
         * Possible transition to: FINALIZING
         */
        COMPLETED(107),

        /**
         * The task completed and is now finalized by a managing object.
         *
         * Final state
         */
        FINALIZING(37),

        /**
         * The task failed and is not running anymore.
         *
         * Possible transition to: CANCELED
         */
        FAILED(53),

        /**
         * A request was send to cancel the running task.
         *
         * Possible transition to: CANCELED
         */
        CANCELING(71),

        /**
         * The task has been successfully cancelled.
         *
         * Possible transition to: FINALIZING
         */
        CANCELED(89);

        private final int code;

        STATE(int code) {
            this.code = code;
        }

        /**
         * Returns true if the task is in the running state
         *
         * @return true if in running state
         */
        public boolean isRunning() {
            return code == 19;
        }

        /**
         * Returns true if the task has completed
         *
         * @return true if in completed state
         */
        public boolean isCompleted() {
            return code == 107;
        }

        public boolean isFailed() {
            return code == 53;
        }

        public boolean isCanceling() {
            return code == 71;
        }

        public boolean isCanceled() {
            return code == 89;
        }

        /**
         * Returns true if in a running or initializing state
         *
         * @return true if in running or initializing
         */
        public boolean isRunningOrInitializing() {
            return code == 19 || code == 7;
        }

        public boolean isAtLeastInitialized() {
            return code >= 13;
        }

        /**
         * Returns true if in final state
         *
         * @return true if in a final state
         */
        public boolean isFinalizing() {
            return code == 37;
        }

        /**
         * Returns true if the task completed (not a final state) failed (final) or canceled (final)
         *
         * @return true if in a final state
         */
        public boolean isCompletedFailedOrCanceled() {
            return code == 107 || code == 53 || code == 89;
        }

        public boolean isCompletedFailedCanceledOrFinalizing() {
            return isCompletedFailedOrCanceled() || isFinalizing();
        }
    }

    STATE getState();
}
