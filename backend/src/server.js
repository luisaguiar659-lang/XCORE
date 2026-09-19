const http = require("http");

const PORT = Number(process.env.PORT || 8080);
const BACKEND_API_KEY = process.env.BACKEND_API_KEY || "";

const commands = new Map();

function json(res, status, body) {
  const data = JSON.stringify(body);
  res.writeHead(status, {"Content-Type": "application/json; charset=utf-8"});
  res.end(data);
}

function authorized(req) {
  if (!BACKEND_API_KEY) return false;
  return req.headers["x-xcore-api-key"] === BACKEND_API_KEY;
}

const server = http.createServer(async (req, res) => {
  try {
    const url = new URL(req.url, "http://" + (req.headers.host || "localhost"));

    if (req.method === "GET" && url.pathname === "/health") {
      return json(res, 200, {
        ok: true,
        service: "xcore-backend",
        whatsappTransport: "android-notification"
      });
    }

    if (req.method === "GET" && url.pathname === "/api/commands") {
      if (!authorized(req)) return json(res, 401, {error: "Não autorizado"});
      return json(res, 200, {commands: Array.from(commands.values())});
    }

    if (req.method === "POST" && url.pathname === "/api/commands") {
      if (!authorized(req)) return json(res, 401, {error: "Não autorizado"});

      let body = "";
      for await (const chunk of req) {
        body += chunk;
        if (body.length > 1024 * 1024) {
          return json(res, 413, {error: "Payload muito grande"});
        }
      }

      let command;
      try {
        command = JSON.parse(body);
      } catch (_) {
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

    return json(res, 404, {error: "Rota não encontrada"});
  } catch (error) {
    console.error(error);
    return json(res, 500, {error: "Erro interno"});
  }
});

server.listen(PORT, () => {
  console.log("XCORE backend ativo na porta " + PORT + " (somente configuração)");
});
