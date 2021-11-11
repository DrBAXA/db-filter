package com.t360;

import com.t360.controller.TimeController;
import io.javalin.Javalin;

import static io.javalin.apibuilder.ApiBuilder.*;

public class TimeSyncApp {

    public static void main(String[] args) {
        TimeController controller = new TimeController();
        controller.initiate();
        Javalin.create()
                .routes(() -> {
                    path("/", () -> {
                        get(controller::welcomePage);
                        ws("/ws", controller::wsHandle);
                    });
                    path("/time-sync.js", () -> {
                        get(controller::js);
                    });
                }).start(8080);
    }
}