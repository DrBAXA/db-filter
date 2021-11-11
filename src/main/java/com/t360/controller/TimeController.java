package com.t360.controller;

import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsContext;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TimeController {

    CopyOnWriteArrayList<WsContext> clients = new CopyOnWriteArrayList<>();
    private String pageCache;
    private String jsCache;

    public void initiate() {
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(this::notifyClients, 0, 1, TimeUnit.SECONDS);
    }

    @SneakyThrows
    public void welcomePage(Context context) {
        if (pageCache == null) {
            pageCache = readFile("welcome.html");
        }
        context.contentType(ContentType.TEXT_HTML);
        context.result(pageCache);
    }

    @SneakyThrows
    public void js(Context context) {
        if (jsCache == null) jsCache = readFile("time-sync.js");
        context.contentType(ContentType.JAVASCRIPT);
        context.result(jsCache);
    }

    @NotNull
    private String readFile(String fileName) throws URISyntaxException, IOException {
        final URL url = Objects.requireNonNull(getClass().getClassLoader().getResource(fileName));
        final Path file = Paths.get(url.toURI());
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }

    public void wsHandle(WsConfig wsConfig) {
        wsConfig.onConnect(wsCtx -> {
            clients.add(wsCtx);
            System.out.println("New client connected: " + wsCtx.getSessionId());
        });
        wsConfig.onClose(wsCtx -> {
            clients.remove(wsCtx);
            System.out.println("New client disconnected: " + wsCtx.getSessionId());
        });

    }

    public void notifyClients() {
        try {
            final ByteBuffer data = ByteBuffer.wrap(new byte[8]);
            data.asLongBuffer().put(System.currentTimeMillis());
            clients.forEach(ctx -> ctx.send(data));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
