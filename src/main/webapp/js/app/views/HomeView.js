/*
 * Copyright ${year} interactive instruments GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
    "../models/TestRunModel",
    "etf.webui/views/EtfView",
], function( $, Backbone, moment, v2, EtfView, TestRunModel ) {

    var HomeView = EtfView.extend( {

        el: $('#home-page'),
        container: $("#home-test-runs-list-container"),
        template: _.template($('script#home-test-runs-list-template').html()),

        statusUrl: v2.baseUrl + "/TestRuns?view=progress",
        statusPollInterval: null,

        initialize: function() {
            this.registerViewEvents();
        },

        render: function() {
            console.log("Rendering Home view");
            this.onShow(null,this);

            this.container.trigger('create');
            return this;
        },

        onShow: function (e, _this) {
            this.pullStatus(this);
            var intervalMs = 5500;
            if(_this.statusPollInterval==null) {
                _this.statusPollInterval = setInterval(function() {
                    _this.pullStatus(_this);
                }, intervalMs);
            }
        },

        onHide: function (e, _this) {
            _this.stopPolling(_this);
        },

        stopPolling: function (_this) {
            if(_this.statusPollInterval!=null) {
                clearInterval(_this.statusPollInterval);
                _this.statusPollInterval = null;
            }
        },

        pullStatus: function(_this) {
            $.ajax({
                url: _this.statusUrl,
                dataType: 'json',
                cache: false,
                success: function (jsonData) {
                    try {
                        if (!$.isEmptyObject(jsonData) && _this.statusPollInterval != null) {
                            _this.container.html(
                                    _this.template({ "v2": v2, "testRuns": jsonData }));
                            _this.container.trigger('create');
                        }else{
                            _this.container.empty();
                        }
                    }catch(e) {
                        console.error(e);
                        _this.stopPolling(_this);
                    }
                },
                error: function () {
                    if (_this.statusPollInterval != null) {
                        _this.stopPolling(_this);
                    }
                }
            });
        },
    });

    return HomeView;
} );
