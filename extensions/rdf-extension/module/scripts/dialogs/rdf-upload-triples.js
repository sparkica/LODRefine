function RdfUploadTriplesDialog(onDone) {
	this._onDone = onDone;
	this._params = {};
	var self = this;
	this._dialog = $(DOM.loadHTML("rdf-extension", "scripts/dialogs/rdf-upload-triples.html"));
	this._elmts = DOM.bind(this._dialog);
	//var dismissBusy = DialogSystem.showBusy();

	console.log("initiating...");

	this._elmts.okButton.click(function() {

		self._params.endpoint = {}
		self._params.endpoint.name = self._elmts.endpointName.val();
		self._params.endpoint.url = self._elmts.endpointUrl.val();
		self._params.endpoint.type = self._elmts.endpointTypes.children(":selected").val();
		self._params.endpoint.auth = self._elmts.authMethod.children(":selected").val();
		self._params.endpoint.graph = self._elmts.defaultGraph.val();
		
		//TODO: what if we don't want default graph
		self._params.graph = self._elmts.defaultGraph.val();
		self._params.existing = false;
		
		//TODO: what about credentials?
		self._params.endpoint.username = self._elmts.userName.val();
		self._params.endpoint.password = self._elmts.userPwd.val();
		
		
		//how to store credentials? encode them and then decode them?
		
		if ($('#config-creds').is(':checked')) {
			alert("Username and password are provided at the installation time. " +
					"They should be looked up in config file.")
		}
		
		
		if(!theProject.overlayModels.rdfSchema)
		{
			alert("No schema defined! Please define schema first.");
			DialogSystem.dismissUntil(self._level - 1);
		}
		
		//schema is added later
		
		console.log("Parameters: ");
		console.log(self._params);
		self._onDone(self._params);
		DialogSystem.dismissUntil(self._level - 1);
			
			
	});

	this._elmts.cancelButton.click(function() {
		DialogSystem.dismissUntil(self._level - 1);
	});
	
	this._elmts.endpointsList.change(function () {
		self._elmts.endpointUrl.val(self._elmts.endpointsList.children(":selected").val())
	});
	
	this._elmts.saveEndpointButton.click(function () {
		//TODO: set up endpoint manager
		alert("storing the endpoint or something");
		//post request to store it all
	});

	this._level = DialogSystem.showDialog(this._dialog);
	//dismissBusy();
}


RdfUploadTriplesDialog.prototype.uploadData = function() {
	var self = this;
}