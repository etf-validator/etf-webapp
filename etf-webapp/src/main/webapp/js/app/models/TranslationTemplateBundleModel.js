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

// Executable Test Suite Model
// ==============

// Includes file dependencies
define([
    "jquery",
    "backbone",
    "etf.webui/v2",
], function( $, Backbone, v2 ) {

    // The Model constructor
    var Model = Backbone.Model.extend( {

        initialize: function( attr, options ) {
            if (!_.isUndefined(attr.parent)) {
                this.set("parent", attr.parent);
            }
        },

        getTranslationFromCol : function(name, collection) {
            var r;
            if(!_.isUndefined(collection)) {
                _.each(collection.LangTranslationTemplateCollection, function (c,i) {
                    if(c.name==name) {
                        var userLang = navigator.language || navigator.userLanguage;
                        return v2.jeach(c.translationTemplates.TranslationTemplate, function (t,i) {
                            if(userLang.lastIndexOf(t.language, 0) === 0) {
                                r=t.$
                                return false;
                            }
                        });
                    }
                });
            }
            return r;
        },

        // toJSON do not generate JSON to speed up things

        getTranslation: function(name) {
            // most likely this is store in the parent templates
            if(!_.isNil(this.get('parent'))) {
                var parentColl = v2.resolveRef(this.get('parent'), this.collection);
                if (!_.isNil(parentColl)) {
                    var r = this.getTranslationFromCol(name, parentColl.translationTemplateCollections);
                    if (!_.isNil(r)) {
                        return r;
                    }
                }
            }
            return this.getTranslationFromCol(name, this.get('translationTemplateCollections'));
        }
    } );

    // Returns the Model class
    return Model;

} );
