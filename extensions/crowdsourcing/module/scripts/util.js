ZemantaCrowdSourcingExtension.util.loadCrowdFlowerApiKeyFromSettings = function(getApiKey) {
        $.post(
                        "/command/core/get-all-preferences",
                        {},
                        function (data) {
                                if (data != null && data["crowdflower.apikey"] != null) {

                                        getApiKey(data["crowdflower.apikey"]);
                                }
                                else {
                                        ZemUtil.showErrorDialog($.i18n._('crowd-ext-api')["error"]);
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
                                if(data) {  	  			  
                                        if(data.status != "ERROR") {
                                                getJobs(data['jobs'],data.status, "");
                                        } else{
                                                getJobs([], data.status, data.message);
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
                                if(data != null && data.status.toLowerCase() != "error") {
                                        updateJobs(data);
                                } 
                                else {
                                        ZemUtil.showErrorDialog($.i18n._('crowd-ext-util')["error-copy"]+ data.message);
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
                                if(data != null && data.status.toLowerCase() != "error") {
                                        updateJobInfo(data);
                                } else {
                                        ZemUtil.showErrorDialog($.i18n._('crowd-ext-util')["error-get-info"] + data.message);
                                }
                        },
                        "json"
        );     
};



ZemUtil.renderColumns = function(columns, elem, tabindex) {

        var chkid = 0;
        $.each(columns, function(index, value){
                var id = 'chk_' + tabindex + '_' + chkid;
                $('<input type="checkbox" class="zem-col" value="' + value.name + '" id="' + id + '"/>').appendTo(elem);
                $('<label for="' + id + '">' + value.name + '</label> <br/>').appendTo(elem);
                chkid++;
        });

};



ZemUtil.showErrorDialog = function (message, parent) {

        var dlg = $(DOM.loadHTML("crowdsourcing", "scripts/dialogs/confirmation-dialog.html"));
        dlg._elmts = DOM.bind(dlg);

        dlg._elmts.dlgTitle.addClass('text-error');
        dlg._elmts.dlgTitle.text($.i18n._('crowd-ext-util')["dlg-title"]);
        dlg._elmts.dlgMessage.addClass('text-error');
        dlg._elmts.dlgMessage.text(message);


        dlg.dialog({
                resizable: false,
                height:200,
                appendTo: $(parent),
                modal: true,
                buttons: {
                        "OK": function() {
                                $( this ).dialog( "close" );
                        }
                }
        });

};

ZemUtil.showConfirmation = function (title, message, parent) {
        var dlg = $(DOM.loadHTML("crowdsourcing", "scripts/dialogs/confirmation-dialog.html"));
        dlg._elmts = DOM.bind(dlg);

        dlg._elmts.dlgTitle.addClass('text-success');
        dlg._elmts.dlgTitle.text(title);
        dlg._elmts.dlgMessage.addClass('text-success');
        dlg._elmts.dlgMessage.text(message);

        dlg.dialog({
                resizable: false,
                height:200,
                appendTo: $(parent),
                modal: true,
                buttons: {
                        "OK": function() {
                                $( this ).dialog( "close" );
                        }
                }
        });
};

//load text from file - for template loading
ZemUtil._loadedText = {};
ZemUtil.loadText = function(module, path) {
        var fullPath = (ModuleWirings[module] + path).substring(1);
        if (!(fullPath in ZemUtil._loadedText)) {
                $.ajax({
                        async: false,
                        url: fullPath,
                        dataType: "text",
                        success: function(text) {
                                ZemUtil._loadedText[fullPath] = text;
                        }
                });
        }
        return ZemUtil._loadedText[fullPath];
};
