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

// Translation Template Collection
// ===================

// Includes file dependencies
define([
    "jquery",
    "backbone",
    "etf.webui/v2",
    "etf.webui/collections/EtfCollection",
    "../models/TranslationTemplateBundleModel" ], function($, Backbone, v2, EtfCollection, TranslationTemplateBundleModel ) {

    var Collection = EtfCollection.extend( {

        url: v2.baseUrl + "/TranslationTemplateBundles",
        collectionName: "Translation Templates",

        parse: function(response) {
            if(_.isUndefined(response.EtfItemCollection.translationTemplateBundles)) {
                return null;
            }
            return response.EtfItemCollection.translationTemplateBundles.TranslationTemplateBundle;
        },

        model: TranslationTemplateBundleModel,

    });

    return Collection;

} );
