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

// Tag Collection
// ===================

// Includes file dependencies
define([
    "jquery",
    "backbone",
    "toastr",
    "etf.webui/v2",
    "../models/TagModel" ], function($, Backbone, toastr, v2, TagModel ) {

    var Collection = Backbone.Collection.extend( {

        url: v2.baseUrl + "/Tags",

        // The Collection constructor
        initialize: function( models, options ) {
            this.deferred = this.fetch();
        },

        parse: function(response) {
            if(_.isUndefined(response.EtfItemCollection.tags)) {
                return null;
            }
             return response.EtfItemCollection.tags.Tag;
        },

        model: TagModel,

        sync: function(method, model, options) {
            var deferred = $.Deferred();

            Backbone.sync(method, model, options);

            return deferred;
        },

        fetch: function(options) {
            var _this = this;
            console.log("Fetching Tags");
            return Backbone.Collection.prototype.fetch.call(this, {
                options: options,
                success: function() {
                    console.log("Successfully fetched Tags");
                    _this.trigger("added");
                    return;
                },
                error: function(response) {
                    toastr["error"]("Could not fetch Tags !");
                    console.log(response);
                    return;
                }
            });
        }
    });

    return Collection;

} );
