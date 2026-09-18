# Integration adapters

Create one adapter per external service:

- whatsapp-business.js
- masterflix.js
- xcloud.js
- gerenciaapp.js

Each adapter should expose a small interface to the orchestrator and keep
authentication, HTTP details, retries and response mapping inside the adapter.

Do not commit API tokens, cookies, passwords or panel credentials.
