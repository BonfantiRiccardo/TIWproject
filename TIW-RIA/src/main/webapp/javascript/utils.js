/**
 * AJAX call management
 */

	function makeCall(method, url, formElement, cback, reset = true) {
	    var req = new XMLHttpRequest(); 					// visible by closure
	    req.onreadystatechange = function() {				//every time the states changes invoke the callback passed as parameter
	      cback(req)
	    }; // closure
	    req.open(method, url);								//prepare request to send to server
	    if (formElement == null) {
	      req.send();										//send empty if formElement is null (should not happen)
	    } else {
	      req.send(new FormData(formElement));				//send data if form is ok
	    }
	    if (formElement !== null && reset === true) {		//reset the form fields
	      formElement.reset();
	    }
	}
