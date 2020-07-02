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
    "etf.webui/v2",
    "etf.webui/views/EtfView",
    "moment",
    "toastr",
    "../models/ExecutableTestSuiteModel",
], function( $, Backbone, v2, EtfView, moment, ExecutableTestSuiteModel ) {
	
	var dependenciesTree = {};
	
	//Function to recursively switch all the dependents tests on selecting a test
	//CODACY bot: marked as too complex. Would need to refactor, but functionality works fine
    function markDependants(id){
    	//Router instanced in compilation time
        var model = router.executableTestSuitesView.collection.models.filter((x) => x.id === id)[0];
        let classList = document.querySelector("option[value="+id+"]").parentElement.parentElement.classList;
        if (typeof model.attributes.dependencies !== "undefined" && !classList.contains("ui-disabled")){
            var idDependency = model.attributes.dependencies.executableTestSuite.ref;
            //CODACY bot: Marked as object injection. Dependencies tree has no sensible information and is not used in any CRUD operation
            if(!dependenciesTree[idDependency]){
                //CODACY bot: Marked as object injection. Dependencies tree has no sensible information and is not used in any CRUD operation
                dependenciesTree[idDependency] = [];
            }
            let classListDependency = document.querySelector("option[value="+idDependency+"]").parentElement.parentElement.classList;
            let activating = classList.contains("ui-flipswitch-active");
            if(activating){
            	//CODACY bot: Marked as object injection. Dependencies tree has no sensible information and is not used in any CRUD operation
                dependenciesTree[idDependency].push(id);
            }else{
            	//CODACY bot: Marked as object injection. Dependencies tree has no sensible information and is not used in any CRUD operation
                dependenciesTree[idDependency].splice(dependenciesTree[idDependency].indexOf(id),1);
            }
            if(activating && !classListDependency.contains("ui-flipswitch-active")){
                classListDependency.add("ui-flipswitch-active");
                markDependants(idDependency);
            }
            if(activating && !classListDependency.contains("ui-disabled")){
                classListDependency.add("ui-disabled");
            }
          //CODACY bot: Marked as object injection. Dependencies tree has no sensible information and is not used in any CRUD operation
            if(dependenciesTree[idDependency].length === 0){
                classListDependency.remove("ui-flipswitch-active");
                classListDependency.remove("ui-disabled");
                markDependants(idDependency);
            }
            
            let dependenciesTreeEmpty = true;
            for (const dep in dependenciesTree){
                if(dep !== "undefined"){
                    dependenciesTreeEmpty &= (dep.length === 0);
                }
            }
            
            if(dependenciesTreeEmpty){
                document.querySelectorAll("option").forEach(function(option){
                    classListDependency = option.parentElement.parentElement.classList;
                    classListDependency.remove("ui-disabled");
                });
            }
        }
    }
	
    var ExecutableTestSuiteView = EtfView.extend( {

        el: $("#start-tests-page"),
        container : $("#executable-test-suite-listview-container"),
        //CODACY bot: _ is defined in compilation time
        //CODACY bot: inherited from master
        template: _.template($('script#executable-test-suite-items-template').html()),

        //CODACY bot: inherited from master
        initialize: function() {
            this.registerViewEvents();
        },

        render: function() {

            var _this = this;
            this.collection.deferred.done(function() {
            	//CODACY bot: inherited from master
                console.log("Rendering Executable Test Suite view: %o", _this.collection.toJSON());
                
                //CODACY bot: inherited from master
                _this.container.html(
                    _this.template({"moment": moment, "v2": v2, "collection": _this.collection.toJSON()})
                );

                $(".executable-test-suite-selection").on("change", function (e) {
                    var i = $(".executable-test-suite-selection option[value!='X']:selected").length;
                    //CODACY bot: inherited from master
                    if (i == 0) {
                        $("#fadin-start-tests-button").hide("scale");
                        $(".executable-test-suite-selection" +
                            v2.getClassNamesForNotSelection(this, "test-object-type-")).parent().removeClass("ui-disabled");
                    //CODACY bot: inherited from master
                    } else if (i == 1) {
                        $("#fadin-start-tests-button").show("scale");
                        $(".executable-test-suite-selection" +
                            v2.getClassNamesForNotSelection(this, "test-object-type-")).parent().addClass("ui-disabled");
                    }
                    markDependants(e.currentTarget.querySelectorAll("option")[1].value);
                });

                $("#fadin-start-tests-button").on("click", function (e) {
                    var u = "#start-tests?ids=";
                    $.each($(".executable-test-suite-selection"), function () {
                        var val = $(this).val();
                        if (val !== "X") {
                            u += val + ",";
                        }
                    });
                    u = u.slice(0, -1);
                    $(this).attr("href", u);
                });
                $("#fadin-start-tests-button").hide();

                // Filter by tag class
                //CODACY bot: inherited from master
                _this.container.listview({
                    autodividers: true,
                    autodividersSelector: function (li) {
                        var tag = $(li).find(".Tag");
                        if (tag.length) {
                            return tag.text();
                        } else {
                            return "";
                        }
                    }
                });

                // Stop collapsible
                $(".ii-stop-propagation").on("click", function (e) {
                    e.stopPropagation();
                    e.stopImmediatePropagation();
                    e.preventDefault();
                });
                
                _this.container.trigger("create");
                _this.container.listview().listview("refresh");
            });
        },

    });
    
    return ExecutableTestSuiteView;
} );

