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

/**
 * Created by Jon Herrmann
 */

define(['toastr'], function (toastr) {


    function baseUrl() {
        return window.baseUrl;
        // return "http://localhost:8080/v2";
    };

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
        console.error("Reference not found: "+eid+ " (" +url+")" );
    }

    function invalidInputError(message, element) {
        toastr.error(message, "Invalid input", {timeOut: 4000, extendedTimeOut: 7000});
        blurWarn(element, 4000);
    }

    function apiCallError(message, title, xhr) {
        var jsonErr;
        try
        {
            jsonErr = $.parseJSON(xhr.responseText);
        }catch(ignore) { }
        if(!_.isUndefined(xhr.xhr)) {
            try
            {
                if(_(xhr.xhr).isFunction()) {
                    jsonErr = $.parseJSON(xhr.xhr().responseText);
                }else{
                    jsonErr = $.parseJSON(xhr.xhr.responseText);
                }
            }catch(ignore) { }
        }

        var errorMesg="unknown error";
        if(!_.isUndefined(jsonErr)) {
            console.error(jsonErr.error);
            if(!_.isUndefined(jsonErr.id)) {
                errorMesg=jsonErr.error;
            }
        }else if(!_.isUndefined(xhr.xhr) && !_.isUndefined(xhr.xhr.responseText)) {
                console.error(xhr.responseText);
        }else{
            console.error(xhr);
        }
        toastr.error(message+errorMesg, title, {timeOut: 15000, extendedTimeOut: 20000});
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
            return fct(obj);
        }
    }

    function eidFromUrl(o) {
        var url = _.isString(o) ? o : _.isUndefined(o.href) ? o.ref : o.href;
        var eidPos = url.indexOf('EID');
        if(eidPos != -1) {
            return url.substring(eidPos,eidPos+40);
        }
        var i = url.lastIndexOf('/');
        return "EID" + url.substring(i+1,i+37);
    };

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
                referenceError(t);
            }
        });
        return c;
    };

    function resolveRef(refProperty, collection, fct) {
        var o = collection.get(eidFromUrl(refProperty));
        if(typeof o !== "undefined") {
            if( typeof fct === "undefined" ) {
                return o.toJSON();
            }else{
                return fct(o);
            }
        }else{
            referenceError(refProperty);
        }
    };

    function beautifyNerdyStr(str) {
        if(!_.isUndefined(str)) {
            return str.charAt(0).toUpperCase() +
                str.slice(1).replace(/[_|.]/g, " ");
        }
    }

    function addColToIdMap(collection, map) {
        if(!_.isUndefined(collection)) {
            $.each(collection, function (i, c) {
                map[c.id]=c;
            })
        }
    };

    function addColToIdMap1IfNotInMap2(collection, map1, map2) {
        if(!_.isUndefined(collection)) {
            $.each(collection, function (i, c) {
                if (_.isUndefined(map2[c.id])) {
                    map1[c.id] = c;
                }
            })
        }
    };

    function getClassNamesStr(selection, startingWith) {
        return getClassNamesArr(selection, startingWith).join(" ");
    }

    function getClassNamesArr(selection, startingWith) {
        return $.grep($(selection).attr('class').split(" "), function(v, i){
            return v.indexOf(startingWith) === 0;
        });
    }

    function getClassNamesForNotSelection(selection, startingWith) {
        var c = [];
        $.each($(selection).attr('class').split(" "), function (i, cl) {
            if(cl.indexOf(startingWith) === 0) {
                c.push(":not(."+cl+")")
            }
        });
        return c.join();
    }

    /**
     * getParams(url).key = [v1, v2]
     * @param url
     */
    function getParameters(url) {
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
            // used object before with underscore: http://underscorejs.org/#object
            .fromPairs()
            .value();
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
    };

// Test Run
    function TestRun(label, executableTestSuiteIds, argumentMap, testObject) {
        this.label = label;
        this.executableTestSuiteIds = executableTestSuiteIds;
        this.arguments = argumentMap;
        this.testObject = testObject;
    };

    function startTestRun(testRun, successCallback) {
        console.log( JSON.stringify(testRun) );
        $.ajax({
            url: baseUrl()+"/TestRuns",
            type: 'POST',
            contentType: "application/json",
            data: JSON.stringify(testRun) ,
            error: function (xhr, status, error) {
                apiCallError(
                    "Could not submit Test Run: ", "Error", xhr);
            },
            success: function (data) {
                successCallback(data);
                toastr.success(
                    "Test Run '"+data.EtfItemCollection.testRuns.TestRun.label+"' submitted",
                    "Test Run submitted", {timeOut: 15000, extendedTimeOut: 30000});
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
        Resource: Resource,
        TestObject: TestObject,
        TestRun: TestRun,
        startTestRun: startTestRun,
        jeach: jeach,
        eidFromUrl: eidFromUrl,
        getParameters: getParameters,
        resolveRefs: resolveRefs,
        resolveRef: resolveRef,
        beautifyNerdyStr: beautifyNerdyStr,
        addColToIdMap: addColToIdMap,
        addColToIdMap1IfNotInMap2: addColToIdMap1IfNotInMap2,
        getClassNamesStr: getClassNamesStr,
        getClassNamesArr: getClassNamesArr,
        getClassNamesForNotSelection: getClassNamesForNotSelection,
        changePage: changePage,
    };
});

