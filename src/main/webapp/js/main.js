requirejs.onError = function (err) {
    console.log(err.requireType);
    if (err.requireType === 'timeout') {
        console.log('Timeout loading modules: ' + err.requireModules);
    }else if (err.requireType === 'scripterror') {
        alert('Error loading the ETF Web Interface, please contact the System Administrator. Error message: '+err.message);
    }
    throw err;
};

require.config( {
    paths: {
        baseUrl: 'js',

        // Do not change order!
        "jquery": [
            "//ajax.aspnetcdn.com/ajax/jQuery/jquery-1.11.3.min", "lib/jquery.min"
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
            "//cdnjs.cloudflare.com/ajax/libs/blueimp-file-upload/9.14.2/js/jquery.iframe-transport.min", "lib/jquery.iframe-transport.min"
        ],
        "jquery-ui/ui/widget": [
            "lib/jquery.ui.widget"
        ],
        "jquery.fileupload": [
            "//cdnjs.cloudflare.com/ajax/libs/blueimp-file-upload/9.14.2/js/jquery.fileupload.min", "lib/jquery.fileupload.min"
        ],
        "etf.webui": [
            "app"
        ],
    },
    onNodeCreated: function(node, config, module, path) {
        var sri = {
            'jquery': 'sha256-rsPUGdUPBXgalvIj4YKJrrUlmLXbOb6Cp7cdxn1qeUc=',
            'jquery.mobile': 'sha256-MkfSkbXhZoQ1CyPwjC30mPfLF8iKF5n564n9WvCLX4E=',
            'jquery.validate': 'sha256-Lj47JmDL+qxf6/elCzHQSUFZmJYmqEECssN5LP/ifRM=',
            'underscore': 'sha256-IyWBFJYclFY8Pn32bwWdSHmV4B9M5mby5bhPHEmeY8w=',
            'backbone': 'sha256-0atoj6xVOJUoBM8Vp5PFywwLLE+aNl2svi4Q9UWZ+dQ=',
            'backbone.paginator': 'sha256-nqCLeI27BiuRxhJEcsKPwUpTusAzME+5qFOWntHhAvy6',
            'moment': 'sha256-Gn7MUQono8LUxTfRA0WZzJgTua52Udm1Ifrk5421zkA=',
            'toastr': 'sha256-yNbKY1y6h2rbVcQtf0b8lq4a+xpktyFc3pSYoGAY1qQ=',
            'jquery.iframe-transport': 'sha256-OiZnRAga/nDE1Ud8eLfBWCwb9mMZmkrRIRblCeRYWj8=',
            'jquery-ui/ui/widget': 'sha256-CvqMlHtDX8dDgshwl03tVwvzncqqMKN0FLzZrNap4+I=',
            'jquery.fileupload': 'sha256-tcXzqklRDpmITiQ0Ff+S6H2uUQl089oXkEyGjOOGmN4='
        };
        if (sri[module]) {
            node.setAttribute('integrity', sri[module]);
            node.setAttribute('crossorigin', 'anonymous');
        }
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
