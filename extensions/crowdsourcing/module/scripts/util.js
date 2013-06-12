ZemantaCrowdSourcingExtension.util.loadCrowdFlowerApiKeyFromSettings = function(getApiKey) {
        $.post(
                        "/command/core/get-all-preferences",
                        {},
                        function (data) {
                                if (data != null && data["crowdflower.apikey"] != null) {

                                        getApiKey(data["crowdflower.apikey"]);
                                }
                                else {
                                        ZemUtil.showErrorDialog("CrowdFlower API key was not found in the settings. Please add it first.");
                                        getApiKey("");
                                }
                        },
                        "json"
        );	
};

ZemantaCrowdSourcingExtension.util.convert2SafeName = function(columnName) {

        var patt = /(\s)+/ig;
        var rep = '_';
        var safeName = columnName.toLowerCase();
        safeName = safeName.replace(patt, rep);
        return safeName;

};


ZemantaCrowdSourcingExtension.util.generateCML = function(tabindex) {
        var cml = '';
        $('#project-columns-'+ tabindex + ' input.zem-col:checked').each( function() {
                cml += '{{' + ZemantaExtension.util.convert2SafeName($(this).attr('value')) + '}}' + '<br/>';
        });

        return cml;
};


ZemantaCrowdSourcingExtension.util.loadAllExistingJobs = function(getJobs) {
        $.post(
                        "command/crowdsourcing/preview-crowdflower-jobs",
                        { 
                        },
                        function(data)
                        {
                                if(data != null) {  	  			  
                                        if(data.status != "ERROR") {
                                                getJobs(data['jobs'],data.status);
                                        } else{
                                                //ZemUtil.showErrorDialog("An error occured while loading existing jobs.\n" + data.message);  
                                                console.log("An error occured while loading existing jobs. Details: " + data.message);
                                                getJobs([], data.message);
                                        }
                                }
                        },
                        "json"
        );     

};


ZemantaCrowdSourcingExtension.util.copyJob = function(extension, updateJobs) {
        $.post(
                        "command/crowdsourcing/copy-crowdflower-job",
                        {"extension": JSON.stringify(extension)},
                        function(data)
                        {
                                if(data != null && data.status != "ERROR") {
                                        updateJobs(data);
                                } 
                                else {
                                        ZemUtil.showErrorDialog("Could not refresh the job list.\n" + data.message);
                                }
                        },
                        "json"
        );     
};


ZemantaCrowdSourcingExtension.util.getJobInfo = function(extension, updateJobInfo) {

        $.post(
                        "command/crowdsourcing/get-crowdflower-job",
                        {"extension": JSON.stringify(extension)},
                        function(data)
                        {
                                if(data != null && data.status != "ERROR") {
                                        updateJobInfo(data);
                                } else {
                                        ZemUtil.showErrorDialog("Error occured while updating job information.\n" + data.message);
                                }
                        },
                        "json"
        );     
};

ZemUtil.showErrorDialog = function (message) {
        var dlg = $('<div class="lodrefine" id="dialog-confirm" title="An error occured">' +
                            '<p class="text-error"><i class="icon-exclamation-sign"></i> &nbsp; &nbsp;' +
                            '<strong>An error occured. Error message: </strong></p> <p>' +
                            message +
            '</p></div>');
        
        dlg.dialog({
                resizable: false,
                height:200,
                modal: true,
                buttons: {
                        "OK": function() {
                                $( this ).dialog( "close" );
                        }
                }
        });

};

ZemUtil.showConfirmation = function (title, message) {
        var dlg = $(DOM.loadHTML("crowdsourcing", "scripts/dialogs/confirmation-dialog.html"));
        dlg._elmts = DOM.bind(dlg);
        
        dlg._elmts.dlgTitle.text(title);
        dlg._elmts.dlgMessage.text(message);
        
        dlg.dialog({
                resizable: false,
                height:200,
                modal: true,
                buttons: {
                        "OK": function() {
                                $( this ).dialog( "close" );
                        }
                }
        });
};


