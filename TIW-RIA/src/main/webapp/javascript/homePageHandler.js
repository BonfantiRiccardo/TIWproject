/**
 * Handles the dynamic and partial update of the home page
 */
{ // avoid variables ending up in the global scope

	//page components
	let directoryTree, documentDetails, wizard, bin,		//variables visible to all the file
	pageOrchestrator = new PageOrchestrator(); 				// main controller

	window.addEventListener("load", () => {				//ONLOAD, IF USER IN SESSION IS NULL REDIRECT TO INDEX
		if (sessionStorage.getItem("username") == null) {
			window.location.href = "index.html";
		} else {											//OTHERWISE START PAGE ORCHESTRATION
			pageOrchestrator.start();
			pageOrchestrator.refresh();						//refresh to INITIALIZE VIEW COMPONENTS
		} 													//display initial content
	}, false);


	// Constructors of view components

	function WelcomeMessage(_username, messagecontainer) {	//constructor of the welcome message object
		this.username = _username;							//saves the username of the session
		this.show = function() {
			messagecontainer.textContent = this.username;	//function to show the username in the messagecontainer
		}
	}

	function DirectoryTree(_alert, _treecontainer) {		// constructor of the MAIN TREE of directories and documents
		this.alert = _alert;								// reference to the alert box object (display messages)
		this.treeContainer = _treecontainer;				// actual container of the tree
		this.draggingDocument;								// reference to the element I am currently dragging
		this.draggingDirectory;

		this.reset = function() {							//reset function to hide the tree of elements
			this.treeContainer.style.visibility = "hidden";
		};

		this.show = function(next) {						//function to show the elements tree, next is the function that should
			var self = this;		// save self for visibility									  be executed after this
			makeCall("GET", "GetDirectories", null,			//call GetDirectories will return the directories of the user
				function(req) {
					if (req.readyState == 4) {					//wait for state 4 (request complete)
						var message = req.responseText;
						if (req.status == 200) {				//if request was successfull then:
							var topDirectories = JSON.parse(req.responseText);
							if (topDirectories.length == 0) {			// if there aren't top directories, set alert message and finish
								self.alert.textContent = "Non hai ancora creato nessuna cartella";
								documentDetails.hide();					//hide document details table and return
								return;
							}
							var documents;						//otherwise make a new request to GetDocuments
							makeCall("GET", "GetDocuments", null,
								function(req) {
									if (req.readyState == 4) {			//wait for state 4 (request complete)
										var message = req.responseText;
										if (req.status == 200) {			//if request was successfull then:
											documents = JSON.parse(req.responseText);

											self.update(topDirectories, documents); //update the tree and show it
											if (next) next(); 	//show the first document of the list if present (execute next function)
										} else if (req.status == 403) {			//trying to access forbidden information
											window.location.href = req.getResponseHeader("Location");
											window.sessionStorage.removeItem('username');	//redirected to login page
										}
										else {								//something went wrong, alert message updated and shown
											self.alert.textContent = message;
										}
									}
								}
							);

						} else if (req.status == 403) {			//trying to access forbidden information, redirected to login page
							window.location.href = req.getResponseHeader("Location");
							window.sessionStorage.removeItem('username');
						}
						else {									//something went wrong, alert message updated and shown
							self.alert.textContent = message;
						}
					}
				}
			);
		};


		this.update = function(topDirectories, documents) {		//function to update the tree of elements (given directories and docs)
			this.treeContainer.innerHTML = ""; 					//empty the tree by deleting all the html inside
			var self = this;
			
			var populateTree = function(directory, appendHere, documents) { 	//self visible here, not this
				var listElement, buttonCreateDir, buttonCreateDoc, docElement, docAnchor;	//local variables
				listElement = document.createElement("li");						//create list element

				listElement.appendChild(document.createTextNode(directory.name));	//insert text into list
				listElement.setAttribute("draggable", true);						//MAKE ELEMENT DRAGGABLE	
				listElement.setAttribute('directoryid', directory.id); 				//set custom HTML attribute "directoryid"
				listElement.classList.add("folder");
				
				//ADD DRAG & DROP EVENTS LINKED TO THE DIRECTORY
				listElement.addEventListener("dragstart", (e) => {  					//add event dragstart for listElement
					self.alert.textContent = "Stai spostando la cartella";			//print info
					self.draggingDirectory = e.target.getAttribute("directoryid");	//save id of the directory I am dragging
				}, false);
				listElement.addEventListener("dragend", (e) => {  					//add event dragend for listElement
					self.alert.innerHTML = "";					//clear the alert and the reference to the directory
					self.draggingDirectory = undefined;			//I was dragging
				}, false);
				listElement.addEventListener("dragover", (e) => {  					//add event dragover for listElement
					if (e.target.getAttribute("directoryid") != null && self.draggingDocument != undefined) {
						e.preventDefault();
						e.target.classList.add("movingTo");			//if I drag a document over the directory, highlight it
					}
				}, false);
				listElement.addEventListener("dragleave", (e) => {			//add event dragleave for listElement
					e.preventDefault();
					e.target.classList.remove("movingTo");						//if I drag a document away from directory, return to normal
				}, false);
				listElement.addEventListener("drop", (e) => {				//add event drop for listElement
					e.stopPropagation();
					e.target.classList.remove("movingTo");
					if (self.draggingDocument != undefined) 
						self.moveDocument(e.target.getAttribute("directoryid")); //call move document function
				}, false);
				
				buttonCreateDir = document.createElement("input");				//create button element
				listElement.appendChild(document.createTextNode("		"));		//add some space
				listElement.appendChild(buttonCreateDir);					//insert button into list and insert text into it
				buttonCreateDir.setAttribute("type", "button");
				buttonCreateDir.classList.add("button");
				buttonCreateDir.setAttribute("value", "Crea sottocartella");
				
				//anchor.directoryid = directory.id;		 			make list item clickable
				buttonCreateDir.setAttribute('directoryid', directory.id); 	// set a custom HTML attribute "directoryid"
				buttonCreateDir.addEventListener("click", (e) => {  		//add event onclick to button
					wizard.show(e.target.getAttribute("directoryid"), 2); 	// show the form to create subdirectory
				}, false);
				buttonCreateDir.href = "#";
				
				buttonCreateDoc = document.createElement("input");				//create button element
				listElement.appendChild(document.createTextNode("		"));		//add some space
				listElement.appendChild(buttonCreateDoc);					//insert button into list and insert text into it
				buttonCreateDoc.setAttribute("type", "button");
				buttonCreateDoc.classList.add("button");
				buttonCreateDoc.setAttribute("value", "Crea documento");
				
				//anchor.directoryid = directory.id;					make list item clickable
				buttonCreateDoc.setAttribute('directoryid', directory.id); 	// set a custom HTML attribute "directoryid"
				buttonCreateDoc.addEventListener("click", (e) => {  		//add event onclick to anchor
					wizard.show(e.target.getAttribute("directoryid"), 3); 	// show the form to create document
				}, false);
				buttonCreateDoc.href = "#";
				
				var subList = document.createElement("ul");					//create new list where subdirs and docs will be appended
				if (directory.subdirectories != undefined && directory.subdirectories.length > 0) {	//if there are subdirectories 
					listElement.appendChild(subList);
					for (let j = 0; j < directory.subdirectories.length; j++) {
						populateTree(directory.subdirectories[j], subList, documents);		//recursively call populateTree function
					}
				}
				
				documents.forEach(function(doc) {						//for each document do:
					let found = false;
					if (directory.id == doc.fatherDirectory) {			//if the current directory is the father
						if(!found) {
							listElement.appendChild(subList);			//append the subList to the listElement
							found = true;
						}
						docElement = document.createElement("li");		//create element of the list
						docElement.setAttribute("draggable", true);		//make element draggable
						docElement.classList.add("doc");
						docElement.addEventListener("dragstart", (e) => {				//add event on dragstart  
							e.stopPropagation();
							self.alert.textContent = "Stai spostando il documento";		//print info
							self.draggingDocument = e.target.getAttribute("documentid");	//save id of the document I am dragging
						}, false);
						docElement.addEventListener("dragend", (e) => {  
							self.alert.innerHTML = "";					//clear the alert and the reference to the document I
							self.draggingDocument = undefined;			//am dragging
						}, false);
						
						docAnchor = document.createElement("a");			//create anchor element
						docElement.appendChild(docAnchor);					//insert anchor into docElement and insert text
						link4Text = document.createTextNode(doc.name);
						docAnchor.appendChild(link4Text);

						//anchor.documentid = doc.id; 					make list item clickable
						docAnchor.setAttribute('documentid', doc.id); 		// set a custom HTML attribute "documentid"
						docAnchor.addEventListener("click", (e) => {  		// add event on click to docAnchor
							documentDetails.show(e.target.getAttribute("documentid")); // show the details of the target document
						}, false);
						docAnchor.href = "#";
						
						subList.appendChild(docElement);
					}
				});
				
				appendHere.appendChild(listElement);
			};
			
			// build updated tree
			for (let i = 0; i < topDirectories.length; i++) {
				populateTree(topDirectories[i], this.treeContainer, documents);	//recursively call function to build tree
			}
			this.treeContainer.style.visibility = "visible";					//set the tree container to visible
		};

		this.autoclick = function(documentId) {									//function to automatically show document details
			var e = new Event("click");
			var selector = "a[documentid='" + documentId + "']";	//prepare custom event and link
			var anchorToClick =  									// the first document of the tree or the one with given id
				(documentId) ? document.querySelector(selector) : this.treeContainer.querySelectorAll("a[documentid]")[0];
			if (anchorToClick) anchorToClick.dispatchEvent(e);
		};

		this.moveDocument = function(directoryId) {				//function to call servlet that moves document
			var self = this;
			if (directoryId != null) {							//check that we actually dragged a document on a directory
				makeCall("GET", "MoveDocToDir?documentid=" + self.draggingDocument + "&directoryid=" + directoryId, null,
					function(req) {
						if (req.readyState == 4) {					//wait for state 4
							var message = req.responseText;
							if (req.status == 200) {				//if request was successfull then:
								var moved = JSON.parse(req.responseText);
							
								pageOrchestrator.refresh(moved.id); //refresh page, next function displays document moved
							}
							else if (req.status == 403) {			//trying to access forbidden information, redirected to login page
								window.location.href = req.getResponseHeader("Location");
								window.sessionStorage.removeItem('username');
							}
							else {									//something went wrong, alert message updated and shown
								self.alert.textContent = message;
							}

							self.draggingDocument = undefined;		//reset dragged document
						}
					}
				);
			} else {
				self.draggingDocument = undefined;								//previous check should make this state impossible
				self.alert.textContent = "Non puoi spostare un documento qui"
			}
		};
		
		this.deleteDocument = function(){
			var self = this;
			if (bin.draggingDocument != undefined) {				//check that we actually dragged a document in the bin
				makeCall("GET", "DeleteDocument?documentid=" + bin.draggingDocument, null,
					function(req) {
						if (req.readyState == 4) {					//wait for state 4
							var message = req.responseText;
							if (req.status == 200) {				//if request was successfull then:
								pageOrchestrator.refresh(); 		//refresh page
								wizard.hide();						//hide wizard because could reference deleted directories
							}
							else if (req.status == 403) {			//trying to access forbidden information, redirected to login page
								window.location.href = req.getResponseHeader("Location");
								window.sessionStorage.removeItem('username');
							}
							else {									//something went wrong, alert message updated and shown
								self.alert.textContent = message;
							}

							self.draggingDocument = undefined;		//reset dragged document
							bin.draggingDocument = undefined;
						}
					}
				);
			} else {
				self.draggingDocument = undefined;								//previous check should make this state impossible
				self.alert.textContent = "Non puoi spostare un documento qui"
			}
		}
		
		this.deleteDirectory = function() {
			var self = this;
			if (bin.draggingDirectory != undefined) {				//check that we actually dragged a directory in the bin
				makeCall("GET", "DeleteDirectory?directoryid=" + bin.draggingDirectory, null,
					function(req) {
						if (req.readyState == 4) {					//wait for state 4
							var message = req.responseText;
							if (req.status == 200) {				//if request was successfull then:
								pageOrchestrator.refresh(); 		//refresh page
								wizard.hide();						//hide wizard because could reference deleted directories
							}
							else if (req.status == 403) {			//trying to access forbidden information, redirected to login page
								window.location.href = req.getResponseHeader("Location");
								window.sessionStorage.removeItem('username');
							}
							else {									//something went wrong, alert message updated and shown
								self.alert.textContent = message;
							}

							bin.draggingDirectory = undefined;		//reset dragged directory
						}
					}
				);
			} else {
				bin.draggingDirectory = undefined;								//previous check should make this state impossible
				self.alert.textContent = "Non puoi spostare una cartella qui"
			}			
		}
	}
	
	function DocumentDetails(options) {							//constructor that handles the documentDetails table
		this.altert = options['alert'];							//reference to alert container object
		this.name = options['name'];							//reference to table container of field name
		this.creationDate = options['creationDate'];			//reference to table container of field creationDate
		this.summary = options['summary'];						//reference to table container of field summary
		this.type = options['type'];							//reference to table container of field type
		this.creator = options['creator'];						//reference to table container of field creator
		this.fatherName = options['fatherName'];				//reference to table container of field fatherName

		this.show = function(documentid) {						//function to show the details table
			var self = this;
			makeCall("GET", "GetDocumentDetails?documentid=" + documentid, null,		//get document details from server
				function(req) {
					if (req.readyState == 4) {					//when state 4 is reached (request complete)
						var message = req.responseText;
						if (req.status == 200) {						//if request is successfull then:
							var doc = JSON.parse(req.responseText);		//get document data
							self.update(doc); 							//update details table and then show it
							
							document.getElementById("document_content").style.visibility = "visible";
							document.getElementById("hideDetails").style.visibility = "visible";		//button to hide details table
							
						} else if (req.status == 403) {					//Error 403: Forbidden, redirect to login
							window.location.href = req.getResponseHeader("Location");
							window.sessionStorage.removeItem('username');
						}
						else {											//set message to alert container
							self.alert.textContent = message;

						}
					}
				}
			);
			
		};

		this.hide = function() {								//function to hide details table
			document.getElementById("document_content").style.visibility = "hidden";
			document.getElementById("hideDetails").style.visibility = "hidden";
		}
		
		this.update = function(doc) {							//function to update details in the table
			this.name.textContent = doc.name;
			this.creationDate.textContent = doc.creationDate;
			this.summary.textContent = doc.summary;
			this.type.textContent = doc.type;
			this.creator.textContent = doc.creator;
			this.fatherName.textContent = doc.fatherName;
		}
	}
	
	function Wizard(_alert) {									//constructor of the WIZARD (forms to create elements)
		this.alert = _alert;							//reference to alert container
		this.dirForm = document.getElementById("create_directory");		//reference to the form objects
		this.subdirForm = document.getElementById("create_subdirectory");
		this.docForm = document.getElementById("create_document");
		this.hideForms = document.getElementById("hideForms");			//reference to the hide button
		this.fatherDirectory;									//reference to the directory that dispatched the event that activated
																//the form
		this.infoText = document.getElementById("infoText");
		
		this.show = function(elementId, toShow) {						//function to show the selected from
			this.hide();							//reset the form view
			this.fatherDirectory = elementId;		//assign the id of the directory upon which the user clicked
			if (toShow == 1) {										//show form based on the choice, show the hide button
				this.dirForm.style.visibility = "visible";
				this.hideForms.style.visibility = "visible";
				this.infoText.textContent = "Stai creando una nuova cartella";
				this.infoText.style.visibility = "visible";
			} else if (toShow == 2) {
				this.subdirForm.style.visibility = "visible";
				this.hideForms.style.visibility = "visible";
				this.infoText.textContent = "Stai creando una nuova sottocartella";
				this.infoText.style.visibility = "visible";
			} else if (toShow == 3) {
				this.docForm.style.visibility = "visible";
				this.hideForms.style.visibility = "visible";
				this.infoText.textContent = "Stai creando un nuovo documento";
				this.infoText.style.visibility = "visible";
			}
		};
		
		this.hide = function() {								//hide all forms
			this.dirForm.style.visibility = "hidden";
			this.subdirForm.style.visibility = "hidden";
			this.docForm.style.visibility = "hidden";
			this.hideForms.style.visibility = "hidden";
			this.infoText.style.visibility = "hidden";
		}

		this.createDir = function() {							//function to create directory
			var self = this;
			if (this.dirForm.checkValidity()) {
				makeCall("POST", "CreateDirectory", self.dirForm,
					function(req) {
						if (req.readyState == 4) {
							var message = req.responseText;
							if (req.status == 200) {
								directoryTree.show();

							} else if (req.status == 403) {
								window.location.href = req.getResponseHeader("Location");
								window.sessionStorage.removeItem('username');
							}
							else {
								self.alert.textContent = message;
							}
						}
					}
				);
				this.hide();
			} else {
				this.dirForm.reportValidity();		//if the content of the form is not valid notify the user
			}
		}

		this.createSubd = function() {							//function to create subdirectory
			var self = this;
			this.subdirForm.children.fatherId.value = this.fatherDirectory;
			if (this.subdirForm.checkValidity()) {
				makeCall("POST", "CreateSubdirectory", self.subdirForm,
					function(req) {
						if (req.readyState == 4) {
							var message = req.responseText;
							if (req.status == 200) {
								directoryTree.show();

							} else if (req.status == 403) {
								window.location.href = req.getResponseHeader("Location");
								window.sessionStorage.removeItem('username');
							}
							else {
								self.alert.textContent = message;
							}
						}
					}
				);
				this.hide();
			} else {
				this.subdirForm.reportValidity();		//if the content of the form is not valid notify the user
			}
		}
		
		this.createDoc = function() {							//function to create document
			var self = this;
			this.docForm.children.fatherDirId.value = this.fatherDirectory;
			if (this.docForm.checkValidity()) {
				makeCall("POST", "CreateDocument", self.docForm,
					function(req) {
						if (req.readyState == 4) {
							var message = req.responseText;
							if (req.status == 200) {
								directoryTree.show();

							} else if (req.status == 403) {
								window.location.href = req.getResponseHeader("Location");
								window.sessionStorage.removeItem('username');
							}
							else {
								self.alert.textContent = message;
							}
						}
					}
				);
				this.hide();
			} else {
				this.docForm.reportValidity();		//if the content of the form is not valid notify the user
			}
		}
	}
	
	function Bin() {											//constructor of the bin object, used to delete elements
		this.binElement = document.getElementById("bin");		//reference to the bin element
		this.modalWindow = document.getElementById("modal");				//reference to the modal window
		this.warningMessage = document.getElementById("warningMessage");	//reference to the warning message
		
		this.draggingDocument;								//reference to the element I am currently dragging
		this.draggingDirectory;
		
		this.binElement.addEventListener("dragover", (e) => {		//add drag over event to paint the bin red
				e.preventDefault();
				e.target.classList.add("deleting");
			}, false);
			
		this.binElement.addEventListener("drop", (e) => {			//add drop event to delete element(s)
				e.stopPropagation();
				e.preventDefault();
				if (directoryTree.draggingDocument != undefined)		//if I am dragging a document, save the id
					this.draggingDocument = directoryTree.draggingDocument;
				else if (directoryTree.draggingDirectory != undefined)		//if I am dragging a directory, save the id
					this.draggingDirectory = directoryTree.draggingDirectory;
				e.target.classList.remove("deleting");									//Remove bin paint
				bin.showModal();
			}, false);
		this.binElement.addEventListener("dragleave", (e) => {			//add drag leave event to remove paint from the bin
				e.preventDefault();
				e.target.classList.remove("deleting");
			}, false);
			
		this.showModal = function() {					//function to show the modal window after an element was dropped in the bin
			this.modalWindow.style.visibility = "visible";
			this.modalWindow.style.display = "block";			//visualize window
			if (bin.draggingDocument != undefined)				//personalize message
				this.warningMessage.textContent = "Sei sicuro di voler eliminare il documento? L'azione è irreversibile!";
			else if (bin.draggingDirectory != undefined)
				this.warningMessage.textContent = "Sei sicuro di voler eliminare la cartella? L'azione è irreversibile e eliminerà tutti i suoi contenuti!";
		}
		
		this.hideModal = function() {							//function to hide modal window
			this.modalWindow.style.visibility = "hidden";
			this.modalWindow.style.display = "none";
		}
		
		this.confirmChoiche = document.getElementById("confirm");	//reference to "confirm" button of the modal window
		this.confirmChoiche.addEventListener("click", (e) => {		//add on click event to the button
			if (bin.draggingDocument != undefined)
				directoryTree.deleteDocument();						//if a document was dragged, call its delete function
			else if (bin.draggingDirectory != undefined)
				directoryTree.deleteDirectory();					//if a directory was dragged, call its delete function
				
			this.hideModal();										//hide the window after the click		
		});
		
		this.cancelChoiche = document.getElementById("cancel");	//reference to "cancel" button of the modal window
		this.cancelChoiche.addEventListener("click", (e) => {	//add on click event to the button
			directoryTree.draggingDocument = undefined;			//reset all the references to the dragged elements
			directoryTree.draggingDirectory = undefined;		//just to be sure but shouldn't be needed
			bin.draggingDocument = undefined;
			bin.draggingDirectory = undefined;
			this.hideModal();									//hide the window after the click
		});
	};

	function PageOrchestrator() {								//constructor of the page orchestrator object
		var alertContainer = document.getElementById("id_alert");		//get the id container

		this.start = function() {								//function to start all the necessary elements to make the page load
			welcomeMessage = new WelcomeMessage(sessionStorage.getItem('username'),
				document.getElementById("id_username"));		//create welcome message and show it
			welcomeMessage.show();

			directoryTree = new DirectoryTree(					//create elements' tree
				alertContainer,
				document.getElementById("topDir"));

			documentDetails = new DocumentDetails({ 			//create details object that will contain document details
				alert: alertContainer,										// many parameters, wrap them in an object
				name: document.getElementById("id_docName"),
				creationDate: document.getElementById("id_docCreationDate"),
				summary: document.getElementById("id_docSummary"),
				type: document.getElementById("id_docType"),
				creator: document.getElementById("id_docCreator"),
				fatherName: document.getElementById("id_docFatherDirectory")
			});
			
			wizard = new Wizard(alertContainer);				//create wizard and hide it
			wizard.hide();
			
			//SET EVENTS TO STATIC PAGE ELEMENTS
			document.querySelector("a[href='Logout']").addEventListener('click', () => {		//add on click event to logout
				window.sessionStorage.removeItem('username');
			});
			
			document.getElementById("addDir").addEventListener('click', () => {				//add on click event to addDir html element
				wizard.show("-1", 1);
			});
			
			document.getElementById("createDir").addEventListener('click', () => {			//add on click to create dir submit button
				wizard.createDir();
			});
			
			document.getElementById("createSubDir").addEventListener('click', () => {	//add on click to create sub dir submit button
				wizard.createSubd();
			});
			
			document.getElementById("createDoc").addEventListener('click', () => {		//add on click to create document submit button
				wizard.createDoc();
			});
			
			document.getElementById("hideForms").addEventListener('click', () => {		//add on click to hide forms button
				wizard.hide();
			});
			
			document.getElementById("hideDetails").addEventListener('click', () => {	//add on click to hide details button
				documentDetails.hide();
			});
			
			//CREATE BIN ELEMENT
			bin = new Bin();
			bin.hideModal(); 	//hide the modal window
		};

		this.refresh = function(currentDocument) { 				// currentDocument null at start but can be set when refresh is called
			alertContainer.textContent = "";					//reset alert
			directoryTree.reset();								//reset tree of elements
			directoryTree.show(function() {
				directoryTree.autoclick(currentDocument);		//call show tree and pass autoclick as next function
			}); // closure preserves visibility of this

		};
	}
}