# Changelog

Tutte le modifiche rilevanti del progetto `rubricaJavaAngular` saranno tracciate in questo file.

Il progetto segue Semantic Versioning:

```text
MAJOR.MINOR.PATCH
```

Ogni task completato deve prevedere:

- aggiornamento versione frontend;
- aggiornamento versione backend;
- aggiornamento di questo changelog;
- commit finale.

## [Non rilasciato]

## [1.1.0] - 2026-05-11

### Aggiunto

- Aggiunta paginazione a contatti e tag con limite massimo di 10 risultati per pagina.
- Aggiunta gestione preferiti con conferma dedicata e filtri per preferiti e tag.
- Aggiunti filtri tag per nome e colore con selezione visuale dei colori presenti.
- Aggiunti toast centrati a scomparsa per messaggi di conferma ed errore.
- Aggiunti tooltip sui pulsanti operativi.
- Estesi i test backend e frontend per paginazione, filtri, preferiti, contatti da verificare e UI dei filtri.

### Corretto

- Segnalati graficamente i contatti da verificare quando mancano email o telefono.
- Reso neutro il pulsante filtro "Tutti" nella selezione colori.
- Corrette le aspettative HTTP nei test backend per creazione e modifica contatto.

## [1.0.1] - 2026-05-11

### Corretto

- Ripristinata la navigazione mobile con una bottom tab bar fissa in stile iOS.
- Migliorato il layout mobile dei contatti evitando sovrapposizioni dei testi lunghi nelle righe.
- Riallineate le dimensioni dei caratteri di menu, card contatto e maschera di modifica su mobile.

## [1.0.0] - 2026-05-10

### Aggiunto

- Preparata la build statica per GitHub Pages con modalità demo locale nel browser.
- Abilitato l'uso dell'app pubblicata senza backend Spring su GitHub Pages.

### Cambiato

- Aggiornata la versione stabile del progetto a `1.0.0`.

## [0.4.2] - 2026-05-10

### Aggiunto

- Aggiunti test d'integrazione backend per login, protezione API, validazione, modifica contatto e conflitto/cancellazione tag.
- Aggiunti test frontend per login, messaggi di errore dismissibili, placeholder contestuali, filtro tag, modifica contatto, tema e logout.
- Inclusi i nuovi test backend nella suite JUnit esistente.

### Corretto

- Corretta la modifica contatto: il form si chiude dopo il salvataggio e la lista viene aggiornata.
- Aggiunta gestione utente degli errori CRUD con messaggi visibili e cancellabili.
- Corretti i path variable espliciti nei controller backend per evitare errori runtime Spring.
- Migliorati logging e gestione errori su backend e frontend.
- Corretti accenti nei messaggi utente e ripuliti warning Java di null-safety nella suite backend.

## [0.4.1] - 2026-05-10

### Corretto

- Migliorato il contrasto dei badge tag in modalita' scura con sfondo piu' chiaro, testo scuro e bordo dedicato.

## [0.4.0] - 2026-05-10

### Cambiato

- Rifinito il layout responsive della home con contatori sempre in riga e tabella contatti a larghezza piena.
- Mantenuta la tabella "Contatti recenti" in formato tabellare su tutti i breakpoint con testi compatti su una riga.
- Semplificata la dashboard rimuovendo sezione tag e pulsanti di creazione dalla home.
- Migliorata l'azione tema con icona dinamica, tooltip e label accessibile.
- Migliorata la gestione colore dei tag con color picker, palette predefinita e layout piu' ordinato.

### Corretto

- I contatti vengono renderizzati subito al caricamento della pagina senza richiedere click o refresh manuali.

## [0.3.8] - 2026-05-10

### Cambiato

- Le voci laterali Contatti e Tag aprono pagine dedicate invece di restare nella dashboard.
- La pagina Tag carica solo i tag, riducendo le chiamate non necessarie.
- Ridotto il logging SQL in sviluppo per rendere piu' rapido e leggibile il caricamento locale.

## [0.3.7] - 2026-05-10

