requirejs.onError = function (err) {
    console.log(err.requireType);
    if (err.requireType === 'timeout') {
        console.log('Timeout loading modules: ' + err.requireModules);
    }else if (err.requireType === 'scripterror') {
        alert('Error loading the ETF Web Interface. Please contact the System Administrator and report this error message: '+err.message);
    }
    throw err;
};

var locale = ((navigator.languages && navigator.languages.length) ? navigator.languages[0] : navigator.userLanguage
    || navigator.language || navigator.browserLanguage || 'en').substring(0, 2);


require.config( {
    paths: {
        baseUrl: 'js',

        // Do not change order!
        "jquery": [
            "https://ajax.aspnetcdn.com/ajax/jQuery/jquery-1.11.3.min",
            "https://code.jquery.com/jquery-1.11.3.min",
            "lib/jquery.min"
        ],

        'jquery.mobile.config': 'config/jquery.mobile.config',

        "jquery.mobile": [
            "https://ajax.googleapis.com/ajax/libs/jquerymobile/1.4.5/jquery.mobile.min",
            "https://ajax.aspnetcdn.com/ajax/jquery.mobile/1.4.5/jquery.mobile-1.4.5.min",
            "lib/jquery.mobile.min"
        ],

        "underscore": [
            "https://cdnjs.cloudflare.com/ajax/libs/lodash.js/4.17.4/lodash.min",
            "https://cdn.jsdelivr.net/npm/lodash@4.17.4/lodash.min",
            "lib/lodash.min"
        ],
        "backbone": [
            "https://cdnjs.cloudflare.com/ajax/libs/backbone.js/1.3.3/backbone-min",
            "https://fastcdn.org/Backbone.js/1.2.3/backbone-min",
            "lib/backbone-min"
        ],
        "backbone.paginator": [
            "https://cdnjs.cloudflare.com/ajax/libs/backbone.paginator/2.0.5/backbone.paginator.min",
            "lib/backbone.paginator.min"
        ],
        "moment": [
            "https://cdn.jsdelivr.net/npm/moment@2.24.0/min/moment.min",
            "https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.24.0/moment.min",
            "lib/momemt.min"
        ],

        "moment.locale": [
            "https://cdn.jsdelivr.net/npm/moment@2.24.0/locale/"+locale,
            "https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.24.0/locale/"+locale,
            "lib/moment.locale.en"
        ],

        "parser": [
            "https://cdnjs.cloudflare.com/ajax/libs/fast-xml-parser/3.12.16/parser.min"
        ],

        "toastr": [
            "https://cdnjs.cloudflare.com/ajax/libs/toastr.js/2.1.3/toastr.min",
            "lib/toastr.min"
        ],
        "jquery.iframe-transport": [
            "https://cdnjs.cloudflare.com/ajax/libs/blueimp-file-upload/9.28.0/js/jquery.iframe-transport.min",
            "lib/jquery.iframe-transport.min"
        ],
        "jquery-ui/ui/widget": [
            "lib/jquery.ui.widget"
        ],
        "jquery.fileupload": [
            "https://cdnjs.cloudflare.com/ajax/libs/blueimp-file-upload/9.28.0/js/jquery.fileupload.min",
            "lib/jquery.fileupload.min"
        ],
        "etf.webui": [
            "app"
        ],
    },
    map: {
        '*': {
            "../moment": "moment"
        }
    },
    onNodeCreated: function(node, config, module, path) {
        var sri = {
            'https://ajax.aspnetcdn.com/ajax/jQuery/jquery-1.11.3.min.js': 'sha384-6ePHh72Rl3hKio4HiJ841psfsRJveeS+aLoaEf3BWfS+gTF0XdAqku2ka8VddikM',
            'https://code.jquery.com/jquery-1.11.3.min.js': 'sha384-+54fLHoW8AHu3nHtUxs9fW2XKOZ2ZwKHB5olRtKSDTKJIb1Na1EceFZMS8E72mzW',

            'https://ajax.googleapis.com/ajax/libs/jquerymobile/1.4.5/jquery.mobile.min.js': 'sha384-XEn4bZ9g8ia7KJWyaf3o/ADO5s2pqwtDl1MdxgCZ9x6rm5QICw5Zk2+vwIJnAeFD',
            'https://ajax.aspnetcdn.com/ajax/jquery.mobile/1.4.5/jquery.mobile-1.4.5.min.js': 'sha384-u7i0wHEdsFrw92D1Z0sk2r6kiOGnZJhnawPUT0he8TRKfD4/XMEsj22l/cHFXO3v',

            'https://cdnjs.cloudflare.com/ajax/libs/lodash.js/4.17.4/lodash.min.js': 'sha384-FwbQ7A+X0UT99MG4WBjhZHvU0lvi67zmsIYxAREyhabGDXt1x0jDiwi3xubEYDYw',
            'https://cdn.jsdelivr.net/npm/lodash@4.17.4/lodash.min.js': 'sha384-zIRexRVB5q09c7QIwaG/PJmsn9EPaqP3V9wNSk3XSC2hU6ns+hMorBODGGcKRnbz',

            'https://cdnjs.cloudflare.com/ajax/libs/backbone.js/1.3.3/backbone-min.js': 'sha384-NNt9ocJfZhIg2c5PbM5G2a3tTaeXhEfqCHWHNB7htzaWKn8MwFkzVyGdzLA8QMX7',
            'https://fastcdn.org/Backbone.js/1.2.3/backbone-min.js': 'sha384-kgH1F06klaG52/uQEQlpP5QZ9tbJZgcU4omvs1DRSHaJGVZWp//NYtoi93ZmGday',

            'https://cdnjs.cloudflare.com/ajax/libs/backbone.paginator/2.0.5/backbone.paginator.min.js': 'sha256-nqCLeI27BiuRxhJEcsKPwUpTusAzME+5qFOWntHhAvy6',

            'https://cdn.jsdelivr.net/npm/moment@2.24.0/min/moment.min.js': 'sha384-fYxN7HsDOBRo1wT/NSZ0LkoNlcXvpDpFy6WzB42LxuKAX7sBwgo7vuins+E1HCaw',
            'https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.24.0/moment.min.js': 'sha384-fYxN7HsDOBRo1wT/NSZ0LkoNlcXvpDpFy6WzB42LxuKAX7sBwgo7vuins+E1HCaw',

            'https://cdn.jsdelivr.net/npm/moment@2.24.0/locale/de.js': 'sha384-u4vVudBaVphJuXJ5BskWwV2AvgqjVmlTn09LZ2TAABA7UBHbGTp88ZK0/ZHkyAnX',
            'https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.24.0/locale/de.js': 'sha384-u4vVudBaVphJuXJ5BskWwV2AvgqjVmlTn09LZ2TAABA7UBHbGTp88ZK0/ZHkyAnX',

            'https://cdn.jsdelivr.net/npm/moment@2.24.0/locale/fr.js': 'sha384-3FwcuGMawv/mFO8kXToMwqRL3Zo2DEwdA2OFneqE7qQgBH+aAlacqr/XVZnSKhdB',
            'https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.24.0/locale/fr.js': 'sha384-3FwcuGMawv/mFO8kXToMwqRL3Zo2DEwdA2OFneqE7qQgBH+aAlacqr/XVZnSKhdB',

            'https://cdn.jsdelivr.net/npm/moment@2.24.0/locale/nl.js': 'sha384-A+2nDIC4M+umioIYsDOMZ6ZEFk4C8SOaiFTd6AdcPyRORVuPkIuKJQtYlsV3hQXO',
            'https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.24.0/locale/nl.js': 'sha384-A+2nDIC4M+umioIYsDOMZ6ZEFk4C8SOaiFTd6AdcPyRORVuPkIuKJQtYlsV3hQXO',

            'https://cdn.jsdelivr.net/npm/moment@2.24.0/locale/es.js': 'sha384-1qAUeyS6EIOskM+UWGhJh3qUqepgvqXZ12P6NiY/UiUeeaHSLMAKMyRpJ3gd4vTG',
            'https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.24.0/locale/es.js': 'sha384-1qAUeyS6EIOskM+UWGhJh3qUqepgvqXZ12P6NiY/UiUeeaHSLMAKMyRpJ3gd4vTG',

            'https://cdn.jsdelivr.net/npm/moment@2.24.0/locale/cs.js': 'sha384-+kucl+Uluan4CpizG7eBtjnqyT9Wt2xcnLyBXwwtxx+ytiURuKJ9cn9JUCNjynyb',
            'https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.24.0/locale/cs.js': 'sha384-+kucl+Uluan4CpizG7eBtjnqyT9Wt2xcnLyBXwwtxx+ytiURuKJ9cn9JUCNjynyb',

            'https://cdnjs.cloudflare.com/ajax/libs/fast-xml-parser/3.12.16/parser.min.js': 'sha384-0FkN2DVRDBJS+GhW7C35F4HUZd1iY6gJ91VbF21KE+PtdPz7BVDAdgQjTYdlQ6Uy',

            'https://cdnjs.cloudflare.com/ajax/libs/toastr.js/2.1.3/toastr.min.js': 'sha256-yNbKY1y6h2rbVcQtf0b8lq4a+xpktyFc3pSYoGAY1qQ=',

            'https://cdnjs.cloudflare.com/ajax/libs/blueimp-file-upload/9.28.0/js/jquery.iframe-transport.min.js': 'sha384-7kOV/RzPaIaLacfvbwe/cjj4/1kekqkFKX538nlnt1yJQMqHpOJaabou8pZDsqVO',
            'https://cdnjs.cloudflare.com/ajax/libs/blueimp-file-upload/9.28.0/js/jquery.fileupload.min.js': 'sha384-ktm5CYsTlQ2eBmFPwrpbH+Ik2ktsL2auWT+sLlxhKNFMQBAgyUEIQRTVvZUB2Nal'
        };
        var jsPos = path.indexOf('js/');
        var sri = sri[path];
        if(jsPos != 0) {
            node.setAttribute('crossorigin', 'anonymous');
            if(sri) {
                node.setAttribute('integrity', sri);
            }else{
                alert('Security error. ' +
                    'Please contact the System Administrator and ' +
                    'report this error message: script integrity check failed for '+path);
            }
        }else{
            node.setAttribute('crossorigin', 'use-credentials');
        }
    },
    shim: {
        "backbone": {
            "deps": [ "underscore", "jquery" ],
            "exports": "Backbone"
        },

        "parser": {
            "exports": "parser"
        },

        'backbone-paginator': {
            deps: ['backbone']
        },

        'jquery.mobile-config': ['jquery'],
        'jquery.mobile': ['jquery','jquery.mobile.config'],

        "jquery.fileupload": {
            deps: [ "jquery",
                "jquery-ui/ui/widget",
                "jquery.iframe-transport"]
        },

        "etf.webui/v2": {
            deps: [ "underscore", "toastr"]
        },
    }
});

