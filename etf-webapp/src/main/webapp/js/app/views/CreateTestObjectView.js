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
    "moment",
    "toastr",
    "etf.webui/v2",
    "etf.webui/views/EtfView",
    "../models/TestObjectModel",
], function( $, Backbone, moment, toastr, v2, EtfView, TestObjectModel ) {

    var CreateTestObjectView = EtfView.extend( {

        el: $('#create-testobject-dialog'),
        container: $("#create-testobject-container"),
        template: _.template($('script#create-testobject-dialog-template').html()),

    events: {
        "click #create-testobject-confirm": "confirmCreate",
    },

    initialize: function(options) {
            this.registerViewEvents();
            this.backPage = options.backPage.context.baseURI;
            this.testObjectCollection = options.collection.testObjectCollection;
    },

    render: function() {
        var _this = this;
        this.collection.deferred.done(function() {
            console.log("Rendering Test Object view: %o", _this.collection.toJSON());

            _this.container.html(
                _this.template({
                    moment: moment,
                    v2: v2,
                    "collection": _this.collection.toJSON()
                })
            );

            // Show tooltips
            $("span.question").hover(
                function () {
                    $(this).append('<div class="tooltip"><p>'+ $(this).attr('help') + '</p></div>');
                },
                function () {
                    $("div.tooltip").remove();
                }
            );

            var dataSourceSelection = $( "#create-test-object-source-selection");
            var fileUploadSelection = $( "#create-test-object-file-based");
            var remoteUrlSelection = $( "#create-test-object-url-based");
            fileUploadSelection.show();
            remoteUrlSelection.hide();
            dataSourceSelection.change( function() {
                if(dataSourceSelection.val()=='file') {
                    fileUploadSelection.show();
                    remoteUrlSelection.hide();
                }else if(dataSourceSelection.val()=='url') {
                    fileUploadSelection.hide();
                    remoteUrlSelection.show();
                }});

            $('#create-testobject-confirm').removeClass('ui-disabled');

            _this.container.trigger('create');
        });
    },

        confirmCreate: function(e) {
            e.preventDefault();

            var testObjectLabelInput = $('#testObjectLabel');
            var errors=false;
            var testObjectLabel = $.trim(testObjectLabelInput.val());
            if (testObjectLabel.length === 0) {
                testObjectLabel = 'auto';
            }else if (testObjectLabel.length < 4) {
                v2.invalidInputError("Der Bezeichner muss mindestens 4 Zeichen lang sein", testObjectLabelInput);
                errors=true;
            }

            var testObjectDescriptionInput = $('#testObjectDescription');
            var testObjectDescription = $.trim(testObjectDescriptionInput.val());
            if (testObjectDescription.length === 0) {
                testObjectDescription = 'auto';
            }else if (testObjectDescription.length < 4) {
                v2.invalidInputError("Die Beschreibung muss mindestens 4 Zeichen lang sein", testObjectDescriptionInput);
                errors=true;
            }


            var createTestObjectRequest;
            var files = null;
            var dataSourceSelection = $( "#create-test-object-source-selection");
            if(dataSourceSelection.val()=='file') {
                var testObjectFileUpload = $('#create-test-object-fileupload');
                if (!testObjectFileUpload.val() || !testObjectFileUpload[0].files) {
                    v2.invalidInputError("Es sind keine Testdaten angegeben", testObjectFileUpload);
                    errors=true;
                }else{
                    createTestObjectRequest = new v2.CreateReusableTestObjectRequest(testObjectLabel,testObjectDescription)
                    files = testObjectFileUpload[0].files;
                }
            }else{
                var remoteUrlSelection = $('#create-test-object-source-service-endpoint');
                if (remoteUrlSelection.val().trim().length < 8) {
                    v2.invalidInputError("Es wurde keine URL angegeben", remoteUrlSelection);
                    errors=true;
                }else{
                    createTestObjectRequest = new v2.CreateReusableTestObjectRequest(testObjectLabel,testObjectDescription,
                        new v2.Resource("serviceEndpoint", remoteUrlSelection.val()))
                }
            }

            var _this = this;
            if(!errors) {
                $('#create-testobject-confirm').addClass('ui-disabled');
                v2.createTestObject(
                    createTestObjectRequest,
                    files,
                    function (data) {
                        if (!_.isUndefined(data.testObject)) {
                            var testObject = new TestObjectModel({id: v2.eidFromUrl(data.testObject.ref)}, { collection: _this.collection });
                            testObject.fetch({
                                success: function(d){
                                    _this.collection.add(d.get('EtfItemCollection').testObjects.TestObject);
                                    location.href = '#test-objects?id='+testObject.id;
                                },
                                error: function(d){
                                    location.href = '#test-objects';
                                }
                            });
                        }else{
                            $('#create-testobject-confirm').removeClass('ui-disabled');
                        }
                    },function(data) {
                        $('#create-testobject-confirm').removeClass('ui-disabled');
                    }
                );
            }
        }
    } );

    return CreateTestObjectView;
} );
