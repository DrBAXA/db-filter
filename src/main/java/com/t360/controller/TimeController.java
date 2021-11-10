package com.t360.controller;

import io.javalin.http.Context;
import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsContext;
import lombok.SneakyThrows;

import java.io.File;
import java.nio.file.Files;
import java.time.Instant;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TimeController {

    CopyOnWriteArrayList<WsContext> clients = new CopyOnWriteArrayList<>();
    private String pageCache;

    public void initiate() {
        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(this::notifyClients, 0, 10, TimeUnit.SECONDS);
    }

    @SneakyThrows
    public void welcomePage(Context context) {
        if (pageCache == null) pageCache = new String(Files.readAllBytes(new File("welcome.html").toPath()));
        context.result(pageCache);
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
        String ms = Instant.now().toEpochMilli() + "";
        clients.forEach(ctx -> ctx.send(ms));
    }
}
