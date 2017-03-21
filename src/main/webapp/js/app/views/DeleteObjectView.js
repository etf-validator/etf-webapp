
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
            event.preventDefault();
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
            $.mobile.changePage(this.backPage, {role: 'page', reverse: false, changeHash:false});
            // Will change to the correct url and trigger a rerender event
            // window.location.href = "#test-objects-page";
        },

        cancel: function(e){
            e.preventDefault();
            console.log("Canceled "+this.typeName+" deletion");
            $.mobile.changePage(this.backPage, {role: 'page', reverse: false, changeHash:false});

        },
    });

    return DeleteTestObjectView;
} );
