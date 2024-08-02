# TIWproject
## Progetto del corso di Tecnologie Informatiche per il Web
[English version here](readmeEN.txt)

## Svolgimento traccia numero 2
Puoi trovare le tracce nel file dei [progetti](Progetti_TIW_2023_2024.pdf).

Per la guida completa all'installazione dei tools e sul funzionamento del progetto fare riferimento a [quella](TIW___Guida_Installazione__2023_2024_.pdf) fornita dal professor Fraternali.

## Documentazione
La documentazione spiega le scelte progettuali sia per la parte [HTML Pure](Documentazione_HTML_PURE.pptx) che per la parte [RIA](Documentazione_RIA.pptx).

## ERRORE RISCONTRATO
Nell'eliminazione ricorsiva di cartelle e documenti dal database non viene controllato il caso in cui ci sia un'eccezione dovuta a un errore di SQL; è risolvibile disattivando l'auto-commit di SQL in modo tale che sia possibile fare rollback del database in caso di eccezione.
