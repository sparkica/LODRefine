
function ZemantaCFEvaluateReconDialog(onDone) {
        this._onDone = onDone;
        this._extension = {};
        this._mappedFields = [];
        this._fields = [];
        var dismissBusy = DialogSystem.showBusy();

        this._dialog = $(DOM.loadHTML("crowdsourcing", "scripts/dialogs/crowdflower-eval-recon-dialog.html"));
        this._elmts = DOM.bind(this._dialog);
        this._elmts.dialogHeader.text($.i18n._('crowd-ext-eval')["header"]);
        this._elmts.crowdext_eval_tab_recon.text($.i18n._('crowd-ext-eval')["tab-recon"]);
        this._elmts.crowdext_eval_upload_header.text($.i18n._('crowd-ext-eval')["upload-header"]);
        this._elmts.crowdext_eval_job_header.text($.i18n._('crowd-ext-eval')["job-header"]);
        this._elmts.label_sel_job.text($.i18n._('crowd-ext-eval')["select-job"]);
        this._elmts.label_sel_template.text($.i18n._('crowd-ext-eval')["select-template"]);
        this._elmts.optNoTemplate.text($.i18n._('crowd-ext-eval')["no-template"]);
        this._elmts.optFreebaseTemplate.text($.i18n._('crowd-ext-eval')["freebase-template"]);
        this._elmts.optDBpediaTemplate.text($.i18n._('crowd-ext-eval')["dbpedia-template"]);
        this._elmts.crowdext_eval_service_warning.text($.i18n._('crowd-ext-eval')["service-warning"]);
        this._elmts.mapHeader.text($.i18n._('crowd-ext-eval')["map-header"]);
        this._elmts.entNameLabel.text($.i18n._('crowd-ext-eval')["ent-name"]);
        this._elmts.entUrlLabel.text($.i18n._('crowd-ext-eval')["ent-url"]);
        this._elmts.reconColHeader.text($.i18n._('crowd-ext-eval')["recon-col-header"]);
        this._elmts.selColLabel.text($.i18n._('crowd-ext-eval')["sel-col"]);
        this._elmts.goldHeader.text($.i18n._('crowd-ext-eval')["gold-header"]);
        this._elmts.goldInfo.text($.i18n._('crowd-ext-eval')["gold-info"]);
        this._elmts.uploadgoldLabel.text($.i18n._('crowd-ext-eval')["gold-upload"]);
        this._elmts.selColGold.text($.i18n._('crowd-ext-eval')["sel-col"]);
        this._elmts.additionalColsLabel.text($.i18n._('crowd-ext-eval')["additional-cols"]);
        this._elmts.additionalColsInfo.text($.i18n._('crowd-ext-eval')["additional-cols-info"]);
        this._elmts.selAdditionalCols.text($.i18n._('crowd-ext-eval')["sel-cols"]);
        this._elmts.okButton.text($.i18n._('crowd-ext-buttons')["ok"]);
        this._elmts.cancelButton.text($.i18n._('crowd-ext-buttons')["cancel"]);
        this._elmts.optionsLabel.text($.i18n._('crowd-ext-eval')["options"]);
        this._elmts.jobTabs.tabs();

        var self = this;

        self._renderAllExistingJobs();
        self._renderColumns();
        self._elmts.goldDataPanel.hide();

        this._elmts.okButton.click(function() {
                self._extension = {};
                self._extension.content_type = "json";
                self._extension.column_names = [];
                var template = self._elmts.reconTemplates.children(":selected").val();

                self._extension.recon_service = template;

                if(template === $.i18n._('crowd-ext-eval')["no-template"]) {
                        ZemUtil.showErrorDialog($.i18n._('crowd-ext-eval')["error-no-template"], '.dialog-frame');
                        DialogSystem.dismissUntil(self._level - 1);
                }

                //add mappings for anchor, link and recon column

                self._extension.job_id =  self._elmts.allJobsList.children(":selected").val();

                var tmp = {};
                tmp.name = $('option[name=anchor]:selected').val();
                tmp.safe_name = 'anchor';
                self._extension.column_names.push(tmp);

                tmp.name = $('option[name=link]:selected').val();
                tmp.safe_name = 'link';
                self._extension.column_names.push(tmp);

                if($('#upload-gold').is(':checked')) {
                        self._extension.golden_column = $('option[name=gold2]:selected').val();;
                } 

                self._extension.recon_column = $('option[name=reconCol]:selected').val();      

                $('#info-fields input:checked').each( function() {
                        var col = {};
                        col.name = $(this).attr('value');
                        col.safe_name = ZemantaCrowdSourcingExtension.util.convert2SafeName(col.name);
                        self._extension.column_names.push(col);
                });

                DialogSystem.dismissUntil(self._level - 1);
                self._onDone(self._extension);
        });

        this._elmts.uploadGoldBtn.change(function () {
                var checked = $('#upload-gold').is(':checked');

                if(checked) {
                        self._elmts.goldDataPanel.show();
                }
                else {
                        self._elmts.goldDataPanel.hide();
                }
        });

        this._elmts.cancelButton.click(function() {	  
                DialogSystem.dismissUntil(self._level - 1);    
        });

        dismissBusy();
        this._level = DialogSystem.showDialog(this._dialog);

};