window.baseUrl = "v2";

require([
    "jquery",
    "backbone",
    "etf.webui/routers/mobileRouter",
    "jquery.mobile"
], function ( $, Backbone, Mobile ) {

    require( [ "toastr" ], function (toastr) {
        toastr.options = {
            "closeButton": true,
            "debug": false,
            "newestOnTop": false,
            "progressBar": true,
            "positionClass": "toast-bottom-right",
            "preventDuplicates": true,
            "onclick": null,
            "showDuration": "300",
            "hideDuration": "1000",
            "timeOut": "8000",
            "extendedTimeOut": "1000",
            "showEasing": "swing",
            "hideEasing": "linear",
            "showMethod": "fadeIn",
            "hideMethod": "fadeOut"
        }
    });

    require( [ "jquery.mobile" ], function ($) {
        // Instantiates a new Backbone.js Mobile Router
        this.router = new Mobile();

    });
});

if(['de', 'fr', 'nl', 'es', 'cs'].indexOf(locale)!==-1) {
    require.config( {
        paths: {
            baseUrl: 'js',
            "moment.locale": [
                "https://cdn.jsdelivr.net/npm/moment@2.24.0/locale/"+locale,
                "https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.24.0/locale/"+locale
            ],
        },
        map: {
            '*': {
                "../moment": "moment"
            }
        }
    });
    define(['moment', 'moment.locale'], function (moment) {
        moment.locale(locale);
    });
}
