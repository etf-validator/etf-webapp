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
            this.tagCollection = options.collection.tagCollection;
            this.testObjectTypeCollection = options.collection.testObjectTypeCollection;
            this.translationTemplateBundleCollection = options.collection.translationTemplateBundleCollection;
        },

        toJSON: function() {
            var tagCollection = this.tagCollection;
            var testObjectTypeCollection = this.testObjectTypeCollection;
            var attributes = _.clone(this.attributes);
            var translationTemplateRef =  this.get("translationTemplateBundle");
            var translationTemplateBundle;
            // do not generate JSON to speed up things
            if(!_.isUndefined(translationTemplateRef)) {
                translationTemplateBundle = v2.resolveRef(
                    translationTemplateRef.href, this.translationTemplateBundleCollection, function(r) { return r; });
            }
            $.each(attributes, function(key, value) {
                switch (key) {
                    case 'tags':
                        attributes[key] = v2.resolveRefs(value.tag, tagCollection);
                        break;
                    case 'supportedTestObjectTypes':
                        attributes[key] = v2.toIdMap(v2.resolveRefs(
                            value.testObjectType, testObjectTypeCollection));
                        break;
                    case 'ParameterList':
                        if(!_.isUndefined(translationTemplateBundle) && value!=null) {
                            _.each(value.parameter, function(p, i) {
                                if(p != null) {
                                    var t;
                                    if(_.isUndefined(p.description)) {
                                        p.description = translationTemplateBundle.getTranslation('TR.parameter.description.'+p.name);
                                    }else if(!_.isUndefined(p.description.ref)) {
                                        p.description=translationTemplateBundle.getTranslation(p.description.ref);
                                    }
                                    if(_.isUndefined(p.label)) {
                                        p.label=translationTemplateBundle.getTranslation('TR.parameter.name.'+p.name);
                                    }
                                }
                            });
                        }
                        attributes[key] = value;
                        break;
                    default:
                        if(_(value.toJSON).isFunction()) {
                            attributes[key] = value.toJSON();
                        }
                }
            });
            return attributes;
        },
        getDependencies: function () {
            var deps = this.get('dependencies');
            if(!_.isUndefined(deps)) {
                return v2.resolveRefs(deps.executableTestSuite, this.collection);
            }
        },

        getTestObjectTypes: function() {
            var map = {};
            var testObjectTypes = this.get('supportedTestObjectTypes');
            if(!_.isUndefined(testObjectTypes)) {
                var types = v2.resolveRefs(testObjectTypes.testObjectType, this.testObjectTypeCollection);
                _.each(types, function(t) {
                    map[t.id] = t;
                    var p = t.parent;
                    while(!_.isUndefined(p)) {
                        map[p.id] = p;
                        p = p.parent;
                    }
                });
            }
            return map;
        },

        getParameters: function() {
            if(this.get('ParameterList')!=null) {
                return this.get('ParameterList').parameter;
            }
            return null;
        }
    } );

    // Returns the Model class
    return Model;
} );
