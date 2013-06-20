var ZemantaCrowdSourcingExtension = {handlers: {}, util: {}};
var ZemUtil = {};


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
                                                        msg = "New job was created successfully.\nYou can see it on your CrowdFlower account."; 
                                                } else {  
                                                        msg = "Data was uploaded successfully.\nYou can see it on your CrowdFlower account.";
                                                }
                                                
                                                ZemUtil.showConfirmation("Creating job/Uploading data", msg, 'body');
                                        } else {
                                                ZemUtil.showErrorDialog("An error occured that prevented creating the job. \n" + o.message, 'body');
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
                                                var msg = "Data was successfully uploaded. Check your CrowdFlower account.";
                                                ZemUtil.showConfirmation("Uploading recon eval data", msg, 'body');
                                        } else {
                                                ZemUtil.showErrorDialog("Something went wrong while uploading. \n" + o.status, 'body');                                                
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
                                                var msg = "Data was successfully uploaded. Check your CrowdFlower account.";
                                                ZemUtil.showConfirmation("Uploading recon eval data", msg, 'body');

                                        } else {
                                                ZemUtil.showErrorDialog("Something went wrong while uploading. \n" + o.message, 'body');                                                
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
        "label": "Crowdsourcing",
        "submenu": [
                    {
                            "id": "crowdsourcing-ext/create-crowdflower-job",
                            label: "Create new job / upload data",
                            click: ZemantaCrowdSourcingExtension.handlers.openJobSettingsDialog
                    },
                    {},
                    {
                            "id":"crowdsourcing-ext/templates",
                            "label": "Templates",
                            "submenu": [
                                        {
                                                "id": "crowdsourcing-ext/templates/eval-recon-data",
                                                "label": "Evaluate reconciled data",
                                                click: ZemantaCrowdSourcingExtension.handlers.evaluateReconDialog	 
                                        },
                                        {
                                                "id":"crowdsourcing-ext/templates/recon-img",
                                                "label": "Reconcile images",
                                                click: ZemantaCrowdSourcingExtension.handlers.imageReconDialog
                                        }
                                        ]
                    },
                    {},
                    {
                            "id": "crowdsourcing-ext/settings",
                            "label": "CrowdFlower settings",
                            click: ZemantaCrowdSourcingExtension.handlers.storeCrowdFlowerSettings
                    }
                    ]
});

