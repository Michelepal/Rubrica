# rubricaJavaAngular

Progetto full stack per la gestione di una rubrica personale multiutente.

Nome progetto locale e repository GitHub:

```text
rubricaJavaAngular
```

L'applicazione prevede un frontend Angular, un backend Spring Boot con Gradle, autenticazione tramite Spring Security e database locale per la persistenza dei dati della rubrica.

## Obiettivo

L'obiettivo e' fornire una base applicativa completa per:

- autenticazione utente;
- gestione contatti personali;
- salvataggio di telefoni, email, indirizzi e tag;
- validazione dei dati lato frontend, backend e database;
- protezione delle API tramite Spring Security;
- codice leggibile, testabile e conforme ai principi SOLID.

## Architettura

La struttura prevista del repository e':

```text
frontend/
backend/
REQUISITI_RUBRICA_FULLSTACK.md
REQUISITI_RUBRICA_FULLSTACK.pdf
README.md
```

In ambiente di sviluppo:

```text
Frontend Angular: http://localhost:4200
Backend Spring Boot: http://localhost:8080
```

In modalita' produzione/integrata, la build statica Angular dovra' essere copiata in:

```text
backend/src/main/resources/static/
```

Spring Boot servira' Angular dal path base:

```text
http://localhost:8080/
```

Le API REST resteranno sotto:

```text
/api/**
```

Il backend dovra' prevedere un fallback SPA verso `index.html` per le rotte Angular come `/login`, `/home`, `/error` e `/contacts`, senza intercettare `/api/**`.

## Ambienti

Il progetto distingue tra sviluppo e produzione.

Sviluppo:

```text
Angular: http://localhost:4200
Spring Boot: http://localhost:8080
API Angular: http://localhost:8080/api
Profilo Spring: dev
```

Produzione/integrata:

```text
Spring Boot serve Angular da backend/src/main/resources/static/
API Angular: /api
Profilo Spring: prod
```

Backend:

```text
application.yml
application-dev.yml
application-prod.yml
```

Frontend:

```text
environment.ts
environment.development.ts
environment.production.ts
```

In sviluppo il backend dovra' abilitare CORS per `http://localhost:4200` oppure il frontend dovra' usare un proxy Angular. In produzione, se Angular e Spring Boot sono serviti dallo stesso host, le chiamate useranno path relativi e CORS non sara' necessario.

Logging:

- in ambiente `dev` i log saranno piu' dettagliati per supportare debug e diagnosi locale;
- in ambiente `prod` i log saranno piu' essenziali, controllati e privi di dati sensibili.

## Tecnologie

Frontend:

- Angular;
- Reactive Forms;
- Angular Router;
- HTTP interceptor;
- Auth guard.

Backend:

- Java;
- Spring Boot;
- Spring Security;
- Spring Web;
- Spring Data JPA;
- Gradle;
- H2 Database locale;
- JWT per autenticazione stateless.

## Versionamento

Ogni commit pubblicato su GitHub dovra' aggiornare la versione del programma sia lato frontend sia lato backend.

La versione frontend sara' mantenuta nel progetto Angular, ad esempio in `frontend/package.json`.

La versione backend sara' mantenuta secondo gli standard Gradle e Spring Boot, usando la proprieta' Gradle standard `version` in `backend/build.gradle` o `backend/build.gradle.kts`.

Il backend dovra' mantenere coordinate Gradle coerenti, ad esempio:

```text
group = 'it.example'
version = 'x.y.z'
```

Il nome progetto dovra' essere definito in `settings.gradle`, ad esempio:

```text
rootProject.name = 'rubricaJavaAngular'
```

La convenzione prevista e' Semantic Versioning:

```text
MAJOR.MINOR.PATCH
```

## Funzionalita' principali

- Redirect dal path base `/` verso `/login`.
- Login gestito tramite Spring Security.
- Redirect alla home Angular dopo login riuscito.
- Redirect a pagina di errore in caso di login fallito.
- CRUD dei contatti.
- Gestione di telefoni, email, indirizzi e tag.
- Modale di conferma prima di ogni modifica o cancellazione.
- Validazione e sanitizzazione dei dati.
- Isolamento dei dati per utente autenticato.
- Interfaccia responsive per mobile, tablet, laptop e desktop.
- Font e scala tipografica adeguati a leggibilita' e visibilita'.
- Switch tra modalita' chiara e modalita' scura.
- Criteri di accessibilita' e usabilita' per persone diversamente abili.
- Compatibilita' grafica con browser moderni come Chrome, Edge, Firefox, Safari e Brave.
- CSS organizzato senza conflitti, con regole globali separate dagli stili specifici dei componenti.
- Gestione completa degli errori REST lato frontend e backend con messaggi comprensibili per l'utente.

## Mockup grafico

Il mockup grafico da validare si trova in:

```text
mockup/index.html
```

Per avviarlo localmente:

```bash
cd mockup
node server.js
```

Poi aprire:

```text
http://localhost:4173
```

## Avvio frontend

I comandi definitivi saranno disponibili dopo la creazione del progetto Angular.

Indicativamente:

```bash
cd frontend
npm install
npm start
```

## Avvio backend

I comandi definitivi saranno disponibili dopo la creazione del progetto Spring Boot.

Indicativamente:

```bash
cd backend
./gradlew bootRun
```

Su Windows:

```bash
cd backend
gradlew.bat bootRun
```

## Test

Il progetto dovra' includere test per:

- validator frontend;
- servizi Angular;
- guard e interceptor;
- autenticazione backend;
- servizi backend;
- controller REST;
- repository e vincoli database;
- isolamento dei dati tra utenti.

Il codice Java backend e il codice JavaScript/TypeScript frontend dovranno essere progettati per ottenere la copertura massima possibile, usando report di coverage. Per il backend e' previsto l'uso di strumenti compatibili con Gradle come JaCoCo; per il frontend il coverage sara' generato dal test runner Angular scelto.

I test dovranno generare report consultabili sullo stato dell'applicazione, includendo test superati, falliti, saltati e copertura.

Percorsi consigliati:

```text
backend/build/reports/tests/
backend/build/reports/jacoco/
frontend/coverage/
```

## Sicurezza

Il progetto non deve pubblicare:

- password in chiaro;
- token;
- credenziali reali;
- database locali;
- file temporanei o build generate.

Le password devono essere salvate solo tramite hash BCrypt.

## Documentazione requisiti

I requisiti completi sono disponibili in:

- `REQUISITI_RUBRICA_FULLSTACK.md`
- `REQUISITI_RUBRICA_FULLSTACK.pdf`

Lo storico dei prompt usati per definire il progetto e' disponibile in:

- `PROMPT_STORICO.md`

Il piano di implementazione suddiviso in task piccoli e verificabili e' disponibile in:

- `TASK_IMPLEMENTAZIONE.md`

Il tracciamento delle modifiche e delle versioni e' disponibile in:

- `CHANGELOG.md`