### Cambiato

- La modifica di un contatto chiude il form inline dopo il salvataggio.
- La creazione di un nuovo contatto si apre in una modale sopra il contenuto centrale.
- I tag associati al contatto si selezionano con una select multipla invece che con checkbox.

## [0.3.6] - 2026-05-10

### Corretto

- Corretto il menu laterale Angular: i collegamenti a contatti e tag scorrono nella pagina senza uscire dalla sessione.
- Aggiunta la route Angular `/tags` per evitare redirect al login su navigazione diretta.
- Reso piu' robusto il seed dei dati demo in sviluppo quando nel DB locale esistono gia' tag ma non contatti.

## [0.3.5] - 2026-05-10

### Corretto

- Rimossi gli avvisi IDE legati a null-analysis JDT nei test backend.
- Semplificata la gestione dei risultati Spring Data evitando conversioni nullability non necessarie nei servizi e nell'handler REST.

## [0.3.4] - 2026-05-10

### Cambiato

- Riallineata la home Angular al mockup dashboard con riepilogo, contatti recenti e sezione tag.
- Sostituite le lettere usate come azioni con pulsanti a icona SVG accessibili.
- Spostato il form contatto in modalita' inline a scomparsa tra le righe della lista contatti.
- La modifica dei tag associati al contatto avviene dentro il form del record tramite selezione dei tag disponibili.

## [0.3.3] - 2026-05-09

### Corretto

- Collegata la pagina Angular della rubrica alle API reali dei contatti.
- La modifica di un contatto ora invia una richiesta `PUT /api/contacts/{id}` dopo conferma in modale e aggiorna subito la lista.
- Aggiunti dati demo nel profilo `dev` per verificare immediatamente modifica e cancellazione su record reali.
- Aggiunto test frontend per verificare la chiamata REST di aggiornamento contatto.

## [0.3.2] - 2026-05-09

### Corretto

- Corretta configurazione CORS backend per consentire al frontend Angular in sviluppo (`localhost:4200`) di chiamare le API su `localhost:8080`.

## [0.3.1] - 2026-05-09

### Corretto

- Corretti i launcher Windows per avviare backend con profilo `dev` e frontend con Node/npm locale nel `PATH`.

## [0.3.0] - 2026-05-09

### Cambiato

- Installato Node/npm locale al progetto per eseguire i task Angular e aggiunto `.node-version`.
- Aggiornato Angular, Angular CLI e build tooling a versioni prive di vulnerabilita' note da `npm audit`.
- Generato `package-lock.json` per rendere riproducibile l'installazione frontend.
- Configurato l'esclusione degli artifact statici Angular copiati nel backend.

### Verificato

- Frontend: `npm audit --audit-level=high` completato con `0 vulnerabilities`.
- Frontend: `npm run build` completato con successo.
- Frontend: `npm test -- --watch=false --code-coverage` completato con `4 SUCCESS`.
- Frontend: report coverage generato in `frontend/coverage/`.
- Integrazione: `npm run copy-to-backend` ha copiato la build Angular in `backend/src/main/resources/static`.

## [0.2.0] - 2026-05-09

### Aggiunto

- Scaffold backend Spring Boot con Gradle, profili `dev` e `prod`, logging differenziato e JaCoCo.
- Entity JPA, repository, DTO, mapper, gestione errori REST, autenticazione JWT e API contatti/tag.
- Scaffold frontend Angular con routing, login, home rubrica, pagina errore, tema chiaro/scuro, modale di conferma e CSS organizzato per file globali/componenti.
- Script per copiare la build Angular nel backend Spring Boot.
- Test backend di caricamento contesto e test frontend base per tema ed error mapping.

### Verificato

- Backend: `.\gradlew.bat clean test` completato con successo e report JaCoCo generato.
- Frontend: build/test non eseguiti per assenza di `npm` nel PATH dell'ambiente Windows corrente.

### Documentazione

- Definiti requisiti funzionali, tecnici, grafici, accessibilita', test, ambienti e piano task.
