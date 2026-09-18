# XCORE

Aplicativo desktop instalável para orquestrar automações de atendimento e provisionamento.

## Primeiro build

A versão **0.2.0** já contém:
- Aplicativo desktop Electron.
- Dashboard visual.
- Navegação por Dashboard, Integrações, Automação e Configurações.
- Modo **Demo** para testar o fluxo sem acessar serviços externos.
- Configuração local.
- Estrutura separada para integrações.
- Pipeline GitHub Actions para gerar instalador Windows (.exe).

## Fluxo planejado

1. Receber mensagem do WhatsApp Business.
2. Interpretar o pedido e os dados do cliente.
3. Gerar/testar o acesso no Masterflix.
4. Ativar MEC e obter M3U.
5. Provisionar M3U + MEC no XCloud.
6. Provisionar M3U + MEC no GerênciaApp.
7. Responder o cliente pelo WhatsApp.

## Estrutura

```
XCORE/
├── app/
│   ├── main/
│   ├── preload/
│   └── renderer/
├── core/
│   ├── config.js
│   └── orchestrator.js
├── integrations/
├── .github/workflows/
└── package.json
```

## Segurança

Credenciais, tokens, cookies e senhas não devem ser commitados no GitHub. As integrações de produção devem usar endpoints e métodos de autenticação autorizados pelos respectivos serviços.

## Build Windows

No ambiente local:
```bash
npm install
npm run build:win
```

O instalador será gerado em `release/`.

Também existe o workflow **Build XCORE Windows** em GitHub Actions. Ele pode ser executado manualmente pelo GitHub Actions e publica o instalador como artifact.

## Status

**Base do aplicativo:** pronta.

**Integrações reais:** aguardando as URLs, documentação/API, método de autenticação e campos exigidos por WhatsApp Business, Masterflix, XCloud e GerênciaApp. Não foram inventados endpoints.
