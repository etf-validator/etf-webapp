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

// Ets View
// =============

// Includes file dependencies
define([
    "jquery",
    "backbone",
    "moment",
    "etf.webui/v2",
    "etf.webui/views/EtfView",
    "../models/TestObjectModel",
    "etf.webui/views/EtfView",
], function( $, Backbone, moment, v2, EtfView, TestObjectModel ) {

    var TestObjectView = EtfView.extend( {

        el: $('#test-objects-page'),
        container: $("#test-object-item-listview-container"),
        template: _.template($('script#test-object-items-template').html()),


    initialize: function() {
            // this.collection.on( "added", this.render, this );
            this.collection.on( "remove", this.remove, this );
            this.registerViewEvents();
        },

        render: function() {
            this.collection.deferred.done(function() {
                console.log("Rendering Test Object view: %o", _this.collection.toJSON());

                _this.container.html(
                    _this.template({moment: moment, v2: v2, "collection": _this.collection.toJSON()})
                );

                _this.container.trigger('create');
                _this.container.listview().listview('refresh');
            });
        },

        remove: function(model) {
            console.log("Removing Test Object "+model.id+" from view" );
            $("#test-object-item-"+model.id).remove();
            return this;
        },
    } );

    return TestObjectView;
} );
