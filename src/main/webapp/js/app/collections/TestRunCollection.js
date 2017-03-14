/*
 * Copyright 2010-2017 interactive instruments GmbH
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

// Test Run Collection
// ===================

// Includes file dependencies
define([
    "jquery",
    "backbone",
    "moment",
    "toastr",
    "etf.webui/v2",
    "../models/TestRunModel" ], function($, Backbone, moment, toastr, v2, TestRunModel ) {

    var Collection = Backbone.Collection.extend( {

        url: v2.baseUrl + "/TestRuns",

        // The Collection constructor
        initialize: function( models, options ) {
            this.deferred = this.fetch();
        },

        parse: function(response) {
            if(_.isUndefined(response.EtfItemCollection.testRuns)) {
                return null;
            }
            return response.EtfItemCollection.testRuns.TestRun;
        },

        comparator: function(a, b) {
            var diff = moment(a.get('startTimestamp')).diff(b.get('startTimestamp'));
            if(diff > 1) {
                return -1;
            }else if(diff < 1) {
                return 1;
            }else{
                return 0;
            }
            return 0;
        },

        model: TestRunModel,

        fetch: function(options) {
            var self = this;
            return Backbone.Collection.prototype.fetch.call(this, {
                options: options,
                success: function() {
                    console.log("Successfully fetched Test Runs");
                    self.trigger("added");
                    return;
                },
                error: function(response) {
                    toastr["error"]("Could not fetch Test Runs !");
                    console.log(response);
                    return;
                }
            });
        }
    });

    return Collection;

} );
