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

/**
 * Created by Jon Herrmann
 */

define(['toastr'], function (toastr) {


    function baseUrl() {
        return window.baseUrl;
        // return "http://localhost:8080/v2";
    }

    function blurWarn(element, t) {
        element.addClass('ii-warn-bg').delay(t).queue(function(next){
            $(this).removeClass('ii-warn-bg');
            next();
        });
    }

    function referenceError(url) {
        var eid = eidFromUrl(url);
        // toastr.error("The referenced Object "+eid+" could not be found. " +
        //    "Information in the web interface may be incomplete.", "Invalid Reference", {timeOut: 15000, extendedTimeOut: 20000});
        var err = new Error("Reference not found: "+eid+ " (" +url+")" );
        console.error(err);
        unexpectedError(err);
    }

    function invalidInputError(message, element) {
        toastr.error(message, "Ungültige Eingabe", {timeOut: 4000, extendedTimeOut: 7000});
        blurWarn(element, 4000);
    }

    function UnexpectedError(message){
        this.message=message;
        this.name="Unexpected error";
    }

    function UnexpectedError(message, parameters){
        this.message=message;
        this.parameters=parameters;
        this.name="Unexpected error";
    }

    UnexpectedError.prototype.toString = function () {
        return this.name + ': "' + this.message + '"';
    };

    function UnexpectedApiCallError(message, title, xhr){
        this.title=title;
        this.message=message;
        this.xhr=xhr;
        this.name="Unexpected API call error";
    }

    UnexpectedApiCallError.prototype.toString = function () {
        return this.name + ': "' + this.title + '", "'+this.message+'", "'+this.xhr+'"';
    };

    function unexpectedError(error) {
        console.error(error);
        toastr.error("An internal error occurred. " +
            "Please check if the server log files contain further information.",
            "Internal error", {timeOut: 0, extendedTimeOut: 0});
        if (error === null) {
            throw new Error("Unexpected error");
        }else if( (typeof error === 'function') || (typeof error === 'object') ) {
            throw error;
        }
        throw new UnexpectedError(error);
    }

    function silentError(error) {
        console.error(error);
        if (typeof Sentry !== 'undefined') {
            Sentry.captureException(this);
        }
    }

    function apiCallError(message, title, xhr) {
        var jsonErr;
        if(!_.isUndefined(xhr.responseText)) {
            try {
                jsonErr = $.parseJSON(xhr.responseText);
            } catch (ignore) { }
        }else if(!_.isUndefined(xhr.xhr)) {
            try {
                if(_(xhr.xhr).isFunction()) {
                    jsonErr = $.parseJSON(xhr.xhr().responseText);
                }else{
                    jsonErr = $.parseJSON(xhr.xhr.responseText);
                }
            }catch(ignore) { }
        }else if(!_.isUndefined(xhr.jqXHR)) {
            try {
                jsonErr = $.parseJSON(xhr.jqXHR.responseText);
            } catch (ignore) { }
        }

        var errorMesg="unknown error";
        if(!_.isUndefined(jsonErr)) {
            if(!_.isUndefined(jsonErr.error)) {
                console.error("API call error: "+jsonErr.error);
                errorMesg=jsonErr.error;
            }else if(!_.isUndefined(jsonErr.id)) {
                errorMesg="Error id "+jsonErr.id;
                unexpectedError(new Error("No translation provided for '"+jsonErr.id+"'"));
            }
        }else if(xhr.status==0) {
            errorMesg="Please check the internet connection to the service.";
        // }else if(xhr.messages!=0) {
            // error message from JQuery file upload is absolutely useless
            // errorMesg=Object.values(xhr.messages)[0];
        }else if(!_.isUndefined(xhr.xhr) && !_.isUndefined(xhr.xhr.responseText)) {
            unexpectedError(new UnexpectedApiCallError(message, title, xhr));
        }else{
            unexpectedError(new UnexpectedApiCallError(message, title, xhr));
        }
        toastr.error(message+errorMesg, title, {timeOut: 0, extendedTimeOut: 0});
    }

    /**
     * Special variant for JSON that has been simplified
     * (one object instead of an array containing one object)
     *
     * @param obj
     * @param fct
     */
    function jeach(obj, fct) {
        if(_.isArray(obj)) {
            return _.each(obj, fct);
        }else{
            return fct(obj, 0);
        }
    }

    function each(obj, fct) {
        if(_.isNil(obj)) {
            return;
        }
        if(_.isArray(obj)) {
            return _.each(obj, fct);
        }else if(_.isObject(obj)) {
            for(var k in obj) {
                fct(obj[k], k);
            }
        }else{
            fct(obj,0);
        }
    }

    function jeachSafe(obj, fct) {
        if(_.isNil(obj)) {
            return;
        }
        if(_.isArray(obj)) {
            return _.each(obj, fct);
        }else{
            return fct(obj, 0);
        }
    }

    function ensureObj(p) {
        if(_.isNil(p)) {
            return {};
        }
        return p;
    }

    function ensureArr(p) {
        if(_.isNil(p)) {
            return [];
        }
        return p;
    }

    function ensureStr(p) {
        if(_.isNil(p)) {
            return "";
        }
        return String(p);
    }

    function eidFromUrl(o) {
        var url = _.isString(o) ? o : _.isUndefined(o.href) ? o.ref : o.href;
        var eidPos = url.indexOf('EID');
        if(eidPos != -1) {
            return url.substring(eidPos,eidPos+40);
        }
        var i = url.lastIndexOf('/');
        return "EID" + url.substring(i+1,i+37);
    }

    /**
     *
     * @param refProperty
     * @param collection
     * @param fct call fct, toJSON is default
     * @returns {Array}
     */
    function resolveRefs(refProperty, collection, fct) {
        var c = [];
        $.each(refProperty, function (i, t) {
            var o = collection.get(eidFromUrl(t));
            if(typeof o !== "undefined") {
                if( typeof fct === "undefined" ) {
                    c.push(o.toJSON());
                }else{
                    c.push(fct(o));
                }
            }else{
                console.error(collection.deferred.state());
                console.error(collection.length);
                console.error(collection);
                referenceError(t);
            }
        });
        return c;
    }

    function resolveRef(refProperty, collection, fct) {
        var o = collection.get(eidFromUrl(refProperty));
        if(typeof o !== "undefined") {
            if( typeof fct === "undefined" ) {
                return o.toJSON();
            }else{
                return fct(o);
            }
        }else{
            console.error(collection.deferred.state());
            console.error(collection.length);
            console.error(collection);
            referenceError(refProperty);
        }
    }

    function resolveRefOrUndefined(refProperty, collection, fct) {
        var o = collection.get(eidFromUrl(refProperty));
        if(typeof o !== "undefined") {
            if( typeof fct === "undefined" ) {
                return o.toJSON();
            }else{
                return fct(o);
            }
        }else{
            return undefined;
        }
    }

    function beautifyNerdyStr(str) {
        if(!_.isUndefined(str)) {
            return _.startCase(
                str.replace(/[_|.]/g, " ")
            );
        }
    }

    function addColToIdMap(collection, map) {
        if(!_.isUndefined(collection)) {
            $.each(collection, function (i, c) {
                map[c.id]=c;
            })
        }
    }

    function toIdMap(collection) {
        var map = {};
        if(!_.isEmpty(collection)) {
            $.each(collection, function (i, c) {
                map[c.id]=c;
            });
        }
        return map;
    }

    function addColToIdMap1IfNotInMap2(collection, map1, map2) {
        if(!_.isUndefined(collection)) {
            $.each(collection, function (i, c) {
                if (_.isUndefined(map2[c.id])) {
                    map1[c.id] = c;
                }
            })
        }
    }

    function getClassNamesStr(selection, startingWith) {
        return getClassNamesArr(selection, startingWith).join(" ");
    }

    function getClassNamesArr(selection, startingWith) {
        return $.grep($(selection).attr('class').split(" "), function(v, i){
            return v.indexOf(startingWith) === 0;
        });
    }

    /**
     * getParams(url).key = [v1, v2]
     * @param url
     */
    function getParameters(url) {
        if(_.isNil(url)) {
            return {};
        }
        var ampPos = url.lastIndexOf('?');
        var params = ampPos==-1 ? url.split('&') : url.substring(ampPos+1).split('&');
        return _.chain(params)
            .map(function (item) {
                if (item) {
                    return _.map(item.split("="), function (item2) {
                        return item2.split(",");
                    });
                }
            })
            .compact()
            .fromPairs()
            .value();
    }

    function getParameterValue(url, value) {
        var parameters = getParameters(url);
        var v = parameters[value];
        if(!_.isNil(v) && !_.isNil(v[0])) {
            return v[0];
        }
        return null;
    }

    // Resource
    function Resource(name, uri) {
        this.name = name;
        this.uri = uri;
    }

// Test Object
    function TestObject(idOrResources) {
        if( typeof idOrResources === 'string' ) {
            this.id = idOrResources;
        }else{
            this.resources = {};
            this.resources[idOrResources.name] = idOrResources.uri;
        }

        this.setCredentials = function(username, password) {
            if(!_.isUndefined(username) && ! username.trim().length < 1) {
                this.username = username;
                this.password = password;
            }
        }
    }

    function CreateReusableTestObjectRequest(label, description, resources) {
        this.label = label;
        this.description = description;
        if(!_.isNil(resources)) {
            this.resources = {};
            this.resources[resources.name] = resources.uri;
        }
    }

    // Test Run
    function TestRun(label, executableTestSuiteIds, argumentMap, testObject) {
        this.label = label;
        this.executableTestSuiteIds = executableTestSuiteIds;
        this.arguments = argumentMap;
        this.testObject = testObject;
    }

    function TestRunTemplate(label, testRunTemplateId, argumentMap, testObject) {
        this.testRunTemplateId = testRunTemplateId;
        this.label = label;
        this.arguments = argumentMap;
        this.testObject = testObject;
    }

    function startTestRun(testRun, successCallback, errorCallback) {
        console.log( JSON.stringify(testRun) );
        $.mobile.loading( "show" );
        $.ajax({
            url: baseUrl()+"/TestRuns",
            type: 'POST',
            contentType: "application/json",
            data: JSON.stringify(testRun) ,
            error: function (xhr, status, error) {
                $.mobile.loading( "hide" );
                apiCallError(
                    "", "Test Run initialization failure", xhr);
                errorCallback(xhr);
            },
            success: function (data) {
                $.mobile.loading( "hide" );
                successCallback(data);
                toastr.success(
                    "Testlauf '"+data.EtfItemCollection.testRuns.TestRun.label+"' wird ausgeführt",
                    "Testlauf erfoglreich gestartet", {timeOut: 15000, extendedTimeOut: 30000});
            }
        });
    }

    function createTestObject(testObject, files, successCallback, errorCallback) {
        console.log( JSON.stringify(testObject) );
        var data = new FormData();
        if(!_.isNull(files)) {
            _.each(files,function(f, i) {
                data.append("file-"+i,f);
            });
        }
        data.append('testobject', JSON.stringify(testObject));
        $.mobile.loading( "show" );
        $.ajax({
            url: baseUrl()+"/TestObjects",
            type: 'POST',
            contentType: false,
            processData: false,
            data: data,
            error: function (xhr, status, error) {
                $.mobile.loading( "hide" );
                apiCallError(
                    "", "Das Testobjekt konnte nicht angelegt werden", xhr);
                errorCallback(xhr);
            },
            success: function (data) {
                $.mobile.loading( "hide" );
                successCallback(data);
                toastr.success(
                    "Testobjekt erfolgreich angelegt", {timeOut: 15000, extendedTimeOut: 30000});
            }
        });
    }

    function changePage(url, data) {
        $("body").pagecontainer("change", url, { reload: false, transition: "flip", changeHash: true });
        window.history.pushState(data,"", url);
    }

    return {
        baseUrl: baseUrl(),
        blurWarn: blurWarn,
        referenceError: referenceError,
        invalidInputError: invalidInputError,
        apiCallError: apiCallError,
        CreateReusableTestObjectRequest: CreateReusableTestObjectRequest,
        Resource: Resource,
        TestObject: TestObject,
        TestRun: TestRun,
        TestRunTemplate: TestRunTemplate,
        startTestRun: startTestRun,
        createTestObject: createTestObject,
        jeach: jeach,
        jeachSafe: jeachSafe,
        each: each,
        ensureObj: ensureObj,
        ensureArr: ensureArr,
        ensureStr: ensureStr,
        eidFromUrl: eidFromUrl,
        getParameters: getParameters,
        getParameterValue: getParameterValue,
        resolveRefs: resolveRefs,
        resolveRef: resolveRef,
        resolveRefOrUndefined: resolveRefOrUndefined,
        beautifyNerdyStr: beautifyNerdyStr,
        addColToIdMap: addColToIdMap,
        toIdMap: toIdMap,
        addColToIdMap1IfNotInMap2: addColToIdMap1IfNotInMap2,
        getClassNamesStr: getClassNamesStr,
        getClassNamesArr: getClassNamesArr,
        changePage: changePage,
        unexpectedError: unexpectedError,
        silentError: silentError,
        UnexpectedError: UnexpectedError
    };
});

