package xyz.zlatanov.frakkintoasters.backend.live;

import lombok.SneakyThrows;
import lombok.val;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import xyz.zlatanov.frakkintoasters.backend.journal.JournalCodec;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

final class TestClient implements AutoCloseable {

    private static final JsonNodeFactory NODES = JsonNodeFactory.instance;

    private final CookieManager                cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
    private final HttpClient                   http    = HttpClient.newBuilder().cookieHandler(cookies).build();
    private final LinkedBlockingQueue<JsonNode> inbox   = new LinkedBlockingQueue<>();
    // the chat's messages, apart from the game's, so a test of the game doesn't see them
    private final LinkedBlockingQueue<JsonNode> chats   = new LinkedBlockingQueue<>();
    private final List<JsonNode>               received = new ArrayList<>();
    private       int                          port;
    private       WebSocket                    socket;
    private       String                       token;
    private       JsonNode                     game;
    private       CompletableFuture<Integer>   closeCode = new CompletableFuture<>();

    TestClient(int port) {
        this.port = port;
    }

    UUID login(String name) {
        get("/api/login/providers");
        return UUID.fromString(post("/api/dev/login", "{\"name\":\"" + name + "\"}").get("id").stringValue());
    }

    @SneakyThrows
    JsonNode get(String path) {
        val response = http.send(HttpRequest.newBuilder(uri("http", path)).GET().build(), HttpResponse.BodyHandlers.ofString());
        return check(response);
    }

    @SneakyThrows
    JsonNode post(String path, String json) {
        val response = http.send(HttpRequest.newBuilder(uri("http", path))
                .header("Content-Type", "application/json")
                .header("X-XSRF-TOKEN", cookie("XSRF-TOKEN"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build(), HttpResponse.BodyHandlers.ofString());
        return check(response);
    }

    @SneakyThrows
    void connect(UUID game, int serverPort) {
        inbox.clear(); // what the old connection had left
        chats.clear();
        closeCode = new CompletableFuture<>();
        port = serverPort;
        val cookieHeader = cookies.getCookieStore().getCookies().stream()
                .map(cookie -> cookie.getName() + "=" + cookie.getValue())
                .collect(Collectors.joining("; "));
        socket = HttpClient.newHttpClient().newWebSocketBuilder()
                .header("Cookie", cookieHeader)
                .header("Origin", "http://localhost:" + port)
                .buildAsync(uri("ws", "/ws/games/" + game), new Listener())
                .get(10, TimeUnit.SECONDS);
    }

    void connect(UUID game) {
        connect(game, port);
    }

    void send(JsonNode message) {
        socket.sendText(JournalCodec.stringify(message), true).join();
    }

    void submit(JsonNode event) {
        val message = NODES.objectNode();
        message.put("type", "submit");
        message.put("token", token);
        message.set("event", event);
        send(message);
    }

    void submit(String withToken, JsonNode event) {
        val message = NODES.objectNode();
        message.put("type", "submit");
        message.put("token", withToken);
        message.set("event", event);
        send(message);
    }

    void choose(int option) {
        val message = NODES.objectNode();
        message.put("type", "choose");
        message.put("token", token);
        message.put("option", option);
        send(message);
    }

    @SneakyThrows
    JsonNode next(String type) {
        val message = inbox.poll(10, TimeUnit.SECONDS);
        if (message == null) {
            throw new AssertionError("No " + type + " message came");
        }
        read(message);
        if (!message.get("type").stringValue().equals(type)) {
            throw new AssertionError("Expected a " + type + " message, got " + message);
        }
        return message;
    }

    void say(String text) {
        val message = NODES.objectNode();
        message.put("type", "say");
        message.put("text", text);
        send(message);
    }

    @SneakyThrows
    JsonNode nextChat(String type) {
        val message = chats.poll(10, TimeUnit.SECONDS);
        if (message == null) {
            throw new AssertionError("No " + type + " message came");
        }
        if (!message.get("type").stringValue().equals(type)) {
            throw new AssertionError("Expected a " + type + " message, got " + message);
        }
        return message;
    }

    @SneakyThrows
    void assertNoChat() {
        val message = chats.poll(500, TimeUnit.MILLISECONDS);
        if (message != null) {
            throw new AssertionError("No chat message should have come, but got " + message);
        }
    }

    @SneakyThrows
    JsonNode nextGame() {
        while (true) {
            val message = inbox.poll(10, TimeUnit.SECONDS);
            if (message == null) {
                throw new AssertionError("No game message came");
            }
            if (message.get("type").stringValue().equals("rejected")) {
                throw new AssertionError("Rejected: " + message);
            }
            read(message);
            if (message.get("type").stringValue().equals("game")) {
                return message;
            }
        }
    }

    @SneakyThrows
    void assertNothingCame() {
        val message = inbox.poll(500, TimeUnit.MILLISECONDS);
        if (message != null) {
            throw new AssertionError("Nothing should have come, but got " + message);
        }
    }


    private void read(JsonNode message) {
        if (message.get("type").stringValue().equals("game")) {
            token = message.get("token").stringValue();
            game = message.get("game");
        }
    }

    void awaitToken(String expected) {
        while (!expected.equals(token)) {
            nextGame();
        }
    }

    JsonNode game() {
        return game;
    }

    @SneakyThrows
    int awaitClose() {
        return closeCode.get(10, TimeUnit.SECONDS);
    }

    boolean loggedIn() {
        return cookies.getCookieStore().getCookies().stream().anyMatch(cookie -> cookie.getName().equals("SESSION"));
    }
    String token() {
        return token;
    }

    List<JsonNode> received() {
        synchronized (received) {
            return List.copyOf(received);
        }
    }

    @Override
    public void close() {
        if (socket != null) {
            socket.abort();
            socket = null;
        }
    }

    private String cookie(String name) {
        return cookies.getCookieStore().getCookies().stream()
                .filter(cookie -> cookie.getName().equals(name))
                .map(java.net.HttpCookie::getValue)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No " + name + " cookie"));
    }

    private URI uri(String scheme, String path) {
        return URI.create(scheme + "://localhost:" + port + path);
    }

    private static JsonNode check(HttpResponse<String> response) {
        if (response.statusCode() >= 300) {
            throw new AssertionError(response.statusCode() + " " + response.body());
        }
        return response.body().isEmpty() ? NODES.nullNode() : JournalCodec.parse(response.body());
    }

    private final class Listener implements WebSocket.Listener {

        private final StringBuilder partial = new StringBuilder();

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            partial.append(data);
            if (last) {
                val message = (ObjectNode) JournalCodec.parse(partial.toString());
                partial.setLength(0);
                synchronized (received) {
                    received.add(message);
                }
                if (message.get("type").stringValue().startsWith("chat")) {
                    chats.add(message);
                } else {
                    inbox.add(message);
                }
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            closeCode.complete(statusCode);
            return null;
        }
    }

}
