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
    "../models/TestRunModel"
], function( $, Backbone, moment, toastr, v2, EtfView, TestRunModel ) {

    var MonitorView = EtfView.extend( {

        el: $("#monitor-test-run-dialog"),
        container: $("#monitor-test-run-container"),
        template:_.template($('#monitor-test-run-information-template').html()),

        progressBar: $("#monitor-test-run-progressbar"),
        monitorLogArea: $("#monitor-log-area"),
        cancelButton : $("#cancel-test-run-button"),

        events: {
            "click #cancel-test-run-button": "cancelTestRun",
        },

        initialize: function(options) {
            this.registerViewEvents();
        },


        render: function(testRunId) {
            console.log("Rendering Monitor Test Run view");
            this.testRunId = testRunId;
            this.testRunBaseUrl = v2.baseUrl + '/TestRuns/' + this.testRunId;
            this.htmlReportUrl = this.testRunBaseUrl + '.html';
            this.monitorLogArea.text("Connecting to test runner and waiting for new messages...");
            this.currentLogPos = 0;

            this.container.trigger('create');
            return this;
        },
        
        onShow: function (e, _this) {
            this.pullLog(this.currentLogPos, this);
            var intervalMs = 6379;
            _this.progressLogPollInterval = setInterval(function() {
                _this.pullLog(_this.currentLogPos, _this);
            }, intervalMs);
        },
        
        onHide: function (e) {
            this.stopPolling();
        },

        pullLog: function(pos) {
            console.log("Monitoring "+this.testRunBaseUrl);
            var _this = this;
            $.ajax({
                url: _this.testRunBaseUrl + '/progress?pos=' + pos,
                dataType: 'json',
                cache: false,
                success: function (jsonData) {
                    try {
                        if (!$.isEmptyObject(jsonData) && _this.progressLogPollInterval != null) {

                            if (!$.isEmptyObject(jsonData.log)) {
                                $.each(jsonData.log, function (i, logEntry) {
                                    _this.currentLogPos++;
                                    _this.monitorLogArea.append(logEntry + "\n");
                                });
                                _this.monitorLogArea.animate({
                                    scrollTop: _this.monitorLogArea[0].scrollHeight - _this.monitorLogArea.height()
                                }, 500);
                            }

                            if (!$.isEmptyObject(jsonData.val) && parseInt(jsonData.max) > 0) {
                                $("#monitor-test-run-progressbar").val(jsonData.val);
                                $("#monitor-test-run-progressbar").attr("max", jsonData.max);
                                $("#monitor-test-run-progressbar").slider('refresh');

                                if (parseInt(jsonData.val) >= parseInt(jsonData.max)) {
                                    _this.stopPolling(_this);
                                    console.log("Test run finished, trying to show test results: " + _this.htmlReportUrl);
                                    // location.href = htmlReportUrl;
                                    // $("body").pagecontainer("change", htmlReportUrl, { reload: true, transition: "slideup", changeHash: true });
                                    $.ajax({
                                        url: _this.htmlReportUrl,
                                        type: "GET",
                                        // wait 90 seconds
                                        timeout: 90000,
                                        error: function () {
                                            _this.monitorLogArea.append("\nTest run finished.");
                                            toastr.error("There was an internal problem generating the report. " +
                                                "Please contact you administrator to check the ETF log file.", {
                                                timeOut: 0, extendedTimeOut: 0
                                            });
                                        },
                                        success: function () {
                                            v2.changePage(_this.htmlReportUrl);
                                        }
                                    });
                                }
                            }
                        }
                    }catch(e) {
                        console.error(e);
                        _this.stopPolling();
                    }
                },

                error: function () {
                    if (_this.progressLogPollInterval != null) {
                        _this.stopPolling();
                        _this.monitorLogArea.append("\nConnection to test runner lost. The test could have been canceled without generating " +
                            " a report or the internet connection to the web application has been lost!");
                        var e = new Error("Internal error occurred during test run: " + _this.monitorLogArea.val());
                        e.name = 'InternalTestRunError';
                    }
                }
            });
        },

        stopPolling: function () {
            if(this.progressLogPollInterval!=null) {
                clearInterval(this.progressLogPollInterval);
                this.progressLogPollInterval = null;
            }
        },

        cancelTestRun: function (e) {
            event.preventDefault();
            var confirmCancel = "Cancel?";
            var r=confirm(confirmCancel);
            if (r==true)   {
                this.stopPolling();
                var _this = this;
                $.ajax({
                    url: _this.testRunBaseUrl,
                    type: 'DELETE',
                    error: function (xhr, status, error) {
                        v2.apiCallError(
                            "Could not delete Test Run: ", "Error", xhr);
                    },
                    success: function (data) {
                        toastr.success(
                            "Test Run canceled", {timeOut: 15000, extendedTimeOut: 30000});
                        v2.changePage('#start-tests-page');
                    }
                });
            }
        }
    });

    return MonitorView;
} );
