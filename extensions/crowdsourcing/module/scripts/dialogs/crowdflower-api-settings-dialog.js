function ZemantaSettingsDialog(onDone) {
  this._onDone = onDone;
  this._settings = {};
  this._settings.apiKey = "";
  this._settings.defaultTimeout = "1500";

  var self = this;
  this._dialog = $(DOM.loadHTML("crowdsourcing", "scripts/dialogs/crowdflower-api-settings-dialog.html"));
  this._elmts = DOM.bind(this._dialog);
  this._elmts.dialogHeader.text($.i18n._('crowd-ext-api')["header"]);
  this._elmts.crowdext_api_settings.text($.i18n._('crowd-ext-api')["settings"]);
  this._elmts.crowdext_registration.html($.i18n._('crowd-ext-api')["register"]);
  this._elmts.crowdext_api_label_key.text($.i18n._('crowd-ext-api')["label-key"]);
  this._elmts.crowdext_api_label_timeout.text($.i18n._('crowd-ext-api')["label-timeout"]);
  this._elmts.crowdext_api_timeout_notice.text($.i18n._('crowd-ext-api')["timeout-notice"]);
  this._elmts.okButton.text($.i18n._('crowd-ext-buttons')["ok"]);
   this._elmts.cancelButton.text($.i18n._('crowd-ext-buttons')["cancel"]);

  this._elmts.okButton.click(function() {
   
	var apiKey = self._elmts.apiKeyInput.val();
	var defaultTimeout = self._elmts.defaultTimeoutInput.val();
	  
	if (apiKey === "") {
      alert($.i18n._('crowd-ext-api')["key-warning"]);
    } else {
      self._settings.apiKey = apiKey;
      
      if(defaultTimeout != "") {
      	self._settings.defaultTimeout = defaultTimeout;
      }
      DialogSystem.dismissUntil(self._level - 1);
      self._onDone(self._settings);
    }
  });
  this._elmts.cancelButton.click(function() {
	    DialogSystem.dismissUntil(self._level - 1);
	  });
  
  this._level = DialogSystem.showDialog(this._dialog);

}