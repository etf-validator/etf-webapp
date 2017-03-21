define(['jquery'], function ($) {

    $(document).on("mobileinit",
        // Set up the "mobileinit" handler before requiring jQuery Mobile's module
        function () {
            $.mobile.ajaxEnabled = false;
            // Prevents all anchor click handling including the addition of active button state and alternate link bluring.
            $.mobile.linkBindingEnabled = false;
            // Disabling this will prevent jQuery Mobile from handling hash changes
            $.mobile.hashListeningEnabled = false;
            $.mobile.pushStateEnabled = false;
            $.mobile.changePage.defaults.changeHash = false;

            $.mobile.autoInitializePage = false;
            $.mobile.page.prototype.options.domCache = false;
            $.mobile.page.prototype.options.degradeInputs.data = true;
            $.mobile.phonegapNavigationEnabled = true;
        }
    );
});
