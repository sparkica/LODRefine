var ZemantaCrowdSourcingExtension = {handlers: {}, util: {}};
var ZemUtil = {};

// Internationalization init
var lang = navigator.language.split("-")[0]
		|| navigator.userLanguage.split("-")[0];
var dictionary = "";
$.ajax({
	url : "/command/core/load-language?",
	type : "POST",
	async : false,
	data : {
	  module : "crowdsourcing",
//		lang : lang
	},
	success : function(data) {
		dictionary = data;
	}
});
$.i18n.setDictionary(dictionary);
// End internationalization

ZemantaCrowdSourcingExtension.handlers.storeCrowdFlowerSettings = function() {

        new ZemantaSettingsDialog(function(newSettings) {
                $.post(
                                "/command/core/set-preference",
                                {
                                        name : "crowdflower.apikey",
                                        value : JSON.stringify(newSettings.apiKey)
                                },
                                function(o) {
                                        if (o.code == "error") {
                                                ZemUtil.showErrorDialog(o.message,'body');
                                        }
                                },
                                "json"
                );
                $.post(
                                "/command/core/set-preference",
                                {
                                        name : "crowdflower.defaultTimeout",
                                        value : JSON.stringify(newSettings.defaultTimeout)
                                },
                                function(o) {
                                        if (o.code == "error") {
                                                ZemUtil.showErrorDialog(o.message, 'body');
                                        }
                                },
                                "json"
                );
        });
};


ZemantaCrowdSourcingExtension.handlers.doNothing = function() {
        alert("Crowdsourcing extension active...");
};


ZemantaCrowdSourcingExtension.handlers.openJobSettingsDialog = function()  {

        new ZemantaCrowdFlowerDialog(function(extension) {

                $.post(
                                "command/crowdsourcing/create-crowdflower-job",
                                { "project" : theProject.id, 
                                        "extension": JSON.stringify(extension),
                                        "engine" : JSON.stringify(ui.browsingEngine.getJSON())
                                },
                                function(o)
                                {
                                        if(o && o.status != "error") {
                                                var msg = "";
                                                
                                                if(extension.new_job === true) {
                                                        msg = $.i18n._('crowd-ext-create')["new-job"]; 
                                                } else {  
                                                        msg = $.i18n._('crowd-ext-create')["data-upload"];
                                                }
                                                
                                                ZemUtil.showConfirmation($.i18n._('crowd-ext-create')["conf-header"]     , msg, 'body');
                                        } else {
                                                ZemUtil.showErrorDialog($.i18n._('crowd-ext-create')["create-error"] + o.message, 'body');
                                        }
                                },
                                "json"
                );     

        });
};

ZemantaCrowdSourcingExtension.handlers.evaluateReconDialog = function()  {

        new ZemantaCFEvaluateReconDialog(function(extension) {

                $.post(
                                "command/crowdsourcing/evaluate-recon-job",
                                { "project" : theProject.id, 
                                        "extension": JSON.stringify(extension),
                                        "engine" : JSON.stringify(ui.browsingEngine.getJSON())
                                },
                                function(o)
                                {
                                        if(o && o.status != "error") {
                                                var msg = $.i18n._('crowd-ext-create')["success-upload"] ;
                                                ZemUtil.showConfirmation($.i18n._('crowd-ext-create')["upload-header"], msg, 'body');
                                        } else {
                                                ZemUtil.showErrorDialog($.i18n._('crowd-ext-create')["upload-error"]+ o.status, 'body');                                                
                                        }
                                },
                                "json"
                );     

        });
};


ZemantaCrowdSourcingExtension.handlers.imageReconDialog = function()  {

        new ZemCFImgReconDialog(function(extension) {

                $.post(
                                "command/crowdsourcing/image-recon-job",
                                { "project" : theProject.id, 
                                        "extension": JSON.stringify(extension),
                                        "engine" : JSON.stringify(ui.browsingEngine.getJSON())
                                },
                                function(o)
                                {
                                        //status can be 200
                                        if (o && o.status != "error") {
                                                var msg = $.i18n._('crowd-ext-create')["success-upload"] ;
                                                ZemUtil.showConfirmation($.i18n._('crowd-ext-create')["upload-header"], msg, 'body');

                                        } else {
                                                ZemUtil.showErrorDialog($.i18n._('crowd-ext-create')["upload-error"] + o.message, 'body');                                                
                                        }
                                },
                                "json"
                );     

        });
};



ZemantaCrowdSourcingExtension.handlers.getApiKey =  function() {
        ZemantaCrowdSourcingExtension.util.loadCrowdFlowerApiKeyFromSettings(function(apiKey) {
                return apiKey;
        });
};


ExtensionBar.addExtensionMenu({
        "id": "crowdsourcing-ext",
        "label": $.i18n._('crowd-ext-menu')["crowdsourcing"],
        "submenu": [
                    {
                            "id": "crowdsourcing-ext/create-crowdflower-job",
                            label: $.i18n._('crowd-ext-menu')["create-job"],
                            click: ZemantaCrowdSourcingExtension.handlers.openJobSettingsDialog
                    },
                    {},
                    {
                            "id":"crowdsourcing-ext/templates",
                            "label": $.i18n._('crowd-ext-menu')["templates"],
                            "submenu": [
                                        {
                                                "id": "crowdsourcing-ext/templates/eval-recon-data",
                                                "label": $.i18n._('crowd-ext-menu')["eval-recon"],
                                                click: ZemantaCrowdSourcingExtension.handlers.evaluateReconDialog	 
                                        },
                                        {
                                                "id":"crowdsourcing-ext/templates/recon-img",
                                                "label": $.i18n._('crowd-ext-menu')["recon-img"],
                                                click: ZemantaCrowdSourcingExtension.handlers.imageReconDialog
                                        }
                                        ]
                    },
                    {},
                    {
                            "id": "crowdsourcing-ext/settings",
                            "label": $.i18n._('crowd-ext-menu')["cf-settings"],
                            click: ZemantaCrowdSourcingExtension.handlers.storeCrowdFlowerSettings
                    }
                    ]
});

