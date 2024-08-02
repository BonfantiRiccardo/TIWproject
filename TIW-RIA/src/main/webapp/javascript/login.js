 /**
 * Login management
 */

(function() { // avoid variables ending up in the global scope

  document.getElementById("loginbutton").addEventListener('click', (e) => {	//add on click event listener to login button
    var form = e.target.closest("form");
    if (form.checkValidity()) {												//check the form content validity
      makeCall("POST", 'CheckLogin', e.target.closest("form"),				//the makecall function sends the data to the server
        function(x) {
          if (x.readyState == XMLHttpRequest.DONE) {						//check the request state if it's DONE (4) then execute:
            var message = x.responseText;
            switch (x.status) {												//check the response status
              case 200:
            	sessionStorage.setItem('username', message);				//if it is success, do log in
                window.location.href = "HomePage.html";
                break;
              case 400: // bad request										//in any other case notify user
                document.getElementById("errorMessageLogin").textContent = message;
                break;
              case 401: // unauthorized
                  document.getElementById("errorMessageLogin").textContent = message;
                  break;
              case 500: // server error
            	document.getElementById("errorMessageLogin").textContent = message;
                break;
            }
          }
        }
      );
    } else {
    	 form.reportValidity();												//if the content of the form is not valid notify the user
    }
  });

})();