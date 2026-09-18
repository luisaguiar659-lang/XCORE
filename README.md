# XCORE

Sistema de automação para atendimento e provisionamento de clientes.

## Fluxo planejado

1. Receber mensagens do WhatsApp Business.
2. Interpretar o pedido e os dados do cliente.
3. Gerar/testar o acesso no painel Masterflix.
4. Ativar MEC e obter o M3U.
5. Provisionar o M3U no painel XCloud e ativar MEC.
6. Provisionar o M3U no painel GerênciaApp e ativar MEC.
7. Responder o cliente no WhatsApp com o resultado.

## Arquitetura

- **WhatsApp**: entrada e resposta de mensagens.
- **Orchestrator**: coordena o fluxo e controla estados/retries.
- **Masterflix**: criação/teste e geração de dados.
- **XCloud**: provisionamento do M3U + MEC.
- **GerênciaApp**: provisionamento do M3U + MEC.
- **Audit/Logs**: rastreabilidade sem registrar segredos.

As integrações externas devem usar APIs oficiais quando disponíveis. Credenciais e tokens ficam somente em variáveis de ambiente/secret manager.

## Status

Estrutura inicial criada. As integrações ainda precisam das URLs, métodos de autenticação e regras de cada painel.
