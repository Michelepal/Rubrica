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
