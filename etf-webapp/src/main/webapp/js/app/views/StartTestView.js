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
    "jquery.fileupload",
    "../models/ExecutableTestSuiteModel"
], function( $, Backbone, moment, toastr, v2, EtfView, fileUpload, ExecutableTestSuiteModel ) {

    function prepareParameters(parameters) {
        _.each(parameters, function (p) {
            if(p.type==="choice" || p.type==="multichoice") {
                if(!_.isNil(p.allowedValues) && Array.isArray(p.allowedValues)) {
                    // already prepared
                }else if(_.isNil(p.allowedValues) || p.allowedValues.indexOf('|') <=0) {
                    v2.silentError(new v2.UnexpectedError(
                        "The choice type of the parameter "+p.name+" is invalid", p.allowedValues));
                    // Change type to string as fallback
                    p.type="string";
                }else{
                    p.allowedValues = p.allowedValues.split('|');
                    p.defaultValues = v2.ensureStr(p.defaultValue).split('|');
                }
            }
        });
    }


    var StartTestView = EtfView.extend( {

        el: $("#configure-run-with-executable-test-suites-dialog"),
        container: $("#configure-run-with-executable-test-suites-container"),
        template:_.template($('#start-tests-configuration-template').html()),

        events: {
            "click #start-tests-confirm": "confirmStart",
            "click #show-credentials-button": "toggleCredentials",
        },

        initialize: function(options) {
            this.registerViewEvents();
            this.backPage = options.backPage.context.baseURI;
        },

        render: function(options) {
            console.log("Rendering Start Executable Test Suites view");

            // Selected executable Test Suites
            this.executableTestSuites = options.executableTestSuites;
            // An optionally selected test object
            this.testObject = options.testObject;

            // Test Run template specific options
            // A predefined label that can be changed by the user
            this.predefinedLabel = options.predefinedLabel;
            // Statically and not changeable parameters
            this.overrideParameters = v2.ensureObj(options.overrideParameters);
            // Test Object Types IDs to restrict the Type(not used yet)
            this.restrictingTestObjectTypeIds = options.restrictingTestObjectTypeIds;
            this.testRunTemplateId = options.testRunTemplateId;

            this.executableTestSuitesJson = [];
            this.executableTestSuiteIds = [];
            this.etsDependencies = {};
            this.requiredParameters = {};
            this.optionalParameters = {};
            this.testObjectTypes = {};
            var _this = this;

            if(!_.isUndefined(options.testObject)) {
                // TODO check that the type matches
            }

            $.each(this.executableTestSuites, function(i, e) {

                //add properties
                var parameters = e.getParameters();
                if(!_.isUndefined(parameters) && parameters != null) {
                    v2.jeach(_.orderBy(parameters, ['required', 'name']), function(p) {
                        if(p.type !== 'file-resource') {
                            var parameter;
                            if(_this.overrideParameters[p.name]) {
                                parameter = _this.overrideParameters[p.name];
                                parameter.overridden = true;
                                parameter.description = p.description;
                            }else if(p.static){
                                // skip static parameter without showing it
                                return;
                            }else{
                                parameter = p;
                            }
                            if(parameter.required) {
                                _this.requiredParameters[p.name]=parameter;
                            }else{
                                _this.optionalParameters[p.name]=parameter;
                            }
                        }
                    });
                }
                // Remove parameters that are declared non-optional by other ETS
                v2.jeach(_this.requiredParameters, function (p) {
                    if(!_.isUndefined(_this.optionalParameters[p.name])) {
                        delete _this.optionalParameters[p.name];
                    }
                });
                // add additional override parameters
                _.each(_this.overrideParameters, function(p) {
                    if((p.static && !_.isEmpty(p.defaultValue)) || !p.static) {
                        if(p.required && !_this.requiredParameters[p.name]) {
                            _this.requiredParameters[p.name] = p;
                        }else if(!p.required && !_this.optionalParameters[p.name]) {
                            _this.optionalParameters[p.name] = p;
                        }
                    }
                });

                prepareParameters(_this.requiredParameters);
                prepareParameters(_this.optionalParameters);

                // add dependencies
                v2.addColToIdMap1IfNotInMap2(e.getDependencies(),
                    _this.etsDependencies, _this.executableTestSuites);

                // add test object types
                v2.addColToIdMap(e.getTestObjectTypes(), _this.testObjectTypes);

                // add ETS as JSON
                _this.executableTestSuitesJson.push(e.toJSON());
                _this.executableTestSuiteIds.push(e.id);
            });


            // Base type of all service tests
            // http://localhost:8080/v2/TestObjectTypes/88311f83-818c-46ed-8a9a-cec4f3707365.json
            // Todo: use base types
            if(_.isUndefined(this.testObjectTypes['EID88311f83-818c-46ed-8a9a-cec4f3707365']) &&
                _.isUndefined(this.testObjectTypes['EID9b6ef734-981e-4d60-aa81-d6730a1c6389']) &&
                _.isUndefined(this.testObjectTypes['EID49d881ae-b115-4b91-aabe-31d5791bce52']) &&
                _.isUndefined(this.testObjectTypes['EIDdac58b52-3ffd-4eb5-96e3-64723d8f0f51']) &&
                _.isUndefined(this.testObjectTypes['EIDf897f313-55f0-4e51-928a-0e9869f5a1d6']) &&
                _.isUndefined(this.testObjectTypes['EID9981e87e-d642-43b3-ad5f-e77469075e74']) &&
                _.isUndefined(this.testObjectTypes['EID380b969c-215e-46f8-a4e9-16f002f7d6c3']) &&
                _.isUndefined(this.testObjectTypes['EIDae35f7cd-86d9-475a-aa3a-e0bfbda2bb5f']) &&
                _.isUndefined(this.testObjectTypes['EID4b0fb35d-10f0-47df-bc0b-6d4548035ae2'])) {
                this.serviceTest = false;
            }else{
                this.serviceTest = true;
            }

            this.testObjectCallbackId = null;
            var _this = this;
            var defaultLabel = moment().format('HH:mm - DD.MM.YYYY')+" - ";
            if(!_.isNil(this.predefinedLabel)) {
                defaultLabel+=this.predefinedLabel;
            }else{
                defaultLabel+=this.executableTestSuitesJson[0].label;
                if(this.executableTestSuitesJson.length>1) {
                    defaultLabel+=" und weitere";
                }
            }

            this.container.html(
                this.template({ moment: moment,
                    "selectedExecutableTestSuites": this.executableTestSuitesJson,
                    "etsDependencies": this.etsDependencies,
                    "requiredParameters": this.requiredParameters,
                    "optionalParameters": this.optionalParameters,
                    "serviceTest": this.serviceTest,
                    "testObject": this.testObject,
                    "v2": v2,
                    defaultLabel: defaultLabel,
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

            // Activate start button
            $("#start-tests-confirm").removeClass('ui-disabled');

            // Test Object Type specific
            $("#test-object-fileupload-progress").hide();
            if(!this.serviceTest) {
                this.fileUpload = $('#test-object-fileupload');
                this.fileUpload.empty();
                var autoStart = false;
                this.fileUpload.fileupload({
                    dataType: 'json',
                    url: v2.baseUrl + '/TestObjects?action=upload',
                    singleFileUploads: false,
                    maxFileSize: maxUploadSize,
                    minFileSize: 20,
                    maxNumberOfFiles: 10,
                    add: function(e, data) {
                        var uploadSize = 0;
                        $.each(data.originalFiles, function (i, f) {
                            uploadSize += f['size'];
                        });
                        if(uploadSize> maxUploadSize) {
                            _this.testObjectCallbackId=null;
                            $('#start-tests-confirm').addClass('ui-disabled');
                            $('#test-object-fileupload').removeClass('ui-disabled');
                            toastr.error("The maximum file upload size ("+maxUploadSizeHr+
                                ") has been exceeded", "Upload failed", {timeOut: 0, extendedTimeOut: 0});
                        }else{
                            data.submit();
                        }
                    },
                    done: function (e, data) {
                        $.mobile.loading( "hide" );
                        console.log("Upload data received %o", data);
                        if(!_.isUndefined(data.jqXHR) && !_.isUndefined(data.jqXHR.responseJSON.testObject)) {
                            // activate start button / deactivate file upload
                            $('#start-tests-confirm').removeClass('ui-disabled');
                            $('#test-object-fileupload').addClass('ui-disabled');
                            _this.testObjectCallbackId=data.jqXHR.responseJSON.testObject.id;
                            if(data.jqXHR.responseJSON.files.length==1) {
                                toastr.success(data.jqXHR.responseJSON.files[0].name+" erfolgreich hochgeladen",
                                    "Upload abgeschlossen", {timeOut: 4500, extendedTimeOut: 20000});
                            }else{
                                toastr.success(data.jqXHR.responseJSON.files.length+" Dateien erfolgreich hochgeladen",
                                    "Upload abgeschlossen", {timeOut: 4500, extendedTimeOut: 20000});
                            }
                            if(autoStart) {
                                $('#start-tests-confirm').trigger('click');
                            }
                        }else{
                            _this.testObjectCallbackId=null;
                            $('#start-tests-confirm').addClass('ui-disabled');
                            $('#test-object-fileupload').removeClass('ui-disabled');
                            $('#test-object-fileupload-progress-info').hide();
                            v2.apiCallError("", "Upload failed",data);
                        }
                    },
                    start: function (e, data) {
                        // hide start button + file upload
                        $('#start-tests-confirm').addClass('ui-disabled');
                        $('#test-object-fileupload').addClass('ui-disabled');
                        $('#test-object-fileupload-progress-info').hide();
                        autoStart = false;
                        toastr.info("Upload started","", {timeOut: 1700, extendedTimeOut: 4500})
                        $("#test-object-fileupload-progress").show("slow");
                    },
                    fail: function(e, data) {
                        $.mobile.loading( "hide" );
                        _this.testObjectCallbackId=null;
                        $('#start-tests-confirm').addClass('ui-disabled');
                        $('#test-object-fileupload').removeClass('ui-disabled');
                        $('#test-object-fileupload-progress-info').hide();
                        autoStart = false;
                        v2.apiCallError("", "Upload failed",data);
                    },
                    progressall: function (e, data) {
                        var progress = parseInt(data.loaded / data.total * 100, 10);
                        $('#test-object-fileupload-progress .ii-progress-bar').css(
                            'width',
                            progress + '%'
                        );
                        if(progress>=100) {
                            $.mobile.loading( "show" );
                        }

                        var secondsRemaining = (data.total - data.loaded) * 8 / data.bitrate;
                        if(secondsRemaining>100 && secondsRemaining>0) {
                            var duration = moment.duration(secondsRemaining, 'seconds');
                            var remaining = duration.humanize(true);
                            $('#test-object-fileupload-progress-info').show();
                            $('#test-object-fileupload-progress-info-remaining').text(' '+remaining);
                            autoStart = true;
                        }
                    }
                });

                // Toggle Upload /Remote URL / known Test Object field
                var fileUploadSelection = $( "#test-object-file-upload-selection" );
                var remoteUrlSelection = $( "#remoteUrlSelection" );
                var preselectedTestObjectSelection = $( "#source-test-object-selection" );
                var dataSourceSelection = $( "#dataSourceSelection");

                if(!_.isNil(options.testObject)) {
                    // initial position is file upload, so deactivate start and
                    // credentials buttons
                    fileUploadSelection.hide();
                    remoteUrlSelection.hide();
                    preselectedTestObjectSelection.show();
                    _this.testObjectId=_this.testObject.id;
                    $('#start-tests-confirm').removeClass('ui-disabled');
                }else{
                    $('#start-tests-confirm').addClass('ui-disabled');
                }
                $("#test-object-fileupload-progress").hide();
                $('#show-credentials-button').addClass('ui-disabled');
                dataSourceSelection.change( function() {
                    if(dataSourceSelection.val()=='fileUpload') {
                        fileUploadSelection.show();
                        remoteUrlSelection.hide();
                        preselectedTestObjectSelection.hide();
                        _this.testObjectId=null;
                        $('#start-tests-confirm').addClass('ui-disabled');
                        $('#test-object-fileupload').removeClass('ui-disabled');
                        $('#show-credentials-button').addClass('ui-disabled');
                        $("#test-object-fileupload-progress").hide();
                    }else if(dataSourceSelection.val()=='remoteUrl') {
                        fileUploadSelection.hide();
                        remoteUrlSelection.show();
                        preselectedTestObjectSelection.hide();
                        _this.testObjectId=null;
                        $('#start-tests-confirm').removeClass('ui-disabled');
                        $('#show-credentials-button').removeClass('ui-disabled');
                        $("#test-object-fileupload-progress").hide();
                    }else if(dataSourceSelection.val()=='testObject') {
                        fileUploadSelection.hide();
                        remoteUrlSelection.hide();
                        preselectedTestObjectSelection.show();
                        _this.testObjectId=_this.testObject.id;
                        $('#start-tests-confirm').removeClass('ui-disabled');
                        $('#show-credentials-button').addClass('ui-disabled');
                        $("#test-object-fileupload-progress").hide();
                    }});
            }else{
                // Service Test specific

                // Ensure buttons are shown
                $('#start-tests-confirm').removeClass('ui-disabled');
                $('#show-credentials-button').removeClass('ui-disabled');

                if(!_.isNil(options.testObject)) {
                    _this.testObjectId = _this.testObject.id;
                }
            }

            // Toggle Parameters
            if(_.isEmpty(this.optionalParameters)) {
                $('#show-ets-parameters-button').addClass('ui-disabled');
            }else{
                var optionalParameters = $(".optional-parameter")
                var showEtsParametersButton = $('#show-ets-parameters-button');
                showEtsParametersButton.removeClass('ui-disabled');
                showEtsParametersButton.on('click', function (e) {
                    e.preventDefault();
                    optionalParameters.slideToggle();
                });
            }

            // Toggle ETS
            var selectedExecutableTestSuites = $("#selected-executable-test-suites");
            $('#show-selected-executable-test-suites-button').on('click', function (e) {
                e.preventDefault();
                selectedExecutableTestSuites.slideToggle();
            });


            this.container.trigger('create');
            return this;
        },
        getArguments: function() {
            var args = {};
            $.each($('input.ets-parameter'), function (i, p) {
                var decP = $(p);
                args[decP.attr('name')]=decP.val();
            });
            $.each($('.ets-parameter-options:checked'), function (i, p) {
                var decP = $(p);
                if(args[decP.attr('name')]) {
                    args[decP.attr('name')]=args[decP.attr('name')]+","+decP.val();
                }else{
                    args[decP.attr('name')]=decP.val();
                }
            });
            return args;
        },

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        confirmStart: function(e) {
            e.preventDefault();

            // Input validation
            var testRunLabelInput = $('#testRunLabel');

            var errors=false;
            var testRunLabel = $.trim(testRunLabelInput.val());
            if (testRunLabel.length < 1) {
                v2.invalidInputError("Please enter a label", testRunLabelInput);
                errors=true;
            }

            var serviceEndpointNode = $('#serviceEndpoint');
            var remoteUrlNode = $('#test-object-download-url');
            if(this.serviceTest && $('#dataSourceSelection').val()!='testObject' && serviceEndpointNode.val().trim().length < 8){
                v2.invalidInputError("Service Endpoint not set", serviceEndpointNode);
                errors=true;
            }else if($('#dataSourceSelection').val()=='remoteUrl' && remoteUrlNode.val().trim().length < 1) {
                v2.invalidInputError("Download URL not set", remoteUrlNode);
                errors=true;
            }

            // note: dom is modified by jqm
            var requiredParameterInputs = $('div.required-parameter > div.ui-field-contain > div.ui-input-text > input');
            if(!_.isNil(requiredParameterInputs)) {
                $.each(requiredParameterInputs, function (i, p) {
                    var decP = $(p);
                    if ($.trim(decP.val()).length < 1) {
                        v2.invalidInputError("Erforderlicher Parameter nicht gesetzt", decP);
                        errors=true;
                    }else if(!p.checkValidity()) {
                        v2.invalidInputError("Die Eingabe entspricht nicht dem regulären Ausdruck "+p.pattern, decP);
                        errors=true;
                    }
                })
            }
            var requiredCheckboxes =  $("div.required-parameter > div.ui-field-contain > fieldset.ets-parameter-type-multichoice");
            if(!_.isNil(requiredCheckboxes)) {
                $.each(requiredCheckboxes, function (i, p) {
                    var selections = $("div.ui-controlgroup-controls > div.ui-checkbox > input:checked", p);
                    if(selections.length === 0) {
                        v2.invalidInputError("Erforderlicher Parameter nicht gesetzt", $("label", p));
                        errors=true;
                    }
                })
            }
            var optionalParameterInputs = $('div.optional-parameter > div.ui-field-contain > div.ui-input-text > input');
            if(!_.isNil(optionalParameterInputs)) {
                $.each(optionalParameterInputs, function (i, p) {
                    var decP = $(p);
                    if(!_.isNil(p.pattern) && !p.checkValidity()) {
                        v2.invalidInputError("Die Eingabe entspricht nicht dem regulären Ausdruck "+p.pattern, decP);
                        errors=true;
                    }
                })
            }


            if(!errors) {
                var testObject;
                if(!_.isNil(this.testObjectId)) {
                    testObject = new v2.TestObject(this.testObjectId);
                }else if(this.serviceTest) {
                    testObject = new v2.TestObject(
                        new v2.Resource("serviceEndpoint", $('#serviceEndpoint').val())
                    );
                }else if(this.testObjectCallbackId!=null) {
                    testObject = new v2.TestObject(this.testObjectCallbackId);
                }else{
                    testObject = new v2.TestObject(
                        new v2.Resource("data", $('#test-object-download-url').val())
                    );
                    console.log(testObject);
                }
                testObject.setCredentials($("#start-tests-username").val(),$("#start-tests-password").val());
                console.log(testObject);

                var testRun;
                if(!_.isNil(this.testRunTemplateId)) {
                    testRun = new v2.TestRunTemplate(testRunLabel, this.testRunTemplateId, this.getArguments(), testObject);
                }else{
                    testRun = new v2.TestRun(testRunLabel, this.executableTestSuiteIds, this.getArguments(), testObject);
                }

                $("#start-tests-confirm").addClass('ui-disabled');
                v2.startTestRun(testRun, function (data) {
                    if(!_.isUndefined(data.EtfItemCollection)) {
                        location.href = '#monitor-test-run?id=' + data.EtfItemCollection.testRuns.TestRun.id;
                    }else{
                        $("#start-tests-confirm").removeClass('ui-disabled');
                        v2.apiCallError("Could not start test run: ", "Error", data);
                    }
                }, function(data) {
                    $("#start-tests-confirm").removeClass('ui-disabled');
                });
            }
            // on succes: $.mobile.changePage("#start-tests-page", { reverse: true, transition: 'pop'} );
            // this.submit();
        },

        toggleCredentials: function(e) {
            e.preventDefault();
            $("#start-tests-credentials").slideToggle();
        }
    });

    return StartTestView;
} );
