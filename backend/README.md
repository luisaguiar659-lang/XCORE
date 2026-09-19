# XCORE Backend

Este serviço recebe o webhook do WhatsApp Business, encontra comandos configurados e envia respostas pelo canal oficial.

## Variáveis obrigatórias

- WEBHOOK_VERIFY_TOKEN
- WHATSAPP_API_URL
- WHATSAPP_ACCESS_TOKEN
- BACKEND_API_KEY

O token do WhatsApp fica somente no servidor. Não coloque esse segredo no APK.

## Rotas

- GET /health
- GET /webhook/whatsapp — verificação do webhook
- POST /webhook/whatsapp — entrada de mensagens
- GET /api/commands — listar comandos
- POST /api/commands — criar/atualizar comando
- DELETE /api/commands/:id — remover comando
- POST /api/conversations/clear — limpar estados

O app deverá usar X-XCORE-API-KEY para sincronizar os comandos.

## Observação

A URL exata da Graph API é configurada por WHATSAPP_API_URL para não fixar uma versão da API no código. Preencha essa variável com o endpoint oficial da sua conta Meta.
