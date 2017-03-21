// Includes file dependencies
define([
    "jquery",
    "backbone",
    "etf.webui/v2",
], function( $, Backbone, v2) {

    /**
     *
     * Store page element in page property.
     * Use registerPageHideEvent() function.
     * Only use addListener to register event listeners, which will
     * be removed on 'pagehide'.
     *
     */
    var EtfView = Backbone.View.extend( {

        registerViewEvents: function() {

            _.bindAll();

            var ctx = this;
            this.$el.on("pagehide", function(e) {
                ctx._onHide(e, ctx)
            });
            this.$el.on("pageshow", function(e) {
                ctx._onShow(e, ctx)
            });
        },

        _onHide: function (e, ctx) {
            ctx.onHide(e, ctx);
        },
        onHide: function (e, ctx) {
            console.log("Hiding %o", ctx);
        },

        _onShow: function(e, ctx) {
            ctx.onShow(e, ctx);
        },
        onShow: function (e, ctx) {
            console.log("Showing %o", ctx);
        },
    });
    return EtfView;
} );
