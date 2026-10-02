# XCORE

Plataforma XCORE: API, painel e players para gerenciamento de clientes, dispositivos, licenças e fontes de conteúdo autorizadas.

## Estrutura

- `backend/` — API Core
- `panel/` — painel web (próxima etapa)
- `roku/` — player Roku (próxima etapa)
- `database/` — migrations e documentação do banco
- `docs/` — documentação

## Backend

Stack inicial:
- Node.js
- TypeScript
- Fastify
- PostgreSQL (integração prevista)
- JWT (integração prevista)

Para desenvolvimento local:

```bash
cd backend
npm install
npm run dev
```

A API inicial expõe `GET /health`.
