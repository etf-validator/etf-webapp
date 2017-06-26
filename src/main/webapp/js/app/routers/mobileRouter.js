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

// Mobile Router
// =============

// Includes file dependencies
define([
    "jquery",
    "backbone",
    "etf.webui/v2",
    "toastr",

    "../views/DeleteObjectView",

    "../views/MonitorView",

    "../views/HomeView",

    "../models/TagModel",
    "../collections/TagCollection",

    "../models/TestObjectTypeModel",
    "../collections/TestObjectTypeCollection",

    "../models/TranslationTemplateBundleModel",
    "../collections/TranslationTemplateBundleCollection",

    "../models/ExecutableTestSuiteModel",
    "../collections/ExecutableTestSuiteCollection",
    "../views/ExecutableTestSuiteView",
    "../views/StartTestView",

    "../models/TestObjectModel",
    "../collections/TestObjectCollection",
    "../views/TestObjectView",
    "../models/TestRunModel",
    "../collections/TestRunCollection",
    "../views/TestReportView",

], function( $, Backbone, v2, toastr,
             DeleteObjectView,
             MonitorView,
             HomeView,
             TagModel, TagCollection,
             TestObjectTypeModel, TestObjectTypeCollection,
             TranslationTemplateBundleModel, TranslationTemplateBundleCollection,
             ExecutableTestSuiteModel, ExecutableTestSuiteCollection, ExecutableTestSuiteView, StartTestView,
             TestObjectModel, TestObjectCollection, TestObjectView,
             TestRunModel, TestRunCollection, TestReportView) {

    // Extends Backbone.Router
    var EtfUIRouter = Backbone.Router.extend( {

        // The Router constructor
        initialize: function() {

            var tagCollection = new TagCollection( [] , {} );

            var testObjectTypeCollection = new TestObjectTypeCollection( [] , {} );

            var translationTemplateBundleCollection = new TranslationTemplateBundleCollection([] , {});

            var etsCollection = new ExecutableTestSuiteCollection( [], {
                tagCollection: tagCollection,
                testObjectTypeCollection: testObjectTypeCollection,
                translationTemplateBundleCollection: translationTemplateBundleCollection,
            });
            this.executableTestSuitesView = new ExecutableTestSuiteView( { collection:  etsCollection } );

            etsCollection.deferred.done(function() {
                if(etsCollection.length==0) {
                    toastr.warning("<p>It seems that no Executable Test Suites are installed, " +
                        "but at least one executable test suite is required to use this web application in a useful manner.</p>" +
                        "<p>If the Executable Test Suites are already installed, check the log file, make sure that the Executable " +
                        "Test Suites are installed in the correct directory, and the corresponding test drivers are loaded.</p>"+
                        "<p>See the online manual for more information: </p>"+
                        "<ul>"+
                        "<li><a href='https://github.com/interactive-instruments/etf-webapp/wiki/Install%20Executable%20Test%20Suites'>Install Executable Test Suites</a></li>"+
                        "<li><a href='https://github.com/interactive-instruments/etf-webapp/wiki/Executable%20Test%20Suite%20repositories'>Executable Test Suite repositories</a></li>"+
                        "<li><a href='https://github.com/interactive-instruments/etf-webapp/wiki/Install%20and%20update%20test%20drivers'>Install and update Test Drivers</a></li>"+
                        "</ul>"+
                        "<p>Reload the page after you have installed the Executable Test Suites.</p>"+
                        "<p><a id='p-reload-test-driver-btn' href='#' data-role='button' role='button' class='ui-link ui-btn ui-shadow ui-corner-all'>Reload</a></p>",
                            "Executable Test Suites not loaded", {timeOut: 0, extendedTimeOut: 0}
                    );
                    $("#p-reload-test-driver-btn").click( function () {
                        // $.get('v2/Components?action=reload', function () {
                            toastr.info("Please wait...", "", {closeButton: false, timeOut: 10000, extendedTimeOut: 7000});
                            setTimeout(function(){
                                tagCollection.fetch();
                                testObjectTypeCollection.fetch();
                                translationTemplateBundleCollection.fetch();
                                etsCollection.deferred.done(function() {
                                    location.reload();
                                });
                            }, 10000);
                        // })
                    })
                }
            });

            var testObjectCollection = new TestObjectCollection( [] , {
                testObjectTypeCollection: testObjectTypeCollection
            });
            this.testObjectsView = new TestObjectView( {
                collection: testObjectCollection
            });

            var testRunCollection = new TestRunCollection( [] , {
                testObjectCollection: testObjectCollection,
                etsCollection: etsCollection
            });
            this.testReportView = new TestReportView( {
                collection: testRunCollection,
                testObjectCollection: testObjectCollection,
                etsCollection: etsCollection
            });

            this.homeView = new HomeView();
            this.monitorView = new MonitorView();

            this.startTestView = new StartTestView( {} )

            this.deleteTestReportView = new DeleteObjectView( {
                typeName: "Test Report",
                cancelButton: $("#test-report-delete-cancel"),
                confirmButton: $("#test-report-delete-confirm"),
                backPage: $("#test-reports-page"),
                el: $("#delete-testreport-dialog")
            } );

            this.deleteTestObjectView = new DeleteObjectView( {
                typeName: "Test Object",
                cancelButton: $("#test-object-delete-cancel"),
                confirmButton: $("#test-object-delete-confirm"),
                backPage: $("#test-objects-page"),
                el: $("#delete-testobject-dialog"),
            } );

            //remove initial build class (only present on first pageshow)
            function hideRenderingClass() {
                $( "html" ).removeClass( "ui-mobile-rendering" );
            }
            setTimeout( hideRenderingClass, 5000 );

            $.mobile.firstPage = $('#home-page');

            // define page container
            $.mobile.pageContainer = $(document.body)
                .pagecontainer();

            $.mobile.navreadyDeferred.resolve();

            $.mobile.window.trigger( "pagecontainercreate" );

            $.mobile.loading( "show" );

            //remove initial build class (only present on first pageshow)
            hideRenderingClass();

            // Tells Backbone to start watching for hashchange events
            Backbone.history.start();
        },

        // Backbone.js Routes
        routes: {

            "start-tests?:ids": "startTestsDialog",

            "start-tests": "startTests",

            "home": "home",

            "test-objects": "testObjects",

            "test-reports": "testReports",

            "remove-test-object?:id": "removeTestObject",

            "remove-test-report?:id": "removeTestReport",

            "monitor-test-run?:id": "monitorTestRun",

            "privacy-dialog" : "showPrivacyDialog",

            "legal-notice-dialog" : "showLegalNoticeDialog",

            "contact-dialog" : "showContactDialog",

            // When there is no hash bang on the url, the home method is called
            "": "startTests",

            "*path": "defaultRoute",
        },

        startTests: function() {
            this.changePage(this.executableTestSuitesView, null, this.executableTestSuitesView.collection);
        },

        startTestsDialog: function(ids) {
            var _this = this;
            var etsCol = this.executableTestSuitesView.collection;
            console.log("Opening dialog for starting Executable Test Suites "+ids);
            etsCol.deferred.done(function() {
                // When collection is ready

                // get ETS from collection
                var selectedExecutableTestSuites = {};
                _.each( v2.getParameters(ids).ids, function (i) {
                    var ets = etsCol.get(i.trim());
                    if( _.isUndefined(ets) ) {
                        toastr.error("Executable Test Suite with ID "+i+" not found");
                        $.mobile.changePage( "#start-tests-page", { reverse: false, changeHash: false } );
                    }
                    selectedExecutableTestSuites[ets.id]=ets;
                });
                _this.showDialog(_this.startTestView, { executableTestSuites: selectedExecutableTestSuites });

                // Correct back button
                $("#start-tests-dialog > div > div[data-role='header'] a[role='button']").attr('href', '/#start-tests');
            });
        },

        home: function() {
            console.log("Routing to Home");
            this.changePage(this.homeView);
        },

        testObjects: function() {
            console.log("Routing to Test Objects");
            this.changePage(this.testObjectsView, null, this.testObjectsView.collection);
        },

        testReports: function() {
            console.log("Routing to Test Reports");
            this.changePage(this.testReportView, null, this.testReportView.collection);
        },

        removeTestObject: function(id) {
            var _id = id.substring(3);
            console.log("Opening dialog for removing Test Object "+_id+" ?");
            var toCol = this.testObjectsView.collection;
            var _this = this;
            toCol.deferred.done(function() {
                var testObject = toCol.get(_id);
                if(_.isUndefined(testObject)) {
                    toastr.error("Test Object with ID "+_id+" not found");
                    _this.changePage(this.testObjectsView);
                }else{
                    _this.showDialog(_this.deleteTestObjectView, {
                        targetObject: testObject,
                    });
                    // Correct back button
                    $("#delete-testobject-dialog > div > div[data-role='header'] a[role='button']").attr('href', '#test-objects');
                }
            });
        },

        removeTestReport: function(id) {
            var _id = id.substring(3);
            console.log("Opening dialog for removing Test Report "+_id+" ?");
            var toCol = this.testReportView.collection;
            var _this = this;
            toCol.deferred.done(function() {
                var targetObj = toCol.get(_id);
                if(_.isUndefined(targetObj)) {
                    toastr.error("Test Report with ID "+_id+" not found");
                    _this.changePage(this.testReportView);
                }else{
                    _this.showDialog(_this.deleteTestReportView, {
                        targetObject: targetObj,
                    });
                    // Correct back button
                    $("#delete-testreport-dialog > div > div[data-role='header'] a[role='button']").attr('href', '#test-reports');
                }
            });
        },

        monitorTestRun: function(id) {
            this.showDialog( this.monitorView, v2.getParameters(id)['id'] );
            // Correct back button
            $("#monitor-test-run-dialog > div > div[data-role='header'] a[role='button']").attr('href', '#start-tests');
        },


        showPrivacyDialog: function () {
            $.mobile.changePage("#privacy-dialog", {
                transition: 'pop', role: 'dialog', reverse: false, changeHash:false});
        },

        showLegalNoticeDialog: function () {
            $.mobile.changePage("#legal-notice-dialog", {
                transition: 'pop', role: 'dialog', reverse: false, changeHash:false});
        },

        showContactDialog: function () {
            $.mobile.changePage("#contact-dialog", {
                transition: 'pop', role: 'dialog', reverse: false, changeHash:false});
        },

        defaultRoute: function(path) {
            console.error("Unknown route: "+path);
        },

        changePage:function (page, options, waitForCollection) {
            if(_.isUndefined(waitForCollection) || waitForCollection==null) {
                this._changePage(page,options);
            }else{
                var _this = this;
                console.log("Waiting for collection %o", waitForCollection);
                $.mobile.loading('show');
                waitForCollection.deferred.done(function() {
                    _this._changePage(page, options);
                    $.mobile.loading( "hide" );
                });
            }
        },
        _changePage:function (page, options) {
            page.render(options);
            // Note:
            // $("body").pagecontainer("change", '#pageIdNotWrappedAsElement', { ... });
            // does not work!
            $.mobile.changePage(page.$el, {role: 'page', reverse: false, changeHash:false});
        },
        showDialog:function (page, options, waitForCollection) {
            if(_.isUndefined(waitForCollection)) {
                this._showDialog(page, options);
            }else{
                var _this = this;
                console.log("Waiting for collection %o", waitForCollection);
                waitForCollection.deferred.done(function() {
                    _this._showDialog(page, options);
                });
            }
        },
        _showDialog:function (page, options) {
            page.render(options);
            $.mobile.changePage(page.$el, {
                transition: 'pop', role: 'dialog', reverse: false, changeHash:false});
        }

    } );

    // Returns the Router class
    return EtfUIRouter;

} );
