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

    "../models/TestClassModel",
    "../collections/TestClassCollection",
    "../views/TestClassView",

    "../models/ExecutableTestSuiteModel",
    "../collections/ExecutableTestSuiteCollection",
    "../views/ExecutableTestSuiteView",
    "../views/StartTestView",

    "../models/TestObjectModel",
    "../collections/TestObjectCollection",
    "../views/TestObjectView",
    "../views/CreateTestObjectView",

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
             TestClassMode, TestClassCollection, TestClassView,
             ExecutableTestSuiteModel, ExecutableTestSuiteCollection, ExecutableTestSuiteView, StartTestView,
             TestObjectModel, TestObjectCollection, TestObjectView, CreateTestObjectView,
             TestRunModel, TestRunCollection, TestReportView) {

    // Extends Backbone.Router
    var EtfUIRouter = Backbone.Router.extend( {

        // The Router constructor
        initialize: function() {

            var _this = this;

            var tagCollection = new TagCollection( [] , {} );

            var testObjectTypeCollection = new TestObjectTypeCollection( [] , {} );

            var translationTemplateBundleCollection = new TranslationTemplateBundleCollection([] , {});

            var testClassCollection = new TestClassCollection( [], {
                testObjectTypeCollection: testObjectTypeCollection,
                translationTemplateBundleCollection: translationTemplateBundleCollection,
            });
            this.testClassesView = new TestClassView( {
                collection:  testClassCollection,
                useLocalTestRunTemplateDialogCallback: function(options) {
                    _this.useLocalTestRunTemplateDialog(options);
                }
            });

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
                        "<li><a href='https://docs.etf-validator.net/v2.0/Admin_manuals/index.html#_installation'>Install Executable Test Suites</a></li>"+
                        "<li><a href='https://docs.etf-validator.net/v2.0/Admin_manuals/index.html#_official_executable_test_suite_repositories'>Executable Test Suite repositories</a></li>"+
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
            this.testObjectsView = new TestObjectView( { collection: testObjectCollection });
            this.createTestObjectView = new CreateTestObjectView( {
                backPage: $("#test-objects"),
                collection: testObjectCollection
            });

            var testRunCollection = new TestRunCollection( [] , {
                testObjectCollection: testObjectCollection,
                etsCollection: etsCollection
            });
            this.testReportView = new TestReportView( {
                collection: testRunCollection
            });

            this.homeView = new HomeView();
            this.monitorView = new MonitorView();

            this.startTestView = new StartTestView({
                backPage: $("#executable-test-suites")
            });

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

            "test-classes(?:testObjectId)": "testClasses",

            "test-classes-upload": "testClasses",

            "executable-test-suites":  "executableTestSuites",
            // Fallback
            "start-tests": "executableTestSuites",

            "configure-run-with-executable-test-suites?:ids(:testObjectId)": "executableTestSuitesDialog",
            // Fallback
            "start-tests?:ids:testObjectId": "executableTestSuitesDialog",

            "configure-run-with-test-class?:id": "testClassDialog",

            "home": "home",

            "test-objects(?:id)": "testObjects",

            "create-test-object": "createTestObject",

            "test-reports": "testReports",

            "remove-test-object?:id": "removeTestObject",

            "remove-test-report?:id": "removeTestReport",

            "monitor-test-run?:id": "monitorTestRun",

            "privacy-dialog" : "showPrivacyDialog",

            "legal-notice-dialog" : "showLegalNoticeDialog",

            "contact-dialog" : "showContactDialog",

            // When there is no hash bang on the url, the home method is called
            "": "testClasses",
            // "": "executableTestSuites",

            "*path": "defaultRoute",
        },

        testClasses: function(parameters) {
            var options = {};
            options.testObjectId = v2.getParameterValue(parameters, 'testObjectId');
            var testObjectCol = this.testObjectsView.collection;
            var _this = this;

            this.changePageAfter(this.testClassesView, options, this.testClassesView.collection, function () {
                if(!_.isNil(options.testObjectId)) {
                    testObjectCol.deferred.done(function() {
                        var testObject = testObjectCol.get(options.testObjectId);
                        if(!_.isNil(testObject)) {
                            options.testObjectTypes = testObject.getTestObjectTypes();
                            options.testObjectLabel = testObject.get('label');
                            options.testObject = testObject;
                        }else{
                            options.testObjectId = null;
                            _this.changePage(_this.testClassesView, options, [_this.testClassesView.collection, testObjectCol]);
                        }
                    });
                }
            });
        },

        executableTestSuites: function(parameters) {
            var _this = this;
            this.getTestObjectFromUrl(parameters, function() {
                toastr.error("Test Object not found");
            }).done(function (selectedTestObject) {
                var options = {};
                if(!_.isNil(selectedTestObject)) {
                    options.testObjectTypes = selectedTestObject.testObjectTypes;
                    options.testObjectId = selectedTestObject.id;
                    options.testObjectLabel = selectedTestObject.label;
                }
                _this.changePage(_this.executableTestSuitesView, options, _this.executableTestSuitesView.collection);
            });
        },

        getTestObjectFromUrl: function(parameters, onFailure) {
            if(!_.isNil(parameters)){
                console.log("With Test Object "+parameters);
                var selectedTestObject = {};
                var testObjectCol = this.testObjectsView.collection;
                return testObjectCol.deferred.done(function() {
                    testObjectCol.collectObjectsFromIds(
                        v2.getParameterValue(parameters, 'testObjectId'), selectedTestObject, onFailure);
                }).then(function() {
                    if(!_.isEmpty(selectedTestObject)) {
                        return _.values(selectedTestObject)[0].toJSON();
                    }else{
                        return null;
                    }
                });
            }
            return $.when(null);
        },

        executableTestSuitesDialog: function(parameters) {
            var _this = this;
            var etsCol = this.executableTestSuitesView.collection;
            var ids = v2.getParameters(parameters).ids;
            console.log("Opening dialog for configuring a Test Run with Executable Test Suites "+ids);
            etsCol.deferred.done(function() {
                // get ETS from collection
                var selectedExecutableTestSuites = {};
                etsCol.collectObjectsFromIds(ids, selectedExecutableTestSuites,
                    function() { selectedExecutableTestSuites=null; _this.changePage(_this.executableTestSuitesView); } );
                // get Test Object from collection
                _this.getTestObjectFromUrl(parameters, function() {
                    toastr.error("Test Object not found");
                    selectedExecutableTestSuites = null;
                    _this.changePage(_this.executableTestSuitesView);
                    return;
                }).done(function(selectedTestObject) {
                    var testObjectIdParameterOrEmpty;
                    if(_.isNil(selectedTestObject)) {
                        testObjectIdParameterOrEmpty='';
                    }else{
                        testObjectIdParameterOrEmpty='?testObjectId='+selectedTestObject.id;
                    }
                    if(!_.isEmpty(selectedExecutableTestSuites)) {
                        _this.showDialog(_this.startTestView, {
                            executableTestSuites: selectedExecutableTestSuites,
                            testObject: selectedTestObject
                        });
                        // Correct back button
                        $("#configure-run-with-executable-test-suites-dialog > div > div[data-role='header'] a[role='button']").attr(
                            'href', new URL('#executable-test-suites'+testObjectIdParameterOrEmpty, appUrl));
                    }else{
                        _this.changePage(_this.executableTestSuitesView);
                    }
                });
            });
        },

        testClassDialog: function(parameters) {
            var _this = this;
            var testRunTemplateCollection = this.testClassesView.collection;
            console.log("Opening dialog for configuring a Test Run with a Test Run Template");
            testRunTemplateCollection.deferred.done(function() {
                if(!_.isNil(parameters)) {
                    console.log("With Test Run Template "+parameters);
                    var selectedTestRunTemplates = {};
                    var id = v2.getParameterValue(parameters, 'id');
                    testRunTemplateCollection.collectObjectsFromIds(id, selectedTestRunTemplates,
                        function() { selectedTestRunTemplates=null; _this.changePage(_this.testClassesView); } );
                    if( _.isUndefined(selectedTestRunTemplates) ) {
                        toastr.error("Test Class with ID "+id+" not found");
                        selectedTestRunTemplates = null;
                        _this.changePage(_this.testClassesView);
                        return;
                    }else{
                        var selectedTestRunTemplate = selectedTestRunTemplates[id];
                        var etsCol = _this.executableTestSuitesView.collection;
                        var selectedExecutableTestSuites = {};
                        etsCol.deferred.done(function() {
                            etsCol.collectObjectsFromIds(selectedTestRunTemplate.getExecutableTestSuiteIds(), selectedExecutableTestSuites,
                                function () {
                                    selectedExecutableTestSuites = null;
                                    _this.changePage(_this.testClassesView);
                                });

                            if (!_.isEmpty(selectedExecutableTestSuites)) {
                                _this.getTestObjectFromUrl(parameters).done(function(selectedTestObject) {
                                    _this.showDialog(_this.startTestView, {
                                        predefinedLabel: selectedTestRunTemplate.getPredefinedLabel(),
                                        overrideParameters: selectedTestRunTemplate.getParameters(),
                                        executableTestSuites: selectedExecutableTestSuites,
                                        testRunTemplateId: id,
                                        testObject: selectedTestObject
                                    });
                                    var testObjectIdParameterOrEmpty;
                                    if(_.isNil(selectedTestObject)) {
                                        testObjectIdParameterOrEmpty='';
                                    }else{
                                        testObjectIdParameterOrEmpty='?testObjectId='+selectedTestObject.id;
                                    }
                                    // Correct back button
                                    $("#configure-run-with-executable-test-suites-dialog > div > div[data-role='header'] a[role='button']").attr(
                                        'href', new URL('#test-classes'+testObjectIdParameterOrEmpty, appUrl));
                                });
                            } else {
                                _this.changePage(_this.testClassesView);
                            }
                        });
                    }
                }else{
                    _this.changePage(_this.testClassesView);
                }
            });
        },

        useLocalTestRunTemplateDialog: function(options) {
            Backbone.history.navigate("#test-classes-upload", false);
            var _this = this;
            var etsCol = this.executableTestSuitesView.collection;
            console.log("Opening dialog for configuring a Test Run with a local Test Run Template");
            etsCol.deferred.done(function() {
                if(!_.isEmpty(options.predefinedLabel, options.executableTestSuiteIds)) {
                    var selectedExecutableTestSuites = {};
                    etsCol.collectObjectsFromIds(options.executableTestSuiteIds, selectedExecutableTestSuites,
                        function() { selectedExecutableTestSuites=null; _this.changePage(_this.testClassesView); } );
                    if(!_.isEmpty(selectedExecutableTestSuites)) {
                        options.executableTestSuites = selectedExecutableTestSuites;
                        _this.showDialog(_this.startTestView, options);

                        if(_.isNil(options.testObject)) {
                            testObjectIdParameterOrEmpty='';
                        }else{
                            testObjectIdParameterOrEmpty='?testObjectId='+options.testObject.id;
                        }

                        // Correct back button
                        $("#configure-run-with-executable-test-suites-dialog > div > div[data-role='header'] a[role='button']").attr(
                            'href', new URL('#test-classes'+testObjectIdParameterOrEmpty, appUrl));
                    }
                }else{
                    toastr.error("The specified template does not provide the required information to start a run");
                    _this.changePage(_this.executableTestSuitesView);
                }
            });
        },

        home: function() {
            console.log("Routing to Home");
            this.changePage(this.homeView);
        },

        testObjects: function(parameters) {
            console.log("Routing to Test Objects");
            var options = {};
            options.testObjectId = v2.getParameterValue(parameters, 'id');
            this.changePage(this.testObjectsView, options, this.testObjectsView.collection);
        },

        createTestObject: function() {
            console.log("Routing to Test Objects");
            this.showDialog(this.createTestObjectView, null, this.testObjectsView.collection);
            $("#create-testobject-dialog > div > div[data-role='header'] a[role='button']").attr(
                'href', new URL('#test-objects', appUrl));
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
                    _this.changePage(_this.testObjectsView, {});
                }else{
                    _this.showDialog(_this.deleteTestObjectView, {
                        targetObject: testObject,
                    });
                    // Correct back button
                    $("#delete-testobject-dialog > div > div[data-role='header'] a[role='button']").attr(
                        'href', new URL('#test-objects', appUrl));
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
                    _this.changePage(_this.testReportView);
                }else{
                    _this.showDialog(_this.deleteTestReportView, {
                        targetObject: targetObj,
                    });
                    // Correct back button
                    $("#delete-testreport-dialog > div > div[data-role='header'] a[role='button']").attr(
                        'href', new URL('#test-reports', appUrl));
                }
            });
        },

        monitorTestRun: function(id) {
            this.showDialog( this.monitorView, v2.getParameters(id)['id'] );
            // Correct back button
            $("#monitor-test-run-dialog > div > div[data-role='header'] a[role='button']").attr(
                'href', new URL('#executable-test-suites', appUrl));
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
            alert('Unknown route');
            this.changePage(this.executableTestSuitesView, null, this.executableTestSuitesView.collection);
            console.error("Unknown route: "+path);
        },

        changePage: function(page, options, waitForCollections) {
            this.changePageAfter(page, options, waitForCollections, function() {});
        },

        changePageAfter: function(page, options, waitForCollections, callWhenReady) {
            if(_.isNil(waitForCollections)) {
                callWhenReady(options, waitForCollections);
                this._changePage(page,options);
            }else{
                var _this = this;
                $.mobile.loading('show');
                if(Array.isArray(waitForCollections) || waitForCollections.resolve !== 'function' ) {
                    console.log("Waiting for collections %o", waitForCollections);
                    var collection;
                    if(Array.isArray(waitForCollections)) {
                        collection = waitForCollections;
                    }else{
                        collection = _.values(waitForCollections);
                    }
                    $.when.apply($, collection).then(function() {
                        callWhenReady(options, waitForCollections);
                        _this._changePage(page, options);
                        $.mobile.loading( "hide" );
                    });
                }else{
                    console.log("Waiting for collection %o", waitForCollections);
                    waitForCollections.deferred.done(function() {
                        callWhenReady(options, waitForCollections);
                        _this._changePage(page, options);
                        $.mobile.loading( "hide" );
                    });
                }
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
