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

// Test Class View
// =============

// Includes file dependencies
define([
    "jquery",
    "backbone",
    "etf.webui/v2",
    "etf.webui/views/EtfView",
    "etf.webui/views/StartTestView",
    "parser",
    "moment",
    "toastr",
    "../models/TestClassModel",
], function( $, Backbone, v2, EtfView, StartTestView, parser, moment, toastr, TestClassModel ) {

    var TestClassView = EtfView.extend( {

        el: $('#test-classes-page'),
        container : $("#test-class-container"),
        template: _.template($('script#test-classe-items-template').html()),

        initialize: function(options) {
            this.registerViewEvents();
            this.useLocalTestRunTemplateDialogCallback = options.useLocalTestRunTemplateDialogCallback;
        },

        events: {
            "change #temp-test-class-file-upload": "parseTestClass"
        },

        render: function(options) {

            var _this = this;

            if(!_.isNil(options) && !_.isEmpty(options.testObjectId)) {
                this.testObject = options.testObject;
                this.testObjectId = options.testObjectId;
                this.testObjectUrlParameter = '&testObjectId='+options.testObjectId;
                this.testObjectLabel = options.testObject.get('label');
                this.testObjectTypes = options.testObjectTypes;
            }else{
                this.testObjectId = '';
                this.testObject = null;
                this.testObjectUrlParameter= '';
                this.testObjectTypes = null;
            }
            this.collection.deferred.done(function() {
                var collection;
                if(!_.isNil(options.testObjectTypes)) {
                    collection = [];
                    _.each(_this.collection.models, function(obj) {
                        var types = obj.getTestObjectTypes();
                        if(Object.keys(types).some(function (id1) {
                            return Object.keys(options.testObjectTypes).some(function (id2) {
                                return types[id1].id === options.testObjectTypes[id2].id;
                            });
                        })) {
                            collection.push(obj.toJSON());
                        }
                    });
                }else{
                    collection = _this.collection.toJSON();
                }

                console.log("Rendering Test Class view: %o", collection);
                _this.container.html(
                    _this.template({
                        "moment": moment,
                        "v2": v2,
                        "collection": _this.filter(collection, _this.testObjectTypes),
                        "testObjectId": _this.testObjectId,
                        "testObjectUrlParameter": _this.testObjectUrlParameter,
                        "testObjectLabel": _this.testObjectLabel
                    })
                );
                _this.fileUpload = $("#temp-test-class-file-upload");
                _this.fileUpload.val("");

                _this.container.trigger('create');
                $("test-class-listview").listview().listview('refresh');
            });
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
            v2.jeach(collection, function(testClass) {
                if(!_.isEmpty(_.intersectionBy(_.values(testClass.supportedTestObjectTypes), _.values(parentTypes), 'id'))) {
                    newCollection.push(testClass);
                }
            });
            return newCollection;
        },

        parseTestClass: function () {
            var _this = this;
            var testRunTemplateFile = this.fileUpload[0].files[0];
            var reader = new FileReader();
            reader.readAsText(testRunTemplateFile);
            reader.onloadend = function(){
                var options = {
                    attributeNamePrefix : "",
                    ignoreAttributes : false,
                    parseAttributeValue: true
                };
                var jsonObj = parser.parse(reader.result, options);
                var selectedExecutableTestSuites = [];
                v2.jeachSafe(jsonObj.TestRunTemplate.executableTestSuites.executableTestSuite, function (ets) {
                    selectedExecutableTestSuites.push( ets['ref'] );
                });

                // remap to name
                var overrideParameters = {};
                if (!_.isUndefined(jsonObj.TestRunTemplate.ParameterList)) {
                    v2.jeachSafe(jsonObj.TestRunTemplate.ParameterList.parameter, function (p) {
                        overrideParameters[p.name] = p;
                    });
                }
                var testObject;
                if(!_.isNil(_this.testObject)) {
                    testObject = _this.testObject.toJSON();
                }

                var options = {
                    predefinedLabel : jsonObj.TestRunTemplate.author+ " - " +jsonObj.TestRunTemplate.label,
                    overrideParameters: overrideParameters,
                    executableTestSuiteIds: selectedExecutableTestSuites,
                    testObject: testObject
                };
                _this.useLocalTestRunTemplateDialogCallback(options);
                _this.fileUpload.val("");
            };
        }
    });

    return TestClassView;
} );
