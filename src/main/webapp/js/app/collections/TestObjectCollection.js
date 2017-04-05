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

// Test Object Collection
// ===================

// Includes file dependencies
define([
    "jquery",
    "backbone",
    "toastr",
    "etf.webui/v2",
    "../models/TestObjectModel" ], function($, Backbone, toastr, v2, TestObjectModel ) {

    var Collection = Backbone.Collection.extend( {

        url: v2.baseUrl + "/TestObjects",

        // The Collection constructor
        initialize: function( models, options ) {
            this.testObjectTypeCollection = options.testObjectTypeCollection;

            var self = this;
            this.deferred = $.when(
                this.testObjectTypeCollection.deferred.done
            ).then(function() {
                return self.fetch()
            });
        },

        parse: function(response) {
            if(_.isUndefined(response.EtfItemCollection.testObjects)) {
                return null;
            }
             return response.EtfItemCollection.testObjects.TestObject;
        },

        model: TestObjectModel,

        fetch: function(options) {
            var self = this;
            return Backbone.Collection.prototype.fetch.call(this, {
                options: options,
                success: function() {
                    console.log("Successfully fetched Test Objects");
                    self.trigger("added");
                    return;
                },
                error: function(response) {
                    toastr["error"]("Could not fetch Test Objects !");
                    console.log(response);
                    return;
                }
            });
        }
    });

    return Collection;

} );
