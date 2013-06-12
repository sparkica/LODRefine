function ZemCFImgReconDialog(onDone) {
        this._onDone = onDone;
        this._extension = {};
        this._mappedFields = [];
        this._fields = [];
        var dismissBusy = DialogSystem.showBusy();

        this._dialog = $(DOM.loadHTML("crowdsourcing", "scripts/dialogs/crowdflower-img-recon-dialog.html"));
        this._elmts = DOM.bind(this._dialog);
        this._elmts.dialogHeader.text("Reconciling images");

        var self = this;

        this._elmts.jobTabs.tabs();
        self._renderAllExistingJobs();

        self._renderColumns();
        //self._elmts.goldDataPanel.hide();

        this._elmts.okButton.click(function() {
                self._extension = {};
                self._extension.content_type = "json";
                self._extension.column_names = [];
                var template = self._elmts.reconTemplates.children(":selected").val();

                self._extension.recon_service = template;

                if(template === "--- no template ---") {
                        alert("You have to choose recon template first.");
                        DialogSystem.dismissUntil(self._level - 1);
                }

                //add mappings for anchor, link and recon column

                self._extension.job_id =  self._elmts.allJobsList.children(":selected").val();

                var tmp = {};
                tmp.name = $('option[name=anchor]:selected').val();
                tmp.safe_name = 'anchor';
                self._extension.column_names.push(tmp);

                var tmp1 = {};
                tmp1.name = $('option[name=link]:selected').val();
                tmp1.safe_name = 'link';
                self._extension.column_names.push(tmp1);

                if($('#upload-gold').is(':checked')) {
                        self._extension.golden_column = $('option[name=gold2]:selected').val();
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



        this._elmts.cancelButton.click(function() {	  
                DialogSystem.dismissUntil(self._level - 1);    

        });


        dismissBusy();
        this._level = DialogSystem.showDialog(this._dialog);

};


ZemCFImgReconDialog.prototype._renderColumns = function() {

        var self = this;
        var columns = theProject.columnModel.columns;
        var columnListContainer = self._elmts.projectColumns;

        var imgUrl = self._elmts.imgUrlColumn;
        var imgDesc = self._elmts.imgDescColumn;
        var destUrl = self._elmts.destUrlColumn;
        var gold = self._elmts.goldDataColumn;
        var info = self._elmts.infoField;

        var renderColumns = function(columns, elem) {

            $.each(columns, function(index, value){         
                imgUrl.append($('<option value="' + value.name + '" name="anchor">'+ value.name + '</option>'));
                imgDesc.append($('<option value="' + value.name + '" name="link">'+ value.name + '</option>'));
                destUrl.append($('<option value="' + value.name + '" name="link">'+ value.name + '</option>'));
                info.append($('<input type="checkbox" value="' + value.name + '" >'+ value.name + '</checkbox>'));
                gold.append($('<option value="' + value.name + '" name="gold2">'+ value.name + '</option>'));
            });
        };

        renderColumns(columns, columnListContainer);

    };



ZemCFImgReconDialog.prototype._renderAllExistingJobs = function() {

        var self = this;
        var selContainer = self._elmts.allJobsList;
        var elemStatus = self._elmts.statusMessage;

        $('<option name="opt_none" value="none">--- select a job --- </option>').appendTo(selContainer);


        ZemantaCrowdSourcingExtension.util.loadAllExistingJobs(function(data, status) {

                if(status === "OK" | status === 200) {
                        elemStatus.html("Jobs are loaded.");
                } else {
                        elemStatus.html("There was an error loading jobs. Error message: <br/>" + status);
                }

                $.each(data, function(index, value) {

                        var title = (value.title == null)? "Title not defined" : value.title;
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