ZemantaCFEvaluateReconDialog.prototype._renderColumns = function() {

        var self = this;
        var columns = theProject.columnModel.columns;
        var columnListContainer = self._elmts.projectColumns;

        var anchor = self._elmts.anchorField;
        var link = self._elmts.linkField;
        var info = self._elmts.infoField;
        var gold2 = self._elmts.goldColumn2;
        var reconCol = self._elmts.reconColumns;

        var renderColumns = function(columns, elem) {

                $.each(columns, function(index, value){			
                        if(value.reconConfig != null) {
                                reconCol.append($('<option value="' + value.name + '" name="reconCol">'+ value.name + '</option>'));					
                        }

                        anchor.append($('<option value="' + value.name + '" name="anchor">'+ value.name + '</option>'));
                        link.append($('<option value="' + value.name + '" name="link">'+ value.name + '</option>'));
                        info.append($('<input type="checkbox" value="' + value.name + '" >'+ value.name + '</checkbox>'));
                        gold2.append($('<option value="' + value.name + '" name="gold2">'+ value.name + '</option>'));
                });
        };

        renderColumns(columns, columnListContainer);

};

ZemantaCFEvaluateReconDialog.prototype._renderAllExistingJobs = function() {

        var self = this;
        var selContainer = self._elmts.allJobsList;
        var elemStatus = self._elmts.statusMessage;

        $('<option name="opt_none" value="none">' + $.i18n._('crowd-ext-eval')["select-job-opt"] + '</option>').appendTo(selContainer);

        ZemantaCrowdSourcingExtension.util.loadAllExistingJobs(function(data, status, message) {
                
                if(status == 'ERROR') {
                        elemStatus.removeClass('text-success');
                        elemStatus.addClass('text-error');
                        elemStatus.html( $.i18n._('crowd-ext-eval')["error-loading"] + message);
                } else {
                        elemStatus.removeClass('text-error');
                        elemStatus.addClass('text-success');
                        elemStatus.html($.i18n._('crowd-ext-eval')["success-loading"] );
                }

                $.each(data, function(index, value) {

                        var title = (value.title == null)? $.i18n._('crowd-ext-eval')["title-undefined"]  : value.title;
                        var job = $('<option name="opt_' + index + '" value=' + value.id + '>' + title + ' (job id: ' + value.id + ')</option>');
                        selContainer.append(job);
                });

                selContainer.change(function() {
                        this._extension = {};
                        this._extension.job_id = $(this).children(":selected").val();
                        this._selectedJob = this._extension.job_id;

                });
        });
};

