# XCORE Backend

Este serviço é usado somente para sincronizar os comandos/configurações do XCORE.

O WhatsApp **não passa pelo backend**. O aplicativo Android captura as mensagens do WhatsApp Business usando `NotificationListenerService` e responde usando a ação de resposta (`RemoteInput`) da própria notificação.

## Variáveis

- `PORT`
- `BACKEND_API_KEY`

## Rotas

- GET /health
- GET /api/commands — listar comandos
- POST /api/commands — criar/atualizar comando
- DELETE /api/commands/:id — remover comando

O app usa `X-XCORE-API-KEY` somente para sincronizar as configurações dos comandos.

## WhatsApp no Android

Para o bot funcionar, o usuário precisa conceder ao XCORE o acesso às notificações em **Configurações do Android → Acesso a notificações**.

O XCORE observa as notificações do WhatsApp Business (`com.whatsapp.w4b`) e também do WhatsApp comum (`com.whatsapp`). Quando uma notificação contém uma ação de resposta, o XCORE pode usar essa ação para enviar a resposta sem usar a Cloud API da Meta.
