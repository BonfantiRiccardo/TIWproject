Project for the Tecnologie Informatiche per il Web course

For the project I had to develop the requirements of track number 2.
You can find all the projects tracks here: Progetti_TIW_2023_2024.pdf

For the complete tools' installation and setup guide please refer to the one given by professor Fraternali: TIW___Guida_Installazione__2023_2024_.pdf

Documentation
The documentation explains all the design choices both for the HTML Pure part and the Rich internet application part.

KNOWN ISSUE
When recursively deleting subfolders and documents from a folder, the code checks if an SQL exception occurs but does not rollback to the previous state of the database in case one of the deletion fails. It should be easily fixable by deactivating via java code the database auto-commit and by rolling back / manually committing at the end.
