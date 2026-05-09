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
