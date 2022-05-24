/*
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

// Ets View
// =============

// Includes file dependencies
define([
    "jquery",
    "backbone",
    "etf.webui/v2",
    "etf.webui/views/EtfView",
    "moment",
    "toastr",
    "../models/ExecutableTestSuiteModel",
], function( $, Backbone, v2, EtfView, moment, ExecutableTestSuiteModel ) {

    var ExecutableTestSuiteView = EtfView.extend( {

        el: $('#executable-test-suites-page'),
        container : $("#executable-test-suite-container"),
        template: _.template($('script#executable-test-suite-items-template').html()),

        initialize: function() {
            this.registerViewEvents();
        },

        filter: function(collection, testObjectTypes) {
            if(testObjectTypes==null) {
                return collection;
            }
            var parentTypes = {}
            _.each(testObjectTypes, function(t) {
                parentTypes[t.id] = t;
                var p = t.parent;
                while(!_.isUndefined(p)) {
                    parentTypes[p.id] = p;
                    p = p.parent;
                }
            });
            var newCollection = [];
            v2.jeach(collection, function(ets) {
                if(!_.isEmpty(_.intersectionBy(_.values(ets.supportedTestObjectTypes), _.values(parentTypes), 'id'))) {
                    newCollection.push(ets);
                }
            });
            return newCollection;
        },

        getClassNamesForNotSelection: function(selection, startingWith) {
            var c = [];

            $.each($(selection).attr('class').split(" "), function (i, cl) {
                if(cl.indexOf(startingWith) === 0) {
                    c.push(":not(."+cl+")")
                }
            });
            return c.join();
        },

        render: function(options) {

            function filterEtsByTypes() {
                $('.executable-test-suite-selection').on('change', function () {
                    var selectedEtss =  $(".executable-test-suite-selection option[value!='X']:selected");
                    if(selectedEtss.length === 0) {
                        $('#fadin-start-executable-test-suites-button').hide('scale');
                        $('.executable-test-suite-selection').parent().removeClass("ui-disabled");
                    } else if (selectedEtss.length === 1) {
                        $('#fadin-start-executable-test-suites-button').show('scale');
                        // Get the test object types of all selected ETS and create an intersection
                        var intersection = [];
                        $.each(selectedEtss, function(i, selectedEts) {
                            var testObjectTypes = v2.getClassNamesArr(selectedEts.parentElement, "test-object-type-");
                            if(_.isEmpty(intersection)) {
                                intersection = testObjectTypes;
                            }else{
                                intersection = _.intersection(intersection, testObjectTypes);
                            }
                        });
                        // comma = OR
                        $('.executable-test-suite-selection:not(.'+_.join(intersection, ", .")+')').parent().addClass('ui-disabled');

                        console.log("."+_.join(intersection, ", ."));
                    }
                });
            }

            if(!_.isNil(options) && !_.isEmpty(options.testObjectId)) {
                this.testObjectId = options.testObjectId;
                this.testObjectUrlParameter = '&testObjectId='+options.testObjectId;
                this.testObjectLabel = options.testObjectLabel;
                this.testObjectTypes = options.testObjectTypes;
            }else{
                this.testObjectId = '';
                this.testObjectUrlParameter = null;
                this.testObjectLabel = '';
                this.testObjectTypes = null;
            }

            var _this = this;
            this.collection.deferred.done(function() {
                console.log("Rendering Executable Test Suite view: %o", _this.collection.toJSON());

                _this.container.html(
                    _this.template({
                        "moment": moment,
                        "v2": v2,
                        "collection": _this.filter(_this.collection.toJSON(), _this.testObjectTypes),
                        "testObjectId": _this.testObjectId,
                        "testObjectLabel": _this.testObjectLabel
                    })
                );

                filterEtsByTypes();

                $('#fadin-start-executable-test-suites-button').on('click', function (e) {
                    var u = '#configure-run-with-executable-test-suites?ids=';
                    $.each($('.executable-test-suite-selection'), function () {
                        var val = $(this).val();
                        if (val !== 'X') {
                            u += val + ",";
                        }
                    });
                    u = u.slice(0, -1);
                    if(!_.isNil(_this.testObjectUrlParameter)) {
                        u += _this.testObjectUrlParameter;
                    }
                    $(this).attr("href", u);
                });
                $('#fadin-start-executable-test-suites-button').hide();

                // Filter by tag class
                var listview = $("#executable-test-suite-listview");
                listview.listview({
                    autodividers: true,
                    autodividersSelector: function (li) {
                        var tag = $(li).find('.Tag');
                        if (tag.length) {
                            return tag.text();
                        } else {
                            return "";
                        }
                    }
                });

                // Stop collapsible
                $('.ii-stop-propagation').on('click', function (e) {
                    e.stopPropagation();
                    e.stopImmediatePropagation();
                    e.preventDefault();
                });

                _this.container.trigger('create');
                listview.listview().listview('refresh');
            });
        },
    });

    return ExecutableTestSuiteView;
} );
