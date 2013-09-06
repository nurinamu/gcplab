var GCPLabUtil = (function(){
	
	var toastObj;

	GCPLabUtil.toast = function(msg, kind, duration){
		if(!toastObj){
			toastObj = $('<div id=\"msgToast\" style=\"align:center;\"></div>').appendTo("body");
		}

		if(!kind){
			kind = 'success';
		}

		toastObj.html("<div class='alert alert-"+kind+"' style='padding:10px 14px 10px' align='center'>"+msg+"</div>");

		var leftVal = window.innerWidth/2-($('div',toastObj).width()/2);
		
		toastObj.css({
			'position':'fixed',
			'display':'block',
			'z-index':9999,
			'top' : 10,
			'left':leftVal
		});

		toastObj.fadeIn(300);

		if(duration){
			setTimeout('GCPLabUtil.closeToast()',duration);
		}else{
			setTimeout('GCPLabUtil.closeToast()',3000);
		}

	}

	GCPLabUtil.closeToast = function(){
		toastObj.fadeOut(300);
	}
	
	GCPLabUtil.encode = function(s) {
		s = encodeURIComponent (s);
		s = s.replace (/\~/g, '%7E').replace (/\!/g, '%21').replace (/\(/g, '%28').replace (/\)/g, '%29').replace (/\'/g, '%27');
		s = s.replace (/%20/g, '+');
		return s;
	}

	GCPLabUtil.decode = function(s) {
		s = s.replace (/\+/g, '%20');
		s = decodeURIComponent (s);
		return s;
	}
	
	
	function GCPLabUtil(){};
	
	return GCPLabUtil;
})();

