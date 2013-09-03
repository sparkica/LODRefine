function SindiceDialog(){
	
}

SindiceDialog.prototype.show = function(column){
	var self = this;
	self._column = column;
    var frame = DialogSystem.createDialog();
    frame.width("400px");
    
    var header = $('<div></div>').addClass("dialog-header").text($.i18n._('rdf-ext-sindice')["related"]).appendTo(frame);
    var body = $('<div class="grid-layout layout-full"></div>').addClass("dialog-body").appendTo(frame);
    var footer = $('<div></div>').addClass("dialog-footer").appendTo(frame);
    
    var html = $(
    		'<div class="" ><span>'+$.i18n._('rdf-ext-sindice')["domain-list"]+':</span><div class="rdf-extension-sindice-domain-container" bind="domains_container"></div></div>'    		
    ).appendTo(body);
    
    self._elmts = DOM.bind(html);
    
    self._level = DialogSystem.showDialog(frame);
    self.guessDomain();
    self._footer(footer);
};

SindiceDialog.prototype.guessDomain = function(column){
	var self = this;
	var dismissBusy = DialogSystem.showBusy($.i18n._('rdf-ext-sindice')["find-related"]);
	$.post("command/rdf-extension/sindiceGuessType",{"project":theProject.id,"columnName":self._column.name},function(data){
		dismissBusy();
		if(data.code==='error'){
			alert(data.message);
		}else{
			
			if(data.domains.length==0){
				self._elmts.domains_container.text($.i18n._('rdf-ext-sindice')["no-domain"]);
			}else{
				for(var i=0;i<data.domains.length;i++){
					var domain = data.domains[i];
					var option = $('<input />').attr('type','radio').attr('value',domain).attr('name','domain_radio').attr('checked',i==0);
					self._elmts.domains_container.append($('<div>').append(option).append($('<span/>').text(domain)));
				}
			}
		}
	});
};

SindiceDialog.prototype._footer = function(footer){
	var self = this;
	$('<button></button>').addClass('button').text($.i18n._('rdf-ext-buttons')["cancel"]).click(function() {
        DialogSystem.dismissUntil(self._level - 1);
    }).appendTo(footer);
	
	$('<button></button>').addClass('button').text($.i18n._('rdf-ext-buttons')["add-sindice"]).click(function() {
		var domain = self._elmts.domains_container.find('input[name="domain_radio"]:checked').val();
		if(!domain){
			alert($.i18n._('rdf-ext-sindice')["sel-domain"]);
			return;
		}
		$.post("command/rdf-extension/addSindiceService",{"domain":domain},function(data){
			RdfReconciliationManager.registerService(data,self._level);
		},"json");
    }).appendTo(footer);
};
