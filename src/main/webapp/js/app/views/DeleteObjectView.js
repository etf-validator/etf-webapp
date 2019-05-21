
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

define([
    "jquery",
    "backbone",
    "toastr",
    "etf.webui/views/EtfView",
], function( $, Backbone, toastr, EtfView) {

    var DeleteTestObjectView = EtfView.extend( {

        initialize : function(options){
            this.registerViewEvents();
            this.typeName = options.typeName;
            this.backPage = options.backPage;

            options.confirmButton.on('click', _.bind(this.confirm, this));
            options.cancelButton.on('click', _.bind(this.cancel, this));
        },

        render:function (options) {
            this.targetObject = options.targetObject;
            return this;
        },

        confirm: function(e){
            e.preventDefault();
            console.log("Confirmed to delete "+this.typeName+" %o", this.targetObject);
            var _this = this;
            var callbacks = {
                success: function(model) {
                    toastr["success"](_this.typeName + " '"+model.get('label')+"' ("+model.id+") deleted!");
                },
                error: function(model, response) {
                    toastr["error"]("Could not delete "+_this.typeName+" '"+model.id+"'!");
                    console.log(response);
                }
            };
            this.targetObject.destroy(callbacks);
            const backUrl = this.backPage.context.baseURI + "#test-reports";
            window.location.replace(backUrl);
            // Will change to the correct url and trigger a rerender event
            // window.location.href = "#test-objects-page";
        },

        cancel: function(e){
            e.preventDefault();
            console.log("Canceled "+this.typeName+" deletion");
            const backUrl = this.backPage.context.baseURI + "#test-reports";
            window.location.replace(backUrl);

        },
    });

    return DeleteTestObjectView;
} );
