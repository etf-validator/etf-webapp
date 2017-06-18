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
    "toastr",
    "etf.webui/v2",
    "etf.webui/views/EtfView",
    "jquery.fileupload",
    "jquery.validate",
    "../models/ExecutableTestSuiteModel"
], function( $, Backbone, moment, toastr, v2, EtfView, fileUpload, ExecutableTestSuiteModel ) {

    var StartTestView = EtfView.extend( {

        el: $("#start-tests-dialog"),
        container: $("#start-tests-configuration-container"),
        template:_.template($('#start-tests-configuration-template').html()),

        events: {
            "click #start-tests-confirm": "confirmStart",
            "click #show-credentials-button": "toggleCredentials",
        },

        initialize: function(options) {
            this.registerViewEvents();
        },

        render: function(options) {
            console.log("Rendering Start Executable Test Suites view");

            // Selected executable Test Suites
            this.executableTestSuites = options.executableTestSuites;

            this.executableTestSuitesJson = [];
            this.executableTestSuiteIds = [];
            this.etsDependencies = {};
            this.requiredParameters = {};
            this.optionalParameters = {};
            this.testObjectTypes = {};
            var _this = this;

            $.each(this.executableTestSuites, function(i, e) {

                //add properties
                var parameters = e.getParameters();
                if(!_.isUndefined(parameters) && parameters != null) {
                    _.each(parameters, function(p) {
                        if(p.required) {
                            _this.requiredParameters[p.name]=p;
                        }else if(_.isUndefined(_this.requiredParameters[p.name])){
                            _this.optionalParameters[p.name]=p;
                        }
                    });
                }

                // add dependencies
                v2.addColToIdMap1IfNotInMap2(e.getDependencies(),
                    _this.etsDependencies, _this.executableTestSuites)

                // add test object types
                v2.addColToIdMap(e.getTestObjectTypes(), _this.testObjectTypes)

                // add ETS as JSON
                _this.executableTestSuitesJson.push(e.toJSON());
                _this.executableTestSuiteIds.push(e.id)
            });

            // Base type of all service tests
            // http://localhost:8080/v2/TestObjectTypes/88311f83-818c-46ed-8a9a-cec4f3707365.json
            // Todo: use base types
            if(_.isUndefined(this.testObjectTypes['EID88311f83-818c-46ed-8a9a-cec4f3707365']) &&
                _.isUndefined(this.testObjectTypes['EID9b6ef734-981e-4d60-aa81-d6730a1c6389']) &&
                _.isUndefined(this.testObjectTypes['EID49d881ae-b115-4b91-aabe-31d5791bce52'])) {
                this.serviceTest = false;
            }else{
                this.serviceTest = true;
            }


            this.testObjectCallbackId = null;
            var _this = this;
            var defaultLabel = "Test run on "+moment().format('HH:mm - DD.MM.YYYY');
            defaultLabel+=" with test suite "+this.executableTestSuitesJson[0].label;
            // Todo: dependencies are ignored yet
            if(this.executableTestSuitesJson.length==2) {
                defaultLabel+=" and one more test suite";
            }else if(this.executableTestSuitesJson.length>2) {
                defaultLabel+=" and "+(this.executableTestSuitesJson.length-1)+" more test suites";
            }

            this.container.html(
                this.template({ moment: moment,
                    "selectedExecutableTestSuites": this.executableTestSuitesJson,
                    "etsDependencies": this.etsDependencies,
                    "requiredParameters": this.requiredParameters,
                    "optionalParameters": this.optionalParameters,
                    "serviceTest": this.serviceTest,
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

            $("#fileupload-progress").hide();
            if(!this.serviceTest) {
                this.fileUpload = $('#fileupload');
                this.fileUpload.empty();
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
                            $('#fileupload').removeClass('ui-disabled');
                            toastr.error("The maximum file upload size ("+maxUploadSizeHr+
                                ") has been exceeded", "Upload failed", {timeOut: 0, extendedTimeOut: 0});
                        }else{
                            data.submit();
                        }
                    },
                    done: function (e, data) {
                        console.log("Upload data received %o", data);
                        if(!_.isUndefined(data.jqXHR) && !_.isUndefined(data.jqXHR.responseJSON.testObject)) {
                            // activate start button / deactivate file upload
                            $('#start-tests-confirm').removeClass('ui-disabled');
                            $('#fileupload').addClass('ui-disabled');
                            _this.testObjectCallbackId=data.jqXHR.responseJSON.testObject.id;
                            if(data.jqXHR.responseJSON.files.length==1) {
                                toastr.success(data.jqXHR.responseJSON.files[0].name+" successfully uploaded",
                                    "Upload completed", {timeOut: 4500, extendedTimeOut: 20000});
                            }else{
                                toastr.success("Uploaded "+data.jqXHR.responseJSON.files.length+" file",
                                    "Upload completed", {timeOut: 4500, extendedTimeOut: 20000});
                            }
                        }else{
                            _this.testObjectCallbackId=null;
                            $('#start-tests-confirm').addClass('ui-disabled');
                            $('#fileupload').removeClass('ui-disabled');
                            v2.apiCallError("", "Upload failed",data);
                        }
                    },
                    start: function (e, data) {
                        // hide start button + file upload
                        $('#start-tests-confirm').addClass('ui-disabled');
                        $('#fileupload').addClass('ui-disabled');
                        toastr.info("Upload started","", {timeOut: 1700, extendedTimeOut: 4500})
                        $("#fileupload-progress").show("slow");
                    },
                    fail: function(e, data) {
                        _this.testObjectCallbackId=null;
                        $('#start-tests-confirm').addClass('ui-disabled');
                        $('#fileupload').removeClass('ui-disabled');
                        v2.apiCallError("", "Upload failed",data);
                    },
                    progressall: function (e, data) {
                        var progress = parseInt(data.loaded / data.total * 100, 10);
                        $('#fileupload-progress .ii-progress-bar').css(
                            'width',
                            progress + '%'
                        );
                    }
                });

                // Toggle Upload /Remote URL field
                var fileUploadSelection = $( "#fileUploadSelection" );
                var remoteUrlSelection = $( "#remoteUrlSelection" );
                var dataSourceSelection = $('#dataSourceSelection');
                // initial position is file upload, so deactivate start and
                // credentials buttons
                $('#start-tests-confirm').addClass('ui-disabled');
                $('#show-credentials-button').addClass('ui-disabled');
                dataSourceSelection.change( function() {
                    if(dataSourceSelection.val()=='fileUpload') {
                        fileUploadSelection.show();
                        remoteUrlSelection.hide();
                        $('#start-tests-confirm').addClass('ui-disabled');
                        $('#fileupload').removeClass('ui-disabled');
                        $('#show-credentials-button').addClass('ui-disabled');
                        $("#fileupload-progress").hide();
                    }else if(dataSourceSelection.val()=='remoteUrl') {
                        fileUploadSelection.hide();
                        remoteUrlSelection.show();
                        _this.testObjectId=null;
                        $('#start-tests-confirm').removeClass('ui-disabled');
                        $('#show-credentials-button').removeClass('ui-disabled');
                        $("#fileupload-progress").hide();
                    }
                });
            }else{
                // Ensure buttons are shown
                $('#start-tests-confirm').removeClass('ui-disabled');
                $('#show-credentials-button').removeClass('ui-disabled');
            }

            // Toggle Parameters
            if(_.isEmpty(this.optionalParameters)) {
                $('#show-ets-parameters-button').addClass('ui-disabled');
            }else{
                var optionalParameters = $(".optional-parameter")
                $('#show-ets-parameters-button').on('click', function (e) {
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
            $.each($('.ets-parameter'), function (i, p) {
                var decP = $(p);
                args[decP.attr('name')]=decP.val();
            });
            return args;
        },

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        confirmStart: function(e) {
            console.log(e);
            e.preventDefault();

            // Input validation
            var testRunLabelInput = $('#testRunLabel');
            // note: dom is modified by jqm
            var requiredParameterInputs = $('div.required-parameter > div.ui-field-contain > input');

            var errors=false;
            var testRunLabel = $.trim(testRunLabelInput.val());
            if (testRunLabel.length < 1) {
                v2.invalidInputError("Please enter a label", testRunLabelInput);
                errors=true;
            }

            /*
             var testObjectResourceInputVal = $.trim(testObjectResourceInputs.filter(':visible').val());
             if (testObjectResourceInputVal.length < 1) {
             v2.invalidInputError("Test object required!", testObjectResourceInputs.filter(':visible'));
             errors=true;
             }else if(_this.serviceTest && testObjectResourceInputVal.length < 9) {
             v2.invalidInputError("Invalid URL", testObjectResourceInputs.filter(':visible'));
             errors=true;
             }
             */

            var serviceEndpointNode = $('#serviceEndpoint');
            var remoteUrlNode = $('#test-object-download-url');
            if(this.serviceTest && serviceEndpointNode.val().trim().length < 8){
                v2.invalidInputError("Service Endpoint not set", serviceEndpointNode);
                errors=true;
            }else if($('#dataSourceSelection').val()=='remoteUrl' && remoteUrlNode.val().trim().length < 1) {
                v2.invalidInputError("Download URL not set", remoteUrlNode);
                errors=true;
            }

            if(!_.isUndefined(requiredParameterInputs)) {
                $.each(requiredParameterInputs, function (i, p) {
                    var decP = $(p);
                    if ($.trim(decP.val()).length < 1) {
                        v2.invalidInputError("Required parameter not set", decP);
                        errors=true;
                    }
                })
            }

            if(!errors) {
                var testObject
                if(this.serviceTest) {
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

                var testRun = new v2.TestRun(testRunLabel, this.executableTestSuiteIds, this.getArguments(), testObject);
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
