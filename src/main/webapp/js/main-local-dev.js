require.config( {
    paths: {
        // Settings for IntelliJ
        baseUrl: '//localhost:63342/etf-bsxds/etf-webapp_main/webapp/js',

        // Do not change order!
        "jquery": [
            "//ajax.aspnetcdn.com/ajax/jQuery/jquery-1.12.4.min", "lib/jquery.min"
        ],
        'jquery.mobile.config': 'config/jquery.mobile.config',
        "jquery.mobile": [
            "//ajax.googleapis.com/ajax/libs/jquerymobile/1.4.5/jquery.mobile.min", "lib/jquery.mobile.min"
        ],
        "jquery.validate": [
            "//ajax.aspnetcdn.com/ajax/jquery.validate/1.14.0/jquery.validate.min", "lib/jquery.validate.min"
        ],
        "underscore": [
            "//cdn.jsdelivr.net/lodash/4.17.4/lodash.min", "lib/lodash.min"
        ],
        "backbone": [
            "//cdnjs.cloudflare.com/ajax/libs/backbone.js/1.3.3/backbone-min", "lib/backbone-min"
        ],
        "backbone.paginator": [
            "//cdnjs.cloudflare.com/ajax/libs/backbone.paginator/2.0.5/backbone.paginator.min", "lib/backbone.paginator.min"
        ],
        "moment": [
            "//cdnjs.cloudflare.com/ajax/libs/moment.js/2.17.1/moment.min", "lib/momemt.min"
        ],
        "toastr": [
            "//cdnjs.cloudflare.com/ajax/libs/toastr.js/2.1.3/toastr.min", "lib/toastr.min"
        ],
        "jquery.iframe-transport": [
            "//cdnjs.cloudflare.com/ajax/libs/blueimp-file-upload/9.14.2/js/jquery.iframe-transport.min", "lib/jquery.iframe-transport"
        ],
        "jquery-ui/ui/widget": [
            "lib/jquery.ui.widget"
        ],
        "jquery.fileupload": [
            "//cdnjs.cloudflare.com/ajax/libs/blueimp-file-upload/9.14.2/js/jquery.fileupload.min", "lib/jquery.fileupload"
        ],
        "etf.webui": [
            "app"
        ],
    },
    shim: {
        "backbone": {
            "deps": [ "underscore", "jquery" ],
            "exports": "Backbone"
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

window.baseUrl = "http://localhost:8080/v2";

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
            "preventDuplicates": false,
            "onclick": null,
            "showDuration": "2500",
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
