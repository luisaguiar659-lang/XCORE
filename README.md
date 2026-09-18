# XCORE

Aplicativo **Android instalável** para gerenciar a automação de atendimento e provisionamento do XCORE.

## Versão Android

A versão **0.3.0** inicia a migração para Android nativo:
- Aplicativo Android com interface nativa.
- Dashboard, Automação e Configurações.
- Modo Demo para validar o fluxo sem acessar serviços externos.
- Estrutura preparada para integrações autorizadas.
- GitHub Actions gera um APK de teste (debug).

## Fluxo planejado

1. Receber mensagem do WhatsApp Business.
2. Interpretar o pedido e os dados do cliente.
3. Gerar/testar o acesso no Masterflix.
4. Ativar MEC e obter M3U.
5. Provisionar M3U + MEC no XCloud.
6. Provisionar M3U + MEC no GerênciaApp.
7. Responder o cliente pelo WhatsApp.

## Estrutura

XCORE/app/src/main/ contém a aplicação Android nativa.

## Build Android

O workflow **Build XCORE Android** instala o SDK Android e gera o APK em app/build/outputs/apk/debug/. O APK será disponibilizado como artifact **XCORE-Android**.

## Segurança

Credenciais, tokens, cookies e senhas não devem ser commitados no GitHub. As integrações de produção devem usar endpoints e métodos de autenticação autorizados pelos respectivos serviços.

## Status

**Aplicativo Android base:** em construção e preparado para o primeiro APK.

**Integrações reais:** aguardando URLs, documentação/API, método de autenticação e campos exigidos por WhatsApp Business, Masterflix, XCloud e GerênciaApp. Não foram inventados endpoints.
