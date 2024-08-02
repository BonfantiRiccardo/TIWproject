 /**
 * Registration management
 */

(function() { // avoid variables ending up in the global scope

  document.getElementById("registerbutton").addEventListener('click', (e) => {	//add on click event listener to registration button
    var form = e.target.closest("form");
    var formData = new FormData(form);
    if (form.checkValidity() && formData.get("password") == formData.get("repeatedPassword")) {	//check the form content validity
      makeCall("POST", 'Registration', e.target.closest("form"),			//the makecall function sends the data to the server
        function(x) {
          if (x.readyState == XMLHttpRequest.DONE) {						//check the request state if it's DONE (4) then execute:
            var message = x.responseText;
            switch (x.status) {												//check the response status
              case 200:
            	sessionStorage.setItem('username', message);				//if it is success, do log in
                window.location.href = "HomePage.html";
                break;
              case 400: // bad request										//in any other case notify user
                document.getElementById("errorMessageRegistration").textContent = message;
                break;
              case 401: // unauthorized
                  document.getElementById("errorMessageRegistration").textContent = message;
                  break;
              case 500: // server error
            	document.getElementById("errorMessageRegistration").textContent = message;
                break;
            }
          }
        }
      );
    } else if (formData.get("password") != formData.get("repeatedPassword")) {		//notify error in parameters
        document.getElementById("errorMessageRegistration").textContent = "Il campo password e ripeti password devono essere uguali";
    } else {
    	 form.reportValidity();												//if the content of the form is not valid notify the user
    }
  });

})();