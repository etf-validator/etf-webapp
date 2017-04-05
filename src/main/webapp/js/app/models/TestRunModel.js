/*
 * Copyright ${year} interactive instruments GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Executable Test Suite Model
// ==============

// Includes file dependencies
define([
    "jquery",
    "backbone",
    "etf.webui/v2",
], function( $, Backbone, v2 ) {

    // The Model constructor
    var Model = Backbone.Model.extend( {

        initialize: function( attr, options ) {
            this.testObjectCollection = options.collection.testObjectCollection;
            this.etsCollection = options.collection.etsCollection;
        },

        toJSON: function() {
            var testObjectCollection = this.testObjectCollection;
            var etsCollection = this.etsCollection;
            var attributes = _.clone(this.attributes);

            var testTasks =  this.get("testTasks");
            if(!_.isUndefined(testTasks)) {
                if(_.isUndefined(testTasks.TestTask[0])) {
                    // single task

                    if(!_.isUndefined(testTasks.TestTask.testObject) &&
                        !_.isUndefined(testTasks.TestTask.testObject.href)) {
                        attributes['testTasks'].TestTask.testObject = v2.resolveRefOrUndefined(
                            testTasks.TestTask.testObject.href, testObjectCollection);
                    }

                    if(!_.isUndefined(testTasks.TestTask.executableTestSuite) &&
                        !_.isUndefined(testTasks.TestTask.executableTestSuite.href)) {
                        attributes['testTasks'].TestTask.executableTestSuite = v2.resolveRefOrUndefined(
                            testTasks.TestTask.executableTestSuite.href,
                            etsCollection);
                    }

                }else{
                    if(!_.isUndefined(testTasks.TestTask[0].testObject) &&
                        !_.isUndefined(testTasks.TestTask[0].testObject.href)) {
                        attributes['testTasks'].TestTask[0].testObject = v2.resolveRefOrUndefined(
                            testTasks.TestTask[0].testObject.href, testObjectCollection);
                    }
                    v2.jeach(testTasks.TestTask, function(task, i) {
                        if(!_.isUndefined(task.executableTestSuite) &&
                            !_.isUndefined(task.executableTestSuite.href)) {
                            attributes['testTasks'].TestTask[i].executableTestSuite = v2.resolveRefOrUndefined(
                                task.executableTestSuite.href,
                                etsCollection);
                        }
                    })
                }


            }
            return attributes;
        },

    } );

    // Returns the Model class
    return Model;

} );
