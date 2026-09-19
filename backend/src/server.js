const http = require("http");
const https = require("https");
const crypto = require("crypto");

const PORT = Number(process.env.PORT || 8080);
const VERIFY_TOKEN = process.env.WEBHOOK_VERIFY_TOKEN || "";
const WHATSAPP_API_URL = process.env.WHATSAPP_API_URL || "";
const WHATSAPP_ACCESS_TOKEN = process.env.WHATSAPP_ACCESS_TOKEN || "";
const BACKEND_API_KEY = process.env.BACKEND_API_KEY || "";

const commands = new Map();
const conversations = new Map();

function json(res, status, body) {
  const data = JSON.stringify(body);
  res.writeHead(status, {"Content-Type": "application/json; charset=utf-8"});
  res.end(data);
}

function authorized(req) {
  if (!BACKEND_API_KEY) return false;
  return req.headers["x-xcore-api-key"] === BACKEND_API_KEY;
}

function normalize(value) {
  return String(value || "").trim().toLowerCase().replace(/\\s+/g, " ");
}

function matches(command, text) {
  if (!command.active || !command.pattern) return false;
  const value = normalize(text);
  const target = normalize(command.pattern);
  switch (command.matchType) {
    case "EXACT": return value === target;
    case "STARTS_WITH": return value.startsWith(target);
    case "KEYWORD": return value.split(/\\s+/).includes(target);
    case "CONTAINS":
    default: return value.includes(target);
  }
}

function findCommand(text) {
  for (const command of commands.values()) {
    if (matches(command, text)) return command;
  }
  return null;
}

function requestJson(urlString, method, payload, headers) {
  return new Promise((resolve, reject) => {
    const url = new URL(urlString);
    const body = payload == null ? "" : JSON.stringify(payload);
    const transport = url.protocol === "https:" ? https : http;
    const req = transport.request(url, {
      method,
      headers: {
        "Content-Type": "application/json",
        "Content-Length": Buffer.byteLength(body),
        ...headers
      }
    }, (res) => {
      let data = "";
      res.setEncoding("utf8");
      res.on("data", chunk => data += chunk);
      res.on("end", () => {
        let parsed = {};
        try { parsed = data ? JSON.parse(data) : {}; } catch (_) { parsed = {raw: data}; }
        resolve({status: res.statusCode || 0, body: parsed});
      });
    });
    req.on("error", reject);
    req.write(body);
    req.end();
  });
}

async function sendWhatsAppText(phone, text) {
  if (!WHATSAPP_API_URL || !WHATSAPP_ACCESS_TOKEN) {
    return {ok: false, error: "WhatsApp API não configurada no backend"};
  }
  const result = await requestJson(WHATSAPP_API_URL, "POST", {
    messaging_product: "whatsapp",
    to: phone,
    type: "text",
    text: {body: text}
  }, {
    Authorization: "Bearer " + WHATSAPP_ACCESS_TOKEN
  });
  return {ok: result.status >= 200 && result.status < 300, ...result};
}

function extractMessages(payload) {
  const output = [];
  const entries = Array.isArray(payload?.entry) ? payload.entry : [];
  for (const entry of entries) {
    for (const change of (entry.changes || [])) {
      const value = change.value || {};
      for (const message of (value.messages || [])) {
        if (message.type === "text" && message.text?.body) {
          output.push({
            messageId: message.id || crypto.randomUUID(),
            phone: message.from || "",
            text: message.text.body
          });
        }
      }
    }
  }
  return output;
}

async function processMessage(message) {
  const phone = message.phone;
  if (!phone) return;
  const active = conversations.get(phone);
  const command = active ? commands.get(active.commandId) : findCommand(message.text);

  if (!command) {
    // Sem comando correspondente: ignora a mensagem.
    return;
  }

  if (!active) {
    conversations.set(phone, {
      commandId: command.id,
      flowId: command.flowId || "",
      step: 0,
      updatedAt: Date.now()
    });
  }

  if (command.response) {
    await sendWhatsAppText(phone, command.response);
  }
  if (command.question && !active) {
    await sendWhatsAppText(phone, command.question);
  }

  conversations.set(phone, {
    commandId: command.id,
    flowId: command.flowId || "",
    step: (active?.step || 0) + 1,
    updatedAt: Date.now()
  });
}

function readBody(req) {
  return new Promise((resolve, reject) => {
    let data = "";
    req.on("data", chunk => {
      data += chunk;
      if (data.length > 1024 * 1024) req.destroy();
    });
    req.on("end", () => resolve(data));
    req.on("error", reject);
  });
}

const server = http.createServer(async (req, res) => {
  try {
    const url = new URL(req.url, "http://" + (req.headers.host || "localhost"));

    if (req.method === "GET" && url.pathname === "/health") {
      return json(res, 200, {ok: true, service: "xcore-backend"});
    }

    if (req.method === "GET" && url.pathname === "/webhook/whatsapp") {
      const mode = url.searchParams.get("hub.mode");
      const token = url.searchParams.get("hub.verify_token");
      const challenge = url.searchParams.get("hub.challenge");
      if (mode === "subscribe" && token && token === VERIFY_TOKEN && challenge) {
        res.writeHead(200, {"Content-Type": "text/plain"});
        return res.end(challenge);
      }
      return json(res, 403, {error: "Verificação inválida"});
    }

    if (req.method === "POST" && url.pathname === "/webhook/whatsapp") {
      const body = await readBody(req);
      let payload;
      try { payload = JSON.parse(body); } catch (_) {
        return json(res, 400, {error: "JSON inválido"});
      }
      for (const message of extractMessages(payload)) {
        processMessage(message).catch(err => console.error("processMessage:", err));
      }
      return json(res, 200, {received: true});
    }

    if (req.method === "GET" && url.pathname === "/api/commands") {
      if (!authorized(req)) return json(res, 401, {error: "Não autorizado"});
      return json(res, 200, {commands: Array.from(commands.values())});
    }

    if (req.method === "POST" && url.pathname === "/api/commands") {
      if (!authorized(req)) return json(res, 401, {error: "Não autorizado"});
      const body = await readBody(req);
      let command;
      try { command = JSON.parse(body); } catch (_) {
        return json(res, 400, {error: "JSON inválido"});
      }
      if (!command.id || !command.pattern) {
        return json(res, 400, {error: "id e pattern são obrigatórios"});
      }
      commands.set(command.id, {
        id: String(command.id),
        name: String(command.name || command.id),
        matchType: command.matchType || "CONTAINS",
        pattern: String(command.pattern),
        flowId: String(command.flowId || ""),
        response: String(command.response || ""),
        question: String(command.question || ""),
        active: command.active !== false
      });
      return json(res, 200, {ok: true});
    }

    if (req.method === "DELETE" && url.pathname.startsWith("/api/commands/")) {
      if (!authorized(req)) return json(res, 401, {error: "Não autorizado"});
      const id = decodeURIComponent(url.pathname.slice("/api/commands/".length));
      commands.delete(id);
      return json(res, 200, {ok: true});
    }

    if (req.method === "POST" && url.pathname === "/api/conversations/clear") {
      if (!authorized(req)) return json(res, 401, {error: "Não autorizado"});
      conversations.clear();
      return json(res, 200, {ok: true});
    }

    return json(res, 404, {error: "Rota não encontrada"});
  } catch (error) {
    console.error(error);
    return json(res, 500, {error: "Erro interno"});
  }
});

server.listen(PORT, () => {
  console.log("XCORE backend ativo na porta " + PORT);
});
